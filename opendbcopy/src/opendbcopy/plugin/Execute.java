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
package opendbcopy.plugin;

import opendbcopy.model.ProjectModel;

import opendbcopy.task.TaskExecute;


/**
 * interface description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public interface Execute {
    /**
     * DOCUMENT ME!
     *
     * @param task DOCUMENT ME!
     * @param model DOCUMENT ME!
     * @param datasourceSource DOCUMENT ME!
     * @param datasourceDestination DOCUMENT ME!
     * @param copyProperties DOCUMENT ME!
     *
     * @throws SQLException DOCUMENT ME!
     */
    public void doExecute(TaskExecute  task,
                          ProjectModel projectModel) throws Exception;
}
