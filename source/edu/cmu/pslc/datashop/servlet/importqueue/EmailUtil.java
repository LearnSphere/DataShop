/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.importqueue;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.FastDateFormat;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.FileDao;
import edu.cmu.pslc.datashop.dao.ProjectDao;
import edu.cmu.pslc.datashop.dao.UserDao;

import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.FileItem;
import edu.cmu.pslc.datashop.item.ImportQueueItem;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.UserItem;

import edu.cmu.pslc.datashop.servlet.HelperFactory;

/**
 * Utility class for sending email messages from Import Queue-related servlets.
 *
 * @author Alida Skogsholm
 * @version $Revision: 14325 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2017-10-05 16:04:08 -0400 (Thu, 05 Oct 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public final class EmailUtil {
    /** Utility classes should have a private constructor. */
    private EmailUtil() { }

    /**
     * Utility method to notify a user of a change in the status of their
     * dataset to be loaded into DataShop.
     * @param newStatus the new value of 'status'
     * @param estImportDateStr nicely-format estimated import date
     * @param iqItem the ImportQueueItem
     * @param baseUrl the base DataShop URL
     * @param datashopHelpEmail email address for datashop-help
     * @param includeResults indicates whether to include results in email body
     * @return content of email message to be sent, null if no email
     */
    public static String notifyUserOfStatusChange(String newStatus,
            String estImportDateStr, ImportQueueItem iqItem, String baseUrl,
            String datashopHelpEmail, boolean includeResults) {

        // Only send email for certain status changes...
        if (!newStatus.equals(ImportQueueItem.STATUS_PASSED)
            && !newStatus.equals(ImportQueueItem.STATUS_ERRORS)
            && !newStatus.equals(ImportQueueItem.STATUS_ISSUES)
            && !newStatus.equals(ImportQueueItem.STATUS_LOADED)
            && !newStatus.equals(ImportQueueItem.STATUS_CANCELED)) {
            return null;
        }

        UserDao userDao = DaoFactory.DEFAULT.getUserDao();
        UserItem uploadedBy = userDao.get((String)iqItem.getUploadedBy().getId());
        String toAddress = uploadedBy.getEmail();
        String datasetName = iqItem.getDatasetName();
        FileDao fileDao = DaoFactory.DEFAULT.getFileDao();
        FileItem file = fileDao.get((Integer)iqItem.getFile().getId());
        String fileName = file.getFileName();
        String itemStr = getItemStr(iqItem);

        StringBuffer message = new StringBuffer();

        // If email address not defined, sent notice to datashop-help.
        if ((toAddress == null) || (toAddress.length() == 0)) {
            message.append("<p>This message cannot be sent to "
                           + uploadedBy.getName() + " "
                           + "because their email address is null or empty.</p>");
        }

        message.append(getHeader(uploadedBy));

        if (newStatus.equals(ImportQueueItem.STATUS_PASSED)) {
            message.append("<p>We recently verified the file ");
            message.append(fileName);
            message.append(" for importing into DataShop. It looks like no errors or ");
            message.append("potential issues were found -- that's great news! ");
            if (estImportDateStr == null) {
                message.append("We have not yet estimated when your " + itemStr);
                message.append("will be imported, but we will let you know soon");
            } else {
                message.append("Given the ");
                message.append("number of transactions in the file and the number of items ");
                message.append("ahead of this one in the queue, we estimate that your ");
                message.append(itemStr + " will be imported by ");
                message.append(estImportDateStr);
            }
            message.append(".</p>");
        } else if (newStatus.equals(ImportQueueItem.STATUS_ERRORS)) {
            message.append("<p>Below are the results of the DataShop verification for ");
            message.append("the file ");
            message.append(fileName);
            message.append(". Unfortunately, the verification process found errors that ");
            message.append("will prevent us from importing this " + itemStr + " -- see below. ");
            message.append("If you would still like to import this " + itemStr + ", please ");
            message.append("correct these errors in your file and upload your ");
            message.append(itemStr + " again.");
            message.append("</p>");
            message.append("<p>Verification Results: </p>");
            message.append("<pre>");
            message.append(iqItem.getVerificationResults());
            message.append("</pre>");
        } else if (newStatus.equals(ImportQueueItem.STATUS_ISSUES)) {
            message.append("<p>Below are the results of the DataShop verification for ");
            message.append("the file ");
            message.append(fileName);
            message.append(". While no errors were found, we did find some potential ");
            message.append("issues that we would like you to review before we proceed ");
            message.append("with importing the " + itemStr + ".");
            message.append("</p>");
            message.append("<p>Verification Results: </p>");
            message.append("<pre>");
            message.append(iqItem.getVerificationResults());
            message.append("</pre>");
            message.append("<p>Please visit your ");
            message.append(generateMyImportQueueLink(baseUrl));
            message.append(" in DataShop to let us know how you would like to proceed.");
            message.append("</p>");
        } else if (newStatus.equals(ImportQueueItem.STATUS_LOADED)) {
            message.append("<p>Good news: we just imported the " + itemStr + " \"");
            message.append(datasetName);
            message.append("\" into DataShop! Please ");
            message.append(generateLoginLink(baseUrl, "take a look"));
            message.append(" and let us know if you have any questions.</p>");

            if (includeResults) {
                message.append("<p>Below are the results of the DataShop verification for ");
                message.append("the file ");
                message.append(fileName);
                message.append(". While no errors were found, we did find some potential ");
                message.append("issues.");
                message.append("</p>");
                message.append("<p>Verification Results: </p>");
                message.append("<pre>");
                message.append(iqItem.getVerificationResults());
                message.append("</pre>");
            }

            message.append("<p>Next steps: <br/>");
            message.append("<ul style=\"list-style-type:disc\">");
            message.append("<li>Verify the data is how you'd like it. If it's not, ");
            message.append("feel free to delete this " + itemStr + ", make changes to your ");
            message.append("import file(s), and upload again.</li>");
            message.append("<li>");
            message.append(generateReleaseOrMoveStr(iqItem.getDataset()));
            message.append("once you're happy with it. This allows you to use all of the ");
            message.append("analysis tools, and export or share the data.</li>");
            message.append("<li>Upload any <a ");
            message.append("href=\"" + baseUrl + "/help?page=irb\">relevant ");
            message.append("IRB documentation</a> for the project, which is a prerequisite ");
            message.append("for sharing your data with others.</li>");
            message.append(generateAccessBullet(iqItem.getProject(), baseUrl));
            message.append("</ul>");
            message.append("</p>");
        } else if (newStatus.equals(ImportQueueItem.STATUS_CANCELED)) {
            message.append("<p>We recently canceled the import of the " + itemStr + " \"");
            message.append(datasetName);
            message.append("\". There was likely a problem with the data in this ");
            message.append(itemStr + ". Expect a follow-up email from someone on the ");
            message.append("DataShop team explaining the circumstances.");
            message.append("</p>");
        }
        message.append(getFooter());

        return message.toString();
    }

    /**
     * Utility method to generate email subject when notifying a user of
     * a change in the status of their dataset to be loaded into DataShop.
     * @param newStatus the new value of 'status'
     * @param iqItem the ImportQueueItem
     * @return email subject, null if no email to be sent
     */
    public static String getStatusChangeSubject(String newStatus,
                                                ImportQueueItem iqItem) {

        // Only send email for certain status changes...
        if (!newStatus.equals(ImportQueueItem.STATUS_PASSED)
            && !newStatus.equals(ImportQueueItem.STATUS_ERRORS)
            && !newStatus.equals(ImportQueueItem.STATUS_ISSUES)
            && !newStatus.equals(ImportQueueItem.STATUS_LOADED)
            && !newStatus.equals(ImportQueueItem.STATUS_CANCELED)) {
            return null;
        }

        String datasetName = iqItem.getDatasetName();
        String itemStr = getItemStr(iqItem);

        String subject = "Verification results for new " + itemStr + " \"" + datasetName + "\"";

        if (newStatus.equals(ImportQueueItem.STATUS_LOADED)) {
            subject = "Your new " + itemStr + " \"" + datasetName + "\" has been imported";
        } else if (newStatus.equals(ImportQueueItem.STATUS_CANCELED)) {
            subject = "Import canceled for " + itemStr + " \"" + datasetName + "\"";
        }

        return subject;
    }

    /**
     * Helper method to generate a link to a user's "My Datasets" page.
     * @param baseUrl the base DataShop URL
     * @return the link
     */
    private static String generateMyImportQueueLink(String baseUrl) {

        StringBuffer sb = new StringBuffer();
        sb.append("<a href=\"");
        sb.append(baseUrl);
        sb.append("/index.jsp?datasets=mine\">");
        sb.append("Import Queue");
        sb.append("</a>");
        return sb.toString();
    }
    /**
     * Helper method to generate a link to the login page.
     * @param baseUrl the base DataShop URL
     * @param displayText text to display in link
     * @return the link
     */
    private static String generateLoginLink(String baseUrl, String displayText) {

        StringBuffer sb = new StringBuffer();
        sb.append("<a href=\"");
        sb.append(baseUrl);
        sb.append("/login?Submit=Log+in");
        sb.append("\">");
        sb.append(displayText);
        sb.append("</a>");
        return sb.toString();
    }

    /**
     * Helper method to generate text based on dataset's status.
     * @param dsItem the DatasetItem
     * @return the link
     */
    private static String generateReleaseOrMoveStr(DatasetItem dsItem) {
        // This is only called for non-released datasets.
        // If dsItem is null, it's a Discourse.
        String result = "Release this dataset ";
        if (dsItem == null) {
            // Discourses can only be uploaded to DiscourseDB project,
            // so 'release' is the next step.
            result = "Release this discourse ";
        } else if (dsItem.getProject() == null) {
            result = "Move this dataset to a project ";
        }
        return result;
    }

    /**
     * Helper method to generate text for granting access to a given dataset.
     * If the dataset is not yet in a project, return an empty string.
     * @param project the ProjectItem
     * @param baseUrl the base DataShop URL
     * @return the link
     */
    private static String generateAccessBullet(ProjectItem project, String baseUrl) {

        if (project == null) {
            return "";
        }

        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        project = projectDao.get((Integer)project.getId());

        Boolean isDiscourse = project.getIsDiscourseDataset();

        StringBuffer sb = new StringBuffer();
        sb.append("<li>Give other people ");
        sb.append(generateProjectPermissionsLink(project, baseUrl));
        sb.append(" (and all other ");
        sb.append(((isDiscourse != null) && isDiscourse) ? "discourses" : "datasets");
        sb.append(" in the project).");
        sb.append("</li>");
        return sb.toString();
    }

    /**
     * Helper method to get ProjectItem for a given DatasetItem. If null,
     * return the DiscourseDB project.
     * @param dsItem the DatasetItem
     * @return the ProjectItem
     */
    /*
    private static ProjectItem getProject(DatasetItem dsItem) {
        // If dsItem is null, it's a Discourse.
        if (dsItem == null) {
            // return discourse project
            ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
            return projectDao.findDiscourseDbProject();
        } else {
            return dsItem.getProject();
        }
    }
    */

    /**
     * Helper method to generate a link to a specific Project permissions page.
     * @param project the ProjectItem
     * @param baseUrl the base DataShop URL
     * @return the link
     */
    private static String generateProjectPermissionsLink(ProjectItem project, String baseUrl) {

        StringBuffer sb = new StringBuffer();
        sb.append("<a href=\"");
        sb.append(baseUrl);
        sb.append("/ProjectPermissions?id=");
        sb.append((Integer)project.getId());
        sb.append("\">");
        sb.append("permission to access this dataset");
        sb.append("</a>");
        return sb.toString();
    }

    /**
     * Utility method to notify a user of a change in the date by which their
     * dataset will be available in DataShop.
     * @param origEstImportDate the original estimated import date
     * @param status the new value of 'status'
     * @param estImportDateStr nicely-format estimated import date
     * @param iqItem the ImportQueueItem
     * @param datashopHelpEmail email address for datashop-help
     * @return content of email message to be sent, null if no email
     */
    public static String notifyUserOfStatusChange(Date origEstImportDate,
                                                   String status,
                                                   String estImportDateStr,
                                                   ImportQueueItem iqItem,
                                                   String datashopHelpEmail) {

        // To avoid multiple emails, for certain status changes, skip this email.
        if (!status.equals(iqItem.getStatus())
            && (status.equals(ImportQueueItem.STATUS_PASSED)
                || status.equals(ImportQueueItem.STATUS_ISSUES))) {
            return null;
        }

        UserDao userDao = DaoFactory.DEFAULT.getUserDao();
        UserItem uploadedBy = userDao.get((String)iqItem.getUploadedBy().getId());
        String toAddress = uploadedBy.getEmail();
        FileDao fileDao = DaoFactory.DEFAULT.getFileDao();
        FileItem file = fileDao.get((Integer)iqItem.getFile().getId());
        String fileName = file.getFileName();
        String formattedDate = estImportDateStr;
        String itemStr = getItemStr(iqItem);

        StringBuffer message = new StringBuffer();

        // If email address not defined, sent notice to datashop-help.
        if ((toAddress == null) || (toAddress.length() == 0)) {
            message.append("<p>This message cannot be sent to "
                           + uploadedBy.getName() + " "
                           + "because their email address is null or empty.</p>");
        }

        message.append(getHeader(uploadedBy));

        // Email sent with initial setting of 'daysToLoad' is different
        // if the status of the upload item is already PASSED.
        if ((origEstImportDate == null)
             && (!iqItem.getStatus().equals(ImportQueueItem.STATUS_PASSED))) {
            message.append("<p>Thank you for uploading the file ");
            message.append(fileName);
            message.append(" to be imported into DataShop. The next step is for your ");
            message.append("file to be verified according to the DataShop format. We'll ");
            message.append("send another email with the results of the verification soon. ");
            message.append("Assuming the verification finds no errors, we estimate that ");
            message.append("your " + itemStr + " will be imported by ");
            message.append(formattedDate);
            message.append(".</p>");
        } else {
            String theVerb = (origEstImportDate == null) ? "set" : "changed";

            message.append("<p>We recently ");
            message.append(theVerb);
            message.append(" the estimated import date of your file ");
            message.append(fileName);
            message.append(" to ");
            message.append(formattedDate);
            message.append(". We appreciate your patience during this process. If you ");
            message.append("have any questions, please don't hesitate to write us.");
            message.append("</p>");
        }
        message.append(getFooter());

        return message.toString();
    }

    /**
     * Utility method to generate email subject when notifying a user of a
     * change in the date by which their dataset will be available in DataShop.
     * @param origEstImportDate the original value of estImportDate
     * @param status the new value of 'status'
     * @param iqItem the ImportQueueItem
     * @return content of email message to be sent, null if no email
     */
    public static String getStatusChangeSubject(Date origEstImportDate,
                                                String status,
                                                ImportQueueItem iqItem) {

        // To avoid multiple emails, for certain status changes, skip this email.
        if (!status.equals(iqItem.getStatus())
            && (status.equals(ImportQueueItem.STATUS_PASSED)
                || status.equals(ImportQueueItem.STATUS_ISSUES))) {
            return null;
        }

        String datasetName = iqItem.getDatasetName();
        String itemStr = getItemStr(iqItem);

        String subject;

        // Email sent with initial setting of 'daysToLoad' is different
        // if the status of the upload item is already PASSED.
        if ((origEstImportDate == null)
            && (!iqItem.getStatus().equals(ImportQueueItem.STATUS_PASSED))) {

            subject = "Update for new " + itemStr + " \"" + datasetName + "\"";

        } else {
            subject = "Update for " + itemStr + " \"" + datasetName + "\"";
        }

        return subject;
    }

    /**
     * Helper method for generating email header for a given user.
     * @param user the UserItem
     * @return the header String
     */
    private static String getHeader(UserItem user) {
        StringBuffer sb = new StringBuffer();
        sb.append("<!DOCTYPE html><html lang=\"en\"><body>");
        sb.append("<p>Dear ");
        sb.append(user.getName());
        sb.append(",</p>");
        return sb.toString();
    }

    /**
     * Helper method for generating email footer.
     * @return the footer String
     */
    private static String getFooter() {
        StringBuffer sb = new StringBuffer();
        sb.append("<p>Thanks,<br/>");
        sb.append("The DataShop Team</p>");
        sb.append("</body></html>");
        return sb.toString();
    }

    /** Constant for the format of email dates. */
    private static final FastDateFormat DATE_FMT =
            FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");

    /**
     * Utility method to send email to datashop-help when a dataset is
     * added to the import queue.
     * @param importQueueItem the ImportQueueItem
     * @param baseUrl the DataShop url
     * @param projectSelection indicated 'new' or 'existing' project
     * @return content of email message to be sent
     */
    public static String notifyDatashopHelp(ImportQueueItem importQueueItem,
                                            String baseUrl,
                                            String projectSelection) {

        String datasetName = importQueueItem.getDatasetName();
        UserItem uploadedBy = importQueueItem.getUploadedBy();
        Boolean anonFlag = importQueueItem.getAnonFlag();
        String isAnonymized = (anonFlag != null && anonFlag) ? "yes" : "no";
        String itemStr = getItemStr(importQueueItem);

        UserDao userDao = DaoFactory.DEFAULT.getUserDao();
        uploadedBy = userDao.get((String)uploadedBy.getId());

        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        ProjectItem projectItem = null;
        if (importQueueItem.getProject() != null) {
            projectItem = projectDao.get((Integer)importQueueItem.getProject().getId());
        }

        FileDao fileDao = DaoFactory.DEFAULT.getFileDao();
        FileItem fileItem = null;
        String displayFileName = null;
        if (importQueueItem.getFile() != null) {
            fileItem = fileDao.get((Integer)importQueueItem.getFile().getId());
            displayFileName = fileItem.getDisplayFileName();
        }

        StringBuffer message = new StringBuffer();
        message.append("<!DOCTYPE html><html lang=\"en\"><body>");
        message.append("<p>A new " + itemStr + " was added to the ");
        message.append("<a href=\"");
        message.append(baseUrl);
        message.append("/ImportQueue\">import queue</a>:<br /></p>");
        message.append("<table style=\"empty-cells:show\" border=\"1\" ");
        message.append("cellpadding=\"6\" cellspacing=\"0\">");

        message.append("<tr><td>Date</td><td>");
        message.append(DATE_FMT.format(importQueueItem.getUploadedTime()));
        message.append("</td></tr>");
        message.append("<tr><td>User</td><td>");
        message.append(uploadedBy.getName());
        message.append(getEmailLink(uploadedBy.getEmail()));
        message.append("</td></tr>");
        message.append("<tr><td>Project</td><td>");
        message.append(getProjectLink(projectItem, baseUrl));
        message.append(getProjectSelectionStr(projectSelection));
        message.append("</td></tr>");
        message.append("<tr><td>" + StringUtils.capitalize(itemStr) + "</td><td>");
        message.append(datasetName);
        message.append("</td></tr>");
        message.append("<tr><td>Description</td><td>");
        message.append(StringUtils.defaultString(importQueueItem.getDescription()));
        message.append("</td></tr>");
        message.append("<tr><td>Filename</td><td>");
        if (fileItem != null) {
            message.append(StringUtils.defaultString(displayFileName));
        } else {
            message.append("");
        }
        message.append("</td></tr>");
        message.append("<tr><td>Format</td><td>");
        message.append(getFormatStr(importQueueItem.getFormat()));
        message.append("</td></tr>");
        message.append("<tr><td>Anonymized</td><td>");
        message.append(isAnonymized);
        message.append("</td></tr>");

        message.append("</table>");
        message.append("</body></html>");

        return message.toString();
    }

    /**
     * Utility method to generate email link given an email address.
     * @param email the user's email address
     * @return String specifying email link
     */
    private static String getEmailLink(String email) {
        if (email == null) { return ""; }

        StringBuffer sb = new StringBuffer();
        sb.append(" (<a href=\"mailto:");
        sb.append(email);
        sb.append("\">");
        sb.append(email);
        sb.append("</a>)");
        return sb.toString();
    }

    /**
     * Utility method to generate link given a ProjectItem.
     * @param project the ProjectItem
     * @param baseUrl the base DataShop URL
     * @return String specifying the link
     */
    private static String getProjectLink(ProjectItem project, String baseUrl) {
        if (project == null) { return ""; }

        StringBuffer sb = new StringBuffer();
        sb.append("<a href=\"");
        sb.append(baseUrl);
        sb.append("/Project?id=");
        sb.append((Integer)project.getId());
        sb.append("\">");
        sb.append(project.getProjectName());
        sb.append("</a>");
        return sb.toString();
    }

    /**
     * Utility method to get project selection string.
     * @param projectSelection the input
     * @return nicely formatted output for the email
     */
    private static String getProjectSelectionStr(String projectSelection) {
        if (projectSelection == null) { return ""; }

        if (projectSelection.equals(UploadDatasetDto.PROJ_EXIST)) {
            return " (existing)";
        } else if (projectSelection.equals(UploadDatasetDto.PROJ_NEW)) {
            return " (new)";
        } else if (projectSelection.equals(UploadDatasetDto.PROJ_LATER)) {
            return "(choose later)";
        } else {
            return "";
        }

    }

    /**
     * Utility method to get format string.
     * @param format the input
     * @return nicely formatted output for the email
     */
    private static String getFormatStr(String format) {
        if (format == null) { return ""; }
        if (format.equals(ImportQueueItem.FORMAT_TAB)) {
            return "tab-delimited";
        } else if (format.equals(ImportQueueItem.FORMAT_XML)) {
            return "xml";
        } else if (format.equals(ImportQueueItem.FORMAT_DISCOURSE)) {
            return "discourse db";
        } else {
            return format;
        }
    }

    /**
     * Helper method to generate item label given ImportQueueItem.
     * @param importQueueItem the ImportQueue item
     * @return the label
     */
    private static String getItemStr(ImportQueueItem importQueueItem) {
        String result = "dataset";

        if (importQueueItem != null) {
            ImportQueueHelper iqHelper = HelperFactory.DEFAULT.getImportQueueHelper();
            if (iqHelper.isDiscourse(importQueueItem)) { result = "discourse"; }
        }
        
        return result;
    }
}
