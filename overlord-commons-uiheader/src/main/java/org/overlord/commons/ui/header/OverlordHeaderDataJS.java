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
import java.util.Date;

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
            g.writeEndObject();
            g.flush();
            g.close();
            response.getOutputStream().write(";".getBytes("UTF-8"));
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

}
