/*
 * Copyright (C) 2004 Anthony Smith
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
package opendbcopy.plugin.jsch;

import opendbcopy.config.XMLTags;
import opendbcopy.controller.MainController;
import opendbcopy.plugin.jsch.module.ScpTo;
import opendbcopy.plugin.model.DynamicPluginThread;
import opendbcopy.plugin.model.Model;
import opendbcopy.plugin.model.exception.MissingAttributeException;
import opendbcopy.plugin.model.exception.MissingElementException;
import opendbcopy.plugin.model.exception.PluginException;
import opendbcopy.plugin.model.simple.SimpleModel;

import org.jdom.Element;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class JschPlugin extends DynamicPluginThread {
    private Model   model;
    private Element conf;
    private Element input;
    private Element output;
    private String  operation;

    /**
     * Creates a new ZipPluign object.
     *
     * @param controller DOCUMENT ME!
     * @param baseModel DOCUMENT ME!
     *
     * @throws PluginException DOCUMENT ME!
     */
    public JschPlugin(MainController controller,
                      Model          baseModel) throws PluginException {
        super(controller, baseModel);
        this.model = (SimpleModel) baseModel;
    }

    /**
     * DOCUMENT ME!
     *
     * @throws PluginException DOCUMENT ME!
     */
    protected final void setUp() throws PluginException {
        if (model.getConf() == null) {
            throw new PluginException(new MissingElementException(model.getPlugin(), XMLTags.CONF));
        } else {
            conf = model.getConf();
        }

        // retrieve operation
        if (conf.getChild(XMLTags.OPERATION) == null) {
            throw new PluginException(new MissingElementException(conf, XMLTags.OPERATION));
        } else {
            if (conf.getChild(XMLTags.OPERATION).getAttributeValue(XMLTags.VALUE) == null) {
                throw new PluginException(new MissingAttributeException(conf.getChild(XMLTags.OPERATION), XMLTags.VALUE));
            } else {
            	if (conf.getChild(XMLTags.OPERATION).getAttributeValue(XMLTags.VALUE).length() == 0) {
            		throw new PluginException("Missing Operation");
            	} else {
                    operation = conf.getChild(XMLTags.OPERATION).getAttributeValue(XMLTags.VALUE);
            	}
            }
        }

        if (model.getInput() != null) {
            input = model.getInput();
        }

        if (model.getOutput() != null) {
            output = model.getOutput();
        } else {
            output = new Element(XMLTags.OUTPUT);
            model.setOutput(output);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws PluginException DOCUMENT ME!
     */
    public final void execute() throws PluginException {
    	if (operation.compareToIgnoreCase(Operation.SCP_TO) == 0) {
    		ScpTo.execute(logger, conf.getChild(operation.toLowerCase().trim()), input, output);
    	}
    }
}
