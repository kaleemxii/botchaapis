package com.botcha.dataschema;

import com.botcha.utilities.Utilities;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created by shverm on 1/29/2016.
 */
public class Channel {
    public Geofence geofence;
    public User admin;
    public String channelID;
    public String channelTag;
    public String summary;
    private transient String chanelToken;
    private transient HashMap<Integer, User> usersById;

    public Channel(Geofence geofence, User admin, String channelID, String channelToken, String channelTag) {
        this.geofence = geofence;
        this.admin = admin;
        this.channelID = channelID;
        this.chanelToken = channelToken;
        this.channelTag = channelTag;
        usersById = new HashMap<>();
    }

    public String getChanneldToken() {
        return channelID + ":" + chanelToken;
    }

    public User getUserById(int userId) {
        return usersById.get(userId);
    }

    public Collection<User> getUsers() {
        return usersById.values();
    }

    public void addUser(User user) {
        usersById.put(user.userId, user);
    }

    public void removeUser(String userId) {
        usersById.remove(userId);
    }

    public Channel getChannelWithSummary() throws IOException {
        Channel clone = this.getClone();
        clone.summary = Utilities.getTopSummaryForChannel(this, 3);
        return clone;
    }

    public Channel getClone() {
        return new Channel(this.geofence, admin, channelID, "", channelTag);
    }
}
