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

package org.overlord.commons.idp;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.overlord.commons.auth.util.IRoleGenerator;
import org.overlord.commons.auth.util.RoleUtil;
import org.picketlink.identity.federation.core.interfaces.RoleGenerator;

/**
 * An implementation of the PicketLink role generator.  Uses the overlord commons
 * auth {@link IRoleGenerator} to delegate the actual generation of the roles to
 * platform-specific code.
 * 
 * @author eric.wittmann@redhat.com
 */
public class IDPRoleGenerator implements RoleGenerator {

    /**
     * @see org.picketlink.identity.federation.core.interfaces.RoleGenerator#generateRoles(java.security.Principal)
     */
    @Override
    public List<String> generateRoles(Principal principal) {
    	Set<String> roles = RoleUtil.generateRoles();
    	return new ArrayList<String>(roles);
    }

}
