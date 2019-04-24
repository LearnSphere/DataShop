package edu.cmu.pslc.datashop.servlet.workflows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.WorkflowComponentDao;
import edu.cmu.pslc.datashop.workflows.WorkflowComponentItem;

public class OptionDependencyHelper {

   /** Debug logging. */
   public static JSONObject fullOptionConstraintMap = null;

   private static Logger staticLogger = Logger.getLogger(WorkflowHelper.class.getName());
   private static HashMap<String, List<OptionDependency>> createOptionConstraintMap(WorkflowComponentItem wfcItem, String constructionType) {

    HashMap<String, List<OptionDependency>> optionConstraintMap = new HashMap<String, List<OptionDependency>>();
    List<Element> allNodes = WorkflowXmlUtils.getNodeList(wfcItem.getSchemaPath(),
            constructionType);
    if (allNodes == null) {
        allNodes = new ArrayList<Element>();
    }

    for (int j = 0; j < allNodes.size(); j++) {
        Element depOption = allNodes.get(j);
        if (depOption.getAttribute("type") != null && depOption.getAttributeValue("name") != null) {
            String depName = depOption.getAttributeValue("name");

            String depOptionType = depOption.getAttributeValue("type");

            String independentOptionName = depOption.getAttributeValue("dependsOn", WorkflowHelper.lsXmlNs);
            String depOptionName = depOption.getAttributeValue("dependentOption", WorkflowHelper.lsXmlNs);

            // can be null
            String depOptionConstraint = depOption.getAttributeValue("constraint", WorkflowHelper.lsXmlNs);

            if (depName != null) {
                depName = WorkflowFileUtils.htmlDecode(depName);
            }
            if (depOptionType != null) {
                depOptionType = WorkflowFileUtils.htmlDecode(depOptionType);
            }
            if (independentOptionName != null) {
                independentOptionName = WorkflowFileUtils.htmlDecode(independentOptionName);
            }
            if (depOptionName != null) {
                depOptionName = WorkflowFileUtils.htmlDecode(depOptionName);
            }
            if (depOptionConstraint != null) {
                depOptionConstraint = WorkflowFileUtils.htmlDecode(depOptionConstraint);
            }

            String negationStr = depOption.getAttributeValue("negation", WorkflowHelper.lsXmlNs);
            Boolean negation = false;
            if (negationStr != null && negationStr.equalsIgnoreCase("true")) {
                negation = true;
            }

            // Right now, simply organize the dependencies by the dependentOption
            if (optionConstraintMap.containsKey(depOptionName)) {
                List<OptionDependency> optionDependencies = optionConstraintMap.get(depOptionName);
                optionDependencies
                        .add(new OptionDependency(j, depName, depOptionType, depOptionName,
                                independentOptionName, depOptionConstraint, negation));
            } else {
                List<OptionDependency> optionDependencies = Collections.synchronizedList(new ArrayList<OptionDependency>());
                optionDependencies
                        .add(new OptionDependency(j, depName, depOptionType, depOptionName,
                                independentOptionName, depOptionConstraint, negation));
                optionConstraintMap.put(depOptionName, optionDependencies);
            }
        }
    }
    return optionConstraintMap;
}

/** Format for the date range method, getDateRangeString. */


    public static JSONObject getFullOptionConstraintMap() {
        if (fullOptionConstraintMap != null) {
            //return fullOptionConstraintMap;
        }
        WorkflowComponentDao wfcDao = DaoFactory.DEFAULT.getWorkflowComponentDao();
        List<WorkflowComponentItem> workflowComponentItems = wfcDao.findByEnabled(true);

        for (WorkflowComponentItem workflowComponentItem : workflowComponentItems) {

            HashMap<String, List<OptionDependency>> optionConstraintMap =
                    createOptionConstraintMap(workflowComponentItem, ComponentHelper.XML_DEPENDENT_OPTION_NODES);

            JSONObject jsonOptionIds = new JSONObject();
            JSONArray jsonDependencyNames = new JSONArray();
            JSONArray jsonNoDependencies = new JSONArray();

            List<String> dependencyNames = new ArrayList<String>();
            List<String> noDependencies = new ArrayList<String>();
            try {
                jsonOptionIds.put("root", 0);

                // An ID exists for each dependent option name
                //HashMap<String, Integer> depOptionIds = new HashMap<String, Integer>();
                //HashMap<String, Integer> indepOptionIds = new HashMap<String, Integer>();
                // Root is the 0 index
                Integer optionId = 1;

                jsonNoDependencies.put("root");
                noDependencies.add("root");

                List<String> dependentOptions = new ArrayList<String>();

                List<String> depKeys = new ArrayList<String>();
                depKeys.addAll(optionConstraintMap.keySet());

                for (String key : depKeys) {
                    String depKey = key;

                    if (depKey != null) {
                        depKey = WorkflowFileUtils.htmlEncode(depKey);
                    }

                    if (!jsonOptionIds.has(depKey)) {
                        jsonOptionIds.put(depKey, optionId);
                        dependentOptions.add(depKey);
                        optionId++;
                    }
                }

                for (String key : optionConstraintMap.keySet()) {
                    List<OptionDependency> optionConstraints = optionConstraintMap.get(key);
                    if (optionConstraints != null) {
                        for (OptionDependency optDep : optionConstraints) {
                            if (optDep.getIndependentOptionName() != null) {
                                String indepKey = optDep.getIndependentOptionName();

                                if (indepKey != null) {
                                    indepKey = WorkflowFileUtils.htmlEncode(indepKey);
                                }

                                if (!jsonOptionIds.has(indepKey)) {
                                    jsonOptionIds.put(indepKey, optionId);
                                    optionId++;
                                }
                                if (!dependentOptions.contains(indepKey)
                                        && !noDependencies.contains(indepKey)) {
                                    noDependencies.add(indepKey);
                                    jsonNoDependencies.put(indepKey);
                                }
                            }
                            if (optDep.getName() != null) {
                                String depName = optDep.getName();

                                if (depName != null) {
                                    depName = WorkflowFileUtils.htmlEncode(depName);
                                }
                                if (!dependencyNames.contains(depName)) {
                                    dependencyNames.add(depName);
                                    jsonDependencyNames.put(depName);
                                    if (!jsonOptionIds.has(depName)) {
                                        jsonOptionIds.put(depName,  optionId);
                                        optionId++;
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                staticLogger.error("Could not build option list for workflow component "
            + workflowComponentItem.getComponentName());
            }


            JSONObject jsonOptionConstraintMap = new JSONObject();
            JSONObject dependenciesMap = new JSONObject();
            for (String depKey : optionConstraintMap.keySet()) {
                List<OptionDependency> deps = optionConstraintMap.get(depKey);
                if (deps != null && !deps.isEmpty()) {
                    JSONArray depArray = new JSONArray();
                    for (OptionDependency dep : deps) {
                        JSONObject jdep = new JSONObject();
                        try {
                            jdep.put("id", WorkflowFileUtils.htmlEncode(dep.getId().toString()));
                            jdep.put("negation", WorkflowFileUtils.htmlEncode(dep.getNegation().toString()));
                            jdep.put("constraint", WorkflowFileUtils.htmlEncode(dep.getDependentOptionConstraint()));
                            jdep.put("depType", WorkflowFileUtils.htmlEncode(dep.getDepOptionType()));
                            jdep.put("depOptName", WorkflowFileUtils.htmlEncode(dep.getDependentOptionName()));
                            jdep.put("indepOptName", WorkflowFileUtils.htmlEncode(dep.getIndependentOptionName()));
                            jdep.put("dependencyName", WorkflowFileUtils.htmlEncode(dep.getName()));

                            depArray.put(jdep);


                        } catch (JSONException e) {
                            staticLogger.error("Could not create option dependency definition for "
                                    + workflowComponentItem.getComponentName() + " and key(" + depKey + ")");
                        }
                    }
                    try {

                        dependenciesMap.put(WorkflowFileUtils.htmlEncode(depKey), depArray);
                    } catch (JSONException e) {
                        staticLogger.error("Could not add option dependency constraint for "
                                + workflowComponentItem.getComponentName() + " and key(" + depKey + ")");
                    }
                }
            }



            try {
                if (fullOptionConstraintMap == null) {
                    fullOptionConstraintMap = new JSONObject();
                }

                if (dependenciesMap.length() > 0) {
                    jsonOptionConstraintMap.put("noDependencies", jsonNoDependencies);
                    jsonOptionConstraintMap.put("dependencies", dependenciesMap);
                    jsonOptionConstraintMap.put("dependencyNames", jsonDependencyNames);
                    jsonOptionConstraintMap.put("dependencyIdsByName", jsonOptionIds);
                }

                String dbFriendlyCompName = workflowComponentItem.getComponentName()
                    .replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();
                fullOptionConstraintMap.put(dbFriendlyCompName,
                        jsonOptionConstraintMap);

            } catch (JSONException e) {
                staticLogger.error("Could not add full option dependency definition for "
                        + workflowComponentItem.getComponentName());
            }
        }
        return fullOptionConstraintMap;
    }

}
