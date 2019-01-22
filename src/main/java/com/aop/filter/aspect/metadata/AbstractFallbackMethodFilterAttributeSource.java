package com.aop.filter.aspect.metadata;

import org.springframework.aop.support.AopUtils;
import org.springframework.core.MethodClassKey;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


//AbstractFallbackTransactionAttributeSource
public abstract class AbstractFallbackMethodFilterAttributeSource implements MethodFilterSource {

    private final Map<Object, MethodFilterAttribute> attributeCache = new ConcurrentHashMap<>(1024);

    private static final MethodFilterAttribute NULL_CACHING_ATTRIBUTE = new MethodFilterAttribute(null){
        @Override
        public String toString() {
            return "";
        }
    };

    @Override
    public MethodFilterAttribute getMethodMetadata(Method method, Class<?> targetClass) {
        if (method.getDeclaringClass() == Object.class) {
            return null;
        }

        Object cacheKey = getCacheKey(method, targetClass);

        MethodFilterAttribute cached = this.attributeCache.get(cacheKey);

        if (cached != null) {
            // Value will either be canonical value indicating there is no transaction attribute,
            // or an actual transaction attribute.
            return cached != NULL_CACHING_ATTRIBUTE ? cached : null;
        }
        else {

            MethodFilterAttribute cacheOps = computeCacheOperations(method, targetClass);
            if (cacheOps != null) {

                this.attributeCache.put(cacheKey, cacheOps);
            }
            else {
                this.attributeCache.put(cacheKey, NULL_CACHING_ATTRIBUTE);
            }
            return cacheOps;

        }

    }

    private MethodFilterAttribute computeCacheOperations(Method method, Class<?> targetClass) {

        if (allowPublicMethodsOnly() && !Modifier.isPublic(method.getModifiers())) {
            return null;
        }


        Method specificMethod = AopUtils.getMostSpecificMethod(method, targetClass);


        MethodFilterAttribute opDef = findCacheOperations(specificMethod);
        if (opDef != null) {
            return opDef;
        }

        // Second try is the caching operation on the target class.
        opDef = findCacheOperations(specificMethod.getDeclaringClass());
        if (opDef != null && ClassUtils.isUserLevelMethod(method)) {
            return opDef;
        }

        if (specificMethod != method) {
            // Fallback is to look at the original method.
            opDef = findCacheOperations(method);
            if (opDef != null) {
                return opDef;
            }
            // Last fallback is the class of the original method.
            opDef = findCacheOperations(method.getDeclaringClass());
            if (opDef != null && ClassUtils.isUserLevelMethod(method)) {
                return opDef;
            }
        }

        return null;

    }


    protected Object getCacheKey(Method method, @Nullable Class<?> targetClass) {
        return new MethodClassKey(method, targetClass);
    }



    @Nullable
    protected abstract MethodFilterAttribute findCacheOperations(Class<?> clazz);


    @Nullable
    protected abstract MethodFilterAttribute findCacheOperations(Method method);


    protected boolean allowPublicMethodsOnly() {
        return false;
    }


}
