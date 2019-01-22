package com.aop.filter.aspect.annotation;

import com.aop.filter.aspect.MethodFilterConfig;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.AdviceModeImportSelector;
import org.springframework.context.annotation.AutoProxyRegistrar;

import java.util.ArrayList;
import java.util.List;


public class MethodFilterAspectRegister extends AdviceModeImportSelector<EnableMethodFilter> {

    @Override
    protected String[] selectImports(AdviceMode adviceMode) {
        switch (adviceMode) {
            case PROXY:
                return getProxyImports();
            default:
                throw new IllegalStateException("AdviceMode " + adviceMode + " is not supported");
        }
    }


    private String[] getProxyImports() {
        List<String> result = new ArrayList<>();
        result.add(AutoProxyRegistrar.class.getName());
        result.add(MethodFilterConfig.class.getName());
        return result.toArray(new String[0]);
    }
}
