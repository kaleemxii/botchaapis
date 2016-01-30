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

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class HelloServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        PrintWriter out = resp.getWriter();
        out.write("Hello, world \n<br><br>\n" +
                "Available APIs: \n<br><br>\n" +
                "/getchannels?userId=xxx&lat=22.33&long=34.54 [&userTag=abc if new user] [&all=true for all channels] \n<br><br>\n" +
                "/createchannel?channelId=abc&adminId=1&adminTag=botcha&lat0=0&long0=0&lat1=0&long1=1&lat2=1&long2=1&lat3=1&long3=1 [&adminTag=dsada if new user] \n<br><br>\n" +
                "/registerchannel?channelId=xxxx&userId=xxxx \n<br><br>\n" +
                "/sendmessage?channelId=xxxx&userId=xxxx&message=dasdsada \n<br><br>\n" +
                "/broadcastmessage?channelId=xxxx&message=dasdsada \n<br><br>\n ");
    }
}
