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
package opendbcopy.resource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.StringTokenizer;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class ResourceManager {
    private String defaultResourceName;

    /**
     * Creates a new ResourceManager object.
     *
     * @param resourceDir DOCUMENT ME!
     * @param defaultResourceName DOCUMENT ME!
     * @param defaultLanguage DOCUMENT ME!
     *
     * @throws FileNotFoundException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     */
    public ResourceManager(String resourcePath,
                           String defaultResourceName,
                           String defaultLanguage) throws FileNotFoundException, IOException {
        this.defaultResourceName = defaultResourceName;

        // override the default locale for this JVM
        if (defaultLanguage != null) {
            Locale.setDefault(new Locale(defaultLanguage));
        }

        addResourceBundle(resourcePath + defaultResourceName);
    }

    /**
     * DOCUMENT ME!
     *
     * @param resourceNameWithoutEnding DOCUMENT ME!
     */
    public final void addResourceBundle(String resourceNameWithoutEnding) {
        CommonResourceBundle.addResourceBundle(ResourceBundle.getBundle(resourceNameWithoutEnding));
    }

    /**
     * DOCUMENT ME!
     *
     * @param key DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final String getString(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Missing key");
        }

        String value = null;

        try {
            value = CommonResourceBundle.getBundle().getString(key);
        } catch (Exception e) {
            // returning the key as string is preferred than raising an exception -> just log for info
            System.out.println("Missing resource for key=" + key + " / Locale=" + Locale.getDefault());
        }

        if (value != null) {
            return value;
        } else {
            return key;
        }
    }

    /**
     * Given a key and String array replace the placeholders in the localised message first Maximum number of parameters that can be replaced is 10
     * (0 to 9 as placeholder)
     *
     * @param key DOCUMENT ME!
     * @param parameter DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final String getString(String   key,
                                  String[] parameter) {
        if (parameter == null) {
            throw new IllegalArgumentException("Missing parameter(s)");
        }

        if (parameter.length > 10) {
            throw new IllegalArgumentException("Too many parameters. Maximum is 10");
        }

        String value = null;

        try {
            value = CommonResourceBundle.getBundle().getString(key);
        } catch (Exception e) {
            // returning the key as string is preferred than raising an exception -> just log for info
            System.out.println("Missing resource for key=" + key + " / Locale=" + Locale.getDefault());
        }

        if (value != null) {
            return replaceParameter(value, parameter);
        } else {
            return key;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the defaultLocale.
     */
    public final Locale getDefaultLocale() {
        return Locale.getDefault();
    }

    /**
     * DOCUMENT ME!
     *
     * @param defaultLocale The defaultLocale to set.
     */
    public final void setDefaultLocale(Locale defaultLocale) {
        Locale.setDefault(defaultLocale);
    }

    /**
     * DOCUMENT ME!
     *
     * @param filename DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private String getFilenameWithoutEnding(String filename) {
        return filename.substring(0, filename.length() - ".properties".length());
    }

    /**
     * DOCUMENT ME!
     *
     * @param message DOCUMENT ME!
     * @param parameter DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private String replaceParameter(String   message,
                                    String[] parameter) {
        String          replacedString = null;

        StringTokenizer st = new StringTokenizer(message, "{");

        int             paramCounter = 0;

        // retrieve first element
        if (st.hasMoreElements()) {
            String nextElement = (String) st.nextElement();

            int    index = nextElement.indexOf("}");

            if (index >= 1) {
                // add parameter
                replacedString = parameter[Integer.parseInt(nextElement.substring(index - 1, index))];

                // add remaining string
                replacedString += nextElement.substring(index + 1, nextElement.length());
            }
        }

        while (st.hasMoreElements()) {
            String nextElement = (String) st.nextElement();

            // add parameter
            if (replacedString == null) {
                replacedString = parameter[Integer.parseInt(nextElement.substring(0, 1))];
            } else {
                replacedString += parameter[Integer.parseInt(nextElement.substring(0, 1))];
            }

            // add remaining string
            replacedString += nextElement.substring(2, nextElement.length());
        }

        return replacedString;
    }
}
