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

package org.overlord.commons.auth.jetty8;

import java.io.IOException;
import java.security.Principal;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.server.UserIdentity;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.overlord.commons.auth.filters.SamlBearerTokenAuthFilter;
import org.overlord.commons.auth.filters.SimplePrincipal;

/**
 * A Jetty 8 version of the {@link SamlBearerTokenAuthFilter}.
 *
 * @author eric.wittmann@redhat.com
 */
@Deprecated
public class Jetty8SamlBearerTokenAuthFilter extends SamlBearerTokenAuthFilter {

    public static final ThreadLocal<SimplePrincipal> TL_principal = new ThreadLocal<SimplePrincipal>();

    private String[] roleClasses;
    
    /**
     * Constructor.
     */
    public Jetty8SamlBearerTokenAuthFilter() {
    }
    
    /**
     * @see org.overlord.commons.auth.filters.SamlBearerTokenAuthFilter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init(FilterConfig config) throws ServletException {
        super.init(config);
        roleClasses = null;
        
        // Role classes
        String parameter = config.getInitParameter("roleClasses"); //$NON-NLS-1$
        if (parameter != null && parameter.trim().length() > 0) {
            roleClasses = parameter.split(","); //$NON-NLS-1$
        } else {
            roleClasses = defaultRoleClasses();
        }
    }
    
    /**
     * @return default role classes
     */
    protected String[] defaultRoleClasses() {
        return JettyAuthConstants.ROLE_CLASSES;
    }
    
    /**
     * @see org.overlord.commons.auth.filters.SamlBearerTokenAuthFilter#login(org.overlord.commons.auth.filters.SamlBearerTokenAuthFilter.Creds, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected SimplePrincipal login(Creds credentials, HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        SimplePrincipal principal = super.login(credentials, request, response);
        TL_principal.set(principal);
        return principal;
    }

    /**
     * @see org.overlord.commons.auth.filters.SamlBearerTokenAuthFilter#doBasicLogin(java.lang.String, java.lang.String, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected SimplePrincipal doBasicLogin(String username, String password, HttpServletRequest request)
            throws IOException {
        ServletContext context = request.getServletContext();
        ServletContextHandler.Context jettyCtx = (ServletContextHandler.Context) context;
        ConstraintSecurityHandler securityHandler = (ConstraintSecurityHandler) jettyCtx.getContextHandler().getChildHandlerByClass(ConstraintSecurityHandler.class);
        if (securityHandler == null)
            return null;
        LoginService loginService = securityHandler.getLoginService();
        if (loginService == null)
            return null;
        UserIdentity identity = loginService.login(username, password);
        if (identity == null)
            return null;
        SimplePrincipal principal = new SimplePrincipal(username);
        for (String cname : roleClasses) {
            try {
                @SuppressWarnings("unchecked")
                Class<? extends Principal> c = (Class<? extends Principal>) Thread.currentThread().getContextClassLoader().loadClass(cname);
                Set<? extends Principal> principals = identity.getSubject().getPrincipals(c);
                for (Principal p : principals) {
                    principal.addRole(p.getName());
                }
            } catch (ClassNotFoundException e) {
                // Skip it!
            }
        }
        return principal;
    }

    /**
     * @see org.overlord.commons.auth.filters.SamlBearerTokenAuthFilter#doFilterChain(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain, org.overlord.commons.auth.filters.SimplePrincipal)
     */
    @Override
    protected void doFilterChain(ServletRequest request, ServletResponse response, FilterChain chain,
            SimplePrincipal principal) throws IOException, ServletException {
        super.doFilterChain(request, response, chain, principal);
        TL_principal.remove();
    }
}
