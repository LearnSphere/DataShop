/**
 *
 */
package edu.cmu.pslc.datashop.servlet.sampletodataset;

import java.util.Date;
import org.apache.commons.lang.time.FastDateFormat;
import edu.cmu.pslc.datashop.dto.DTO;
/**
 *
 * @author Mike Komisin
 * @version $Revision: 10810 $
 * <BR>Last modified by: $Author:  $
 * <BR>Last modified on: $Date: 2014-03-17 14:07:52 -0400 (Mon, 17 Mar 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class SampleHistoryDto extends DTO {
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
    /** Time. */
    private Date time;
    /** Action field. */
    private String action;
    /** Info field. */
    private String info;

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

}
