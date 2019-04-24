/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2010
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.SampleItem;

/**
 * Handles data access for the transaction export.  Maintains a single connection throughout
 * transaction export processing in order to ensure access to temporary tables.
 *
 * @author Jim Rankin
 * @version $Revision: 6047 $
 * <BR>Last modified by: $Author: jrankin $
 * <BR>Last modified on: $Date: 2010-04-23 11:10:27 -0400 (Fri, 23 Apr 2010) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface TxExportDao {
    /**
     * Prepare a call to the SQL stored procedure to generate transaction export data for the
     * given sample and the batch of students specified by limit and offset.
     * We need this so the user has the option to cancel the export.
     * canceled or finishes.
     * @param sample the sample
     * @param limit the maximum number of students to process in this batch
     * @param offset the index of the start of this batch of students
     * @return the prepared call
     * @throws SQLException if something goes wrong, heaven forbid
     */
    CallableStatement prepareTxExportStatement(SampleItem sample, int limit, int offset)
    throws SQLException;

    /**
     * Drop customized tables and procedures for export, and delete the
     * "started transaction export" message.
     * @param sample the sample
     * @param dataset the dataset
     * @throws SQLException if something goes wrong deleting the procedures and tables
     */
    void cleanupExport(SampleItem sample, DatasetItem dataset) throws SQLException;

    /**
     * Call the SQL stored procedure to generate transaction export headers for the given sample.
     * @param sample the sample
     * @throws SQLException if something goes wrong, heaven forbid
     */
    void callTxHeadersSP(SampleItem sample) throws SQLException;

    /**
     * Load the transaction export stored procedure file, customized for this sample.
     * @param filePath path to the transaction export stored procedure file
     * @param sample the sample for which we want to customize the SP file
     * @return whether execution was successful
     * @throws IOException if file at filePath does not exist
     */
    boolean loadTxExportSP(String filePath, SampleItem sample) throws IOException;

    /**
     * Return results of the tx_export table created by the tx_export stored procedure.
     * (ResultSet allows us to page through the results as needed, instead of
     * fetching all the results at once.)
     * @param sample parameterize query with the sample id
     * created for transaction export
     * @param limit maximum number of results to pull from the database at a time.
     * @return ResultSet representing the contents of the tx_export table.
     * @throws SQLException thrown by methods on PreparedStatement
     */
    ResultSet getSPTxs(SampleItem sample, Integer limit) throws SQLException;

    /**
     * Headers to display in the transaction export, taken from the tx_headers table.
     * @param sample parameterize query with the sample id
     * created for transaction export
     * @return headers to display in the transaction export, taken from the tx_headers table.
     */
    Object[] getSPTxHeaders(final SampleItem sample);

    /**
     * Get the number of rows in the tx_export table.
     * @param sample the sample to parameterize on
     * @return the number of rows in the tx_export table
     */
    long getTxExportCount(SampleItem sample);

}
