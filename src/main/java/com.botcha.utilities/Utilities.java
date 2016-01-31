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
        Message answer = AnsweringUtility.getQuestionAnswerFromMessages(question, messages);

        if (answer != null) {
            String channelTag = "bot";

            for (Channel channel : DataBase.getGeoFenceChannels()) {
                if (channel.admin.userId == answer.fromUserId) {
                    channelTag = channel.channelTag;
                }
            }
            return "Found a relevant answer in channel #" + channelTag + " : " + answer.text;
        }

        return "Sorry couldn't find an answer for you..";
    }


    public static String getMessageAnswerFromChannel(Channel channel,
                                                     String question) throws IOException {
        Message answer = AnsweringUtility.getQuestionAnswerFromMessages(question,
                getChannelUpdates(channel, true, 50));


        return answer == null ? "Sorry couldn't find an answer for you.. try asking admin of channel directly by '@admin [your question]'" :
                "Found relevant answer in channel history : " + answer.text;
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

            messages.add(new Message(text.trim(), fromUserId));
            if (--maxCount == 0) break;
        }

        return Lists.reverse(messages);
    }

    public static void ProcessMessage(int userId, String channelIdParam, String messageParam) throws IOException {
        messageParam = messageParam.toLowerCase();
        User user = DataBase.getUserByUserId(userId);

        if (user == null) {
            throw new IOException("BAD REQUEST userId not registered");
        }

        Channel channel = DataBase.getChannelByChannelId(channelIdParam);

        if (channel == null) {
            throw new IOException("BAD REQUEST channelId not registered");
        }

        // check is the sender is the admin of channel
        if (channel.admin != null && channel.admin.userId == userId) {
            messageParam = messageParam.trim();
            if (messageParam.startsWith("@")) { // admin replying to @userTag user
                String userTag = messageParam.substring(1, messageParam.indexOf(' '));
                String message = messageParam.substring(userTag.length() + 2);
                User toUser = DataBase.getUserByUserTag(userTag, channel.getUsers());
                if (toUser != null) {
                    Utilities.sendMessageToUser(toUser.userId, channel, "@admin:" + message);
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
                } else { // user asking to channel bot, so bot replies
                    String answer = Utilities.getMessageAnswerFromChannel(channel, messageParam);
                    Utilities.sendMessageToUser(userId, channel, answer);
                }
            }
        }
    }

}
