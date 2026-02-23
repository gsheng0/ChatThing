package org.chatassistant.ai.tools;

import org.chatassistant.ai.tools.annotation.AiAgentTool;
import org.chatassistant.entities.Pair;

import java.util.*;

@AiAgentTool
public class Settle {
    private final GetSummary getSummary;

    public Settle(final GetSummary getSummary) {
        this.getSummary = getSummary;
    }

    /**
     * Generates a set of payments that will clear out any recorded debts if applied, aka "settling up"
     * @return a map mapping a person to a Pair of a String and a Double, representing the transactions that person needs to pay out,
     *          specified by name and amount
     */
    public Map<String, List<Pair<String, Double>>> settle() {
        final Map<String, Double> ledger = getSummary.getSummary();
        final List<String> negative = new ArrayList<>();
        final List<String> positive = new ArrayList<>();
        for (final String name : ledger.keySet()) {
            final double amount = ledger.get(name);
            if (amount < 0) {
                negative.add(name);
            } else if (amount > 0) {
                positive.add(name);
            }
        }
        negative.sort(Comparator.comparingDouble(ledger::get));
        positive.sort((a, b) -> Double.compare(ledger.get(b), ledger.get(a)));

        final Map<String, List<Pair<String, Double>>> transactions = new HashMap<>();
        int posIndex = 0;
        for (final String sender : negative) {
            double amountOwed = -1 * ledger.get(sender);
            final List<Pair<String, Double>> currentTransactions = new ArrayList<>();
            while (amountOwed > 0) {
                final String receiver = positive.get(posIndex);
                double positiveAmount = ledger.get(receiver);
                if (positiveAmount >= amountOwed) {
                    currentTransactions.add(Pair.of(receiver, amountOwed));
                    positiveAmount -= amountOwed;
                    ledger.put(receiver, positiveAmount);
                    amountOwed = 0;
                } else {
                    currentTransactions.add(Pair.of(receiver, positiveAmount));
                    amountOwed -= positiveAmount;
                    posIndex++;
                }
            }
            transactions.put(sender, currentTransactions);
        }
        return transactions;
    }
}
