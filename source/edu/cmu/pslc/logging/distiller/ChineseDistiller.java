package edu.cmu.pslc.logging.distiller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import edu.cmu.pslc.logging.ContextMessage;
import edu.cmu.pslc.logging.FileLogger;
import edu.cmu.pslc.logging.Message;
import edu.cmu.pslc.logging.ToolMessage;
import edu.cmu.pslc.logging.TutorMessage;
import edu.cmu.pslc.logging.element.ConditionElement;
import edu.cmu.pslc.logging.element.CustomFieldElement;
import edu.cmu.pslc.logging.element.DatasetElement;
import edu.cmu.pslc.logging.element.LevelElement;
import edu.cmu.pslc.logging.element.MetaElement;
import edu.cmu.pslc.logging.element.ProblemElement;
import edu.cmu.pslc.logging.element.SkillElement;
import edu.cmu.pslc.logging.util.DateTools;

/**
 * Change the files Connie and Derek gave us and put them in our format.
 * Assumptions: 1. that every of data has the same number of conditions, KCs and
 * custom fields
 *
 * @author Alida Skogsholm
 * @version $Revision: 8625 $ <BR>
 * Last modified by: $Author: mkomisin $ <BR>
 * Last modified on: $Date: 2013-01-31 09:03:36 -0500 (Thu, 31 Jan 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ChineseDistiller {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** Dataset name. */
    private static final String DATASET_NAME = "Chinese Writing Study Fall 2009";

    /** Directory for XML files. */
    private static final String XML_DIR = "log/xml/";

    /** Number of milliseconds in a second. */
    private static final int NUM_MILLIS = 1000;

    /** Buffered writer for tab-delimited file. */
    private BufferedWriter out = null;

    /** Logging utility which logs to a plain XML file. */
    private FileLogger fileLogger = null;

    /** The dataset name. */
    private String datasetName = DATASET_NAME;
    /** The file name. */
    private String fileName;
    /** The file type/format. */
    private String formatType;

    /** The list of custom field names. */
    private ArrayList<String> cfNameList = new ArrayList();

    /**
     * Display the usage if an error occurs while handling the command line
     * arguments.
     * @see #handleOptions(String[])
     */
    protected void displayUsage() {
        System.err.println("\nUSAGE: java -classpath ... "
                + "GeometryDistiller" + " [-f file_name]"
                + " [-d dataset_name]" + " [-t format_type]");
        System.err.println("Option descriptions:");
        System.err.println("\t-h\tusage info");
        System.err.println("\t-f\tinput file name");
        System.err.println("\t-d\tdataset name");
        System.err.println("\t-t\tformat type");
    }

    /**
     * Handle the command line options.
     * @param args arguments on the command line
     * @see #displayUsage()
     */
    protected void handleOptions(String[] args) {
        if (args.length == 0 || args == null) {
            return;
        }

        java.util.ArrayList argsList = new java.util.ArrayList();
        for (int i = 0; i < args.length; i++) {
            argsList.add(args[i]);
        }

        // loop through the arguments
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-h")) {
                displayUsage();
                System.exit(0);
            } else if (args[i].equals("-f")) {
                if (++i < args.length) {
                    fileName = args[i];
                } else {
                    System.err
                            .println("Error: a file name must be specified with: -f");
                    displayUsage();
                    System.exit(1);
                }
            } else if (args[i].equals("-d")) {
                if (++i < args.length) {
                    datasetName = args[i];
                } else {
                    System.err
                            .println("Error: a dataset name must be specified with: -d");
                    displayUsage();
                    System.exit(1);
                }
            } else if (args[i].equals("-t")) {
                if (++i < args.length) {
                    formatType = args[i];
                } else {
                    System.err
                            .println("Error: a format type must be specified with: -t");
                    displayUsage();
                    System.exit(1);
                }
            } else {
                System.err.println("Error: improper command line arguments: "
                        + argsList);
                displayUsage();
                System.exit(1);
            } // end if then else
        } // end for loop
    } // end handleOptions

    /**
     * Set up tab-delimited and/or xml logging depending on boolean values.
     * @throws FileNotFoundException a file not found exception
     * @throws UnsupportedEncodingException an unsupported encoding exception
     */
    private void setUpLogging() throws UnsupportedEncodingException,
            FileNotFoundException {
        // Create a directory; all non-existent ancestor directories are
        // automatically created
        boolean success = (new File(XML_DIR)).mkdirs();
        if (!success) {
            // Directory creation failed
            logger.warn("Failed to create directory: " + XML_DIR);
        }
    }

    /**
     * Set the action logger to do stream logging and not do disk logging and do
     * production mode if specified on command line. Maybe the other flags
     * should be set on the command line as well.
     * @throws IOException an IO exception
     */
    private void closeLogging() throws IOException {
        if (out != null) {
            out.close();
        }
        if (fileLogger != null) {
            fileLogger.close();
        }
    }

    /**
     * Logs a session for the OLI loggers but starts a new file for the plain
     * file logger.
     * @param userId the user id
     * @param sessionId the session id
     */
    private void logSession(String userId, String sessionId) {
        if (fileLogger != null) {
            fileLogger.close();
        }
        fileLogger = FileLogger.create(XML_DIR + formatType + "_log_" + userId + ".xml");
    }

    /**
     * Log the given message with the given timestamp to the logger(s) which
     * have already been setup.
     * @param message the message to log
     * @param time the time stamp to use
     */
    private void logAction(Message message, Date time) {
        if (fileLogger != null) {
            logger.debug("Logging to plain file");
            fileLogger.log(message, time);
        }
    }

    /**
     * Read the input file and produce a tab-delimited file and or xml file,
     * either of which DataShop can import.
     * @throws IOException an IO exception
     */
    public final void justDoIt() throws IOException {
        logger.info("Reading and parsing file");
        RawData rawData = readAndParseFile();

        logger.info("Creating transaction list");
        List<Transaction> transactionList = null;
        if (formatType.equals("learning")) {
            transactionList = getLearningTxs(rawData);
        } else { // post-test
            transactionList = getPosttestTxs(rawData);
        }

        logger.info("Writing XML file");
        writeXmlFile(transactionList);
    }

    /** KC Model Name: Default. */
    private static final String KC_MODEL_NAME = "Default";

    /** Regular expression for the Custom Field header columns. */
    private static final Pattern CF_PATTERN = Pattern.compile("CF\\s*(.*)");

    /** Regular expression for the KC/Outcome header columns. */
    private static final Pattern LEARNING_KC_PATTERN = Pattern.compile(".*[1-9]");

    /** Regular expression for the KC/Outcome header columns. */
    private static final Pattern PT_KC_PATTERN = Pattern.compile("KC\\s*(.*)");

    /** The number of opportunities for each item. */
    private static final int NUM_OPPS = 3;

    /** Constant. */
    private static final int NUM_SECS_BETWEEN_ROWS = 60;
    /** Constant. */
    private static final int NUM_SECS_BETWEEN_ACTIONS = 10;

    /**
     * Read in learning data and produce a list of transactions.
     * @param rawData
     *            the data read in from the file
     * @return a list of Transaction objects
     */
    public List<Transaction> getLearningTxs(RawData rawData) {
        List<Transaction> transactionList = new ArrayList();
        List<String> headers = rawData.getHeaders();
        List<List> data = rawData.getData();

        Date[] dayArray = {
                DateTools.getDate("2009-11-07 00:00:00"),
                DateTools.getDate("2009-11-08 00:00:00"),
                DateTools.getDate("2009-11-09 00:00:00") };
        int rowNum = 0;
        boolean firstRow = true;
        for (List<String> row : data) {
            Transaction tx = new Transaction();
            int colNum = 0;
            for (String column : row) {
                String header = headers.get(colNum);
                if (header.equals("Anon Student Id")) {
                    tx.setAnonUserId(column);
                } else if (header.equals("Session Id")) {
                    tx.setSessionId("Day_" + column);
                } else if (header.equals("Problem Name")) {
                    tx.setProblemName(column);
                    tx.setDatasetLevel("Learning");
                } else if (header.equals("Condition Name")) {
                    String conditionName = column;
                    String conditionType = "";
                    if (headers.get(colNum + 1).equals("Condition Type")) {
                        conditionType = row.get(colNum + 1);
                    }
                    tx.addCondition(conditionName, conditionType);
                } else {
                    Matcher cfmatcher = CF_PATTERN.matcher(header);
                    if (cfmatcher.matches()) {
                        String cfName = cfmatcher.group(1).trim();
                        cfName = cfName.replace("(", "");
                        cfName = cfName.replace(")", "");
                        tx.addCustomField(cfName, column);
                    } else {
                        Matcher kcmatcher = LEARNING_KC_PATTERN.matcher(header);
                        if (kcmatcher.matches()) {
                            String kcName = header.substring(0,
                                    header.length() - 1);
                            String countString = header.substring(header
                                    .length() - 1);
                            int count = 1;
                            try {
                                count = new Integer(countString);
                            } catch (NumberFormatException exception) {
                                logger.error("NumberFormatException", exception);
                            }
                            int day = ((count - 1) / NUM_OPPS) + 1;
                            int opp = count - NUM_OPPS * (day - 1);
                            Transaction newTx = new Transaction(tx);
                            newTx.setSessionId(tx.getAnonUserId() + "_Day_"
                                    + day);
                            Date date = dayArray[day - 1];
                            date = addDuration(date,
                                    (NUM_SECS_BETWEEN_ROWS * rowNum)
                                    + (opp * NUM_SECS_BETWEEN_ACTIONS));
                            newTx.setTxTime(date);
                            if (logger.isTraceEnabled()) {
                                logger.trace("Get Txs : " + kcName + " "
                                    + newTx.getTxTime() + " "
                                    + newTx.getSessionId()
                                    + " Opp " + opp);
                            }
                            newTx.setSelection(kcName);
                            newTx.setAction("Opp" + opp);
                            newTx.setInput("");
                            newTx.setOutcome(determineOutcome(column, rowNum,
                                    colNum, newTx.getSelection(), newTx
                                            .getAction()));
                            newTx.addKC(KC_MODEL_NAME, kcName);

                            transactionList.add(newTx);
                        }
                    }
                }
                colNum++;
            } // end for loop on columns
            if (firstRow) {
                for (Pair cfPair : tx.getCustomFieldList()) {
                    cfNameList.add(cfPair.getName());
                }
            }
            firstRow = false;
            rowNum++;
        } // end for loop on rows
        return transactionList;
    }

    /** The string DataShop accepts for correct. */
    private static final String CORRECT = "CORRECT";

    /** The string DataShop accepts for incorrect. */
    private static final String INCORRECT = "INCORRECT";

    /** The outcome threshold value of .50. */
    private static final double THRESHOLD = .50;

    /**
     * Return CORRRECT or INCORRECT depending on whether the given value falls
     * above the threshold.
     * @param valueString the value for the KC
     * @param row row number in original data
     * @param col column number in original data
     * @param selection the selection of the row
     * @param action the action of the row
     * @return a string, CORRECT or INCORRECT
     */
    private String determineOutcome(String valueString, int row, int col,
            String selection, String action) {
        String outcome = INCORRECT;
        try {
            Double value = new Double(valueString);
            if (value > THRESHOLD) {
                outcome = CORRECT;
            }
        } catch (NumberFormatException exception) {
            logger.warn("Invalid number found [" + valueString + "] in row "
                    + row + " and col " + col + " which is select/action: "
                    + selection + "/" + action);
        }
        return outcome;
    }

    /**
     * Read in post-test data and produce a list of transactions.
     * @param rawData the unprocessed/raw data from the input file
     * @return a list of transactions
     */
    public List<Transaction> getPosttestTxs(RawData rawData) {
        List<Transaction> transactionList = new ArrayList();
        List<String> headers = rawData.getHeaders();
        List<List> data = rawData.getData();

        int rowNum = 0;
        boolean firstRow = true;
        for (List<String> row : data) {
            Transaction tx = new Transaction();
            int colNum = 0;
            for (String column : row) {
                String header = headers.get(colNum);
                if (header.equals("Anon Student Id")) {
                    tx.setAnonUserId(column);
                } else if (header.equals("Session Id")) {
                    tx.setSessionId("Day_" + column);
                } else if (header.equals("Time")) {
                    tx.setTxTime(DateTools.getDate(column));
                } else if (header.equals("Time Offset")) {
                    int timeOffset = new Integer(column);
                    int secs = timeOffset / NUM_MILLIS;
                    int ms = timeOffset % NUM_MILLIS;
                    Date correctTime = addDuration(tx.getTxTime(), secs);
                    tx.setContextTime(correctTime);
                    tx.setTxTime(correctTime);
                    tx.setMS(ms);
                } else if (header.equals("Duration")) {
                    int duration = new Integer(column);
                    int secs = duration / NUM_MILLIS;
                    Date correctTime = addDuration(tx.getTxTime(), secs);
                    tx.setTxTime(correctTime);
                } else if (header.equals("Level(task)")) {
                    tx.setDatasetLevel(column);
                } else if (header.equals("Problem Name")) {
                    tx.setProblemName(column);
                } else if (header.equals("Condition Name")) {
                    String conditionName = column;
                    String conditionType = "";
                    if (headers.get(colNum + 1).equals("Condition Type")) {
                        conditionType = row.get(colNum + 1);
                    }
                    tx.addCondition(conditionName, conditionType);
                } else {
                    Matcher cfmatcher = CF_PATTERN.matcher(header);
                    if (cfmatcher.matches()) {
                        if (column.length() > 0) {
                            column = column.trim();
                            String cfName = cfmatcher.group(1).trim();
                            cfName = cfName.replace("(", "");
                            cfName = cfName.replace(")", "");
                            tx.addCustomField(cfName, column);
                        }
                    } else {
                        Matcher kcmatcher = PT_KC_PATTERN.matcher(header);
                        if (kcmatcher.matches()) {
                            String kcName = kcmatcher.group(1).trim();
                            kcName = kcName.replace("(", "");
                            kcName = kcName.replace(")", "");

                            Transaction newTx = new Transaction(tx);

                            newTx.setSessionId(tx.getAnonUserId() + "_"
                                    + tx.getSessionId());
                            newTx.setSelection(kcName);
                            newTx.setAction("");
                            newTx.setInput("");
                            newTx.setOutcome(determineOutcome(column, rowNum,
                                    colNum, newTx.getSelection(), newTx
                                            .getAction()));
                            newTx.addKC(KC_MODEL_NAME, kcName);

                            transactionList.add(newTx);
                        }
                    }
                }
                colNum++;
            } // end for loop on columns
            if (firstRow) {
                for (Pair cfPair : tx.getCustomFieldList()) {
                    cfNameList.add(cfPair.getName());
                }
            }
            firstRow = false;
            rowNum++;
        } // end for loop on rows
        return transactionList;
    }

    /** The minimum number of fields in a row of the raw data. */
    private static final int MIN_NUM_FIELDS = 5;

    /**
     * Read the data from the file, parse it and return a list of RawData
     * objects.
     * @return list of type RawData (inner class)
     */
    public RawData readAndParseFile() {
        RawData rawData = new RawData();
        boolean firstRow = true;
        try {
            logger.info("Dataset Name: " + datasetName);
            logger.info("Reading File: " + fileName);
            logger.info("Format Type: " + formatType);

            File inputFile = new File(fileName);
            FileReader fileReader = new FileReader(inputFile);
            BufferedReader bufReader = new BufferedReader(fileReader);

            int minNumFields = MIN_NUM_FIELDS;
            String line = null;
            int lineCounter = 0;
            while ((line = bufReader.readLine()) != null) {
                lineCounter++;
                String[] result = line.split("\t");

                if (result.length < minNumFields) {
                    logger
                            .info("Skipping line " + lineCounter
                                    + " without enough columns ["
                                    + result.length + "]");
                    continue;
                }

                if (firstRow) {
                    firstRow = false;
                    rawData.addHeader(result);
                    minNumFields = rawData.getNumHeaders();
                    logger.info("Expecting " + minNumFields
                            + " columns in each row of data.");
                } else {
                    rawData.addRow(result);
                }

            } // end while more lines to read

            bufReader.close();

        } catch (FileNotFoundException exception) {
            exception.printStackTrace();
            logger.error(exception.getMessage());
        } catch (IOException exception) {
            exception.printStackTrace();
            logger.error(exception.getMessage());
        }

        return rawData;
    } // end readAndParseFile method

    /** String constant for the Eastern Time Zone. */
    private static final String TIME_ZONE = "EST";

    /**
     * Write the transactions to a XML file.
     * @param transactionList the list of transactions
     * @throws IOException an IO exception on the write to file
     */
    public void writeXmlFile(List<Transaction> transactionList)
            throws IOException {

        ContextMessage contextMsg = null;
        String currSession = null;
        String currStudent = null;
        String currProblem = null;
        Date contextTime = new Date();
        Date txTime = new Date();

        boolean newContextMsgNeeded = false;

        int txIdx = 0;
        for (Transaction tx : transactionList) {
            if (logger.isTraceEnabled()) {
                logger.trace(txIdx + " transaction " + tx.toString());
            }
            txTime = tx.getTxTime();
            if (tx.getContextTime() != null) {
                contextTime = tx.getContextTime();
            } else {
                contextTime = tx.getTxTime();
            }

            if (currSession == null || !currSession.equals(tx.getSessionId())) {
                currSession = tx.getSessionId();
                newContextMsgNeeded = true;
            }

            if (currStudent == null || !currStudent.equals(tx.getAnonUserId())) {
                currStudent = tx.getAnonUserId();
                logSession(currStudent, tx.getSessionId());
                newContextMsgNeeded = true;
            }

            if (currProblem == null || !currProblem.equals(tx.getProblemName())) {
                currProblem = tx.getProblemName();
                newContextMsgNeeded = true;
            }

            //
            // Context Message
            //
            if (newContextMsgNeeded) {
                logger.info(txIdx + " transaction for " + currStudent
                        + " problem " + currProblem + " for session " + tx.getSessionId());
                MetaElement metaElement = new MetaElement(currStudent, true,
                        tx.getSessionId(),
                        DateTools.getTimeStringWithOutTimeZone(contextTime), TIME_ZONE);

                contextMsg = ContextMessage.createStartProblem(metaElement);
                ProblemElement problem = new ProblemElement(currProblem);
                LevelElement unitLevel = new LevelElement("Phase", tx.getDatasetLevel(), problem);
                contextMsg.setDataset(new DatasetElement(DATASET_NAME, unitLevel));
                for (Pair pair : tx.getConditionList()) {
                    contextMsg.addCondition(new ConditionElement(
                            pair.getName(), pair.getValue()));
                }
                logAction(contextMsg, contextTime);
            }

            //
            // Tool Message
            //
            ToolMessage toolMsg = ToolMessage.create(contextMsg);
            toolMsg.setAsAttempt();
            toolMsg.addSai(tx.getSelection(), tx.getAction(), tx.getInput());
            logAction(toolMsg, txTime);

            //
            // Tutor Message
            //
            TutorMessage tutorMsg = TutorMessage.create(toolMsg);
            if (tx.getOutcome().equals(CORRECT)) {
                tutorMsg.setAsCorrectAttemptResponse();
            } else {
                tutorMsg.setAsIncorrectAttemptResponse();
            }
            tutorMsg.addSai(tx.getSelection(), tx.getAction(), "");

            for (Pair pair : tx.getKCList()) {
                tutorMsg.addSkill(new SkillElement(pair.getValue(), "", pair
                        .getName()));
            }

            for (Pair pair : tx.getCustomFieldList()) {
                tutorMsg.addCustomField(new CustomFieldElement(pair.getName(),
                        pair.getValue()));
            }
            logAction(tutorMsg, txTime);
            txIdx++;
            newContextMsgNeeded = false;
        }
    }

    /**
     * Add the given number of seconds to the given time stamp.
     * @param time the given time stamp
     * @param seconds the number of seconds to add
     * @return the new time stamp
     */
    private Date addDuration(Date time, int seconds) {
        Date newTime = new Date(time.getTime() + (seconds * NUM_MILLIS));
        return newTime;
    }

    /**
     * Main.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        Logger logger = Logger.getLogger("ChineseDistiller.main");
        logger.info("ChineseDistiller starting...");
        ChineseDistiller distiller = null;
        try {
            // create a distiller
            distiller = new ChineseDistiller();

            // parse arguments to get file name and curriculum name
            distiller.handleOptions(args);

            // Production mode is set on the command line
            distiller.setUpLogging();

            // read file, parse it and do something cool with it
            distiller.justDoIt();

        } catch (Throwable throwable) {
            logger.error("Unknown error occurred:" + throwable.getMessage(),
                    throwable);
        } finally {
            // close the output streams
            try {
                distiller.closeLogging();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        logger.info("ChineseDistiller done.");
    } // end main

    /**
     * Holds the data read in from the given file.
     */
    class RawData {
        /** A list of the column headings. */
        private List<String> headers = new ArrayList();
        /** A list of lists of row data. */
        private List<List> data = new ArrayList();

        /** Default constructor, does nothing. */
        public RawData() {
        }

        /**
         * Adds the column headings.
         * @param result the parsed line read from the file
         */
        public void addHeader(String[] result) {
            int len = result.length;
            for (int idx = 0; idx < len; idx++) {
                headers.add(result[idx]);
            }
        }

        /**
         * Returns the number of column headings.
         * @return the number of column headings
         */
        public int getNumHeaders() {
            return headers.size();
        }

        /**
         * Adds a row of data.
         * @param result the parsed line read from the file
         */
        public void addRow(String[] result) {
            List<String> row = new ArrayList();
            int len = result.length;
            for (int idx = 0; idx < len; idx++) {
                row.add(result[idx]);
            }
            data.add(row);
        }

        /**
         * Returns the list of headers.
         * @return the list of headers.
         */
        public List getHeaders() {
            return headers;
        }

        /**
         * Returns the list of data.
         * @return the list of data.
         */
        public List getData() {
            return data;
        }
    }

    /**
     * Hold all the information to write out a transaction.
     */
    class Transaction {
        /** Anonymous User Id. */
        private String anonUserId;
        /** Session Id. */
        private String sessionId;
        /** Time stamp. */
        private Date contextTime;
        /** Time stamp. */
        private Date txTime;
        /** Milliseconds, future. */
        private int ms;
        /** Dataset Level - Phase. */
        private String datasetLevel;
        /** Problem Name. */
        private String problemName;
        /** CORRECT vs INCORRECT. */
        private String outcome;
        /** Selection. */
        private String selection;
        /** Action. */
        private String action;
        /** Input. */
        private String input;
        /** Condition list. */
        private ArrayList<Pair> conditionList = new ArrayList();
        /** Custom Field list. */
        private ArrayList<Pair> customFieldList = new ArrayList();
        /** KC list. */
        private ArrayList<Pair> kcList = new ArrayList();

        /**
         * Returns a string version of this object.
         * @return a string
         */
        public String toString() {
            return anonUserId + " " + sessionId + " "
                    + contextTime + " "
                    + txTime + " "
                    + datasetLevel + " " + problemName + " " + outcome + " "
                    + selection + " " + action + " " + input + " ";
        }

        /**
         * Constructor.
         */
        Transaction() {
            // do nothing
        }

        /**
         * Copy constructor.
         * @param other a transaction object to copy
         */
        Transaction(Transaction other) {
            this.anonUserId = other.anonUserId;
            this.sessionId = other.sessionId;
            this.contextTime = other.contextTime;
            this.txTime = other.txTime;
            this.ms = other.ms;
            this.datasetLevel = other.datasetLevel;
            this.problemName = other.problemName;
            this.outcome = other.outcome;
            this.selection = other.selection;
            this.action = other.action;
            this.input = other.input;
            this.conditionList = (ArrayList) other.conditionList.clone();
            this.customFieldList = (ArrayList) other.customFieldList.clone();
            this.kcList = (ArrayList) other.kcList.clone();
        }

        /**
         * Returns the anonUserId.
         * @return the anonUserId.
         */
        public String getAnonUserId() {
            return anonUserId;
        }

        /**
         * Sets the anon UserId.
         * @param anonUserId the subject
         */
        public void setAnonUserId(String anonUserId) {
            this.anonUserId = anonUserId;
        }

        /**
         * Returns the session id.
         * @return the session id
         */
        public String getSessionId() {
            return sessionId;
        }
        /**
         * Set session id.
         * @param sessionId the session id
         */
        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }
        /**
         * Get context time.
         * @return the context time
         */
        public Date getContextTime() {
            return contextTime;
        }
        /**
         * Set context time.
         * @param contextTime the context time
         */
        public void setContextTime(Date contextTime) {
            this.contextTime = contextTime;
        }
        /**
         * Get transaction time.
         * @return the transaction time
         */
        public Date getTxTime() {
            return txTime;
        }
        /**
         * Set transaction time.
         * @param txTime the transaction time
         */
        public void setTxTime(Date txTime) {
            this.txTime = txTime;
        }
        /**
         * Get milliseconds.
         * @return milliseconds
         */
        public int getMS() {
            return ms;
        }
        /**
         * Set milliseconds.
         * @param ms milliseconds
         */
        public void setMS(int ms) {
            this.ms = ms;
        }
        /**
         * Get dataset level.
         * @return the dataset level
         */
        public String getDatasetLevel() {
            return datasetLevel;
        }
        /**
         * Set dataset level.
         * @param datasetLevel the dataset level
         */
        public void setDatasetLevel(String datasetLevel) {
            this.datasetLevel = datasetLevel;
        }
        /**
         * Get problem name.
         * @return the problem name
         */
        public String getProblemName() {
            return problemName;
        }
        /**
         * Set problem name.
         * @param problemName the problem name
         */
        public void setProblemName(String problemName) {
            this.problemName = problemName;
        }
        /**
         * Get outcome.
         * @return the outcome
         */
        public String getOutcome() {
            return outcome;
        }
        /**
         * Set outcome.
         * @param outcome the outcome
         */
        public void setOutcome(String outcome) {
            this.outcome = outcome;
        }
        /**
         * Get selection.
         * @return the selection
         */
        public String getSelection() {
            return selection;
        }
        /**
         * Set selection.
         * @param selection the selection
         */
        public void setSelection(String selection) {
            this.selection = selection;
        }
        /**
         * Get action.
         * @return the action
         */
        public String getAction() {
            return action;
        }
        /**
         * Set action.
         * @param action the action
         */
        public void setAction(String action) {
            this.action = action;
        }
        /**
         * Get input.
         * @return the input
         */
        public String getInput() {
            return input;
        }
        /**
         * Set input.
         * @param input the input
         */
        public void setInput(String input) {
            this.input = input;
        }
        /**
         * Add condition
         * @param conditionName the condition name
         * @param conditionType the condition type
         */
        public void addCondition(String conditionName, String conditionType) {
            Pair pair = new Pair(conditionName, conditionType);
            conditionList.add(pair);
        }

        /**
         * Add a custom field.
         * @param cfName the name
         * @param cfValue the value
         */
        public void addCustomField(String cfName, String cfValue) {
            Pair pair = new Pair(cfName, cfValue);
            customFieldList.add(pair);
        }

        /**
         * Add a KC.
         * @param kcModel the KC model
         * @param kcName the KC name
         */
        public void addKC(String kcModel, String kcName) {
            Pair pair = new Pair(kcModel, kcName);
            kcList.add(pair);
        }

        /**
         * Returns the condition list.
         * @return the condition list
         */
        public List<Pair> getConditionList() {
            return conditionList;
        }

        /**
         * Returns the custom field list.
         * @return the custom field list
         */
        public List<Pair> getCustomFieldList() {
            return customFieldList;
        }

        /**
         * Returns the KC list.
         * @return the KC list
         */
        public List<Pair> getKCList() {
            return kcList;
        }
    }

    /**
     * Hold a name value pair. For a condition, the name is the condition name,
     * the value the condition type. For a custom field, the name is the CF
     * name, the value the CF value. For a knowledge component, the name is the
     * KC model name, the value the KC name.
     */
    class Pair {
        /** Name. */
        private String name;
        /** Value. */
        private String value;

        /**
         * Constructor.
         * @param name the name
         * @param value the value
         */
        Pair(String name, String value) {
            this.name = name;
            this.value = value;
        }

        /**
         * Returns the name.
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * Returns the value.
         * @return the value
         */
        public String getValue() {
            return value;
        }
    }
}
