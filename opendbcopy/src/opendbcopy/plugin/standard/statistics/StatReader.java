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
 *
 * --------------------------------------------------------------------------*/
package opendbcopy.plugin.standard.statistics;

import opendbcopy.config.XMLTags;

import opendbcopy.connection.DBConnection;

import opendbcopy.connection.exception.DriverNotFoundException;
import opendbcopy.connection.exception.OpenConnectionException;

import opendbcopy.model.ProjectModel;

import opendbcopy.model.exception.MissingAttributeException;
import opendbcopy.model.exception.MissingElementException;
import opendbcopy.model.exception.UnsupportedAttributeValueException;

import opendbcopy.plugin.ExecuteSkeleton;

import opendbcopy.task.TaskExecute;

import org.apache.log4j.Logger;

import org.jdom.Element;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.Date;
import java.util.Iterator;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public abstract class StatReader {
    private static Logger logger = Logger.getLogger(StatReader.class.getName());

    /**
     * DOCUMENT ME!
     *
     * @param task DOCUMENT ME!
     * @param projectModel DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     * @throws UnsupportedAttributeValueException DOCUMENT ME!
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     * @throws OpenConnectionException DOCUMENT ME!
     * @throws DriverNotFoundException DOCUMENT ME!
     * @throws SQLException DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public static void readStatistics(TaskExecute  task,
                                      ProjectModel projectModel) throws IllegalArgumentException, UnsupportedAttributeValueException, MissingAttributeException, MissingElementException, OpenConnectionException, DriverNotFoundException, SQLException, Exception {
        if ((task == null) || (projectModel == null)) {
            throw new IllegalArgumentException("Missing arguments values: task=" + task + " projectModel=" + projectModel);
        }

        // source db
        readStatisticsForDb(task, projectModel, projectModel.getSourceDb(), projectModel.getSourceStatistics());

        // destination db
        if (projectModel.getDbMode() == projectModel.DUAL_MODE) {
            readStatisticsForDb(task, projectModel, projectModel.getDestinationDb(), projectModel.getDestinationStatistics());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param task DOCUMENT ME!
     * @param projectModel DOCUMENT ME!
     * @param db_element DOCUMENT ME!
     * @param statistics DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     * @throws OpenConnectionException DOCUMENT ME!
     * @throws DriverNotFoundException DOCUMENT ME!
     * @throws SQLException DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public static void readStatisticsForDb(TaskExecute  task,
                                           ProjectModel projectModel,
                                           Element      db_element,
                                           Element      statistics) throws IllegalArgumentException, MissingAttributeException, MissingElementException, OpenConnectionException, DriverNotFoundException, SQLException, Exception {
        if ((task == null) || (projectModel == null) || (db_element == null) || (statistics == null)) {
            throw new IllegalArgumentException("Missing arguments values: task=" + task + " projectModel=" + projectModel + " db_element=" + db_element + " statistics=" + statistics);
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

        task.setLengthOfTaskTable(nbrTables);
        task.setCurrentTable(tableCounter);

        logger.info("Counting records of " + db_element.getChild(XMLTags.CONNECTION).getAttributeValue(XMLTags.URL));

        while (itTables.hasNext() && !task.isInterrupted()) {
            Element table = (Element) itTables.next();
            String  tableName = table.getAttributeValue(XMLTags.NAME);

            if (db_element.getName().compareTo(XMLTags.SOURCE_DB) == 0) {
                qualifiedTableName     = projectModel.getQualifiedSourceTableName(tableName);
                nbrRecords             = ExecuteSkeleton.getNumberOfRecords(stmSource, projectModel, XMLTags.SOURCE_DB, tableName);
            } else {
                qualifiedTableName     = projectModel.getQualifiedDestinationTableName(tableName);
                nbrRecords             = ExecuteSkeleton.getNumberOfRecords(stmSource, projectModel, XMLTags.DESTINATION_DB, tableName);
            }

            task.setMessage(qualifiedTableName);

            logger.info(qualifiedTableName + " (" + nbrRecords + ")");

            Element tableStatistics = new Element(XMLTags.TABLE);
            tableStatistics.setAttribute(XMLTags.NAME, table.getAttributeValue(XMLTags.NAME));
            tableStatistics.setAttribute(XMLTags.RECORDS, Integer.toString(nbrRecords));
            statistics.addContent(tableStatistics);

            task.setCurrentTable(++tableCounter);

            totalNbrRecords += nbrRecords;
        }

        logger.info(db_element.getChild(XMLTags.CONNECTION).getAttributeValue(XMLTags.URL) + ": TOTAL records = " + totalNbrRecords);
    }
}
