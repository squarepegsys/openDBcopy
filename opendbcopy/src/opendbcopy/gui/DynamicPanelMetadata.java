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
package opendbcopy.gui;

/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class DynamicPanelMetadata {
    private String  title;
    private String  className;
    private boolean registerObserver;

    /**
     * Creates a new DynamicPanelMetadata object.
     *
     * @param title DOCUMENT ME!
     * @param className DOCUMENT ME!
     * @param registerObserver DOCUMENT ME!
     */
    DynamicPanelMetadata(String  title,
                         String  className,
                         boolean registerObserver) {
        this.title                = title;
        this.className            = className;
        this.registerObserver     = registerObserver;
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
     * @return Returns the registerObserver.
     */
    public boolean isRegisterObserver() {
        return registerObserver;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the title.
     */
    public String getTitle() {
        return title;
    }
}
