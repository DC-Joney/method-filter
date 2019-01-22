package com.aop.filter.aspect.support;

import com.aop.filter.aspect.MethodInvoker;
import com.aop.filter.aspect.expression.MethodFilterAspectSupport;
import lombok.extern.log4j.Log4j2;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.cache.interceptor.CacheOperationInvoker;

import java.lang.reflect.Method;

@Log4j2
public class MethodFilterInterceptor extends MethodFilterAspectSupport implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {

        Method method = invocation.getMethod();

        MethodInvoker methodFilterInvoker = () -> {
            try {
                return invocation.proceed();
            }
            catch (Throwable ex) {
                throw new CacheOperationInvoker.ThrowableWrapper(ex);
            }
        };

        try {

            if(invocation.getMethod().getReturnType().isPrimitive()){
                return invocation.proceed();
            }

            return execute(methodFilterInvoker, invocation.getThis(), method, invocation.getArguments());
        }
        catch (CacheOperationInvoker.ThrowableWrapper th) {
            throw th.getOriginal();
        }
    }
}
