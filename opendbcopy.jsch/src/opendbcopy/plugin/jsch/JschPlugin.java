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

import opendbcopy.plugin.model.DynamicPluginThread;
import opendbcopy.plugin.model.Model;
import opendbcopy.plugin.model.database.DatabaseModel;
import opendbcopy.plugin.model.exception.MissingAttributeException;
import opendbcopy.plugin.model.exception.MissingElementException;
import opendbcopy.plugin.model.exception.PluginException;

import opendbcopy.util.InputOutputHelper;

import org.jdom.Element;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


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
        this.model = (DatabaseModel) baseModel;
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
                operation = conf.getChild(XMLTags.OPERATION).getAttributeValue(XMLTags.VALUE);
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
        List fileLists = input.getChildren(XMLTags.FILELIST);

        if ((fileLists == null) || (fileLists.size() == 0)) {
            throw new PluginException(new MissingElementException(input, XMLTags.FILELIST));
        }

        Iterator itFileLists = fileLists.iterator();

        while (itFileLists.hasNext()) {
            Element filelist = (Element) itFileLists.next();
            String  identifier = filelist.getAttributeValue(XMLTags.ID);
            String  dir = filelist.getAttributeValue(XMLTags.DIR);

            if (identifier == null) {
                throw new PluginException(new MissingAttributeException(filelist, XMLTags.ID));
            }

            try {
                ArrayList files = InputOutputHelper.getFileList(filelist);

                // Create a buffer for reading the files
                byte[] buf = new byte[1024];

                // Create the ZIP file
                String          outFilename = dir + "/" + identifier + ".zip";
                ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outFilename));

                // Set the compression ratio
                out.setLevel(Deflater.BEST_COMPRESSION);

                // Compress the files
                for (int i = 0; i < files.size(); i++) {
                    File            file = (File) files.get(i);

                    FileInputStream in = new FileInputStream(file.getAbsolutePath());

                    // Add ZIP entry to output stream.
                    out.putNextEntry(new ZipEntry(dir + "/" + file.getName()));

                    // Transfer bytes from the file to the ZIP file
                    int len;

                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }

                    // Complete the entry
                    out.closeEntry();
                    in.close();
                }

                // Complete the ZIP file
                out.close();

                // set output of plugin
                Element outputFileList = InputOutputHelper.createFileListElement(new File(outFilename), identifier);

                if (output == null) {
                    output = new Element(XMLTags.OUTPUT);
                }

                output.addContent(outputFileList);
            } catch (MissingAttributeException e) {
                throw new PluginException(e);
            } catch (MissingElementException e) {
                throw new PluginException(e);
            } catch (FileNotFoundException e) {
                throw new PluginException(e);
            } catch (IOException e) {
                throw new PluginException(e);
            }
        }

        model.setOutput(output);
    }
}
