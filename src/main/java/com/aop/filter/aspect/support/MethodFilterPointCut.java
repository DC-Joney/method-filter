package com.aop.filter.aspect.support;

import com.aop.filter.aspect.metadata.MethodFilterSource;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.Method;

public abstract class MethodFilterPointCut extends StaticMethodMatcherPointcut {

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        MethodFilterSource filterSource = getMethodFilterSource();
        return (filterSource != null && filterSource.getMethodMetadata(method, targetClass) != null);
    }


    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MethodFilterPointCut)) {
            return false;
        }
        MethodFilterPointCut otherPc = (MethodFilterPointCut) other;
        return ObjectUtils.nullSafeEquals(getMethodFilterSource(), otherPc.getMethodFilterSource());
    }


    @Nullable
    protected abstract MethodFilterSource getMethodFilterSource();

}
