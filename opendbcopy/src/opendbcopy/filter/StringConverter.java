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
package opendbcopy.filter;

/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public final class StringConverter {
    /**
     * DOCUMENT ME!
     *
     * @param o DOCUMENT ME!
     * @param returnNullWhenEmpty DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static Object trimString(Object  o,
                                    boolean returnNullWhenEmpty) {
        if (returnNullWhenEmpty) {
            if (((String) o).trim().length() == 0) {
                return null;
            } else {
                return ((String) o).trim();
            }
        } else {
            return ((String) o).trim();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param o DOCUMENT ME!
     * @param returnNullWhenEmpty DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static Object trimAndRemoveMultipleIntermediateWhitespaces(Object  o,
                                                                      boolean returnNullWhenEmpty) {
        Object in = trimString(o, returnNullWhenEmpty);

        if ((in != null) && (((String) in).length() > 0)) {
            StringBuffer strb = new StringBuffer(((String) in));
            StringBuffer strbn = new StringBuffer();

            int          index = 0;

            while (index < strb.length()) {
                if ((strb.charAt(index) == ' ') && (index != 0)) {
                    strbn.append(" ");

                    while (strb.charAt(index) == ' ') {
                        index++;
                    }
                }

                strbn.append(strb.charAt(index));
                index++;
            }

            return strbn.toString();
        } else {
            if (returnNullWhenEmpty) {
                return null;
            } else {
                return in;
            }
        }
    }
}
