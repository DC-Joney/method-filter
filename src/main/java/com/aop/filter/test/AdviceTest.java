package com.aop.filter.test;

import com.aop.filter.service.AspectExample;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Log4j2
@Component
public class AdviceTest {
    @Autowired
    private AspectExample aspectExample;

    @PostConstruct
    public void init(){
        String state = aspectExample
                .aspectTest(3)
                .orElse("fail");

        System.out.println(state);

    }
}
