package edu.cmu.pslc.datashop.workflows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.WorkflowErrorTranslationDao;


public class ErrorMessageMap {
    private Map<String, List<String>> errorMessageMap;
    private Map<String, String> componentIdHumanMap;
    private WorkflowErrorTranslationDao wfErrDao;
    public ErrorMessageMap() {
        errorMessageMap = Collections.synchronizedMap(new HashMap<String, List<String>>());
        wfErrDao = DaoFactory.DEFAULT.getWorkflowErrorTranslationDao();
        componentIdHumanMap = Collections.synchronizedMap(new HashMap<String, String>());
    }

    public Set<String> getErrorMessageMapKeyset() {
        return errorMessageMap.keySet();
    }

    public List<String> getErrorMessageMap(String key) {

        if (errorMessageMap.containsKey(key)) {
            return errorMessageMap.get(key);
        }

        return null;
    }

    /**
     * If specified, return the human-readable component id for the 
     * specified component id.
     * @param componentId
     * @return String human-readable id
     */
    public String getComponentIdHuman(String componentId) {
        if (componentIdHumanMap.containsKey(componentId)) {
            return componentIdHumanMap.get(componentId);
        } else {
            return componentId;
        }
    }

    /**
     * Adds a message for a given component id and returns the number of error messages
     * for the same component. A null value is used as the key for workflow-level errors.
     * @param componentId the component id or null for workflow-level errors
     * @param message the error message
     * @return the number of error messages for the given component
     */
    public Integer add(String componentId, String message) {

        // A null or empty component id Key represents a workflow-related error
        if (componentId == null || componentId.isEmpty()) {
            componentId = "workflow";
        }

        List<String> errors = null;
        if (errorMessageMap.containsKey(componentId)) {
            errors = errorMessageMap.get(componentId);
        } else {
            errors = new ArrayList<String>();
        }
        String translation = null;
        // Find error translations which match the signature in the workflow_error_translation table
        List<WorkflowErrorTranslationItem> wfErrItems = wfErrDao.findByMessage(message);
        Boolean replaceFlag = false;
        if (wfErrItems != null && !wfErrItems.isEmpty()) {
            for (WorkflowErrorTranslationItem wfErrItem : wfErrItems) {
                if (wfErrItem.getTranslation() != null) {
                    // If we have a regexp and a translation, we can create dynamic translations,
                    // and we can use parens in our regexp to get replacement groups
                    StringBuffer modifiedTranslation = new StringBuffer();
                    if (wfErrItem.getRegexp() != null) {
                        Pattern pattern = Pattern.compile(wfErrItem.getRegexp());
                        Matcher matcher = pattern.matcher(message);
                        if (matcher.find()) {
                            // Create the newly translated error message
                            modifiedTranslation.append(
                                message.replaceAll(wfErrItem.getRegexp(), wfErrItem.getTranslation()));
                        }
                    }
                    String trimmedTranslation = modifiedTranslation.toString().trim();
                    // If the translation exists, then replace or add it to the error message map.
                    if (!trimmedTranslation.isEmpty()) {
                        translation = trimmedTranslation;
                    } else {
                        translation = wfErrItem.getTranslation();
                    }
                    if (wfErrItem.getReplaceFlag() != null && wfErrItem.getReplaceFlag()) {
                        replaceFlag = true;
                    }
                }
            }
        }
        // If an error translation exists, prepend it to the error messages.
        if (translation != null) {
            errors.add(translation);
        }

        if (translation == null || !replaceFlag){
            errors.add(message);
        }

        errorMessageMap.put(componentId, errors);

        return 0;
    }

    /**
     * Adds a message for a given component id and returns the number of error messages
     * for the same component. A null value is used as the key for workflow-level errors.
     * Allows user to specify the human-readable name of the component.
     * @param componentId the component id or null for workflow-level errors
     * @param componentIdHuman the human-readable id of the component
     * @param message the error message
     * @return the number of error messages for the given component
     */
    public Integer add(String componentId, String componentIdHuman, String message) {
        if ((componentId != null) && (componentIdHuman != null)) {
            componentIdHumanMap.put(componentId, componentIdHuman);
        }
        return add(componentId, message);
    }


    /** Returns the errorMessageMap as newline delimited text.
     * @param delim the string delimiter (per line)
     * @return the errorMessageMap as text
     */
    public String getErrorMessageMapAsText(String delim) {
        StringBuffer errorMessageStringBuffer = new StringBuffer();
        for (String key : errorMessageMap.keySet()) {
            for (String errorMessage : errorMessageMap.get(key)) {
                errorMessageStringBuffer.append(errorMessage + delim);
            }
        }
        return errorMessageStringBuffer.toString();
    }

    /** Returns the errorMessageMap as newline delimited text for the component.
     * @param componentId the component id
     * @param delim the string delimiter (per line)
     * @return the errorMessageMap as text
     */
    public String getErrorMessageMapAsText(String componentId, String delim) {
        StringBuffer errorMessageStringBuffer = new StringBuffer();
        if (errorMessageMap.containsKey(componentId)) {
            for (String errorMessage : errorMessageMap.get(componentId)) {
                errorMessageStringBuffer.append(errorMessage + delim);
            }
        }
        return errorMessageStringBuffer.toString();
    }

    public JSONObject getErrorMessageMapAsJson() {
        try {
            JSONArray allComponents = new JSONArray();
            Boolean hasErrors = false;
            for (String key : errorMessageMap.keySet()) {

                    JSONArray componentMessages = new JSONArray();
                    for (String errorMessage : errorMessageMap.get(key)) {
                        JSONObject jsonMessage = new JSONObject();
                        jsonMessage.put("error", errorMessage);
                        componentMessages.put(jsonMessage);
                        hasErrors = true;
                    }

                    if (hasErrors) {
                        // Key: componentId, value: array of error messages
                        JSONObject messageMap = new JSONObject();
                        messageMap.put("component_message_container", componentMessages);
                        JSONObject componentContainer = new JSONObject();
                        componentContainer.put(key, messageMap);
                        allComponents.put(componentContainer);

                    }
            }

            JSONObject map = new JSONObject();
            map.put("component_message_map", allComponents);
            return map;

        } catch (JSONException e) {
            ///logger.error("Could not convert error messages to JSON: "
            ///    + errorMessageStringBuffer.toString());
            // in this case, return the generic workflow message map (to be done)
        }

        return null;
    }


    /**
     * Gets the error message map as a JSON object for the component.
     * @param componentId the component id
     * @return the error message map as a JSON object for the component
     */
    public JSONObject getErrorMessageMapAsJson(String componentId) {
        try {
            JSONArray allComponents = new JSONArray();
            Boolean hasErrors = false;
            if (errorMessageMap.containsKey(componentId)) {

                    JSONArray componentMessages = new JSONArray();
                    for (String errorMessage : errorMessageMap.get(componentId)) {
                        JSONObject jsonMessage = new JSONObject();
                        jsonMessage.put("error", errorMessage);
                        componentMessages.put(jsonMessage);
                        hasErrors = true;
                    }

                    if (hasErrors) {
                        // Key: componentId, value: array of error messages
                        JSONObject messageMap = new JSONObject();
                        messageMap.put("component_message_container", componentMessages);
                        JSONObject componentContainer = new JSONObject();
                        componentContainer.put(componentId, messageMap);
                        allComponents.put(componentContainer);

                    }
            }

            JSONObject map = new JSONObject();
            map.put("component_message_map", allComponents);
            return map;

        } catch (JSONException e) {
            ///logger.error("Could not convert error messages to JSON: "
            ///    + errorMessageStringBuffer.toString());
            // in this case, return the generic workflow message map (to be done)
        }

        return null;
    }

    public boolean isEmpty() {
        return errorMessageMap.isEmpty();
    }


}
