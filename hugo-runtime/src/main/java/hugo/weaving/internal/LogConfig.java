package hugo.weaving.internal;

import hugo.weaving.DebugLog;

/**
 * Created by wanghb on 17/7/2.
 */

class LogConfig {

    private boolean exclude;
    private int logLevel = DebugLog.VERBOSE;
    private long[] levelDuration;

    public long[] getLevelDuration() {
        return levelDuration;
    }

    public void setLevelDuration(long[] levelDuration) {
        this.levelDuration = levelDuration;
    }

    public boolean isExclude() {
        return exclude;
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
}
