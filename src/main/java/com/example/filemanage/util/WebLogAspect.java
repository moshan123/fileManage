package com.example.filemanage.util;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;


import com.google.gson.Gson;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class WebLogAspect {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Gson gson = new Gson();

    ThreadLocal<Long>  startTime = new ThreadLocal<Long>();

    @Pointcut("execution(public * com.example.filemanage.controller.*.*(..))")//切入点描述 这个是controller包的切入点
    public void controllerLog(){}//签名，可以理解成这个切入点的一个名称


    @Before("controllerLog()") //在切入点的方法run之前要干的
    public void logBeforeController(JoinPoint joinPoint) {
        startTime.set(System.currentTimeMillis());
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();//这个RequestContextHolder是Springmvc提供来获得请求的东西
        HttpServletRequest request = ((ServletRequestAttributes)requestAttributes).getRequest();

        // 记录下请求内容
        logger.info("===============请求内容begin===============");
        logger.info("URL : " + request.getRequestURL().toString());
        logger.info("HTTP_METHOD : " + request.getMethod());
        logger.info("IP : " + request.getRemoteAddr());
        logger.info("THE ARGS OF THE CONTROLLER : " + Arrays.toString(joinPoint.getArgs()));

        //下面这个getSignature().getDeclaringTypeName()是获取包+类名的   然后后面的joinPoint.getSignature.getName()获取了方法名
        logger.info("CLASS_METHOD : " + joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());
        logger.info("请求类方法参数:" + Arrays.toString(joinPoint.getArgs()));
        logger.info("TARGET: " + joinPoint.getTarget());//返回的是需要加强的目标类的对象
        logger.info("THIS: " + joinPoint.getThis());//返回的是经过加强后的代理类的对象
        logger.info("===============请求内容end===============");


    }

    @AfterReturning(returning = "o", pointcut = "controllerLog()")
    public void logAfterController(Object o) {
        logger.info("--------------返回内容begin----------------");
        logger.info("Response内容:" + gson.toJson(o));
        logger.info("--------------返回内容end----------------");
        logger.info("请求处理时间为:"+(System.currentTimeMillis() - startTime.get()) + "ms");
    }



}

