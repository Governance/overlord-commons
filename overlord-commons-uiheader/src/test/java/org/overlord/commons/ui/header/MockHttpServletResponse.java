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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MultivaluedMap;

import org.jboss.resteasy.mock.MockHttpResponse;

/**
 *
 * @author eric.wittmann@redhat.com
 */
public class MockHttpServletResponse extends MockHttpResponse implements HttpServletResponse {

    /**
     * Constructor.
     */
    public MockHttpServletResponse() {
    }

    /**
     * @see javax.servlet.ServletResponse#getCharacterEncoding()
     */
    @Override
    public String getCharacterEncoding() {
        return "UTF-8"; //$NON-NLS-1$
    }

    /**
     * @see javax.servlet.ServletResponse#getContentType()
     */
    @Override
    public String getContentType() {
        return null;
    }

    /**
     * @see javax.servlet.ServletResponse#getWriter()
     */
    @Override
    public PrintWriter getWriter() throws IOException {
        return null;
    }

    /**
     * @see javax.servlet.ServletResponse#setCharacterEncoding(java.lang.String)
     */
    @Override
    public void setCharacterEncoding(String charset) {
    }

    /**
     * @see javax.servlet.ServletResponse#setContentLength(int)
     */
    @Override
    public void setContentLength(int len) {
        setIntHeader("Content-Length", len); //$NON-NLS-1$
    }

    /**
     * @see javax.servlet.ServletResponse#setContentType(java.lang.String)
     */
    @Override
    public void setContentType(String type) {
        setHeader("Content-Type", type); //$NON-NLS-1$
    }

    /**
     * @see javax.servlet.ServletResponse#setBufferSize(int)
     */
    @Override
    public void setBufferSize(int size) {

    }

    /**
     * @see javax.servlet.ServletResponse#getBufferSize()
     */
    @Override
    public int getBufferSize() {

        return 0;
    }

    /**
     * @see javax.servlet.ServletResponse#flushBuffer()
     */
    @Override
    public void flushBuffer() throws IOException {

    }

    /**
     * @see javax.servlet.ServletResponse#resetBuffer()
     */
    @Override
    public void resetBuffer() {

    }

    /**
     * @see javax.servlet.ServletResponse#setLocale(java.util.Locale)
     */
    @Override
    public void setLocale(Locale loc) {

    }

    /**
     * @see javax.servlet.ServletResponse#getLocale()
     */
    @Override
    public Locale getLocale() {

        return null;
    }

    /**
     * @see javax.servlet.http.HttpServletResponse#addCookie(javax.servlet.http.Cookie)
     */
    @Override
    public void addCookie(Cookie cookie) {

    }

    /**
     * @see javax.servlet.http.HttpServletResponse#containsHeader(java.lang.String)
     */
    @Override
    public boolean containsHeader(String name) {

        return false;
    }

    /**
     * @see javax.servlet.http.HttpServletResponse#encodeURL(java.lang.String)
     */
    @Override
    public String encodeURL(String url) {

        return null;
    }

    /**
     * @see javax.servlet.http.HttpServletResponse#encodeRedirectURL(java.lang.String)
     */
    @Override
    public String encodeRedirectURL(String url) {

        return null;
    }

    /**
     * @see javax.servlet.http.HttpServletResponse#encodeUrl(java.lang.String)
     */
    @Override
    public String encodeUrl(String url) {

        return null;
    }

    /**
     * @see javax.servlet.http.HttpServletResponse#encodeRedirectUrl(java.lang.String)
     */
    @Override
    public String encodeRedirectUrl(String url) {

        return null;
    }

    /**
     * @see javax.servlet.http.HttpServletResponse#sendRedirect(java.lang.String)
     */
    @Override
    public void sendRedirect(String location) throws IOException {

    }

    /**
     * @see javax.servlet.http.HttpServletResponse#setDateHeader(java.lang.String, long)
     */
    @Override
    public void setDateHeader(String name, long date) {
        setHeader(name, "<DATE VALUE>"); //$NON-NLS-1$
    }

    /**
     * @see javax.servlet.http.HttpServletResponse#addDateHeader(java.lang.String, long)
     */
    @Override
    public void addDateHeader(String name, long date) {

    }

    /**
     * @see javax.servlet.http.HttpServletResponse#setHeader(java.lang.String, java.lang.String)
     */
    @Override
    public void setHeader(String name, String value) {
        super.getOutputHeaders().putSingle(name, value);
    }

    /**
     * @see javax.servlet.http.HttpServletResponse#addHeader(java.lang.String, java.lang.String)
     */
    @Override
    public void addHeader(String name, String value) {

    }

    /**
     * @see javax.servlet.http.HttpServletResponse#setIntHeader(java.lang.String, int)
     */
    @Override
    public void setIntHeader(String name, int value) {
        setHeader(name, String.valueOf(value));
    }

    /**
     * @see javax.servlet.http.HttpServletResponse#addIntHeader(java.lang.String, int)
     */
    @Override
    public void addIntHeader(String name, int value) {

    }

    /**
     * @see javax.servlet.http.HttpServletResponse#setStatus(int, java.lang.String)
     */
    @Override
    public void setStatus(int sc, String sm) {

    }

    /**
     * @see org.jboss.resteasy.mock.MockHttpResponse#getOutputStream()
     */
    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return new MockServletOutputStream((ByteArrayOutputStream) super.getOutputStream());
    }

    /**
     *
     */
    public String getOutputHeadersAsString() {
        StringBuilder builder = new StringBuilder();
        MultivaluedMap<String,Object> headers = getOutputHeaders();
        Set<String> headerNames = new TreeSet<String>(headers.keySet());
        for (String headerName : headerNames) {
            builder.append(headerName);
            builder.append(": "); //$NON-NLS-1$
            boolean first = true;
            for (Object headerValue : headers.get(headerName)) {
                String val = headerValue.toString();
                if (first) {
                    builder.append(val);
                    first = false;
                } else {
                    builder.append(", ").append(val); //$NON-NLS-1$
                }
            }
            builder.append("\r\n"); //$NON-NLS-1$
        }
        return builder.toString();
    }

    /**
     */
    public String getOutputAsString() {
        try {
            return this.baos.toString("UTF-8"); //$NON-NLS-1$
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getHeader(String name) {
        return null;
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return null;
    }

    @Override
    public Collection<String> getHeaderNames() {

        return null;
    }

}
