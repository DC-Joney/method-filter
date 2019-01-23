package com.aop.filter.service;

import com.aop.filter.aspect.annotation.MethodFilter;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Log4j2
@Component
public class AspectExample {

    @MethodFilter(test = "#id < 0")
    public Optional<String> aspectTest(Integer id){
        return Optional.ofNullable(String.valueOf(id));
    }


    @MethodFilter(test = "#request.getQueryParams().containsKey(#keyName)")
    public Optional<String> aspectRequest(ServerHttpRequest request,String keyName) {
        List<String> queryList = request.getQueryParams().get(keyName);

        //do Something

        log.info("success");

        return Optional.of("success");
    }
}
