package com.botcha.utilities;

import com.botcha.dataschema.Channel;
import com.google.gson.Gson;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by shverm on 1/29/2016.
 */
public class Utilities {

    public static Gson Gson = new Gson();

    public static void removeUserFromChannelsExcept(String userId, List<Channel> channels) {
        Set<String> channelIds = new HashSet<>(channels.size());
        for (Channel channel : channels) {
            channelIds.add(channel.channelID);
        }

        for (Channel channel : DataBase.staticChannels.values()) {

            if (!channelIds.contains(channel.channelID)) {
                channel.removeUser(userId);
            }
        }
    }

    public static String sendGet(String url) throws Exception {

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

    public static void sendMessageToAllUser(List<String> userIds, String channelId, String message) throws Exception {
        for (String userId : userIds) {
            sendMessageToUser(userId, channelId, message);
        }
    }

    public static void sendMessageToAllUserInChannel(Channel channel, String message) throws Exception {
        for (String userId : channel.users.keySet()) {
            sendMessageToUser(userId, channel.channelID, message);
        }
    }

    public static void sendMessageToUser(String userId, String channelId, String message) throws Exception {
        //https://api.telegram.org/bot125820728:AAGMxfd0FMD48rVZIhz4CuGCwShtr-afZ4U/sendmessage?chat_id=113462548&text=fku
        String url = new StringBuffer("https://api.telegram.org/bot")
                .append(channelId)
                .append("/sendmessage?chat_id=")
                .append(userId)
                .append("&text=")
                .append(URLEncoder.encode(message, "UTF-8")).toString();
        sendGet(url);
    }


    public static List<Channel> getChannels(double latitude, double longitude) {
        List<Channel> channels = new ArrayList<Channel>();
        for (Channel channel : DataBase.staticChannels.values()) {
            if (channel.geofence.Contains(latitude, longitude)) {
                channels.add(channel);
            }
        }
        return channels;
    }
}
