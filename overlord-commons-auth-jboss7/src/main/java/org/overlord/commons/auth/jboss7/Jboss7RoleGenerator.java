/*
 * Copyright 2014 JBoss Inc
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

package org.overlord.commons.auth.jboss7;

import java.io.File;
import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.jboss.security.SecurityContextAssociation;
import org.overlord.commons.auth.util.IRoleGenerator;

/**
 * Generates roles for the JBoss 7 based environments (JBoss 7.x, EAP 6.x, etc).
 */
public class Jboss7RoleGenerator implements IRoleGenerator {
	
	/**
	 * C'tor.
	 */
	public Jboss7RoleGenerator() {
	}

	/**
	 * @see org.overlord.commons.auth.util.IRoleGenerator#accept()
	 */
	@Override
	public boolean accept() {
        String property = System.getProperty("jboss.server.config.dir"); //$NON-NLS-1$
        if (property != null) {
            File f = new File(property, "standalone.xml"); //$NON-NLS-1$
            if (f.isFile())
                return true;
        }
        return false;
	}

	/**
	 * @see org.overlord.commons.auth.util.IRoleGenerator#generateRoles(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	public Set<String> generateRoles(HttpServletRequest request) {
        try {
            Principal principal = SecurityContextAssociation.getPrincipal();
            Set<Principal> userRoles = SecurityContextAssociation.getSecurityContext()
                    .getAuthorizationManager().getUserRoles(principal);
            Set<String> roles = new HashSet<String>();
            if (userRoles != null) {
                for (Principal role : userRoles) {
                    roles.add(role.getName());
                }
            }
            return roles;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
	}

}
