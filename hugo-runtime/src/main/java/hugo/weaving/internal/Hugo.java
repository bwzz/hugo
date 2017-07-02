package hugo.weaving.internal;

import android.os.Build;
import android.os.Looper;
import android.os.Trace;
import android.util.Log;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import hugo.weaving.DebugLog;

@Aspect
public class Hugo {
  private static volatile boolean enabled = true;

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

  public static void setEnabled(boolean enabled) {
    Hugo.enabled = enabled;
  }

  @Around("method() || constructor()")
  public Object logAndExecute(ProceedingJoinPoint joinPoint) throws Throwable {
    enterMethod(joinPoint);

    long startNanos = System.nanoTime();
    Object result = joinPoint.proceed();
    long stopNanos = System.nanoTime();
    long lengthMillis = TimeUnit.NANOSECONDS.toMillis(stopNanos - startNanos);

    exitMethod(joinPoint, result, lengthMillis);

    return result;
  }

  private static void enterMethod(JoinPoint joinPoint) {
    if (!enabled) return;

    LogConfig logConfig = parseLogConfig(joinPoint);
    if (logConfig.isExclude()) {
      return;
    }

    CodeSignature codeSignature = (CodeSignature) joinPoint.getSignature();

    Class<?> cls = codeSignature.getDeclaringType();
    String methodName = codeSignature.getName();
    String[] parameterNames = codeSignature.getParameterNames();
    Object[] parameterValues = joinPoint.getArgs();

    StringBuilder builder = new StringBuilder("\u21E2 ");
    builder.append(methodName).append('(');
    for (int i = 0; i < parameterValues.length; i++) {
      if (i > 0) {
        builder.append(", ");
      }
      builder.append(parameterNames[i]).append('=');
      builder.append(Strings.toString(parameterValues[i]));
    }
    builder.append(')');

    if (Looper.myLooper() != Looper.getMainLooper()) {
      builder.append(" [Thread:\"").append(Thread.currentThread().getName()).append("\"]");
    }

    log(logConfig, asTag(cls), builder.toString(), 0);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
      final String section = builder.toString().substring(2);
      Trace.beginSection(section);
    }
  }

  private static void exitMethod(JoinPoint joinPoint, Object result, long lengthMillis) {
    if (!enabled) return;

    LogConfig logConfig = parseLogConfig(joinPoint);
    if (logConfig.isExclude()) {
      return;
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
      Trace.endSection();
    }

    Signature signature = joinPoint.getSignature();

    Class<?> cls = signature.getDeclaringType();
    String methodName = signature.getName();
    boolean hasReturnType = signature instanceof MethodSignature
        && ((MethodSignature) signature).getReturnType() != void.class;

    StringBuilder builder = new StringBuilder("\u21E0 ")
        .append(methodName)
        .append(" [")
        .append(lengthMillis)
        .append("ms]");

    if (hasReturnType) {
      builder.append(" = ");
      builder.append(Strings.toString(result));
    }

    log(logConfig, asTag(cls), builder.toString(), lengthMillis);
  }

  private static String asTag(Class<?> cls) {
    if (cls.isAnonymousClass()) {
      return asTag(cls.getEnclosingClass());
    }
    return cls.getSimpleName();
  }

  private static LogConfig parseLogConfig(JoinPoint joinPoint) {
    LogConfig logConfig = new LogConfig();
    Signature signature = joinPoint.getSignature();
    DebugLog debugLog = null;
    if (signature instanceof MethodSignature) {
      Method method = ((MethodSignature) signature).getMethod();
      if (method != null) {
        debugLog = method.getAnnotation(DebugLog.class);
      }
    }
    if (debugLog == null) {
      Class<?> cls = signature.getDeclaringType();
      debugLog = cls.getAnnotation(DebugLog.class);
    }
    if (debugLog == null) {
      return logConfig;
    }
    logConfig.setExclude(debugLog.exclude());
    logConfig.setLogLevel(debugLog.level());
    logConfig.setLevelDuration(debugLog.levelDuration());
    return logConfig;
  }

  private static void log(LogConfig logConfig, String tag, String log, long duration) {
    int level;
    if (logConfig.getLogLevel() != DebugLog.DEFAULT) {
      level = logConfig.getLogLevel();
    } else {
      level = logConfig.getLogLevelByDuration(duration);
      if (level < DebugLog.VERBOSE) {
        level = DebugLog.VERBOSE;
      }
    }
    switch (level) {
      case DebugLog.DEBUG:
        Log.d(tag, log);
        break;
      case DebugLog.ERROR:
        Log.e(tag, log);
        break;
      case DebugLog.INFO:
        Log.i(tag, log);
        break;
      case DebugLog.VERBOSE:
        Log.v(tag, log);
        break;
      case DebugLog.WARN:
        Log.w(tag, log);
        break;
      default:
        Log.e(tag, log);
    }
  }
}
