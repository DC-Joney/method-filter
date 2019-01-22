package com.aop.filter.test;

import com.aop.filter.aspect.annotation.MethodFilter;
import org.springframework.stereotype.Component;

@Component
public class AspectExample {

    @MethodFilter(condition = "#id < 0")
    public int aspectTest(Integer id){
        return id;
    }

    @MethodFilter(condition = "@rest.test1()")
    public Integer aspectTest1(Integer id){
        return id;
    }
}
