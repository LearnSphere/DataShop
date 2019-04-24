/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2012
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.admin;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.auth.OliUserServices;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetUserLogDao;
import edu.cmu.pslc.datashop.dao.UserDao;
import edu.cmu.pslc.datashop.dao.UserRoleDao;
import edu.cmu.pslc.datashop.item.PasswordResetItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.item.UserRoleItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;
import edu.cmu.pslc.datashop.servlet.ServletDateUtil;
import edu.cmu.pslc.datashop.servlet.auth.AccessFilter.AccessContext;
import edu.cmu.pslc.datashop.servlet.auth.PasswordUtil;
import edu.cmu.pslc.datashop.util.ServerNameUtils;

/**
 * Class that provides manage user functionalities to the admin.
 * (DS1430)
 * @author Young Suk Ahn
 * @version $Revision: 15454 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2018-08-31 13:00:06 -0400 (Fri, 31 Aug 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ManageUsersServlet extends AbstractServlet {

    /** Serial Version UID. */
    private static final long serialVersionUID = -215939045014920938L;

    /** Title for the Admin page - "Admin". */
    public static final String SERVLET_LABEL = "Admin";

    /** Action names to decide which operation to execute. **/
    public static final String SERVLET_NAME = "ManageUsers";

    /** Action names to decide which operation to execute. **/
    public static final String ACTION_USER_DETAILS = "details";
    /** Action names to decide which operation to execute. **/
    public static final String ACTION_USER_EDIT = "edit";
    /** Action names to decide which operation to execute. **/
    public static final String ACTION_USER_ROLE_EDIT = "roleedit";
    /** Action names to decide which operation to execute. **/
    public static final String ACTION_PWD_RESET = "pwdreset";
    /** Action names to decide which operation to execute. **/
    public static final String ACTION_SUSPEND = "suspend";
    /** Action names to decide which operation to execute. **/
    public static final String ACTION_RESTORE = "restore";

    /** JSP Page for the forwarding **/
    private static final String USER_LIST_JSP = "/jsp_admin/admin_user_list.jsp";
    /** JSP Page for the forwarding **/
    private static final String USER_DETAILS_JSP = "/jsp_admin/admin_user_details.jsp";
    /** JSP Page for the forwarding **/
    private static final String USER_FORM_JSP = "/jsp_admin/admin_user_form.jsp";
    /** JSP Page for the forwarding **/
    private static final String USER_ROLE_FORM_JSP = "/jsp_admin/admin_user_roleform.jsp";

    /** Session key for the filter info **/
    private static final String SESS_KEY_FILTER = "ManageUsers-filter";

    /** Session key for the filter info. **/
    public static final String DATE_FORMAT = "MM/dd/yyyy";

    /** Admin flag admin-only. **/
    public static final String ADMIN_FLAG_ADMIN = "admin";

    /** Admin flag non-admin-only. **/
    public static final String ADMIN_FLAG_NONADMIN = "nonadmin";

    /** The roleMap contains all the roles to be displayed in the form **/
    private static final Map<String, String> ROLE_MAP = new LinkedHashMap<String, String>();

    /** Checked value. **/
    public static final String CHECKED_VAL = "checked";

    /** Maximum entries shown in the table **/
    private static final int LIST_MAX_ENTRIES = 20;

    /** Message to user to narrow their search. */
    private static final String MSG_NARROW_FILTER = "There may be more entries matching your query."
            + " You may want to narrow your search filter.";

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    @Override
    public void init() throws ServletException {
        super.init();

        ROLE_MAP.put(UserRoleItem.ROLE_LOGGING_ACTIVITY, "Logging Activity");
        ROLE_MAP.put(UserRoleItem.ROLE_WEB_SERVICES, "Web Services");
        ROLE_MAP.put(UserRoleItem.ROLE_TERMS_OF_USE_MANAGER, "Terms of Use Manager");
        ROLE_MAP.put(UserRoleItem.ROLE_EXTERNAL_TOOLS, "External Tools");
        ROLE_MAP.put(UserRoleItem.ROLE_RESEARCH_MANAGER, "Research Manager");
        ROLE_MAP.put(UserRoleItem.ROLE_DATASHOP_EDIT, "DataShop Edit");
        ROLE_MAP.put(UserRoleItem.ROLE_RESEARCH_GOAL_EDIT, "Research Goal Edit");
    }

    /**
     * Process the get method, which has read semantics.
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        logDebug("doGet begin :: ", getDebugParamsString(req));

        // Set the most recent servlet name for the help page
        setRecentReport(req.getSession(true), SERVLET_NAME);

        String actionName = getActionName(req);
        try {
            this.preProcess(req, resp);
            if (ACTION_USER_DETAILS.equals(actionName)) {
                this.showUserDetails(req, resp);
            } else if (ACTION_USER_EDIT.equals(actionName)) {
                this.showUserForm(req, resp);
            } else if (ACTION_USER_ROLE_EDIT.equals(actionName)) {
                this.showRoleForm(req, resp);
            } else if (ACTION_PWD_RESET.equals(actionName)) {
                this.doNotifyPasswordReset(req, resp);
            } else if (ACTION_SUSPEND.equals(actionName)) {
                this.doSuspendAccount(req, resp);
            } else if (ACTION_RESTORE.equals(actionName)) {
                this.doRestoreAccount(req, resp);
            } else  {
                this.showUsersList(req, resp);
            }

        } catch (Exception exception) {
            forwardError(req, resp, logger, exception);
        } finally {
            logDebug("doGet end");
        }
    }

    /**
     * Process post method, which as insert/update semantics.
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        logDebug("doPost begin :: ", getDebugParamsString(req));

        // Set the most recent servlet name for the help page
        setRecentReport(req.getSession(true), SERVLET_NAME);

        String actionName = getActionName(req);
        try {
            this.preProcess(req, resp);
            if (ACTION_USER_EDIT.equals(actionName)) {
                this.doUpdateUser(req, resp);
            } else if (ACTION_USER_ROLE_EDIT.equals(actionName)) {
                this.doUpdateRole(req, resp);
            } else if (ACTION_SUSPEND.equals(actionName)) {
                this.doSuspendAccount(req, resp);
            } else if (ACTION_RESTORE.equals(actionName)) {
                this.doRestoreAccount(req, resp);
            } else {
                this.showUsersList(req, resp);
            }
        } catch (Exception exception) {
            forwardError(req, resp, logger, exception);
        } finally {
            logDebug("doPost end");
        }
    }

    /**
     * Gets the action name from the request. The action name is used define the specific function
     * to call in the get/post operation.
     * @param req HttpServletRequest
     * @return the request parameter, 'action'
     */
    private String getActionName(HttpServletRequest req) {
        String actionName =  req.getParameter("action");
        return actionName;
    }

    /**
     * Checks the preconditions: user should be logged in and must be an admin.
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     */
    private void preProcess(HttpServletRequest req, HttpServletResponse resp) {
        UserItem userItem = (UserItem)req.getAttribute(AccessContext.KEY_USER_ITEM);
        if (userItem == null) {
            logger.warn("User not logged in attempted to access AdminUsersServlet. "
                    + "Is the AccessFilter properly set?");
            throw new IllegalStateException("You are not logged in.");
        }

        if (!userItem.getAdminFlag()) {
            logger.warn("A non-admin user attempted to access AdminUsersServlet. "
                    + "Is the AccessFilter properly set?");
            throw new IllegalStateException("You are not an Adminstrator.");
        }

        // To indicate which sub-tab (menu) currently is
        req.getSession().setAttribute("datasets", SERVLET_LABEL);
    }

    /**
     * Displays list of all users with query form
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    private void showUsersList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        UserItem filterInSession = (UserItem)req.getSession().getAttribute(SESS_KEY_FILTER);

        if (filterInSession == null) {
            filterInSession = new UserItem();
            filterInSession.setCreationTime(null);
            filterInSession.setAdminFlag(null);
        }
        String matchIdLike = req.getParameter("idLike");
        String matchNameLike = req.getParameter("nameLike");
        String matchEmailLike = req.getParameter("emailLike");
        String matchInstitutionLike = req.getParameter("institutionLike");
        String createdFrom = req.getParameter("createdFrom");
        String adminFlag = req.getParameter("adminFlag");

        if (matchIdLike != null) {
            filterInSession.setId(matchIdLike.trim());
        }
        if (matchNameLike != null) {
            filterInSession.setFirstName(matchNameLike.trim());
        }
        if (matchEmailLike != null) {
            filterInSession.setEmail(matchEmailLike.trim());
        }
        if (matchInstitutionLike != null) {
            filterInSession.setInstitution(matchInstitutionLike.trim());
        }
        if (createdFrom != null) {
            if (!StringUtils.isBlank(createdFrom)) {
                SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
                try {
                    Date date = sdf.parse(createdFrom);
                    filterInSession.setCreationTime(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } else {
                filterInSession.setCreationTime(null);
            }
        }

        if (adminFlag != null) {
            if (adminFlag.equalsIgnoreCase(ADMIN_FLAG_ADMIN)) {
                filterInSession.setAdminFlag(true);
            } else if (adminFlag.equalsIgnoreCase(ADMIN_FLAG_NONADMIN)) {
                filterInSession.setAdminFlag(false);
            } else {
                filterInSession.setAdminFlag(null);
            }
        }
        req.getSession().setAttribute(SESS_KEY_FILTER, filterInSession);

        UserDao userDao = DaoFactory.DEFAULT.getUserDao();
        DatasetUserLogDao userLogDao = DaoFactory.DEFAULT.getDatasetUserLogDao();
        List<UserItem> users = userDao.findBy(filterInSession, 0, LIST_MAX_ENTRIES);
        List<ManageUsersDto> dtoList = new ArrayList<ManageUsersDto>();
        for (UserItem userItem : users) {
            ManageUsersDto dto = new ManageUsersDto();
            dto.setUserItem(userItem);
            dto.setCreatedDate(ServletDateUtil.getDateString(userItem.getCreationTime()));
            Date lastLoginTime = userLogDao.getLastLogin(userItem);
            dto.setLastLoginDate(ServletDateUtil.getDateString(lastLoginTime));
            dtoList.add(dto);
        }

        if (users.size() == LIST_MAX_ENTRIES) {
            req.setAttribute("message", MSG_NARROW_FILTER);
        }

        req.setAttribute("userList", dtoList);
        req.setAttribute("users", users);
        req.setAttribute("criteria", filterInSession);

        getServletContext().getRequestDispatcher(USER_LIST_JSP).forward(req, resp);
    }


    /**
     * Displays the user details, which also contains the .
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    private void showUserDetails(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String errorMessage = req.getParameter("error");
        req.setAttribute("errorMessage", errorMessage);
        String matchId = req.getParameter("id");
        UserDao userDao = DaoFactory.DEFAULT.getUserDao();
        UserItem userItem = userDao.get(matchId);

        userItem.getName();
        req.setAttribute("user", userItem);

        // Get more info about the user than what is in the user table
        DatasetUserLogDao userLogDao = DaoFactory.DEFAULT.getDatasetUserLogDao();
        Date lastLoginTime = userLogDao.getLastLogin(userItem);

        final Integer limit = 5;
        List<String> projectNames = userLogDao.getLastProjects(userItem, limit);
        String lastFiveProjects = projectNames.toString().replace("[", "").replace("]", "");

        // Obtaining the roles
        UserRoleDao userRoleDao =  DaoFactory.DEFAULT.getUserRoleDao();
        List<UserRoleItem> userRoles = userRoleDao.find((String)userItem.getId());
        req.setAttribute("userRoles", userRoles);
        req.setAttribute("roleMap", ManageUsersServlet.ROLE_MAP);

        /*
         * isUserEnabled information is in SystemUser (from begin mind package)
         * so we need to retrieve it separately for suspend/resume.
         */
        Boolean isUserEnabled = OliUserServices.isUserEnabled(matchId);
        req.setAttribute("isUserEnabled", isUserEnabled);

        ManageUsersDto dto = new ManageUsersDto();
        dto.setUserItem(userItem);
        dto.setUserEnabled(isUserEnabled);
        dto.setUserRoles(userRoles);
        dto.setCreatedDate(ServletDateUtil.getDateString(userItem.getCreationTime()));
        dto.setLastLoginDate(ServletDateUtil.getDateString(lastLoginTime));
        dto.setLastFiveProjects(lastFiveProjects);
        req.setAttribute("manageUsersDto", dto);

        getServletContext().getRequestDispatcher(USER_DETAILS_JSP).forward(req, resp);
    }

    /**
     * Shows the user profile's edit form
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    private void showUserForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String matchId = req.getParameter("id");
        UserDao userDao = DaoFactory.DEFAULT.getUserDao();
        UserItem userItem = userDao.get(matchId);

        req.setAttribute("user", userItem);

        getServletContext().getRequestDispatcher(USER_FORM_JSP).forward(req, resp);
    }

    /**
     * Updates the user information in the database.
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    private void doUpdateUser(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String id = req.getParameter("id");

        UserDao userDao = DaoFactory.DEFAULT.getUserDao();
        UserItem userItem = userDao.get(id); // Check for null pointer
        if (userItem == null) {
            // User not found!
            resp.sendRedirect(ManageUsersServlet.SERVLET_NAME);
        }
        userItem.setFirstName(req.getParameter("firstName"));
        userItem.setLastName(req.getParameter("lastName"));
        userItem.setInstitution(req.getParameter("institution"));
        userItem.setEmail(req.getParameter("email"));
        userItem.setAdminFlag(Boolean.parseBoolean(req.getParameter("adminFlag")));
        userDao.saveOrUpdate(userItem);

        resp.sendRedirect(ManageUsersServlet.SERVLET_NAME + "?action="
                + ManageUsersServlet.ACTION_USER_DETAILS + "&id=" + id);
    }

    /**
     * Shows the user's role edit form.
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    private void showRoleForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String matchId = req.getParameter("id");
        UserDao userDao = DaoFactory.DEFAULT.getUserDao();
        UserItem userItem = userDao.get(matchId);

        UserRoleDao userRoleDao =  DaoFactory.DEFAULT.getUserRoleDao();
        List<UserRoleItem> userRoles = userRoleDao.find((String)userItem.getId());

        Set<String> userRoleSet = new HashSet<String>();
        for (UserRoleItem userRole: userRoles) {
            userRoleSet.add(userRole.getRole());
        }

        req.setAttribute("user", userItem);
        req.setAttribute("userRoleSet", userRoleSet);
        req.setAttribute("roleMap", ManageUsersServlet.ROLE_MAP);

        getServletContext().getRequestDispatcher(USER_ROLE_FORM_JSP).forward(req, resp);
    }

    /**
     * Updates the role setting in the database.
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    private void doUpdateRole(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String id = req.getParameter("id");

        UserDao userDao = DaoFactory.DEFAULT.getUserDao();
        UserItem userItem = userDao.get(id);

        UserRoleDao userRoleDao =  DaoFactory.DEFAULT.getUserRoleDao();
        List<UserRoleItem> userRoles = userRoleDao.find((String)userItem.getId());
        Set<String> userRoleSet = new HashSet<String>();
        for (UserRoleItem userRole: userRoles) {
            userRoleSet.add(userRole.getRole());
        }

        for (Map.Entry<String, String> entry: ManageUsersServlet.ROLE_MAP.entrySet()) {
            String value = req.getParameter(entry.getKey());

            if (CHECKED_VAL.equals(value)) {
                // Checked: add new role if not previously granted, skip otherwise
                if (!userRoleSet.contains(entry.getKey())) {
                    UserRoleItem newUserRole = new UserRoleItem(userItem, entry.getKey());
                    userRoleDao.saveOrUpdate(newUserRole);
                    notifyUser(newUserRole);
                }
            } else  {
                // UnChecked: remove role if previously granted, skip otherwise
                if (userRoleSet.contains(entry.getKey())) {
                    UserRoleItem removeUserRole = new UserRoleItem(userItem, entry.getKey());
                    userRoleDao.delete(removeUserRole);
                }
            }
        }

        //super.redirect(req, resp, jspName); is actually forward...
        resp.sendRedirect(ManageUsersServlet.SERVLET_NAME + "?action="
                + ManageUsersServlet.ACTION_USER_DETAILS + "&id=" + id);
    }

    /**
     * Sends a notification email to the user to reset his/her password.
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    private void doNotifyPasswordReset(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String id = req.getParameter("id");

        UserDao userDao = DaoFactory.DEFAULT.getUserDao();
        UserItem userItem = userDao.get(id);

        String errorMessage = null;

        if (userItem != null) {
            try {
                if (isSendmailActive()) {
                    if (userItem.getEmail() == null || userItem.getEmail().length() <= 0) {
                        errorMessage = "Email is blank, cannot send notification.";
                    } else {
                        PasswordResetItem passwordResetItem =
                                PasswordUtil.createPasswordResetItem(userItem);
                        try {
                            PasswordUtil.notifyEmail(userItem, passwordResetItem,
                                                     getEmailAddressDatashopHelp(),
                                                     getEmailAddressDatashopBucket(),
                                                     getBaseUrl(req));
                        } catch (IllegalArgumentException exception) {
                            errorMessage = "Unable to send email.";
                            String msg = errorMessage + " " + exception.getMessage();
                            logger.error(msg, exception);
                        }
                    }
                }
            } catch (IllegalArgumentException exception) {
                // handle WebISO user
                errorMessage = "Unable to reset password: " + exception.getMessage();
                logger.warn(errorMessage, exception);
            }

        }

        String errorQueryString = urlEncode("error", errorMessage);
        resp.sendRedirect(ManageUsersServlet.SERVLET_NAME + "?action="
                + ManageUsersServlet.ACTION_USER_DETAILS + "&id=" + id + errorQueryString);
    }

    /**
     * Suspends an OLI user
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    private void doSuspendAccount(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String matchId = req.getParameter("id");

        String errorMessage = null;
        if (OliUserServices.isOliEnabled()) {
            if (!OliUserServices.suspendUser(matchId)) {
                errorMessage = "Error occurred suspending user account.";
                logger.debug(errorMessage);
            } else {
                logger.info("Suspend Account: " + matchId);
            }
        } else {
            errorMessage = "OLI services are not enabled.";
            logger.debug(errorMessage);
        }

        String errorQueryString = urlEncode("error", errorMessage);

        resp.sendRedirect(ManageUsersServlet.SERVLET_NAME + "?action="
                + ManageUsersServlet.ACTION_USER_DETAILS + "&id=" + matchId + errorQueryString);
    }

    /**
     * Suspends an OLI user
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    private void doRestoreAccount(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String matchId = req.getParameter("id");

        String errorMessage = null;
        if (OliUserServices.isOliEnabled()) {
            if (!OliUserServices.restoreUser(matchId)) {
                errorMessage = "Error occurred restoring user account.";
                logger.debug(errorMessage);
            } else {
                logger.info("Restore Account: " + matchId);
            }
        } else {
            errorMessage = "OLI services are not enabled.";
            logger.debug(errorMessage);
        }

        String errorQueryString = urlEncode("error", errorMessage);

        resp.sendRedirect(ManageUsersServlet.SERVLET_NAME + "?action="
                + ManageUsersServlet.ACTION_USER_DETAILS + "&id=" + matchId + errorQueryString);
    }

    /**
     * Generates a URL encoded query string.
     * @param key the key name
     * @param data the value for the given key
     * @return a URL encoded query string
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    public final String urlEncode(String key, String data) throws UnsupportedEncodingException
    {
        if (StringUtils.isEmpty(data)) {
            return "";
        }
        return "&" + key + "=" + URLEncoder.encode(data, "UTF-8");
    }

    /** Constant for DataShop URL string to replace. */
    private static final String DATASHOP_URL = "DATASHOP_URL";

    /**
     * Helper method to notify user of new role.
     * This is only done for the roles users can request, namely:
     * logging, web services, external tools and datashop-edit.
     * @param newUserRole the new UserRoleItem
     */
    private void notifyUser(UserRoleItem newUserRole) {

        UserItem user = newUserRole.getUser();
        String newRole = newUserRole.getRole();

        RoleEmailInfo rei = ROLE_EMAIL_INFO_MAP.get(newRole);
        if (rei == null) { return; }

        // Replace DATASHOP_URL with server-specific URL.
        String dsUrl = ServerNameUtils.getDataShopUrl();

        String content = rei.getContent().replaceAll(DATASHOP_URL, dsUrl);
        
        String userEmail = user.getEmail();

        // Nothing to do if the user hasn't specified an email address.
        if (userEmail == null) { return; }

        String subject = rei.getSubject();
        StringBuffer message = new StringBuffer();
        message.append("<br>");
        message.append("Dear ");
        message.append(user.getName());
        message.append(", ");
        message.append("<br>");
        message.append("<br>");
        message.append(content);
        message.append("<br>");
        message.append("<br>");
        message.append("Thank you,");
        message.append("<br>");
        message.append("The DataShop Team");
        message.append("<br>");

        sendEmail(getEmailAddressDatashopHelp(), userEmail, subject, message.toString());
    }

    /**
     * Helper object to hold info necessary to generate emails by role.
     */
    static class RoleEmailInfo {
        /** The email subject. */
        private String subject;
        /** The email content. */
        private String content;

        /**
         * Constructor
         * @param subject the email subject
         * @param content the email content
         */
        RoleEmailInfo(String subject, String content) {
            this.subject = subject;
            this.content = content;
        }

        /** Getter for email subject. @return String the subject */
        public String getSubject() {
            return subject;
        }

        /** Getter for email content. @return String the content */
        public String getContent() {
            return content;
        }
    }

    /** Constants for Logging Activity. */
    private static final String LOGGING_EMAIL_SUBJECT =
        "Access to DataShop Logging Activity Report granted";
    private static final String LOGGING_HELP_URL =
        DATASHOP_URL + "/help?page=loggingActivity";
    private static final String LOGGING_EMAIL_CONTENT =
        "You have been granted access to DataShop's Logging Activity report. "
        + "Please see our <a href=\"" + LOGGING_HELP_URL + "\">Logging Activity</a> help page.";

    /** Constants for Web Services. */
    private static final String WEB_SVC_EMAIL_SUBJECT =
        "Access to DataShop Web Services granted";
    private static final String WEB_SVC_HELP_URL =
        DATASHOP_URL + "/help?page=webServicesCredentials";
    private static final String CITING_DS_URL = DATASHOP_URL + "/help?page=citing";
    private static final String WEB_SVC_EMAIL_CONTENT =
        "You have been granted access to DataShop's Web Services. For more information about how "
        + "to use these services, see our <a href=\"" + WEB_SVC_HELP_URL + "\">"
        + "Web Services Credentials</a> help page. Please let us know if you need any help and "
        + "if you are successful in using them.<br><br>"
        + "Also, please refer to our <a href=\"" + CITING_DS_URL + "\">"
        + "Citing DataShop and Datasets</a> help page if you publish any findings from "
        + "DataShop's datasets.";

    /** Constants for External Tools. */
    private static final String EXT_TOOLS_EMAIL_SUBJECT =
        "Permission to add external tools to DataShop granted";
    private static final String EXT_TOOLS_HELP_URL =
        DATASHOP_URL + "/help?page=externalTools";
    private static final String EXT_TOOLS_EMAIL_CONTENT =
        "Your request for permission to add tools to DataShop has been granted. "
        + "Please refer to our <a href=\"" + EXT_TOOLS_HELP_URL + "\">External Tools</a> "
        + "help page to get started. If you have any questions, please don't hesitate "
        + "to contact us -- we're happy to help.";

    /** Constants for DataShop-Edit. */
    private static final String DS_EDIT_EMAIL_SUBJECT =
        "Permission to upload datasets to DataShop granted";
    private static final String DS_EDIT_HELP_URL =
        DATASHOP_URL + "/help?page=import";
    private static final String DS_EDIT_EMAIL_CONTENT =
        "Your request for permission to upload datasets and create projects in "
        + "DataShop has been granted. Please refer to our <a href=\""
        + DS_EDIT_HELP_URL + "\">Importing New Data</a> help page to get started. "
        + "If you have any questions, please don't hesitate to contact us -- we're happy to help.";

    /** Map which holds instances of RoleEmailInfo by role. */
    private static final Map<String, RoleEmailInfo> ROLE_EMAIL_INFO_MAP =
        new HashMap<String, RoleEmailInfo>() {
        {
            put(UserRoleItem.ROLE_LOGGING_ACTIVITY,
                new RoleEmailInfo(LOGGING_EMAIL_SUBJECT, LOGGING_EMAIL_CONTENT));
            put(UserRoleItem.ROLE_WEB_SERVICES,
                new RoleEmailInfo(WEB_SVC_EMAIL_SUBJECT, WEB_SVC_EMAIL_CONTENT));
            put(UserRoleItem.ROLE_EXTERNAL_TOOLS,
                new RoleEmailInfo(EXT_TOOLS_EMAIL_SUBJECT, EXT_TOOLS_EMAIL_CONTENT));
            put(UserRoleItem.ROLE_DATASHOP_EDIT,
                new RoleEmailInfo(DS_EDIT_EMAIL_SUBJECT, DS_EDIT_EMAIL_CONTENT));
        }
    };
}
