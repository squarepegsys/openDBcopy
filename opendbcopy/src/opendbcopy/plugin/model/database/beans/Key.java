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
package opendbcopy.plugin.model.database.beans;

import opendbcopy.config.XMLTags;

import opendbcopy.plugin.model.exception.MissingAttributeException;

import org.jdom.Element;


/**
 * class description
 *
 * @author  Anthony Smith
 * @version $Revision$
 */
public class Key {
    private Element keyElement;
    private String  parentTableName;
    private String  parentColumnName;
    private String  childColumnName;
    private String  childKeyName;
    private String  parentKeyName;
    private Integer updateRule;
    private Integer deleteRule;
    private Integer keySequence;

    /**
     * Creates a new Key object.
     */
    public Key() {
    }

    /**
     * Creates a new Key object.
     *
     * @param keyElement DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     */
    public Key(Element keyElement) throws MissingAttributeException {
        this.keyElement = keyElement;

        // parent table name
        if (keyElement.getAttributeValue(XMLTags.PKTABLE_NAME) != null) {
            parentTableName = keyElement.getAttributeValue(XMLTags.PKTABLE_NAME);
        } else {
            throw new MissingAttributeException(keyElement, XMLTags.PKTABLE_NAME);
        }

        // parent column name
        if (keyElement.getAttributeValue(XMLTags.PKCOLUMN_NAME) != null) {
            parentColumnName = keyElement.getAttributeValue(XMLTags.PKCOLUMN_NAME);
        } else {
            throw new MissingAttributeException(keyElement, XMLTags.PKCOLUMN_NAME);
        }

        // child column name
        if (keyElement.getAttributeValue(XMLTags.FKCOLUMN_NAME) != null) {
            childColumnName = keyElement.getAttributeValue(XMLTags.FKCOLUMN_NAME);
        } else {
            throw new MissingAttributeException(keyElement, XMLTags.FKCOLUMN_NAME);
        }

        // parent key name
        if (keyElement.getAttributeValue(XMLTags.PK_NAME) != null) {
            if (keyElement.getAttributeValue(XMLTags.PK_NAME).compareToIgnoreCase("null") != 0) {
                parentKeyName = keyElement.getAttributeValue(XMLTags.PK_NAME);
            }
        } else {
            throw new MissingAttributeException(keyElement, XMLTags.PK_NAME);
        }

        // child key name
        if (keyElement.getAttributeValue(XMLTags.FK_NAME) != null) {
            if (keyElement.getAttributeValue(XMLTags.FK_NAME).compareToIgnoreCase("null") != 0) {
                childKeyName = keyElement.getAttributeValue(XMLTags.FK_NAME);
            }
        } else {
            throw new MissingAttributeException(keyElement, XMLTags.FK_NAME);
        }

        // key sequence
        if (keyElement.getAttributeValue(XMLTags.KEY_SEQ) != null) {
            if (keyElement.getAttributeValue(XMLTags.KEY_SEQ).compareToIgnoreCase("null") != 0) {
                keySequence = new Integer(keyElement.getAttributeValue(XMLTags.KEY_SEQ));
            }
        } else {
            throw new MissingAttributeException(keyElement, XMLTags.KEY_SEQ);
        }

        // update rule
        if (keyElement.getAttributeValue(XMLTags.UPDATE_RULE) != null) {
            if (keyElement.getAttributeValue(XMLTags.UPDATE_RULE).compareToIgnoreCase("null") != 0) {
                updateRule = new Integer(keyElement.getAttributeValue(XMLTags.UPDATE_RULE));
            }
        } else {
            throw new MissingAttributeException(keyElement, XMLTags.UPDATE_RULE);
        }

        // delete rule
        if (keyElement.getAttributeValue(XMLTags.DELETE_RULE) != null) {
            if (keyElement.getAttributeValue(XMLTags.DELETE_RULE).compareToIgnoreCase("null") != 0) {
                deleteRule = new Integer(keyElement.getAttributeValue(XMLTags.DELETE_RULE));
            }
        } else {
            throw new MissingAttributeException(keyElement, XMLTags.DELETE_RULE);
        }
    }
	/**
	 * @return Returns the childColumnName.
	 */
	public String getChildColumnName() {
		return childColumnName;
	}
	/**
	 * @param childColumnName The childColumnName to set.
	 */
	public void setChildColumnName(String childColumnName) {
		this.childColumnName = childColumnName;
	}
	/**
	 * @return Returns the childKeyName.
	 */
	public String getChildKeyName() {
		return childKeyName;
	}
	/**
	 * @param childKeyName The childKeyName to set.
	 */
	public void setChildKeyName(String childKeyName) {
		this.childKeyName = childKeyName;
	}
	/**
	 * @return Returns the deleteRule.
	 */
	public Integer getDeleteRule() {
		return deleteRule;
	}
	/**
	 * @param deleteRule The deleteRule to set.
	 */
	public void setDeleteRule(Integer deleteRule) {
		this.deleteRule = deleteRule;
	}
	/**
	 * @return Returns the keySequence.
	 */
	public Integer getKeySequence() {
		return keySequence;
	}
	/**
	 * @param keySequence The keySequence to set.
	 */
	public void setKeySequence(Integer keySequence) {
		this.keySequence = keySequence;
	}
	/**
	 * @return Returns the parentColumnName.
	 */
	public String getParentColumnName() {
		return parentColumnName;
	}
	/**
	 * @param parentColumnName The parentColumnName to set.
	 */
	public void setParentColumnName(String parentColumnName) {
		this.parentColumnName = parentColumnName;
	}
	/**
	 * @return Returns the parentKeyName.
	 */
	public String getParentKeyName() {
		return parentKeyName;
	}
	/**
	 * @param parentKeyName The parentKeyName to set.
	 */
	public void setParentKeyName(String parentKeyName) {
		this.parentKeyName = parentKeyName;
	}
	/**
	 * @return Returns the parentTableName.
	 */
	public String getParentTableName() {
		return parentTableName;
	}
	/**
	 * @param parentTableName The parentTableName to set.
	 */
	public void setParentTableName(String parentTableName) {
		this.parentTableName = parentTableName;
	}
	/**
	 * @return Returns the updateRule.
	 */
	public Integer getUpdateRule() {
		return updateRule;
	}
	/**
	 * @param updateRule The updateRule to set.
	 */
	public void setUpdateRule(Integer updateRule) {
		this.updateRule = updateRule;
	}
}
