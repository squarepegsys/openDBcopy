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

import java.util.*;


/**
 * CommonResourceBundle contains collection of ResourceBundles and provides transparent access to their localized objects.
 *
 * @author Serguei Eremenko sergeremenko[at]yahoo.com
 * @version 1.0
 */
public abstract class CommonResourceBundle extends ResourceBundle {
    /** Default implementation of this abstract class */
    private static DefResourceBundle instance;

    /** Collection of resource bundles */
    private static ArrayList bundles = new ArrayList();

    /** Resource bundle base names */
    protected String[] baseName;

    /**
     * Sets the resource bundle base names as an array
     *
     * @param baseName DOCUMENT ME!
     */
    protected CommonResourceBundle(String[] baseName) {
        this.baseName = baseName;
    }

    /**
     * Sets the resource bundle base names as an array from a string like: test1,test2 etc or test1 test2 etc
     *
     * @param baseName DOCUMENT ME!
     */
    protected CommonResourceBundle(String baseName) {
        buildBaseName(baseName, " ,");
    }

    /**
     * Creates a new CommonResourceBundle object.
     */
    public CommonResourceBundle() {
        this(new String[0]);
    }

    /**
     * DOCUMENT ME!
     *
     * @return a resource bundle
     */
    public static ResourceBundle getBundle() {
        if (instance == null) {
            instance = new DefResourceBundle();
        }

        return instance;
    }

    /**
     * DOCUMENT ME!
     *
     * @return an array of all resource bundle base names
     */
    public String[] getBaseName() {
        return baseName;
    }

    /**
     * Adds a resource bundle to the collection of bundles
     *
     * @param bundle the ResourceBundle to add
     */
    public static void addResourceBundle(ResourceBundle bundle) {
        bundles.add(bundle);
    }

    /**
     * Removes a resource bundle from the collection of bundles
     *
     * @param bundle the ResourceBundle to remove
     */
    public static void removeResourceBundle(ResourceBundle bundle) {
        bundles.remove(bundle);
    }

    /**
     * DOCUMENT ME!
     *
     * @return Enumeration of the keys
     */
    public abstract Enumeration getKeys();

    /**
     * Gets an object for the given key from this resource bundle and null if this resource bundle does not contain an object for the given key
     *
     * @return DOCUMENT ME!
     */
    protected abstract Object handleGetObject(String key);

    /**
     * Builds the resource bundle base names as an array from a string like: test1,test2 etc or test1 test2 etc
     *
     * @param base DOCUMENT ME!
     * @param delim DOCUMENT ME!
     *
     * @throws RuntimeException DOCUMENT ME!
     */
    protected void buildBaseName(String base,
                                 String delim) {
        String s = null;

        try {
            s = System.getProperty(base);

            if (s == null) {
                return;
            }

            StringTokenizer st = new StringTokenizer(s, delim);

            baseName = new String[st.countTokens()];

            int i = 0;

            while (st.hasMoreTokens()) {
                baseName[i++] = st.nextToken().trim();
            }
        } catch (Exception e) {
            throw new RuntimeException("Can not resolve base name: " + s);
        }
    }

    /**
     * Default implementation
     */
    static class DefResourceBundle extends CommonResourceBundle {
        /**
         * Creates a new DefResourceBundle object.
         *
         * @param baseName DOCUMENT ME!
         */
        public DefResourceBundle(String[] baseName) {
            super(baseName);
        }

        /**
         * Creates a new DefResourceBundle object.
         *
         * @param baseName DOCUMENT ME!
         */
        public DefResourceBundle(String baseName) {
            super(baseName);
        }

        /**
         * Creates a new DefResourceBundle object.
         */
        public DefResourceBundle() {
            this(new String[0]);
        }

        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         *
         * @throws NoSuchElementException DOCUMENT ME!
         */
        public Enumeration getKeys() {
            return new Enumeration() {
                    Enumeration enum = null;
                    int         i = 0;

                    public boolean hasMoreElements() {
                        boolean b = false;

                        while ((enum == null) || !(b = enum.hasMoreElements())) {
                            if (i >= bundles.size()) {
                                enum = null;

                                return b;
                            }

                            enum = ((ResourceBundle) bundles.get(i++)).getKeys();
                        }

                        return b;
                    }

                    public Object nextElement() {
                        if (enum == null) {
                            throw new NoSuchElementException();
                        }

                        return enum.nextElement();
                    }
                };
        }

        /**
         * DOCUMENT ME!
         *
         * @param key DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        protected Object handleGetObject(String key) {
            ResourceBundle rb = null;

            String         val = null;

            for (int i = 0; i < bundles.size(); i++) {
                rb = (ResourceBundle) bundles.get(i);

                try {
                    val = rb.getString(key);
                } catch (Exception e) {
                }

                if (val != null) {
                    break;
                }
            }

            return val;
        }
    }
}
