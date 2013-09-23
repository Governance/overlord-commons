#!/bin/sh

# Add User Utility
#
# A simple utility for adding new users to the properties file used
# for domain management authentication out of the box.
#

DIRNAME=`dirname "$0"`

# OS specific support (must be 'true' or 'false').
cygwin=false;
if  [ `uname|grep -i CYGWIN` ]; then
    cygwin = true;
fi

# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin ; then
    [ -n "$JBOSS_HOME" ] &&
        JBOSS_HOME=`cygpath --unix "$JBOSS_HOME"`
    [ -n "$JAVA_HOME" ] &&
        JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
    [ -n "$JAVAC_JAR" ] &&
        JAVAC_JAR=`cygpath --unix "$JAVAC_JAR"`
fi

# Setup JBOSS_HOME
RESOLVED_JBOSS_HOME=`cd "$DIRNAME/.."; pwd`
if [ "x$JBOSS_HOME" = "x" ]; then
    # get the full path (without any relative bits)
    JBOSS_HOME=$RESOLVED_JBOSS_HOME
else
 SANITIZED_JBOSS_HOME=`cd "$JBOSS_HOME"; pwd`
 if [ "$RESOLVED_JBOSS_HOME" != "$SANITIZED_JBOSS_HOME" ]; then
   echo "WARNING JBOSS_HOME may be pointing to a different installation - unpredictable results may occur."
   echo ""
 fi
fi
export JBOSS_HOME

# Setup the JVM
if [ "x$JAVA" = "x" ]; then
    if [ "x$JAVA_HOME" != "x" ]; then
        JAVA="$JAVA_HOME/bin/java"
    else
        JAVA="java"
    fi
fi

if [ "x$JBOSS_MODULEPATH" = "x" ]; then
    JBOSS_MODULEPATH="$JBOSS_HOME/modules"
fi

if [ "x$JBOSS_CONFIGPATH" = "x" ]; then
    JBOSS_CONFIGPATH="$JBOSS_HOME/standalone/configuration"
fi

if [ "x$JBOSS_VAULTPATH" = "x" ]; then
    JBOSS_VAULTPATH="$JBOSS_HOME/vault"
fi

JBOSS_VAULT_KEYSTORE="$JBOSS_CONFIGPATH/vault.keystore"

# For Cygwin, switch paths to Windows format before running java
if $cygwin; then
    JBOSS_HOME=`cygpath --path --windows "$JBOSS_HOME"`
    JAVA_HOME=`cygpath --path --windows "$JAVA_HOME"`
    JBOSS_MODULEPATH=`cygpath --path --windows "$JBOSS_MODULEPATH"`
    JBOSS_CONFIGPATH=`cygpath --path --windows "$JBOSS_CONFIGPATH"`
    JBOSS_VAULTPATH=`cygpath --path --windows "$JBOSS_VAULTPATH"`
    JBOSS_VAULT_KEYSTORE=`cygpath --path --windows "$JBOSS_VAULT_KEYSTORE"`
fi

# Sample JPDA settings for remote socket debugging
#JAVA_OPTS="$JAVA_OPTS -agentlib:jdwp=transport=dt_socket,address=8787,server=y,suspend=y"
# Uncomment to override standalone and domain user location  
#JAVA_OPTS="$JAVA_OPTS -Djboss.server.config.user.dir=../standalone/configuration -Djboss.domain.config.user.dir=../domain/configuration"

echo "*****************************************"
echo "**  Creating a new Overlord user       **"
echo "*****************************************"
echo ""
NEW_USER_NAME=""
while [ "x$NEW_USER_NAME" == "x" ]
do
    echo "User name: "
    read NEW_USER_NAME
done


NEW_USER_PASS=""
NEW_USER_PASS_REPEAT="__"
while [ "x$NEW_USER_PASS" != "x$NEW_USER_PASS_REPEAT" ]
do
    echo "Password: "
    read -s NEW_USER_PASS
    echo "Password (repeat): "
    read -s NEW_USER_PASS_REPEAT
done

echo "User roles (comma separated list e.g. dev,qa): "
read NEW_USER_ROLES


eval \"$JAVA\" $JAVA_OPTS \
         -jar \"$JBOSS_HOME/jboss-modules.jar\" \
         -mp \"${JBOSS_MODULEPATH}\" \
         org.overlord.commons.overlord-commons-auth-tool \
         adduser \
         -configdir \"${JBOSS_CONFIGPATH}\" \
         -vaultdir \"${JBOSS_VAULTPATH}\" \
         -keystore \"${JBOSS_VAULT_KEYSTORE}\" \
         -storepass vault22 \
         -alias vault \
         -salt 8675309K \
         -count 50 \
         -user $NEW_USER_NAME \
         -password $NEW_USER_PASS \
         -roles $NEW_USER_ROLES
