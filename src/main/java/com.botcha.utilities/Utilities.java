package com.botcha.utilities;

import com.botcha.dataschema.Channel;
import com.botcha.dataschema.GeoCoordinates;
import com.botcha.dataschema.Message;
import com.botcha.dataschema.User;
import com.google.common.collect.Lists;
import com.google.gson.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Utilities {

    public static Gson Gson = new Gson();

    public static void removeUserFromChannelsExcept(String userId, List<Channel> channels) {
        Set<String> channelIds = new HashSet<>(channels.size());
        for (Channel channel : channels) {
            channelIds.add(channel.channelID);
        }

        for (Channel channel : DataBase.getGeoFenceChannels()) {

            if (!channelIds.contains(channel.channelID)) {
                channel.removeUser(userId);
            }
        }
    }


    public static String sendGet(String url) throws IOException {
        System.out.println(url);
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        int responseCode = con.getResponseCode();
        return getInputStream(con.getInputStream()).toString();
    }

    public static String getInputStream(InputStream stream) throws IOException {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(stream));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }



    public static void sendMessageToAllUserInChannel(Channel channel, String message) throws IOException {
        for (User user : channel.getUsers()) {
            sendMessageToUser(user.userId, channel, message);
        }
    }

    public static void sendMessageToUser(int userId, Channel channel, String message) throws IOException {
        //https://api.telegram.org/bot125820728:AAGMxfd0FMD48rVZIhz4CuGCwShtr-afZ4U/sendmessage?chat_id=113462548&text=fku
        String url = null;
        try {
            url = new StringBuffer("https://api.telegram.org/bot")
                    .append(channel.getChanneldToken())
                    .append("/sendmessage?chat_id=")
                    .append(userId)
                    .append("&text=")
                    .append(URLEncoder.encode(message, "UTF-8")).toString();
            sendGet(url);
        } catch (Exception e) {
            throw new IOException(e);
        }

    }


    public static List<Channel> getChannels(double latitude, double longitude) throws IOException {
        List<Channel> channels = new ArrayList<Channel>();
        for (Channel channel : DataBase.getGeoFenceChannels()) {
            if (channel.geofence.Contains(latitude, longitude)) {
                channels.add(channel);
            }
        }
        return channels;
    }

    public static String getTopSummaryForChannel(Channel channel, int maxCount) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (Message message : getChannelUpdates(channel, false, maxCount)) {
            sb.append(message.text).append('\n');

        }
        return sb.length() == 0 ? "No messages yet on this channel.." : sb.toString();
    }

    public static String getMessageAnswer(User user, String question) throws IOException {
        GeoCoordinates coordinates = user.getCoordinates();
        List<Channel> channels = getChannels(coordinates.latitude, coordinates.longitude);
        List<Message> messages = new ArrayList<>();
        for (Channel channel : channels) {
            messages.addAll(getChannelUpdates(channel, true, 50));
        }
        String answer = AnsweringUtility.getQuestionAnswerFromMessages(question, messages, true, 3);

        return answer != null ? "Found some relevant information for you ..." + answer :
                "Sorry, couldn't find an answer for you..";
    }


    public static String getMessageAnswerFromChannel(Channel channel,
                                                     String question) throws IOException {
        String answer = AnsweringUtility.getQuestionAnswerFromMessages(question,
                getChannelUpdates(channel, true, 50), false, 3);


        return answer == null ? "Sorry couldn't find an answer for you.. try asking admin of channel directly by '@admin [your question]'" :
                "Found some relevant information for you ... : " + answer;
    }

    public static List<Message> getChannelUpdates(Channel channel,
                                                  boolean getBotOnlyPosts, int maxCount) throws IOException {

        List<Message> messages = new ArrayList<>();

        //https://api.telegram.org/bot125820728:AAGMxfd0FMD48rVZIhz4CuGCwShtr-afZ4U/getupdates
        String updates = sendGet("https://api.telegram.org/bot" + channel.getChanneldToken() + "/getupdates");
        JsonElement jelement = new JsonParser().parse(updates);
        JsonObject jobject = jelement.getAsJsonObject();
        JsonPrimitive ok = jobject.getAsJsonPrimitive("ok");
        if (ok.isJsonNull() || !ok.getAsBoolean()) {
            return messages;
        }

        JsonArray results = jobject.getAsJsonArray("result");
        for (JsonElement result : results) {
            JsonObject message = result.getAsJsonObject().getAsJsonObject("message");
            int fromUserId = message.getAsJsonObject("from").getAsJsonPrimitive("id").getAsInt();
            String text = message.getAsJsonPrimitive("text").getAsString().toLowerCase();

            // filter messages for intelli sense
            if (text.startsWith("/broadcast")) {
                text = text.substring("/broadcast".length());
            } else if (getBotOnlyPosts && text.startsWith("/post")) {
                text = text.substring("/post".length());
            } else if (fromUserId != channel.admin.userId || text.isEmpty()) {
                continue;
            }

            messages.add(new Message(text.trim(), fromUserId, channel.channelID));
            if (--maxCount == 0) break;
        }

        return Lists.reverse(messages);
    }

    public static void ProcessMessage(int userId, String channelIdParam, String messageParam) throws IOException {
        messageParam = messageParam.toLowerCase();
        User user = DataBase.getUserByUserId(userId);
        //System.out.println(messageParam);
        if (user == null) {
            throw new IOException("BAD REQUEST userId not registered");
        }

        Channel channel = DataBase.getChannelByChannelId(channelIdParam);

        if (channel == null) {
            throw new IOException("BAD REQUEST channelId not registered");
        }

        // reponses
        if ((channel.geofence == null || channel.channelTag.equals("stci_botchabot")) &&
                messageParam.startsWith("/register")) {
            Utilities.sendMessageToUser(user.userId, channel,
                    "Team botcha! you have been registerd\n" +
                            "for STCi Hackathon. Your presentation\n" +
                            "slot is 11:00 AM - 11:15 AM\n" +
                            "Tell cortana to remind me");
            return;
        }

        if ((channel.geofence == null || channel.channelTag.equals("microsoft_botchabot")) &&
                messageParam.startsWith("/events")) {
            Utilities.sendMessageToUser(user.userId, channel,
                    "Machine learning workshop at MPR3\n" +
                            "I’ll Join\n\n" +
                            "Band Parikramaa performing in\n" +
                            "Amphitheatre in Building 2\n" +
                            "I’ll Join");
            return;
        }

        //System.out.println("xy1");
        // check is the sender is the admin of channel
        if (channel.admin != null && channel.admin.userId == userId) {
            //System.out.println("xy2");
            messageParam = messageParam.trim();
            if (messageParam.startsWith("@")) { // admin replying to @userTag user
                //System.out.println("xy3");
                String userTag = messageParam.substring(1, messageParam.indexOf(' '));
                String message = messageParam.substring(userTag.length() + 2);
                User toUser = DataBase.getUserByUserTag(userTag, channel.getUsers());
                if (toUser != null) {
                    //System.out.println("xy4");
                    Utilities.sendMessageToUser(toUser.userId, channel, "@admin " + message);
                }
            } else if (!messageParam.startsWith("/post")) { // admin is not posting to bot
                Utilities.sendMessageToAllUserInChannel(channel, messageParam);
            }

        } else { // if the send is not admin
            if (channel.geofence == null && channel.admin == null) { // if this is master big bot channel
                String answer = Utilities.getMessageAnswer(user, messageParam);
                Utilities.sendMessageToUser(userId, channel, answer);

            } else { // user is asking in normal channel

                if (messageParam.startsWith("@admin")) { // user asking to @admin of channel
                    String message = messageParam.substring("@admin".length());
                    Utilities.sendMessageToUser(channel.admin.userId, channel, "@" + user.userTag + ": " + message);
                } else if (messageParam.startsWith("/broadcast")) { // user asking to @admin of channel
                    String message = messageParam.substring("/broadcast".length());
                    Utilities.sendMessageToAllUserInChannel(channel, message);
                } else if (messageParam.startsWith("/post")) { // user asking to @admin of channel
                    String message = messageParam.substring("/post".length());
                    Utilities.sendMessageToUser(userId, channel, "Sure, I'll make a note of it");
                } else { // user asking to channel bot, so bot replies
                    String answer = Utilities.getMessageAnswerFromChannel(channel, messageParam);
                    Utilities.sendMessageToUser(userId, channel, answer);
                }
            }
        }
    }

}
