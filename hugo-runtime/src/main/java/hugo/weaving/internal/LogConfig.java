package hugo.weaving.internal;

import hugo.weaving.DebugLog;

/**
 * Created by wanghb on 17/7/2.
 */

public class LogConfig {

    private boolean enable = true;
    private boolean exclude = false;
    private int logLevel = DebugLog.DEFAULT;
    private long[] levelDuration = new long[]{50, 40, 30, 20, 10};
    private boolean trace = true;
    private boolean onlyMainThread = false;

    public void setLevelDuration(long[] levelDuration) {
        this.levelDuration = levelDuration;
    }

    public boolean isExclude() {
        return !enable || exclude;
    }

    public void setExclude(boolean exclude) {
        this.exclude = exclude;
    }

    public int getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(int logLevel) {
        this.logLevel = logLevel;
    }

    public int getLogLevelByDuration(long duration) {
        if (levelDuration == null || levelDuration.length < 1) {
            return getLogLevel();
        }
        for (int i = 0; i < levelDuration.length; ++i) {
            if (duration >= levelDuration[i]) {
                return Math.max(DebugLog.ERROR - i, DebugLog.VERBOSE);
            }
        }
        return getLogLevel();
    }

    public boolean isTrace() {
        return trace;
    }

    public void setTrace(boolean trace) {
        this.trace = trace;
    }

    public boolean isOnlyMainThread() {
        return onlyMainThread;
    }

    public void setOnlyMainThread(boolean onlyMainThread) {
        this.onlyMainThread = onlyMainThread;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    LogConfig copy() {
        LogConfig logConfig = new LogConfig();
        logConfig.setEnable(enable);
        logConfig.setLevelDuration(levelDuration);
        logConfig.setLogLevel(logLevel);
        logConfig.setExclude(exclude);
        logConfig.setTrace(trace);
        logConfig.setOnlyMainThread(onlyMainThread);
        return logConfig;
    }
}
