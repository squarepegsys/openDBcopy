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
package opendbcopy.io;

import org.apache.log4j.Logger;

import org.jdom.Document;

import org.jdom.output.XMLOutputter;

import java.io.FileWriter;
import java.io.IOException;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public final class ExportToXML {
    private static Logger logger = Logger.getLogger(ExportToXML.class.getName());

    /**
     * DOCUMENT ME!
     *
     * @param doc DOCUMENT ME!
     * @param fileName DOCUMENT ME!
     * @param encoding DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     */
    public static final void createXML(Document doc,
                                       String   fileName,
                                       String   encoding) throws IllegalArgumentException, IOException {
        if ((doc == null) || (fileName == null) || (encoding == null) || (fileName.length() == 0)) {
            throw new IllegalArgumentException("Missing arguments (doc, fileName or encoding) to export into XML file");
        }

        // now save the document in an XML file
        FileWriter   xml = new FileWriter(fileName);
        XMLOutputter serializer = new XMLOutputter("   ", true, encoding);
        serializer.setTextTrim(true);
        serializer.output(doc, xml);

        xml.flush();
        xml.close();

        logger.info(fileName + " successfully exported.");
    }
}
