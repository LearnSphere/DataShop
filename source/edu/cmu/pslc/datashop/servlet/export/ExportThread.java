/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.export;

import java.io.File;

/**
 * Interface for any exporter that will use a progress bar.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 4765 $
 * <BR>Last modified by: $Author: bkb $
 * <BR>Last modified on: $Date: 2008-05-01 10:52:19 -0400 (Thu, 01 May 2008) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface ExportThread {

    /**
     * Gets the percent finished for this thread.
     * @return int of the percent remaining or -1 if there was an error.
     */
    int getPercent();

    /** stops, cleans up, and exits the thread. */
    void stop();

    /**
     * Gets the File that was built.
     * @return a File that is the complete export.
     */
    File getResultsFile();

    /** Delete the temporary file. */
    void deleteTempFile();
}
