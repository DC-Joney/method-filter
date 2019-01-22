package com.aop.filter.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController(value = "rest")
public class AspectDemoController {

    @Autowired
    private AspectExample aspectExample;

    @GetMapping("/test")
    public Mono<Void> test(@RequestParam("id") Integer id){
        return Mono.fromRunnable(()-> aspectExample.aspectTest1(id));
    }

    public boolean test1(){
        return false;
    }

}
