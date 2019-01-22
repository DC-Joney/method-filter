package com.aop.filter.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class AdviceTest {
    @Autowired
    private AspectExample aspectExample;

    @PostConstruct
    public void init(){
        aspectExample.aspectTest(3);
    }
}
