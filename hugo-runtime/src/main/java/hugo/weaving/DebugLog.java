package hugo.weaving;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

@Target({TYPE, METHOD, CONSTRUCTOR})
@Retention(CLASS)
public @interface DebugLog {

    int enable() default DEFAULT;

    boolean exclude() default false;

    int level() default DEFAULT;

    long[] levelDuration() default {50, 40, 30, 20, 10};

    int trace() default DEFAULT;

    int onlyMainThread() default DEFAULT;

    int DEFAULT = -1;

    int TRUE = -2;

    int FALSE = -3;

    /**
     * Priority constant for the println method; use Log.v.
     */
    int VERBOSE = 2;

    /**
     * Priority constant for the println method; use Log.d.
     */
    int DEBUG = 3;

    /**
     * Priority constant for the println method; use Log.i.
     */
    int INFO = 4;

    /**
     * Priority constant for the println method; use Log.w.
     */
    int WARN = 5;

    /**
     * Priority constant for the println method; use Log.e.
     */
    int ERROR = 6;
}