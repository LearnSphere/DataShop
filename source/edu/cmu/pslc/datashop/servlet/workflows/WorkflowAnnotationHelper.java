package edu.cmu.pslc.datashop.servlet.workflows;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.filter.ElementFilter;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.WorkflowAnnotationDao;
import edu.cmu.pslc.datashop.workflows.WorkflowAnnotationItem;
import edu.cmu.pslc.datashop.workflows.WorkflowItem;

public class WorkflowAnnotationHelper {

       /** Debug logging. */
       private Logger logger = Logger.getLogger(getClass().getName());
    /**
     * Save a specific annotation to the db
     * @param annotationId id of annotation to save
     * @param workflowId id of the workflow the annotation is in
     * @param text String contents of the annotation
     * @param lastUpdated last time the annotation was changed
     */
    private  void saveAnnotationToDb(String annotationId, String workflowId,
    String text, Date lastUpdated) {
        logger.debug("in saveAnnotationToDb");
        WorkflowAnnotationDao wad = DaoFactory.DEFAULT.getWorkflowAnnotationDao();
        Long annotId = null;
        try {
            annotId = Long.parseLong(annotationId);
        } catch (Exception e) {
            logger.error("Couldn't parse annotationId");
            return;
        }

        WorkflowAnnotationItem annotationItem = wad.get(annotId);
        if (annotationItem == null) {
            logger.error("Did not find annotation item.  Annotation id: " + annotId.toString());
            return;
        }

        String savedText = annotationItem.getText();
        logger.debug("savedText: " + savedText);

        // If the user updated the annotation, save the new text and update time stamp
        if (!savedText.equals(text)) {
            annotationItem.setText(text);
            Date d = new Date();
            annotationItem.setLastUpdated(d);
            wad.saveOrUpdate(annotationItem);
        }
    }

    /**
     * Save the annotations to db
     * @param workflowItem the workflowItem
     * @param dataFilesDirectory the data files directory (usually, /datashop/dataset_files)
     * @param digraphDoc the current digraph of the workflow.  Used to get annotation information.
     */
    public void saveAnnotations(WorkflowItem workflowItem, String dataFilesDirectory, Element digraphDoc) {
        if (digraphDoc == null || workflowItem == null) {
            logger.error("digraphDoc or workflowItem == null in saveAnnotations");
            return;
        }

        Element wfId = digraphDoc.getChild("id");
        String workflowId = wfId.getTextTrim();

        Element annotations = (Element) digraphDoc.getChild("annotations");

        if (annotations != null && annotations.getChildren() != null) {
            for (Element annotation : (List<Element>) annotations.getChildren()) {
                String annotationId = "";
                String annotationWorkflowId = "";
                String annotationText = "";
                Date lastUpdated = null;

                Element annotationIdEle = annotation.getChild("annotation_id");
                if (annotationIdEle != null) {
                    annotationId = annotationIdEle.getTextTrim();
                }
                Element annotationWfIdEle = annotation.getChild("workflow_id");
                if (annotationWfIdEle != null) {
                    annotationWorkflowId = annotationWfIdEle.getTextTrim();
                }
                Element annotationTextEle = annotation.getChild("text");
                if (annotationTextEle != null) {
                    annotationText = annotationTextEle.getTextTrim();
                }
                Element annotationUpdatedEle = annotation.getChild("lastUpdated");
                if (annotationUpdatedEle != null) {
                    String annotationDateStr = annotationUpdatedEle.getTextTrim();
                    try {
                        Date d = new Date();
                        Long dateNum = Long.parseLong(annotationDateStr);
                        lastUpdated = new Date(dateNum);//new Date(annotationDateStr);
                    } catch (Exception e) {
                        logger.error("Couldn't convert date from annotation xml: " +
                                     annotationDateStr + ". " + e.toString());
                    }
                }

                saveAnnotationToDb(annotationId, annotationWorkflowId, annotationText, lastUpdated);
            }
        }
    }

    /**
     * Delete all annotations from the DB that do not exist in this workflow
     * @param workflowItem workflow to delete annotations from
     * @param workflowRootElement root of workflow XML that has up to date info on annotations
     * @param saveFlag
     */
    public static void removeDeletedAnnotationsFromDB(WorkflowItem workflowItem,
            Element workflowRootElement, Boolean saveFlag) {
        // Remove annotations that no longer exist in the workflow
        WorkflowAnnotationDao wad = DaoFactory.DEFAULT.getWorkflowAnnotationDao();
        List<WorkflowAnnotationItem> annotations = wad.find(workflowItem);

        if (annotations != null) {
            for (WorkflowAnnotationItem annotation : annotations) {
                // Does the digraphDoc (workflow in GUI) contain this annotation still?
                Boolean foundInLatest = false;
                if (workflowRootElement != null) {
                    // Look through all descendants of the latest workflow xml to detect
                    // if the queued annotation still exists.

                    for (Iterator<Element> iter = workflowRootElement
                                                  .getDescendants(new ElementFilter()); iter.hasNext();) {
                        Element desc = iter.next();
                        if (desc.getName().equalsIgnoreCase("annotation_id")
                                && desc.getText().trim().equalsIgnoreCase(
                                    annotation.getId().toString())) {
                            foundInLatest = true;
                        }
                    }

                    if (!foundInLatest && saveFlag) {
                        // The item in the queue no longer exists
                        // in the workflow so we can
                        // delete it from the database.
                        wad.delete(annotation);
                    }
                }
            }
        }
    }
}
