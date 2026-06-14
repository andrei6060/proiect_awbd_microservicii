package com.clinic.clinic.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * Cross-cutting logging for the service layer. Logs method entry/exit at DEBUG
 * (method name + argument count only — never argument values, to avoid leaking
 * passwords, tokens or other secrets) and logs uncaught exceptions at ERROR.
 */
@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Pointcut("execution(* com.clinic.clinic.Service..*(..))")
    public void serviceLayer() {
    }

    @Around("serviceLayer()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().toShortString();
        int argCount = joinPoint.getArgs() == null ? 0 : joinPoint.getArgs().length;
        log.debug("Entering {} with {} argument(s)", method, argCount);
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        log.debug("Exiting {} ({} ms)", method, System.currentTimeMillis() - start);
        return result;
    }

    @AfterThrowing(pointcut = "serviceLayer()", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable ex) {
        log.error("Exception in {}: {}", joinPoint.getSignature().toShortString(), ex.toString());
    }
}
