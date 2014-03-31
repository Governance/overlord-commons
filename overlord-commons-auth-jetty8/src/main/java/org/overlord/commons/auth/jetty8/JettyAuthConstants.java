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

/**
 * Some jetty 8 auth constants.
 *
 * @author eric.wittmann@redhat.com
 */
public class JettyAuthConstants {
    public static final String [] ROLE_CLASSES = new String [] {
        "org.eclipse.jetty.security.MappedLoginService$RolePrincipal", //$NON-NLS-1$
        "org.eclipse.jetty.plus.jaas.JAASRole", //$NON-NLS-1$
        "org.apache.karaf.jaas.boot.principal.RolePrincipal" //$NON-NLS-1$
    };
}
