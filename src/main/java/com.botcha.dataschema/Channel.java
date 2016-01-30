package com.botcha.dataschema;

import java.util.HashMap;

/**
 * Created by shverm on 1/29/2016.
 */
public class Channel {
    public Geofence geofence;
    public User admin;
    public String channelID;
    public HashMap<String, User> users;

    public Channel(Geofence geofence, User admin, String channelID) {
        this.geofence = geofence;
        this.admin = admin;
        this.channelID = channelID;
        users = new HashMap<>();
    }

    public void addUser(User user) {
        users.put(user.userId, user);
    }

    public void removeUser(String userId) {
        users.remove(userId);
    }
}
