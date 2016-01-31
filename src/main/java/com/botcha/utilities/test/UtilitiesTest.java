package com.botcha.utilities.test;

import com.botcha.utilities.DataBase;
import com.botcha.utilities.Utilities;

/**
 * Created by xy on 31/1/16.
 */
public class UtilitiesTest {

    @org.junit.Test
    public void testSendMessageToAllUser() throws Exception {

    }

    @org.junit.Test
    public void testSendMessageToAllUserInChannel() throws Exception {

    }

    @org.junit.Test
    public void testSendMessageToUser() throws Exception {

    }

    @org.junit.Test
    public void testGetChannels() throws Exception {

    }

    @org.junit.Test
    public void testGetMessageAnswer() throws Exception {
        System.out.print(Utilities.getMessageAnswer(DataBase.getUserByUserId(113462548), "when is lunch"));
    }

    @org.junit.Test
    public void testGetMessageAnswerFromChannel() throws Exception {

    }

    @org.junit.Test
    public void testGetChannelUpdates() throws Exception {

    }
}