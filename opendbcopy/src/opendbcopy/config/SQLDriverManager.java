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

import opendbcopy.io.ExportToXML;
import opendbcopy.io.ImportFromXML;

import opendbcopy.plugin.model.exception.MissingAttributeException;
import opendbcopy.plugin.model.exception.MissingElementException;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.io.File;
import java.io.IOException;

import java.util.Iterator;
import java.util.TreeMap;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class SQLDriverManager {
    private Document driversDoc;
    private TreeMap  drivers;
    private File     personalSQLDriversFile;

    /**
     * Creates a new SQLDrivers object.
     *
     * @param personalSQLDriversFile DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     * @throws JDOMException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     */
    public SQLDriverManager(File personalSQLDriversFile) throws MissingAttributeException, MissingElementException, JDOMException, IOException {
        this.personalSQLDriversFile     = personalSQLDriversFile;

        this.drivers = new TreeMap();
        loadAvailableDrivers();
    }

    /**
     * DOCUMENT ME!
     *
     * @param nameOrClassName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final Driver getDriver(String nameOrClassName) {
        if (nameOrClassName == null) {
            throw new IllegalArgumentException("Missing nameOrClassName");
        }

        if (drivers.containsKey(nameOrClassName)) {
            return (Driver) drivers.get(nameOrClassName);
        } else {
            return findDriverByClassName(nameOrClassName);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param name DOCUMENT ME!
     * @param className DOCUMENT ME!
     * @param url DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     * @throws RuntimeException DOCUMENT ME!
     */
    public final Driver saveDriver(String name,
                                   String className,
                                   String url) throws MissingAttributeException, MissingElementException {
        // Driver exists which do not require className to work. Therefore only check existence of url
        if (url == null) {
            throw new IllegalArgumentException("Missing url");
        }

        Driver driver = null;

        // check if driver already exists
        driver = getDriver(name);

        if (driver == null) {
            driver = getDriver(className);
        }

        // ok, it is a new driver
        if (driver == null) {
            if (name == null) {
                name = className;
            }

            if (name == null) {
                throw new RuntimeException("Cannot store a driver without a name nor className");
            }

            Element driverElement = new Element(XMLTags.DRIVER);
            driverElement.setAttribute(XMLTags.NAME, name);

            Element classElement = new Element(XMLTags.CLASS);
            Element urlElement = new Element(XMLTags.URL);

            classElement.setAttribute(XMLTags.NAME, className);
            urlElement.setAttribute(XMLTags.URL, url);

            driverElement.addContent(classElement);
            driverElement.addContent(urlElement);

            driversDoc.getRootElement().addContent(driverElement);

            driver = new Driver(driverElement);

            drivers.put(driver.getName(), driver);
        } else {
            driver.setName(name);
            driver.setClassName(className);
        }

        return driver;
    }

    /**
     * DOCUMENT ME!
     *
     * @param driver DOCUMENT ME!
     * @param url DOCUMENT ME!
     * @param username DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final void setSourceDriverDefault(Driver driver,
                                             String url,
                                             String username) {
        if (driver == null) {
            throw new IllegalArgumentException("Missing driver");
        }

        resetDefault(XMLTags.SOURCE_DB);
        driver.setDefault(XMLTags.SOURCE_DB, url, username);
    }

    /**
     * DOCUMENT ME!
     *
     * @param driver DOCUMENT ME!
     * @param url DOCUMENT ME!
     * @param username DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final void setDestinationDriverDefault(Driver driver,
                                                  String url,
                                                  String username) {
        if (driver == null) {
            throw new IllegalArgumentException("Missing driver");
        }

        resetDefault(XMLTags.DESTINATION_DB);
        driver.setDefault(XMLTags.DESTINATION_DB, url, username);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final TreeMap getDrivers() {
        return drivers;
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public final void saveDriverFileIntoUserHome() throws IOException {
        ExportToXML.createXML(driversDoc, personalSQLDriversFile.getAbsolutePath());
    }

    /**
     * DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     * @throws JDOMException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     */
    private void loadAvailableDrivers() throws MissingAttributeException, MissingElementException, JDOMException, IOException {
        driversDoc = ImportFromXML.importFile(personalSQLDriversFile);

        Element root = driversDoc.getRootElement();

        if ((root == null) && (root.getChildren().size() == 0)) {
            throw new MissingElementException(root, XMLTags.DRIVER);
        }

        Iterator itDrivers = root.getChildren(XMLTags.DRIVER).iterator();

        while (itDrivers.hasNext()) {
            Element driverElement = (Element) itDrivers.next();

            Driver  driver = new Driver(driverElement);
            drivers.put(driver.getName(), driver);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param database DOCUMENT ME!
     */
    private void resetDefault(String database) {
        Iterator itDrivers = drivers.values().iterator();

        while (itDrivers.hasNext()) {
            Driver tempDriver = (Driver) itDrivers.next();

            if ((tempDriver.getDefault(database) != null) && (tempDriver.getDefault(database).getName().compareTo(database) == 0)) {
                tempDriver.resetDefault(database);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param className DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    /**
     * DOCUMENT ME!
     *
     * @param className DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private Driver findDriverByClassName(String className) {
        Iterator itDrivers = drivers.values().iterator();

        while (itDrivers.hasNext()) {
            Driver driver = (Driver) itDrivers.next();

            if (driver.getClassName().compareTo(className) == 0) {
                return driver;
            }
        }

        return null;
    }
}
