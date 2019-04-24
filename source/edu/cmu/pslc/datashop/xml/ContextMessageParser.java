/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.xml;

/**
 * Interface for the various context message parsers for each version of the
 * context message DTD.
 *
 * @author Hui cheng
 * @version $Revision: 2194 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2006-01-26 13:47:44 -0500 (Thu, 26 Jan 2006) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface ContextMessageParser {
    /**
     * Get ContextMessage object from the XML data.
     * @return a ContextMessage object
     */
    ContextMessage getContextMessage();

    /**
     * The setter of defaultProblemName.
     * @param problem String the default problem name.
     */
    void setDefaultProblemName (String problem);

    /**
     * The setter of defaultDatasetName.
     * @param dataset String the default dataset name.
     */
    void setDefaultDatasetName (String dataset);

    /**
     * The setter of defaultDatasetLevelName.
     * @param datasetLevel String the default dataset level name.
     */
    void setDefaultDatasetLevelName (String datasetLevel);

    /**
     * The setter of defaultDatasetLevelTitle.
     * @param datasetLevelTitle String the default dataset level title.
     */
    void setDefaultDatasetLevelTitle (String datasetLevelTitle);

    /**
     * The getter of defaultProblemName.
     * @return the default problem name.
     */
    String getDefaultProblemName ();

    /**
     * The getter of defaultDatasetName.
     * @return the default dataset name.
     */
    String getDefaultDatasetName ();

    /**
     * The getter of defaultDatasetLevelName.
     * @return the default dataset level name.
     */
    String getDefaultDatasetLevelName ();

    /**
     * The getter of defaultDatasetLevelTitle.
     * @return the default dataset level title.
     */
    String getDefaultDatasetLevelTitle ();

}

