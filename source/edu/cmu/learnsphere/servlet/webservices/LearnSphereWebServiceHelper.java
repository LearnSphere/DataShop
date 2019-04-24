/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.learnsphere.servlet.webservices;

import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.INACCESSIBLE_WORKFLOW_ERR;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.INVALID_WORKFLOW_ERR;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.INVALID_XML_ERR;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.UNKNOWN_ERR;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.INVALID_PARAM_VAL_ERR;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.WORKFLOW_ALREADY_RUNNING_ERR;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.WORKFLOW_INITIALIZATION_ERR;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.REQUIRED_FIELD_MISSING_ERR;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.WORKFLOW_DEFINITION_ERR;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.WORKFLOW_UPLOAD_FILE_OVERSIZE;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.WORKFLOW_INTERNAL_FILE_ERR;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.jdom.JDOMException;
import org.jdom.filter.ElementFilter;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import edu.cmu.learnsphere.dto.WorkflowAnnotationDTO;
import edu.cmu.learnsphere.dto.WorkflowAnnotationsDTO;
import edu.cmu.learnsphere.dto.WorkflowAttachedDatasetsDTO;
import edu.cmu.learnsphere.dto.WorkflowComponentDTO;
import edu.cmu.learnsphere.dto.WorkflowComponentsDTO;
import edu.cmu.learnsphere.dto.WorkflowConnectionDTO;
import edu.cmu.learnsphere.dto.WorkflowConnectionsDTO;
import edu.cmu.learnsphere.dto.WorkflowDTO;
import edu.cmu.learnsphere.dto.WorkflowErrorsDTO;
import edu.cmu.learnsphere.dto.WorkflowImportDTO;
import edu.cmu.learnsphere.dto.WorkflowOptionDTO;
import edu.cmu.learnsphere.dto.WorkflowOptionsDTO;
import edu.cmu.learnsphere.dto.WorkflowOutputFileDTO;
import edu.cmu.learnsphere.dto.WorkflowOutputFilesDTO;
import edu.cmu.learnsphere.dto.WorkflowResultDTO;
import edu.cmu.learnsphere.servlet.webservices.LearnSphereWebService.DataAccessParam;
import edu.cmu.pslc.datashop.dao.ComponentFileDao;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.UserDao;
import edu.cmu.pslc.datashop.dao.WorkflowAnnotationDao;
import edu.cmu.pslc.datashop.dao.WorkflowComponentAdjacencyDao;
import edu.cmu.pslc.datashop.dao.WorkflowComponentDao;
import edu.cmu.pslc.datashop.dao.WorkflowComponentInstanceDao;
import edu.cmu.pslc.datashop.dao.WorkflowComponentInstancePersistenceDao;
import edu.cmu.pslc.datashop.dao.WorkflowDao;
import edu.cmu.pslc.datashop.dao.WorkflowDatasetMapDao;
import edu.cmu.pslc.datashop.dao.WorkflowFileDao;
import edu.cmu.pslc.datashop.dao.WorkflowPersistenceDao;
import edu.cmu.pslc.datashop.dao.WorkflowTagDao;
import edu.cmu.pslc.datashop.dao.WorkflowTagMapDao;
import edu.cmu.pslc.datashop.dto.DatasetDTO;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.servlet.workflows.ComponentHelper;
import edu.cmu.pslc.datashop.servlet.workflows.ComponentHierarchyHelper;
import edu.cmu.pslc.datashop.servlet.workflows.ConnectionHelper;
import edu.cmu.pslc.datashop.servlet.workflows.WorkflowAccessHelper;
import edu.cmu.pslc.datashop.servlet.workflows.WorkflowAnnotationHelper;
import edu.cmu.pslc.datashop.servlet.workflows.WorkflowFileHelper;
import edu.cmu.pslc.datashop.servlet.workflows.WorkflowFileUtils;
import edu.cmu.pslc.datashop.servlet.workflows.WorkflowHelper;
import edu.cmu.pslc.datashop.servlet.workflows.WorkflowIfaceHelper;
import edu.cmu.pslc.datashop.servlet.workflows.WorkflowImportHelper;
import edu.cmu.pslc.datashop.servlet.workflows.WorkflowPropertiesHelper;
import edu.cmu.pslc.datashop.servlet.workflows.WorkflowRowDto;
import edu.cmu.pslc.datashop.servlet.workflows.WorkflowXmlUtils;
import edu.cmu.pslc.datashop.util.DataShopInstance;
import edu.cmu.pslc.datashop.util.FileUtils;
import edu.cmu.pslc.datashop.workflows.ComponentFileItem;
import edu.cmu.pslc.datashop.workflows.ErrorMessageMap;
import edu.cmu.pslc.datashop.workflows.Workflow;
import edu.cmu.pslc.datashop.workflows.WorkflowAnnotationItem;
import edu.cmu.pslc.datashop.workflows.WorkflowComponentAdjacencyItem;
import edu.cmu.pslc.datashop.workflows.WorkflowComponentInstanceItem;
import edu.cmu.pslc.datashop.workflows.WorkflowComponentInstancePersistenceItem;
import edu.cmu.pslc.datashop.workflows.WorkflowDatasetMapId;
import edu.cmu.pslc.datashop.workflows.WorkflowDatasetMapItem;
import edu.cmu.pslc.datashop.workflows.WorkflowFileItem;
import edu.cmu.pslc.datashop.workflows.WorkflowItem;
import edu.cmu.pslc.datashop.workflows.WorkflowPersistenceItem;
import edu.cmu.pslc.datashop.workflows.WorkflowTagItem;
import edu.cmu.pslc.datashop.workflows.WorkflowTagMapId;
import edu.cmu.pslc.datashop.workflows.WorkflowTagMapItem;
import edu.cmu.pslc.datashop.workflows.WorkflowsMail;
import edu.cmu.pslc.datashop.servlet.webservices.WebServiceException;
import edu.cmu.pslc.datashop.servlet.webservices.WebServiceHelper;

/**
 * Web service methods requiring database access go here.
 *
 * @author Hui cheng
 * @version $Revision: 15980 $ <BR>
 *          Last modified by: $Author: hcheng $ <BR>
 *          Last modified on: $Date: 2019-04-02 00:48:54 -0400 (Tue, 02 Apr 2019) $
 * <!-- $KeyWordsOff: $ -->
 */
public class LearnSphereWebServiceHelper extends WebServiceHelper{
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());
    /** Default directory for the workflow components. */
    private static final String WF_COMPONENTS_DIR_DEFAULT = "/datashop/workflow_components";
    /** Workflow components directory is set via ManageInstance servlet. */
    private String workflowComponentsDir = WF_COMPONENTS_DIR_DEFAULT;
    /** DataShop instance id. */
    private static final Long DATASHOP_INSTANCE_ID = 1L;
    /** Format for the date range method, getDateRangeString. */
    public static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("MMM d, yyyy HH:mm:ss");
    //copied from AbstractServlet
    /** Max file size is 400MB. */
    private static final Integer MAX_FILE_SIZE = 400 * 1024 * 1024;
    /** Max file size is 1GB for DS Admins. */
    private static final Integer MAX_FILE_SIZE_ADMIN = 1000 * 1024 * 1024;
    public List<WorkflowDTO> workflowDTOsForUser(UserItem user,
                    Boolean mine, Boolean global, DataAccessParam dataAccess, boolean verbose)
                                    throws WebServiceException {
                List<WorkflowDTO> dtos = new ArrayList<WorkflowDTO>();
                Session session = null;
                try {
                    session = newSession();
                    UserItem attachedUser = attach(session, UserItem.class, user);
                    WorkflowDao wfDao = DaoFactory.DEFAULT.getWorkflowDao();
                    WorkflowHelper wfHelper = HelperFactory.DEFAULT.getWorkflowHelper();
                    List<WorkflowRowDto> rowDtos = new ArrayList<WorkflowRowDto>();
                    if (mine == null || mine) {
                            List<WorkflowRowDto> privateWorkflowDtos = wfDao.getWorkflowRowInfo(user, null, true);
                            for (WorkflowRowDto workflowDto : privateWorkflowDtos) {
                                    if (global != null && global.booleanValue() != workflowDto.getIsGlobal()) {
                                            continue;
                                    }
                                    rowDtos.add(workflowDto);
                            }
                    }
                    if (global == null || global) {
                            List<WorkflowRowDto> privateWorkflowDtos = wfDao.getWorkflowRowInfo(user, null, false);
                            for (WorkflowRowDto workflowDto : privateWorkflowDtos) {
                                    //if this rowDto is private and not mine, skip. this happens with admin user
                                    if (!workflowDto.getIsGlobal() && !workflowDto.getOwnerId().equals(user.getId()))
                                            continue;
                                    if (mine != null && mine && !workflowDto.getOwnerId().equals(user.getId()))
                                            continue;
                                    if (mine != null && !mine && workflowDto.getOwnerId().equals(user.getId()))
                                            continue;

                                    rowDtos.add(workflowDto);
                            }
                    }
                    List<WorkflowRowDto> newRowDtos = new ArrayList<WorkflowRowDto>();

                    for (WorkflowRowDto workflowDto : rowDtos) {
                            String thisDataAccess = WorkflowIfaceHelper.getDataAccess(workflowDto);
                            workflowDto.setDataAccessHtml(thisDataAccess);
                            //check data_access
                            if (dataAccess != null && !dataAccess.equals(DataAccessParam.ALL)
                                            && !dataAccess.getParamVal().equals(thisDataAccess)) {
                                    continue;
                            }
                            newRowDtos.add(workflowDto);
                    }
                    for (WorkflowRowDto workflowRowDto : newRowDtos) {
                            if (verbose) {
                                    //don't output item related fields, too expensive
                                    //WorkflowItem workflowItem = wfDao.find(workflowRowDto.getWorkflowId().longValue());
                                    //workflowItem = attach(session, WorkflowItem.class, workflowItem);
                                    try {
                                            dtos.add(workflowDTOForRowAndItem(workflowRowDto, null));

                                    } catch (ParserConfigurationException pce) {
                                            throw new WebServiceException(INVALID_XML_ERR, "XML parsing error for workflow " + workflowRowDto.getWorkflowId() + ". Error: " + pce);
                                    } catch (SAXException saxe) {
                                            throw new WebServiceException(INVALID_XML_ERR, "XML parsing error for workflow " + workflowRowDto.getWorkflowId() + ". Error: " + saxe);
                                    } catch (IOException ioe) {
                                            throw new WebServiceException(UNKNOWN_ERR, "IO exception for workflow " + workflowRowDto.getWorkflowId() + ". Error: " + ioe);
                                    } catch (NumberFormatException nfe) {
                                            throw new WebServiceException(INVALID_PARAM_VAL_ERR, "Number format error for workflow " + workflowRowDto.getWorkflowId() + ". Error: " + nfe);
                                    } catch (JSONException jsone) {
                                            throw new WebServiceException(UNKNOWN_ERR, "JSONException for workflow " + workflowRowDto.getWorkflowId() + " errors parsing. Error: " + jsone);
                                    }

                            } else {
                                    dtos.add(workflowDTOForRow(workflowRowDto));
                            }
                    }

                } finally {
                    if (session != null) {
                        session.close();
                    }
                }

                return dtos;
    }

    public WorkflowDTO workflowDTOForId(UserItem user, int workflowId, boolean verbose)
                                    throws WebServiceException {
                WorkflowDTO dto = null;
                Session session = null;
                try {
                        session = newSession();
                        UserItem attachedUser = attach(session, UserItem.class, user);
                        WorkflowDao wfDao = DaoFactory.DEFAULT.getWorkflowDao();
                        WorkflowRowDto rowDto = wfDao.getWorkflowRowInfo(workflowId);
                        if (rowDto != null) {
                                String owner = rowDto.getOwnerId();
                                boolean isGlobal = rowDto.getIsGlobal();

                                if (!isGlobal && !owner.equals(attachedUser.getId())) {
                                        throw new WebServiceException(INACCESSIBLE_WORKFLOW_ERR, "Workflow "
                                                        + workflowId + " is not accessible.");
                                }
                                if (verbose) {
                                        WorkflowDao aWfDao = DaoFactory.DEFAULT.getWorkflowDao();
                                        WorkflowItem workflowItem = aWfDao.get((long)workflowId);
                                        WorkflowItem attachedWorkflowItem = attach(session, WorkflowItem.class, workflowItem);
                                        try {
                                                dto = workflowDTOForRowAndItem(rowDto, attachedWorkflowItem);
                                        } catch (ParserConfigurationException pce) {
                                                throw new WebServiceException(INVALID_XML_ERR, "XML parsing error for workflow " + workflowId + ". Error: " + pce);
                                        } catch (SAXException saxe) {
                                                throw new WebServiceException(INVALID_XML_ERR, "XML parsing error for workflow " + workflowId + ". Error: " + saxe);
                                        } catch (IOException ioe) {
                                                throw new WebServiceException(UNKNOWN_ERR, "IO exception for workflow " + workflowId + ". Error: " + ioe);
                                        } catch (NumberFormatException nfe) {
                                                throw new WebServiceException(INVALID_PARAM_VAL_ERR, "Number format error for workflow " + workflowId + ". Error: " + nfe);
                                        } catch (JSONException jsone) {
                                                throw new WebServiceException(UNKNOWN_ERR, "JSONException for workflow " + workflowId + " errors parsing. Error: " + jsone);
                                        }

                                } else {
                                        dto = workflowDTOForRow(rowDto);
                                }

                        } else {
                                throw new WebServiceException(INVALID_WORKFLOW_ERR, "Workflow " + workflowId
                                                + " is not valid.");
                        }

                    } finally {
                        if (session != null) {
                            session.close();
                        }
                    }

                    return dto;
    }

    public Hashtable<String, List<WorkflowFileItem>> workflowFilesForId(UserItem user, int workflowId, String fileType, String baseDir)
                    throws WebServiceException {
        Hashtable<String, List<WorkflowFileItem>> workflowFileItems = new Hashtable<String, List<WorkflowFileItem>>();
        Session session = null;
        String fileId = null;
        try {
                session = newSession();
                UserItem attachedUser = attach(session, UserItem.class, user);
                WorkflowFileDao wfFileDao = DaoFactory.DEFAULT.getWorkflowFileDao();
                WorkflowDTO dto = workflowDTOForId(attachedUser, workflowId, true);

                String owner = dto.getOwner();
                boolean isGlobal = Boolean.parseBoolean(dto.isGlobal());
                if (!isGlobal && !owner.equals(attachedUser.getId())) {
                        throw new WebServiceException(INACCESSIBLE_WORKFLOW_ERR, "Workflow "
                                        + workflowId + " is not accessible.");
                }
                WorkflowComponentsDTO components = dto.getComponents();
                if (components == null)
                        return null;
                for (WorkflowComponentDTO thisComponent : components.getComponents()) {
                        List<WorkflowFileItem> thisCompFileItems = workflowFileItems.get(thisComponent.getId());
                        if (fileType == null || fileType.equals("input")) {
                                WorkflowImportDTO importFileDto = thisComponent.getImportFile();
                                if (importFileDto != null) {
                                        fileId = importFileDto.getId();
                                        WorkflowFileItem fileItem = wfFileDao.find(Integer.parseInt(fileId));
                                        WorkflowFileItem attachedFileItem = attach(session, WorkflowFileItem.class, fileItem);
                                        if (attachedFileItem != null) {
                                                if (isAccessible(user, attachedFileItem, baseDir)) {
                                                        if (thisCompFileItems == null) {
                                                                thisCompFileItems = new ArrayList<WorkflowFileItem>();
                                                                workflowFileItems.put(thisComponent.getId(), thisCompFileItems);
                                                        }
                                                        thisCompFileItems.add(attachedFileItem);
                                                }
                                        }
                                }
                        }
                        if (fileType == null || fileType.equals("output")) {
                                WorkflowResultDTO resultDto = thisComponent.getResult();
                                if (resultDto != null) {
                                        WorkflowOutputFilesDTO outputFilesDto = resultDto.getOutputFiles();
                                        if (outputFilesDto != null && outputFilesDto.getOutputFiles() != null) {
                                                for (WorkflowOutputFileDTO outputFileDto : outputFilesDto.getOutputFiles()) {
                                                        fileId = outputFileDto.getPath();
                                                        WorkflowFileItem fileItem = wfFileDao.find(Integer.parseInt(fileId));
                                                        WorkflowFileItem attachedFileItem = attach(session, WorkflowFileItem.class, fileItem);
                                                        if (attachedFileItem != null)
                                                                if (isAccessible(user, attachedFileItem, baseDir)) {
                                                                        if (thisCompFileItems == null) {
                                                                                thisCompFileItems = new ArrayList<WorkflowFileItem>();
                                                                                workflowFileItems.put(thisComponent.getId(), thisCompFileItems);
                                                                        }
                                                                        thisCompFileItems.add(attachedFileItem);
                                                                }
                                                        }
                                                }
                                        }

                        }
                }
        } catch (NumberFormatException nfe) {
                throw new WebServiceException(UNKNOWN_ERR, "Number format error for fileId: " + fileId + " in workflow: " + workflowId + ". Error: " + nfe);
        } catch (IOException ioe) {
                throw new WebServiceException(UNKNOWN_ERR, "IOException for fileId: " + fileId + " in workflow: " + workflowId + ". Error: " + ioe);
        } finally {
                if (session != null) {
                    session.close();
                }
       }

        return workflowFileItems;
    }

    /**
     * Translate the workflowRowDTO into a workflow DTO.
     *
     * @param row the workflowRowDto
     * @return the row dto translated into a DTO
     */
    private WorkflowDTO workflowDTOForRow (WorkflowRowDto row) {
        WorkflowDTO dto = new WorkflowDTO();
        dto.setId(row.getWorkflowId().longValue());
        dto.setName(row.getWorkflowName());
        dto.setOwner(row.getOwnerId());
        dto.setLastUpdated(row.getLastUpdated());
        dto.setState(row.getState());
        dto.setDataAccess(row.getDataAccessHtml());
        dto.setGlobal(row.getIsGlobal());
        return dto;
    }

    /**
     * Translate the workflowRowDTO and workflowItem into a workflow DTO.
     * This is for the verbose situation
     *
     * @param user userItem
     * @param row workflowRowDto
     * @param workflowItem the workflow item
     *
     * @return the item and row translated into a DTO
     */
    private WorkflowDTO workflowDTOForRowAndItem( WorkflowRowDto row, WorkflowItem workflowItem)
                    throws ParserConfigurationException, SAXException, IOException, NumberFormatException, JSONException {
            WorkflowDTO dto = workflowDTOForRow(row);
            dto.setDescription(row.getDescription());
            logger.info("Workflow service trying to access workflow id: " + dto.getId());

            //process attached datasets, don't do this when workflowItem is null
            if (workflowItem != null) {
                    List<DatasetItem> datasets = workflowItem.getDatasetsExternal();
                    WorkflowAttachedDatasetsDTO datasetsDto = null;
                    if (datasets != null && datasets.size() > 0) {
                            datasetsDto = new WorkflowAttachedDatasetsDTO();
                            dto.setAttachedDatasets(datasetsDto);
                            for (DatasetItem dsItem : datasets) {
                                    DatasetDTO dsDto = new DatasetDTO();
                                    dsDto.setId((Integer)dsItem.getId());
                                    dsDto.setName(dsItem.getDatasetName());
                                    if (dsItem.getProject() != null)
                                            dsDto.setProject(dsItem.getProject().getProjectName());
                                    dto.addAttachedDataset(dsDto);
                            }
                    }
            }
            //process annotation, components
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(row.getWorkflowXml()));
            Document doc = builder.parse(is);
            //annotation
            NodeList nodes = doc.getElementsByTagName("annotation");
            WorkflowAnnotationsDTO annotations = null;
            for (int i = 0; i < nodes.getLength(); i++) {
                    if (i == 0) {
                            annotations = new WorkflowAnnotationsDTO();
                            dto.setAnnotations(annotations);
                    }
                    WorkflowAnnotationDTO workflowAnnotationDTO = new WorkflowAnnotationDTO();
                    Element element = (Element) nodes.item(i);
                    String cdText = getXmlTextFromTag (element, "text");
                    if (cdText != null) {
                            workflowAnnotationDTO.setAnnotationText(cdText);
                    }
                    cdText = getXmlTextFromTag (element, "annotation_id");
                    if (cdText != null) {
                            workflowAnnotationDTO.setId(Long.parseLong(cdText));
                    }
                    dto.addAnnotation(workflowAnnotationDTO);
            }

            //component
            nodes = doc.getElementsByTagName("component");
            WorkflowComponentsDTO components = null;
            for (int i = 0; i < nodes.getLength(); i++) {
                    if (i == 0) {
                            components = new WorkflowComponentsDTO();
                            dto.setComponents(components);
                    }
                    WorkflowComponentDTO workflowComponentDTO = new WorkflowComponentDTO();
                    Element element = (Element) nodes.item(i);
                    String cdText = getXmlTextFromTag (element, "component_id");
                    if (cdText != null) {
                            workflowComponentDTO.setId(cdText);
                    }
                    cdText = getXmlTextFromTag (element, "component_id_human");
                    if (cdText != null) {
                            workflowComponentDTO.setIdHuman(cdText);
                    }
                    cdText = getXmlTextFromTag (element, "component_name");
                    if (cdText != null) {
                            workflowComponentDTO.setName(cdText);
                    }
                    cdText = getXmlTextFromTag (element, "component_type");
                    if (cdText != null) {
                            workflowComponentDTO.setType(cdText);
                    }
                    dto.addComponent(workflowComponentDTO);
                    //connections for this component
                    NodeList connNodes = element.getElementsByTagName("connection");
                    WorkflowConnectionsDTO connections = null;
                    for (int j = 0; j < connNodes.getLength(); j++) {
                            if (j == 0) {
                                    connections = new WorkflowConnectionsDTO();
                                    workflowComponentDTO.setConnections(connections);
                            }
                            WorkflowConnectionDTO workflowConnectionDTO = new WorkflowConnectionDTO();
                            Element connEle = (Element) connNodes.item(j);
                            cdText = getXmlTextFromTag (connEle, "index");
                            if (cdText != null) {
                                    workflowConnectionDTO.setIndex(cdText);
                            }
                            cdText = getXmlTextFromTag (connEle, "to");
                            if (cdText != null) {
                                    workflowConnectionDTO.setTo(cdText);
                            }
                            cdText = getXmlTextFromTag (connEle, "from");
                            if (cdText != null) {
                                    workflowConnectionDTO.setFrom(cdText);
                            }
                            cdText = getXmlTextFromTag (connEle, "tindex");
                            if (cdText != null) {
                                    workflowConnectionDTO.setTindex(cdText);
                            }
                            cdText = getXmlTextFromTag (connEle, "frindex");
                            if (cdText != null) {
                                    workflowConnectionDTO.setFrindex(cdText);
                            }
                            connections.addConnection(workflowConnectionDTO);
                    }
                    //options for this component, there should be only one tag named options
                    //this will handle import and other options
                    NodeList optionNodes = element.getElementsByTagName("options");
                    WorkflowOptionsDTO options = null;
                    if (optionNodes != null && optionNodes.getLength() > 0) {
                            Element optionsEle = (Element)optionNodes.item(0);
                            NodeList optionChildNodes = optionsEle.getChildNodes();
                            for (int x = 0; x < optionChildNodes.getLength(); x++) {
                                    Node node = optionChildNodes.item(x);
                                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                                            String tagName = node.getNodeName();
                                            if (tagName.equalsIgnoreCase("left") || tagName.equalsIgnoreCase("top") || tagName.equalsIgnoreCase("files"))
                                                    continue;
                                            else if (tagName.equalsIgnoreCase("import")) {
                                                    //parse import
                                                    String importStr = node.getFirstChild().getNodeValue();
                                                    importStr = importStr.trim().substring(importStr.indexOf("{") + 1, importStr.lastIndexOf("}"));
                                                    List<String> importItems = Arrays.asList(importStr.split(","));
                                                    WorkflowImportDTO workflowImportDTO = null;
                                                    if (importItems != null && importItems.size() > 0) {
                                                            workflowImportDTO = new WorkflowImportDTO();
                                                            workflowComponentDTO.setImportFile(workflowImportDTO);
                                                    }
                                                    for (String item : importItems) {
                                                            List<String> importVarVals = Arrays.asList(item.split(":"));
                                                            if (importVarVals != null && importVarVals.size() == 2) {
                                                                    String var = importVarVals.get(0);
                                                                    var = var.trim().substring(var.indexOf("\"") + 1, var.lastIndexOf("\""));
                                                                    String val = importVarVals.get(1);
                                                                    val = val.trim().substring(val.indexOf("\"") + 1, val.lastIndexOf("\""));
                                                                    if ("fileName".equals(var))
                                                                            workflowImportDTO.setName(val);
                                                                    else if ("importFileType".equals(var))
                                                                            workflowImportDTO.setType(val);
                                                                    else if ("fileTypeSelection".equals(var))
                                                                            workflowImportDTO.setFileSelectionType(val);
                                                                    else if ("importFileNameTitle".equals(var))
                                                                            workflowImportDTO.setTitle(val);
                                                                    else if ("fileId".equals(var))
                                                                            workflowImportDTO.setId(val);
                                                                    else if ("datasetLink".equals(var) && !val.equals(""))
                                                                            workflowImportDTO.setDatasetLink(val);
                                                                    else if ("datasetName".equals(var) && !val.equals(""))
                                                                            workflowImportDTO.setDatasetName(val);
                                                            }
                                                    }

                                            } else {
                                                    if (options == null) {
                                                            options = new WorkflowOptionsDTO();
                                                            workflowComponentDTO.setOptions(options);
                                                    }
                                                    WorkflowOptionDTO optionDto = new WorkflowOptionDTO();
                                                    optionDto.setName(tagName);
                                                    String optionVal = null;
                                                    if (node != null && node.getFirstChild() != null )
                                                            optionVal= node.getFirstChild().getNodeValue();
                                                    if (optionVal == null)
                                                            optionVal = "";
                                                    optionDto.setValue(optionVal);
                                                    options.addOption(optionDto);
                                            }
                                    }
                            }
                    }
            }
            //if workflow is null don't return result or error. takes too long
            if (workflowItem == null || components == null || (workflowItem.getResults() == null || workflowItem.getResults().trim().equals("")))
                    return dto;
            is = new InputSource(new StringReader("<outputs_tag>" + workflowItem.getResults() + "</outputs_tag>"));
            Document outputDoc = builder.parse(is);
            Element parent = (Element)outputDoc.getElementsByTagName("outputs_tag").item(0);
            NodeList outputNodes = parent.getChildNodes();
            for (int i = 0; i < outputNodes.getLength(); i++) {
                    //this is the output element
                    if (outputNodes.item(i).getChildNodes().getLength() == 0)
                            continue;
                    Element outputEle = (Element)outputNodes.item(i);
                    String componentId = getXmlTextFromTag (outputEle, "component_id");
                    List<WorkflowComponentDTO> allComponents = components.getComponents();
                    for (WorkflowComponentDTO thisComponent : allComponents) {
                            if (componentId.equals(thisComponent.getId())) {
                                    WorkflowResultDTO result = thisComponent.getResult();
                                    if (result == null) {
                                            result = new WorkflowResultDTO();
                                            thisComponent.setResult(result);
                                    }
                                    String es = getXmlTextFromTag (outputEle, "elapsed_seconds");
                                    if (es != null && !es.equals(""))
                                            result.setElapsedSeconds(es);

                                    //process output files for component that is not import
                                    if (!thisComponent.getType().equals("data") && thisComponent.getImportFile() == null) {
                                            NodeList outputFileNodeList = outputEle.getElementsByTagName("files");
                                            if (outputFileNodeList != null && outputFileNodeList.getLength() > 0) {
                                                    Element outputFileEle = (Element)outputFileNodeList.item(0);
                                                    WorkflowOutputFilesDTO outputFiles = result.getOutputFiles();
                                                    if (outputFiles == null) {
                                                            outputFiles = new WorkflowOutputFilesDTO();
                                                            result.setOutputFiles(outputFiles);
                                                    }
                                                    //WorkflowOutputFileDTO workflowOutputFileDTO = new WorkflowOutputFileDTO();
                                                    //outputFiles.addOutputFile(workflowOutputFileDTO);
                                                    NodeList outputFileChildNodes = outputFileEle.getChildNodes();
                                                    for (int x = 0; x < outputFileChildNodes.getLength(); x++) {
                                                            Node node = outputFileChildNodes.item(x);
                                                            if (node.getNodeType() == Node.ELEMENT_NODE) {
                                                                    WorkflowOutputFileDTO workflowOutputFileDTO = new WorkflowOutputFileDTO();
                                                                    outputFiles.addOutputFile(workflowOutputFileDTO);
                                                                    String tagName = node.getNodeName();
                                                                    workflowOutputFileDTO.setType(tagName);
                                                                    NodeList subNodes = node.getChildNodes();
                                                                    for (int y = 0; y < subNodes.getLength(); y++) {
                                                                            Node subNode = subNodes.item(y);
                                                                            if (subNode.getNodeType() == Node.ELEMENT_NODE) {
                                                                                    String subNodeName = subNode.getNodeName();
                                                                                    String subNodeVal = subNode.getFirstChild().getNodeValue();
                                                                                    if (subNodeVal != null) {
                                                                                            if ("file_path".equals(subNodeName)) {
                                                                                                    workflowOutputFileDTO.setPath(subNodeVal);
                                                                                            } else if ("file_name".equals(subNodeName)) {
                                                                                                    workflowOutputFileDTO.setName(subNodeVal);
                                                                                            } else if ("label".equals(subNodeName)) {
                                                                                                    workflowOutputFileDTO.setLabel(subNodeVal);
                                                                                            } else if ("index".equals(subNodeName)) {
                                                                                                    workflowOutputFileDTO.setIndex(subNodeVal);
                                                                                            }
                                                                                    }

                                                                            }
                                                                    }
                                                            }

                                                    }
                                            }
                                    }
                                    break;
                            }
                    }
            }

            //process errors for result
            WorkflowComponentInstanceDao WorkflowComponentInstanceDao = DaoFactory.DEFAULT.getWorkflowComponentInstanceDao();
            List<WorkflowComponentInstanceItem> workflowComponentInstances =  WorkflowComponentInstanceDao.findByWorkflow(workflowItem);
            if (workflowComponentInstances != null) {
                    for (WorkflowComponentInstanceItem instance : workflowComponentInstances) {
                            //loop through all components to set state and errors
                            if (dto.getComponents() != null && dto.getComponents().getComponents() != null) {
                                    for (WorkflowComponentDTO wfComponentDto : dto.getComponents().getComponents()) {
                                            if (wfComponentDto.getId().equals(instance.getComponentName())) {
                                                    wfComponentDto.setState(instance.getState());
                                                    if (instance.getErrors() != null && !instance.getErrors().trim().equals("")) {
                                                            WorkflowErrorsDTO errorsDto = wfComponentDto.getErrors();
                                                            if (errorsDto == null) {
                                                                    errorsDto = new WorkflowErrorsDTO();
                                                                    wfComponentDto.setErrors(errorsDto);
                                                            }
                                                            //parse error as json
                                                            JSONObject obj = new JSONObject(instance.getErrors());
                                                            JSONArray arr = obj.getJSONArray("component_message_map");
                                                            if (arr != null && arr.length() > 0 && arr.getJSONObject(0) != null) {
                                                                    JSONObject componentJSONobj = arr.getJSONObject(0).getJSONObject(instance.getComponentName());
                                                                    if (componentJSONobj != null) {
                                                                            JSONArray errors = componentJSONobj.getJSONArray("component_message_container");
                                                                            if (errors != null) {
                                                                                    for (int i = 0; i < errors.length(); i++) {
                                                                                            String errorMsg = errors.getJSONObject(i).getString("error");
                                                                                            if (errorMsg != null && !errorMsg.equals("")) {
                                                                                                    errorsDto.addError(errorMsg);
                                                                                            }
                                                                                    }
                                                                            }
                                                                    }
                                                            }

                                                    }
                                                    break;
                                            }
                                    }
                            }
                    }
            }
            return dto;
    }

    public void deleteAWorkflow(UserItem user, int workflowId, String baseDir) throws WebServiceException {
            Session session = null;
            try {
                    session = newSession();
                    UserItem attachedUser = attach(session, UserItem.class, user);
                    WorkflowDao wfDao = DaoFactory.DEFAULT.getWorkflowDao();
                    WorkflowFileDao wfFileDao = DaoFactory.DEFAULT.getWorkflowFileDao();
                    ComponentFileDao wmfDao = DaoFactory.DEFAULT.getComponentFileDao();
                    WorkflowHelper workflowHelper = HelperFactory.DEFAULT.getWorkflowHelper();

                    WorkflowItem workflowItem = wfDao.get((long)workflowId);
                    if (workflowItem == null)
                            throw new WebServiceException(INVALID_WORKFLOW_ERR, "Workflow " + workflowId
                                    + " is not valid.");
                    WorkflowItem attachedWorkflowItem = attach(session, WorkflowItem.class, workflowItem);
                    //only owner or admin can delete
                    if (attachedWorkflowItem.getOwner() == null
                                    || (!attachedWorkflowItem.getOwner().getId().equals(attachedUser.getId())
                                        && !attachedUser.getAdminFlag()))
                            throw new WebServiceException(INACCESSIBLE_WORKFLOW_ERR, "Workflow "
                                            + workflowId + " is not accessible.");
                    //after all details are taken of:
                    logger.info("Deleting workflow (" + attachedWorkflowItem.getId() + ")");

                    /*
                    File workflowsDir = new File(WorkflowHelper.getStrictDirFormat(baseDir)
                                + WorkflowHelper.getWorkflowsDir((long)workflowId));
                    if (!FileUtils.deleteFile(workflowsDir)) {
                            logger.error("Could not delete " + workflowsDir.getAbsolutePath());
                            //throw errors or go on?
                    }*/
                    try {
                            logger.trace("Deleting files for workflow (" + workflowId + ")");
                            List<ComponentFileItem> cfItems = wmfDao.findByWorkflow(attachedWorkflowItem);

                            for (ComponentFileItem wmfItem : cfItems) {
                                    WorkflowFileItem fileItem = wfFileDao.get((Integer) wmfItem.getFile().getId());
                                    wfFileDao.delete(fileItem);
                            }
                            // These should be deleted when workflow_file records are deleted, but let's be sure.
                            deleteComponentFileItems(wmfDao, attachedWorkflowItem, baseDir);
                            // Delete associated workflow Tags
                            deleteAllWorkflowTags(attachedWorkflowItem);
                            // Delete entire workflow directory
                            try {
                                //workflowHelper.deleteWorkflowDir(attachedWorkflowItem, baseDir);
                                    WorkflowFileUtils.deleteWorkflowDir(workflowItem, baseDir);
                                    logger.trace("Workflow files for (" + workflowId + ") deleted.");
                            } catch (IOException e) {
                                logger.error("Could not delete workflow directory for " + attachedWorkflowItem.getWorkflowName() + " ("
                                        + attachedWorkflowItem.getId() + ")");
                                //don't throw we service error because has to go on with other database delete. And not able to delete files will not break anything big
                            }
                            // Delete the workflow item from the database
                            wfDao.delete(workflowItem);
                            logger.info("Workflow " + workflowId + " is deleted successfully.");

                        } catch (Exception e) {
                            logger.error("Could not delete workflow " + workflowId + ". Please resolve manually. Error: " + e.toString());
                            throw new WebServiceException(UNKNOWN_ERR, "Error found for deleting workflow " + workflowId + ". Error: " + e.getMessage());
                        }
                } finally {
                    if (session != null) {
                        session.close();
                    }
                }
    }


    public void runAWorkflow(UserItem user, final long workflowId, final String baseDir) throws WebServiceException {
            Session session = null;
            try {
                    session = newSession();
                    final UserItem attachedUser = attach(session, UserItem.class, user);
                    WorkflowDao wfDao = DaoFactory.DEFAULT.getWorkflowDao();

                    WorkflowItem workflowItem = wfDao.get(workflowId);
                    if (workflowItem == null)
                            throw new WebServiceException(INVALID_WORKFLOW_ERR, "Workflow " + workflowId
                                    + " is not valid.");
                    WorkflowItem attachedWorkflowItem = attach(session, WorkflowItem.class, workflowItem);
                    //only owner can run a workflow
                    if (attachedWorkflowItem.getOwner() == null
                                    || (!attachedWorkflowItem.getOwner().getId().equals(attachedUser.getId())))
                            throw new WebServiceException(INACCESSIBLE_WORKFLOW_ERR, "Workflow "
                                            + workflowId + " is not accessible.");

                    if (attachedWorkflowItem.getState().equalsIgnoreCase(WorkflowItem.WF_STATE_RUNNING)
                            || attachedWorkflowItem.getState().equalsIgnoreCase(WorkflowItem.WF_STATE_RUNNING_DIRTY)) {
                            throw new WebServiceException(WORKFLOW_ALREADY_RUNNING_ERR, "Workflow "
                                            + workflowId + " is already running. Please wait for the process to complete.");
                    }
                    //catch initialization error
                    Workflow workflow = new Workflow(workflowId);
                    try {
                        workflow.init(WorkflowFileUtils.getStrictDirFormat(baseDir),
                                        WorkflowFileUtils.getWorkflowsDir(workflowId), attachedUser);
                    } catch (Exception e) {
                            throw new WebServiceException(WORKFLOW_INITIALIZATION_ERR, "Workflow "
                                            + workflowId + " has initialization error.");
                    }

                    //after all details are taken of, run workflow in another thread
                    logger.info("Set workflow (" + attachedWorkflowItem.getId() + ") to run");
                    new Thread(new Runnable() {
                            public void run() {
                                runWorkflowInNewThread(attachedUser, workflowId, baseDir);
                            }
                        }).start();
                } finally {
                    if (session != null) {
                        session.close();
                    }
                }
    }

    /**
     * Start caching the step rollup if it doesn't exist.
     *
     * @param user the user
     * @param workflowId the workflow id
     * @param baseDir the base dir
     */
    private void runWorkflowInNewThread(final UserItem user, final long workflowId, final String baseDir) {
        Session session = null;
        Date lastUpdated = null;
        Boolean foundErrors = false;
        ErrorMessageMap errorMessageMap = new ErrorMessageMap();
        WorkflowDao wfDao = null;
        WorkflowHelper workflowHelper = null;
        WorkflowItem workflowItem = null;
        WorkflowItem attachedWorkflowItem = null;
        logger.info("Set to run workflow (" + workflowId + ") for user " + user.getId() + " in a new thread.");
        try {
            session = newSession();
            wfDao = DaoFactory.DEFAULT.getWorkflowDao();
            workflowHelper = HelperFactory.DEFAULT.getWorkflowHelper();
            //get workflow item
            workflowItem = wfDao.get(workflowId);
            if (workflowItem == null) {
                    logger.error("Workflow item not found error found in running workflow (" + workflowId + ") for user " + user.getId() + " in a new thread.");
                    return;
            }
            attachedWorkflowItem = attach(session, WorkflowItem.class, workflowItem);
            //update the state of this workflow
            attachedWorkflowItem.setState(WorkflowItem.WF_STATE_RUNNING);
            workflowItem.setState(WorkflowItem.WF_STATE_RUNNING);
            wfDao.saveOrUpdate(workflowItem);

            String results = null;
            Workflow workflow = new Workflow(workflowId);
            try {
                workflow.init(WorkflowFileUtils.getStrictDirFormat(baseDir), WorkflowFileUtils.getWorkflowsDir(workflowId), user);
            } catch (Exception e) {
                    foundErrors = true;
                    attachedWorkflowItem.setState(WorkflowItem.WF_STATE_ERROR);
                    workflowItem.setState(WorkflowItem.WF_STATE_ERROR);
                    wfDao.saveOrUpdate(workflowItem);
                    logger.error("Workflow initialization error found in running workflow (" + workflowId + ") for user " + user.getId() + " in a new thread.");
                    return;
            }
            logger.info("Running workflow (" + workflowId + ") for user " + user.getId() + " in a new thread.");
            workflow.run();
            results = workflow.getWorkflowResults();
            logger.info("Finished running workflow (" + workflowId + ") for user " + user.getId());
            lastUpdated = attachedWorkflowItem.getLastUpdated();
            attachedWorkflowItem.setResults(results);
            attachedWorkflowItem.setLastUpdated(new Date());
            workflowItem.setResults(results);
            workflowItem.setLastUpdated(new Date());
            try {
                    wfDao.saveOrUpdate(workflowItem);
            } catch (Exception e) {
                    foundErrors = true;
                    attachedWorkflowItem.setState(WorkflowItem.WF_STATE_ERROR);
                    workflowItem.setState(WorkflowItem.WF_STATE_ERROR);
                    wfDao.saveOrUpdate(workflowItem);
                    logger.error("Workflow results save error found in running workflow (" + workflowId + ") for user " + user.getId() + " in a new thread.");
                    return;
            }
            errorMessageMap = workflow.getErrorMessageMap();
            if (errorMessageMap.isEmpty()) {
                    attachedWorkflowItem.setState(WorkflowItem.WF_STATE_SUCCESS);
                    workflowItem.setState(WorkflowItem.WF_STATE_SUCCESS);
            } else {
                    attachedWorkflowItem.setState(WorkflowItem.WF_STATE_ERROR);
                    workflowItem.setState(WorkflowItem.WF_STATE_ERROR);
            }
            wfDao.saveOrUpdate(workflowItem);
            workflowHelper.saveComponentXmlFiles(null, attachedWorkflowItem, baseDir);
        } catch (Exception e) {
                foundErrors = true;
                logger.error("Unknown component error found: " + e.getMessage());
                ComponentHelper.setRunningComponentsToError(attachedWorkflowItem);
                // Set workflow to error state and save to persistence tables
                try {
                        attachedWorkflowItem.setState(WorkflowItem.WF_STATE_ERROR);
                        workflowItem.setState(WorkflowItem.WF_STATE_ERROR);
                        wfDao.saveOrUpdate(workflowItem);
                        workflowHelper.saveComponentInstances(attachedWorkflowItem, baseDir);
                        workflowHelper.saveComponentXmlFiles(null, attachedWorkflowItem, baseDir);
                } catch (Exception e2) {
                        logger.error("Could not save workflow item. " + e2.getMessage());
                        return;
                }
                return;
        } finally {
             // send email if long-running
                if (isLongRunning(lastUpdated, attachedWorkflowItem)) {
                        String datashopHelpEmail = DataShopInstance.getDatashopHelpEmail();
                        boolean sendEmail = false;
                        if (DataShopInstance.getIsSendmailActive() != null )
                                sendEmail = DataShopInstance.getIsSendmailActive();
                        if (datashopHelpEmail != null && sendEmail) {
                                sendEmail(datashopHelpEmail,
                                                user.getEmail(),
                                                WorkflowsMail.generateSubject(attachedWorkflowItem.getWorkflowName(), attachedWorkflowItem.getState()),
                                                WorkflowsMail.generateContent(user.getName(), attachedWorkflowItem.getWorkflowName(),
                                                                attachedWorkflowItem.getState(), attachedWorkflowItem.getId(), errorMessageMap));
                        }
                }
                if (session != null) {
                        session.close();
                }
        }

    }

    public Long saveWorkflowAsNew(UserItem user, long workflowId, String baseDir, String newWorkflowName, String description, Boolean global)
                    throws WebServiceException {
            Session session = null;
            Long newWorkflowId = null;
            try {
                    session = newSession();
                    final UserItem attachedUser = attach(session, UserItem.class, user);
                    WorkflowDao wfDao = DaoFactory.DEFAULT.getWorkflowDao();
                    WorkflowItem workflowItem = wfDao.get(workflowId);
                    if (workflowItem == null)
                            throw new WebServiceException(INVALID_WORKFLOW_ERR, "Workflow " + workflowId
                                    + " is not valid.");

                    Boolean isShared = false;
                    if (global != null)
                            isShared = global;
                    String workflowDescription = "";
                    if (description != null)
                            workflowDescription = WorkflowFileUtils.htmlEncode(description);
                    if (newWorkflowName == null || newWorkflowName.isEmpty())
                            throw new WebServiceException(REQUIRED_FIELD_MISSING_ERR, "Missing required parameter: new workflow name");

                    WorkflowItem newWorkflowItem = null;
                    try {
                            newWorkflowItem = createWorkflowItem(workflowId, null, attachedUser,
                                    newWorkflowName, workflowDescription, isShared, true, baseDir);
                    } catch (Exception e) {
                            logger.error("Error found in save as new service for workflow: " + workflowId + ". Error: " + e.toString());
                            throw new WebServiceException(UNKNOWN_ERR, "Error found in save as new service for workflow: " + workflowId + ". Error: " + e.toString());
                    }
                    newWorkflowId = newWorkflowItem.getId();
            } finally {
                    if (session != null) {
                            session.close();
                    }
            }
            return newWorkflowId;
    }

    public WorkflowItem parseWorkflowDefinitionFile(UserItem user, long workflowId, File unzipFileDirectory, String baseDir)
                    throws WebServiceException {
            WorkflowItem workflowItem = null;
            WorkflowItem attachedWorkflowItem = null;
            Session session = null;
            Date currentDateTime = new Date();
            try {
                session = newSession();
                UserItem attachedUser = attach(session, UserItem.class, user);
                DatasetDao dsDao = DaoFactory.DEFAULT.getDatasetDao();
                WorkflowDao wfDao = DaoFactory.DEFAULT.getWorkflowDao();
                WorkflowComponentDao wfcDao = DaoFactory.DEFAULT.getWorkflowComponentDao();
                WorkflowComponentInstanceDao wfcInstanceDao = DaoFactory.DEFAULT.getWorkflowComponentInstanceDao();
                WorkflowDatasetMapDao wfDsMapdao = DaoFactory.DEFAULT.getWorkflowDatasetMapDao();
                WorkflowAnnotationDao wfAnnotationdao = DaoFactory.DEFAULT.getWorkflowAnnotationDao();
                WorkflowHelper wfHelper = HelperFactory.DEFAULT.getWorkflowHelper();
                WorkflowFileHelper workflowFileHelper = HelperFactory.DEFAULT.getWorkflowFileHelper();
                WorkflowImportHelper workflowImportHelper = HelperFactory.DEFAULT.getWorkflowImportHelper();
                WorkflowAnnotationHelper workflowAnnotationHelper = HelperFactory.DEFAULT.getWorkflowAnnotationHelper();
                Iterator it = org.apache.commons.io.FileUtils.iterateFiles(unzipFileDirectory, null, false);
                boolean foundXmlDefinitionFile = false;
                List<String> importFileNames = new ArrayList<String>();
                while(it.hasNext()){
                    File curFile = (File) it.next();
                    long fileSize = curFile.length();
                    //if file is too large
                    if (attachedUser.getAdminFlag() && fileSize > MAX_FILE_SIZE_ADMIN) {
                            throw new WebServiceException(WORKFLOW_UPLOAD_FILE_OVERSIZE, "File size exceeds 1GB allowance");
                    } else if (fileSize > MAX_FILE_SIZE) {
                        throw new WebServiceException(WORKFLOW_UPLOAD_FILE_OVERSIZE, "File size exceeds 400MB allowance");
                    }
                    importFileNames.add(curFile.getName());
                }
                //has to repeat this
                it = org.apache.commons.io.FileUtils.iterateFiles(unzipFileDirectory, null, false);
                while(it.hasNext()){
                    File curFile = (File) it.next();
                    String fileExt = FilenameUtils.getExtension(curFile.getName());
                    String curFileAbsolutePath = curFile.getAbsolutePath();
                    //find the workflow definition XML file
                    if (fileExt.equalsIgnoreCase("xml")) {
                        StringBuilder workflowXml = new StringBuilder();
                        StringBuilder annotationsXml = new StringBuilder();
                        try {
                            //delete the xml declaration <?xml version=....?> otherwise SAXbuilder doesn't work
                            String xmlString = org.apache.commons.io.FileUtils.readFileToString(curFile, "UTF-8");
                            xmlString = xmlString.replaceAll("\\<\\?xml(.+?)\\?\\>", "").trim();
                            org.apache.commons.io.FileUtils.writeStringToFile(curFile, xmlString, "UTF-8");
                            org.jdom.Element rootElement = WorkflowXmlUtils.getRootElementFromXmlFile(curFileAbsolutePath);
                            if (rootElement.getName().equalsIgnoreCase("learnsphere_message")) {
                                //this is the xml definition file
                                foundXmlDefinitionFile = true;
                                //workflow id
                                List<org.jdom.Element> elements = WorkflowXmlUtils.getNodeList(rootElement, "/learnsphere_message/workflow[@id]");
                                if (elements.size() != 1)
                                        throw new WebServiceException(WORKFLOW_DEFINITION_ERR, "Workflow definition error: workflow tag is missing or misformed.");
                                org.jdom.Element workflowElement = elements.get(0);
                                String s_WFId = workflowElement.getAttributeValue("id");
                                long WFId = -1;
                                try {
                                    WFId = Long.parseLong(s_WFId);
                                } catch (NumberFormatException nfe) {
                                    throw new WebServiceException(WORKFLOW_DEFINITION_ERR, "Workflow definition error: workflow ID is misformed.");
                                }
                                if (WFId < 1)
                                    throw new WebServiceException(WORKFLOW_DEFINITION_ERR, "Workflow definition error: workflow tag is missing or misformed.");
                                if (WFId != workflowId) {
                                    throw new WebServiceException(WORKFLOW_DEFINITION_ERR, "Workflow definition error: workflow id specified in XML is different from workflowId passed in URL.");
                                }
                                //retrieve this workflow from database
                                workflowItem = wfDao.get(WFId);
                                if (workflowItem == null)
                                    throw new WebServiceException(INVALID_WORKFLOW_ERR, "Workflow " + WFId + " is not valid.");
                                attachedWorkflowItem = attach(session, WorkflowItem.class, workflowItem);
                                //if this workflow is running, can't modify
                                if (attachedWorkflowItem.getState().equalsIgnoreCase(WorkflowItem.WF_STATE_RUNNING)
                                        || attachedWorkflowItem.getState().equalsIgnoreCase(WorkflowItem.WF_STATE_RUNNING_DIRTY)) {
                                        throw new WebServiceException(WORKFLOW_ALREADY_RUNNING_ERR, "Workflow "
                                                        + attachedWorkflowItem.getId() + " is currently running. Please wait for the process to complete.");
                                }
                                //only workflow owner or admin can modify this workflow
                                if (attachedWorkflowItem.getOwner() == null
                                                || (!attachedWorkflowItem.getOwner().getId().equals(attachedUser.getId())
                                                    && !attachedUser.getAdminFlag()))
                                    throw new WebServiceException(INACCESSIBLE_WORKFLOW_ERR, "Workflow "
                                                        + workflowItem.getId() + " is not accessible.");
                                //get all component_ids and make sure they are valid
                                elements = WorkflowXmlUtils.getNodeList(rootElement, "//component");
                                List<String> componentIds = new ArrayList<String>();
                                List<String> allowedComponentTypes = wfcDao.findDistinctComponentTypes(true);
                                for (org.jdom.Element componentElement : elements) {
                                    String componentId = componentElement.getAttributeValue("id");
                                    if (componentId == null || componentId.trim().equals(""))
                                        throw new WebServiceException(WORKFLOW_DEFINITION_ERR, "Workflow definition error: component id is required.");
                                    String[] componentIdParts = componentId.split("-");
                                    if (componentIdParts.length != 3) {
                                        throw new WebServiceException(WORKFLOW_DEFINITION_ERR, "Workflow definition error: component id is misformed.");
                                    }
                                    if (!allowedComponentTypes.contains(componentIdParts[0].toLowerCase())) {
                                        throw new WebServiceException(WORKFLOW_DEFINITION_ERR, "Workflow definition error: component id is misformed.");
                                    }
                                    try {
                                        Integer.parseInt(componentIdParts[1]);
                                        Integer.parseInt(componentIdParts[2].substring(1));
                                    } catch (NumberFormatException nfe) {
                                        throw new WebServiceException(WORKFLOW_DEFINITION_ERR, "Workflow definition error: component id is misformed.");
                                    }
                                    componentIds.add(componentId);
                                }
                                //process id tag
                                addNewElementToXml(workflowXml, "id", "" + workflowItem.getId(), null, null);
                                //process isView tag, copied from LeranSphereServlet.doPostEditWorkflow()
                                Boolean isView = true;
                                if (attachedWorkflowItem.getOwner() != null && attachedWorkflowItem.getOwner().getId().equals(attachedUser.getId())) {
                                        isView = false;
                                }
                                addNewElementToXml(workflowXml, "isView", "" + isView, null, null);
                                //process name, required
                                elements = WorkflowXmlUtils.getNodeList(workflowElement, "/learnsphere_message/workflow/name");
                                if (elements.size() != 1)
                                    throw new WebServiceException(WORKFLOW_DEFINITION_ERR, "Workflow definition error: workflow name is missing.");
                                String workflowName = elements.get(0).getValue();
                                if (workflowName == null || workflowName.trim().equals(""))
                                    throw new WebServiceException(WORKFLOW_DEFINITION_ERR, "Workflow definition error: workflow name is missing.");
                                workflowItem.setWorkflowName(workflowName);
                                attachedWorkflowItem.setWorkflowName(workflowName);
                                addNewElementToXml(workflowXml, "name", workflowName, null, null);

                                //process workflow owner, required
                                elements = WorkflowXmlUtils.getNodeList(workflowElement, "/learnsphere_message/workflow/owner");
                                if (elements.size() != 1)
                                    throw new WebServiceException(WORKFLOW_DEFINITION_ERR, "Workflow definition error: workflow owner is missing.");
                                String workflowOwner = elements.get(0).getValue();
                                if (workflowOwner == null || workflowOwner.trim().equals(""))
                                    throw new WebServiceException(WORKFLOW_DEFINITION_ERR, "Workflow definition error: workflow owner is missing.");
                                //make sure owner in xml is the same as retrieved workflow owner, can't change owner
                                if (attachedWorkflowItem.getOwner() != null && !attachedWorkflowItem.getOwner().getId().equals(workflowOwner))
                                    throw new WebServiceException(WORKFLOW_DEFINITION_ERR, "Workflow definition error: workflow owner doesn't match existing workflow owner for workflow ID: " + attachedWorkflowItem.getId());
                                //process global flag, required
                                elements = WorkflowXmlUtils.getNodeList(workflowElement, "/learnsphere_message/workflow/global");
                                if (elements.size() != 1)
                                    throw new WebServiceException(WORKFLOW_DEFINITION_ERR, "Workflow definition error: workflow global flag is missing. ");
                                String s_global = elements.get(0).getValue();
                                if (s_global == null || s_global.trim().equals("") || (!s_global.equalsIgnoreCase("true") && !s_global.equalsIgnoreCase("false")))
                                    throw new WebServiceException(WORKFLOW_DEFINITION_ERR, "Workflow definition error: workflow global flag can only be true or false.");
                                boolean b_global = Boolean.parseBoolean(s_global);
                                workflowItem.setGlobalFlag(b_global);
                                attachedWorkflowItem.setGlobalFlag(b_global);
                                addNewElementToXml(workflowXml, "isShared", "" + b_global, null, null);
                                //process description
                                elements = WorkflowXmlUtils.getNodeList(workflowElement, "/learnsphere_message/workflow/description");
                                if (elements.size() > 0) {
                                    String description = elements.get(0).getValue();
                                    if (description != null && !description.trim().equals("")) {
                                        workflowItem.setDescription(description);
                                        attachedWorkflowItem.setDescription(description);
                                    }
                                }

                                //set last_updated set to now
                                workflowItem.setLastUpdated(currentDateTime);
                                attachedWorkflowItem.setLastUpdated(currentDateTime);
                                addNewElementToXml(workflowXml, "lastUpdated", DATE_FORMAT.format(currentDateTime), null, null);

                                //state
                                elements = WorkflowXmlUtils.getNodeList(workflowElement, "/learnsphere_message/workflow/state");
                                if (elements.size() > 0) {
                                    String state = elements.get(0).getValue();
                                    if (state != null && !state.trim().equals("")) {
                                        List<String> allAcceptableStates = Arrays.asList("new","running","running_dirty","error","success","paused");
                                        if (allAcceptableStates.contains(state)) {
                                            workflowItem.setState(state);
                                            attachedWorkflowItem.setState(state);
                                        } else {
                                            throw new WebServiceException(WORKFLOW_DEFINITION_ERR, "Workflow definition error: workflow state is not an acceptable value. ");
                                        }
                                    }
                                }

                                //attached datasets
                                elements = WorkflowXmlUtils.getNodeList(workflowElement, "/learnsphere_message/workflow/attached_datasets");
                                if (elements != null && elements.size() != 0) {
                                    if (elements.size() != 1)
                                        throw new WebServiceException(WORKFLOW_DEFINITION_ERR, "Workflow definition error: only one attached_datasets element is allowed. ");
                                    org.jdom.Element datasetsElement = elements.get(0);
                                    elements = WorkflowXmlUtils.getNodeList(datasetsElement, "/learnsphere_message/workflow/attached_datasets/dataset");
                                    //delete all current dataset-workflow_mapping
                                    List<WorkflowDatasetMapItem> allWfDsMaps = wfDsMapdao.findByWorkflow(attachedWorkflowItem);
                                    for (WorkflowDatasetMapItem thisMap : allWfDsMaps)
                                        wfDsMapdao.delete(thisMap);
                                    //add new map
                                    for (org.jdom.Element element : elements) {
                                        WorkflowDatasetMapItem newMap = new WorkflowDatasetMapItem();
                                        String s_dsId = element.getAttributeValue("id");
                                        try {
                                            int dsId = Integer.parseInt(s_dsId);
                                            DatasetItem dsItem = dsDao.find(dsId);
                                            if (dsItem == null)
                                                throw new WebServiceException(WORKFLOW_DEFINITION_ERR, "Workflow definition error: invalid dataset Id is used for attached_datasets element. ");
                                            newMap.setAddedBy(attachedUser);
                                            newMap.setAddedTime(currentDateTime);
                                            newMap.setDatasetExternal(dsItem);
                                            newMap.setWorkflowExternal(attachedWorkflowItem);
                                            newMap.setAutoDisplayFlag(true);
                                            wfDsMapdao.saveOrUpdate(newMap);
                                        } catch (NumberFormatException nfe) {
                                            throw new WebServiceException(WORKFLOW_DEFINITION_ERR, "Workflow definition error: dataset id is misformed for attached_datasets element. ");
                                        }
                                    }
                                }

                                //annotations
                                int leftMargin = 100;
                                int leftIncrement = 50;
                                int topMargin = 10;
                                int topIncrement = 10;
                                elements = WorkflowXmlUtils.getNodeList(workflowElement, "/learnsphere_message/workflow/annotations");
                                if (elements != null && elements.size() != 0) {
                                    if (elements.size() != 1)
                                        throw new WebServiceException(WORKFLOW_DEFINITION_ERR, "Workflow definition error: only one annotations element is allowed. ");
                                    org.jdom.Element datasetsElement = elements.get(0);
                                    elements = WorkflowXmlUtils.getNodeList(datasetsElement, "/learnsphere_message/workflow/annotations/annotation");
                                    //get all current annotations
                                    List<WorkflowAnnotationItem> allWfAnnoItems = wfAnnotationdao.find(attachedWorkflowItem);
                                    //Map<Long, WorkflowAnnotationItem> allWfAnnoIdItemMap = new HashMap<Long, WorkflowAnnotationItem>();
                                    for (WorkflowAnnotationItem thisItem : allWfAnnoItems) {
                                    	wfAnnotationdao.delete(thisItem);
                                    }
                                    //add the annotations
                                    for (org.jdom.Element element : elements) {
                                        String annoText = "";
                                        org.jdom.Element textEle = element.getChild("annotation_text");
                                        if (textEle != null)
                                            annoText = textEle.getText();
                                        //make a new annotation
                                        WorkflowAnnotationItem wfAnnoItem = new WorkflowAnnotationItem();
                                        wfAnnoItem.setLastUpdated(currentDateTime);
                                        wfAnnoItem.setWorkflow(attachedWorkflowItem);
                                        wfAnnoItem.setText(annoText);
                                        wfAnnotationdao.saveOrUpdate(wfAnnoItem);
                                        
                                        if (wfAnnoItem != null) {
                                            StringBuilder annotationXml = new StringBuilder();
                                            addNewElementToXml(annotationXml, "text", annoText, null, null);
                                            addNewElementToXml(annotationXml, "annotation_id", "" + wfAnnoItem.getId(), null, null);
                                            addNewElementToXml(annotationXml, "left", "" + leftMargin, null, null);
                                            addNewElementToXml(annotationXml, "top", "" + topMargin, null, null);
                                            annotationXml.insert(0, "<annotation>");
                                            annotationXml.append("</annotation>");
                                            annotationsXml.append(annotationXml);
                                            leftMargin += leftIncrement;
                                            topMargin += topIncrement;
                                        }

                                    }
                                    
                                }

                                //delete all files in component folder to clean up for new files
                                makeAndCleanFolderComponentFolder(attachedWorkflowItem.getId(), baseDir);

                                boolean hasComponent = false;
                                WorkflowFileItem newFileItem = null;
                                StringBuilder componentsXml = new StringBuilder();
                                List<WorkflowComponentInstanceItem> wfcInstances = new ArrayList<WorkflowComponentInstanceItem>();
                                //to keep the changed component
                                Map<String, String> changedComponentMap = new HashMap<String, String>();
                                //loop through components
                                elements = WorkflowXmlUtils.getNodeList(workflowElement, "/learnsphere_message/workflow/components");
                                if (elements != null && elements.size() > 1)
                                    throw new WebServiceException(WORKFLOW_DEFINITION_ERR, "Workflow definition error: only one components element is allowed. ");
                                else if (elements != null && elements.size() == 1) {
                                    org.jdom.Element componentsElement = elements.get(0);
                                    elements = WorkflowXmlUtils.getNodeList(componentsElement, "/learnsphere_message/workflow/components/component");
                                    for (org.jdom.Element component : elements) {
                                        if (!component.getName().equalsIgnoreCase("component")) {
                                            throw new WebServiceException(WORKFLOW_DEFINITION_ERR, "Workflow definition error: non-component element is found in components element. ");
                                        }
                                        hasComponent = true;
                                        boolean isImportComponent = false;
                                        //reset newFileItem
                                        newFileItem = null;
                                        String componentId = component.getAttributeValue("id"); //Data-1-x8308...
                                        String componentName = null;
                                        String componentType = null;
                                        String componentIdHuman = null;
                                        String componentState = null;
                                        leftMargin += leftIncrement;
                                        topMargin += topIncrement;
                                        org.jdom.Element nameEle = component.getChild("name"); //required
                                        if (nameEle == null || nameEle.getValue().trim().equals(""))
                                            throw new WebServiceException(WORKFLOW_DEFINITION_ERR, "Workflow definition error: name element is required for a component element. ");
                                        else
                                            componentName = nameEle.getValue();
                                        org.jdom.Element typeEle = component.getChild("type"); //required
                                        if (typeEle == null || typeEle.getValue().trim().equals(""))
                                            throw new WebServiceException(WORKFLOW_DEFINITION_ERR, "Workflow definition error: type element is required for a component element. ");
                                        else
                                            componentType = typeEle.getValue();
                                        org.jdom.Element idHumanEle = component.getChild("id_human"); //Data #1, not required
                                        if (idHumanEle != null)
                                            componentIdHuman = idHumanEle.getValue();
                                        org.jdom.Element stateEle = component.getChild("state");//not required
                                        if (stateEle != null)
                                            componentState = stateEle.getValue();
                                        /*//this situation should have been stopped before reaching here
                                        if (componentId == null || componentId.trim().equals("")) {
                                            SecureRandom random = new SecureRandom();
                                            int num = random.nextInt(1000000);
                                            while (num < 100000)
                                                num = random.nextInt(1000000);
                                            componentId = Character.toUpperCase(componentType.charAt(0)) + componentType.substring(1).toLowerCase() +
                                                    "-1-x" + num;
                                        }*/
                                        //write the component xml
                                        StringBuilder componentXml = new StringBuilder();
                                        addNewElementToXml(componentXml, "component_id", componentId, null, null);
                                        if (componentIdHuman != null)
                                            addNewElementToXml(componentXml, "component_id_human", componentIdHuman, null, null);
                                        addNewElementToXml(componentXml, "workflow_id", "" + attachedWorkflowItem.getId(), null, null);
                                        addNewElementToXml(componentXml, "component_name", "" + componentName, null, null);
                                        addNewElementToXml(componentXml, "component_type", "" + componentType, null, null);
                                        addNewElementToXml(componentXml, "left", "" + leftMargin, null, null);
                                        addNewElementToXml(componentXml, "top", "" + topMargin, null, null);
                                        //process connection
                                        org.jdom.Element connectionsElement = component.getChild("connections");
                                        StringBuilder connectionsXml = new StringBuilder();
                                        if (connectionsElement != null) {
                                            List<org.jdom.Element> connectionElements = connectionsElement.getChildren();
                                            for (org.jdom.Element connectionElement : connectionElements) {
                                                StringBuilder connectionXml = new StringBuilder();
                                                org.jdom.Element tempEle = connectionElement.getChild("to");
                                                String tempVal = null;
                                                if (tempEle != null) {
                                                    tempVal = tempEle.getValue();
                                                    //need to make sure this is valid component_id!!!!
                                                    if (!componentIds.contains(tempVal)) {
                                                        throw new WebServiceException(WORKFLOW_DEFINITION_ERR, "Workflow definition error: component_id defined in connection element is not found in component definition. ");
                                                    }
                                                }
                                                if (tempVal != null && !tempVal.trim().equals("")) {
                                                    addNewElementToXml(connectionXml, "to", tempVal, null, null);
                                                }

                                                tempVal = null;
                                                tempEle = connectionElement.getChild("from");
                                                if (tempEle != null) {
                                                    tempVal = tempEle.getValue();
                                                    //need to make sure this is valid component_id!!!!
                                                    if (!componentIds.contains(tempVal)) {
                                                        throw new WebServiceException(WORKFLOW_DEFINITION_ERR, "Workflow definition error: component_id defined in connection element is not found in component definition. ");
                                                    }
                                                }
                                                if (tempVal != null && !tempVal.trim().equals("")) {
                                                    addNewElementToXml(connectionXml, "from", tempVal, null, null);
                                                }

                                                tempVal = null;
                                                tempEle = connectionElement.getChild("index");
                                                if (tempEle != null)
                                                    tempVal = tempEle.getValue();
                                                if (tempVal != null && !tempVal.trim().equals("")) {
                                                    addNewElementToXml(connectionXml, "index", tempVal, null, null);
                                                }

                                                tempVal = null;
                                                tempEle = connectionElement.getChild("frindex");
                                                if (tempEle != null)
                                                    tempVal = tempEle.getValue();
                                                if (tempVal != null && !tempVal.trim().equals("")) {
                                                    addNewElementToXml(connectionXml, "frindex", tempVal, null, null);
                                                }

                                                tempVal = null;
                                                tempEle = connectionElement.getChild("tindex");
                                                if (tempEle != null)
                                                    tempVal = tempEle.getValue();
                                                if (tempVal != null && !tempVal.trim().equals("")) {
                                                    addNewElementToXml(connectionXml, "tindex", tempVal, null, null);
                                                }

                                                if (!connectionXml.toString().trim().equals("")) {
                                                    connectionXml.insert(0, "<connection>");
                                                    connectionXml.append("</connection>");
                                                    connectionsXml.append(connectionXml.toString());
                                                }
                                            }
                                            if (!connectionsXml.toString().trim().equals("")) {
                                                connectionsXml.insert(0, "<connections>");
                                                connectionsXml.append("</connections>");
                                            }
                                        }
                                        //make sure file names match with the real uploaded files
                                        //process file related stuff
                                        org.jdom.Element importFileElement = component.getChild("import_file");
                                        String fileName = null;
                                        String fileType = null;
                                        String fileSelectionType = null;
                                        StringBuilder fileOptionXml = new StringBuilder();
                                        StringBuilder importXml = new StringBuilder();
                                        StringBuilder metaDataXml = new StringBuilder();
                                        if (importFileElement != null) {
                                            org.jdom.Element tempEle = importFileElement.getChild("name"); //required
                                            if (tempEle == null || tempEle.getValue() == null || tempEle.getValue().trim().equals(""))
                                                throw new WebServiceException(WORKFLOW_DEFINITION_ERR, "Workflow definition error: name element is required for an import_file element. ");
                                            else {
                                                fileName = tempEle.getValue();
                                                if (!importFileNames.contains(fileName))
                                                    throw new WebServiceException(WORKFLOW_DEFINITION_ERR, "Workflow definition error: file defined in import_file element is not found in the import zip file: " + fileName);
                                            }
                                            tempEle = importFileElement.getChild("type"); //required
                                            if (tempEle == null || tempEle.getValue() == null || tempEle.getValue().trim().equals(""))
                                                throw new WebServiceException(WORKFLOW_DEFINITION_ERR, "Workflow definition error: type element is required for an import_file element. ");
                                            else
                                                fileType = tempEle.getValue();
                                            tempEle = importFileElement.getChild("file_selection_type"); //required
                                            if (tempEle == null || tempEle.getValue() == null || tempEle.getValue().trim().equals(""))
                                                throw new WebServiceException(WORKFLOW_DEFINITION_ERR, "Workflow definition error: file_selection_type element is required for an import_file element. ");
                                            else
                                                fileSelectionType = tempEle.getValue();

                                            File thisUploadedFile = new File(unzipFileDirectory + File.separator + fileName);
                                            if (!thisUploadedFile.exists())
                                                throw new WebServiceException(WORKFLOW_DEFINITION_ERR, "Workflow definition error: can't find file: " + fileName);
                                            newFileItem = workflowImportHelper.saveFileUpload(workflowFileHelper, wfHelper, attachedWorkflowItem, componentId, fileType, thisUploadedFile,
                                                    baseDir, attachedUser);
                                            
                                            File fileForNewFileItem = new File(baseDir + File.separator + newFileItem.getFilePath() + newFileItem.getFileName());
                                            if (!fileForNewFileItem.exists() || !fileForNewFileItem.isFile())
                                            	throw new WebServiceException(WORKFLOW_DEFINITION_ERR, "Workflow definition error: can't make file: " + fileForNewFileItem.getAbsolutePath());
                                            //make the meta data 
                                            String mimeType = null;
                                            try {
                                                mimeType = WorkflowFileUtils.getMimeType(thisUploadedFile);
                                                if (mimeType != null && mimeType.matches("text/.*")) {
                                                    String commonSchemasDir = WorkflowFileUtils.getStrictDirFormat(WorkflowHelper.getWorkflowComponentsDir())
                                                            + "CommonSchemas/";
                                                    // Assume it's a table if it's text,
                                                    // and try to get the column headers.
                                                    String delim = null;
                                                    
                                                    if (fileType.equalsIgnoreCase(WorkflowImportHelper.FILE_TYPE_CSV)) {
                                                        delim = ",";
                                                    } else if (fileType.equalsIgnoreCase(WorkflowImportHelper.FILE_TYPE_TAB_DELIMITED)) {
                                                        delim = "\t";
                                                    } else if (ComponentHierarchyHelper.isCastable(commonSchemasDir + "TableTypes.xsd", fileType,
                                                            WorkflowImportHelper.FILE_TYPE_TAB_DELIMITED)) {
                                                        delim = "\t";
                                                    } else if (ComponentHierarchyHelper.isCastable(commonSchemasDir + "TableTypes.xsd", fileType,
                                                            WorkflowImportHelper.FILE_TYPE_CSV)) {
                                                        delim = ",";
                                                    }
                                                    LinkedHashMap<String, Integer> columnHeaders = WorkflowImportHelper.getColumnHeaders(thisUploadedFile, delim);
                                                    if (columnHeaders != null && columnHeaders.size() > 0) {
                                                    	Iterator<Entry<String,Integer>> itr = columnHeaders.entrySet().iterator();
                                                    	while (itr.hasNext()) {
                                                    	    Entry<String,Integer> entry = itr.next();
                                                    	    String colHeader = entry.getKey();
                                                    	    colHeader = colHeader.replaceAll("<", "&lt;")
                                                    	    		.replaceAll(">", "&gt;")
                                                    	    		.replaceAll("\"", "&quot;") 
                                                    	    		.replaceAll("&", "&amp;") 
                                                    	    		.replaceAll("'", "&apos;");
                                                    	    Integer colIndex = entry.getValue();
                                                    	    metaDataXml.append("<header>\n" +
                                                    	    					"<id>header" + colIndex + "</id>\n" +
                                                    	    					"<index>" + colIndex + "</index>\n" +
                                                    	    					"<name>" + colHeader + "</name>\n" + 
                                                    	    					"</header>\n");
                                                    	}
                                                    }
                                                }
                                            } catch (IOException ioe) {
                                            	throw new WebServiceException(WORKFLOW_DEFINITION_ERR, "Workflow definition error: can't find file mimetype: " + fileName);
                                            }
                                            isImportComponent = true;
                                            changedComponentMap.put(componentId, ComponentHelper.DIRTY_FILE);
                                            //don't need the following bc req doesn't need to e set
                                            //extractImportFileMetadata(workflowHelper, req, workflowItem, newFileItem, baseDir, componentsDir);
                                            //write this new file to workflow_xml
                                            addNewElementToXml(fileOptionXml, "index", "" + 0, null, null);
                                            addNewElementToXml(fileOptionXml, "file_path", "" + newFileItem.getId(), null, null);
                                            addNewElementToXml(fileOptionXml, "label", fileType, null, null);
                                            addNewElementToXml(fileOptionXml, "file_name", newFileItem.getFileName(), null, null);
                                            importXml.append("<Import>");
                                            importXml.append("{\"fileName\":\"");
                                            importXml.append(newFileItem.getFileName() + "\",\"importFileType\":\"");
                                            importXml.append(fileType + "\",\"importFileNameTitle\":\"File: ");
                                            importXml.append(fileName + "\",\"fileTypeSelection\":\"");
                                            importXml.append(fileSelectionType + "\",\"uploadLocation\":\"importFile\",\"searchDatasetsString\":\"\",\"datasetLink\":\"\",\"datasetName\":\"\",\"fileId\":\"" + newFileItem.getId() + "\"}</Import>");
                                        }
                                        if (!fileOptionXml.toString().trim().equals("")) {
                                            fileOptionXml.insert(0, "<options><files><" + fileType + ">");
                                            if (!metaDataXml.toString().trim().equals(""))
                                            	fileOptionXml.append("<metadata>\n" + metaDataXml + "</metadata>\n");
                                            fileOptionXml.append("</" + fileType + "></files>");
                                            fileOptionXml.append(importXml).append("</options>");
                                        }
                                        //process options element
                                        org.jdom.Element optionsElement = component.getChild("options");
                                        StringBuilder optionsXml = new StringBuilder();
                                        if (optionsElement != null) {
                                            List<org.jdom.Element> optionElements = optionsElement.getChildren();
                                            for (org.jdom.Element optionElement : optionElements) {
                                                StringBuilder optionXml = new StringBuilder();
                                                org.jdom.Element tempEle = optionElement.getChild("name");
                                                String optionName = null;
                                                if (tempEle == null || tempEle.getValue() == null || tempEle.getValue().trim().equals(""))
                                                    throw new WebServiceException(WORKFLOW_DEFINITION_ERR, "Workflow definition error: name element is required for an option element. ");
                                                else {
                                                    optionName = tempEle.getValue();
                                                }
                                                tempEle = optionElement.getChild("value");
                                                String optionValue = null;
                                                if (tempEle == null)
                                                    throw new WebServiceException(WORKFLOW_DEFINITION_ERR, "Workflow definition error: value element is required for an option element. ");
                                                else {
                                                    optionValue = tempEle.getValue();
                                                }
                                                addNewElementToXml(optionXml, optionName, optionValue, null, null);
                                                if (!optionXml.toString().trim().equals("")) {
                                                    optionsXml.append(optionXml.toString());
                                                }
                                            }
                                            if (!optionsXml.toString().trim().equals("")) {
                                                optionsXml.insert(0, "<options>");
                                                optionsXml.append("</options>");
                                            }
                                        }
                                        componentXml.insert(0, "<component>");
                                        if (!connectionsXml.toString().trim().equals(""))
                                            componentXml.append(connectionsXml);
                                        if (!fileOptionXml.toString().trim().equals(""))
                                            componentXml.append(fileOptionXml);
                                        if (!optionsXml.toString().trim().equals(""))
                                            componentXml.append(optionsXml);
                                        componentXml.append("</component>");
                                        componentsXml.append(componentXml);
                                        //log component log action
                                        if (newFileItem != null)
                                            wfHelper.logWorkflowComponentUserAction(attachedUser, workflowItem, null,
                                                componentId, componentName, componentType, componentIdHuman,
                                                null, newFileItem, null, WorkflowHelper.LOG_WEB_SERVICE_UPLOAD_FILE,
                                                    "path : " + newFileItem.getFilePath() + ", "
                                                    + "name : " + newFileItem.getFileName());
                                        else
                                            wfHelper.logWorkflowComponentUserAction(attachedUser, workflowItem, null,
                                                    componentId, componentName, componentType, componentIdHuman,
                                                    null, null, null, WorkflowHelper.LOG_WEB_SERVICE_MODIFY_COMPONENT, null);

                                        //make a folder for non-import component
                                        if (!isImportComponent) {
                                            makeAndCleanFolderForNonImportComponent(attachedWorkflowItem.getId(), componentId, baseDir);
                                            changedComponentMap.put(componentId, ComponentHelper.DIRTY_OPTION);
                                        }
                                        
                                        //make new work flow component instance
                                        WorkflowComponentInstanceItem wfciItem = new WorkflowComponentInstanceItem();
                                        wfciItem.setComponentName(componentId);
                                        wfciItem.setWorkflow(attachedWorkflowItem);
                                        wfciItem.setState("new");
                                        wfciItem.setDirtyFile(true);
                                        wfciItem.setDirtyOption(true);
                                        wfciItem.setDirtySelection(true);
                                        wfcInstances.add(wfciItem);
                                    }//end of for (org.jdom.Element component : elements)
                                }//end of else if (elements != null && elements.size() == 1) {
                                if (hasComponent && !componentsXml.toString().trim().equals("")) {
                                    componentsXml.insert(0, "<components>");
                                    componentsXml.append("</components>");
                                }
                                //set workflowXml
                                workflowXml.insert(0, "<workflow>");
                                workflowXml.append(componentsXml);
                                if (!annotationsXml.toString().equals("")) {
                                    annotationsXml.insert(0, "<annotations>");
                                    annotationsXml.append("</annotations>");
                                    workflowXml.append(annotationsXml);
                                }
                                workflowXml.append("</workflow>");
                                workflowItem.setWorkflowXml(workflowXml.toString());
                                attachedWorkflowItem.setWorkflowXml(workflowXml.toString());
                                //set result to null
                                workflowItem.setResults(null);
                                attachedWorkflowItem.setResults(null);
                                //set status
                                workflowItem.setState("new");
                                attachedWorkflowItem.setState("new");
                                //save workflow
                                //first make digraphDoc using workflowXml
                                org.jdom.Element digraphDoc = null;
                                SAXBuilder saxBuilder = new SAXBuilder();
                                saxBuilder.setReuseParser(false);
                                InputStream stream = null;
                                // Throws UnsupportedEncoding Exception
                                stream = new ByteArrayInputStream(workflowXml.toString().getBytes("UTF-8"));
                                org.jdom.Document digraphDom = null;
                                if (stream != null) {
                                    digraphDom = saxBuilder.build(stream);
                                    stream.close();
                                }
                                if (digraphDom != null)
                                    digraphDoc = digraphDom.getRootElement();

                                //clean old WF instances and add new WF instances
                                List<WorkflowComponentInstanceItem> existingInstanceItems = wfcInstanceDao.findByWorkflow(attachedWorkflowItem);
                                List<String> existingInstances = new ArrayList<String>();
                                if (existingInstanceItems != null) {
                                    for (WorkflowComponentInstanceItem thisInstance : existingInstanceItems) {
                                        //only delete the one that is not defined in XML
                                        if (!componentIds.contains(thisInstance.getComponentName())) {
                                            wfcInstanceDao.delete(thisInstance);
                                        } else {
                                            existingInstances.add(thisInstance.getComponentName());
                                        }
                                    }
                                }
                                for (WorkflowComponentInstanceItem wfcInstance : wfcInstances) {
                                    if (!existingInstances.contains(wfcInstance.getComponentName())){
                                        wfcInstance.setState(WorkflowComponentInstanceItem.WF_STATE_NEW);
                                        wfcInstanceDao.saveOrUpdate(wfcInstance);
                                    }
                                }
                                // required for backwards compatibility with older workflows: try to create wfc adjacency
                                ConnectionHelper.updateAdjacencyList(baseDir, workflowItem, attachedUser);
                                ComponentHelper.removeDeletedComponents(workflowItem, digraphDoc, baseDir);
                                
                                //delete old workflow persistence
                                //????????
                                
                                
                                // Update the database with this workflow XML
                                wfHelper.saveWorkflowToDatabase(workflowItem, baseDir);
                                wfHelper.saveComponentInstances(workflowItem, baseDir);
                                wfHelper.saveComponentXmlFiles(null, workflowItem, baseDir);
                                workflowAnnotationHelper.saveAnnotations(workflowItem, baseDir, digraphDoc);
                                processDirtyBits(changedComponentMap, workflowFileHelper, workflowImportHelper, attachedWorkflowItem, digraphDoc, attachedUser, true, baseDir);

                            }
                        } catch (IOException e) {
                            String errMsg = "Found IOException for reading XML from or to file. Error: " + e.getMessage();
                            throw new WebServiceException(WORKFLOW_INTERNAL_FILE_ERR, errMsg);

                        } catch (JDOMException e) {
                            String errMsg = "Found JDOMException for making workflow XML into a jdom element. Error: " + e.getMessage();
                            throw new WebServiceException(WORKFLOW_INTERNAL_FILE_ERR, errMsg);

                        }
                    }//end of if (fileExt.equalsIgnoreCase("xml"))
                }//end of while(it.hasNext())
                if (!foundXmlDefinitionFile) {
                    workflowItem = null;
                      throw new WebServiceException(WORKFLOW_DEFINITION_ERR, "Workflow definition error: workflow definition XML is missing. ");
                }
            }  finally {
                    if (session != null) {
                        session.close();
                    }
           }
            return workflowItem;
    }

    private void makeAndCleanFolderForNonImportComponent(long workflowId, String componentId, String baseDir) 
    			throws IOException{
        String subPath = WorkflowFileUtils.sanitizePath(baseDir + File.separator +
                WorkflowFileUtils.getWorkflowsDir(workflowId) + componentId + "/output/");
        File folderPathFile = new File(subPath);
        if (!folderPathFile.exists() || !folderPathFile.isDirectory()) {
            folderPathFile.mkdirs();
        }
        org.apache.commons.io.FileUtils.cleanDirectory(folderPathFile); 
    }
    
    private void makeAndCleanFolderComponentFolder(long workflowId, String baseDir) 
			throws IOException{
	    String subPath = WorkflowFileUtils.sanitizePath(baseDir + File.separator +
	            WorkflowFileUtils.getWorkflowsDir(workflowId) + "components/");
	    File folderPathFile = new File(subPath);
	    if (!folderPathFile.exists() || !folderPathFile.isDirectory()) {
	        folderPathFile.mkdirs();
	    }
	    org.apache.commons.io.FileUtils.cleanDirectory(folderPathFile); 
	}

    //helper to build up workflowXml string. only works with single attribute
    private void addNewElementToXml(StringBuilder workflowXml, String elementName, String elementValue, String attriuteName, String attriuteValue) {
            if (elementName == null || elementName.trim().equals(""))
                    return;
            if (elementValue == null || elementValue.trim().equals("")) {
                    workflowXml.append("<" + elementName);
            } else {
                    workflowXml.append("<" + elementName);
            }
            if (attriuteName != null && !attriuteName.trim().equals("")) {
                    workflowXml.append(" ").append(attriuteName).append("=\"");
                    if (attriuteValue != null)
                            workflowXml.append(attriuteValue);
                    workflowXml.append("\"");
            }
            if (elementValue == null || elementValue.trim().equals("")) {
                    workflowXml.append("/>");
            } else {
                    workflowXml.append(">").append(elementValue).append("</" + elementName + ">");
            }
            workflowXml.append(System.lineSeparator());
    }

    //set the dirty bits for workflow_component_instance
    //copy from WorkflowEditorServlet processDirtyBits
    private void processDirtyBits(Map<String, String> dirtyBits, WorkflowFileHelper workflowFileHelper,
            WorkflowImportHelper workflowImportHelper,
            WorkflowItem workflowItem, org.jdom.Element workflowRootElement,
                UserItem loggedInUserItem, Boolean saveFlag, String baseDir) {
            ComponentFileDao compFileDao = DaoFactory.DEFAULT.getComponentFileDao();
            WorkflowComponentInstanceDao wciDao = DaoFactory.DEFAULT.getWorkflowComponentInstanceDao();
            if (workflowItem != null && loggedInUserItem != null && workflowItem.getOwner() != null
                    && workflowItem.getOwner().getId().equals(loggedInUserItem.getId())) {
                if (dirtyBits != null && dirtyBits.size() > 0) {

                        logger.trace("Check component instance.");
                        Set<String> keys = dirtyBits.keySet();
                        List<String> deletedComponents = new ArrayList<String>();
                        if (keys != null && !workflowItem.getState().equalsIgnoreCase(WorkflowItem.WF_STATE_RUNNING)) {
                            logger.trace("Dirty bits found.");
                            List<WorkflowComponentInstanceItem> componentInstances = wciDao.findByWorkflow(workflowItem);
                            // Remove components no longer in the workflow.
                            if (componentInstances != null) {
                                for (Iterator<WorkflowComponentInstanceItem> wciiIter = componentInstances.iterator(); wciiIter
                                        .hasNext();) {
                                    Integer wciiId = (Integer) ((WorkflowComponentInstanceItem) wciiIter.next()).getId();
                                    WorkflowComponentInstanceItem wcii = wciDao.get(wciiId);

                                    // Set all component instances to state = new,
                                    // except for running instances
                                    Boolean foundInLatest = false;
                                    if (!wcii.getState().equalsIgnoreCase(WorkflowComponentInstanceItem.WF_STATE_RUNNING)) {

                                        // Does the digraphDoc (workflow in GUI) contain this component still?
                                        if (workflowRootElement != null) {
                                            // Look through all descendants of the latest workflow xml to detect
                                            // if the queued component still exists.
                                            for (Iterator<org.jdom.Element> iter = workflowRootElement
                                                    .getDescendants(new ElementFilter()); iter.hasNext();) {
                                                org.jdom.Element desc = iter.next();
                                                if (desc.getName().equalsIgnoreCase("component_id")
                                                        && desc.getText().trim().equalsIgnoreCase(
                                                            wcii.getComponentName())) {
                                                    foundInLatest = true;
                                                }
                                            }

                                            if (!foundInLatest && saveFlag) {

                                                // The item in the queue no longer exists
                                                // in the workflow so we can
                                                // delete it from the database.

                                                // From the component_file table
                                                workflowFileHelper.updateFileMappings(compFileDao, workflowItem, wcii.getComponentName(), true, baseDir);

                                                // From the workflow_component_instance table
                                                wciDao.delete(wcii);

                                            }
                                        }
                                    }
                                }
                            }

                            List<ComponentFileItem> cfItems = compFileDao.findByWorkflow(workflowItem);
                            for (Iterator<ComponentFileItem> cfIterator = cfItems.iterator(); cfIterator
                                    .hasNext();) {
                                Long wfiId = (Long) ((ComponentFileItem) cfIterator.next()).getId();
                                ComponentFileItem cfItem = compFileDao.get(wfiId);

                                // Set all component instances to state = new,
                                // except for running instances
                                Boolean foundInLatest = false;
                                if (!workflowItem.getState().equalsIgnoreCase(WorkflowItem.WF_STATE_RUNNING)) {

                                    // Does the digraphDoc (workflow in GUI) contain this component still?
                                    if (workflowRootElement != null) {
                                        // Look through all descendants of the latest workflow xml to detect
                                        // if the queued component still exists.
                                        for (Iterator<org.jdom.Element> iter = workflowRootElement
                                                .getDescendants(new ElementFilter()); iter.hasNext();) {
                                            org.jdom.Element desc = iter.next();
                                            if (desc.getName().equalsIgnoreCase("component_id")
                                                    && desc.getText().trim().equalsIgnoreCase(
                                                            cfItem.getComponentId())) {
                                                foundInLatest = true;
                                            }
                                        }

                                        if (!foundInLatest && saveFlag) {

                                            // The item in the queue no longer exists
                                            // in the workflow so we can
                                            // delete it from the database.

                                            // From the component_file table
                                            workflowFileHelper.updateFileMappings(compFileDao, workflowItem, cfItem.getComponentId(), true, baseDir);


                                        }
                                    }
                                }
                            }

                            for (String key : keys) {
                                if (deletedComponents.contains(key)) {
                                    continue;
                                }
                                String value = (String) dirtyBits.get(key);

                                WorkflowComponentInstanceItem componentInstance = wciDao.findByWorkflowAndId(workflowItem,
                                        key);
                                if (componentInstance != null) {
                                    // This component was found in queue
                                    logger.trace("Found component instance: " + key);

                                    Integer wcInstanceId = (Integer) componentInstance.getId();
                                    WorkflowComponentInstanceItem wcInstanceItem = wciDao.get(wcInstanceId);

                                    if (!wcInstanceItem.getState().equalsIgnoreCase(
                                                WorkflowComponentInstanceItem.WF_STATE_RUNNING)) {
                                        logger.trace("Setting component to 'new' state: "
                                                + wcInstanceItem.getComponentName());
                                        wcInstanceItem.setState(WorkflowComponentInstanceItem.WF_STATE_NEW);

                                        workflowImportHelper.triggerDirtyBit(wcInstanceItem, value);

                                        wciDao.saveOrUpdate(wcInstanceItem);
                                    }

                                    // Set remaining components to state = new,
                                    // except for running or completed components
                                    for (WorkflowComponentInstanceItem wcii : wciDao.findByWorkflow(workflowItem)) {
                                        Hibernate.initialize(wcii);
                                        // Set all component instances to state = new, except for running instances
                                        if (!wcii.getState().equalsIgnoreCase(
                                                WorkflowComponentInstanceItem.WF_STATE_RUNNING)
                                                && !wcii.getState().equalsIgnoreCase(
                                                    WorkflowComponentInstanceItem.WF_STATE_RUNNING)
                                                && !wcii.getState().equalsIgnoreCase(
                                                        WorkflowComponentInstanceItem.COMPLETED)) {
                                            // Set state to new if not running
                                            wcii.setState(WorkflowComponentInstanceItem.WF_STATE_NEW);
                                            logger.trace("Setting component to 'new' state: " + wcii.getComponentName());
                                            wciDao.saveOrUpdate(wcii);
                                        } else {
                                            logger.trace("Running component '" + wcii.getComponentName()
                                                    + "' will be allowed to complete.");
                                        }
                                    }

                                } else {
                                    // This component does not exist in queue
                                    logger.trace("New component instance: " + key);

                                    WorkflowComponentInstanceItem wcInstanceItem = new WorkflowComponentInstanceItem();
                                    workflowImportHelper.triggerDirtyBit(wcInstanceItem, value);

                                    wcInstanceItem.setWorkflow(workflowItem);
                                    wcInstanceItem.setComponentName(key);
                                    wcInstanceItem.setState(WorkflowComponentInstanceItem.WF_STATE_NEW);
                                    wciDao.saveOrUpdate(wcInstanceItem);
                                }
                            }
                        }


                }
            }
        }

    /**
     * test if a file can be accessed by a user, logic is copied from LearnSphereServlet.returnFile()
     *
     * @param userItem the logged in user
     * @param fileItem the FileItem
     */
    private boolean isAccessible(UserItem userItem, WorkflowFileItem fileItem, String baseDir) throws IOException {
            ComponentFileDao compFileDao = DaoFactory.DEFAULT.getComponentFileDao();
            WorkflowDao workflowDao = DaoFactory.DEFAULT.getWorkflowDao();
            WorkflowHelper wfHelper = HelperFactory.DEFAULT.getWorkflowHelper();
            Boolean hasAccess = false;
            List<ComponentFileItem> cfItems = compFileDao.findByFile(fileItem);
            WorkflowItem workflowItem = null;
            if (cfItems != null && !cfItems.isEmpty()) {
                    for (ComponentFileItem wfmItem : cfItems) {
                            workflowItem = workflowDao.get((Long) wfmItem.getWorkflow().getId());
                            if (workflowItem != null) {
                                    String componentAccessLevel = WorkflowAccessHelper.getComponentAccessLevel(
                                            userItem, baseDir, workflowItem, wfmItem.getComponentId());
                                    if (componentAccessLevel != null && (componentAccessLevel.equalsIgnoreCase(WorkflowItem.LEVEL_VIEW)
                                            || componentAccessLevel.equalsIgnoreCase(WorkflowItem.LEVEL_EDIT))) {
                                        hasAccess = true;
                                    }
                            }
                    }
            }
        return hasAccess;
    }

    private static String getCharacterDataFromElement(Element e) {
            Node child = e.getFirstChild();
            if (child instanceof CharacterData) {
               CharacterData cd = (CharacterData) child;
               return cd.getData();
            }
            return null;
    }

    private String getXmlTextFromTag (Element element, String tagName) {
            String cd = null;
            NodeList nodeList = element.getElementsByTagName(tagName);
            if (nodeList != null && nodeList.getLength() > 0) {
                    Element line = (Element) nodeList.item(0);
                    cd = getCharacterDataFromElement(line);
            }
            return cd;
    }

    /**
     * copied from LearnSphereServlet. Ideally this should be in the WorkflowHelper
     */
    private void deleteComponentFileItems(ComponentFileDao compFileDao, WorkflowItem workflowItem, String baseDir) {

        List<ComponentFileItem> cfItems = compFileDao.findByWorkflow(workflowItem);
        List<String> filePaths = new ArrayList<String>();
        if (cfItems != null) {
            for (ComponentFileItem cfItem : cfItems) {
                if (cfItem.getFile() != null) {
                    WorkflowFileDao wfFileDao = DaoFactory.DEFAULT.getWorkflowFileDao();
                    WorkflowFileItem componentXmlFile = wfFileDao.get((Integer) cfItem.getFile().getId());
                    String filePath = WorkflowFileUtils.sanitizePath(componentXmlFile.getFullFileName(WorkflowFileUtils
                            .getStrictDirFormat(baseDir)));
                    if (filePath != null) {
                        filePaths.add(filePath);
                    }
                }
                compFileDao.delete(cfItem);
            }

            if (!filePaths.isEmpty()) {
                for (String filePath: filePaths) {
                    File fileTest = new File(filePath);
                    if (fileTest.exists() && fileTest.canWrite() && fileTest.isFile()) {
                        fileTest.delete();
                    }
                }
            }
        }

    }

    /**
     * copied from LearnSphereServlet. Ideally this should be in the WorkflowHelper
     */
    private void deleteAllWorkflowTags(WorkflowItem workflowItem) {
        try {
            WorkflowTagMapDao wfTagMapDao = DaoFactory.DEFAULT.getWorkflowTagMapDao();
            List<WorkflowTagMapItem> tagMapItems = wfTagMapDao.findByWorkflow(workflowItem);

            for (int i = 0; i < tagMapItems.size(); i++) {
                WorkflowTagMapId mapId = (WorkflowTagMapId) tagMapItems.get(i).getId();

                if (mapId != null) {
                    Long tagId = mapId.getWorkflowTagId();
                    WorkflowTagDao wfTagDao = DaoFactory.DEFAULT.getWorkflowTagDao();
                    WorkflowTagItem tagItem = wfTagDao.get(tagId);
                    if (tagItem != null) {
                        wfTagDao.delete(tagItem);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Could not remove all tags of the workflow being deleted: " + e.toString());
        }
    }
    /**
     * Constant used to define long-running workflow, in milliseconds (5 minutes).
     */
    private static final long LONG_RUNNING_THRESHOLD = 300000L;

    /**
     * copied from LearnSphereServlet. Ideally this should be in the WorkflowHelper
     */
    private Boolean isLongRunning(Date lastUpdate, WorkflowItem workflowItem) {

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
     * copied from LearnSphereServlet. Ideally this should be in the WorkflowHelper
     * Create a new workflow item.
     *
     * @param workflowId the workflow Id
     * @param datasetItem the optional DatasetItem
     * @param owner the owner
     * @param newWorkflowName the new workflow name
     * @param workflowDescription the workflow description
     * @param isShared whether or not the workflow is public
     * @param isSaveAsNew whether or not to save this workflow as a new workflow
     * @return the newly created workflow item
     */
    private WorkflowItem createWorkflowItem(Long workflowId, DatasetItem datasetItem, UserItem newWorkflowOwner,
                                            String newWorkflowName, String workflowDescription,
                                            Boolean isShared, Boolean isSaveAsNew, String baseDir) {
            String escapedWorkflowName = WorkflowFileUtils.htmlEncode(newWorkflowName);
            // Ensure that the newWorkflowName is valid xml
            if (!WorkflowPropertiesHelper.isValidWorkflowName(escapedWorkflowName)) {
                    return null;
            }

            Date now = new Date();
            // Try to create the new workflow, then return the workflow name and id
            WorkflowDao workflowDao = DaoFactory.DEFAULT.getWorkflowDao();
            WorkflowFileDao wfFileDao = DaoFactory.DEFAULT.getWorkflowFileDao();
            WorkflowHelper workflowHelper = HelperFactory.DEFAULT.getWorkflowHelper();
            WorkflowItem newWorkflowItem = new WorkflowItem();
            newWorkflowItem.setWorkflowName(escapedWorkflowName);
            newWorkflowItem.setOwner(newWorkflowOwner);
            newWorkflowItem.setDescription(workflowDescription);

            newWorkflowItem.setGlobalFlag(isShared);
            newWorkflowItem.setIsRecommended(false);
            newWorkflowItem.setLastUpdated(now);
            newWorkflowItem.setState(WorkflowItem.WF_STATE_NEW);
            workflowDao.saveOrUpdate(newWorkflowItem);

            // If a dataset is associated with this workflow, then add an entry to the wf dataset map.
            if (datasetItem != null) {
                    newWorkflowItem.addDataset(datasetItem);
                    workflowDao.saveOrUpdate(newWorkflowItem);

                    WorkflowDatasetMapDao wfdsMapDao = DaoFactory.DEFAULT.getWorkflowDatasetMapDao();
                    WorkflowDatasetMapItem wfdsMapItem = new WorkflowDatasetMapItem();
                    wfdsMapItem.setId(new WorkflowDatasetMapId(newWorkflowItem, datasetItem));
                    wfdsMapItem.setAddedBy(newWorkflowOwner);
                    wfdsMapItem.setAddedTime(now);
                    wfdsMapItem.setAutoDisplayFlag(true);
                    wfdsMapDao.saveOrUpdate(wfdsMapItem);
            }

            WorkflowItem existingWorkflowItem = null;
            WorkflowPersistenceDao wpDao = DaoFactory.DEFAULT.getWorkflowPersistenceDao();
            WorkflowPersistenceItem wpItem = new WorkflowPersistenceItem();

            if (workflowId != null) {
                    existingWorkflowItem = workflowDao.get(workflowId);
                    if (existingWorkflowItem != null) {
                            String newWorkflowXml = null;
                            WorkflowPersistenceItem persistentWf = wpDao.findByWorkflow(existingWorkflowItem);
                            // Get the persistent workflow if it exists (not the live workflow)
                            if (persistentWf != null) {
                                    Hibernate.initialize(persistentWf.getWorkflowXml());
                                    newWorkflowXml = persistentWf.getWorkflowXml();
                            }
                            /*In web service, this should not be a case
                            // If this is a 'Save as' from an open, live WF, use that content.
                            String digraphToCopy = getWorkflowXmlFromJson(req, resp); // mck3 look into this method
                                     if (digraphToCopy != null) {
                                           newWorkflowXml = digraphToCopy;
                                     }
                             */
                            org.jdom.Element resultsRoot = null;
                            String existingResults = null;
                            if (existingWorkflowItem.getResults() != null) {
                                    existingResults = existingWorkflowItem.getResults();
                            }

                            String existingWorkflowXml = null;
                            try {
                                    existingWorkflowXml = WorkflowPropertiesHelper.updateWorkflowName(newWorkflowXml, escapedWorkflowName);
                            } catch (IOException e1) {
                                    logger.error("Could not update workflow name in XML.");
                            }
                            org.jdom.Element workflowRoot = null;
                            if (existingWorkflowXml != null) {
                                    try {
                                            workflowRoot = WorkflowXmlUtils.getStringAsElement(existingWorkflowXml);
                                            if (existingResults != null) {
                                                    resultsRoot = WorkflowXmlUtils.getStringAsElement("<outputs>" + existingResults + "</outputs>");
                                            }
                                    } catch (JDOMException e) {
                                            logger.error("Info file is not valid XML.");
                                    } catch (IOException e) {
                                            logger.error("Info file could not be read.");
                                    }

                                    // Output results to save (data is accessible to user)??????
                                    List<String> outputsToSave = new ArrayList<String>();
                                    HashMap<String, String> componentAccessLevels = new HashMap<String, String>();
                                    // Remove references to previous files and previous workflow ids
                                    // from the component
                                    // before saving it as a new workflow
                                    HashMap<String, String> componentAccessLeves = new HashMap<String, String>();
                                    ElementFilter componentElemFilter = new ElementFilter("component");
                                    for (Iterator<org.jdom.Element> iter = workflowRoot.getDescendants(componentElemFilter); iter.hasNext();) {
                                            org.jdom.Element componentElem = iter.next();
                                            if (componentElem != null) {
                                                    if (componentElem.getChild("component_id") != null) {
                                                            String componentId = componentElem.getChildTextTrim("component_id");
                                                            String componentAccessLevel = WorkflowAccessHelper.getComponentAccessLevel(
                                                                            newWorkflowOwner, baseDir, existingWorkflowItem, componentId);
                                                            componentAccessLevels.put(componentId, componentAccessLevel);

                                                    }
                                            }
                                    }


                                    List<String> unsatisfiedComponentAccess = new ArrayList<String>();

                                    ElementFilter filesElementFilter = new ElementFilter("files");
                                    for (Iterator<org.jdom.Element> iter = workflowRoot.getDescendants(filesElementFilter); iter.hasNext();) {
                                            org.jdom.Element filesElement = iter.next();
                                            // if workflowitem.edit or view, then copy files to new workflow
                                            // component_file_persistence, workflow_file, workflow_file_map, workflow_component_instance_persistence;
                                            for (org.jdom.Element fileElement : (List<org.jdom.Element>) filesElement.getChildren()) {
                                                    // Each input must contain a file element and a model element to be valid.
                                                    if (fileElement != null
                                                                    && fileElement.getChild("file_path") != null
                                                                    && fileElement.getChild("file_name") != null) {

                                                            String filePathStr = fileElement.getChild("file_path").getTextTrim();
                                                            if (filePathStr.matches("[0-9]+")) {
                                                                    String newComponentId = null;
                                                                    if (fileElement.getParentElement() != null && fileElement.getParentElement().getParentElement() != null
                                                                                    && fileElement.getParentElement().getParentElement().getParentElement() != null
                                                                                    && fileElement.getParentElement().getParentElement().getParentElement().getChildTextTrim("component_id") != null) {
                                                                            newComponentId = fileElement.getParentElement().getParentElement().getParentElement().getChildTextTrim("component_id");

                                                                    } else if (fileElement.getParentElement() != null && fileElement.getParentElement().getParentElement() != null
                                                                                    && fileElement.getParentElement().getParentElement().getChildTextTrim("component_id") != null) {
                                                                            newComponentId = fileElement.getParentElement().getParentElement().getChildTextTrim("component_id");
                                                                    }
                                                                    if (newComponentId != null) {
                                                                            Integer wfFileId = Integer.parseInt(filePathStr);
                                                                            WorkflowFileItem wfFileItem = wfFileDao.get(wfFileId);
                                                                            if (wfFileItem != null) {
                                                                                    String componentAccessLevel = componentAccessLevels.get(newComponentId);
                                                                                    if (componentAccessLevel != null && (componentAccessLevel.equalsIgnoreCase(WorkflowItem.LEVEL_VIEW)
                                                                                                    || componentAccessLevel.equalsIgnoreCase(WorkflowItem.LEVEL_EDIT))) {
                                                                                            outputsToSave.add(newComponentId);
                                                                                            WorkflowFileItem newFileItem = saveWorkflowFileAsWorkflowFile(newWorkflowItem.getId(), newComponentId,
                                                                                                            wfFileItem.getTitle(), wfFileItem.getDescription(), wfFileItem, baseDir, newWorkflowOwner);
                                                                                            org.jdom.Element fpElem = fileElement.getChild("file_path");
                                                                                            if (newFileItem != null && newFileItem.getOwner().getId().equals(newWorkflowOwner.getId())) {
                                                                                                    fpElem.setText(newFileItem.getId().toString());
                                                                                            }
                                                                                    } else {
                                                                                            fileElement.removeContent();
                                                                                            unsatisfiedComponentAccess.add(newComponentId);
                                                                                    }
                                                                            }
                                                                    } else {
                                                                            fileElement.removeContent();
                                                                            unsatisfiedComponentAccess.add(newComponentId);
                                                                    }
                                                            }
                                                    }
                                            }//end of for loop: filesElement.getChildren()
                                    }//end of for loop: workflowRoot.getDescendants(filesElementFilter)
                                    // Copy .bak files
                                    String strictDirBaseDir = WorkflowFileUtils.getStrictDirFormat(baseDir);
                                    File componentsDir = new File(strictDirBaseDir + "/" + WorkflowFileUtils.getWorkflowsDir(existingWorkflowItem.getId()) + WorkflowHelper.COMPONENTS_XML_DIRECTORY_NAME);
                                    File newComponentsDir = new File(strictDirBaseDir + "/" + WorkflowFileUtils.getWorkflowsDir(newWorkflowItem.getId()) + WorkflowHelper.COMPONENTS_XML_DIRECTORY_NAME);

                                    newComponentsDir.mkdirs();
                                    FileUtils.makeWorldReadable(newComponentsDir);

                                    // Set the workflow id in the workflow XML to the new id
                                    List<org.jdom.Element> workflowIdElements = new ArrayList<org.jdom.Element>();
                                    ElementFilter workflowIdElementFilter = new ElementFilter("id");
                                    for (Iterator<org.jdom.Element> iter = workflowRoot.getDescendants(workflowIdElementFilter); iter.hasNext();) {
                                            org.jdom.Element workflowIdElement = iter.next();
                                            workflowIdElements.add(workflowIdElement);
                                            break;
                                    }

                                    for (org.jdom.Element workflowIdElement : workflowIdElements) {
                                            workflowIdElement.setText(newWorkflowItem.getId().toString());
                                    }
                                    // Save XML and optionally the Results to the newWorkflowItem
                                    try {
                                            if (resultsRoot != null && resultsRoot.getChildren() != null) {
                                                    List<org.jdom.Element> resChilds = resultsRoot.getChildren();
                                                    StringBuffer sbuf = new StringBuffer();
                                                    for (org.jdom.Element res : resChilds) {
                                                            if (res.getChildTextTrim("component_id") != null
                                                                            && outputsToSave.contains(res.getChildTextTrim("component_id"))) {
                                                                    sbuf.append(WorkflowXmlUtils.getElementAsString(res));
                                                            } else if (res.getChildTextTrim("component_id") != null) {
                                                                    // This must (and will) be filtered by access later in the results methods.
                                                                    sbuf.append(WorkflowXmlUtils.getElementAsString(res));
                                                            }
                                                    }
                                                    if (!sbuf.toString().isEmpty()) {
                                                            String modifiedResults = filterResultsByAuth(existingWorkflowItem, newWorkflowOwner, baseDir);

                                                            org.jdom.Element modifiedResultsElem = WorkflowXmlUtils.getStringAsElement("<LS_root_8675309>" + modifiedResults + "</LS_root_8675309>");
                                                            ElementFilter filesElementFilter2 = new ElementFilter("files");
                                                            if (modifiedResultsElem != null) { // optionmeta.Import : [[Text: {"fileName":"ds4_student_step_All_Data_2_2018_1026_102957.txt","importFileType":"file","importFileNameTitle":"Project: Unclassified\r\nDataset: wtf of unicode\r\nFile: ds4_student_step_All_Data_2_2018_1026_102957.txt","fileTypeSelection":"file","datasetListSelection":"372","uploadLocation":"importDataset","searchDatasetsString":"","datasetLink":"4","datasetName":"wtf of unicode","fileId":"434"}]]
                                                                    for (Iterator<org.jdom.Element> iter = modifiedResultsElem.getDescendants(filesElementFilter); iter.hasNext();) {
                                                                            org.jdom.Element filesElement = iter.next();
                                                                            // if workflowitem.edit or view, then copy files to new workflow
                                                                            // component_file_persistence, workflow_file, workflow_file_map, workflow_component_instance_persistence;
                                                                            for (org.jdom.Element fileElement : (List<org.jdom.Element>) filesElement.getChildren()) {
                                                                                    // Each input must contain a file element and a model element to be valid.
                                                                                    if (fileElement != null && fileElement.getChild("file_path") != null && fileElement.getChild("file_name") != null) {
                                                                                            String filePathStr = fileElement.getChild("file_path").getTextTrim();
                                                                                            if (filePathStr.matches("[0-9]+")) {
                                                                                                    String newComponentId = null;
                                                                                                    if (fileElement.getParentElement() != null && fileElement.getParentElement().getParentElement() != null
                                                                                                                    && fileElement.getParentElement().getParentElement().getParentElement() != null
                                                                                                                    && fileElement.getParentElement().getParentElement().getParentElement().getChildTextTrim("component_id") != null) {
                                                                                                            newComponentId = fileElement.getParentElement().getParentElement().getParentElement().getChildTextTrim("component_id");
                                                                                                    } else if (fileElement.getParentElement() != null && fileElement.getParentElement().getParentElement() != null
                                                                                                                    && fileElement.getParentElement().getParentElement().getChildTextTrim("component_id") != null) {
                                                                                                            newComponentId = fileElement.getParentElement().getParentElement().getChildTextTrim("component_id");
                                                                                                    }
                                                                                                    if (newComponentId != null) {
                                                                                                            Integer wfFileId = Integer.parseInt(filePathStr);
                                                                                                            WorkflowFileItem wfFileItem = wfFileDao.get(wfFileId);
                                                                                                            if (wfFileItem != null) {
                                                                                                                    String componentAccessLevel = componentAccessLevels.get(newComponentId);
                                                                                                                    if (componentAccessLevel != null && (componentAccessLevel.equalsIgnoreCase(WorkflowItem.LEVEL_VIEW)
                                                                                                                                    || componentAccessLevel.equalsIgnoreCase(WorkflowItem.LEVEL_EDIT))) {
                                                                                                                            if (!outputsToSave.contains(newComponentId)) {
                                                                                                                                    outputsToSave.add(newComponentId);
                                                                                                                            }
                                                                                                                            WorkflowFileItem newFileItem = saveWorkflowFileAsWorkflowFile(newWorkflowItem.getId(), newComponentId,
                                                                                                                                            wfFileItem.getTitle(), wfFileItem.getDescription(), wfFileItem, baseDir, newWorkflowOwner);
                                                                                                                            org.jdom.Element fpElem = fileElement.getChild("file_path");
                                                                                                                            if (newFileItem != null && newFileItem.getOwner().getId().equals(newWorkflowOwner.getId())) {
                                                                                                                                    fpElem.setText(newFileItem.getId().toString());
                                                                                                                            }
                                                                                                                    } else {
                                                                                                                            fileElement.removeContent();
                                                                                                                            unsatisfiedComponentAccess.add(newComponentId);
                                                                                                                    }
                                                                                                            }
                                                                                                    } else {
                                                                                                            fileElement.removeContent();
                                                                                                            unsatisfiedComponentAccess.add(newComponentId);
                                                                                                    }
                                                                                            }
                                                                                    }//end of if: fileElement != null.....
                                                                            } //end of for loop: filesElement.getChildren()
                                                                    } //end of for lop: modifiedResultsElem.getDescendants(filesElementFilter)
                                                            }//end of if modifiedResultsElem != null


                                                            if (modifiedResultsElem != null) {
                                                                    String finalString = WorkflowXmlUtils.getElementAsString(modifiedResultsElem).replaceAll("<LS_root_8675309>", "").replaceAll("</LS_root_8675309>", "");
                                                                    newWorkflowItem.setResults(finalString);
                                                            }
                                                    }//end of if (!sbuf.toString().isEmpty())
                                            }//end of if (resultsRoot != null && resultsRoot.getChildren() != null)
                                    } catch (IOException e) {
                                            logger.error("Workflow root could not be read.");
                                    } catch (JDOMException e) {
                                            logger.error("Workflow modified results could not be read.");
                                    }

                                    // Save the progress
                                    workflowDao.saveOrUpdate(newWorkflowItem);

                                    // Use the existing workflow item to determine component access since
                                    // it is a duplicate and the adjacency map has yet to be realized for the new workflow item.
                                    HashMap<String, String> accessMap = WorkflowAccessHelper.getComponentAccessMap(newWorkflowOwner,
                                                    WorkflowFileUtils.getStrictDirFormat(baseDir), existingWorkflowItem);
                                    Boolean notOwnedWorkflow = !(existingWorkflowItem.getOwner().getId().equals(newWorkflowOwner.getId()));

                                    File[] files = componentsDir.listFiles();

                                    // Setup new component instances
                                    WorkflowComponentInstancePersistenceDao wciPersistenceDao = DaoFactory.DEFAULT.getWorkflowComponentInstancePersistenceDao();
                                    WorkflowComponentInstanceDao wciDao = DaoFactory.DEFAULT.getWorkflowComponentInstanceDao();
                                    // Keep track of the Data (Import) custom json elements
                                    Map<String, org.jdom.Element> importJsons = new HashMap<String, org.jdom.Element>();

                                    for (Iterator<org.jdom.Element> iter = workflowRoot.getDescendants(componentElemFilter); iter.hasNext();) {
                                            org.jdom.Element componentElem = iter.next();
                                            if (componentElem != null) {
                                                    if (componentElem.getChild("component_id") != null) {
                                                            String componentId = componentElem.getChildTextTrim("component_id");

                                                            for (int idx = 0; files != null && idx < files.length; idx++) {
                                                                    File fileOrDir = files[idx];
                                                                    String backupFilePath = WorkflowFileUtils.sanitizePath(fileOrDir.getAbsolutePath());
                                                                    if (fileOrDir.isFile()) {
                                                                            String componentMatchPath = WorkflowFileUtils.sanitizePath(strictDirBaseDir + "/"
                                                                                            + WorkflowFileUtils.getWorkflowsDir(existingWorkflowItem.getId())
                                                                                            + WorkflowHelper.COMPONENTS_XML_DIRECTORY_NAME + "/"
                                                                                            + componentId + ".xml.bak");
                                                                            if (backupFilePath.endsWith(".bak") && componentMatchPath.equals(backupFilePath)) {
                                                                                    String newContent = null;
                                                                                    try {
                                                                                            // If it's a Data- or Import- component, or if the user doesn't have
                                                                                            // file access to it, then use the modified element.
                                                                                            if (componentId.matches("Data-1-x.*") || componentId.matches("Import-1-x.*")
                                                                                                            || unsatisfiedComponentAccess.contains(componentId)) {

                                                                                                    if (unsatisfiedComponentAccess.contains(componentId)) {
                                                                                                            // Custom import optionmeta
                                                                                                            ElementFilter optionsFilter = new ElementFilter("options");
                                                                                                            for (Iterator<org.jdom.Element> iter2 = componentElem.getDescendants(optionsFilter); iter2.hasNext();) {
                                                                                                                    org.jdom.Element optionElement = iter2.next();
                                                                                                                    if (optionElement.getName().equals("options")
                                                                                                                                    && optionElement.getChild("Import") != null) {
                                                                                                                            importJsons.put(componentId, optionElement.getChild("Import")); // mckhere
                                                                                                                    }
                                                                                                            }
                                                                                                    }


                                                                                                    // Finally, we can remove or modify the Data (Import) custom json
                                                                                                    // without ConcurrentModificationException.
                                                                                                    if (!importJsons.isEmpty()) {
                                                                                                            for (String cId : importJsons.keySet()) {
                                                                                                                    org.jdom.Element importElem = importJsons.get(cId);

                                                                                                                    if (unsatisfiedComponentAccess.contains(cId)) {
                                                                                                                            importElem.detach();
                                                                                                                    } else {
                                                                                                                            // update the file_path even though the json
                                                                                                                            // only matters to the UI, as the back-end
                                                                                                                            // uses the <options><files> elements instead.
                                                                                                                            JSONObject jsonObj = null;
                                                                                                                            try {
                                                                                                                                    jsonObj = new JSONObject(importElem.getTextTrim());
                                                                                                                            } catch (JSONException e) {

                                                                                                                            }
                                                                                                                            if (jsonObj != null) {
                                                                                                                                    try {
                                                                                                                                            jsonObj.put("file_path", "");
                                                                                                                                    } catch (JSONException e) {

                                                                                                                                    }
                                                                                                                            }
                                                                                                                    }
                                                                                                            }
                                                                                                    }
                                                                                                    newContent = WorkflowXmlUtils.getElementAsString(componentElem);
                                                                                            } else {
                                                                                                    // Otherwise, access is allowed for the component files
                                                                                                    // so we simply replace the workflow ids.
                                                                                                    String fileContent = WorkflowFileUtils.readFile(backupFilePath);
                                                                                                    try {
                                                                                                            org.jdom.Element filePersistentElement = WorkflowXmlUtils.getStringAsElement(fileContent);
                                                                                                            String compId = null;
                                                                                                            if (filePersistentElement != null && filePersistentElement.getChild("component_id") != null) {
                                                                                                                    compId = filePersistentElement.getChildText("component_id");
                                                                                                            }
                                                                                                            // If access to the component's ancestor is not view/edit, then
                                                                                                            // use the previously updated componentElem
                                                                                                            if (!accessMap.containsKey(compId)
                                                                                                                            || accessMap.get(compId) == null
                                                                                                                            || !(accessMap.get(compId).equalsIgnoreCase(WorkflowItem.LEVEL_EDIT)
                                                                                                                                            || accessMap.get(compId).equalsIgnoreCase(WorkflowItem.LEVEL_VIEW))) {
                                                                                                                    newContent = WorkflowXmlUtils.getElementAsString(componentElem);
                                                                                                                    unsatisfiedComponentAccess.add(compId);
                                                                                                            } else {
                                                                                                                    // Else the user can access the component's ancenstor
                                                                                                                    // so retain the input metadata, such as header info
                                                                                                                    newContent = fileContent;
                                                                                                            }
                                                                                                    } catch (JDOMException e) {
                                                                                                            logger.error("Could not filter file persistent component XML");
                                                                                                    }
                                                                                            }
                                                                                    } catch (IOException e) {
                                                                                            logger.error("Cannot parse new workflow XML.");
                                                                                    }

                                                                                    String componentXmlFilePath = newComponentsDir + "/" + fileOrDir.getName();
                                                                                    File componentXmlFile = new File(componentXmlFilePath);
                                                                                    File backupFile = new File(backupFilePath);
                                                                                    if (backupFile != null && backupFile.exists()) {
                                                                                            // Replace instances of the original workflow id with the new id
                                                                                            if (newContent != null) {
                                                                                                    String componentXml = WorkflowAccessHelper.removeDataFromPrivateOptions(
                                                                                                                    newContent, newWorkflowItem, newWorkflowOwner, baseDir, notOwnedWorkflow);

                                                                                                    WorkflowFileUtils.writeFile(componentXmlFilePath,
                                                                                                                    componentXml.replaceAll("<workflow_id>" + workflowId + "</workflow_id>",
                                                                                                                                    "<workflow_id>" + newWorkflowItem.getId() + "</workflow_id>")
                                                                                                                    .replaceAll("workflows/" + workflowId + "/",
                                                                                                                                    "workflows/" + newWorkflowItem.getId() + "/")
                                                                                                                    .replaceAll("<LS_root_8675309>", "").replaceAll("</LS_root_8675309>", "")
                                                                                                                    );
                                                                                            }
                                                                                    }
                                                                            }//end of if (backupFilePath.endsWith(".bak") && componentMatchPath.equals(backupFilePath))
                                                                    }//end of if (fileOrDir.isFile())
                                                            } // end for loop: idx < files.length


                                                            try {
                                                                    newWorkflowItem.setWorkflowXml(WorkflowAccessHelper.removeDataFromPrivateOptions(
                                                                                    WorkflowXmlUtils.getElementAsString(workflowRoot), newWorkflowItem,
                                                                                    newWorkflowOwner, baseDir, notOwnedWorkflow));
                                                            } catch (IOException e) {
                                                                    logger.error("Could not update workflow XML");
                                                            }

                                                            // Save the progress
                                                            workflowDao.saveOrUpdate(newWorkflowItem);

                                                            Boolean hasComponentWarning = false;
                                                            WorkflowComponentInstancePersistenceItem existingWcip =
                                                                            wciPersistenceDao.findByWorkflowAndId(existingWorkflowItem, componentId);

                                                            // workflow_id, component_name, dirty_file, dirty_option, state = new, depth_level = 0, errors = null
                                                            WorkflowComponentInstanceItem wciItem = new WorkflowComponentInstanceItem();
                                                            wciItem.setComponentName(componentId);
                                                            wciItem.setDirtyOption(true);
                                                            wciItem.setWorkflow(newWorkflowItem);
                                                            if (existingWcip != null) {
                                                                    wciItem.setDepthLevel(existingWcip.getDepthLevel());
                                                                    wciItem.setDirtyAddConnection(existingWcip.getDirtyAddConnection());
                                                                    wciItem.setDirtyAncestor(existingWcip.getDirtyAncestor());
                                                                    wciItem.setDirtyDeleteConnection(existingWcip.getDirtyDeleteConnection());
                                                                    wciItem.setDirtyFile(existingWcip.getDirtyFile());
                                                                    wciItem.setDirtyOption(existingWcip.getDirtyOption());
                                                                    wciItem.setDirtySelection(existingWcip.getDirtySelection());
                                                                    wciItem.setErrors(existingWcip.getErrors());
                                                                    if (unsatisfiedComponentAccess.contains(existingWcip.getComponentName())) {
                                                                            wciItem.setState(WorkflowComponentInstanceItem.WF_STATE_NEW);
                                                                    } else {
                                                                            wciItem.setState(existingWcip.getState());
                                                                            if (existingWcip.getState().equalsIgnoreCase(
                                                                                WorkflowComponentInstanceItem.COMPLETED_WARN)) {
                                                                                hasComponentWarning = true;
                                                                            }
                                                                    }
                                                            } else {
                                                                    wciItem.setState(WorkflowComponentInstanceItem.WF_STATE_NEW);
                                                            }

                                                            wciDao.saveOrUpdate(wciItem);

                                                            // Copy Debugging info
                                                            WorkflowFileHelper.copyDebugFiles(baseDir, componentId, existingWorkflowItem,
                                                                    newWorkflowItem, newWorkflowOwner, hasComponentWarning);
                                                    }
                                            }
                                    }
                                    try {
                                            newWorkflowItem.setWorkflowXml(WorkflowAccessHelper.removeDataFromPrivateOptions(
                                                            WorkflowXmlUtils.getElementAsString(workflowRoot), newWorkflowItem,
                                                            newWorkflowOwner, baseDir, notOwnedWorkflow));
                                    } catch (IOException e) {
                                            logger.error("Could not update workflow XML: " + e.toString());
                                    }
                                    // Save the progress
                                    workflowDao.saveOrUpdate(newWorkflowItem);
                                    workflowHelper.saveWorkflowToDatabase(newWorkflowItem, baseDir);

                            } else {
                                    workflowId = null;
                            }
                    } //end of if (existingWorkflowItem != null)
                    else {
                            workflowId = null;
                    }
            }//end of if (workflowId != null)

            if (existingWorkflowItem == null) {
                    String componentsXml = "";
                    newWorkflowItem.setWorkflowXml("<workflow>" + "<id>" + newWorkflowItem.getId() + "</id>" + "<name>"
                                    + WorkflowFileUtils.htmlEncode(newWorkflowItem.getWorkflowName()) + "</name>"
                                    + "<isShared>" + isShared.toString()
                                    + "</isShared>" + "<lastUpdated>" + DATE_FORMAT.format(newWorkflowItem.getLastUpdated())
                                    + "</lastUpdated>" + componentsXml + "</workflow>");
            }


            newWorkflowItem.setLastUpdated(now);
            workflowDao.saveOrUpdate(newWorkflowItem);

            if (Hibernate.isPropertyInitialized(newWorkflowItem, "workflowXml")) {
                    wpItem.setWorkflow(newWorkflowItem);
                    String lastWorkflowXml = newWorkflowItem.getWorkflowXml();
                    if (lastWorkflowXml != null) {
                            wpItem.setWorkflowXml(lastWorkflowXml);
                            wpItem.setLastUpdated(now);
                            wpDao.saveOrUpdate(wpItem);
                            // Remove the component instance persistence objects
                            WorkflowComponentInstancePersistenceDao wciPersistDao = DaoFactory.DEFAULT.getWorkflowComponentInstancePersistenceDao();
                            List<WorkflowComponentInstancePersistenceItem> wciPersistList = wciPersistDao.findByWorkflow(newWorkflowItem);
                            if (wciPersistList != null) {
                                    for (WorkflowComponentInstancePersistenceItem wciPersistenceItem : wciPersistList) {
                                            wciPersistDao.delete(wciPersistenceItem);
                                    }
                            }
                    }
            }

            workflowHelper.saveComponentInstances(newWorkflowItem, baseDir);
            workflowHelper.saveComponentXmlFiles(existingWorkflowItem, newWorkflowItem, baseDir);

            return newWorkflowItem;
    }


    /**copied from LearnSphereServlet. Ideally this should be in the WorkflowHelper
     *
     *
     * Saves an uploaded file to the system.
     * @param workflowId the workflow id
     * @param componentId the component id
     * @param fileTitle the file title (e.g. A, file1, myFile, etc.)
     * @param fileDesc the file description (e.g. Student-step, transaction, etc)
     * @param existingFileItem the existing file item
     * @param datashopBaseDir the files base directory, e.g. /datashop/files
     * @param userItem the user item (file uploader/owner)
     * @return the FileItem
     * @throws Exception any exception
     */
    private WorkflowFileItem saveWorkflowFileAsWorkflowFile(Long workflowId, String componentId,
            String fileTitle, String fileDesc, WorkflowFileItem existingFileItem, String datashopBaseDir,
            UserItem userItem) {

        Boolean successFlag = false;
        WorkflowFileItem dsFileItem = null;
        if (existingFileItem != null) {

            WorkflowDao workflowDao = DaoFactory.DEFAULT.getWorkflowDao();
            WorkflowItem workflowItem = workflowDao.get(workflowId);
            DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
            WorkflowFileDao wfFileDao = DaoFactory.DEFAULT.getWorkflowFileDao();
            WorkflowHelper workflowHelper = HelperFactory.DEFAULT.getWorkflowHelper();

            DatasetItem datasetItem = null;

            String subPath = WorkflowFileUtils.getWorkflowsDir(workflowId) + componentId + "/output/";

            String fullPath = WorkflowFileUtils.sanitizePath(existingFileItem.getFullPathName(datashopBaseDir)) + existingFileItem.getFileName();
            File testFile = new File(fullPath);

            if (testFile != null && testFile.isFile() && testFile.canRead()) {

                String fileFullName = null;
                if (fullPath.indexOf('/') >= 0) {
                    fileFullName = fullPath.substring(fullPath.lastIndexOf('/') + 1);
                }
                File newDirectory = null;

                // Check to make sure the user has selected a file.
                if (fileFullName != null && fileFullName.length() > 0) {
                    // Create the directory
                    String wholePath = WorkflowFileUtils.sanitizePath(datashopBaseDir + "/" + subPath);
                    newDirectory = new File(wholePath);
                    if (newDirectory.isDirectory() || newDirectory.mkdirs()) {
                        FileUtils.makeWorldReadable(newDirectory);
                    } else {
                        logger.error("saveWorkflowFileAsWorkflowFile: Creating directory failed " + newDirectory);
                    }
                } else {
                    logger.error("saveWorkflowFileAsWorkflowFile: The fileName cannot be null or empty.");
                }
                List<String> fileList = Arrays.asList(newDirectory.list());
                String fileNamePrefix = null;
                String newFileName = null;
                String fileExt = "";
                File tmpFile = new File(fullPath);
                Boolean moveFile = false;
                File inputFile = null;
                List<String> textExtensions = Arrays.asList(new String[] { ".txt", ".csv", ".tsv", ".log" });

                if (tmpFile.getName().matches((".*\\.zip"))) {

                    try {
                        inputFile = File.createTempFile("wf" + workflowId, ".tmp", newDirectory);
                        moveFile = true;
                        newFileName = WorkflowFileUtils.unzipFile(tmpFile, inputFile);

                    } catch (IOException e) {
                        logger.error("Could not unzip file " + tmpFile.getAbsolutePath() + " to "
                                + inputFile.getAbsolutePath());
                    }

                } else {
                    inputFile = tmpFile;
                    newFileName = tmpFile.getName();
                    fileNamePrefix = tmpFile.getName();
                }

                String mimeType = null;

                try {
                    mimeType = WorkflowFileUtils.getMimeType(inputFile);
                } catch (IOException e) {
                    logger.error("Tika could not retrieve mime type of file " + inputFile.getAbsolutePath());
                }
                // When taken as a zipEntry, some systems can mistakenly
                // identify the mime type as application/octet-stream.
                // This fixes that. (UPDATE: TAKING OUT PRIOR TO TESTING.. I THINK IT ISN'T NEEDED)
                if (tmpFile.getName().matches((".*\\.zip")) && mimeType != null
                        && mimeType.equalsIgnoreCase("application/octet-stream") && fileExt != null
                        && textExtensions.contains(fileExt)) {
                    // mimeType = "text/plain";
                }

                if (inputFile != null && newDirectory != null) {
                    // Write the file to the directory
                    File newFile = new File(newDirectory.getAbsolutePath(), newFileName);
                    Hibernate.initialize(workflowItem);
                    if (workflowItem != null && !workflowItem.getState().equalsIgnoreCase(WorkflowItem.WF_STATE_RUNNING)) {
                        try {
                            // If we created a temporary text file
                            // while extracting from a zip, then move the file
                            ComponentFileDao compFileDao = DaoFactory.DEFAULT.getComponentFileDao();

                            // This method is also used for saving new files from existing workflow_file items.
                            List<ComponentFileItem> existingCfItems = compFileDao.findByFile(existingFileItem);
                            if (!existingCfItems.isEmpty()) {
                                ComponentFileItem existingCfItem = existingCfItems.get(0);
                                if (datasetItem == null && existingCfItem.getDataset() != null) {
                                    datasetItem = datasetDao.get((Integer) existingCfItem.getDataset().getId());
                                }
                            }

                            if (newFile.exists()) {
                                newFile.delete();
                            }

                            if (moveFile) {
                                Files.move(inputFile.toPath(), newFile.toPath());
                            // Otherwise, we will copy the file.
                            } else {
                                FileUtils.copyFile(inputFile, newFile);
                            }
                            Integer fileId = WorkflowFileHelper.createOrGetWorkflowFile(
                                workflowItem, userItem, datashopBaseDir,
                                        newFile, fileTitle, "0", datasetItem, componentId, false);
                            WorkflowFileDao workflowFileDao = DaoFactory.DEFAULT.getWorkflowFileDao();
                            if (fileId != null) {
                                dsFileItem = workflowFileDao.get(fileId);
                                successFlag = true;
                            }
                        } catch (IOException e) {
                            logger.error("Could not copy file " + inputFile.getAbsolutePath() + " to "
                                    + newFile.getAbsolutePath());
                        }
                    }

                }
            }
        }
        if (successFlag) {
            return dsFileItem;
        } else {
            return null;
        }
    }

    /**
     * copied from LearnSphereServlet. Ideally this should be in the WorkflowHelper
     */
    private static final String[] dataSensitiveElements = { "optionmeta", "inputmeta", "files", "options", "errors" };
    private String filterResultsByAuth(WorkflowItem workflowItem, UserItem loggedInUserItem, String baseDir) {
            String modifiedResults = null;
            WorkflowComponentAdjacencyDao wfcAdjDao = DaoFactory.DEFAULT.getWorkflowComponentAdjacencyDao();
            WorkflowHelper workflowHelper = HelperFactory.DEFAULT.getWorkflowHelper();

            List<WorkflowComponentAdjacencyItem> wfcAdjItems = wfcAdjDao.findByWorkflow(workflowItem);
            if (workflowItem != null) {
                HashMap<String, String> accessMap = null;
                String unmodifiedResults = workflowItem.getResults();

                accessMap = WorkflowAccessHelper.getComponentAccessMap(loggedInUserItem,
                                WorkflowFileUtils.getStrictDirFormat(baseDir), workflowItem);
                if (unmodifiedResults == null) {
                    unmodifiedResults = new String("");
                }

                WorkflowComponentDao wcDao = DaoFactory.DEFAULT.getWorkflowComponentDao();
                try {
                    List<String> foundComponents = new ArrayList<String>();
                    org.jdom.Element tempResults = WorkflowXmlUtils.getStringAsElement("<LS_root_8675309>"
                        + unmodifiedResults + "</LS_root_8675309>");

                    List<org.jdom.Element> deleteElems = new ArrayList<org.jdom.Element>();
                    if (tempResults != null && tempResults.getChildren() != null && !tempResults.getChildren().isEmpty()) {
                        for (Iterator<org.jdom.Element> rootIter = tempResults.getChildren().iterator(); rootIter.hasNext(); ) {
                                org.jdom.Element outputElem = rootIter.next();
                            String compId = null;
                            if (outputElem.getChild("component_id") != null) {
                                compId = outputElem.getChildText("component_id");
                                if (!foundComponents.contains(compId)) {
                                    foundComponents.add(compId);

                                    if (!accessMap.containsKey(compId)
                                        || accessMap.get(compId) == null
                                            || !(accessMap.get(compId).equalsIgnoreCase(WorkflowItem.LEVEL_EDIT)
                                            || accessMap.get(compId).equalsIgnoreCase(WorkflowItem.LEVEL_VIEW))) {
                                        for (String dsElement : dataSensitiveElements) {
                                            if (outputElem.getChildren(dsElement) != null
                                                    && !outputElem.getChildren(dsElement).isEmpty()) {
                                                deleteElems.addAll(outputElem.getChildren(dsElement));
                                            }
                                        }
                                    } else {
                                        WorkflowComponentInstancePersistenceDao wfcipDao = DaoFactory.DEFAULT.getWorkflowComponentInstancePersistenceDao();
                                        WorkflowComponentInstancePersistenceItem wfcipItem =
                                            wfcipDao.findByWorkflowAndId(workflowItem, compId);

                                        if (wfcipItem != null && wfcipItem.isDirty()
                                                && !loggedInUserItem.getId().equals(workflowItem.getOwner().getId())) {
                                            for (String dsElement : dataSensitiveElements) {
                                                if (outputElem.getChildren(dsElement) != null
                                                        && !outputElem.getChildren(dsElement).isEmpty()) {
                                                    deleteElems.addAll(outputElem.getChildren(dsElement));
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    for (String componentId : accessMap.keySet()) {
                        if (componentId != null) {
                            String componentType = null;
                            if (componentId != null && componentId.matches(WorkflowIfaceHelper.COMPONENT_ID_PATTERN)) {
                                componentType = componentId.substring(0, componentId.indexOf("-1-x"));
                            }
                            String restrictString = null;
                            if (accessMap.get(componentId) != null && (accessMap.get(componentId).equalsIgnoreCase(WorkflowItem.LEVEL_EDIT)
                                    || accessMap.get(componentId).equalsIgnoreCase(WorkflowItem.LEVEL_VIEW))) {
                                restrictString = new String("");
                            } else {
                                restrictString = new String("<restricted />");
                            }

                            if (!foundComponents.contains(componentId) && componentType != null) {
                                String emptyOutput = "<output0>"
                                    + "<component_id>" + componentId + "</component_id>"
                                    + "<component_id_human>" + componentType + "</component_id_human>"
                                    + "<component_type>" + componentType + "</component_type>"
                                    + "<component_name>" + componentId + "</component_name>"
                                    + "<elapsed_seconds>0</elapsed_seconds>"
                                    + "<errors>No workflow results.</errors>"
                                    + "<files />"
                                    + "<inputmeta />"
                                    + "<optionmeta />"
                                    + restrictString
                                    + "</output0>";
                                tempResults.addContent(WorkflowXmlUtils.getStringAsElement(emptyOutput).detach());
                            }
                        }
                    }


                    for (org.jdom.Element dsElement : deleteElems) {
                        dsElement.removeContent();
                    }

                    if (tempResults != null && !tempResults.getChildren().isEmpty()) {
                        for (org.jdom.Element outNode : (List<org.jdom.Element>) tempResults.getChildren()) {
                            if (outNode != null && !outNode.getChildren().isEmpty()) {

                                    if (outNode.getChild("component_id") != null) {
                                        String cId = outNode.getChildTextTrim("component_id");
                                        if (accessMap.get(cId) == null || !(accessMap.get(cId).equalsIgnoreCase(WorkflowItem.LEVEL_EDIT)
                                                || accessMap.get(cId).equalsIgnoreCase(WorkflowItem.LEVEL_VIEW))) {
                                            outNode.addContent(new org.jdom.Element("restricted"));
                                        }
                                    }

                            }
                        }
                    }

                    String tempResultsString = WorkflowXmlUtils.getElementAsString(tempResults)
                        .replaceAll("<LS_root_8675309>", "").replaceAll("</LS_root_8675309>", "").trim();

                    String modifiedResultsString = WorkflowAccessHelper.removePrivateOptionMetaData(
                        tempResultsString, loggedInUserItem, workflowItem.getOwner());
                    if (modifiedResultsString != null) {
                        modifiedResults = modifiedResultsString.replaceAll("<LS_root_8675309>", "").replaceAll("</LS_root_8675309>", "");
                    }

                } catch (JDOMException e) {
                    logger.error("Could not convert results to element.");
                } catch (IOException e) {
                    logger.error("Could not create new element.");
                }


            }
            return modifiedResults;
        }

}
