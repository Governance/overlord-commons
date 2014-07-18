<?xml version="1.0" encoding="UTF-8"?>
<!-- XSLT file to add the security domains to the standalone.xml -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:param name="keystore-password" />
  <xsl:param name="key-password" />

  <xsl:output xmlns:xalan="http://xml.apache.org/xalan" method="xml" encoding="UTF-8" indent="yes"
    xalan:indent-amount="2" />

  <xsl:template match="/*[name()='server']/*[name()='extensions']">
    <xsl:variable name="currentNS" select="namespace-uri(.)" />
    <xsl:element name="extensions" namespace="{$currentNS}">
      <xsl:apply-templates select="./node()|./text()" />
      <xsl:element name="extension" namespace="{$currentNS}">
        <xsl:attribute name="module">org.overlord.commons.eap.extensions.config</xsl:attribute>
      </xsl:element>
    </xsl:element>
  </xsl:template>

  <xsl:template match="/*[name()='server']/*[name()='profile']">
    <xsl:variable name="currentNS" select="namespace-uri(.)" />
    <xsl:element name="profile" namespace="{$currentNS}">
      <xsl:apply-templates select="./node()|./text()" />
      <xsl:element name="subsystem" namespace="urn:jboss:domain:overlord-configuration:1.0">
        <xsl:element name="configurations" namespace="urn:jboss:domain:overlord-configuration:1.0">
          <xsl:element name="configuration" namespace="urn:jboss:domain:overlord-configuration:1.0">
            <xsl:attribute name="name">overlord</xsl:attribute>
            <xsl:element name="properties" namespace="urn:jboss:domain:overlord-configuration:1.0">
              <xsl:element name="property" namespace="urn:jboss:domain:overlord-configuration:1.0">
                <xsl:attribute name="name">overlord.port</xsl:attribute>
                <xsl:attribute name="value">8080</xsl:attribute>
              </xsl:element>
              <xsl:element name="property" namespace="urn:jboss:domain:overlord-configuration:1.0">
                <xsl:attribute name="name">overlord.baseUrl</xsl:attribute>
                <xsl:attribute name="value">http://localhost:${overlord.port}</xsl:attribute>
              </xsl:element>
              <xsl:element name="property" namespace="urn:jboss:domain:overlord-configuration:1.0">
                <xsl:attribute name="name">overlord.auth.saml-keystore</xsl:attribute>
                <xsl:attribute name="value">${sys:jboss.server.config.dir}/overlord-saml.keystore</xsl:attribute>
              </xsl:element>
              <xsl:element name="property" namespace="urn:jboss:domain:overlord-configuration:1.0">
                <xsl:attribute name="name">overlord.auth.saml-keystore-password</xsl:attribute>
                <xsl:attribute name="value">
                  <xsl:text>${vault:</xsl:text>
                  <xsl:value-of select="$keystore-password" />
                  <xsl:text>}</xsl:text>
                </xsl:attribute>
              </xsl:element>
              <xsl:element name="property" namespace="urn:jboss:domain:overlord-configuration:1.0">
                <xsl:attribute name="name">overlord.auth.saml-key-alias</xsl:attribute>
                <xsl:attribute name="value">overlord</xsl:attribute>
              </xsl:element>
              <xsl:element name="property" namespace="urn:jboss:domain:overlord-configuration:1.0">
                <xsl:attribute name="name">overlord.auth.saml-key-alias-password</xsl:attribute>
                <xsl:attribute name="value">
                  <xsl:text>${vault:</xsl:text>
                  <xsl:value-of select="$key-password" />
                  <xsl:text>}</xsl:text>
                </xsl:attribute>
              </xsl:element>
            </xsl:element>
          </xsl:element>
        </xsl:element>
      </xsl:element>
    </xsl:element>
  </xsl:template>

  <!-- Copy everything else. -->
  <xsl:template match="@*|node()|text()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()|text()" />
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
