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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.Principal;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.picketlink.common.constants.GeneralConstants;

/**
 * A filter that grabs the picketlink principal from the http session and
 * then wraps the inbound request.  The wrapped request uses the principal
 * and roles found in the current session to implement the standard http
 * request auth-related methods.
 *
 * @author eric.wittmann@redhat.com
 */
public class PicketLinkAuthWrapperFilter implements Filter {

    /**
     * Constructor.
     */
    public PicketLinkAuthWrapperFilter() {
    }

    /**
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init(FilterConfig config) throws ServletException {
    }

    /**
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpSession session = req.getSession();
        Principal userPrincipal = (Principal) session.getAttribute(GeneralConstants.PRINCIPAL_ID);
        List<String> userRoles = (List<String>) session.getAttribute(GeneralConstants.ROLES_ID);
        
        if (userPrincipal != null && userRoles != null) {
            chain.doFilter(proxyRequest(req, userPrincipal, userRoles), response);
        } else {
            chain.doFilter(req, response);
        }
    }

    /**
     * Wrap/proxy the http request.
     * @param request
     * @param principal
     * @param roles
     */
    private ServletRequest proxyRequest(final HttpServletRequest request, final Principal principal,
            final List<String> roles) {
        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (method.getName().equals("getUserPrincipal")) { //$NON-NLS-1$
                    return principal;
                } else if (method.getName().equals("getRemoteUser")) { //$NON-NLS-1$
                    return principal.getName();
                } else if (method.getName().equals("isUserInRole")) { //$NON-NLS-1$
                    String role = (String) args[0];
                    return roles.contains(role);
                }
                return method.invoke(request, args);
            }
        };
        return (HttpServletRequest) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class[] { HttpServletRequest.class }, handler);
    }

    /**
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy() {
    }

}
