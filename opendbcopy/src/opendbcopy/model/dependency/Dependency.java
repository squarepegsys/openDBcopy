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
package opendbcopy.model.dependency;

import opendbcopy.config.XMLTags;

import opendbcopy.model.ProjectModel;

import opendbcopy.model.exception.DependencyNotSolvableException;
import opendbcopy.model.exception.MissingAttributeException;
import opendbcopy.model.exception.MissingElementException;
import opendbcopy.model.exception.UnsupportedAttributeValueException;

import org.jdom.Element;

import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class Dependency {
    private static final int    MAX_NUMBER_RECURSIONS = 500;
    private static final String ROOT_NODE = "root";
    private ProjectModel        projectModel;
    private Element             db_element;
    private HashMap             unsortedNodes;
    private TreeMap             sortedNodes;
    private Node                rootNode;
    private int                 nbrTables = 0;

    /**
     * Creates a new Dependency object.
     *
     * @param projectModel DOCUMENT ME!
     * @param db_element DOCUMENT ME!
     *
     * @throws DependencyNotSolvableException DOCUMENT ME!
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws UnsupportedAttributeValueException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     */
    public Dependency(ProjectModel projectModel,
                      Element      db_element) throws DependencyNotSolvableException, MissingAttributeException, UnsupportedAttributeValueException, MissingElementException {
        this.projectModel      = projectModel;
        this.db_element        = db_element;
        this.unsortedNodes     = new HashMap();
        this.sortedNodes       = new TreeMap();

        removeOldProcessOrderAttributes();
        setupRootNode();
        traverseDependencies();
    }

    /**
     * DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws UnsupportedAttributeValueException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     */
    public final void setProcessOrder() throws MissingAttributeException, UnsupportedAttributeValueException, MissingElementException {
        Node     table = null;
        Iterator itSortedTables = sortedNodes.values().iterator();

        int      processOrder = 0;

        while (itSortedTables.hasNext()) {
            table = (Node) itSortedTables.next();

            if (projectModel.getDbMode() == projectModel.SINGLE_MODE) {
                projectModel.getSourceTable(table.getName()).setAttribute(XMLTags.PROCESS_ORDER, Integer.toString(processOrder));
            } else {
                projectModel.getMappingDestinationTable(table.getName()).setAttribute(XMLTags.PROCESS_ORDER, Integer.toString(processOrder));
            }

            processOrder++;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws UnsupportedAttributeValueException DOCUMENT ME!
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     */
    private void removeOldProcessOrderAttributes() throws UnsupportedAttributeValueException, MissingAttributeException, MissingElementException {
        Iterator itTables;
        Element  table;

        if (projectModel.getDbMode() == projectModel.DUAL_MODE) {
            itTables = projectModel.getMappingTables().iterator();

            while (itTables.hasNext()) {
                table = (Element) itTables.next();

                if (table.getAttributeValue(XMLTags.PROCESS_ORDER) != null) {
                    table.removeAttribute(XMLTags.PROCESS_ORDER);
                }
            }
        } else {
            itTables = projectModel.getSourceTables().iterator();

            while (itTables.hasNext()) {
                table = (Element) itTables.next();

                if (table.getAttributeValue(XMLTags.PROCESS_ORDER) != null) {
                    table.removeAttribute(XMLTags.PROCESS_ORDER);
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws DependencyNotSolvableException DOCUMENT ME!
     * @throws UnsupportedAttributeValueException DOCUMENT ME!
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     */
    private void traverseDependencies() throws DependencyNotSolvableException, UnsupportedAttributeValueException, MissingAttributeException, MissingElementException {
        this.nbrTables = getNbrTables();

        addNodesToRootWithNoParentRelation();

        // do not continue if all tables do not have parent relations
        if (getNbrTables() > sortedNodes.size()) {
            readUnsortedNodes();

            boolean jobDone = false;
            int     nbrLoops = 0;

            jobDone = completeTheGame();

            while (!jobDone && (nbrLoops < MAX_NUMBER_RECURSIONS)) {
                jobDone = completeTheGame();
                nbrLoops++;
            }

            if (nbrLoops == MAX_NUMBER_RECURSIONS) {
                throw new DependencyNotSolvableException(unsortedNodes, sortedNodes, nbrLoops, "Tried to resolve foreign key dependencies but cannot complete providing a final list of sorted tables. Check your foreign keys (referential integrity) for loops!");
            }
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void setupRootNode() {
        rootNode = new Node(ROOT_NODE);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws UnsupportedAttributeValueException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     */
    private void addNodesToRootWithNoParentRelation() throws MissingAttributeException, UnsupportedAttributeValueException, MissingElementException {
        Iterator itTables = db_element.getChildren(XMLTags.TABLE).iterator();
        Element  table = null;
        String   tableName = "";

        while (itTables.hasNext()) {
            table         = (Element) itTables.next();
            tableName     = table.getAttributeValue(XMLTags.NAME);

            // check if element needs to be processed
            if (projectModel.getDbMode() == projectModel.DUAL_MODE) {
                Element mappingTable = projectModel.getMappingDestinationTable(tableName);

                if (mappingTable != null) {
                    if (Boolean.valueOf(mappingTable.getAttributeValue(XMLTags.PROCESS)).booleanValue()) {
                        if (getNbrImportedKeys(table) == 0) {
                            Node node = new Node(tableName);
                            rootNode.addChild(node);
                            sortedNodes.put(new Integer(sortedNodes.size()), node);
                        } else {
                            unsortedNodes.put(tableName, new Node(tableName));
                        }
                    }
                }
            } else {
                if (Boolean.valueOf(table.getAttributeValue(XMLTags.PROCESS)).booleanValue()) {
                    if (getNbrImportedKeys(table) == 0) {
                        Node node = new Node(tableName);
                        rootNode.addChild(node);
                        sortedNodes.put(new Integer(sortedNodes.size()), node);
                    } else {
                        unsortedNodes.put(tableName, new Node(tableName));
                    }
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void readUnsortedNodes() {
        if (unsortedNodes.size() > 0) {
            Node     node = null;
            Element  table = null;

            Iterator itUnsortedTables = unsortedNodes.values().iterator();

            while (itUnsortedTables.hasNext()) {
                node      = (Node) itUnsortedTables.next();
                table     = getTable(node.getName());

                Iterator itImportedKeys = table.getChildren(XMLTags.IMPORTED_KEY).iterator();

                while (itImportedKeys.hasNext()) {
                    node.addParent(new Node(((Element) itImportedKeys.next()).getAttributeValue(XMLTags.PKTABLE_NAME)));
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private boolean completeTheGame() {
        if (unsortedNodes.size() > 0) {
            Node     node = null;
            HashMap  parents = null;
            int      parentCounter = 0;

            Iterator itNodes = unsortedNodes.values().iterator();

            while (itNodes.hasNext()) {
                node     = (Node) itNodes.next();

                parents = node.getParents();

                if (parents != null) {
                    Iterator  itParents = parents.values().iterator();

                    boolean[] contains_parent = new boolean[parents.size()];
                    parentCounter = 0;

                    while (itParents.hasNext()) {
                        Node parent = (Node) itParents.next();

                        if (sortedNodes.containsValue(parent)) {
                            contains_parent[parentCounter] = true;
                        } else {
                            contains_parent[parentCounter] = false;
                        }

                        parentCounter++;
                    }

                    boolean all_parents_already_in_list = true;

                    // if all parents are already within sortedNodes, this child can also be added to sortedNodes
                    for (int i = 0; i < parents.size(); i++) {
                        if (all_parents_already_in_list) {
                            all_parents_already_in_list = contains_parent[i];
                        }
                    }

                    if (all_parents_already_in_list) {
                        sortedNodes.put(new Integer(sortedNodes.size()), node);
                    }
                }
            }

            removeSortedNodesFromUnsortedNodes();
        }

        if (unsortedNodes.size() == 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void removeSortedNodesFromUnsortedNodes() {
        Node     node = null;
        Iterator itSortedNodes = sortedNodes.values().iterator();

        while (itSortedNodes.hasNext()) {
            node = (Node) itSortedNodes.next();

            if (unsortedNodes.containsValue(node)) {
                unsortedNodes.remove(node.getName());
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param tableName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private Element getTable(String tableName) {
        Element  table = null;

        Iterator itTables = db_element.getChildren(XMLTags.TABLE).iterator();

        while (itTables.hasNext()) {
            table = (Element) itTables.next();

            if (table.getAttributeValue(XMLTags.NAME).compareTo(tableName) == 0) {
                return table;
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws UnsupportedAttributeValueException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     */
    private int getNbrTables() throws MissingAttributeException, UnsupportedAttributeValueException, MissingElementException {
        int      nbrTables = 0;
        Iterator itTables;

        if (projectModel.getDbMode() == projectModel.DUAL_MODE) {
            itTables = projectModel.getMappingTables().iterator();

            while (itTables.hasNext()) {
                if (Boolean.valueOf(((Element) itTables.next()).getAttributeValue(XMLTags.PROCESS)).booleanValue()) {
                    nbrTables++;
                }
            }
        } else {
            itTables = projectModel.getSourceTables().iterator();

            while (itTables.hasNext()) {
                if (Boolean.valueOf(((Element) itTables.next()).getAttributeValue(XMLTags.PROCESS)).booleanValue()) {
                    nbrTables++;
                }
            }
        }

        return nbrTables;
    }

    /**
     * DOCUMENT ME!
     *
     * @param table DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private int getNbrImportedKeys(Element table) {
        return table.getChildren(XMLTags.IMPORTED_KEY).size();
    }

    /**
     * DOCUMENT ME!
     *
     * @param table DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private int getNbrExportedKeys(Element table) {
        return table.getChildren(XMLTags.EXPORTED_KEY).size();
    }
}
