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
package opendbcopy.controller;

import opendbcopy.config.XMLTags;

import opendbcopy.io.ImportFromXML;

import org.jdom.Document;
import org.jdom.Element;

import java.util.StringTokenizer;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public final class Arguments {
    /**
     * Given Empty "String" arguments returns null if no parameters provided
     *
     * @param args DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public static final Document process(String[] args) throws Exception {
        Element arguments = new Element("arguments");
        Element argument = null;

        if (args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                argument = processArgument(args[i]);

                if (argument != null) {
                    arguments.addContent(argument);
                } else {
                    throw new Exception("only pairs of variable=value are allowed!");
                }
            }

            int nbrArguments = arguments.getChildren().size();

            switch (nbrArguments) {
            // only project file name
            case 1:

                if ((arguments.getChild(XMLTags.FILE) != null) && (arguments.getChild(XMLTags.FILE).getAttributeValue(XMLTags.VALUE) != null)) {
                    return ImportFromXML.importFile(arguments.getChild(XMLTags.FILE).getAttributeValue(XMLTags.VALUE));
                } else {
                    printInfoAndExit();
                }

                break;

            // project file name and operation
            case 2:

                if ((arguments.getChild(XMLTags.FILE) != null) && (arguments.getChild(XMLTags.OPERATION) != null) && (arguments.getChild(XMLTags.RUNLEVEL) == null)) {
                    Document project = ImportFromXML.importFile(arguments.getChild(XMLTags.FILE).getAttributeValue(XMLTags.VALUE));
                    Element  operation = new Element(XMLTags.OPERATION);
                    operation.setAttribute(XMLTags.NAME, arguments.getChild(XMLTags.OPERATION).getAttributeValue(XMLTags.VALUE));
                    project.getRootElement().addContent(operation);
                    project.getRootElement().setAttribute(XMLTags.RUNLEVEL, Integer.toString(5));

                    return project;
                } else {
                    printInfoAndExit();
                }

                break;

            // project file name, operation and runlevel
            case 3:

                if ((arguments.getChild(XMLTags.FILE) != null) && (arguments.getChild(XMLTags.OPERATION) != null) && (arguments.getChild(XMLTags.RUNLEVEL) != null)) {
                    Document project = ImportFromXML.importFile(arguments.getChild(XMLTags.FILE).getAttributeValue(XMLTags.VALUE));
                    Element  operation = new Element(XMLTags.OPERATION);
                    operation.setAttribute(XMLTags.NAME, arguments.getChild(XMLTags.OPERATION).getAttributeValue(XMLTags.VALUE));
                    project.getRootElement().addContent(operation);
                    project.getRootElement().setAttribute(XMLTags.RUNLEVEL, arguments.getChild(XMLTags.RUNLEVEL).getAttributeValue(XMLTags.VALUE));

                    return project;
                } else {
                    printInfoAndExit();
                }

                break;

            // import project and add arguments to project as arguments element						
            default:

                if ((arguments.getChild(XMLTags.FILE) != null) && (arguments.getChild(XMLTags.FILE).getAttributeValue(XMLTags.VALUE) != null)) {
                    Document project = ImportFromXML.importFile(arguments.getChild(XMLTags.FILE).getAttributeValue(XMLTags.VALUE));
                    project.getRootElement().addContent(arguments);

                    return project;
                } else {
                    printInfoAndExit();
                }

                break;
            }
        }

        // in case of no arguments provided
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param argument DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    private static Element processArgument(String argument) throws Exception {
        Element         element = null;
        StringTokenizer st = new StringTokenizer(argument, "=");

        String          firstElement = "";
        String          lastElement = "";

        int             counter = 0;

        while (st.hasMoreElements()) {
            if (counter == 0) {
                element = new Element((String) st.nextElement());
            }

            if (counter == 1) {
                element.setAttribute(XMLTags.VALUE, (String) st.nextElement());
            }

            if (counter > 1) {
                throw new Exception("only pairs of variable=value are allowed!");
            }

            counter++;
        }

        return element;
    }

    /**
     * DOCUMENT ME!
     */
    private static void printInfoAndExit() {
        System.err.println("MISSING ARGUMENTS! Program will now terminate!");
        System.err.println("Minimum argument is:     file=<project_path_filename>"); // ");
        System.err.println("Non mandatory arguments: operation=<operation> runlevel=<runlevel>");
        System.out.println("**********************************************************************************************");
        System.out.println("LEGEND");
        System.out.println("file:      your opendbcopy project file to open (see different examples below)");
        System.out.println("operation: 'execute' to immediately execute the project's plugin");
        System.out.println("runlevel:  '1' for SHELL only, '5' for GUI (Graphical User Interface)");
        System.out.println("**********************************************************************************************");
        System.out.println("Example call 1: (no arguments -> create a new project)");
        System.out.println("> java opendbcopy.controller.MainController");
        System.out.println("----------------------------------------------------------------------------------------------");
        System.out.println("Example call 2: (import project from xml file -> continue your work)");
        System.out.println("> java opendbcopy.controller.MainController file=c:/opendbcopy/myproject.xml");
        System.out.println("----------------------------------------------------------------------------------------------");
        System.out.println("Example call 3: (import project xml from file and execute contained plugin in GUI mode)");
        System.out.println("> java opendbcopy.controller.MainController file=/tmp/projects/myproject.xml operation=execute");
        System.out.println("----------------------------------------------------------------------------------------------");
        System.out.println("Example call 4: (import project xml from file and execute contained plugin in SHELL mode)");
        System.out.println("> java opendbcopy.controller.MainController file=myproject.xml operation=execute runlevel=1");
        System.out.println("----------------------------------------------------------------------------------------------");
        System.out.println("For further help please visit the project website: http://opendbcopy.sourceforge.net");
        System.exit(0);
    }
}
