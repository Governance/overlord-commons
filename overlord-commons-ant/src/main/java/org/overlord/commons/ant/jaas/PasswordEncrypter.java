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
package org.overlord.commons.ant.jaas;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Ant task that generates an password with md5 encryption and hexadecimal
 * encoding, using exactly the same code used in apache karaf.
 *
 * @author David Virgil Naranjo
 */
public class PasswordEncrypter extends Task {

    private String password;

    private String addproperty;

    /**
     * Gets the password.
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password.
     *
     * @param password
     *            the new password
     */
    public void setPassword(String password) {
        this.password = password;
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

    /*
     * (non-Javadoc)
     *
     * @see org.apache.tools.ant.Task#execute()
     */
    @Override
    public void execute() throws BuildException {
        String generatedPassword = ""; //$NON-NLS-1$

        if (addproperty == null || addproperty.equals("")) { //$NON-NLS-1$
            throw new BuildException("\tThe output property is required for this task."); //$NON-NLS-1$
        }

        if (password == null || password.equals("")) { //$NON-NLS-1$
            throw new BuildException("\tThe password property is required for this task."); //$NON-NLS-1$
        }
        // Get complete hashed password in hex format
        generatedPassword = DigestUtils.sha256Hex(password);

        if (addproperty != null && !addproperty.equals("")) { //$NON-NLS-1$
            getProject().setProperty(addproperty, generatedPassword);
        }
    }


}
