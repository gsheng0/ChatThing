package org.chatassistant.ai.openai;

import java.util.List;

public class Session {
    private String model;
    private List<Content> sessionContext;

    private Session(final String model){
        this.model = model;
    }

    public void addContext(final Content content){
        sessionContext.add(content);
    }

    public List<Content>  getContext(){
        return sessionContext;
    }

    public String getModel(){
        return model;
    }

    public static Session of(final String model){
        return new Session(model);
    }

    public String toString(){
        return "[" + model + ": " + sessionContext + "]";
    }
}
