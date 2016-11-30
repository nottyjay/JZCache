# JZCache

##原生使用
----------
采用Java设计制作的Cache中间件。本地采用Ehcache作为L2缓存，可选用Redis作为L3缓存。当信息更新时，需要L3广播通知其它订阅者更新各自的L2缓存部分。使用起来非常方便
```
CacheChannel cache = JZCache.getChannel();
```
即可使用。此外包含Spring的Cache支持。

##配合Spring Cache使用
----------
JZCache提供直接的Spring Cache方案。当需要使用Spring Cache驱动时，仅需要在Spring配置文件的缓存部分修改为
```
<bean id="cacheManager" class="org.springframework.cache.support.SimpleCacheManager">
    <property name="caches">
        <set>
            <bean class="com.juzan.base.cache.spring.JZCache4SpringCache">
                <property name="region" value="${自定义Region名}"/>
            </bean>
        </set>
    </property>
</bean>
```
