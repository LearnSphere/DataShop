/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2007
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.datasetinfo;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.cmu.pslc.datashop.dao.CurriculumDao;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.DomainDao;
import edu.cmu.pslc.datashop.dao.ImportQueueDao;
import edu.cmu.pslc.datashop.dao.LearnlabDao;
import edu.cmu.pslc.datashop.dao.PaperDao;
import edu.cmu.pslc.datashop.dao.ProjectDao;
import edu.cmu.pslc.datashop.dao.UserDao;
import edu.cmu.pslc.datashop.dto.DatasetDTO;
import edu.cmu.pslc.datashop.helper.DatasetCreator;
import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.CurriculumItem;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.DomainItem;
import edu.cmu.pslc.datashop.item.ImportQueueItem;
import edu.cmu.pslc.datashop.item.LearnlabItem;
import edu.cmu.pslc.datashop.item.PaperItem;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;
import edu.cmu.pslc.datashop.servlet.DatasetContext;
import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.util.DataShopInstance;

import org.apache.commons.lang.time.FastDateFormat;

/**
 * This class assists in the editing of the dataset info.
 *
 * @author Benjamin Billings
 * @version $Revision: 13162 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-04-21 10:22:07 -0400 (Thu, 21 Apr 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DatasetInfoEditServlet extends AbstractServlet  {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** Curriculum parameter */
    private static final String CURRICULUM_PARAM = "curriculum";
    /** Description parameter */
    private static final String DATASET_DESCRIPTION_PARAM = "datasetDescription";
    /** Status parameter */
    private static final String DATASET_STATUS_PARAM = "datasetStatus";
    /** File parameter */
    private static final String FILE_PARAM = "fileId";
    /** File parameter */
    private static final String PREV_FILE_PARAM = "prevFileId";
    /** Tutor parameter */
    private static final String TUTOR_PARAM = "tutor";
    /** Domain LearnLab parameter */
    private static final String DOMAIN_LEARNLAB_PARAM = "domain_learnlab";
    /** Project parameter */
    private static final String DATASET_NAME_PARAM = "datasetName";
    /** Dataset name parameter */
    private static final String PROJECT_PARAM = "project";
    /** Study Flag parameter */
    private static final String STUDY_FLAG_PARAM = "studyFlag";
    /** Hypothesis parameter */
    private static final String HYPOTHESIS_PARAM = "hypothesis";
    /** Acknowledgment parameter */
    private static final String ACKNOWLEDGMENT_PARAM = "acknowledgment";
    /** Citation parameter */
    private static final String CITATION_PARAM = "citation";
    /** Notes parameter */
    private static final String NOTES_PARAM = "notes";
    /** School parameter */
    private static final String SCHOOL_PARAM = "school";
    /** Start Date parameter */
    private static final String START_DATE_PARAM = "startDate";
    /** End Date parameter */
    private static final String END_DATE_PARAM = "endDate";

    /** Auto Set the dates parameter */
    private static final String AUTO_SET_DATES_PARAM = "autosetDates";

    /** Get Project List parameter */
    private static final String GET_PROJECT_LIST_PARAM = "getProjectList";
    /** Get User List parameter */
    private static final String GET_USER_LIST_PARAM = "getUserList";
    /** Get Curriculum List parameter */
    private static final String GET_CURRICULUM_LIST_PARAM = "getCurriculumList";
    /** Get Domain Learnlab List parameter */
    private static final String GET_DOMAIN_LEARNLAB_LIST_PARAM = "getDomainLearnlabList";

    /** String delimiter */
    private static final String SLASH_DELIM = "/";

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

    /** Message for the user. */
    private static final String LOGIN_AGAIN_MSG =
        "You are no longer logged in.  Backup your current changes and then log in again.";

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

            boolean isLoggedIn = isLoggedIn(req);
            if (!isLoggedIn) {
                out = resp.getWriter();
                out.write(buildJSONMessage("UNAUTHENTICATED", LOGIN_AGAIN_MSG).toString());
                out.flush();
                out.close();
                return;
            }

            DatasetContext datasetContext = getDatasetContext(req);
            DatasetItem datasetItem = null;
            if (datasetContext != null) {
                datasetItem = datasetContext.getDataset();
            }

            UserItem userItem = getUser(req);
            boolean hasAdminAuthorization = userItem.getAdminFlag();

            if (!hasAdminAuthorization) {
                boolean hasEditAuthorization = hasEditAuthorization(req, datasetContext);
                if (!hasEditAuthorization) {
                    out = resp.getWriter();
                    out.write(buildJSONMessage("UNAUTHORIZED",
                    "You are not authorized to edit this dataset.").toString());
                    out.flush();
                    out.close();
                    return;
                }
            }

            JSONObject returnJSON = processUserListParameter(req, hasAdminAuthorization);

            if (returnJSON == null) {
                returnJSON = proccessParameters(req, datasetItem, hasAdminAuthorization, userItem);
            }

            if (returnJSON == null) {
                returnJSON = buildJSONMessage("ERROR", "Unknown parameters.");
            }

            resp.setContentType("application/json");
            out = resp.getWriter();
            out.write(returnJSON.toString());
            out.flush();
        } catch (Throwable throwable) {
            logger.error("Exception occurred editing dataset info.", throwable);
            resp.setContentType("text/html");
            try {
                out = resp.getWriter();
                out.write(buildJSONMessage("ERROR",
                        "An unexpected error occurred, please try again and/or "
                        + "contact the datashop team and describe the error.").toString());
                out.flush();
            } catch (JSONException jsonException) {
                logger.warn("First an Exception occurred.", throwable);
                logger.warn("Then an JSONException occurred.", jsonException);
            } catch (IOException ioException) {
                logger.warn("First an Exception occurred.", throwable);
                logger.warn("Then an IOException occurred.", ioException);
            }
        } finally {
            if (out != null) {
                out.close();
            }
            logger.debug("doPost end");
        }
    }

    /**
     * Build a JSONObject list of project names.
     * @return a JSONObject that is a list of project IDs and Names
     * @throws JSONException a JSON exception building the JSONObject
     */
    private JSONObject getProjectList() throws JSONException {

        logger.debug("Getting list of projects as a JSON string.");

        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        List projectList = projectDao.findAll();
        Collections.sort(projectList);
        List <JSONObject> jsonProjectList = new ArrayList <JSONObject>();

        //insert a "blank" item.
        JSONObject projectJSON = new JSONObject();
        projectJSON.append("value", "");
        projectJSON.append("text", "");
        jsonProjectList.add(projectJSON);

        for (Iterator it = projectList.iterator(); it.hasNext();) {
                ProjectItem projectItem = (ProjectItem)it.next();
                projectJSON = new JSONObject();
                projectJSON.append("value", projectItem.getProjectName());
                projectJSON.append("text", projectItem.getProjectName());
                jsonProjectList.add(projectJSON);
        }

        JSONObject projectListJSON = new JSONObject();
        JSONArray jsonArray = new JSONArray(jsonProjectList);
        projectListJSON.put("suggestions", jsonArray);
        return projectListJSON;
    }

    /**
     * Process all the edit parameters returning a JSONObject for a return message;
     * @param req the HttpServletRequest
     * @param dataset the dataset being modified
     * @param hasAdminAuthorization boolean indicating administrator level authorization.
     * @param user the UserItem of the user making the request (for logging purposes)
     * @return JSONObject return message
     * @throws JSONException an exception occurred creating the JSON objects.
     */
    private JSONObject proccessParameters(HttpServletRequest req, DatasetItem dataset,
            Boolean hasAdminAuthorization, UserItem user) throws JSONException {
        if (dataset == null) {
            return null;
        }

        JSONObject returnJSON = null;

        DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
        String fieldParam = req.getParameter("field");
        String value = req.getParameter("value");
        String startDateString = req.getParameter(START_DATE_PARAM);
        String endDateString = req.getParameter(END_DATE_PARAM);
        String autoSetDates = req.getParameter(AUTO_SET_DATES_PARAM);
        dataset = datasetDao.get((Integer)dataset.getId());

        if (DATASET_DESCRIPTION_PARAM.equals(fieldParam)) {
            String oldDescription = dataset.getDescription();
            dataset.setDescription((value == "") ? null : value);
            datasetDao.saveOrUpdate(dataset);
            returnJSON = buildJSONMessage("SUCCESS", "Description updated.", value);
            UserLogger.log(dataset, user, UserLogger.DATASET_INFO_MODIFY,
                    (value.equals(oldDescription)) ? "No Change for description"
                            : "Changed description from '" + oldDescription
                                + "' to '" + value + "'");
        }

        if (DATASET_STATUS_PARAM.equals(fieldParam)) {
            String oldStatus = dataset.getStatus();
            dataset.setStatus((value == "") ? null : value);
            datasetDao.saveOrUpdate(dataset);
            returnJSON = buildJSONMessage("SUCCESS", "Status updated.", value);
            UserLogger.log(dataset, user, UserLogger.DATASET_INFO_MODIFY,
                    (value.equals(oldStatus)) ? "No Change for status"
                            : "Changed status from '" + oldStatus + "' to '" + value + "'");
        }

        if (TUTOR_PARAM.equals(fieldParam)) {
            String oldTutor = dataset.getTutor();
            dataset.setTutor((value == "") ? null : value);
            datasetDao.saveOrUpdate(dataset);
            returnJSON = buildJSONMessage("SUCCESS", "Tutor updated.", value);
            UserLogger.log(dataset, user, UserLogger.DATASET_INFO_MODIFY,
                    (value.equals(oldTutor)) ? "No Change for tutor"
                            : "Changed tutor from '" + oldTutor + "' to '" + value + "'");
        }


        if (STUDY_FLAG_PARAM.equals(fieldParam)) {
            String studyFlagBool = value;
            if (studyFlagBool.equals("Not Specified")) {
                studyFlagBool = DatasetItem.STUDY_FLAG_NOT_SPEC;
            }
            String oldStudyFlag = dataset.getStudyFlag();
            dataset.setStudyFlag(studyFlagBool);
            datasetDao.saveOrUpdate(dataset);
            returnJSON = buildJSONMessage("SUCCESS", "Has study updated.", value);
            UserLogger.log(dataset, user, UserLogger.DATASET_INFO_MODIFY,
                    (value.equals(oldStudyFlag)) ? "No Change for studyFlag"
                            : "Changed studyflag from '" + oldStudyFlag
                                + "' to '" + studyFlagBool + "'");
        }

        if (HYPOTHESIS_PARAM.equals(fieldParam)) {
            String oldHypothesis = dataset.getHypothesis();
            dataset.setHypothesis((value == "") ? null : value);
            datasetDao.saveOrUpdate(dataset);
            returnJSON = buildJSONMessage("SUCCESS", "Hypothesis updated.", value);
            UserLogger.log(dataset, user, UserLogger.DATASET_INFO_MODIFY,
                    (value.equals(oldHypothesis)) ? "No Change for hypothesis"
                            : "Changed hypothesis from '" + oldHypothesis
                                + "' to '" + value + "'");
        }

        if (NOTES_PARAM.equals(fieldParam)) {
            String oldNotes = dataset.getNotes();
            dataset.setNotes((value == "") ? null : value);
            datasetDao.saveOrUpdate(dataset);
            returnJSON = buildJSONMessage("SUCCESS", "Notes updated.", value);
            UserLogger.log(dataset, user, UserLogger.DATASET_INFO_MODIFY,
                    (value.equals(oldNotes)) ? "No Change for notes"
                            : "Changed notes from '" + oldNotes + "' to '" + value + "'");
        }

        if (SCHOOL_PARAM.equals(fieldParam)) {
            String oldSchools = dataset.getSchool();
            dataset.setSchool((value == "") ? null : value);
            datasetDao.saveOrUpdate(dataset);
            returnJSON = buildJSONMessage("SUCCESS", "School updated.", value);
            UserLogger.log(dataset, user, UserLogger.DATASET_INFO_MODIFY,
                    (value.equals(oldSchools)) ? "No Change for school"
                            : "Changed school from '" + oldSchools + "' to '" + value + "'");
        }

        if (startDateString != null || endDateString  != null) {
            returnJSON = processDatesSave(startDateString, endDateString, dataset, user);
        }
        if (autoSetDates != null) { returnJSON = autoSetDates(dataset, user); }

        if (CURRICULUM_PARAM.equals(fieldParam)) {
            returnJSON = proccessCurriculumSave(dataset, value, user);
        }
        String getCurriculumList = req.getParameter(GET_CURRICULUM_LIST_PARAM);
        if (getCurriculumList != null) { returnJSON = getCurriculumList(); }

        if (DOMAIN_LEARNLAB_PARAM.equals(fieldParam)) {
            returnJSON = proccessDomainLearnlabSave(dataset, value, user);
        }

        String getDomainLearnlabList = req.getParameter(GET_DOMAIN_LEARNLAB_LIST_PARAM);
        List domainLearnlabList = new ArrayList();
        if (getDomainLearnlabList != null) {
            DatasetInfoEditHelper datasetInfoEditHelper =
                HelperFactory.DEFAULT.getDatasetInfoEditHelper();
            domainLearnlabList =  datasetInfoEditHelper.getDomainLearnlabList();
        }
        if (getDomainLearnlabList != null) {
            returnJSON = getDomainLearnlabList(domainLearnlabList);
        }

        //System Administrator only edits from this point on.
        if (PROJECT_PARAM.equals(fieldParam) && hasAdminAuthorization) {
            returnJSON = proccessProjectSave(dataset, value, user);
        } else if (PROJECT_PARAM.equals(fieldParam)) {
            returnJSON = buildJSONMessage("UNAUTHORIZED",
                        "You are not authorized to change the project.");
        }

        if (DATASET_NAME_PARAM.equals(fieldParam) && hasAdminAuthorization) {
            returnJSON = proccessDatasetSave(req, dataset, value, user);
        } else if (DATASET_NAME_PARAM.equals(fieldParam)) {
            returnJSON = buildJSONMessage("UNAUTHORIZED",
                        "You are not authorized to change the dataset name.");
        }

        String getProjectList = req.getParameter(GET_PROJECT_LIST_PARAM);
        if (getProjectList != null && hasAdminAuthorization) {
            returnJSON = getProjectList();
        } else if (getProjectList != null) {
            returnJSON = buildJSONMessage("UNAUTHORIZED",
                        "You are not authorized to view the list of projects.");
        }



        if (ACKNOWLEDGMENT_PARAM.equals(fieldParam)) {
            returnJSON = proccessAcknowledgmentSave(req, dataset, value, user);
        }

        String citationString = req.getParameter(CITATION_PARAM);
        String fileIdString = req.getParameter(FILE_PARAM);
        String prevFileIdString = req.getParameter(PREV_FILE_PARAM);

        int fileId = ((fileIdString != null) && !(fileIdString.equals("")))
                     ? Integer.parseInt(fileIdString) : 0;

        int prevFileId = 0;
        if (prevFileIdString != null) {
           prevFileId = (!prevFileIdString.equals(""))
                     ? Integer.parseInt(prevFileIdString) : 0;
        } else {
            prevFileId = -1;
        }

        if ((fileIdString != null) && (citationString == null)) {
            DatasetInfoEditHelper datasetInfoEditHelper =
                HelperFactory.DEFAULT.getDatasetInfoEditHelper();
            String citation =  datasetInfoEditHelper.getCitation(fileId);
            returnJSON = buildJSONMessage("SUCCESS", "File selected.", citation);
        } else if ((fileIdString != null) && (citationString != null)) {
            logger.debug("go to proccessCitationSave");
            returnJSON = proccessCitationSave(
                    req, dataset, prevFileId, fileId, citationString, user);
        }

        updateMasterInstance(dataset);

        String getUserList = req.getParameter(GET_USER_LIST_PARAM);
        if (getUserList != null && hasAdminAuthorization) {
            returnJSON = getUserList();
        } else if (getUserList != null) {
            returnJSON = buildJSONMessage("UNAUTHORIZED",
            "You are not authorized to view the list of users.");
        }
        return returnJSON;
    }

    /**
     * Helper method to push dataset change out to master DataShop instance.
     * @param dataset the DatasetItem
     */
    private void updateMasterInstance(DatasetItem dataset) {

        // If a slave, update master DataShop instance with dataset info.
        if (DataShopInstance.isSlave()) {
            try {
                Integer datasetId = (Integer)dataset.getId();
                DatasetDTO datasetDto =
                    HelperFactory.DEFAULT.getWebServiceHelper().datasetDTOForId(datasetId);
                DatasetCreator.INSTANCE.setDataset(datasetDto);
            } catch (Exception e) {
                // Failed to push Dataset info to master. Ignore?
                logDebug("Failed to push dataset info to master for dataset '"
                         + dataset.getDatasetName() + "': " + e);
            }
        }
    }

    /**
     * Get the user list.
     * @param req the HttpServletRequest
     * @param hasAdminAuthorization boolean indicating administrator level authorization.
     * @return JSONObject return message
     * @throws JSONException an exception occurred creating the JSON objects.
     */
    private JSONObject processUserListParameter(HttpServletRequest req,
            Boolean hasAdminAuthorization) throws JSONException {
        JSONObject returnJSON = null;
        String getUserList = req.getParameter(GET_USER_LIST_PARAM);
        if (getUserList != null && hasAdminAuthorization) {
            returnJSON = getUserList();
        } else if (getUserList != null) {
            returnJSON = buildJSONMessage("UNAUTHORIZED",
            "You are not authorized to view the list of users.");
        }
        return returnJSON;
    }

    /**
     * Process the saving of a new project.
     * @param dataset The dataset on which to set the project.
     * @param projectName The name of the project to set.
     * @param user The user making the changes (for logging purposes)
     * @return String of the project.
     * @throws JSONException a JSON exception building the JSONObject
     */
    private JSONObject proccessProjectSave(DatasetItem dataset, String projectName, UserItem user)
            throws JSONException  {
        if (projectName == null) {
            throw new IllegalArgumentException("projectName cannot be null.");
        }
        if (dataset == null) {
            throw new IllegalArgumentException("dataset cannot be null.");
        }

        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        ProjectItem projectItem;
        Collection matchingProjects = projectDao.find(projectName);
        JSONObject returnMessage;
        if (matchingProjects.size() == 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("No project found matching name " + projectName);
            }
            if (projectName.equals("")) {
                projectItem = null;
                returnMessage = buildJSONMessage("SUCCESS", "Project successfully cleared.");
            } else {
                projectItem = new ProjectItem();
                projectItem.setProjectName(projectName);
                projectItem.setCreatedBy(user);
                projectItem.setDatasetLastAddedToNow();
                projectDao.saveOrUpdate(projectItem);
                returnMessage = buildJSONMessage("SUCCESS",
                        "New project created and set.", projectName);
            }
        } else {
            if (matchingProjects.size() > 0) {
                logger.warn("More than one project with name " + projectName
                        + " found, using first.");
            }
            projectItem = (ProjectItem)matchingProjects.iterator().next();
            returnMessage = buildJSONMessage("SUCCESS", "Project set.", projectName);
        }

        String oldProjectName = null;
        if (dataset.getProject() != null) {
            oldProjectName = DaoFactory.DEFAULT.getProjectDao().get(
                    (Integer)dataset.getProject().getId()).getProjectName();
        }

        dataset.setProject(projectItem);
        DaoFactory.DEFAULT.getDatasetDao().saveOrUpdate(dataset);

        if (projectItem != null) {
            projectItem.setNeedsAttention(true);
            projectItem.setDatasetLastAddedToNow();
            DaoFactory.DEFAULT.getProjectDao().saveOrUpdate(projectItem);
        }

        // Need to push this change to import_queue, if applicable
        ImportQueueDao iqDao = DaoFactory.DEFAULT.getImportQueueDao();
        ImportQueueItem iqItem = iqDao.findByDataset(dataset);
        if (iqItem != null) {
            iqItem.setProject(projectItem);
            iqDao.saveOrUpdate(iqItem);
        }

        UserLogger.log(dataset, user, UserLogger.DATASET_INFO_MODIFY,
                (oldProjectName != null && oldProjectName.equals(projectName))
                    ? "No Change for project"
                        : "Changed project from '" + oldProjectName
                             + "' to '" + projectName + "'");

        String logMsg = "User " + user.getId() + " changed project for dataset '";
        logMsg += dataset.getDatasetName() + "' (" + dataset.getId() + "). ";

        if (projectItem == null) {
            logMsg += "The project was cleared.";
        } else {
            logMsg += "The project was changed to '" + projectName;
            logMsg += "' (" + projectItem.getId() + "). ";
            logMsg += "Needs Attention: Yes.";
        }
        logger.info(logMsg);

        return returnMessage;
    }

    /**
     * Process the saving of a new project.
     * @param req the HTTP servlet request
     * @param dataset The dataset to update.
     * @param datasetName The name of the dataset to set.
     * @param user the UserItem of the user requesting the change (for logging purposes)
     * @return String of the project.
     * @throws JSONException a JSON exception building the JSONObject
     */
    private JSONObject proccessDatasetSave(HttpServletRequest req,
            DatasetItem dataset, String datasetName, UserItem user)
            throws JSONException  {
        if (datasetName == null) {
            throw new IllegalArgumentException("datasetName cannot be null.");
        }
        if (dataset == null) {
            throw new IllegalArgumentException("dataset cannot be null.");
        }

        if (datasetName.equals("")) {
            return buildJSONMessage("ERROR", "Dataset name cannot be blank.");
        }

        String oldDatasetName = dataset.getDatasetName();

        DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
        List matchingDatasets = datasetDao.find(datasetName);
        if (matchingDatasets.size() > 0) {
            if (matchingDatasets.size() > 1) {
                logger.warn("More than one dataset found with name " + datasetName);
            }
            DatasetItem matching = (DatasetItem)matchingDatasets.get(0);
            if (!matching.getId().equals(dataset.getId())) {
                return buildJSONMessage("ERROR",
                    "Dataset already exists, please choose a different name. ", datasetName);
            } else {
                return buildJSONMessage("MESSAGE", "No change to dataset name.");
            }
        }

        dataset.setDatasetName(datasetName);
        datasetDao.saveOrUpdate(dataset);

        //Save the new dataset name in the context after saving to the database.
        //DS648:  (Dataset Name changes not shown in content header
        //or used in saving new files and papers.)
        DatasetContext datasetContext = getDatasetContext(req);
        datasetContext.setDataset(dataset);

        // Need to push this change to import_queue, if applicable
        ImportQueueDao iqDao = DaoFactory.DEFAULT.getImportQueueDao();
        ImportQueueItem iqItem = iqDao.findByDataset(dataset);
        if (iqItem != null) {
            iqItem.setDatasetName(datasetName);
            iqDao.saveOrUpdate(iqItem);
        }

        UserLogger.log(dataset, user, UserLogger.DATASET_INFO_MODIFY,
                (oldDatasetName.equals(datasetName)) ? "No change"
                        : "Changed dataset name from '" + oldDatasetName
                            + "' to '" + datasetName + "'");

        return buildJSONMessage("SUCCESS", "Dataset name set.", datasetName);
    }

    /**
     * Get a list of all datashop users for display.
     * @return a JSONObject of the data.
     * @throws JSONException a JSON exception building the JSONObject
     */
    private JSONObject getUserList() throws JSONException {
        logger.debug("Getting list of users as a JSON string.");

        UserDao userDao = DaoFactory.DEFAULT.getUserDao();
        List userList = userDao.findAll();
        Collections.sort(userList);
        List <JSONObject> jsonUserList = new ArrayList <JSONObject>();

        //add a blank option.
        JSONObject userJSON = new JSONObject();
        userJSON.append("value", "");
        userJSON.append("text", "");
        jsonUserList.add(userJSON);

        for (Iterator it = userList.iterator(); it.hasNext();) {
                UserItem userItem = (UserItem)it.next();
                if (userItem.getId().equals(UserItem.DEFAULT_USER)) { continue; }

                String firstName = (userItem.getFirstName() != null)
                                        ? userItem.getFirstName() : "-";
                String lastName = (userItem.getLastName() != null)
                                        ? userItem.getLastName() : "-";
                String email = (userItem.getEmail() != null)
                                        ? userItem.getEmail() : "-";

                String displayString = lastName + ", " + firstName
                    + " (" + userItem.getId() + ", " + email + ")";

                userJSON = new JSONObject();
                userJSON.append("text", displayString);
                userJSON.append("value", userItem.getId());
                jsonUserList.add(userJSON);
        }

        JSONObject projectListJSON = new JSONObject();
        JSONArray jsonArray = new JSONArray(jsonUserList);
        projectListJSON.put("suggestions", jsonArray);
        return projectListJSON;
    }

    /**
     * Save the domain and learnlab for the dataset.
     * @param dataset the dataset to set the domain and learnlab on.
     * @param domainLearnlabValue the domain and learnlab value.
     * @param user the user requesting the change (for logging purposes)
     * @return String of success.
     * @throws JSONException a JSON exception building the JSONObject
     */
    private JSONObject proccessDomainLearnlabSave(DatasetItem dataset, String domainLearnlabValue,
            UserItem user) throws JSONException {
        if (domainLearnlabValue == null) {
            throw new IllegalArgumentException("Domain/Learnlab cannot be null.");
        }
        if (dataset == null) {
            throw new IllegalArgumentException("dataset cannot be null.");
        }

        String displayString = "";
        String oldDomain = null;
        String oldLearnlab = null;
        String domainName = null;
        String learnlabName = null;
        if (!domainLearnlabValue.equals("")) {
            int slash = 0;
            //get domain and learnlab
            if (domainLearnlabValue.contains(SLASH_DELIM)) {
                slash = domainLearnlabValue.indexOf(SLASH_DELIM);
                domainName = domainLearnlabValue.substring(0, slash);
                learnlabName = domainLearnlabValue.substring(slash + 1);
                logger.debug("domainNames: " + domainName);
            } else {
                if (domainLearnlabValue.equals("Other")) {
                    domainName = domainLearnlabValue;
                    learnlabName = "Other";
                    logger.debug("domainNames: " + domainName);
                }
            }

            //Get the domain and learnlab.
            DomainDao domainDao = DaoFactory.DEFAULT.getDomainDao();
            LearnlabDao learnlabDao = DaoFactory.DEFAULT.getLearnlabDao();

            DomainItem domainItem = new DomainItem();
            domainItem = (DomainItem)domainDao.findByName(domainName);

            LearnlabItem learnlabItem = new LearnlabItem();
            learnlabItem = (LearnlabItem)(learnlabDao.findByName(learnlabName));

            if (dataset.getDomain() != null) {
                oldDomain = DaoFactory.DEFAULT.getDomainDao().get(
                        (Integer)dataset.getDomain().getId()).getName();
            }
            if (dataset.getLearnlab() != null) {
                oldLearnlab = DaoFactory.DEFAULT.getLearnlabDao().get(
                        (Integer)dataset.getLearnlab().getId()).getName();
            }
            dataset.setLearnlab(learnlabItem);
            dataset.setDomain(domainItem);
            if (!domainName.equals("Other")) {
                displayString = (String)domainItem.getName() + SLASH_DELIM
                                    + (String)learnlabItem.getName();
            } else {
                displayString = (String)domainItem.getName();
            }
        } else {
            oldDomain = null;
            if (dataset.getDomain() != null) {
                DomainDao domainDao = DaoFactory.DEFAULT.getDomainDao();
                DomainItem domainItem = domainDao.get((Integer)dataset.getDomain().getId());
                oldDomain = domainItem.getName();
            }

            oldLearnlab = null;
            if (dataset.getLearnlab() != null) {
                LearnlabDao learnlabDao = DaoFactory.DEFAULT.getLearnlabDao();
                LearnlabItem learnlabItem = learnlabDao.get((Integer)dataset.getLearnlab().getId());
                oldLearnlab = learnlabItem.getName();
            }

            dataset.setLearnlab(null);
            dataset.setDomain(null);
        }
        DaoFactory.DEFAULT.getDatasetDao().saveOrUpdate(dataset);

        UserLogger.log(dataset, user, UserLogger.DATASET_INFO_MODIFY,
                (((oldDomain != null && oldDomain.equals(domainName))
                        && (oldLearnlab != null && oldLearnlab.equals(learnlabName)))
                        || ((oldDomain != null
                                && oldDomain.equals(domainName)) && (oldLearnlab == null)))
                        ? "No change to Domain/Learnlab"
                        : "Changed Domain/Learnlab from '" + oldDomain + SLASH_DELIM + oldLearnlab
                        + "' to '" + domainLearnlabValue + "'");
        return buildJSONMessage("SUCCESS", "Domain/Learnlab set.", displayString);
    }


    /**
     * Process the saving of a new acknowledgment.
     * @param req the HTTP servlet request
     * @param dataset The dataset to update.
     * @param acknowledgment The acknowledgment of the dataset to set.
     * @param user the UserItem of the user requesting the change (for logging purposes)
     * @return String of the project.
     * @throws JSONException a JSON exception building the JSONObject
     */
    private JSONObject proccessAcknowledgmentSave(HttpServletRequest req,
            DatasetItem dataset, String acknowledgment, UserItem user)
            throws JSONException  {
        if (dataset == null) {
            throw new IllegalArgumentException("dataset cannot be null.");
        }

        String oldAcknowledgment = dataset.getAcknowledgment();
        oldAcknowledgment = oldAcknowledgment == null ? "" : oldAcknowledgment;

        DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
        dataset.setAcknowledgment(acknowledgment);
        datasetDao.saveOrUpdate(dataset);

        //Save the new dataset name in the context after saving to the database.
        //DS648:  (Dataset Name changes not shown in content header
        //or used in saving new files and papers.)
        DatasetContext datasetContext = getDatasetContext(req);
        datasetContext.setDataset(dataset);

        UserLogger.log(dataset, user, UserLogger.DATASET_INFO_MODIFY,
                (oldAcknowledgment.equals(acknowledgment)) ? "No change"
                        : "Changed acknowledgment from '" + oldAcknowledgment
                            + "' to '" + acknowledgment + "'");

        return buildJSONMessage("SUCCESS",
                "Acknowledgment updated.", acknowledgment);
    }

    /**
     * Get a list of all current curriculums.
     * @return a JSONObject of the data.
     * @throws JSONException a JSON exception building the JSONObject
     */
    private JSONObject getCurriculumList() throws JSONException {
        logger.debug("Getting list of curriculums as a JSON string.");

        CurriculumDao curriculumDao = DaoFactory.DEFAULT.getCurriculumDao();
        List currList = curriculumDao.findAll();
        Collections.sort(currList);
        List <JSONObject> jsonCurriculumList = new ArrayList <JSONObject>();

        //insert a "blank" item.
        JSONObject curriculumJSON = new JSONObject();
        curriculumJSON.append("value", "");
        curriculumJSON.append("text", "");
        jsonCurriculumList.add(curriculumJSON);

        for (Iterator it = currList.iterator(); it.hasNext();) {
                CurriculumItem curriculumItem = (CurriculumItem)it.next();
                curriculumJSON = new JSONObject();
                curriculumJSON.append("value", curriculumItem.getCurriculumName());
                curriculumJSON.append("text", curriculumItem.getCurriculumName());
                jsonCurriculumList.add(curriculumJSON);
        }

        JSONObject curriculumListJSON = new JSONObject();
        JSONArray jsonArray = new JSONArray(jsonCurriculumList);
        curriculumListJSON.put("suggestions", jsonArray);
        return curriculumListJSON;
    }

    /**
     * Get a list of all current domain and learnlab pairs.
     * @param domainLearnlabList the list of domain learnlab items
     * @return a JSONObject of the data.
     * @throws JSONException a JSON exception building the JSONObject
     */
    private JSONObject getDomainLearnlabList(List domainLearnlabList) throws JSONException {
        logger.debug("Getting list of domain and learnlab as a JSON string.");

        List <JSONObject> jsonDomainLearnlabList = new ArrayList <JSONObject>();

        //insert a "blank" item.
        JSONObject domainLearnlabJSON = new JSONObject();
        domainLearnlabJSON.append("value", "");
        domainLearnlabJSON.append("text", "");
        jsonDomainLearnlabList.add(domainLearnlabJSON);

        for (Iterator it = domainLearnlabList.iterator(); it.hasNext();) {
                String domainLearnlab = it.next().toString();
                domainLearnlabJSON = new JSONObject();
                domainLearnlabJSON.append("value", domainLearnlab);
                domainLearnlabJSON.append("text", domainLearnlab);
                jsonDomainLearnlabList.add(domainLearnlabJSON);
        }

        JSONObject domainLearnlabListJSON = new JSONObject();
        JSONArray jsonArray = new JSONArray(jsonDomainLearnlabList);
        domainLearnlabListJSON.put("suggestions", jsonArray);
        return domainLearnlabListJSON;
    }

    /**
     * Save the curriculum for the dataset.
     * @param dataset the dataset to set the curriculums on.
     * @param curriculumName the name of the curriculum
     * @param user the UserItem of the user requesting the change (for logging purposes)
     * @return String of success.
     * @throws JSONException a JSON exception building the JSONObject
     */
    private JSONObject proccessCurriculumSave(DatasetItem dataset, String curriculumName,
            UserItem user) throws JSONException {
        if (curriculumName == null) {
            throw new IllegalArgumentException("Curriculum Name cannot be null.");
        }
        if (dataset == null) {
            throw new IllegalArgumentException("dataset cannot be null.");
        }

        String diplayString = "";
        String oldCurriculum = null;
        if (!curriculumName.equals("")) {
            CurriculumItem toSave = null;
            CurriculumDao currDao = DaoFactory.DEFAULT.getCurriculumDao();
            List existingCurriculums = currDao.findAll();
            for (Iterator it = existingCurriculums.listIterator(); it.hasNext();) {
                CurriculumItem existingItem = (CurriculumItem)it.next();
                if (existingItem.getCurriculumName().equals(curriculumName)) {
                    toSave = existingItem;
                }
            }

            if (toSave == null) {
                toSave = new CurriculumItem();
                toSave.setCurriculumName(curriculumName);
                currDao.saveOrUpdate(toSave);
            }

            if (dataset.getCurriculum() != null) {
                oldCurriculum = DaoFactory.DEFAULT.getCurriculumDao().get(
                        (Integer)dataset.getCurriculum().getId()).getCurriculumName();
            }

            dataset.setCurriculum(toSave);
            diplayString = toSave.getCurriculumName();
        } else {
            if (dataset.getCurriculum() != null) {
                oldCurriculum = DaoFactory.DEFAULT.getCurriculumDao().get(
                        (Integer)dataset.getCurriculum().getId()).getCurriculumName();
            }
            dataset.setCurriculum(null);
        }

        DaoFactory.DEFAULT.getDatasetDao().saveOrUpdate(dataset);
        UserLogger.log(dataset, user, UserLogger.DATASET_INFO_MODIFY,
                (curriculumName.equals(oldCurriculum)) ? "No Change for curriculum"
                        : "Changed curriculum from '" + oldCurriculum
                            + "' to '" + curriculumName + "'");

        return buildJSONMessage("SUCCESS", "Curriculum set.", diplayString);
    }

    /**
     * Constant for date formatting definition.
     * SimpleDateFormat is not thread-safe but we will synchronize on this instance
     * and this should be a low-traffic path, will little to no need for concurrency.
     */
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * Save the "date" information for a given dataset.
     * @param startDateString a properly formated date string for the start date.
     * @param endDateString a properly formated date string for the end date.
     * @param dataset the dataset to set the date strings on.
     * @param user the UserItem of the user making the changes (for logging purposes)
     * @return a JSONObject that is the success message.
     * @throws JSONException an exception creating the JSON object.
     */
    private JSONObject processDatesSave(String startDateString, String endDateString,
                DatasetItem dataset, UserItem user) throws JSONException {
        if (dataset == null) {
            throw new IllegalArgumentException("dataset cannot be null.");
        }

        JSONObject message = null;
        try {
            Date startDate = null;
            Date endDate = null;

            if (startDateString != null && !startDateString.equals("")) {
                synchronized (DATE_FORMAT) {
                    startDate = DATE_FORMAT.parse(startDateString);
                }
            }
            if (endDateString != null && !endDateString.equals("")) {
                synchronized (DATE_FORMAT) {
                    endDate = DATE_FORMAT.parse(endDateString);
                }
            }


            if (endDate != null && startDate != null && endDate.before(startDate)) {
                return buildJSONMessage("ERROR", "Start date cannot come after end date.");
            }

            Date oldStartDate = dataset.getStartTime();
            Date oldEndDate = dataset.getEndTime();

            dataset.setStartTime(startDate);
            dataset.setEndTime(endDate);
            DaoFactory.DEFAULT.getDatasetDao().saveOrUpdate(dataset);

            FastDateFormat formatter = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");

            UserLogger.log(dataset, user, UserLogger.DATASET_INFO_MODIFY,
                    "Changed start date from '"
                        + ((oldStartDate == null) ? "null" : formatter.format(oldStartDate))
                    + "' to '"
                        + ((dataset.getStartTime() == null)
                                ? "null" : formatter.format(dataset.getStartTime()))
                    + "' and end date from '"
                        + ((oldEndDate == null) ? "null" : formatter.format(oldEndDate))
                    + "' to '"
                        + ((dataset.getEndTime() == null)
                                ? "null" : formatter.format(dataset.getEndTime()))
                    + "'");

            message = buildJSONMessage("SUCCESS", "Dates updated.",
                    DatasetItem.getDateRangeString(dataset));

            if (startDate == null) {
                message.append("startDate", "");
            } else {
                message.append("startDate", formatter.format(startDate));
            }

            if (endDate == null) {
                message.append("endDate", "");
            } else {
                message.append("endDate", formatter.format(endDate));
            }

        } catch (ParseException parseException) {
            logger.warn(parseException.getMessage(), parseException);
            message = buildJSONMessage("ERROR", "An unexpected error occurred trying to save. "
                    + " The datashop was unable to understand the date format recieved. "
                    + " Please try again and contact the datashop team if this errors persists.");
        }

        if (logger.isDebugEnabled()) { logger.debug(message); }

        return message;
    }

    /**
     * Automatically set the start and end times based on the transactional data.
     * @param dataset the dataset to set the start/end times for
     * @param user the UserItem of the user asking for the change (for logging purposes)
     * @return a JSONObject message of success/failure.
     * @throws JSONException an exception building the JSON object.
     */
    private JSONObject autoSetDates(DatasetItem dataset, UserItem user) throws JSONException {
        JSONObject message = null;
        Date oldStartDate = dataset.getStartTime();
        Date oldEndDate = dataset.getEndTime();
        FastDateFormat formatter = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");

        DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
        dataset = datasetDao.autoSetDates(dataset);

        if (dataset == null) {
            message = buildJSONMessage("ERROR",
                    "Dates did not update, are you sure this dataset has data?.");
        }

        UserLogger.log(dataset, user, UserLogger.DATASET_INFO_MODIFY,
                "Autoset start date from '"
                    + ((oldStartDate == null) ? "null" : formatter.format(oldStartDate))
                + "' to '"
                    + ((dataset.getStartTime() == null)
                            ? "null" : formatter.format(dataset.getStartTime()))
                + "' and end date from '"
                    + ((oldEndDate == null) ? "null" : formatter.format(oldEndDate))
                + "' to '"
                    + ((dataset.getEndTime() == null)
                            ? "null" : formatter.format(dataset.getEndTime()))
                + "'");



        message = buildJSONMessage("SUCCESS", "Dates updated.",
                DatasetItem.getDateRangeString(dataset));
        if (dataset.getStartTime() == null) {
            message.append("startDate", "");
        } else {
            message.append("startDate", formatter.format(dataset.getStartTime()));
        }

        if (dataset.getEndTime() == null) {
            message.append("endDate", "");
        } else {
            message.append("endDate", formatter.format(dataset.getEndTime()));
        }
        return message;
    }

    /**
     * Process the saving of a new citation.
     * @param req the HTTP servlet request
     * @param dataset The dataset to update.
     * @param prevFileId The id of the file that was previously selected.
     * @param fileId The id of the file.
     * @param citation The citation of the paper.
     * @param user the UserItem of the user requesting the change (for logging purposes)
     * @return String of the project.
     * @throws JSONException a JSON exception building the JSONObject
     */
    private JSONObject proccessCitationSave(HttpServletRequest req,
            DatasetItem dataset, int prevFileId, int fileId, String citation, UserItem user)
            throws JSONException  {
        JSONObject message = null;
        if (dataset == null) {
            throw new IllegalArgumentException("dataset cannot be null.");
        }
        logger.debug("prevFileId: " + prevFileId);
        logger.debug("fileId: " + fileId);
        logger.debug("citation: " + citation);

        DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
        PaperDao paperDao = DaoFactory.DEFAULT.getPaperDao();

        dataset = datasetDao.get((Integer)dataset.getId());
        String oldCitation = "";
        if (dataset.getPreferredPaper() != null) {
            oldCitation =
                paperDao.get((Integer)dataset.getPreferredPaper().getId()).getCitation();
        }

        // if citation is null, no file has been selected, set preferred paper to null
        if (citation == null) {
            dataset.setPreferredPaper(null);
            datasetDao.saveOrUpdate(dataset);
         // if citation is empty and file Id is not 0, return error
        } else if ((citation.equals("")) && (fileId > 0) && (prevFileId != 0)) {
           message =  buildJSONMessage("ERROR",
                "You cannot specify a paper as the preferred citation "
                    + "if there is no citation text. "
                    + "Please either enter a citation or select another paper.");

           message.append("fileId", fileId);
           message.append("citation", citation);

           return message;
         // if citation is empty and file Id is 0
        } else if ((citation.equals("")) && (fileId == 0)) {
            dataset.setPreferredPaper(null);
            datasetDao.saveOrUpdate(dataset);
        // if no file is selected, and citation for another file needs to be restored
        } else if ((prevFileId == 0) && (fileId > 0)) {
            dataset.setPreferredPaper(null);
            datasetDao.saveOrUpdate(dataset);

            List<PaperItem> paperList = paperDao.findAll();
            for (PaperItem paper :  paperList) {
                if (paper.getFile().getId().equals(fileId)) {
                    paper.setCitation(citation);
                    paperDao.saveOrUpdate(paper);
                }
            }
        } else {
             // if citation is not null, set the paper citation and preferred paper.
            List<PaperItem> paperList = paperDao.findAll();
            for (PaperItem paper :  paperList) {
                if (paper.getFile().getId().equals(fileId)) {
                    paper.setCitation(citation);
                    paperDao.saveOrUpdate(paper);
                    dataset.setPreferredPaper(paper);
                    datasetDao.saveOrUpdate(dataset);
                }
            }
        }
        //Save the new dataset name in the context after saving to the database.
        //DS648:  (Dataset Name changes not shown in content header
        //or used in saving new files and papers.)
        DatasetContext datasetContext = getDatasetContext(req);
        datasetContext.setDataset(dataset);

        UserLogger.log(dataset, user, UserLogger.DATASET_INFO_MODIFY,
                (oldCitation.equals(citation)) ? "No change"
                        : "Changed citation from '" + oldCitation
                            + "' to '" + citation + "'");

        message = buildJSONMessage("SUCCESS",
                "Preferred Citation updated.");

        if (dataset.getPreferredPaper() == null) {
            message.append("fileId", null);
            message.append("citation", null);
        } else {
            message.append("fileId", fileId);
            message.append("citation", citation);
        }

        return message;
    }

    /**
     * Create a message as a JSON object.
     * @param messageType The type of message ('ERROR', 'SUCCESS', 'UNAUTHORIZED', 'MESSAGE');
     * @param message The text of the message.
     * @return The message as a JSON object.
     * @throws JSONException a JSON exception building the JSONObject
     */
    private JSONObject buildJSONMessage(String messageType, String message)
            throws JSONException {
        return buildJSONMessage(messageType, message, null);
    }

    /**
     * Create a message as a JSON object.
     * @param messageType The type of message ('ERROR', 'SUCCESS', 'UNAUTHORIZED', 'MESSAGE');
     * @param message The text of the message.
     * @param value The value of message, usually refers to an update/delete/save.
     * @return The message as a JSON object.
     * @throws JSONException a JSON exception building the JSONObject
     */
    private JSONObject buildJSONMessage(String messageType, String message, String value)
                throws JSONException {
        logger.debug("Generating a return message as a JSON object.");

        JSONObject messageJSON = new JSONObject();
        messageJSON.append("messageType", messageType);
        messageJSON.append("message", message);
        messageJSON.append("value", value);
        return messageJSON;
    }
}
