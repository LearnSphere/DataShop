/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2008
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.kcmodel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.lang.time.FastDateFormat;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetSystemLogDao;
import edu.cmu.pslc.datashop.dao.DatasetUserLogDao;
import edu.cmu.pslc.datashop.dao.KCModelStepExportDao;
import edu.cmu.pslc.datashop.dao.ModelExportDao;
import edu.cmu.pslc.datashop.dao.SampleMetricDao;
import edu.cmu.pslc.datashop.dao.SkillDao;
import edu.cmu.pslc.datashop.dao.SkillModelDao;
import edu.cmu.pslc.datashop.dao.SubgoalDao;
import edu.cmu.pslc.datashop.dto.ExportCache;
import edu.cmu.pslc.datashop.dto.StepExportRow;
import edu.cmu.pslc.datashop.helper.SystemLogger;
import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.KCModelStepExportItem;
import edu.cmu.pslc.datashop.item.SkillItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.item.SubgoalItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.item.SkillModelItem.AbstractKcmComparator;
import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.servlet.webservices.WebServiceException;
import edu.cmu.pslc.datashop.util.StringUtils;

/**
 * Business tier class for management of KC Models.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 15763 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2018-12-16 15:57:08 -0500 (Sun, 16 Dec 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public class KCModelHelper {

    /** Debug logging. */
    private static Logger logger = Logger.getLogger(KCModelHelper.class);

    /** Regular expression to find KC headers */
    private static final String KC_MODEL_HEADER_REG_EXP =
            Pattern.compile("KC\\s*\\((.+)\\)",
                            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE).toString();

    /** Regular expression to find KC Model headers: Backward Compatibility. */
    private static final String KC_MODEL_HEADER_REG_EXP_OLD =
            Pattern.compile("KC Model \\((.+)\\)",
                            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE).toString();

    /** Regular expression of characters not allowed in KCM names. */
    private static final String REGEX_KCM_NOT_ALLOWED_CHARS = "[^\\sA-Za-z0-9_-]";

    /** The SkillModelDao, duH! */
    private SkillModelDao skillModelDao;
    /** The SkillDao, duH! */
    private SkillDao skillDao;
    /** The SkillMetricDao, duH! */
    private SampleMetricDao sampleMetricDao;

    /** Returns skillModelDao. @return Returns the skillModelDao. */
    public SkillModelDao getSkillModelDao() {
        return skillModelDao;
    }

    /** Set skillModelDao. @param skillModelDao The skillModelDao to set. */
    public void setSkillModelDao(SkillModelDao skillModelDao) {
        this.skillModelDao = skillModelDao;
    }

    /** Returns skillDao. @return Returns the skillDao. */
    public SkillDao getSkillDao() {
        return skillDao;
    }

    /** Set skillDao. @param skillDao The skillDao to set. */
    public void setSkillDao(SkillDao skillDao) {
        this.skillDao = skillDao;
    }

    /** Returns sampleMetricDao. @return Returns the sampleMetricDao. */
    public SampleMetricDao getSampleMetricDao() {
        return sampleMetricDao;
    }

    /** Set sampleMetricDao. @param sampleMetricDao The sampleMetricDao to set. */
    public void setSampleMetricDao(SampleMetricDao sampleMetricDao) {
        this.sampleMetricDao = sampleMetricDao;
    }

    /**
     * Gets a list of properly sorted skill models.  The list is sorted to have
     * the owner's first, then other models, then auto-generated ones and alpha
     * sort in each of those categories.
     * @param dataset The dataset to get skill models for.
     * @param owner The user who's models should come first.
     * @return a sorted list of skill model items.
     */
    public List <SkillModelItem> getModelList(DatasetItem dataset, UserItem owner) {
        List <SkillModelItem> models = skillModelDao.find(dataset);
        List <SkillModelItem> myModels = new ArrayList <SkillModelItem>();
        List <SkillModelItem> autoGenModels = new ArrayList <SkillModelItem>();
        List <SkillModelItem> otherModels = new ArrayList <SkillModelItem>();

        //sort the models into 3 types: my, auto-gen and others
        for (SkillModelItem model : models) {
            if (SkillModelItem.SOURCE_AUTO_GEN.equals(model.getSource())) {
                autoGenModels.add(model);
            } else if (model.getOwner() == null) {
                otherModels.add(model);
            } else if (owner.equals(model.getOwner())) {
                myModels.add(model);
            } else {
                otherModels.add(model);
            }
        }

        //now sort each of those lists by model name
        Collections.sort(myModels);
        Collections.sort(autoGenModels);
        Collections.sort(otherModels);

        //append the lists together to one big single list
        myModels.addAll(otherModels);
        myModels.addAll(autoGenModels);

        return myModels;
    }

    /**
     * Gets a list of skill models sorted by given sort by option.
     * @param dataset the given dataset
     * @param kcmSortBy the property of the KC Model to sort by
     * @param kcmSortAscendingFlag indicates whether to sort ascending or not
     * @param kcmGroupByNumObservations indicates whether to group by the number of observations
     * @return a list of skill model items sorted by given sort by option
     */
    public List <SkillModelItem> getModelListSorted(DatasetItem dataset,
                                                    String kcmSortBy,
                                                    Boolean kcmSortAscendingFlag,
                                                    Boolean kcmGroupByNumObservations) {
        // Get a list of skill models pre-ordered by number of observations or name
        List <SkillModelItem> kcmList = kcmGroupByNumObservations
                ? skillModelDao.findOrderByNumObservations(dataset)
                : skillModelDao.findOrderByName(dataset);

        // Choose a comparator according to the sort-by flag
        AbstractKcmComparator comparator;
        if (kcmSortBy.equals(KCModelContext.SORT_BY_AIC)) {
            comparator = new SkillModelItem.AicComparator();
        } else if (kcmSortBy.equals(KCModelContext.SORT_BY_BIC)) {
            comparator = new SkillModelItem.BicComparator();
        } else if (kcmSortBy.equals(KCModelContext.SORT_BY_STUDENT_STRATIFIED_CV_RMSE)) {
            comparator = new SkillModelItem.CvStudentStratifiedRmseComparator();
        } else if (kcmSortBy.equals(KCModelContext.SORT_BY_ITEM_STRATIFIED_CV_RMSE)) {
            comparator = new SkillModelItem.CvStepStratifiedRmseComparator();
        } else if (kcmSortBy.equals(KCModelContext.SORT_BY_UNSTRATIFIED_CV_RMSE)) {
            comparator = new SkillModelItem.CvUnstratifiedRmseComparator();
        } else if (kcmSortBy.equals(KCModelContext.SORT_BY_OBS_UNSTRATIFIED)) {
            comparator = new SkillModelItem.CvObsUnstratifiedComparator();
        } else if (kcmSortBy.equals(KCModelContext.SORT_BY_NAME)) {
            comparator = new SkillModelItem.KCModelNameComparator();
        } else if (kcmSortBy.equals(KCModelContext.SORT_BY_NUM_KCS)) {
            comparator = new SkillModelItem.NumKcsComparator();
        } else if (kcmSortBy.equals(KCModelContext.SORT_BY_OBS)) {
            comparator = new SkillModelItem.ObsWithKcsComparator();
        } else if (kcmSortBy.equals(KCModelContext.SORT_BY_DATE)) {
            comparator = new SkillModelItem.DateCreatedComparator();
        } else if (kcmSortBy.equals(KCModelContext.SORT_BY_CREATOR)) {
            comparator = new SkillModelItem.CreatorComparator();
        } else {
            comparator = new SkillModelItem.AicComparator();
        }

        // Modify the sorting behavior if necessary
        comparator.setGroupByNumObservations(kcmGroupByNumObservations);
        comparator.setReversed(!kcmSortAscendingFlag);

        // Sort the list and return the result
        Collections.sort(kcmList, comparator);
        return kcmList;
    }

    /**
     * Get a sorted list of names for a give skill model.
     * @param model the model to get skill names for.
     * @return A list of strings that are the skills in the model.
     */
    public List <String> getSkillNames(SkillModelItem model) {
        model = skillModelDao.get((Long)model.getId());
        List <String> skillNames = new ArrayList <String> ();
        for (Object skill : model.getSkillsExternal()) {
            skillNames.add(((SkillItem)skill).getSkillName());
        }
        Collections.sort(skillNames);
        /* This is not really needed.  Its here just in case the
         * the number of skills is not set on the model item.
         * Putting this here is not wasting very much time as we have
         * the list of skills anyways. */
        int numSkillNames = skillNames.size();
        Integer numSkills = model.getNumSkills();
        if (numSkills == null || numSkills != numSkillNames) {
            model.setNumSkills(numSkillNames);
            skillModelDao.saveOrUpdate(model);
        }
        return skillNames;
    }

    /**
     * Get the display text for a given skill model owner.
     * @param model The model to get the owner name for.
     * @return A String of the formated name or the user_id if the first and last
     * names are null
     */
    public String getOwnerDisplayName(SkillModelItem model) {
        model = skillModelDao.get((Long)model.getId());
        UserItem owner = model.getOwner();
        String formattedName;
        if (owner != null) {
            formattedName = (String)owner.getId();
            if (owner.getFirstName() != null && !owner.getFirstName().equals("")) {
                if (owner.getLastName() != null && !owner.getLastName().equals("")) {
                    // First and last name exist
                    formattedName = owner.getFirstName().substring(0, 1).toUpperCase()
                        + ". " + owner.getLastName();
                }
            }

        } else {
            formattedName = "system";
        }
        return formattedName;
    }

    /**
     * Determines if the user is the owner of the given model.
     * @param user UserItem to check.
     * @param model SkillModelItem to check
     * @return true if the user is the owner, false otherwise.
     */
    public Boolean isModifyAuthorized(UserItem user, Long modelId) {

        SkillModelItem model = skillModelDao.get(modelId);

        if (model != null && user.getAdminFlag()) {
            return true;
        }

        if (model == null || model.getOwner() == null) {
            return false;
        }

        return (user.getId().compareTo(model.getOwner().getId()) == 0);
    }

    /**
     * Get a list of skill model items from a JSON array of skill model IDs.
     * @param modelsJSON JSON array of skill model database IDs.
     * @return sorted list of skill model items.
     */
    public List <SkillModelItem> getModelList(JSONArray modelsJSON) {
        List <SkillModelItem> skillModels = new ArrayList <SkillModelItem> ();

        try {
            for (int i = 0, n = modelsJSON.length(); i < n; i++) {
                Long modelId = Long.valueOf((String)modelsJSON.get(i));
                SkillModelItem model = skillModelDao.get(modelId);
                if (model != null) { skillModels.add(model); }
            }
        } catch (JSONException jsonException) {
            logger.error(
                "JSON Exception caught trying to parse the selected skill models for export",
                jsonException);
        }
        Collections.sort(skillModels);
        return skillModels;
    }

    /**
     * Get a batch of steps for exporting.
     * @param dataset the dataset to get steps for.
     * @param offset the current offset.
     * @param batchSize the size of the batch to get.
     * @return List of steps to process.
     */
    public List <StepExportRow> getStepBatch(DatasetItem dataset, int offset, int batchSize) {
        KCModelStepExportDao dao = DaoFactory.DEFAULT.getKCModelStepExportDao();
        List <StepExportRow> stepInfo = dao.findStepExportRows(dataset, offset, batchSize);

        if ((stepInfo == null) || (stepInfo.size() == 0)) {
            // This shouldn't happen!
            logger.debug("getStepBatch: new method didn't find anything...");
            ModelExportDao modelExportDao = DaoFactory.DEFAULT.getModelExportDao();
            stepInfo = modelExportDao.getStepExport(dataset, batchSize, offset);
        }

        return stepInfo;
    }

    /**
     * Get a mapping between steps and skills in the given dataset for the given set of models.
     * @param dataset the dataset to get the mapping for.
     * @param skillModelList List of skill models to get a mapping for.
     * @return a MultiMap where the key is the step_id and the value is a Collection of skill_ids
     * mapped to the given value.
     */
    public MultiMap getStepSkillMap(DatasetItem dataset, List <SkillModelItem> skillModelList) {
        ModelExportDao modelExportDao = DaoFactory.DEFAULT.getModelExportDao();
        return modelExportDao.getStepSkillMapping(dataset, skillModelList);
    }

    /**
     * Get the column headers for step mapping export.
     * @param skillModelList List of skill models being exported.
     * @return StrinBuffer of the tab delimited headers.
     */
    public StringBuffer getStepMappingHeaders(List <SkillModelItem> skillModelList) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Step ID" + "\t");
        buffer.append("Problem Hierarchy" + "\t");
        buffer.append("Problem Name" + "\t");
        buffer.append("Max Problem View" + "\t");
        buffer.append("Step Name" + "\t");
        buffer.append("Avg Incorrects" + "\t");
        buffer.append("Avg Hints" + "\t");
        buffer.append("Avg Corrects" + "\t");
        buffer.append("% First Attempt Incorrects" + "\t");
        buffer.append("% First Attempt Hints" + "\t");
        buffer.append("% First Attempt Corrects" + "\t");
        buffer.append("Avg Step Duration (sec)" + "\t");
        buffer.append("Avg Correct Step Duration (sec)" + "\t");
        buffer.append("Avg Error Step Duration (sec)" + "\t");
        buffer.append("Total Students" + "\t");
        buffer.append("Total Opportunities" + "\t");

        for (SkillModelItem model : skillModelList) {
            model = skillModelDao.get((Long)model.getId());
            Long numColumns = sampleMetricDao.getMaxDistinctSkillsAcrossSteps(
                    model.getDataset(), model);
            for (int i = 0; i < numColumns; i++) {
                buffer.append("KC (" + model.getSkillModelName() + ")\t");
            }
        }

        buffer.append("KC (new KC model name)" + "\t");
        return buffer;
    }

    /**
     * Process a single row of the step export into a tab delimited string.
     * @param row a StepExportRow which contains the step information.
     * @param skillMapping a multi map containing the information about a given step.
     * @param cache ExportCache to prevent multiple looks-ups when feasible.
     * @param skillModelList the list of skill models selected to export.
     * @return StringBuffer of the tab delimited information.
     */
    public StringBuffer processStepRow(StepExportRow row,
            MultiMap skillMapping, ExportCache cache, List <SkillModelItem> skillModelList) {

        StringBuffer buffer = new StringBuffer();

        //build the columns
        for (String column : row.getColumns()) { buffer.append(column + "\t"); }

        //resort the list of skills attached to this step by model.
        Collection skillIds = (Collection)skillMapping.get(row.getStepId());
        MultiValueMap skillToModelMap = new MultiValueMap();
        if (skillIds != null) {
            for (Object skillId : skillIds) {
                SkillItem skill = cache.getSkill((Long)skillId);
                if (skill == null) {
                    skill = skillDao.get((Long)skillId);
                    cache.addToSkills(skill);
                }
                skillToModelMap.put(skill.getSkillModel().getId(), skill);
            }
        }

        Collections.sort(skillModelList);
        for (SkillModelItem model : skillModelList) {
            int numColumnsUsed = 0;
            Long numColumns = sampleMetricDao.getMaxDistinctSkillsAcrossSteps(model.getDataset(),
                                                                              model);

            if (skillToModelMap.getCollection(model.getId()) != null) {
                List skills = new ArrayList();
                skills.addAll(skillToModelMap.getCollection(model.getId()));
                Collections.sort(skills);
                for (Object skillObj : skills) {
                    SkillItem skill = (SkillItem)skillObj;
                    buffer.append(skill.getSkillName() + "\t");
                    numColumnsUsed++;
                }
            }

            //add the blank columns hanging out at the end.
            while (numColumnsUsed < numColumns) {
                buffer.append(" \t"); //keep the space to help avoid parse errors.
                numColumnsUsed++;
            }
        }

        return buffer;
    }

    /**
     * Get the size of the step export for a given dataset.
     * @param dataset The dataset to get the number of steps for.
     * @return Long of the number of steps.
     */
    public Long getStepExportSize(DatasetItem dataset) {
        return sampleMetricDao.getTotalUniqueSteps(dataset);
    }

    /**
     * Delete a skill model if the current user is authorized.
     * @param currentUser the current user.
     * @param modelId the skill model id to delete.
     * @return boolean of true if deleted, false otherwise.
     */
    public Boolean deleteModel(UserItem currentUser, Long modelId) {
        SkillModelItem model = skillModelDao.get(modelId);
        if (model != null && isModifyAuthorized(currentUser, modelId)) {
            long startTime = System.currentTimeMillis();

            long elapsedTime = System.currentTimeMillis() - startTime;
            String logMsg = model.getDataset() + " " + currentUser
                    + " " + UserLogger.MODEL_DELETE + " Deleted KCM '"
                        + model.getSkillModelName() + "' (" + modelId + ")";
            DatasetItem modelDataset = model.getDataset();

            skillModelDao.delete(model);
            UserLogger.log(modelDataset, currentUser,
                    UserLogger.MODEL_DELETE, logMsg);
            SystemLogger.log(modelDataset, null, UserLogger.MODEL_DELETE,
                    logMsg,
                    true, elapsedTime);

            return true;
        } else {
            return false;
        }
    }

    /**
     * Rename a skill model if the current user is authorized.
     * @param currentUser the current user.
     * @param modelId ID of the skill model to renamed.
     * @param newName the new name of model.
     * @return boolean of true if deleted, false otherwise.
     * @throws JSONException a JSON exception building the JSONObject
     */
    public JSONObject renameModel(UserItem currentUser, Long modelId, String newName)
        throws JSONException {

        SkillModelItem model = skillModelDao.get(modelId);

        if (isModifyAuthorized(currentUser, (Long)model.getId())) {

            newName = newName.trim();
            if (model.getSkillModelName().equals(newName)) {
                return buildJSONMessage("MESSAGE",
                        "Model is already named '" + model.getSkillModelName() + "'.", null);
            }

            if (newName == null || newName.equals("")) {
                return buildJSONMessage("ERROR",
                        "Model name cannot be blank", null);
            }

            // Look for invalid characters in new name.
            Pattern pattern = Pattern.compile(REGEX_KCM_NOT_ALLOWED_CHARS);
            Matcher matcher = pattern.matcher(newName);
            if (matcher.find()) {
                return buildJSONMessage("ERROR", "Invalid character(s) found in model name \""
                                        + newName
                                        + "\". Valid characters include space, dash, "
                                        + "underscore, letters and numbers.", null);
            }

            List <SkillModelItem> modelList = skillModelDao.find(model.getDataset());
            modelList.remove(model);
            for (SkillModelItem existingModel : modelList) {
                if (existingModel.getSkillModelName().equals(newName)) {
                    return buildJSONMessage("ERROR",
                        "A model with name '" + newName
                        + "' already exists in this dataset, please choose a different name", null);
                }
            }

            String oldName = model.getSkillModelName();
            model.setSkillModelName(newName);
            model.setModifiedTime(new Date());
            skillModelDao.saveOrUpdate(model);

            UserLogger.log(model.getDataset(), currentUser,
                    UserLogger.MODEL_RENAME, "Renamed KCM (" + model.getId() + ") '"
                    + oldName + "' to '" + newName + "'");
            SystemLogger.log(model.getDataset(), model, UserLogger.MODEL_RENAME,
                    "Renamed KCM (" + model.getId() + ") '"
                    + oldName + "' to '" + newName + "'", true, null);
            return buildJSONMessage("SUCCESS",
                    "Model name updated to '" + model.getSkillModelName() + "'.", newName);

        } else {
            return buildJSONMessage("UNAUTHORIZED",
                    "You are not authorized to modify this model name", null);
        }
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
        messageJSON.put("messageType", messageType);
        messageJSON.put("message", message);
        messageJSON.put("value", value);
        return messageJSON;
    }

    /**
     * Performs a verification on the upload model file to check for errors before
     * importing.
     * @param importFile the file that is being imported
     * @param user user doing the importing
     * @param dataset dataset the model is being imported to
     * @return JSONObejct with information from the file.
     * @throws JSONException error creating the JSON return object
     * @throws IOException Problem accessing the file.
     */
    public JSONObject verifyModelFile(File importFile, UserItem user, DatasetItem dataset)
            throws JSONException, IOException {
        String prefix = "verifyModelFile(" + (String)user.getId() + ", " + dataset.getId() + "): ";
        JSONObject returnJSON = new JSONObject();
        BufferedReader in = null;
        try {
            logger.info(prefix + "Starting.");
            in = new BufferedReader(new InputStreamReader(new FileInputStream(importFile), "UTF8"));

            String line = in.readLine();
            // eat any whitespace at the top of the file.
            while (line.length() == 0 || !line.startsWith("")) { line = in.readLine(); }

            //walk through the line looking for the column headings.
            String[] lineSplit = line.split("\t");
            int position = 0;
            boolean stepIDFound = false;

            String msg = "Started verifying KCM file: " + importFile.getName() + ".";
            UserLogger.log(dataset, user, UserLogger.MODEL_VERIFY, msg);
            logger.info(prefix + msg);

            MultiValueMap modelsAndPositions = new MultiValueMap();
            for (String string : lineSplit) {
                string = string.trim();
                if (string.equals("Step ID")) { stepIDFound = true; }
                if (string.matches(KC_MODEL_HEADER_REG_EXP)) {
                    String model = string.replaceAll(KC_MODEL_HEADER_REG_EXP, "$1");
                    modelsAndPositions.put(model, position);
                } else if (string.matches(KC_MODEL_HEADER_REG_EXP_OLD)) {
                    logger.debug("String matches OLD KC Model header: " + string);
                    String model = string.replaceAll(KC_MODEL_HEADER_REG_EXP_OLD, "$1");
                    modelsAndPositions.put(model, position);
                }
                position++;
            }

            if (!stepIDFound) {
                returnJSON.put("outcome", "ERROR");
                returnJSON.put("message", "The step identifier column \"Step ID\" was not found. "
                        + "This column is required for a knowledge component model import.");

                msg = "Invalid KCM file: " + importFile.getName() + ".";
                UserLogger.log(dataset, user, UserLogger.MODEL_VERIFY, msg);
                logger.info(prefix + msg + " Step ID column not found.");
                return returnJSON;
            }

            if (modelsAndPositions.keySet().size() < 1) {
                returnJSON.put("outcome", "ERROR");
                returnJSON.put("message", "No knowledge component models found to import. "
                        + "Models must have a heading with the format \"KC ([name])\" "
                        + "where [name] is replaced with the model name.");

                msg = "Found no models to verify in file: " + importFile.getName() + ".";
                UserLogger.log(dataset, user, UserLogger.MODEL_VERIFY, msg);
                logger.info(prefix + msg);
                return returnJSON;
            }

            String modelName = "";
            Pattern pattern = Pattern.compile(REGEX_KCM_NOT_ALLOWED_CHARS);
            Matcher matcher = pattern.matcher(modelName);

            // Ensure new model names are valid.
            for (Object model : modelsAndPositions.keySet()) {
                modelName = (String)model;
                matcher = pattern.matcher(modelName);
                if (matcher.find()) {
                    returnJSON.put("outcome", "ERROR");
                    returnJSON.put("message", "Invalid character(s) found in model name \""
                                   + modelName
                                   + "\". Valid characters for KC model names include space, "
                                   + "dash, underscore, letters and numbers.");

                    msg = "Found invalid KC Model name(s) in file: " + importFile.getName() + ".";
                    UserLogger.log(dataset, user, UserLogger.MODEL_VERIFY, msg);
                    logger.info(prefix + msg);
                    return returnJSON;
                }
            }

            List <SkillModelItem> existingSkillModels = skillModelDao.find(dataset);
            JSONArray existingModelNames = new JSONArray();
            //built a list of all existing skill model names
            for (SkillModelItem modelItem : existingSkillModels) {
                existingModelNames.put(modelItem.getSkillModelName());
            }

            //figure out the specifics for each new model including column positions,
            //whether it exists, etc.
            JSONArray modelsArray = new JSONArray();
            for (Object model : modelsAndPositions.keySet()) {
                JSONObject modelJSON = new JSONObject();

                modelJSON.put("name", model);
                modelJSON.put("positions", modelsAndPositions.getCollection(model));
                modelName = (String)model;
                if (modelName.length() > SkillModelItem.MODEL_NAME_MAX_LENGTH) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("modelName is " + modelName
                                + " and its too long: " + modelName.length());
                    }
                    modelJSON.put("nameLength", modelName.length());
                }
                for (SkillModelItem modelItem : existingSkillModels) {
                    if (modelItem.getSkillModelName().equals(model)) {
                        modelJSON.put("id", modelItem.getId());
                        modelJSON.put("isOwner", (modelItem.getOwner() == null)
                                ? false : (user.equals(modelItem.getOwner())));
                        //store the existing model names for use in the client later.
                        modelJSON.put("existingModels", existingModelNames);
                    }
                }
                modelsArray.put(modelJSON);
            }
            returnJSON.put("models", modelsArray);
            returnJSON.put("outcome", "SUCCESS");
            msg = "Successfully verified KCM import file: " + importFile.getName() + ".";

            // Check for "extra" info... rows with more columns than expected.
            line = in.readLine();
            int numHeaders = lineSplit.length;
            int count = 0;
            int missingStepIds = 0;
            boolean extraColsFound = false;
            StringBuffer warningMsg = new StringBuffer();
            SubgoalDao subgoalDao = DaoFactory.DEFAULT.getSubgoalDao();
            while (line != null) {
                lineSplit = line.split("\t");
                if (!extraColsFound && (lineSplit.length > numHeaders)) {
                    returnJSON.put("outcome", "WARNING");
                    warningMsg.append("A line was found with extra data. ")
                        .append("It has ").append(lineSplit.length).append(" columns while ")
                        .append("there are only ").append(numHeaders).append(" headers. This ")
                        .append("extra data will be ignored.");
                    msg = "Warning: too many columns in line " + (count + 1) + " of file: "
                        + importFile.getName() + ".";
                    extraColsFound = true;
                } else if (lineSplit.length > 0) {
                    String stepTag = lineSplit[0].trim();
                    //skip blank lines and the headers
                    if (stepTag != null && !stepTag.equals("") && !stepTag.equals("Step ID")) {
                        SubgoalItem subgoal = subgoalDao.find(dataset, stepTag);
                        if (subgoal == null) {
                            missingStepIds++;
                        }
                    }
                    count++;
                    line = in.readLine();
                } else {
                    count++;
                    line = in.readLine();
                }
            }

            if (missingStepIds > 0) {
                if (warningMsg.length() > 0) {
                    warningMsg.append(" Also, ");
                }
                warningMsg.append(missingStepIds)
                    .append(" rows will be skipped as the matching step was not found."); 
                msg = "Warning: " + missingStepIds + " rows do not have matching step.";
            }

            if (warningMsg.length() > 0) {
                returnJSON.put("outcome", "WARNING");
                returnJSON.put("message", warningMsg.toString());
            }

            UserLogger.log(dataset, user, UserLogger.MODEL_VERIFY, msg);
            logger.info(prefix + msg);
        } finally {
            in.close();
        }
        return returnJSON;
    }

    /**
     * Performs a verification on the KCM import data to check for errors before
     * importing. Used for web service.
     * @param importData the data that is being imported
     * @param user user doing the importing
     * @param dataset dataset the model is being imported to
     * @return JSONObejct with information about verification.
     * @throws WebServiceException Problem accessing the data.
     */
    public JSONObject verifyModelDataForWebService(String importData, UserItem user, DatasetItem dataset)
            throws WebServiceException {
        String prefix = "verifyModelFile(" + (String)user.getId() + ", " + dataset.getId() + "): ";
        /** Date format. */
        FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");
        String msg = "";
        // Check whether there already is an import running for this dataset.
        DatasetUserLogDao logDao = DaoFactory.DEFAULT.getDatasetUserLogDao();
        Date lastStarted = logDao.areKcmsImporting(dataset);
        if (lastStarted != null) {
                msg = "KCM import already running"
                                + " (since " + DATE_FORMAT.format(lastStarted) + ")."
                                + " Please try importing your model again after it has completed.";
                logger.info(prefix + msg);
                throw new WebServiceException(WebServiceException.KCM_INPUT_SERVICE_BUSY, msg);
        }
        String[] lines = importData.split("\\r?\\n");
        //walk through the line looking for the column headings.
        String[] headers = lines[0].split("\t");
        boolean stepIDFound = false;
        msg = "Started verifying KCM data via web service. ";
        UserLogger.log(dataset, user, UserLogger.MODEL_VERIFY, msg);
        logger.info(prefix + msg);
        int position = 0;
        MultiValueMap modelsAndPositions = new MultiValueMap();
        for (String string : headers) {
                string = string.trim();
                if (string.equals("Step ID")) { stepIDFound = true; }
                if (string.matches(KC_MODEL_HEADER_REG_EXP)) {
                    String model = string.replaceAll(KC_MODEL_HEADER_REG_EXP, "$1");
                    modelsAndPositions.put(model, position);
                } else if (string.matches(KC_MODEL_HEADER_REG_EXP_OLD)) {
                    logger.debug("String matches OLD KC Model header: " + string);
                    String model = string.replaceAll(KC_MODEL_HEADER_REG_EXP_OLD, "$1");
                    if (modelsAndPositions.containsKey(model)) {
                            msg = "Bad file format. Duplicate KCM names found in import file.";
                            UserLogger.log(dataset, user, UserLogger.MODEL_VERIFY, msg);
                            logger.info(prefix + msg);
                            throw new WebServiceException(WebServiceException.INVALID_KCM_INPUT_FORMAT, msg);
                    }
                    modelsAndPositions.put(model, position);
                }
                position++;
        }

        if (!stepIDFound) {
                msg = "Bad file format. The step identifier column \"Step ID\" was not found.";
                UserLogger.log(dataset, user, UserLogger.MODEL_VERIFY, msg);
                logger.info(prefix + msg);
                throw new WebServiceException(WebServiceException.INVALID_KCM_INPUT_FORMAT, msg);
        }
        if (modelsAndPositions.keySet().size() < 1) {
                msg = "Bad file format. No KCM found to import.";
                UserLogger.log(dataset, user, UserLogger.MODEL_VERIFY, msg);
                logger.info(prefix + msg);
                throw new WebServiceException(WebServiceException.INVALID_KCM_INPUT_FORMAT, msg);
        }
        Pattern pattern = Pattern.compile(REGEX_KCM_NOT_ALLOWED_CHARS);
        // Ensure new model names are valid.
        for (Object model : modelsAndPositions.keySet()) {
                Matcher matcher = pattern.matcher((String)model);
                if (matcher.find()) {
                        msg = "Bad file format. Invalid character(s) found in model name.";
                        UserLogger.log(dataset, user, UserLogger.MODEL_VERIFY, msg);
                        logger.info(prefix + msg);
                        throw new WebServiceException(WebServiceException.INVALID_KCM_INPUT_FORMAT, msg);
                }
        }

        List<String> ignoredModels = new ArrayList<String>();

        List <SkillModelItem> existingSkillModels = skillModelDao.find(dataset);
        JSONArray existingModelNames = new JSONArray();
        //built a list of all existing skill model names
        for (SkillModelItem modelItem : existingSkillModels) {
            existingModelNames.put(modelItem.getSkillModelName());
        }
        JSONArray modelsArray = new JSONArray();
        for (Object model : modelsAndPositions.keySet()) {
                String modelName = (String)model;
                if (modelName.length() > SkillModelItem.MODEL_NAME_MAX_LENGTH) {
                        msg = "Bad file format. Model name is " + modelName + ", and it's too long: " + modelName.length();
                        UserLogger.log(dataset, user, UserLogger.MODEL_VERIFY, msg);
                        logger.info(prefix + msg);
                        throw new WebServiceException(WebServiceException.INVALID_KCM_INPUT_FORMAT, msg);
                }
                Boolean ignoreModel = false;
                for (SkillModelItem modelItem : existingSkillModels) {
                    // If a model already exists, ignore it. In the UI we give them the option. Assume it here.
                    if (modelItem.getSkillModelName().equals(modelName)) {
                        msg = "Model name is " + modelName + ", and it exists already. Ignoring it.";
                        UserLogger.log(dataset, user, UserLogger.MODEL_VERIFY, msg);
                        logger.info(prefix + msg);
                        ignoreModel = true;
                        ignoredModels.add(modelName);
                    }
                }
                if (!ignoreModel) {
                    JSONObject modelJSON = new JSONObject();
                    try {
                        modelJSON.put("name", modelName);
                        modelJSON.put("positions", modelsAndPositions.getCollection(model));
                        modelJSON.put("action", "Import New");
                    } catch (JSONException ex) {
                        msg = "JSONException thrown while making modelJSON object.";
                        UserLogger.log(dataset, user, UserLogger.MODEL_VERIFY, msg);
                        logger.info(prefix + msg);
                        WebServiceException.unknownErrorException();
                    }
                    modelsArray.put(modelJSON);
                }
        }

        if (modelsArray.length() == 0) {
            msg = "No models to import. Ignoring duplicate(s): " + StringUtils.join(", ", ignoredModels);
            UserLogger.log(dataset, user, UserLogger.MODEL_VERIFY, msg);
            logger.info(prefix + msg);
            throw new WebServiceException(WebServiceException.INVALID_KCM_INPUT_FORMAT, msg);
        }

        JSONObject returnJSON = new JSONObject();
        try {
                returnJSON.put("models", modelsArray);
        } catch (JSONException ex) {
                msg = "JSONException thrown while making retrunJSON object.";
                UserLogger.log(dataset, user, UserLogger.MODEL_VERIFY, msg);
                logger.info(prefix + msg);
                WebServiceException.unknownErrorException();
        }
        //verify step ids
        msg = "Getting subgoal guid on dataset (" + dataset.getId() + ")";
        UserLogger.log(dataset, user, UserLogger.MODEL_VERIFY, msg);
        logger.info(prefix + msg);
        Integer offset = 0;
        final int limit = 5000;
        Vector<String> guids = new Vector<String>();
        SubgoalDao subgoalDao = DaoFactory.DEFAULT.getSubgoalDao();
        List <SubgoalItem> subgoals = subgoalDao.find(dataset, limit, offset);
        while (subgoals.size() > 0) {
                for (SubgoalItem subgoalItem : subgoals) {
                        guids.add(subgoalItem.getGuid());
                }
                offset += limit;
                subgoals = subgoalDao.find(dataset, limit, offset);
        }
        //walk through the line looking for stepids.
        for (int i = 1; i < lines.length; i++) {
                String[] eles = lines[i].split("\t");
                // Guard against empty lines
                if ((!eles[0].trim().equals("")) && (!guids.contains(eles[0].trim()))) {
                        msg = "Invalid step ID found: " + eles[0];
                        UserLogger.log(dataset, user, UserLogger.MODEL_VERIFY, msg);
                        logger.info(prefix + msg);
                        throw new WebServiceException(WebServiceException.INVALID_STEP_ID, msg);
                }
        }
        //all verified successfully
        msg = "Successfully verified KCM import data.";
        UserLogger.log(dataset, user, UserLogger.MODEL_VERIFY, msg);
        logger.info(prefix + msg);
        return returnJSON;
    }

    /**
     * Performs import KCM data which is saved to a tmp file. Used for web service.
     * @param the temporary importFile that contains the data to be imported
     * @param user user doing the importing
     * @param dataset dataset the model is being imported to
     * @throws WebServiceException Problem importing the data.
     */
    public void importModelDataForWebService(DatasetItem dataset, UserItem user, File importFile, JSONObject modelsJSON, String aggSpFilePath, String ssssDir)
                    throws WebServiceException {
            logger.debug("Importing skill model for dataset(" + dataset.getId() + ").");
            KCModelContext kcModelContext = new KCModelContext();
            kcModelContext.setImportFile(importFile);
            //check if we are creating a new thread
            KCModelImportControllerBean importControllerBean;
            synchronized (kcModelContext) {
                    importControllerBean = HelperFactory.DEFAULT.getKCModelImportControllerBean();
                    kcModelContext.setImportBean(importControllerBean);
            }

            logger.info("handleImport: starting new thread for file: " + importFile.getName());
            importControllerBean.setAttributes(dataset, user, importFile, modelsJSON,
                            aggSpFilePath, ssssDir);
            new Thread(importControllerBean).start();
    }
}