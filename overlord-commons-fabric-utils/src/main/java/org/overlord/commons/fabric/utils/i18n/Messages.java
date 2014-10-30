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
package org.overlord.commons.fabric.utils.i18n;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 *
 *
 * @author eric.wittmann@redhat.com
 */
public class Messages {
    private static final String BUNDLE_NAME = "org.overlord.commons.fabric.utils.i18n.messages"; //$NON-NLS-1$

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

    /**
     * Look up a message in the i18n resource message bundle by key, then format
     * the message with the given params and return the result.
     *
     * @param key
     * @param params
     */
    public static String format(String key, Object... params) {
        ResourceBundle bundle = RESOURCE_BUNDLE;
        if (bundle.containsKey(key)) {
            String msg = bundle.getString(key);
            return MessageFormat.format(msg, params);
        } else {
            return MessageFormat.format("!!{0}!!", key); //$NON-NLS-1$
        }
    }
}
