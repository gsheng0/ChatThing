package org.chatassistant.ai.openai;

public class Content {
    private String type;
    private String value;
    private Content(final String type, final String value){
        this.type = type;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public static Content of(final String type, final String value){
        return new Content(type, value);
    }

    public String toString(){
        return "[" + type + ": " + value + "]";
    }
}
