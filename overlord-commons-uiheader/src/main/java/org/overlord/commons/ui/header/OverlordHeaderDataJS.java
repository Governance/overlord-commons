/*
 * Copyright 2013 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.overlord.commons.ui.header;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;

/**
 * This is a simple servlet that generates the javascript data used by the Overlord Header javascript.
 *
 * @author eric.wittmann@redhat.com
 */
public class OverlordHeaderDataJS extends HttpServlet {

    private static final long serialVersionUID = -4982770016769892713L;

    /**
     * Constructor.
     */
    public OverlordHeaderDataJS() {
    }

    /**
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        // Tell the browser to never cache this JavaScript (it's generated and includes
        // information like the currently logged-in user).
        noCache(response);

        // Now generate the JavaScript data (JSON)
        response.setContentType("text/javascript");

        try {
            response.getOutputStream().write("var OVERLORD_HEADER_DATA = ".getBytes("UTF-8"));
            JsonFactory f = new JsonFactory();
            JsonGenerator g = f.createJsonGenerator(response.getOutputStream(), JsonEncoding.UTF8);
            g.useDefaultPrettyPrinter();
            g.writeStartObject();
            g.writeStringField("username", getRemoteUser(request));
            g.writeStringField("logoutLink", getLogoutLink(request));
            g.writeArrayFieldStart("tabs");
            List<TabInfo> tabs = getTabs(request);
            for (TabInfo tabInfo : tabs) {
                g.writeStartObject();
                g.writeStringField("href", tabInfo.href);
                g.writeStringField("label", tabInfo.label);
                g.writeBooleanField("active", tabInfo.active);
                g.writeEndObject();
            }
            g.writeEndArray();
            g.writeEndObject();
            g.flush();
            response.getOutputStream().write(";".getBytes("UTF-8"));
            g.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the remote user, handles null.
     * @param request
     */
    private String getRemoteUser(HttpServletRequest request) {
        return request.getRemoteUser() == null ? "<anonymous>" : request.getRemoteUser();
    }

    /**
     * Gets the configured logout link.
     * @param request
     */
    private String getLogoutLink(HttpServletRequest request) {
        // TODO get the logout link from a config file
        return "?GLO=true";
    }

    /**
     * Gets the tabs configured to appear in the UI.
     * @param request
     */
    private List<TabInfo> getTabs(HttpServletRequest request) {
        // TODO get tab information from a config file
        List<TabInfo> tabs = new ArrayList<TabInfo>();
        tabs.add(new TabInfo("/dt-gov", "DTGov", false));
        tabs.add(new TabInfo("/rt-gov", "RTGov", false));
        tabs.add(new TabInfo("/gadget-server", "Gadget Server", false));
        tabs.add(new TabInfo("/s-ramp-ui", "S-RAMP", true));
        return tabs;
    }

    /**
     * Make sure to tell the browser not to cache it.
     *
     * @param response
     */
    private void noCache(HttpServletResponse response) {
        Date now = new Date();
        response.setDateHeader("Date", now.getTime());
        // one day old
        response.setDateHeader("Expires", now.getTime() - 86400000L);
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-control", "no-cache, no-store, must-revalidate");
    }

    /**
     * A single tab in the UI.
     * @author eric.wittmann@redhat.com
     */
    private static final class TabInfo {
        public String href;
        public String label;
        public boolean active;
        public TabInfo(String href, String label, boolean active) {
            this.href = href;
            this.label = label;
            this.active = active;
        }
    }

}
