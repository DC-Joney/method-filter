package com.aop.filter.test;

import com.aop.filter.aspect.annotation.MethodFilter;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class AspectExample {

    @MethodFilter(condition = "#id < 0")
    public int aspectTest(Integer id){
        return id;
    }

    @MethodFilter(condition = "#id < 0")
    public Integer aspectTest1(int id){
        log.info(id);
        return id;
    }
}
