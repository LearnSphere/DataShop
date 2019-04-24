/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2008
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.SetItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.item.UserItem;

/**
 * This servlet is for handling the Set actions from the Manage KC Sets dialog.
 * There are two session parameters expected.  One for the type of request
 * and the other for the set name.
 * The request can be:
 * <ul>
 * <li>get list</li>
 * <li>load</li>
 * <li>save</li>
 * <li>rename</li>
 * <li>delete</li>
 * </ul>
 *
 * @author Alida Skogsholm
 * @version $Revision: 8426 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2012-12-14 14:26:17 -0500 (Fri, 14 Dec 2012) $
 * <!-- $KeyWordsOff: $ -->
 */
public class SetServlet extends AbstractServlet {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** The Servlet name. */
    public static final String SERVLET = "Set";

    /** Session Parameter. */
    private static final String SET_REQUEST_PARAM = "ds_set_request";
    /** Possible value for session parameter. */
    private static final String SET_REQUEST_GET_LIST = "get_list";
    /** Possible value for session parameter. */
    private static final String SET_REQUEST_SAVE_SET = "save";
    /** Possible value for session parameter. */
    private static final String SET_REQUEST_RENAME_SET = "rename";
    /** Possible value for session parameter. */
    private static final String SET_REQUEST_DELETE_SET = "delete";
    /** Set name parameter. */
    private static final String SET_NAME_PARAM = "ds_set_name";
    /** New set name parameter. */
    private static final String NEW_SET_NAME_PARAM = "ds_set_new_name";

    /**
     * Handles the HTTP get.
     * @see javax.servlet.http.HttpServlet
     * @param req HttpServletRequest.
     * @param resp HttpServletResponse.
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        doPost(req, resp);
    }

    /**
     * Handles the HTTP post.
     * @see javax.servlet.http.HttpServlet
     * @param req HttpServletRequest.
     * @param resp HttpServletResponse.
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        logDebug("doPost begin :: ", getDebugParamsString(req));
        PrintWriter out = null;
        try {
            setEncoding(req, resp);

            DatasetContext datasetContext = getDatasetContext(req);
            String infoPrefix = getInfoPrefix(datasetContext);

            // Get the request type.
            String setRequest = req.getParameter(SET_REQUEST_PARAM);
            if (setRequest == null) {
                logger.warn("Set request type was not specified: ");
                return;
            }
            logger.info(infoPrefix + ": " + setRequest);

            // Get some necessary information to process any request.
            NavigationHelper navHelper = HelperFactory.DEFAULT.getNavigationHelper();
            DatasetContext info = getDatasetContext(req);
            SkillModelItem skillModelItem = navHelper.getSelectedSkillModelItem(info);

            if (skillModelItem != null) {
                logger.info(infoPrefix + ": " + setRequest
                        + " for KCM: " + skillModelItem.getSkillModelName());
            }

            SetItem loadedSetItem = navHelper.getSelectedSkillSetItem(info);
            String loadedSetName = "";
            if (loadedSetItem != null) {
                loadedSetName = loadedSetItem.getName();
            }

            String setName = req.getParameter(SET_NAME_PARAM);

            if (setRequest.equals(SET_REQUEST_GET_LIST)) {
                boolean modifiedFlag = info.getNavContext().isSkillSetModified();
                JSONObject setList = getSetList(info.getUser(), skillModelItem,
                        loadedSetName, modifiedFlag);
                out = writeJson(resp, setList);
                return;

            } else if (setRequest.equals(SET_REQUEST_SAVE_SET)) {
                JSONObject json = saveSet(info, skillModelItem, setName);
                out = writeJson(resp, json);
                return;

            } else if (setRequest.equals(SET_REQUEST_RENAME_SET)) {
                JSONObject json = renameSet(req, info, skillModelItem, setName);
                out = writeJson(resp, json);
                return;

            } else if (setRequest.equals(SET_REQUEST_DELETE_SET)) {
                JSONObject json = deleteSet(info, skillModelItem, setName);
                out = writeJson(resp, json);
                return;

            } else {
                logger.warn("Invalid set request type specified: " + setRequest);
            }

            setInfo(req, info);

        } catch (Exception exception) {
            forwardError(req, resp, logger, exception);
        } finally {
            if (out != null) {
                out.close();
            }
            logger.debug("doPost end");
        }
    } // end doPost

    /**
     * Get a list of skill sets.
     * @param userItem the current user
     * @param skillModelItem the skill model item
     * @param loadedSetName the name of the loaded set
     * @param modifiedFlag indicates whether the current set is modified or not
     * @return JSONObject with setName
     * @throws JSONException a possible exception when using JSON
     */
    private JSONObject getSetList(UserItem userItem, SkillModelItem skillModelItem,
            String loadedSetName, boolean modifiedFlag)
            throws JSONException {
        SetHelper setHelper = HelperFactory.DEFAULT.getSetHelper();

        JSONObject json = setHelper.getSetList(userItem, skillModelItem,
                loadedSetName, modifiedFlag);
        json.put("setName", loadedSetName);
        json.put("modifiedFlag", modifiedFlag);
        if (skillModelItem != null) {
            json.put("modelName", skillModelItem.getSkillModelName());
        }
        return json;
    }

    /**
     * Save set.
     * @param datasetContext the datasetContext
     * @param skillModelItem the skill model item
     * @param setName the set name
     * @return JSONObject with setName
     * @throws JSONException a possible exception when using JSON
     */
    private JSONObject saveSet(DatasetContext datasetContext,
            SkillModelItem skillModelItem, String setName) throws JSONException {

        NavigationHelper navHelper = HelperFactory.DEFAULT.getNavigationHelper();
        SetHelper setHelper = HelperFactory.DEFAULT.getSetHelper();

        // Get the selected skills
        List selectedSkills = navHelper.getSelectedSkills(datasetContext);

        // Save the set
        JSONObject json = setHelper.saveSkillSet(datasetContext.getDataset(),
                skillModelItem, datasetContext.getUser(),
                setName, "", selectedSkills);

        boolean successFlag = (Boolean)json.get("successFlag");
        if (successFlag) {
            // If save successful, change the selected set in the navigation helper
            // Get the new list of sets to return to caller
            List<JSONArray> stepJSONList
                    = setHelper.getSetListJSON(datasetContext.getUserId(),
                            skillModelItem, setName, false);
            json.put("sets", stepJSONList);

            SetItem newItem = (SetItem)json.get("setItem");
            datasetContext.getNavContext().addSkillSet(new SelectableItem(newItem));
            navHelper.selectSkillSet(datasetContext, (Integer)newItem.getId());
            datasetContext.getNavContext().setSkillSetModified(false);
            json.put("modifiedFlag", false);
        }

        json.put("setName", setName);
        return json;
    }

    /**
     * Rename set.
     * @param req the HTTP servlet request
     * @param datasetContext the dataset information stored in the session.
     * @param skillModelItem the skill model item
     * @param oldSetName the set name
     * @return JSONObject with sets
     * @throws JSONException a possible exception when using JSON
     */
    private JSONObject renameSet(HttpServletRequest req, DatasetContext datasetContext,
            SkillModelItem skillModelItem, String oldSetName) throws JSONException {

        NavigationHelper navHelper = HelperFactory.DEFAULT.getNavigationHelper();
        SetHelper setHelper = HelperFactory.DEFAULT.getSetHelper();
        String newSetName = req.getParameter(NEW_SET_NAME_PARAM);

        UserItem userItem = datasetContext.getUser();
        DatasetItem datasetItem = datasetContext.getDataset();

        // Get the set item
        SetItem setItem = setHelper.getSkillSet(skillModelItem, oldSetName);

        // Rename the set in the database
        JSONObject json = setHelper.renameSkillSet(datasetItem, skillModelItem,
                userItem, setItem, newSetName);

        boolean successFlag = (Boolean)json.get("successFlag");
        if (successFlag) {
            // Rename the set in the HTTP session
            navHelper.renameSkillSetItem(datasetContext, oldSetName, newSetName);
            boolean modifiedFlag = datasetContext.getNavContext().isSkillSetModified();

            // Set the setName to the currently loaded set name, if there is one.
            String loadedSetName = "";
            SetItem loadedSetItem = navHelper.getSelectedSkillSetItem(datasetContext);
            if (loadedSetItem != null) {
                loadedSetName = loadedSetItem.getName();
            }
            json.put("setName", loadedSetName);

            // Get the new list of sets to return to caller
            List<JSONArray> stepJSONList
                    = setHelper.getSetListJSON((String)userItem.getId(),
                            skillModelItem, loadedSetName, modifiedFlag);
            json.put("sets", stepJSONList);
            json.put("modifiedFlag", modifiedFlag);
        }

        return json;
    }

    /**
     * Delete set.
     * @param datasetContext the dataset information stored in the session.
     * @param skillModelItem the skill model item
     * @param setName the set name
     * @return JSONObject with sets
     * @throws JSONException a possible exception when using JSON
     */
    private JSONObject deleteSet(DatasetContext datasetContext,
            SkillModelItem skillModelItem, String setName) throws JSONException {

        NavigationHelper navHelper = HelperFactory.DEFAULT.getNavigationHelper();
        SetHelper setHelper = HelperFactory.DEFAULT.getSetHelper();

        // Get the set item
        SetItem deletedSkillSetItem = setHelper.getSkillSet(skillModelItem, setName);

        // Delete the set
        JSONObject json = setHelper.deleteSkillSet(datasetContext.getDataset(), skillModelItem,
                datasetContext.getUser(), deletedSkillSetItem);

        datasetContext.getNavContext().removeSkillSet(deletedSkillSetItem);

        boolean successFlag = (Boolean)json.get("successFlag");
        if (successFlag) {
            // Set the set name to the currently loaded set, blank if the current set was deleted.
            String loadedSetName = "";
            boolean modifiedFlag = false;
            SetItem loadedSetItem = navHelper.getSelectedSkillSetItem(datasetContext);
            if (loadedSetItem != null) {
                loadedSetName = loadedSetItem.getName();
                if (loadedSetName.equals(setName)) {
                    loadedSetName = "";
                    modifiedFlag = false;
                    datasetContext.getNavContext().setSkillSetModified(modifiedFlag);
                } else {
                    modifiedFlag = datasetContext.getNavContext().isSkillSetModified();
                }
            }
            json.put("setName", loadedSetName);

            // Get the new list of sets to return to caller
            List<JSONArray> stepJSONList
                    = setHelper.getSetListJSON(datasetContext.getUserId(),
                            skillModelItem, loadedSetName, modifiedFlag);
            json.put("sets", stepJSONList);
            json.put("modifiedFlag", modifiedFlag);
        }

        return json;
    }

    /**
     * Writes a JSON object back.
     * @param resp the HttpServletResponse
     * @param json the JSONObject
     * @return the PrintWriter so it can be closed properly
     * @throws IOException an IO exception in case something bad happens
     */
    private PrintWriter writeJson(HttpServletResponse resp, JSONObject json)
            throws IOException {
        PrintWriter out;
        resp.setContentType("application/json");
        out = resp.getWriter();
        out.write(json.toString());
        out.flush();
        return out;
    }
} // end class
