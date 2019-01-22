package com.aop.filter.aspect.metadata;

import com.aop.filter.aspect.annotation.MethodFilter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAttributes;

import java.lang.reflect.AnnotatedElement;

public class SpringMethodFilterAnnotationParser implements MethodFilterAnnotationParser {

    @Override
    public MethodFilterAttribute parseAnnotation(AnnotatedElement element) {
        AnnotationAttributes attributes = AnnotatedElementUtils.findMergedAnnotationAttributes(
                element, MethodFilter.class, false, false);
        if (attributes != null) {
            return parseTransactionAnnotation(attributes);
        }
        else {
            return null;
        }
    }


    private MethodFilterAttribute parseTransactionAnnotation(AnnotationAttributes attributes) {
        String targetFilter = attributes.getString("condition");
        return new MethodFilterAttribute(targetFilter);
    }

}
