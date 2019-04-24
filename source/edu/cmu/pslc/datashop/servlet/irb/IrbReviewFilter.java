package edu.cmu.pslc.datashop.servlet.irb;

import java.util.Date;

import org.apache.commons.lang.time.FastDateFormat;

import edu.cmu.pslc.datashop.servlet.ServletDateUtil;

/**
 * Helper class for strings used to filter IRB Review page contents.
 *
 * @author Cindy Tipper
 * @version $Revision: 10513 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2014-02-03 12:56:21 -0500 (Mon, 03 Feb 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class IrbReviewFilter {

    //----- ATTRIBUTES -----

    /** String by which to filter 'Subject to DataShop 2012 IRB'. */
    private String subjectTo;
    /** String by which to filter 'Shareability Review Status'. */
    private String shareability;
    /** String by which to filter 'Data Collection Type'. */
    private String dataCollectionType;
    /** String by which to filter 'Unreviewed Datasets'. */
    private String unreviewedDatasets;

    /** String by which to filter 'Public/Private'. */
    private String publicStr;
    /** String by which to filter 'Needs Attention'. */
    private Boolean needsAttn;

    /** Project Created before String. */
    private String pcBefore;
    /** Project Created date. */
    private Date pcDate;

    /** Dataset Last Added before String. */
    private String dlaBefore;
    /** Dataset Last Added date. */
    private Date dlaDate;

    //----- CONSTRUCTOR -----

    /**
     * Constructor.
     * @param subjToStr the string by which to filter 'Subject to DataShop 2012 IRB'
     * @param shareabilityStr the string by which to filter 'Shareability Review Status'
     * @param dataTypeStr the string by which to filter 'Data Collection Type'
     * @param datasetsStr the string by which to filter 'Unreviewed Datasets'
     * @param publicStr the string by which to filter 'Public/Private'
     * @param needsAttn the string by which to filter 'Needs Attention'
     */
    public IrbReviewFilter(String subjToStr, String shareabilityStr,
                           String dataTypeStr, String datasetsStr,
                           String publicStr,
                           String needsAttn) {
        setSubjectTo(subjToStr);
        setShareability(shareabilityStr);
        setDataCollectionType(dataTypeStr);
        setUnreviewedDatasets(datasetsStr);
        setPublicStr(publicStr);
        setNeedsAttn(needsAttn);
    }

    //----- GETTERS and SETTERS -----

    /**
     * Setter for 'Subject to DataShop 2012 IRB.'.
     * @param subjToStr String value
     */
    public void setSubjectTo(String subjToStr) {
        if ((subjToStr != null) && subjToStr.equals("blank")) { subjToStr = null; }
        this.subjectTo = subjToStr;
    }

    /**
     * Getter for 'Subject to DataShop 2012 IRB.'.
     * @return String the value
     */
    public String getSubjectTo() {
        return subjectTo;
    }

    /**
     * Setter for 'Shareability Review Status'.
     * @param shareStr String value
     */
    public void setShareability(String shareStr) {
        if ((shareStr != null) && shareStr.equals("blank")) { shareStr = null; }
        this.shareability = shareStr;
    }

    /**
     * Getter for 'Shareability Review Status'.
     * @return String the value
     */
    public String getShareability() {
        return shareability;
    }

    /**
     * Setter for 'Data Collection Type'.
     * @param dataTypeStr String value
     */
    public void setDataCollectionType(String dataTypeStr) {
        if ((dataTypeStr != null) && dataTypeStr.equals("blank")) { dataTypeStr = null; }
        this.dataCollectionType = dataTypeStr;
    }

    /**
     * Getter for 'Data Collection Type'.
     * @return String the value
     */
    public String getDataCollectionType() {
        return dataCollectionType;
    }

    /**
     * Setter for 'Unreviewed Datasets'.
     * @param datasetsStr String value
     */
    public void setUnreviewedDatasets(String datasetsStr) {
        if ((datasetsStr != null) && datasetsStr.equals("blank")) { datasetsStr = null; }
        this.unreviewedDatasets = datasetsStr;
    }

    /**
     * Getter for 'Unreviewed Datasets'.
     * @return String the value
     */
    public String getUnreviewedDatasets() {
        return unreviewedDatasets;
    }

    /**
     * Gets the publicStr.
     * @return the publicStr
     */
    public String getPublicStr() {
        return publicStr;
    }

    /**
     * Sets the publicStr.
     * @param publicStr the publicStr to set
     */
    public void setPublicStr(String publicStr) {
        if ((publicStr != null) && publicStr.equals("blank")) { publicStr = null; }
        this.publicStr = publicStr;
    }

    /**
     * Gets the needsAttn.
     * @return the needsAttn
     */
    public Boolean getNeedsAttn() {
        return needsAttn;
    }

    /**
     * Sets the needsAttn.
     * @param needsAttn the needsAttn to set
     */
    public void setNeedsAttn(String needsAttn) {
        if (needsAttn != null) {
            if (needsAttn.equals("blank")) {
                this.needsAttn = null;
            } else if (needsAttn.equals("true")) {
                this.needsAttn = true;
            } else if (needsAttn.equals("false")) {
                this.needsAttn = false;
            }
        } else {
            needsAttn = null;
        }
    }

    /** Date-only date format. */
    private static final FastDateFormat DATE_FMT = FastDateFormat.getInstance("yyyy-MM-dd");

    /**
     * Gets the pcBefore.
     * @return the pcBefore
     */
    public String getPcBefore() {
        return pcBefore;
    }

    /**
     * Sets the pcBefore.
     * @param pcBefore the pcBeforeto set
     */
    public void setPcBefore(String pcBefore) {
        this.pcBefore = pcBefore;
    }

    /**
     * Gets the pcDate.
     * @return the pcDate
     */
    public Date getPcDate() {
        return pcDate;
    }
    /**
     * Gets the pcDateStr.
     * @return the pcDate as a string
     */
    public String getPcDateStr() {
        if (pcDate == null) { return null; }
        return DATE_FMT.format(pcDate);
    }

    /**
     * Sets the pcDate.
     * @param pcDate the pcDate to set
     */
    public void setPcDate(Date pcDate) {
        this.pcDate = pcDate;
    }
    /**
     * Sets the pcDate.
     * @param dateString the date string
     */
    public void setPcDate(String dateString) {
        Date theDate = ServletDateUtil.getDateFromString(dateString);
        if (theDate != null) {
            this.pcDate = theDate;
        }
    }

    /**
     * Gets the dlaBefore.
     * @return the dlaBefore
     */
    public String getDlaBefore() {
        return dlaBefore;
    }

    /**
     * Sets the dlaBefore.
     * @param dlaBefore the dlaBefore to set
     */
    public void setDlaBefore(String dlaBefore) {
        this.dlaBefore = dlaBefore;
    }

    /**
     * Gets the dlaDate.
     * @return the dlaDate
     */
    public Date getDlaDate() {
        return dlaDate;
    }

    /**
     * Gets the dlaDate.
     * @return the dlaDate as a string
     */
    public String getDlaDateStr() {
        if (dlaDate == null) { return null; }
        return DATE_FMT.format(dlaDate);
    }

    /**
     * Sets the dlaDate.
     * @param dlaDate the dlaDate to set
     */
    public void setDlaDate(Date dlaDate) {
        this.dlaDate = dlaDate;
    }
    /**
     * Sets the dlaDate.
     * @param dateString the date string
     */
    public void setDlaDate(String dateString) {
        Date theDate = ServletDateUtil.getDateFromString(dateString);
        if (theDate != null) {
            this.dlaDate = theDate;
        }
    }
}
