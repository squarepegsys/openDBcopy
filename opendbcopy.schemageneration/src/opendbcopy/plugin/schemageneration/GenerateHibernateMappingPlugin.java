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
package opendbcopy.plugin.schemageneration;

import opendbcopy.config.XMLTags;

import opendbcopy.controller.MainController;

import opendbcopy.io.PropertiesToFile;
import opendbcopy.io.Writer;

import opendbcopy.plugin.model.DynamicPluginThread;
import opendbcopy.plugin.model.Model;
import opendbcopy.plugin.model.database.DatabaseModel;
import opendbcopy.plugin.model.database.typeinfo.TypeMapping;
import opendbcopy.plugin.model.exception.MissingAttributeException;
import opendbcopy.plugin.model.exception.MissingElementException;
import opendbcopy.plugin.model.exception.PluginException;
import opendbcopy.plugin.model.exception.UnsupportedAttributeValueException;

import org.jdom.Element;

import java.io.IOException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class GenerateHibernateMappingPlugin extends DynamicPluginThread {
    private DatabaseModel model;
    private TypeMapping   typeMapping;
    private StringBuffer  sb;
    private Element       conf;
    private String        path = "";
    private String        packageName = "";
    private String        hibernateDialect;
    private String        newLine = "";
    private String        database = "";
    private String        identifierQuoteStringOut = "";
    private boolean       show_qualified_table_name = false;
    private int           counterRecords = 0;
    private int           counterTables = 0;
    private List          processTables;

    /**
     * Creates a new CopyMapping object.
     *
     * @param controller DOCUMENT ME!
     * @param baseModel DOCUMENT ME!
     *
     * @throws PluginException DOCUMENT ME!
     */
    public GenerateHibernateMappingPlugin(MainController controller,
                                          Model          baseModel) throws PluginException {
        super(controller, baseModel);
        this.model = (DatabaseModel) baseModel;
    }

    /**
     * DOCUMENT ME!
     *
     * @throws PluginException DOCUMENT ME!
     */
    protected void setUp() throws PluginException {
        try {
            // setup SQL Type -> Java Type Mapping up as HashMap for fast lookup
            typeMapping     = new TypeMapping(getTypeMapping());

            newLine     = System.getProperty("line.separator");

            // read the plugin configuration
            conf                 = model.getConf();
            path                 = conf.getChild(XMLTags.PATH).getAttributeValue(XMLTags.VALUE);
            packageName          = conf.getChild("package_name").getAttributeValue(XMLTags.VALUE);
            hibernateDialect     = conf.getChild("hibernate_dialect").getAttributeValue(XMLTags.VALUE);

            if (model.getDbMode() == model.DUAL_MODE) {
                database = model.getDestinationDb().getName();
            } else {
                database = model.getSourceDb().getName();
            }

            // extract the tables to dump
            if (model.getDbMode() == model.DUAL_MODE) {
                processTables = model.getDestinationTablesToProcessOrdered();
            } else {
                processTables = model.getSourceTablesToProcessOrdered();
            }

            int nbrTables = processTables.size();

            // now set the number of tables that need to be copied
            model.setLengthProgressTable(nbrTables);
        } catch (Exception e) {
            throw new PluginException(e.getMessage());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws PluginException DOCUMENT ME!
     */
    public final void execute() throws PluginException {
        try {
            String   destinationTableName = "";
            String   tableName = "";
            String   fileName = "";

            Iterator itProcessTables = processTables.iterator();

            while (itProcessTables.hasNext() && !isInterrupted()) {
                Element tableProcess = (Element) itProcessTables.next();
                List    processColumns = null;
                counterRecords = 0;

                if (model.getDbMode() == model.DUAL_MODE) {
                    destinationTableName     = tableProcess.getAttributeValue(XMLTags.DESTINATION_DB);
                    processColumns           = model.getMappingColumnsToProcessByDestinationTable(destinationTableName);
                    fileName                 = path + destinationTableName + ".hbm.xml";
                }

                sb = new StringBuffer();

                genHibernateMappingFile(destinationTableName, processColumns);

                Writer.write(sb, fileName);

                postMessage(fileName + " written");

                model.setCurrentProgressTable(++counterTables);
            }

            // create hibernate.properties file
            Properties p = new Properties();
            Element    destinationDriver = model.getDestinationConnection();

            p.setProperty("hibernate.connection.driver_class", destinationDriver.getAttributeValue(XMLTags.DRIVER_CLASS));
            p.setProperty("hibernate.connection.url", destinationDriver.getAttributeValue(XMLTags.URL));

            if ((destinationDriver.getAttributeValue(XMLTags.USERNAME) == null) || (destinationDriver.getAttributeValue(XMLTags.USERNAME).length() == 0)) {
                p.setProperty("hibernate.connection.username", "");
            } else {
                p.setProperty("hibernate.connection.username", destinationDriver.getAttributeValue(XMLTags.USERNAME));
            }

            if ((destinationDriver.getAttributeValue(XMLTags.PASSWORD) == null) || (destinationDriver.getAttributeValue(XMLTags.PASSWORD).length() == 0)) {
                p.setProperty("hibernate.connection.password", "");
            } else {
                p.setProperty("hibernate.connection.password", destinationDriver.getAttributeValue(XMLTags.PASSWORD));
            }

            p.setProperty("hibernate.dialect", hibernateDialect);

            PropertiesToFile.exportPropertiesToFile(p, path + "hibernate.properties");
            model.setProgressMessage("hibernate properties written to " + path + "hibernate.properties");

            if (!isInterrupted()) {
                postMessage(counterTables + " table(s) processed");
            }
        } catch (MissingAttributeException e) {
            throw new PluginException(e);
        } catch (UnsupportedAttributeValueException e) {
            throw new PluginException(e);
        } catch (MissingElementException e) {
            throw new PluginException(e);
        } catch (IOException e) {
            throw new PluginException(e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param destinationTableName DOCUMENT ME!
     * @param processColumns DOCUMENT ME!
     *
     * @throws MissingElementException DOCUMENT ME!
     */
    private void genHibernateMappingFile(String destinationTableName,
                                         List   processColumns) throws MissingElementException {
        initXMLHeader();

        sb.append("<hibernate-mapping>" + newLine);
        sb.append("<class name=\"" + packageName + "." + destinationTableName + "\" table=\"" + destinationTableName + "\">" + newLine);

        Iterator itProcessColumns = processColumns.iterator();

        HashMap  primaryKeys = new HashMap();
        HashMap  importedKeys = new HashMap();
        HashMap  exportedKeys = new HashMap();
        HashMap  indexes = new HashMap();

        // check for primary keys
        if ((model.getDestinationPrimaryKeys(destinationTableName) != null) && (model.getDestinationPrimaryKeys(destinationTableName).size() > 0)) {
            Iterator itPrimaryKeys = model.getDestinationPrimaryKeys(destinationTableName).iterator();

            while (itPrimaryKeys.hasNext()) {
                Element pkElement = (Element) itPrimaryKeys.next();
                primaryKeys.put(pkElement.getAttributeValue(XMLTags.COLUMN_NAME), pkElement);
            }
        }

        // check for imported keys
        if ((model.getDestinationImportedKeys(destinationTableName) != null) && (model.getDestinationImportedKeys(destinationTableName).size() > 0)) {
            Iterator itImportedKeys = model.getDestinationImportedKeys(destinationTableName).iterator();

            while (itImportedKeys.hasNext()) {
                Element impElement = (Element) itImportedKeys.next();
                importedKeys.put(impElement.getAttributeValue(XMLTags.FKCOLUMN_NAME), impElement);
            }
        }

        // check for exported keys
        if ((model.getDestinationExportedKeys(destinationTableName) != null) && (model.getDestinationExportedKeys(destinationTableName).size() > 0)) {
            Iterator itExportedKeys = model.getDestinationExportedKeys(destinationTableName).iterator();

            while (itExportedKeys.hasNext()) {
                Element expElement = (Element) itExportedKeys.next();
                exportedKeys.put(expElement.getAttributeValue(XMLTags.PKCOLUMN_NAME), expElement);
            }
        }

        // check for indexes
        if ((model.getDestinationIndexes(destinationTableName) != null) && (model.getDestinationIndexes(destinationTableName).size() > 0)) {
            Iterator itIndexes = model.getDestinationIndexes(destinationTableName).iterator();

            while (itIndexes.hasNext()) {
                Element indexElement = (Element) itIndexes.next();
                indexes.put(indexElement.getAttributeValue(XMLTags.COLUMN_NAME), indexElement);
            }
        }

        while (itProcessColumns.hasNext()) {
            Element column = (Element) itProcessColumns.next();

            column = model.getDestinationColumn(destinationTableName, column.getAttributeValue(XMLTags.DESTINATION_DB));

            // primary key
            if (primaryKeys.containsKey(column.getAttributeValue(XMLTags.NAME))) {
                sb.append("<id");
                sb.append(" name=\"" + column.getAttributeValue(XMLTags.NAME) + "\"");
                sb.append(" column=\"" + column.getAttributeValue(XMLTags.NAME) + "\"");
                sb.append(" type=\"" + typeMapping.getJavaType(column.getAttributeValue(XMLTags.DATA_TYPE)) + "\"");
                sb.append(">" + newLine);
                sb.append("   <generator class=\"native\"></generator>" + newLine);
                sb.append("</id>" + newLine);
            }
            // imported key
            else if (importedKeys.containsKey(column.getAttributeValue(XMLTags.NAME))) {
                Element impKey = (Element) importedKeys.get(column.getAttributeValue(XMLTags.NAME));

                sb.append("<many-to-one");
                sb.append(" name=\"" + impKey.getAttributeValue(XMLTags.PKTABLE_NAME) + "\"");
                sb.append(" class=\"" + impKey.getAttributeValue(XMLTags.PKTABLE_NAME) + "\"");
                sb.append(" cascade=\"none\"");
                sb.append(" outer-join=\"auto\"");
                sb.append(" cascade=\"none\"");
                sb.append(" column=\"" + impKey.getAttributeValue(XMLTags.PKCOLUMN_NAME) + "\"");
                sb.append(" not-null=\"" + column.getAttributeValue(XMLTags.NULLABLE) + "\"");
                sb.append(" />");
            }
            // exported key
            else if (exportedKeys.containsKey(column.getAttributeValue(XMLTags.NAME))) {
                Element expKey = (Element) exportedKeys.get(column.getAttributeValue(XMLTags.NAME));

                sb.append("<bag");
                sb.append(" name=\"" + expKey.getAttributeValue(XMLTags.FKTABLE_NAME) + "\"");
                sb.append(" lazy=\"false\"");
                sb.append(" inverse=\"false\"");
                sb.append(" cascade=\"none\"");
                sb.append(">" + newLine);
                sb.append("   <key");
                sb.append(" column=\"" + expKey.getAttributeValue(XMLTags.FKCOLUMN_NAME) + "\"");
                sb.append(" />" + newLine);
                sb.append("   <one-to-many");
                sb.append(" class=\"" + expKey.getAttributeValue(XMLTags.FKTABLE_NAME) + "\"");
                sb.append(" />" + newLine);
                sb.append("</bag>" + newLine);
            }
            // normal column
            else {
                sb.append("<property");
                sb.append(" name=\"" + column.getAttributeValue(XMLTags.NAME) + "\"");
                sb.append(" type=\"" + typeMapping.getJavaType(column.getAttributeValue(XMLTags.DATA_TYPE)) + "\"");
                sb.append(">" + newLine);
                sb.append("   <column" + newLine);
                sb.append("     name=\"" + column.getAttributeValue(XMLTags.NAME) + "\"" + newLine);
                sb.append("     length=\"" + column.getAttributeValue(XMLTags.COLUMN_SIZE) + "\"" + newLine);

                // check if not null
                if (column.getAttributeValue(XMLTags.NULLABLE).compareTo("false") == 0) {
                    sb.append("     not-null=\"" + "true" + "\"" + newLine);
                }

                // check for uniqueness and indexes
                if (indexes.containsKey(column.getAttributeValue(XMLTags.NAME))) {
                    Element index = (Element) indexes.get(column.getAttributeValue(XMLTags.NAME));

                    // check uniqueness
                    if ((index.getAttributeValue(XMLTags.NON_UNIQUE) != null) && (index.getAttributeValue(XMLTags.NON_UNIQUE).compareTo("0") == 0)) {
                        sb.append("     unique=\"true\"" + newLine);
                    }

                    // add index name
                    if ((index.getAttributeValue(XMLTags.INDEX_NAME) != null) && (index.getAttributeValue(XMLTags.INDEX_NAME).length() > 0)) {
                        sb.append("     index=\"" + index.getAttributeValue(XMLTags.INDEX_NAME) + "\"" + newLine);
                    }
                }

                sb.append("   />" + newLine);
                sb.append("</property>" + newLine);
            }
        }

        sb.append("</class>" + newLine);
        sb.append("</hibernate-mapping>" + newLine);
    }

    /**
     * DOCUMENT ME!
     */
    private void initXMLHeader() {
        sb.append("<?xml version=\"1.0\"?>" + newLine);
        sb.append("<!DOCTYPE hibernate-mapping PUBLIC \"-//Hibernate/Hibernate Mapping DTD 2.0//EN\" \"http://hibernate.sourceforge.net/hibernate-mapping-2.0.dtd\">" + newLine);
    }
}
