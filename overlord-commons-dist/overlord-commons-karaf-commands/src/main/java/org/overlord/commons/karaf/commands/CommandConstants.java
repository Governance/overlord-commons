package org.overlord.commons.karaf.commands;

public interface CommandConstants {
    interface OverlordProperties {

        public static final String FILE_KEYSTORE_NAME = "overlord-saml.keystore";
        public static final String OVERLORD_PROPERTIES_FILE_NAME = "overlord.properties";

        public static final String OVERLORD_KEYSTORE_ALIAS_PASSWORD_KEY = "overlord.auth.saml-key-alias-password";
        public static final String OVERLORD_KEYSTORE_PASSWORD_KEY = "overlord.auth.saml-keystore-password";
        public static final String OVERLORD_SAML_ALIAS = "overlord.auth.saml-key-alias";
        public static final String OVERLORD_SAML_ALIAS_VALUE = "overlord";
        public static final String OVERLORD_SAML_KEYSTORE = "overlord.auth.saml-keystore";
        public static final String OVERLORD_SAML_KEYSTORE_VALUE = "${sys:karaf.home}/etc/overlord-saml.keystore";
        public static final String OVERLORD_SAML_KEYSTORE_FABRIC_VALUE = "profile:overlord-saml.keystore";
        public static final String OVERLORD_BASE_URL = "overlord.baseUrl";
        public static final String OVERLORD_BASE_URL_VALUE = "${container:httpUrl}";
    }
}
