package hugo.weaving;

import android.os.Build;
import android.os.Looper;
import android.os.Trace;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.CodeSignature;

import hugo.weaving.internal.HugoUtil;

/**
 * Created by wanghb on 17/7/3.
 */

public interface HugoCallback {

    void enterMethod(JoinPoint joinPoint, LogConfig logConfig);

    void exitMethod(JoinPoint joinPoint, Object result, long lengthMillis, LogConfig logConfig);

    class DefaultHugoCallback implements HugoCallback {

        private ThreadLocal<Integer> stackSize = new ThreadLocal<>();

        @Override
        public void enterMethod(JoinPoint joinPoint, LogConfig logConfig) {
            logConfig = interceptLogConfig(logConfig);
            if (isSkip(logConfig)) {
                return;
            }

            CodeSignature codeSignature = (CodeSignature) joinPoint.getSignature();
            Class<?> cls = codeSignature.getDeclaringType();
            String tag = HugoUtil.asTag(cls);

            StringBuilder builder = new StringBuilder(HugoUtil.extractEnterMessage(joinPoint));
            if (stackSize.get() == null) {
                stackSize.set(0);
            }
            HugoUtil.print(logConfig, tag, builder.toString(), 0, stackSize.get());
            stackSize.set(stackSize.get() + 1);

            if (logConfig.isTrace() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                builder.insert(2, ":");
                builder.insert(2, tag);
                final String section = builder.toString()
                        .substring(2, Math.min(127, builder.length()));
                Trace.beginSection(section);
            }
        }


        @Override
        public void exitMethod(JoinPoint joinPoint, Object result, long lengthMillis,
                               LogConfig logConfig) {
            logConfig = interceptLogConfig(logConfig);
            if (isSkip(logConfig)) {
                return;
            }

            if (logConfig.isTrace() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                Trace.endSection();
            }

            Signature signature = joinPoint.getSignature();
            Class<?> cls = signature.getDeclaringType();
            String tag = HugoUtil.asTag(cls);

            stackSize.set(stackSize.get() - 1);
            HugoUtil.print(logConfig, tag,
                    HugoUtil.extractExitMessage(result, lengthMillis, joinPoint), lengthMillis,
                    stackSize.get());
        }

        protected LogConfig interceptLogConfig(LogConfig logConfig) {
            return logConfig;
        }

        private static boolean isSkip(LogConfig logConfig) {
            if (logConfig.isExclude()) {
                return true;
            }
            if (logConfig.isOnlyMainThread()) {
                return Looper.myLooper() != Looper.getMainLooper();
            }
            return false;
        }
    }
}
