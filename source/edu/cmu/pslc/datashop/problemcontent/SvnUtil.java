/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2014
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.problemcontent;

import java.io.File;
import java.util.Date;

import org.apache.log4j.Logger;

import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNInfo;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

/**
 * Utilities to retrieve SNV information for files.
 *
 * @author Cindy Tipper
 * @version $Revision: 11124 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2014-06-05 13:57:18 -0400 (Thu, 05 Jun 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public final class SvnUtil {

    /** Logger for this class. */
    private static Logger logger = Logger.getLogger(SvnUtil.class);

    /**
     * The constructor.
     */
    private SvnUtil() { }

    /**
     * Determine the 'Last Changed Date' for a file in SVN.
     * @param svnUser SVN user
     * @param svnPassword SVN user password
     * @param theFile a File object
     * @return the Date
     */
    public static Date getContentDate(String svnUser, String svnPassword, File theFile) {

        if ((svnUser == null) || (svnPassword == null)) {
            logger.info("SVN username and password required to determine problem"
                        + " content date for file: " + theFile.getName());
            return null;
        }

        Date result = null;
        try {
            ISVNAuthenticationManager authManager =
                SVNWCUtil.createDefaultAuthenticationManager(svnUser, svnPassword);

            DefaultSVNOptions options = (DefaultSVNOptions)SVNWCUtil.createDefaultOptions(true);
            SVNClientManager clientManager = SVNClientManager.newInstance(options, authManager);

            SVNWCClient wcClient = clientManager.getWCClient();

            SVNInfo svnInfo = wcClient.doInfo(theFile, SVNRevision.WORKING);

            // This is equivalent to "Last Changed Date" retrieved with 'svn info'.
            result = svnInfo.getCommittedDate();
        } catch (Exception e) {
            logger.info("Call to SVN doInfo failed: " + e);
        }

        return result;
    }

    /**
     * Determine the current revision (change number) for a file in SVN.
     * @param svnUser SVN user
     * @param svnPassword SVN user password
     * @param theFile a File object
     * @return the revision
     */
    public static Long getRevision(String svnUser, String svnPassword, File theFile) {

        if ((svnUser == null) || (svnPassword == null)) {
            logger.info("SVN username and password required to determine problem"
                        + " content date for file: " + theFile.getName());
            return null;
        }

        Long result = null;
        try {
            ISVNAuthenticationManager authManager =
                SVNWCUtil.createDefaultAuthenticationManager(svnUser, svnPassword);

            DefaultSVNOptions options = (DefaultSVNOptions)SVNWCUtil.createDefaultOptions(true);
            SVNClientManager clientManager = SVNClientManager.newInstance(options, authManager);

            SVNWCClient wcClient = clientManager.getWCClient();

            SVNInfo svnInfo = wcClient.doInfo(theFile, SVNRevision.WORKING);
            SVNRevision revision = svnInfo.getCommittedRevision();
            
            result = revision.getNumber();
        } catch (Exception e) {
            logger.info("Call to SVN doInfo failed: " + e);
        }

        return result;
    }

    /** Constant for DataShop SVN repository user. */
    private static final String DEFAULT_SVN_USER = "ctipper";

    /** Constant for DataShop SVN repository password. */
    private static final String DEFAULT_SVN_PASSWORD = "spcbhitsot";

    /**
     * Determine the current revision (change number) for a file in SVN.
     * The file is assumed to be in the DataShop SVN repository.
     * @param theFile a File object
     * @return the revision
     */
    public static Long getRevision(File theFile) {

        return getRevision(DEFAULT_SVN_USER, DEFAULT_SVN_PASSWORD, theFile);
    }

} // end class
