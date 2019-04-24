/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2008
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dto;

import java.io.IOException;
import java.util.Date;

/**
 * Conveys all of the relevant data for a specific cached file export.
 * Passed in as a parameter to TransactionExportHelper methods.
 *
 * @author Jim Rankin
 * @version $Revision: 9298 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2013-05-29 12:29:28 -0400 (Wed, 29 May 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class TxExportInfo extends CachedFileInfo {
    /** Export Cache for batch transaction processing. */
    private ExportCache exportCache;

    /** Where to find the transaction export stored procedure template. */
    private String txExportSPFilePath;

    /**
     * Where to find the transaction export stored procedure template.
     * @return Where to find the transaction export stored procedure template
     */
    public String getTxExportSPFilePath() {
        return txExportSPFilePath;
    }
    /**
     * Where to find the transaction export stored procedure template.
     * @param txExportSPFilePath Where to find the transaction export stored procedure template
     */
    public void setTxExportSPFilePath(String txExportSPFilePath) {
        this.txExportSPFilePath = txExportSPFilePath;
    }

    /**
     * Create a TxExportInfo with the appropriate default values.
     * @throws IOException if there is a problem creating the temporary file
     */
    public TxExportInfo() throws IOException {
        super();
    };

    /**
     * Export Cache for batch transaction processing.
     * @return Export Cache for batch transaction processing.
     */
    public ExportCache getExportCache() {
        return exportCache;
    }
    /**
     * Export Cache for batch transaction processing.
     * @param exportCache Export Cache for batch transaction processing.
     */
    public void setExportCache(ExportCache exportCache) {
        this.exportCache = exportCache;
    }
}
