package com.example.hugo;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.TextView;

import hugo.weaving.DebugLog;
import hugo.weaving.internal.Hugo;

@DebugLog(onlyMainThread = DebugLog.TRUE)
public class HugoActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Hugo.pin("super.OnCreate");
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setText("Check logcat!");
        Hugo.pin("setContentView");
        setContentView(tv);

        printArgs("The", "Quick", "Brown", "Fox");

        Hugo.pin("fibonacci 7");
        Log.i("Fibonacci", "fibonacci's 4th number is " + fibonacci(7));
        Hugo.pin("new Greeter(\"Jake\")");
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

    @DebugLog(onlyMainThread = DebugLog.FALSE)
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
                Hugo.pin("run");
                sleepyMethod(SOME_POINTLESS_AMOUNT_OF_TIME);
                Hugo.pin("sleepyMethod");
                sleepyMethodLogOnChildThread(SOME_POINTLESS_AMOUNT_OF_TIME);
                Hugo.pin("sleepyMethodLogOnChildThread");
                fibonacci(9);
                Hugo.pin("fibonacci 9");
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
