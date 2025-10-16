package org.chatassistant.tasks.task.base;

import org.chatassistant.tasks.task.Task;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class BaseTask implements Task {
    private AtomicBoolean running = new AtomicBoolean(false);

    @Override
    public abstract void run();

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public void start() {
        running.set(true);
    }

    @Override
    public void stop() {
        running.set(false);
    }
}
