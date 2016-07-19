package com.compomics.compomicscrowd.pladiquest.model.conversation;

import com.compomics.compomicscrowd.pladiquest.control.input.ActionTerm;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import org.springframework.core.io.ClassPathResource;

/**
 *
 * @author Kenneth Verheggen <kenneth.verheggen@gmail.com>
 */
public class ConversationLibrary extends HashMap<ActionTerm, HashMap<Boolean, List<String>>> {

    private static final ConversationLibrary INSTANCE = new ConversationLibrary();
    private static final Random rand = new Random();

    private ConversationLibrary() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ClassPathResource("replies.txt").getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isEmpty()) {
                    String[] split = line.split("\\|");
                    ActionTerm term = ActionTerm.valueOf(split[0].substring(1).toUpperCase());
                    if (term != null) {
                        HashMap<Boolean, List<String>> replyMap = getOrDefault(term, new HashMap<>());
                        boolean positiveReply = split[1].equalsIgnoreCase("+");
                        List<String> replies = replyMap.getOrDefault(positiveReply, new ArrayList<>());
                        replies.add(split[2]);
                        replyMap.put(positiveReply, replies);
                        put(term, replyMap);
                    }
                }
            }
        } catch (IOException ex) {

        }
    }

    public String getRandomReply(ActionTerm term, boolean positive) {
        List<String> replies = get(term).get(positive);
        String reply;
        if (replies != null && !replies.isEmpty()) {
            int index = rand.nextInt(replies.size() - 1);
            return replies.get(index);
        } else {
            reply = "I have no clue what to do...";
        }
        return reply;
    }

    public static ConversationLibrary getInstance() {
        return INSTANCE;
    }

}
