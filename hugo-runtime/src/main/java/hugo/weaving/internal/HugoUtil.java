package hugo.weaving.internal;

import android.os.Looper;
import android.util.Log;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

import hugo.weaving.DebugLog;
import hugo.weaving.Hugo;
import hugo.weaving.LogConfig;

/**
 * Created by wanghb on 17/7/7.
 */

public class HugoUtil {

    public static String extractEnterMessage(JoinPoint joinPoint) {
        CodeSignature codeSignature = (CodeSignature) joinPoint.getSignature();
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
        return builder.toString();
    }

    public static String extractExitMessage(Object result, long lengthMillis, JoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        String methodName = signature.getName();
        boolean hasReturnType = signature instanceof MethodSignature && ((MethodSignature) signature)
                .getReturnType() != void.class;

        StringBuilder builder = new StringBuilder("\u21E0 ").append(methodName)
                .append(" [")
                .append(lengthMillis)
                .append("ms]");

        if (Looper.myLooper() != Looper.getMainLooper()) {
            builder.append(" [Thread:\"").append(Thread.currentThread().getName()).append("\"]");
        }

        if (hasReturnType) {
            builder.append(" = ");
            builder.append(Strings.toString(result));
        }
        return builder.toString();
    }

    public static String asTag(Class<?> cls) {
        if (cls.isAnonymousClass()) {
            String tag = asTag(cls.getEnclosingClass());
            if (cls.getSuperclass() == null) {
                return tag;
            }
            if (!cls.getSuperclass().equals(Object.class)) {
                return cls.getSuperclass().getSimpleName() + "@" + tag;
            }
            Class<?>[] classes = cls.getInterfaces();
            if (classes == null || classes.length <= 0) {
                return tag;
            }
            StringBuilder stringBuilder = new StringBuilder();
            boolean firstTime = true;
            for (Class<?> c : classes) {
                if (firstTime) {
                    firstTime = false;
                } else {
                    stringBuilder.append(",");
                }
                stringBuilder.append(c.getSimpleName());
            }
            if (stringBuilder.length() > 50 - tag.length()) {
                stringBuilder.delete(50 - tag.length(), stringBuilder.length());
                stringBuilder.append("...");
            }
            stringBuilder.append("@").append(tag);
            return stringBuilder.toString();
        }
        return cls.getSimpleName();
    }

    public static LogConfig parseLogConfig(JoinPoint joinPoint) {
        LogConfig logConfig = Hugo.logConfig.copy();
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
            debugLog = findDebugLogAnnotation(cls);
        }
        if (debugLog == null) {
            return logConfig;
        }
        logConfig.setExclude(debugLog.exclude());
        logConfig.setLogLevel(debugLog.level());
        logConfig.setLevelDuration(debugLog.levelDuration());
        if (debugLog.enable() != DebugLog.DEFAULT) {
            logConfig.setEnable(debugLog.enable() == DebugLog.TRUE);
        }
        if (debugLog.trace() != DebugLog.DEFAULT) {
            logConfig.setTrace(debugLog.trace() == DebugLog.TRUE);
        }
        if (debugLog.onlyMainThread() != DebugLog.DEFAULT) {
            logConfig.setOnlyMainThread(debugLog.onlyMainThread() == DebugLog.TRUE);
        }
        return logConfig;
    }

    private static DebugLog findDebugLogAnnotation(Class<?> cls) {
        if (cls == null) {
            return null;
        }
        DebugLog debugLog = cls.getAnnotation(DebugLog.class);
        if (debugLog != null) {
            return debugLog;
        }
        return findDebugLogAnnotation(cls.getEnclosingClass());
    }

    public static void print(LogConfig logConfig, String tag, String log, long duration,
                             int stackSize) {
        int level;
        if (logConfig.getLogLevel() != DebugLog.DEFAULT) {
            level = logConfig.getLogLevel();
        } else {
            level = logConfig.getLogLevelByDuration(duration);
            if (level < DebugLog.VERBOSE) {
                level = DebugLog.VERBOSE;
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < stackSize; ++i) {
            stringBuilder.append("  ");
        }
        stringBuilder.append(log);
        log = stringBuilder.toString();
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
