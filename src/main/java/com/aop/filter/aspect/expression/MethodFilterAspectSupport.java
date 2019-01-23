package com.aop.filter.aspect.expression;

import com.aop.filter.aspect.MethodInvoker;
import com.aop.filter.aspect.metadata.MethodFilterAttribute;
import com.aop.filter.aspect.metadata.MethodFilterSource;
import org.reactivestreams.Publisher;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.ReactiveAdapter;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.core.ReactiveTypeDescriptor;
import org.springframework.expression.EvaluationContext;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.aop.support.AopUtils.getTargetClass;

public class MethodFilterAspectSupport implements BeanFactoryAware, InitializingBean {

    private BeanFactory beanFactory;

    private boolean initialized;

    private final MethodFilterExpressionEvaluator evaluator = new MethodFilterExpressionEvaluator();


    private MethodFilterSource methodFilterSource;

    private final Map<MethodFilterCacheKey, MethodFilterSourceMetadata> metadataCache = new ConcurrentHashMap<>(1024);


    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void afterPropertiesSet() {
        initialized = true;
    }


    @Nullable
    protected Object execute(MethodInvoker invoker, Object target, Method method, Object[] args) {

        if (this.initialized) {

            ReactiveAdapterRegistry registry = new ReactiveAdapterRegistry();

            Class<?> returnType = method.getReturnType();

            if (!Optional.class.isAssignableFrom(returnType)) {
                throw new IllegalStateException("The returnType " + returnType + " on " + method + " must return an instance of org.reactivestreams.Publisher (i.e. Mono / Flux) in order to support Reactor Context");
            }

            Class<?> targetClass = getTargetClass(target);
            MethodFilterSource cacheOperationSource = getMethodFilterSource();
            if (cacheOperationSource != null) {
                MethodFilterAttribute attribute = cacheOperationSource.getMethodMetadata(method, targetClass);
                if (attribute != null) {

                    MethodFilterSourceMetadata metadata = getCacheOperationMetadata(attribute, method, targetClass);

                    MethodFilterSourceContext context = new MethodFilterSourceContext(metadata,args,method);

                    if(!context.isConditionPassing()){
                        return Optional.empty();
                    }
                }
            }
        }
        return Optional.ofNullable(invoker.invoke());
    }


    private MethodFilterSourceMetadata getCacheOperationMetadata(
            MethodFilterAttribute attribute, Method method, Class<?> targetClass) {

        MethodFilterCacheKey cacheKey = new MethodFilterCacheKey(attribute, method, targetClass);

        MethodFilterSourceMetadata metadata = this.metadataCache.get(cacheKey);

        if (metadata == null) {

            metadata = new MethodFilterSourceMetadata(attribute, method, targetClass);
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



    protected class MethodFilterSourceContext {

        private final MethodFilterSourceMetadata metadata;

        private final Object[] args;

        private final Object target;

        @Nullable
        private Boolean conditionPassing;

        public MethodFilterSourceContext(MethodFilterSourceMetadata metadata, Object[] args, Object target) {
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


    protected static class MethodFilterSourceMetadata {

        private final MethodFilterAttribute attribute;

        private final Method method;

        private final Class<?> targetClass;

        private final Method targetMethod;

        private final AnnotatedElementKey methodKey;


        public MethodFilterSourceMetadata(MethodFilterAttribute attribute, Method method, Class<?> targetClass) {

            this.attribute = attribute;
            this.method = BridgeMethodResolver.findBridgedMethod(method);
            this.targetClass = targetClass;
            this.targetMethod = (!Proxy.isProxyClass(targetClass) ?
                    AopUtils.getMostSpecificMethod(method, targetClass) : this.method);
            this.methodKey = new AnnotatedElementKey(this.targetMethod, targetClass);
        }
    }


}
