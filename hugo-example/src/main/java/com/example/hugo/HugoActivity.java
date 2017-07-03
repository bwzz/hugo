package com.example.hugo;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.TextView;

import hugo.weaving.DebugLog;

@DebugLog(onlyMainThread = DebugLog.TRUE)
public class HugoActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setText("Check logcat!");
        setContentView(tv);

        printArgs("The", "Quick", "Brown", "Fox");

        Log.i("Fibonacci", "fibonacci's 4th number is " + fibonacci(7));

        Greeter greeter = new Greeter("Jake");
        Log.d("Greeting", greeter.sayHello());
        Log.d("Greeting", greeter.sayHello2());

        Charmer charmer = new Charmer("Jake");
        Log.d("Charming", charmer.askHowAreYou());

        startSleepyThread();
    }

    @DebugLog(exclude = false, level = DebugLog.ERROR, levelDuration = {3, 2, 1})
    private void printArgs(String... args) {
        for (String arg : args) {
            Log.i("Args", arg);
        }
    }

    @DebugLog(levelDuration = {3, 2, 1}, onlyMainThread = DebugLog.FALSE)
    private int fibonacci(int number) {
        if (number <= 0) {
            throw new IllegalArgumentException("Number must be greater than zero.");
        }
        if (number == 1 || number == 2) {
            return 1;
        }
        // NOTE: Don't ever do this. Use the iterative approach!
        return fibonacci(number - 1) + fibonacci(number - 2);
    }

    private void startSleepyThread() {
        new Thread(new Runnable() {
            private static final long SOME_POINTLESS_AMOUNT_OF_TIME = 50;

            @Override
            public void run() {
                sleepyMethod(SOME_POINTLESS_AMOUNT_OF_TIME);
                sleepyMethodLogOnChildThread(SOME_POINTLESS_AMOUNT_OF_TIME);
                fibonacci(9);
            }

            @DebugLog(onlyMainThread = DebugLog.TRUE)
            private void sleepyMethod(long milliseconds) {
                SystemClock.sleep(milliseconds);
            }

            private void sleepyMethodLogOnChildThread(long milliseconds) {
                SystemClock.sleep(milliseconds);
            }
        }, "I'm a lazy thr.. bah! whatever!").start();
    }

    @DebugLog(exclude = true)
    static class Greeter {
        private final String name;

        Greeter(String name) {
            this.name = name;
        }

        private String sayHello() {
            return "Hello, " + name;
        }

        @DebugLog
        private String sayHello2() {
            return "Hello2, " + name;
        }
    }

    @DebugLog
    static class Charmer {
        private final String name;

        private Charmer(String name) {
            this.name = name;
        }

        public String askHowAreYou() {
            return "How are you " + name + "?";
        }
    }
}
