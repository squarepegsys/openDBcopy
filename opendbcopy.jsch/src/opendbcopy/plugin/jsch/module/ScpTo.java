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
package opendbcopy.plugin.jsch.module;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import opendbcopy.config.XMLTags;
import opendbcopy.plugin.jsch.config.JschXMLTags;
import opendbcopy.plugin.model.exception.MissingAttributeException;
import opendbcopy.plugin.model.exception.MissingElementException;
import opendbcopy.plugin.model.exception.PluginException;
import opendbcopy.util.InputOutputHelper;

import org.apache.log4j.Logger;
import org.jdom.Element;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public final class ScpTo {
    /**
     * DOCUMENT ME!
     *
     * @param logger DOCUMENT ME!
     * @param conf DOCUMENT ME!
     * @param input DOCUMENT ME!
     * @param output DOCUMENT ME!
     *
     * @throws PluginException DOCUMENT ME!
     */
    public static final void execute(Logger  logger,
                                     Element conf,
                                     Element input,
                                     Element output) throws PluginException {
        File[]    inputFiles = null;
        URL[]    outputURLs = null;
        ArrayList remoteURLs = new ArrayList();

        try {
            inputFiles = retrieveInput(conf, input);

            String remotePath = retrieveOutputPath(conf);
            String host = retrieveHost(conf);
            int    port = retrievePort(conf);
            String userName = retrieveUserName(conf);
            String password = retrievePassword(conf);
            String fileListIdentifier = retrieveFileListIdentifier(conf);

            if ((inputFiles != null) && (inputFiles.length > 0)) {
                for (int i = 0; i < inputFiles.length; i++) {
                    File inputFile = inputFiles[i];
                    remoteURLs.add(new URL("file", host, -1, remotePath + "/" + inputFile.getName()));
                    secureCopyDir(logger, host, port, userName, password, inputFile, remotePath);
                }
            }
            
            outputURLs     = new URL[remoteURLs.size()];            
            outputURLs    = (URL[]) remoteURLs.toArray(outputURLs);

            Element outputElement = InputOutputHelper.createURLListElement(outputURLs);
            output.addContent(outputElement);
            
        } catch (MissingAttributeException e) {
            throw new PluginException(e);
        } catch (MissingElementException e) {
            throw new PluginException(e);
        } catch (FileNotFoundException e) {
            throw new PluginException(e);
        } catch (IOException e) {
            throw new PluginException(e);
        } catch (Exception e) {
            throw new PluginException(e);
        }
    }

    /**
     * Only works if the remote directory already exists or is created first, which it isn't at the moment
     *
     * @param logger DOCUMENT ME!
     * @param host DOCUMENT ME!
     * @param port DOCUMENT ME!
     * @param userName DOCUMENT ME!
     * @param password DOCUMENT ME!
     * @param sourceFile DOCUMENT ME!
     * @param remotePath DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws JSchException DOCUMENT ME!
     * @throws PluginException DOCUMENT ME!
     */
    private static void secureCopyDir(Logger logger,
                                      String host,
                                      int    port,
                                      String userName,
                                      String password,
                                      File   sourceFile,
                                      String remotePath) throws IOException, JSchException, PluginException {
        if (sourceFile.isDirectory()) {
            File[] fileList = sourceFile.listFiles();

            for (int i = 0; i < fileList.length; i++) {
                if (fileList[i].isDirectory()) {
                	secureCopyDir(logger, host, port, userName, password, fileList[i], remotePath + "/" + sourceFile.getName() + "/" + fileList[i].getName());
                } else if (fileList[i].isFile()) {
                	secureCopyFile(logger, host, port, userName, password, fileList[i], remotePath + "/" + sourceFile.getName() + "/" + fileList[i].getName());
                }
            }
        } else {
            secureCopyFile(logger, host, port, userName, password, sourceFile, remotePath + "/" + sourceFile.getName());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param logger DOCUMENT ME!
     * @param host DOCUMENT ME!
     * @param port DOCUMENT ME!
     * @param userName DOCUMENT ME!
     * @param password DOCUMENT ME!
     * @param sourceFile DOCUMENT ME!
     * @param remotePath DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws JSchException DOCUMENT ME!
     * @throws PluginException DOCUMENT ME!
     */
    private static void secureCopyFile(Logger logger,
                                       String host,
                                       int    port,
                                       String userName,
                                       String password,
                                       File   sourceFile,
                                       String remotePath) throws IOException, JSchException, PluginException {
        JSch    jsch = new JSch();
        Session session = jsch.getSession(userName, host, port);

        session.setHost(host);
        session.setUserName(userName);
        session.setPassword(password);
        session.connect();

        // exec 'scp -t rfile' remotely
        String  command = "scp -t " + remotePath;
        Channel channel = session.openChannel("exec");
        ((ChannelExec) channel).setCommand(command);

        // get I/O streams for remote scp
        OutputStream out = channel.getOutputStream();
        InputStream  in = channel.getInputStream();

        channel.connect();

        byte[] tmp = new byte[1];

        if (checkAck(in) != 0) {
            throw new PluginException("Acknowledgment error");
        }

        // send "C0644 filesize filename", where filename should not include '/'
        int filesize = (int) (sourceFile).length();
        command = "C0644 " + filesize + " ";

        command += sourceFile.getName();

        command += "\n";
        out.write(command.getBytes());
        out.flush();

        if (checkAck(in) != 0) {
            throw new PluginException("Acknowledgment error");
        }

        // send a content of sourceFile
        FileInputStream fis = new FileInputStream(sourceFile);
        byte[]          buf = new byte[1024];

        while (true) {
            int len = fis.read(buf, 0, buf.length);

            if (len <= 0) {
                break;
            }

            out.write(buf, 0, len);
            out.flush();
        }

        // send '\0'
        buf[0] = 0;
        out.write(buf, 0, 1);
        out.flush();

        if (checkAck(in) != 0) {
            throw new PluginException("Acknowledgment error");
        }

        logger.info("copied local file " + sourceFile.getAbsolutePath() + " to " + host + remotePath);
    }

    /**
     * DOCUMENT ME!
     *
     * @param in DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws PluginException DOCUMENT ME!
     */
    private static int checkAck(InputStream in) throws IOException, PluginException {
        int b = in.read();

        // b may be 0 for success,
        //          1 for error,
        //          2 for fatal error,
        //          -1
        if (b == 0) {
            return b;
        }

        if (b == -1) {
            return b;
        }

        if ((b == 1) || (b == 2)) {
            StringBuffer sb = new StringBuffer();
            int          c;

            do {
                c = in.read();
                sb.append((char) c);
            } while (c != '\n');

            if (b == 1) { // error
                throw new PluginException("Acknowledgment error: " + sb.toString());
            }

            if (b == 2) { // fatal error
                throw new PluginException("Acknowledgment fatal error: " + sb.toString());
            }
        }

        return b;
    }

    /**
     * DOCUMENT ME!
     *
     * @param conf DOCUMENT ME!
     * @param input DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     * @throws FileNotFoundException DOCUMENT ME!
     * @throws PluginException DOCUMENT ME!
     */
    private static File[] retrieveInput(Element conf,
                                        Element input) throws MissingAttributeException, MissingElementException, FileNotFoundException, PluginException {
        ArrayList inputFiles = new ArrayList();

        Element   inputConf = conf.getChild(XMLTags.INPUT);

        if (inputConf == null) {
            throw new PluginException(new MissingElementException(new Element(XMLTags.INPUT), XMLTags.INPUT));
        }

        // check what shall be taken as input
        String inputConfSelection = inputConf.getChild(XMLTags.FILE_DIR_FILELISTS_SELECTION).getAttributeValue(XMLTags.VALUE);

        if ((inputConfSelection != null) && (inputConfSelection.length() > 0)) {
            if (inputConfSelection.compareToIgnoreCase(XMLTags.FILE) == 0) {
                inputConfSelection = inputConf.getChild(XMLTags.FILE).getAttributeValue(XMLTags.VALUE);

                if ((inputConfSelection == null) || (inputConfSelection.length() == 0)) {
                    throw new PluginException("Missing file specified in input configuration");
                }

                // try to retrieve file and check if it exists
                File inputFile = new File(inputConfSelection);

                if (!inputFile.exists()) {
                    throw new PluginException("File " + inputFile.getAbsolutePath() + " does not exist!");
                } else {
                    inputFiles.add(inputFile);
                }
            } else if (inputConfSelection.compareTo(XMLTags.DIR) == 0) {
                inputConfSelection = inputConf.getChild(XMLTags.DIR).getAttributeValue(XMLTags.VALUE);

                if ((inputConfSelection == null) || (inputConfSelection.length() == 0)) {
                    throw new PluginException("Missing directory specified in input configuration");
                }

                // try to retrieve file and check if it exists
                File inputFile = new File(inputConfSelection);

                if (!inputFile.exists()) {
                    throw new PluginException("Directory " + inputFile.getAbsolutePath() + " does not exist!");
                } else {
                    inputFiles.add(inputFile);
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

                List inputFileLists = input.getChildren(XMLTags.FILELIST);

                if ((inputFileLists != null) && (inputFileLists.size() > 0)) {
                    Iterator itInputFileLists = inputFileLists.iterator();

                    while (itInputFileLists.hasNext()) {
                        Element fileList = (Element) itInputFileLists.next();
                        inputFiles.addAll(InputOutputHelper.getFileList(fileList));
                    }
                }
            } else {
                throw new PluginException("You must specify an input type");
            }
        }

        if (inputFiles != null) {
            File[] inputFilesArray = new File[inputFiles.size()];

            return (File[]) inputFiles.toArray(inputFilesArray);
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param conf DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws PluginException DOCUMENT ME!
     */
    private static String retrieveOutputPath(Element conf) throws PluginException {
        if ((conf.getChild(JschXMLTags.REMOTE_DIR) != null) && (conf.getChild(JschXMLTags.REMOTE_DIR).getAttributeValue(XMLTags.VALUE).length() > 0)) {
            return conf.getChild(JschXMLTags.REMOTE_DIR).getAttributeValue(XMLTags.VALUE);
        } else {
            throw new PluginException("Missing output path for remote directory");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param conf DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws PluginException DOCUMENT ME!
     */
    private static String retrieveHost(Element conf) throws PluginException {
        if ((conf.getChild(JschXMLTags.HOST) != null) && (conf.getChild(JschXMLTags.HOST).getAttributeValue(XMLTags.VALUE).length() > 0)) {
            return conf.getChild(JschXMLTags.HOST).getAttributeValue(XMLTags.VALUE);
        } else {
            throw new PluginException("Missing Host Name");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param conf DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws PluginException DOCUMENT ME!
     */
    private static int retrievePort(Element conf) throws PluginException {
        if ((conf.getChild(JschXMLTags.PORT) != null) && (conf.getChild(JschXMLTags.PORT).getAttributeValue(XMLTags.VALUE).length() > 0)) {
            return Integer.parseInt(conf.getChild(JschXMLTags.PORT).getAttributeValue(XMLTags.VALUE));
        } else {
            throw new PluginException("Missing Port Number");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param conf DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws PluginException DOCUMENT ME!
     */
    private static String retrieveUserName(Element conf) throws PluginException {
        if ((conf.getChild(XMLTags.USERNAME) != null) && (conf.getChild(XMLTags.USERNAME).getAttributeValue(XMLTags.VALUE).length() > 0)) {
            return conf.getChild(XMLTags.USERNAME).getAttributeValue(XMLTags.VALUE);
        } else {
            throw new PluginException("Missing User Name");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param conf DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws PluginException DOCUMENT ME!
     */
    private static String retrievePassword(Element conf) throws PluginException {
        if (conf.getChild(XMLTags.PASSWORD) != null) {
            return conf.getChild(XMLTags.PASSWORD).getAttributeValue(XMLTags.VALUE);
        } else {
            throw new PluginException("Missing Password");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param conf DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws PluginException DOCUMENT ME!
     */
    private static String retrieveFileListIdentifier(Element conf) throws PluginException {
        if ((conf.getChild(XMLTags.OUTPUT).getChild(XMLTags.FILELIST).getAttributeValue(XMLTags.VALUE) != null) && (conf.getChild(XMLTags.OUTPUT).getChild(XMLTags.FILELIST).getAttributeValue(XMLTags.VALUE).length() > 0)) {
            return conf.getChild(XMLTags.OUTPUT).getChild(XMLTags.FILELIST).getAttributeValue(XMLTags.VALUE);
        } else {
            throw new PluginException("Missing FileList identifier");
        }
    }
}
