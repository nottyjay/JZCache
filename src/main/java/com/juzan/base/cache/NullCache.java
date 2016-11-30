package com.juzan.base.cache;

import com.juzan.base.cache.Cache;
import com.juzan.base.cache.CacheException;

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
public class NullCache implements Cache {

    @Override
    public Object get(Object key) throws CacheException {
        return null;
    }

    @Override
    public void put(Object key, Object value) throws CacheException {

    }

    @Override
    public void update(Object key, Object value) throws CacheException {

    }

    @Override
    public List keys() throws CacheException {
        return null;
    }

    @Override
    public void evict(Object key) throws CacheException {

    }

    @Override
    public void evict(List keys) throws CacheException {

    }

    @Override
    public void clear() throws CacheException {

    }

    @Override
    public void destroy() throws CacheException {

    }
}
