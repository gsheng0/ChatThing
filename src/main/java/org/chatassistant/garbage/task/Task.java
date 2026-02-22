package org.chatassistant.task;

public interface Task extends Runnable {

    void start();

    void stop();

    boolean isRunning();
}
