package org.chatassistant.task.runner;

public abstract class Runner implements Runnable {
    private volatile boolean running = true;

    protected boolean isRunning() {
        return running;
    }

    public void stop() {
        running = false;
    }
}
