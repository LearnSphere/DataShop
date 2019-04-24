package edu.cmu.pslc.datashop.servlet.workflows;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.JDOMException;

import edu.cmu.pslc.datashop.dao.AuthorizationDao;
import edu.cmu.pslc.datashop.dao.ComponentFileDao;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.WorkflowComponentAdjacencyDao;
import edu.cmu.pslc.datashop.dao.WorkflowComponentDao;
import edu.cmu.pslc.datashop.dao.WorkflowComponentInstancePersistenceDao;
import edu.cmu.pslc.datashop.dao.WorkflowFileDao;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.workflows.ComponentFileItem;
import edu.cmu.pslc.datashop.workflows.WorkflowComponentAdjacencyItem;
import edu.cmu.pslc.datashop.workflows.WorkflowComponentInstancePersistenceItem;
import edu.cmu.pslc.datashop.workflows.WorkflowComponentItem;
import edu.cmu.pslc.datashop.workflows.WorkflowFileItem;
import edu.cmu.pslc.datashop.workflows.WorkflowItem;

public class WorkflowAccessHelper {


    /** Used in the floyd-warshall algorithm to get distance matrix. Not to exceed 20000. Set to 10000. */
    private static Integer FLOYD_WARSHALL_MAX_DIST = Integer.MAX_VALUE;
    public static final String START_SYMBOL = "START_SYMBOL";

    /** Debug logging. */
    private static Logger staticLogger = Logger.getLogger(WorkflowHelper.class.getName());

    /**
    * Gets the lowest permissions (based on AuthorizationItem levels) of
    * all the datasets and files in a given workflow.
    * @param workflowItem the workflow item
    * @param loggedInUserItem the logged-in user item
    * @return the lowest permissions (based on AuthorizationItem levels) of
    * all the datasets and files in a given workflow.
    */
   public String getFloorPermissions(WorkflowItem workflowItem, UserItem loggedInUserItem) {
       ComponentFileDao compFileDao = DaoFactory.DEFAULT.getComponentFileDao();
       AuthorizationDao authDao = DaoFactory.DEFAULT.getAuthorizationDao();
       List<ComponentFileItem> compFileItems = compFileDao.findByWorkflow(workflowItem);

       String minLevel = null;
       // Public users cannot view public datasets without logging in
       if (loggedInUserItem != null && !((String) loggedInUserItem.getId()).equalsIgnoreCase(UserItem.DEFAULT_USER)) {
           if (workflowItem.getGlobalFlag()
               || (loggedInUserItem != null
                   && loggedInUserItem.getId().equals(workflowItem.getOwner().getId()))) {
               if (compFileItems == null || compFileItems.isEmpty()) {
                   if (loggedInUserItem != null
                           && loggedInUserItem.getId().equals(workflowItem.getOwner().getId())) {
                       return WorkflowItem.LEVEL_EDIT;
                   } else if (loggedInUserItem != null) {
                       // There are no files associated with this workflow.
                       // Returning null ensures it is stripped of files.
                       return null;
                   }
               }


               if (compFileItems != null && loggedInUserItem != null) {
                   // If any workflow imports are not accessible to this user,
                   // then break out of the loop and return null.
                   for (ComponentFileItem compFileItem : compFileItems) {
                       String authLevel = null;

                       if (compFileItem.getDataset() != null) {
                           // dataset authorization trumps file ownership
                           authLevel = authDao.getAuthLevel(
                               loggedInUserItem, compFileItem.getDataset());
                           if (authLevel != null && (loggedInUserItem.getId()
                               .equals(workflowItem.getOwner().getId()))) {
                               // owner of file, use LEVEL_EDIT
                               authLevel = WorkflowItem.LEVEL_EDIT;
                           } else if (authLevel != null) {
                               // global workflow, use LEVEL_VIEW
                               authLevel = WorkflowItem.LEVEL_VIEW;
                           } else {
                               authLevel = null; // no permissions, use workflow stripped of files
                           }

                       } else {
                           // The workflow is shared, but this file is not in a dataset, use ownership.
                           if (compFileItem.getFile() != null) {
                               WorkflowFileDao wfFileDao = DaoFactory.DEFAULT.getWorkflowFileDao();
                               WorkflowFileItem itemExists = wfFileDao.find((Integer) compFileItem.getFile().getId());
                               if (itemExists != null) {
                                   WorkflowFileItem fileItem = wfFileDao.get(
                                       (Integer) compFileItem.getFile().getId());
                                   if (fileItem != null) {
                                       if (fileItem.getOwner() != null
                                           && loggedInUserItem.getId()
                                               .equals(fileItem.getOwner().getId())) {
                                           // owner of file, use LEVEL_EDIT
                                           authLevel = WorkflowItem.LEVEL_EDIT;
                                       } else {
                                           // If an import component inside a global workflow
                                           // has a different owner owner, do not allow view
                                           // until the file is attached to a dataset.
                                           if (fileItem.getFilePath().matches("workflows" + "/" + workflowItem.getId() + "/Import[a-zA-Z0-9_\\-]+/output/")
                                                   || fileItem.getFilePath().matches("workflows" + "/" + workflowItem.getId() + "/Data[a-zA-Z0-9_\\-]+/output/")) {
                                               // If this is an Import component, the file is not owned by this user,
                                               // and the workflow is not shared, then they do not have access to this file.
                                               authLevel = null;
                                           } else if (fileItem.getFilePath().matches("workflows" + "/" + workflowItem.getId() + "/[a-zA-Z0-9_\\-]+/output/")) {
                                               // Not an import so this is one of the files generated by the workflow, itself.
                                               // (Not uploads)
                                               authLevel = WorkflowItem.LEVEL_VIEW;
                                           }
                                       }
                                   }
                               }
                           }
                       }

                       if (authLevel == null) {
                           minLevel = null;
                           break;
                       } else {
                           if (authLevel.equalsIgnoreCase(WorkflowItem.LEVEL_VIEW)) {
                               minLevel = authLevel;
                           } else if (authLevel.equalsIgnoreCase(WorkflowItem.LEVEL_EDIT)
                                   && minLevel == null) {
                               minLevel = authLevel;
                           }
                       }

                   }
               }
           }

           if (loggedInUserItem.getAdminFlag()) {
               if (loggedInUserItem.getId().equals(workflowItem.getOwner().getId())) {
                   minLevel = WorkflowItem.LEVEL_EDIT;
               } else if (!loggedInUserItem.getId().equals(workflowItem.getOwner().getId())) {
                   minLevel = WorkflowItem.LEVEL_VIEW;
               }
           }
       }
       return minLevel;
   }

    public static String pruneWorkflowXml(WorkflowItem workflowItem, UserItem loggedInUserItem, String baseDir) {

       String prunedWorkflowXml = null;
       String workflowXml = null;
       if (workflowItem != null && workflowItem.getWorkflowXml() != null) {
           workflowXml = workflowItem.getWorkflowXml();
       }

       // Update wfc adjacency table.
       ConnectionHelper.updateAdjacencyList(baseDir, workflowItem, loggedInUserItem);


       if (workflowXml != null) {
           try {
               Element root = WorkflowXmlUtils.getStringAsElement(workflowXml);

               Element components = root.getChild("components");
               List<Element> componentNodes = null;
               List<Element> deleteItems = new ArrayList<Element>();
               List<Element> restrictedComponents = new ArrayList<Element>();
               // This workflow has components
               if (components != null && components.getChildren("component") != null) {

                   componentNodes = components.getChildren("component");
                   if (componentNodes != null && !componentNodes.isEmpty()) {
                       for (Element componentNode : componentNodes) {
                           if (componentNode.getChild("component_id") != null) {
                               String componentId = componentNode.getChildTextTrim("component_id");
                               if (componentId != null) {

                                   String componentAccessLevel = getComponentAccessLevel(
                                           loggedInUserItem, baseDir, workflowItem, componentId);
                                   if (componentAccessLevel == null) {
                                       // The access is null, meaning this component has restricted access. Either because of its
                                       // parents or it is in the ready state.
                                       restrictedComponents.add(componentNode);

                                       Element inputsElement = componentNode.getChild("inputs");
                                       Element optionsElement = componentNode.getChild("options");

                                       if (inputsElement != null) {
                                           deleteItems.add(inputsElement);
                                       }
                                       if (optionsElement != null) {
                                           deleteItems.add(optionsElement);
                                       }
                                   }


                               }
                           }
                       }
                   }
                   // Remove inputs and options elements
                   for (Element deleteItem : deleteItems) {
                       String tagName = deleteItem.getName();
                       Element parentElement = (Element) deleteItem.getParent();
                       deleteItem.detach();
                       if (tagName.equalsIgnoreCase("options") || tagName.equalsIgnoreCase("inputs")) {
                           parentElement.addContent(new Element(tagName));
                       }

                   }
                   for (Element restrictedComponent : restrictedComponents) {
                       Element restrictedTag = new Element("restricted");
                       restrictedComponent.addContent(restrictedTag);
                   }

                   prunedWorkflowXml = WorkflowXmlUtils.getElementAsString(root);
               }
           } catch (JDOMException e) {
               e.printStackTrace();
           } catch (IOException e) {
               e.printStackTrace();
           }
       }
       return prunedWorkflowXml;
   }

   /**
    * When sending the workflow xml to the user, remove the content of the options that they do not
    * have access to i.e. private options based on the xsd.
    * @param originalXml
    * @param workflowItem
    * @param loggedInUserItem
    * @param baseDir
    * @return
    */
   public static String removeDataFromPrivateOptions(
           String originalXml, WorkflowItem workflowItem, UserItem loggedInUserItem, String baseDir,
               Boolean overrideOwner) {
       String sanitizedWfXml = null;

       if (originalXml != null) {
           WorkflowComponentInstancePersistenceDao wfcipDao =
                   DaoFactory.DEFAULT.getWorkflowComponentInstancePersistenceDao();
           WorkflowComponentDao wfcDao = DaoFactory.DEFAULT.getWorkflowComponentDao();

           try {
               Element root = WorkflowXmlUtils.getStringAsElement(originalXml);

               Element components = null;
               if (root.getName().equals("component")) {
                   components = new Element("LS_root_8675309");
                   components.addContent((Element) root.clone());
               } else if (root.getName().equals("workflow")) {
                   components = root.getChild("components");
               }
               List<Element> componentNodes = null;
               // This workflow has components
               if (components != null && components.getChildren("component") != null) {
                   componentNodes = components.getChildren("component");
                   if (componentNodes != null && !componentNodes.isEmpty()) {
                       // Loop through components
                       for (Element componentNode : componentNodes) {
                           if (componentNode.getChild("component_id") != null) {
                               String componentId = componentNode.getChildTextTrim("component_id");
                               String componentName = componentNode.getChildTextTrim("component_name");
                               if (componentId != null && componentName != null) {
                                   // Check for private options
                                   // Get a list of the options
                                   Element optionsElement = componentNode.getChild("options");

                                   List<Element> optionNodes = null;
                                   if (optionsElement != null ) {
                                       optionNodes = optionsElement.getChildren();
                                   }

                                   // Get a list of the private options
                                   List<Element> privateOptionNodes = null;
                                   WorkflowComponentItem wfcItem = wfcDao.findByName(componentName);
                                   String xPathGetPrivateOptions =
                                           "//xs:complexType[@name='OptionsType']/*/xs:element[contains(@ls:privateOption,'true')]";
                                   String schemaPath = null;
                                   if (wfcItem != null) {
                                       schemaPath = wfcItem.getSchemaPath();
                                   }
                                   if (schemaPath != null) {
                                       privateOptionNodes = WorkflowXmlUtils.getNodeList(schemaPath, xPathGetPrivateOptions);
                                   }

                                   ArrayList<String> privOptIds = new ArrayList<String>();
                                   Element privOptEles = new Element("privateOptions");
                                   if (privateOptionNodes != null) {
                                       for (Element privOpt : privateOptionNodes) {
                                           String optionName = privOpt.getAttributeValue("name");
                                           privOptIds.add(optionName);
                                           privOptEles.addContent(new Element(optionName));
                                       }
                                       componentNode.addContent(privOptEles);
                                   }

                                   // Get the user's privileges for this workflow
                                   boolean isOwner = false;
                                   if (workflowItem != null && workflowItem.getOwner() != null
                                           && workflowItem.getOwner().getId().equals(loggedInUserItem.getId())) {
                                       isOwner = true;
                                   }

                                   if (overrideOwner) {
                                       isOwner = false;
                                   }

                                   // If the user only has view access to the component, delete the data in the private option
                                   if (!isOwner) {
                                       //Delete the data of private options
                                       if (optionNodes != null) {
                                           for (Element optionEle : optionNodes) {
                                               if (privOptIds.contains(optionEle.getName())) {
                                                   //This is a private option, delete its data
                                                   optionEle.setText("This is a private option and you do not have access to its value.");
                                               }
                                           }
                                       }
                                   }
                               }
                           }
                       }
                   }
                   sanitizedWfXml = WorkflowXmlUtils.getElementAsString(root);
               } else {
                   sanitizedWfXml = originalXml;
               }
           } catch (JDOMException e) {
               e.printStackTrace();
           } catch (IOException e) {
               e.printStackTrace();
           }
       }
       return sanitizedWfXml;
   }

   public static String removePrivateOptionMetaData(String xmlData, UserItem loggedInUserItem, UserItem workflowOwner) {

       String privatizedXmlData = null;
       WorkflowComponentDao workflowComponentDao = DaoFactory.DEFAULT.getWorkflowComponentDao();

       if (xmlData != null) {

           Element root = null;
           try {
               root = WorkflowXmlUtils.getStringAsElement("<LS_root_8675309>" + xmlData + "</LS_root_8675309>");
           } catch (JDOMException e) {
               staticLogger.error("Could not convert xml data.");
           } catch (IOException e) {
               staticLogger.error("Could not read xml data.");
           }

           if (root != null && workflowOwner != null && loggedInUserItem != null) {

               Boolean isOwner = false;
               if (workflowOwner.getId().equals(loggedInUserItem.getId())) {
                   isOwner = true;
               }

               List<Element> components1 = (List<Element>) root.getChildren();

               List<Element> deleteItems = new ArrayList<Element>();
               List<Element> restrictedComponents = new ArrayList<Element>();
               // This workflow has components

               if (components1 != null && !components1.isEmpty()) {
                   for (Element componentNode : components1) {
                       if (componentNode.getChild("component_id") != null) {
                           String componentId = componentNode.getChildTextTrim("component_id");
                           String componentName = componentNode.getChildTextTrim("component_name");

                           if (componentName != null) {
                               WorkflowComponentItem workflowComponentItem = workflowComponentDao
                                       .findByName(componentName);
                               if (workflowComponentItem != null) {

                                   Element optionMeta = componentNode.getChild("optionmeta");

                                   // Get all children of any options element
                                   List<Element> optionNodes = WorkflowXmlUtils.getNodeListInjection(workflowComponentItem.getSchemaPath(),
                                           ComponentHelper.XML_OPTIONS_CHILD_NODES);

                                   if (optionNodes != null) {
                                       staticLogger.trace("Component options found: " + optionNodes.size());
                                       for (int i = 0; i < optionNodes.size(); i++) {

                                           Element optionNode = optionNodes.get(i);

                                           List<String> actualValuesKeys = new ArrayList<String>();
                                           if (optionNode != null && optionNode.getAttribute("type") != null
                                                   && optionNode.getAttribute("name") != null) {

                                               String displayName = optionNode.getAttributeValue("id");
                                               String name = optionNode.getAttributeValue("name");
                                               String type = optionNode.getAttributeValue("type");
                                               String defaultValue = optionNode.getAttributeValue("default");

                                               // Allow users to set an option to
                                               // be private
                                               String isPrivateStr = optionNode.getAttributeValue("privateOption",
                                                       WorkflowHelper.lsXmlNs);
                                               boolean isPrivate = false;
                                               if (isPrivateStr != null) {
                                                   try {
                                                       isPrivate = Boolean.parseBoolean(isPrivateStr);
                                                   } catch (Exception e) {
                                                       // The private option
                                                       // defaults to false unless
                                                       // the user specifies it to
                                                       // be true
                                                   }
                                               }

                                               if (isPrivate && !isOwner) {
                                                   if (optionMeta != null) {
                                                       for (Element optionMetaChild : (List<Element>) optionMeta.getChildren()) {
                                                           if (optionMetaChild.getName().equalsIgnoreCase(name)) {
                                                               optionMetaChild.setText("******");
                                                           }
                                                       }
                                                   }
                                               }
                                           }
                                       }
                                   }
                               }
                           }
                       }
                   }
                   try {
                       privatizedXmlData = WorkflowXmlUtils.getElementAsString(root);
                       staticLogger.error("PRIVATIZED: " + privatizedXmlData);
                   } catch (IOException e) {
                       staticLogger.error("Could not read privatized XML data.");
                   }
               }
           }
       }
       return privatizedXmlData;
   }

   public static synchronized String getComponentAccessLevel(
        UserItem loggedInUserItem, String baseDir,
            WorkflowItem workflowItem, String thisDescendantId) {

        Integer[][] adjMat = null;
        DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
        AuthorizationDao authDao = DaoFactory.DEFAULT.getAuthorizationDao();
        ComponentFileDao cfDao = DaoFactory.DEFAULT.getComponentFileDao();
        WorkflowComponentAdjacencyDao wfcAdjDao = DaoFactory.DEFAULT.getWorkflowComponentAdjacencyDao();
        List<WorkflowComponentAdjacencyItem> wfcAdjItems =
            wfcAdjDao.findByWorkflow(workflowItem);

        WorkflowComponentInstancePersistenceDao wfcipDao = DaoFactory.DEFAULT.getWorkflowComponentInstancePersistenceDao();
        WorkflowComponentInstancePersistenceItem wfcipItem =
            wfcipDao.findByWorkflowAndId(workflowItem, thisDescendantId);

        if ((wfcipItem != null && wfcipItem.isDirty())
                && !workflowItem.getOwner().getId().equals(loggedInUserItem.getId())) {
            return null;
        }

        // Only get them if they don't exist.
        if (wfcAdjItems == null || wfcAdjItems.isEmpty()) {
            wfcAdjItems = ConnectionHelper.updateAdjacencyList(baseDir, workflowItem, loggedInUserItem);
        }

        if (wfcAdjItems == null && workflowItem.getOwner().getId().equals(loggedInUserItem.getId())) {
            return WorkflowItem.LEVEL_EDIT;
        }

        // Setup adjacency matrix
        if (wfcAdjItems != null && !wfcAdjItems.isEmpty()) {
            adjMat = new Integer[wfcAdjItems.size()][wfcAdjItems.size()];

            for (Integer i = 0; i < wfcAdjItems.size(); i++) {
                for (Integer j = 0; j < wfcAdjItems.size(); j++) {

                    String childId = wfcAdjItems.get(i).getChildId();
                    String compId = wfcAdjItems.get(j).getComponentId();
                    if (childId != null && compId != null && compId.equalsIgnoreCase(childId)) {
                        adjMat[i][j] = 1;
                    } else if (i == j) {
                        adjMat[i][j] = 1;
                    } else {
                        adjMat[i][j] = FLOYD_WARSHALL_MAX_DIST;
                    }
                }
            }
        }

        String minLevel = START_SYMBOL;
        // Run Floyd-warshall algorithm to get all-pairs shortest paths
        if (wfcAdjItems != null) {
            for (Integer i = 0; i < wfcAdjItems.size(); i++) {
                for (Integer j = 0; j < wfcAdjItems.size(); j++) {
                    for (Integer k = 0; k < wfcAdjItems.size(); k++) {
                        if (adjMat[j][i] != FLOYD_WARSHALL_MAX_DIST && adjMat[i][k] != FLOYD_WARSHALL_MAX_DIST
                            && adjMat[j][k] > adjMat[j][i] + adjMat[i][k]) {
                            adjMat[j][k] = adjMat[j][i] + adjMat[i][k];
                        }
                    }
                }
            }

            for (Integer j = 0; j < wfcAdjItems.size(); j++) {
                String descendantId = wfcAdjItems.get(j).getComponentId();
                if (thisDescendantId != null && thisDescendantId.equalsIgnoreCase(descendantId)) {

                    for (Integer i = 0; i < wfcAdjItems.size(); i++) {
                        // j = descendant, i = ancestor
                        String ancestorId = wfcAdjItems.get(i).getComponentId();

                        // Only imports and generators without input step into this conditional block:
                        if ((ancestorId.equalsIgnoreCase(thisDescendantId) // Either the descendant is the ancestor,
                                || adjMat[i][j] < FLOYD_WARSHALL_MAX_DIST) // or there is a path to the import
                            ) { /*mck2&& (wfcAdjItems.get(i).getComponentId().matches("Import.*")
                            || wfcAdjItems.get(i).getComponentId().matches("Generate.*"))) {*/


                            List<ComponentFileItem> compFileItems = cfDao.findImportByComponent(workflowItem, ancestorId);
                            if (loggedInUserItem != null && !((String) loggedInUserItem.getId()).equalsIgnoreCase(UserItem.DEFAULT_USER)) {
                                   if (workflowItem.getGlobalFlag()
                                       || (loggedInUserItem != null
                                           && loggedInUserItem.getId().equals(workflowItem.getOwner().getId()))) {

                                       if (compFileItems != null && loggedInUserItem != null) {
                                           // If any workflow imports are not accessible to this user,
                                           // then break out of the loop and return null.
                                           for (ComponentFileItem compFileItem : compFileItems) {
                                               String authLevel = null;
                                               compFileItem = cfDao.get((Long) compFileItem.getId());

                                               if (compFileItem.getDataset() != null) {
                                                   DatasetItem dsItem = datasetDao.get((Integer) compFileItem.getDataset().getId());
                                                   // dataset authorization trumps file ownership
                                                   authLevel = authDao.getAuthLevel(
                                                       loggedInUserItem, dsItem);
                                                   if (authLevel != null && (loggedInUserItem.getId()
                                                       .equals(workflowItem.getOwner().getId()))) {
                                                       // owner of file, use LEVEL_EDIT
                                                       authLevel = WorkflowItem.LEVEL_EDIT;
                                                   } else if (authLevel != null) {
                                                       // global workflow, use LEVEL_VIEW
                                                       authLevel = WorkflowItem.LEVEL_VIEW;
                                                   } else {
                                                       authLevel = null; // no permissions, use workflow stripped of files
                                                   }

                                               } else {
                                                   // The workflow is shared, but this file is not in a dataset, use ownership.
                                                   if (compFileItem.getFile() != null) {
                                                       WorkflowFileDao wfFileDao = DaoFactory.DEFAULT.getWorkflowFileDao();
                                                       WorkflowFileItem itemExists = wfFileDao.find((Integer) compFileItem.getFile().getId());
                                                       if (itemExists != null) {
                                                           WorkflowFileItem fileItem = wfFileDao.get(
                                                               (Integer) compFileItem.getFile().getId());
                                                           if (fileItem != null) {
                                                               if (fileItem.getOwner() != null
                                                                   && loggedInUserItem.getId()
                                                                       .equals(fileItem.getOwner().getId())) {
                                                                   // owner of file, use LEVEL_EDIT
                                                                   authLevel = WorkflowItem.LEVEL_EDIT;
                                                               } else {
                                                                   // If an import component inside a global workflow
                                                                   // has a different owner owner, do not allow view
                                                                   // until the file is attached to a dataset.
                                                                   if (fileItem.getFilePath().matches("workflows" + "/" + workflowItem.getId() + "/Import[a-zA-Z0-9_\\-]+/output/")
                                                                           || fileItem.getFilePath().matches("workflows" + "/" + workflowItem.getId() + "/Data[a-zA-Z0-9_\\-]+/output/")) {
                                                                       // If this is an Import component, the file is not owned by this user,
                                                                       // and the workflow is not shared, then they do not have access to this file.
                                                                       authLevel = null;
                                                                   } else if (fileItem.getFilePath().matches("workflows" + "/" + workflowItem.getId() + "/[a-zA-Z0-9_\\-]+/output/")) {
                                                                       // Not an import so this is one of the files generated by the workflow, itself.
                                                                       // (Not uploads)
                                                                       authLevel = WorkflowItem.LEVEL_VIEW;
                                                                   } else {
                                                                       // Since the file belongs to a component and not a dataset,
                                                                       // we shouldn't allow the user to access this file outright.
                                                                       authLevel = null;
                                                                   }
                                                               }
                                                           }
                                                       }
                                                   }
                                               }

                                               if (authLevel == null) {
                                                   minLevel = null;
                                                   break;
                                               } else if (minLevel != null) {
                                                   if (authLevel.equalsIgnoreCase(WorkflowItem.LEVEL_VIEW)
                                                           || minLevel.equalsIgnoreCase(START_SYMBOL)) {
                                                       minLevel = authLevel;
                                                   } else if (authLevel.equalsIgnoreCase(WorkflowItem.LEVEL_EDIT)
                                                           && !minLevel.equalsIgnoreCase(WorkflowItem.LEVEL_VIEW)) {
                                                       minLevel = authLevel;
                                                   } else {
                                                       minLevel = null;
                                                   }
                                               }

                                           }
                                       }
                                   }

                                   if (loggedInUserItem.getAdminFlag()) {
                                       if (loggedInUserItem.getId().equals(workflowItem.getOwner().getId())) {
                                           minLevel = WorkflowItem.LEVEL_EDIT;
                                       } else if (!loggedInUserItem.getId().equals(workflowItem.getOwner().getId())) {
                                           minLevel = WorkflowItem.LEVEL_VIEW;
                                       }
                                   }
                               }

                        }
                    }
                }
            }
        }
        if (minLevel != null && minLevel.equalsIgnoreCase(START_SYMBOL)) {
            minLevel = null;
            if (workflowItem.getOwner().getId().equals(loggedInUserItem.getId())) {
                return WorkflowItem.LEVEL_EDIT;
            }
        }

        return minLevel;
    }

   public static HashMap<String, String> getComponentAccessMap(
            UserItem loggedInUserItem, String baseDir, WorkflowItem workflowItem) {
        HashMap<String, String> accessMap = new HashMap<String, String>();
        Integer[][] adjMat = null;
        DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
        AuthorizationDao authDao = DaoFactory.DEFAULT.getAuthorizationDao();
        ComponentFileDao cfDao = DaoFactory.DEFAULT.getComponentFileDao();
        WorkflowComponentAdjacencyDao wfcAdjDao =
            DaoFactory.DEFAULT.getWorkflowComponentAdjacencyDao();
        WorkflowComponentInstancePersistenceDao wfcipDao =
            DaoFactory.DEFAULT.getWorkflowComponentInstancePersistenceDao();
        List<WorkflowComponentAdjacencyItem> wfcAdjItems =
            wfcAdjDao.findByWorkflow(workflowItem);

        // Only get them if they don't exist.
        if (wfcAdjItems == null || wfcAdjItems.isEmpty()) {
            wfcAdjItems = ConnectionHelper.updateAdjacencyList(baseDir, workflowItem, loggedInUserItem);
        }
        // Setup adjacency matrix
        if (wfcAdjItems != null && !wfcAdjItems.isEmpty()) {
            adjMat = new Integer[wfcAdjItems.size()][wfcAdjItems.size()];

            for (Integer i = 0; i < wfcAdjItems.size(); i++) {
                for (Integer j = 0; j < wfcAdjItems.size(); j++) {

                    String childId = wfcAdjItems.get(i).getChildId();
                    String compId = wfcAdjItems.get(j).getComponentId();
                    if (childId != null && compId != null && compId.equalsIgnoreCase(childId)) {
                        adjMat[i][j] = 1;
                    } else if (i == j) {
                        adjMat[i][j] = 1;
                    } else {
                        adjMat[i][j] = FLOYD_WARSHALL_MAX_DIST;
                    }
                }
            }

            // Run Floyd-warshall algorithm to get all-pairs shortest paths
            for (Integer i = 0; i < wfcAdjItems.size(); i++) {
                for (Integer j = 0; j < wfcAdjItems.size(); j++) {
                    for (Integer k = 0; k < wfcAdjItems.size(); k++) {
                        if (adjMat[j][i] != FLOYD_WARSHALL_MAX_DIST && adjMat[i][k] != FLOYD_WARSHALL_MAX_DIST
                            && adjMat[j][k] > adjMat[j][i] + adjMat[i][k]) {
                            adjMat[j][k] = adjMat[j][i] + adjMat[i][k];
                        }
                    }
                }
            }

            for (Integer j = 0; j < wfcAdjItems.size(); j++) {
                String descendantId = wfcAdjItems.get(j).getComponentId();

                String minLevel = START_SYMBOL;
                for (Integer i = 0; i < wfcAdjItems.size(); i++) {
                    // j = descendant, i = ancestor
                    String ancestorId = wfcAdjItems.get(i).getComponentId();
                    // Only imports and generators without input step into this conditional block:
                    if ((ancestorId.equalsIgnoreCase(descendantId) // Either the descendant is the ancestor,
                            || adjMat[i][j] < FLOYD_WARSHALL_MAX_DIST) // or there is a path to the import
                        ) { /*mck2&& (wfcAdjItems.get(i).getComponentId().matches("Import.*")
                        || wfcAdjItems.get(i).getComponentId().matches("Generate.*"))) { */




                        List<ComponentFileItem> compFileItems = cfDao.findImportByComponent(workflowItem, ancestorId);
                        if (loggedInUserItem != null && !((String) loggedInUserItem.getId()).equals(UserItem.DEFAULT_USER)) {
                               if (workflowItem.getGlobalFlag()
                                   || (loggedInUserItem != null
                                       && loggedInUserItem.getId().equals(workflowItem.getOwner().getId()))) {

                                   if (compFileItems != null && loggedInUserItem != null) {
                                       // If any workflow imports are not accessible to this user,
                                       // then break out of the loop and return null.
                                       for (ComponentFileItem compFileItem : compFileItems) {
                                           String authLevel = null;
                                           compFileItem = cfDao.get((Long) compFileItem.getId());

                                               WorkflowComponentInstancePersistenceItem wfcipItem =
                                                   wfcipDao.findByWorkflowAndId(workflowItem, compFileItem.getComponentId());

                                               if (wfcipItem != null && wfcipItem.isDirty()
                                                       && !loggedInUserItem.getId().equals(workflowItem.getOwner().getId())) {
                                                   authLevel = null;
                                               } else {
                                               if (compFileItem.getDataset() != null) {
                                                   DatasetItem dsItem = datasetDao.get((Integer) compFileItem.getDataset().getId());
                                                   // dataset authorization trumps file ownership
                                                   authLevel = authDao.getAuthLevel(
                                                       loggedInUserItem, dsItem);
                                                   if (authLevel != null && (loggedInUserItem.getId()
                                                       .equals(workflowItem.getOwner().getId()))) {
                                                       // owner of file, use LEVEL_EDIT
                                                       authLevel = WorkflowItem.LEVEL_EDIT;
                                                   } else if (authLevel != null) {
                                                       // global workflow, use LEVEL_VIEW
                                                       authLevel = WorkflowItem.LEVEL_VIEW;
                                                   } else {
                                                       authLevel = null; // no permissions, use workflow stripped of files
                                                   }

                                               } else {
                                                   // The workflow is shared, but this file is not in a dataset, use ownership.
                                                   if (compFileItem.getFile() != null) {
                                                       WorkflowFileDao wfFileDao = DaoFactory.DEFAULT.getWorkflowFileDao();
                                                       WorkflowFileItem itemExists = wfFileDao.find((Integer) compFileItem.getFile().getId());
                                                       if (itemExists != null) {
                                                           WorkflowFileItem fileItem = wfFileDao.get(
                                                               (Integer) compFileItem.getFile().getId());
                                                           if (fileItem != null) {
                                                               if (fileItem.getOwner() != null
                                                                   && loggedInUserItem.getId()
                                                                       .equals(fileItem.getOwner().getId())) {
                                                                   // owner of file, use LEVEL_EDIT
                                                                   authLevel = WorkflowItem.LEVEL_EDIT;
                                                               } else {
                                                                   // If an import component inside a global workflow
                                                                   // has a different owner owner, do not allow view
                                                                   // until the file is attached to a dataset.
                                                                   if (fileItem.getFilePath().matches("workflows" + "/" + workflowItem.getId() + "/Import[a-zA-Z0-9_\\-]+/output/")
                                                                               || fileItem.getFilePath().matches("workflows" + "/" + workflowItem.getId() + "/Data[a-zA-Z0-9_\\-]+/output/")) {
                                                                       // If this is an Import component, the file is not owned by this user,
                                                                       // and the workflow is not shared, then they do not have access to this file.
                                                                       authLevel = null;
                                                                   } else if (fileItem.getFilePath().matches("workflows" + "/" + workflowItem.getId() + "/[a-zA-Z0-9_\\-]+/output/")) {
                                                                       // Not an import so this is one of the files generated by the workflow, itself.
                                                                       // (Not uploads)
                                                                       authLevel = WorkflowItem.LEVEL_VIEW;
                                                                   }
                                                               }
                                                           }
                                                       }
                                                   }
                                               }
                                               }

                                           if (authLevel == null) {
                                               minLevel = null;
                                               break;
                                           } else if (minLevel != null) {
                                               if (authLevel.equalsIgnoreCase(WorkflowItem.LEVEL_VIEW)
                                                       || minLevel.equalsIgnoreCase(START_SYMBOL)) {
                                                   minLevel = authLevel;
                                               } else if (authLevel.equalsIgnoreCase(WorkflowItem.LEVEL_EDIT)
                                                       && !minLevel.equalsIgnoreCase(WorkflowItem.LEVEL_VIEW)) {
                                                   minLevel = authLevel;
                                               } else {
                                                   minLevel = null;
                                               }
                                           }

                                       }
                                   }
                               }

                               if (loggedInUserItem.getAdminFlag()) {
                                   if (loggedInUserItem.getId().equals(workflowItem.getOwner().getId())) {
                                       minLevel = WorkflowItem.LEVEL_EDIT;
                                   } else if (!loggedInUserItem.getId().equals(workflowItem.getOwner().getId())) {
                                       minLevel = WorkflowItem.LEVEL_VIEW;
                                   }
                               }
                           }

                    }
                }
                if (minLevel != null && minLevel.equalsIgnoreCase(START_SYMBOL)) {
                    minLevel = null;
                }
               accessMap.put(descendantId, minLevel);
            }
        }


        return accessMap;
    }


}
