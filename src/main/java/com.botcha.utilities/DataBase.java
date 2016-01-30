package com.botcha.utilities;

import com.botcha.dataschema.Channel;
import com.botcha.dataschema.GeoCoordinates;
import com.botcha.dataschema.Geofence;
import com.botcha.dataschema.User;

import java.util.HashMap;

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


    private static void buildDummyData() {
        GeoCoordinates microsoftLeftBottom = new GeoCoordinates(17.42726335237322, 78.34141373634338);
        GeoCoordinates microsoftLeftTop = new GeoCoordinates(17.429023997842013, 78.33863496780396);
        GeoCoordinates microsoftRightTop = new GeoCoordinates(17.43590263591793, 78.34269046783447);
        GeoCoordinates microsoftRightBottom = new GeoCoordinates(17.43414205681682, 78.34575891494751);

        GeoCoordinates mprLeftTop = new GeoCoordinates(17.429554363369927, 78.34092993289232);
        GeoCoordinates mprLeftBottom = new GeoCoordinates(17.42954476690242, 78.34095273166895);
        GeoCoordinates mprRightTop = new GeoCoordinates(17.429711745365164, 78.34103118628263);
        GeoCoordinates mprRightBottom = new GeoCoordinates(17.429697670558106, 78.34106404334307);


        staticUsers = new HashMap<>();
        staticChannels = new HashMap<>();

        Geofence microsoftGeofence = new Geofence(microsoftLeftBottom, microsoftLeftTop, microsoftRightTop, microsoftRightBottom);
        Geofence mprGeofence = new Geofence(mprLeftBottom, mprLeftTop, mprRightTop, mprRightBottom);

        User microsoftAdmin = new User(1, "admin");
        User mprAdmin = new User(2, "admin");


        Channel microsoftChannel = new Channel(microsoftGeofence, microsoftAdmin, "1");
        Channel mprChannel = new Channel(mprGeofence, mprAdmin, "2");


        addChannel(microsoftChannel);
        addChannel(mprChannel);


        addUser(microsoftAdmin);
        addUser(mprAdmin);

    }
}
