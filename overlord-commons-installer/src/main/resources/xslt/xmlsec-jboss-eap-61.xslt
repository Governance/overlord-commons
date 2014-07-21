<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns="urn:jboss:module:1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  version="1.0">

  <xsl:output method="xml" encoding="UTF-8" />

  <!-- Copy everything. -->
  <xsl:template match="@* | node() | text()">
    <xsl:copy>
      <xsl:apply-templates select="@* | node() | text()" />
    </xsl:copy>
  </xsl:template>

  <!-- Temporary fix for EAP 6.1. See https://bugzilla.redhat.com/show_bug.cgi?id=979334. -->
  <xsl:template match="/*">
    <xsl:copy>
      <xsl:apply-templates select="@* | node() | text()" />

      <exports>
        <exclude path="javax/**" />
      </exports>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>