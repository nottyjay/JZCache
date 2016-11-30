package com.juzan.base.cache.util;

import com.juzan.base.cache.CacheManager;
import com.juzan.base.cache.serializer.*;
import net.sf.ehcache.CacheException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class SerializationUtils {

    private final static Logger LOG = LoggerFactory.getLogger(SerializationUtils.class);

    private static Serializer g_ser;

    static {
        String ser = CacheManager.getSerializer();
        if (ser == null || "".equals(ser.trim()))
            g_ser = new JavaSerializer();
        else {
            if (ser.equals("java")) {
                g_ser = new JavaSerializer();
            } else if (ser.equals("fst")) {
                g_ser = new FSTSerializer();
            } else if (ser.equals("kryo")) {
                g_ser = new KryoSerializer();
            } else if (ser.equals("kryo_pool_ser")){
                g_ser = new KryoPoolSerializer();
            } else {
                try {
                    g_ser = (Serializer) Class.forName(ser).newInstance();
                } catch (Exception e) {
                    throw new CacheException("Cannot initialize Serializer named [" + ser + ']', e);
                }
            }
        }
        LOG.info("Using Serializer -> [" + g_ser.name() + ":" + g_ser.getClass().getName() + ']');
    }

    public static byte[] serialize(Object obj) throws IOException {
        return g_ser.serialize(obj);
    }

    public static Object deserialize(byte[] bytes) throws IOException {
        return g_ser.deserialize(bytes);
    }
}
