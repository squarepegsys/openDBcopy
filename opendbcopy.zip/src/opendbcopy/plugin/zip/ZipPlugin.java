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
package opendbcopy.plugin.zip;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import opendbcopy.config.XMLTags;
import opendbcopy.controller.MainController;
import opendbcopy.plugin.model.DynamicPluginThread;
import opendbcopy.plugin.model.Model;
import opendbcopy.plugin.model.exception.MissingAttributeException;
import opendbcopy.plugin.model.exception.MissingElementException;
import opendbcopy.plugin.model.exception.PluginException;
import opendbcopy.util.InputOutputHelper;

import org.jdom.Element;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class ZipPlugin extends DynamicPluginThread {
    private static final String outputFileType = "zip";
    private Element             conf;
    private Element             input;
    private Element             output;
    private Model               model;
    private File                inputFile; // may be null in case of filelists
    private List                inputFilelists;
    private File                outputFile;
    private FileOutputStream    fos = null;
    private ZipOutputStream     zos = null;

    /**
     * Creates a new ZipPluign object.
     *
     * @param controller DOCUMENT ME!
     * @param baseModel DOCUMENT ME!
     *
     * @throws PluginException DOCUMENT ME!
     */
    public ZipPlugin(MainController controller,
                     Model          baseModel) throws PluginException {
        super(controller, baseModel);
        model = baseModel;
    }

    /**
     * DOCUMENT ME!
     *
     * @throws PluginException DOCUMENT ME!
     */
    protected final void setUp() throws PluginException {
        String inputConfSelection = null;

        conf     = model.getConf();

        // get input
        input = model.getInput();

        // create new output if it does not yet exist
        if (model.getOutput() == null) {
            output = new Element(XMLTags.OUTPUT);
            model.setOutput(output);
        } else {
            output = model.getOutput();
        }

        Element inputConf = conf.getChild(XMLTags.INPUT);
        Element outputConf = conf.getChild(XMLTags.OUTPUT);

        if (inputConf == null) {
            throw new PluginException(new MissingElementException(new Element(XMLTags.INPUT), XMLTags.INPUT));
        }

        if (outputConf == null) {
            throw new PluginException(new MissingElementException(new Element(XMLTags.OUTPUT), XMLTags.OUTPUT));
        }

        // first check that mandator output path / filename is set
        // read output file path / filename
        if (outputConf.getChild(XMLTags.FILE).getAttributeValue(XMLTags.VALUE).length() > 0) {
            String pathFilename = outputConf.getChild(XMLTags.FILE).getAttributeValue(XMLTags.VALUE);

            int    indexFileExtension = pathFilename.indexOf(outputFileType);

            // file does not yet contain extension
            if (indexFileExtension != (pathFilename.length() - outputFileType.length())) {
                pathFilename = pathFilename + "." + outputFileType;
            }

            outputFile = new File(pathFilename);
        } else {
            throw new PluginException("Missing output path / filename for compressed file");
        }

        // check what shall be taken as input
        inputConfSelection = inputConf.getChild(XMLTags.FILE_DIR_FILELISTS_SELECTION).getAttributeValue(XMLTags.VALUE);

        if ((inputConfSelection != null) && (inputConfSelection.length() > 0)) {
            if (inputConfSelection.compareToIgnoreCase(XMLTags.FILE) == 0) {
                inputConfSelection = inputConf.getChild(XMLTags.FILE).getAttributeValue(XMLTags.VALUE);

                if ((inputConfSelection == null) || (inputConfSelection.length() == 0)) {
                    throw new PluginException("Missing file specified in input configuration");
                }

                // try to retrieve file and check if it exists
                inputFile = new File(inputConfSelection);

                if (!inputFile.exists()) {
                    throw new PluginException("File " + inputFile.getAbsolutePath() + " does not exist!");
                }
            } else if (inputConfSelection.compareTo(XMLTags.DIR) == 0) {
                inputConfSelection = inputConf.getChild(XMLTags.DIR).getAttributeValue(XMLTags.VALUE);

                if ((inputConfSelection == null) || (inputConfSelection.length() == 0)) {
                    throw new PluginException("Missing directory specified in input configuration");
                }

                // try to retrieve file and check if it exists
                inputFile = new File(inputConfSelection);

                if (!inputFile.exists()) {
                    throw new PluginException("Directory " + inputFile.getAbsolutePath() + " does not exist!");
                }
            }
            // input is provided by filelists from former plugin in its output
            else if (inputConfSelection.compareTo(XMLTags.FILELISTS) == 0) {
                if (input == null) {
                    throw new PluginException("Missing output from former plugin!");
                }

                if (input.getChildren(XMLTags.FILELIST).size() == 0) {
                    throw new PluginException("Missing output filelists from former plugin!");
                }

                inputFilelists = input.getChildren(XMLTags.FILELIST);
            } else {
                throw new PluginException("You must specify an input type");
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws PluginException DOCUMENT ME!
     */
    public final void execute() throws PluginException {
        // set output
        Element outputFileElement = null;

        try {
            fos     = new FileOutputStream(outputFile.getAbsolutePath());
            zos     = new ZipOutputStream(fos);

            // zip filelist(s) from input of former plugin
            if (inputFilelists != null) {
                Iterator itFileLists = inputFilelists.iterator();

                // only takes the first filelist -> future versions shall implement something nicer regarding filelists
                if (itFileLists.hasNext()) {
                    Element   filelist = (Element) itFileLists.next();
                    String    dir = filelist.getAttributeValue(XMLTags.DIR);
                    ArrayList files = InputOutputHelper.getFileList(filelist);
                    zipFiles(files, dir);
                    outputFileElement = InputOutputHelper.createFileElement(inputFile);
                }
            }
            // zip file or directory not from input
            else {
                zipFileOrDirectory(inputFile);

                // create output entry
                if (inputFile.isFile()) {
                    outputFileElement = InputOutputHelper.createFileElement(inputFile);
                } else {
                    outputFileElement = InputOutputHelper.createDirElement(inputFile);
                }
            }

            zos.flush();
            zos.close();
            fos.close();

            output.addContent(outputFileElement);

            logger.info("Created zip file " + outputFile.getAbsolutePath());
        } catch (MissingAttributeException e) {
            throw new PluginException(e);
        } catch (MissingElementException e) {
            throw new PluginException(e);
        } catch (FileNotFoundException e) {
            throw new PluginException(e);
        } catch (IOException e) {
            throw new PluginException(e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param files DOCUMENT ME!
     * @param dir DOCUMENT ME!
     *
     * @throws FileNotFoundException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     */
    private void zipFiles(ArrayList files,
                          String    dir) throws FileNotFoundException, IOException {
        for (int i = 0; i < files.size(); i++) {
            zipFileEntry((File) files.get(i), dir);
        }
    }

    /**
     * file may be a single file or directory
     *
     * @param file DOCUMENT ME!
     *
     * @throws FileNotFoundException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     */
    private void zipFileOrDirectory(File file) throws FileNotFoundException, IOException {
        if (file.isDirectory()) {
            String dirRoot = file.getName();
            zipDir(file, dirRoot);
        } else {
            zipFileEntry(file, null);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param fileIn DOCUMENT ME!
     * @param dirAppender DOCUMENT ME!
     *
     * @throws FileNotFoundException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     */
    private void zipDir(File   fileIn,
                        String dirAppender) throws FileNotFoundException, IOException {
        if (fileIn.exists() == true) {
            if (fileIn.isDirectory() == true) {
                File[] fileList = fileIn.listFiles();

                for (int i = 0; i < fileList.length; i++) {
                    if (fileList[i].isDirectory()) {
                        zipDir(fileList[i], dirAppender + MainController.fileSep + fileList[i].getName());
                    } else if (fileList[i].isFile()) {
                        zipFileEntry(fileList[i], dirAppender);
                    }
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param zipEntryFile DOCUMENT ME!
     * @param dirAppender DOCUMENT ME!
     *
     * @throws FileNotFoundException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     */
    private void zipFileEntry(File   zipEntryFile,
                              String dirAppender) throws FileNotFoundException, IOException {
        // Create a file input stream and a buffered input stream.
        FileInputStream     fis = new FileInputStream(zipEntryFile);
        BufferedInputStream bis = new BufferedInputStream(fis);

        // Create a Zip Entry and put it into the archive (no data yet).
        ZipEntry fileEntry = null;

        if (dirAppender != null) {
            fileEntry = new ZipEntry(dirAppender + MainController.fileSep + zipEntryFile.getName());
        } else {
            fileEntry = new ZipEntry(zipEntryFile.getName());
        }

        zos.putNextEntry(fileEntry);

        // Create a byte array object named data and declare byte count variable.
        byte[] data = new byte[1024];
        int    byteCount;

        // Create a loop that reads from the buffered input stream and writes
        // to the zip output stream until the bis has been entirely read.
        while ((byteCount = bis.read(data, 0, 1024)) > -1) {
            zos.write(data, 0, byteCount);
        }
    }
}
