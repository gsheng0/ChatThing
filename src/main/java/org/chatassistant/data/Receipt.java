package org.chatassistant.data;

import lombok.Getter;

public class Receipt {
    @Getter
    private final String receipt;
    @Getter
    private final String name;

    private Receipt(final String prompt, final String name){
        this.receipt = prompt;
        this.name = name;
    }

    public static Receipt of(final String name, final String prompt){
        return new Receipt(name, prompt);
    }
}
