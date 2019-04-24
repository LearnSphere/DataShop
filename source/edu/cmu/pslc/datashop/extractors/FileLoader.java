/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.extractors;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.JDOMParseException;
import org.jdom.input.SAXBuilder;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.MessageDao;
import edu.cmu.pslc.datashop.extractors.ffi.ImportQueue;
import edu.cmu.pslc.datashop.item.MessageItem;
import edu.cmu.pslc.datashop.util.VersionInformation;
import edu.cmu.pslc.datashop.xml.XmlParser;
import edu.cmu.pslc.datashop.xml.XmlParserFactory;

/**
 * Retrieves the tutor related log records
 * from an oli-log-type database table, read the XML from
 * the info field, parse it, and then write out the data
 * to the message table.
 *
 * @author Alida Skogsholm
 * @version $Revision: 9391 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-06-12 20:14:48 -0400 (Wed, 12 Jun 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class FileLoader extends AbstractLoader {
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** The MessageDao to use. */
    private MessageDao messageDao;

    /** The SaxBuilder to use. */
    private SAXBuilder builder;

    /** The transactionTemplate is used for transaction callback.*/
    private TransactionTemplate transactionTemplate;

    /** Number of messages to save in a single transaction */
    private static final int BULK_UPDATE_NUMBER = 1000;

    /** Import Source for message. */
    public static final String FILE_LOADER_SOURCE = "file_loader";

    /**
     * The constructor.
     */
    public FileLoader() { }

    /**
     * Sets transactionTemplate.
     * @param transactionTemplate the TransactionTemplate to be set to.*/
    public void setTransactionTemplate (TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    /**
     * Gets transactionTemplate.
     * @return TransactionTemplate*/
    public TransactionTemplate getTransactionTemplate() {
        return this.transactionTemplate;
    }

    /**
     * Returns log data given a filename.
     * @param fileName the name of the file
     * @param theFile as File
     * @return the XML in the given file
     */
    private Document getLogData(String fileName, File theFile) {
        Document document = null;
        BufferedReader input = null;
        try {
            document = builder.build(theFile);
        } catch (IOException ex) {
            logger.warn("IOException occurred with " + fileName + ". ", ex);
            document = null;
        } catch (JDOMParseException ex) {
            logger.warn("JDOMParseException occurred with " + fileName + ". ", ex);
            document = null;
        } catch (JDOMException ex) {
            logger.warn("JDOMException occurred with " + fileName + ". ", ex);
            document = null;
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException ex) {
                logger.warn("IOException occurred with " + fileName + ". ", ex);
            }
        }
        return document;
    }

    /**
     * The main method of this converter.
     * @param directory name of the directory
     * @param anonymizeUserId flag to indicate if userId should/must be anonymized by DataShop
     */
    public void convert(String directory, Boolean anonymizeUserId) {
        builder = new SAXBuilder();
        messageDao = DaoFactory.DEFAULT.getMessageDao();

        try {
            logger.info("Getting files...");
            List fileList = getFilenameList(logger, directory);

            logger.info("Reading and converting...");

            XmlParser parser = null;
            for (int j = 0, m = fileList.size(); j < m; j++) {
                File theFile = (File)fileList.get(j);
                String filename = theFile.getName();
                logger.info("Reading file " + filename + " (" + (j + 1) + "/" + m + ")");
                if (theFile.isFile()) {
                    Document xmlDocument = getLogData(filename, theFile);
                    if (xmlDocument != null) {
                        parser = XmlParserFactory.getInstance().get(xmlDocument);
                        if (anonymizeUserId != null) {
                            parser.setAnonymizeUserId(anonymizeUserId);
                        }
                        if (parser != null) {

                            List msgList = parser.getMessageItems();
                            List msgBulkList = new ArrayList();
                            int counter = 0;
                            int total = 0;
                            Date startTime = new Date();
                            for (int i = 0, n = msgList.size(); i < n; i++) {
                                msgBulkList.add(msgList.get(i));
                                counter++;
                                total++;
                                if (counter >= BULK_UPDATE_NUMBER) {
                                    bulkSave(msgBulkList);
                                    counter = 0;
                                    msgBulkList.clear();
                                }
                            }
                            //get any leftovers saved here.
                            bulkSave(msgBulkList);
                            long time = new Date().getTime() - startTime.getTime();
                            logger.info("Finished with " + total + " messages in " + time + "ms");

                        } else {
                            logger.warn("Invalid XML in file " + filename);
                        }
                    } else {
                        logger.warn("Invalid XML in file " + filename);
                    }
                } else {
                    logger.warn("Invalid file " + filename);
                }
            } // end for loop
            logger.info("\nDone.");

        } catch (Throwable throwable) {
            throwable.printStackTrace();
            logger.error("Unknown error.", throwable);
        }
    }

    /**
     * The save all the messages in the msgList in a single transaction.
     * @param msgList list of messages to save.
     */
    private void bulkSave(List msgList) {
        BulkTransactionCallback btc = new BulkTransactionCallback(msgList);
        Boolean successFlag = (Boolean)transactionTemplate.execute(btc);
        if (!successFlag.booleanValue()) {
            logger.error("Something bad happened saving the message.");
        }
    }

    /**
     * Run the Info Field Converter.
     * @param args command line arguments
     */
    public static void main(String[] args) {

        String dataDir = null;
        if (args.length == 0) {
            dataDir = System.getProperty("user.dir");
        } else {
            dataDir = args[0];
        }

        Boolean anonymizeUserId = null;
        if (args.length > 2) {
            if (args[1].equals("-anonymizeUserId")) {
                anonymizeUserId = Boolean.parseBoolean(args[2]);
            }
        }

        Logger logger = Logger.getLogger("FileLoader.main");

        String version = VersionInformation.getReleaseString();
        logger.info("FileLoader starting (" + version + ")...");
        logger.info("File directory is: " + dataDir);
        boolean playMode = ImportQueue.isInPlayMode();
        try {
            if (playMode) {
                ImportQueue.pause();
            }

            FileLoader fileLoader = ExtractorFactory.DEFAULT.getFileLoader();
            fileLoader.convert(dataDir, anonymizeUserId);

        } catch (Throwable throwable) {
            logger.error("Unknown error in main method.", throwable);
        } finally {
            if (playMode) {
                ImportQueue.play();
            }
            logger.info("FileLoader done.");
        }
    }

    /**
     * Inner class to bulk update transactions for speed.
     */
    public class BulkTransactionCallback implements TransactionCallback {

        /** Debug logging. */
        private Logger logger = Logger.getLogger(getClass().getName());

        /** List of tool-tutor pairs. */
        private List msgList;


        /**
         * Constructor.
         * @param msgList List of MessageItems to save to save
         *
         */
        public BulkTransactionCallback(List msgList) {
            this.msgList = msgList;
        }

        /**
         * Do a bunch of messages at a time.  Hoping to improve speed.
         * @param ts TransactionStatus.
         * @return true if successful, false otherwise
         */
        public Object doInTransaction(TransactionStatus ts) {
            try {
                for (Iterator mIter = msgList.iterator(); mIter.hasNext();) {
                    MessageItem theMsg = (MessageItem)mIter.next();
                    theMsg.setImportSource(FILE_LOADER_SOURCE);
                    saveMessageItem(logger, messageDao, theMsg);
                }
            } catch (Throwable exception) {
                logger.error(exception.toString(), exception);
                ts.setRollbackOnly();
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }
    } // end inner class BulkTransactionCallback

} // end class
