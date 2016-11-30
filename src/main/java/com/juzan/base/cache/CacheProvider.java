package com.juzan.base.cache;

import java.util.Properties;

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
public interface CacheProvider {

    /**
     * 缓存的标识名称
     * @return return cache provider name
     */
    public String name();

    /**
     * 判断当前缓存状态
     * @return
     */
    public boolean isUseful();

    /**
     * Configure the cache
     *
     * @param regionName the name of the cache region
     * @param autoCreate autoCreate settings
     * @param listener listener for expired elements
     * @return return cache instance
     * @throws CacheException cache exception
     */
    public Cache buildCache(String regionName, boolean autoCreate, CacheExpiredListener listener) throws CacheException;

    /**
     * Callback to perform any necessary initialization of the underlying cache implementation
     * during SessionFactory construction.
     *
     * @param props current configuration settings.
     */
    public void start(Properties props) throws CacheException;

    /**
     * Callback to perform any necessary cleanup of the underlying cache implementation
     * during SessionFactory.close().
     */
    public void stop();
}
