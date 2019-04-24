 /* Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2012
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.extractors;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.AccessRequestStatusDao;
import edu.cmu.pslc.datashop.dao.AccessRequestHistoryDao;
import edu.cmu.pslc.datashop.dao.ProjectDao;
import edu.cmu.pslc.datashop.dao.UserDao;
import edu.cmu.pslc.datashop.item.AccessRequestHistoryItem;
import edu.cmu.pslc.datashop.item.AccessRequestStatusItem;
import edu.cmu.pslc.datashop.item.AuthorizationItem;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.accessrequest.AccessRequestUtil;
import edu.cmu.pslc.datashop.util.DataShopInstance;
import edu.cmu.pslc.datashop.util.ServerNameUtils;

/**
 * This class is used to gather pending requests and provide a summary
 * report to DataShop Help if no arguments are passed in at run-time.
 * Alternatively, a destination e-mail address may be passed in at run-time.
 *
 * @author Mike Komisin
 * @version $Revision: 12959 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-02-24 16:32:07 -0500 (Wed, 24 Feb 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class AccessRequestsPending extends AbstractExtractor {

    /** A line break. */
    private static final String BREAK = "<br>";

    /** Logger. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** Format for the request date. */
    private static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("MMMMM dd, yyyy");

    /**
     * The main method that executes this extractor.
     * @param args an e-mail address may be passed in at run-time
     */
    public static void main(String[] args) {
        Logger logger = Logger.getLogger("AccessRequestsPending.main");
        logger.info("AccessRequestsPending starting...");

        // Initialize DataShop instance from the database
        DataShopInstance.initialize();

        try {
            AccessRequestsPending accessRequestsPending = new AccessRequestsPending();

            // Handle the command line options
            accessRequestsPending.handleOptions(args);

            // Send the report if pending requests older than 1 week exist
            String message = accessRequestsPending.buildPendingReport();
            if (message != null && !message.isEmpty()) {
                accessRequestsPending.sendEmail(
                          accessRequestsPending.getEmailAddress(), // from
                          accessRequestsPending.getEmailAddress(), // to
                          "Pending Access Requests",               // subject
                          message);                                // message
            } else {
                logger.info("Nothing pending, no email sent.");
            }

            // If necessary, send reminder email to PI, DP and/or user.
            accessRequestsPending.sendFollowUpEmails();

        } catch (Throwable throwable) {
            logger.error("Unknown error in main method.", throwable);

        } finally {
            logger.info("AccessRequestsPending done.");
        }
    }

    /**
     * Default constructor.
     */
    public AccessRequestsPending() {
        // Intentionally blank
    }

    /**
     * Returns a summary of the pending requests or an empty string if no pending requests exist.
     * @return a summary of the pending requests or an empty string if no pending requests exist
     */
    public String buildPendingReport() {

        // The string buffer
        StringBuffer sbuffer = new StringBuffer("");
        // AR Status Dao
        AccessRequestStatusDao arStatusDao = DaoFactory.DEFAULT.getAccessRequestStatusDao();

        // Two new lists to store the votes
        List<AccessRequestStatusItem> monthOldRequests =
                new ArrayList<AccessRequestStatusItem>();
        List<AccessRequestStatusItem> weekOldRequests =
                new ArrayList<AccessRequestStatusItem>();

        // Get all pending votes
        List<AccessRequestStatusItem> pendingRequests =
                        (List<AccessRequestStatusItem>)arStatusDao.findAllPending();

        Calendar cal = Calendar.getInstance();
        Date now = cal.getTime();

        // Get the date object for last week
        cal.add(Calendar.DATE, AccessRequestStatusItem.ONE_WEEK_AGO);
        Date lastWeek = (Date) cal.getTime();

        // Reset calendar
        cal.setTime(now);

        // Get the date object for one month ago
        cal.add(Calendar.DATE, AccessRequestStatusItem.ONE_MONTH_AGO);
        Date lastMonth = (Date) cal.getTime();

        // Add pending votes GTE a month old to a new list
        for (AccessRequestStatusItem request : pendingRequests) {
            if (!request.getLastActivityDate().after(lastMonth)) {
                monthOldRequests.add(request);
            }
        }

        // Add pending votes GTE a week old and LTE a month old to a new list
        for (AccessRequestStatusItem request : pendingRequests) {
            if (!request.getLastActivityDate().after(lastWeek)
                    && request.getLastActivityDate().after(lastMonth)) {
                weekOldRequests.add(request);
            }
        }

        if (!weekOldRequests.isEmpty() || !monthOldRequests.isEmpty()) {
            // Week old requests table
            sbuffer.append("<h4>Week Old Requests (");
            sbuffer.append(weekOldRequests.size());
            sbuffer.append(")</h4>");
            sbuffer.append(newRequestTable(weekOldRequests));

            // Month old requests table
            sbuffer.append("<h4>Month Old Requests (");
            sbuffer.append(monthOldRequests.size());
            sbuffer.append(")</h4>");
            sbuffer.append(newRequestTable(monthOldRequests));

            sbuffer.append(BREAK);
        }

        return sbuffer.toString();
    }

    /**
     * Returns a table of requests with user Id, status, and last activity date.
     * @param requests the AccessRequestStatusItem list
     * @return a table of requests with user Id, status, and last activity date
     */
    public String newRequestTable(List<AccessRequestStatusItem> requests) {
        // StringBuffer to store html table
        StringBuffer sbuffer = new StringBuffer("");
        // Build the table
        if (requests != null && !requests.isEmpty()) {
            sbuffer.append("<table border=\"1\" cellpadding=\"6\" cellspacing=\"0\">");

            sbuffer.append("<th>User</th>");
            sbuffer.append("<th>Project/PI/DP");
            sbuffer.append("<th>Status</th>");
            sbuffer.append("<th>Last Activity</th>");

            for (AccessRequestStatusItem request : requests) {
                ProjectItem project = request.getProject();
                ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
                project = projectDao.get((Integer)project.getId());

                sbuffer.append("<tr><td>");

                // The user full name (user-id)
                UserItem user = request.getUser();
                UserDao userDao = DaoFactory.DEFAULT.getUserDao();
                user = userDao.get((String)user.getId());
                sbuffer.append(user.getName());
                if (user.getEmail() != null) {
                    sbuffer.append(" (<a href=\"mailto:"
                            + user.getEmail()
                            + "?cc=datashop-help@lists.andrew.cmu.edu"
                            + "&subject=Access request for project &quot;"
                            + project.getProjectName() + "&quot;\">"
                            + user.getId() + "</a>)");
                } else {
                    sbuffer.append(" (" + user.getId() + ")");
                }
                sbuffer.append("</td><td>");

                // The project name, the PI full name (user-id), the DP full name (user-id)
                sbuffer.append("<em>" + project.getProjectName() + "</em>");
                sbuffer.append("<br />");
                if (project.getPrimaryInvestigator() != null) {
                    sbuffer.append("PI: ");
                    sbuffer.append(project.getPrimaryInvestigator().getName());
                    if (project.getPrimaryInvestigator().getEmail() != null) {
                        sbuffer.append(" (<a href=\"mailto:"
                                + project.getPrimaryInvestigator().getEmail()
                                + "?cc=datashop-help@lists.andrew.cmu.edu"
                                + "&subject=Access request for project &quot;"
                                + project.getProjectName() + "&quot;\">"
                                + project.getPrimaryInvestigator().getId() + "</a>)");
                    } else {
                        sbuffer.append(" (" + project.getPrimaryInvestigator().getId() + ")");
                    }
                }
                if (project.getDataProvider() != null) {
                    sbuffer.append("<br />");
                    sbuffer.append("DP: ");
                    sbuffer.append(project.getDataProvider().getName());
                    if (project.getDataProvider().getEmail() != null) {
                        sbuffer.append(" (<a href=\"mailto:"
                                     + project.getDataProvider().getEmail()
                                     + "?cc=datashop-help@lists.andrew.cmu.edu"
                                     + "&subject=Access request for project &quot;"
                                     + project.getProjectName() + "&quot;\">"
                                     + project.getDataProvider().getId() + "</a>)");
                    } else {
                        sbuffer.append(" (" + project.getDataProvider().getId() + ")");
                    }
                }
                sbuffer.append("</td><td>");
                // The status
                sbuffer.append(request.getStatus());
                sbuffer.append("</td><td>");
                // The last activity date
                sbuffer.append(request.getLastActivityDate());
                sbuffer.append("</td></tr>");
            }
            sbuffer.append("</table>");
        }
        return sbuffer.toString();
    }

    /**
     * Handle the command line arguments.
     * @param args command line arguments passed into main
     */
    protected void handleOptions(String[] args) {
        if (args == null || args.length == 0) {
            return;
        }

        java.util.ArrayList argsList = new java.util.ArrayList();
        for (int i = 0; i < args.length; i++) {
            argsList.add(args[i]);
        }

        // loop through the arguments
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-h") || args[i].equals("-help")) {
                displayUsage();
                System.exit(0);
            } else if (args[i].equals("-e") || args[i].equals("-email")) {
                setSendEmailFlag(true);
                if (++i < args.length) {
                    setEmailAddress(args[i]);
                } else {
                    System.err.println(
                        "Error: a email address must be specified with this argument");
                    displayUsage();
                    System.exit(1);
                }
            } else {
                System.err.println("Error: improper command line arguments: " + argsList);
                displayUsage();
                System.exit(1);
            } // end if then else
        } // end for loop

    } // end handleOptions

    /**
     * Display the usage of this utility.
     */
    protected void displayUsage() {
        System.err.println("\nUSAGE: java -classpath ..."
            + " AccessRequestsPending [-email address]");
        System.err.println("Option descriptions:");
        System.err.println("\t-e, email \t email address for report");
        System.err.println("\t-h, help  \t display this help message ");
    }

    /** Constant for two weeks ago. */
    private static final int TWO_WEEKS_AGO = 2 * AccessRequestStatusItem.ONE_WEEK_AGO;

    /** Constant for three weeks ago. */
    private static final int THREE_WEEKS_AGO = 3 * AccessRequestStatusItem.ONE_WEEK_AGO;

    /** Constant for four weeks ago. */
    private static final int FOUR_WEEKS_AGO = 4 * AccessRequestStatusItem.ONE_WEEK_AGO;

    /**
     * Determine if age of pending requests means we should send follow-up
     * email to PI/DP and/or user.
     */
    private void sendFollowUpEmails() {

        Calendar cal = Calendar.getInstance();
        // We are only interested in the date, not hours, minutes, etc.
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.AM_PM, Calendar.AM);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        Date now = cal.getTime();

        // Get the date object for last week
        cal.add(Calendar.DATE, AccessRequestStatusItem.ONE_WEEK_AGO);
        Date oneWeekAgo = (Date) cal.getTime();

        // reset calendar
        cal.setTime(now);

        cal.add(Calendar.DATE, TWO_WEEKS_AGO);
        Date twoWeeksAgo = (Date) cal.getTime();

        cal.setTime(now);

        cal.add(Calendar.DATE, THREE_WEEKS_AGO);
        Date threeWeeksAgo = (Date) cal.getTime();

        cal.setTime(now);

        cal.add(Calendar.DATE, FOUR_WEEKS_AGO);
        Date fourWeeksAgo = (Date) cal.getTime();

        // Map of pending requests by user.
        Map<UserItem, List<AccessRequestStatusItem>> oneAndTwoWeekMap =
            new HashMap<UserItem, List<AccessRequestStatusItem>>();

        AccessRequestStatusDao arStatusDao = DaoFactory.DEFAULT.getAccessRequestStatusDao();

        // Week-old requests
        List<AccessRequestStatusItem> weekOldRequests =
            (List<AccessRequestStatusItem>)arStatusDao.findAllPendingByDateRange(oneWeekAgo,
                                                                                 twoWeeksAgo);
        for (AccessRequestStatusItem item : weekOldRequests) {
            // Ensure we don't send duplicates...
            if (item.getEmailStatus().equals(AccessRequestStatusItem.EMAIL_STATUS_NONE)) {
                addItemToPiDpMap(item, oneAndTwoWeekMap);
            }
        }

        // Two week-old requests
        List<AccessRequestStatusItem> twoWeekOldRequests =
            (List<AccessRequestStatusItem>)arStatusDao.findAllPendingByDateRange(twoWeeksAgo,
                                                                                 threeWeeksAgo);
        for (AccessRequestStatusItem item : twoWeekOldRequests) {
            // Ensure we don't send duplicates...
            if (item.getEmailStatus().equals(AccessRequestStatusItem.EMAIL_STATUS_FIRST_SENT)) {
                addItemToPiDpMap(item, oneAndTwoWeekMap);
            }
        }

        // Map of three-week-old pending requests by user.
        Map<UserItem, List<AccessRequestStatusItem>> threeWeekMap =
            new HashMap<UserItem, List<AccessRequestStatusItem>>();

        List<AccessRequestStatusItem> threeWeekOldRequests =
            (List<AccessRequestStatusItem>)arStatusDao.findAllPendingByDateRange(threeWeeksAgo,
                                                                                 fourWeeksAgo);
        for (AccessRequestStatusItem item : threeWeekOldRequests) {
            // Ensure we don't send duplicates...
            if (item.getEmailStatus().equals(AccessRequestStatusItem.EMAIL_STATUS_SECOND_SENT)) {
                addItemToUserMap(item.getUser(), item, threeWeekMap);
            }
        }

        // Pending requests older than four weeks.
        List<AccessRequestStatusItem> fourWeekOldRequests =
            (List<AccessRequestStatusItem>)arStatusDao.findAllPendingOlderThan(fourWeeksAgo);

        // One- and two-week old requests are handled the same.
        for (UserItem item : oneAndTwoWeekMap.keySet()) {
            List<AccessRequestStatusItem> theList = oneAndTwoWeekMap.get(item);
            sendReminderEmail(item, theList);
        }

        // Three week-old requests... handled a bit differently.
        for (UserItem item : threeWeekMap.keySet()) {
            List<AccessRequestStatusItem> theList = threeWeekMap.get(item);
            sendUserEmails(item, theList);
        }

        // Requests older than four weeks are denied.
        for (AccessRequestStatusItem item : fourWeekOldRequests) {
            denyRequest(item);
        }
    }

    /**
     * Utility to add pending request to PI/DP-specific list.
     * @param item the pending access request item
     * @param userMap a map of PI/DP user items to a list of pending requests
     */
    private void addItemToPiDpMap(AccessRequestStatusItem item,
                                  Map<UserItem, List<AccessRequestStatusItem>> userMap) {

        ProjectItem project = item.getProject();
        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        project = projectDao.get((Integer)project.getId());

        String status = item.getStatus();
        // At this point, status has to be one of the following:
        // not_reviewed, pi_approved or dp_approved
        boolean hasPiVoted =
            (status.equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_PI_APPROVED))
            ? true : false;
        boolean hasDpVoted =
            (status.equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DP_APPROVED))
            ? true : false;

        if ((project.getPrimaryInvestigator() != null) && !hasPiVoted) {
            UserItem pi = project.getPrimaryInvestigator();
            addItemToUserMap(pi, item, userMap);
        }
        if ((project.getDataProvider() != null) && !hasDpVoted) {
            UserItem dp = project.getDataProvider();
            // Don't send duplicates if PI = DP...
            String ownership = project.getOwnership();
            if (!ownership.equals(ProjectItem.PROJECT_OWNERSHIP_PI_EQ_DP)) {
                addItemToUserMap(dp, item, userMap);
            }
        }
    }

    /**
     * Utility to add pending request to user-specific list.
     * @param user the UserItem to add to the map
     * @param item the pending access request item
     * @param userMap a map of users to a list of pending requests
     */
    private void addItemToUserMap(UserItem user, AccessRequestStatusItem item,
                                  Map<UserItem, List<AccessRequestStatusItem>> userMap) {

        UserDao userDao = DaoFactory.DEFAULT.getUserDao();
        user = userDao.get((String)user.getId());

        List<AccessRequestStatusItem> itemList =
            (List<AccessRequestStatusItem>)userMap.get(user);
        if (itemList == null) {
            itemList = new ArrayList<AccessRequestStatusItem>();
        }
        itemList.add(item);
        userMap.put(user, itemList);
    }

    /**
     * Send reminder email to PI/DP for one- and two-week-old pending requests.
     * @param piDpItem the PI/DP of the project
     * @param requests the pending requests
     */
    private void sendReminderEmail(UserItem piDpItem, List<AccessRequestStatusItem> requests) {

        boolean isPlural = (requests.size() > 1) ? true : false;

        String subject = "Pending requests for access to your DataShop datasets";

        StringBuffer message = new StringBuffer();
        message.append("<p>Dear ");
        message.append(piDpItem.getName());
        message.append(",</p>");
        message.append("<p>");
        message.append("Just a quick reminder that you have ");
        message.append(requests.size());
        message.append(" unanswered request");
        if (isPlural) { message.append("s"); }
        message.append(" for access to your DataShop dataset");
        if (isPlural) { message.append("s"); }
        message.append(". You can respond to ");
        if (isPlural) {
            message.append("these requests");
        } else {
            message.append("this request");
        }
        message.append(" on the ");
        message.append("<a href=\"");
        message.append(getDatashopUrl());
        message.append("/AccessRequests");
        message.append("\">");
        message.append("Access Requests");
        message.append("</a>");
        message.append(" page.");
        message.append("</p>");

        message.append(pendingRequestsTable(requests));

        message.append("<p>Thanks,<br />");
        message.append("The DataShop Team</p>");

        String emailAddr = piDpItem.getEmail();
        if ((emailAddr != null) && (emailAddr.length() > 0)) {

            sendEmail(getEmailAddress(),   // from ds-help
                      emailAddr,           // to
                      subject,
                      message.toString());

            for (AccessRequestStatusItem request : requests) {
                String emailStatus = request.getEmailStatus();
                String newStatus = AccessRequestStatusItem.EMAIL_STATUS_FIRST_SENT;
                if (emailStatus.equals(AccessRequestStatusItem.EMAIL_STATUS_FIRST_SENT)) {
                    newStatus = AccessRequestStatusItem.EMAIL_STATUS_SECOND_SENT;
                }
                updateEmailStatus(request, newStatus);
            }

        } else {
            updateEmailStatus(requests, AccessRequestStatusItem.EMAIL_STATUS_UNABLE_TO_SEND);
            failureToSendEmail(piDpItem.getName(), subject, message);
        }
    }

    /**
     * Utility to update the email_status of a list of pending requests.
     * @param requests list of pending access requests
     * @param status the new email status
     */
    private void updateEmailStatus(List<AccessRequestStatusItem> requests, String status) {

        for (AccessRequestStatusItem request : requests) {
            updateEmailStatus(request, status);
        }
    }

    /**
     * Utility to update the email_status of a pending request.
     * @param request the pending access request
     * @param status the new email status
     */
    private void updateEmailStatus(AccessRequestStatusItem request, String status) {

        AccessRequestStatusDao arStatusDao = DaoFactory.DEFAULT.getAccessRequestStatusDao();
        request = arStatusDao.get((Integer)request.getId());
        request.setEmailStatus(status);
        arStatusDao.saveOrUpdate(request);
    }

    /**
     * Utility to send email to datashop-help when user doesn't have an email address.
     * @param userName the user's name
     * @param subject the email subject
     * @param message the email body
     */
    private void failureToSendEmail(String userName, String subject, StringBuffer message) {

        logger.info("Email address is null. Unable to send email to: " + userName);

        // Notify datashop-help that message cannot be sent.
        message.insert(0, "<p>This message cannot be sent to " + userName
                       + " because their email address is null or empty.</p>");
        sendEmail(getEmailAddress(), getEmailAddress(), subject, message.toString());
    }

    /**
     * Utility to generate HTML table of pending requests.
     * @param requests list of the pending requests
     * @return HTML string for table
     */
    private String pendingRequestsTable(List<AccessRequestStatusItem> requests) {

        StringBuffer sb = new StringBuffer();
        sb.append("<table border=\"1\" cellpadding=\"6\" cellspacing=\"0\">");

        sb.append("<th>User</th>");
        sb.append("<th>Date of Request</th>");
        sb.append("<th>Project</th>");
        sb.append("<th>Reason</th>");

        AccessRequestHistoryDao arHistoryDao =
            DaoFactory.DEFAULT.getAccessRequestHistoryDao();

        for (AccessRequestStatusItem request: requests) {

            // Get corresponding AccessRequestHistoryItem
            AccessRequestHistoryItem history =
                arHistoryDao.findLastRequest(request);

            sb.append("<tr><td>");
            sb.append(getUserLink(request.getUser()));
            sb.append("</td>");
            sb.append("<td>");
            sb.append(DATE_FORMAT.format(history.getDate()));
            sb.append("</td>");
            sb.append("<td>");
            sb.append(getProjectLink(request.getProject()));
            sb.append("</td>");
            sb.append("<td>");
            sb.append(history.getReason());
            sb.append("</td>");
            sb.append("</tr>");
        }

        sb.append("</table>");

        return sb.toString();
    }

    /**
     * Utility for generating email link from UserItem.
     * @param user the UserItem
     * @return email link for user
     */
    private String getUserLink(UserItem user) {

        UserDao userDao = DaoFactory.DEFAULT.getUserDao();
        user = userDao.get((String)user.getId());

        StringBuffer sb = new StringBuffer();
        sb.append("<a href=\"mailto:");
        sb.append(user.getEmail());
        sb.append("\">");
        sb.append(user.getName());
        sb.append("</a>");
        return sb.toString();
    }

    /**
     * Utility for generating project link from ProjectItem.
     * @param project the ProjectItem
     * @return link to project
     */
    private String getProjectLink(ProjectItem project) {

        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        project = projectDao.get((Integer)project.getId());

        StringBuffer sb = new StringBuffer();
        sb.append("<a href=\"");
        sb.append(getDatashopUrl());
        sb.append("/Project?id=");
        sb.append((Integer)project.getId());
        sb.append("\">");
        sb.append(project.getProjectName());
        sb.append("</a>");
        return sb.toString();
    }

    /**
     * Send email to users when their pending request is three weeks old,
     * BCC'ing the PI/DP of the project.
     * @param userItem the owner of the request
     * @param requests the pending requests
     */
    private void sendUserEmails(UserItem userItem, List<AccessRequestStatusItem> requests) {

        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();

        String subject = "Your pending request for access to your DataShop datasets";

        // Send an email for each pending request as the email is per-project.
        for (AccessRequestStatusItem request : requests) {
            String status = request.getStatus();
            // At this point, status has to be one of the following:
            // not_reviewed, pi_approved or dp_approved
            boolean hasPiVoted =
                (status.equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_PI_APPROVED))
                 ? true : false;
            boolean hasDpVoted =
                (status.equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DP_APPROVED))
                 ? true : false;

            ProjectItem project = request.getProject();
            project = projectDao.get((Integer)project.getId());

            UserItem pi = project.getPrimaryInvestigator();
            UserItem dp = project.getDataProvider();

            boolean bothExist = ((pi != null) && (dp != null));

            List<String> bccList = new ArrayList<String>();
            if ((pi != null) && (pi.getEmail() != null)) {
                bccList.add(pi.getEmail());
            }
            if ((dp != null) && (dp.getEmail() != null)) {
                bccList.add(dp.getEmail());
            }

            String piDpText = null;
            if ((dp == null) || (bothExist && (hasDpVoted || (pi.equals(dp))))) {
                piDpText = "PI";
            } else if ((pi == null) || (bothExist && hasPiVoted)) {
                piDpText = "data provider";
            } else {
                piDpText = "PI and data provider";
            }

            String notText =
                piDpText.contains("and") ? " do not " : " does not ";

            StringBuffer message = new StringBuffer();
            message.append("<p>Dear ");
            message.append(userItem.getName());
            message.append(",</p>");
            message.append("<p>");
            message.append("If the ");
            message.append(piDpText);
            message.append(notText);
            message.append("respond within the next week to ");
            message.append("your request for access to \"");
            message.append(project.getProjectName());
            message.append("\", we will deny the request on their behalf. ");
            message.append("If you know the ");
            if (piDpText.indexOf("and") > 0) {
                message.append(piDpText.replace("and", "or"));
            } else {
                message.append(piDpText);
            }
            message.append(", we recommend you contact them directly; ");
            message.append("otherwise, you are welcome to re-request access using the ");
            message.append("\"Re-request access\" button.");
            message.append("</p>");

            message.append("<p>Thanks,<br />");
            message.append("The DataShop Team</p>");

            String emailAddr = userItem.getEmail();
            if ((emailAddr != null) && (emailAddr.length() > 0)) {

                sendEmail(getEmailAddress(),   // from datashop-help
                          userItem.getEmail(), // to
                          bccList,
                          subject,
                          message.toString());

                updateEmailStatus(request, AccessRequestStatusItem.EMAIL_STATUS_THIRD_SENT);
            } else {
                updateEmailStatus(request, AccessRequestStatusItem.EMAIL_STATUS_UNABLE_TO_SEND);
                failureToSendEmail(userItem.getName(), subject, message);
            }
        }
    }

    /**
     * Utility method to make necessary database changes indicating that the
     * request is being denied by the system.
     * @param request the AccessRequestStatusItem being denied
     */
    private void denyRequest(AccessRequestStatusItem request) {
        logger.info("Denying request: " + request);
        UserItem user = request.getUser();
        UserDao userDao = DaoFactory.DEFAULT.getUserDao();
        user = userDao.get((String)user.getId());

        ProjectItem project = request.getProject();
        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        project = projectDao.get((Integer)project.getId());

        // Determine what level of access was originally requested.
        String level = AuthorizationItem.LEVEL_EDIT;   // default
        AccessRequestHistoryDao arHistoryDao = DaoFactory.DEFAULT.getAccessRequestHistoryDao();
        List<AccessRequestHistoryItem> historyList =
            (List<AccessRequestHistoryItem>)arHistoryDao.findByStatus(request);
        for (AccessRequestHistoryItem history : historyList) {
            if (history.getAction().
                equals(AccessRequestHistoryItem.ACCESS_REQUEST_ACTION_REQUEST)) {
                level = history.getLevel();
                break;
            }
        }

        // Push 'deny' out to database.
        AccessRequestUtil.createHistoryEntry(request, userDao.findOrCreateSystemUser(),
                                             AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_ADMIN,
                                             AccessRequestHistoryItem.ACCESS_REQUEST_ACTION_DENY,
                                             level,
                                             "Automatic denial of four-week-old request", true);

        // Update the status
        AccessRequestStatusDao arStatusDao = DaoFactory.DEFAULT.getAccessRequestStatusDao();

        request.setStatus(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DENIED);
        request.setHasRequestorSeen(false);
        request.setHasAdminSeen(false);
        request.setHasPiSeen(false);
        request.setHasDpSeen(false);
        request.setLastActivityDate((Date)Calendar.getInstance().getTime());
        arStatusDao.saveOrUpdate(request);

        logger.info("Updated status entry for"
                    + " ProjectId (" + project.getId() + ")"
                    + " and User Id (" + user.getId() + ")");

        updateEmailStatus(request, AccessRequestStatusItem.EMAIL_STATUS_DENIED);
    }

    /**
     * Get the URL for DataShop.
     * @return the url
     */
    private String getDatashopUrl() {
        return ServerNameUtils.getDataShopUrl();
    }
}
