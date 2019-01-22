package com.aop.filter.aspect;

import java.lang.reflect.AnnotatedElement;

public interface AnnotationParser<T> {

     T parseAnnotation(AnnotatedElement element);

}
