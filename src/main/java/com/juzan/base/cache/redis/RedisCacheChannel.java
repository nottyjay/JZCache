package com.juzan.base.cache.redis;

import com.juzan.base.cache.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.Jedis;
import redis.clients.util.SafeEncoder;

import java.net.SocketTimeoutException;
import java.util.List;

/**
 * ====================================================================
 * 上海聚攒软件开发有限公司
 * --------------------------------------------------------------------
 *
 * @author Nottyjay
 * @version 1.0.beta
 * @since 1.0-beta
 * ====================================================================
 */
public class RedisCacheChannel extends BinaryJedisPubSub implements CacheChannel, CacheExpiredListener {

    private static final Logger LOG = LoggerFactory.getLogger(RedisCacheChannel.class);

    private String name;
    private static String channel = JZCache.getConfig().getProperty("redis.channel_name");
    private final static RedisCacheChannel instance = new RedisCacheChannel("default");
    private final Thread thread_subscribe;

    public static final RedisCacheChannel getInstance(){
        return instance;
    }

    public RedisCacheChannel(String name) {
        this.name = name;
        try {
            long ct = System.currentTimeMillis();
            CacheManager.initCacheProvider(this);

            thread_subscribe = new Thread(new Runnable() {
                @Override
                public void run() {
                    try (Jedis jedis = RedisCacheProvider.getResource()) {
                        jedis.subscribe(RedisCacheChannel.this, SafeEncoder.encode(channel));
                    }
                }
            });

            thread_subscribe.start();

            LOG.info("Connected to channel:" + this.name + ", time " + (System.currentTimeMillis() - ct) + " ms.");

        } catch (Exception e) {
            throw new CacheException(e);
        }
    }

    /**
     * 读取缓存中的数据
     * @param region: Cache Region name
     * @param key: Cache key
     * @return
     */
    @Override
    public CacheObject get(String region, Object key) {
        CacheObject obj = new CacheObject();
        if (region != null && key != null) {
            obj.setRegion(region);
            obj.setKey(key);
            obj.setValue(CacheManager.get(LEVEL_1, region, key));
            if (obj.getValue() == null) {
                LOG.debug("Can't find value for key: {}, region: {} in level_1", key, region);
                obj.setValue(CacheManager.get(LEVEL_2, region, key));
                if (obj.getValue() != null) {
                    LOG.debug("Read cache from level_2");
                    obj.setLevel(LEVEL_2);
                    CacheManager.set(LEVEL_1, region, key, obj.getValue());
                }else{
                    LOG.debug("Can't find value for key: {}, region: {} in level_2", key, region);
                }
            } else {
                LOG.debug("Read cache from level_1");
                obj.setLevel(LEVEL_1);
            }
        }

        return obj;
    }

    /**
     * 写入缓存数据
     * @param region: Cache Region name
     * @param key: Cache key
     * @param value: Cache value
     */
    @Override
    public void set(String region, Object key, Object value) {
        if (region != null && key != null) {
            if (value == null) {
                // value为null。默认为清除该缓存
                evict(region, key);
            } else {
                // 分几种情况
                // Object obj1 = CacheManager.get(LEVEL_1, region, key);
                // Object obj2 = CacheManager.get(LEVEL_2, region, key);
                // 1. L1 和 L2 都没有
                // 2. L1 有 L2 没有（这种情况不存在，除非是写 L2 的时候失败
                // 3. L1 没有，L2 有
                // 4. L1 和 L2 都有
                _sendEvictCmd(region, key);// 清除原有的一级缓存的内容
                CacheManager.set(LEVEL_1, region, key, value);
                CacheManager.set(LEVEL_2, region, key, value);
            }
        }
    }

    /**
     * 删除缓存
     * @param region:  Cache Region name
     * @param key: Cache key
     */
    @Override
    public void evict(String region, Object key) {
        CacheManager.evict(LEVEL_1, region, key); // 删除一级缓存
        CacheManager.evict(LEVEL_2, region, key); // 删除二级缓存
        _sendEvictCmd(region, key); // 发送广播
    }

    /**
     * 批量删除缓存
     * @param region: Cache region name
     * @param keys: Cache key
     */
    @Override
    public void batchEvict(String region, List keys) {
        CacheManager.batchEvict(LEVEL_1, region, keys);
        CacheManager.batchEvict(LEVEL_2, region, keys);
        _sendEvictCmd(region, keys);
    }

    /**
     * 清空缓存
     * @param region: Cache region name
     * @throws CacheException
     */
    @Override
    public void clear(String region) throws CacheException {
        CacheManager.clear(LEVEL_1, region);
        CacheManager.clear(LEVEL_2, region);
        _sendClearCmd(region);
    }

    /**
     * 获取这一分区下缓存主键
     * 仅限L1缓存
     * @param region: Cache region name
     * @return
     * @throws CacheException
     */
    @Override
    public List keys(String region) throws CacheException {
        return CacheManager.keys(LEVEL_1, region);
    }

    @Override
    public void close() {

    }

    /**
     * 为了保证每个节点缓存的一致，当某个缓存对象因为超时被清除时，应该通知群组其他成员
     * @param region: Cache region name
     * @param key: cache key
     */
    @Override
    public void notifyElementExpired(String region, Object key) {
        LOG.debug("Cache data expired, region=" + region + ",key=" + key);

        // 删除二级缓存
        if (key instanceof List)
            CacheManager.batchEvict(LEVEL_2, region, (List) key);
        else
            CacheManager.evict(LEVEL_2, region, key);

        // 发送广播
        _sendEvictCmd(region, key);
    }

    /**
     * 发送清除缓存的广播命令
     *
     * @param region
     *            : Cache region name
     * @param key
     *            : cache key
     */
    private void _sendEvictCmd(String region, Object key) {
        // 发送广播
        Command cmd = new Command(Command.OPT_DELETE_KEY, region, key);
        try (Jedis jedis = RedisCacheProvider.getResource()) {
            jedis.publish(SafeEncoder.encode(channel), cmd.toBuffers());
        } catch (Exception e) {
            LOG.error("Unable to delete cache,region=" + region + ",key=" + key, e);
        }
    }

    /**
     * 发送清除缓存的广播命令
     * @param region: Cache region name
     */
    private void _sendClearCmd(String region) {
        // 发送广播
        Command cmd = new Command(Command.OPT_CLEAR_KEY, region, "");
        try (Jedis jedis = RedisCacheProvider.getResource()) {
            jedis.publish(SafeEncoder.encode(channel), cmd.toBuffers());
        } catch (Exception e) {
            LOG.error("Unable to clear cache,region=" + region, e);
        }
    }
}
