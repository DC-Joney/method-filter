package com.aop.filter.aspect.expression;

import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.ParameterNameDiscoverer;

import java.lang.reflect.Method;

class MethodFilterEvaluationContext extends MethodBasedEvaluationContext {

    public MethodFilterEvaluationContext(Object rootObject, Method method, Object[] arguments, ParameterNameDiscoverer parameterNameDiscoverer) {
        super(rootObject, method, arguments, parameterNameDiscoverer);
    }

    @Override
    public Object lookupVariable(String name) {
        return super.lookupVariable(name);
    }
}
