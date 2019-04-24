package edu.cmu.pslc.datashop.extractors;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.MessageDao;
import edu.cmu.pslc.datashop.dto.DatasetDTO;
import edu.cmu.pslc.datashop.dto.ItemCache;
import edu.cmu.pslc.datashop.dto.ToolTutorMessageItemPair;
import edu.cmu.pslc.datashop.dto.UserSession;
import edu.cmu.pslc.datashop.extractors.ffi.ImportQueue;
import edu.cmu.pslc.datashop.helper.DatasetCreator;
import edu.cmu.pslc.datashop.helper.SystemLogger;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.MessageItem;
import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.util.DataShopInstance;
import edu.cmu.pslc.datashop.util.MemoryUtils;
import edu.cmu.pslc.datashop.util.VersionInformation;
import edu.cmu.pslc.datashop.xml.ContextMessage;
import edu.cmu.pslc.datashop.xml.ContextMessageParser;
import edu.cmu.pslc.datashop.xml.ContextMessageParserFactory;
import edu.cmu.pslc.datashop.xml.SemanticEvent;
import edu.cmu.pslc.datashop.xml.ToolMessage;
import edu.cmu.pslc.datashop.xml.ToolMessageParser;
import edu.cmu.pslc.datashop.xml.ToolMessageParserFactory;
import edu.cmu.pslc.datashop.xml.TutorMessage;
import edu.cmu.pslc.datashop.xml.TutorMessageParser;
import edu.cmu.pslc.datashop.xml.TutorMessageParserFactory;
import edu.cmu.pslc.datashop.xml.validator.XMLValidator;
/**
 * Populates the Tutor Transaction table by rolling up the
 * data in the tutor message table.
 *
 * @author Alida Skogsholm
 * @version $Revision: 12706 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-10-20 09:09:25 -0400 (Tue, 20 Oct 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public class TutorMessageConverter extends AbstractExtractor {
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** Constant for "Default" value. */
    private static final String DEFAULT_VALUE = "Default";

    /** The transactionTemplate is used for transaction callback.*/
    private TransactionTemplate transactionTemplate;

    /** The session factory */
    private SessionFactory sessionFactory;

    /** The messageDao */
    private MessageDao messageDao;

    /** The time of instantiation */
    private final Date instantiationTime = new Date();

    /** Default dataset name. */
    private String defaultDatasetName;
    /** Default dataset level name. */
    private String defaultDatasetLevelName;
    /** Default dataset level tile name. */
    private String defaultDatasetLevelTitleName;
    /** Default problem name. */
    private String defaultProblemName;
    /** Flag indicating whether to retry messages where processed_flag is ERROR. */
    private boolean retryErrorMessagesFlag = false;

    /** Default school name. */
    public static final String DEFAULT_DATASET_NAME     = DEFAULT_VALUE;
    /** Default project name. */
    public static final String DEFAULT_DATASETLEVEL_NAME    = DEFAULT_VALUE;
    /** Default project name. */
    public static final String DEFAULT_DATASETLEVEL_TITLE_NAME    = DEFAULT_VALUE;
    /** Default problem name. */
    public static final String DEFAULT_PROBLEM_NAME    = DEFAULT_VALUE;
    /** Semantic Event Name - "RESULT". */
    private static final String SEM_EV_NAME_RESULT    = "RESULT";
    /** Semantic Event Name - "ATTEMPT". */
    private static final String SEM_EV_NAME_ATTEMPT   = "ATTEMPT";
    /** Semantic Event Name - "HINT_REQUEST". */
    private static final String SEM_EV_NAME_HINT_REQUEST = "HINT_REQUEST";
    /** Semantic Event Name - "HINT_MSG". */
    private static final String SEM_EV_NAME_HINT_MSG     = "HINT_MSG";

    /** The minimum version required to validate XML via the XMLValidator */
    private static final Integer MIN_XML_VALIDATE_VERSION = 4;

    /** The maximum number of tool tutor pairs to wrap in a single top-level transaction. */
    private static final int BULK_PAIR_LIMIT = 1000;

    /** Map of datasets and 'Appears Anonymous' flag, tracked across ContextMessages. */
    private Map appearsAnonMap = new HashMap<DatasetItem, Boolean>();

    /** Set of all modified datasets for a given run */
    private Set <DatasetItem> modifiedDatasets;

    /**
     * Default constructor.
     */
    public TutorMessageConverter() {
        modifiedDatasets = new HashSet <DatasetItem>();
    }

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
     * Gets the sessionFactory.
     * @return the sessionFactory
     */
    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * Sets the sessionFactory.
     * @param sessionFactory the sessionFactory to set
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Extract data from the database that has not been processed yet
     * and load it into the tutor_transaction table as well as the
     * supporting tables referenced as foreign keys.
     *
     * The steps are:
     * <UL>
     * <LI>Load the user/session pairs
     * <LI>Loop through the user/session pairs
     * <LI>For each user/session, get the associated context messages, save to database
     * </UL>
     */
    public void extract() {
        messageDao = DaoFactory.HIBERNATE.getMessageDao();

        List userSessionList;
        String details = "null or " + MessageItem.SUCCESS_QUESTIONABLE_FLAG;

        if (retryErrorMessagesFlag) {
            logger.info("Getting context messages with " + details + " processed flag.");
            userSessionList = messageDao.getDistinctUserSessionsWithError();
            details = MessageItem.ERROR_FLAG;
        } else {
            logger.info("Getting context messages with " + details + " processed flag.");
            details = "null or " + MessageItem.SUCCESS_QUESTIONABLE_FLAG;
            userSessionList = messageDao.getDistinctUserSessions();
        }

        //get a list of user-session pairs from the message table
        extractGivenDistinctUserSessionPairs(userSessionList, details, retryErrorMessagesFlag);

        //DS979: (TMC: Need to look for and mark more messages as ERROR instead of leaving as NULL.)
        markToolTutorMessagesAsErrorIfContextMessageIsBad(userSessionList);

    } // end extract

    /**
     * Extract data from the raw data section of the analysis db which is passed in.
     * @param userSessionList distinct list of user session pairs
     * @param details the details of which set of context messages we are working with,
     * null/RECHECK or ERROR
     * @param getErrorMsgsFlag if true, get context messages with an error to try them again
     */
    private void extractGivenDistinctUserSessionPairs(
            List userSessionList, String details, boolean getErrorMsgsFlag) {
        logger.info("Number of user/session: " + userSessionList.size()
                    + " where processed flag is " + details);
        Date startTime = null;
        if (logger.isDebugEnabled()) { startTime = new Date(); }

        for (int i = 0, n = userSessionList.size(); i < n; i++) {
            UserSession userSession = (UserSession)userSessionList.get(i);
            startTime = logTime(logger, "Get context message start at: ", startTime);
            MemoryUtils.logMemoryUsage(Level.TRACE, true, logger,
                    "TMC-extractGivenDistinctUserSessionPairs");

            //for each user-session pair, get a list of context messages
            List contextMessageItems = messageDao.getContextMessageItems(userSession.getUserId(),
                                            userSession.getSessionId(), getErrorMsgsFlag);
            logger.info((i + 1) + "/" + n + " user/session: "
                    + userSession.getUserId() + "/" + userSession.getSessionId()
                    + ": Number of context messageItems " + contextMessageItems.size()
                    + " where processed flag is " + details);

            startTime = logTime(logger, "Iterate context message start at : ", startTime);
            int toolTutorPairCount = 0;
            Date messageConvertStartTime = new Date();
            // Save each context message
            for (Iterator contextMessageIter = contextMessageItems.iterator();
                                                contextMessageIter.hasNext();) {

                startTime = logTime(logger, "Parse context message start at : ", startTime);
                MessageItem currentMessageItem = (MessageItem)contextMessageIter.next();
                ContextMessageParser parser = ContextMessageParserFactory
                                                .getInstance().get(currentMessageItem);
                if (parser == null) {
                    //update the processed status and info for this message
                    String msg = "In extract, parser is null.";
                    setProcessedFlagToError(currentMessageItem, msg, null);
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("parser is instantiated for context message: "
                                + "user/session: contextMsgId "
                                + userSession.getUserId() + "/" + userSession.getSessionId()
                                + ": " + currentMessageItem.getContextMessageId());
                    }
                    //set the default dataset, dataset level, problem in case it will be used
                    parser.setDefaultProblemName(defaultProblemName);
                    parser.setDefaultDatasetName(defaultDatasetName);
                    parser.setDefaultDatasetLevelName(defaultDatasetLevelName);
                    parser.setDefaultDatasetLevelTitle(defaultDatasetLevelTitleName);

                    ContextMessage currentContextMessage = null;
                    try {
                        // Ask the parser for the current context message
                        currentContextMessage = parser.getContextMessage();
                        // Set minimum time for context messages that occur later (by time)
                        // than the first message with the same context message id
                        String userId = currentMessageItem.getUserId();
                        String sessionTag = currentMessageItem.getSessionTag();
                        String contextMessageId = currentMessageItem.getContextMessageId();

                        Date minTime = messageDao.getMessageMinTime(userId, contextMessageId);
                        if (minTime != null && currentMessageItem.getTime().after(minTime)) {
                            currentMessageItem.setTime(minTime);
                        }

                        // sessionsByContextMessageId
                    } catch (Exception exception) {
                        String errorMsg;
                        try {
                            if (new Integer(currentMessageItem.getXmlVersion())
                                    >= MIN_XML_VALIDATE_VERSION
                                    && !isValidMessage(currentMessageItem)) {
                                errorMsg = "Invalid context message.";
                            } else {
                                errorMsg = "Unexpected error parsing context message.";
                            }
                        } catch (Exception validateException) {
                            errorMsg = "Exception getting XML version.";
                        }
                        setProcessedFlagToError(currentMessageItem, errorMsg, exception);
                        continue;
                    }
                    startTime = logTime(logger, "Done parsing context message at : ", startTime);

                    ContextMessage newMsg =
                        saveContextData(currentContextMessage, currentMessageItem);
                    //if the current context message has dataset with real id,
                    //go on for the tool and tutor.

                    if (newMsg != null) {
                        boolean isModified = false;
                        if (logger.isDebugEnabled()) {
                            logger.debug("Saved context data for " + currentMessageItem.getGuid());
                        }

                        //Do all the tutor/tool pairs first.
                        startTime = logTime(logger, "nasty join start at : ", startTime);
                        List allPairs = messageDao.getToolTutorMessages(userSession.getUserId(),
                                            userSession.getSessionId(),
                                            newMsg.getContextId());
                        startTime = logTime(logger, "nasty join done at : ", startTime);
                        // Get all tutor/tool pairs and iterate through them
                        List tutorToolPairs =  getToolTutorPairs(allPairs);
                        startTime = logTime(logger, "get ToolTutorPairs done at : ", startTime);
                        // Update count of tutor/tool pairs
                        toolTutorPairCount += tutorToolPairs.size();
                        for (Iterator ttiter = tutorToolPairs.iterator(); ttiter.hasNext();) {
                            startTime = logTime(logger, "saving transaction start at : ",
                                    startTime);
                            startTime = new Date();
                            List pairList = new ArrayList();
                            // Index for the batch of T/T pairs
                            int idx = 0;
                            // An inner loop to create a finite-sized T/T pair list
                            while (idx < BULK_PAIR_LIMIT && ttiter.hasNext()) {
                                ToolTutorPair toolTutorPair = (ToolTutorPair)ttiter.next();
                                pairList.add(toolTutorPair);
                                idx++;
                            }
                            // Save the pair list
                            if (!saveTutorToolPairList(newMsg, pairList)) {
                                String errorMsg = "saveTutorToolPairList returned false.";
                                setProcessedFlagToError(currentMessageItem, errorMsg, null);
                            } else {
                                // Tool/tutor pair saved
                                isModified = true;
                            }
                        }
                        startTime = logTime(logger, "saving pair transactions done at : ",
                                startTime);

                        //Now do the lonely tool messages
                        if (processToolOnlyMessages(currentMessageItem, userSession.getUserId(),
                                userSession.getSessionId(), newMsg)) {
                            // Lonely tool message saved
                            isModified = true;
                        }

                        if (isModified) {
                            modifiedDatasets.add(newMsg.getDatasetItem());
                            /*
                             * DS1525: update 'Appears Anonymous' flag for dataset.
                             * Track flag across ContextMessages in dataset. Once set to 'false'
                             * for a dataset, this cannot be changed.
                             */
                            if (newMsg.getAppearsAnonIsNA() != null) {
                                Boolean appearsAnonIsNA =
                                        (Boolean) appearsAnonMap.get(
                                                newMsg.getDatasetItem());
                                if (appearsAnonIsNA == null) {
                                    appearsAnonMap.put(
                                            newMsg.getDatasetItem(),
                                            newMsg.getAppearsAnonIsNA());
                                } else if (appearsAnonIsNA) {
                                    if (!newMsg.getAppearsAnonIsNA()) {
                                        appearsAnonMap.put(
                                                newMsg.getDatasetItem(),
                                                newMsg.getAppearsAnonIsNA());
                                    }
                                }
                            }
                        }
                        //TODO What to do with orphan tutor messages?

                    } else {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Context data NOT saved for: "
                                    + currentMessageItem.getGuid());
                        }
                    }
                }
            } //end of looping of all context messages

            NumberFormat nf = getNumberFormat();
            logger.info("Number of tool/tutor pairs for user/session: "
                   + userSession.getUserId() + "/" + userSession.getSessionId()
                   + ": " + toolTutorPairCount + " converted at the rate of "
                   + nf.format((float)toolTutorPairCount / secondsElapsed(messageConvertStartTime))
                   + " a second.");
        } //end of looping of userSession pairs

        DatasetDao datasetDao = DaoFactory.HIBERNATE.getDatasetDao();

        for (Iterator <DatasetItem> it = modifiedDatasets.iterator(); it.hasNext();) {
            DatasetItem dataset = it.next();
            if (dataset != null) {
                SystemLogger.log(dataset,
                    SystemLogger.ACTION_MODIFY_DATASET, "New data loaded for "
                    + dataset.getDatasetName() + "(" + dataset.getId() + ") from TMC");
                dataset.setDataLastModified(new Date());

                Boolean appearsAnonIsNA = (Boolean)appearsAnonMap.get(dataset);

                // Set/Update DatasetItem to reflect 'Appears Anonymous' flag.
                if (dataset.getAppearsAnonymous() == null) {
                    if ((appearsAnonIsNA != null) && appearsAnonIsNA) {
                        dataset.setAppearsAnonymous(DatasetItem.APPEARS_ANON_NA);
                    } else {
                        // Default
                        dataset.setAppearsAnonymous(DatasetItem.APPEARS_ANON_NOT_REVIEWED);
                    }
                } else if (dataset.getAppearsAnonymous().equals(DatasetItem.APPEARS_ANON_NA)) {
                    // Existing can only move from 'N/A' to 'Not Reviewed'.
                    if ((appearsAnonIsNA != null) && !appearsAnonIsNA) {
                        dataset.setAppearsAnonymous(DatasetItem.APPEARS_ANON_NOT_REVIEWED);
                    }
                }

                //save dataset changes
                datasetDao.saveOrUpdate(dataset);

                //update the start/end times on the dataset
                datasetDao.autoSetDates(dataset);

                // If a slave, update master DataShop instance with dataset info.
                if (DataShopInstance.isSlave()) {
                    String datasetName = dataset.getDatasetName();
                    Integer datasetId = (Integer)dataset.getId();
                    logger.info("Update master DataShop instance with dataset info"
                                + datasetName + " (" + datasetId + ")");
                    try {
                        DatasetDTO datasetDto =
                            HelperFactory.DEFAULT.getWebServiceHelper().datasetDTOForId(datasetId);
                        DatasetCreator.INSTANCE.setDataset(datasetDto);
                    } catch (Exception e) {
                        // Failed to push Dataset info to master. Ignore?
                        logger.debug("Failed to push dataset info to master for dataset '"
                                     + datasetName + "': " + e);
                    }
                }
            }
        }
    }

    /**
     * DS979: (TMC: Need to look for and mark more messages as ERROR instead of leaving as NULL.)
     * Get all the messages for ERROR context messages and mark them as ERROR too.
     * @param userSessionList distinct list of user session pairs
     */
    private void markToolTutorMessagesAsErrorIfContextMessageIsBad(List userSessionList) {
        final boolean getErrorMsgsFlag = true;
        final String details = MessageItem.ERROR_FLAG;

        logger.info("Number of user/session: " + userSessionList.size()
                    + " where processed flag is " + details);

        for (int i = 0, n = userSessionList.size(); i < n; i++) {
            UserSession userSession = (UserSession)userSessionList.get(i);

            //for each user-session pair, get a list of context messages
            List contextMessageItems = messageDao.getContextMessageItems(userSession.getUserId(),
                                            userSession.getSessionId(), getErrorMsgsFlag);

            logger.info((i + 1) + "/" + n + " user/session: "
                    + userSession.getUserId() + "/" + userSession.getSessionId()
                    + ": Number of context messageItems " + contextMessageItems.size()
                    + " where processed flag is " + details);

            for (Iterator contextMessageIter = contextMessageItems.iterator();
                                                contextMessageIter.hasNext();) {
                MessageItem currentMessageItem = (MessageItem)contextMessageIter.next();

                List msgs = messageDao.getNonContextMessages(
                                    userSession.getUserId(),
                                    userSession.getSessionId(),
                                    currentMessageItem.getContextMessageId());

                setProcessedFlagToError(msgs, "Context message has an ERROR.", null);

            } //end of looping of all context messages
        } //end of looping of userSession pairs
    }

    /**
     * Returns a number format.
     * @return a number format
     */
    private NumberFormat getNumberFormat() {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(1);
        nf.setMinimumFractionDigits(1);
        return nf;
    }

    /**
     * Process and Save Lonely Tool Messages.
     * @param currentMessageItem the current message item being processed.
     * @param userId the user Id for the owner of the messages.
     * @param sessionId The session Id of the messages.
     * @param contextMessage The context message for the messages.
     * @return true if at least one message processed, false otherwise.
     */
    private boolean processToolOnlyMessages(MessageItem currentMessageItem,
            String userId, String sessionId,
            ContextMessage contextMessage) {
        boolean messageProcessed = false;
        boolean errorFree = true;

        List toolOnly = messageDao.getToolMessages(userId, sessionId,
                contextMessage.getContextId());
        if (logger.isDebugEnabled()) {
            logger.debug("Retrieved " + toolOnly.size() + " tool only messages for "
                    + userId + "/" + contextMessage.getContextId() + " user/context id.");
        }

        for (Iterator titer = toolOnly.iterator(); titer.hasNext();) {
            List toolMsgItemList = new ArrayList();

            int idx = 0;
            while (idx < BULK_PAIR_LIMIT && titer.hasNext()) {
                MessageItem toolMsgItem = (MessageItem)titer.next();
                toolMsgItemList.add(toolMsgItem);
                idx++;
            }

            if (saveToolList(contextMessage, toolMsgItemList)) {
                messageProcessed = true;
            } else {
                errorFree = false;
            }
        }

        if (!errorFree) {
            String errorMsg = "processToolOnlyMessages returned false.";
            setProcessedFlagToError(currentMessageItem, errorMsg, null);
        }

        return messageProcessed;
    }

    /**
     * Checks whether a given message is valid for purposes of determining if an
     * encountered error was due to bad xml, or bad code.
     * @param messageItem the message item to test.
     * @return true if valid, false otherwise.
     * @throws IOException Error building a temp file.
     */
    private boolean isValidMessage(MessageItem messageItem) throws IOException {

        XMLValidator validator = new XMLValidator(false);

        File tmpFile = null;
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        BufferedWriter bufferedWriter = null;
        boolean success = false;
        try {
            tmpFile = File.createTempFile("validateMessageTmpFile", ".xml");
            tmpFile.deleteOnExit();
            fos = new FileOutputStream(tmpFile, false);
            osw =  new OutputStreamWriter(fos, "UTF8");
            bufferedWriter = new BufferedWriter(osw);

            String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "\n<tutor_related_message_sequence"
                + "\n    xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'"
                + "\n    xsi:noNamespaceSchemaLocation="
                + "'http://pslcdatashop.org/dtd/tutor_message_v"
                    + messageItem.getXmlVersion() + ".xsd'"
                + "\n    version_number=\"" + messageItem.getXmlVersion() + "\" >"
                + "\n\t" + messageItem.getInfo()
                + "\n</tutor_related_message_sequence>";

            bufferedWriter.write(xml);

            success = validator.isValidFile(tmpFile);
            tmpFile.delete();
        } finally {
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (osw != null) {
                osw.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
        return success;
    }

    /**
     * Set the processed flag to error and info field for a list of message items.
     * This should only be called when it doesn't need to be put in a transaction.
     * @param messageItems a list of message items to update
     * @param errorMsg any information messages that are pertinent, useful
     * @param exception the exception that caused the error
     */
    private void setProcessedFlagToError(List messageItems,
            String errorMsg, Exception exception) {
        for (Iterator iter = messageItems.iterator(); iter.hasNext();) {
            MessageItem messageItem = (MessageItem)iter.next();
            setProcessedFlagToError(messageItem, errorMsg, exception);
        }
    }

    /**
     * Set the processed flag to error and info field of the given message item.
     * This should only be called when it doesn't need to be put in a transaction.
     * @param messageItem The MessageItem that should be updated.
     * @param errorMsg any information messages that are pertinent, useful
     * @param exception the exception that caused the error
     */
    private void setProcessedFlagToError(MessageItem messageItem,
            String errorMsg, Exception exception) {
        messageItem.setProcessedFlag(MessageItem.ERROR_FLAG);
        messageItem.setProcessedTime(new Date());
        messageItem.setProcessedInfo(errorMsg);
        messageDao.saveOrUpdate(messageItem);

        String prefix = "For message user/session/contextMsgId "
                    + messageItem.getUserId() + "/"
                    + messageItem.getSessionTag() + "/ "
                    + messageItem.getContextMessageId();
        if (messageItem.getTransactionId() != null) {
            prefix += " tx(" + messageItem.getTransactionId() + ")";
        }
        prefix += " where GUID = " + messageItem.getGuid() + ". ";

        if (exception != null) {
            logger.error(prefix + errorMsg, exception);
        } else {
            logger.error(prefix + errorMsg);
        }
    }

    /** Blank error message. */
    private static final String BLANK_ERROR_MSG = "<blank error message>";

    /**
     * Save context message to the database.
     * @param contextMessage The context message that should be saved.
     * @param messageItem the messageItem that should be updated.
     * @return the "filled in" context message
     */
    public ContextMessage saveContextData(ContextMessage contextMessage,
            MessageItem messageItem) {
        messageDao = DaoFactory.HIBERNATE.getMessageDao();
        ContextMessage newMsg = null;

        boolean errorFlag = false;
        String errorMsg = BLANK_ERROR_MSG;

        if (messageItem.getUserId() == null) {
            errorFlag = true;
            errorMsg = "UserId is null.";
        } else if (messageItem.getUserId().length() == 0) {
            errorFlag = true;
            errorMsg = "UserId is empty.";
        } else if (messageItem.getSessionTag() == null) {
            errorFlag = true;
            errorMsg = "SessionTag is null.";
        } else if (messageItem.getSessionTag().length() == 0) {
            errorFlag = true;
            errorMsg = "SessionTag is empty.";
        } else if (messageItem.getTime() == null) {
            errorFlag = true;
            errorMsg = "Time is null.";
        }

        if (!errorFlag) {
            ContextMessageTransactionCallback cb
                = new ContextMessageTransactionCallback(
                        contextMessage, messageItem, instantiationTime);
            newMsg = (ContextMessage)transactionTemplate.execute(cb);
            if (cb.isErrorFlag()) {
                errorFlag = true;
                errorMsg = cb.getErrorMessages().toString();
                newMsg = null;
            } else if (newMsg == null) {
                errorFlag = true;
                errorMsg = "Context message object is null.";
            }
        }

        if (errorFlag) {
            //update the messageItem to indicate an error occurred
            setProcessedFlagToError(messageItem, errorMsg, null);
        }
        return newMsg;
    }

    /**
     * Save tutor and tool messages to the database.
     * @param contextMessage The context message that contains dataset, student and etc.
     * @param toolTutorPairList List of ToolTutorPairs
     * and contains list of messageItems that should be updated for the processed status.
     * @return boolean of success.
     */
    private boolean saveTutorToolPairList(ContextMessage contextMessage,
            List toolTutorPairList) {
        BulkTransactionCallback btc
            = new BulkTransactionCallback(contextMessage, toolTutorPairList,
                BulkTransactionCallback.TUTOR_TOOL_PAIR_TYPE);
        Boolean successFlag = (Boolean)transactionTemplate.execute(btc);
        return successFlag;
    }

    /**
     * Save a single tool message to the database.
     * @param contextMessage The context message that contains dataset, student and etc.
     * @param toolMessageList List of tool messages to be saved.
     * @return boolean of success.
     */
    private boolean saveToolList(ContextMessage contextMessage, List toolMessageList) {
        BulkTransactionCallback btc
            = new BulkTransactionCallback(contextMessage, toolMessageList,
                    BulkTransactionCallback.TOOL_ONLY_TYPE);
        Boolean successFlag = (Boolean)transactionTemplate.execute(btc);
        return successFlag;
    }

    /**
     * Take a list of ToolTutorMessageItemPair objects and combine pairs where
     * necessary and return a list of ToolTutorMessagePair objects.  Each of the
     * pairs returned will represent one transaction.  This special reasoning is
     * for CTAT which will return 2 or 3 responses to a student action.  One with
     * the skills and SAI, one with the tutor advice and one response with the
     * evaluation text.
     * <P>
     * Note that this method takes care of getting the proper XML parser as well.
     *
     * @param msgItemPairList a list of MessageItem pairs from the MessageDao
     * @return a list of tool and tutor message pair, which is an inner class, ToolTutorPair
     */
    private List getToolTutorPairs(List msgItemPairList) {
        List toolTutorPairList = new ArrayList();
        Map transactionIdMap = new HashMap();
        for (Iterator iter = msgItemPairList.iterator(); iter.hasNext();) {
            ToolTutorMessageItemPair msgItemPair = (ToolTutorMessageItemPair)iter.next();
            MessageItem toolMsgItem = msgItemPair.getToolMessageItem();
            MessageItem tutorMsgItem = msgItemPair.getTutorMessageItem();
            String transactionId = toolMsgItem.getTransactionId();

            try {
                // If the transactionId is already in the transaction id map, then
                // that means this transaction id was already seen. If that
                // is the case, then combine the contents into the previous message.
                if (transactionIdMap.containsKey(transactionId)) {
                    // Add the new message content to the one that was already parsed.
                    ToolTutorPair prevPair = (ToolTutorPair)transactionIdMap.get(transactionId);
                    TutorMessage prevTutorMsg = prevPair.getTutorMessageItem();

                    Set prevActionEvalSet =
                        prevTutorMsg.getActionEvaluationsExternal();
                    List prevTutorAdviceSet = prevTutorMsg.getTutorAdvicesExternal();
                    // Parse the new message
                    TutorMessageParser tutorParser =
                        TutorMessageParserFactory.getInstance().get(tutorMsgItem);
                    TutorMessage newTutorMsg = tutorParser.getTutorMessage();
                    Set newActionEvalSet  = newTutorMsg.getActionEvaluationsExternal();
                    List newTutorAdviceSet = newTutorMsg.getTutorAdvicesExternal();
                    Set newSkillSet       = newTutorMsg.getSkillsExternal();
                    // Combine the contents of the new and previous messages, store in the previous.
                    if ((prevActionEvalSet.size() == 0) && (newActionEvalSet.size() > 0)) {
                        prevTutorMsg.setActionEvaluations(newActionEvalSet);
                    }
                    if ((prevTutorAdviceSet.size() == 0) && (newTutorAdviceSet.size() > 0)) {
                        prevTutorMsg.setTutorAdvices(newTutorAdviceSet);
                    }
                    if (newSkillSet.size() > 0) {
                        prevTutorMsg.setSkills(newSkillSet);
                        prevTutorMsg.setProblemItem(newTutorMsg.getProblemItem());
                        prevTutorMsg.copyEventDescriptors(newTutorMsg);
                    }
                    prevPair.addTutorMessageItem(tutorMsgItem);
                } else {
                    // We have not seen this transaction id before.
                    ToolMessageParser toolParser =
                        ToolMessageParserFactory.getInstance().get(toolMsgItem);
                    ToolMessage newToolMsg = toolParser.getToolMessage();
                    // Parse the new message.
                    TutorMessageParser tutorParser =
                        TutorMessageParserFactory.getInstance().get(tutorMsgItem);
                    TutorMessage newTutorMsg = tutorParser.getTutorMessage();
                    // Attempt and Result pairing
                    if (hasSemanticEventWithName(
                            newToolMsg.getSemanticEventsExternal(), SEM_EV_NAME_ATTEMPT)) {
                        if (hasSemanticEventWithName(
                            newTutorMsg.getSemanticEventsExternal(), SEM_EV_NAME_RESULT)) {

                            ToolTutorPair newPair = new ToolTutorPair(newToolMsg, newTutorMsg);
                            newPair.addToolMessageItem(toolMsgItem);
                            newPair.addTutorMessageItem(tutorMsgItem);
                            transactionIdMap.put(transactionId, newPair);
                            toolTutorPairList.add(newPair);
                        } else {
                            logger.info("Tutor message does not have any RESULT semantic events"
                                    + " for tool message: "
                                    + newToolMsg.getSemanticEventsExternal());
                        }
                    // Hint and Hint Request pairing
                    } else if (hasSemanticEventWithName(
                            newToolMsg.getSemanticEventsExternal(), SEM_EV_NAME_HINT_REQUEST)) {
                        if (hasSemanticEventWithName(
                            newTutorMsg.getSemanticEventsExternal(), SEM_EV_NAME_HINT_MSG)) {

                            ToolTutorPair newPair = new ToolTutorPair(newToolMsg, newTutorMsg);
                            newPair.addToolMessageItem(toolMsgItem);
                            newPair.addTutorMessageItem(tutorMsgItem);
                            transactionIdMap.put(transactionId, newPair);
                            toolTutorPairList.add(newPair);
                        } else {
                            logger.info("Tutor message does not have any HINT_MSG semantic events"
                                    + " for tool message: "
                                    + newToolMsg.getSemanticEventsExternal());
                        }
                    // Untutored message
                    } else {
                        ToolTutorPair newPair = new ToolTutorPair(newToolMsg, newTutorMsg);
                        newPair.addToolMessageItem(toolMsgItem);
                        newPair.addTutorMessageItem(tutorMsgItem);
                        transactionIdMap.put(transactionId, newPair);
                        toolTutorPairList.add(newPair);
                    }
                }
            } catch (Exception exception) {
                String errorMsg;
                try {
                    if (new Integer(toolMsgItem.getXmlVersion()) >= MIN_XML_VALIDATE_VERSION
                            && !isValidMessage(toolMsgItem)) {
                        errorMsg = "Invalid tool message.";
                    } else if (new Integer(toolMsgItem.getXmlVersion()) >= MIN_XML_VALIDATE_VERSION
                        && !isValidMessage(tutorMsgItem)) {
                        errorMsg = "Invalid turor message.";
                    } else {
                        errorMsg = "Unexpected error parsing.";
                    }
                } catch (Exception validateException) {
                    errorMsg = "Exception getting XML version.";
                }

                setProcessedFlagToError(toolMsgItem, errorMsg, exception);
                setProcessedFlagToError(tutorMsgItem, errorMsg, exception);

                continue;
            }
        }
        return toolTutorPairList;
    }

    /**
     * Returns true if the list of semantic events has a semantic event
     * with the given name.
     * @param semEvList the list of semantic events
     * @param name the given name
     * @return true if the semantic event exists, false otherwise
     */
    private boolean hasSemanticEventWithName(List semEvList, String name) {
        boolean hasResultSemEv = false;
        if (semEvList.size() > 0) {
            for (Iterator semEvIter = semEvList.iterator(); semEvIter.hasNext();) {
                SemanticEvent semEvent = (SemanticEvent)semEvIter.next();
                if (semEvent.getName() != null && semEvent.getName().equals(name)) {
                    hasResultSemEv = true;
                    break;
                }
            }
        }
        return hasResultSemEv;
    }

    /**
     * Take a list of MessageItems (all tutor messages) objects and combine pairs where
     * necessary and return a list of ToolTutorMessagePair objects.  Each of the
     * pairs returned will represent one transaction.  This special reasoning is
     * for CTAT which will return 2 or 3 responses to a student action.  One with
     * the skills and SAI, one with the tutor advice and one response with the
     * evaluation text.
     * <P>
     * Note that this method takes care of getting the proper XML parser as well.
     *
     * @param msgItemList a list of Tutor Messages as MessageItems from the MessageDao
     * @return a list of tool (which is null) and tutor message pair,
     * which is an inner class, ToolTutorPair

    private List getTutorMessages(List msgItemList) {
        List itemList = new ArrayList();
        Map transactionIdMap = new HashMap();
        for (Iterator iter = msgItemList.iterator(); iter.hasNext();) {

            MessageItem msgItem = (MessageItem)iter.next();
            String transactionId = msgItem.getTransactionId();

            if (transactionIdMap.containsKey(transactionId)) {
                // not new transaction
                ToolTutorPair existingPair = (ToolTutorPair)transactionIdMap.get(transactionId);
                TutorMessage existingItem = existingPair.getTutorMessageItem();

                Set prevActionEvalSet =
                    existingItem.getActionEvaluationsExternal();
                Set prevTutorAdviceSet = existingItem.getTutorAdvicesExternal();

                TutorMessageParser tutorParser =
                    TutorMessageParserFactory.getInstance().get(msgItem);
                TutorMessage newTutorMsg = tutorParser.getTutorMessage();
                Set newActionEvalSet  = newTutorMsg.getActionEvaluationsExternal();
                Set newTutorAdviceSet = newTutorMsg.getTutorAdvicesExternal();
                Set newSkillSet       = newTutorMsg.getSkillsExternal();

                if ((prevActionEvalSet.size() == 0) && (newActionEvalSet.size() > 0)) {
                    existingItem.setActionEvaluations(newActionEvalSet);
                }
                if ((prevTutorAdviceSet.size() == 0) && (newTutorAdviceSet.size() > 0)) {
                    existingItem.setTutorAdvices(newTutorAdviceSet);
                }
                if (newSkillSet.size() > 0) {
                    existingItem.setSkills(newSkillSet);
                    existingItem.setProblemItem(newTutorMsg.getProblemItem());
                    existingItem.copyEventDescriptors(newTutorMsg);
                }
                existingPair.addTutorMessageItem(msgItem);
            } else {
                TutorMessageParser tutorParser =
                    TutorMessageParserFactory.getInstance().get(msgItem);
                TutorMessage newTutorMsg = tutorParser.getTutorMessage();

                ToolTutorPair newMatchedTutors = new ToolTutorPair(null, newTutorMsg);
                newMatchedTutors.addTutorMessageItem(msgItem);
                transactionIdMap.put(transactionId, newMatchedTutors);
                itemList.add(newMatchedTutors);
            }
        }
        return itemList;
    }*/

    /**
     * Display the usage of this utility.
     */
    protected void displayUsage() {
        System.err.println("\nUSAGE: java -classpath ..."
            + " TutorMessageConverter [-dataset dataset_name] [-datasetLevel datasetlevel_name]"
            + " [-datasetLevelTitle datasetlevel_title_name] [-problem problem_name]"
            + " [-email address]");
        System.err.println("Option descriptions:");
        System.err.println("\t-dataset           \t datasetName");
        System.err.println("\t-datasetLevel      \t datasetLevelName");
        System.err.println("\t-datasetLevelTitle \t datasetLevelTitleName");
        System.err.println("\t-problem           \t problemName");
        System.err.println("\t-error");
        System.err.println("\t-e, email           \t send email if major failure");
        System.err.println("\t-h, help           \t this help message ");
    }

    /**
     * Handle the command line arguments.
     * @param args - command line arguments passed into main
     * @return returns null if no exit is required,
     * 0 if exiting successfully (as in case of -help),
     * or any other number to exit with an error status
     */
    protected Integer handleOptions(String[] args) {
        // The value is null if no exit is required,
        // 0 if exiting successfully (as in case of -help),
        // or any other number to exit with an error status
        Integer exitLevel = null;
        if (args != null && args.length != 0) {

            java.util.ArrayList argsList = new java.util.ArrayList();
            for (int i = 0; i < args.length; i++) {
                argsList.add(args[i]);
            }

            // loop through the arguments
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-h")) {
                    displayUsage();
                    exitLevel = 0;
                } else if (args[i].equals("-help")) {
                    displayUsage();
                    exitLevel = 0;
                } else if (args[i].equals("-e") || args[i].equals("-email")) {
                    setSendEmailFlag(true);
                    if (++i < args.length) {
                        setEmailAddress(args[i]);
                    } else {
                        System.err.println(
                            "Error: a email address must be specified with this argument");
                        displayUsage();
                        exitLevel = 1;
                    }
                } else if (args[i].equals("-dataset")) {
                    if (++i < args.length) {
                        defaultDatasetName = args[i];
                    } else {
                        System.err.println(
                            "Error: a name must be specified with this argument");
                        displayUsage();
                        exitLevel = 1;
                    }
                } else if (args[i].equals("-datasetLevel")) {
                    if (++i < args.length) {
                        defaultDatasetLevelName = args[i];
                    } else {
                        System.err.println(
                            "Error: a name must be specified with this argument");
                        displayUsage();
                        exitLevel = 1;
                    }
                } else if (args[i].equals("-datasetLevelTitle")) {
                    if (++i < args.length) {
                        defaultDatasetLevelTitleName = args[i];
                    } else {
                        System.err.println(
                            "Error: a name must be specified with this argument");
                        displayUsage();
                        exitLevel = 1;
                    }
                } else if (args[i].equals("-problem")) {
                    if (++i < args.length) {
                        defaultProblemName = args[i];
                    } else {
                        System.err.println(
                            "Error: a name must be specified with this argument");
                        displayUsage();
                        exitLevel = 1;
                    }
                } else if (args[i].equals("-error")) {
                    retryErrorMessagesFlag = true;
                } else {
                    System.err.println("Error: improper command line arguments: " + argsList);
                    displayUsage();
                    exitLevel = 1;
                } // end if then else

                // If the exitLevel was set, then break out of the loop
                if (exitLevel != null) {
                    break;
                }
            } // end for loop
        }
        return exitLevel;
    } // end handleOptions

    /**
     * Handle the dataset, dataset level, dataset level title and problem default.
     * If defaults are not set by the command lines, use the DEFAULT...
     *
     */
    protected void handleDefaults() {
        if (defaultDatasetName == null || defaultDatasetName.trim().equals("")) {
            defaultDatasetName = DEFAULT_DATASET_NAME;
        }
        if (defaultDatasetLevelName == null || defaultDatasetLevelName.trim().equals("")) {
            defaultDatasetLevelName = DEFAULT_DATASETLEVEL_NAME;
        }
        if (defaultDatasetLevelTitleName == null
                || defaultDatasetLevelTitleName.trim().equals("")) {
            defaultDatasetLevelTitleName = DEFAULT_DATASETLEVEL_TITLE_NAME;
        }
        if (defaultProblemName == null || defaultProblemName.trim().equals("")) {
            defaultProblemName = DEFAULT_PROBLEM_NAME;
        }
    }

    /**
     * Run the Tutor Message Converter.
     * USAGE: java -classpath ... TutorMessageConverter [-dataset dataset_name]
     *     [-dataset level datasetlevel_name]
     *     [-problem problem_name]
     * @param args - command line arguments
     */
    public static void main(String[] args) {
        Logger logger = Logger.getLogger("TutorMessageConverter.main");
        String version = VersionInformation.getReleaseString();
        logger.info("TutorMessageConverter starting (" + version + ")...");
        TutorMessageConverter tutorMessageConverter
            = ExtractorFactory.DEFAULT.getTutorMessageConverter();
        
        // Initialize DataShop instance from the database
        DataShopInstance.initialize();

        boolean playMode = ImportQueue.isInPlayMode();
        // If an exitLevel exists, it will be used to exit
        // before the TMC is run; otherwise exitLevel is null so continue.
        Integer exitLevel = null;
        // Handle the command line options:
        // The handleOptions method is called before entering the try-catch block
        // because it isn't affected by the ImportQueue.
        exitLevel = tutorMessageConverter.handleOptions(args);
        try {
            // If exitLevel is null, then proceed with conversion
            if (exitLevel == null) {
                // Pause the IQ
                if (playMode) {
                    logger.info("main:: Pausing the ImportQueue.");
                    ImportQueue.pause();
                }
                //set the default values for dataset, dataset level,
                // dataset level title, and problem.
                tutorMessageConverter.handleDefaults();
                tutorMessageConverter.extract();
            }
        } catch (Throwable throwable) {
            // Log error and send email if this happens!
            tutorMessageConverter.sendErrorEmail(logger, "Unknown error in main method.",
                    throwable);
            exitLevel = 1;
        } finally {
            if (playMode) {
                logger.info("main:: Unpausing the ImportQueue.");
                ImportQueue.play();
            }
            exitOnStatus(exitLevel);
            logger.info("TutorMessageConverter done.");
        }
    } // end main

    /**
     * Inner class to next transactions for speed.
     */
    public class BulkTransactionCallback implements TransactionCallback {

        /** Debug logging. */
        private final Logger logger = Logger.getLogger(getClass().getName());

        /** Type of bulk transaction callback for handling tutor/tool pairs. */
        public static final String TUTOR_TOOL_PAIR_TYPE = "tutor_tool_pair_type";

        /** Type of bulk transaction callback for handling only tool messages. */
        public static final String TOOL_ONLY_TYPE = "tool_only_type";

        /**
         * The contextMessage which has dataset, student, school and etc
         * information that will be used.
         */
        private ContextMessage contextMessage;

        /** List of tool-tutor pairs. */
        private List itemsToProcess;

        /** Cache of known items. */
        private ItemCache itemCache;

        /** the type of bulk transaction */
        private String type;


        /**
         * Constructor.
         * @param contextMessage ContextMessage that contains information needed
         * @param itemsToProcess List of Items to save
         * @param type the type of messages contained in this bulk transaction.
         */
        public BulkTransactionCallback(ContextMessage contextMessage,
                List itemsToProcess, String type) {
            this.contextMessage = contextMessage;
            this.itemsToProcess = itemsToProcess;
            if (!type.equals(TUTOR_TOOL_PAIR_TYPE) && !type.equals(TOOL_ONLY_TYPE)) {
                throw new IllegalArgumentException("Unknown type for bulk transaction class");
            }
            this.type = type;
        }

        /**
         * Do a bunch of pairs at a time.  Hoping to improve speed.
         * @param ts TransactionStatus.
         * @return true if successful, false otherwise
         */
        public Object doInTransaction(TransactionStatus ts) {
            if (type.equals(TUTOR_TOOL_PAIR_TYPE)) {
                return processTutorToolPairs(ts);
            } else if (type.equals(TOOL_ONLY_TYPE)) {
                return processToolMessages(ts);
            } else {
                logger.error("Unknown type of bulk transaction wrapper :: " + type);
                return Boolean.FALSE;
            }
        }

        /**
         * Process a tutor/tool message set.
         * @param ts The bulk transaction status.
         * @return Boolean of success
         */
        private Boolean processTutorToolPairs(TransactionStatus ts) {
            try {
                itemCache = new ItemCache();
                for (int i = 0, n = itemsToProcess.size(); i < n; i++) {
                    TutorMessageConverter.ToolTutorPair pair =
                        (TutorMessageConverter.ToolTutorPair)itemsToProcess.get(i);
                    if (!saveTutorToolData(contextMessage, pair)) {
                        logger.error("There was an error processing the tutor/tool pairs,"
                                + "rolling back transaction.");
                        ts.setRollbackOnly();
                        return Boolean.FALSE;
                    }
                }
                itemCache.clearAll();
            } catch (Throwable exception) {
                logger.error(exception.toString(), exception);
                ts.setRollbackOnly();
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }

        /**
         * Process a tool only message set.
         * @param ts The bulk transaction status.
         * @return Boolean of success
         */
        private Boolean processToolMessages(TransactionStatus ts) {
            try {
                itemCache = new ItemCache();
                for (int i = 0, n = itemsToProcess.size(); i < n; i++) {
                    MessageItem toolMsgItem =
                        (MessageItem)itemsToProcess.get(i);

                    if (!saveToolData(contextMessage, toolMsgItem)) {
                        logger.error("There was an error processing the lonely tool messages,"
                                + "rolling back transaction.");
                        ts.setRollbackOnly();
                        return Boolean.FALSE;
                    }
                }
                itemCache.clearAll();
            } catch (Throwable exception) {
                logger.error(exception.toString(), exception);
                ts.setRollbackOnly();
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }

        /**
         * Save tutor and tool messages to the database.
         * @param contextMessage The context message that contains dataset, student and etc.
         * @param toolTutorPair the ToolTutorPair that contains tutor and tool should be saved
         * and contains list of messageItems that should be updated for the processed status.
         * @return boolean of success, a false indicates a failure of some kind.
         */
        public boolean saveTutorToolData(ContextMessage contextMessage,
                ToolTutorPair toolTutorPair) {
            ToolTutorTransactionCallback tt
                = new ToolTutorTransactionCallback(contextMessage, toolTutorPair, itemCache);
            ItemCache newCache = null;

            transactionTemplate.setPropagationBehavior(
                    TransactionDefinition.PROPAGATION_NESTED);
            newCache = (ItemCache)transactionTemplate.execute(tt);

            if (newCache == null) {
                String errorMsg = "Cache is null.";
                setProcessedFlagToError(toolTutorPair.getToolMessageItemList(),
                        errorMsg, null);
                setProcessedFlagToError(toolTutorPair.getTutorMessageItemList(),
                        errorMsg, null);
                return false;
            } else {
                itemCache = newCache;
                return true;
            }
        }

        /**
         * Save messages to the database.
         * @param contextMessage The context message that contains dataset, student and etc.
         * @param toolMsgItem the Tool MessageItem with the data to be saved.
         * @return boolean of success, a false indicates a failure of some kind.
         */
        public boolean saveToolData(ContextMessage contextMessage,
                MessageItem toolMsgItem) {

            ToolTransactionCallback tt
                = new ToolTransactionCallback(contextMessage, toolMsgItem, itemCache);

            ItemCache newCache = null;
            transactionTemplate.setPropagationBehavior(
                    TransactionDefinition.PROPAGATION_NESTED);
            newCache = (ItemCache)transactionTemplate.execute(tt);

            if (newCache == null) {
                String errorMsg = "newCache is null.";
                setProcessedFlagToError(toolMsgItem, errorMsg, null);
                return false;
            } else {
                itemCache = newCache;
                return true;
            }
        }

    } // end inner class BulkTransactionCallback

    /**
     * Inner class to hold a pair of tool and tutor messages.
     */
    public class ToolTutorPair {
        /** The tool message item, can be null. */
        private ToolMessage toolMessage;
        /** The list of message items that went into this pair. */
        private List toolMsgItemList = new ArrayList();
        /** The tutor message item, can be null. */
        private TutorMessage tutorMessage;
        /** The list of message items that went into this pair. */
        private List tutorMsgItemList = new ArrayList();
        /**
         * The constructor.
         * @param tool the tool message item
         * @param tutor the tutor message item
         */
        public ToolTutorPair(ToolMessage tool, TutorMessage tutor) {
            this.toolMessage = tool;
            this.tutorMessage = tutor;
        }
        /**
         * Returns the tool message item.
         * @return the tool message item
         */
        public ToolMessage getToolMessageItem() {
            return toolMessage;
        }
        /**
         * Returns the tutor message item.
         * @return the tutor message item
         */
        public TutorMessage getTutorMessageItem() {
            return tutorMessage;
        }
        /**
         * Returns the list of tool message items that go into this pair.
         * @return a list of MessageItem objects
         */
        public List getToolMessageItemList() {
            return toolMsgItemList;
        }
        /**
         * Add a tool message item.
         * @param toolMsgItem tool message item
         */
        public void addToolMessageItem(MessageItem toolMsgItem) {
            this.toolMsgItemList.add(toolMsgItem);
        }
        /**
         * Returns the list of tutor message items that go into this pair.
         * @return a list of MessageItem objects
         */
        public List getTutorMessageItemList() {
            return tutorMsgItemList;
        }
        /**
         * Add a tutor message item.
         * @param tutorMsgItem tool message item
         */
        public void addTutorMessageItem(MessageItem tutorMsgItem) {
            this.tutorMsgItemList.add(tutorMsgItem);
        }
        /**
         * Returns a string representation of both items.
         * @return a string
         */
        public String toString() {
            return toolMessage + ", " + tutorMessage;
        }
    } // end inner class


    /** An extra level of debug logging. */
    private static final boolean LOG_TIME_FLAG = false;

    /**
     * Log timer level debugging messages.
     * @param logger the logger to use
     * @param msg the debug message
     * @param startTime the previous time stamp
     * @return the new start time
     */
    public static Date logTime(Logger logger, String msg, Date startTime) {
        if (LOG_TIME_FLAG) {
            if (logger.isDebugEnabled()) {
                Date now = new Date();
                long time = now.getTime() - startTime.getTime();
                startTime = now;
                logger.debug("TIME: " + msg + time);
            }
        }
        return startTime;
    }

    /**
     * Return the number of minuses elapsed since the given time as a float.
     * @param startTime Date of the time to calculate from.
     * @return float of elapsed minuses.
     */
    public static float minutesElapsed(Date startTime) {
        final float millisecondsInAMinute = 1000 * 60;
        long timeInMilliseconds = new Date().getTime() - startTime.getTime();
        return (float)timeInMilliseconds / millisecondsInAMinute;
    }

    /**
     * Return the number of seconds elapsed since the given time as a float.
     * @param startTime Date of the time to calculate from.
     * @return float of elapsed seconds
     */
    public static float secondsElapsed(Date startTime) {
        final float millisecondsInASecond = 1000;
        long timeInMilliseconds = new Date().getTime() - startTime.getTime();
        return (float)timeInMilliseconds / millisecondsInASecond;
    }

} // end class TutorMessageConverter
