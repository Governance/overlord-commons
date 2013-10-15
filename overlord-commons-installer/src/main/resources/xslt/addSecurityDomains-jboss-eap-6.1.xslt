<?xml version="1.0" encoding="UTF-8"?>
<!-- XSLT file to add the security domains to the standalone.xml -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:param name="keystore-password" />
  <xsl:param name="overlord-password" />

  <xsl:output xmlns:xalan="http://xml.apache.org/xalan" method="xml" encoding="UTF-8" indent="yes" xalan:indent-amount="2" />

  <xsl:template match="/*[name()='server']/*[name()='extensions']">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()|text()" />
    </xsl:copy>
    <vault>
      <vault-option name="KEYSTORE_URL" value="${{jboss.server.config.dir}}/vault.keystore" />
      <vault-option name="KEYSTORE_PASSWORD" value="MASK-BIxfWy96dzp" />
      <vault-option name="KEYSTORE_ALIAS" value="vault" />
      <vault-option name="SALT" value="8675309K" />
      <vault-option name="ITERATION_COUNT" value="50" />
      <vault-option name="ENC_FILE_DIR" value="${{jboss.home.dir}}/vault/" />
    </vault>
  </xsl:template>

  <xsl:template match="*[name()='profile']/*[name()='subsystem']/*[name()='security-domains']">
    <security-domains>
      <security-domain name="overlord-idp" cache-type="default">
        <authentication>
          <login-module code="org.overlord.commons.auth.jboss7.VaultedUsersRolesLoginModule"
            flag="required">
            <module-option name="usersProperties" value="${{jboss.server.config.dir}}/overlord-idp-users.properties" />
            <module-option name="rolesProperties" value="${{jboss.server.config.dir}}/overlord-idp-roles.properties" />
          </login-module>
        </authentication>
      </security-domain>
      <security-domain name="overlord-sp" cache-type="default">
        <authentication>
          <login-module code="org.picketlink.identity.federation.bindings.jboss.auth.SAML2LoginModule"
            flag="required" />
        </authentication>
      </security-domain>
      <security-domain name="overlord-jaxrs" cache-type="default">
        <authentication>
          <login-module code="org.overlord.commons.auth.jboss7.SAMLBearerTokenLoginModule" flag="sufficient">
            <module-option name="allowedIssuers" value="/s-ramp-ui,/dtgov,/dtgov-ui,/gadget-web" />
            <module-option name="signatureRequired" value="true" />
            <module-option name="keystorePath" value="${{jboss.server.config.dir}}/vault.keystore" />
            <module-option name="keystorePassword">
              <xsl:attribute name="value">
                <xsl:text>${</xsl:text>
                <xsl:value-of select="$keystore-password" />
                <xsl:text>}</xsl:text>
              </xsl:attribute>
            </module-option>
            <module-option name="keyAlias" value="overlord" />
            <module-option name="keyPassword">
              <xsl:attribute name="value">
                <xsl:text>${</xsl:text>
                <xsl:value-of select="$overlord-password" />
                <xsl:text>}</xsl:text>
              </xsl:attribute>
            </module-option>
          </login-module>
          <login-module code="org.overlord.commons.auth.jboss7.VaultedUsersRolesLoginModule"
            flag="sufficient">
            <module-option name="usersProperties" value="${{jboss.server.config.dir}}/overlord-idp-users.properties" />
            <module-option name="rolesProperties" value="${{jboss.server.config.dir}}/overlord-idp-roles.properties" />
          </login-module>
        </authentication>
      </security-domain>
      <xsl:apply-templates select="@* | *" />
    </security-domains>
  </xsl:template>

  <!-- Copy everything else. -->
  <xsl:template match="@*|node()|text()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()|text()" />
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
