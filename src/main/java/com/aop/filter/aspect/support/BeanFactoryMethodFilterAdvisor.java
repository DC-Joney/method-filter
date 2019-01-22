package com.aop.filter.aspect.support;

import com.aop.filter.aspect.metadata.MethodFilterSource;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractBeanFactoryPointcutAdvisor;

public class BeanFactoryMethodFilterAdvisor extends AbstractBeanFactoryPointcutAdvisor {

    private MethodFilterSource methodFilterSource;

    private final MethodFilterPointCut pointcut = new MethodFilterPointCut() {
        @Override
        protected MethodFilterSource getMethodFilterSource() {
            return methodFilterSource;
        }
    };


    public void setMethodFilterSource(MethodFilterSource methodFilterSource) {
        this.methodFilterSource = methodFilterSource;
    }

    @Override
    public Pointcut getPointcut() {
        return pointcut;
    }
}
