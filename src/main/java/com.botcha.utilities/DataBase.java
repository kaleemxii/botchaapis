package com.botcha.utilities;

import com.botcha.dataschema.Channel;
import com.botcha.dataschema.GeoCoordinates;
import com.botcha.dataschema.Geofence;
import com.botcha.dataschema.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Created by xy on 30/1/16.
 */
public class DataBase {
    public static HashMap<String, Channel> staticChannels;
    public static HashMap<Integer, User> staticUsers;

    static {
        buildDummyData();
    }

    public static void addUser(User user) {
        staticUsers.put(user.userId, user);
    }

    public static void addChannel(Channel channel) {
        staticChannels.put(channel.channelID, channel);
    }

    public static Channel getChannelByChannelId(String id) {
        return staticChannels.get(id);
    }

    public static User getUserByUserId(int id) {
        return staticUsers.get(id);
    }

    public static User getUserByUserTag(String userTag, Collection<User> users) {
        users = users == null ? DataBase.staticUsers.values() : users;
        for (User user : users) {
            if (user.userTag.equals(userTag)) {
                return user;
            }
        }
        return null;
    }

    public static List<Channel> getGeoFenceChannels() {
        Collection<Channel> values = DataBase.staticChannels.values();
        List<Channel> channels = new ArrayList<>(values.size() - 1);
        for (Channel channel : values) {
            if (channel.geofence != null) {
                channels.add(channel);
            }
        }
        return channels;
    }



    private static void buildDummyData() {

        staticUsers = new HashMap<>();
        staticChannels = new HashMap<>();

        User microsoftAdmin = new User(113462548, "kaleem"); // kaleem
        User building3Admin = new User(186345694, "gaurav"); // gaurav
        User mprAdmin = new User(184748820, "shashank");  // shashank
        User hydTrafficAdmin = new User(191313868, "krishna");  // leela

        // microsoftAdmin.setCoordinates(new GeoCoordinates(17.42968, 78.34089));

        addUser(microsoftAdmin);
        addUser(building3Admin);
        addUser(hydTrafficAdmin);
        addUser(mprAdmin);

        Channel microsoftChannel = new Channel(
                new Geofence(
                        new GeoCoordinates(17.43374, 78.34529)
                        , new GeoCoordinates(17.42842, 78.34196)
                        , new GeoCoordinates(17.42944, 78.34014)
                        , new GeoCoordinates(17.43397, 78.34344))

                , microsoftAdmin, "182587512", "AAG-Dpsxq_A-p87g970EefYDyYGRwkoTCrE", "microsoft_botcha");

        Channel building3Channel = new Channel(
                new Geofence(
                        new GeoCoordinates(17.42968, 78.34162)
                        , new GeoCoordinates(17.42913, 78.341180)
                        , new GeoCoordinates(17.42953, 78.34037)
                        , new GeoCoordinates(17.43002, 78.34090))

                , building3Admin, "196576190", "AAFqzEAZyzaLuWuvOXRRlyzD_D6AccKtZMU", "studio91_botcha");

        Channel mprChannel = new Channel(
                new Geofence(
                        new GeoCoordinates(17.42971, 78.34097)
                        , new GeoCoordinates(17.42964, 78.34092)
                        , new GeoCoordinates(17.42969, 78.34080)
                        , new GeoCoordinates(17.42977, 78.34086))

                , mprAdmin, "166521051", "AAHe6Ej9FqOLMpZmx4_sYbRYsgiDI4g6_Ec", "stci_botcha");


        Channel hydTrafficChannel = new Channel(
                new Geofence(
                        new GeoCoordinates(17.42224, 78.60335)
                        , new GeoCoordinates(17.32459, 78.50310)
                        , new GeoCoordinates(17.43337, 78.30122)
                        , new GeoCoordinates(17.55387, 78.44130))

                , hydTrafficAdmin, "183756332", "AAH3STRx21t_g0zS0ghakhJ1b88Ntc-nP30", "hyderabad_botcha");

        Channel botchaChannel = new Channel(null, null, "192493113", "AAEd8UGh8sum7P02Np39m2cGhuFRyT7xkj4", "botcha");

        addChannel(microsoftChannel);
        addChannel(building3Channel);
        addChannel(mprChannel);
        addChannel(hydTrafficChannel);
        addChannel(botchaChannel);
    }

}
