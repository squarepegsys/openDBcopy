<?xml version="1.0" encoding="UTF-8" ?>
<!--

XML Style Sheet for OpenOffice.org documents.  Written by Mark Headd.  Any problems, please let me know - markh@voiceingov.org.

*****************************************************************************************************************************************
This style sheet is based on one written by Philipp "philiKON" von Weitershausen (philikon@philikon.de), published under the terms of the Mozilla Public License (MPL).   You can obtain that style sheet from http://www.zope.org/Members/philikon/ZooDocument.

The contents of this file are subject to the Mozilla Public License Version 1.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.mozilla.org/MPL/ 
Software distributed under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for the specific language governing rights and limitations under the License. 
*****************************************************************************************************************************************

This style sheet is based on one written by Philipp "philiKON" von Weitershausen (philikon@philikon.de), published under the terms of the Mozilla Public License (MPL).   You can obtain that style sheet from http://www.zope.org/Members/philikon/ZooDocument.

This XSLT Style Sheet can be used to convert OOo documents to compliant XHTML Transitional using the XML filter feature of the OOo 1.1 application.  To use this style sheet, follow these directions:

* Save this file to hard drive, or accessible network drive.
* Open the OOo 1.1 application.
* From the main menu bar, select "Tools" then "XML Filter Settings".
* In the "XML Filter Settings" menu, select "New".
* In the "General" tab, provide a filter name (e.g., "W3C XHTML 1.0"), application name (default is OpenOffice.org Writer), name of file type (e.g.,
   "w3c_xhtml"), file extension (use "html"), and comments (if desired).
* In the "Transformation" tab, for the "XSLT for export" option browse to the location of this XSLT file on your hard drive or network drive.
* You can leave all other options on this tab blank.
* Click "OK".
* Close "XML Filter Settings" menu.
* To save a file to XHTML 1.0, select "File" from the main menu - use the "Export" option.
* Under file format, select the file type associated with this style sheet (if you used the instructions above, the "w3c_xhtml" file type should appear.

-->

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:svg="http://www.w3.org/2000/svg"
                xmlns:office="http://openoffice.org/2000/office"
                xmlns:meta="http://openoffice.org/2000/meta"
                xmlns:style="http://openoffice.org/2000/style"
                xmlns:text="http://openoffice.org/2000/text"
                xmlns:table="http://openoffice.org/2000/table"
                xmlns:draw="http://openoffice.org/2000/drawing"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
		   exclude-result-prefixes="draw fo office meta style svg table text xlink dc">

<!-- 
These variables can be used to provide some document formating.  To initialize these vairables, From the main menu select "File" then "Properties".

UNDER THE DESCRIPTION TAB:
Title: is used as the page title (i.e., content between the <title></title> tags in the resultant HTML document.

UNDER THE USER DEFINED TAB:
Info 1: Specifies the location of an external style sheet to format the resulting HTML document.  Be sure to use the file extension (e.g., "basic.css")
           Resultant HTML file will refer to a CSS file located in a parallel folder called "css"  (e.g., "css/basic.css")
           
(The following table formatting elements can be ignored if table style is provided for in a separate style sheet.)

Info 2: Sets the cell padding for any tables used in the document.  If no values are entered, the table may not display properly, unless cell padding value is     
           provided for in separate style sheet.           
Info 3: Sets the cell spacing for any tables used in the document.  If no values are entered, the table may not display properly, unless cell spacing value is     
           provided for in separate style sheet.           
Info 4: Sets the table width for any tables used in the document.  If no values are entered, the table may not display properly, unless table width value is     
           provided for in separate style sheet.  (Note: when entering a value, enter appropriate unit as well - e.g.,  80%, 250px, etc.)

If you want to change the names of these fields (by clicking on the "Info Fields" button on the "User Defined" tab) 
to titles that are more intuitive, go ahead.  The names of these fields are not important, but their order is - the order outlined
above can't be changed without modifying the filter itself.

           
-->

<xsl:variable name="css" select="//office:meta/meta:user-defined[position()=1]" />
<xsl:variable name="cellpadding" select="//office:meta/meta:user-defined[position()=2]" />
<xsl:variable name="cellspacing" select="//office:meta/meta:user-defined[position()=3]" />
<xsl:variable name="width" select="//office:meta/meta:user-defined[position()=4]" />


<xsl:output method="xml" omit-xml-declaration="no" encoding="UTF-8"/>
<xsl:template match="/">

<!-- Add DOCTYPE Declaration and Page Title -->

<xsl:text disable-output-escaping="yes">	&lt;!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.0 Transitional//EN'
 'http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd'&gt;</xsl:text>

<html>
<head>
	<title><xsl:value-of select="//dc:title" /></title>

<!-- Add reference to external CSS file -->

<xsl:element name="link">
<xsl:attribute name="rel">stylesheet</xsl:attribute>
<xsl:attribute name="href"><xsl:value-of select="concat('css/', $css)"/></xsl:attribute>
<xsl:attribute name="type">text/css</xsl:attribute>
<xsl:attribute name="media">all</xsl:attribute>
</xsl:element>

<xsl:element name="style">
<xsl:attribute name="type">text/css</xsl:attribute>
table {
align: center;
width: <xsl:value-of select="$width" />;
cellpadding: <xsl:value-of select="$cellpadding" />;
cellspacing: <xsl:value-of select="$cellspacing" />;
}
</xsl:element>
	
</head>
  <xsl:apply-templates />
</html>

</xsl:template>
<xsl:template match="//office:body">
<body>

<xsl:apply-templates />

<!-- place footnotes at end of page -->

<xsl:for-each select="//text:footnote">
<p class="footnote">
<span>
<xsl:element name="a">
<xsl:attribute name="href"><xsl:value-of select="concat('#body', @text:id)" /></xsl:attribute>
<xsl:attribute name="id"><xsl:value-of select="@text:id"/></xsl:attribute>
<xsl:attribute name="class">footnotesymbol</xsl:attribute>
<xsl:value-of select="text:footnote-citation"/>
</xsl:element>
</span>
<xsl:text disable-output-escaping="yes"> </xsl:text>
<xsl:value-of select="text:footnote-body/text:p[@text:style-name='Footnote']"/>
</p>

</xsl:for-each>

</body>
</xsl:template>

<!-- Filter superfluous information from result document -->

<xsl:template match="office:meta">
<!-- -->
</xsl:template>

<xsl:template match="office:settings">
<!-- -->
</xsl:template>

<xsl:template match="office:automatic-styles">
<!-- -->
</xsl:template>

<!-- Remove page header and footer if they exist in document -->

<xsl:template match="style:header">
<xsl:comment>Removed document header: <xsl:value-of select="."/></xsl:comment>
</xsl:template>

<xsl:template match="style:footer">
<xsl:comment>Removed document footer: <xsl:value-of select="."/></xsl:comment>
</xsl:template>

<!-- Headings -->

<xsl:template match="//text:h">
<xsl:if test="@text:style-name='Heading 1' or @text:level='1'"> 
		<h1>
		<xsl:apply-templates />
		</h1>
	</xsl:if>
<xsl:if test="@text:style-name='Heading 2' or @text:level='2'"> 
		<h2>
		<xsl:apply-templates />
		</h2>
	</xsl:if>
<xsl:if test="@text:style-name='Heading 3' or @text:level='3'"> 
		<h3>
		<xsl:apply-templates />
		</h3>
	</xsl:if>
<xsl:if test="@text:style-name='Heading 4' or @text:level='4'"> 
		<h4>
		<xsl:apply-templates />
		</h4>
	</xsl:if>
	<xsl:if test="@text:style-name='Heading 5' or @text:level='5'"> 
		<h5>
		<xsl:apply-templates />
		</h5>
	</xsl:if>
</xsl:template> 


<!-- Paragraph -->
<xsl:template match="//text:p">
<xsl:choose>
	<xsl:when  test="@text:style-name='Heading 1'">
		<h1>
		<xsl:apply-templates />
		</h1>
	</xsl:when>
	<xsl:when  test="@text:style-name='Heading 2'">
		<h2>
		<xsl:apply-templates />
		</h2>
	</xsl:when>
	<xsl:when  test="@text:style-name='Heading 3'">
		<h3>
		<xsl:apply-templates />
		</h3>
	</xsl:when>
	<xsl:when  test="@text:style-name='Heading 4'">
		<h4>
		<xsl:apply-templates />
		</h4>
	</xsl:when>
	<xsl:when  test="@text:style-name='Heading 5'">
		<h5>
		<xsl:apply-templates />
		</h5>
	</xsl:when>
<xsl:otherwise>
<xsl:choose>
			<xsl:when test="string-length()=0">
      	<xsl:apply-templates />
			</xsl:when>
			<xsl:otherwise>
     		<p><xsl:apply-templates /></p>
			</xsl:otherwise>
	</xsl:choose>
</xsl:otherwise>
</xsl:choose> 
</xsl:template>

<!-- Preformatted text -->
<xsl:template match="//text:p[@text:style-name='Preformatted Text']">
<pre>
<xsl:apply-templates />
</pre>
</xsl:template>

<!-- Space -->
<xsl:template match="//text:s">
  <xsl:for-each select="@text:c">   
    <xsl:text>&#160;</xsl:text>
  </xsl:for-each>
</xsl:template>

<!-- Tab Stop-->
<xsl:template match="//text:tab-stop">
  <xsl:text>	</xsl:text>
</xsl:template>

<!-- Span -->
<xsl:template match="//text:span">
<xsl:choose>
	<xsl:when test="@text:style-name='Emphasis'"> <!-- Emphasis -->
		<em>
		<xsl:apply-templates />
		</em>
	</xsl:when>
	<xsl:when test="@text:style-name='T1'"> <!-- Emphasis -->
		<strong>
		<xsl:apply-templates />
		</strong>
	</xsl:when>	
	<xsl:when test="@text:style-name='Strong Emphasis'"> <!-- Strong Emphasis -->
		<strong>
		<xsl:apply-templates />
		</strong>
	</xsl:when>
	<xsl:when test="@text:style-name='T2'"> <!-- Strong Emphasis -->
		<strong>
		<xsl:apply-templates />
		</strong>
	</xsl:when>
	<xsl:when test="@text:style-name='Definition'"> <!-- Definition -->
		<dfn>
		<xsl:apply-templates />
		</dfn>
	</xsl:when>
	<xsl:when test="@text:style-name='Citation'"> <!-- Citation -->
		<cite>
		<xsl:apply-templates />
		</cite>
	</xsl:when>
	<xsl:when test="@text:style-name='Source Text'"> <!-- Source Text -->
		<code>
		<xsl:apply-templates />
		</code>
	</xsl:when>
	<xsl:otherwise>				 	 <!-- All else -->
     <xsl:value-of select="."/>
	</xsl:otherwise>
</xsl:choose>
</xsl:template>


<!-- Footnotes -->
<xsl:template match="//text:footnote">

<span class="Footnoteanchor">
<xsl:text disable-output-escaping="yes"> </xsl:text>
<xsl:element name="a">
<xsl:attribute name="href"><xsl:value-of select="concat('#', @text:id)"/></xsl:attribute>
<xsl:attribute name="id"><xsl:value-of select="concat('body', @text:id)"/></xsl:attribute>
<xsl:value-of select="text:footnote-citation"/>
</xsl:element>
</span>
</xsl:template>

<!-- Link -->
<xsl:template match="//text:a">
<a>
  <xsl:attribute name="href"><xsl:value-of select="@xlink:href" /></xsl:attribute>
  <xsl:if test="@office:target-frame-name">
   <xsl:attribute name="target"><xsl:value-of select="@office:target-frame-name" /></xsl:attribute>
  </xsl:if>
  <xsl:apply-templates />
</a>
</xsl:template>


<!-- Bookmark -->
<xsl:template match="//text:bookmark">
<a id="{@text:name}" />
</xsl:template>


<!-- Ordered List  -->
<xsl:template match="//text:ordered-list">
<ol>
  <xsl:apply-templates />
</ol>
</xsl:template>


<!-- Unordered List  -->
<xsl:template match="//text:unordered-list">
<ul>
  <xsl:apply-templates />
</ul>
</xsl:template>


<!-- List Item -->
<xsl:template match="//text:list-item">
<li><xsl:apply-templates /></li>
</xsl:template>


<!-- Line break  -->
<xsl:template match="//text:line-break">
<br />
</xsl:template>


<!-- Tables  -->

<xsl:template match="//table:table">
<xsl:element name="table">
<xsl:attribute name="summary">This table used for layout purposes only.</xsl:attribute>
<xsl:apply-templates />
</xsl:element>
</xsl:template>


<!-- Table Header Rows -->
<xsl:template match="//table:table-header-rows">
   <tr>
  <xsl:apply-templates mode="header-row" />
 </tr>
 </xsl:template>


<!-- Table Row -->
<xsl:template match="//table:table-row">
<tr>
  <xsl:apply-templates />
</tr>
</xsl:template>


<!-- Table Cell -->
<xsl:template match="//table:table-cell">
<td>
  <xsl:apply-templates />
</td>
</xsl:template>

<xsl:template match="//table:table-cell" mode="header-row">
<th>
  <xsl:apply-templates />
</th>
</xsl:template>



<!-- Images: 
You must provide a name for the image that is the same as the file name (e.g., "sample_image.gif").  Resultant HTML file will refer to an image file located in a parallel folder called "images"  (e.g., "images/sample_image.gif")

* Right mouse click the image and select "Graphics"
* In the "Options" tab, enter the file name in the "Name" field - be sure to use the appropriate file extension (e.g., .gif, .jpg).  
* Enter the ALT text to be used in the "Alternative (Text Only)" field.
 -->

<xsl:template match="//draw:image">
<xsl:text disable-output-escaping="yes">&lt;img alt="</xsl:text><xsl:value-of select="svg:desc" /><xsl:text disable-output-escaping="yes">" src="images/</xsl:text><xsl:value-of select="@draw:name" /><xsl:text disable-output-escaping="yes">" /&gt;</xsl:text>
</xsl:template>

</xsl:stylesheet>

