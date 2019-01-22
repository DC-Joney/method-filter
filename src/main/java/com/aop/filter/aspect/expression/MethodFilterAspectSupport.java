package com.aop.filter.aspect.expression;

import com.aop.filter.aspect.MethodInvoker;
import com.aop.filter.aspect.metadata.MethodFilterAttribute;
import com.aop.filter.aspect.metadata.MethodFilterSource;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.expression.EvaluationContext;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.aop.support.AopUtils.getTargetClass;

public class MethodFilterAspectSupport implements BeanFactoryAware, InitializingBean {

    private BeanFactory beanFactory;

    private boolean initialized;

    private final MethodFilterExpressionEvaluator evaluator = new MethodFilterExpressionEvaluator();


    private MethodFilterSource methodFilterSource;

    private final Map<MethodFilterCacheKey, CacheOperationMetadata> metadataCache = new ConcurrentHashMap<>(1024);


    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initialized = true;
    }


    @Nullable
    protected Object execute(MethodInvoker invoker, Object target, Method method, Object[] args) {

        if (this.initialized) {
            Class<?> targetClass = getTargetClass(target);
            MethodFilterSource cacheOperationSource = getMethodFilterSource();
            if (cacheOperationSource != null) {
                MethodFilterAttribute attribute = cacheOperationSource.getMethodMetadata(method, targetClass);
                if (attribute != null) {

                    CacheOperationMetadata metadata = getCacheOperationMetadata(attribute, method, targetClass);

                    CacheOperationContext context = new CacheOperationContext(metadata,args,method);

                    if(!context.isConditionPassing()){
                        return null;
                    }
                }
            }
        }
        return invoker.invoke();
    }


    private  CacheOperationMetadata getCacheOperationMetadata(
            MethodFilterAttribute attribute, Method method, Class<?> targetClass) {

        MethodFilterCacheKey cacheKey = new MethodFilterCacheKey(attribute, method, targetClass);

        CacheOperationMetadata metadata = this.metadataCache.get(cacheKey);

        if (metadata == null) {

            metadata = new CacheOperationMetadata(attribute, method, targetClass);
            this.metadataCache.put(cacheKey, metadata);
        }
        return metadata;
    }


    public void setMethodFilterSource(MethodFilterSource methodFilterSource) {
        this.methodFilterSource = methodFilterSource;
    }

    public MethodFilterSource getMethodFilterSource() {
        return this.methodFilterSource;
    }



    protected class CacheOperationContext {

        private final CacheOperationMetadata metadata;

        private final Object[] args;

        private final Object target;

        @Nullable
        private Boolean conditionPassing;

        public CacheOperationContext(CacheOperationMetadata metadata, Object[] args, Object target) {
            this.metadata = metadata;
            this.args = extractArgs(metadata.method, args);
            this.target = target;
        }

        public MethodFilterAttribute getOperation() {
            return this.metadata.attribute;
        }

        public Object getTarget() {
            return this.target;
        }

        public Method getMethod() {
            return this.metadata.method;
        }

        public Object[] getArgs() {
            return this.args;
        }

        private Object[] extractArgs(Method method, Object[] args) {
            if (!method.isVarArgs()) {
                return args;
            }
            Object[] varArgs = ObjectUtils.toObjectArray(args[args.length - 1]);
            Object[] combinedArgs = new Object[args.length - 1 + varArgs.length];
            System.arraycopy(args, 0, combinedArgs, 0, args.length - 1);
            System.arraycopy(varArgs, 0, combinedArgs, args.length - 1, varArgs.length);
            return combinedArgs;
        }

        protected boolean isConditionPassing() {
            if (this.conditionPassing == null) {
                if (StringUtils.hasText(this.metadata.attribute.getTargetFilter())) {
                    EvaluationContext evaluationContext = createEvaluationContext();
                    this.conditionPassing = evaluator.condition(this.metadata.attribute.getTargetFilter(),
                            this.metadata.methodKey, evaluationContext);
                }
                else {
                    this.conditionPassing = true;
                }
            }
            return this.conditionPassing;
        }

        private EvaluationContext createEvaluationContext() {
            return evaluator.createEvaluationContext(this.metadata.method, this.args,
                    this.target, this.metadata.targetClass, this.metadata.targetMethod, beanFactory);
        }


    }


    private static final class MethodFilterCacheKey implements Comparable<MethodFilterCacheKey> {

        private final MethodFilterAttribute attribute;

        private final AnnotatedElementKey methodCacheKey;

        private MethodFilterCacheKey(MethodFilterAttribute attribute, Method method, Class<?> targetClass) {
            this.attribute = attribute;
            this.methodCacheKey = new AnnotatedElementKey(method, targetClass);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof MethodFilterCacheKey)) {
                return false;
            }
            MethodFilterCacheKey otherKey = (MethodFilterCacheKey) other;
            return (this.attribute.equals(otherKey.attribute) &&
                    this.methodCacheKey.equals(otherKey.methodCacheKey));
        }

        @Override
        public int hashCode() {
            return (this.attribute.hashCode() * 31 + this.methodCacheKey.hashCode());
        }

        @Override
        public String toString() {
            return this.attribute + " on " + this.methodCacheKey;
        }

        @Override
        public int compareTo(MethodFilterCacheKey other) {
            int result = this.attribute.getTargetFilter().compareTo(other.attribute.getTargetFilter());
            if (result == 0) {
                result = this.methodCacheKey.compareTo(other.methodCacheKey);
            }
            return result;
        }
    }


    protected static class CacheOperationMetadata {

        private final MethodFilterAttribute attribute;

        private final Method method;

        private final Class<?> targetClass;

        private final Method targetMethod;

        private final AnnotatedElementKey methodKey;


        public CacheOperationMetadata(MethodFilterAttribute attribute, Method method, Class<?> targetClass) {

            this.attribute = attribute;
            this.method = BridgeMethodResolver.findBridgedMethod(method);
            this.targetClass = targetClass;
            this.targetMethod = (!Proxy.isProxyClass(targetClass) ?
                    AopUtils.getMostSpecificMethod(method, targetClass) : this.method);
            this.methodKey = new AnnotatedElementKey(this.targetMethod, targetClass);
        }
    }


}
