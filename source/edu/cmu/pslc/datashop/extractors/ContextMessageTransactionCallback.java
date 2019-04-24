/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2014
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.extractors;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import edu.cmu.pslc.datashop.dao.ClassDao;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.DatasetLevelDao;
import edu.cmu.pslc.datashop.dao.InstructorDao;
import edu.cmu.pslc.datashop.dao.MessageDao;
import edu.cmu.pslc.datashop.dao.ProblemDao;
import edu.cmu.pslc.datashop.dao.ProblemEventDao;
import edu.cmu.pslc.datashop.dao.ProjectDao;
import edu.cmu.pslc.datashop.dao.SchoolDao;
import edu.cmu.pslc.datashop.dao.SessionDao;
import edu.cmu.pslc.datashop.dao.StudentDao;
import edu.cmu.pslc.datashop.helper.DatasetCreator;
import edu.cmu.pslc.datashop.helper.SystemLogger;
import edu.cmu.pslc.datashop.item.ClassItem;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.DatasetLevelItem;
import edu.cmu.pslc.datashop.item.InstructorItem;
import edu.cmu.pslc.datashop.item.MessageItem;
import edu.cmu.pslc.datashop.item.ProblemEventItem;
import edu.cmu.pslc.datashop.item.ProblemItem;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.SchoolItem;
import edu.cmu.pslc.datashop.item.SessionItem;
import edu.cmu.pslc.datashop.item.StudentItem;
import edu.cmu.pslc.datashop.util.DataShopInstance;
import edu.cmu.pslc.datashop.xml.ContextMessage;

/**
 * Handle the transaction for context message.
 *
 * @author Hui Cheng
 * @version $Revision: 14294 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2017-09-29 09:42:01 -0400 (Fri, 29 Sep 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ContextMessageTransactionCallback implements TransactionCallback {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**The context message to be saved.*/
    private ContextMessage contextMessage;

    /**The message item whose processed flag need to be updated.*/
    private MessageItem messageItem;

    /**The time to compare the server receipt time to so we can check the age of the data */
    private Date currentTime;

    /** Flag indicating whether any warnings or error occurred while processing this message. */
    private boolean errorFlag = false;

    /** String holding any/all the warnings and errors that occur while processing this message.*/
    private StringBuffer errorMessages = new StringBuffer();

    /**
     * Constructor.
     * @param cm ContextMessage to be processed.
     * @param mi MessageItem whose status should be updated.
     * @param time Date of when the TMC is running to compare the server receipt against.
     */
    public ContextMessageTransactionCallback(ContextMessage cm, MessageItem mi, Date time) {
        this.contextMessage = cm;
        this.messageItem = mi;
        this.currentTime = time;
    }

    /**
     * Save the session item.
     * @param sessionTag the actual session string
     * @param datasetItem the dataset
     * @param studentItem the student
     * @param startTime the start time of the session
     * @return a saved session item
     */
    private SessionItem saveSession(String sessionTag, DatasetItem datasetItem,
            StudentItem studentItem, Date startTime) {
        SessionDao sessionDao = DaoFactory.DEFAULT.getSessionDao();

        SessionItem sessionItem =
                sessionDao.get(sessionTag, datasetItem, (Long)studentItem.getId());
        if (sessionItem == null) {
            sessionItem = new SessionItem();
            sessionItem.setSessionTag(sessionTag);
            sessionItem.setStartTime(startTime);
            sessionItem.setDataset(datasetItem);
            sessionItem.setStudent(studentItem);
            sessionDao.saveOrUpdate(sessionItem);
        } else {
            sessionItem.setStudent(studentItem);
            sessionDao.saveOrUpdate(sessionItem);
        }
        return sessionItem;
    }

    /**
     * Save a school item.
     * @param schoolItem the SchoolItem to be saved or retrieved.
     * @return schoolItem that have been updated with id and etc..
     * @throws Exception thrown for rollback in caller.
     */
    private SchoolItem saveSchool (SchoolItem schoolItem) throws Exception {
        SchoolDao schoolDao = DaoFactory.HIBERNATE.getSchoolDao();

        if (schoolItem != null && schoolItem.getSchoolName() != null
                && !schoolItem.getSchoolName().equals("")) {
            String schoolName = schoolItem.getSchoolName();
            Collection schoolList = schoolDao.find(schoolName);
            // if the school does not exist, then save it to the database
            if (!schoolList.contains(schoolItem)) {
                schoolDao.saveOrUpdate(schoolItem);
                return schoolItem;
            } else {
                for (Iterator iter = schoolList.iterator(); iter.hasNext();) {
                    SchoolItem item = (SchoolItem)iter.next();
                    if (item.equals(schoolItem)) {
                        return item;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Save a student item.
     * @param studentItem the StudentItem to be saved or retrieved.
     * @return studentItem that have been updated with id and etc..
     * @throws Exception all exceptions for rollback in caller.
     */
    private StudentItem saveStudent (StudentItem studentItem) throws Exception {
        StudentDao studentDao = DaoFactory.HIBERNATE.getStudentDao();
        edu.cmu.pslc.datashop.mapping.dao.StudentDao mappedStudentDao =
            edu.cmu.pslc.datashop.mapping.dao.DaoFactory.HIBERNATE.getStudentDao();

        if (studentItem != null && studentItem.getActualUserId() != null
                && !studentItem.getActualUserId().equals("")) {

            // if user id is anonymized already, then do not anonymize it again.
            if (contextMessage.getAnonFlag() != null
                && contextMessage.getAnonFlag().booleanValue()) {
                // put the anonymous id in the anonymous field
                studentItem.setAnonymousUserId(studentItem.getActualUserId());
                // and put a prefix on the anonymous id for the actual field
                studentItem.setActualUserId("ds_student_" + studentItem.getAnonymousUserId());
                contextMessage.setAppearsAnonIsNA(false);
            } else {
                studentItem = encryptUserId(studentItem);
                contextMessage.setAppearsAnonIsNA(true);
            }

            // check if the student exists already or not
            Collection studentList = mappedStudentDao.find(studentItem.getActualUserId());
            if (studentList.size() <= 0) {
                // Student doesn't exist.
                String actualUserId = studentItem.getActualUserId();

                // Don't persist actual_user_id in analysis_db
                studentItem.setActualUserId(null);
                studentDao.saveOrUpdate(studentItem);

                // Write full student info to mapping_db.
                edu.cmu.pslc.datashop.mapping.item.StudentItem mappedStudentItem =
                    getMappedStudentItem(studentItem);
                mappedStudentItem.setActualUserId(actualUserId);
                mappedStudentDao.saveOrUpdate(mappedStudentItem);

                return studentItem;
            } else {
                //the student should exist, so find it in this list
                StudentItem foundStudent = null;
                for (Iterator iter = studentList.iterator(); iter.hasNext();) {
                    edu.cmu.pslc.datashop.mapping.item.StudentItem item =
                        (edu.cmu.pslc.datashop.mapping.item.StudentItem)iter.next();
                    if (item.getActualUserId().equals(studentItem.getActualUserId())) {
                        foundStudent = getOriginalStudentItem(item);
                        break;
                    }
                }
                if (foundStudent == null) {
                    logger.warn("saveStudent: student not found ["
                                + studentItem.getActualUserId() + "]");
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("saveStudent: student found ["
                                     + studentItem.getActualUserId() + "]");
                    }
                }
                return foundStudent;
            }
        }
        return null;
    }

    /**
     * Helper method to encrypt student id and check for duplicates.
     *
     * @param student the StudentItem
     * @return StudentItem
     */
    private StudentItem encryptUserId(StudentItem student) {

        student.encryptUserId();

        // Ensure the generate anon_user_id doesn't already exist. Rare but possible.
        StudentDao studentDao = DaoFactory.DEFAULT.getStudentDao();
        List<StudentItem> matches = (List<StudentItem>)studentDao.findByAnonId(student.getAnonymousUserId());
        while (matches.size() > 0) {
            student.encryptUserId();
            matches = (List<StudentItem>)studentDao.findByAnonId(student.getAnonymousUserId());
        }

        return student;
    }

    /**
     * Convert original StudentItem to one suitable for the mapping_db.
     * @param item the original student item
     * @return edu.cmu.pslc.datashop.mapping.item.StudentItem
     */
    private edu.cmu.pslc.datashop.mapping.item.StudentItem getMappedStudentItem(StudentItem item) {
        edu.cmu.pslc.datashop.mapping.item.StudentItem result = 
            new edu.cmu.pslc.datashop.mapping.item.StudentItem();

        result.setActualUserId(item.getActualUserId());
        result.setAnonymousUserId(item.getAnonymousUserId());
        result.setOriginalId((Long)item.getId());

        return result;
    }

    /**
     * Convert mapped StudentItem to the original.
     * @param mapped the StudentItem in mapping_db
     * @return StudentItem item the original student item
     */
    private StudentItem getOriginalStudentItem(edu.cmu.pslc.datashop.mapping.item.StudentItem mapped) {
        StudentDao studentDao = DaoFactory.HIBERNATE.getStudentDao();
        return studentDao.find((Long)mapped.getOriginalId());
    }

    /**
     * Save a instructor item.
     * @param instructorItem the InstructorItem to be saved or retrieved.
     * @return InstructorItem.
     * @throws Exception all exceptions for rollback in caller.
     */
    private InstructorItem saveInstructor (InstructorItem instructorItem) throws Exception {
        InstructorDao instructorDao = DaoFactory.HIBERNATE.getInstructorDao();

        if (instructorItem != null && instructorItem.getInstructorName() != null
                && !instructorItem.getInstructorName().equals("")) {
            Collection instList = instructorDao.find(instructorItem.getInstructorName());
            // if the school does not exist, then save it to the database
            if (!instList.contains(instructorItem)) {
                instructorDao.saveOrUpdate(instructorItem);
                return instructorItem;
            } else {
                for (Iterator iter = instList.iterator(); iter.hasNext();) {
                    InstructorItem item = (InstructorItem)iter.next();
                    if (item.equals(instructorItem)) {
                        return item;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Save a class item.
     * @param classItem the ClassItem to be saved or retrieved.
     * @return ClassItem.
     * @throws Exception all exceptions for rollback in caller.
     */
    private ClassItem saveClass (ClassItem classItem) throws Exception {
        ClassDao classDao = DaoFactory.HIBERNATE.getClassDao();

        if (classItem != null && classItem.getClassName() != null
                && !classItem.getClassName().equals("")) {
            Collection classList = classDao.find(classItem.getClassName());
            // if the class does not exist, then save it to the database
            if (!classList.contains(classItem)) {
                classDao.saveOrUpdate(classItem);
                return classItem;
            } else {
                for (Iterator iter = classList.iterator(); iter.hasNext();) {
                    ClassItem item = (ClassItem)iter.next();
                    if (item.equals(classItem)) {
                        return item;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Save a problem item.
     * @param datasetLevelItem the dataset level where this problem belongs
     * @param problemItem the ProblemItem to be saved or retrieved
     * @return ProblemItem.
     * @throws Exception all exceptions for rollback in caller
     */
    private ProblemItem saveProblem (DatasetLevelItem datasetLevelItem, ProblemItem problemItem)
            throws Exception {
        ProblemDao problemDao = DaoFactory.HIBERNATE.getProblemDao();

        if (problemItem != null && problemItem.getProblemName() != null
                && !problemItem.getProblemName().equals("")) {
            // The problem list of existing problems for this dataset level
            Collection problemList = datasetLevelItem.getProblemsExternal();
            // The potentially new problem item
            problemItem = (ProblemItem)problemDao.findOrCreateIgnoreDescription(
                problemList, problemItem);

            datasetLevelItem.addProblem(problemItem);
            problemDao.saveOrUpdate(problemItem);
        } else {
            if (problemItem == null) {
                logError("Problem item is null.");
            } else if (problemItem.getProblemName() == null) {
                logError("Problem name is null.");
            } else if (problemItem.getProblemName().equals("")) {
                logError("Problem name is empty.");
            } else {
                logError("Problem is missing.");
            }
        }

        return problemItem;
    }

    /**
     * Saves a problem event based on the name of the context message.
     */
    private void saveProblemEvent() {
        if (contextMessage.getName() == null || contextMessage.getName().equals("")) {
            return;
        }

        ProblemEventDao problemEventDao = DaoFactory.HIBERNATE.getProblemEventDao();
        ProblemDao problemDao = DaoFactory.HIBERNATE.getProblemDao();
        ProblemItem problemItem = contextMessage.getProblemItem();

        if (problemItem != null && problemItem.getId() != null) {
            problemItem = problemDao.get((Long)problemItem.getId());
            ProblemEventItem problemEventItem = new ProblemEventItem();
            problemEventItem.setProblem(new ProblemItem((Long)problemItem.getId()));

            problemEventItem.setEventType(contextMessage.getName());
            problemEventItem.setSession(contextMessage.getSessionItem());
            problemEventItem.setStartTime(messageItem.getTime());

            Collection problemEventList = problemItem.getProblemEventsExternal();
            problemEventItem =
                (ProblemEventItem)problemEventDao.findOrCreate(problemEventList, problemEventItem);
            problemDao.saveOrUpdate(problemItem);
        }
    }

    /**
     * Save a datasetLevel item.
     * Note this method is recursive.
     * @param datasetItem the dataset
     * @param parentLevel the parent level if there is one
     * @param levelToSave a top level dataset level to be retrieved or saved
     * @return DatasetLevelItem.
     * @throws Exception all exceptions for rollback in caller.
     */
    private ProblemItem saveDatasetLevel(DatasetItem datasetItem,
            DatasetLevelItem parentLevel, DatasetLevelItem levelToSave)
            throws Exception {
        ProblemItem problemItem = null;
        DatasetLevelDao datasetLevelDao = DaoFactory.DEFAULT.getDatasetLevelDao();
        //first save this datasetLevel item
        if (levelToSave != null
                && levelToSave.getLevelName() != null
                && !levelToSave.getLevelName().equals("")) {
            List children = levelToSave.getChildrenExternal();
            List problems = levelToSave.getProblemsExternal();

            Collection levelList = null;
            if (parentLevel != null) {
                levelList = parentLevel.getChildrenExternal();
            } else {
                levelList = datasetItem.getDatasetLevelsExternal();
            }
            levelToSave = (DatasetLevelItem)
                datasetLevelDao.findOrCreate(levelList, levelToSave);
            datasetItem.addDatasetLevel(levelToSave);
            datasetLevelDao.saveOrUpdate(levelToSave);

            //loop through all children, and call this method recursively
            if (children.size() == 0) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Lowest level found: " + levelToSave);
                }
                //handle problems that are associated to this datasetLevel as it has no children
                if (problems.size() > 0) {
                    if (problems.size() > 1) {
                        logger.error("More than one problem found in dataset level " + levelToSave);
                    }
                    ProblemItem problem = (ProblemItem)problems.get(0);
                    problem.setDatasetLevel(levelToSave);
                    problemItem = saveProblem(levelToSave, problem);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Problem saved is " + problemItem);
                    }
                } else {
                    logger.error("Problem expected but not found in dataset level " + levelToSave);
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Children levels found for: " + levelToSave);
                }
                if (children.size() > 1) {
                    logger.error("More than one child level found in dataset level " + levelToSave);
                }
                DatasetLevelItem child = (DatasetLevelItem)children.get(0);
                child.setDataset(levelToSave.getDataset());
                child.setParent(levelToSave);
                problemItem = saveDatasetLevel(datasetItem, levelToSave, child);
            }
        } else {
            if (levelToSave == null) {
                logError("Dataset level is null.");
            } else if (levelToSave.getLevelName() == null) {
                logError("Dataset level name is null.");
            } else if (levelToSave.getLevelName().equals("")) {
                logError("Dataset level name is empty.");
            } else {
                logError("Dataset level is missing.");
            }
        }

        return problemItem;
    }

    /** 
     * Save a dataset item.
     * @param incompleteDatasetItem the DatasetItem to be saved or retrieved.
     * @param classItem the DatasetItem to be saved or retrieved.
     * @return DatasetItem.
     * @throws Exception all exceptions for rollback in caller.
     */
    private DatasetItem saveDataset(DatasetItem incompleteDatasetItem, ClassItem classItem)
            throws Exception {
        DatasetItem actualDatasetItem = null;
        DatasetDao datasetDao = DaoFactory.HIBERNATE.getDatasetDao();
        ProjectDao projectDao = DaoFactory.HIBERNATE.getProjectDao();

        //save the dataset item
        if (incompleteDatasetItem != null
                && incompleteDatasetItem.getDatasetName() != null
                && !incompleteDatasetItem.getDatasetName().equals("")) {

            if (logger.isDebugEnabled()) {
                logger.debug("Saving incomplete dataset: " + incompleteDatasetItem);
            }

            String datasetName = incompleteDatasetItem.getDatasetName();
            if (DataShopInstance.isSlave()) {
                datasetName += DataShopInstance.getSlaveIdStr();
            }

            List datasetList = datasetDao.find(datasetName);
            if (datasetList.size() <= 0) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Creating new dataset: " + datasetName);
                }

                incompleteDatasetItem = DatasetCreator.INSTANCE.createNewDataset(datasetName);
                if (incompleteDatasetItem == null) {
                    logger.debug("Failed to create new DatasetItem.");
                    return null;
                }

                // If the dataset is new, then find and/or create the Unclassified project
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
                    incompleteDatasetItem.setProject(projectItem);
                }
                
                datasetDao.saveOrUpdate(incompleteDatasetItem);
                actualDatasetItem = incompleteDatasetItem;
                SystemLogger.log(actualDatasetItem, SystemLogger.ACTION_CREATE_DATASET,
                        "New Dataset '" + actualDatasetItem.getDatasetName()
                        + "' created from a context message in the TMC");
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Found dataset in list: "
                            + incompleteDatasetItem.getDatasetName());
                }
                if (datasetList.size() > 1) {
                    logger.error("Found " + datasetList.size() + " datasets with name "
                            + incompleteDatasetItem.getDatasetName());
                }
                actualDatasetItem = (DatasetItem)datasetList.get(0);
            }

            datasetDao.saveOrUpdate(actualDatasetItem);

        } else {
            if (incompleteDatasetItem == null) {
                logError("Dataset is null.");
            } else if (incompleteDatasetItem.getDatasetName() == null) {
                logError("Dataset name is null.");
            } else if (incompleteDatasetItem.getDatasetName().equals("")) {
                logError("Dataset name is empty.");
            } else {
                logError("Dataset is missing.");
            }
        }

        return actualDatasetItem;
    }

    /**
     * Implement the interface.
     * All transaction actions are in one method for rollback.
     * The steps are:
     * <UL>
     * <LI>save session</LI>
     * <LI>save school</LI>
     * <LI>save student</LI>
     * <LI>save instructor after setting schoolId</LI>
     * <LI>save class after setting instructorId and schoolId</LI>
     * <LI>save dataset and all related</LI>
     * </UL>
     * @param ts TransactionStatus.
     * @return Object.
     * */
    public Object doInTransaction(TransactionStatus ts) {
        //messageDao is used for updating the process status
        MessageDao messageDao = DaoFactory.HIBERNATE.getMessageDao();

        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Context message id is " + messageItem.getContextMessageId());
            }

            SchoolItem school = contextMessage.getSchoolItem();
            if (school != null) {
                school = saveSchool(school);
                contextMessage.setSchoolItem(school);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("School is " + school);
            }

            // Set the student from the messageItem userId
            StudentItem student = new StudentItem();
            student.setActualUserId(messageItem.getUserId());
            student = saveStudent(student);
            contextMessage.setStudentItem(student);

            if (logger.isDebugEnabled()) {
                logger.debug("Student is " + student);
            }

            InstructorItem instructor = contextMessage.getInstructorItem();
            if (instructor != null) {
                if (school != null) {
                    instructor.setSchool(school);
                }
                instructor = saveInstructor(instructor);
                contextMessage.setInstructorItem(instructor);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Instructor is " + instructor);
            }

            ClassItem classItem = contextMessage.getClassItem();
            if (classItem != null) {
                if (school != null) {
                    classItem.setSchool(school);
                }
                if (instructor != null) {
                    classItem.setInstructor(instructor);
                }
                classItem.addStudent(student);
                classItem = saveClass(classItem);
                contextMessage.setClassItem(classItem);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Class is " + classItem);
            }

            DatasetItem incompleteDatasetItem = contextMessage.getDatasetItem();
            DatasetItem actualDatasetItem = null;

            if (incompleteDatasetItem != null) {
                actualDatasetItem = saveDataset(incompleteDatasetItem, classItem);
            }

            contextMessage.setDatasetItem(actualDatasetItem);

            if (actualDatasetItem != null) {
                actualDatasetItem = DaoFactory.DEFAULT.getDatasetDao()
                        .get((Integer)actualDatasetItem.getId());

                SessionItem sessionItem = saveSession(messageItem.getSessionTag(),
                        actualDatasetItem, contextMessage.getStudentItem(), messageItem.getTime());
                contextMessage.setSessionItem(sessionItem);

                // add the class to this dataset if there is one
                if (classItem != null) {
                    actualDatasetItem.addClass(classItem);
                }

                ProblemItem problemItem = null;

                List datasetLevels = incompleteDatasetItem.getDatasetLevelsExternal();
                //loop through the dataset levels and save each one.
                if (logger.isDebugEnabled()) {
                    logger.debug("Number of dataset levels are " + datasetLevels.size());
                }
                for (Iterator datasetLevelsIter = datasetLevels.iterator();
                    datasetLevelsIter.hasNext();) {
                    DatasetLevelItem datasetLevel = (DatasetLevelItem)datasetLevelsIter.next();
                    datasetLevel.setDataset(actualDatasetItem);
                    datasetLevel.setParent(null);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Number of Children are "
                                + datasetLevel.getChildrenExternal().size());
                    }
                    problemItem = saveDatasetLevel(actualDatasetItem, null, datasetLevel);
                }

                if (problemItem != null && problemItem.getProblemName() != null) {
                    contextMessage.setProblemItem(problemItem);

                    ProblemEventDao problemEventDao = DaoFactory.DEFAULT.getProblemEventDao();
                    if (!problemEventDao.isEventRepresented(contextMessage, messageItem)) {
                        saveProblemEvent();
                    }
                } else {
                    saveProblemEvent();
                }

            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Oh no the actual dataset item is null.");
                }
            }

            //update the messageItem to processedFlag='DONE'
            if (messageItem.getServerReceiptTime() == null) {
                messageItem.setProcessedFlag(MessageItem.SUCCESS_FLAG);
            } else if (currentTime.getTime() - messageItem.getServerReceiptTime().getTime()
                    < AbstractLoader.CONFIDENCE_TIME) {
                messageItem.setProcessedFlag(MessageItem.SUCCESS_QUESTIONABLE_FLAG);
            } else {
                messageItem.setProcessedFlag(MessageItem.SUCCESS_FLAG);
            }
            messageItem.setProcessedTime(new Date());
            messageDao.saveOrUpdate(messageItem);

        } catch (Exception exception) {
            ts.setRollbackOnly();
            logError("Saving of context message failed.", exception);
            return null;
        }
        return contextMessage;

    } //end doInTransaction

    /**
     * Save all the warning and error messages to one string buffer for easy retrieval later.
     * Also save the fact that a warning or error occurred.
     * @param msg the warning or error message
     */
    private void logError(String msg) {
        logError(msg, null);
    }

    /**
     * Save all the warning and error messages to one string buffer for easy retrieval later.
     * Also save the fact that a warning or error occurred.
     * @param msg the warning or error message
     * @param exception if one occurred, null otherwise
     */
    private void logError(String msg, Exception exception) {
        errorFlag = true;
        errorMessages.append(msg);
        errorMessages.append(" ");

        String prefix = "For message user/session/contextMsgId "
                    + messageItem.getUserId() + "/"
                    + messageItem.getSessionTag() + "/ "
                    + messageItem.getContextMessageId()
                    + " where GUID = " + messageItem.getGuid() + ". ";
        if (exception != null) {
            logger.error(prefix + msg, exception);
        } else {
            logger.error(prefix + msg);
        }
    }

    /**
     * Returns the error flag.
     * @return the error flag
     */
    public boolean isErrorFlag() {
        return errorFlag;
    }

    /**
     * Returns the errorMessages.
     * @return the errorMessages
     */
    protected StringBuffer getErrorMessages() {
        return errorMessages;
    }
}