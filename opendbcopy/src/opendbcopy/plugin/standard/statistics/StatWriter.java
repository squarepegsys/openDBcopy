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
package opendbcopy.plugin.standard.statistics;

import opendbcopy.config.XMLTags;

import opendbcopy.model.ProjectModel;

import opendbcopy.task.TaskExecute;

import org.apache.log4j.Logger;

import org.jdom.Element;

import java.io.File;
import java.io.FileWriter;

import java.util.Iterator;
import java.util.List;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public abstract class StatWriter {
    private static Logger logger = Logger.getLogger(StatWriter.class.getName());

    /**
     * DOCUMENT ME!
     *
     * @param task DOCUMENT ME!
     * @param projectModel DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public static void writeStatisticsToFile(TaskExecute  task,
                                             ProjectModel projectModel) throws Exception {
        StringBuffer buffer = new StringBuffer();
        int          difference = 0;
        int          sourceRecords = 0;
        int          destinationRecords = 0;
        int          totalSourceRecords = 0;
        int          totalDestinationRecords = 0;
        String       newLine = System.getProperty("line.separator");

        // get parameters for plugin
        Element conf = projectModel.getPlugin().getChild(XMLTags.CONF);
        String  fileName = conf.getChild(XMLTags.PATH).getAttributeValue(XMLTags.VALUE) + conf.getChild(XMLTags.FILE).getAttributeValue(XMLTags.VALUE) + "." + conf.getChild(XMLTags.FILE_TYPE).getAttributeValue(XMLTags.VALUE);
        String  delimiter = conf.getChild(XMLTags.DELIMITER).getAttributeValue(XMLTags.VALUE);

        // init the string buffer to show table headings
        buffer.append("Created by opendbcopy statistics on " + projectModel.getSourceStatistics().getAttributeValue(XMLTags.CAPTURE_DATE) + newLine);
        buffer.append(newLine);

        buffer.append("SOURCE" + delimiter);
        buffer.append("RECORDS" + delimiter);

        if (projectModel.getDbMode() == projectModel.DUAL_MODE) {
            buffer.append("DESTINATION" + delimiter);
            buffer.append("RECORDS" + delimiter);
            buffer.append("+ / -" + delimiter);
        }

        buffer.append(newLine);

        if (projectModel.getDbMode() == projectModel.DUAL_MODE) {
            // go through the tables which are mapped
            Iterator itMappingTables = projectModel.getMappingTables().iterator();

            while (itMappingTables.hasNext() && !task.isInterrupted()) {
                Element mappingTable = (Element) itMappingTables.next();
                Element statisticsSourceTable = projectModel.getSourceStatisticsTable(mappingTable.getAttributeValue(XMLTags.SOURCE_DB));
                Element statisticsDestinationTable = projectModel.getDestinationStatisticsTable(mappingTable.getAttributeValue(XMLTags.DESTINATION_DB));

                if ((statisticsSourceTable != null) && (statisticsDestinationTable != null)) {
                    sourceRecords          = Integer.parseInt(statisticsSourceTable.getAttributeValue(XMLTags.RECORDS));
                    destinationRecords     = Integer.parseInt(statisticsDestinationTable.getAttributeValue(XMLTags.RECORDS));
                    difference             = sourceRecords - destinationRecords;

                    buffer.append(statisticsSourceTable.getAttributeValue(XMLTags.NAME) + delimiter);
                    buffer.append(statisticsSourceTable.getAttributeValue(XMLTags.RECORDS) + delimiter);
                    buffer.append(statisticsDestinationTable.getAttributeValue(XMLTags.NAME) + delimiter);
                    buffer.append(statisticsDestinationTable.getAttributeValue(XMLTags.RECORDS) + delimiter);
                    buffer.append(difference + delimiter);
                    buffer.append(newLine);
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
            buffer.append(newLine);

            // add empty line    	
            buffer.append(newLine);

            // go through unmapped source tables
            List     unmappedSourceTables = projectModel.getUnmappedSourceTables();

            Iterator itUnmappedSourceTables = unmappedSourceTables.iterator();

            while (itUnmappedSourceTables.hasNext()) {
                Element table = projectModel.getSourceStatisticsTable(((Element) itUnmappedSourceTables.next()).getAttributeValue(XMLTags.NAME));
                buffer.append(table.getAttributeValue(XMLTags.NAME) + delimiter);
                buffer.append(table.getAttributeValue(XMLTags.RECORDS) + delimiter);
                buffer.append("UNMAPPED" + delimiter);
                buffer.append(delimiter);
                buffer.append(delimiter);
                buffer.append(newLine);
            }

            // go through unmapped destination tables
            List     unmappedDestinationTables = projectModel.getUnmappedDestinationTables();

            Iterator itUnmappedDestinationTables = unmappedDestinationTables.iterator();

            while (itUnmappedDestinationTables.hasNext()) {
                Element table = projectModel.getDestinationStatisticsTable(((Element) itUnmappedDestinationTables.next()).getAttributeValue(XMLTags.NAME));
                buffer.append("UNMAPPED" + delimiter);
                buffer.append(delimiter);
                buffer.append(table.getAttributeValue(XMLTags.NAME) + delimiter);
                buffer.append(table.getAttributeValue(XMLTags.RECORDS) + delimiter);
                buffer.append(delimiter);
                buffer.append(newLine);
            }
        }
        // single_db mode
        else {
            // go through source tables
            List     sourceTables = projectModel.getSourceTables();

            Iterator itSourceTables = sourceTables.iterator();

            while (itSourceTables.hasNext()) {
                Element table = projectModel.getSourceStatisticsTable(((Element) itSourceTables.next()).getAttributeValue(XMLTags.NAME));
                buffer.append(table.getAttributeValue(XMLTags.NAME) + delimiter);
                buffer.append(table.getAttributeValue(XMLTags.RECORDS) + delimiter);
                buffer.append(newLine);

                sourceRecords = Integer.parseInt(table.getAttributeValue(XMLTags.RECORDS));
                totalSourceRecords += sourceRecords;
            }

            buffer.append("TOTAL" + delimiter + totalSourceRecords + delimiter);
        }

        FileWriter fileWriter = new FileWriter(new File(fileName));

        fileWriter.write(buffer.toString());

        fileWriter.close();

        logger.info("statistics written to " + fileName);
    }
}
