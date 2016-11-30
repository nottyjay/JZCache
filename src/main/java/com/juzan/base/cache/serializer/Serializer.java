package com.juzan.base.cache.serializer;

import java.io.IOException;

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
public interface Serializer {

    public String name();

    public byte[] serialize(Object obj) throws IOException;

    public Object deserialize(byte[] bytes) throws IOException ;
}
