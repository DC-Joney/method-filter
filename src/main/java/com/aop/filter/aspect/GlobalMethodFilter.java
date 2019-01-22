package com.aop.filter.aspect;

@FunctionalInterface
public interface GlobalMethodFilter {
    boolean filter(Object[] args);
}
