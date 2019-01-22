package com.aop.filter.aspect.expression;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MethodFilterExpressionEvaluator {

    private final SpelExpressionParser parser;

    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();


    private final Map<ExpressionKey, Expression> conditionCache = new ConcurrentHashMap<>(64);


    public static final Object NO_RESULT = new Object();


    public static final Object RESULT_UNAVAILABLE = new Object();


    public static final String RESULT_VARIABLE = "result";



    protected MethodFilterExpressionEvaluator(SpelExpressionParser parser) {
        Assert.notNull(parser, "SpelExpressionParser must not be null");
        this.parser = parser;
    }


    protected MethodFilterExpressionEvaluator() {
        this(new SpelExpressionParser());
    }

    protected SpelExpressionParser getParser() {
        return this.parser;
    }


    protected Expression getExpression(Map<ExpressionKey, Expression> cache,
                                       AnnotatedElementKey elementKey, String expression) {

        MethodFilterExpressionEvaluator.ExpressionKey expressionKey = createKey(elementKey, expression);

        Expression expr = cache.get(expressionKey);
        if (expr == null) {
            expr = getParser().parseExpression(expression);
            cache.put(expressionKey, expr);
        }
        return expr;
    }

    private MethodFilterExpressionEvaluator.ExpressionKey createKey(AnnotatedElementKey elementKey, String expression) {
        return new MethodFilterExpressionEvaluator.ExpressionKey(elementKey, expression);
    }


    public EvaluationContext createEvaluationContext(Method method, Object[] args, Object target, Class<?> targetClass, Method targetMethod,
                                                      @Nullable BeanFactory beanFactory) {

        MethodFilterExpressionRoot rootObject = new MethodFilterExpressionRoot(
                 method, args, target, targetClass);

        MethodFilterEvaluationContext evaluationContext = new MethodFilterEvaluationContext(
                rootObject, targetMethod, args, parameterNameDiscoverer);

        if (beanFactory != null) {
            evaluationContext.setBeanResolver(new BeanFactoryResolver(beanFactory));
        }
        return evaluationContext;
    }


    public boolean condition(String conditionExpression, AnnotatedElementKey methodKey, EvaluationContext evalContext) {
        return (Boolean.TRUE.equals(getExpression(this.conditionCache, methodKey, conditionExpression).getValue(
                evalContext, Boolean.class)));
    }


    protected static class ExpressionKey implements Comparable<ExpressionKey> {

        private final AnnotatedElementKey element;

        private final String expression;

        protected ExpressionKey(AnnotatedElementKey element, String expression) {
            Assert.notNull(element, "AnnotatedElementKey must not be null");
            Assert.notNull(expression, "Expression must not be null");
            this.element = element;
            this.expression = expression;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof MethodFilterExpressionEvaluator.ExpressionKey)) {
                return false;
            }
            MethodFilterExpressionEvaluator.ExpressionKey otherKey = (MethodFilterExpressionEvaluator.ExpressionKey) other;
            return (this.element.equals(otherKey.element) &&
                    ObjectUtils.nullSafeEquals(this.expression, otherKey.expression));
        }

        @Override
        public int hashCode() {
            return this.element.hashCode() * 29 + this.expression.hashCode();
        }

        @Override
        public String toString() {
            return this.element + " with expression \"" + this.expression + "\"";
        }

        @Override
        public int compareTo(MethodFilterExpressionEvaluator.ExpressionKey other) {
            int result = this.element.toString().compareTo(other.element.toString());
            if (result == 0) {
                result = this.expression.compareTo(other.expression);
            }
            return result;
        }
    }

}
