package com.botcha.apis;

import com.botcha.dataschema.Channel;
import com.botcha.dataschema.GeoCoordinates;
import com.botcha.dataschema.Geofence;
import com.botcha.dataschema.User;
import com.botcha.utilities.DataBase;
import com.google.common.base.Strings;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CreateChannelServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");

        String channelIdParam = req.getParameter("channelId");
        String channelTagParam = req.getParameter("channelTag");
        String adminIdParam = req.getParameter("adminId");


        if (Strings.isNullOrEmpty(channelIdParam)
                || Strings.isNullOrEmpty(channelTagParam)
                || Strings.isNullOrEmpty(adminIdParam)

                ) {
            resp.sendError(400, "BAD REQUEST channelId/channelTag/adminId is missing");
            return;
        }

        int adminId = Integer.parseInt(adminIdParam);

        // get the coordinates
        List<GeoCoordinates> coordinates = new ArrayList<>();
        while (true) {
            int index = coordinates.size();
            String latParam = req.getParameter("lat" + index);
            String longParam = req.getParameter("long" + index);
            if (Strings.isNullOrEmpty(latParam)
                    || Strings.isNullOrEmpty(longParam)) {
                break;
            }
            double latitude = Double.parseDouble(latParam);
            double longitude = Double.parseDouble(longParam);
            coordinates.add(new GeoCoordinates(latitude, longitude));
        }

        if (coordinates.size() < 3) {
            resp.sendError(400, "BAD REQUEST at least 3 lat[N]/long[N] pairs are required");
            return;
        }

        Geofence geofence = new Geofence(coordinates);

        User admin = DataBase.getUserByUserId(adminId);

        if (admin == null) {
            String adminTagParam = req.getParameter("adminTag");
            if (Strings.isNullOrEmpty(adminTagParam)) {
                resp.sendError(400, "BAD REQUEST admin doesn't exists pass adminTag param");
                return;
            }

            admin = new User(adminId, adminTagParam);
            DataBase.addUser(admin);
        }
        resp.getWriter().write("{'ok':true}");
        Channel channel = new Channel(geofence, admin, channelIdParam, channelTagParam);

        // add the user to our users list
        DataBase.addChannel(channel);


    }
}
