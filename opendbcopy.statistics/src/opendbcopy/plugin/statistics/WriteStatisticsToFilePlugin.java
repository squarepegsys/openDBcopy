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
package opendbcopy.plugin.statistics;

import opendbcopy.config.APM;
import opendbcopy.config.XMLTags;

import opendbcopy.connection.DBConnection;

import opendbcopy.connection.exception.DriverNotFoundException;
import opendbcopy.connection.exception.OpenConnectionException;

import opendbcopy.controller.MainController;

import opendbcopy.plugin.model.DynamicPluginThread;
import opendbcopy.plugin.model.Model;
import opendbcopy.plugin.model.exception.MissingAttributeException;
import opendbcopy.plugin.model.exception.MissingElementException;
import opendbcopy.plugin.model.exception.PluginException;
import opendbcopy.plugin.model.exception.UnsupportedAttributeValueException;

import opendbcopy.sql.Helper;

import opendbcopy.util.InputOutputHelper;

import org.jdom.Element;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.Date;
import java.util.Iterator;
import java.util.List;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class WriteStatisticsToFilePlugin extends DynamicPluginThread {
    private StatisticsModel model;

    /**
     * Creates a new WriteStatisticsToFilePlugin object.
     *
     * @param controller DOCUMENT ME!
     * @param baseModel DOCUMENT ME!
     *
     * @throws PluginException DOCUMENT ME!
     */
    public WriteStatisticsToFilePlugin(MainController controller,
                                       Model          baseModel) throws PluginException {
        super(controller, baseModel);
        model = (StatisticsModel) baseModel;
    }

    /**
     * DOCUMENT ME!
     *
     * @throws PluginException DOCUMENT ME!
     */
    public final void execute() throws PluginException {
        try {
            // source db
            if (!isInterrupted()) {
                readStatisticsForDb(model.getSourceDb(), model.getSourceStatistics());
            }

            // destination db
            if ((model.getDbMode() == model.DUAL_MODE) && !isInterrupted()) {
                readStatisticsForDb(model.getDestinationDb(), model.getDestinationStatistics());
            }

            if (!isInterrupted()) {
                writeStatisticsToFile();
            }
        } catch (UnsupportedAttributeValueException e) {
            throw new PluginException(e);
        } catch (MissingAttributeException e) {
            throw new PluginException(e);
        } catch (MissingElementException e) {
            throw new PluginException(e);
        } catch (DriverNotFoundException e) {
            throw new PluginException(e);
        } catch (SQLException e) {
            throw new PluginException(e);
        } catch (IOException e) {
            throw new PluginException(e);
        }
    }

    /**
     * Creates a new writeStatisticsToFile object.
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     * @throws UnsupportedAttributeValueException DOCUMENT ME!
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     */
    private void writeStatisticsToFile() throws IllegalArgumentException, UnsupportedAttributeValueException, MissingAttributeException, MissingElementException, IOException, PluginException {
        StringBuffer buffer = new StringBuffer();
        int          difference = 0;
        int          sourceRecords = 0;
        int          destinationRecords = 0;
        int          totalSourceRecords = 0;
        int          totalDestinationRecords = 0;

        // get parameters for plugin
        Element conf = model.getConf();
        
        String pathFilename = conf.getChild(XMLTags.FILE).getAttributeValue(XMLTags.VALUE);
        if (pathFilename == null) {
        	throw new PluginException("Missing path / filename to store statistics");
        }

        String fileType = conf.getChild(XMLTags.FILE_TYPE).getAttributeValue(XMLTags.VALUE);
        if (fileType == null) {
        	throw new PluginException("Missing file type");
        }

        int indexFileExtension = pathFilename.indexOf(fileType);

        // file does not yet contain extension
        if (indexFileExtension != pathFilename.length() - fileType.length()) {
        	pathFilename = pathFilename + "." + fileType;
        }
        
        String  delimiter = ";";

        // init the string buffer to show table headings
        buffer.append("Created by openDBcopy Statistics Plugin on " + model.getSourceStatistics().getAttributeValue(XMLTags.CAPTURE_DATE) + APM.LINE_SEP);
        buffer.append(APM.LINE_SEP);

        buffer.append("SOURCE" + delimiter);
        buffer.append("RECORDS" + delimiter);

        if (model.getDbMode() == model.DUAL_MODE) {
            buffer.append("DESTINATION" + delimiter);
            buffer.append("RECORDS" + delimiter);
            buffer.append("+ / -" + delimiter);
        }

        buffer.append(APM.LINE_SEP);

        if (model.getDbMode() == model.DUAL_MODE) {
            // go through the tables which are mapped
            Iterator itMappingTables = model.getMappingTables().iterator();

            while (itMappingTables.hasNext() && !isInterrupted()) {
                Element mappingTable = (Element) itMappingTables.next();
                Element statisticsSourceTable = model.getSourceStatisticsTable(mappingTable.getAttributeValue(XMLTags.SOURCE_DB));
                Element statisticsDestinationTable = model.getDestinationStatisticsTable(mappingTable.getAttributeValue(XMLTags.DESTINATION_DB));

                if ((statisticsSourceTable != null) && (statisticsDestinationTable != null)) {
                    sourceRecords          = Integer.parseInt(statisticsSourceTable.getAttributeValue(XMLTags.RECORDS));
                    destinationRecords     = Integer.parseInt(statisticsDestinationTable.getAttributeValue(XMLTags.RECORDS));
                    difference             = sourceRecords - destinationRecords;

                    buffer.append(statisticsSourceTable.getAttributeValue(XMLTags.NAME) + delimiter);
                    buffer.append(statisticsSourceTable.getAttributeValue(XMLTags.RECORDS) + delimiter);
                    buffer.append(statisticsDestinationTable.getAttributeValue(XMLTags.NAME) + delimiter);
                    buffer.append(statisticsDestinationTable.getAttributeValue(XMLTags.RECORDS) + delimiter);
                    buffer.append(difference + delimiter);
                    buffer.append(APM.LINE_SEP);
                }

                totalSourceRecords += sourceRecords;
                totalDestinationRecords += destinationRecords;
            }

            // add total records of mapped tables to buffer
            buffer.append("TOTAL" + delimiter);
            buffer.append(totalSourceRecords + delimiter);
            buffer.append("TOTAL" + delimiter);
            buffer.append(totalDestinationRecords + delimiter);
            buffer.append(totalSourceRecords - totalDestinationRecords);
            buffer.append(APM.LINE_SEP);

            // add empty line    	
            buffer.append(APM.LINE_SEP);

            // go through unmapped source tables
            List     unmappedSourceTables = model.getUnmappedSourceTables();

            Iterator itUnmappedSourceTables = unmappedSourceTables.iterator();

            while (itUnmappedSourceTables.hasNext() && !isInterrupted()) {
                Element table = model.getSourceStatisticsTable(((Element) itUnmappedSourceTables.next()).getAttributeValue(XMLTags.NAME));
                buffer.append(table.getAttributeValue(XMLTags.NAME) + delimiter);
                buffer.append(table.getAttributeValue(XMLTags.RECORDS) + delimiter);
                buffer.append("UNMAPPED" + delimiter);
                buffer.append(delimiter);
                buffer.append(delimiter);
                buffer.append(APM.LINE_SEP);
            }

            // go through unmapped destination tables
            List     unmappedDestinationTables = model.getUnmappedDestinationTables();

            Iterator itUnmappedDestinationTables = unmappedDestinationTables.iterator();

            while (itUnmappedDestinationTables.hasNext() && !isInterrupted()) {
                Element table = model.getDestinationStatisticsTable(((Element) itUnmappedDestinationTables.next()).getAttributeValue(XMLTags.NAME));
                buffer.append("UNMAPPED" + delimiter);
                buffer.append(delimiter);
                buffer.append(table.getAttributeValue(XMLTags.NAME) + delimiter);
                buffer.append(table.getAttributeValue(XMLTags.RECORDS) + delimiter);
                buffer.append(delimiter);
                buffer.append(APM.LINE_SEP);
            }
        }
        // single_db mode
        else {
            // go through source tables
            List     sourceTables = model.getSourceTables();

            Iterator itSourceTables = sourceTables.iterator();

            while (itSourceTables.hasNext() && !isInterrupted()) {
                Element table = model.getSourceStatisticsTable(((Element) itSourceTables.next()).getAttributeValue(XMLTags.NAME));
                buffer.append(table.getAttributeValue(XMLTags.NAME) + delimiter);
                buffer.append(table.getAttributeValue(XMLTags.RECORDS) + delimiter);
                buffer.append(APM.LINE_SEP);

                sourceRecords = Integer.parseInt(table.getAttributeValue(XMLTags.RECORDS));
                totalSourceRecords += sourceRecords;
            }

            buffer.append("TOTAL" + delimiter + totalSourceRecords + delimiter);
        }

        if (!isInterrupted()) {
            File       outputFile = new File(pathFilename);
            FileWriter fileWriter = new FileWriter(outputFile);

            fileWriter.write(buffer.toString());

            fileWriter.close();

            // append output file to plugin
            model.appendToOutput(InputOutputHelper.createFileElement(outputFile));

            logger.info("statistics written to " + pathFilename);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param db_element DOCUMENT ME!
     * @param statistics DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     * @throws OpenConnectionException DOCUMENT ME!
     * @throws DriverNotFoundException DOCUMENT ME!
     * @throws SQLException DOCUMENT ME!
     */
    private void readStatisticsForDb(Element db_element,
                                     Element statistics) throws IllegalArgumentException, MissingAttributeException, MissingElementException, OpenConnectionException, DriverNotFoundException, SQLException {
        if ((db_element == null) || (statistics == null)) {
            throw new IllegalArgumentException("Missing arguments values: db_element=" + db_element + " statistics=" + statistics);
        }

        // remove former statistics if existing
        if (statistics.getChildren().size() > 0) {
            statistics.removeChildren(XMLTags.TABLE);
        }

        // set the capture data
        statistics.setAttribute(XMLTags.CAPTURE_DATE, new Date().toString());

        // get connection
        Connection conn = DBConnection.getConnection(db_element.getChild(XMLTags.CONNECTION));

        // extract the tables to read
        Iterator  itTables = db_element.getChild(XMLTags.MODEL).getChildren(XMLTags.TABLE).iterator();

        Statement stmSource = conn.createStatement();

        int       nbrTables = db_element.getChild(XMLTags.MODEL).getChildren(XMLTags.TABLE).size();
        int       nbrRecords = 0;
        int       tableCounter = 0;
        int       totalNbrRecords = 0;
        String    qualifiedTableName = "";

        model.setLengthProgressTable(nbrTables);
        model.setCurrentProgressTable(tableCounter);

        logger.info("Counting records of " + db_element.getChild(XMLTags.CONNECTION).getAttributeValue(XMLTags.URL));

        while (itTables.hasNext() && !isInterrupted()) {
            Element table = (Element) itTables.next();
            String  tableName = table.getAttributeValue(XMLTags.NAME);

            if (db_element.getName().compareTo(XMLTags.SOURCE_DB) == 0) {
                qualifiedTableName     = model.getQualifiedSourceTableName(tableName);
                nbrRecords             = Helper.getNumberOfRecords(stmSource, model, XMLTags.SOURCE_DB, tableName);
            } else {
                qualifiedTableName     = model.getQualifiedDestinationTableName(tableName);
                nbrRecords             = Helper.getNumberOfRecords(stmSource, model, XMLTags.DESTINATION_DB, tableName);
            }

            model.setProgressMessage(qualifiedTableName);

            logger.info(qualifiedTableName + " (" + nbrRecords + ")");

            Element tableStatistics = new Element(XMLTags.TABLE);
            tableStatistics.setAttribute(XMLTags.NAME, table.getAttributeValue(XMLTags.NAME));
            tableStatistics.setAttribute(XMLTags.RECORDS, Integer.toString(nbrRecords));
            statistics.addContent(tableStatistics);

            model.setCurrentProgressTable(++tableCounter);

            totalNbrRecords += nbrRecords;
        }

        logger.info(db_element.getChild(XMLTags.CONNECTION).getAttributeValue(XMLTags.URL) + ": TOTAL records = " + totalNbrRecords);
    }
}
