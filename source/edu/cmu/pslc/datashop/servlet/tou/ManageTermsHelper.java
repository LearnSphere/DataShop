/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2014
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.tou;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import edu.cmu.pslc.datashop.helper.UserLogger;
import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.ProjectTermsOfUseHistoryDao;
import edu.cmu.pslc.datashop.dao.ProjectTermsOfUseMapDao;
import edu.cmu.pslc.datashop.dao.TermsOfUseVersionDao;

import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.ProjectTermsOfUseHistoryItem;
import edu.cmu.pslc.datashop.item.TermsOfUseItem;
import edu.cmu.pslc.datashop.item.TermsOfUseVersionItem;
import edu.cmu.pslc.datashop.item.UserItem;

import edu.cmu.pslc.datashop.servlet.AbstractServlet;

/**
 * This servlet is for managing the Datashop and project-specific terms of use.
 *
 * @author Alida Skogsholm
 * @version $Revision: 10554 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2014-02-13 13:22:41 -0500 (Thu, 13 Feb 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ManageTermsHelper extends AbstractServlet {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Update the terms of use for each project in a list.
     * @param userItem the user deleting the terms
     * @param projects the list of projects to update
     * @param tou the terms of use to apply to the project, or null to clear the project terms
     */
    public void updateAppliedTerms(UserItem userItem,
                                   List<ProjectItem> projects, TermsOfUseItem tou) {
        Date now = (Date) Calendar.getInstance().getTime();
        TermsOfUseVersionDao touVersionDao = DaoFactory.DEFAULT.getTermsOfUseVersionDao();
        TermsOfUseVersionDao termsOfUseVersionDao = DaoFactory.DEFAULT.getTermsOfUseVersionDao();
        ProjectTermsOfUseMapDao mapDao
            = DaoFactory.DEFAULT.getProjectTermsOfUseMapDao();
        TermsOfUseItem oldTou;

        // The array lists and hash map are used for logging purposes
        ArrayList<String> appliedProjectNames = new ArrayList<String>();
        ArrayList<String> clearedProjectNames;
        HashMap<String, ArrayList<String>> clearedProjectHash
            = new HashMap<String, ArrayList<String>>();

        // Use a project Id list to circumvent lazy initializations and
        // ensure that we only add new project-specific history entries
        // IFF the previous history entries have an expire_date set
        List<Integer> projectIdList = new ArrayList<Integer>();
        for (ProjectItem project : projects) {
            projectIdList.add((Integer) project.getId());
        }

        // Update the project terms of use map item and version status for each project and ToU
        for (ProjectItem project : projects) {
            clearedProjectNames = new ArrayList<String>();  // must be new, cannot be clear

            if (tou != null) {
                logger.info("Updating ProjectTermsOfUseMap for Project Id ("
                        + project.getId() + ") and ToU (" + tou.getName() + ")");
            } else {
                logger.info("Clear ProjectTermsOfUseMap for Project Id ("
                        + project.getId() + ")");

                TermsOfUseItem touItem = mapDao.getTermsOfUseForProject(project);
                if (touItem != null) {
                    String actionString = TermsOfUseVersionItem.ACTION_CLEARED;
                    notifyProjectLeads(project, touItem, null, actionString);
                }
            }

            oldTou = (TermsOfUseItem) mapDao.getTermsOfUseForProject(project);

            // Remove the old project-terms mapping
            // if the old terms of use are different than the new terms
            // or if there are no new terms
            if ((oldTou != null)
                    && (tou == null || !oldTou.getId().equals(tou.getId()))) {

                // Update the project-ToU history item
                TermsOfUseVersionItem oldHeadVersion
                    = touVersionDao.findAppliedVersion(oldTou.getName());
                List<ProjectItem> historyProjects
                    = (List<ProjectItem>) mapDao.getProjectsForTermsOfUse(oldTou);

                // Update expire date in history table for cleared project ToU mappings
                for (ProjectItem historyProject : historyProjects) {
                    if (projectIdList.contains((Integer)historyProject.getId())) {
                        clearedProjectNames.add(historyProject.getProjectName());
                        setHistoryExpireDate(historyProject, oldHeadVersion);

                    }
                }
                // Don't overwrite the clearedProjectNames for the former ToU
                // if it's already been cleared
                if (!clearedProjectHash.containsKey(oldTou.getName())) {
                    clearedProjectHash.put(oldTou.getName(), clearedProjectNames);
                }
                mapDao.remove(project, oldTou);
            }

            // If the old terms are not associated with any other projects,
            // then mark the old head as archived
            if (oldTou != null) {
                TermsOfUseVersionItem oldHeadVersion
                    = touVersionDao.findAppliedVersion(oldTou.getName());
                // Make sure no other projects are using these terms
                List<ProjectItem> otherProjects
                    = (List<ProjectItem>) mapDao.getProjectsForTermsOfUse(oldTou);

                // Update oldHeadVersion
                if (otherProjects != null
                        && otherProjects.size() == 0) {
                    // if oldHeadVersion is not null then there is a previously applied version
                    if (oldHeadVersion != null) {
                        oldHeadVersion.setArchivedDate(now);
                        oldHeadVersion.setStatus(TermsOfUseVersionItem
                                .TERMS_OF_USE_VERSION_STATUS_ARCHIVED);

                        // Save or update the ToU version item
                        termsOfUseVersionDao.saveOrUpdate(oldHeadVersion);
                    }
                }
            }

            // Mark the new head as applied if a terms of use is given
            if (tou != null) {
                TermsOfUseVersionItem headVersion = touVersionDao.findLastVersion(tou.getName());
                if (headVersion != null) {
                    headVersion.setAppliedDate(now);
                    headVersion.setStatus(TermsOfUseVersionItem
                            .TERMS_OF_USE_VERSION_STATUS_APPLIED);
                    termsOfUseVersionDao.saveOrUpdate(headVersion);

                    // Update the project-ToU history item
                    createHistoryEntry(project, headVersion);

                    // Add new project terms map entry
                    if (oldTou == null || !oldTou.getId().equals(tou.getId())) {
                        mapDao.add(project, tou);

                        // Mailer
                        String actionString = TermsOfUseVersionItem.ACTION_APPLIED;
                        appliedProjectNames.add(project.getProjectName());
                        notifyProjectLeads(project, tou, headVersion, actionString);
                    }
                }
            }
        }

        // Log all newly cleared projects by their former terms of use
        if (!clearedProjectHash.isEmpty()) {
            String actionString = TermsOfUseVersionItem.ACTION_CLEARED;
            for (String key : clearedProjectHash.keySet()) {
                ArrayList<String> sortedList = clearedProjectHash.get(key);
                Collections.sort(sortedList);
                UserLogger.log(null, userItem, UserLogger.CLEAR_TERMS,
                        key + " " + actionString + " "
                            + sortedList.toString(), false);
            }
        }
        // Log a single statement with the newly applied ToU and a list of projects
        if (!appliedProjectNames.isEmpty()) {
            String actionString = TermsOfUseVersionItem.ACTION_APPLIED;
            Collections.sort(appliedProjectNames);
            UserLogger.log(null, userItem, UserLogger.APPLY_TERMS,
                    tou.getName() + " " + actionString + " "
                        + appliedProjectNames.toString(), false);
        }

    }   // end of updateAppliedTerms

    /**
     * Creates a new entry for the project-specific terms of use history item
     * @param project The project item
     * @param versionItem The terms of use version item
     */
    public void createHistoryEntry(ProjectItem project, TermsOfUseVersionItem versionItem) {

        Date now = (Date) Calendar.getInstance().getTime();
        ProjectTermsOfUseHistoryDao historyDao
            = DaoFactory.DEFAULT.getProjectTermsOfUseHistoryDao();
        ProjectTermsOfUseHistoryItem historyItem = new ProjectTermsOfUseHistoryItem();

        List<ProjectTermsOfUseHistoryItem> checkHistoryItems
            = (List<ProjectTermsOfUseHistoryItem>) historyDao
                .findByProjectAndVersion(project, versionItem);

        boolean hasExpired = true;
        if (!checkHistoryItems.isEmpty()) {
            for (ProjectTermsOfUseHistoryItem checkExpire : checkHistoryItems) {
                if (checkExpire.getExpireDate() == null) {
                    hasExpired = false;
                }
            }
        }

        if (hasExpired) {
            historyItem.setProject(project);
            historyItem.setTermsOfUseVersion(versionItem);
            historyItem.setEffectiveDate(now);
            historyDao.findOrCreate(historyDao.findAll(), historyItem);

            logger.info("Created new history entry for Project Id (" + project.getId()
                + "), and version Id (" + versionItem.getId() + ")");
        }
     }  // end of createHistoryEntry

    /**
     * Set the expireDate to the current date in the project-specific terms of use history item
     * @param project The project item
     * @param versionItem The terms of use version item
     */
    public void setHistoryExpireDate(ProjectItem project, TermsOfUseVersionItem versionItem) {
        Date now = (Date) Calendar.getInstance().getTime();
        ProjectTermsOfUseHistoryDao historyDao
            = DaoFactory.DEFAULT.getProjectTermsOfUseHistoryDao();
        List<ProjectTermsOfUseHistoryItem> historyItems
            = (List<ProjectTermsOfUseHistoryItem>) historyDao
            .findByProjectAndVersion(project, versionItem);
        for (ProjectTermsOfUseHistoryItem historyItem : historyItems) {
            if (historyItem.getExpireDate() == null) {
                historyItem.setExpireDate(now);
                historyDao.saveOrUpdate(historyItem);
            }
        }

        logger.info("Updating expire date in previous history entry for Project (" + project.getId()
                + "), and version (" + versionItem.getVersion() + ")");

        /* New terms have been cleared so send an e-mail to project PI */
    }   // end of setHistoryExpireDate

    /**
     * Sends an e-mail notification to the Data Provider and Principal Investigator
     * of an arbitrary project, or sends a notification to Datashop help if the
     * e-mail addresses of both users are missing.
     * @param project The project which was affected by the terms of use change
     * @param touItem The terms of use associated with the change
     * @param newVersionItem The new version item (if null, then clear ToU assignment)
     * @param actionString The action string (statically referenced in TermsOfUseVersionItem)
     */
    public void notifyProjectLeads(ProjectItem project, TermsOfUseItem touItem,
                                   TermsOfUseVersionItem newVersionItem, String actionString) {

        String projectName = project.getProjectName();
        if (project.getProjectName() == null) {
            projectName = "No Name";
        }
        UserItem primaryInvestigator = project.getPrimaryInvestigator();
        UserItem dataProvider = project.getDataProvider();
        String subject = "Terms of use change for project \"" + projectName + "\"";
        StringBuffer bodyBuffer = new StringBuffer("");

        bodyBuffer.append("<p>We are notifying you that the terms of use titled <b>");
        bodyBuffer.append(touItem.getName());
        bodyBuffer.append("</b> ");
        bodyBuffer.append("have been ");
        bodyBuffer.append(actionString);
        bodyBuffer.append(" the project <b>");
        bodyBuffer.append(projectName);
        bodyBuffer.append("</b>. ");
        bodyBuffer.append("This change is effective immediately.</p>");
        StringBuffer versionBuffer = new StringBuffer("");

        if (newVersionItem != null
                && (actionString.equals(TermsOfUseVersionItem.ACTION_APPLIED)
                        || actionString.equals(TermsOfUseVersionItem.ACTION_UPDATED))) {

            versionBuffer.append(
                    "<p>The version of the terms applied to this project is now version ");
            versionBuffer.append(newVersionItem.getVersion());
            versionBuffer.append(". ");
            versionBuffer.append("The content of these terms is shown below.</p>");
            versionBuffer.append(newVersionItem.getTerms());
        }

        // Setup a list of e-mail recipients (currently just data provider and PI)
        TreeSet<UserItem> recipients = new TreeSet<UserItem>();
        // Add all recipients
        if (primaryInvestigator != null) {
            recipients.add(primaryInvestigator);
        }
        if (dataProvider != null) {
            recipients.add(dataProvider);
        }

        // Send an e-mail to each unique recipient
        for (Object obj : recipients.toArray()) {
            UserItem recipient = (UserItem) obj;
            if (recipient == null) {
                return;
            }
            StringBuffer messageBuffer = new StringBuffer("");
            String toAddress = "";
            String recipientRole = "";

            messageBuffer.append("<!DOCTYPE html><html lang=\"en\"><head><title>");
            messageBuffer.append("Your DataShop project's terms of use have changed.");
            messageBuffer.append("</title></head><body>");

            if (recipient.getEmail() != null) {
                // Since the DP and PI are the same person, send 1 e-mail
                toAddress = recipient.getEmail();
            } else {
                // No e-mail found for DP or PI, send to Datashop help
                toAddress = getEmailAddressDatashopHelp();
                if (recipient.equals(dataProvider) && recipient.equals(primaryInvestigator)) {
                    recipientRole = "data provider and primary investigator";
                } else if (recipient.equals(primaryInvestigator)) {
                    recipientRole = "primary investigator";
                } else if (recipient.equals(dataProvider)) {
                    recipientRole = "data provider";
                }
                messageBuffer.append("<p>This message cannot be sent to "
                        + recipient.getName() + " "
                        + "who is the " + recipientRole + " "
                        + "of the project because the email address is null or empty.</p>");
            }
            messageBuffer.append("<p>Dear ");
            messageBuffer.append(recipient.getName());
            messageBuffer.append(",</p>");
            messageBuffer.append(bodyBuffer);
            messageBuffer.append(versionBuffer);
            //append closing
            messageBuffer.append("<p>Thanks,<br />");
            messageBuffer.append("The DataShop Team</p>");
            messageBuffer.append("</body></html>");

            sendEmail(getEmailAddressDatashopHelp(),
                      toAddress, subject,
                      messageBuffer.toString());
        }

    } // end of notifyProjectLeads

}   // end of ManageTermsHelper class
