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

package org.overlord.commons.auth.tomcat7;

import java.io.File;
import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.catalina.realm.GenericPrincipal;
import org.overlord.commons.auth.util.IRoleGenerator;

/**
 * A role generator that works when the runtime is Tomcat 7.
 */
public class Tomcat7RoleGenerator implements IRoleGenerator {
	
	/**
	 * C'tor.
	 */
	public Tomcat7RoleGenerator() {
	}

	/**
	 * @see org.overlord.commons.auth.util.IRoleGenerator#accept()
	 */
	@Override
	public boolean accept() {
        String property = System.getProperty("catalina.home"); //$NON-NLS-1$
        if (property != null) {
            File f = new File(property, "bin/catalina.sh"); //$NON-NLS-1$
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
            Principal principal = request.getUserPrincipal();
            if (principal instanceof GenericPrincipal) {
                GenericPrincipal gp = (GenericPrincipal) principal;
                String[] gpRoles = gp.getRoles();
                Set<String> roles = new HashSet<String>(gpRoles.length);
                for (String role : gpRoles) {
                    roles.add(role);
                }
                return roles;
            }
            throw new Exception(Messages.getString("TomcatSAMLAssertionFactory.UnexpectedPrincipalType") + principal.getClass()); //$NON-NLS-1$
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
	}


}
