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
package opendbcopy.email;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

import sun.net.smtp.SmtpClient;

import java.io.IOException;
import java.io.PrintStream;

import java.util.StringTokenizer;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class MailAppender extends AppenderSkeleton {
    private static Logger logger = Logger.getLogger(MailAppender.class.getName());
    private String        fromEmail;
    private String        toEmail;
    private String        mailServer;
    private String        messageText;
    private String        dateTime;
    private String        type;
    private String        sourceName;
    private String        sourceLine;

    /**
     * DOCUMENT ME!
     *
     * @param event DOCUMENT ME!
     */
    public final void append(LoggingEvent event) {
        messageText = layout.format(event);

        retrieveMessageItems();

        if ((mailServer.length() > 0) && (toEmail.length() > 0)) {
            sendEmail();
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void retrieveMessageItems() {
        StringTokenizer st = new StringTokenizer(messageText, "&");

        mailServer      = st.nextToken().trim();
        fromEmail       = st.nextToken().trim();
        toEmail         = st.nextToken().trim();
        dateTime        = st.nextToken().trim();
        type            = st.nextToken().trim();
        sourceName      = st.nextToken().trim();
        sourceLine      = st.nextToken().trim();
        messageText     = st.nextToken().trim();
    }

    /**
     * DOCUMENT ME!
     */
    private void sendEmail() {
        try {
            SmtpClient  client;
            PrintStream message;

            client = new SmtpClient(mailServer);
            client.from(fromEmail);
            client.to(toEmail);
            message = client.startMessage();
            message.println("From: " + fromEmail);
            message.println("To: " + toEmail);
            message.println("Subject: " + "opendbcopy " + type + ": " + messageText + "\n");
            message.println(type + ": " + messageText + "\n\n");
            message.println("Launched by " + sourceName + " " + sourceLine + "\n");
            message.println("Message created at " + dateTime + " by opendbcopy\n");
            client.closeServer();
        } catch (IOException e) {
            logger.error("Cannot send email " + e.toString());
        }
    }

    /**
     * DOCUMENT ME!
     */
    public final void close() {
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final boolean requiresLayout() {
        return true;
    }
}
