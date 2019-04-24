package edu.cmu.pslc.datashop.servlet.workflows;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.log4j.Logger;

public class MachineManager {

    private static final int READ_TIMEOUT_MAX = 3600000;
    private static final int CONNECT_TIMEOUT_MAX = 3600000;
    private HttpsURLConnection conn = null;
    private HttpURLConnection localhostConn = null;

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    public MachineManager() {

    }

    public static HttpParams getHttpParameters() {
        HttpParams httpParameters = new BasicHttpParams();
        int timeoutConnection = CONNECT_TIMEOUT_MAX;
        HttpConnectionParams.setConnectionTimeout(httpParameters,
                timeoutConnection);
        int timeoutSocket = READ_TIMEOUT_MAX;
        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
        return httpParameters;
    }

    private File readFileResponse(InputStream responseStream) {

        File testingFile = null;
        HttpClient client = new DefaultHttpClient(getHttpParameters());

        try {

            testingFile = File.createTempFile("wf_", ".zip");

            OutputStream outputStream = new FileOutputStream(testingFile);
            byte[] buffer = new byte[8 * 1024]; int bytesRead;
            while ((bytesRead = responseStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();

            if (testingFile  == null || !testingFile.exists() || !testingFile.isFile()) {
                logger.error("Could not complete response sequence. Bad file.");
            }

        } catch (IOException e) {
            logger.error(e.toString());
        }  finally {
            if (responseStream != null) {
                try {
                    responseStream.close();
                } catch (IOException e) {
                    logger.error(e.toString());
                }
            }
        }

        client.getConnectionManager().shutdown();
        return testingFile;
    }

    private File executeSecureRequest(String urlString, MultipartEntity reqEntity) {

        try {

            URL url = new URL(urlString);

            conn = (HttpsURLConnection) url.openConnection();



            //conn = (HttpsURLConnection) url.openConnection();
            conn.setReadTimeout(READ_TIMEOUT_MAX);
            conn.setConnectTimeout(CONNECT_TIMEOUT_MAX);
            conn.setRequestMethod("POST");

            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestProperty("charset", "utf-8");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.addRequestProperty("Content-length", reqEntity.getContentLength() + "");
            conn.addRequestProperty(reqEntity.getContentType().getName(), reqEntity.getContentType().getValue());

            OutputStream os = null;

            conn.connect();

            try {
                os = conn.getOutputStream();
                reqEntity.writeTo(conn.getOutputStream());
            } finally {
                os.close();
            }

            if (conn.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                return readFileResponse(conn.getInputStream());
            }

        } catch (IOException e) {
             logger.error(e.toString());
        } finally {

        }
        return null;
    }


    private File executeLocalhostRequest(String urlString, MultipartEntity reqEntity) {

        try {

            URL url = new URL(urlString);

            localhostConn = (HttpURLConnection) url.openConnection();



            //conn = (HttpsURLConnection) url.openConnection();
            localhostConn.setReadTimeout(READ_TIMEOUT_MAX);
            localhostConn.setConnectTimeout(CONNECT_TIMEOUT_MAX);
            localhostConn.setRequestMethod("POST");

            localhostConn.setUseCaches(false);
            localhostConn.setDoInput(true);
            localhostConn.setDoOutput(true);
            localhostConn.setRequestProperty("charset", "utf-8");
            localhostConn.setRequestProperty("Connection", "Keep-Alive");
            localhostConn.addRequestProperty("Content-length", reqEntity.getContentLength() + "");
            localhostConn.addRequestProperty(reqEntity.getContentType().getName(), reqEntity.getContentType().getValue());

            OutputStream os = null;

            localhostConn.connect();

            try {
                os = localhostConn.getOutputStream();
                reqEntity.writeTo(localhostConn.getOutputStream());
            } finally {
                os.close();
            }

            if (localhostConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return readFileResponse(localhostConn.getInputStream());
            }

        } catch (IOException e) {
             logger.error(e.toString());
        } finally {

        }
        return null;
    }

    /* A working test method follows (provided you have a LearnSphere Component Execution docker instance listening on localhost:9000) */
    /* DO NOT DELETE! */
    /*public static void main(String[] args) {
        String argDelim = "";
        String replaceChars = null;
        String delimReplace = null;
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.indexOf("win") >= 0) {
            argDelim = "\"";
            replaceChars = "\"";
            delimReplace = "\\\"";
        } else {
            replaceChars = "([\\s$'\"\\#\\[\\]!<>|;{}()~])";
            delimReplace = "$1";
        }

        String workflowDir = "C:/datashop/dataset_files/workflows/";
        String wfcDir = "C:/Users/mkomisin/git/WorkflowComponents/";
        String schemaFile = wfcDir + "AnalysisAfm/schema/AnalysisAFM_v1_0.xsd";
        String toolDir = wfcDir + "AnalysisAfm/";
        String userId = "mkomisin";
        List<String> inputComponentIds = new ArrayList<String>();
        inputComponentIds.add("Import-1-x823629");
        List<String> inputComponentFiles = new ArrayList<String>();
        inputComponentFiles.add("ds96_student_step_export_1.txt");
        HashMap<String, String> processBuilderParametersMap = new HashMap<String, String>();
        processBuilderParametersMap.put("componentXmlFile", argDelim + "C:/datashop/dataset_files/workflows/9/components/Analysis-1-x864419.xml" + argDelim);
        processBuilderParametersMap.put("workflowDir", argDelim + workflowDir + argDelim);
        processBuilderParametersMap.put("schemaFile", argDelim + schemaFile + argDelim);
        processBuilderParametersMap.put("toolDir", argDelim + toolDir + argDelim);
        File outputFile = new File(localPath + workflowId.toString() + "/" + componentId + nonce + ".zip");
        MachineManager mm = new MachineManager(); // OLD; call to this example needs updated
        mm.zipAndUpload(workflowDir, 10L, "Analysis-1-x782268", "C:/datashop/dataset_files/workflows/",
            "C:/Users/mkomisin/git/WorkflowComponents/", "123457", "C:/Users/mkomisin/git/WorkflowComponents/AnalysisAfm/dist/AnalysisAfm-1.0.jar",
                "mkomisin", inputComponentIds, inputComponentFiles, processBuilderParametersMap, "http://localhost:9000");
    }*/

    public File zipAndUpload(String localPath, Long workflowId, String componentId, String remoteData, String remoteWfc,
            String nonce, String jarFile, String userId, List<String> inputComponentIds,  List<String> inputComponentFiles,
            Map<String, String> processBuilderParametersMap, String remoteServer, Long heapSize, File outputFile, File resultsZipFile) {

        List<File> addedFiles = new ArrayList<File>();
        if (workflowId != null) {
            logger.debug("Loading remote component: " + componentId + " in workflow (" + workflowId + ")");

            FileOutputStream fos;
            try {
                fos = new FileOutputStream(outputFile);
                ZipOutputStream zos = new ZipOutputStream(fos);

                File componentDir = new File(localPath + workflowId.toString() + "/" + componentId);
                File componentXml = new File(localPath + workflowId.toString() + "/components/");

                addDirToZipArchive(zos, componentDir, null, componentId, inputComponentIds, inputComponentFiles);
                addDirToZipArchive(zos, componentXml, null, componentId, inputComponentIds, inputComponentFiles);

                for (String inputComponentId : inputComponentIds) {
                    String parentPath = localPath + workflowId.toString() + "/" + inputComponentId + "/output/";
                    File testInputDir = new File(parentPath);
                    if (testInputDir.exists() && testInputDir.isDirectory()) {
                        for (String inputFileName : inputComponentFiles) {
                            File testInputFile = new File(parentPath + inputFileName);

                            if (testInputFile.exists() && testInputFile.isFile() && !addedFiles.contains(testInputFile)) {
                                addDirToZipArchive(zos, testInputFile, inputComponentId + "/output", componentId, inputComponentIds, inputComponentFiles);
                                addedFiles.add(testInputFile);
                            }
                        }
                    }
                }
                zos.flush();
                fos.flush();
                zos.close();
                fos.close();
            } catch (FileNotFoundException e) {
                logger.error(e.toString());
            } catch (Exception e) {
                logger.error(e.toString());
            }

            // Now, upload the file with the necessary params

            if (outputFile != null && outputFile.exists() && outputFile.canRead()) {
                logger.debug("Uploading component data: " + componentId + " in workflow (" + workflowId + ")");
                resultsZipFile = uploadAndExecute(remoteServer, heapSize, outputFile, outputFile.getName(), "description text",
                    workflowId, componentId, remoteData, remoteWfc, nonce, jarFile, userId, processBuilderParametersMap, resultsZipFile);
            } else {
                logger.error("Unknown error in initExecution.");
            }
        }
        return resultsZipFile;
    }

    public File uploadAndExecute(String remoteServer, Long heapSize, File file, String fileName, String fileDescription,
            Long workflowId, String componentId, String remoteData, String remoteWfc,
            String nonce, String jarFile, String userId, Map<String, String> processBuilderParametersMap, File resultsZipFile) {
        resultsZipFile = executeMultiPartRequest(remoteServer, heapSize, file, fileDescription,
            workflowId, componentId, remoteData, remoteWfc, nonce, jarFile, userId, processBuilderParametersMap, resultsZipFile);

        if (conn != null) {
            conn.disconnect();
        }
        if (localhostConn != null) {
            localhostConn.disconnect();
        }
        if (resultsZipFile != null && resultsZipFile.exists() && resultsZipFile.isFile()) {
            logger.info("Returned results zip file: " + resultsZipFile.getAbsolutePath());
        } else {
            logger.error("No results returned: " + componentId + " in workflow (" + workflowId + ")");
        }
        return resultsZipFile;

    }

    public File executeMultiPartRequest(String remoteServer, Long heapSize, File file, String fileDescription,
            Long workflowId, String componentId, String remoteData, String remoteWfc,
            String nonce, String jarFile, String userId, Map<String, String> processBuilderParametersMap, File resultsZipFile) {

        MultipartEntity multiPartEntity = new MultipartEntity();
        try {

            String fileName = componentId + nonce;
            // The usual form parameters can be added this way
            multiPartEntity.addPart("fileDescription", new StringBody(fileDescription != null ? fileDescription : ""));
            multiPartEntity.addPart("fileName", new StringBody(fileName != null ? fileName : file.getName()));
            multiPartEntity.addPart("name", new StringBody("newfile"));
            multiPartEntity.addPart("workflowId", new StringBody(workflowId.toString()));
            multiPartEntity.addPart("componentId", new StringBody(componentId));
            multiPartEntity.addPart("remoteData", new StringBody(remoteData));
            multiPartEntity.addPart("remoteWfc", new StringBody(remoteWfc));
            multiPartEntity.addPart("nonce", new StringBody(nonce));
            multiPartEntity.addPart("jarFile", new StringBody(jarFile));
            multiPartEntity.addPart("userId", new StringBody(userId));
            multiPartEntity.addPart("heapSize", new StringBody(heapSize.toString()));
            for (String key : processBuilderParametersMap.keySet()) {
                multiPartEntity.addPart(key, new StringBody(processBuilderParametersMap.get(key)));
            }

            /*
             * Need to construct a FileBody with the file that needs to be
             * attached and specify the mime type of the file. Add the fileBody
             * to the request as an another part. This part will be considered
             * as file part and the rest of them as usual form-data parts
             */
            FileBody fileBody = new FileBody(file, "application/octect-stream");
            multiPartEntity.addPart("attachment", fileBody);

        } catch (UnsupportedEncodingException e) {
            logger.error(e.toString());
        }

        File returnFile = null;
        if (remoteServer.matches("https://.*")) {
            returnFile = executeSecureRequest(remoteServer, multiPartEntity);
        } else if (remoteServer.matches("http://localhost([:][0-9]+){0,1}[/]{0,1}.*") || remoteServer.matches("http://127.0.0.1([:][0-9]+){0,1}[/]{0,1}.*")) {
            returnFile = executeLocalhostRequest(remoteServer, multiPartEntity);
        } else {
            logger.error("Compute nodes require an HTTPS connection to the remote server "
                + " or an HTTP connection to localhost.");
        }

        // can be null if not https!

            // Delete the results file if it already exists.
            if (resultsZipFile != null && resultsZipFile.exists()) {
                resultsZipFile.delete();
                try {
                    resultsZipFile.createNewFile();
                } catch (IOException e) {
                    logger.error("Could not create new file " + resultsZipFile.getAbsolutePath());
                }
            }

            if (resultsZipFile != null && resultsZipFile.exists()
                    && returnFile != null && returnFile.exists() && returnFile.isFile()) {
                try {
                    FileUtils.copyFile(returnFile, resultsZipFile);
                } catch (IOException e) {
                    logger.error("Could not copy file " + returnFile.getAbsolutePath() + " to "
                        + resultsZipFile.getAbsolutePath());
                }
            } else {
                logger.error("Could not retrieve file from remote host " + returnFile.getAbsolutePath());
            }


        return resultsZipFile;
    }

    public static void addDirToZipArchive(ZipOutputStream zos, File fileToZip, String parrentDirectoryName, String componentId,
            List<String> inputComponentIds,  List<String> inputComponentFiles)
            throws Exception {
        if (fileToZip == null || !fileToZip.exists()) {
            return;
        }

        String zipEntryName = fileToZip.getName();
        if (parrentDirectoryName != null && !parrentDirectoryName.isEmpty()) {
            zipEntryName = parrentDirectoryName + "/" + fileToZip.getName();
        }

        if (inputComponentIds != null) {
            for (String inputComponentId : inputComponentIds) {

            }
        }

        if (fileToZip.isDirectory()) {

            if (fileToZip.getName().matches("components")) {
                for (File file : fileToZip.listFiles()) {
                    if (file.getName().matches("(?i)" + componentId + "\\.xml")
                            || file.getName().matches("(?i)" + componentId + "\\.xml.bak")) {
                        addDirToZipArchive(zos, file, zipEntryName, componentId, inputComponentIds, inputComponentFiles);
                    }
                }
            } else if (fileToZip.getName().matches("inputComponentIds")) {
                for (File file : fileToZip.listFiles()) {
                    if (file.getName().matches("(?i)" + componentId + "\\.xml")
                            || file.getName().matches("(?i)" + componentId + "\\.xml.bak")) {
                        addDirToZipArchive(zos, file, zipEntryName, componentId, inputComponentIds, inputComponentFiles);
                    }
                }
            } else {
                for (File file : fileToZip.listFiles()) {
                    addDirToZipArchive(zos, file, zipEntryName, componentId, inputComponentIds, inputComponentFiles);
                }
            }

        } else {

            byte[] buffer = new byte[1024];
            FileInputStream fis = new FileInputStream(fileToZip);
            zos.putNextEntry(new ZipEntry(zipEntryName));
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }
            zos.closeEntry();
            fis.close();
        }
    }

    public void cancel() {
        if (conn != null) {
            conn.disconnect();
        }
        if (localhostConn != null) {
            localhostConn.disconnect();
        }
    }




}
