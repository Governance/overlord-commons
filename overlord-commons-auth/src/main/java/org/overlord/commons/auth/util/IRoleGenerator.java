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

package org.overlord.commons.auth.util;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;

/**
 * Interface used to get the list of roles for the current user.  Implementations
 * of this interface must be runtime/platform/container specific, since there is
 * no container agnostic mechanism to get the current list of roles.
 */
public interface IRoleGenerator {
	
	/**
	 * Return true if the implementation is capable of generating roles for the
	 * current runtime platform.  This allows multiple implementations to be 
	 * available at the same time, and only the appropriate one will be used.
	 * @return true if the impl accepts the current runtime
	 */
	public boolean accept();

	/**
	 * Generates the roles for the current user.
	 * @return the list of generated roles
	 */
	public Set<String> generateRoles(HttpServletRequest request);

}
