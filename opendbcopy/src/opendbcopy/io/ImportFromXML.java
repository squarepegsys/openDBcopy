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
 * $Log$
 * --------------------------------------------------------------------------*/
package opendbcopy.io;

import org.apache.log4j.Logger;

import org.jdom.Document;

import org.jdom.input.SAXBuilder;

import java.io.File;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public final class ImportFromXML {
    private static Logger logger = Logger.getLogger(ImportFromXML.class.getName());

    /**
     * DOCUMENT ME!
     *
     * @param fileName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public static final Document importFile(String fileName) throws Exception {
        Document   doc = null;

        SAXBuilder parser = new SAXBuilder();
        doc = parser.build(new File(fileName));

        return doc;
    }
}
