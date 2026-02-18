package org.chatassistant.tasks.task;

public interface Task extends Runnable {

    void start();

    void stop();

    boolean isRunning();
}
/*
I want some sort of easy interface for "linking" tasks:
ProducerTask ptask = new ProducerTask_Impl();
ConsumerTask ctask = new ConsumerTask_Impl();
ConsumerTask ctask1 = new ConsumerTask_Impl();

ptask.registerConsumer(ctask);
ptask.registerConsumer(ctask2);

Should each consumer only take from one producer?
Multiple producers for one consumer seems to be asking for complication
Maybe if multiple sources need to feed into one consumer?
Not sure what cases there will be for that
But I guess it takes more work to guarantee that there is only one producer for each consumer

How should the internals work?
Default implementations for:
    void registerConsumer(ConsumerTask)
    A produce()
    void consume(B)

Processing task should have both produce() and consume() be one? How would that be enforced cleanly?
    Probably just write another interface instead of trying to make this too elegant
    but then I'll need to add another method for registering consumers
    Consumer interface should need a "getInputQueue"

ConsumerTask.java
run() {
    while(running) {
        while(!inputDeque.isEmpty()) {
            consume(inputDeque.getFirst())
        }
    }
}

ProducerTask.java
run() {
    while(running) {
        output = produce();
        for (deque : registeredInputDeques) {
            deque.addLast(output)
        }
    }
}

ProcesserTask.java
run() {
    while(running) {
        while(!inputDeque.isEmpty()) {
            consume();
            output = produce();
            for (deque : registeredInputDeques) {
                deque.addLast(output);
            }
        }
    }
}


Wrapper class around BlockingDeque to avoid modifications?
    Probably not needed since BlockingDeque is being hidden away from the implementations for Producer/Consumer Task


 */
