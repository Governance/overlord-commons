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
package org.overlord.commons.ant.crypt;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.overlord.commons.codec.AesEncrypter;

/**
 * A custom ant task capable of encrypting a property using synchronous 
 * AES encryption.
 */
public class AesEncrypterTask extends Task {

    private String plain;
    private String addproperty;

    /**
     * Constructor.
     */
    public AesEncrypterTask() {
    }

    /**
     * @return the plain text value
     */
    public String getPlain() {
        return plain;
    }

    /**
     * @param plain
     */
    public void setPlain(String plain) {
        this.plain = plain;
    }

    /**
     * Gets the addproperty.
     *
     * @return the addproperty
     */
    public String getAddproperty() {
        return addproperty;
    }

    /**
     * Sets the addproperty.
     *
     * @param addproperty
     *            the new addproperty
     */
    public void setAddproperty(String addproperty) {
        this.addproperty = addproperty;
    }

    /**
     * @see org.apache.tools.ant.Task#execute()
     */
    @Override
    public void execute() throws BuildException {
        String generatedPassword = ""; //$NON-NLS-1$

        if (addproperty == null || addproperty.equals("")) { //$NON-NLS-1$
            throw new BuildException("\tThe 'addproperty' attribute is required."); //$NON-NLS-1$
        }

        if (plain == null || plain.equals("")) { //$NON-NLS-1$
            throw new BuildException("\tThe 'plain' attribute is required."); //$NON-NLS-1$
        }
        // Encrypt the password using AES encryptiong
        generatedPassword = AesEncrypter.encrypt(plain);

        if (addproperty != null && !addproperty.equals("")) { //$NON-NLS-1$
            getProject().setProperty(addproperty, generatedPassword);
        }
    }

}
