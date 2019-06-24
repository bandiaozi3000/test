package com.hotel.test;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 处理和包装异常
 */
@Aspect
@Component
public class AopDemo {
    private static final Logger logger = LoggerFactory.getLogger(AopDemo.class);

    @Pointcut(value = "execution(* com.hotel.test.ExceptionDemo.*(..))")
    private void pointCut() {
    }

    @Around(value = "pointCut()")
    public Object handlerControllerMethod(ProceedingJoinPoint pjp) {
        long startTime = System.currentTimeMillis();
        String result = "";
        try {
            result = (String) pjp.proceed();
            logger.info(pjp.getSignature() + "use time:" + (System.currentTimeMillis() - startTime));
        } catch (Throwable e) {
            logger.info("error : {} ",e.getMessage());
        }
        return result;
    }


}