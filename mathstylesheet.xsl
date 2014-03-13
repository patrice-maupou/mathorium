<?xml version="1.0" encoding="UTF-8"?>

<!--
    Document   : mathtylesheet.xsl
    Created on : 23 novembre 2013, 17:17
    Author     : Patrice
    Description:
        Purpose of transformation follows.
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html"/>

    <!-- TODO customize transformation rules 
         syntax recommendation http://www.w3.org/TR/xslt 
    -->
    <xsl:template match="/">
        <html>
            <head>
                <title>math</title>
            </head>
            <body>
                <xsl:for-each select="expressions/generator">                    
                    <xsl:apply-templates select="@name"></xsl:apply-templates>
                    <xsl:for-each select="expression">   
                        <xsl:apply-templates/>                        
                    </xsl:for-each>                        
                </xsl:for-each>
            </body>
        </html>
    </xsl:template>

</xsl:stylesheet>
