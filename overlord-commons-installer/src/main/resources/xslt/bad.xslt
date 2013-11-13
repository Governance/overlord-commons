<?xml version="1.0" encoding="UTF-8"?>
<!-- XSLT file to add the security domains to the standalone.xml -->
<xsl:stylesheet xmlns="urn:jboss:domain:1.4" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:as="urn:jboss:domain:1.4"
  xmlns:sd="urn:jboss:domain:security:1.2" xmlns:xalan="http://xml.apache.org/xalan" version="1.0">

  <xsl:param name="keystore-password" />
  <xsl:param name="key-password" />

  <xsl:output method="xml" encoding="UTF-8" indent="yes" xalan:indent-amount="2" />

  <xsl:template match="as:extensions">
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
    <xsl:variable name="currentNS" select="namespace-uri(.)" />
    <xsl:element name="security-domains" namespace="{$currentNS}">
      <xsl:element name="security-domain" namespace="{$currentNS}">
        <xsl:attribute name="name">overlord-idp</xsl:attribute>
        <xsl:attribute name="cache-type">default</xsl:attribute>
        <xsl:element name="authentication" namespace="{$currentNS}">
          <xsl:element name="login-module" namespace="{$currentNS}">
            <xsl:attribute name="code">RealmDirect</xsl:attribute>
            <xsl:attribute name="flag">required</xsl:attribute>
            <xsl:element name="module-option" namespace="{$currentNS}">
              <xsl:attribute name="name">password-stacking</xsl:attribute>
              <xsl:attribute name="value">useFirstPass</xsl:attribute>
            </xsl:element>
          </xsl:element>
        </xsl:element>
      </xsl:element>
      <xsl:element name="security-domain" namespace="{$currentNS}">
        <xsl:attribute name="name">overlord-sp</xsl:attribute>
        <xsl:attribute name="cache-type">default</xsl:attribute>
        <xsl:element name="authentication" namespace="{$currentNS}">
          <xsl:element name="login-module" namespace="{$currentNS}">
            <xsl:attribute name="code">org.picketlink.identity.federation.bindings.jboss.auth.SAML2LoginModule</xsl:attribute>
            <xsl:attribute name="flag">required</xsl:attribute>
          </xsl:element>
        </xsl:element>
      </xsl:element>
      <xsl:element name="security-domain" namespace="{$currentNS}">
        <xsl:attribute name="name">overlord-jaxrs</xsl:attribute>
        <xsl:attribute name="cache-type">default</xsl:attribute>
        <xsl:element name="authentication" namespace="{$currentNS}">
          <xsl:element name="login-module" namespace="{$currentNS}">
            <xsl:attribute name="code">org.overlord.commons.auth.jboss7.SAMLBearerTokenLoginModule</xsl:attribute>
            <xsl:attribute name="flag">sufficient</xsl:attribute>
            <xsl:element name="module-option" namespace="{$currentNS}">
              <xsl:attribute name="name">allowedIssuers</xsl:attribute>
              <xsl:attribute name="value">/s-ramp-ui,/dtgov,/dtgov-ui,/gadget-web</xsl:attribute>
            </xsl:element>
            <xsl:element name="module-option" namespace="{$currentNS}">
              <xsl:attribute name="name">signatureRequired</xsl:attribute>
              <xsl:attribute name="value">true</xsl:attribute>
            </xsl:element>
            <xsl:element name="module-option" namespace="{$currentNS}">
              <xsl:attribute name="name">keystorePath</xsl:attribute>
              <xsl:attribute name="value">${jboss.server.config.dir}/overlord-saml.keystore</xsl:attribute>
            </xsl:element>
            <xsl:element name="module-option" namespace="{$currentNS}">
              <xsl:attribute name="name">keystorePassword</xsl:attribute>
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
                <xsl:value-of select="$key-password" />
                <xsl:text>}</xsl:text>
              </xsl:attribute>
            </xsl:element>
          </xsl:element>
          <xsl:element name="login-module" namespace="{$currentNS}">
            <xsl:attribute name="code">RealmDirect</xsl:attribute>
            <xsl:attribute name="flag">required</xsl:attribute>
            <xsl:element name="module-option" namespace="{$currentNS}">
              <xsl:attribute name="name">password-stacking</xsl:attribute>
              <xsl:attribute name="value">useFirstPass</xsl:attribute>
            </xsl:element>
          </xsl:element>
        </xsl:element>
      </xsl:element>
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
