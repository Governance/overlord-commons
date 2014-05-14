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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;

/**
 * A servlet filter that can serve the Overlord header resources from the 
 * overlord-commons-uiheader JAR.  This should only be used in environments
 * where resources cannot be served from JARs in the WEB-INF/lib directory
 * of the host WAR (e.g. old versions of Jetty, Fuse, Karaf, etc).
 *
 * @author eric.wittmann@redhat.com
 */
public class OverlordHeaderResources implements Filter {
    
    private final static Map<String, String> FILE_TYPES = new HashMap<String, String>();
    private final static Map<String, Boolean> EXISTS_CHECK = new HashMap<String, Boolean>();
    static {
        FILE_TYPES.put("png", "image/png"); //$NON-NLS-1$ //$NON-NLS-2$
        FILE_TYPES.put("css", "text/css"); //$NON-NLS-1$ //$NON-NLS-2$
        FILE_TYPES.put("js", "text/javascript"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    /**
     * Constructor.
     */
    public OverlordHeaderResources() {
    }

    /**
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    /**
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) request;
        String path = "/META-INF/resources" + httpReq.getServletPath(); //$NON-NLS-1$
        if (path.endsWith("/")) { //$NON-NLS-1$
            chain.doFilter(request, response);
            return;
        }
        Boolean exists = EXISTS_CHECK.get(path);
        if (exists != null && !exists) {
            chain.doFilter(request, response);
            return;
        }
        ClassLoader cl = getClass().getClassLoader();
        URL resourceURL = cl.getResource(path);
        if (resourceURL == null) {
            EXISTS_CHECK.put(path, false);
            chain.doFilter(request, response);
        } else {
            serveResource(response, resourceURL);
        }
    }

    /**
     * Serve the appropriate resource's binary content.
     * @param response
     * @param resourceURL
     * @throws IOException 
     */
    private void serveResource(ServletResponse response, URL resourceURL) throws IOException {
        String extension = resourceURL.getPath().substring(resourceURL.getPath().lastIndexOf('.')+1);
        String ct = FILE_TYPES.get(extension);
        response.setContentType(ct);
        
        InputStream in = null;
        OutputStream out = null;
        try {
            in = resourceURL.openStream();
            out = response.getOutputStream();
            IOUtils.copy(in, out);
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }
        
    }

    /**
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy() {
    }

}
