<?xml version="1.0" encoding="UTF-8"?>
<!-- XSLT file to add the security domains to the standalone.xml -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:param name="keystore-password" />
  <xsl:param name="key-password" />

  <xsl:output xmlns:xalan="http://xml.apache.org/xalan" method="xml" encoding="UTF-8" indent="yes" xalan:indent-amount="2" />

  <xsl:template match="/*[name()='server']/*[name()='extensions']">
    <xsl:variable name="currentNS" select="namespace-uri(.)" />
    <xsl:copy>
      <xsl:apply-templates select="@*|node()|text()" />
    </xsl:copy>
    <xsl:element name="vault" namespace="{$currentNS}">
      <xsl:element name="vault-option" namespace="{$currentNS}">
        <xsl:attribute name="name">KEYSTORE_URL</xsl:attribute>
        <xsl:attribute name="value">${jboss.server.config.dir}/vault.keystore</xsl:attribute>
      </xsl:element>
      <xsl:element name="vault-option" namespace="{$currentNS}">
        <xsl:attribute name="name">KEYSTORE_PASSWORD</xsl:attribute>
        <xsl:attribute name="value">MASK-BIxfWy96dzp</xsl:attribute>
      </xsl:element>
      <xsl:element name="vault-option" namespace="{$currentNS}">
        <xsl:attribute name="name">KEYSTORE_ALIAS</xsl:attribute>
        <xsl:attribute name="value">vault</xsl:attribute>
      </xsl:element>
      <xsl:element name="vault-option" namespace="{$currentNS}">
        <xsl:attribute name="name">SALT</xsl:attribute>
        <xsl:attribute name="value">8675309K</xsl:attribute>
      </xsl:element>
      <xsl:element name="vault-option" namespace="{$currentNS}">
        <xsl:attribute name="name">ITERATION_COUNT</xsl:attribute>
        <xsl:attribute name="value">50</xsl:attribute>
      </xsl:element>
      <xsl:element name="vault-option" namespace="{$currentNS}">
        <xsl:attribute name="name">ENC_FILE_DIR</xsl:attribute>
        <xsl:attribute name="value">${jboss.home.dir}/vault/</xsl:attribute>
      </xsl:element>
    </xsl:element>
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
    </xsl:element>
  </xsl:template>

  <!-- Copy everything else. -->
  <xsl:template match="@*|node()|text()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()|text()" />
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
