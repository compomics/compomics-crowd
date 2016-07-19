package com.compomics.compomicscrowd.pladiquest.control.input;

import java.util.ArrayList;

/**
 *
 * @author Kenneth Verheggen <kenneth.verheggen@gmail.com>
 */
public enum ActionTerm {
    ATTACK("attack", "punch", "kick", "stab", "assault", "lunge"),
    LOOT("take", "pick up", "loot", "obtain", "retrieve"),
    DROP("drop", "let go", "discard", "remove"),
    PUT("put", "load", "combine"),
    ACTIVATE("use","turn on", "switch on", "activate"),
    OPEN("open", "unlock"),
    READ("read", "inspect", "look at"),
    VICTORY("open exit", "escape", "do a little dance");

    private final ArrayList<String> synonyms = new ArrayList<>();

    private ActionTerm(String... synonyms) {
        for (String aSynonym : synonyms) {
            this.synonyms.add(aSynonym);
        }
    }

    public String getPrefix(String term) {
        for (String aSynonym : synonyms) {
            if (term.toLowerCase().startsWith(aSynonym)) {
                return aSynonym;
            }
        }
        return "";
    }
}
