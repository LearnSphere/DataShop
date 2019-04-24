/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.learnsphere.webservices;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;

import static edu.cmu.pslc.datashop.util.StringUtils.join;
import static edu.cmu.pslc.datashop.util.CollectionUtils.map;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.util.DataShopInstance;

/**
 * Client for LearnShpere Web Services.  Provide methods for accessing each web service here.
 *
 * @author Hui Cheng
 * @version $Revision: 15894 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2019-03-12 17:47:14 -0400 (Tue, 12 Mar 2019) $
 * <!-- $KeyWordsOff: $ -->
 */
public class LearnSphereWebServicesClient {
    /** Any URL for accessing a LearnSphere web service should match this pattern. */
    private static final Pattern SERVICE_URL_PATTERN =
        Pattern.compile("(.*)/learnsphere/services(.*)(\\?.*)?");
    /** extract everything after "services" and before "?" (the simple path) in a service URL */
    private static final Pattern SIMPLE_SERVICE_PATH_PATTERN =
        Pattern.compile(".*/learnsphere/services([^\\?]*)\\??.*");
    /** extract everything after "services" */
    private static final Pattern SERVICE_PATH_PATTERN =
        Pattern.compile(".*/learnsphere/services(.*)(\\\\?.*)?");

    /** path indicating the web services root */
    private static final String SERVICES = "/learnsphere/services";
    /** use UTF-8 encoding */
    private static final String UTF8 = "UTF-8";
    /** We perform encryption with the HMAC Sha1 algorithm. */
    private static final String HMAC_SHA1 = "HmacSHA1";
    /** default to local host for testing */
    private static final String SERVICES_ROOT = "https://pslcdatashop.web.cmu.edu";
    /** format for HTTP date strings */
    private static FastDateFormat httpDateFmt;
    /** number of bytes to read at a time. */
    public static final int BUFFER_SIZE = 18024;
    
    private static final int READ_TIMEOUT_MAX = 3600000;
    private static final int CONNECT_TIMEOUT_MAX = 3600000;

    /** URL prefix for web services */
    private String rootURL;

    /** public API token for authentication */
    private String apiToken;
    /** secret key for authentication */
    private String secret;

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());
    
    /**
     * Instantiates a LearnSphereWeServiceClient to be used for remote requests to master LearnSphere servers.
     * @return the web service client or null if any required properties are missing
     */
    public static LearnSphereWebServicesClient getDatashopClientForRemoteRequests() {
        Boolean isSlave = DataShopInstance.isSlave();
        String masterUrl = DataShopInstance.getMasterUrl();
        String apiToken = DataShopInstance.getSlaveApiToken();
        String secret = DataShopInstance.getSlaveSecret();

        // Ensure this is a slave DataShop instance and in a state that is ready to make requests
        if ((isSlave != null && !isSlave) || masterUrl == null || apiToken == null || secret == null) {
            return null;
        }

        // Initialize and return a LearnSphere client ready for remote requests
        return new LearnSphereWebServicesClient(masterUrl, apiToken, secret);
    }

    /**
     * Instantiates a LearnSphere client to be used for remote requests to master DataShop servers.
     * @return the web service client or null if any required properties are missing
     */
    public static LearnSphereWebServicesClient getDatashopClientForLocalRequests() {
        String localUrl = "http://localhost:8080";
        String apiToken = DataShopInstance.getSlaveApiToken();
        String secret = DataShopInstance.getSlaveSecret();

        // Initialize and return a DataShop client ready for remote requests
        return new LearnSphereWebServicesClient(localUrl, apiToken, secret);
    }

    /**
     * Constructor.
     * @param servicesRoot URL prefix for web services
     * @param apiToken public API token for authentication
     * @param secret secret key for authentication
     */
    public LearnSphereWebServicesClient(String servicesRoot, String apiToken, String secret) {
        rootURL = servicesRoot;
        this.apiToken = apiToken;
        this.secret = secret;
    }

    /**
     * Constructor.  Root URL defaults to local host for testing.
     * @param apiToken public API token for authentication
     * @param secret secret key for authentication
     */
    public LearnSphereWebServicesClient(String apiToken, String secret) {
        this(SERVICES_ROOT, apiToken, secret);
    };
    
    /**
     * Returns the root URL.
     * @return the rootURL
     */
    public String getRootURL() {
        return rootURL;
    }
    
    /**
     * Sets the root URL.
     */
    public void setRootURL(String rootURL){
        this.rootURL = rootURL;
    }
    
    /**
     * Gets the API token.
     * @return the API token
     */
    public String getApiToken() {
        return this.apiToken;
    }
    
    /**
     * Sets the API token.
     * @param apiToken the API token
     */
    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }
    
    /**
     * Gets the secret.
     * @return the secret
     */
    public String getSecret() {
        return this.secret;
    }
    
    /**
     * Sets the secret
     * @param secret the secret
     */
    public void setSecret(String secret) {
        this.secret = secret;
    }
    
    /**
     * Format for HTTP date strings.
     * @return format for HTTP date strings
     */
    private String httpTimestamp() {
        if (httpDateFmt == null) {
            httpDateFmt = FastDateFormat.getInstance("EEE, dd MMM yyyy HH:mm:ss zzz",
                    TimeZone.getTimeZone("GMT"));
        }
        return httpDateFmt.format(new Date());
    }

    /**
     * Encrypt the data with the secret key.
     * @param data the data to encrypt
     * @return the encrypted data
     */
    private String encrypt(String data) {
        try {
            SecretKeySpec signingKey = new SecretKeySpec(secret.getBytes(), HMAC_SHA1);
            Mac mac = Mac.getInstance(HMAC_SHA1);

            mac.init(signingKey);

            byte[] rawHmac = mac.doFinal(data.getBytes());

            return new String(Base64.encodeBase64(rawHmac, true), UTF8);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Create a connection to the web service specified by the path, and supply authentication
     * credentials using the provided parameters.
     * @param servicePath path identifying the web service
     * @param method HTTP method
     * @param contentMD5 MD5 hash of the message content if PUT or POST,
     * empty string if GET or DELETE
     * @param contentType mime type of content if PUT or POST, empty string if GET or DELETE
     * @return a connection to the web service specified by the path
     * @throws Exception if something goes wrong
     */
    private HttpURLConnection signedRequest(String servicePath, String method, String contentMD5,
            String contentType) throws Exception {
        HttpURLConnection conn = (HttpURLConnection)
        new URL(rootURL + SERVICES + servicePath).openConnection();
        conn.setRequestMethod(method);
        Matcher m = SIMPLE_SERVICE_PATH_PATTERN.matcher(conn.getURL().toString());
        if (!m.matches()) {
            throw new IllegalStateException(conn.getURL() + " is not a valid web service URL.");
        }
        String path = m.group(1);

        String tstamp = httpTimestamp();
        String toSign = join("\n", method, contentMD5, contentType, tstamp, path);
        String sig = URLEncoder.encode(encrypt(toSign), UTF8);
        conn.setRequestProperty("authorization", "DATASHOP " + apiToken + ":" + sig);
        conn.setRequestProperty("date", tstamp);
        return conn;
    }

    /**
     * Signed connection to the service specified by path using HTTP GET method.
     * @param servicePath specifies the service to GET
     * @return a newly created, signed GET connection to the specified web service
     */
    public HttpURLConnection serviceGetConnection(String servicePath) {
        try {
            return signedRequest(servicePath, "GET", "", "");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Signed connection to the service specified by path with HTTP method PUT.
     * TODO this is a place holder for unit tests for now, but a real PUT method will need
     * a message payload to deliver
     * @param servicePath specifies the service to PUT
     * @return a newly created, signed PUT connection to the specified web service
     */
    public HttpURLConnection servicePutConnection(String servicePath) {
        try {
            return signedRequest(servicePath, "PUT", "", "");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Signed connection to the service specified by path with HTTP method POST.
     * @param servicePath specifies the service
     * @return a newly created, signed POST connection to the specified web service
     */
    public HttpURLConnection servicePostConnection(String servicePath) {
        try {
            return signedRequest(servicePath, "POST", "", "");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Print the results of calling the specified service to standard out.
     * @param path the path indicating which web service to call.
     * @param accept mime type we expect in return, such as text/xml
     * (should we allow multiple values here?)
     * @throws IOException thrown if there is a communications problem
     */
    public void printService(String path, String accept) throws IOException {
        HttpURLConnection conn = serviceGetConnection(path);
        conn.setRequestProperty("accept", accept);
        try {
            print(conn.getInputStream());
        } catch (IOException ioe) {
            print(conn.getErrorStream());
        }
    }

    /**
     * Get the results of calling the specified service as a string.
     * @param path the path indicating which web service to call.
     * @param accept mime type we expect in return, such as text/xml
     * (should we allow multiple values here?)
     * @return the results of calling the specified service as a string
     * @throws IOException thrown if there is a communications problem
     */
    public String getService(String path, String accept) throws IOException {
        HttpURLConnection conn = serviceGetConnection(path);

        conn.setRequestProperty("accept", accept);
        try {
            return printToString(conn.getInputStream());
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return printToString(conn.getErrorStream());
        }
    }
    
    public void getFileService(String path, String accept, String outputFileLoc) throws IOException {
            HttpURLConnection conn = serviceGetConnection(path);
            conn.setRequestProperty("accept", accept);
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {//not OK
                    InputStream is =  conn.getInputStream();
                    FileOutputStream out = new FileOutputStream(outputFileLoc);
                    int len = 0;
                    byte[] buffer = new byte[4096];
                    while((len = is.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                    }
                    out.flush();
                    out.close();
                    is.close();
                    System.out.println("Output zip file is: " + outputFileLoc);
            } else {
                    System.out.println("Error found: this workflow is \"" + conn.getResponseMessage() + "\"");
            }
            
            
            
    }

    /**
     * Get the results of calling the specified service as a string.
     * @param path the path indicating which web service to call.
     * @param postData the content
     * @param accept mime type we expect in return, such as text/xml
     * @return the results of calling the specified service as a string
     * @throws IOException thrown if there is a communications problem
     */
    public String getPostService(String path, String postData, String accept) throws IOException {
        HttpURLConnection conn = servicePostConnection(path);
        conn.setDoOutput(true);
        conn.setRequestProperty("accept", accept);
        conn.setRequestProperty("Accept-Charset", UTF8);
        conn.setRequestProperty("Content-Type",
                "application/x-www-form-urlencoded;charset=" + UTF8);
        URL url = new URL(rootURL + SERVICES + path);
        String query = url.getQuery();
        if (postData != null && !postData.trim().equals("")) {
            if (query != null && !query.equals("")) {
                query += "&";
            } else {
                query = "";
            }
            query += String.format("postData=%s", URLEncoder.encode(postData, UTF8));
        }
        if (query == null) {
                query = "";
        }
        OutputStream output = null;
        output = conn.getOutputStream();
        output.write(query.getBytes(UTF8));
        if (output != null) {
            output.close();
        }

        try {
            return printToString(conn.getInputStream());
        } catch (IOException ioe) {
            return printToString(conn.getErrorStream());
        }
    }
    
    /**
     * Get the results of calling the specified service as a string.
     * @param path the path indicating which web service to call.
     * @param postData the content
     * @param accept mime type we expect in return, such as text/xml
     * @return the results of calling the specified service as a string
     * @throws IOException thrown if there is a communications problem
     */
    public String getPostServiceWithFileAttachment(String path, String postData, File fileToAttach, String accept) throws IOException {
            //set request
            MultipartEntity multiPartEntity = new MultipartEntity();
            try {
                    if (postData != null)
                            multiPartEntity.addPart("postData", new StringBody(postData));
            } catch (UnsupportedEncodingException e) {
                logger.error(e.toString());
            }
            /* copied from servlet.workflows.MachineManager.java
             * Need to construct a FileBody with the file that needs to be
             * attached and specify the mime type of the file. Add the fileBody
             * to the request as an another part. This part will be considered
             * as file part and the rest of them as usual form-data parts
             */
            FileBody fileBody = new FileBody(fileToAttach, "application/octect-stream");
            multiPartEntity.addPart("attachment", fileBody);

            HttpURLConnection conn = servicePostConnection(path); 
        
            conn.setReadTimeout(READ_TIMEOUT_MAX);
            conn.setConnectTimeout(CONNECT_TIMEOUT_MAX);
        
            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestProperty("charset", "utf-8");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.addRequestProperty("Content-length", multiPartEntity.getContentLength() + "");
            conn.addRequestProperty(multiPartEntity.getContentType().getName(), multiPartEntity.getContentType().getValue());
            //conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + UTF8);
        
            conn.setRequestProperty("accept", accept);
            conn.setRequestProperty("Accept-Charset", UTF8);
            
            OutputStream os = null;

            //connection is open already
            //conn.connect();

            try {
                os = conn.getOutputStream();
                //multiPartEntity.writeTo(conn.getOutputStream());
                multiPartEntity.writeTo(os);
            } finally {
                os.close();
            }
            
            try {
            	return printToString(conn.getInputStream());
            } catch (IOException ioe) {
            	return printToString(conn.getErrorStream());
            }

    }

    /**
     * Print the entire contents of in to out.
     * @param in the input stream
     * @param out the output stream
     * @throws IOException if something goes wrong
     */
    protected void print(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[BUFFER_SIZE];
        int len;
        if (in == null) {
            System.err.println("InputStream is null.");
            return;
        }
        if (out == null) {
            System.err.println("OutputStream is null.");
            return;
        }

        while ((len = in.read(buf)) > 0) { out.write(buf, 0, len); }
    }

    /**
     * Print the entire contents of the input stream to standard out.
     * @param is the input stream
     * @throws IOException if something goes wrong
     */
    private void print(InputStream is) throws IOException { print(is, System.out); }

    /**
     * Return the entire contents of the input stream as a string.
     * @param is the input stream
     * @return the entire contents of the input stream as a string
     * @throws IOException if something goes wrong
     */
    protected String printToString(InputStream is) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        print(is, out);

        return out.toString("UTF-8");
    }

    /**
     * Hello World is our sanity check, to confirm that web services are up and running.
     * @throws Exception if something goes wrong
     */
    public void printHelloWorld() throws Exception {
        printService("/helloworld", "text/plain");
    }

    /**
     * Hello World is our sanity check, to confirm that web services are up and running.
     * @return "Hello World!" if all goes well
     * @throws Exception if something goes wrong
     */
    public String getHelloWorld() throws Exception {
        return getService("/helloworld", "text/plain");
    }

    /**
     * Convenience method for calling the workflow web service.
     * @param id the workflow id
     * @throws IOException thrown if there is a communications problem
     */
    public void printWorkflow(Integer id) throws IOException {
        printService("/workflows/" + id, "text/xml");
    }

    /**
     * Convenience method for calling the workflow web service.
     * @param id the workflow id
     * @return the XML data for the workflow as a string
     * @throws IOException thrown if there is a communications problem
     */
    public String getWorkflow(Integer id) throws IOException {
        return getService("/workflows/" + id, "text/xml");
    }
    
    /**
     * Convenience method for calling the workflow run web service.
     * @param id the workflow id
     * @return the XML message for the workflow as a string
     * @throws IOException thrown if there is a communications problem
     */
    public String runWorkflow(Integer id) throws IOException {
        return getService("/workflows/" + id + "/run", "text/xml");
    }
    
    /**
     * Convenience method for calling the workflow delete web service.
     * @param id the workflow id
     * @return the XML message for the workflow as a string
     * @throws IOException thrown if there is a communications problem
     */
    public String deleteWorkflow(Integer id) throws IOException {
        return getService("/workflows/" + id + "/delete", "text/xml");
    }
    
    /**
     * Convenience method for calling the workflow saveAsNew web service.
     * @param id the workflow id
     * @return the XML verbose description of the new workflow as a string
     * @throws IOException thrown if there is a communications problem
     */
    public String saveAsNewWorkflow(Integer id, String newWorkflowName, boolean isGlobal, String description) throws IOException {
        return getService("/workflows/" + id + "/save_as_new?new_workflow_name=" + URLEncoder.encode(newWorkflowName, "UTF-8")
                        + "&global=" + isGlobal + "&description=" + URLEncoder.encode(description, "UTF-8"), "text/xml");
    }
    
    /**
     * Convenience method for calling the workflows web service.
     * @throws IOException thrown if there is a communications problem
     */
    public void printWorkflows() throws IOException {
        printService("/workflows/", "text/xml");
    }

    /**
     * Convenience method for calling the workflows web service.
     * @return the XML data for the workflows as a string
     * @throws IOException thrown if there is a communications problem
     */
    public String getWorkflows() throws IOException {
        return getService("/workflows/", "text/xml");
    }

    /**
     * Convenience method for calling the workflow_files web service.
     * @param id the workflow id
     * @throws IOException thrown if there is a communications problem
     */
    public void getWorkflowFiles(Integer id, String zipFilePath) throws IOException {
        getFileService("/workflow_files/" + id, "application/zip", zipFilePath);
    }


    /**
     * Look for a file named learnsphere_webservices.properties in the current directory, if it exists,
     * and load properties from it.
     */
    private static void loadProperties() {
        String userDir = System.getProperty("user.dir");
        String filename = userDir + "/webservices.properties";
        File propsFile = new File(filename);

        if (propsFile.exists()) {
            try {
                System.getProperties().load(new FileInputStream(filename));
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
    
    /**
     * Run LearnSphereWebServiceClient from the command line.
     * @param args Arguments are alternating keys and values.  Valid keys are
     * apitoken - the user's public token
     * secret - the user's secret key
     * root - the root URL for the web service host
     * path - the path indicating which web service to call.
     * file - name of the file to upload to the server
     * ALTERNATIVE:  a single argument is taken to be a complete web services URL to call,
     * with the credentials provided in learnsphere_webservices.properties.
     */
    public static void main(String[] args) {
        String apiToken = null, secret = null, path = null, root = null;
        String inputFileName = null;
        loadProperties();

        // a single argument is taken to be a complete web services URL to call
        if (args.length == 1) {
            String serviceURL = args[0];
            Matcher m = SERVICE_URL_PATTERN.matcher(serviceURL);
            if (m.matches()) {
                root = m.group(1);
                path = m.group(2);
            } else {
                System.out.println("Invalid web services URL: " + serviceURL);
                System.exit(1);
            }
        } else {
            //when keys/values are passed in as arguments
            Map<String, String> argsMap = map((Object[])args);
            apiToken = argsMap.get("api.token");
            secret = argsMap.get("secret");
            root = argsMap.get("root");
            String servicePath = argsMap.get("path");
            if (servicePath != null) {
                    Matcher m = SERVICE_PATH_PATTERN.matcher(servicePath);
                    if (m.matches()) {
                        path = m.group(1);
                    } else {
                        System.out.println("Invalid web services path: " + servicePath);
                        System.exit(1);
                    }
            }
        }
        //if apioken and secret are not passed as arguments, get from properties file
        if (apiToken == null) { apiToken = System.getProperty("api.token"); }
        if (secret == null) { secret = System.getProperty("secret"); }
        if (apiToken == null) {
                System.out.println("No property or parameter found for api.token");
                System.exit(1);
        }
        if (secret == null) {
                System.out.println("No property or parameter found for secret");
                System.exit(1);
        }
        //root can be passed by argument
        if (root == null) {
            for (int i = 0; i < args.length; i++) {
                Matcher m = SERVICE_URL_PATTERN.matcher(args[i]);
                if (m.matches()) {
                    root = m.group(1);
                    path = m.group(2);
                }
            }
        }
        //root can be passed by properties file
        if (root == null) { root = System.getProperty("root"); }
        if (root == null) { root = SERVICES_ROOT; }
        //path can be passed as argument
        if (path == null) {
            for (int i = 0; i < args.length; i++) {
                Matcher m = SERVICE_PATH_PATTERN.matcher(args[i]);
                if (m.matches()) {
                    path = m.group(1);
                }
            }
        }
        //path can be passed from properties file
        if (path == null) {
                String servicePath = System.getProperty("path");
                if (servicePath != null) {
                        Matcher m = SERVICE_PATH_PATTERN.matcher(servicePath);
                        if (m.matches()) {
                            path = m.group(1);
                        }
                }
        }
        LearnSphereWebServicesClient client = new LearnSphereWebServicesClient(root, apiToken, secret);
        try {
            if (path == null) {
                client.printHelloWorld();
            } else if (Pattern.matches(".*/workflow_files/(\\d+)\\\\?.*", path)) {
                    System.out.println("getting workflow_files for path: " + path);
                    client.getFileService(path, "application/zip", "result.zip");
            } else if (Pattern.matches(".*/workflows/(\\d+)/files\\\\?.*", path)) {
                    System.out.println("getting workflow files for path: " + path);
                    client.getFileService(path, "application/zip", "result.zip");
            } else if (Pattern.matches(".*/workflows/(\\d+)/run",  path)
                            || Pattern.matches(".*/workflows/(\\d+)/delete", path)
                            || Pattern.matches(".*/workflows/(\\d+)/save_as_new.*", path)) {
                    client.printService(path, "text/xml");
            } else if (Pattern.matches(".*/workflows/(\\d+)/modify",  path)) {
                    if (args.length > 1)
                            inputFileName = args[args.length-1];
                    if (inputFileName == null) {
                            //possibly getting file name from build.properties
                            inputFileName = System.getProperty("file");
                    }
                    if (inputFileName != null) {
                            File inputFile = new File(inputFileName);
                            if (inputFile.isFile() && FilenameUtils.getExtension(inputFileName).equalsIgnoreCase("zip")) {
                                    System.out.println(client.getPostServiceWithFileAttachment(path, null, inputFile, "text/xml"));
                            } else {
                                    System.err.println("Error: invalid input file");
                                    System.exit(1);
                            }
                    }
            } else {
                client.printService(path, "text/xml");
            }
            
            
        } catch (Exception e) {
            System.err.println("There was a problem: " + e);
            e.printStackTrace();
        }
        
    }
}
