/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.xml;


/**
 * Abstract class for the various context message XML parsers for each version of the
 * tutor message DTD.
 *
 * @author Hui Cheng
 * @version $Revision: 2294 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2006-02-03 20:05:06 -0500 (Fri, 03 Feb 2006) $
 * <!-- $KeyWordsOff: $ -->
 */
public abstract class AbstractContextMessageParser implements ContextMessageParser {

    /** The actual XML data. */
    private String xml;

    /**
     * The default dataset name to use in case course is not sent in XML.
     */
    private String defaultDatasetName = "";

    /**
     * The default dataset level name to use in case dataset level is not sent in XML.
     */
    private String defaultDatasetLevelName = "";

    /**
     * The default dataset level title to use in case dataset level is not sent in XML.
     */
    private String defaultDatasetLevelTitle = "";

    /**
     * The default problem to use in case problem is not sent in XML.
     */
    private String defaultProblemName = "";


    /**
     * Constructor.
     * @param xml the XML data
     */
    protected AbstractContextMessageParser(String xml) {
        this.xml = xml;
    }

    /**
     * Returns the XML data.
     * @return the XML data
     */
    protected String getXml() {
        return this.xml;
    }

    /**
     * The setter of defaultProblemName.
     * @param problem String the default problem name.
     */
    public void setDefaultProblemName (String problem) {
        this.defaultProblemName = problem;
    }

    /**
     * The getter of defaultProblemName.
     * @return String the default problem name.
     */
    public String getDefaultProblemName () {
        return this.defaultProblemName;
    }

    /**
     * The setter of defaultDatasetName.
     * @param dataset String the default dataset name.
     */
    public void setDefaultDatasetName (String dataset) {
        this.defaultDatasetName = dataset;
    }

    /**
     * The getter of defaultDatasetName.
     * @return the default dataset name.
     */
    public String getDefaultDatasetName () {
        return defaultDatasetName;
    }

    /**
     * The setter of defaultDatasetLevelName.
     * @param datasetLevel String the default dataset level name.
     */
    public void setDefaultDatasetLevelName (String datasetLevel) {
        this.defaultDatasetLevelName = datasetLevel;
    }

    /**
     * The getter of defaultDatasetLevelName.
     * @return the default datasetLevel name.
     */
    public String getDefaultDatasetLevelName () {
        return defaultDatasetLevelName;
    }

    /**
     * The setter of defaultDatasetLevelTitle.
     * @param datasetLevelTitle String the default dataset level title.
     */
    public void setDefaultDatasetLevelTitle (String datasetLevelTitle) {
        this.defaultDatasetLevelTitle = datasetLevelTitle;
    }

    /**
     * The getter of defaultDatasetLevelTitle.
     * @return the default datasetLevel title.
     */
    public String getDefaultDatasetLevelTitle () {
        return defaultDatasetLevelTitle;
    }

    /**
     * Get a ContextMessage object from the XML.
     * @return a ContextMessage populated by XML parser.
     */
    public abstract ContextMessage getContextMessage();

}

