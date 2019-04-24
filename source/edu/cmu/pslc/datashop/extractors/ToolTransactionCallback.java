/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.extractors;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import edu.cmu.pslc.datashop.dao.AttemptActionDao;
import edu.cmu.pslc.datashop.dao.AttemptInputDao;
import edu.cmu.pslc.datashop.dao.AttemptSelectionDao;
import edu.cmu.pslc.datashop.dao.CfTxLevelDao;
import edu.cmu.pslc.datashop.dao.ConditionDao;
import edu.cmu.pslc.datashop.dao.CustomFieldDao;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.SkillDao;
import edu.cmu.pslc.datashop.dao.SkillModelDao;
import edu.cmu.pslc.datashop.dao.SubgoalAttemptDao;
import edu.cmu.pslc.datashop.dao.TransactionDao;
import edu.cmu.pslc.datashop.dto.ItemCache;
import edu.cmu.pslc.datashop.item.AttemptActionItem;
import edu.cmu.pslc.datashop.item.AttemptInputItem;
import edu.cmu.pslc.datashop.item.AttemptSelectionItem;
import edu.cmu.pslc.datashop.item.ConditionItem;
import edu.cmu.pslc.datashop.item.CustomFieldItem;
import edu.cmu.pslc.datashop.item.CustomFieldNameValueItem;
import edu.cmu.pslc.datashop.item.CfTxLevelItem;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.InterpretationItem;
import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.item.MessageItem;
import edu.cmu.pslc.datashop.item.ProblemItem;
import edu.cmu.pslc.datashop.item.SkillItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.item.StudentItem;
import edu.cmu.pslc.datashop.item.SubgoalAttemptItem;
import edu.cmu.pslc.datashop.item.SubgoalItem;
import edu.cmu.pslc.datashop.item.TransactionItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.xml.ContextMessage;
import edu.cmu.pslc.datashop.xml.EventDescriptor;
import edu.cmu.pslc.datashop.xml.SemanticEvent;
import edu.cmu.pslc.datashop.xml.ToolMessage;
import edu.cmu.pslc.datashop.xml.ToolMessageParser;
import edu.cmu.pslc.datashop.xml.ToolMessageParserFactory;
import edu.cmu.pslc.datashop.xml.UiEvent;

/**
 * Handle the transaction for context message.
 *
 * @author Hui Cheng
 * @version $Revision: 13692 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-11-16 09:58:28 -0500 (Wed, 16 Nov 2016) $
 * <!-- $KeyWordsOff: $ -->
 */

public class ToolTransactionCallback implements TransactionCallback {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**The contextMessage which has dataset, student, school and etc
     * information that will be used.*/
    private ContextMessage contextMessage;

    /**The toolMessage to be saved.*/
    private ToolMessage toolMessage;

    /**The MessageItem to be parsed and saved */
    private MessageItem toolMsgItem;

    /** Item cache to help with number of database hits. */
    private ItemCache itemCache;

    /**The transaction item that should be saved/processed.*/
    private TransactionItem transaction = new TransactionItem();

    /**
     * Constructor.
     * @param context ContextMessage that contains information needed.
     * @param messageItem The MessageItem that is a tool message that will be saved.
     * @param itemCache the ItemCache for known items already processed/retrieved and
     * attached to the hibernate session.
     */
    public ToolTransactionCallback(ContextMessage context,
            MessageItem messageItem, ItemCache itemCache) {
        this.contextMessage = context;
        this.toolMsgItem = messageItem;

        //parse the message item to get the tool message.
        ToolMessageParser toolParser = ToolMessageParserFactory.getInstance().get(messageItem);
        this.toolMessage = toolParser.getToolMessage();
        this.itemCache = itemCache;
    }

    /**Save update list of messageItem for the processed status.
     * @param messageItem the messageItemt to be updated.
     * @param status String "DONE" or "FAILED"
     * @throws Exception all exceptions for rollback in caller.
     */
    private void saveMessageItemsProcessStatus (MessageItem messageItem, String status)
        throws Exception {
            messageItem.setProcessedFlag(status);
            messageItem.setProcessedTime(new Date());
            DaoFactory.DEFAULT.getMessageDao().saveOrUpdate(messageItem);

            if (logger.isDebugEnabled()) {
                logger.debug("Saving of tool message, status: " + status
                        + " user/session/contextMsgId "
                        + messageItem.getUserId() + "/" + messageItem.getSessionTag()
                        + ": " + messageItem.getContextMessageId()
                        + ". MessageId: " + messageItem.getId());
            }
    }

    /**
     * Get the first problem that is encountered from the context message.
     * @return ProblemItem that is encountered first while traversing through all dataset levels.
     */
    private ProblemItem getProblemFromContextMessage() {
        ProblemItem returnVal = contextMessage.getProblemItem();
        return returnVal;
    }

    /**
     * Get the first problem that is encountered from the context message.
     * @param toolMessage the tool message
     * @return ProblemItem that is encountered first while traversing through all dataset levels.
     */
    private ProblemItem getProblemFromToolMessage(ToolMessage toolMessage) {
        ProblemItem returnVal = toolMessage.getProblemItem();
        return returnVal;
    }

    /**
     * Create a SubgoalAttempt from the event descriptor(s) in the tool message.
     * Also save the attemptSelection, attemptAction and attemptInput items.
     * @param toolMessage the tool message object
     * @param problemItem the problem item
     * @return a subgoalAttempt
     * @throws Exception all exceptions for rollback in caller.
     */
    private SubgoalAttemptItem getSubgoalAttempt(ToolMessage toolMessage,
                        ProblemItem problemItem) throws Exception {
        SubgoalAttemptItem subgoalAttemptItem = null;


        //Since their is no tutor message with an outcome, set type to untutored
        String correctFlag = SubgoalAttemptItem.CORRECT_FLAG_UNTUTORED;

        SubgoalAttemptDao subgoalAttemptDao = DaoFactory.DEFAULT.getSubgoalAttemptDao();

        if (itemCache.getExistingAttempts().isEmpty()) {
            itemCache.setExistingAttempts(subgoalAttemptDao.find(problemItem));
        }

        List selectionList = new ArrayList();
        List actionList = new ArrayList();
        List inputList = new ArrayList();

        //loop through the tool message event descriptors
        //to get the attempt selections, actions and inputs.
        for (Iterator edIter = toolMessage.getEventDescriptorsExternal().iterator();
                edIter.hasNext();) {
            EventDescriptor eventDesc = (EventDescriptor)edIter.next();
            selectionList.addAll(eventDesc.getSelectionsExternal());
            actionList.addAll(eventDesc.getActionsExternal());
            inputList.addAll(eventDesc.getInputsExternal());
        }

        //create a new item
        SubgoalAttemptItem newAttempt = new SubgoalAttemptItem();
        newAttempt.setCorrectFlag(correctFlag);

        SubgoalAttemptItem currentAttempt =
            getExistingAttempt(newAttempt, selectionList, actionList, inputList);

        if (currentAttempt != null) {
            subgoalAttemptItem = currentAttempt;
        } else {
            subgoalAttemptItem = newAttempt;

            AttemptSelectionDao attemptSelectionDao = DaoFactory.DEFAULT.getAttemptSelectionDao();
            AttemptActionDao attemptActionDao = DaoFactory.DEFAULT.getAttemptActionDao();
            AttemptInputDao attemptInputDao = DaoFactory.DEFAULT.getAttemptInputDao();

            subgoalAttemptDao.saveOrUpdate(subgoalAttemptItem);

            // add the selections
            for (Iterator newIter = selectionList.iterator(); newIter.hasNext();) {
                AttemptSelectionItem newItem = (AttemptSelectionItem)newIter.next();
                subgoalAttemptItem.addAttemptSelection(newItem);
                attemptSelectionDao.saveOrUpdate(newItem);
            }
            // add the actions
            for (Iterator newIter = actionList.iterator(); newIter.hasNext();) {
                AttemptActionItem newItem = (AttemptActionItem)newIter.next();
                subgoalAttemptItem.addAttemptAction(newItem);
                attemptActionDao.saveOrUpdate(newItem);
            }
            // add the inputs
            for (Iterator newIter = inputList.iterator(); newIter.hasNext();) {
                AttemptInputItem newItem = (AttemptInputItem)newIter.next();
                subgoalAttemptItem.addAttemptInput(newItem);
                attemptInputDao.saveOrUpdate(newItem);
            }
        }

        itemCache.addAttempt(subgoalAttemptItem);
        itemCache.setExistingAttemptActions(subgoalAttemptItem, actionList);
        itemCache.setExistingAttemptSelections(subgoalAttemptItem, selectionList);
        itemCache.setExistingAttemptInputs(subgoalAttemptItem, inputList);
        return subgoalAttemptItem;
    }

    /**
     * Its complicated trying to figure out if a subgoal attempt already exists.  You
     * have to loop through all the selections, actions and inputs.
     * @param newAttempt an instance of the attempt with subgoal and other attributes set
     * @param selectionList the list of selection items for the possibly new attempt
     * @param actionList the list of action items for the possibly new attempt
     * @param inputList the list of input items for the possibly new attempt
     * @return the existing attempt or null if not found
     */
    private SubgoalAttemptItem getExistingAttempt(SubgoalAttemptItem newAttempt,
            List selectionList, List actionList, List inputList) {

        AttemptSelectionDao attemptSelectionDao = DaoFactory.DEFAULT.getAttemptSelectionDao();
        AttemptActionDao attemptActionDao = DaoFactory.DEFAULT.getAttemptActionDao();
        AttemptInputDao attemptInputDao = DaoFactory.DEFAULT.getAttemptInputDao();

        SubgoalAttemptItem currentAttempt = null;
        boolean isSameAttempt = true;
        for (Iterator currAttIter = itemCache.getExistingAttempts().iterator();
                currAttIter.hasNext();) {

            currentAttempt = (SubgoalAttemptItem)currAttIter.next();
            isSameAttempt = true;

            // If the attempts do not match, then go on to next possible attempt
            if (currentAttempt.getSubgoal() != null && newAttempt.getSubgoal() == null) {
                isSameAttempt = false;
                continue;
            }

            if (currentAttempt.getSubgoal() == null && newAttempt.getSubgoal() != null) {
                isSameAttempt = false;
                continue;
            }

            if (currentAttempt.getSubgoal() != null && newAttempt.getSubgoal() != null) {
                if (!currentAttempt.getSubgoal().getId().equals(newAttempt.getSubgoal().getId())
                        || !currentAttempt.getCorrectFlag().equals(newAttempt.getCorrectFlag())) {
                    isSameAttempt = false;
                    continue;
                }
            }

            // FIRST check the inputs
            if (itemCache.getExistingAttemptInputs(currentAttempt).isEmpty()) {
                itemCache.setExistingAttemptInputs(
                        currentAttempt, attemptInputDao.find(currentAttempt));
            }

            Collection currentInpList = itemCache.getExistingAttemptInputs(currentAttempt);
            // If the sizes of the list don't even match, then go on to next possible attempt
            if (currentInpList.size() != inputList.size()) {
                isSameAttempt = false;
                continue;
            }
            for (Iterator currIter = currentInpList.iterator(); currIter.hasNext();) {
                AttemptInputItem currItem = (AttemptInputItem)currIter.next();
                boolean found = false;
                for (Iterator newIter = inputList.iterator(); newIter.hasNext();) {
                    AttemptInputItem newItem = (AttemptInputItem)newIter.next();
                    if (currItem.getInput().equals(newItem.getInput())) {
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
            if (itemCache.getExistingAttemptSelections(currentAttempt).isEmpty()) {
                itemCache.setExistingAttemptSelections(
                        currentAttempt, attemptSelectionDao.find(currentAttempt));
            }
            Collection currentSelList = itemCache.getExistingAttemptSelections(currentAttempt);

            // If the sizes of the list don't even match, then go on to next possible attempt
            if (currentSelList.size() != selectionList.size()) {
                isSameAttempt = false;
                continue;
            }
            for (Iterator currIter = currentSelList.iterator(); currIter.hasNext();) {
                AttemptSelectionItem currItem = (AttemptSelectionItem)currIter.next();
                boolean found = false;
                for (Iterator newIter = selectionList.iterator(); newIter.hasNext();) {
                    AttemptSelectionItem newItem = (AttemptSelectionItem)newIter.next();
                    if (currItem.getSelection().equals(newItem.getSelection())) {
                        if (currItem.getType() == null) {
                            if (newItem.getType() == null) {
                                found = true;
                                break;
                            }
                        } else if (currItem.getType().equals(newItem.getType())) {
                            found = true;
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
            if (itemCache.getExistingAttemptActions(currentAttempt).isEmpty()) {
                itemCache.setExistingAttemptActions(
                        currentAttempt, attemptActionDao.find(currentAttempt));
            }
            Collection currentActList = itemCache.getExistingAttemptActions(currentAttempt);

            // If the sizes of the list don't even match, then go on to next possible attempt
            if (currentActList.size() != currentActList.size()) {
                isSameAttempt = false;
                continue;
            }

            for (Iterator currIter = currentActList.iterator(); currIter.hasNext();) {
                AttemptActionItem currItem = (AttemptActionItem)currIter.next();
                boolean found = false;
                for (Iterator newIter = actionList.iterator(); newIter.hasNext();) {
                    AttemptActionItem newItem = (AttemptActionItem)newIter.next();
                    if (currItem.getAction().equals(newItem.getAction())) {
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

        if (!isSameAttempt) {
            currentAttempt = null;
        }
        return currentAttempt;
    }

    /**Set the sessionId, time, time zone for the transaction
     * based on the first element of the toolMsgItemList or the tutorMsgItemList.
     * */
    private void setTxnFieldsByMsgItem() {

        //These fields should be the same for all objects in the tool and tutor lists
        //so just get the first one.
        transaction.setSession(contextMessage.getSessionItem());
        transaction.setTimeZone(toolMsgItem.getTimeZone());
        Date msgItemDate = toolMsgItem.getTime();

        GregorianCalendar msgItemCal = new GregorianCalendar();
        msgItemCal.setTime(msgItemDate);
        GregorianCalendar now = new GregorianCalendar();
        int year = msgItemCal.get(Calendar.YEAR);
        if ((year > now.get(Calendar.YEAR) + VALID_YEAR_FUTURE_OFFSET)
         || (year < now.get(Calendar.YEAR) - VALID_YEAR_PAST_OFFSET)) {
            Date serverReceiptDate = toolMsgItem.getServerReceiptTime();
            GregorianCalendar serverReceiptCal = new GregorianCalendar();
            if (serverReceiptDate != null) {
                serverReceiptCal.setTime(serverReceiptDate);
            }
            int newYear = serverReceiptCal.get(Calendar.YEAR);

            msgItemCal.set(Calendar.YEAR, newYear);

            logger.warn("Invalid year (" + year + ") found in GUID "
                    + toolMsgItem.getGuid() + " resetting year to " + newYear);
        }

        transaction.setTransactionTime(msgItemCal.getTime());

    }

    /** The number of years in the future beyond which the year in the time stamp must be bogus. */
    private static final int VALID_YEAR_FUTURE_OFFSET = 20;

    /** The number of years in the past beyond which the year in the time stamp must be bogus. */
    private static final int VALID_YEAR_PAST_OFFSET = 50;


    /**
     * Set transaction_type/subtype_tutor/tool based on semantic_event.
     * @return flag indicating success
     */
    private boolean setTxnFieldsBySemantic() {
        List toolSemantic = toolMessage.getSemanticEventsExternal();
        if (toolSemantic.size() <= 0) { return false; }
        for (Iterator toolIter = toolSemantic.iterator(); toolIter.hasNext();) {
            SemanticEvent event = (SemanticEvent)toolIter.next();
            transaction.setTransactionSubtypeTool(event.getSubtype());
            transaction.setTransactionTypeTool(event.getName());
            //only use the first action_evaluation, so break now;
            break;
        }
        return true;
    }

    /**
     * Set transaction_type/subtype_tutor/tool based on ui_event.
     * @return flag indicating success
     */
    private boolean setTxnFieldsByUiEvent() {
        List uiEventList = toolMessage.getUiEventsExternal();
        if (uiEventList.size() <= 0) { return false; }
        for (Iterator toolIter = uiEventList.iterator(); toolIter.hasNext();) {
            UiEvent event = (UiEvent)toolIter.next();
            transaction.setTransactionSubtypeTool("UI Event");
            transaction.setTransactionTypeTool(event.getName());
            //only use the first UI event, so break now;
            break;
        }
        return true;
    }

    /**
     * Process skill, skill_model.
     * TODO this method doesn't seem necessary.  Calling it was a bug.  We should
     * not associate the skills with probabilities in the context message to any
     * subsequent transactions for that context message.
     * @throws Exception all exceptions for rollback in caller.
     */
    private void processSkills() throws Exception {
        List skills = contextMessage.getSkillsExternal();

        SkillDao skillDao = DaoFactory.DEFAULT.getSkillDao();
        SkillModelDao skillModelDao = DaoFactory.DEFAULT.getSkillModelDao();
        DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
        DatasetItem datasetItem = transaction.getDataset();
        if (datasetItem != null && datasetItem.getId() != null) {
            datasetItem = datasetDao.get((Integer)datasetItem.getId());
        }

        //populate all the skill models in this dataset that already exist in the DB.
        if (itemCache.getExistingModels().isEmpty()) {
            itemCache.setExistingModels(skillModelDao.find(datasetItem));
        }

        //populate the skills from the db for all existing skills for each skill model.
        if (itemCache.getExistingSkills().isEmpty()) {
            for (Iterator it = itemCache.getExistingModels().iterator(); it.hasNext();) {
                itemCache.setExistingSkills(skillDao.find((SkillModelItem)it.next()));
            }
        }

        //loop through all skills and skill models
        for (Iterator iter = skills.iterator(); iter.hasNext();) {

            SkillItem skillItem = (SkillItem)iter.next();
            SkillModelItem skillModelItem = skillItem.getSkillModel();
            skillModelItem.setDataset(datasetItem);

            //see if the skillModelItem already exists in the list of existing skills,
            //create it if it doesn't and add it to the list of existing skills models.
            skillModelItem = (SkillModelItem)skillModelDao.
                                findOrCreate(itemCache.getExistingModels(), skillModelItem);
            if (!itemCache.getExistingModels().contains(skillModelItem)) {
                itemCache.getExistingModels().add(skillModelItem);
            }

            if (skillItem.getSkillName() != null) {
                skillItem.setSkillModel(skillModelItem);
                skillItem = (SkillItem)skillDao.findOrCreate(
                                itemCache.getExistingSkills(), skillItem);
            } else {
                continue;
            }

            if (!itemCache.getExistingSkills().contains(skillItem)) {
                itemCache.addModel(skillModelItem);
                skillModelItem.addSkill(skillItem);
                skillItem.setSkillModel(skillModelItem);
            }

            //Note: this is a unidirectional association
            transaction.addSkill(skillItem);
            if (transaction.getSubgoal() != null) {
                transaction.getSubgoal().addSkillFast(skillItem);
            }

            skillDao.saveOrUpdate(skillItem);

            itemCache.addSkill(skillItem);
        }
    }

    /**Process custom fields
     * @param datasetItem the dataset we are currently working on.
     * @param problemItem the problem currently being worked on.
     * @param studentItem the student currently being worked on.
     * @throws Exception all exceptions for rollback in caller.
     * */
    private void processCustomFields (DatasetItem datasetItem, ProblemItem problemItem,
            StudentItem studentItem) throws Exception {
        List toolCustom = toolMessage.getCustomFieldsExternal();
        if (toolCustom.isEmpty()) { return; }

        CustomFieldDao customFieldDao = DaoFactory.DEFAULT.getCustomFieldDao();

        if (itemCache.getExistingCustomFields().isEmpty()) {
            itemCache.setExistingCustomFields(customFieldDao.find(datasetItem));
        }

        String toolMsgGuid = toolMsgItem.getGuid();

        //loop through all tool custom fields
        for (Iterator toolIter = toolCustom.iterator(); toolIter.hasNext();) {
            CustomFieldNameValueItem toolItem = (CustomFieldNameValueItem)toolIter.next();
            if (toolItem.getName() == null
             || toolItem.getName().length() <= 0) {
                logger.warn("Custom field name is null or zero length for tool message with GUID: "
                               + toolMsgGuid);
                continue;
            }
            if (toolItem.getValue() == null
             || toolItem.getValue().length() <= 0) {
                logger.warn("Custom field value is null or zero length for tool message with GUID: "
                        + toolMsgGuid);
                continue;
            }
            //set custom field value
            String customFieldValue = toolItem.getBigValue() != null
                ? toolItem.getBigValue() : toolItem.getValue();
            toolItem.setValue(customFieldValue);
            CustomFieldItem customFieldItem = toolItem.makeACustomFieldItem();
            customFieldItem.setDataset(transaction.getDataset());
            customFieldItem.setLevel(CustomFieldItem.CF_LEVEL_TRANSACTION);
            customFieldItem.setOwner(new UserItem (UserItem.SYSTEM_USER));
            customFieldItem = (CustomFieldItem)customFieldDao.findOrCreate(
                        itemCache.getExistingCustomFields(), customFieldItem);
            //process cf_tx_level
            CfTxLevelItem cfTxLevelItem = toolItem.makeACfTxLevelItem(customFieldItem, transaction);
            cfTxLevelItem.setLoggingFlag(true);
            CfTxLevelDao cfTxLevelDao = DaoFactory.DEFAULT.getCfTxLevelDao();
            cfTxLevelDao.saveOrUpdate(cfTxLevelItem);


            itemCache.addCustomField(customFieldItem);
        }
    }

    /**
     * Process conditions.
     * @throws Exception all exceptions for rollback in caller
     */
    private void processConditions () throws Exception {
        List contextConditionsList = contextMessage.getConditionsExternal();
        if (contextConditionsList.isEmpty()) { return; }

        ConditionDao conditionDao = DaoFactory.DEFAULT.getConditionDao();

        if (itemCache.getExistingConditions().isEmpty()) {
            itemCache.setExistingConditions(conditionDao.find(transaction.getDataset()));
        }

        //loop through all the conditions
        for (Iterator tutorIter = contextConditionsList.iterator(); tutorIter.hasNext();) {
            ConditionItem conditionItem = (ConditionItem)tutorIter.next();
            conditionItem.setDataset(transaction.getDataset());
            if (logger.isDebugEnabled()) {
                logger.debug("ConditionItem: " + conditionItem);
                logger.debug("ConditionList: " + itemCache.getExistingConditions());
            }
            conditionItem = (ConditionItem)conditionDao.findOrCreate(
                    itemCache.getExistingConditions(), conditionItem);
            conditionDao.saveOrUpdate(conditionItem);
            itemCache.addCondition(conditionItem);
            transaction.addCondition(conditionItem);
        }
    }

    /**
     * Implement the interface.
     * Put all actions in this for transaction management.
     * @param ts TransactionStatus.
     * @return true if successful, false otherwise
     */
    public Object doInTransaction(TransactionStatus ts) {

        Date startTime = null;
        if (logger.isDebugEnabled()) { startTime = new Date(); }

        try {
            //get the problem from the context message
            ProblemItem problemItem = getProblemFromContextMessage();
            if (problemItem == null) {
               // then look in tool message
                problemItem = getProblemFromToolMessage(toolMessage);
                if (problemItem == null) {
                    // TODO finally get the default problem if nothing else
                    //problemItem = problemDao.
                    logger.warn("ProblemItem is null");
                    return null;
                }
            }

            if (problemItem == null || problemItem.getId() == null) {
                logger.warn("ProblemItem or it's ID is null :: " + problemItem);
                return null;
            }

            if (itemCache.getProblem() == null) {
                problemItem = DaoFactory.DEFAULT.getProblemDao().get((Long)problemItem.getId());
                itemCache.setProblem(problemItem);
            } else { problemItem = itemCache.getProblem(); }

            //logger.info(problemItem);
            DatasetItem datasetItem = contextMessage.getDatasetItem();
            datasetItem = DaoFactory.DEFAULT.getDatasetDao().get((Integer)datasetItem.getId());
            transaction.setDataset(datasetItem);

            //get the subgoal attempt message including connecting to the subgoal and
            //set the attempt selection, input and action.
            //TODO because there may be multiple action evaluations,
            //we may need to save multiple subgoal attempt.
            SubgoalAttemptItem subgoalAttemptItem = getSubgoalAttempt(toolMessage, problemItem);
            startTime = TutorMessageConverter.logTime(logger, "getSubgoalAttempt: ", startTime);

            transaction.setProblem(problemItem);

            //set all fields for transaction and save
            transaction.setSubgoalAttempt(subgoalAttemptItem);
            startTime = TutorMessageConverter.logTime(logger, "setSubgoalAttempt: ", startTime);

            if (contextMessage.getSchoolItem() != null) {
                transaction.setSchool(contextMessage.getSchoolItem());
            }
            startTime = TutorMessageConverter.logTime(logger, "setSchoolItem: ", startTime);

            if (contextMessage.getClassItem() != null) {
                transaction.setClassItem(contextMessage.getClassItem());
            }
            startTime = TutorMessageConverter.logTime(logger, "setClassItem: ", startTime);

            //Use semantic_event for transaction_type/subtype_tutor/tool
            //TODO we only use the first semantic_event. need to handle multiple.
            boolean semanticEventsFound = setTxnFieldsBySemantic();
            if (semanticEventsFound) {
            startTime = TutorMessageConverter.logTime(logger,
                        "setTxnFieldsBySemantic: ", startTime);
            } else {
                boolean uiEventsFound = setTxnFieldsByUiEvent();
                if (uiEventsFound) {
                startTime = TutorMessageConverter.logTime(logger,
                            "setTxnFieldsByUiEvent: ", startTime);
                }
            }

            //Use messageItem to transaction set sessionId, time zone, time
            setTxnFieldsByMsgItem();
            startTime = TutorMessageConverter.logTime(logger,
                    "setTxnFieldsByMsgItem in : ", startTime);

            TransactionDao transactionDao = DaoFactory.DEFAULT.getTransactionDao();
            //set transaction guid
            transaction.setGuid(transactionDao.generateGUID());
            transactionDao.saveOrUpdate(transaction);
            startTime = TutorMessageConverter.logTime(logger, "Saved transaction in : ", startTime);

            processConditions();
            startTime = TutorMessageConverter.logTime(logger,
                    "Processed conditions in: ", startTime);

            transactionDao.saveOrUpdate(transaction);

            //anything requiring the transaction id comes after the transaction save.
            processCustomFields(datasetItem, problemItem, contextMessage.getStudentItem());
            startTime = TutorMessageConverter.logTime(logger,
                    "Processed custom fields in: ", startTime);

            //update the processed flag for the associated messageItems
            saveMessageItemsProcessStatus(toolMsgItem, MessageItem.SUCCESS_FLAG);
            startTime = TutorMessageConverter.logTime(logger,
                        "Saved process status in: ", startTime);
        } catch (Throwable exception) {

            ts.setRollbackOnly();
            String guid = toolMsgItem.getGuid();
            String userId = toolMsgItem.getUserId();
            String sessionId = toolMsgItem.getSessionTag();
            String contextMsgId = toolMsgItem.getContextMessageId();

            logger.error("Saving of tool/tutor message failed: " + " for GUID " + guid
                    + " which is user/session/contextMsgId: "
                    + userId + "/" + sessionId + "/" + contextMsgId
                    + ". Exception: " + exception, exception);
            return null;
        }
        return itemCache;
    }

    /**
     * A simple container for one subgoal item, the chosen interpretation
     * and a list of the alternative interpretations, if there are any.
     */
    class SubgoalInterps {
        /** Subgoal. */
        private SubgoalItem subgoalItem = null;
        /** Interpretation. */
        private InterpretationItem chosenInterpretation = null;
        /** List of alternative interpretations. */
        private List alternativeInterpretations = new ArrayList();
        /** Default constructor. */
        public SubgoalInterps() {
        }
        /**
         * Returns the interpretation item.
         * @return the interpretation item
         */
        public InterpretationItem getChosenInterpretation() {
            return chosenInterpretation;
        }
        /**
         * Sets the chosen interpretation.
         * @param chosenInterpretation the chosen interpretation
         */
        public void setChosenInterpretation(InterpretationItem chosenInterpretation) {
            this.chosenInterpretation = chosenInterpretation;
        }
        /**
         * Returns the list of alternative interpretations.
         * @return the list of alternative interpretations
         */
        public List getAlternativeInterpretations() {
            return alternativeInterpretations;
        }
        /**
         * Sets the list of alternative interpretations.
         * @param otherInterpretations the list of alternative interpretations
         */
        public void setAlternativeInterpretations(List otherInterpretations) {
            this.alternativeInterpretations = otherInterpretations;
        }
        /**
         * Adds a new alternative to the list of alternative interpretations.
         * @param alternative another alternative interpretation
         */
        public void addAlternativeInterpretation(InterpretationItem alternative) {
            this.alternativeInterpretations.add(alternative);
        }
        /**
         * Returns the subgoal item.
         * @return the subgoal item
         */
        public SubgoalItem getSubgoalItem() {
            return subgoalItem;
        }
        /**
         * Sets the subgoal item.
         * @param subgoalItem the subgoal item
         */
        public void setSubgoalItem(SubgoalItem subgoalItem) {
            this.subgoalItem = subgoalItem;
        }
    }

}
