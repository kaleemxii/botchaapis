package com.botcha.utilities;

import com.botcha.dataschema.Channel;
import com.botcha.dataschema.GeoCoordinates;
import com.botcha.dataschema.Message;
import com.botcha.dataschema.User;
import com.google.gson.*;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
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

    public static String sendGet2(String url) throws IOException {

        HttpGet httpGet = new HttpGet(url);

        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse httpResponse = httpClient.execute(httpGet);

        int responseCode = httpResponse.getStatusLine().getStatusCode();

        BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));

        StringBuffer response = new StringBuffer();

        String inputLine;
        while ((inputLine = reader.readLine()) != null) {
            response.append(inputLine);
        }
        reader.close();
        httpClient.close();

        return response.toString();
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

    public static String sendGet(String scheme, String url, String path, List<NameValuePair> params) throws Exception {
        URIBuilder builder = new URIBuilder();
        builder.setScheme(scheme).setHost(url).setPath(path);
        for (NameValuePair param : params) {
            builder.addParameter(param.getName(), param.getValue());
        }
        URI uri = builder.build();
        return sendGet(uri.toString());
    }

    public static void sendMessageToAllUser(List<Integer> userIds, String channelId, String message) throws IOException {
        for (int userId : userIds) {
            sendMessageToUser(userId, channelId, message);
        }
    }

    public static void sendMessageToAllUserInChannel(Channel channel, String message) throws IOException {
        for (User user : channel.getUsers()) {
            sendMessageToUser(user.userId, channel.channelID, message);
        }
    }

    public static void sendMessageToUser(int userId, String channelId, String message) throws IOException {
        //https://api.telegram.org/bot125820728:AAGMxfd0FMD48rVZIhz4CuGCwShtr-afZ4U/sendmessage?chat_id=113462548&text=fku
        String url = null;
        try {
            url = new StringBuffer("https://api.telegram.org/bot")
                    .append(channelId)
                    .append("/sendmessage?chat_id=")
                    .append(userId)
                    .append("&text=")
                    .append(URLEncoder.encode(message, "UTF-8")).toString();
            sendGet(url);
        } catch (Exception e) {
            throw new IOException(e);
        }

    }


    public static List<Channel> getChannels(double latitude, double longitude) {
        List<Channel> channels = new ArrayList<Channel>();
        for (Channel channel : DataBase.getGeoFenceChannels()) {
            if (channel.geofence.Contains(latitude, longitude)) {
                channels.add(channel);
            }
        }
        return channels;
    }


    public static String getMessageAnswer(User user, String question) throws IOException {
        GeoCoordinates coordinates = user.getCoordinates();
        List<Channel> channels = getChannels(coordinates.latitude, coordinates.longitude);
        List<Message> messages = new ArrayList<>();
        for (Channel channel : channels) {
            messages.addAll(getChannelUpdates(channel.channelID));
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


    public static String getMessageAnswerFromChannel(Channel channel, String question) throws IOException {
        Message answer = AnsweringUtility.getQuestionAnswerFromMessages(question, getChannelUpdates(channel.channelID));


        return answer != null ? "Sorry couldn't find an answer for you.. try asking admin of channel directly by '@admin [your question]'" :
                "Found relevant answer in channel history : " + answer.text;
    }

    public static List<Message> getChannelUpdates(String channelId) throws IOException {

        List<Message> messages = new ArrayList<>();

        //https://api.telegram.org/bot125820728:AAGMxfd0FMD48rVZIhz4CuGCwShtr-afZ4U/getupdates
        String updates = sendGet("https://api.telegram.org/bot" + channelId + "/getupdates");
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
            messages.add(new Message(text, fromUserId));
        }

        return messages;
    }

    public static void ProcessMessage(int userId, String channelIdParam, String messageParam) throws IOException {

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
                    Utilities.sendMessageToUser(toUser.userId, channelIdParam, message);
                }
            } else { // broadcast to all users
                Utilities.sendMessageToAllUserInChannel(channel, messageParam);
            }

        } else { // if the send is not admin
            if (channel.geofence == null && channel.admin == null) { // if this is master big bot channel
                String answer = Utilities.getMessageAnswer(user, messageParam);
                Utilities.sendMessageToUser(userId, channelIdParam, answer);

            } else { // user is asking in normal channel

                if (messageParam.startsWith("@admin")) { // user asking to @admin of channel
                    String message = messageParam.substring("@admin".length());
                    Utilities.sendMessageToUser(channel.admin.userId, channelIdParam, message);
                } else { // user asking to channel bot, so bot replies
                    String answer = Utilities.getMessageAnswerFromChannel(channel, messageParam);
                    Utilities.sendMessageToUser(userId, channelIdParam, answer);
                }
            }
        }
    }

}
