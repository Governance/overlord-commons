<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:output method="xml" encoding="UTF-8"
      doctype-public="-//Jetty//Configure//EN"
      doctype-system="http://www.eclipse.org/jetty/configure.dtd"
   />

  <xsl:template match="/Configure/child::*[last()]">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()|text()" />
    </xsl:copy>
    
    <xsl:text>
    
    </xsl:text>
    <Set class="org.eclipse.jetty.util.resource.Resource" name="defaultUseCaches">false</Set>

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
            </xsl:text><New class="org.eclipse.jetty.plus.jaas.JAASLoginService"><xsl:text>
                </xsl:text><Set name="name">Overlord</Set><xsl:text>
                </xsl:text><Set name="loginModuleName">karaf</Set><xsl:text>
                </xsl:text><Set name="roleClassNames"><xsl:text>
                    </xsl:text><Array type="java.lang.String"><xsl:text>
                        </xsl:text><Item>org.apache.karaf.jaas.boot.principal.RolePrincipal</Item><xsl:text>
                    </xsl:text></Array><xsl:text>
                </xsl:text></Set><xsl:text>
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
