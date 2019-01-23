package com.aop.filter.test;

import com.aop.filter.service.AspectExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController(value = "rest")
public class AspectDemoController {

    @Autowired
    private AspectExample aspectExample;

    @GetMapping("/test")
    public Mono<Void> test(ServerHttpRequest request){
        return Mono.fromRunnable(()-> aspectExample.aspectRequest(request,"id"));
    }

    public boolean test1(){
        return false;
    }

}
