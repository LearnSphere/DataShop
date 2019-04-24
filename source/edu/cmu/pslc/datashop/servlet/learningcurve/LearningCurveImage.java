/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */

package edu.cmu.pslc.datashop.servlet.learningcurve;

/**
 * DTO for a LearningCurve.
 *
 * @author Cindy Tipper
 * @version $Revision: 10585 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2014-02-18 16:42:17 -0500 (Tue, 18 Feb 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class LearningCurveImage {

    //----- CONSTANTS -----

    /** Constant for the 'Too little data' classification. */
    public static final String CLASSIFIED_TOO_LITTLE_DATA = "Too little data";

    /** Constant for the 'Low and flat' classification. */
    public static final String CLASSIFIED_LOW_AND_FLAT = "Low and flat";

    /** Constant for the 'Still high' classification. */
    public static final String CLASSIFIED_STILL_HIGH = "Still high";

    /** Constant for the 'No Learning' classification. */
    public static final String CLASSIFIED_NO_LEARNING = "No learning";

    /** Constant for the 'Other' classification. */
    public static final String CLASSIFIED_OTHER = "Other";

    /** Constant for the 'Other' classification label. */
    public static final String CLASSIFIED_OTHER_LABEL = "Good";

    /** Constant for cuves not classified. */
    public static final String NOT_CLASSIFIED = "Not classified";

    //----- ATTRIBUTES -----

    /** The filename of the graph image. */
    private String filename;
    /** The class name for the thumbnail. */
    private String thumbClass;
    /** The URL for the anchor. */
    private String anchorURL;
    /** The label for the thumbnail, skill name or student id. */
    private String thumbLabel;
    /** The URL for the image. */
    private String imageURL;
    /** The title for the thumbnail. */
    private String imageTitle;
    /** The classification of this graph. */
    private String classification = NOT_CLASSIFIED;
    /** The last opportunity included in the classification. */
    private Integer lastValidOpportunity;

    //----- CONSTRUCTOR -----

    /**
     * Default constructor.
     */
    public LearningCurveImage() { }

    //----- GETTERS and SETTERS -----

    /**
     * Get the filename of the graph image.
     * @return filename
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Set the filename of the graph image.
     * @param filename the name of the file
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * Get the class name for the thumbnail.
     * @return the thumbClass
     */
    public String getThumbClass() {
        return thumbClass;
    }

    /**
     * Set the class name for the thumbnail.
     * @param thumbClass the thumbClass to set
     */
    public void setThumbClass(String thumbClass) {
        this.thumbClass = thumbClass;
    }

    /**
     * Get the URL for the anchor.
     * @return the anchorURL
     */
    public String getAnchorURL() {
        return anchorURL;
    }

    /**
     * Set the URL for the anchor.
     * @param anchorURL the anchorURL to set
     */
    public void setAnchorURL(String anchorURL) {
        this.anchorURL = anchorURL;
    }

    /**
     * Get the label for the thumbnail.
     * @return the thumbLabel
     */
    public String getThumbLabel() {
        return thumbLabel;
    }

    /**
     * Set the label for the thumbnail.
     * @param thumbLabel the thumbLabel to set
     */
    public void setThumbLabel(String thumbLabel) {
        this.thumbLabel = thumbLabel;
    }

    /**
     * Get the URL for the image.
     * @return the imageURL
     */
    public String getImageURL() {
        return imageURL;
    }

    /**
     * Set the URL for the image.
     * @param imageURL the imageURL to set
     */
    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    /**
     * Get the title for the thumbnail.
     * @return the imageTitle
     */
    public String getImageTitle() {
        return imageTitle;
    }

    /**
     * Set the title for the thumbnail.
     * @param imageTitle the imageTitle to set
     */
    public void setImageTitle(String imageTitle) {
        this.imageTitle = imageTitle;
    }

    /**
     * Get the classification for the thumbnail.
     * @return the classification
     */
    public String getClassification() {
        return classification;
    }

    /**
     * Set the classification for the thumbnail.
     * @param classification the classification to set
     */
    public void setClassification(String classification) {
        this.classification = classification;
    }

    /**
     * Get the last classified opportunity.
     * @return the lastValidOpportunity
     */
    public Integer getLastValidOpportunity() {
        return lastValidOpportunity;
    }

    /**
     * Set the last classified opportunity.
     * @param lastOpp the last opportunity classified
     */
    public void setLastValidOpportunity(Integer lastOpp) {
        this.lastValidOpportunity = lastOpp;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("LearningCurveImage [");
        sb.append("filename = ");
        sb.append(filename);
        sb.append(", thumbClass = ");
        sb.append(thumbClass);
        sb.append(", anchorURL = ");
        sb.append(anchorURL);
        sb.append(", thumbLabel = ");
        sb.append(thumbLabel);
        sb.append(", imageURL = ");
        sb.append(imageURL);
        sb.append(", imageTitle = ");
        sb.append(imageTitle);
        sb.append(", classification = ");
        sb.append(classification);
        sb.append(", lastValidOpportunity = ");
        sb.append(lastValidOpportunity);
        sb.append("]");
        return sb.toString();
    }
}
