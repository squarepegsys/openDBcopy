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

import opendbcopy.plugin.model.exception.MissingAttributeException;
import opendbcopy.plugin.model.exception.MissingElementException;

import org.jdom.Element;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class Driver {
    private Element driver;

    /**
     * Creates a new Driver object.
     *
     * @param driver DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     */
    public Driver(Element driver) throws MissingAttributeException, MissingElementException {
        this.driver = driver;

        checkDriver();
    }

    /**
     * DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     */
    public final void checkDriver() throws MissingAttributeException, MissingElementException {
        if (driver.getAttributeValue(XMLTags.NAME) == null) {
            throw new MissingAttributeException(driver, XMLTags.NAME);
        }

        if (driver.getChild(XMLTags.CLASS) == null) {
            throw new MissingElementException(driver, XMLTags.CLASS);
        }

        if (driver.getChild(XMLTags.URL) == null) {
            throw new MissingElementException(driver, XMLTags.URL);
        }

        if (driver.getChild(XMLTags.CLASS).getAttributeValue(XMLTags.NAME) == null) {
            throw new MissingAttributeException(driver.getChild(XMLTags.CLASS), XMLTags.NAME);
        }

        if (driver.getChild(XMLTags.URL).getAttributeValue(XMLTags.VALUE) == null) {
            throw new MissingAttributeException(driver.getChild(XMLTags.URL), XMLTags.VALUE);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the className.
     */
    public String getClassName() {
        return driver.getChild(XMLTags.CLASS).getAttributeValue(XMLTags.NAME);
    }

    /**
     * DOCUMENT ME!
     *
     * @param className The className to set.
     */
    public void setClassName(String className) {
        driver.getChild(XMLTags.CLASS).setAttribute(XMLTags.NAME, className);
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the name.
     */
    public String getName() {
        return driver.getAttributeValue(XMLTags.NAME);
    }

    /**
     * DOCUMENT ME!
     *
     * @param name The name to set.
     */
    public void setName(String name) {
        driver.setAttribute(XMLTags.NAME, name);
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the url.
     */
    public String getUrl() {
        return driver.getChild(XMLTags.URL).getAttributeValue(XMLTags.VALUE);
    }

    /**
     * DOCUMENT ME!
     *
     * @param url The url to set.
     */
    public void setUrl(String url) {
        driver.getChild(XMLTags.URL).setAttribute(XMLTags.VALUE, url);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Element getDefault(String database) {
        if (isDefault(database)) {
            return driver.getChild(XMLTags.DEFAULT).getChild(database);
        } else {
            return null;
        }
    }

    public String getDefaultURL(String database) {
    	Element defaultElement = getDefault(database);
    	
    	if (defaultElement != null) {
    		return defaultElement.getChild(XMLTags.URL).getAttributeValue(XMLTags.VALUE);
    	} else {
    		return null;
    	}
    }
    
    public String getDefaultUsername(String database) {
    	Element defaultElement = getDefault(database);
    	
    	if (defaultElement != null) {
    		return defaultElement.getChild(XMLTags.USERNAME).getAttributeValue(XMLTags.VALUE);
    	} else {
    		return null;
    	}
    }

    /**
     * DOCUMENT ME!
     *
     * @param database DOCUMENT ME!
     */
    public void setDefault(String database, String url, String username) {
    	if (database == null) {
    		throw new IllegalArgumentException("Missing database");
    	}
    	
    	Element defaultElement = null;
    	
    	if (driver.getChild(XMLTags.DEFAULT) == null) {
    		defaultElement = new Element(XMLTags.DEFAULT);
    	} else {
    		defaultElement = driver.getChild(XMLTags.DEFAULT);
    		resetDefault(database);
    	}

    	Element dbElement = new Element(database);
    	Element urlElement = new Element(XMLTags.URL);
    	Element usernameElement = new Element(XMLTags.USERNAME);
    	
    	urlElement.setAttribute(XMLTags.VALUE, url);
    	usernameElement.setAttribute(XMLTags.VALUE, username);
    	
    	dbElement.addContent(urlElement);
    	dbElement.addContent(usernameElement);
    	
    	defaultElement.addContent(dbElement);
    	
    	if (driver.getChild(XMLTags.DEFAULT) == null) {
        	driver.addContent(defaultElement);
    	}
    }
    
    public void resetDefault(String database) {
    	if (database == null) {
    		throw new IllegalArgumentException("Missing database");
    	}

    	driver.getChild(XMLTags.DEFAULT).removeChild(database);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private boolean isDefault(String database) {
    	if (driver.getChild(XMLTags.DEFAULT) != null) {
            if (driver.getChild(XMLTags.DEFAULT).getChild(database) != null) {
                return true;
            } else {
            	return false;
            }    		
        } else {
            return false;
        }
    }
}
