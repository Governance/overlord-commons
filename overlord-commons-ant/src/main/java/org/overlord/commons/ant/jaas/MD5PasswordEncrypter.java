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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Ant task that generates an password with md5 encryption and hexadecimal
 * encoding, using exactly the same code used in apache karaf.
 * 
 * @author David Virgil Naranjo
 */
public class MD5PasswordEncrypter extends Task {

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
        String generatedPassword="";

        if (addproperty == null || addproperty.equals("")) { //$NON-NLS-1$
            throw new BuildException("\tThe output property is required for this task."); //$NON-NLS-1$
        }

        if (password == null || password.equals("")) { //$NON-NLS-1$
            throw new BuildException("\tThe password property is required for this task."); //$NON-NLS-1$
        }
        try {
            // Create MessageDigest instance for MD5
            MessageDigest md = MessageDigest.getInstance("MD5");
            //Add password bytes to digest
            md.update(password.getBytes());
            //Get the hash's bytes
            byte[] bytes = md.digest();

            //Get complete hashed password in hex format
            generatedPassword = new String(hexEncode(bytes));
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new BuildException("\tThere is a problem encrypting the password with MD5 algorithm"); //$NON-NLS-1$
        }

        if (addproperty != null && !addproperty.equals("")) { //$NON-NLS-1$
            getProject().setProperty(addproperty, generatedPassword);
        }
    }

    private static final byte[] hexTable = { (byte) '0', (byte) '1', (byte) '2', (byte) '3', (byte) '4', (byte) '5', (byte) '6', (byte) '7',
            (byte) '8', (byte) '9', (byte) 'a', (byte) 'b', (byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f' };

    /**
     * Hex encode.
     *
     * @param in
     *            the in
     * @return the string
     */
    public static String hexEncode(byte[] in) {
        int inOff = 0;
        int length = in.length;
        byte[] out = new byte[length * 2];
        for (int i = 0, j = 0; i < length; i++, j += 2) {
            out[j] = hexTable[(in[inOff] >> 4) & 0x0f];
            out[j + 1] = hexTable[in[inOff] & 0x0f];
            inOff++;
    }
        return new String(out);
    }

}
