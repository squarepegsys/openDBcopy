/*
 * Copyright (C) 2003 Anthony Smith
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * ----------------------------------------------------------------------------
 * TITLE $Id$
 * ---------------------------------------------------------------------------
 *
 * --------------------------------------------------------------------------*/
package opendbcopy.plugin;

import opendbcopy.config.XMLTags;

import opendbcopy.io.ExportToXML;
import opendbcopy.io.ImportFromXML;

import opendbcopy.model.exception.MissingAttributeException;
import opendbcopy.model.exception.MissingElementException;
import opendbcopy.model.exception.UnsupportedAttributeValueException;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.io.File;
import java.io.IOException;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class PluginMetadata {
    private File          pluginFile;
    private Document      pluginDocument;
    private Element       plugin;
    private Element       conf;
    private String        encoding;
    private String        className;
    private String        author;
    private String        description;

    /**
     * Creates a new Plugin object.
     *
     * @param pluginManager DOCUMENT ME!
     * @param pluginFile DOCUMENT ME!
     * @param encoding DOCUMENT ME!
     *
     * @throws UnsupportedAttributeValueException DOCUMENT ME!
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     * @throws JDOMException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     */
    public PluginMetadata(File          pluginFile,
                          String        encoding) throws UnsupportedAttributeValueException, MissingAttributeException, MissingElementException, JDOMException, IOException {
        this.encoding          = encoding;
        pluginDocument         = ImportFromXML.importFile(pluginFile);

        plugin = pluginDocument.getRootElement();

        if (plugin == null) {
            throw new MissingElementException(new Element("plugin"), "plugin");
        }

        if (plugin.getChild(XMLTags.CLASS) == null) {
            throw new MissingElementException(plugin, XMLTags.CLASS);
        } else if (plugin.getChild(XMLTags.CLASS).getAttributeValue(XMLTags.NAME) == null) {
            throw new MissingAttributeException(plugin.getChild(XMLTags.CLASS), XMLTags.NAME);
        }

        if (plugin.getChild(XMLTags.DESCRIPTION) == null) {
            throw new MissingElementException(plugin, XMLTags.DESCRIPTION);
        } else if (plugin.getChild(XMLTags.DESCRIPTION).getAttributeValue(XMLTags.VALUE) == null) {
            throw new MissingAttributeException(plugin.getChild(XMLTags.DESCRIPTION), XMLTags.VALUE);
        }

        this.className       = plugin.getChild(XMLTags.CLASS).getAttributeValue(XMLTags.NAME);
        this.description     = plugin.getChild(XMLTags.DESCRIPTION).getAttributeValue(XMLTags.VALUE);
        this.conf            = plugin.getChild(XMLTags.CONF);

        if (className.length() == 0) {
            throw new UnsupportedAttributeValueException(plugin.getChild(XMLTags.CLASS), XMLTags.NAME);
        }

        if (description.length() == 0) {
            throw new UnsupportedAttributeValueException(plugin.getChild(XMLTags.DESCRIPTION), XMLTags.VALUE);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param pathFilename DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public final void savePluginFile(String pathFilename) throws IOException {
        if (pathFilename == null) {
            pathFilename = pluginFile.toURI() + pluginFile.getName();
        }

        ExportToXML.createXML(pluginDocument, pathFilename, encoding);
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the author.
     */
    public String getAuthor() {
        return author;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the className.
     */
    public String getClassName() {
        return className;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the conf.
     */
    public Element getConf() {
        return conf;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the plugin.
     */
    public Element getPlugin() {
        return plugin;
    }
}
