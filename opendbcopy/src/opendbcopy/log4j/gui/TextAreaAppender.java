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
package opendbcopy.log4j.gui;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.spi.LoggingEvent;

import javax.swing.JTextArea;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class TextAreaAppender extends AppenderSkeleton {
    private static TextAreaAppender instance = null;
    private JTextArea               textArea;
    private Layout                  layout;
    private boolean                 enabled;

    /**
     * Creates a new TextAreaAppender object. Be aware that this appender can be enabled and disabled. Use enable(boolean) therefore Default value is
     * disabled, so not events are logged
     */
    public TextAreaAppender() {
        super();

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setAutoscrolls(true);

        layout       = new SimpleLayout();
        instance     = this;

        setEnabled(false); // default
    }

    /**
     * Creates a new TextAreaAppender object.
     *
     * @param layout DOCUMENT ME!
     * @param name DOCUMENT ME!
     */
    public TextAreaAppender(Layout layout,
                            String name) {
        super();

        this.layout     = layout;
        this.name       = name;

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setAutoscrolls(true);
        instance = this;
    }

    /**
     * DOCUMENT ME!
     *
     * @param enable DOCUMENT ME!
     */
    public void setEnabled(boolean enable) {
        enabled = enable;
        textArea.setEditable(enable);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public synchronized static TextAreaAppender getInstance() {
        if (instance == null) {
            instance = new TextAreaAppender();
        }

        return instance;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public JTextArea getTextArea() {
        return textArea;
    }

    /**
     * DOCUMENT ME!
     *
     * @param layout DOCUMENT ME!
     */
    public void setLayout(Layout layout) {
        this.layout = layout;
    }

    /**
     * DOCUMENT ME!
     *
     * @param textArea DOCUMENT ME!
     */
    public void setTextArea(JTextArea textArea) {
        this.textArea = textArea;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean requiresLayout() {
        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @param event DOCUMENT ME!
     */
    protected void append(LoggingEvent event) {
        if (enabled) {
            StringBuffer text = new StringBuffer(this.layout.format(event));

            // Print Stacktrace
            if (event.getLevel().isGreaterOrEqual(Level.WARN)) {
                if (layout.ignoresThrowable()) {
                    String[] s = event.getThrowableStrRep();

                    if (s != null) {
                        int len = s.length;

                        for (int i = 0; i < len; i++) {
                            text.append(s[i]);
                            text.append(Layout.LINE_SEP);
                        }
                    }
                }
            }

            textArea.append(text.toString());
            textArea.setCaretPosition(textArea.getDocument().getLength());
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void close() {
        /**
         * @todo Implement this org.apache.log4j.Appender abstract method
         */
    }
}
