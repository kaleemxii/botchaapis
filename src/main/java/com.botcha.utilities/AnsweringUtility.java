package com.botcha.utilities;

import com.botcha.dataschema.Message;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by xy on 31/1/16.
 */
public class AnsweringUtility {
    private static Pattern nouns = Pattern.compile("hackathon|lunch|traffic|alert[s]?|event[s]?|presentation[s]?|bed[s]?");
    private static Pattern timeQuestions = Pattern.compile("when(?: is)?|(?:what )?time(?: is)?");
    private static Pattern timeAnswers = Pattern.compile("at|\\d[a|p]m|");
    private static Pattern placeQuestions = Pattern.compile("where(?: is)?");
    private static Pattern placeAnswers = Pattern.compile("is|at|in");


    public static String getQuestionAnswerFromMessages(String question, List<Message> messages, boolean withChannelTag, int top) {
        question = question.toLowerCase();

        int placeQuestion, timeQuestion;
        HashSet<String> questionNouns = new HashSet<>();

        Matcher m = nouns.matcher(question);
        while (m.find()) {
            questionNouns.add(m.group());
        }

        if (questionNouns.isEmpty()) return null;

        timeQuestion = getMatchesCount(timeQuestions.matcher(question));
        placeQuestion = getMatchesCount(placeQuestions.matcher(question));

        TreeMap<Integer, Message> map = new TreeMap<>(Collections.reverseOrder());

        //       Message maxMatchMessage = null;
//        int maxScore = -1;
        Pattern questionNounsPattern = Pattern.compile(StringUtils.join(questionNouns, '|'));
        for (Message message : messages) {
            ////System.out.println(message.text);
            int nounCount = getMatchesCount(questionNounsPattern.matcher(message.text));
            if (nounCount > 0) {
                int timeScore = getMatchesCount(timeAnswers.matcher(message.text));
                int placeScore = getMatchesCount(placeAnswers.matcher(message.text));
                int score = nounCount * questionNouns.size() + timeScore * timeQuestion + placeScore * placeQuestion;
                map.put(score, message);
//                if (maxScore < timeScore + placeScore) {
//                    maxScore = timeScore * timeQuestion + placeScore * placeQuestion;
//                    maxMatchMessage = message;
//                }
            }

        }

        if (map.size() == 0) return null;
        StringBuilder sb = new StringBuilder();
        for (Message message : map.values()) {

            if (withChannelTag) {
                sb.append('#').append(DataBase.getChannelByChannelId(message.channelId).channelTag).append(' ');
            }
            sb.append('@').
                    append(DataBase.getUserByUserId(message.fromUserId).userTag)
                    .append(message.text)
                    .append('\n');
            if (--top == 0) break;
        }

        return sb.toString();
    }

    private static int getMatchesCount(Matcher m) {
        int i = 0;
        while (m.find()) {
            if (!m.group().isEmpty()) {
                i++;
            }
        }
        return i;
    }
}