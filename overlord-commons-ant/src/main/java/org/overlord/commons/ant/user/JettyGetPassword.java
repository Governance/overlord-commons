package org.overlord.commons.ant.user;

public class JettyGetPassword extends AbstractGetPassword {

    /*
     * (non-Javadoc)
     *
     * @see org.overlord.commons.ant.user.AbstractUserValidator#validate()
     */
    @Override
    protected boolean validate(String password) throws Exception {
        return true;
    }
}

