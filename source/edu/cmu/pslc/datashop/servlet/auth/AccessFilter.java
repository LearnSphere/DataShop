/*
* Carnegie Mellon University, Human Computer Interaction Institute
* Copyright 2012
* All Rights Reserved
*/
package edu.cmu.pslc.datashop.servlet.auth;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.chain.Catalog;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.config.ConfigParser;
import org.apache.commons.chain.impl.CatalogFactoryBase;
import org.apache.commons.chain.impl.ChainBase;
import org.apache.commons.chain.impl.ContextBase;
import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.UserItem;

/**
 * This Servlet Filter is used to filter all request and control the access of
 * the DataShop pages.
 * It first checks whether the accessing resource is a non-processing resource
 * (such as images, scripts, etc).
 * It uses Apache's Commons Chain library.
 *
 * The Servlet Filter is enabled by defining it in the web.xml
 *
 * @author Young Suk Ahn
 * @since Datashop version 6.0
 * @version $Revision: 13778 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2017-01-25 12:35:21 -0500 (Wed, 25 Jan 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public class AccessFilter implements Filter {

    /** File that contains the definition of commands. **/
    public static final String COMMAND_CATALOG_PATH = "/WEB-INF/accessfilter-commands.xml";
    /** File that contains the url to command name mappings. **/
    public static final String MAPPING_FILENAME = "/WEB-INF/accessfilter-mapping.properties";


    /** extension of the unhandled resources. **/
    public static final String[] UNHANDLED_EXTS = {"jpg", "jpeg", "png",
                                                   "gif", "tiff", "ico",
                                                   "css", "js", "swf",
                                                   "xml", "svg" };

    /** Default command key (used when no mapping was found). **/
    public static final String DEFAULT_COMMAND_KEY = "_default_";

    /** Names of the servletPath (page) to be redirected. */
    public static final String REDIRECT_INDEX_JSP = "index.jsp";
    /** Login servlet path. */
    public static final String REDIRECT_LOGIN = "/login";
    /** Dataset info servlet path. */
    public static final String REDIRECT_DATASET_INFO = "/DatasetInfo";
    /** Error servlet path. */
    public static final String REDIRECT_ERROR = "/Error";

    /** Session key for the value of the last visit URL (used to redirect when
     * user logs in. **/
    public static final String LAST_VISIT_URL_KEY = "last_visit_url";

    /** HTTP UnAuthorized Error Code. */
    public static final int FOUR_O_ONE = 401;

    /** The command catalog (from apache.chains). **/
    private Catalog pageHandlerMap = null;

    /** Set of extensions that are ignored, i.e.: not processed. **/
    private Set<String> unhandledExtensions = new HashSet<String>(Arrays.asList(UNHANDLED_EXTS));

    /** Map that contains the url path to command name mapping. **/
    private Map<String, String> pathToCommandMapping = new HashMap<String, String>();
    /** Log4j Logger. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** Shibboleth URLs start with... */
    private static final String SHIBBOLETH_URL = "/Shibboleth.sso";

    @Override
    public void init(FilterConfig config) throws ServletException {

        this.pageHandlerMap = getCommandCatalog(config);

        // Loads the properties and register each of the url paths (in CSV
        // format) to the mapping.
        InputStream is = config.getServletContext().getResourceAsStream(
                MAPPING_FILENAME);
        Properties props = new Properties();
        try {
            props.load(is);
            for (Map.Entry entry : props.entrySet()) {
                String pathCsv = (String) entry.getValue();

                String[] paths = pathCsv.split(",");
                for (String path : paths) {
                    this.pathToCommandMapping.put(path.trim(),
                            (String) entry.getKey());
                }
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public void destroy() {
        // Nothing to do
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        /* Obtain the Http Request/Response as the Filter provides a generic
         * ServletRequest/Response
         */
        HttpServletRequest httpRequest = null;
        HttpServletResponse httpResponse = null;
        if (request instanceof HttpServletRequest
                && response instanceof HttpServletResponse) {
            httpRequest = (HttpServletRequest) request;
            httpResponse = (HttpServletResponse) response;
        } else {
            logger.error("This is not an Http request");
            return;
        }

        /*
         * We don't need to do access control on resources with specific
         * extensions: i.e. images, javascripts, etc.
         */
        String servletPath = httpRequest.getServletPath();

        /*
         * If a machine doesn't have Shibboleth installed (and running properly)
         * then Apache won't catch the request and it will end-up here.
         * So, all Shibboleth.sso requests will generate a PageNotFound error.
         * This is a good spot to catch them and ignore them because it's
         * likely an indicator that the DS host is a development machine.
         */
        if (servletPath.startsWith(SHIBBOLETH_URL)) {
            logger.info("Ignoring Shibboleth error; likely a dev machine.");
            return;
        }

        String reqUrl = httpRequest.getRequestURL().toString();
        int extensionPos = reqUrl.lastIndexOf(".");
        if (extensionPos >= 0) {
            String extension = reqUrl.substring(extensionPos + 1)
                    .toLowerCase();
            if (this.unhandledExtensions.contains(extension)) {
                chain.doFilter(request, response);
                return;
            }
        }

        /* Control rules starts here:
         * AccessContext initially contains request/response and the servletPath.
         */
        AccessContext accessCtx = new AccessContext(httpRequest, httpResponse,
                servletPath);

        /** Check if the request is ajax request **/
        String ajaxRequest = httpRequest.getParameter("ajaxRequest");
        if (ajaxRequest != null && ajaxRequest.equalsIgnoreCase("true")) {
            accessCtx.setAjax(true);
        }

        /*
         * Convert the servletPath (e.g. /index.jsp) to the command name
         * (e.g. checkAgreedTermsAndAdmin)
         */
        String commandName = this.pathToCommandMapping.get(servletPath);

        Command cmd = pageHandlerMap.getCommand(commandName);
        if (cmd == null) {
            // If no Command was found, execute the default one.
            cmd = pageHandlerMap.getCommand(DEFAULT_COMMAND_KEY);
        }

        if (logger.isDebugEnabled()) {
            if (cmd instanceof ChainBase) {
                logger.debug("Executing Filter Command Chain ["
                        + commandName + "] for servletPath: "
                        + servletPath);
            } else {
                logger.debug("Executing Filter Command ["
                        + cmd.getClass().getSimpleName() + "] for servletPath: "
                        + servletPath);
            }
        }

        // The command returns TRUE if stopped
        boolean result = true;
        try {
            result = cmd.execute(accessCtx);
        } catch (Exception e) {
            // Redirect to error page
            logger.error(
                    "There was an unexpected error during access check process.", e);
            AccessFilter.forwardError(accessCtx, logger, e);
            return;
        }

        if (!result) {
            // All checks passed through, proceed to call the actual servlet/pages.
            chain.doFilter(request, response);
        }

    }

    /**
     * Build the url of the current page (which is the last page from Login
     * module's perspective).
     * @param req the  HTTP servlet request
     * @return the url of the current page
     */
    public static final String lastVisitUrl(HttpServletRequest req) {
        String lastVisitUrl = req.getRequestURI();
        if (req.getQueryString() != null) {
            lastVisitUrl += "?" + req.getQueryString();
        }
        return lastVisitUrl;
    }

    /**
     * Redirect to the specified URL. Check for AJAX request.
     * @param req the HTTP servlet request
     * @param resp the HTTP servlet response
     * @param redirectUrl the redirect URL
     * @throws IOException an IOException
     */
    public static void redirect(HttpServletRequest req,
            HttpServletResponse resp, String redirectUrl) throws IOException {
        String isAJAXRequest = req.getParameter("AJAXRequest"); // parameter
        if (isAJAXRequest == null) {
            resp.sendRedirect(redirectUrl);
        } else {
            resp.sendError(FOUR_O_ONE,
                    "Not Authorized to view the requested component");
        }
    }

    /**
     * Similar to AbstractServlet's forwardError.
     * @param accessCtx the access context
     * @param logger the Log4j logger
     * @param exception the exception
     * @throws ServletException a servlet exception
     * @throws IOException an IO exception
     */
    public static void forwardError(AccessContext accessCtx, Logger logger,
            Exception exception) throws ServletException, IOException {

        String userId = ((accessCtx.getUserItem() == null) ? "unknown"
                : (String) accessCtx.getUserItem().getId());
        String message = "AbstractServlet.forwardError" + " : user " + userId
                + " : " + exception.getMessage();
        logger.error(message, exception);
        accessCtx.getHttpResponse().setContentType("text/html");
        accessCtx.getHttpRequest().setAttribute(
                "javax.servlet.jsp.jspException", exception);
        redirect(accessCtx.getHttpRequest(), accessCtx.getHttpResponse(),
                REDIRECT_ERROR);
    }

    /**
     * Gets the command catalog.
     * @param config the filter configuration
     * @return the command catalog
     */
    private Catalog getCommandCatalog(FilterConfig config) {
        // Parse the configuration file
        ConfigParser parser = new ConfigParser();
        String fileLocation = COMMAND_CATALOG_PATH;
        try {
            // InputStream is =
            // config.getServletContext().getResourceAsStream(MAPPING_FILENAME);
            parser.parse(config.getServletContext().getResource(fileLocation));
        } catch (Exception ex) {
            logger.error(ex);
        }
        return CatalogFactoryBase.getInstance().getCatalog();
    }

    ////////// Inner class: AccessContext /////////

    /**
     * Context class used to pass relevant information to the Command The
     * relevant information includes http request/response, user, datasetItem,
     * isAuthorized.
     *
     * @author Young Suk Ahn
     *
     */
    public class AccessContext extends ContextBase {
        /** A serious version UID. */
        private static final long serialVersionUID = -5539735975417211829L;

        /** Keys used to access the attribute map. **/
        public static final String KEY_USER_ITEM = "_userItem";
        /** Dataset id key. */
        public static final String KEY_DATASET_ID = "_datasetId";
        /** Dataset item key. */
        public static final String KEY_DATASET_ITEM = "_datasetItem";
        /** Is authorized for dataset key. */
        public static final String KEY_IS_AUTHORIZED_FOR_DS = "_isAuthorizedForDataset";

        /** Convenience member to hold the http request. **/
        private HttpServletRequest httpRequest = null;

        /** Convenience member to hold the http response. **/
        private HttpServletResponse httpResponse = null;

        /** The servlet path (as obtained from the AccesFilter). **/
        private String servletPath = null;

        /** isAjax call (if the parameter contains ajaxRequest) **/
        private boolean isAjax = false;

        /** The attributes that holds all the values used by the Commands. **/
        private Map<String, Object> attributes = new HashMap<String, Object>();

        /**
         * AccessContext constructor.
         * @param httpRequest the HTTP servlet request
         * @param httpResponse the HTTP servlet response
         * @param servletPath the servlet path
         */
        public AccessContext(HttpServletRequest httpRequest,
                HttpServletResponse httpResponse, String servletPath) {
            this.httpRequest = httpRequest;
            this.httpResponse = httpResponse;
            this.servletPath = servletPath;
        }

        /**
         * Checks whether an attribute was set or not.
         * Notice that the getter will not differentiate unset attribute
         * from null set attribute.
         * @param key the key to check.
         * @return true if the key exists, false otherwise.
         */
        public boolean containsAttribute(String key) {
            return attributes.containsKey(key);
        }

        /**
         * Return the HttpRequest that was set by the AccessFilter.
         * @return the HTTP servlet request
         */
        public HttpServletRequest getHttpRequest() {
            return httpRequest;
        }

        /**
         * Return the HttpResponse that was set by the AccessFilter.
         * @return the HTTP servlet response
         */
        public HttpServletResponse getHttpResponse() {
            return httpResponse;
        }
        /**
         * Return the servlet path.
         * @return the servlet path
         */
        public String getServletPath() {
            return servletPath;
        }

        /**
         * Returns the userItem object set by a Command.
         * @return userItem if was set, null otherwise.
         */
        public UserItem getUserItem() {
            return (attributes.containsKey(KEY_USER_ITEM) ? (UserItem) attributes
                    .get(KEY_USER_ITEM) : null);
        }

        /**
         * Set the user item.
         * @param userItem the user item
         */
        public void setUserItem(UserItem userItem) {
            attributes.put(KEY_USER_ITEM, userItem);
            this.getHttpRequest().setAttribute(KEY_USER_ITEM, userItem);
        }

        /**
         * Returns the datasetId set by a Command.
         * @return datasetId if was set, null otherwise.
         */
        public String getDatasetId() {
            return (attributes.containsKey(KEY_DATASET_ID) ? (String) attributes
                    .get(KEY_DATASET_ID) : null);
        }

        /**
         * Set the dataset id.
         * @param datasetid the dataset id
         */
        public void setDatasetId(String datasetid) {
            attributes.put(KEY_DATASET_ID, datasetid);
            this.getHttpRequest().setAttribute(KEY_DATASET_ID, datasetid);
        }

        /**
         * Returns the DatasetItem object set by a Command.
         * @return DatasetItem if was set, null otherwise.
         */
        public DatasetItem getDatasetItem() {
            return (attributes.containsKey(KEY_DATASET_ITEM) ? (DatasetItem) attributes
                    .get(KEY_DATASET_ITEM) : null);
        }

        /**
         * Set the dataset item.
         * @param datasetItem the dataset item
         */
        public void setDatasetItem(DatasetItem datasetItem) {
            attributes.put(KEY_DATASET_ITEM, datasetItem);
            this.getHttpRequest().setAttribute(KEY_DATASET_ITEM, datasetItem);
        }

        /**
         * Returns true if the user is authorized for dataset.
         * This attribute is set by LoadDatasetCommand
         * @return true if authorized, false otherwise.
         */
        public Boolean isAuthorizedForDataset() {
            return (attributes.containsKey(KEY_IS_AUTHORIZED_FOR_DS) ? (Boolean) attributes
                    .get(KEY_IS_AUTHORIZED_FOR_DS) : null);
        }

        /**
         * Set isAuthorizedForDataset key.
         * @param isAuthorizedForDataset whether the user is authorized for the dataset
         */
        public void setAuthorizedForDataset(boolean isAuthorizedForDataset) {
            attributes.put(KEY_IS_AUTHORIZED_FOR_DS, isAuthorizedForDataset);
            this.getHttpRequest().setAttribute(KEY_IS_AUTHORIZED_FOR_DS,
                    isAuthorizedForDataset);
        }

        /**
         * Is AJAX in play.
         * @return whether AJAX is in play
         */
        public boolean isAjax() {
            return isAjax;
        }

        /**
         * Set isAjax.
         * @param isAjax whether AJAX is in play
         */
        public void setAjax(boolean isAjax) {
            this.isAjax = isAjax;
        }

    }

}
