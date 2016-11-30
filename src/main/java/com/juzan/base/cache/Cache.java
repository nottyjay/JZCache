package com.juzan.base.cache;

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
public interface Cache {

    /**
     * Get an item from the cache, nontransactionally
     * @param key cache key
     * @return the cached object or null
     */
    public Object get(Object key) throws CacheException;

    /**
     * Add an item to the cache, nontransactionally, with
     * failfast semantics
     * @param key cache key
     * @param value cache value
     */
    public void put(Object key, Object value) throws CacheException;

    /**
     * Add an item to the cache
     * @param key cache key
     * @param value cache value
     */
    public void update(Object key, Object value) throws CacheException;

    @SuppressWarnings("rawtypes")
    public List keys() throws CacheException ;

    /**
     * @param key Cache key
     * Remove an item from the cache
     */
    public void evict(Object key) throws CacheException;

    /**
     * Batch remove cache objects
     * @param keys the cache keys to be evicted
     */
    @SuppressWarnings("rawtypes")
    public void evict(List keys) throws CacheException;

    /**
     * Clear the cache
     */
    public void clear() throws CacheException;

    /**
     * Clean up
     */
    public void destroy() throws CacheException;
}
