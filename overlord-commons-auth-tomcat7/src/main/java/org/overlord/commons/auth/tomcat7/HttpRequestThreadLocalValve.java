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

package org.overlord.commons.auth.tomcat7;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;

/**
 * A simple valve that saves the inbound request to a thread local variable to be used later
 * in the processing chain.
 *
 * @author eric.wittmann@redhat.com
 */
public class HttpRequestThreadLocalValve extends ValveBase {
    
    public static final ThreadLocal<HttpServletRequest> TL_request = new ThreadLocal<HttpServletRequest>();

    /**
     * @see org.apache.catalina.valves.ValveBase#invoke(org.apache.catalina.connector.Request, org.apache.catalina.connector.Response)
     */
    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {
        TL_request.set(request);
        try {
            getNext().invoke(request, response);
        } finally {
            TL_request.set(null);
        }
    }

}
