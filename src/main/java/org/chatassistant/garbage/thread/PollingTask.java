package org.chatassistant.garbage.thread;

import org.chatassistant.entities.Message;
import org.chatassistant.data.MessageDB;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingDeque;

public class PollingTask implements Runnable {
    private static final int PASSIVE = 0;
    private static final int ACTIVE = 1;

    private final BlockingDeque<List<Message>> messageDeque;
    private final Set<Message> messageSet;
    private final int[] sleepTimes;
    private final MessageDB messageDB;

    private int status = 0;
    private int counter = 5;
    private boolean running;

    public PollingTask(final BlockingDeque<List<Message>> messageDeque, int passivePollingSleep, int activePollingSleep) {
        messageDB = MessageDB.getInstance();
        this.messageSet = new HashSet<>(messageDB.getRecentMessages());
        this.messageDeque = messageDeque;
        sleepTimes = new int[]{ passivePollingSleep, activePollingSleep };
        this.running = true;
    }

    public PollingTask(final BlockingDeque<List<Message>> messageDeque) {
        this(messageDeque, 5, 1);
    }


    @Override
    public void run() {
        System.out.println("Polling Thread: Fetching messageSet...");
        while (running) {
            try {
                final List<Message> recentMessages = messageDB.getRecentMessages();
                final List<Message> messageBatch = new ArrayList<>();
                boolean newMessages = false;
                for(final Message message : recentMessages) {
                    if(messageSet.contains(message) || message.getText().startsWith("[Intern]:")){
                        continue;
                    }
                    newMessages = true;
                    messageBatch.add(message);
                    messageSet.add(message);
                    System.out.println("Processed message: ");
                    System.out.println(message);
                }
                if(newMessages){
                    messageDeque.addLast(messageBatch);
                }

                if(newMessages) {
                    status = ACTIVE;
                    counter = 5;
                } else if(--counter <= 0){
                    status = PASSIVE;
                }
                Thread.sleep(sleepTimes[status] * 1000L);
            } catch (InterruptedException e) {
                System.out.println("Polling Thread: Interrupted.");
                Thread.currentThread().interrupt();
            }
        }
    }

    public void stop(){
        running = false;
    }
}
