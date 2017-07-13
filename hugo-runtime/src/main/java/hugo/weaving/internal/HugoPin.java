package hugo.weaving.internal;

import android.os.Looper;

import java.util.concurrent.TimeUnit;

import hugo.weaving.Hugo;

/**
 * Created by wanghb on 17/7/7.
 */

public class HugoPin {

    private ThreadLocal<Long> lastNanoTime = new ThreadLocal<>();

    private ThreadLocal<Integer> counter = new ThreadLocal<>();

    public void pin(String pin) {
        StringBuilder stringBuilder = new StringBuilder("\u23F0 : ");
        long duration = Long.MAX_VALUE;
        if (lastNanoTime.get() == null) {
            stringBuilder.append(pin);
            lastNanoTime.set(System.nanoTime());
            counter.set(1);
        } else {
            long nanoTime = System.nanoTime();
            duration = TimeUnit.NANOSECONDS.toMillis(nanoTime - lastNanoTime.get());
            stringBuilder.append(counter.get());
            stringBuilder.append(" [").append(duration).append("ms] : ").append(pin);
            lastNanoTime.set(nanoTime);
            counter.set(counter.get() + 1);
        }
        if (Looper.myLooper() != Looper.getMainLooper()) {
            stringBuilder.append(" [Thread:\"")
                    .append(Thread.currentThread().getName())
                    .append("\"]");
        }
        HugoUtil.print(Hugo.logConfig, "HugoPin", stringBuilder.toString(), duration, 0);
    }
}
