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
import com.jcraft.jsch.*;

import java.awt.*;
import java.awt.event.*;

import java.io.*;

import javax.swing.*;


/**
 * class description
 *
 * @author  Anthony Smith
 * @version $Revision$
 */
public class ScpTo {
    /**
     * DOCUMENT ME!
     *
     * @param arg DOCUMENT ME!
     */
    public static void main(String[] arg) {
        if (arg.length != 2) {
            System.err.println("usage: java ScpTo file1 user@remotehost:file2");
            System.exit(-1);
        }

        try {
            String lfile = arg[0];
            String user = arg[1].substring(0, arg[1].indexOf('@'));
            arg[1] = arg[1].substring(arg[1].indexOf('@') + 1);

            String  host = arg[1].substring(0, arg[1].indexOf(':'));
            String  rfile = arg[1].substring(arg[1].indexOf(':') + 1);

            JSch    jsch = new JSch();
            Session session = jsch.getSession(user, host, 22);

            // username and password will be given via UserInfo interface.
            UserInfo ui = new MyUserInfo();
            session.setUserInfo(ui);
            session.connect();

            // exec 'scp -t rfile' remotely
            String  command = "scp -t " + rfile;
            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);

            // get I/O streams for remote scp
            OutputStream out = channel.getOutputStream();
            InputStream  in = channel.getInputStream();

            channel.connect();

            byte[] tmp = new byte[1];

            if (checkAck(in) != 0) {
                System.exit(0);
            }

            // send "C0644 filesize filename", where filename should not include '/'
            int filesize = (int) (new File(lfile)).length();
            command = "C0644 " + filesize + " ";

            if (lfile.lastIndexOf('/') > 0) {
                command += lfile.substring(lfile.lastIndexOf('/') + 1);
            } else {
                command += lfile;
            }

            command += "\n";
            out.write(command.getBytes());
            out.flush();

            if (checkAck(in) != 0) {
                System.exit(0);
            }

            // send a content of lfile
            FileInputStream fis = new FileInputStream(lfile);
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
                System.exit(0);
            }

            System.exit(0);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    static int checkAck(InputStream in) throws IOException {
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
                System.out.print(sb.toString());
            }

            if (b == 2) { // fatal error
                System.out.print(sb.toString());
            }
        }

        return b;
    }

    /**
     * class description
     *
     * @author  Anthony Smith
     * @version $Revision$
     */
    public static class MyUserInfo implements UserInfo {
        String     passwd;
        JTextField passwordField = (JTextField) new JPasswordField(20);

        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public String getPassword() {
            return passwd;
        }

        /**
         * DOCUMENT ME!
         *
         * @param str DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public boolean promptYesNo(String str) {
            Object[] options = { "yes", "no" };
            int      foo = JOptionPane.showOptionDialog(null, str, "Warning", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);

            return foo == 0;
        }

        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public String getPassphrase() {
            return null;
        }

        /**
         * DOCUMENT ME!
         *
         * @param message DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public boolean promptPassphrase(String message) {
            return true;
        }

        /**
         * DOCUMENT ME!
         *
         * @param message DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public boolean promptPassword(String message) {
            Object[] ob = { passwordField };
            int      result = JOptionPane.showConfirmDialog(null, ob, message, JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                passwd = passwordField.getText();

                return true;
            } else {
                return false;
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param message DOCUMENT ME!
         */
        public void showMessage(String message) {
            JOptionPane.showMessageDialog(null, message);
        }
    }
}
