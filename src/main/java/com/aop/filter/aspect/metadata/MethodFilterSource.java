package com.aop.filter.aspect.metadata;

import org.springframework.lang.Nullable;

import java.lang.reflect.Method;

public interface MethodFilterSource {
    @Nullable
    MethodFilterAttribute getMethodMetadata(Method method, @Nullable Class<?> targetClass);
}
