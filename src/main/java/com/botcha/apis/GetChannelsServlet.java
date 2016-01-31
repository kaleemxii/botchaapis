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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class GetChannelsServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");

        String allParam = req.getParameter("all");
        if (!Strings.isNullOrEmpty(allParam) && allParam.equals("true")) {
            PrintWriter out = resp.getWriter();
            out.write(Utilities.Gson.toJson(DataBase.staticChannels.values()));
            return;
        }

        String userIdParam = req.getParameter("userId");
        String latParam = req.getParameter("lat");
        String longParam = req.getParameter("long");

        if (Strings.isNullOrEmpty(userIdParam)
                || Strings.isNullOrEmpty(latParam)
                || Strings.isNullOrEmpty(longParam)) {
            resp.sendError(400, "BAD REQUEST userId/lat/long is missing");
            return;
        }

        // check the user if available
        int userId = Integer.parseInt(userIdParam);
        User user = DataBase.getUserByUserId(userId);
        if (user == null) {
            String userTagParam = req.getParameter("userTag");
            if (Strings.isNullOrEmpty(userTagParam)) {
                resp.sendError(400, "BAD REQUEST user doesn't exists pass userTag param");
                return;
            }
            user = new User(userId, userTagParam);
            //add users to our list
            DataBase.addUser(user);
        }



        double latitude = Double.parseDouble(latParam);
        double longitude = Double.parseDouble(longParam);
        List<Channel> channels = Utilities.getChannels(latitude, longitude);
        List<Channel> outchannels = new ArrayList<>();
        for (Channel channel : channels) {
            outchannels.add(channel.getChannelWithSummary());
        }

        PrintWriter out = resp.getWriter();
        out.write(Utilities.Gson.toJson(outchannels));

        // update user co-ordinates
        user.getCoordinates().update(latitude, longitude);

        // remove the user from other channels if present
        Utilities.removeUserFromChannelsExcept(userIdParam, channels);
    }

}
