package com.botcha.dataschema;

/**
 * Created by xy on 31/1/16.
 */
public class Message {

    public String text;
    public int fromUserId;

    public Message(String message, int fromUserId) {
        this.text = message;
        this.fromUserId = fromUserId;
    }
}

