package org.chatassistant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Negative amounts in spreadsheet = that person should send money
// Positive amounts = that person should receive money
@SpringBootApplication
public class Main {
    public static final String TEST_PROMPT_PATH = "/Users/georgesheng/proj/scheduler2/src/main/resources/testPrompt";
    public static final String TEST_PROMPT_PATH2 = "/Users/georgesheng/proj/scheduler2/src/main/resources/testPrompt2";
    public static final String NORMAL_PROMPT_PATH = "/Users/georgesheng/proj/scheduler2/src/main/resources/expenseTrackingPrompt";
    public static final String NORMAL_PROMPT_PATH2 = "/Users/georgesheng/proj/scheduler2/src/main/resources/expenseTrackingPrompt2";
    public static final String NORMAL_PROMPT_PATH3 = "/Users/georgesheng/proj/scheduler2/src/main/resources/expenseTrackingPrompt3";
    public static final String EXTRACT_RECEIPT_PROMPT_PATH = "/Users/georgesheng/proj/scheduler2/src/main/resources/extractReceiptPrompt";

    public static final String PROMPT = NORMAL_PROMPT_PATH3;

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
