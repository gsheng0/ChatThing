package org.chatassistant.entities;

public class Pair<A, B> {
    public static <A, B> Pair<A, B> of(A a, B b){
        return new Pair<>(a,b);
    }

    private final A a;
    private final B b;
    private Pair(A a, B b){
        this.a = a;
        this.b = b;
    }

    public A getFirst(){
        return a;
    }

    public B getSecond(){
        return b;
    }

    public String toString(){
        return "[" + a.toString() + ", " + b.toString() + "]";
    }
}
