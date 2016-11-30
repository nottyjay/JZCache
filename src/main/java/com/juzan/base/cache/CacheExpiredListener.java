package com.juzan.base.cache;

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
public interface CacheExpiredListener {

    /**
     * 当缓存中的某个对象超时被清除的时候触发
     * @param region: Cache region name
     * @param key: cache key
     */
    public void notifyElementExpired(String region, Object key) ;
}
