package com.example.shop.config;

import com.example.shop.common.CurrentUserContext;
import com.example.shop.user.UserAccount;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditAspect {
    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);

    @AfterReturning("execution(* com.example.shop..*Service.*(..))")
    public void logServiceCall(JoinPoint joinPoint) {
        log.info("service method finished: {}", joinPoint.getSignature().toShortString());
    }

    @Before("@annotation(requireRole)")
    public void checkRole(RequireRole requireRole) {
        UserAccount user = CurrentUserContext.getRequired();
        if (user.getRole() != requireRole.value()) {
            throw new IllegalStateException("Permission denied, required role: " + requireRole.value());
        }
    }
}
