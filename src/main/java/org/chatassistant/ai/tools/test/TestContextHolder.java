package org.chatassistant.ai.tools.test;

import java.util.HashMap;
import java.util.Map;

public class TestContextHolder {

    public static Map<String, Double> ledger = new HashMap<>();
    public static void reset(){
        ledger.clear();
    }
}
