<?xml version="1.0" encoding="UTF-8"?>
<!-- XSLT file to add the security domains to the standalone.xml -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:as="urn:jboss:domain:1.4"
  xmlns:sd="urn:jboss:domain:security:1.2" xmlns:xalan="http://xml.apache.org/xalan" version="1.0">

  <xsl:output method="xml" encoding="UTF-8" indent="yes" xalan:indent-amount="2" />

  <xsl:template match="as:profile/sd:subsystem/sd:security-domains">
    <security-domains>
      <security-domain name="overlord-idp" cache-type="default">
        <authentication>
          <login-module code="UsersRoles" flag="required">
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
            <module-option name="allowedIssuers" value="/s-ramp-ui,/s-ramp-governance,/dtgov-ui,/gadget-web" />
          </login-module>
          <login-module code="UsersRoles" flag="sufficient">
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