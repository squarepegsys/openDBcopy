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
package opendbcopy.model.typeinfo;

/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public final class TypeInfoHelper {
    private final static String ESCAPE_CHARACTER = "\\";

    /**
     * DOCUMENT ME!
     *
     * @param stringIn DOCUMENT ME!
     * @param typeInfoOut DOCUMENT ME!
     * @param identifierQuoteStringOut DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public static final String getFormattedString(String   stringIn,
                                                  TypeInfo typeInfoOut,
                                                  String   identifierQuoteStringOut) throws Exception {
        if ((stringIn != null) && (typeInfoOut != null) && (identifierQuoteStringOut != null)) {
            stringIn = stringIn.replaceAll(identifierQuoteStringOut, ESCAPE_CHARACTER + identifierQuoteStringOut);

            // add prefix and suffix if required
            if (typeInfoOut.getLiteralPrefix() != null && typeInfoOut.getLiteralSuffix() != null)
            	return typeInfoOut.getLiteralPrefix() + stringIn + typeInfoOut.getLiteralSuffix();
            else
            	return stringIn;
        } else {
            return null;
        }
    }
}
