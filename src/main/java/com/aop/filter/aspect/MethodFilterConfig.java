package com.aop.filter.aspect;

import com.aop.filter.aspect.metadata.AnnotationMethodFilterSource;
import com.aop.filter.aspect.metadata.MethodFilterSource;
import com.aop.filter.aspect.support.BeanFactoryMethodFilterAdvisor;
import com.aop.filter.aspect.support.MethodFilterInterceptor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

@Configuration
@ConditionalOnClass(BeanFactoryMethodFilterAdvisor.class)
class MethodFilterConfig {

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public BeanFactoryMethodFilterAdvisor aspectComponentAdvisor(){
        BeanFactoryMethodFilterAdvisor advisor =  new BeanFactoryMethodFilterAdvisor();
        advisor.setMethodFilterSource(methodFilterSource());
        advisor.setAdvice(methodFilterInterceptor());
        return advisor;
    }


    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public MethodFilterInterceptor methodFilterInterceptor() {
        MethodFilterInterceptor interceptor = new MethodFilterInterceptor();
        interceptor.setMethodFilterSource(methodFilterSource());
        return interceptor;
    }


    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public MethodFilterSource methodFilterSource() {
        return new AnnotationMethodFilterSource();
    }

}
