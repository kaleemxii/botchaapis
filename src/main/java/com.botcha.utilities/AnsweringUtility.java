package com.botcha.utilities;

import com.botcha.dataschema.Message;
import com.google.common.collect.Lists;

import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by xy on 31/1/16.
 */
public class AnsweringUtility {
    private static Pattern nouns = Pattern.compile("hackathon|lunch|presentation(s)?");
    private static Pattern timeQuestions = Pattern.compile("when|(what )?time( is)?");
    private static Pattern timeAnswers = Pattern.compile("at|\\d[a|p]m|");
    private static Pattern placeQuestions = Pattern.compile("where( is)?|");
    private static Pattern placeAnswers = Pattern.compile("is|at|in");


    public static Message getQuestionAnswerFromMessages(String question, List<Message> messages) {
        question = question.toLowerCase();

        int placeQuestion, timeQuestion;
        HashSet<String> questionNouns = new HashSet<>();
        Matcher m = nouns.matcher(question);

        if (!m.find()) {
            return null;
        }
        for (int groupIdx = 0; groupIdx < m.groupCount() + 1; groupIdx++) {
            questionNouns.add(m.group(groupIdx));
        }
        timeQuestion = timeQuestions.matcher(question).groupCount();
        placeQuestion = placeQuestions.matcher(question).groupCount();

        Message maxMatchMessage = null;
        int maxScore = -1;

        for (Message message : Lists.reverse(messages)) {
            if (message.text.matches(String.join("|", questionNouns))) {
                int timeScore = timeAnswers.matcher(question).groupCount();
                int placeScore = placeAnswers.matcher(question).groupCount();

                if (maxScore < timeScore + placeScore) {
                    maxScore = timeScore * timeQuestion + placeScore * placeQuestion;
                    maxMatchMessage = message;
                }
            }
        }

        return maxMatchMessage;
    }
}