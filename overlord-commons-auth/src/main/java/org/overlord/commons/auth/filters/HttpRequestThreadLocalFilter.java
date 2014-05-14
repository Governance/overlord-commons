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

package org.overlord.commons.auth.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * A filter that saves the current request to a thread local.
 *
 * @author eric.wittmann@redhat.com
 */
public class HttpRequestThreadLocalFilter implements Filter {
    
    public static final ThreadLocal<HttpServletRequest> TL_request = new ThreadLocal<HttpServletRequest>();
    
    /**
     * Constructor.
     */
    public HttpRequestThreadLocalFilter() {
    }
    
    /**
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init(FilterConfig arg0) throws ServletException {
    }
    
    /**
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        TL_request.set((HttpServletRequest) request);
        chain.doFilter(request, response);
        TL_request.remove();
    }
    
    /**
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy() {
    }

}
