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
package opendbcopy.config;

import info.clearthought.layout.TableLayout;

import javax.swing.ImageIcon;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public final class GUI {
    /** Images */
    private static final ImageIcon openIcon = new ImageIcon("resource/images/Open16.gif");
    private static final ImageIcon saveAsIcon = new ImageIcon("resource/images/SaveAs16.gif");
    private static final ImageIcon newIcon = new ImageIcon("resource/images/New16.gif");
    private static final ImageIcon historyIcon = new ImageIcon("resource/images/History16.gif");
    private static final ImageIcon imageIconLeft = new ImageIcon("resource/images/left.gif");
    private static final ImageIcon imageIconRight = new ImageIcon("resource/images/right.gif");
    private static final ImageIcon imageIconUp = new ImageIcon("resource/images/up.gif");
    private static final ImageIcon imageIconDown = new ImageIcon("resource/images/down.gif");
    private static final ImageIcon imageIconDelete = new ImageIcon("resource/images/delete.gif");
    private static final ImageIcon imageIconFile = new ImageIcon("resource/images/Save24.gif");
    private static final ImageIcon imageIconDir = new ImageIcon("resource/images/Open24.gif");
    private static final ImageIcon imageIconConfig = new ImageIcon("resource/images/Preferences24.gif");

    /** GUI constants for layout */

    // border
    public final static double B = 5;

    // fill
    public final static double F = TableLayout.FILL;

    // preferred
    public final static double P = TableLayout.PREFERRED;

    // vertical space
    public final static double VS = 5;

    // vertical gap
    public final static double VG = 10;

    // horizontal gap
    public final static double HG = 10;

    /**
     * DOCUMENT ME!
     *
     * @return Returns the historyIcon.
     */
    public static final ImageIcon getHistoryIcon() {
        return historyIcon;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the newIcon.
     */
    public static final ImageIcon getNewIcon() {
        return newIcon;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the openIcon.
     */
    public static final ImageIcon getOpenIcon() {
        return openIcon;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the saveAsIcon.
     */
    public static final ImageIcon getSaveAsIcon() {
        return saveAsIcon;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the imageIconDelete.
     */
    public static ImageIcon getImageIconDelete() {
        return imageIconDelete;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the imageIconDown.
     */
    public static ImageIcon getImageIconDown() {
        return imageIconDown;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the imageIconLeft.
     */
    public static ImageIcon getImageIconLeft() {
        return imageIconLeft;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the imageIconRight.
     */
    public static ImageIcon getImageIconRight() {
        return imageIconRight;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the imageIconUp.
     */
    public static ImageIcon getImageIconUp() {
        return imageIconUp;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the iconFile.
     */
    public static ImageIcon getImageIconFile() {
        return imageIconFile;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the iconConfig.
     */
    public static ImageIcon getImageIconConfig() {
        return imageIconConfig;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the iconDir.
     */
    public static ImageIcon getImageIconDir() {
        return imageIconDir;
    }
}
