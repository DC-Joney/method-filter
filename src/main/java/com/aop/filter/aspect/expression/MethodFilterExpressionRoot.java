package com.aop.filter.aspect.expression;

import java.lang.reflect.Method;

public class MethodFilterExpressionRoot {

    private final Method method;

    private final Object[] args;

    private final Object target;

    private final Class<?> targetClass;

    public MethodFilterExpressionRoot(Method method, Object[] args, Object target, Class<?> targetClass) {

        this.method = method;
        this.target = target;
        this.targetClass = targetClass;
        this.args = args;
    }


    public Method getMethod() {
        return this.method;
    }

    public String getMethodName() {
        return this.method.getName();
    }

    public Object[] getArgs() {
        return this.args;
    }

    public Object getTarget() {
        return this.target;
    }

    public Class<?> getTargetClass() {
        return this.targetClass;
    }

}
