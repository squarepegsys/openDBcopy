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
package opendbcopy.plugin.delete;

import opendbcopy.config.XMLTags;

import opendbcopy.connection.DBConnection;

import opendbcopy.connection.exception.CloseConnectionException;

import opendbcopy.controller.MainController;

import opendbcopy.plugin.model.DynamicPluginThread;
import opendbcopy.plugin.model.Model;
import opendbcopy.plugin.model.database.DatabaseModel;
import opendbcopy.plugin.model.exception.PluginException;

import opendbcopy.sql.Helper;

import org.jdom.Element;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.Iterator;
import java.util.List;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class DeleteTablesPlugin extends DynamicPluginThread {
    private DatabaseModel model;
    private List          processTables = null;
    private Connection    connDestination = null;
    private Statement     stmDestination = null;
    private String        deleteString = null;
    private int           counterTables = 0;

    /**
     * Creates a new DeleteDestinationTablesPlugin object.
     *
     * @param controller DOCUMENT ME!
     * @param baseModel DOCUMENT ME!
     *
     * @throws PluginException DOCUMENT ME!
     */
    public DeleteTablesPlugin(MainController controller,
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
            // get connection to destination database
            connDestination     = DBConnection.getConnection(model.getDestinationConnection());

            // extract the tables to copy
            processTables = model.getDestinationTablesToProcessOrdered();

            // now set the number of tables that need to be copied
            model.setLengthProgressTable(processTables.size());
        } catch (Exception e) {
            throw new PluginException(e.getMessage());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws PluginException DOCUMENT ME!
     */
    public void execute() throws PluginException {
        try {
            Iterator itProcessTables = processTables.iterator();

            while (!isInterrupted() && itProcessTables.hasNext()) {
                Element tableProcess = (Element) itProcessTables.next();

                String  destinationTableName = tableProcess.getAttributeValue(XMLTags.DESTINATION_DB);

                // setting record counter to minimum of progress bar
                model.setCurrentProgressRecord(0);
                model.setLengthProgressRecord(0);

                // get Delete Statement
                stmDestination     = connDestination.createStatement();
                deleteString       = Helper.getDeleteTableStatement(model.getQualifiedDestinationTableName(destinationTableName));

                model.setCurrentProgressTable(counterTables);

                // Execute Delete
                int recordsDeleted = stmDestination.executeUpdate(deleteString);

                model.setLengthProgressRecord(recordsDeleted);
                model.setCurrentProgressRecord(recordsDeleted);

                model.setProgressMessage("Deleted " + recordsDeleted + " records in " + model.getQualifiedDestinationTableName(destinationTableName));
                logger.info("Deleted " + recordsDeleted + " records in " + model.getQualifiedDestinationTableName(destinationTableName));

                // required in case of last table that had to be copied
                counterTables++;
                model.setCurrentProgressTable(counterTables);

                // set processed
                tableProcess.setAttribute(XMLTags.PROCESSED, "true");
            }

            if (!isInterrupted()) {
                if (processTables.size() > 0) {
                    connDestination.commit();
                    stmDestination.close();
                    DBConnection.closeConnection(connDestination);
                    logger.info(counterTables + " table(s) deleted and commited.");
                } else {
                    logger.warn("no tables to process!");
                }
            } else {
                if (processTables.size() > 0) {
                    connDestination.rollback();
                    stmDestination.close();
                }

                DBConnection.closeConnection(connDestination);
                logger.info("execution cancelled by user. A Rollback has been made.");
            }
        } catch (SQLException sqle) {
            throw new PluginException(sqle);
        } catch (Exception e1) {
            // clean up
            try {
                DBConnection.closeConnection(connDestination);
            } catch (CloseConnectionException e2) {
                // bad luck ... don't worry
            }

            throw new PluginException(e1.getMessage());
        }
    }
}
