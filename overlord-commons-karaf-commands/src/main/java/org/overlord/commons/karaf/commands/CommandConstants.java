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
