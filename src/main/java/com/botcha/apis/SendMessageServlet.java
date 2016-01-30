/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.botcha.apis;

import com.botcha.dataschema.Channel;
import com.botcha.dataschema.User;
import com.botcha.utilities.DataBase;
import com.botcha.utilities.Utilities;
import com.google.common.base.Strings;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SendMessageServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");

        String userIdParam = req.getParameter("userId");
        String channelIdParam = req.getParameter("channelId");
        String messageParam = req.getParameter("message");

        if (Strings.isNullOrEmpty(userIdParam)
                || Strings.isNullOrEmpty(channelIdParam)
                || Strings.isNullOrEmpty(messageParam)) {
            resp.sendError(400, "BAD REQUEST userIdParam/channelId/message is missing");
            return;
        }

        User user = DataBase.getUserByUserId(userIdParam);

        if (user == null) {
            resp.sendError(400, "BAD REQUEST userId not registered");
            return;
        }

        Channel channel = DataBase.getChannelByChannelId(channelIdParam);

        if (channel == null) {
            resp.sendError(400, "BAD REQUEST channelId not registered");
            return;
        }

        try {
            Utilities.sendMessageToUser(userIdParam, channelIdParam, messageParam);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
}