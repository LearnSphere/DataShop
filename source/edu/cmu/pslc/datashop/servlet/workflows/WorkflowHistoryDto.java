/**
 *
 */
package edu.cmu.pslc.datashop.servlet.workflows;

import java.util.Date;
import org.apache.commons.lang.time.FastDateFormat;
import edu.cmu.pslc.datashop.dto.DTO;
/**
 *
 * @author Mike Komisin
 * @version $Revision:  $
 * <BR>Last modified by: $Author:  $
 * <BR>Last modified on: $Date:  $
 * <!-- $KeyWordsOff: $ -->
 */
public class WorkflowHistoryDto extends DTO {
    /** Date format. */
    private static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("MMM d, yyyy");
    /** User id. */
    private String userId;
    /** User id with email link. */
    private String userLink;
    /** Dataset id. */
    private Integer datasetId;
    /** Dataset name. */
    private String datasetName;
    /** Sample id. */
    private Integer sampleId;
    /** Sample name. */
    private String sampleName;
    /** Import Queue id. */
    private Integer importQueueId;
    /** Import Queue name. */
    private String importQueueName;
    /** Workflow id. */
    private Long workflowId;
    /** Workflow name. */
    private String workflowName;
    /** Time. */
    private Date time;
    /** Action field. */
    private String action;
    /** Info field. */
    private String info;
    /** Sample filters. */
    private String sampleFilters;

    /** Get the user id.
     * @return the userId
     */
    public String getUserId() {
        return userId;
    }
    /** Set the user id.
     * @param userId the userId to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }
    /** Get the user id with an email link.
     * @return the user id with an email link
     */
    public String getUserLink() {
        return userLink;
    }
    /** Set the user id with an email link.
     * @param userLink the user id with an email link
     */
    public void setUserLink(String userLink) {
        this.userLink = userLink;
    }
    /** Get the dataset id.
     * @return the datasetId
     */
    public Integer getDatasetId() {
        return datasetId;
    }
    /** Set the dataset id.
     * @param datasetId the datasetId to set
     */
    public void setDatasetId(Integer datasetId) {
        this.datasetId = datasetId;
    }
    /** Get the dataset name.
     * @return the datasetName
     */
    public String getDatasetName() {
        return datasetName;
    }
    /** Set the dataset name.
     * @param datasetName the datasetName to set
     */
    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }
    /** Get the sample id.
     * @return the sampleId
     */
    public Integer getSampleId() {
        return sampleId;
    }
    /** Set the sample id.
     * @param sampleId the sampleId to set
     */
    public void setSampleId(Integer sampleId) {
        this.sampleId = sampleId;
    }
    /** Get the sample name.
     * @return the sampleName
     */
    public String getSampleName() {
        return sampleName;
    }
    /** Set the sample name.
     * @param sampleName the sampleName to set
     */
    public void setSampleName(String sampleName) {
        this.sampleName = sampleName;
    }
    /** Get the import queue id.
     * @return the importQueueId
     */
    public Integer getImportQueueId() {
        return importQueueId;
    }
    /** Set the import queue id.
     * @param importQueueId the importQueueId to set
     */
    public void setImportQueueId(Integer importQueueId) {
        this.importQueueId = importQueueId;
    }
    /** Get the import queue name.
     * @return the importQueueName
     */
    public String getImportQueueName() {
        return importQueueName;
    }
    /** Set the import queue name.
     * @param importQueueName the importQueueName to set
     */
    public void setImportQueueName(String importQueueName) {
        this.importQueueName = importQueueName;
    }
    /** Get the workflow id.
     * @return the workflowId
     */
    public Long getWorkflowId() {
        return workflowId;
    }
    /** Set the workflow id.
     * @param workflowId the workflowId to set
     */
    public void setWorkflowId(Long workflowId) {
        this.workflowId = workflowId;
    }
    /** Get the workflow name.
     * @return the workflowName
     */
    public String getWorkflowName() {
        return workflowName;
    }
    /** Set the workflow name.
     * @param workflowName the workflowName to set
     */
    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }
    /** Get the time.
     * @return the time
     */
    public Date getTime() {
        return time;
    }
    /** Set the time.
     * @param time the time to set
     */
    public void setTime(Date time) {
        this.time = time;
    }
    /**
     * Get the date/time as a string.
     * @return the date/time as a string
     */
    public String getTimeAsString() {
        return DATE_FORMAT.format(time);
    }
    /** Get the action.
     * @return the action
     */
    public String getAction() {
        return action;
    }
    /** Set the action.
     * @param action the action to set
     */
    public void setAction(String action) {
        this.action = action;
    }
    /** Get the info.
     * @return the info
     */
    public String getInfo() {
        return info;
    }
    /** Set the info.
     * @param info the info to set
     */
    public void setInfo(String info) {
        this.info = info;
    }
    /** Get the sample filters text.
     * @return the sampleFilters
     */
    public String getSampleFilters() {
        return sampleFilters;
    }
    /** Set the sample filters text.
     * @param sampleFilters the sampleFilters to set
     */
    public void setSampleFilters(String sampleFilters) {
        this.sampleFilters = sampleFilters;
    }

}
