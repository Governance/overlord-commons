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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;

import org.jboss.resteasy.core.Headers;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.specimpl.HttpHeadersImpl;
import org.jboss.resteasy.specimpl.PathSegmentImpl;
import org.jboss.resteasy.specimpl.UriInfoImpl;

/**
 *
 * @author eric.wittmann@redhat.com
 */
public class MockHttpServletRequest extends MockHttpRequest implements HttpServletRequest {

    /**
     * Constructor.
     * @throws URISyntaxException
     */
    public MockHttpServletRequest() throws URISyntaxException {
        httpHeaders = new HttpHeadersImpl();
        httpHeaders.setAcceptableLanguages(new ArrayList<String>());
        httpHeaders.setAcceptableMediaTypes(new ArrayList<MediaType>());
        httpHeaders.setRequestHeaders(new Headers<String>());

        URI absoluteUri = new URI("/path/to/overlord-header.js");
        URI absolutePath = UriBuilder.fromUri(absoluteUri).replaceQuery(null).build();
        URI baseUri = EMPTY_URI;
        URI relativeUri = baseUri.relativize(absoluteUri);

        List<PathSegment> encodedPathSegments = PathSegmentImpl
                .parseSegments(relativeUri.getRawPath(), false);
        uri = new UriInfoImpl(absolutePath, baseUri, "/" + relativeUri.getRawPath(),
                absoluteUri.getRawQuery(), encodedPathSegments);
        preprocessedPath = uri.getPath(false);
    }

    /**
     * @see javax.servlet.ServletRequest#getAttributeNames()
     */
    @Override
    public Enumeration<String> getAttributeNames() {
        Vector<String> attrs = new Vector<String>(attributes.keySet());
        return attrs.elements();
    }

    /**
     * @see org.jboss.resteasy.mock.MockHttpRequest#getInputStream()
     */
    @Override
    public ServletInputStream getInputStream() {
        return (ServletInputStream) super.getInputStream();
    }

    /**
     * @see javax.servlet.ServletRequest#getCharacterEncoding()
     */
    @Override
    public String getCharacterEncoding() {
        return "UTF-8";
    }

    /**
     * @see javax.servlet.ServletRequest#setCharacterEncoding(java.lang.String)
     */
    @Override
    public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
    }

    /**
     * @see javax.servlet.ServletRequest#getContentLength()
     */
    @Override
    public int getContentLength() {
        return -1;
    }

    /**
     * @see javax.servlet.ServletRequest#getContentType()
     */
    @Override
    public String getContentType() {
        return null;
    }

    /**
     * @see javax.servlet.ServletRequest#getParameter(java.lang.String)
     */
    @Override
    public String getParameter(String name) {
        return null;
    }

    /**
     * @see javax.servlet.ServletRequest#getParameterNames()
     */
    @Override
    public Enumeration<String> getParameterNames() {
        return null;
    }

    /**
     * @see javax.servlet.ServletRequest#getParameterValues(java.lang.String)
     */
    @Override
    public String[] getParameterValues(String name) {
        return null;
    }

    /**
     * @see javax.servlet.ServletRequest#getParameterMap()
     */
    @Override
    public Map<String, String> getParameterMap() {
        return null;
    }

    /**
     * @see javax.servlet.ServletRequest#getProtocol()
     */
    @Override
    public String getProtocol() {
        return "http";
    }

    /**
     * @see javax.servlet.ServletRequest#getScheme()
     */
    @Override
    public String getScheme() {
        return null;
    }

    /**
     * @see javax.servlet.ServletRequest#getServerName()
     */
    @Override
    public String getServerName() {
        return "localhost";
    }

    /**
     * @see javax.servlet.ServletRequest#getServerPort()
     */
    @Override
    public int getServerPort() {
        return 8080;
    }

    /**
     * @see javax.servlet.ServletRequest#getReader()
     */
    @Override
    public BufferedReader getReader() throws IOException {
        return null;
    }

    /**
     * @see javax.servlet.ServletRequest#getRemoteAddr()
     */
    @Override
    public String getRemoteAddr() {
        return null;
    }

    /**
     * @see javax.servlet.ServletRequest#getRemoteHost()
     */
    @Override
    public String getRemoteHost() {
        return null;
    }

    /**
     * @see javax.servlet.ServletRequest#getLocale()
     */
    @Override
    public Locale getLocale() {
        return null;
    }

    /**
     * @see javax.servlet.ServletRequest#getLocales()
     */
    @Override
    public Enumeration<?> getLocales() {
        return null;
    }

    /**
     * @see javax.servlet.ServletRequest#isSecure()
     */
    @Override
    public boolean isSecure() {
        return true;
    }

    /**
     * @see javax.servlet.ServletRequest#getRequestDispatcher(java.lang.String)
     */
    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return null;
    }

    /**
     * @see javax.servlet.ServletRequest#getRealPath(java.lang.String)
     */
    @Override
    public String getRealPath(String path) {
        return null;
    }

    /**
     * @see javax.servlet.ServletRequest#getRemotePort()
     */
    @Override
    public int getRemotePort() {
        return 8080;
    }

    /**
     * @see javax.servlet.ServletRequest#getLocalName()
     */
    @Override
    public String getLocalName() {
        return null;
    }

    /**
     * @see javax.servlet.ServletRequest#getLocalAddr()
     */
    @Override
    public String getLocalAddr() {
        return null;
    }

    /**
     * @see javax.servlet.ServletRequest#getLocalPort()
     */
    @Override
    public int getLocalPort() {
        return 0;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getAuthType()
     */
    @Override
    public String getAuthType() {
        return null;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getCookies()
     */
    @Override
    public Cookie[] getCookies() {
        return null;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getDateHeader(java.lang.String)
     */
    @Override
    public long getDateHeader(String name) {
        return 0;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getHeader(java.lang.String)
     */
    @Override
    public String getHeader(String name) {
        return null;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getHeaders(java.lang.String)
     */
    @Override
    public Enumeration<?> getHeaders(String name) {
        return null;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getHeaderNames()
     */
    @Override
    public Enumeration<?> getHeaderNames() {
        return null;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getIntHeader(java.lang.String)
     */
    @Override
    public int getIntHeader(String name) {
        return 0;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getMethod()
     */
    @Override
    public String getMethod() {
        return "GET";
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getPathInfo()
     */
    @Override
    public String getPathInfo() {
        return null;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getPathTranslated()
     */
    @Override
    public String getPathTranslated() {
        return null;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getContextPath()
     */
    @Override
    public String getContextPath() {
        return null;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getQueryString()
     */
    @Override
    public String getQueryString() {
        return null;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getRemoteUser()
     */
    @Override
    public String getRemoteUser() {
        return "ewittman";
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#isUserInRole(java.lang.String)
     */
    @Override
    public boolean isUserInRole(String role) {
        return false;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getUserPrincipal()
     */
    @Override
    public Principal getUserPrincipal() {
        return new Principal() {
            @Override
            public String getName() {
                return getRemoteUser();
            }
        };
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getRequestedSessionId()
     */
    @Override
    public String getRequestedSessionId() {
        return null;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getRequestURI()
     */
    @Override
    public String getRequestURI() {
        return getUri().toString();
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getRequestURL()
     */
    @Override
    public StringBuffer getRequestURL() {
        return null;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getServletPath()
     */
    @Override
    public String getServletPath() {
        return null;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getSession(boolean)
     */
    @Override
    public HttpSession getSession(boolean create) {
        return null;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getSession()
     */
    @Override
    public HttpSession getSession() {
        return new MockHttpSession();
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdValid()
     */
    @Override
    public boolean isRequestedSessionIdValid() {
        return false;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromCookie()
     */
    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromURL()
     */
    @Override
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromUrl()
     */
    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }

}
