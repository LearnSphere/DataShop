/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

/**
 * A utility to send e-mail. Used by both the extractors and servlets.
 *
 * @author Cindy Tipper
 * @version $Revision: 15738 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2018-12-04 16:35:34 -0500 (Tue, 04 Dec 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public final class MailUtils {

    /** Debug logging. */
    private static Logger logger = Logger.getLogger(MailUtils.class);

    /** E-mail host for CMU. */
    private static final String HOST = "relay.andrew.cmu.edu";

    /** Private constructor as this is a utility class. */
    private MailUtils() { }

    /**
     *  Get SMTP host.
     *  @return String SMTP host
     */
    public static String getSmtpHost() {
        if (DataShopInstance.getDatashopSmtpHost() != null) {
            return DataShopInstance.getDatashopSmtpHost();
        } else {
            return HOST;
        }
    }

    /** Default SMTP port. */
    private static final Integer PORT = 25;

    /** Default SSL SMTP port. */
    private static final Integer SSL_PORT = 465;

    /**
     * Get SMTP port.
     * @return Integer SMTP port
     */
    public static Integer getSmtpPort() {
        if (DataShopInstance.getDatashopSmtpPort() != null) {
            return DataShopInstance.getDatashopSmtpPort();
        } else if (DataShopInstance.getUseSslSmtp()) {
            return SSL_PORT;
        } else {
            return PORT;
        }
    }

    /**
     * Send email.
     * @param fromAddress the From: email address
     * @param toAddress the To: email address
     * @param replyToAddress the "Reply To:" email address
     * @param bccList the Bcc: email addresses
     * @param subject the subject of the email
     * @param message the content of the email
     */
    public static void sendEmail(String fromAddress, String toAddress, String replyToAddress,
                                 List<String> bccList, String subject, String message) {

        logger.info("sendEmail to " + toAddress +  " from " + fromAddress
                    + " with subject [" + subject + "] and message [" + message + "]");

        // Get the host name of the local machine so its easier/quicker to
        // understand where this email is coming from, then append it to the
        // subject and include it in the message. No-op for production.
        String hostNameForEmail = ServerNameUtils.getHostNameForEmail();
        if (!hostNameForEmail.equals("")) {
            subject = subject + " (" + hostNameForEmail + ")";
        }

        try {
            Message msg = createMessage();
            msg.setContent(message, "text/html");

            sendMessage(fromAddress, toAddress, replyToAddress, bccList, subject, msg);
        } catch (MessagingException exception) {
            logger.error("MessageException occurred.", exception);
        }
    }

    /**
     * Send error email.
     * @param source the class sending the error
     * @param fromAddress the From: email address
     * @param toAddress the To: email address
     * @param message the content of the email
     * @param throwable the error
     */
    public static void sendErrorEmail(String source, String fromAddress, String toAddress,
                                      String message, Throwable throwable) {

        String hostNameForEmail = ServerNameUtils.getHostNameForEmail();
        if (!hostNameForEmail.equals("")) {
            hostNameForEmail = "Host (" + hostNameForEmail + ") ";
        }
        message = source + ": " + hostNameForEmail + message;

        String subject = message;

        // Add throwable into to message.
        if (throwable != null) {
            String extraInfo = "\n" + stack2string(throwable);
            message = message + "\n" + new Date() + extraInfo;
        } else {
            message = message + "\n" + new Date();
        }

        try {
            Message msg = createMessage();
            msg.setText(message);

            // Send with null "Reply To" and BCC.
            sendMessage(fromAddress, toAddress, null, null, subject, msg);
        } catch (MessagingException exception) {
            logger.error("MessageException occurred.", exception);
        }
    }

    /**
     * Common code for creating MimeMessage.
     *
     * @return Message the message
     */
    private static Message createMessage() {
        // Create properties, set host, get session
        Properties props = new Properties();
        props.put("mail.smtp.host", getSmtpHost());

        if (DataShopInstance.getDatashopSmtpPort() != null) {
            props.setProperty("mail.smtp.socketFactory.port", String.valueOf(getSmtpPort()));
        }

        Session session = Session.getInstance(props);

        // If using secure SMTP, initialize session with appropriate properties.
        if (DataShopInstance.getUseSslSmtp()) {
            props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.setProperty("mail.smtp.socketFactory.fallback", "false");

            if ((DataShopInstance.getDatashopSmtpUser() != null) &&
                (DataShopInstance.getDatashopSmtpPassword() != null)) {

                props.put("mail.smtp.auth","true");

                session = Session.getDefaultInstance(props, new Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(DataShopInstance.getDatashopSmtpUser(),
                                                              DataShopInstance.getDatashopSmtpPassword());
                        }
                });
            }
        }

        // Instantiate a message.
        Message msg = new MimeMessage(session);

        return msg;
    }

    /**
     * Returns a string representation of the throwable's stack trace.
     * @param throwable the throwable that just occurred
     * @return a string representation of the stack trace
     */
    private static String stack2string(Throwable throwable) {
        try {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            throwable.printStackTrace(printWriter);
            return "------\r\n" + stringWriter.toString() + "------\r\n";
        } catch (Exception exception) {
            return "bad stack2string";
        }
    }

    /**
     * Send MIME message.
     * @param fromAddress the From: email address
     * @param toAddress the To: email address
     * @param replyToAddress the "Reply To:" email address
     * @param bccList the Bcc: email addresses
     * @param subject the subject of the email
     * @param msg the MIME message to be sent
     * @throws MessagingException exception
     */
    private static void sendMessage(String fromAddress, String toAddress, String replyToAddress,
                                    List<String> bccList, String subject, Message msg)
        throws MessagingException {
        // Set message attributes
        msg.setFrom(new InternetAddress(fromAddress));
        InternetAddress[] address = {new InternetAddress(toAddress)};
        msg.setRecipients(Message.RecipientType.TO, address);
        if (replyToAddress != null) {
            InternetAddress[] replyTo = {new InternetAddress(replyToAddress)};
            msg.setReplyTo(replyTo);
        }
        if (bccList != null) {
            for (String bcc : bccList) {
                if ((bcc == null) || (bcc.trim().length() == 0)) { continue; }
                InternetAddress[] bccAddr = {new InternetAddress(bcc)};
                msg.setRecipients(Message.RecipientType.BCC, bccAddr);
            }
        }
        msg.setSentDate(new Date());
        msg.setSubject(subject);

        //Send the e-mail message
        Transport.send(msg);
    }
}
