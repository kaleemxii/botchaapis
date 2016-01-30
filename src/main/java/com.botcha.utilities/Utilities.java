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
import java.io.InputStreamReader;
import java.net.URI;
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


        return answer != null ? "Sorry couldn't find an answer for you.. try asking admin of channle directly by '@admin [your question]'" :
                "Found relevant answer in channel history : " + answer.text;
    }

    public static List<Message> getChannelUpdates(String channelId) throws IOException {

        List<Message> messages = new ArrayList<>();

        //https://api.telegram.org/bot125820728:AAGMxfd0FMD48rVZIhz4CuGCwShtr-afZ4U/getupdates
        String updates = sendGet("https://api.telegram.org/bot" + channelId + "/getupdates");

        JsonElement jelement = new JsonParser().parse(updates);
        JsonObject jobject = jelement.getAsJsonObject();
        JsonObject ok = jobject.getAsJsonObject("ok");
        if (ok.isJsonNull() || !ok.getAsString().equals("true")) {
            return messages;
        }

        JsonArray results = jobject.getAsJsonArray("result");
        for (JsonElement result : results) {
            JsonObject message = result.getAsJsonObject().getAsJsonObject("message");
            int fromUserId = message.getAsJsonObject("from").getAsJsonObject("id").getAsInt();
            String text = message.getAsJsonObject("text").getAsString().toLowerCase();
            messages.add(new Message(text, fromUserId));
        }

        return messages;
    }

}
