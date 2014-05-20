<?xml version="1.0" encoding="UTF-8"?>
<!-- XSLT file to add the security domains to the standalone.xml -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:output method="xml" encoding="UTF-8"
      doctype-public="-//Jetty//Configure//EN"
      doctype-system="http://www.eclipse.org/jetty/configure.dtd"
   />

  <xsl:template match="/Configure/Set[@name = 'ThreadPool']">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()|text()" />
    </xsl:copy>

    <xsl:text>
    
    </xsl:text>
    <xsl:comment> =========================================================== </xsl:comment>
    <xsl:text>
    </xsl:text>
    <xsl:comment> Add the Overlord Authentication Realm                       </xsl:comment>
    <xsl:text>
    </xsl:text>
    <xsl:comment> =========================================================== </xsl:comment>
    <xsl:text>
    </xsl:text>
    <Call name="addBean"><xsl:text>
      </xsl:text><Arg><xsl:text>
        </xsl:text><New class="org.eclipse.jetty.security.HashLoginService"><xsl:text>
          </xsl:text><Set name="name">Overlord</Set><xsl:text>
          </xsl:text><Set name="config"><xsl:text>
            </xsl:text><SystemProperty name="jetty.home.url" default="." />/etc/realm.properties</Set><xsl:text>
          </xsl:text><Set name="refreshInterval">0</Set><xsl:text>
        </xsl:text></New><xsl:text>
      </xsl:text></Arg><xsl:text>
    </xsl:text></Call>
  </xsl:template>

  <!-- Copy everything else. -->
  <xsl:template match="@*|node()|text()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()|text()" />
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
