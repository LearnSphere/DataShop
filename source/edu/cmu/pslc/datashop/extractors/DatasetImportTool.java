/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2006
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.extractors;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import edu.cmu.pslc.datashop.dao.AttemptActionDao;
import edu.cmu.pslc.datashop.dao.AttemptInputDao;
import edu.cmu.pslc.datashop.dao.AttemptSelectionDao;
import edu.cmu.pslc.datashop.dao.CfTxLevelDao;
import edu.cmu.pslc.datashop.dao.ClassDao;
import edu.cmu.pslc.datashop.dao.ConditionDao;
import edu.cmu.pslc.datashop.dao.CustomFieldDao;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.DatasetLevelDao;
import edu.cmu.pslc.datashop.dao.DomainDao;
import edu.cmu.pslc.datashop.dao.FeedbackDao;
import edu.cmu.pslc.datashop.dao.LearnlabDao;
import edu.cmu.pslc.datashop.dao.ProblemDao;
import edu.cmu.pslc.datashop.dao.ProjectDao;
import edu.cmu.pslc.datashop.dao.SchoolDao;
import edu.cmu.pslc.datashop.dao.SessionDao;
import edu.cmu.pslc.datashop.dao.SkillDao;
import edu.cmu.pslc.datashop.dao.SkillModelDao;
import edu.cmu.pslc.datashop.dao.SubgoalAttemptDao;
import edu.cmu.pslc.datashop.dao.SubgoalDao;
import edu.cmu.pslc.datashop.dao.TransactionDao;
import edu.cmu.pslc.datashop.dto.ItemCache;
import edu.cmu.pslc.datashop.helper.SystemLogger;
import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.servlet.datasetinfo.DatasetInfoEditHelper;
import edu.cmu.pslc.datashop.item.AttemptActionItem;
import edu.cmu.pslc.datashop.item.AttemptInputItem;
import edu.cmu.pslc.datashop.item.AttemptSAI;
import edu.cmu.pslc.datashop.item.AttemptSelectionItem;
import edu.cmu.pslc.datashop.item.ClassItem;
import edu.cmu.pslc.datashop.item.ConditionItem;
import edu.cmu.pslc.datashop.item.CustomFieldItem;
import edu.cmu.pslc.datashop.item.CfTxLevelItem;
import edu.cmu.pslc.datashop.item.CustomFieldNameValueItem;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.DatasetLevelItem;
import edu.cmu.pslc.datashop.item.DomainItem;
import edu.cmu.pslc.datashop.item.FeedbackItem;
import edu.cmu.pslc.datashop.item.LearnlabItem;
import edu.cmu.pslc.datashop.item.ProblemItem;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.SchoolItem;
import edu.cmu.pslc.datashop.item.SessionItem;
import edu.cmu.pslc.datashop.item.SkillItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.item.StudentItem;
import edu.cmu.pslc.datashop.item.SubgoalAttemptItem;
import edu.cmu.pslc.datashop.item.SubgoalItem;
import edu.cmu.pslc.datashop.item.TransactionItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.util.LogUtils;
import edu.cmu.pslc.datashop.util.MemoryUtils;
import edu.cmu.pslc.datashop.util.VersionInformation;
import edu.cmu.pslc.importdata.DatasetVerificationTool;
import edu.cmu.pslc.importdata.ImportConstants;
import edu.cmu.pslc.importdata.ImportUtilities;

import static edu.cmu.pslc.logging.util.DateTools.getDate;


/**
 * Tool used to import tab-delimited dataset files into the DataShop.
 * Uses the DatasetVerificationTool to check the dataset file for validity.
 * If file structure is valid, data is inserted into the DataShop.
 *
 * @author Kyle A Cunningham
 * @version $Revision: 14294 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2017-09-29 09:42:01 -0400 (Fri, 29 Sep 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DatasetImportTool extends AbstractExtractor {
    /** Debug logger. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** The name of this tool, used in displayUsage method. */
    private static final String TOOL_NAME = DatasetImportTool.class.getSimpleName();

    /** Number of transactions to return and process in a chunk */
    private static final int BATCH_SIZE = 1000;
    /** default delimiter */
    private static final String DEFAULT_DELIMITER = "\t";
    /** empty string */
    private static final String EMPTY_STRING = "";
    /** actual student id prefix*/
    private static final String ACTUAL_STUDENT_ID_PREFIX = "ds_student_";
    /** Maximum custom field value length */
    private static final int MAX_CUSTOM_FIELD_LENGTH = 65000;

    /** The transactionTemplate is used for transaction callback.*/
    private TransactionTemplate transactionTemplate;
    /** dataset name. */
    private static String datasetName;
    /** The directory name from the command line. */
    private static String inputDirectoryName;
    /** file name. */
    private static String inputFileName;
    /** domain name. */
    private static String inputDomainName;
    /** learnlab name. */
    private static String inputLearnlabName;
    /** Flag on whether to show the memory usage or not (default). */
    private static boolean showMemoryUsageFlag = false;

    /** buffered writer for output processing */
    private static Writer outWriter;
    /** Counter for lines processed */
    private static int lineCounter = 1;
    /** Total line count to process */
    private static int linesToProcess = 0;

    /** ArrayList containing processed SkillModel Items (already committed)*/
    private static ArrayList<SkillModelItem> skillModels = new ArrayList();

    /**
     * ItemCache used to hold processed items for this dataset.
     * Helps us save on database reads.
     */
    private static ItemCache itemCache = new ItemCache();

    /** Set of variables to indicate if various optional columns are present. */
    /** Indicates if row is included in dataset file. */
    private boolean rowPresent = false;
    /** Indicates if sample is included in dataset file. */
    private boolean samplePresent = false;
    /** Indicates if time zone is included in dataset file */
    private boolean timeZonePresent = false;
    /** Indicates if duration is included in dataset file. */
    private boolean durationPresent = false;
    /** Indicates if student response type is included in dataset file */
    private boolean studentResponseTypePresent = false;
    /** Indicates if student response subtype is included in dataset file */
    private boolean studentResponseSubtypePresent = false;
    /** Indicates if tutor response subtype is included in dataset file */
    private boolean tutorResponseTypePresent = false;
    /** Indicates if tutor response subtype is included in dataset file */
    private boolean tutorResponseSubtypePresent = false;
    /** Indicates if step name is included in the dataset file */
    private boolean stepNamePresent = false;
    /** Indicates if attempt at step is included in dataset file */
    private boolean attemptAtStepPresent = false;
    /** Indicates if outcome is included in dataset file */
    private boolean outcomePresent = false;
    /** Indicates if feedback text is included in dataset file */
    private boolean feedbackTextPresent = false;
    /** Indicates if feedback classification is included in dataset file */
    private boolean feedbackClassificationPresent = false;
    /** Indicates if help level is included in dataset file */
    private boolean helpLevelPresent = false;
    /** Indicates if total # hints is included in dataset file */
    private boolean totalNumHintsPresent = false;
    /** Indicates if condition(s) included in dataset file */
    private boolean conditionPresent = false;
    /** Indicates if knowledge component(s) included in dataset file */
    private boolean kcPresent = false;
    /** Indicates if KC category is included in dataset file */
    private boolean kcCategoryPresent = false;
    /** Indicates if school is included in dataset file */
    private boolean schoolPresent = false;
    /** Indicates if class is included in dataset file */
    private boolean classPresent = false;
    /** Indicates if custom field(s) included in dataset file */
    private boolean cfPresent = false;
    /** Flag for the end of file. */
    private boolean eof = false;

    /** Number of Student columns */
    private Integer maxStudentCount = new Integer(0);
    /** Number of Dataset columns */
    private Integer maxDatasetLevelCount = new Integer(0);
    /** Number of Selection columns */
    private Integer maxSelectionCount = new Integer(0);
    /** Number of Action Columns */
    private Integer maxActionCount = new Integer(0);
    /** Number of Input columns */
    private Integer maxInputCount = new Integer(0);
    /** Number of Conditions */
    private Integer maxConditionCount = new Integer(0);
    /** Number of Skills */
    private Integer maxSkillCount = new Integer(0);
    /** Number of Custom Field columns */
    private Integer maxCustomFieldCount = new Integer(0);
    /** ArrayList containing Dataset Level Titles */
    private ArrayList datasetLevelTitles = new ArrayList();
    /** ArrayList containing Skill Model names as Strings */
    private ArrayList skillModelNames = new ArrayList();
    /** Set containing Custom Field Names */
    private ArrayList customFieldNames = new ArrayList();


    /**
     * Default Constructor.
     */
    public DatasetImportTool() { };

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
     * Set up some variables necessary for processing the dataset file.
     * @param dvt reference to the DatasetVerificationTool
     */
    private void init(DatasetVerificationTool dvt) {
        this.eof = false;
        this.attemptAtStepPresent = dvt.isAttemptAtStepPresent();
        this.cfPresent = dvt.isCfPresent();
        this.classPresent = dvt.isClassPresent();
        this.conditionPresent = dvt.isConditionPresent();
        this.durationPresent = dvt.isDurationPresent();
        this.feedbackClassificationPresent = dvt.isFeedbackClassificationPresent();
        this.feedbackTextPresent = dvt.isFeedbackTextPresent();
        this.helpLevelPresent = dvt.isHelpLevelPresent();
        this.kcPresent = dvt.isKcPresent();
        this.kcCategoryPresent = dvt.isKcCategoryPresent();
        this.outcomePresent = dvt.isOutcomePresent();
        this.rowPresent = dvt.isRowPresent();
        this.samplePresent = dvt.isSamplePresent();
        this.schoolPresent = dvt.isSchoolPresent();
        this.stepNamePresent = dvt.isStepNamePresent();
        this.studentResponseTypePresent = dvt.isStudentResponseTypePresent();
        this.studentResponseSubtypePresent = dvt.isStudentResponseSubtypePresent();
        this.timeZonePresent = dvt.isTimeZonePresent();
        this.totalNumHintsPresent = dvt.isTotalNumHintsPresent();
        this.tutorResponseTypePresent = dvt.isTutorResponseTypePresent();
        this.tutorResponseSubtypePresent = dvt.isTutorResponseSubtypePresent();
        this.maxActionCount = dvt.getMaxActionCount();
        this.maxConditionCount = dvt.getMaxConditionCount();
        this.maxCustomFieldCount = dvt.getMaxCustomFieldCount();
        this.maxDatasetLevelCount = dvt.getMaxDatasetLevelCount();
        this.maxInputCount = dvt.getMaxInputCount();
        this.maxSelectionCount = dvt.getMaxSelectionCount();
        this.maxSkillCount = dvt.getMaxSkillCount();
        this.maxStudentCount = dvt.getMaxStudentCount();
        this.datasetLevelTitles = dvt.getDatasetLevelTitles();
        this.skillModelNames = dvt.getSkillModelNames();
        this.customFieldNames = dvt.getCustomFieldNames();
        linesToProcess = dvt.getLinesProcessed() - 1;
        if (logger.isDebugEnabled()) {
            logger.debug("kcPresent : " + kcPresent);
            logger.debug("dsLevel count is: " + maxDatasetLevelCount);
            logger.debug("action count is: " + maxActionCount);
            logger.debug("input count is: " + maxInputCount);
            logger.debug("selection count is: " + maxSelectionCount);
            logger.debug("cfPresent = " + cfPresent + " customFieldNames.size: "
                    + customFieldNames.size());
            logger.debug("tutorResponseTypePresent: " + tutorResponseTypePresent);
            logger.debug("studentResponseTypePresent: " + studentResponseTypePresent);
        }
        // initialize the skillModels, but without a dataset item yet
        if (kcPresent) {
            initializeSkillModels();
        }
    } // end method

    /**
     * Creates the SkillModelItems for the new dataset.
     * SkillModelItems are processed (checking against itemCache and inserting
     * into the database) by processSkillModels(), which is called by
     * doInTransaction().  Order of the list is important, so an ArrayList
     * opposed to a HashSet is utilized.
     */
    private void initializeSkillModels() {
        skillModels = new ArrayList<SkillModelItem>();
        for (String name : (List<String>)skillModelNames) {
            SkillModelItem skillModelItem = new SkillModelItem();
            skillModelItem.setSkillModelName(name);
            skillModelItem.setGlobalFlag(true);
            skillModelItem.setStatus(SkillModelItem.STATUS_NOT_READY);
            skillModelItem.setLfaStatus(SkillModelItem.LFA_STATUS_QUEUED);
            skillModelItem.setSource(SkillModelItem.SOURCE_IMPORTED);
            skillModelItem.setMappingType(SkillModelItem.MAPPING_CORRECT_TRANS);
            skillModels.add(skillModelItem);
        }
    }

    /**
     * Finish processing the DatasetItem for the new dataset file.  The datasetItem is
     * initialized by initializeDataset(), with the exception of the project.
     * Check to see if a dataset exists with the same dataset_name.
     * If an error occurs, an appropriate message is printed and the system exits processing.
     * @param loggedToSystemLogTableFlag indicates whether the system log table was updated
     * @param newDatasetName name of the new dataset
     * @return the new dataset item with the given dataset name
     */
    private DatasetItem getDatasetItem(boolean loggedToSystemLogTableFlag, String newDatasetName) {
        DatasetItem newDatasetItem = null;

        DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();

        List existingDatasetItems = datasetDao.find(newDatasetName);
        if (existingDatasetItems.size() == 0) {
            newDatasetItem = new DatasetItem();
            newDatasetItem.setDatasetName(newDatasetName);

            // Find and/or create the Unclassified project
            // so that we can set the project of the new dataset.
            ProjectItem projectItem = null;
            Collection projectList = projectDao.find(ProjectItem.UNCLASSIFIED);
            for (Iterator iter = projectList.iterator(); iter.hasNext();) {
                projectItem = (ProjectItem)iter.next();
                break;
            }
            if (projectItem == null) {
                projectItem = new ProjectItem();
                projectItem.setProjectName(ProjectItem.UNCLASSIFIED);
                projectDao.saveOrUpdate(projectItem);
            }

            if (projectItem != null) {
                newDatasetItem.setProject(projectItem);
            }

            // Set the domain of the new dataset.
            DomainDao domainDao = DaoFactory.DEFAULT.getDomainDao();
            DomainItem domainItem = null;
            domainItem = domainDao.findByName(inputDomainName);
            if (domainItem != null) {
                newDatasetItem.setDomain(domainItem);
            }

            // Set the learnlab of the new dataset.
            LearnlabDao learnlabDao = DaoFactory.DEFAULT.getLearnlabDao();
            LearnlabItem learnlabItem = null;
            learnlabItem = learnlabDao.findByName(inputLearnlabName);
            if (learnlabItem != null) {
                newDatasetItem.setLearnlab(learnlabItem);
            }
            datasetDao.saveOrUpdate(newDatasetItem);
            List datasetItemList = datasetDao.find(newDatasetName);
            if (datasetItemList.size() == 0) {
                logger.error("Could not create and retreive the DatasetItem with this name: "
                        + newDatasetName);
                return null;
            } else {
                newDatasetItem = (DatasetItem)datasetItemList.get(0);

                SystemLogger.log(newDatasetItem, SystemLogger.ACTION_CREATE_DATASET,
                        TOOL_NAME + ": Created new dataset '"
                        + newDatasetItem.getDatasetName() + " (" + newDatasetItem.getId() + ")");

                if (logger.isDebugEnabled()) {
                    logger.debug("New dataset name is: "
                        + newDatasetItem.getDatasetName() + " (" + newDatasetItem.getId() + ")");
                }
            }
        } else if (existingDatasetItems.size() == 1) {
            List datasetItemList = datasetDao.find(datasetName);
            newDatasetItem = (DatasetItem)datasetItemList.get(0);

            if (!loggedToSystemLogTableFlag) {
                SystemLogger.log(newDatasetItem, SystemLogger.ACTION_MODIFY_DATASET,
                        TOOL_NAME + ": Adding to dataset '"
                        + newDatasetItem.getDatasetName() + " (" + newDatasetItem.getId() + ")");
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Existing dataset is: "
                        + newDatasetItem.getDatasetName() + " (" + newDatasetItem.getId() + ")");
            }
        } else {
            logger.error("Found multiple datasets that match '" + datasetName + ".");
            System.exit(-1);
        }
        return newDatasetItem;
    } // end method getDatasetItem

    /**
     * Process a single batch of dataset lines to import.
     * @param loggedToSystemLogTableFlag indicates whether the system log table was updated
     * @param inputReader a buffered reader
     * @return flag indicating whether call was successful
     */
    private boolean insertTheLines(boolean loggedToSystemLogTableFlag, BufferedReader inputReader) {
        return (Boolean)transactionTemplate.execute(
                new BulkTransactionCallback(loggedToSystemLogTableFlag, inputReader));
    }

    /**
     * Processes session items for a given DatasetLineItem.  Responsible for creating a
     * new SessionItem and assigning some attributes.
     * @param datasetItem the dataset item
     * @param dli the dataset line to process
     * @return a new SessionItem
     */
    private SessionItem processSession(DatasetItem datasetItem,
            DatasetLineItem dli) {
        SessionDao sessionDao = DaoFactory.DEFAULT.getSessionDao();
        String sessionTag = dli.getSessionID();
        SessionItem session = new SessionItem();

        session.setSessionTag(sessionTag);
        session.setStartTime(getDate(dli.getTime()));
        session.setDataset(datasetItem);

        // check the itemCache to see if we already have this session
        SessionItem cached = checkSession(session, itemCache.getExistingSessions());

        if (cached != null) {
            session = cached;
            logDebug("processSession(): Found session in the cache: ", sessionTag);
        } else {
            // we don't have it in the cache so check the database
            SessionItem dbSess = checkSession(session, sessionDao.find(sessionTag));

            if (dbSess != null) {
                session = dbSess;
                logDebug("processSession(): Found session in the db: ", session.getSessionTag());
            } else {
                // we don't have it in the cache or database so create a new one
                logDebug("processSession(): Added a new session: ", session);
            }
            itemCache.addSession(session);
        }

        sessionDao.saveOrUpdate(session);
        if (logger.isDebugEnabled()) {
            for (StudentItem st : (List<StudentItem>)session.getStudent()) {
                logDebug("student: ", st.getId());
            }
        }

        return session;
    }

    /** Checks the session items.
     * @param session the session
     * @param sessions the collection of sessions
     * @return a session item if found, null otherwise
     */
    private SessionItem checkSession(SessionItem session, Collection<SessionItem> sessions) {
        for (SessionItem s : sessions) {
            if (s.getSessionTag().equals(session.getSessionTag())
                    && s.getDataset().equals(session.getDataset())) {
                return s;
            }
        }
        return null;
    }

    /**
     * Processes a subgoal item for a given DatasetLineItem.
     * @param subgoalName the name of the step/subgoal
     * @param problem the problem item this subgoal corresponds to
     * @return a new SubgoalItem
     */
    private SubgoalItem processSubgoal(String subgoalName, ProblemItem problem) {
        if (subgoalName == null || subgoalName.equals(EMPTY_STRING)) {
            logger.info("processSubgoal(): Subgoal Name is empty or null");
            return null;
        } else {
            SubgoalDao subgoalDao = DaoFactory.DEFAULT.getSubgoalDao();
            SubgoalItem subgoal = new SubgoalItem();
            subgoal.setSubgoalName(subgoalName);
            subgoal.setProblem(problem);
            subgoal.setGuid(subgoalDao.generateGUID(subgoal));

            boolean found = false;

            for (SubgoalItem cached : itemCache.getExistingSubgoals()) {
                found = cached.getSubgoalName().equals(subgoal.getSubgoalName())
                && cached.getProblem().equals(subgoal.getProblem());
                if (found) {
                    subgoal = cached;
                    logDebug("processSubgoal(): Found the subgoal in the cache.");
                }
            }
            // if not found then look in the database
            if (!found) {
                List<SubgoalItem> subgoals = problem.getSubgoalsExternal();
                for (SubgoalItem dbSubgoal : subgoals) {
                    found = dbSubgoal.getSubgoalName().equals(subgoalName);
                    if (found) { subgoal = dbSubgoal; }
                }
                // if still not found then create a new one
                if (!found) {
                    subgoalDao.saveOrUpdate(subgoal);
                    logDebug("processSubgoals(): Inserting a new SubgoalItem.");
                }
                itemCache.addSubgoal(subgoal);
            }
            return subgoal;
        }
    } // end processSubgoal()

    /**
     * Create a SubgoalAttempt from the event descriptor(s) in the tool message.
     * Also save the attemptSelection, attemptAction and attemptInput items.
     * @param subgoal the subgoal item
     * @param newAttempt the new subgoal attempt item
     * @param selections the list of selections for this attempt
     * @param actions the list of actions for this attempt
     * @param inputs the list of inputs for this attempt
     * @param problemItem the problem item
     * @return a subgoalAttempt
     */
    private SubgoalAttemptItem processSubgoalAttempt(SubgoalItem subgoal,
                        SubgoalAttemptItem newAttempt,
                        List<AttemptSelectionItem> selections,
                        List<AttemptActionItem> actions,
                        List<AttemptInputItem> inputs,
                        ProblemItem problemItem) {

        SubgoalAttemptItem subgoalAttemptItem = null;
        SubgoalAttemptDao subgoalAttemptDao = DaoFactory.DEFAULT.getSubgoalAttemptDao();
        SubgoalAttemptItem currentAttempt =
            getExistingAttempt(newAttempt, selections, actions, inputs, problemItem);

        if (currentAttempt != null) {
            subgoalAttemptItem = currentAttempt;
            logDebug("processSubgoalAttempt(): attempt already exists in cache: ",
                    subgoalAttemptItem.toString());
        } else {
            currentAttempt = getExistingAttemptFromDB(problemItem, subgoal, newAttempt,
                    selections, actions, inputs);
            if (currentAttempt != null) {
                subgoalAttemptItem = currentAttempt;
                logDebug("processSubgoalAttempt(): attempt already exists in db: ",
                        subgoalAttemptItem);
            } else {
                subgoalAttemptItem = newAttempt;
                AttemptSelectionDao attemptSelectionDao
                    = DaoFactory.DEFAULT.getAttemptSelectionDao();
                AttemptActionDao attemptActionDao = DaoFactory.DEFAULT.getAttemptActionDao();
                AttemptInputDao attemptInputDao = DaoFactory.DEFAULT.getAttemptInputDao();

                subgoalAttemptDao.saveOrUpdate(subgoalAttemptItem);

                // add the selections
                for (AttemptSelectionItem selection : selections) {
                    logDebug("selection is ", selection);
                    subgoalAttemptItem.addAttemptSelection(selection);
                    attemptSelectionDao.saveOrUpdate(selection);
                }
                // add the actions
                for (AttemptActionItem action : actions) {
                    subgoalAttemptItem.addAttemptAction(action);
                    attemptActionDao.saveOrUpdate(action);
                }
                // add the inputs
                for (AttemptInputItem input : inputs) {
                    subgoalAttemptItem.addAttemptInput(input);
                    attemptInputDao.saveOrUpdate(input);
                }
                itemCache.addAttempt(subgoalAttemptItem);
                logDebug("processSubgoalAttempt(): saving a new subgoal attempt. ",
                        subgoalAttemptItem);
            }
        }
        return subgoalAttemptItem;
    }

    /**
     * Its complicated trying to figure out if a subgoal attempt already exists.  You
     * have to loop through all the selections, actions and inputs.
     * @param newAttempt an instance of the attempt with subgoal and other attributes set
     * @param selectionList the list of selection items for the possibly new attempt
     * @param actionList the list of action items for the possibly new attempt
     * @param inputList the list of input items for the possibly new attempt
     * @param problemItem the problem for the subgoal attempt
     * @return the existing attempt or null if not found
     */
    private SubgoalAttemptItem getExistingAttempt(SubgoalAttemptItem newAttempt,
            List<AttemptSelectionItem> selectionList, List<AttemptActionItem> actionList,
            List<AttemptInputItem> inputList, ProblemItem problemItem) {

        AttemptSelectionDao attemptSelectionDao = DaoFactory.DEFAULT.getAttemptSelectionDao();
        AttemptActionDao attemptActionDao = DaoFactory.DEFAULT.getAttemptActionDao();
        AttemptInputDao attemptInputDao = DaoFactory.DEFAULT.getAttemptInputDao();

        SubgoalAttemptItem currentAttempt = null;
        if (itemCache.getExistingAttempts().size() == 0) {
            return currentAttempt;
        } else {
            boolean isSameAttempt = true;
            for (SubgoalAttemptItem attempt : itemCache.getExistingAttempts()) {
                currentAttempt = attempt;
                isSameAttempt = true;

                // If the attempts do not match, then go on to next possible attempt
                SubgoalItem currentSubgoal = currentAttempt.getSubgoal();
                SubgoalItem newSubgoal = newAttempt.getSubgoal();

                if (currentSubgoal != null && newSubgoal == null) {
                    isSameAttempt = false;
                    continue;
                }
                if (currentSubgoal != null && newSubgoal != null
                        && (!currentSubgoal.getId().equals(newSubgoal.getId())
                                || !currentAttempt.getCorrectFlag().equals(
                                        newAttempt.getCorrectFlag()))) {
                    isSameAttempt = false;
                    continue;
                }

                // FIRST check the inputs
                if (itemCache.getExistingAttemptInputs(currentAttempt).isEmpty()
                        && !inputList.isEmpty()) {
                    itemCache.setExistingAttemptInputs(
                            currentAttempt, attemptInputDao.find(currentAttempt));
                }

                Collection<AttemptInputItem> currentInpList =
                    itemCache.getExistingAttemptInputs(currentAttempt);
                // If the sizes of the list don't even match, then go on to next possible attempt
                if (currentInpList.size() != inputList.size()) {
                    isSameAttempt = false;
                    continue;
                }
                for (AttemptInputItem currInput : currentInpList) {
                    boolean found = false;
                    for (AttemptInputItem newInput : inputList) {
                        if (currInput.getInput().equals(newInput.getInput())) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        isSameAttempt = false;
                        break;
                    }
                }

                // If anything doesn't match, then go on to next possible attempt
                if (!isSameAttempt) {
                    continue;
                }

                // SECOND check the selections
                if (itemCache.getExistingAttemptSelections(currentAttempt).isEmpty()
                        && !selectionList.isEmpty()) {
                    itemCache.setExistingAttemptSelections(
                            currentAttempt, attemptSelectionDao.find(currentAttempt));
                }
                Collection<AttemptSelectionItem> currentSelList =
                    itemCache.getExistingAttemptSelections(currentAttempt);

                // If the sizes of the list don't even match, then go on to next possible attempt
                if (currentSelList.size() != selectionList.size()) {
                    isSameAttempt = false;
                    continue;
                }
                for (AttemptSelectionItem currSel : currentSelList) {
                    boolean found = false;
                    for (AttemptSelectionItem newSel : selectionList) {
                        if (currSel.getSelection().equals(newSel.getSelection())) {
                            found = currSel.getType() == null ? newSel.getType() == null
                                    : currSel.getType().equals(newSel.getType());
                            if (found) {
                                break;
                            }
                        }
                    }
                    if (!found) {
                        isSameAttempt = false;
                        break;
                    }
                }

                // If anything doesn't match, then go on to next possible attempt
                if (!isSameAttempt) {
                    continue;
                }

                // FINALLY check the actions
                if (itemCache.getExistingAttemptActions(currentAttempt).isEmpty()
                        && !actionList.isEmpty()) {
                    itemCache.setExistingAttemptActions(
                            currentAttempt, attemptActionDao.find(currentAttempt));
                }
                Collection<AttemptActionItem> currentActList =
                    itemCache.getExistingAttemptActions(currentAttempt);

                // If the sizes of the list don't even match, then go on to next possible attempt
                if (currentActList.size() != currentActList.size()) {
                    isSameAttempt = false;
                    continue;
                }

                for (AttemptActionItem currAction : currentActList) {
                    boolean found = false;
                    for (AttemptActionItem newAct : actionList) {
                        if (currAction.getAction().equals(newAct.getAction())) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        isSameAttempt = false;
                        break;
                    }
                }

                // If everything matches, get out and use this attempt
                if (isSameAttempt) {
                    break;
                }

            } // end for loop on subgoal attempts

            return isSameAttempt ? currentAttempt : null;
        } // end else
    } // end getExistingAttempt

    /**
     * Its complicated trying to figure out if a subgoal attempt already exists.  You
     * have to loop through all the selections, actions and inputs.
     * @param problemItem the problem for the subgoal attempt
     * @param subgoalItem the subgoal
     * @param newAttempt an instance of the attempt with subgoal and other attributes set
     * @param selections the list of selection items for the possibly new attempt
     * @param actions the list of action items for the possibly new attempt
     * @param inputs the list of input items for the possibly new attempt
     * @return the existing attempt or null if not found
     */
    private SubgoalAttemptItem getExistingAttemptFromDB(ProblemItem problemItem,
            SubgoalItem subgoalItem,
            SubgoalAttemptItem newAttempt,
            List<AttemptSelectionItem> selections,
            List<AttemptActionItem> actions,
            List<AttemptInputItem> inputs) {
        SubgoalAttemptItem foundAttempt = null;

        SubgoalAttemptDao subgoalAttemptDao = DaoFactory.DEFAULT.getSubgoalAttemptDao();

        List<SubgoalAttemptItem> subgoalAttempts = null;
        if (subgoalItem != null) {
            subgoalItem = DaoFactory.DEFAULT.getSubgoalDao().get((Long)subgoalItem.getId());
            subgoalAttempts = subgoalItem.getSubgoalAttemptsExternal();
        } else {
            subgoalAttempts = new ArrayList(); //FIXME DS929 what attempts should we look through?
        }

        if (subgoalAttempts.size() == 0) {
            return foundAttempt; // null
        } else {
            boolean found = false;

            for (SubgoalAttemptItem loopAttempt : subgoalAttempts) {
                // If the attempts do not match, then go on to next possible attempt
                if (loopAttempt.getSubgoal() != null && newAttempt.getSubgoal() == null) {
                    continue;
                }

                if (loopAttempt.getSubgoal() != null
                    && newAttempt.getSubgoal() != null
                    && (!loopAttempt.getSubgoal().getId().equals(newAttempt.getSubgoal().getId())
                        || !loopAttempt.getCorrectFlag().equals(newAttempt.getCorrectFlag()))) {
                    continue;
                }

                // FIRST check the inputs
                loopAttempt = subgoalAttemptDao.get((Long)loopAttempt.getId());
                List<AttemptInputItem> loopInputs = loopAttempt.getAttemptInputsExternal();
                if (loopInputs.isEmpty() && !inputs.isEmpty()) {
                    continue;
                }

                // If the sizes of the list don't even match, then go on to next possible attempt
                if (loopInputs.size() != inputs.size()) {
                    continue;
                }

                // List sizes match, check items in list
                for (AttemptInputItem loopInput : loopInputs) {
                    boolean inputFound = false;
                    for (AttemptInputItem newItem : inputs) {
                        if (loopInput.getInput().equals(newItem.getInput())) {
                            inputFound = true;
                            break;
                        }
                    }
                    if (inputFound) {
                        found = true;
                        break; // out of loop on input items
                    }
                }

                // If anything doesn't match, then go on to next possible attempt
                if (!found) {
                    continue;
                }

                // SECOND check the selections
                List<AttemptSelectionItem> loopSelections
                    = loopAttempt.getAttemptSelectionsExternal();
                if (loopSelections.isEmpty() && !selections.isEmpty()) {
                    continue;
                }

                // If the sizes of the list don't even match, then go on to next possible attempt
                if (loopSelections.size() != selections.size()) {
                    continue;
                }

                // List sizes match, check items in list
                for (AttemptSelectionItem loopSelection : loopSelections) {
                    boolean selFound = false;
                    for (AttemptSelectionItem newItem : selections) {
                        if (loopSelection.getSelection().equals(newItem.getSelection())) {
                            selFound = true;
                            break;
                        }
                    }
                    if (selFound) {
                        found = true;
                        break; // out of loop on selection items
                    }
                }

                // If anything doesn't match, then go on to next possible attempt
                if (!found) {
                    continue;
                }

                // THIRD check the actions
                List<AttemptActionItem> loopActions = loopAttempt.getAttemptActionsExternal();
                if (loopActions.isEmpty() && !actions.isEmpty()) {
                    continue;
                }

                // If the sizes of the list don't even match, then go on to next possible attempt
                if (loopActions.size() != actions.size()) {
                    continue;
                }

                // List sizes match, check items in list
                for (AttemptActionItem loopAction : loopActions) {
                    boolean actionFound = false;
                    for (AttemptActionItem newItem : actions) {
                        if (loopAction.getAction().equals(newItem.getAction())) {
                            actionFound = true;
                            break;
                        }
                    }
                    if (actionFound) {
                        found = true;
                        break; // out of loop on action items
                    }
                }

                // If everything matches, get out and use this attempt
                if (found) {
                    foundAttempt = loopAttempt;
                    break;
                }
            } // end for loop on subgoal attempts

            if (!found) {
                foundAttempt = null;
            }
            return foundAttempt;
        } // end else
    } // end getExistingAttemptFromDB


    /**
     * Creates a new FeedbackItem for the provided subgoal attempt.
     * @param subgoalAttemptItem the subgoal attempt for this feedback
     * @param problemItem the problem item for this feedback
     * @param feedbackText the text/message for the feedback
     * @param feedbackClassification the classification for this feedback
     * @return the new feedback item or null if text is empty
     */
    private FeedbackItem processFeedback(SubgoalAttemptItem subgoalAttemptItem,
                ProblemItem problemItem, String feedbackText,
                String feedbackClassification) {
        FeedbackItem feedbackItem = new FeedbackItem();
        if (feedbackText == null || feedbackText.trim().length() <= 0) {
            logDebug("processFeedback(): Feedback Text is null or empty");
            return null;
        } else {
            FeedbackDao feedbackDao = DaoFactory.DEFAULT.getFeedbackDao();
            feedbackItem.setFeedbackText(feedbackText);
            feedbackItem.setSubgoalAttempt(subgoalAttemptItem);
            if (feedbackClassification != null) {
                feedbackItem.setClassification(feedbackClassification);
            }
            if (itemCache.getExistingFeedbacks().isEmpty()) {
                itemCache.setExistingFeedbacks(feedbackDao.find(problemItem));
            }
            boolean found = false;
            for (FeedbackItem loopItem : itemCache.getExistingFeedbacks()) {
                if (feedbackItem.getSubgoalAttempt().equals(subgoalAttemptItem)
                        && feedbackItem.getFeedbackText().equals(loopItem.getFeedbackText())
                        && feedbackItem.getClassification().equals(loopItem.getClassification())) {
                    found = true;
                    feedbackItem = loopItem;
                }
            }
            //if not found in cache, look in db
            if (!found) {
                List<FeedbackItem> dbFeedbacks = subgoalAttemptItem.getFeedbacksExternal();
                for (FeedbackItem loopItem : dbFeedbacks) {
                    if (feedbackItem.getFeedbackText().equals(loopItem.getFeedbackText())
                     && feedbackItem.getClassification().equals(loopItem.getClassification())) {
                        found = true;
                        feedbackItem = loopItem;
                    }
                }
                itemCache.addFeedback(feedbackItem);
            }
            //if not found in db, then create
            if (!found) {
                feedbackDao.saveOrUpdate(feedbackItem);
                itemCache.addFeedback(feedbackItem);
            }

            return feedbackItem;
       }
    }

    /**
     * Create or retrieve the DatasetLevels for this dataset item.  We can get the set of
     * dataset level titles ahead of time but do not know the dataset level names until we
     * read the dataset line items.  So, get all of the dataset levels for the datasetItem.
     * If they do not already exist, create them.
     * @param datasetItem the dataset item
     * @param datasetLevels the datasetLevels for this transaction
     * @return the processed datasetLevels or null
     */
    private ArrayList processDatasetLevels(DatasetItem datasetItem, ArrayList datasetLevels) {
        ArrayList processedDatasetLevels = new ArrayList();
        if (datasetLevels.size() == 0) {
            logger.error("processDatasetLevels: The list of dataset level names"
                    + " is null.  We have a problem.");
            System.exit(-1);
            return null;
        } else {
            DatasetLevelDao datasetLevelDao = DaoFactory.DEFAULT.getDatasetLevelDao();
            DatasetLevelItem parentLevelItem = null;
            for (int i = 0; i < datasetLevels.size(); i++) {
                boolean datasetLevelFound = false;
                DatasetLevelItem datasetLevelItem = (DatasetLevelItem) datasetLevels.get(i);
                datasetLevelItem.setDataset(datasetItem);

                // We've already set up the dataset levels in readTheLine().
                // Check to see if they already exist in the database. If not, insert them.
                for (Iterator it = itemCache.getExistingDatasetLevels().iterator();
                        it.hasNext();) {
                    DatasetLevelItem cacheItem = (DatasetLevelItem)it.next();
                    // if i == 0, we know this is the parent
                    if (i == 0) {
                        if (cacheItem.getLevelName().equals(datasetLevelItem.getLevelName())
                                && cacheItem.getLevelTitle().equals(
                                        datasetLevelItem.getLevelTitle())) {
                            processedDatasetLevels.add(cacheItem);
                            parentLevelItem = cacheItem;
                            datasetLevelFound = true;
                        }
                    } else {
                        // this is a child, so set its parent and look for it in the cache
                        datasetLevelItem.setParent(parentLevelItem);
                        if (cacheItem.getLevelName().equals(datasetLevelItem.getLevelName())
                            && cacheItem.getLevelTitle().equals(datasetLevelItem.getLevelTitle())) {
                            if (cacheItem.getParent() != null) {
                                if (cacheItem.getParent().equals(datasetLevelItem.getParent())) {
                                    // we found it in the cache
                                    processedDatasetLevels.add(cacheItem);
                                    if (logger.isDebugEnabled()) {
                                        logger.debug("processDatasetLevels():"
                                                + " Found current Level in the cache.");
                                    }
                                    datasetLevelFound = true;
                                    datasetLevelItem = cacheItem;
                                }
                            }
                        }
                    }
                } // end for
                if (!datasetLevelFound) {
                    // we didn't find it in the cache, so look for it in the database
                    List dbList = datasetLevelDao.findMatchingByTitleAndName(
                            datasetLevelItem.getLevelTitle(),
                            datasetLevelItem.getLevelName(), datasetItem);
                    if (dbList.contains(datasetLevelItem)) {
                        for (Iterator levelIter = dbList.iterator(); levelIter.hasNext();) {
                            DatasetLevelItem dbItem = (DatasetLevelItem)levelIter.next();
                            if (dbItem.equals(datasetLevelItem)) {
                                datasetLevelItem = dbItem;
                            }
                        }
                        if (logger.isDebugEnabled()) {
                            logger.debug("processDatasetLevels():"
                                    + " Current level exists in database.");
                        }
                    } else {
                        // we didn't find it in the cache or database, so let's add it
                        // we need to save the parent first, otherwise the children will not have
                        // a parent id to key to
                        if (i == 0) {
                            datasetLevelDao.saveOrUpdate(datasetLevelItem);
                            parentLevelItem = datasetLevelItem;
                            if (logger.isDebugEnabled()) {
                                logger.debug("processDatasetLevels(): Created the parent "
                                        + parentLevelItem.toString());
                            }
                        } else {
                            datasetLevelItem.setParent(parentLevelItem);
                            logDebug("processDatasetLevels(): Assigned the parent ",
                                    parentLevelItem);
                            datasetLevelDao.saveOrUpdate(datasetLevelItem);
                        }
                        logDebug("processDatasetLevels():",
                                " Current level does not exist in database.",
                                " Creating a new one.");
                    }
                    processedDatasetLevels.add(datasetLevelItem);
                    itemCache.addDatasetLevel(datasetLevelItem);
                }
            } // end for
            return processedDatasetLevels;
        }
    }

    /**
     * Processes a problem item for a given DatasetLineItem.
     * @param problemName the name of the problem
     * @param datasetLevels the datasetLevels for this problem
     * @return a new ProblemItem
     */
    private ProblemItem processProblem(String problemName, ArrayList datasetLevels) {
        if (problemName == null) {
            logger.info("processProblem(): Problem name is null");
            return null;
        } else {
            ProblemDao problemDao = DaoFactory.DEFAULT.getProblemDao();
            int size = datasetLevels.size();
            ProblemItem problemItem = new ProblemItem();
            problemItem.setProblemName(problemName);
            size--;
            // should it be the last one?
            DatasetLevelItem datasetLevelItem = (DatasetLevelItem) datasetLevels.get(size);
            problemItem.setDatasetLevel(datasetLevelItem);

            boolean found = false;
            for (Iterator it = itemCache.getExistingProblems().iterator();
                    it.hasNext();) {
                ProblemItem cacheItem = (ProblemItem) it.next();
                if (cacheItem.getProblemName().equals(problemItem.getProblemName())
                    && cacheItem.getDatasetLevel().equals(problemItem.getDatasetLevel())) {
                    problemItem = cacheItem;
                    found = true;
                }
            }
            // if not found then look in the database
            if (!found) {
                List dbProblemList = problemDao.findMatchingByLevelAndName(
                        datasetLevelItem, problemItem.getProblemName());
                for (Iterator probIter = dbProblemList.iterator(); probIter.hasNext();) {
                    ProblemItem dbProblem = (ProblemItem)probIter.next();
                    if (dbProblem.equals(problemItem)) {
                        problemItem = dbProblem;
                        found = true;
                        itemCache.addProblem(problemItem);
                    }
                }
            }
            // if still not found then create a new one
            if (!found) {
                problemDao.saveOrUpdate(problemItem);
                if (logger.isDebugEnabled()) {
                    logger.debug("processProblem(): Inserting a new problem item.");
                }
                itemCache.addProblem(problemItem);
            }
            return problemItem;
        }
    }

    /**
     * Takes each skillModelItem in the skillModels list, checks for existence in
     * the itemCache and if not found, saved to the database.
     * @param datasetItem the dataset item
     */
    private void processSkillModels(DatasetItem datasetItem) {
        if (skillModels == null || skillModels.size() == 0) {
            if (logger.isDebugEnabled()) {
                logger.info("processSkillModels(): No Skill Models to insert for this dataset.");
            }
        } else {
            SkillModelDao skillModelDao = DaoFactory.DEFAULT.getSkillModelDao();
            for (Iterator skillModelIt = skillModels.iterator(); skillModelIt.hasNext();) {
                // the skill models have already been created by initializeSkillModels().
                // We now need to check for their existence in the cache.
                boolean found = false;
                SkillModelItem skillModelItem = (SkillModelItem) skillModelIt.next();
                skillModelItem.setDataset(datasetItem);
                String name = skillModelItem.getSkillModelName();
                for (Iterator it = itemCache.getExistingModels().iterator();
                        it.hasNext();) {
                    SkillModelItem cacheItem = (SkillModelItem) it.next();
                    if (cacheItem.getSkillModelName().equals(skillModelItem.getSkillModelName())
                            && cacheItem.getDataset().equals(skillModelItem.getDataset())) {
                        skillModelItem = cacheItem;
                        found = true;
                        if (logger.isDebugEnabled()) {
                            logger.debug("processSkillModels():"
                                    + " Found the skill model (" + name + ") in the cache.");
                        }
                        break;
                    }
                } // end for

                // if not found in the cache then look in the database
                if (!found) {
                    List dbSkillModels = skillModelDao.find(datasetItem);
                    for (Iterator iter = dbSkillModels.iterator(); iter.hasNext();) {
                        SkillModelItem dbItem = (SkillModelItem) iter.next();
                        if (dbItem.getSkillModelName().equals(skillModelItem.getSkillModelName())) {
                            skillModelItem = dbItem;
                            itemCache.addModel(skillModelItem);
                            found = true;
                            if (logger.isDebugEnabled()) {
                                logger.debug("processSkillModels():"
                                        + " Found the skill model (" + name + ") in the database.");
                            }
                            break;
                        }
                    }
                }
                // if still not found then save a new one to the database
                if (!found) {
                    skillModelDao.saveOrUpdate(skillModelItem);
                    itemCache.addModel(skillModelItem);
                    if (logger.isDebugEnabled()) {
                        logger.debug("processSkillModels(): Inserted a new SkillModelItem ( "
                                + name + ")");
                    }
                } else {
                    logger.debug("processSkillModels(): skill model (" + name + ") found.");
                }
            } // end for
        }
    }

    /**
     * Process each SkillItem in the set of skills for each dataset line.
     * Check to see if the Skill already exists in the database.  If so, add it
     * to the set of processedSkills.  If not, insert the skill item into the database.
     * The order of processing matters - otherwise a SkillItem could be assigned an
     * incorrect SkillModel item [using an ArrayList supports this ordering].
     * @param skills a set of skills for a particular DatasetLineItem
     * @param subgoalItem the subgoal the skills belong to
     * @return a list of processed SkillItems, to be assigned to the transaction
     */
    private HashSet processSkills(ArrayList skills, SubgoalItem subgoalItem) {
        HashSet processedSkills = new HashSet();
        if (skills == null || skills.size() == 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("processSkills: No skills to process for this dataset line.");
            }
            return processedSkills;
        }

        SkillDao skillDao = DaoFactory.DEFAULT.getSkillDao();

        //
        // First loop through the skills and find the skill model item, and re-attach.
        //
        SkillModelItem skillModelItem = null;
        for (Iterator skillsIt = skills.iterator(); skillsIt.hasNext();) {
            SkillItem skillItem = (SkillItem) skillsIt.next();
            // get the non-transient skillModelItem from the itemCache and
            // re-attach it to the skillItem
            skillModelItem = skillItem.getSkillModel();
            boolean found = false;
            for (Iterator skillModelIt = itemCache.getExistingModels().iterator();
            skillModelIt.hasNext();) {
                SkillModelItem cacheItem = (SkillModelItem) skillModelIt.next();
                if (skillModelItem.getSkillModelName().equals(cacheItem.getSkillModelName())) {
                    skillModelItem = cacheItem;
                    found = true;
                }
            }
            if (!found) {
                logger.error("processSkills: the skill model item ("
                        + skillModelItem.getSkillModelName() + ") is not found.");
                //TODO should we do something more if the skill model item is not found?
            }
            skillItem.setSkillModel(skillModelItem);
        }

        //
        // Then loop through the skills, look for each skill item in the cache, database or create.
        //
        for (Iterator iter = skills.iterator(); iter.hasNext();) {
            SkillItem skillItem = (SkillItem)iter.next();
            boolean skillFound = false;
            if (skillItem.getSkillName() == null) {
                continue;
            }

            // check the cache
            for (Iterator cacheIter = itemCache.getExistingSkills().iterator();
            cacheIter.hasNext();) {
                SkillItem cacheItem = (SkillItem)cacheIter.next();
                if (cacheItem.getSkillName().equals(skillItem.getSkillName())
                  &&
                  (
                   (cacheItem.getCategory() == null
                    && skillItem.getCategory() == null)
                                ||
                   (cacheItem.getCategory() != null
                    && skillItem.getCategory() != null
                    && cacheItem.getCategory().equals(skillItem.getCategory()))
                  )
                  && cacheItem.getSkillModel().equals(skillItem.getSkillModel())) {
                    skillItem = cacheItem;
                    skillFound = true;
                    if (logger.isDebugEnabled()) {
                        logger.debug("processSkills: found skill in cache :: "
                                + skillItem.getSkillName()
                                + " (" + skillItem.getId() + ")");
                    }
                    break;
                }
            }

            // check the database
            if (!skillFound) {
                List dbSkills = skillDao.find(skillItem.getSkillModel());
                for (Iterator dbIter = dbSkills.iterator();
                dbIter.hasNext();) {
                    SkillItem dbItem = (SkillItem)dbIter.next();
                    if (dbItem.getSkillName().equals(skillItem.getSkillName())
                      &&
                      (
                       (dbItem.getCategory() == null
                        && skillItem.getCategory() == null)
                                    ||
                       (dbItem.getCategory() != null
                        && skillItem.getCategory() != null
                        && dbItem.getCategory().equals(skillItem.getCategory()))
                      )
                      && dbItem.getSkillModel().getSkillModelName().equals(
                                skillItem.getSkillModel().getSkillModelName())) {
                        skillItem = dbItem;
                        skillFound = true;
                        itemCache.addSkill(skillItem);
                        if (logger.isDebugEnabled()) {
                            logger.debug("processSkills: found skill in database :: "
                                    + skillItem.getSkillName()
                                    + " (" + skillItem.getId() + ")");
                        }
                        break;
                    }
                }
            }

            if (skillFound) {
                skillItem = skillDao.get((Long)skillItem.getId());
                if (subgoalItem != null) {
                    skillItem.addSubgoal(subgoalItem);
                    skillDao.saveOrUpdate(skillItem);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("processSkills: updated :: "
                            + skillItem.getSkillName() + " (" + skillItem.getId() + ")");
                }
            } else {
                if (subgoalItem != null) {
                    skillItem.addSubgoal(subgoalItem);
                }
                skillDao.saveOrUpdate(skillItem);
                skillItem = skillDao.get((Long)skillItem.getId());
                itemCache.addSkill(skillItem);
                if (logger.isDebugEnabled()) {
                    logger.debug("processSkills: created :: "
                            + skillItem.getSkillName() + " (" + skillItem.getId() + ")");
                }
            }
            processedSkills.add(skillItem);
        }
        return processedSkills;
    } // end processSkills

    /**
     * Process each NameValuePair for custom field in the set of customFields for each dataset line.
     * Check to see if the custom field already exists in the database.  If so, add it
     * to the set of processedCustomFields.
     * If not, insert the custom field into the database.
     * After getting custom field Id back, save cf_tx_level.
     * @param datasetItem the dataset item
     * @param customFields the set of CustomFieldNameSValuePair for this dataset line
     * @param txItem the transaction to associate these custom fields to.
     * @return a set of processed custom field items to be added to the transaction item
     */
    private HashSet processCustomFields(DatasetItem datasetItem,
            Set customFields, TransactionItem txItem) {
        HashSet processedCustomFields = new HashSet();
        if (customFields == null || customFields.size() == 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("processCustomFields(): "
                           + "No custom fields to process for this dataset line.");
            }
            return processedCustomFields;
        }
        CustomFieldDao customFieldDao = DaoFactory.DEFAULT.getCustomFieldDao();
        for (Iterator iter = customFields.iterator(); iter.hasNext();) {
            CustomFieldNameValueItem cfNameValueItem = (CustomFieldNameValueItem) iter.next();
        String customFieldName = cfNameValueItem.getName();
        String customFieldValue = cfNameValueItem.getBigValue() != null
            ? cfNameValueItem.getBigValue() : cfNameValueItem.getValue();
        CustomFieldItem customFieldItem = cfNameValueItem.makeACustomFieldItem();
            customFieldItem.setDataset(datasetItem);
        customFieldItem.setLevel(CustomFieldItem.CF_LEVEL_TRANSACTION);
        customFieldItem.setOwner(new UserItem(UserItem.SYSTEM_USER));

        //check if this customField is already processed
        if (!processedCustomFields.contains(customFieldItem)) {
            //add to database, and processedCustomField
        customFieldDao.saveOrUpdate(customFieldItem);
        processedCustomFields.add(customFieldItem);
        if (logger.isDebugEnabled()) {
                    logger.debug("processCustomFields(): "
                            + "Inserting a new custom field item.");
                }
        } else {
            //update customFieldItem id with the one from processedCustomFields
            for (Iterator cfIter = processedCustomFields.iterator(); cfIter.hasNext();) {
                        CustomFieldItem cfItem = (CustomFieldItem) cfIter.next();
                if (cfItem.equals(customFieldItem)) {
                    customFieldItem = cfItem;
                    break;
                }
            }
        }
        //process cf_tx_level
        CfTxLevelItem cfTxLevelItem =
                cfNameValueItem.makeACfTxLevelItem(customFieldItem, txItem);
        cfTxLevelItem.setLoggingFlag(true);

        CfTxLevelDao cfTxLevelDao = DaoFactory.DEFAULT.getCfTxLevelDao();
        cfTxLevelDao.saveOrUpdate(cfTxLevelItem);
        if (logger.isDebugEnabled()) {
                    logger.debug("processCustomFields(): "
                            + "Inserting a new CfTxLevel item.");
                }
        }

        return processedCustomFields;
    }

    /**
     * Creates a new subgoal attempt for the provided subgoal item.
     * @param subgoalItem the subgoal this attempt belongs to (can be null, especially
     * in the case of lonely tool messages).
     * @param outcome the outcome for this attempt (can be null).
     * @return a new SubgoalAttemptItem
     */
    private SubgoalAttemptItem createSubgoalAttempt(SubgoalItem subgoalItem,
            String outcome) {
        SubgoalAttemptItem subgoalAttemptItem = new SubgoalAttemptItem();
        subgoalAttemptItem.setSubgoal(subgoalItem);
        if (outcome == null) {
             subgoalAttemptItem.setCorrectFlag("unknown");
        } else {
            subgoalAttemptItem.setCorrectFlag(outcome);
        }
        return subgoalAttemptItem;
    }

    /** Format for selection, action, or input string that includes an xml_id and a type */
    private static final Pattern SAI_PATTERN = Pattern.compile("(.*)\\((.*),(.*)\\)");

    /** Constant. */
    private static final int SAI_PARAM_TYPE = 3;

    /**
     * Parse the selection/action/input, and optional xml_id and type, to set the appropriate
     * values on the SAI object.
     * @param sai attempt selection, action, or input
     * @param subgoalAttempt the subgoal attempt
     * @param value string representation of the SAI values
     */
    private void parseSAI(AttemptSAI sai, SubgoalAttemptItem subgoalAttempt,
            String value) {
        sai.setSubgoalAttempt(subgoalAttempt);
        Matcher m = SAI_PATTERN.matcher(value);

        if (m.matches()) {
            sai.setValue(m.group(1).trim());
            sai.setXmlId(m.group(2).trim());
            sai.setType(m.group(SAI_PARAM_TYPE).trim());
        } else {
            sai.setValue(value.trim());
        }
    }

    /**
     * Processes attempt action items for a given DatasetLineItem.  Responsible for creating
     * new AttemptActionItems and adding them to the set of attempt actions for that
     * particular line.
     * @param actions a set of attempt actions for a dataset line item
     * @param subgoalAttempt the subgoal attempt the actions belong to
     * @return a set of new AttemptActionItems
     */
    private List<AttemptActionItem> processAttemptActions(Set<String> actions,
            SubgoalAttemptItem subgoalAttempt) {
        List<AttemptActionItem> processedAttemptActions = new ArrayList<AttemptActionItem>();
        if (actions == null || actions.isEmpty()) {
            logDebug("processAttemptActions(): No Actions to process");
        } else {
            for (String actionValue : actions) {
                if (actionValue == null || actionValue.trim().isEmpty()) {
                    logDebug("processAttemptActions(): Attempt Action is null or empty");
                } else {
                    AttemptActionItem action = new AttemptActionItem();
                    parseSAI(action, subgoalAttempt, actionValue);
                    processedAttemptActions.add(action);
                }
            }
        }
        return processedAttemptActions;
    }

    /**
     * Processes attempt selection items for a given DatasetLineItem.  Responsible for creating
     * new AttemptSelectionItems and adding them to the set of attempt selections for
     * that particular dataset line item.
     * @param selections a set of attempt selections for a dataset line item
     * @param subgoalAttempt the subgoal attempt the selections belong to
     * @return a set of new AttemptSelectionItems
     */
    private List<AttemptSelectionItem> processAttemptSelections(Set<String> selections,
            final SubgoalAttemptItem subgoalAttempt) {
        List<AttemptSelectionItem> processedAttemptSelections =
            new ArrayList<AttemptSelectionItem>();
        if (selections == null || selections.isEmpty()) {
            logDebug("processAttemptSelections(): No Selections to process");
        } else {
            for (String selectionValue : selections) {
                if (selectionValue == null || selectionValue.trim().isEmpty()) {
                    logDebug("processAttemptSelections(): Attempt Selection is null or empty");
                } else {
                    AttemptSelectionItem selection = new AttemptSelectionItem();
                    parseSAI(selection, subgoalAttempt, selectionValue);
                    processedAttemptSelections.add(selection);
                }
            }
        }
        return processedAttemptSelections;
    }

    /**
     * Processes attempt input items for a given DatasetLineItem.  Responsible for creating
     * new AttemptInputItems and adding them to the set of attempt inputs for that particular
     * dataset line item.
     * @param inputs a set of attempt inputs for a dataset line item
     * @param subgoalAttempt the subgoal attempt the inputs belong to
     * @return a set of new AttemptInputItems
     */
    private List<AttemptInputItem> processAttemptInputs(Set<String> inputs,
            SubgoalAttemptItem subgoalAttempt) {
        List<AttemptInputItem> processedAttemptInputs = new ArrayList();
        if (inputs == null || inputs.isEmpty()) {
            logDebug("processAttemptInputs(): No Inputs to process");
        } else {
            for (String inputValue : inputs) {
                if (inputValue == null || inputValue.trim().length() <= 0) {
                    logDebug("proccessAttemptInputs(): Attempt Input is null or empty");
                } else {
                    AttemptInputItem input = new AttemptInputItem();
                    parseSAI(input, subgoalAttempt, inputValue);
                    processedAttemptInputs.add(input);
                }
            }
        }
        return processedAttemptInputs;
    }

    /**
     * Checks to see if the incoming outcome is null.  If not, return the
     * outcome value.
     * @param outcome the outcome for a particular subgoal attempt
     * @return the outcome or null if outcome is empty/null
     */
    private String processOutcome(String outcome) {
        // Outcome is required, but just in case let's check it again.
        if (outcome == null) {
            logger.error("processOutcome(): Outcome is null!");
            return null;
        } else {
            return outcome;
        }
    }

    /**
     * Process each ConditionItem in the set of conditions for a dataset line.
     * Check to see if the condition already exists in the database.  If so, add it
     * to the set of processedConditions.  If not, insert the condition into the database.
     * @param datasetItem the dataset item
     * @param conditions the set of ConditionItems to process
     * @return a set of ConditionItems for the transaction item
     */
    private Set<ConditionItem> processConditions(DatasetItem datasetItem,
            Set<ConditionItem> conditions) {
        Set<ConditionItem> processedConditions = new HashSet<ConditionItem>();
        if (conditions == null || conditions.size() == 0) {
            logDebug("processCondition(): No conditions to process for this dataset line.");
            return processedConditions;
        } else {
            boolean found = false;
            ConditionDao conditionDao = DaoFactory.DEFAULT.getConditionDao();
            for (ConditionItem conditionItem : conditions) {
                conditionItem.setDataset(datasetItem);
                for (ConditionItem cacheItem : itemCache.getExistingConditions()) {
                    found = cacheItem.getConditionName().equals(conditionItem.getConditionName());
                    if (found) {
                        conditionItem = cacheItem;
                        processedConditions.add(conditionItem);
                        logDebug("processCondition(): Found condition in the cache.");
                    }
                }
                if (!found) {
                    conditionItem = (ConditionItem)conditionDao.findOrCreate(
                        conditionDao.findMatchingByName(
                                conditionItem.getConditionName(), datasetItem, false),
                        conditionItem);
                    itemCache.addCondition(conditionItem);
                    processedConditions.add(conditionItem);
                    logDebug("processCondition(): Inserted a new condition item.");
                }
                found = false;
            } // end for
            return processedConditions;
        }
    }

    /**
     * Process a school item for a given dataset line.  If the SchoolItem already exists
     * in the database, return that school item.  Otherwise, insert a new school item and
     * return it.
     * @param schoolName the name of the school
     * @return a SchoolItem for the school name
     */
    private SchoolItem processSchool(String schoolName) {
        if (schoolName == null || schoolName.equals("")
                || schoolName.equals(" ")) {
                logDebug("processSchool(): Incoming School name is null!");
            return null;
        }

        SchoolDao schoolDao = DaoFactory.DEFAULT.getSchoolDao();
        SchoolItem schoolItem = new SchoolItem();
        schoolItem.setSchoolName(schoolName);
        boolean found = false;
        for (Iterator it = itemCache.getExistingSchools().iterator();
                it.hasNext();) {
            SchoolItem cacheItem = (SchoolItem) it.next();
            if (cacheItem.getSchoolName().equals(schoolItem.getSchoolName())) {
                schoolItem = cacheItem;
                found = true;
                logDebug("processSchool(): Found school in the cache.");
            }
        }
        if (!found) {
            schoolItem = (SchoolItem) schoolDao.findOrCreate(schoolDao.find(
                    schoolItem.getSchoolName()), schoolItem);
            itemCache.addSchool(schoolItem);
            logDebug("processSchool(): Using findOrCreate() to process the SchoolItem.");
        }
        return schoolItem;
    }

    // 1. new item
    // 2. check in cache (need comparator)
    // 3. check in db (need comparator)
    // 4. if wasn't in cached, add
    // 5. return the item

    /**
     * Process a class item for a given dataset line.  If the ClassItem already exists
     * in the database return that class item.  Otherwise, insert a new class item and
     * return it.  Cannot use dao.findOrCreate for ClassItems.
     * @param className the name of the class
     * @return a ClassItem for the class name
     */
    private ClassItem processClass(String className) {
        if (className == null || className.equals("") || className.equals(" ")) {
            logDebug("processClass(): Incoming Class name is null!");
            return null;
        }

        boolean found = false;
        ClassDao classDao = DaoFactory.DEFAULT.getClassDao();
        ClassItem classItem = new ClassItem();
        classItem.setClassName(className);
        for (ClassItem cacheItem : itemCache.getExistingClasses()) {
            if (cacheItem.getClassName().equals(classItem.getClassName())) {
                classItem = cacheItem;
                found = true;
                logDebug("processClass(): Found the class in the cache.");
            }
        }
        if (!found) {
            classItem = (ClassItem) classDao.findOrCreate(
                    classDao.find(classItem.getClassName()), classItem);
            itemCache.addClass(classItem);
            logDebug("processClass(): Using findOrCreate() to process the ClassItem.");
        }
        return classItem;
    }

    /**
     * Main method for driving the processing of the dataset file.
     * A file is processed in batches - each batch consists of reading a
     * set number of lines, then creating DatasetLineItems for each line.
     * Next, the set of dataset lines is passed to insertTheLines() for
     * insertion into the database.
     * @param loggedToSystemLogTableFlag indicates whether the system log table was updated
     * @param fileName the name of the file to process
     */
    private void process(boolean loggedToSystemLogTableFlag, String fileName) {
        String line;

        try {
            // reset the reader to the beginning of the file.
            BufferedReader inputReader = ImportUtilities.handleInputFile(fileName);

            Boolean keepReading = true;
            logger.info("Looking for the column header row ...");
            // eat any whitespace at the top of the file.
            while (keepReading) {
                line = inputReader.readLine();
                keepReading = !line.startsWith(ImportConstants.ROW_HEADING)
                                && !line.startsWith(ImportConstants.SAMPLE_HEADING)
                                && !line.startsWith(ImportConstants.STUDENT_HEADING);
            }
            // advance past the column row.
            // line = inputReader.readLine();

            logger.info("There are " + linesToProcess + " lines to process.");
            MemoryUtils.logMemoryUsage(showMemoryUsageFlag,
                    logger, "process: before while EOF  ");
            while (!eof) {
                MemoryUtils.logMemoryUsage(showMemoryUsageFlag,
                        logger, "process: before while BATCH");

                insertTheLines(loggedToSystemLogTableFlag, inputReader);

                MemoryUtils.logMemoryUsage(showMemoryUsageFlag,
                        logger, "process: after while BATCH");
                loggedToSystemLogTableFlag = true;
            }
            MemoryUtils.logMemoryUsage(showMemoryUsageFlag, logger, "process: After while EOF   ");
            logger.info("Reached the end of the file.");
            logger.info("Processed a total of " + (lineCounter - 1) + " lines.");
        } catch (Exception e) {
            logger.error("DatasetImportTool.process(): ", e);
        }
    }

    /**
     * Read a batch of lines into Dataset Line Items.
     * @param inputReader reader for the dataset import tool
     * @return a list of dataset items
     * @throws IOException if something goes wrong
     */
    private List<DatasetLineItem> readTheLines(BufferedReader inputReader) throws IOException {
        int linesProcessed = 0;
        List<DatasetLineItem> datasetItems = new ArrayList<DatasetLineItem>();

        while (linesProcessed < BATCH_SIZE) {
            String line = inputReader.readLine();

            eof = line == null;
            if (eof) {
                break;
            }
            if (line.length() == 0
                    || line.split(DEFAULT_DELIMITER).length == 0) {
                logger.info("Skipping a blank line.");
            } else {
                datasetItems.add(readTheLine(line));
            }
            linesProcessed++;
        }

        return datasetItems;
    }

    /**
     * Read a line from the input file and create a new DatasetLineItem based on the contents.
     *
     * Working Assumption: the line has been checked for null or empty
     * before passing to this method.
     *
     * @param line String containing the dataset line information
     * @return a single DatasetLineItem holding the information passed in
     */
    private DatasetLineItem readTheLine(String line) {
        DatasetLineItem item = new DatasetLineItem();
        try {
            String[] lineSplit = line.split(DEFAULT_DELIMITER);
            int position = 0;
            if (rowPresent) {
                // ignore it;
                position++;
            }
            if (samplePresent) {
                // ignore it;
                position++;
            }
            item.setStudentId(lineSplit[position]);
            item.setSessionID(lineSplit[position++]);
            item.setTime(lineSplit[position++]);
            if (timeZonePresent) {
                item.setTimeZone(lineSplit[position++]);
            }
            if (durationPresent) {
                // move on, we don't care.
                position++;
            }
            if (studentResponseTypePresent) {
                item.setStudentResponseType(lineSplit[position++]);
            }
            if (studentResponseSubtypePresent) {
                item.setStudentResponseSubtype(lineSplit[position++]);
            }
            if (tutorResponseTypePresent) {
                item.setTutorResponseType(lineSplit[position++]);
            }
            if (tutorResponseSubtypePresent) {
                item.setTutorResponseSubtype(lineSplit[position++]);
            }
            // order matters here
            final Iterator<String> datasetTitlesIt = datasetLevelTitles.iterator();
            for (int i = 0; i < maxDatasetLevelCount; i++) {
                if (lineSplit[position] != null && !lineSplit[position].equals("")) {
                    DatasetLevelItem datasetLevel = new DatasetLevelItem();

                    datasetLevel.setLevelName(lineSplit[position++]);
                    datasetLevel.setLevelTitle(datasetTitlesIt.next());
                    item.addDatasetLevel(datasetLevel);
               } else {
                   position++;
                   datasetTitlesIt.next();
               }
            } // end for datasetLevel
            item.setProblemName(lineSplit[position++]);
            if (stepNamePresent) {
                item.setStepName(lineSplit[position++]);
            }
            if (attemptAtStepPresent) {
                item.setAttemptAtStep(lineSplit[position++]);
            }
            if (outcomePresent) {
                item.setOutcome(lineSplit[position++]);
            }
            for (int i = 0; i < maxSelectionCount; i++) {
                item.addSelection(lineSplit[position++]);
            }
            for (int i = 0; i < maxActionCount; i++) {
                item.addAction(lineSplit[position++]);
            }
            for (int i = 0; i < maxInputCount; i++) {
                item.addInput(lineSplit[position++]);
            }
            if (feedbackTextPresent) {
                item.setFeedbackText(lineSplit[position++]);
            }
            if (feedbackClassificationPresent) {
                item.setFeedbackClassification(lineSplit[position++]);
            }
            if (helpLevelPresent) {
                if (!lineSplit[position].isEmpty()) {
                    item.setHelpLevel(new Integer(lineSplit[position]));
                }
                position++;
            }
            if (totalNumHintsPresent) {
                if (!lineSplit[position].isEmpty()) {
                    item.setTotalNumberHints(new Integer(lineSplit[position]));
                }
                position++;
            }
            if (conditionPresent) {
                for (int i = 0; i < maxConditionCount; i++) {
                    if (!lineSplit[position].equals(EMPTY_STRING)) {
                        ConditionItem conditionItem = new ConditionItem();
                        conditionItem.setConditionName(lineSplit[position++]);
                        conditionItem.setType(lineSplit[position++]);
                        item.addCondition(conditionItem);
                    } else { position += 2; }
                }
            }
            if (kcPresent) {
                // order matters here
                Iterator<SkillModelItem> skillModelIt = skillModels.iterator();
                for (int i = 0; i < maxSkillCount; i++) {
                    if (!lineSplit[position].equals("")) {
                        SkillItem skillItem = new SkillItem();
                        skillItem.setSkillName(lineSplit[position++]);
                        if (kcCategoryPresent && position < lineSplit.length) {
                            skillItem.setCategory(lineSplit[position++]);
                        }
                        skillItem.setSkillModel(skillModelIt.next());
                        // add them one-by-one since the next read may be a null pointer.
                        item.addKnowledgeComponent(skillItem);
                    } else {
                        // move 2 for the skill name and category
                        position += (kcCategoryPresent ? 2 : 1);
                        skillModelIt.next();
                    }
                }
            }
            if (schoolPresent) {
                item.setSchool(lineSplit[position++]);
            }
            if (classPresent && position < lineSplit.length) {
                item.setClassID(lineSplit[position++]);
            }
            if (cfPresent) {
                // order matters here
                Iterator<String> customFieldNamesIt = customFieldNames.iterator();
                for (int i = 0; i < maxCustomFieldCount; i++) {
                    if (position >= lineSplit.length) {
                        break;
                    } else if (!lineSplit[position].equals("")) {
                        CustomFieldNameValueItem cfNameValueItem = new CustomFieldNameValueItem();
                        String cfValue = lineSplit[position++];

                        if (cfValue.length() > MAX_CUSTOM_FIELD_LENGTH) {
                            cfValue = cfValue.substring(0, MAX_CUSTOM_FIELD_LENGTH);
                        }
                        cfNameValueItem.setValue(cfValue);
                        cfNameValueItem.setName(customFieldNamesIt.next());
                        // add them one-by-one since the next read may be a null pointer.
                        item.addCustomField(cfNameValueItem);
                    } else {
                        position++;
                        customFieldNamesIt.next();
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.error("readTheLine(): Unexpectedly reached the end of line " + lineCounter
                    + ".", e);
        } catch (Exception e) {
            logger.error("readTheLine(): There was an error processing line "
                    + lineCounter + ".");
            logger.error(e.toString(), e);
        }
        return item;
    } // end readTheLine

    /**
     * Only log if debugging is enabled.
     * @param args concatenate all arguments into the string to be logged
     */
    public void logDebug(Object... args) {
        LogUtils.logDebug(logger, args);
    }

    /**
     * Handle the command line arguments.
     * @param args - command line arguments passed into main
     */
    protected void handleOptions(String[] args) {
        if (args == null || args.length == 0) {
            displayUsage(outWriter);
            return;
        }

        java.util.ArrayList argsList = new java.util.ArrayList();
        for (int i = 0; i < args.length; i++) {
            argsList.add(args[i]);
        }

        // loop through the arguments
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-h")
             || args[i].equals("-help")) {
                displayUsage(outWriter);
                System.exit(0);
            } else if (args[i].equals("-v")
                    || args[i].equals("-version")) {
                logger.info("INFO: " + VersionInformation.getReleaseString());
                System.exit(0);
            } else if (args[i].equals("-dir")
                    || args[i].equals("-directory")) {
                if (++i < args.length) {
                    inputDirectoryName = args[i];
                } else {
                    logger.error("A directory must be specified with this argument");
                    displayUsage(outWriter);
                    System.exit(1);
                }
            } else if (args[i].equals("-f")
                    || args[i].equals("-fileName")) {
                if (++i < args.length) {
                    inputFileName = args[i];
                } else {
                    logger.error("A file name must be specified with this argument");
                    displayUsage(outWriter);
                    System.exit(1);
                }
            } else if (args[i].equals("-d")
                    || args[i].equals("-datasetName")) {
                if (++i < args.length) {
                    datasetName = args[i];
                } else {
                    logger.error("A dataset name must be specified with this argument");
                    displayUsage(outWriter);
                    System.exit(1);
                }
            } else if (args[i].equals("-m")
                    || args[i].equals("-memory")) {
                showMemoryUsageFlag = true;
            } else if (args[i].equals("-dom")
                    || args[i].equals("-domain")) {
                if (++i < args.length) {
                    DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
                    List<DatasetItem> datasetList = datasetDao.findAll();
                    logger.info("datasetList.size(): " +  datasetList.size());
                    DomainDao domainDao = DaoFactory.DEFAULT.getDomainDao();
                    DomainItem domainItem = domainDao.findByName(args[i]);
                    if (domainItem != null) {
                        inputDomainName = args[i];
                    } else {
                        logger.error("The domain is not valid");
                        displayUsage(outWriter);
                        System.exit(1);
                    }
                } else {
                    logger.error("A domain name must be specified with this argument");
                    displayUsage(outWriter);
                    System.exit(1);
                }
            } else if (args[i].equals("-l")
                    || args[i].equals("-learnlab")) {
                if (++i < args.length) {
                    LearnlabDao learnlabDao = DaoFactory.DEFAULT.getLearnlabDao();
                    LearnlabItem learnlabItem = learnlabDao.findByName(args[i]);
                    logger.error("The learnlab name: " + args[i]);
                    if (learnlabItem != null) {
                        inputLearnlabName = args[i];
                        if (inputDomainName != null) {
                            boolean foundMatch = false;
                            DatasetInfoEditHelper datasetInfoEditHelper =
                                HelperFactory.DEFAULT.getDatasetInfoEditHelper();
                                List<String> domainLearnlabLists = datasetInfoEditHelper
                                                                    .getDomainLearnlabList();
                            for (Iterator it = domainLearnlabLists.iterator(); it.hasNext();) {
                                if (it.next().equals(inputDomainName + "/" + inputLearnlabName)) {
                                    foundMatch = true;
                                }
                            }
                            if (!foundMatch) {
                                logger.error("The Domain \"" + inputDomainName
                                        + "\" and the Learnlab \"" +  inputLearnlabName
                                        + "\" combination is not valid.");
                                displayUsage(outWriter);
                                System.exit(1);
                            }
                        }
                    } else {
                        logger.error("The learnlab is not valid");
                        displayUsage(outWriter);
                        System.exit(1);
                    }
                } else {
                    logger.error("A learnlab name must be specified with this argument");
                    displayUsage(outWriter);
                    System.exit(1);
                }
            }
        } // end for loop
    } // end handleOptions

    /**
     * Display the usage of this utility.
     * @param outWriter PrintWriter instance
     */
    public void displayUsage(Writer outWriter) {
        StringBuffer usageMessage = new StringBuffer();
        usageMessage.append("\nUSAGE: java -classpath ... "
                + TOOL_NAME
                + " [-fileName input_file_name]"
                + " [-directory directory_path]"
                + " [-datasetName dataset_name]"
                + " [-help] [-version] [-memory] [-domain domain_name] [-learnlab learnlab_name]");
        usageMessage.append("Options:");
        usageMessage.append("\t-f, -fileName      \t Import the given tab-delimited file");
        usageMessage.append("\t-dir, -directory   \t Import the files in the given directory");
        usageMessage.append("\t-d, -datasetName   \t The name of the new dataset");
        usageMessage.append("\t-h, -help          \t Display this help and exit");
        usageMessage.append("\t-v, -version       \t Display the version and exit");
        usageMessage.append("\t-m, -memory        \t Log the memory usage");
        usageMessage.append("\t-dom, -domain        \t The name of the domain");
        usageMessage.append("\t-l, -learnlab        \t The name of the learnlab");
        logger.info(usageMessage.toString());
        System.exit(-1);
    }

    /** Run the Dataset Import Tool.
     * USAGE: java -classpath ...  [-filename input_file_name]
     *     [-datasetName dataset_name] [-debug]
     * @param args - command line arguments
     */
    public static void main(String[] args) {
        DatasetImportTool dit = ExtractorFactory.DEFAULT.getDatasetImportTool();
        DatasetVerificationTool dvt = new DatasetVerificationTool();

        Logger logger = Logger.getLogger("DatasetImportTool.main");
        String version = VersionInformation.getReleaseString();
        logger.info(TOOL_NAME + " starting (" + version + ")...");

        try {
            dit.handleOptions(args);
            boolean loggedToSystemLogTableFlag = false;
            if ((datasetName == null) || (inputDomainName == null) || (inputLearnlabName == null)) {
                logger.error("Please verify the accuracy of the arguments.");
                System.exit(-1);
            } else if (inputFileName != null) {
                // Open the dataset file for verification and process it
                logger.info("Calling DatasetVerificationTool to check validity"
                        + " of dataset file");
                String result = dvt.process(inputFileName);
                if (result.equals("")) {
                    // continue with processing
                    logger.info("Processing the dataset file ...");
                    dit.init(dvt);
                    dit.process(loggedToSystemLogTableFlag, inputFileName);
                } else {
                    // exit the tool
                    System.exit(-1);
                }
            } else if (inputDirectoryName != null) {
                // Open the dataset file for verification and process it
                List fileList = getFilenameList(logger, inputDirectoryName);
                for (int j = 0, m = fileList.size(); j < m; j++) {
                    itemCache = new ItemCache();
                    //itemCache.clearAll();
                    skillModels = new ArrayList();
                    dvt = new DatasetVerificationTool();
                    File theFile = (File)fileList.get(j);
                    String filename = theFile.getName();
                    String fileWithPath = theFile.getAbsolutePath();
                    logger.info("Reading file " + filename + " (" + (j + 1) + "/" + m + ")");

                    String result = dvt.process(fileWithPath);
                    if (result.equals("")) {
                        // continue with processing
                        logger.info("Processing file " + filename);
                        dit.init(dvt);
                        dit.process(loggedToSystemLogTableFlag, fileWithPath);
                        loggedToSystemLogTableFlag = true;
                    } else {
                        logger.error("Skipping file " + filename);
                    }
                }
            } else {
                logger.error("Please verify the accuracy of the arguments.");
                System.exit(-1);
            }
        } catch (FileNotFoundException e) {
            logger.error("The specified file name ("
                    + inputFileName + ") was not found.  "
                    + "Please check your input parameter for accuracy.", e);
            //Do not print the exception message.
            //Do not print stack trace.
        } catch (IOException e) {
            logger.error("IOException: " + e.getMessage(), e);
        } catch (NullPointerException npe) {
            logger.error("NullPointerException: " + npe.getMessage(), npe);
        } // end try/catch block
    } // end main()

    /**
     * Inner class created to manage the database transactions.
     * @author kcunning
     *
     */
    public class BulkTransactionCallback implements TransactionCallback {
        /** Debug logging. */
        private Logger logger = Logger.getLogger(getClass().getName());
        /** Reader for the dataset import file. */
        private BufferedReader inputReader;
        /**
         * Flag indicating whether we need to log another modify action
         * to the dataset system log table.
         */
        private boolean loggedToSystemLogTableFlag;
        /** store student, subgoal attempt at step here,
         *  because not saved to db until end of transaction */
        private MultiKeyMap nextAttemptMap = new MultiKeyMap();

        /**
         * Constructor.
         * @param loggedToSystemLog indicates whether the system log table was updated
         * @param inputReader the reader to user later
         */
        public BulkTransactionCallback(boolean loggedToSystemLog, BufferedReader inputReader) {
            this.loggedToSystemLogTableFlag = loggedToSystemLog;
            this.inputReader = inputReader;
        }

        /**
         * Return the max subgoal attempt for the given student and subgoal.
         * Retrieve from transaction dao if not in the cache, then cache
         * and return the incremented value.
         * @param studentItem the student item
         * @param subgoalItem the subgoal item
         * @return the max subgoal attempt number
         */
        private int getNextAttemptAtSubgoal(StudentItem studentItem, SubgoalItem subgoalItem) {
            Comparable studId = studentItem.getId(), subId = subgoalItem.getId();
            int nextAttempt;

            if (!nextAttemptMap.containsKey(studId, subId)) {
                nextAttempt = DaoFactory.DEFAULT.getTransactionDao().getNextAttemptAtSubgoal(
                        studentItem, subgoalItem);
            } else {
                nextAttempt = (Integer)nextAttemptMap.get(studId, subId) + 1;
            }
            nextAttemptMap.put(studId, subId, nextAttempt);

            return nextAttempt;
        }

        /**
         * Do a batch of line items at a time.  Hoping to improve speed.
         * @param ts TransactionStatus.
         * @return true if successful, false otherwise
         */
        public Object doInTransaction(TransactionStatus ts) {
            try {
                List<DatasetLineItem> lineItems = readTheLines(inputReader);
                logger.info("Preparing to process a batch of " + lineItems.size() + " items.");
                DatasetItem datasetItem = getDatasetItem(loggedToSystemLogTableFlag, datasetName);
                TransactionDao txDao = DaoFactory.DEFAULT.getTransactionDao();

                if (kcPresent) {
                    processSkillModels(datasetItem);
                }

                for (DatasetLineItem dli : lineItems) {
                    SessionItem sessionItem = processSession(datasetItem, dli);
                    TransactionItem txItem = datasetLineItemToTx(dli);
                    StudentItem studentItem = sessionItem.getStudent();
                    txItem.setDataset(datasetItem);
                    txItem.setSession(sessionItem);

                    ArrayList datasetLevels = processDatasetLevels(datasetItem,
                                                                   dli.getDatasetLevels());
                    ProblemItem problemItem = processProblem(dli.getProblemName(),
                                                             datasetLevels);
                    txItem.setProblem(problemItem);
                    SubgoalItem subgoalItem = processSubgoal(dli.getStepName(), problemItem);
                    Set knowledgeComponents = new HashSet();
                    if (kcPresent) {
                        knowledgeComponents =
                            processSkills(dli.getKnowledgeComponents(), subgoalItem);
                    }
                    txItem.setSkills(knowledgeComponents);
                    if (stepNamePresent) {
                        txItem.setSubgoal(subgoalItem);
                    }
                    if (outcomePresent) {
                        txItem.setOutcome(processOutcome(dli.getOutcome()));
                    }
                    SubgoalAttemptItem subgoalAttemptItem = createSubgoalAttempt(subgoalItem,
                            txItem.getOutcome());
                    List<AttemptActionItem> processedAttemptActions =
                        processAttemptActions(dli.getActions(), subgoalAttemptItem);
                    List<AttemptSelectionItem> processedAttemptSelections =
                        processAttemptSelections(dli.getSelections(), subgoalAttemptItem);
                    List<AttemptInputItem> processedAttemptInputs =
                        processAttemptInputs(dli.getInputs(), subgoalAttemptItem);
                    subgoalAttemptItem = processSubgoalAttempt(subgoalItem, subgoalAttemptItem,
                            processedAttemptSelections, processedAttemptActions,
                            processedAttemptInputs, problemItem);
                    if (subgoalAttemptItem == null) {
                        logger.error("Subgoal Attempt is null!  Skipping feedback processing.");
                    } else if (feedbackTextPresent) {
                        txItem.setFeedback(processFeedback(subgoalAttemptItem,
                                problemItem, dli.getFeedbackText(),
                                dli.getFeedbackClassification()));
                    }
                    txItem.setSubgoalAttempt(subgoalAttemptItem);

                    if (subgoalItem != null) {
                        txItem.setAttemptAtSubgoal(
                                getNextAttemptAtSubgoal(studentItem, subgoalItem));
                    }

                    // this could be null
                    logDebug("total num hints is ", dli.getTotalNumberHints());
                    if (totalNumHintsPresent) {
                        logDebug("total num hints present.");
                    }
                    if (conditionPresent) {
                        Set<ConditionItem> conditions =
                            processConditions(datasetItem, dli.getConditions());
                        for (ConditionItem condition : conditions) {
                            txItem.addCondition(condition);
                        }
                    }
                    txItem.setGuid(txDao.generateGUID());
                    txDao.saveOrUpdate(txItem);

                    //all items that require the transaction_id come after here.
                    if (cfPresent) {
                        processCustomFields(datasetItem,
                                            new HashSet(dli.getCustomFields().values()), txItem);
                    }


                    if (linesToProcess <= BATCH_SIZE) {
                        logDebug("Processed line ", lineCounter, "/", linesToProcess);
                    }
                    lineCounter++;
                }
                logDebug("eof is ", eof);
                if (linesToProcess > BATCH_SIZE) {
                    logger.info("Processed " + (lineCounter - 1) + " of " + linesToProcess
                            + " lines to process.");
                }
                itemCache.clearAll();

                return eof;
            } catch (Throwable exception) {
                logger.error("Throwable: " + exception.getMessage(), exception);
                ts.setRollbackOnly();
                eof = true;
                return eof;
            }
        }

        /**
         * Convert a line from the file to a transaction.
         * @param dli the line of data
         * @return a transaction item
         */
        private TransactionItem datasetLineItemToTx(DatasetLineItem dli) {
            TransactionItem txItem = new TransactionItem();

            txItem.setTransactionTime(getDate(dli.getTime()));
            txItem.setTimeZone(dli.getTimeZone());
            txItem.setTransactionTypeTool(dli.getStudentResponseType());
            txItem.setTransactionSubtypeTool(dli.getStudentResponseSubtype());
            txItem.setTransactionTypeTutor(dli.getTutorResponseType());
            txItem.setTransactionSubtypeTutor(dli.getTutorResponseSubtype());
            logDebug("setting total num hints ", dli.getTotalNumberHints());
            if (dli.getTotalNumberHints() != null) {
                txItem.setTotalNumHints(dli.getTotalNumberHints().shortValue());
            }
            if (dli.getHelpLevel() != null) {
                txItem.setHelpLevel(dli.getHelpLevel().shortValue());
            }
            txItem.setClassItem(processClass(dli.getClassID()));
            txItem.setSchool(processSchool(dli.getSchool()));
            return txItem;
        }
    } // end inner class BulkTransactionCallback
} // end DatasetImportTool Class
