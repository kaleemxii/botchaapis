package com.botcha.dataschema;

/**
 * Created by xy on 31/1/16.
 */
public class Message {

    public String text;
    public int fromUserId;
    public String channelId;

    public Message(String message, int fromUserId, String channelId) {
        this.text = message;
        this.fromUserId = fromUserId;
        this.channelId = channelId;
    }
}

