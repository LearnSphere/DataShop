/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2008
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.SetDao;
import edu.cmu.pslc.datashop.dao.SkillDao;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.item.SetItem;
import edu.cmu.pslc.datashop.item.SkillItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.helper.UserLogger;

/**
 * This class contains the business tier logic for manipulating and creating Skill Sets.
 *
 * @author Alida Skogsholm
 * @version $Revision: 4765 $
 * <BR>Last modified by: $Author: bkb $
 * <BR>Last modified on: $Date: 2008-05-01 10:52:19 -0400 (Thu, 01 May 2008) $
 * <!-- $KeyWordsOff: $ -->
 */
public class SetHelper {

    /** Debug logger. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** User action constant. */
    private static final String USER_ACTION_LOADED   = "KC Set Loaded";
    /** User action constant. */
    private static final String USER_ACTION_CREATE   = "KC Set Created";
    /** User action constant. */
    private static final String USER_ACTION_MODIFIED = "KC Set Modified";
    /** User action constant. */
    private static final String USER_ACTION_DELETED  = "KC Set Deleted";
    /** User action constant. */
    private static final String USER_ACTION_RENAMED  = "KC Set Renamed";
    /** Constant for string indicating that the set is loaded. */
    private static final String LOADED = "(loaded)";
    /** Constant for string indicating that the set is loaded and modified. */
    private static final String LOADED_MODIFIED = "(loaded, modified)";

    /** Default Constructor. */
    public SetHelper() {
        logger.debug("constructor");
    }

    /**
     * Get the skill set with the given name for the given KM.
     * @param skillModelItem the selected skill model
     * @param setName the name of the set to get
     * @return the SetItem
     */
    SetItem getSkillSet(SkillModelItem skillModelItem, String setName) {
        if (logger.isDebugEnabled()) {
            logger.debug("getSet: " + setName
                    + " for skill model " + skillModelItem.getSkillModelName());
        }

        SetDao setDao = DaoFactory.DEFAULT.getSetDao();

        SetItem setItem = null;
        List setItems = setDao.find(skillModelItem, setName);
        if (setItems.size() > 0) {
            setItem = (SetItem)setItems.get(0);
        }
        if (setItems.size() > 1) {
            logger.warn("There are two sets with the same name, type and skill model id: "
                + setName + ", " + SetItem.SET_TYPE_SKILL + ", "
                + skillModelItem.getSkillModelName() + "(" + skillModelItem.getId() + ")");
        }

        return setItem;
    }

    /**
     * Builds the a JSON Object with the list of sets for the given KCM.
     * @param userItem the current user
     * @param skillModelItem the given KCM
     * @param loadedSetName the loaded set name, null if none is currently selected/loaded
     * @param isModified flag indicating whether the currently loaded set is modified
     * @return a JSONObject.
     * @throws JSONException an exception building the JSONObject.
     */
    JSONObject getSetList(UserItem userItem,
            SkillModelItem skillModelItem,
            String loadedSetName, boolean isModified) throws JSONException {
        JSONObject returnJSON = new JSONObject();

        List<JSONArray> setListJSON = getSetListJSON((String)userItem.getId(),
                skillModelItem, loadedSetName, isModified);
        returnJSON.put("sets", setListJSON);
        returnJSON.put("successFlag", true);
        int numSets = setListJSON.size();
        returnJSON.put("message", "Successfully returned list of " + numSets + " sets.");

        return returnJSON;
    }

    /**
     * Builds the a List of JSONArray objects for the list of sets for the given KCM.
     * @param userId the id of the current user
     * @param skillModelItem the given KCM
     * @param loadedSetName the loaded set name, null if none is currently selected/loaded
     * @param isModified flag indicating whether the currently loaded set is modified
     * @return a JSONObject.
     * @throws JSONException an exception building the JSONObject.
     */
    List<JSONArray> getSetListJSON(String userId, SkillModelItem skillModelItem,
            String loadedSetName, boolean isModified) throws JSONException {
        if (logger.isDebugEnabled()) {
            logger.debug("getSetListJSON: loadedSetName is " + loadedSetName + ".");
        }
        List <JSONArray> setListJSON = new ArrayList <JSONArray>();

        //if dataset has no skill models won't get a set list.
        if (skillModelItem == null) { return setListJSON; }

        SetDao setDao = DaoFactory.DEFAULT.getSetDao();
        List setList = setDao.findSkillSets(skillModelItem);

        for (Iterator iter = setList.iterator(); iter.hasNext();) {
            SetItem setItem = (SetItem)iter.next();
            JSONArray setInfoJSON = new JSONArray();
            setInfoJSON.put(setItem.getId()); // 0. id
            setInfoJSON.put(setItem.getName()); // 1. name
            setInfoJSON.put(setItem.getDescription()); // 2. description
            boolean ownerFlag = false;
            if (userId.equals(setItem.getOwner().getId())) {
                ownerFlag = true;
            }
            setInfoJSON.put(ownerFlag); // 3. owner flag, true if owner is the user
            setInfoJSON.put(setItem.getNumberOfSkills()); // 4. number of KCs
            if (setItem.getName().equals(loadedSetName)) {
                if (isModified) {
                    setInfoJSON.put(LOADED_MODIFIED); // 5. loaded and modified string
                } else {
                    setInfoJSON.put(LOADED); // 5. loaded string (not modified)
                }
            } else {
                setInfoJSON.put(""); // 5. blank string (not loaded)
            }
            setListJSON.add(setInfoJSON);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("getSetListJSON: Found " + setList.size()
                    + " sets for KCM " + skillModelItem.getSkillModelName()
                    + " (" + skillModelItem.getId() + ")");
        }

        return setListJSON;
    }

    /**
     * Load the given skill set.
     * @param datasetItem the selected dataset
     * @param skillModelItem the selected skill model
     * @param userItem the current user
     * @param setItem the selected skill set
     * @return the list of skills in the given set
     */
    List loadSkillSet(DatasetItem datasetItem, SkillModelItem skillModelItem,
            UserItem userItem, SetItem setItem) {
        SetDao setDao = DaoFactory.DEFAULT.getSetDao();
        setItem = setDao.get((Integer)setItem.getId());
        String setName = setItem.getName();

        if (logger.isDebugEnabled()) {
            logger.debug("loadSkillSet: " + setName + " for user " + userItem.getId()
                    + " for skill model " + skillModelItem.getSkillModelName());
        }

        String action = USER_ACTION_LOADED;
        String info = "Loaded set '" + setName
                + "'(" + setItem.getId() + ") for KCM '"
                + skillModelItem.getSkillModelName() + "' (" + skillModelItem.getId() + ").";
        UserLogger.log(datasetItem, userItem, action, info, false);

        return setItem.getSkillsExternal();
    }

    /**
     * Save the currently selected skills to a new set.
     * @param datasetItem the current dataset
     * @param skillModelItem the current skill model
     * @param userItem the current user who will also own the set
     * @param setName the name of the set
     * @param description the description of the set
     * @param selectedSkills the list of selected skills
     * @return a JSONObject with a successFlag, message and set list
     * @throws JSONException an exception building the JSONObject
     */
    JSONObject saveSkillSet(DatasetItem datasetItem, SkillModelItem skillModelItem,
            UserItem userItem, String setName,
            String description, List selectedSkills) throws JSONException {

        // Trim any whitespace first.
        setName = setName.trim();

        if (logger.isDebugEnabled()) {
            logger.debug("saveSkillSet: set '" + setName + "' for user " + userItem.getId()
                    + " for skill model " + skillModelItem.getSkillModelName());
        }

        JSONObject returnJSON = new JSONObject();
        boolean successFlag = false;
        String message;

        //
        // Check if any KCs are selected.
        //
        if (selectedSkills.size() == 0) {
            message = " Cannot create set '" + setName + "' without any KCs selected.";
            returnJSON.put("successFlag", successFlag);
            returnJSON.put("message", message);
            return returnJSON;
        }

        //
        // Check if the new name is valid, that is check if it has
        // any characters except white space.
        //
        if (setName == null || setName.trim().length() == 0) {
            message = "Please enter a name.";
            returnJSON.put("successFlag", successFlag);
            returnJSON.put("message", message);
            return returnJSON;
        }

        //
        // Check if the new name is valid, that is check if it is too long
        //
        if (setName.trim().length() > Item.TINY_TEXT_LENGTH) {
            if (logger.isDebugEnabled()) {
                logger.debug("saveSkillSet: name is too long at " + setName.length()
                        + " characters. Max is " + Item.TINY_TEXT_LENGTH);
            }
            message = "Please enter a shorter name.";
            returnJSON.put("successFlag", successFlag);
            returnJSON.put("message", message);
            return returnJSON;
        }

        String action = null;
        String info = null;

        SetDao setDao = DaoFactory.DEFAULT.getSetDao();
        SkillDao skillDao = DaoFactory.DEFAULT.getSkillDao();

        SetItem setItem = null;
        List setItems = setDao.find(skillModelItem, setName);
        if (setItems.size() > 0) {
            setItem = (SetItem)setItems.get(0);
        }
        if (setItems.size() > 1) {
            logger.warn("There are two sets with the same name, type and skill model id: "
                + setName + ", " + SetItem.SET_TYPE_SKILL + ", "
                + skillModelItem.getSkillModelName() + "(" + skillModelItem.getId() + ")");
        }

        //
        // If the set does not exist yet, then create a new one.
        //
        if (setItem == null) {
            setItem = new SetItem();
            setItem.setType(SetItem.SET_TYPE_SKILL);
            setItem.setName(setName);
            setItem.setOwner(userItem);

            for (Iterator iter = selectedSkills.iterator(); iter.hasNext();) {
                SkillItem skillItem = (SkillItem)iter.next();
                skillItem = skillDao.get((Long)skillItem.getId());
                setItem.addSkill(skillItem);
            }

            setDao.saveOrUpdate(setItem);

            successFlag = true;
            message = "Saved set '" + setName + "'.";
            action = USER_ACTION_CREATE;
            info = "Created set '" + setName + "'(" + setItem.getId()
                 + ") for KCM '" + skillModelItem.getSkillModelName()
                 + "'(" + skillModelItem.getId() + "): (n=" + selectedSkills.size() + ")";

        //
        // The set exists, change the list of skills by removing all the ones in the set
        // and adding all the currently selected skills.
        // Also check if the current user is the owner.  If not, return error.
        //
        } else {
            if (setItem.getOwner().getId().equals(userItem.getId())) {
                List oldSkills = setItem.getSkillsExternal();
                // Remove skills from set
                for (Iterator iter = oldSkills.iterator(); iter.hasNext();) {
                    SkillItem oldSkill = (SkillItem)iter.next();
                    oldSkill = skillDao.get((Long)oldSkill.getId());
                    setItem.removeSkill(oldSkill);
                }
                // Add selected skills to set
                for (Iterator iter = selectedSkills.iterator(); iter.hasNext();) {
                    SkillItem newSkill = (SkillItem)iter.next();
                    newSkill = skillDao.get((Long)newSkill.getId());
                    setItem.addSkill(newSkill);
                }

                setDao.saveOrUpdate(setItem);

                successFlag = true;
                message = "Saved set '" + setName + "'.";
                action = USER_ACTION_MODIFIED;
                info = "Modified set '" + setName + "'(" + setItem.getId()
                     + ") for KCM '" + skillModelItem.getSkillModelName()
                     + "'(" + skillModelItem.getId() + "): (n=" + selectedSkills.size() + ")";

            } else { // user not owner of this set, this should be prevented by JavaScript
                successFlag = false;
                message = "Set '" + setName + "' was created by " + setItem.getOwner().getName()
                    + " and can't be overwritten.  Please choose a different name.";
            }
        }

        if (successFlag) {
            returnJSON.put("setItem", setItem);
            UserLogger.log(datasetItem, userItem, action, info, false);
        } else {
            logger.info("saveSkillSet: " + message);
        }

        returnJSON.put("successFlag", successFlag);
        returnJSON.put("message", message);
        return returnJSON;
    }

    /**
     * Rename a set.
     * @param datasetItem the current dataset
     * @param skillModelItem the current skill model
     * @param userItem the current user who will also own the set
     * @param setItem the set to be renamed
     * @param newSetName the new name of the set
     * @return a JSONObject with a successFlag and message
     * @throws JSONException an exception building the JSONObject
     */
    JSONObject renameSkillSet(DatasetItem datasetItem, SkillModelItem skillModelItem,
            UserItem userItem, SetItem setItem, String newSetName) throws JSONException {

        // Trim any whitespace first.
        newSetName = newSetName.trim();

        if (logger.isDebugEnabled()) {
            logger.debug("renameSkillSet set (" + setItem.getId() + ")"
                    + " to '" + newSetName + "'"
                    + " for user " + userItem.getId()
                    + " for skill model " + skillModelItem.getSkillModelName());
        }

        JSONObject returnJSON = new JSONObject();
        boolean successFlag = false;
        String message = null;

        //
        // Check if the new name is valid
        //
        if (newSetName == null || newSetName.trim().length() == 0) {
            message = "Please enter a new name.";
            returnJSON.put("successFlag", successFlag);
            returnJSON.put("message", message);
            return returnJSON;
        }

        SetDao setDao = DaoFactory.DEFAULT.getSetDao();
        String action = null;
        String info = null;

        //
        // Check if a set with the new name exists already.
        //
        SetItem existingSetItem = null;
        List setItems = setDao.find(skillModelItem, newSetName);
        if (setItems.size() > 0) {
            existingSetItem = (SetItem)setItems.get(0);
        }
        if (existingSetItem != null) {
            message = " Cannot rename set to '" + newSetName + "' because it already exists.";
            returnJSON.put("successFlag", successFlag);
            returnJSON.put("message", message);
            return returnJSON;
        }

        //
        // If the set does not exist, its an error.
        //
        if (setItem == null) {
            successFlag = false;
            message = "Set does not exist.";
        //
        // The set exists.
        //
        } else {
            setItem = setDao.get((Integer)setItem.getId()); //attach to session
            String oldSetName = setItem.getName(); //get the current/old name
            if (setItem.getOwner().getId().equals(userItem.getId())) {
                setItem.setName(newSetName);
                setDao.saveOrUpdate(setItem);
                successFlag = true;
                message = "Renamed set '" + oldSetName + "' to '" + newSetName + "'.";
                action = USER_ACTION_RENAMED;
                info = "Renamed set '" + oldSetName + "' to '" + newSetName
                     +  " ' (" + setItem.getId()
                     + ") for KCM '" + skillModelItem.getSkillModelName()
                     + "'(" + skillModelItem.getId() + ")";
            } else { // user not owner of this set, this should be prevented by JavaScript
                successFlag = false;
                message = "Set '" + oldSetName + "' was created by " + setItem.getOwner().getName()
                    + " and can't be renamed.";
            }
        }

        setDao.saveOrUpdate(setItem);

        if (successFlag) {
            UserLogger.log(datasetItem, userItem, action, info, false);
        }

        returnJSON.put("successFlag", successFlag);
        returnJSON.put("message", message);
        return returnJSON;
    }

    /**
     * Delete a set.
     * @param datasetItem the current dataset
     * @param skillModelItem the current skill model
     * @param userItem the current user who will also own the set
     * @param setItem the set to be renamed
     * @return a JSONObject with a successFlag and message
     * @throws JSONException an exception building the JSONObject
     */
    JSONObject deleteSkillSet(DatasetItem datasetItem, SkillModelItem skillModelItem,
            UserItem userItem, SetItem setItem) throws JSONException {
        if (logger.isDebugEnabled()) {
            logger.debug("deleteSkillSet set (" + setItem.getId() + ")"
                    + " for user " + userItem.getId()
                    + " for skill model " + skillModelItem.getSkillModelName());
        }

        JSONObject returnJSON = new JSONObject();
        boolean successFlag = false;
        String message = null;

        SetDao setDao = DaoFactory.DEFAULT.getSetDao();
        String action = null;
        String info = null;

        //
        // If the set does not exist, its an error.
        //
        if (setItem == null) {
            successFlag = false;
            message = "Set does not exist.";
            logger.error("User '" + userItem.getId()
                    + "' tried to delete a set that does not exist.");
        //
        // The set exists.
        //
        } else {
            setItem = setDao.get((Integer)setItem.getId()); //attach to session
            String setName = setItem.getName(); //get the current/old name
            if (setItem.getOwner().getId().equals(userItem.getId())) {
                setDao.delete(setItem);
                successFlag = true;
                message = "Deleted set '" + setName + "'.";
                action = USER_ACTION_DELETED;
                info = "Deleted set '" + setName + "'"
                     +  " (" + setItem.getId()
                     + ") for KCM '" + skillModelItem.getSkillModelName()
                     + "'(" + skillModelItem.getId() + ")";
            } else { // user not owner of this set, this should be prevented by JavaScript
                successFlag = false;
                message = "Set '" + setName + "' was created by " + setItem.getOwner().getName()
                    + " and can't be deleted.";
            }
        }

        if (successFlag) {
            UserLogger.log(datasetItem, userItem, action, info, false);
        }

        returnJSON.put("successFlag", successFlag);
        returnJSON.put("message", message);
        return returnJSON;
    }
}
