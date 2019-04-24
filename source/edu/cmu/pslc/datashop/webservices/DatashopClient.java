/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.webservices;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.time.FastDateFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import static edu.cmu.pslc.datashop.util.StringUtils.join;
import static edu.cmu.pslc.datashop.util.CollectionUtils.map;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.util.DataShopInstance;

/**
 * Client for Datashop Web Services.  Provide methods for accessing each web service here.
 *
 * @author Jim Rankin
 * @version $Revision: 12983 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-03-14 15:13:11 -0400 (Mon, 14 Mar 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DatashopClient {
    /** Any URL for accessing a DataShop web service should match this pattern. */
    private static final Pattern SERVICE_URL_PATTERN =
        Pattern.compile("(.*)/services(.*)(\\?.*)?");
    /** extract everything after "services" and before "?" (the simple path) in a service URL */
    private static final Pattern SIMPLE_SERVICE_PATH_PATTERN =
        Pattern.compile(".*/services([^\\?]*)\\??.*");

    /** path indicating the web services root */
    private static final String SERVICES = "/services";
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

    /** URL prefix for web services */
    private String rootURL;

    /** public API token for authentication */
    private String apiToken;
    /** secret key for authentication */
    private String secret;

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());
    
    /**
     * Instantiates a Datashop client to be used for remote requests to master DataShop servers.
     * @return the web service client or null if any required properties are missing
     */
    public static DatashopClient getDatashopClientForRemoteRequests() {
        Boolean isSlave = DataShopInstance.isSlave();
        String masterUrl = DataShopInstance.getMasterUrl();
        String apiToken = DataShopInstance.getSlaveApiToken();
        String secret = DataShopInstance.getSlaveSecret();

        // Ensure this is a slave DataShop instance and in a state that is ready to make requests
        if ((isSlave != null && !isSlave) || masterUrl == null || apiToken == null || secret == null) {
            return null;
        }

        // Initialize and return a DataShop client ready for remote requests
        return new DatashopClient(masterUrl, apiToken, secret);
    }

    /**
     * Instantiates a Datashop client to be used for remote requests to master DataShop servers.
     * @return the web service client or null if any required properties are missing
     */
    public static DatashopClient getDatashopClientForLocalRequests() {
        String localUrl = "http://localhost:8080";
        String apiToken = DataShopInstance.getSlaveApiToken();
        String secret = DataShopInstance.getSlaveSecret();

        // Initialize and return a DataShop client ready for remote requests
        return new DatashopClient(localUrl, apiToken, secret);
    }

    /**
     * Constructor.
     * @param servicesRoot URL prefix for web services
     * @param apiToken public API token for authentication
     * @param secret secret key for authentication
     */
    public DatashopClient(String servicesRoot, String apiToken, String secret) {
        rootURL = servicesRoot;
        this.apiToken = apiToken;
        this.secret = secret;
    }

    /**
     * Constructor.  Root URL defaults to local host for testing.
     * @param apiToken public API token for authentication
     * @param secret secret key for authentication
     */
    public DatashopClient(String apiToken, String secret) {
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
     * Create a socket factory for the key store with the given name on the classpath.
     * (This is only used for the PSLC QA server, which does not have a publicly signed key.)
     * @param keystoreName name of the key store on the classpath
     * @param passwd the key store password
     * @return a SSL Socket Factory for the named key store
     */
    private SSLSocketFactory getKeystoreSocketFactory(String keystoreName, String passwd) {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            InputStream keystoreStream =
                getClass().getClassLoader().getResourceAsStream(keystoreName);

            keyStore.load(keystoreStream, passwd.toCharArray());

            TrustManagerFactory tmf =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

            tmf.init(keyStore);

            SSLContext ctx = SSLContext.getInstance("TLS");

            ctx.init(null, tmf.getTrustManagers(), null);

            return ctx.getSocketFactory();
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
     * @param contentType mime type of content
     * @return a newly created, signed POST connection to the specified web service
     */
    public HttpURLConnection servicePostConnection(String servicePath, String contentType) {
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

    /**
     * Get the results of calling the specified service as a string.
     * @param path the path indicating which web service to call.
     * @param postData the content
     * @param accept mime type we expect in return, such as text/xml
     * @return the results of calling the specified service as a string
     * @throws IOException thrown if there is a communications problem
     */
    public String getPostService(String path, String postData, String accept) throws IOException {
        HttpURLConnection conn = servicePostConnection(path, accept);
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
     * Convenience method for calling the dataset web service.
     * @param id the dataset id
     * @throws IOException thrown if there is a communications problem
     */
    public void printDataset(Integer id) throws IOException {
        printService("/datasets/" + id, "text/xml");
    }

    /**
     * Convenience method for calling the dataset web service.
     * @param id the dataset id
     * @return the XML data for the dataset as a string
     * @throws IOException thrown if there is a communications problem
     */
    public String getDataset(Integer id) throws IOException {
        return getService("/datasets/" + id, "text/xml");
    }
    
    /**
     * Convenience method for calling the dataset add web service.
     * @param name the dataset name
     * @param instanceId the remote instance id
     * @return the XML data for the dataset as a string
     * @throws IOException thrown if there is a communications problem
     */
    public String getDatasetAdd(String name, Long instanceId) throws IOException {
        String encodedName = URLEncoder.encode(name, UTF8);
        StringBuffer path = new StringBuffer();
        path.append("/datasets/add?name=").append(encodedName);
        path.append("&instanceId=").append(instanceId);
        return getService(path.toString(), "text/xml");
    }

    /**
     * Convenience method for calling the dataset delete web service.
     * @param id the dataset id
     * @return the XML data for the dataset as a string
     * @throws IOException thrown if there is a communications problem
     */
    public String getDatasetDelete(Integer id) throws IOException {
        return getService("/datasets/" + id + "/delete/", "text/xml");
    }

    /**
     * Convenience method for calling the dataset set web service.
     * @param id the dataset id
     * @param datasetInfo the meta-data for the dataset as XML
     * @return the XML data for the dataset as a string
     * @throws IOException thrown if there is a communications problem
     */
    public String getDatasetSet(Integer id, String datasetInfo) throws IOException {
        return getPostService("/datasets/" + id + "/set", datasetInfo, "text/xml");
    }

    /**
     * Convenience method for calling the discourse add web service.
     * @param name the discourse name
     * @param instanceId the remote instance id
     * @return the XML data for the discourse as a string
     * @throws IOException thrown if there is a communications problem
     */
    public String getDiscourseAdd(String name, Long instanceId) throws IOException {
        String encodedName = URLEncoder.encode(name, UTF8);
        StringBuffer path = new StringBuffer();
        path.append("/discourses/add?name=").append(encodedName);
        path.append("&instanceId=").append(instanceId);
        return getService(path.toString(), "text/xml");
    }

    /**
     * Convenience method for calling the discourse delete web service.
     * @param id the discourse id
     * @return the XML data for the discourse as a string
     * @throws IOException thrown if there is a communications problem
     */
    public String getDiscourseDelete(Long id) throws IOException {
        return getService("/discourses/" + id + "/delete/", "text/xml");
    }

    /**
     * Convenience method for calling the discourse set web service.
     * @param id the discourse id
     * @param discourseInfo the meta-data for the discourse as XML
     * @return the XML data for the discourse as a string
     * @throws IOException thrown if there is a communications problem
     */
    public String getDiscourseSet(Long id, String discourseInfo) throws IOException {
        return getPostService("/discourses/" + id + "/set", discourseInfo, "text/xml");
    }

    /**
     * Convenience method for calling the instance add web service.
     * @param name the instance name
     * @param url the DataShop URL for the instance
     * @return the XML data for the instance as a string
     * @throws IOException thrown if there is a communications problem
     */
    public String getInstanceAdd(String name, String url) throws IOException {
        String encodedName = URLEncoder.encode(name, UTF8);
        String encodedUrl = URLEncoder.encode(url, UTF8);
        StringBuffer path = new StringBuffer();
        path.append("/instances/add?name=").append(encodedName);
        path.append("&url=").append(encodedUrl);
        return getService(path.toString(), "text/xml");
    }

    /**
     * Convenience method for calling the instance set web service.
     * @param instanceId the remote instance id
     * @param name the instance name
     * @param url the DataShop URL for the instance
     * @return the XML data for the instance as a string
     * @throws IOException thrown if there is a communications problem
     */
    public String getInstanceSet(Long instanceId, String name, String url) throws IOException {
        String encodedName = URLEncoder.encode(name, UTF8);
        String encodedUrl = URLEncoder.encode(url, UTF8);
        StringBuffer path = new StringBuffer();
        path.append("/instances/").append(instanceId);
        path.append("/set?name=").append(encodedName);
        path.append("&url=").append(encodedUrl);
        return getService(path.toString(), "text/xml");
    }
    
     /**
     * Convenience method for calling the sample web service.
     * @param datasetId the dataset id
     * @param sampleId the sample id
     * @throws IOException thrown if there is a communications problem
     */
    public void printSample(Integer datasetId, Integer sampleId) throws IOException {
        printService("/datasets/" + datasetId + "/samples/" + sampleId, "text/xml");
    }

    /**
     * Convenience method for calling the sample web service.
     * @param datasetId the dataset id
     * @param sampleId the sample id
     * @return the XML data for the sample as a string
     * @throws IOException thrown if there is a communications problem
     */
    public String getSample(Integer datasetId, Integer sampleId) throws IOException {
        return getService("/datasets/" + datasetId + "/samples/" + sampleId, "text/xml");
    }

    /**
     * Look for a file named webservices.properties in the current directory, if it exists,
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
     * Run DatashopClient from the command line.
     * @param args Arguments are alternating keys and values.  Valid keys are
     * apitoken - the user's public token
     * secret - the user's secret key
     * root - the root URL for the web service host
     * path - the path indicating which web service to call.
     * file - name of the file to upload to the server
     * ALTERNATIVE:  a single argument is taken to be a complete web services URL to call,
     * with the credentials provided in webservices.properties.
     */
    public static void main(String[] args) {
        String apiToken = null, secret = null, path = null, root = null,
            eaFileName = null, cfDefFileName = null, kcmImportFileName = null,
            metricsFileName = null, datasetInfoFileName = null;
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
            Map<String, String> argsMap = map((Object[])args);

            apiToken = argsMap.get("apitoken");
            secret = argsMap.get("secret");
            root = argsMap.get("root");
            path = argsMap.get("path");
            eaFileName = argsMap.get("file");
            cfDefFileName = argsMap.get("file");
            kcmImportFileName = argsMap.get("file");
            metricsFileName = argsMap.get("file");
            datasetInfoFileName = argsMap.get("file");
        }
        if (apiToken == null) { apiToken = System.getProperty("api.token"); }
        if (secret == null) { secret = System.getProperty("secret"); }
        //root/path can be passed by argument
        if (root == null) {
            for (int i = 0; i < args.length; i++) {
                Matcher m = SERVICE_URL_PATTERN.matcher(args[i]);
                if (m.matches()) {
                    root = m.group(1);
                    path = m.group(2);
                }
            }
        }
        if (root == null) { root = System.getProperty("service.root"); }
        if (root == null) { root = SERVICES_ROOT; }
        //external analysis can be passed as argument
        if (eaFileName == null) {
            for (int i = 0; i < args.length; i++) {
                if (args[i].equalsIgnoreCase("file")
                        && args.length > (i + 1)) {
                    eaFileName = args[i + 1];
                    break;
                }
            }
        }
        if (eaFileName == null) { eaFileName = System.getProperty("file"); }

        //custom field definition xml file passed as argument
        if (cfDefFileName == null) {
                for (int i = 0; i < args.length; i++) {
                        if (args[i].equalsIgnoreCase("file")
                            && args.length > (i + 1)) {
                            cfDefFileName = args[i + 1];
                        break;
                    }
                }
            }
        if (cfDefFileName == null) {
                cfDefFileName = System.getProperty("file");
        }
        
        //kcm import tab-delimited file passed as argument
        if (kcmImportFileName == null) {
                for (int i = 0; i < args.length; i++) {
                        if (args[i].equalsIgnoreCase("file")
                            && args.length > (i + 1)) {
                                kcmImportFileName = args[i + 1];
                        break;
                    }
                }
            }
        if (kcmImportFileName == null) {
                kcmImportFileName = System.getProperty("file");
        }

        // Metrics Report tab-delimited file passed as argument
        if (metricsFileName == null) {
            for (int i = 0; i < args.length; i++) {
                if (args[i].equalsIgnoreCase("file") && args.length > (i + 1)) {
                    metricsFileName = args[i + 1];
                    break;
                }
            }
        }
        if (metricsFileName == null) {
            metricsFileName = System.getProperty("file");
        }

        // DatasetInfo XML file passed as argument
        if (datasetInfoFileName == null) {
            for (int i = 0; i < args.length; i++) {
                if (args[i].equalsIgnoreCase("file") && args.length > (i + 1)) {
                    datasetInfoFileName = args[i + 1];
                    break;
                }
            }
        }
        if (datasetInfoFileName == null) {
            datasetInfoFileName = System.getProperty("file");
        }

        if (apiToken == null) {
            System.out.println("No property or parameter found for api.token");
            System.exit(1);
        }
        if (secret == null) {
            System.out.println("No property or parameter found for secret");
            System.exit(1);
        }
        DatashopClient client = new DatashopClient(root, apiToken, secret);
        try {
            if (path == null) {
                client.printHelloWorld();
            } else {
                //for external analysis add, get file content
                if (Pattern.matches(".*/analyses/add\\?.*", path)
                                || Pattern.matches(".*/analyses/add", path)) {
                    if (eaFileName != null && !eaFileName.equals("")) {
                        StringBuilder sb = new StringBuilder();
                        try {
                            FileReader fr = new FileReader(eaFileName);
                            BufferedReader br = new BufferedReader(fr);
                            String strLine;
                            //Read File Line By Line
                            while ((strLine = br.readLine()) != null) {
                                sb.append(strLine);
                                sb.append(System.getProperty("line.separator"));
                            }
                            //Close readers
                            fr.close();
                            br.close();
                        } catch (Exception e) { //Catch exception if any
                            System.err.println("Error: " + e.getMessage());
                            System.exit(1);
                        }
                        System.out.print(client.getPostService(path, sb.toString(),
                        "text/xml"));
                    } else {
                        System.out.print(client.getPostService(path, "", "text/xml"));
                    }
                } else if (Pattern.matches(".*/customfields/add\\?.*", path)
                        || Pattern.matches(".*/customfields/add", path)
                        || Pattern.matches(".*/customfields/(\\d+)/set", path)) {
                    if (cfDefFileName != null && !cfDefFileName.equals("")) {
                        StringBuilder sb = new StringBuilder();
                        try {
                            FileReader fr = new FileReader(cfDefFileName);
                            BufferedReader br = new BufferedReader(fr);
                            String strLine;
                            //Read File Line By Line
                            while ((strLine = br.readLine()) != null) {
                                sb.append(strLine);
                                sb.append(System.getProperty("line.separator"));
                            }
                            //Close readers
                            fr.close();
                            br.close();
                        } catch (Exception e) { //Catch exception if any
                            System.err.println("Error: " + e.getMessage());
                            System.exit(1);
                        }
                        System.out.print(client.getPostService(path, sb.toString(),
                                "text/xml"));
                    } else {
                        System.out.println("Please specify a file name with the "
                                           + "'file' argument");
                    }
                } else if (Pattern.matches(".*/importkcm/?", path)) {
                        if (kcmImportFileName != null && !kcmImportFileName.equals("")) {
                                StringBuilder sb = new StringBuilder();
                                try {
                                    FileReader fr = new FileReader(kcmImportFileName);
                                    BufferedReader br = new BufferedReader(fr);
                                    String strLine;
                                    //Read File Line By Line
                                    while ((strLine = br.readLine()) != null) {
                                        sb.append(strLine);
                                        sb.append(System.getProperty("line.separator"));
                                    }
                                    //Close readers
                                    fr.close();
                                    br.close();
                                } catch (Exception e) { //Catch exception if any
                                    System.err.println("Error: " + e.getMessage());
                                    System.exit(1);
                                }
                                System.out.print(client.getPostService(path, sb.toString(), "text/xml"));
                        } else {
                                System.out.println("Please specify a file name with the "
                                                   + "'file' argument");
                        }
                } else if (Pattern.matches(".*/instances/(\\d+)/metrics/set", path)) {
                    if (metricsFileName != null && !metricsFileName.equals("")) {
                        StringBuilder sb = new StringBuilder();
                        try {
                            FileReader fr = new FileReader(metricsFileName);
                            BufferedReader br = new BufferedReader(fr);
                            String strLine;
                            //Read File Line By Line
                            while ((strLine = br.readLine()) != null) {
                                sb.append(strLine);
                                sb.append(System.getProperty("line.separator"));
                            }
                            //Close readers
                            fr.close();
                            br.close();
                        } catch (Exception e) { //Catch exception if any
                            System.err.println("Error: " + e.getMessage());
                            System.exit(1);
                        }
                        System.out.print(client.getPostService(path, sb.toString(), "text/xml"));
                    } else {
                        System.out.println("Please specify a file name with the "
                                           + "'file' argument");
                    }
                } else if (Pattern.matches(".*/datasets/(\\d+)/set", path)) {
                    if (datasetInfoFileName != null && !datasetInfoFileName.equals("")) {
                        StringBuilder sb = new StringBuilder();
                        try {
                            FileReader fr = new FileReader(datasetInfoFileName);
                            BufferedReader br = new BufferedReader(fr);
                            String strLine;
                            //Read File Line By Line
                            while ((strLine = br.readLine()) != null) {
                                sb.append(strLine);
                                sb.append(System.getProperty("line.separator"));
                            }
                            //Close readers
                            fr.close();
                            br.close();
                        } catch (Exception e) { //Catch exception if any
                            System.err.println("Error: " + e.getMessage());
                            System.exit(1);
                        }
                        System.out.print(client.getPostService(path, sb.toString(), "text/xml"));
                    } else {
                        System.out.println("Please specify a file name with the "
                                           + "'file' argument");
                    }
                } else if (Pattern.matches(".*/learningcurves/classify(\\?)?(.)*", path)){
                        client.printService(path, "text/plain");
                } else if (Pattern.matches(".*/datasets/add.*",  path)
                        || Pattern.matches(".*/datasets/add/\\?.*", path)) {
                    System.out.println(client.getService(path, "text/xml"));
                } else {
                    client.printService(path, "text/xml");
                }
            }
        } catch (Exception e) {
            System.err.println("There was a problem: " + e);
            e.printStackTrace();
        }
    }
}
