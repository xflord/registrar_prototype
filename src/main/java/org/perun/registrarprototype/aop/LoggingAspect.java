package org.perun.registrarprototype.aop;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.perun.registrarprototype.security.CurrentUser;
import org.perun.registrarprototype.security.SessionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * For now aspect logging to log around/after API calls, could be used for messaging, auditing, IdM communication as well,
 * not sure whether flexible enough for business logic, might just clutter the logs
 */
@Aspect
@Component
public class LoggingAspect {
  private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

  private final SessionProvider sessionProvider;

  public LoggingAspect(SessionProvider sessionProvider) {
    this.sessionProvider = sessionProvider;
  }


  @Pointcut("within(org.perun.registrarprototype.controllers.*)")
  public void controllerPointcut() {
  }

  @Pointcut("within(org.perun.registrarprototype.services..*)")
  public void applicationPointcut() {
  }

  @AfterThrowing(pointcut = "controllerPointcut()", throwing = "e")
  public void logAfterThrowing(JoinPoint joinPoint, Throwable e) throws Throwable {
    log.warn("Method {}.{} threw: {}", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName(), e.getMessage(), e);
  }

  @Around("controllerPointcut()")
  public Object logApiCalls(ProceedingJoinPoint pjp) throws Throwable {
    CurrentUser user = sessionProvider.getCurrentSession().getPrincipal();
    String username = user.name() == null ? "anonymous" : user.name();
    // in case request info is needed for loging
    //    ServletRequestAttributes attrs =
    //        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    //    HttpServletRequest request = (attrs != null ? attrs.getRequest() : null);


    long start = System.currentTimeMillis();

    Object result = pjp.proceed();
    long duration = System.currentTimeMillis() - start;

    log.debug("Method {}.{}() called by {}, took {} ms", pjp.getSignature().getDeclaringTypeName(), pjp.getSignature().getName(), username, duration);

    return result;
  }
}
