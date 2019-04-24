/*
 * @(#)DiskImporter.java $Revision: 6410 $ $Date: 2010-11-04 15:42:54 -0400 (Thu, 04 Nov 2010) $
 *
 * Copyright (c) 2002-2003 Carnegie Mellon University.
 */
package edu.cmu.pslc.logging.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * OLI Logging class for importing XML files from disk.
 *
 * Important Note: This class has been cleaned up for our coding standards, but some of
 * the code is necessary though it is not clear how or why.  These lines are marked
 * with: "Not sure what this is for, but do not delete it."   The program will appear
 * to run successfully without them but nothing actually goes to the server.
 *
 * @version $Revision: 6410 $ $Date: 2010-11-04 15:42:54 -0400 (Thu, 04 Nov 2010) $
 * @author  Bill Jerome
 */
public class DiskImporter {
    /** URL. */
    private URL url;
    /** FileReader. */
    private FileReader infile = null;
    /** Exception. */
    private Exception lastException;

    /** Not sure what this is for, but do not delete it. */
    private Vector xmlBuffers;
    /** Not sure what this is for, but do not delete it. */
    private Boolean opened;

    /** Logger. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Default constructor will set a development server as a URL
     * and set up a file.
     */
    public DiskImporter() {

        // Not sure what this is for, but do not delete it.
        opened = Boolean.FALSE;

        /*
        try {
            url = new URL("http://olidev.ote.cmu.edu/log/server");
        } catch (MalformedURLException ex) {
            setLastError(ex);
        }
         */
        // Not sure what this is for, but do not delete it.
        xmlBuffers = new Vector();

    } // end constructor

    /**
     * The main method.
     * @param args the command line arguments
     * @throws IOException an IO exception
     */
    public static void main(String[] args) throws IOException {
        Logger logger = Logger.getLogger("edu.cmu.pslc.logging.util.DiskImporter");
        logger.info("DiskImporter starting...");
        if ((args.length == 0)
            || ((args.length == 1) & (args[0].equals("--help")))) {
            logger.info("Usage: DiskImporter [in file [url]]");
            return;
        }

        DiskImporter dI = new DiskImporter();

        String inputFileOrDir = null;

        if (args.length > 0) {
            inputFileOrDir = args[0];
            logger.info("File: " + inputFileOrDir);
        }

        if (args.length > 1) {
            logger.info("URL : " + args[1]);
            dI.setURL(args[1]);
        }

        if (inputFileOrDir != null) {
            File theFile = new File(inputFileOrDir);

            if (theFile.isDirectory()) {
                logger.info("Reading directory " + inputFileOrDir);
                File[] files = theFile.listFiles();
                for (int idx = 0; idx < files.length; idx++) {
                    String fileName = inputFileOrDir + File.separator + files[idx].getName();
                    logger.info("Sending file " + fileName);
                    dI.setInfile(fileName);
                    dI.send();
                }
            } else {
                String fileName = inputFileOrDir;
                logger.info("Sending file " + fileName);
                dI.setInfile(fileName);
                dI.send();
            }
        }

        if (dI.getLastError() != null) {
            logger.error("Last Error: " + dI.getLastError());
        }
    }

    /**
     * Set the URL for OLI logging servlet to log to.
     * @param  connURL  URL of the servlet
     * @return  <code>TRUE</code> on success, otherwise <code>FALSE</code>
     */
    public Boolean setURL(String connURL) {
        try {
            url = new URL(connURL);
        } catch (MalformedURLException ex) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    /**
     * Set the input file name.
     * @param  filename  The filename to read XML from
     * @return  <code>TRUE</code> on success, otherwise <code>FALSE</code>
     */
    public Boolean setInfile(String filename) {
        try {
            infile = new FileReader(filename);
        } catch (FileNotFoundException ex) {
            setLastError(ex);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    /**
     * Get the HTTP URL connection.
     * @return the connection
     */
    private HttpURLConnection getConnection() {
        HttpURLConnection conn;
        try {
            conn = (HttpURLConnection)url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "text/xml");
            conn.addRequestProperty("Checksum", "It's log, it's log");
            conn.connect();
        } catch (IOException ex) {
            setLastError(ex);
            return null;
        }
        return conn;
    }

    /**
     * Send.
     * @return a flag indicating true or false.
     */
    public Boolean send() {
        if (infile == null) {
            if (setInfile("log.dat") == Boolean.FALSE) {
                logger.error("Valid log file not found");
                return Boolean.FALSE;
            }
        }

        String toSend = "";
        String line = "";
        BufferedReader in = new BufferedReader(infile);

        final int reportSize = 1000;

        try {
            while ((line = in.readLine()) != null) {
                toSend = toSend + line;
            }
        } catch (IOException ex) {
            setLastError(ex);
            return Boolean.FALSE;
        }

        String[] xmlDocs = toSend.split("<\\?xml");

        logger.info("There are " + (xmlDocs.length - 1) + " documents to process");

        int documentCount = 0;
        for (int i = 1; i < xmlDocs.length; i++) {
            xmlDocs[i] = "<?xml" + xmlDocs[i];

            // Not sure what this is for, but do not delete it.
            InputStream inputStream;
            OutputStream outputStream;
            HttpURLConnection conn = getConnection();
            if (conn == null) {
                logger.error("Error opening connection to " + url
                        + ": " + getLastError());
                return Boolean.FALSE;
            }
            try {
                outputStream = conn.getOutputStream();
            } catch (IOException ex) {
                setLastError(ex);
                return Boolean.FALSE;
            }

            documentCount++;
            try {
                outputStream.write(xmlDocs[i].getBytes("ISO-8859-1"));
                outputStream.flush();
                // Not sure what this is for, but do not delete it.
                inputStream = conn.getInputStream();
                conn.disconnect();
            } catch (IOException ex) {
                logger.error("Encountered a problem at or near document " + documentCount);
                logger.error(xmlDocs[i]);
                setLastError(ex);
                return Boolean.FALSE;
            }
            if (i % reportSize == 0) {
                logger.info(i + "/" + (xmlDocs.length - 1) + " documents sent to the server");
            }
        } // end for loop on xml documents
        logger.info("Success!! " + documentCount + " documents sent to the server!");
        logger.info("DiskImporter finished.");
        return Boolean.TRUE;
    } // end send method

    /**
     * Return the last exception from the class.
     * @return Exception object
     */
    public Exception getLastError() {
        return lastException;
    }

    /**
     * Set the last error.
     * @param ex the exception.
     */
    private void setLastError(Exception ex) {
        lastException = ex;
        logger.error(ex);
    }

} // end class DiskImporter
