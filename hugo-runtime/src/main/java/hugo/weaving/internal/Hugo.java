package hugo.weaving.internal;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.util.concurrent.TimeUnit;

@Aspect
public class Hugo {
  private static volatile HugoCallback hugoCallback = new HugoCallback.DefaultHugoCallback();

  static volatile LogConfig logConfig = new LogConfig();

  static HugoPin hugoPin = new HugoPin();

  @Pointcut("within(@hugo.weaving.DebugLog *)")
  public void withinAnnotatedClass() {}

  @Pointcut("execution(!synthetic * *(..)) && withinAnnotatedClass()")
  public void methodInsideAnnotatedType() {}

  @Pointcut("execution(!synthetic *.new(..)) && withinAnnotatedClass()")
  public void constructorInsideAnnotatedType() {}

  @Pointcut("execution(@hugo.weaving.DebugLog * *(..)) || methodInsideAnnotatedType()")
  public void method() {}

  @Pointcut("execution(@hugo.weaving.DebugLog *.new(..)) || constructorInsideAnnotatedType()")
  public void constructor() {}

  public static void setLogConfig(LogConfig logConfig) {
    Hugo.logConfig = logConfig;
  }

  public static void setHugoCallback(HugoCallback hugoCallback) {
    Hugo.hugoCallback = hugoCallback;
  }

  public static void pin(String pin) {
    Hugo.hugoPin.pin(pin);
  }

  @Around("method() || constructor()")
  public Object logAndExecute(ProceedingJoinPoint joinPoint) throws Throwable {
    LogConfig logConfig = HugoUtil.parseLogConfig(joinPoint);
    enterMethod(joinPoint, logConfig);

    long startNanos = System.nanoTime();
    Object result = joinPoint.proceed();
    long stopNanos = System.nanoTime();
    long lengthMillis = TimeUnit.NANOSECONDS.toMillis(stopNanos - startNanos);

    exitMethod(joinPoint, result, lengthMillis, logConfig);

    return result;
  }

  private static void enterMethod(JoinPoint joinPoint, LogConfig logConfig) {
    if (hugoCallback != null) {
      hugoCallback.enterMethod(joinPoint, logConfig);
    }
  }

  private static void exitMethod(JoinPoint joinPoint, Object result, long lengthMillis,
                                 LogConfig logConfig) {
    if (hugoCallback != null) {
      hugoCallback.exitMethod(joinPoint, result, lengthMillis, logConfig);
    }
  }
}
