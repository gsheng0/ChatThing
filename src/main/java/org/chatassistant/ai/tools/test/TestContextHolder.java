package org.chatassistant.ai.tools.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestContextHolder {

    public static Map<String, Double> ledger = new HashMap<>();

    /** Each entry is (chatName, message) recorded by SendTextMessageTest */
    public static List<String[]> sendTextMessageCalls = new ArrayList<>();

    public static void reset(){
        ledger.clear();
        sendTextMessageCalls.clear();
    }
}
