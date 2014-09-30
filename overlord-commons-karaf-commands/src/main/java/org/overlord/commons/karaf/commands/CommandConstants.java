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
package org.overlord.commons.karaf.commands;

public interface CommandConstants {
    interface OverlordProperties {

        public static final String FILE_KEYSTORE_NAME = "overlord-saml.keystore"; //$NON-NLS-1$
        public static final String OVERLORD_PROPERTIES_FILE_NAME = "overlord.properties"; //$NON-NLS-1$

        public static final String OVERLORD_KEYSTORE_ALIAS_PASSWORD_KEY = "overlord.auth.saml-key-alias-password"; //$NON-NLS-1$
        public static final String OVERLORD_KEYSTORE_PASSWORD_KEY = "overlord.auth.saml-keystore-password"; //$NON-NLS-1$
        public static final String OVERLORD_SAML_ALIAS = "overlord.auth.saml-key-alias"; //$NON-NLS-1$
        public static final String OVERLORD_PORT = "overlord.port"; //$NON-NLS-1$
        public static final String OVERLORD_SAML_ALIAS_VALUE = "overlord"; //$NON-NLS-1$
        public static final String OVERLORD_SAML_KEYSTORE = "overlord.auth.saml-keystore"; //$NON-NLS-1$
        public static final String OVERLORD_SAML_KEYSTORE_VALUE = "${sys:karaf.home}/etc/overlord-saml.keystore"; //$NON-NLS-1$
        public static final String OVERLORD_SAML_KEYSTORE_FABRIC_VALUE = "${profile:overlord-saml.keystore}"; //$NON-NLS-1$
        public static final String OVERLORD_BASE_URL = "overlord.baseUrl"; //$NON-NLS-1$
        public static final String OVERLORD_BASE_URL_VALUE = "${container:httpUrl}"; //$NON-NLS-1$
    }
}
