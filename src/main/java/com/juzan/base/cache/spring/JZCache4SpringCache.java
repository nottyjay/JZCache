package com.juzan.base.cache.spring;

import com.juzan.base.cache.CacheChannel;
import com.juzan.base.cache.CacheObject;
import com.juzan.base.cache.JZCache;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;

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
public class JZCache4SpringCache implements Cache {

    private CacheChannel channel;
    private String region;
    private String name = "JZCache";

    public JZCache4SpringCache() {
        this.channel = JZCache.getChannel();
    }

    public JZCache4SpringCache(CacheChannel channel) {
        this.channel = channel;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Object getNativeCache() {
        return this.channel;
    }

    @Override
    public ValueWrapper get(Object key) {
        CacheObject cacheObject = this.channel.get(region, key);
        return cacheObject.getValue() != null ? new SimpleValueWrapper(cacheObject.getValue()) : null;
    }

    @Override
    public void put(Object key, Object value) {
        this.channel.set(region, key, value);
    }

    @Override
    public void evict(Object key) {
        this.channel.evict(region, key);
    }

    @Override
    public void clear() {
        this.channel.clear(region);
    }

    public void setChannel(CacheChannel channel) {
        this.channel = channel;
    }

    public void setRegion(String region) {
        this.region = region;
    }

}
