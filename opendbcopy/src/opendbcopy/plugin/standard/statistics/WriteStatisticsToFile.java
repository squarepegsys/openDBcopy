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

import opendbcopy.model.ProjectModel;

import opendbcopy.plugin.ExecuteSkeleton;

import opendbcopy.plugin.exception.PluginException;

import opendbcopy.task.TaskExecute;

import java.sql.SQLException;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class WriteStatisticsToFile extends ExecuteSkeleton {
    /**
     * Creates a new CopyMapping object.
     *
     * @param task DOCUMENT ME!
     * @param projectModel DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     * @throws PluginException DOCUMENT ME!
     */
    public WriteStatisticsToFile(TaskExecute  task,
                                 ProjectModel projectModel) throws IllegalArgumentException, PluginException {
        if ((task == null) || (projectModel == null)) {
            throw new IllegalArgumentException("Missing arguments values: task=" + task + " projectModel=" + projectModel);
        }

        // execute unprocessed tables
        doExecute(task, projectModel);
    }

    /**
     * DOCUMENT ME!
     *
     * @param task DOCUMENT ME!
     * @param projectModel DOCUMENT ME!
     *
     * @throws PluginException DOCUMENT ME!
     */
    public final void doExecute(TaskExecute  task,
                                ProjectModel projectModel) throws PluginException {
        try {
            StatReader.readStatistics(task, projectModel);
            StatWriter.writeStatisticsToFile(task, projectModel);
        } catch (SQLException sqle) {
            throw new PluginException(sqle.getMessage(), sqle.getSQLState(), sqle.getErrorCode());
        } catch (Exception e) {
            throw new PluginException(e.getMessage());
        }

        task.setTaskStatus(task.done);
    }
}
