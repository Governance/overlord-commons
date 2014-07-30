package org.overlord.commons.karaf.commands.i18n;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 *
 * Messages for the karaf commands project.
 * @author David Virgil Naranjo
 */
public class Messages {
    private static final String BUNDLE_NAME = "org.overlord.commons.karaf.commands.messages"; //$NON-NLS-1$

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    private Messages() {
    }

    public static String getString(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }
}
