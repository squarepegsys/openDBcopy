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

import opendbcopy.config.FileCharacter;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public final class Reader {
    /**
     * DOCUMENT ME!
     *
     * @param fileName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     * @throws FileNotFoundException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     */
    public static final StringBuffer read(String fileName) throws IllegalArgumentException, FileNotFoundException, IOException {
        if ((fileName == null) || (fileName.length() == 0)) {
            throw new IllegalArgumentException("Missing fileName");
        }

        StringBuffer stringBuffer = new StringBuffer();
        char[]       cbuf = null;

        FileReader   fileReader = new FileReader(fileName);

        int          c = fileReader.read();

        while (c != FileCharacter.EOF) {
            stringBuffer.append((char) c);
            c = fileReader.read();
        }

        fileReader.close();

        return stringBuffer;
    }
}
