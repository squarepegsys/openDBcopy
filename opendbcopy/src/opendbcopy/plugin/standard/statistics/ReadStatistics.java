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

import opendbcopy.model.ProjectModel;

import opendbcopy.plugin.ExecuteSkeleton;

import opendbcopy.task.TaskExecute;

import org.apache.log4j.Logger;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class ReadStatistics extends ExecuteSkeleton {
    private static Logger logger = Logger.getLogger(ReadStatistics.class.getName());

    /**
     * Creates a new CopyMapping object.
     *
     * @param task DOCUMENT ME!
     * @param projectModel DOCUMENT ME!
     */
    public ReadStatistics(TaskExecute  task,
                          ProjectModel projectModel) {
        try {
            // execute unprocessed tables
            doExecute(task, projectModel);
        }
        // hummm ... check error log
         catch (Exception e) {
            logger.error(e.toString());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param task DOCUMENT ME!
     * @param projectModel DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public final void doExecute(TaskExecute  task,
                                ProjectModel projectModel) throws Exception {
        StatReader.readStatistics(task, projectModel);

        task.setTaskStatus(task.done);
    }
}
