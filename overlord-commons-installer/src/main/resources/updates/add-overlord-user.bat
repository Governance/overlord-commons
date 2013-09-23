@echo off
rem -------------------------------------------------------------------------
rem Add User script for Windows
rem -------------------------------------------------------------------------
rem
rem A simple utility for adding new users to the properties file used 
rem for domain management authentication out of the box.

rem $Id$

@if not "%ECHO%" == ""  echo %ECHO%
@if "%OS%" == "Windows_NT" setlocal

if "%OS%" == "Windows_NT" (
  set "DIRNAME=%~dp0%"
) else (
  set DIRNAME=.\
)

pushd %DIRNAME%..
set "RESOLVED_JBOSS_HOME=%CD%"
popd

if "x%JBOSS_HOME%" == "x" (
  set "JBOSS_HOME=%RESOLVED_JBOSS_HOME%" 
)

pushd "%JBOSS_HOME%"
set "SANITIZED_JBOSS_HOME=%CD%"
popd

if "%RESOLVED_JBOSS_HOME%" NEQ "%SANITIZED_JBOSS_HOME%" (
    echo WARNING JBOSS_HOME may be pointing to a different installation - unpredictable results may occur.
)

set DIRNAME=

if "%OS%" == "Windows_NT" (
  set "PROGNAME=%~nx0%"
) else (
  set "PROGNAME=jdr.bat"
)

rem Setup JBoss specific properties
if "x%JAVA_HOME%" == "x" (
  set  JAVA=java
  echo JAVA_HOME is not set. Unexpected results may occur.
  echo Set JAVA_HOME to the directory of your local JDK to avoid this message.
) else (
  set "JAVA=%JAVA_HOME%\bin\java"
)

rem Find jboss-modules.jar, or we can't continue
if exist "%JBOSS_HOME%\jboss-modules.jar" (
    set "RUNJAR=%JBOSS_HOME%\jboss-modules.jar"
) else (
  echo Could not locate "%JBOSS_HOME%\jboss-modules.jar".
  echo Please check that you are in the bin directory when running this script.
  goto END
)

rem Setup JBoss specific properties

rem Set default module root paths
if "x%JBOSS_MODULEPATH%" == "x" (
  set "JBOSS_MODULEPATH=%JBOSS_HOME%\modules"
)

if "x%JBOSS_CONFIGPATH%" == "x" (
  set "JBOSS_CONFIGPATH=%JBOSS_HOME%\standalone\configuration"
)

if "x%JBOSS_VAULTPATH%" == "x" (
  set "JBOSS_VAULTPATH=%JBOSS_HOME%\vault"
)

set "JBOSS_VAULT_KEYSTORE=%JBOSS_CONFIGPATH%\vault.keystore"

echo "*****************************************"
echo "**  Creating a new Overlord user       **"
echo "*****************************************"
echo ""
set /p NEW_USER_NAME="Username: "

:loop
set /p NEW_USER_PASSWORD="Password: "
set /p NEW_USER_PASSWORD_REPEAT="Password (repeat): "
if NOT "x%NEW_USER_PASSWORD%" == "x%NEW_USER_PASSWORD_REPEAT%" (
  goto :loop
)

set /p NEW_USER_ROLES="User roles (comma separated list e.g. dev,qa): "

rem Uncomment to override standalone and domain user location  
rem set "JAVA_OPTS=%JAVA_OPTS% -Djboss.server.config.user.dir=..\standalone\configuration -Djboss.domain.config.user.dir=..\domain\configuration"

"%JAVA%" %JAVA_OPTS% ^
    -jar "%JBOSS_HOME%\jboss-modules.jar" ^
    -mp "%JBOSS_MODULEPATH%" ^
         org.overlord.commons.overlord-commons-auth-tool ^
         adduser ^
         -configdir "%JBOSS_CONFIGPATH%" ^
         -vaultdir "%JBOSS_VAULTPATH%" ^
         -keystore "%JBOSS_VAULT_KEYSTORE%" ^
         -storepass vault22 ^
         -alias vault ^
         -salt 8675309K ^
         -count 50 ^
         -user "%NEW_USER_NAME%" ^
         -password "%NEW_USER_PASSWORD%" ^
         -roles "%NEW_USER_ROLES%"

:END
if "x%NOPAUSE%" == "x" pause
