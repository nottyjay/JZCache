package com.juzan.base.cache.redis;

import com.juzan.base.cache.Cache;
import com.juzan.base.cache.CacheException;
import com.juzan.base.cache.JZCache;
import com.juzan.base.cache.util.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.util.ArrayList;
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
public class RedisCache implements Cache {

    private static final Logger LOG = LoggerFactory.getLogger(RedisCache.class);

    private String region;
    private byte[] regionByte;
    private JedisPool pool;

    public RedisCache(String region, JedisPool pool) {
        if (region == null || region.length() == 0)
            region = "_"; // 缺省region

        region = getRegionName(region);
        this.pool = pool;
        this.region = region;
        this.regionByte = region.getBytes();
    }

    /**
     * 在region里增加一个可选的层级,作为命名空间,使结构更加清晰
     * 同时满足小型应用,多个J2Cache共享一个redis database的场景
     * @param region
     * @return
     */
    private String getRegionName(String region) {
        String nameSpace = JZCache.getConfig().getProperty("redis.namespace", "");
        if(nameSpace != null && !(nameSpace.length() == 0)) {
            region = nameSpace + ":" + region;
        }
        return region;
    }

    protected byte[] getKeyName(Object key) {
        if(key instanceof Number)
            return ("I:" + key).getBytes();
        else if(key instanceof String || key instanceof StringBuilder || key instanceof StringBuffer)
            return ("S:" + key).getBytes();
        return ("O:" + key).getBytes();
    }

    @Override
    public Object get(Object key) throws CacheException {
        if (null == key)
            return null;
        Object obj = null;
        try (Jedis cache = pool.getResource()) {
            byte[] b = cache.hget(regionByte, getKeyName(key));
            if(b != null)
                obj = SerializationUtils.deserialize(b);
        } catch (Exception e) {
            LOG.error("Error occured when get data from redis cache", e);
            if(e instanceof IOException || e instanceof NullPointerException)
                evict(key);
        }
        return obj;
    }

    @Override
    public void put(Object key, Object value) throws CacheException {
        if (key == null)
            return;
        if (value == null)
            evict(key);
        else {
            try (Jedis cache = pool.getResource()) {
                cache.hset(regionByte, getKeyName(key), SerializationUtils.serialize(value));
            } catch (Exception e) {
                throw new CacheException(e);
            }
        }
    }

    @Override
    public void update(Object key, Object value) throws CacheException {
        put(key, value);
    }

    @Override
    public List keys() throws CacheException {
        try (Jedis cache = pool.getResource()) {
            return new ArrayList<String>(cache.hkeys(region));
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }

    @Override
    public void evict(Object key) throws CacheException {
        if (key == null)
            return;
        try (Jedis cache = pool.getResource()) {
            cache.hdel(regionByte, getKeyName(key));
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }

    @Override
    public void evict(List keys) throws CacheException {
        if(keys == null || keys.size() == 0)
            return ;
        try (Jedis cache = pool.getResource()) {
            int size = keys.size();
            byte[][] okeys = new byte[size][];
            for(int i=0; i<size; i++){
                okeys[i] = getKeyName(keys.get(i));
            }
            cache.hdel(regionByte, okeys);
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }

    @Override
    public void clear() throws CacheException {
        try (Jedis cache = pool.getResource()) {
            cache.del(regionByte);
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }

    @Override
    public void destroy() throws CacheException {
        this.clear();
    }
}
