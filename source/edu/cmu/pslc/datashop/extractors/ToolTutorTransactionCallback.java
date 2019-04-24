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
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.NonUniqueObjectException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import edu.cmu.pslc.datashop.dao.ActionDao;
import edu.cmu.pslc.datashop.dao.AttemptActionDao;
import edu.cmu.pslc.datashop.dao.AttemptInputDao;
import edu.cmu.pslc.datashop.dao.AttemptSelectionDao;
import edu.cmu.pslc.datashop.dao.CfTxLevelDao;
import edu.cmu.pslc.datashop.dao.CogStepSeqDao;
import edu.cmu.pslc.datashop.dao.CognitiveStepDao;
import edu.cmu.pslc.datashop.dao.ConditionDao;
import edu.cmu.pslc.datashop.dao.CustomFieldDao;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.FeedbackDao;
import edu.cmu.pslc.datashop.dao.InterpretationAttemptDao;
import edu.cmu.pslc.datashop.dao.InterpretationDao;
import edu.cmu.pslc.datashop.dao.MessageDao;
import edu.cmu.pslc.datashop.dao.SelectionDao;
import edu.cmu.pslc.datashop.dao.SkillDao;
import edu.cmu.pslc.datashop.dao.SkillModelDao;
import edu.cmu.pslc.datashop.dao.SubgoalAttemptDao;
import edu.cmu.pslc.datashop.dao.SubgoalDao;
import edu.cmu.pslc.datashop.dao.TransactionDao;
import edu.cmu.pslc.datashop.dto.ItemCache;
import edu.cmu.pslc.datashop.item.ActionItem;
import edu.cmu.pslc.datashop.item.AttemptActionItem;
import edu.cmu.pslc.datashop.item.AttemptInputItem;
import edu.cmu.pslc.datashop.item.AttemptSelectionItem;
import edu.cmu.pslc.datashop.item.CogStepSeqItem;
import edu.cmu.pslc.datashop.item.CognitiveStepItem;
import edu.cmu.pslc.datashop.item.ConditionItem;
import edu.cmu.pslc.datashop.item.CustomFieldItem;
import edu.cmu.pslc.datashop.item.CustomFieldNameValueItem;
import edu.cmu.pslc.datashop.item.CfTxLevelItem;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.FeedbackItem;
import edu.cmu.pslc.datashop.item.InterpretationAttemptItem;
import edu.cmu.pslc.datashop.item.InterpretationItem;
import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.item.MessageItem;
import edu.cmu.pslc.datashop.item.ProblemItem;
import edu.cmu.pslc.datashop.item.SelectionItem;
import edu.cmu.pslc.datashop.item.SkillItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.item.StudentItem;
import edu.cmu.pslc.datashop.item.SubgoalAttemptItem;
import edu.cmu.pslc.datashop.item.SubgoalItem;
import edu.cmu.pslc.datashop.item.TransactionItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.xml.ActionEvaluation;
import edu.cmu.pslc.datashop.xml.ContextMessage;
import edu.cmu.pslc.datashop.xml.EventDescriptor;
import edu.cmu.pslc.datashop.xml.Interpretation;
import edu.cmu.pslc.datashop.xml.SemanticEvent;
import edu.cmu.pslc.datashop.xml.ToolMessage;
import edu.cmu.pslc.datashop.xml.TutorMessage;

/**
 * Handle the transaction for context message.
 *
 * @author Hui Cheng
 * @version $Revision: 13692 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-11-16 09:58:28 -0500 (Wed, 16 Nov 2016) $
 * <!-- $KeyWordsOff: $ -->
 */

public class ToolTutorTransactionCallback implements TransactionCallback {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** Constant. */
    private static final String SUBGOAL_UNKNOWN_NAME = "Unknown";

    /**The contextMessage which has dataset, student, school and etc
     * information that will be used.*/
    private ContextMessage contextMessage;

    /**The toolTutorPair to be saved.*/
    private TutorMessageConverter.ToolTutorPair toolTutorPair;

    /** Item cache to help with number of database hits. */
    private ItemCache itemCache;

    /**The transaction item that should be saved/processed.*/
    private TransactionItem transaction = new TransactionItem();

    /**
     * Constructor.
     * @param context ContextMessage that contains information needed.
     * @param toolTutorPair The TutorMessageConverter.ToolTutorPair that will be saved.
     * @param itemCache the ItemCache for known items already processed/retrieved and
     * attached to the hibernate session.
     */
    public ToolTutorTransactionCallback(ContextMessage context,
            TutorMessageConverter.ToolTutorPair toolTutorPair, ItemCache itemCache) {
        this.contextMessage = context;
        this.toolTutorPair = toolTutorPair;
        this.itemCache = itemCache;
    }

    /**Save update list of messageItem for the processed status.
     * @param messageItems the List of messageItems to be updated.
     * @param status String "DONE" or "FAILED"
     * @throws Exception all exceptions for rollback in caller.
     */
    private void saveMessageItemsProcessStatus (List messageItems, String status) throws Exception {
        MessageDao messageDao = DaoFactory.DEFAULT.getMessageDao();
        for (Iterator iter = messageItems.iterator(); iter.hasNext();) {
            MessageItem messageItem = (MessageItem)iter.next();
            messageItem.setProcessedFlag(status);
            messageItem.setProcessedTime(new Date());
            messageDao.saveOrUpdate(messageItem);

            if (logger.isDebugEnabled()) {
                logger.debug("Saving of tool/tutor message, status: " + status
                        + " user/session/contextMsgId "
                        + messageItem.getUserId() + "/" + messageItem.getSessionTag()
                        + ": " + messageItem.getContextMessageId()
                        + ". MessageId: " + messageItem.getId());
            }
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
     * @param tutorMessage the tutor message
     * @return ProblemItem that is encountered first while traversing through all dataset levels.
     */
    private ProblemItem getProblemFromTutorMessage(TutorMessage tutorMessage) {
        ProblemItem returnVal = tutorMessage.getProblemItem();
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
     * Create a Subgoal from the event descriptor(s) in the tutor message.
     * Also save the selection, action and input items.
     * @param toolMessage the tool message object
     * @param tutorMessage the tutor message object
     * @param problemItem the problem item
     * @return a subgoal.
     * @throws Exception all exceptions for rollback in caller.
     */
    private SubgoalInterps getSubgoal(
            ToolMessage toolMessage,
            TutorMessage tutorMessage,
            ProblemItem problemItem) throws Exception {
        SubgoalInterps subgoalInterps = null;

        if (problemItem == null || problemItem.getId() == null) {
            logger.error("The problem or its id is null.");
            return null;
        }

        SubgoalDao subgoalDao = DaoFactory.DEFAULT.getSubgoalDao();
        SelectionDao selectionDao = DaoFactory.DEFAULT.getSelectionDao();
        ActionDao actionDao = DaoFactory.DEFAULT.getActionDao();

        // re-attach the problem to make sure its loaded properly
        //problemItem = DaoFactory.DEFAULT.getProblemDao().get((Long)problemItem.getId());

        SubgoalItem subgoalItem = new SubgoalItem();
        subgoalItem.setProblem(problemItem);

        // get subgoal name
        String subgoalName = tutorMessage.getSAI();

        // If there is no SAI in the tutor message, then look for interpretations.
        subgoalInterps = processInterpretations(problemItem);
        InterpretationItem interpItem = subgoalInterps.getChosenInterpretation();

        if (interpItem != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Creating subgoal with interpretation");
            }
            subgoalItem.setInterpretation(interpItem);
            subgoalItem.setSubgoalName("hasInterpretation(s)");
            subgoalDao.saveOrUpdate(subgoalItem);
        } else {
            if (logger.isDebugEnabled()) {
               logger.debug("Creating subgoal without interpretation");
            }
            // If no desired SAI and no interpretations...
            if (subgoalName.length() == 0) {
                subgoalName = toolMessage.getSAI();
                // If there is no SAI at all, then let's call this the default step.
                if (subgoalName.length() == 0) {
                    subgoalName = SUBGOAL_UNKNOWN_NAME;
                    if (logger.isDebugEnabled()) {
                        logger.debug("Using '" + SUBGOAL_UNKNOWN_NAME + "' for subgoalName");
                    }
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("toolMessage.getSAI returned "
                                + subgoalName + " for subgoalName");
                    }
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("tutorMessage.getSAI returned "
                            + subgoalName + " for subgoalName");
                }
            }

            subgoalItem.setSubgoalName(subgoalName);

            if (subgoalItem != null
                    && subgoalItem.getSubgoalName() != null
                    && !subgoalItem.getSubgoalName().equals("")) {

                //if the cache has not been initialized for the subgoals and all its
                //items then go ahead and find all the existing ones for the problem.
                if (itemCache.getExistingSubgoals().isEmpty()) {
                    logger.debug("Initializing the subgoal Cache");
                    itemCache.setExistingSubgoals(subgoalDao.find(problemItem));
                    itemCache.setExistingSelections(selectionDao.find(problemItem));
                    itemCache.setExistingActions(actionDao.find(problemItem));
                }

                subgoalItem = (SubgoalItem)subgoalDao.findOrCreate(
                        itemCache.getExistingSubgoals(), subgoalItem);
                itemCache.addSubgoal(subgoalItem);
                problemItem.addSubgoal(subgoalItem);
                subgoalDao.saveOrUpdate(subgoalItem);
            }

            // loop through the event descriptors to get the selections, actions and inputs,
            // then set the subgoal, and then save them if necessary.
            for (Iterator edIter = tutorMessage.getEventDescriptorsExternal().iterator();
                    edIter.hasNext();) {
                EventDescriptor eventDesc = (EventDescriptor)edIter.next();

                // get selections
                List selList = eventDesc.getSelectionsExternal();
                for (Iterator selIter = selList.iterator(); selIter.hasNext();) {
                    SelectionItem selItem = (SelectionItem)selIter.next();
                    selItem.setSubgoal(subgoalItem);
                    selItem = (SelectionItem)selectionDao.findOrCreate(
                            itemCache.getExistingSelections(), selItem);
                    itemCache.addSelection(selItem);
                }

                // get actions
                List actList = eventDesc.getActionsExternal();
                for (Iterator actIter = actList.iterator(); actIter.hasNext();) {
                    ActionItem actItem = (ActionItem)actIter.next();
                    actItem.setSubgoal(subgoalItem);
                    actItem = (ActionItem)selectionDao.findOrCreate(
                            itemCache.getExistingActions(), actItem);
                    itemCache.addAction(actItem);
                }

                //note: inputs are not part of the subgoal
            }
        }

        subgoalInterps.setSubgoalItem(subgoalItem);

        return subgoalInterps;
    }

    /**
     * Create a SubgoalAttempt from the event descriptor(s) in the tool message.
     * Also save the attemptSelection, attemptAction and attemptInput items.
     * @param toolMessage the tool message object
     * @param tutorMessage the tutor message to get the action evaluation from.
     * @param subgoalItem the subgoal item
     * @param problemItem the problem item
     * @return a subgoalAttempt
     * @throws Exception all exceptions for rollback in caller.
     */
    private SubgoalAttemptItem getSubgoalAttempt(ToolMessage toolMessage,
                        TutorMessage tutorMessage,
                        SubgoalItem subgoalItem,
                        ProblemItem problemItem) throws Exception {
        SubgoalAttemptItem subgoalAttemptItem = null;

        if (subgoalItem == null || subgoalItem.getId() == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Can't set subgoal attempt due to null subgoal. Context message Id:"
                        + toolMessage.getContextId());
            }
            return subgoalAttemptItem;
        }

        // get the attempt's unique name (within its subgoal) from the SAI
        /*
        String attemptName = toolMessage.getSAI();
        if (logger.isDebugEnabled()) {
            logger.debug("toolMessage.getSAI returned " + attemptName + " for attemptName");
        }*/

        //get correct_flag for saving subgoal
        //for now we will just save the first action evaluation.
        //TODO Need to handle multiple actionEvaluations.
        List correctFlags = tutorMessage.getCorrectFlags();
        String correctFlag = null;
        if (correctFlags.size() == 0) {
            correctFlag = SubgoalAttemptItem.CORRECT_FLAG_UNKNOWN;
            if (logger.isDebugEnabled()) {
                logger.debug("Use unknown correct flag as none was given. Context message Id: "
                        + toolMessage.getContextId());
            }
        } else {
            correctFlag = (String)correctFlags.get(0);
        }

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
        newAttempt.setSubgoal(subgoalItem);
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
            subgoalItem.addSubgoalAttempt(subgoalAttemptItem);

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
            if (!currentAttempt.getSubgoal().getId().equals(newAttempt.getSubgoal().getId())
                    || !currentAttempt.getCorrectFlag().equals(newAttempt.getCorrectFlag())) {
                isSameAttempt = false;
                continue;
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

    /**
     * Create a feedback item from the action evaluation(s) in the tutor message.
     * @param subgoalAttemptItem the subgoalAttemptItem that will be used by the feedback
     * @param problemItem the problem item this feedback will fall under.
     * @param tutorMessage the tutor message that tutorAdvice is located.
     * @return FeeedbackItem
     * @throws Exception all exceptions for rollback in caller.
     */
    private FeedbackItem getFeedbackItem(SubgoalAttemptItem subgoalAttemptItem,
                                    ProblemItem problemItem,
                                    TutorMessage tutorMessage) throws Exception {
        FeedbackItem feedbackItem = null;

        if (subgoalAttemptItem == null || subgoalAttemptItem.getId() == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Can't get feedback due to null subgoal attempt. Context message Id:"
                        + tutorMessage.getContextId());
            }
            return feedbackItem;
        }

        List tutorAdvice = tutorMessage.getTutorAdvicesExternal();
        if (tutorAdvice.size() == 0) {
            return feedbackItem;
        }

        feedbackItem = new FeedbackItem();
        feedbackItem.setSubgoalAttempt(subgoalAttemptItem);

        // get tutor advice for saving feedback text and save a new feedback for each tutor
        // advice, but only return the first in the list (it is implicitly assumed that if more
        // than one feedback text appear we are getting the stack off all possible, but that
        // the first was the only one we saw).
        FeedbackItem firstFeedback = null;
        for (Iterator taIter = tutorAdvice.listIterator(); taIter.hasNext();) {
            feedbackItem = new FeedbackItem();
            feedbackItem.setSubgoalAttempt(subgoalAttemptItem);
            feedbackItem.setFeedbackText(((String)taIter.next()).replaceAll("[\r\n\t]+", " "));
            //get classification for feedback
            String classification = null;
            String hintId = null;
            for (Iterator cIter = tutorMessage.getActionEvaluationsExternal().iterator();
                                    cIter.hasNext();) {
                ActionEvaluation ae = (ActionEvaluation)cIter.next();
                classification = ae.getClassification();
                hintId = ae.getHintId();
                break;
            }
            feedbackItem.setClassification(classification);

            if (hintId != null && hintId.length() > Item.TINY_TEXT_LENGTH) {
                logger.warn("Truncating feedback template tag for context message: "
                        + tutorMessage.getContextId() + " for value[" + hintId + "]");
                hintId = hintId.substring(0, Item.TINY_TEXT_LENGTH);
            }
            feedbackItem.setTemplateTag(hintId);

            FeedbackDao feedbackDao = DaoFactory.DEFAULT.getFeedbackDao();
            if (itemCache.getExistingFeedbacks().isEmpty()) {
                itemCache.setExistingFeedbacks(feedbackDao.find(problemItem));
            }

            feedbackItem = (FeedbackItem)feedbackDao.findOrCreate(
                    itemCache.getExistingFeedbacks(), feedbackItem);
            subgoalAttemptItem.addFeedback(feedbackItem);
            itemCache.addFeedback(feedbackItem);

            if (firstFeedback == null) { firstFeedback = feedbackItem; }
            //For the moment only save the first one as their is no way to store
            //unseen feedbacks in the database.
            break;
        }
        return firstFeedback;
    }

    /**Set the sessionId, time, time zone for the transaction
     * based on the first element of the toolMsgItemList or the tutorMsgItemList.
     * */
    private void setTxnFieldsByMsgItem() {
        List toolMsgItemList = toolTutorPair.getToolMessageItemList();
        List tutorMsgItemList = toolTutorPair.getTutorMessageItemList();
        MessageItem firstMsgItem = null;
        //These fields should be the same for all objects in the tool and tutor lists
        //so just get the first one.
        if (toolMsgItemList.size() > 0) {
            firstMsgItem = (MessageItem)toolMsgItemList.get(0);
        } else if (tutorMsgItemList.size() > 0) {
            firstMsgItem = (MessageItem)tutorMsgItemList.get(0);
        }
        if (firstMsgItem != null) {
            transaction.setSession(contextMessage.getSessionItem());
            transaction.setTimeZone(firstMsgItem.getTimeZone());
            Date msgItemDate = firstMsgItem.getTime();

            GregorianCalendar msgItemCal = new GregorianCalendar();
            msgItemCal.setTime(msgItemDate);
            GregorianCalendar now = new GregorianCalendar();
            int year = msgItemCal.get(Calendar.YEAR);
            if ((year > now.get(Calendar.YEAR) + VALID_YEAR_FUTURE_OFFSET)
             || (year < now.get(Calendar.YEAR) - VALID_YEAR_PAST_OFFSET)) {
                Date serverReceiptDate = firstMsgItem.getServerReceiptTime();
                GregorianCalendar serverReceiptCal = new GregorianCalendar();
                if (serverReceiptDate != null) {
                    serverReceiptCal.setTime(serverReceiptDate);
                }
                int newYear = serverReceiptCal.get(Calendar.YEAR);

                msgItemCal.set(Calendar.YEAR, newYear);

                logger.warn("Invalid year (" + year + ") found in GUID "
                        + firstMsgItem.getGuid() + " resetting year to " + newYear);
            }

            transaction.setTransactionTime(msgItemCal.getTime());
        }

    }

    /** The number of years in the future beyond which the year in the time stamp must be bogus. */
    private static final int VALID_YEAR_FUTURE_OFFSET = 20;

    /** The number of years in the past beyond which the year in the time stamp must be bogus. */
    private static final int VALID_YEAR_PAST_OFFSET = 50;

    /**Set help_level, total_hint, and outcome for the transaction
     * based on the first action_evaluation.
     * */
    private void setTxnFieldsByActionEval() {
        TutorMessage tutorMessage = toolTutorPair.getTutorMessageItem();
        Set evaluations = tutorMessage.getActionEvaluationsExternal();
        for (Iterator iter = evaluations.iterator(); iter.hasNext();) {
            ActionEvaluation evaluation = (ActionEvaluation)iter.next();
            transaction.setHelpLevel(evaluation.getCurrentHintNumber());
            transaction.setTotalNumHints(evaluation.getTotalHintsAvailable());
            transaction.setOutcome(evaluation.getContent());
            //only use the first action_evaluation, so break now;
            break;
        }
    }

    /**Set transaction_type/subtype_tutor/tool based on semantic_event
     * */
    private void setTxnFieldsBySemantic() {
        TutorMessage tutorMessage = toolTutorPair.getTutorMessageItem();
        ToolMessage toolMessage = toolTutorPair.getToolMessageItem();
        List tutorSemantic = tutorMessage.getSemanticEventsExternal();
        List toolSemantic = toolMessage.getSemanticEventsExternal();
        for (Iterator tutorIter = tutorSemantic.iterator(); tutorIter.hasNext();) {
            SemanticEvent event = (SemanticEvent)tutorIter.next();
            transaction.setTransactionSubtypeTutor(event.getSubtype());
            transaction.setTransactionTypeTutor(event.getName());
            //only use the first action_evaluation, so break now;
            break;
        }
        for (Iterator toolIter = toolSemantic.iterator(); toolIter.hasNext();) {
            SemanticEvent event = (SemanticEvent)toolIter.next();
            transaction.setTransactionSubtypeTool(event.getSubtype());
            transaction.setTransactionTypeTool(event.getName());
            //only use the first action_evaluation, so break now;
            break;
        }
    }

    /**Process skill, skill_model
     * @throws Exception all exceptions for rollback in caller.
     * */
    private void processSkills () throws Exception {
        TutorMessage tutorMessage = toolTutorPair.getTutorMessageItem();
        Set skills = tutorMessage.getSkillsExternal();

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
            transaction.getSubgoal().addSkillFast(skillItem);

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
        TutorMessage tutorMessage = toolTutorPair.getTutorMessageItem();
        ToolMessage toolMessage = toolTutorPair.getToolMessageItem();
        List tutorCustom = tutorMessage.getCustomFieldsExternal();
        List toolCustom = toolMessage.getCustomFieldsExternal();
        List contextCustom = contextMessage.getCustomFieldsExternal();
        if (tutorCustom.isEmpty()
                && toolCustom.isEmpty()
                && contextCustom.isEmpty()) {
            return;
        }
        String contextMessageId = contextMessage.getContextId();
        CustomFieldDao customFieldDao = DaoFactory.DEFAULT.getCustomFieldDao();
        CfTxLevelDao cfTxLevelDao = DaoFactory.DEFAULT.getCfTxLevelDao();

        if (itemCache.getExistingCustomFields().isEmpty()) {
            itemCache.setExistingCustomFields(customFieldDao.find(datasetItem));
        }

        //loop through all tutor custom fields, skip any custom fields which have invalid XML
        for (Iterator tutorIter = tutorCustom.iterator(); tutorIter.hasNext();) {
            CustomFieldNameValueItem tutorItem = (CustomFieldNameValueItem)tutorIter.next();
            if (tutorItem.getName() == null
             || tutorItem.getName().length() <= 0) {
                logger.warn("Custom field name is null or zero length for context message: "
                        + contextMessageId);
                continue;
            }
            if (tutorItem.getValue() == null
             || tutorItem.getValue().length() <= 0) {
                logger.warn("Custom field value is null or zero length for context message: "
                        + contextMessageId);
                continue;
            }
            //set custom field value
            String customFieldValue = tutorItem.getBigValue() != null
                    ? tutorItem.getBigValue() : tutorItem.getValue();

            tutorItem.setValue(customFieldValue);
            CustomFieldItem customFieldItemTutor = tutorItem.makeACustomFieldItem();
            customFieldItemTutor.setDataset(transaction.getDataset());
            customFieldItemTutor.setLevel(CustomFieldItem.CF_LEVEL_TRANSACTION);
            customFieldItemTutor.setOwner(new UserItem(UserItem.SYSTEM_USER));
            customFieldItemTutor = (CustomFieldItem)customFieldDao.findOrCreate(
                    itemCache.getExistingCustomFields(), customFieldItemTutor);
            //process cf_tx_level
            try {
                CfTxLevelItem cfTxLevelItemTutor =
                tutorItem.makeACfTxLevelItem(customFieldItemTutor, transaction);
                cfTxLevelItemTutor.setLoggingFlag(true);

                cfTxLevelDao.saveOrUpdate(cfTxLevelItemTutor);

            } catch (NonUniqueObjectException e) {
                // #295: first one in wins...
                if (e.getCause() instanceof NonUniqueObjectException) {
                    logger.debug("Value for [custom field, transaction] pair already exists: ["
                                + customFieldItemTutor.getId() + ", " + transaction.getId() + "]");
                } else {
                    throw e;
                }
            }

            itemCache.addCustomField(customFieldItemTutor);
        }

        //loop through all tool custom fields, skip any custom fields which have invalid XML
        for (Iterator toolIter = toolCustom.iterator(); toolIter.hasNext();) {
        CustomFieldNameValueItem toolItem = (CustomFieldNameValueItem)toolIter.next();
            if (toolItem.getName() == null
             || toolItem.getName().length() <= 0) {
                logger.warn("Custom field name is null or zero length for context message: "
                        + contextMessageId);
                continue;
            }
            if (toolItem.getValue() == null
             || toolItem.getValue().length() <= 0) {
                logger.warn("Custom field value is null or zero length for context message: "
                        + contextMessageId);
                continue;
            }
            //set custom field value
            String customFieldValue = toolItem.getBigValue() != null
                    ? toolItem.getBigValue() : toolItem.getValue();
            toolItem.setValue(customFieldValue);
            CustomFieldItem customFieldItemTool = toolItem.makeACustomFieldItem();
            customFieldItemTool.setDataset(transaction.getDataset());
            customFieldItemTool.setLevel(CustomFieldItem.CF_LEVEL_TRANSACTION);
            customFieldItemTool.setOwner(new UserItem(UserItem.SYSTEM_USER));
            customFieldItemTool = (CustomFieldItem)customFieldDao.findOrCreate(
                    itemCache.getExistingCustomFields(), customFieldItemTool);
            //process cf_tx_level
            try {
                CfTxLevelItem cfTxLevelItemTool =
                toolItem.makeACfTxLevelItem(customFieldItemTool, transaction);
                cfTxLevelItemTool.setLoggingFlag(true);
                cfTxLevelDao.saveOrUpdate(cfTxLevelItemTool);

            } catch (Exception e) {
                // #295: first one in wins...
                if (e.getCause() instanceof NonUniqueObjectException) {
                    logger.debug("Value for [custom field, transaction] pair already exists: ["
                                + customFieldItemTool.getId() + ", " + transaction.getId() + "]");
                } else {
                    throw e;
                }
            }

        itemCache.addCustomField(customFieldItemTool);
        }

        //loop through all context custom fields, skip any custom fields which have invalid XML
        for (Iterator iter = contextCustom.iterator(); iter.hasNext();) {
                CustomFieldNameValueItem contextItem = (CustomFieldNameValueItem)iter.next();
                if (contextItem.getName() == null
                 || contextItem.getName().length() <= 0) {
                    logger.warn("Custom field name is null or zero length for context message: "
                            + contextMessageId);
                    continue;
                }
                if (contextItem.getValue() == null
                 || contextItem.getValue().length() <= 0) {
                    logger.warn("Custom field value is null or zero length for context message: "
                            + contextMessageId);
                    continue;
                }
                //set custom field value
                String customFieldValue = contextItem.getBigValue() != null
                        ? contextItem.getBigValue() : contextItem.getValue();
                contextItem.setValue(customFieldValue);
                CustomFieldItem customFieldItemContext = contextItem.makeACustomFieldItem();
                customFieldItemContext.setDataset(transaction.getDataset());
                customFieldItemContext.setLevel(CustomFieldItem.CF_LEVEL_TRANSACTION);
                customFieldItemContext.setOwner(new UserItem(UserItem.SYSTEM_USER));
                customFieldItemContext = (CustomFieldItem)customFieldDao.findOrCreate(
                        itemCache.getExistingCustomFields(), customFieldItemContext);
                //process cf_tx_level
                try {
                    CfTxLevelItem cfTxLevelItemContext =
                        contextItem.makeACfTxLevelItem(customFieldItemContext, transaction);
                    cfTxLevelItemContext.setLoggingFlag(true);

                    cfTxLevelDao.saveOrUpdate(cfTxLevelItemContext);

                } catch (NonUniqueObjectException e) {
                    // #295: first one in wins...
                    if (e.getCause() instanceof NonUniqueObjectException) {
                        logger.debug("Value for [custom field, transaction] pair already exists: ["
                                    + customFieldItemContext.getId() + ", "
                                    + transaction.getId() + "]");
                    } else {
                        throw e;
                    }
                }

                itemCache.addCustomField(customFieldItemContext);
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

        //loop through all the conditions in the message
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
     * Process interpretations and return the chosen interpretation.
     * @param problemItem the problem these interpretations are associated with
     * @throws Exception all exceptions for rollback in caller
     * @return an interpretation item
     */
    private SubgoalInterps processInterpretations(ProblemItem problemItem) throws Exception {
        SubgoalInterps subgoalInterps = new SubgoalInterps();
        InterpretationItem interpItem = null;
        logger.debug("processInterpretations: begin");

        InterpretationDao interpretationDao = DaoFactory.DEFAULT.getInterpretationDao();
        CognitiveStepDao cogStepDao = DaoFactory.DEFAULT.getCognitiveStepDao();

        TutorMessage tutorMessage = toolTutorPair.getTutorMessageItem();

        //The Interpretation objects, note that these are different from interpretationItem.
        List interpretations = tutorMessage.getInterpretationsExternal();

        if (logger.isDebugEnabled()) {
            logger.debug("processInterpretations: # interpretations: " + interpretations.size());
        }

        //loop through interpretation
        for (Iterator iter = interpretations.iterator(); iter.hasNext();) {
            Interpretation interpretation = (Interpretation)iter.next();

            List corInterpSteps = interpretation.getCorrectSteps();
            List incInterpSteps = interpretation.getIncorrectSteps();

            List corActualSteps = cogStepDao.find(problemItem, corInterpSteps);
            List incActualSteps = cogStepDao.find(problemItem, incInterpSteps);

            // check if all the steps exist
            if (corActualSteps.size() == corInterpSteps.size()
                && incActualSteps.size() == incInterpSteps.size()) {

                if (logger.isDebugEnabled()) {
                    logger.debug("All the steps are there");
                }

                if (corActualSteps.size() > 0 && incActualSteps.size() == 0) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Correct Steps Only: " + corActualSteps);
                    }
                    interpItem = interpretationDao.find(corActualSteps, Boolean.TRUE);

                    //if steps exist but an interpretation doesn't, then create one
                    if (interpItem == null) {
                        interpItem = new InterpretationItem();
                        interpretationDao.saveOrUpdate(interpItem);
                        proccessCognitiveSteps(problemItem,
                            interpItem, Boolean.TRUE, corInterpSteps, corActualSteps);
                        if (logger.isDebugEnabled()) {
                            logger.debug("Created an interpretation:" + interpItem);
                        }
                    }
                } else if (corActualSteps.size() == 0 && incActualSteps.size() > 0) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Incorrect Steps Only: " + incActualSteps);
                    }
                    interpItem = interpretationDao.find(incActualSteps, Boolean.FALSE);

                    //if steps exist but an interpretation doesn't, then create one
                    if (interpItem == null) {
                        interpItem = new InterpretationItem();
                        interpretationDao.saveOrUpdate(interpItem);
                        proccessCognitiveSteps(problemItem,
                            interpItem, Boolean.FALSE, incInterpSteps, incActualSteps);
                        if (logger.isDebugEnabled()) {
                            logger.debug("Created an interpretation:" + interpItem);
                        }
                    }
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Correct and Incorrect steps.");
                    }
                    interpItem = interpretationDao.find(corActualSteps, incActualSteps);

                    //if steps exist but an interpretation doesn't, then create one
                    if (interpItem == null) {
                        interpItem = new InterpretationItem();
                        interpretationDao.saveOrUpdate(interpItem);

                        proccessCognitiveSteps(problemItem,
                                interpItem, Boolean.TRUE, corInterpSteps, corActualSteps);
                        proccessCognitiveSteps(problemItem,
                                interpItem, Boolean.FALSE, incInterpSteps, incActualSteps);
                        if (logger.isDebugEnabled()) {
                            logger.debug("Created an interpretation:" + interpItem);
                        }
                    }
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("InterpretationItem: " + interpItem);
                }

            // all the steps did not exist, create the missing steps, and create the CSS
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("All the steps are NOT there");
                }

                interpItem = new InterpretationItem();
                interpretationDao.saveOrUpdate(interpItem);

                proccessCognitiveSteps(problemItem,
                        interpItem, Boolean.TRUE, corInterpSteps, corActualSteps);
                proccessCognitiveSteps(problemItem,
                        interpItem, Boolean.FALSE, incInterpSteps, incActualSteps);

            } // end else

            if (interpretation.getChosen().booleanValue()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Chosen interpretation found.");
                }
                subgoalInterps.setChosenInterpretation(interpItem);
            } else if (interpItem != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Alternative interpretation found.");
                }
                subgoalInterps.addAlternativeInterpretation(interpItem);
            }
        }
        logger.debug("processInterpretations: end");
        return subgoalInterps;
    }

    /**
     * Create the missing steps, then create the sequences accordingly.
     * @param problemItem the problem
     * @param interpretationItem the new interpretation
     * @param correctFlag indicates whether the sequence is correct or incorrect
     * @param interpStepList the list of steps from the XML
     * @param actualStepList the list of steps which already exist
     * @throws Exception all exceptions for the rollback in the caller.
     */
    private void proccessCognitiveSteps(ProblemItem problemItem,
            InterpretationItem interpretationItem,
            Boolean correctFlag,
            Collection interpStepList, Collection actualStepList) throws Exception {
        List finalStepList = new ArrayList();

        CogStepSeqDao cssDao = DaoFactory.DEFAULT.getCogStepSeqDao();
        CognitiveStepDao cogStepDao = DaoFactory.DEFAULT.getCognitiveStepDao();
        SkillDao skillDao = DaoFactory.DEFAULT.getSkillDao();
        SkillModelDao skillModelDao = DaoFactory.DEFAULT.getSkillModelDao();
        DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();

        DatasetItem datasetItem = transaction.getDataset();
        if (datasetItem != null && datasetItem.getId() != null) {
            datasetItem = datasetDao.get((Integer)datasetItem.getId());
        }

        if (itemCache.getExistingCognitiveSteps().isEmpty()) {
            itemCache.setExistingCognitiveSteps(
                    cogStepDao.find(problemItem));
            if (logger.isDebugEnabled()) {
                logger.debug("Initialize CogSteps Cache with "
                        + itemCache.getExistingCognitiveSteps().size() + " items");
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("interpStepList : " + interpStepList.size());
        }

        for (Iterator interpStepIter = interpStepList.iterator(); interpStepIter.hasNext();) {
            CognitiveStepItem stepItem = (CognitiveStepItem)interpStepIter.next();
            CognitiveStepItem actualItem = null;
            boolean found = false;

            for (Iterator actualIter = actualStepList.iterator(); actualIter.hasNext();) {
                actualItem = (CognitiveStepItem)actualIter.next();
                if (actualItem.getStepInfo().equals(stepItem.getStepInfo())) {
                    found = true;
                    if (logger.isDebugEnabled()) {
                        logger.debug("Step found : " + actualItem);
                    }
                    break;
                }
            } // end for loop on actual steps (inner)

            Collection skills = stepItem.getSkillsExternal();
            CognitiveStepItem itemToSave;
            if (!found) {
                if (logger.isDebugEnabled()) {
                    logger.debug("No step was found, creating step: " + stepItem.getStepInfo());
                }
                itemToSave = new CognitiveStepItem();
                itemToSave.setStepInfo(stepItem.getStepInfo());
                itemToSave.setProblem(problemItem);
            } else {
                itemToSave = actualItem;
            }


            //populate all the skill models in this dataset that already exist in the DB.
            if (itemCache.getExistingModels().isEmpty()) {
                itemCache.setExistingModels(skillModelDao.find(datasetItem));
                if (logger.isDebugEnabled()) {
                    logger.debug("Initialize Skill Model Cache with size : "
                            + itemCache.getExistingModels().size());
                }
            }

            //populate the skills from the db for all existing skills for each skill model.
            if (itemCache.getExistingSkills().isEmpty()) {
                for (Iterator it = itemCache.getExistingModels().iterator(); it.hasNext();) {
                    itemCache.setExistingSkills(skillDao.find((SkillModelItem)it.next()));
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Initialize Skill Cache : "
                            + itemCache.getExistingSkills().size());
                }
            }

            for (Iterator iter = skills.iterator(); iter.hasNext();) {

                SkillItem skillItem = (SkillItem)iter.next();
                SkillModelItem skillModelItem = skillItem.getSkillModel();
                skillModelItem.setDataset(datasetItem);

                //see if the skillModelItem already exists in the list of existing skills,
                //create it if it doesn't and add it to the list of existing skills models.
                skillModelItem = (SkillModelItem)skillModelDao.
                                    findOrCreate(itemCache.getExistingModels(), skillModelItem);

                if (!itemCache.getExistingModels().contains(skillModelItem)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Adding Model to cache : " + skillModelItem);
                    }
                    itemCache.addModel(skillModelItem);
                }

                skillItem.setSkillModel(skillModelItem);
                skillItem = (SkillItem)skillDao.findOrCreate(
                        itemCache.getExistingSkills(), skillItem);

                //Note: this is a unidirectional association
                transaction.addSkill(skillItem);
                itemToSave.addSkill(skillItem);

                itemCache.addSkill(skillItem);
                if (logger.isDebugEnabled()) {
                    logger.debug("Adding Skill to cache : " + skillItem);
                }
            }

            if (itemToSave == null) {
                logger.error("The Step Item is null");
            }

            itemToSave = (CognitiveStepItem)cogStepDao.findOrCreate(
                    itemCache.getExistingCognitiveSteps(), itemToSave);

            itemCache.addCognitiveStep(itemToSave);
            finalStepList.add(itemToSave);
        } // end for loop on interpretation's steps (outer)

        logger.debug("interpStepList done");


        for (Iterator finalIter = finalStepList.iterator(); finalIter.hasNext();) {
           CognitiveStepItem stepItem = (CognitiveStepItem)finalIter.next();

           CogStepSeqItem cssItem = new CogStepSeqItem();
           cssItem.setCognitiveStep(stepItem);
           cssItem.setInterpretation(interpretationItem);
           cssItem.setCorrectFlag(correctFlag);

           cssDao.saveOrUpdate(cssItem);
           logger.debug("Saved new CogStepSeqItem : " + cssItem);
       }
    }

    /**
     * Gets the attempt at subgoal.
     * @param studentItem the student item
     * @param subgoalItem the subgoal item
     * @return the new attempt number
     * @throws Exception for caller to catch
     */
    private Integer getAttemptAtSubgoal(StudentItem studentItem, SubgoalItem subgoalItem)
            throws Exception {
        Integer theAttempt = itemCache.getAttemptNumber(subgoalItem);

        if (theAttempt == null) {
            TransactionDao transactionDao = DaoFactory.DEFAULT.getTransactionDao();
            theAttempt = transactionDao.getNextAttemptAtSubgoal(studentItem, subgoalItem);
        } else {
            theAttempt = Integer.valueOf(theAttempt.intValue() + 1);
        }
        itemCache.setAttemptNumber(subgoalItem, theAttempt);
        return theAttempt;
    }

    /**
     * Implement the interface.
     * Put all actions in this for transaction management.
     * @param ts TransactionStatus.
     * @return true if successful, false otherwise
     */
    public Object doInTransaction(TransactionStatus ts) {

        //get lists of messageItems that need to be updated for the process status.
        List toolMsgItemList = toolTutorPair.getToolMessageItemList();
        List tutorMsgItemList = toolTutorPair.getTutorMessageItemList();
        TutorMessage tutorMessage = toolTutorPair.getTutorMessageItem();
        ToolMessage toolMessage = toolTutorPair.getToolMessageItem();

        Date startTime = null;
        if (logger.isDebugEnabled()) { startTime = new Date(); }

        try {
            //get the problem from the context message
            ProblemItem problemItem = getProblemFromContextMessage();
            if (problemItem == null) {
                // look in tutor message
                problemItem = getProblemFromTutorMessage(tutorMessage);
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

            //set the problem on the transaction.
            transaction.setProblem(problemItem);

            //get the subgoal from the tutor message and connect to this problem
            SubgoalInterps subgoalInterps = getSubgoal(toolMessage, tutorMessage, problemItem);
            SubgoalItem subgoalItem = subgoalInterps.getSubgoalItem();
            startTime = TutorMessageConverter.logTime(logger, "getSubgoal: ", startTime);

            //get the subgoal attempt message including connecting to the subgoal and
            //set the attempt selection, input and action.
            //TODO because there may be multiple action evaluations,
            //we may need to save multiple subgoal attempt.
            SubgoalAttemptItem subgoalAttemptItem
                = getSubgoalAttempt(toolMessage, tutorMessage, subgoalItem, problemItem);
            startTime = TutorMessageConverter.logTime(logger, "getSubgoalAttempt: ", startTime);

            InterpretationAttemptDao iaDao = DaoFactory.DEFAULT.getInterpretationAttemptDao();
            //add the interpretations to the subgoal attempt
            if (subgoalInterps.getChosenInterpretation() != null) {
                InterpretationAttemptItem iaItem = new InterpretationAttemptItem();
                iaItem.setSubgoalAttemptExternal(subgoalAttemptItem);
                iaItem.setChosenFlag(Boolean.TRUE);
                iaItem.setInterpretationExternal(subgoalInterps.getChosenInterpretation());
                iaDao.saveOrUpdate(iaItem);
                subgoalAttemptItem.addInterpretationAttempt(iaItem);
            }
            for (Iterator iter = subgoalInterps.getAlternativeInterpretations().iterator();
                     iter.hasNext();) {
                InterpretationItem interpItem = (InterpretationItem)iter.next();
                InterpretationAttemptItem iaItem = new InterpretationAttemptItem();
                iaItem.setSubgoalAttemptExternal(subgoalAttemptItem);
                iaItem.setChosenFlag(Boolean.FALSE);
                iaItem.setInterpretationExternal(interpItem);
                iaDao.saveOrUpdate(iaItem);
                subgoalAttemptItem.addInterpretationAttempt(iaItem);
            }

            //save feedback with the subgoalAttemptItem.
            //TODO there maybe multiple tutor_advice but we will just save the first one,
            //use the classification of the first actionEvaluations
            //and save it for the transaction.
            FeedbackItem feedbackItem = getFeedbackItem(
                    subgoalAttemptItem, problemItem, tutorMessage);
                startTime = TutorMessageConverter.logTime(logger, "getFeedback: ", startTime);

            //set all fields for transaction and save
            transaction.setSubgoalAttempt(subgoalAttemptItem);
            transaction.setSubgoal(subgoalItem);
            if (feedbackItem != null) { transaction.setFeedback(feedbackItem); }
            startTime = TutorMessageConverter.logTime(logger, "setFeedback: ", startTime);

            if (contextMessage.getSchoolItem() != null) {
                transaction.setSchool(contextMessage.getSchoolItem());
            }
            startTime = TutorMessageConverter.logTime(logger, "setSchoolItem: ", startTime);

            if (contextMessage.getClassItem() != null) {
                transaction.setClassItem(contextMessage.getClassItem());
            }
            startTime = TutorMessageConverter.logTime(logger, "setClassItem: ", startTime);

            //Use the first action evaluation to set outcome, help_level, and total hints
            //TODO we only use the first action_evaluation. Need to handle multiple.
            setTxnFieldsByActionEval();
            startTime = TutorMessageConverter.logTime(logger,
                        "setTxnFieldsByActionEval: ", startTime);

            //Use semantic_event for transaction_type/subtype_tutor/tool
            //TODO we only use the first semantic_event. need to handle multiple.
            setTxnFieldsBySemantic();
            startTime = TutorMessageConverter.logTime(logger,
                        "setTxnFieldsBySemantic: ", startTime);

            //Use messageItem to transaction set sessionId, time zone, time
            setTxnFieldsByMsgItem();
            startTime = TutorMessageConverter.logTime(logger,
                    "setTxnFieldsByMsgItem in : ", startTime);

            //DO NOT set the attempt at subgoal here, its shoot-me-now slow
            //set the attempt at subgoal
            //transaction.setAttemptAtSubgoal(getAttemptAtSubgoal(contextMessage.getStudentItem(),
            //                                transaction.getSubgoal()));
            //startTime = TutorMessageConverter.logTime(logger,
            //                "Set attempt at subgoal in: ", startTime);

            TransactionDao transactionDao = DaoFactory.DEFAULT.getTransactionDao();
            //set transaction guid
            transaction.setGuid(transactionDao.generateGUID());
            transactionDao.saveOrUpdate(transaction);
            startTime = TutorMessageConverter.logTime(logger, "Saved transaction in : ", startTime);

            processSkills();
            startTime = TutorMessageConverter.logTime(logger, "Processed skills in : ", startTime);

            processConditions();
            startTime = TutorMessageConverter.logTime(logger,
                    "Processed conditions in: ", startTime);

            transactionDao.saveOrUpdate(transaction);

            //anything requiring the transaction id comes after the transaction save.
            processCustomFields(datasetItem, problemItem, contextMessage.getStudentItem());
            startTime = TutorMessageConverter.logTime(logger,
                    "Processed custom fields in: ", startTime);

            //update the processed flag for the associated messageItems
            saveMessageItemsProcessStatus(toolMsgItemList, MessageItem.SUCCESS_FLAG);
            saveMessageItemsProcessStatus(tutorMsgItemList, MessageItem.SUCCESS_FLAG);
            startTime = TutorMessageConverter.logTime(logger,
                        "Saved process status in: ", startTime);
        } catch (Throwable exception) {

            ts.setRollbackOnly();
            String guid = "unknown";
            String userId = "unknown";
            String sessionId = "unknown";
            String contextMsgId = "unknown";
            if (toolMsgItemList.size() > 0) {
                MessageItem messageItem = (MessageItem)toolMsgItemList.get(0);
                guid = messageItem.getGuid();
                userId = messageItem.getUserId();
                sessionId = messageItem.getSessionTag();
                contextMsgId = messageItem.getContextMessageId();
            }

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
