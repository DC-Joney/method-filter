package com.aop.filter.aspect.metadata;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MethodFilterAttribute {

    private String targetFilter;

}
