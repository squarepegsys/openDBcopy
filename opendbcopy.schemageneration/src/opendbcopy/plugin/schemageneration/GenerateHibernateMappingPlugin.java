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

import opendbcopy.config.APM;
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

import java.io.File;
import java.io.IOException;

import java.sql.DatabaseMetaData;

import java.util.ArrayList;
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
    private static final String HBM_FILE_SUFFIX = ".hbm.xml";
    private static final String MANY_TO_ONE_REF_EXTENSION = "Ref";
    private DatabaseModel       model;
    private TypeMapping         typeMapping;
    private StringBuffer        sb;
    private Element             conf;
    private File                outputPath;
    private String              packageName;
    private String              hibernateDialect;
    private String              database = "";
    private String              uniqueKeyGenerator = "";
    private String              identifierQuoteStringOut = "";
    private String              outerJoin;
    private boolean             show_qualified_table_name = false;
    private boolean             lazy;
    private boolean             inverse;
    private int                 counterRecords = 0;
    private int                 counterTables = 0;
    private List                processTables;

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

            // read the plugin configuration
            conf     = model.getConf();

            outputPath = new File(conf.getChild(XMLTags.DIR).getAttributeValue(XMLTags.VALUE));

            if (!outputPath.exists()) {
                boolean mkDirOk = outputPath.mkdir();

                if (!mkDirOk) {
                    throw new PluginException("Could not create " + outputPath.getAbsolutePath());
                }
            }

            packageName          = conf.getChild(XMLTags.PACKAGE_NAME).getAttributeValue(XMLTags.VALUE);
            hibernateDialect     = conf.getChild(XMLTags.HIBERNATE_DIALECT).getAttributeValue(XMLTags.CLASS);

            outerJoin     = conf.getChild(XMLTags.OUTER_JOIN).getAttributeValue(XMLTags.VALUE);

            lazy        = Boolean.valueOf(conf.getChild(XMLTags.LAZY).getAttributeValue(XMLTags.VALUE)).booleanValue();
            inverse     = Boolean.valueOf(conf.getChild(XMLTags.INVERSE).getAttributeValue(XMLTags.VALUE)).booleanValue();

            // retrieve unique key generator
            uniqueKeyGenerator = conf.getChild(XMLTags.GENERATOR_CLASS).getAttributeValue(XMLTags.VALUE);

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
                    fileName                 = outputPath.getAbsolutePath() + APM.FILE_SEP + destinationTableName + HBM_FILE_SUFFIX;
                }

                sb = new StringBuffer();

                genHibernateMappingFile(destinationTableName, processColumns);

                Writer.write(sb, fileName);

                logger.info(fileName + " written");

                model.setCurrentProgressTable(++counterTables);
            }

            // create hibernate.properties file
            Properties p = new Properties();
            Element    destinationDriver = model.getDestinationConnection();
            Element    destinationDbProductName = model.getDestinationMetadata().getChild(XMLTags.DB_PRODUCT_NAME);

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

            if ((destinationDbProductName != null) && (destinationDbProductName.getAttributeValue(XMLTags.VALUE) != null)) {
                p.setProperty(XMLTags.DB_PRODUCT_NAME, destinationDbProductName.getAttributeValue(XMLTags.VALUE));
            } else {
                p.setProperty(XMLTags.DB_PRODUCT_NAME, "unknown");
            }

            PropertiesToFile.exportPropertiesToFile(p, outputPath.getAbsolutePath() + APM.FILE_SEP + "hibernate.properties");
            logger.info("hibernate properties written to " + outputPath.getAbsolutePath() + APM.FILE_SEP + "hibernate.properties");

            if (!isInterrupted()) {
                logger.info(counterTables + " table(s) processed");
                logger.info("Please see user manual for documentation on how to continue");
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
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws UnsupportedAttributeValueException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     */
    private void genHibernateMappingFile(String destinationTableName,
                                         List   processColumns) throws MissingAttributeException, UnsupportedAttributeValueException, MissingElementException {
        initXMLHeader();

        sb.append("<hibernate-mapping>" + APM.LINE_SEP);
        sb.append("<class name=\"" + packageName + "." + destinationTableName + "\" table=\"" + destinationTableName + "\">" + APM.LINE_SEP);

        HashMap   mapPrimaryKeyElements = new HashMap();
        HashMap   mapImportedKeyElements = new HashMap();
        HashMap   mapExportedKeyElements = new HashMap();
        HashMap   mapIndexElements = new HashMap();
        ArrayList listImportedKeyElementsOrdered = new ArrayList();

        // check for primary keys
        if ((model.getDestinationPrimaryKeys(destinationTableName) != null) && (model.getDestinationPrimaryKeys(destinationTableName).size() > 0)) {
            Iterator itPrimaryKeys = model.getDestinationPrimaryKeys(destinationTableName).iterator();

            while (itPrimaryKeys.hasNext()) {
                Element pkElement = (Element) itPrimaryKeys.next();
                mapPrimaryKeyElements.put(pkElement.getAttributeValue(XMLTags.COLUMN_NAME), pkElement);
            }
        }

        // check for imported keys
        if ((model.getDestinationImportedKeys(destinationTableName) != null) && (model.getDestinationImportedKeys(destinationTableName).size() > 0)) {
            Iterator itImportedKeys = model.getDestinationImportedKeys(destinationTableName).iterator();

            while (itImportedKeys.hasNext()) {
                Element impElement = (Element) itImportedKeys.next();
                mapImportedKeyElements.put(impElement.getAttributeValue(XMLTags.FKCOLUMN_NAME), impElement);
                listImportedKeyElementsOrdered.add(impElement);
            }
        }

        // check for exported keys
        if ((model.getDestinationExportedKeys(destinationTableName) != null) && (model.getDestinationExportedKeys(destinationTableName).size() > 0)) {
            Iterator itExportedKeys = model.getDestinationExportedKeys(destinationTableName).iterator();

            while (itExportedKeys.hasNext()) {
                Element expElement = (Element) itExportedKeys.next();
                mapExportedKeyElements.put(expElement.getAttributeValue(XMLTags.FKTABLE_NAME), expElement);
            }
        }

        // check for indexes
        if ((model.getDestinationIndexes(destinationTableName) != null) && (model.getDestinationIndexes(destinationTableName).size() > 0)) {
            Iterator itIndexes = model.getDestinationIndexes(destinationTableName).iterator();

            while (itIndexes.hasNext()) {
                Element indexElement = (Element) itIndexes.next();
                mapIndexElements.put(indexElement.getAttributeValue(XMLTags.COLUMN_NAME), indexElement);
            }
        }

        // split columns into different lists
        ArrayList normalColumns = new ArrayList();
        ArrayList primaryKeyColumns = new ArrayList();
        ArrayList importedKeyColumns = new ArrayList();
        Element   column = null;

        for (int i = 0; i < processColumns.size(); i++) {
            column     = (Element) processColumns.get(i);
            column     = model.getDestinationColumn(destinationTableName, column.getAttributeValue(XMLTags.DESTINATION_DB));

            if (mapPrimaryKeyElements.containsKey(column.getAttributeValue(XMLTags.NAME))) {
                primaryKeyColumns.add(column);
            } else if (mapImportedKeyElements.containsKey(column.getAttributeValue(XMLTags.NAME))) {
                importedKeyColumns.add(column);
            } else {
                normalColumns.add(column);
            }
        }

        // single primary key column
        if (primaryKeyColumns.size() == 1) {
            processSinglePrimaryKey(sb, (Element) primaryKeyColumns.get(0), destinationTableName);
        }
        // compound primary key
        else {
            processCompositeKey(sb, destinationTableName, mapImportedKeyElements, listImportedKeyElementsOrdered, primaryKeyColumns);
        }

        // Process normal columns
        for (int i = 0; i < normalColumns.size(); i++) {
            processNormalColumn(sb, mapIndexElements, (Element) normalColumns.get(i));
        }

        // imported keys
        processImportedKeys(sb, mapImportedKeyElements, importedKeyColumns);

        // exported key
        if (mapExportedKeyElements.size() > 0) {
            Iterator itExportedKeys = mapExportedKeyElements.values().iterator();

            while (itExportedKeys.hasNext()) {
                processExportedKey((Element) itExportedKeys.next());
            }
        }

        sb.append("</class>" + APM.LINE_SEP);
        sb.append("</hibernate-mapping>" + APM.LINE_SEP);
    }

    /**
     * DOCUMENT ME!
     */
    private void initXMLHeader() {
        sb.append("<?xml version=\"1.0\"?>" + APM.LINE_SEP);
        sb.append("<!DOCTYPE hibernate-mapping PUBLIC \"-//Hibernate/Hibernate Mapping DTD 2.0//EN\" \"http://hibernate.sourceforge.net/hibernate-mapping-2.0.dtd\">" + APM.LINE_SEP);
    }

    /**
     * DOCUMENT ME!
     *
     * @param sb DOCUMENT ME!
     * @param column DOCUMENT ME!
     * @param tableName DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws UnsupportedAttributeValueException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     */
    private void processSinglePrimaryKey(StringBuffer sb,
                                         Element      column,
                                         String       tableName) throws MissingAttributeException, UnsupportedAttributeValueException, MissingElementException {
        sb.append("<id");
        sb.append(" name=\"" + column.getAttributeValue(XMLTags.NAME) + "\"");
        sb.append(" column=\"" + column.getAttributeValue(XMLTags.NAME) + "\"");
        sb.append(" type=\"" + typeMapping.getJavaType(column.getAttributeValue(XMLTags.DATA_TYPE)) + "\"");
        sb.append(">" + APM.LINE_SEP);

        // add params if available - and replace [table] patterns
        Element  generatorElement = null;
        Iterator itGeneratorElements = conf.getChild(XMLTags.GENERATOR_CLASS).getChildren().iterator();

        while (itGeneratorElements.hasNext() && (generatorElement == null)) {
            Element element = (Element) itGeneratorElements.next();

            if (element.getAttributeValue(XMLTags.CLASS).compareTo(uniqueKeyGenerator) == 0) {
                generatorElement = element;
            }
        }

        if ((generatorElement != null) && (generatorElement.getChildren().size() > 0)) {
            sb.append("   <generator class=\"" + uniqueKeyGenerator + "\">" + APM.LINE_SEP);

            List children = conf.getChild(XMLTags.GENERATOR_CLASS).getChild(uniqueKeyGenerator).getChildren();

            for (int i = 0; i < children.size(); i++) {
                Element child = (Element) children.get(i);
                sb.append("      <" + child.getName() + " name=\"" + child.getAttributeValue(XMLTags.NAME) + "\"" + ">");

                String text = child.getText();

                if ((text != null) && (text.length() > 0)) {
                    if ((text.indexOf("[") >= 0) && (text.indexOf("]") > 0)) {
                        int startIndex = text.indexOf("[");
                        int endIndex = text.indexOf("]");

                        sb.append(text.substring(0, startIndex) + tableName + text.substring(endIndex + "]".length(), text.length()));
                    } else {
                        sb.append(text);
                    }
                }

                sb.append("</" + child.getName() + ">" + APM.LINE_SEP);
            }

            sb.append("   </generator>" + APM.LINE_SEP);
        } else {
            sb.append("   <generator class=\"" + uniqueKeyGenerator + "\" />" + APM.LINE_SEP);
        }

        sb.append("</id>" + APM.LINE_SEP);
    }

    /**
     * DOCUMENT ME!
     *
     * @param sb DOCUMENT ME!
     * @param destinationTableName DOCUMENT ME!
     * @param importedKeys DOCUMENT ME!
     * @param importedKeysOrdered DOCUMENT ME!
     * @param primaryKeyColumns DOCUMENT ME!
     */
    private void processCompositeKey(StringBuffer sb,
                                     String       destinationTableName,
                                     HashMap      importedKeys,
                                     ArrayList    importedKeysOrdered,
                                     ArrayList    primaryKeyColumns) {
        // check which primary key is only within this table and which ones point to another table
        ArrayList onlyPrimaryKeyColumns = new ArrayList();
        ArrayList primaryAndImportedKeyColumns = new ArrayList();

        for (int i = 0; i < primaryKeyColumns.size(); i++) {
            Element pkColumn = (Element) primaryKeyColumns.get(i);

            if (importedKeys.containsKey(pkColumn.getAttributeValue(XMLTags.NAME))) {
                primaryAndImportedKeyColumns.add(pkColumn);
            } else {
                onlyPrimaryKeyColumns.add(pkColumn);
            }
        }

        // prepare imported key many-to-one fragments
        HashMap importedKeysMap = new HashMap();

        // imported keys to one or several parent tables... create a key-many-to-one element for each parent table
        ArrayList parentTables = new ArrayList();

        for (int i = 0; i < importedKeysOrdered.size(); i++) {
            Element impKey = (Element) importedKeysOrdered.get(i);

            if (!parentTables.contains(impKey.getAttributeValue(XMLTags.PKTABLE_NAME))) {
                parentTables.add(impKey.getAttributeValue(XMLTags.PKTABLE_NAME));
            }
        }

        for (int i = 0; i < parentTables.size(); i++) {
            String    parentTableName = (String) parentTables.get(i);

            ArrayList childColumns = new ArrayList();

            // retrieve the columns in right order which reference this parentTable
            for (int j = 0; j < importedKeysOrdered.size(); j++) {
                Element impKey = (Element) importedKeysOrdered.get(j);

                if (impKey.getAttributeValue(XMLTags.PKTABLE_NAME).compareToIgnoreCase(parentTableName) == 0) {
                    childColumns.add(impKey);
                }
            }

            StringBuffer fragment = new StringBuffer();

            fragment.append("  <!-- bi-directional many-to-one association to " + parentTableName + " -->" + APM.LINE_SEP);
            fragment.append("  <key-many-to-one" + APM.LINE_SEP);
            fragment.append("      name=" + "\"" + parentTableName + "\"" + APM.LINE_SEP);
            fragment.append("      class=" + "\"" + packageName + "." + parentTableName + "\"" + ">" + APM.LINE_SEP);

            String columnNameIdentifier = null;

            for (int j = 0; j < childColumns.size(); j++) {
                Element col = (Element) childColumns.get(j);
                fragment.append("      <column name=" + "\"" + col.getAttributeValue(XMLTags.FKCOLUMN_NAME) + "\"" + " />" + APM.LINE_SEP);
                columnNameIdentifier = col.getAttributeValue(XMLTags.FKCOLUMN_NAME);
            }

            fragment.append("  </key-many-to-one>" + APM.LINE_SEP);

            // take the latest, if there are several, fk_column_name as identifier for later correct insertion
            importedKeysMap.put(columnNameIdentifier, fragment);
        }

        // write composite-id header
        sb.append("<composite-id" + APM.LINE_SEP);
        sb.append(" name=\"comp_id_" + destinationTableName + "\"" + APM.LINE_SEP);
        sb.append(" class=\"" + packageName + "." + destinationTableName + "PK" + "\">" + APM.LINE_SEP);

        // now the primary keys must be inserted in correct order
        for (int indexOrder = 0; indexOrder < primaryKeyColumns.size(); indexOrder++) {
            Element pkColumn = (Element) primaryKeyColumns.get(indexOrder);

            // simple primary key column
            if (onlyPrimaryKeyColumns.contains(pkColumn)) {
                sb.append("  <key-property" + APM.LINE_SEP);
                sb.append("      name=" + "\"" + pkColumn.getAttributeValue(XMLTags.NAME) + "\"" + APM.LINE_SEP);
                sb.append("      column=" + "\"" + pkColumn.getAttributeValue(XMLTags.NAME) + "\"" + APM.LINE_SEP);
                sb.append("      type=\"" + typeMapping.getJavaType(pkColumn.getAttributeValue(XMLTags.DATA_TYPE)) + "\"" + APM.LINE_SEP);
                sb.append("      length=\"" + pkColumn.getAttributeValue(XMLTags.COLUMN_SIZE) + "\"" + "/>" + APM.LINE_SEP);
            }
            // imported key
            else {
                String columnName = pkColumn.getAttributeValue(XMLTags.NAME);

                if (importedKeysMap.containsKey(columnName)) {
                    sb.append(importedKeysMap.get(columnName));
                }
            }
        }

        // write composite-id footer
        sb.append("</composite-id>" + APM.LINE_SEP);
    }

    /**
     * DOCUMENT ME!
     *
     * @param sb DOCUMENT ME!
     * @param indexes DOCUMENT ME!
     * @param column DOCUMENT ME!
     */
    private void processNormalColumn(StringBuffer sb,
                                     HashMap      indexes,
                                     Element      column) {
        sb.append("<property");
        sb.append(" name=\"" + column.getAttributeValue(XMLTags.NAME) + "\"");
        sb.append(" type=\"" + typeMapping.getJavaType(column.getAttributeValue(XMLTags.DATA_TYPE)) + "\"");
        sb.append(">" + APM.LINE_SEP);
        sb.append("   <column" + APM.LINE_SEP);
        sb.append("     name=\"" + column.getAttributeValue(XMLTags.NAME) + "\"" + APM.LINE_SEP);
        sb.append("     length=\"" + column.getAttributeValue(XMLTags.COLUMN_SIZE) + "\"" + APM.LINE_SEP);

        // check if not null
        if (column.getAttributeValue(XMLTags.NULLABLE).compareTo("false") == 0) {
            sb.append("     not-null=\"" + "true" + "\"" + APM.LINE_SEP);
        }

        // check for uniqueness and indexes
        if (indexes.containsKey(column.getAttributeValue(XMLTags.NAME))) {
            Element index = (Element) indexes.get(column.getAttributeValue(XMLTags.NAME));

            // check uniqueness
            if ((index.getAttributeValue(XMLTags.NON_UNIQUE) != null) && (index.getAttributeValue(XMLTags.NON_UNIQUE).compareTo("0") == 0)) {
                sb.append("     unique=\"true\"" + APM.LINE_SEP);
            }

            // add index name
            if ((index.getAttributeValue(XMLTags.INDEX_NAME) != null) && (index.getAttributeValue(XMLTags.INDEX_NAME).length() > 0)) {
                sb.append("     index=\"" + index.getAttributeValue(XMLTags.INDEX_NAME) + "\"" + APM.LINE_SEP);
            }
        }

        sb.append("   />" + APM.LINE_SEP);
        sb.append("</property>" + APM.LINE_SEP);
    }

    /**
     * Imported Keys are appended at the end of a table, given by hbm2java Tool, independent of hbm column order An imported key can be a reference
     * to a single primary key or compound key of another table
     *
     * @param sb DOCUMENT ME!
     * @param mapImportedKeyElements DOCUMENT ME!
     * @param listImportedKeyColumns DOCUMENT ME!
     *
     * @throws MissingElementException DOCUMENT ME!
     */
    private void processImportedKeys(StringBuffer sb,
                                     HashMap      mapImportedKeyElements,
                                     ArrayList    listImportedKeyColumns) throws MissingElementException {
        // find out which columns are imported keys pointing to a compound key - or primary key
        HashMap   mapImportedTables = new HashMap();
        ArrayList listImportedTablesOrdered = new ArrayList();

        for (int i = 0; i < listImportedKeyColumns.size(); i++) {
            Element columnElement = (Element) listImportedKeyColumns.get(i);
            Element impElement = (Element) mapImportedKeyElements.get(columnElement.getAttributeValue(XMLTags.NAME));

            String  referencedTableName = impElement.getAttributeValue(XMLTags.PKTABLE_NAME);
            
            if (!listImportedTablesOrdered.contains(referencedTableName)) {
                listImportedTablesOrdered.add(referencedTableName);
            }

            if (!mapImportedTables.containsKey(referencedTableName)) {
                ArrayList list = new ArrayList();
                list.add(impElement);
                mapImportedTables.put(referencedTableName, list);
            } else {
                ArrayList list = (ArrayList) mapImportedTables.get(referencedTableName);
                list.add(impElement);
            }
        }

        for (int i = 0; i < listImportedTablesOrdered.size(); i++) {
            String    referencedTableName = (String) listImportedTablesOrdered.get(i);

            ArrayList listImpElements = (ArrayList) mapImportedTables.get(referencedTableName);

            if (listImpElements.size() == 1) {
                Element impElement = (Element) listImpElements.get(0);
                Element columnElement = model.getDestinationColumn(impElement.getAttributeValue(XMLTags.FKTABLE_NAME), impElement.getAttributeValue(XMLTags.FKCOLUMN_NAME));
                processSingleImportedKey(sb, impElement, columnElement);
            } else {
                processCompositeImportedKey(sb, listImpElements);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param sb DOCUMENT ME!
     * @param impKey DOCUMENT ME!
     * @param column DOCUMENT ME!
     */
    private void processSingleImportedKey(StringBuffer sb,
                                          Element      impKey,
                                          Element      column) {
        sb.append("<many-to-one");
        sb.append(" name=\"" + impKey.getAttributeValue(XMLTags.PKTABLE_NAME).toLowerCase() + MANY_TO_ONE_REF_EXTENSION + "\"");
        sb.append(" class=\"" + packageName + "." + impKey.getAttributeValue(XMLTags.PKTABLE_NAME) + "\"");

        int deleteRule = Integer.parseInt(impKey.getAttributeValue(XMLTags.DELETE_RULE));

        processImportDeleteRule(sb, deleteRule);

        sb.append(" outer-join=\"" + outerJoin + "\"");

        if (column.getAttributeValue(XMLTags.NULLABLE).compareTo("false") == 0) {
            sb.append(" not-null=\"" + "true" + "\"");
        }

        sb.append(">" + APM.LINE_SEP);

        sb.append(" <column name=\"" + impKey.getAttributeValue(XMLTags.FKCOLUMN_NAME) + "\"" + " />" + APM.LINE_SEP);

        sb.append("</many-to-one>" + APM.LINE_SEP);
    }

    /**
     * DOCUMENT ME!
     *
     * @param sb DOCUMENT ME!
     * @param impKeys DOCUMENT ME!
     *
     * @throws MissingElementException DOCUMENT ME!
     */
    private void processCompositeImportedKey(StringBuffer sb,
                                             ArrayList    impKeys) throws MissingElementException {
        // Retrieve basic information
        Element impElement = (Element) impKeys.get(0);
        Element columnElement = model.getDestinationColumn(impElement.getAttributeValue(XMLTags.FKTABLE_NAME), impElement.getAttributeValue(XMLTags.FKCOLUMN_NAME));
        String  importedTableName = impElement.getAttributeValue(XMLTags.PKTABLE_NAME);
        String  deleteRuleString = impElement.getAttributeValue(XMLTags.DELETE_RULE);

        sb.append("<many-to-one");
        sb.append(" name=\"" + importedTableName.toLowerCase() + MANY_TO_ONE_REF_EXTENSION + "\"");
        sb.append(" class=\"" + packageName + "." + importedTableName + "\"");

        int deleteRule = Integer.parseInt(deleteRuleString);

        processImportDeleteRule(sb, deleteRule);

        sb.append(" outer-join=\"" + outerJoin + "\"");

        if (columnElement.getAttributeValue(XMLTags.NULLABLE).compareTo("false") == 0) {
            sb.append(" not-null=\"" + "true" + "\"");
        }

        sb.append(">" + APM.LINE_SEP);

        for (int i = 0; i < impKeys.size(); i++) {
            Element impKey = (Element) impKeys.get(i);
            sb.append(" <column name=\"" + impKey.getAttributeValue(XMLTags.FKCOLUMN_NAME) + "\"" + " />" + APM.LINE_SEP);
        }

        sb.append("</many-to-one>" + APM.LINE_SEP);
    }

    /**
     * DOCUMENT ME!
     *
     * @param sb DOCUMENT ME!
     * @param deleteRule DOCUMENT ME!
     */
    private void processImportDeleteRule(StringBuffer sb,
                                         int          deleteRule) {
        switch (deleteRule) {
        case DatabaseMetaData.importedKeyNoAction:
            sb.append(" cascade=\"none\"");

            break;

        case DatabaseMetaData.importedKeyRestrict:
            sb.append(" cascade=\"none\"");

            break;

        case DatabaseMetaData.importedKeyCascade:
            sb.append(" cascade=\"all\"");

            break;

        case DatabaseMetaData.importedKeySetNull:
            sb.append(" cascade=\"all\"");

            break;

        case DatabaseMetaData.importedKeySetDefault:
            sb.append(" cascade=\"all\"");

            break;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param expKey DOCUMENT ME!
     */
    private void processExportedKey(Element expKey) {
        sb.append("<set");
        
        // the exported key name must be different than the foreign key table name as other object may already have a reference called the same
        sb.append(" name=\"" + expKey.getAttributeValue(XMLTags.FKTABLE_NAME).toLowerCase() + "s" + "\"");
        sb.append(" lazy=\"" + lazy + "\"");
        sb.append(" inverse=\"" + inverse + "\"");

        int deleteRule = Integer.parseInt(expKey.getAttributeValue(XMLTags.DELETE_RULE));

        switch (deleteRule) {
        case DatabaseMetaData.importedKeyNoAction:
            sb.append(" cascade=\"none\"");

            break;

        case DatabaseMetaData.importedKeyRestrict:
            sb.append(" cascade=\"none\"");

            break;

        case DatabaseMetaData.importedKeyCascade:
            sb.append(" cascade=\"all\"");

            break;

        case DatabaseMetaData.importedKeySetNull:
            sb.append(" cascade=\"all\"");

            break;

        case DatabaseMetaData.importedKeySetDefault:
            sb.append(" cascade=\"all\"");

            break;
        }

        sb.append(">" + APM.LINE_SEP);
        sb.append("   <key");
        sb.append(" column=\"" + expKey.getAttributeValue(XMLTags.FKCOLUMN_NAME) + "\"");
        sb.append(" />" + APM.LINE_SEP);
        sb.append("   <one-to-many");
        sb.append(" class=\"" + packageName + "." + expKey.getAttributeValue(XMLTags.FKTABLE_NAME) + "\"");
        sb.append(" />" + APM.LINE_SEP);
        sb.append("</set>" + APM.LINE_SEP);
    }
}
