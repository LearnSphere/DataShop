/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet;

import static edu.cmu.pslc.datashop.util.CollectionUtils.keyValues;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.time.FastDateFormat;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import edu.cmu.pslc.datashop.auth.AuthInfo;
import edu.cmu.pslc.datashop.auth.OliUserServices;
import edu.cmu.pslc.datashop.dao.AuthorizationDao;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DataShopInstanceDao;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.ProjectDao;
import edu.cmu.pslc.datashop.dao.SampleMetricDao;
import edu.cmu.pslc.datashop.dao.TermsOfUseDao;
import edu.cmu.pslc.datashop.dao.TermsOfUseVersionDao;
import edu.cmu.pslc.datashop.dao.UserDao;
import edu.cmu.pslc.datashop.dao.UserRoleDao;
import edu.cmu.pslc.datashop.dao.UserTermsOfUseHistoryDao;
import edu.cmu.pslc.datashop.dao.UserTermsOfUseMapDao;
import edu.cmu.pslc.datashop.dto.DTO;
import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.helper.UserState;
import edu.cmu.pslc.datashop.item.AuthorizationItem;
import edu.cmu.pslc.datashop.item.DataShopInstanceItem;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.item.TermsOfUseItem;
import edu.cmu.pslc.datashop.item.TermsOfUseVersionItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.item.UserTermsOfUseHistoryItem;
import edu.cmu.pslc.datashop.item.UserTermsOfUseMapItem;
import edu.cmu.pslc.datashop.servlet.tou.TermsServlet;
import edu.cmu.pslc.datashop.util.DataShopInstance;
import edu.cmu.pslc.datashop.util.LogUtils;
import edu.cmu.pslc.datashop.util.MailUtils;
import edu.cmu.pslc.datashop.util.ServerNameUtils;
import edu.cmu.pslc.datashop.util.CollectionUtils.KeyValue;

/**
 * Handles the basic log in.
 *
 * @author Alida Skogsholm
 * @version $Revision: 16047 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2019-04-22 15:53:43 -0400 (Mon, 22 Apr 2019) $
 * <!-- $KeyWordsOff: $ -->
 */
public abstract class AbstractServlet extends HttpServlet {
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** The JSP name for the index */
    protected static final String REDIRECT_INDEX_JSP = "index.jsp";

    /** HTTP UnAuthorized Error Code */
    protected static final int FOUR_O_ONE = 401;

    /** Session Key for the UserItem */
    protected static final String USER_SESSION_KEY = "cmu.edu.pslc.datashop.item.UserItem";

    /** Default base directory for the files associated with a dataset. */
    private static final String BASE_DIR_DEFAULT = "/datashop/dataset_files";

    /** Base directory for the files associated with a dataset. */
    private String baseDir = BASE_DIR_DEFAULT;

    /** Default base directory for the files associated with a dataset. */
    private static final String AGGREGATOR_SP_FILEPATH_DEFAULT = "/datashop/dataset_files";

    /** Base directory for the files associated with a dataset. */
    private String aggregatorSpFilePath = AGGREGATOR_SP_FILEPATH_DEFAULT;

    /** Default e-mail address of DataShop Help. */
    private static final String EMAIL_DATASHOP_HELP_DEFAULT =
        "qa-datashop-help@lists.andrew.cmu.edu";

    /** Default e-mail address of DataShop Help. */
    private static String emailAddressDatashopHelp = EMAIL_DATASHOP_HELP_DEFAULT;

    /** Default maximum transaction count for immediate dataset deletion (import queue). */
    private static final Integer MAX_TRANSACTIONS_ALLOW_DELETION = 10000;

    /** Default maximum transaction count for immediate dataset deletion (import queue). */
    private static Integer numTransactionsImmediateDelete = MAX_TRANSACTIONS_ALLOW_DELETION;

    /** Default e-mail address of DataShop Research Manager. */
    private static final String EMAIL_DATASHOP_RM_DEFAULT =
        "qa-ds-research-manager@lists.andrew.cmu.edu";

    /** Default e-mail address of DataShop Research Manager. */
    private static String emailAddressDatashopRM = EMAIL_DATASHOP_RM_DEFAULT;

    /** Default e-mail address of DataShop Email Bucket. */
    private static final String EMAIL_DATASHOP_BUCKET_DEFAULT =
        "qa-ds-email-bucket@lists.andrew.cmu.edu";

    /** Default e-mail address of DataShop Email Bucket. */
    private static String emailAddressDatashopBucket = EMAIL_DATASHOP_BUCKET_DEFAULT;

    /** Whether or not send-mail is enabled for Terms Of Use changes. */
    private static boolean isSendmailActive = false;

    /** Max file size is 400MB. */
    private static final Integer MAX_FILE_SIZE = 400 * 1024 * 1024;

    /** Max file size is 1GB for DS Admins. */
    private static final Integer MAX_FILE_SIZE_ADMIN = 1000 * 1024 * 1024;

    /** Default number of lines for FFI to verify during ImportQueue upload. */
    private static final Integer NUM_FFI_VERIFY_LINES_DEFAULT = 100;

    /** Number of lines for FFI to verify during ImportQueue upload. */
    private static Integer numFFIVerifyLines = NUM_FFI_VERIFY_LINES_DEFAULT;

    /** Used for checking ids and the like. */
    public static final String STRICT_CHARS = "[a-zA-Z0-9_\\-]+";

    /**
     * Initialization for the servlet to read the baseDir from the initialization parameters.
     * @throws ServletException (checked exception)
     */
    public void init() throws ServletException {
        super.init(); //this is required when overriding this method
        ServletContext context = this.getServletContext();

        baseDir = context.getInitParameter("baseDir");
        aggregatorSpFilePath = context.getInitParameter("aggregator_sp_filepath");
        String maxTxAllowDel = context.getInitParameter("numTransactionsImmediateDelete");
        if (maxTxAllowDel != null && maxTxAllowDel.matches("\\d+")) {
            numTransactionsImmediateDelete = Integer.parseInt(
                    context.getInitParameter("numTransactionsImmediateDelete"));
        }
        numFFIVerifyLines = Integer.parseInt(context.getInitParameter("numFFIVerifyLines"));

        DataShopInstance.initialize();

        if (logger.isDebugEnabled()) {
            logger.info("init :: baseDir is " + baseDir);
        }
    }

    /**
     * Set the recent report servlet name in the http session for the help page
     * to be able to go back to it.
     * @param httpSession the HTTP session
     * @param servletName the name of the most recent servlet
     */
    protected void setRecentReport(HttpSession httpSession, String servletName) {
        httpSession.setAttribute("recent_report", servletName);
    }

    /**
     * Forward this request to the JSP.
     * @param req the request
     * @param resp the response
     * @param jspName name of the JSP to return
     * @throws ServletException (checked exception)
     * @throws IOException (checked exception)
     */
    protected void redirect(HttpServletRequest req, HttpServletResponse resp,
            String jspName) throws ServletException, IOException {
        getServletContext().getRequestDispatcher(jspName).forward(req, resp);
    }

    /**
     * Log the error and forward the exception to the Error Servlet.
     * Note that the caller has to be careful not to have opened a PrintWriter
     * before calling this method.
     * "You need to be careful when forwarding, though. If you open a PrintWriter
     * in a servlet and then try to forward, the forward will fail."
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @param logger Debug Logger
     * @param exception The exception that occurred in the servlet
     * @throws ServletException (checked exception)
     * @throws IOException (checked exception)
     */
    protected void forwardError(HttpServletRequest req, HttpServletResponse resp,
            Logger logger, Exception exception) throws ServletException, IOException {
        String userId = "unknown";
        UserItem userItem = getUser(req);
        if (userItem != null) {
            userId = (String)userItem.getId();
        }
        String message = "AbstractServlet.forwardError"
            + " : user " + userId
            + " : " + exception.getMessage();
        logger.error(message, exception);
        resp.setContentType("text/html");
        req.setAttribute("javax.servlet.jsp.jspException", exception);
        redirect(req, resp, "/Error");
    }

    /**
     * Helper function that gets called when you need to redirect to home.  It
     * will first check that the request was not an AJAX request, if an AJAX request
     * will return a generic error string.
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @throws IOException (checked exception) redirect exception.
     */
    protected void redirectHome(HttpServletRequest req, HttpServletResponse resp)
    throws IOException {
        try {
            String isAJAXRequest = req.getParameter("AJAXRequest"); // parameter
            if (isAJAXRequest == null) {
                resp.sendRedirect(REDIRECT_INDEX_JSP);
            } else {
                resp.sendError(FOUR_O_ONE, "Not Authorized to view the requested component");
            }
        } catch (IOException exception) {
            logger.error("IOException in redirectHome: "
                    + exception.getLocalizedMessage(), exception);
        }
    }



    /**
     * The one and only method the subclasses need to call to ensure that the
     * user is logged in and authorized for the selected dataset.
     *
     * Following the AccessFilter and 'servlet redirect on long' work, the only
     * servlet still using this is DatasetInfoReportServlet.
     *
     * If you find yourself writing a servlet that needs to call this method
     * first make sure it doesn't belong in the accessfilter map or this call
     * will be redundant.
     *
     * @param req the HTTP servlet request
     * @return true if the user is logged in and authorized, false otherwise
     */
    protected boolean isAuthorizedForDataset(HttpServletRequest req) {
        boolean returnFlag = false;
        if (isLoggedIn(req)) {
            if (isAuthorized(req, getDatasetContext(req))) {
                returnFlag = true;
            }
        }
        return returnFlag;
    }

    /** String constant for the HTTP prefix. */
    private static final String HTTP = "http";
    /** Constant for the length of the HTTP string constant. */
    private static final int HTTPS_IDX = HTTP.length();

    /**
     * Check for a secure HTTP connection.  If not, then redirect to a URL
     * with the S in HTTPS.
     * @param req the HTTP Servlet request
     * @param resp HttpServletResponse
     * @return true if we have redirected, false otherwise
     * @throws IOException (checked exception) redirect exception.
     */
    protected boolean redirectToSecureUrl(HttpServletRequest req, HttpServletResponse resp)
    throws IOException {
        if (OliUserServices.isOliEnabled()) {
            if (!req.isSecure()) {
                StringBuffer newUrl = req.getRequestURL();
                newUrl.insert(HTTPS_IDX, 's');
                logDebug("Need to redirect to HTTPS, new URL is ", newUrl);
                resp.sendRedirect(newUrl.toString());
                return true;
            }
        }
        return false;
    }

    /**
     * Needed for servlets which do not require user to be logged in.  But we
     * do need to check if the datasetContext's user item needs to be reset.
     * @param httpSession the HTTP session
     * @param req the HTTP Servlet request
     */
    protected void checkLogin(HttpSession httpSession, HttpServletRequest req) {
        logDebug("checkLogin begin");
        boolean flag = isLoggedIn(req);
        logDebug("checkLogin end, flag: ", flag);
    }

    /**
     * Check if the user is logged in.
     * @param req the {@link HttpServletRequest}
     * @return true if the user is logged in (or if on development machine), false otherwise
     */
    protected boolean isLoggedIn(HttpServletRequest req) {
        if (getLoggedInUserItem(req) == null) {
            return false;
        }
        return true;
    }

    /**
     * Check if the user is logged in.  If so, then make sure the Data Shop user
     * is created properly.  If on OLI-less system (i.e. development) then the
     * user is always logged in.
     * @param req the {@link HttpServletRequest}
     * @return the account id if logged in, null otherwise
     */
    protected UserItem getLoggedInUserItem(HttpServletRequest req) {
        boolean isLoggedIn = false;
        String accountId = req.getRemoteUser();
        UserDao userDao = DaoFactory.DEFAULT.getUserDao();
        UserItem currentUser = getUser(req);
        UserItem userItem = null;
        HttpSession httpSession = req.getSession();

        if (accountId == null) {
            UserItem sessionUser = AbstractServlet.getUser(req);
            if (sessionUser != null) {
                accountId = (String)sessionUser.getId();
            }
        }

        if (isGoogleUser(accountId)) {
            LogUtils.logDebug(logger, "isLoggedIn: Google user");

            if (accountId != null) {
                userItem = userDao.get(accountId);
                if (userItem == null) {
                    userItem = new UserItem();
                    userItem.setId(accountId);
                    userDao.saveOrUpdate(userItem);
                }
                isLoggedIn = true;
            }
        } else if (OliUserServices.isOliEnabled()) {
            logDebug("isLoggedIn: OLI services enabled");
            AuthInfo authInfo = OliUserServices.isLoggedIn(req);
            if (authInfo != null) {
                accountId = authInfo.getUserId();
                userItem = userDao.get(accountId);
                if (userItem == null) {
                    userItem = new UserItem();
                    userItem.setId(accountId);
                    userItem.setFirstName(authInfo.getFirstName());
                    userItem.setLastName(authInfo.getLastName());
                    userItem.setEmail(authInfo.getEmailAddress());
                    userItem.setInstitution(authInfo.getInstitution());
                    userDao.saveOrUpdate(userItem);
                    logger.info("User " + accountId + " saved in analysis database.");
                }
                isLoggedIn = true;
            }
        } else {
            logDebug("isLoggedIn: OLI services NOT enabled");

            if (accountId != null) {
                userItem = userDao.get(accountId);
                if (userItem == null) {
                    userItem = new UserItem();
                    userItem.setId(accountId);
                    userDao.saveOrUpdate(userItem);
                }
                isLoggedIn = true;
            }
        }

        if (!isLoggedIn) {
            httpSession.removeAttribute(USER_SESSION_KEY);

        } else {
            if ((currentUser == null) || (!userItem.equals(currentUser))) {
                httpSession.setAttribute(USER_SESSION_KEY, userItem);
            }
            setRecentDatasets(req.getSession(true), (String)userItem.getId());
        }

        logDebug("User ", accountId, " logged in: ", isLoggedIn);
        return userItem;
    }

    /** Constant. */
    private static final String GMAIL_DOMAIN = "gmail.com";

    /**
     * Helper function to determine if current user is a Google user.
     * @param accountId the logged in user
     * @return flag indicating if user is a Google user
     */
    private Boolean isGoogleUser(String accountId) {
        if (accountId == null) { return false; }

        if (accountId.indexOf(GMAIL_DOMAIN) > 0) {
            return true;
        }

        return false;
    }

    /**
     * Determine if the user is authorized on the selected dataset.
     * Look in the session datasetContext and/or in the servlet request.
     * @param req the HTTP servlet request
     * @param datasetContext the HTTP session datasetContext
     * @return true if the user is authorized to view the selected dataset
     */
    private boolean isAuthorized(HttpServletRequest req, DatasetContext datasetContext) {
        if (datasetContext == null) {
            logger.error("isAuthorized: datasetContext is unexpectedly null.");
            return false;
        }
        String accountId = datasetContext.getUserId();
        Integer datasetId = (Integer)datasetContext.getDataset().getId();
        ProjectHelper projHelper = HelperFactory.DEFAULT.getProjectHelper();
        if (accountId == null) {
            logger.warn("User id is unexpectedly null.");
        }
        return projHelper.isAuthorized(accountId, datasetId);
    }

    /**
     * Determine if the user is authorized to edit the selected dataset.
     * Look in the session datasetContext and/or in the servlet request.
     * @param req the HTTP servlet request
     * @param datasetContext the HTTP session datasetContext
     * @return true if the user is authorized to view the selected dataset
     */
    protected boolean hasEditAuthorization(HttpServletRequest req,
            DatasetContext datasetContext) {
        if (datasetContext == null) { return false; }

        String accountId = datasetContext.getUserId();
        Integer datasetId = (Integer)datasetContext.getDataset().getId();
        logDebug("hasEditAuthorization(User ", accountId, ", dataset ", datasetId, ")");

        if (accountId == null) {
            logger.warn("User id is unexpectedly null.");
            return false;
        }

        ProjectHelper projHelper = HelperFactory.DEFAULT.getProjectHelper();
        boolean hasEditAuth = projHelper.hasEditAuthorization(accountId, datasetId, datasetContext);
        if (!hasEditAuth) {
            hasEditAuth = projHelper.isDataShopAdmin(accountId, datasetContext);
        }
        return hasEditAuth;
    }

    /**
     * Determine if the user is authorized as an administrator.
     * @param req the HTTP servlet request
     * @param datasetContext the HTTP session datasetContext
     * @return true if the user is authorized to view the selected dataset
     */
    protected boolean hasAdminAuthorization(
            HttpServletRequest req, DatasetContext datasetContext) {
        if (datasetContext == null) { return false; }

        String accountId = datasetContext.getUserId();
        logDebug("hasAdminAuthorization(User ", accountId, ")");

        if (accountId == null) {
            logger.warn("User id is unexpectedly null.");
            return false;
        }

        ProjectHelper projHelper = HelperFactory.DEFAULT.getProjectHelper();
        return projHelper.isDataShopAdmin(accountId, datasetContext);
    }

    /**
     * Determine if the user is authorized as an administrator.
     * @param username the account id
     * @return true if the user is an administrator
     */
    protected boolean hasAdminAuthorization(String username) {
        if (username == null) {
            logger.warn("hasAdminAuthorization(User (" + username + ") is unexpectedly null");
            return false;
        }

        logDebug("hasAdminAuthorization(User (", username, ")");
        ProjectHelper projHelper = HelperFactory.DEFAULT.getProjectHelper();
        return projHelper.hasAdminAuthorization(username);
    }

    /**
     * Determine if the user has the logging activity role or is an administrator.
     * @param req the HTTP servlet request
     * @return true if the user is authorized to view the logging activity report
     */
    protected boolean hasLoggingActivityRole(HttpServletRequest req) {
        boolean flag = false;
        UserItem userItem = getLoggedInUserItem(req);
        UserRoleDao userRoleDao = DaoFactory.DEFAULT.getUserRoleDao();
        flag = userRoleDao.hasLoggingActivityAuth(userItem);
        return flag;
    }

    /**
     * Determine if the user has the web services role or is an administrator.
     * @param req the HTTP servlet request
     * @return true if the user is authorized to view the web services report
     */
    protected boolean hasWebServicesRole(HttpServletRequest req) {
        boolean flag = false;
        UserItem userItem = getLoggedInUserItem(req);
        UserRoleDao userRoleDao = DaoFactory.DEFAULT.getUserRoleDao();
        flag = userRoleDao.hasWebServicesAuth(userItem);
        return flag;
    }

    /**
     * Overload the getDatasetContext method to first get the session from the request.
     * This method will return null if unsuccessful in determining dataset id.
     * @param req the request
     * @return the DatasetContext object
     */
    protected DatasetContext getDatasetContext(HttpServletRequest req) {
        HttpSession httpSession = req.getSession(true);
        String datasetIdString = req.getParameter("datasetId");

        //see if we already saved it as an attribute.
        if (datasetIdString == null) {
            datasetIdString = (String)req.getAttribute("datasetId");
        }

        if (datasetIdString == null
                || datasetIdString.equals("") || datasetIdString.equals("null")) {
            logger.debug("Dataset ID parameter was blank, returning null context.");
            return null;
        } else {
            if (!datasetIdString.matches("\\d+")) {
                req.removeAttribute("datasetId");
                logger.debug("Dataset ID parameter was not a number, returning null context.");
                return null;
            }
        }


        //set it in the request as an attribute because sometimes (like when parsing
        //multi-part form requests) it gets lost and then subsequent calls on this
        //function with the same request will fail.
        // NOTE: do not set it in the session as user may view multiple
        // datasets simultaneously.
        req.setAttribute("datasetId", datasetIdString);

        return (DatasetContext)httpSession.getAttribute("datasetContext_" + datasetIdString);
    }

    /**
     * If the request is a multi-part form, parse for FileItems. This method will
     * return null if an error occurs, including a request to upload a file that
     * exceeds FILE_MAX_SIZE bytes.
     * @param req the request
     * @return the list of org.apache.commons.fileupload.FileItems
     */
    protected List<org.apache.commons.fileupload.FileItem> getDatasetUploadItems(
            HttpServletRequest req) {

        List <org.apache.commons.fileupload.FileItem> multiPartFormItems = null;
        if (ServletFileUpload.isMultipartContent(req)) { //deprecated method, okay 8/19/08
            try {
                DiskFileItemFactory factory = new DiskFileItemFactory();

                // Set factory constraints
                final int memoryLimit = 1024000;
                factory.setSizeThreshold(memoryLimit);

                UserItem userItem = getUser(req);

                // Create a new file upload handler
                ServletFileUpload upload = new ServletFileUpload(factory);
                if ((userItem != null) && (hasAdminAuthorization((String)userItem.getId()))) {
                    upload.setFileSizeMax(MAX_FILE_SIZE_ADMIN);
                } else {
                    upload.setFileSizeMax(MAX_FILE_SIZE);
                }

                // Parse the request
                List <org.apache.commons.fileupload.FileItem> items = upload.parseRequest(req);
                multiPartFormItems = new ArrayList <org.apache.commons.fileupload.FileItem> ();
                for (org.apache.commons.fileupload.FileItem item : items) {
                    String name = item.getFieldName();
                    if (item.isFormField() && name.equals("datasetId")) {
                        logDebug("Form field ", name, " with value ",
                                item.getString(), " detected.");
                    }
                    if (item.isFormField()) {
                        // These items can be processed in any order by FileServlet.handleUpload()
                        // as long as the 'fileName' item is processed last. So... always insert
                        // these items in the front.
                        multiPartFormItems.add(0, item);
                    } else {
                        // Ensure that the 'fileName' item is processed
                        // last during FileServlet.handleUpload()
                        multiPartFormItems.add(item);
                    }
                }

            } catch (FileUploadException fileUploadException) {
                logger.error("FileUploadException trying to read multi-part form data to get "
                        + "dataset id for dataset context.", fileUploadException);
                multiPartFormItems = null;
            }
        }

        if (multiPartFormItems != null) {
            return Collections.synchronizedList(multiPartFormItems);
        } else {
            return null;
        }
    }

    /**
     * Save the session datasetContext back to the session.  Only do this if the
     * user or dataset have changed by checking the dirty flag;
     * @param req {@link HttpServletRequest}
     * @param datasetContext the DatasetContext object
     */
    protected void setInfo(HttpServletRequest req, DatasetContext datasetContext) {
        setNumTransactions(datasetContext);
        req.getSession().setAttribute(
                "datasetContext_" + datasetContext.getDataset().getId(),
                datasetContext);
    }

    /**
     * Helper method to set the number of transactions for a given
     * DatasetContext.
     * @param datasetContext the DatasetContext
     */
    private void setNumTransactions(DatasetContext datasetContext) {
        SampleMetricDao smDao = DaoFactory.DEFAULT.getSampleMetricDao();
        datasetContext.setNumTransactions(smDao.getTotalTransactions(datasetContext.getDataset()));
    }

    /**
     * Get the UserItem stored in the session.
     * @param req {@link HttpServletRequest}
     * @return the UserItem, which has the account id
     */
    public static UserItem getUser(HttpServletRequest req) {
        return (UserItem)req.getSession().getAttribute(USER_SESSION_KEY);
    }

    /**
     * Set the user into the HttpSession, cleans the session if the user has changed.
     * @param req {@link HttpServletRequest}
     * @param userItem the user item
     */
    public void setUserAndCleanSession(HttpServletRequest req, UserItem userItem) {
        HttpSession session = req.getSession();
        UserItem currentUser = (UserItem)session.getAttribute(USER_SESSION_KEY);
        if ((userItem == null && currentUser != null)
                || (currentUser != null && !userItem.equals(currentUser))) {
            if (!session.isNew()) {
                session.invalidate();
            }
            session = req.getSession(true);
        }
        session.setAttribute(USER_SESSION_KEY, userItem);
    }

    /**
     * Set the user's recent dataset list in the HTTP session.
     * @param httpSession the HTTP session
     * @param accountId the account id
     */
    protected void setRecentDatasets(HttpSession httpSession, String accountId) {
        List datasetList = UserState.getRecentDatasetsViewed(accountId);
        logDebug("Number of recent datasets is: ", datasetList.size());
        httpSession.setAttribute("recent_datasets", datasetList);
    }

    /**
     * Use default content-type.
     * @param resp the response
     * @param str the content
     * @throws IOException (checked exception)
     */
    protected void writeString(HttpServletResponse resp, String str)
    throws IOException {
        writeString(resp, null, str);
    }

    /**
     * Write string with content type as the response.
     * @param resp the response
     * @param contentType the content-type
     * @param str the content
     * @throws IOException (checked exception)
     */
    public static void writeString(HttpServletResponse resp, String contentType, String str)
    throws IOException {
        if (contentType != null) {
            resp.setContentType(contentType);
        }
        PrintWriter out = resp.getWriter();
        out.write(str);
        out.flush();
        out.close();
    }

    /**
     * Write JSON object as the response.
     * @param resp the response
     * @param json the content
     * @throws IOException (checked exception)
     */
    protected void writeJSON(HttpServletResponse resp, JSONObject json) throws IOException {
        logTraceOrDebugLimited("writing json:", json.toString());
        writeString(resp, "application/json", json.toString());
    }

    /**
     * Write a validation message as a JSON object response.
     * By convention, the Javascript side can look for a validation message for a confirmation or
     * to know if something different from the usual case happened.
     * @param resp the response
     * @param msg the validation message
     * @throws IOException (checked exception)
     * @throws JSONException (checked exception)
     */
    protected void writeStatusMessage(HttpServletResponse resp, final String msg)
            throws IOException, JSONException {
        writeJSON(resp, json("statusMessage", msg));
    }

    /**
     * Write JSON object as the response, including the selected sub-tab.
     * (To prevent sub-tabs from inadvertently changing when the page reloads.)
     * @param resp the response
     * @param datasetContext the session datasetContext
     * @param json the content
     * @throws Exception unknown exception
     */
    protected void writeJSON(HttpServletResponse resp, DatasetContext datasetContext,
            JSONObject json) throws Exception {
        writeJSON(resp, json);
    }

    /**
     * Write DTO object as JSON response.
     * @param resp the response
     * @param dto translate into JSONObject then write to response
     * @throws IOException (checked exception)
     * @throws JSONException (checked exception)
     */
    protected void writeJSON(HttpServletResponse resp, DTO dto)
    throws IOException, JSONException {
        writeJSON(resp, (JSONObject)jsonFor(dto.propertiesMap()));
    }

    /**
     * We always use UTF-8 character encoding.
     * @param req the request
     * @param resp the response
     * @throws UnsupportedEncodingException (checked exception) unexpected encoding
     */
    protected void setEncoding(HttpServletRequest req, HttpServletResponse resp)
    throws UnsupportedEncodingException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
    }

    /**
     * Convenience method to get the selected skill model from the navigation helper.
     * @param navHelper the navigation helper
     * @param datasetContext the session datasetContext
     * @return the selected skill model
     */
    protected SkillModelItem getSkillModel(NavigationHelper navHelper,
            DatasetContext datasetContext) {
        return DaoFactory.DEFAULT.getSkillModelDao().get(
                navHelper.getSelectedSkillModel(datasetContext));
    }

    /**
     * @param o a field to be displayed in a PageGrid
     * @return the string to display for o
     */
    private String getDisplayString(Object o) {
        if (o == null) {
            return ".";
        }
        return o.toString();
    }

    /**
     * The data grid parameter.
     * @param req the request
     * @return The data grid parameter.
     */
    protected String dataGrid(HttpServletRequest req) {
        return req.getParameter("getDataGrid");
    }

    /**
     * This is a PageGrid request if there is a getDataGrid parameter.
     * @param req the request
     * @return whether or not this is a PageGrid request
     */
    protected boolean isPageGridRequest(HttpServletRequest req) {
        return dataGrid(req) != null;
    }

    /** Number formatter for integers. */
    private static final DecimalFormat INT_FORMAT = new DecimalFormat("##,###,##0");

    /**
     * Construct a JSON representation of a batch of results to display in a PageGrid
     * @param helper Encapsulates the specifics of a particular export
     * @param limit max items to return
     * @param offset first item to return
     * @param getMax whether to calculate and return the max value
     * @param datasetContext needed for the user id for benchmarking
     * @return JSON representation of a batch of results to display in a PageGrid
     * @throws JSONException (checked exception)
     */
    protected JSONObject getJSONResults(PageGridHelper helper, Integer limit,
            Integer offset, Boolean getMax,
            DatasetContext datasetContext) throws JSONException {
        if (logger.isTraceEnabled()) {
            logger.trace("Getting page grid data as a JSON Object.");
        }

        // The call to pageGridItems must happen first.
        List items = helper.pageGridItems(limit, offset);

        JSONArray headers = new JSONArray();
        for (Object header : helper.headers()) {
            //check to see if this is a multi-dimensional header.
            if (header instanceof List) {
                headers.put(new JSONArray((List)header));
            } else {
                headers.put(header);
            }
        }

        List<JSONArray> jsonList = new ArrayList<JSONArray>();

        for (int i = 0; i < items.size(); i++) {
            Object item = items.get(i);
            JSONArray jsonArr = new JSONArray();
            jsonArr.put(i + offset + 1);
            Object[] itemObjs = helper.translateItem(item);
            for (Object o : itemObjs) {
                jsonArr.put(getDisplayString(o));
            }
            for (int j = itemObjs.length + 1; j < headers.length(); j++) {
                jsonArr.put("");
            }
            jsonList.add(jsonArr);
        }

        JSONObject tableJSON = new JSONObject();

        if (items == null || items.size() == 0) {
            tableJSON.put("validationMessage", helper.validationMessage());
        } else {
            tableJSON.put("headers", headers);
            tableJSON.put("rows", new JSONArray(jsonList));
            tableJSON.put("limit", limit);
            if (getMax) {
                tableJSON.append("max", helper.max());
                String maxDisplay = INT_FORMAT.format(helper.max());
                tableJSON.append("maxDisplay", maxDisplay);
            }

            logger.info(getBenchmarkPrefix(datasetContext)
                    + " returning valid page grid results.");
        }

        if (logger.isTraceEnabled()) {
            logger.trace("page grid results :: " + tableJSON);
        }

        return tableJSON;
    }

    /**
     * Get an integer parameter from the request.
     * @param req the request
     * @param key the parameter to turn into Integer
     * @return an integer parameter from the request.
     */
    protected Integer getIntegerParam(HttpServletRequest req, String key) {
        String param = req.getParameter(key);
        Integer value = null;

        try {
            if (param != null) {
                value = new Integer(param);
            } else {
                logger.debug("integer parameter value was null. continuing...");
            }
        } catch (NumberFormatException numFormatException) {
            logger.error("Invalid Number Format :: " + key + " :: " + param);
            logger.debug("Caught Exception: ", numFormatException);
            value = null;
        }

        return value;
    }

    /**
     * Get a Boolean parameter from the request.
     * @param req the request
     * @param key the parameter to turn into Integer
     * @return a boolean parameter from the request.
     */
    protected Boolean getBooleanParam(HttpServletRequest req, String key) {
        String param = req.getParameter(key);
        Boolean value = null;

        try {
            if (param != null) {
                value = new Boolean(param);
            }
        } catch (NumberFormatException numFormatException) {
            logger.error("Invalid Boolean Format :: " + key + " :: " + param);
            logger.debug("Caught Exception: ", numFormatException);
            value = null;
        }

        return value;
    }

    /**
     * Construct and send a JSON response containing the data for a PageGrid request.
     * @param req the request
     * @param resp the response
     * @param helper Encapsulates the specifics of a particular export
     * @throws Exception exception
     */
    protected void handlePageGridRequest(HttpServletRequest req,
            HttpServletResponse resp,
            PageGridHelper helper) throws Exception {
        try {
            DatasetContext datasetContext = getDatasetContext(req);
            if (req.getParameter("updateLimitOnly") != null) {
                // this means that we need to only update the user-selected limit for the page
                // grid and then return
                Integer limit = getIntegerParam(req, "limit");
                logger.debug("performing ajax update for limit only - setting limit to " + limit);
                datasetContext.setPagegridRows(limit);
                writeString(resp, "I done diddit.");
            } else {
                JSONObject results = getJSONResults(helper,
                        getLimit(req), getIntegerParam(req, "offset"),
                        Boolean.valueOf(req.getParameter("getMax")), datasetContext);
                writeJSON(resp, datasetContext, results);
            }
        } catch (Exception e) {
            logger.error("exception fetching page grid data ", e);
        }
    }

    /**
     * Get the limit value for the page grid request.  First check the context to see
     * if the user has changed the limit value.
     * @param req the request
     * @return the appropriate limit value for use when retrieving rows for the page grid.
     */
    private Integer getLimit(HttpServletRequest req) {
        DatasetContext context = getDatasetContext(req);
        Boolean limitHasChanged = Boolean.valueOf(req.getParameter("limitHasChanged"));
        Integer limitFromPageGridReq = getIntegerParam(req, "limit");
        if (limitHasChanged) {
            context.setPagegridRows(limitFromPageGridReq);
        }
        return context.getPagegridRows();
    }

    /**
     * Returns the infoPrefix.
     * @param datasetContext the context needed to get the user id
     * @return the infoPrefix
     */
    protected String getInfoPrefix(DatasetContext datasetContext) {
        return getClass().getSimpleName() + " user " + datasetContext.getUserId();
    }
    /**
     * Get the info prefix for a user.
     * @param userId the user ID
     * @return prefix for log4j logging
     */
    protected String getInfoPrefix(String userId) {
        return getClass().getSimpleName() + " user " + userId;
    }

    /** Prefix for log4j benchmarking logs. */
    private static final String BENCHMARK = "BENCHMARK ";

    /**
     * Returns the prefix for logging a benchmark, given a datasetContext for the user ID.
     * @param datasetContext the context needed to get the user id
     * @return a string useful for log4j logging
     */
    protected String getBenchmarkPrefix(DatasetContext datasetContext) {
        return BENCHMARK + getClass().getSimpleName() + " user " + datasetContext.getUserId();
    }

    /**
     * Returns the prefix for logging a benchmark, given the user ID.
     * This is used by TxExportBean.
     * @param className the name of the class calling this method
     * @param userId the user ID
     * @return a string useful for log4j logging
     */
    public static String getBenchmarkPrefix(String className, String userId) {
        return BENCHMARK + className + " user " + userId;
    }

    /**
     * Sets the baseDir.
     * @param baseDir the base directory for file storage.
     */
    protected void setBaseDir(String baseDir) { this.baseDir = baseDir; }

    /**
     * Returns the baseDir.
     * @return the baseDir.
     */
    public String getBaseDir() { return baseDir; }

    /**
     * Sets the aggregator stored proc. file path location.
     * @param aggSpFilePath the file path to the aggregator stored procedure.
     */
    protected void setAggSpFilePath(String aggSpFilePath) {
        this.aggregatorSpFilePath = aggSpFilePath;
    }

    /**
     * Returns the aggregator stored proc file path.
     * @return the aggregator stored proc file path.
     */
    public String getAggSpFilePath() {
        return aggregatorSpFilePath;
    }

    /**
     * Returns the e-mail address for Datashop help.
     * @return the e-mail address for Datashop help.
     */
    public String getEmailAddressDatashopHelp() {
        if (DataShopInstance.getDatashopHelpEmail() != null) {
            return DataShopInstance.getDatashopHelpEmail();
        } else {
            return emailAddressDatashopHelp;
        }
    }

    /**
     * Returns the e-mail address for Datashop Research Manager.
     * @return the e-mail address for Datashop Research Manager.
     */
    public String getEmailAddressDatashopRM() {
        if (DataShopInstance.getDatashopRmEmail() != null) {
            return DataShopInstance.getDatashopRmEmail();
        } else {
            return emailAddressDatashopRM;
        }
    }

    /** Session attribute constant for RM's email address. */
    public static final String RM_EMAIL = "rm_email";

    /**
     * Sets the RM's email address in the session.
     * @param httpSession the HTTP session
     */
    public void setEmailAddressDatashopRMinSession(HttpSession httpSession) {
        httpSession.setAttribute(RM_EMAIL, getEmailAddressDatashopRM());
    }

    /**
     * Returns the e-mail address for Datashop Email Bucket.
     * @return the e-mail address for Datashop Email Bucket.
     */
    public String getEmailAddressDatashopBucket() {
        if (DataShopInstance.getDatashopBucketEmail() != null) {
            return DataShopInstance.getDatashopBucketEmail();
        } else {
            return emailAddressDatashopBucket;
        }
    }

    /**
     * Returns the maximum transactions to allow deletion of this a dataset.
     * @return the maximum transactions to allow deletion of this a dataset.
     */
    public Integer getNumTransactionsImmediateDelete() {
        return numTransactionsImmediateDelete;
    }


    /**
     * Returns whether or not sending mail is active.
     * @return whether or not sending mail is active.
     */
    public Boolean isSendmailActive() {
        if (DataShopInstance.getIsSendmailActive() != null) {
            return DataShopInstance.getIsSendmailActive();
        } else {
            return isSendmailActive;
        }
    }

    /**
     * Returns the number of lines for FFI to verify during ImportQueue upload.
     * @return the number of lines
     */
    public Integer getNumFFIVerifyLines() {
        return numFFIVerifyLines;
    }

    /**
     * Returns the file path of the transaction export stored procedure.
     * @return the file path
     */
    public String getTxExportSpFilePath() {
        return getServletContext().getInitParameter("tx_export_sp_filepath");
    }

    /**
     * Check value for edge cases before inserting into JSON.
     * @param json the JSONObject
     * @param key the key
     * @param value the value if something goes wrong
     * @throws JSONException (checked exception)
     */
    private static void putJSONValue(JSONObject json, String key, Object value)
    throws JSONException {
        try {
            json.put(key, jsonFor(value));
        } catch (JSONException jsone) {
            // display "-" for invalid values
            json.put(key, "-");
        }
    }

    /**
     * Convenience method for creating a JSON object.
     * @param keyValues alternating String keys and Object values
     * @return JSONObject constructed from the alternating String keys and Object values
     * in keyValues
     * @throws JSONException (checked exception)
     */
    public static final JSONObject json(final Object... keyValues) throws JSONException {
        final List<KeyValue<String, Object>> kvs = keyValues(keyValues);

        return new JSONObject() { {
            for (KeyValue<String, Object> kv : kvs) {
                putJSONValue(this, kv.getKey(), kv.getValue());
            }
        } };
    }

    /**
     * Generate JSONObject with same keys and values as map.
     * @param <V> kind of values contained in map
     * @param map the map
     * @return JSONObject with same keys and values as map.
     * @throws JSONException (checked exception)
     */
    public static final <V> JSONObject jsonForMap(final Map<String, V> map) throws JSONException {
        return new JSONObject() { {
            for (String key : map.keySet()) {
                putJSONValue(this, key, map.get(key));
            }
        } };
    }

    /**
     * Generate a JSONObject, given a Java object.  This method makes use of recursion.
     * @param object the object to process.
     * @return a JSON object.
     * @throws JSONException (checked exception) something could not be parsed.
     */
    public static final Object jsonFor(Object object) throws JSONException {
        if (object instanceof DTO) {
            return jsonFor(((DTO)object).propertiesMap());
        }
        if (object instanceof Map) {
            JSONObject jsonMap = new JSONObject();
            Set<String> keySet = ((Map)object).keySet();
            for (String key : keySet) {
                Object value = ((Map)object).get(key);
                jsonMap.put(key, jsonFor(value));
            }
            return jsonMap;
        } else if (object instanceof List) {
            JSONArray jsonList = new JSONArray();
            for (Object value : ((List)object)) {
                jsonList.put(jsonFor(value));
            }
            return jsonList;
        } else if (object instanceof Float && ((Float)object).isNaN()) {
            // display "-" for invalid values
            return ("-");
        } else {
            return object;
        }
    } // end jsonFor()

    /**
     * Check whether the request is an update for the Navigation or SampleSelector helper.
     * @param req the request
     * @return the AJAX update string, if any, null otherwise
     */
    protected String checkAJAXUpdate(HttpServletRequest req) {
        DatasetContext datasetContext = getDatasetContext(req);
        NavigationHelper navHelper = HelperFactory.DEFAULT.getNavigationHelper();
        SampleSelectorHelper sampleSelectorHelper =
            HelperFactory.DEFAULT.getSampleSelectorHelper();
        String ajaxUpdate = navHelper.updateNav(req, datasetContext);

        if (ajaxUpdate == null) {
            ajaxUpdate = sampleSelectorHelper.update(req, datasetContext, getBaseDir(),
                    getAggSpFilePath());
        }

        return ajaxUpdate;
    }

    /**
     * Write the AJAX update String as the response.
     * @param resp the response
     * @param ajaxUpdate the AJAX update String
     * @throws IOException (checked exception)
     */
    protected void writeAJAXUpdate(HttpServletResponse resp, String ajaxUpdate)
    throws IOException {
        logTraceOrDebugLimited("Ajax Update string not null.  Sending: ", ajaxUpdate);
        writeString(resp, "text/html", ajaxUpdate);
    }

    /**
     * Check whether this request is an AJAX update, and write the AJAX update response if so.
     * @param req the request
     * @param resp the response
     * @return whether this is an AJAX update
     * @throws IOException (checked exception)
     */
    protected boolean updateNavigationOptions(HttpServletRequest req, HttpServletResponse resp)
    throws IOException {
        String ajaxUpdate = checkAJAXUpdate(req);
        boolean isAJAXUpdate = ajaxUpdate != null;

        if (isAJAXUpdate) {
            writeAJAXUpdate(resp, ajaxUpdate);
        }

        return isAJAXUpdate;
    }

    /**
     * This is the only way to make the logger available to subclasses.
     * @return the logger for this class
     */
    protected Logger logger() {
        return logger;
    }

    /**
     * Only log if trace level is enabled.
     * @param args concatenate all arguments into the string to be logged
     */
   public void logTrace(Object... args) {
       LogUtils.logTrace(logger, args);
   }

    /**
     * Only log if debugging is enabled.
     * @param args concatenate all arguments into the string to be logged
     */
    public void logDebug(Object... args) {
        LogUtils.logDebug(logger, args);
    }

    /**
     * Only log if debugging is enabled.
     * @param args concatenate all arguments into the string to be logged
     */
    public void logInfo(Object... args) {
        LogUtils.logInfo(logger, args);
    }

    /** Constant. */
    private static final int MAX_DEBUG_LENGTH = 100;

    /**
     * Log up to the max length of the string if debug enabled,
     * or log the whole string if trace is enabled.
     * @param prefix the first string to log
     * @param str the second string to log and chop off if necessary
     */
    public void logTraceOrDebugLimited(String prefix, String str) {

        if (logger.isDebugEnabled()) {
            if (logger.isTraceEnabled()) {
                logger.trace(prefix + str);
            } else {
                int length = str.length();
                if (length > MAX_DEBUG_LENGTH) {
                    length = MAX_DEBUG_LENGTH;
                }
                logger.debug(prefix +  str.substring(0, length));
            }
        }
    }

   /**
     * Return a single string of all the request parameters, except any parameter
     * with the word 'password' in it.
     * @param req the HTTP request
     * @return a string
     */
    protected String getDebugParamsString(HttpServletRequest req) {
        String paramsString = "";
        for (String param :  (Set<String>)req.getParameterMap().keySet()) {
            if (param.toLowerCase().contains("password")) {
                paramsString += param + "(******), ";
            } else if (param.toLowerCase().contains("postdata")) {
                paramsString += param + ", ";
            } else {
                String p = req.getParameter(param);
                // Don't print really long params..
                if (p.length() < 100) {
                    paramsString += param + "(" + p + "), ";
                } else {
                    paramsString += param + "(...)";
                }
            }
        }
        return paramsString;
    }

    /** Time stamp format for the export files. */
    private static final FastDateFormat TIME_STAMP_FORMAT =
            FastDateFormat.getInstance("yyyy_MMdd_HHmmss");

    /**
     * Returns a file name for the export file. It starts with the dataset id,
     * then the type of export, followed by a timestamp from when the export actually
     * occurred.
     * @param datasetItem the dataset item
     * @param type the type of export
     * @return the file name
     */
    public static String getExportFileName(DatasetItem datasetItem, String type) {
        String fileName =
                "ds" + datasetItem.getId().toString()
                + "_" + type
                + "_" + TIME_STAMP_FORMAT.format(new Date());
        return fileName;
    }

    /**
     * Get a form parameter and trim it given the parameter name.
     * @param req HttpServletRequest
     * @param name the parameter name
     * @return the value of the form parameter, or null
     */
    protected static String getParameter(HttpServletRequest req, String name) {
        String param = req.getParameter(name);
        if (param != null) {
            param = param.trim();
        }
        return param;
    }

    /** The Terms Agree JSP name. */
    private static final String TERMS_AGREE_JSP_NAME = "/terms_agree.jsp";

    /**
     * Forward to the terms of use agree servlet.
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @throws ServletException possible exception on a forward
     * @throws IOException possible exception on a forward
     */
    protected void forwardTermsAgree(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        RequestDispatcher disp;
        disp = getServletContext().getRequestDispatcher(TERMS_AGREE_JSP_NAME);
        disp.forward(req, resp);
    }

    /**
     * Determine if the user has agreed to the DataShop and, if specified, Project
     * Terms of Use. Look in the session datasetContext and/or in the servlet request.
     *
     * Following the AccessFilter and 'servlet redirect on login' work the only servlet
     * still using this method is the ExternalToolsServlet. This is because users can
     * access that page without being logged in.
     *
     * If you find yourself writing a servlet that needs to call this method first make
     * sure it doesn't belong in the accessfilter map or this call will be redundant.
     *
     * @param req the HTTP servlet request
     * @param bothFlag true indicates to check both DataShop and Project ToU, false just DS
     * @return true if the user is authorized to view the selected dataset
     */
    protected boolean hasAgreedToTerms(HttpServletRequest req, boolean bothFlag) {
        boolean hasAgreedToTerms = false;
        logDebug("hasAgreedToTerms begin");

        if (isLoggedIn(req)) {
            UserItem userItem = getUser(req);
            String username = (String) userItem.getId();
            String prefix = "hasAgreedToTerms, user (" + username + ") ";

            DatasetContext datasetContext = getDatasetContext(req);
            ProjectHelper projHelper =
                    HelperFactory.DEFAULT.getProjectHelper();

            DatasetItem datasetItem = null;
            Boolean isAuthorizedForDataset;

            if (datasetContext != null) {
                datasetItem = datasetContext.getDataset();
            }

            try {
                Integer datasetId = (Integer) datasetItem.getId();
                isAuthorizedForDataset = projHelper.isAuthorized(
                        username, datasetId);
            } catch (Exception e) {
                isAuthorizedForDataset = false;
            }

            HttpSession httpSession = req.getSession();
            String originalUrl = req.getRequestURI();
            String queryString = req.getQueryString();
            if (queryString != null) {
                originalUrl += "?" + req.getQueryString();
            }
            httpSession.setAttribute(
                    TermsServlet.TERMS_URL_ATTRIB, originalUrl);

            if (hasAgreedToDataShopTerms(req, username)) {
                logTrace(prefix + "has agreed to datashop terms");
                if (bothFlag && isAuthorizedForDataset) {

                    if (datasetContext != null) {
                        Integer datasetId = (Integer)datasetContext.getDataset().getId();
                        httpSession.setAttribute(TermsServlet.TERMS_DATASET_ID_ATTRIB, datasetId);
                        if (hasAgreedToProjectTerms(req, username, datasetId)) {
                            hasAgreedToTerms = true;
                            logTrace(prefix + "has agreed to project terms");
                        } else {
                            getProjectTermsOfUse(req, username, datasetId);
                            logDebug(prefix, "has NOT agreed to project terms");
                        }
                    } else {
                       logger.error(prefix + "datasetContext is unexpectedly null.");
                    }
                } else {
                    hasAgreedToTerms = true;
                }
            } else {
                getDataShopTermsOfUse(req, username);
                logDebug(prefix, "has NOT agreed to datashop terms");
            }
        } else {
            //return true if user has not logged in
            logDebug("user has NOT logged in");
            hasAgreedToTerms = true;
        }
        logDebug("hasAgreedToTerms end, flag: " + hasAgreedToTerms);
        return hasAgreedToTerms;
    }

    /**
     * Determine if the user has agreed to the DataShop terms of use if one exists.
     * @param req the HTTP servlet request
     * @param username the ID of the user, account ID
     * @return true if the user has agreed or none exist, false otherwise
     */
    private boolean hasAgreedToDataShopTerms(
            HttpServletRequest req, String username) {
        ProjectHelper projHelper = HelperFactory.DEFAULT.getProjectHelper();
        return projHelper.hasAgreedToDataShopTerms(username);
    }

    /**
     * Determine if the user has agreed to the Project terms of use if one exists.
     * @param req the HTTP servlet request
     * @param username the ID of the user, account ID
     * @param datasetId the ID of the dataset
     * @return true if the user has agreed or none exist, false otherwise
     */
    private boolean hasAgreedToProjectTerms(
            HttpServletRequest req, String username, Integer datasetId) {
        ProjectHelper projHelper = HelperFactory.DEFAULT.getProjectHelper();
        DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
        DatasetItem datasetItem = datasetDao.get(datasetId);
        ProjectItem projectItem = datasetItem.getProject();
        if (projectItem != null) {
            return projHelper.hasAgreedToProjectTerms(username, (Integer)projectItem.getId());
        } else {
            return true;
        }
    }

    /**
     * Get DataShop's current terms of use and put it in the HTTP session.
     * @param req HttpServletRequest
     * @param username the account ID
     */
    protected void getDataShopTermsOfUse(
            HttpServletRequest req,
            String username) {

        UserDao userDao = DaoFactory.DEFAULT.getUserDao();
        UserItem userItem = null;
        if (username == null || username.length() == 0) {
            userItem = userDao.findOrCreateDefaultUser();
        } else {
            userItem = userDao.find(username);
        }
        String ipAddress = " [ipaddress:" + req.getRemoteAddr() + "]";

        String termsDate = "";
        String termsText = "";

        TermsOfUseVersionDao termsVersionDao = DaoFactory.DEFAULT.getTermsOfUseVersionDao();
        TermsOfUseVersionItem versionItem = termsVersionDao.getDataShopTerms(null);

        if (versionItem != null) {
            termsDate = TermsServlet.DISPLAY_DATE_FORMAT.format(versionItem.getAppliedDate());
            termsText = versionItem.getTerms();

            UserLogger.log(null, userItem, UserLogger.VIEW_TERMS,
                    "DataShop terms, version: " + versionItem.getVersion()
                    + " (current)" + ipAddress, false);
        }

        HttpSession httpSession = req.getSession();
        httpSession.setAttribute(TermsServlet.TERMS_TYPE_ATTRIB, TermsOfUseItem.DATASHOP_TERMS);
        httpSession.setAttribute(TermsServlet.TERMS_TEXT_ATTRIB, termsText);
        httpSession.setAttribute(TermsServlet.TERMS_EFFECTIVE_DATE_ATTRIB, termsDate);
        httpSession.setAttribute(TermsServlet.TERMS_UPDATE_FLAG_ATTRIB, false);
    }

    /**
     * Get project-specific current terms of use and put it in the HTTP session.
     * @param req HttpServletRequest
     * @param username the account ID
     * @param datasetId the ID of the dataset
     */
    protected void getProjectTermsOfUse(
            HttpServletRequest req,
            String username,
            Integer datasetId) {

        UserDao userDao = DaoFactory.DEFAULT.getUserDao();
        UserItem userItem = null;
        if (username == null || username.length() == 0) {
            userItem = userDao.findOrCreateDefaultUser();
        } else {
            userItem = userDao.find(username);
        }
        String ipAddress = " [ipaddress:" + req.getRemoteAddr() + "]";

        String termsDate = "";
        String termsText = "";

        DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        DatasetItem datasetItem = datasetDao.get(datasetId);
        ProjectItem projectItem = datasetItem.getProject();
        Integer projectId = (Integer)projectItem.getId();
        projectItem = projectDao.get(projectId);
        String projectName = projectItem.getProjectName();

        TermsOfUseVersionDao termsVersionDao = DaoFactory.DEFAULT.getTermsOfUseVersionDao();
        TermsOfUseVersionItem versionItem = termsVersionDao.getProjectTerms(projectId, null);

        if (versionItem != null) {
            termsDate = TermsServlet.DISPLAY_DATE_FORMAT.format(versionItem.getAppliedDate());
            termsText = versionItem.getTerms();

            TermsOfUseDao termsDao = DaoFactory.DEFAULT.getTermsOfUseDao();
            TermsOfUseItem termsItem = termsDao.get((Integer)versionItem.getTermsOfUse().getId());
            String termsName = termsItem.getName();
            UserLogger.log(null, userItem, UserLogger.VIEW_TERMS,
                    "Project terms (" + termsName + ") version: " + versionItem.getVersion()
                    + " (current)" + ipAddress, false);
        }

        TermsOfUseDao termsDao = DaoFactory.DEFAULT.getTermsOfUseDao();
        TermsOfUseItem termsItem = termsDao.get((Integer)versionItem.getTermsOfUse().getId());
        UserTermsOfUseMapDao userTermsOfUseMapDao = DaoFactory.DEFAULT.getUserTermsOfUseMapDao();
        boolean updateFlag = userTermsOfUseMapDao.hasAgreedBefore(username, termsItem);

        HttpSession httpSession = req.getSession();
        httpSession.setAttribute(TermsServlet.TERMS_TYPE_ATTRIB, TermsOfUseItem.PROJECT_TERMS);
        httpSession.setAttribute(TermsServlet.TERMS_TEXT_ATTRIB, termsText);
        httpSession.setAttribute(TermsServlet.TERMS_EFFECTIVE_DATE_ATTRIB, termsDate);
        httpSession.setAttribute(TermsServlet.TERMS_PROJECT_NAME_ATTRIB, projectName);
        httpSession.setAttribute(TermsServlet.TERMS_UPDATE_FLAG_ATTRIB, updateFlag);
    }

    /**
     * Save that the user agreed to the current Terms of Use to the database.
     * @param userItem the user item
     */
    protected void agreeToDataShopTerms(UserItem userItem) {
        TermsOfUseVersionDao termsVersionDao = DaoFactory.DEFAULT.getTermsOfUseVersionDao();
        UserTermsOfUseMapDao userTouMapDao = DaoFactory.DEFAULT.getUserTermsOfUseMapDao();
        UserTermsOfUseHistoryDao userTouHistoryDao =
                DaoFactory.DEFAULT.getUserTermsOfUseHistoryDao();

        TermsOfUseVersionItem versionItem = termsVersionDao.getDataShopTerms(null);
        if (versionItem != null) {
            UserTermsOfUseMapItem userTouMapItem = new UserTermsOfUseMapItem();
            userTouMapItem.setTermsOfUseExternal(versionItem.getTermsOfUse());
            userTouMapItem.setUserExternal(userItem);
            userTouMapItem.setTermsOfUseVersion(versionItem);
            userTouMapItem.setDate(new Date());
            userTouMapDao.saveOrUpdate(userTouMapItem);

            UserTermsOfUseHistoryItem userTouHistoryItem = new UserTermsOfUseHistoryItem();
            userTouHistoryItem.setTermsOfUseVersion(versionItem);
            userTouHistoryItem.setUser(userItem);
            userTouHistoryItem.setDate(userTouMapItem.getDate());
            userTouHistoryDao.saveOrUpdate(userTouHistoryItem);

            Integer version = versionItem.getVersion();
            UserLogger.log(null, userItem, UserLogger.AGREE_TERMS,
                    "DataShop terms, version: " + version, false);
            logger.info("Saved user (" + userItem.getId()
                    + ") agreement to DataShop terms version " + version);
        }
    }

    /**
     * Return the base URL as a string without the original servlet name.
     * @param req HttpServletRequest.
     * @return the base URL
     */
    protected String getBaseUrl(HttpServletRequest req) {
        String result = ServerNameUtils.getDataShopUrl();
        // If for some reason the above isn't valid, revert to old approach.
        if (result == null) {
            String requestUrl = req.getRequestURL().toString();
            String servlet = req.getServletPath();
            result = requestUrl.substring(0, requestUrl.indexOf(servlet));
        }
        return result;
    }

    /**
     * Helper method to get integer id and handle failure to do so.
     * @param idAttribute String value of id attribute in URL
     * @return Integer the projectId, null if not specified
     */
    protected Integer getIntegerId(String idAttribute) {
        if (idAttribute == null) {
            return null;
        }

        try {
            return Integer.parseInt(idAttribute);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Helper method to get long id and handle failure to do so.
     * @param idAttribute String value of id attribute in URL
     * @return Long the id, null if not specified
     */
    protected Long getLongId(String idAttribute) {
        if (idAttribute == null) {
            return null;
        }

        try {
            return Long.parseLong(idAttribute);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Updates the access flag based on the dataset context and user item.
     * It will not update the flag if the user is a project admin or datashop admin.
     * @param datasetContext the dataset context
     * @param userItem the user item
     * @param servletName the name of the calling servlet for logging purposes
     */
    public void updateAccessFlag(DatasetContext datasetContext,
            UserItem userItem, String servletName) {
        // Do not count DA's
        if (userItem == null || userItem.getAdminFlag()) {
            return;
        } else if (datasetContext != null
                && datasetContext.getDataset() != null) {
            DatasetDao dsDao = DaoFactory.DEFAULT.getDatasetDao();
            DatasetItem datasetItem =
                    dsDao.get((Integer)datasetContext.getDataset().getId());
            // Only update the flag if it hasn't been updated once before
            if (!datasetItem.getAccessedFlag()) {
                // If the dataset belongs to a project and the user has viewed it, then update
                // the ds_dataset.accessed_flag = true. Do not count PA's
                if (datasetItem.getProject() != null) {
                    AuthorizationDao authDao = DaoFactory.DEFAULT.getAuthorizationDao();
                    Integer projectId = (Integer)datasetItem.getProject().getId();
                    String level = authDao.getAuthLevel((String)userItem.getId(), projectId);
                    // Set dataset accessedFlag if the user is not a project administrator
                    if (level != null && !level.equals(AuthorizationItem.LEVEL_ADMIN)) {
                        datasetItem.setAccessedFlag(true);
                        dsDao.saveOrUpdate(datasetItem);
                        String infoString = "Dataset '"
                                + datasetItem.getDatasetName() + "' (" + datasetItem.getId()
                                + "): Servlet '" + servletName + "'.";
                        UserLogger.log(datasetItem, userItem,
                                UserLogger.ACTION_DATASET_ACCESSED, infoString);
                    }
                }
            }
        }
    }

    /**
     * Utility to send email to (and from) datashop-help.
     * @param source the class sending the email
     * @param subject the subject of the email
     * @param message the body of the email
     */
    protected void sendDataShopHelpEmail(String source, String subject, String message) {
        sendDataShopHelpEmail(source, subject, message, null);
    }

    /**
     * Utility to send email to (and from) datashop-help.
     * @param source the class sending the email
     * @param subject the subject of the email
     * @param message the body of the email
     * @param replyTo the address to used as "Reply To" field
     */
    protected void sendDataShopHelpEmail(String source, String subject, String message,
                                         String replyTo) {
        if (isSendmailActive()) {
            // Prepend message with host and source information.
            String host = ServerNameUtils.getHostNameForEmail();
            if (!host.equals("")) {
                message += "<br>Host: " + ServerNameUtils.getHostNameForEmail();
            }
            if (source != null) {
                message += "<br>";
                message += "<br>From: " + source + " at " + new Date();
            }

            // Send email with null BCC. No need since recipient is datashop-help.
            MailUtils.sendEmail(getEmailAddressDatashopHelp(), getEmailAddressDatashopHelp(),
                                replyTo, null, subject, message);
        }

    }

    /**
     * Utility to send email with a BCC to address specified by ds-email-bucket.
     * @param fromAddress the sender of the email
     * @param toAddress the recipient of the email
     * @param subject the subject of the email
     * @param message the body of the email
     */
    protected void sendEmail(String fromAddress, String toAddress, String subject, String message) {
        if (isSendmailActive()) {
            // Append footer to message if not null.
            String footer = ServerNameUtils.getFooterForEmail(fromAddress);
            if (footer != null) {
                message += footer;
            }

            // Send email, with BCC to ds-email-bucket.
            List<String> bccList = new ArrayList<String>();
            bccList.add(getEmailAddressDatashopBucket());

            // Use null "Reply To" address.
            MailUtils.sendEmail(fromAddress, toAddress, null, bccList, subject, message);
        }
    }

    /**
     * Strip any HTML from the given input but preserve new lines.
     * @param input the text to remove HTML from
     * @return given input stripped of HTML but with NLs preserved
     */
    public String stripHtml(String input) {
        StringBuffer buffer = new StringBuffer();
        for (String line : input.split("\n")) {
            buffer.append(Jsoup.parse(line).text() + "\n");
        }
        return buffer.toString();
    }

    protected void logChangeToAppearsAnon(DatasetItem datasetItem, UserItem userItem,
            String oldValue, String newValue) {
        String logInfoStr = "Dataset: '" + datasetItem.getDatasetName()
                + "' (" + datasetItem.getId() + ")";
        logInfoStr += ": Changed Appears Anonymous from '"
                + oldValue + "' to '" + newValue + "'";
        UserLogger.log(datasetItem, userItem, UserLogger.DATASET_INFO_MODIFY,
                logInfoStr, false);
    }

    protected void logChangeToIrbUploaded(DatasetItem datasetItem, UserItem userItem,
            String oldValue, String newValue) {
        String logInfoStr = "Dataset: '" + datasetItem.getDatasetName()
                + "' (" + datasetItem.getId() + ")";
        logInfoStr += ": Changed IRB Uploaded from '"
                + oldValue + "' to '" + newValue + "'";
        UserLogger.log(datasetItem, userItem, UserLogger.DATASET_INFO_MODIFY,
                logInfoStr, false);

        String logMsg = "User " + userItem.getId() + " ";
        logMsg += logInfoStr;
        if (datasetItem.getReleasedFlag()) {
            logMsg += " And the dataset is already released.";
        } else {
            logMsg += " But the dataset has NOT been released yet.";
        }
        logger.info(logMsg);
    }

    protected void logChangeToHasStudyData(DatasetItem datasetItem, UserItem userItem,
            String oldValue, String newValue) {
        String logInfoStr = "Dataset: '" + datasetItem.getDatasetName()
                + "' (" + datasetItem.getId() + ")";
        logInfoStr += ": Changed studyflag from '"
                + oldValue + "' to '" + newValue + "'";
        UserLogger.log(datasetItem, userItem, UserLogger.DATASET_INFO_MODIFY,
                logInfoStr, false);
    }

    protected Boolean isValidUrl(String urlStr) {
        try {
            URL url = new URL(urlStr);
        } catch (MalformedURLException e) {
            logger.error("isValidUrl:: " + e.toString());
            return false;
        }
        return true;
    }
}
