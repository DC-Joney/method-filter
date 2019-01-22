package com.aop.filter.aspect.metadata;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class AnnotationMethodFilterSource extends AbstractFallbackMethodFilterAttributeSource implements Serializable {


    private final boolean publicMethodsOnly;

    private final Set<MethodFilterAnnotationParser> annotationParsers;


    public AnnotationMethodFilterSource() {
        this(true);
    }


    public AnnotationMethodFilterSource(boolean publicMethodsOnly) {
        this.publicMethodsOnly = publicMethodsOnly;
        this.annotationParsers = Collections.singleton(new SpringMethodFilterAnnotationParser());
    }


    public AnnotationMethodFilterSource(MethodFilterAnnotationParser annotationParser) {
        this.publicMethodsOnly = true;
        Assert.notNull(annotationParser, "CacheAnnotationParser must not be null");
        this.annotationParsers = Collections.singleton(annotationParser);
    }


    public AnnotationMethodFilterSource(MethodFilterAnnotationParser... annotationParsers) {
        this.publicMethodsOnly = true;
        Assert.notEmpty(annotationParsers, "At least one CacheAnnotationParser needs to be specified");
        this.annotationParsers = new LinkedHashSet<>(Arrays.asList(annotationParsers));
    }


    public AnnotationMethodFilterSource(Set<MethodFilterAnnotationParser> annotationParsers) {
        this.publicMethodsOnly = true;
        Assert.notEmpty(annotationParsers, "At least one CacheAnnotationParser needs to be specified");
        this.annotationParsers = annotationParsers;
    }



    @Override
    protected MethodFilterAttribute findCacheOperations(Class<?> clazz) {
        return null;
    }

    @Override
    protected MethodFilterAttribute findCacheOperations(Method method) {
        return determineTransactionAttribute(method);
    }

    @Nullable
    protected MethodFilterAttribute determineTransactionAttribute(AnnotatedElement element) {
        for (MethodFilterAnnotationParser annotationParser : this.annotationParsers) {
            MethodFilterAttribute attr = annotationParser.parseAnnotation(element);
            if (attr != null) {
                return attr;
            }
        }
        return null;
    }


    protected boolean allowPublicMethodsOnly() {
        return publicMethodsOnly;
    }


}
