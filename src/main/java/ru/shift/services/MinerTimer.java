package ru.shift.services;

import ru.shift.model.GameState;
import ru.shift.model.GameStateListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;

public final class MinerTimer implements GameStateListener {

    private final List<IntConsumer> tickListeners = new ArrayList<>();

    private final Timer timer = new Timer("miner-timer", true);

    private final AtomicInteger seconds = new AtomicInteger(0);
    private volatile TimerTask currentTask;

    public void addTickListener(IntConsumer listener) {
        if (listener != null) {
            tickListeners.add(listener);
        }
    }

    @Override
    public void onGameStateChanged(GameState state) {
        switch (state) {
            case INIT -> reset();
            case PLAY -> start();
            case WON, LOST -> stop();
        }
    }

    private void reset() {
        seconds.set(0);
        executeTick(0);
        stopInternal();
    }

    private void start() {
        seconds.set(0);
        executeTick(0);

        stopInternal();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                int s = seconds.incrementAndGet();
                executeTick(s);
            }
        };
        currentTask = task;
        timer.scheduleAtFixedRate(task, 1000L, 1000L);
    }

    private void stop() {
        stopInternal();
    }

    private void stopInternal() {
        TimerTask task = currentTask;
        if (task != null) {
            task.cancel();
            currentTask = null;
        }
    }

    private void executeTick(int secs) {
        for (IntConsumer l : tickListeners) {
            l.accept(secs);
        }
    }

}
