package com.dxj.aspect;

import com.dxj.domain.LoginLog;
import com.dxj.service.LogService;
import com.dxj.service.LoginLogService;
import lombok.extern.slf4j.Slf4j;
import com.dxj.domain.Log;
import com.dxj.exception.BadRequestException;
import com.dxj.utils.ThrowableUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author dxj
 * @date 2018-11-24
 */
@Component
@Aspect
@Slf4j
public class DataScopeAspect {

    private final LogService logService;
    private final LoginLogService loginLogService;

    private long currentTime = 0L;

    @Autowired
    public DataScopeAspect(LogService logService, LoginLogService loginLogService) {
        this.logService = logService;
        this.loginLogService = loginLogService;
    }

    /**
     * 配置切入点
     */
    @Pointcut("@annotation(com.dxj.aop.log.Log)")
    public void logPointcut() {
        // 该方法无方法体,主要为了让同类中其他方法使用此切入点
    }

    //登录日志
    @Pointcut("@annotation(com.dxj.aop.log.LoginLog)")
    public void loginLogPointcut() {
        // 该方法无方法体,主要为了让同类中其他方法使用此切入点
    }

    /**
     * 配置环绕通知,使用在方法logPointcut()上注册的切入点
     *
     * @param joinPoint join point for advice
     */
    @Around("logPointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint){
        Object result = getObject(joinPoint);
        //操作日志
        Log log = new Log("INFO",System.currentTimeMillis() - currentTime);
        logService.save(joinPoint, log);
        return result;
    }

    //登录日志
    @Around("loginLogPointcut()")
    public Object loginLogAround(ProceedingJoinPoint joinPoint){
        Object result = getObject(joinPoint);
        //操作日志
        LoginLog loginLog = new LoginLog("INFO",System.currentTimeMillis() - currentTime);
        loginLogService.save(joinPoint, loginLog);
        return result;
    }

    private Object getObject(ProceedingJoinPoint joinPoint) {
        Object result;
        currentTime = System.currentTimeMillis();
        try {
            result = joinPoint.proceed();
        } catch (Throwable e) {
            throw new BadRequestException(e.getMessage());
        }
        return result;
    }


    /**
     * 配置异常通知
     *
     * @param joinPoint join point for advice
     * @param e exception
     */
    @AfterThrowing(pointcut = "logPointcut()", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {

        //异常日志
        Log log = new Log("ERROR",System.currentTimeMillis() - currentTime);
        log.setExceptionDetail(ThrowableUtil.getStackTrace(e));
        logService.save((ProceedingJoinPoint)joinPoint, log);

    }
    /**
     * 配置登录异常通知
     *
     * @param joinPoint join point for advice
     */
    @AfterThrowing(pointcut = "loginLogPointcut()", throwing = "e")
    public void loginLogAfterThrowing(JoinPoint joinPoint, Throwable e) {

        long time = System.currentTimeMillis() - currentTime;
        //登录异常日志
        LoginLog loginLog = new LoginLog("ERROR", time);
        loginLogService.save((ProceedingJoinPoint)joinPoint, loginLog);
        //同时记录在异常日志中
        Log log = new Log("ERROR", time);
        log.setDescription("登录");
        log.setExceptionDetail(ThrowableUtil.getStackTrace(e));
        logService.save((ProceedingJoinPoint)joinPoint, log);

    }
}
