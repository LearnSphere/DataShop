package edu.cmu.pslc.datashop.servlet.workflows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.json.JSONArray;
import org.json.JSONException;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.WorkflowDao;
import edu.cmu.pslc.datashop.dao.WorkflowPaperDao;
import edu.cmu.pslc.datashop.dao.WorkflowTagDao;
import edu.cmu.pslc.datashop.dao.WorkflowTagMapDao;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.workflows.WorkflowItem;
import edu.cmu.pslc.datashop.workflows.WorkflowPaperItem;
import edu.cmu.pslc.datashop.workflows.WorkflowTagItem;
import edu.cmu.pslc.datashop.workflows.WorkflowTagMapId;
import edu.cmu.pslc.datashop.workflows.WorkflowTagMapItem;

public class WorkflowPropertiesHelper {

    public static final Pattern WORKFLOW_TAG_PATTERN = Pattern.compile("^[ A-Za-z0-9-_@*+.$]+$");

    /**
     * Constant used to define long-running workflow, in milliseconds (5 minutes).
     */
    private static final long LONG_RUNNING_THRESHOLD = 300000L;

    public static final String WORKFLOW_NAME_XPATH = "//workflow/name";

    /** Debug logging. */
    private static Logger staticLogger = Logger.getLogger(WorkflowPropertiesHelper.class.getName());

    /**
     * Determine if the workflow name is valid for xml
     * @param workflowName
     * @return true if it is a valid workflow name
     */
    public static boolean isValidWorkflowName(String workflowName) {
        try {
            SAXBuilder saxBuilder = new SAXBuilder();
            saxBuilder.setReuseParser(false);
            String wfNameXmlString = "<name>"+workflowName+"</name>";
            InputStream stream = new ByteArrayInputStream(wfNameXmlString.getBytes("UTF-8"));
            Document digraphDom = saxBuilder.build(stream);
        } catch (Exception e) {
            staticLogger.error("newWorkflowName, " + workflowName
                    + ", is not valid xml: " + e.toString());
            return false;
        }
        return true;
    }

    public JSONArray getAuthorList() {
        if (ComponentHelper.authorList == null || ComponentHelper.authorList.toString().equalsIgnoreCase("[]")) {
            WorkflowDao wfDao = DaoFactory.DEFAULT.getWorkflowDao();
            List<UserItem> wfOwners = wfDao.findOwners();
            for (UserItem wfOwner : wfOwners) {
                if (wfOwner.getUserAlias() != null && !wfOwner.getUserAlias().isEmpty()) {
                    ComponentHelper.authorList.put(wfOwner.getUserAlias());
                } else if (wfOwner.getName() != null && !wfOwner.getName().isEmpty()) {
                    ComponentHelper.authorList.put(wfOwner.getName());
                }
            }
        }
        return ComponentHelper.authorList;
    }

    public void setAuthorList(JSONArray authorList) {
        ComponentHelper.authorList= authorList;
    }
    /**
     * Get the list of unique tags from the database again.
     * Save it as the global variable workflowTagsList
     */
    public static void refreshWorkflowTagsList() {
        // Get list of workflow tag items from the db
        WorkflowTagDao wfTagDao = DaoFactory.DEFAULT.getWorkflowTagDao();

        JSONArray workflowsOrderdByPopularity = wfTagDao.getTagsByPopularity(-1);

        // Clear previous list. Populate with latest list.
        ComponentHelper.workflowTagsList = workflowsOrderdByPopularity;
    }

    /**
     * Add a tag to the database
     * @param workfowItem - workflow that should be tagged
     * @param newTag - String value of the tag
     * @return true if the tag was successfully added
     */
    public static boolean addTag(WorkflowItem workfowItem, String newTag) {

        // Determine if newTag is a valid tag name
        Matcher m = WORKFLOW_TAG_PATTERN.matcher(newTag);
        if (!m.matches()) {
            staticLogger.error("New tag (" + newTag + ") is invalid.");
            return false;
        }

        WorkflowTagDao wfTagDao = DaoFactory.DEFAULT.getWorkflowTagDao();
        WorkflowTagMapDao wfTagMapDao = DaoFactory.DEFAULT.getWorkflowTagMapDao();

        WorkflowTagItem wfTagItem = new WorkflowTagItem(newTag);
        wfTagDao.saveOrUpdate(wfTagItem);

        WorkflowTagMapItem wfTagMapItem = new WorkflowTagMapItem();

        wfTagMapItem.setWorkflowExternal(workfowItem);
        wfTagMapItem.setTagExternal(wfTagItem);

        wfTagMapDao.saveOrUpdate(wfTagMapItem);
        return true;
    }

    /**
     * Remove a workflow's tag from the db
     * @param workfowItem
     * @param tagToRemove - String value of the tag to remove
     */
    public static void removeTag(WorkflowItem workfowItem, String tagToRemove) {
        try {
            WorkflowTagMapDao wfTagMapDao = DaoFactory.DEFAULT.getWorkflowTagMapDao();
            List<WorkflowTagMapItem> tagMapItems = wfTagMapDao.findByWorkflow(workfowItem);

            for (int i = 0; i < tagMapItems.size(); i++) {
                WorkflowTagMapId mapId = (WorkflowTagMapId) tagMapItems.get(i).getId();

                if (mapId != null) {
                    Long tagId = mapId.getWorkflowTagId();
                    WorkflowTagDao wfTagDao = DaoFactory.DEFAULT.getWorkflowTagDao();
                    WorkflowTagItem tagItem = wfTagDao.get(tagId);
                    if (tagItem != null) {
                        if (tagItem.getTag().equals(tagToRemove)) {
                            wfTagDao.delete(tagItem);
                        }
                    }
                }
            }
        } catch (Exception e) {
            staticLogger.error("Could not get workflow tags for a workflow in a list: " + e.toString());
        }
    }

    /**
     * @return List of all unique tags in the database
     */
    public static JSONArray getWorkflowTags() {
        return ComponentHelper.workflowTagsList;
    }

    /**
     * Get all of the tags for a given workflow
     * @param wfItem
     * @return List<String> of tags
     */
    public static List<String> getWorkflowTags(WorkflowItem wfItem) {
        List<String> tags = new ArrayList<String>();
        try {
            WorkflowTagMapDao wfTagMapDao = DaoFactory.DEFAULT.getWorkflowTagMapDao();
            List<WorkflowTagMapItem> tagMapItems = wfTagMapDao.findByWorkflow(wfItem);

            for (int i = 0; i < tagMapItems.size(); i++) {
                WorkflowTagMapId mapId = (WorkflowTagMapId) tagMapItems.get(i).getId();

                if (mapId != null) {
                    Long tagId = mapId.getWorkflowTagId();
                    WorkflowTagDao wfTagDao = DaoFactory.DEFAULT.getWorkflowTagDao();
                    WorkflowTagItem tagItem = wfTagDao.get(tagId);
                    if (tagItem != null) {
                        tags.add(tagItem.getTag());
                    }
                }
            }
        } catch (Exception e) {
            staticLogger.error("Could not get workflow tags for a workflow in a list: " + e.toString());
        }
        return tags;
    }

    /**
     * Sending tags between the front end and backend.  Stringify a JSON Array
     * of tags to send to front end.
     * @param wfItem
     * @return Stringified JSON Array of tags for this workflow
     */
    public static String getWorkflowTagsAsString(WorkflowItem wfItem) {
        List<String> tags = getWorkflowTags(wfItem);
        JSONArray tagsJson = new JSONArray();
        int i = 0;
        for (String tag : tags) {
            try {
                tagsJson.put(i++, tag);
            } catch (JSONException e) {
                staticLogger.error("Exception adding tag to json array: " + e.toString());
            }
        }
        return tagsJson.toString();
    }

    public static String updateWorkflowName(String workflowXml, String newWfName) throws IOException {
        Element digraphDoc = null;
        if (workflowXml != null && newWfName != null) {

            try {
                SAXBuilder saxBuilder = new SAXBuilder();
                saxBuilder.setReuseParser(false);
                InputStream stream = new ByteArrayInputStream(workflowXml.getBytes("UTF-8"));
                Document digraphDom = saxBuilder.build(stream);
                stream.close();
                digraphDoc = digraphDom.getRootElement();

                List<Element> wfNameElems = WorkflowXmlUtils.getNodeList(digraphDoc, WORKFLOW_NAME_XPATH);

                for (int i = 0; i < wfNameElems.size(); i++) {
                    Element wfNameElem = wfNameElems.get(i);
                    String oldName = wfNameElem.getTextTrim();
                    if (!oldName.equals(newWfName)) {
                        wfNameElem.setText(WorkflowFileUtils.htmlDecode(newWfName));
                    }
                }

            } catch (Exception e) {
                staticLogger.error("Could not update workflow name element: " + e.toString());
            }
        }
        return WorkflowXmlUtils.getElementAsString(digraphDoc);
    }


    /**
     * Helper function to determine if specified workflow is long-running.
     *
     * @param lastUpdate the Date the workflow was last updated, prior to the run
     * @param workflowItem the workflow
     * @return flag indicating if runtime exceeded threshold
     */
    public static Boolean isLongRunning(Date lastUpdate, WorkflowItem workflowItem) {

        Boolean result = false;

        Long runStart = null;
        if (lastUpdate == null) {
            lastUpdate = new Date();
        }
        runStart = lastUpdate.getTime();

        Long runFinish = null;
        if (workflowItem.getLastUpdated() != null) {
            runFinish = workflowItem.getLastUpdated().getTime();
        } else {
            runFinish = runStart;
        }

        if (runFinish > (runStart + LONG_RUNNING_THRESHOLD)) {
            result = true;
        }

        return result;
    }


    /**
     * Sending tags between the front end and backend.  Stringify a JSON Array
     * of tags to send to front end.
     * @param wfItem
     * @return Stringified JSON Array of tags for this workflow
     */
    public static Integer getWorkflowPaperCount(WorkflowItem wfItem) {
        Integer pCount = 0;
        WorkflowPaperDao wpDao = DaoFactory.DEFAULT.getWorkflowPaperDao();
        List<WorkflowPaperItem> wpItems = wpDao.findByWorkflow(wfItem);
        if (wpItems != null && !wpItems.isEmpty()) {
            pCount = wpItems.size();
        }
        return pCount;
    }
}
