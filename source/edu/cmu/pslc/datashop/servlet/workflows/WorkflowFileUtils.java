package edu.cmu.pslc.datashop.servlet.workflows;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MimeTypes;

import edu.cmu.pslc.datashop.util.FileUtils;
import edu.cmu.pslc.datashop.workflows.WorkflowItem;

public class WorkflowFileUtils {

    public static final int IS_READER_BUFFER = 8192;

    public static final int OS_WRITER_BUFFER = 8192;
    static final int ZIP_INPUT_STREAM_MAX_BYTES = 1024;
    public static final int ZIP_BUFFER_SIZE = 2048;

    private static final Detector DETECTOR = new DefaultDetector(MimeTypes.getDefaultMimeTypes());

    /** Permissions required for DataShop-related files in POSIX symbolic notation. */
    public static final String FILE_PERMISSIONS = "rw-rw-r--";

    /** Permissions required for DataShop-related directories in POSIX symbolic notation. */
    public static final String DIRECTORY_PERMISSIONS = "rwxrwxr-x";

    /** Group required for DataShop-related files or directories. */
    public static final String DATASHOP_GROUP = "datashop";

    final static int[] ILLEGAL_FILENAME_CHARS = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
            10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26,
            27, 28, 29, 30, 31, 34, 42, 47, 58, 60, 62, 63, 92, 124 };

    /** Debug logging. */
    private static Logger staticLogger = Logger.getLogger(WorkflowFileUtils.class.getName());

    /**
     * Replace backslashes with forward slashes since forward slashes work on
     * windows and linux with fewer delimiter issues. To keep this fast, no file
     * checks should be done.
     *
     * @param path
     *            the file or directory path
     * @return the sanitized file or directory path
     */
    public static String sanitizePath(String path) {

        if (path != null) {
            String osName = System.getProperty("os.name").toLowerCase();

            String newPath = path;
            // First replace delimited spaces if there are any
            if (osName.indexOf("win") < 0) {
                newPath = newPath.replaceAll("\\ ", " ");
            }
            // Next, always use forward slashes for file/dir paths
            newPath = newPath.replaceAll("\\\\", "/").replaceAll("//", "/").replaceAll("\\.\\./", "");

            if (osName.indexOf("win") < 0) {
                // Linux does not accept spaces without backslash
                newPath = newPath.replaceAll(" ", "\\ ");
            } else {

            }
            return newPath;
        }
        return null;
    }

    public static String htmlEncode(String text) {
	    if (text != null) {
	        text = StringEscapeUtils.escapeXml(text);
	    }
	    // can be null
	    return text;
	}

	public static String htmlDecode(String text) {
	    if (text != null) {
	        if (text != null) {
	            text = StringEscapeUtils.unescapeXml(text);
	        }
	    }
	    // can be null
	    return text;
	}

	/**
	 * Removes characters that are troublesome for file systems.
	 * @param filepath the filepath
	 * @return the filepath without troublesome characters
	 */
	public static String removeIllegalChars(String filepath) {
	     StringBuffer newFilenameBuffer = new StringBuffer();
	     if (filepath != null) {
	         for (int i = 0; i < filepath.length(); i++) {
	             int charInt = (int)filepath.charAt(i);
	             if (Arrays.binarySearch(ILLEGAL_FILENAME_CHARS, charInt) < 0) {
	                 newFilenameBuffer.append((char)charInt);
	             }
	
	         }
	     }
	     return newFilenameBuffer.toString();
	 }

	public static String getStrictDirFormat(String fileDir) {
	    if (fileDir != null) {
	        fileDir = WorkflowFileUtils.sanitizePath(fileDir);
	
	        Integer lastIndexOfSlash = fileDir.lastIndexOf("/");
	        if (lastIndexOfSlash != fileDir.length() - 1) {
	            fileDir = fileDir + "/";
	        }
	    }
	    return fileDir;
	}

	public static Boolean matchesIgnoreCase(String str, String pattern) {
	 Boolean matches = false;
	 Pattern inputTagPattern = Pattern.compile(pattern,
	         Pattern.CASE_INSENSITIVE);
	 Matcher matcher  = inputTagPattern.matcher(str);
	 if (matcher.matches()) {
	     matches = true;
	 }
	 return matches;
	}

	public static String getMimeType(File file) throws IOException {
	    TikaInputStream tikaIS = null;
	
	    // Check the file extension. Some files give the detector issues so try
	    // to weed them out early
	    if (file != null) {
	        String fileExtension = FilenameUtils.getExtension(file.getAbsolutePath());
	        if (fileExtension.equals("ods")) {
	            return "file";
	        }
	    }
	
	    // The file is not of the types we hard coded, so use a detector to
	    // determine its type
	
	    try {
	        if (file != null) {
	            tikaIS = TikaInputStream.get(file.toPath());
	
	            final Metadata metadata = new Metadata();
	            // metadata.set(Metadata.RESOURCE_NAME_KEY, file.getName());
	
	            return DETECTOR.detect(tikaIS, metadata).toString();
	        }
	    } finally {
	        if (tikaIS != null) {
	            tikaIS.close();
	        }
	    }
	    return null;
	}

	/**
     * Returns the string of the given file's text or an empty string if it
     * could not be read.
     *
     * @param filePath
     *            the file
     * @return the string of the given file's text or an empty string if it
     *         could not be read.
     */
    public static String readFile(String filePath) {
        filePath = filePath.replaceAll("[/]+", "/");
        staticLogger.info("Reading " + filePath);

        InputStream inputStream = null;
        StringBuffer sBuffer = new StringBuffer();
        // Setup the readers.
        Path path = null;
        try {

            path = Paths.get(filePath);

            staticLogger.info("File absolute path: " + path.toAbsolutePath());

            inputStream = new FileInputStream(path.toFile());
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8),
                    IS_READER_BUFFER);

            String line = null;
            while ((line = br.readLine()) != null) {
                sBuffer.append(line);
            }

            br.close();

        } catch (FileNotFoundException e) {
            staticLogger.error("File not found: " + filePath);
        } catch (IOException e) {
            staticLogger.error("IOException while reading: " + filePath);
        }
        return sBuffer.toString();
    }

    public static Boolean writeFile(String filePath, String content) {

        Boolean success = false;
        OutputStream outputStream = null;
        BufferedWriter br = null;
        // Setup the writer.
        File file = new File(WorkflowFileUtils.sanitizePath(filePath));
        try {
            outputStream = new FileOutputStream(file);
            br = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), OS_WRITER_BUFFER);
            br.append(content);
            br.close();
            success = true;

        } catch (IOException e) {
            staticLogger.error("IOException while reading: " + filePath);
            success = false;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                }
            }
        }
        return success;
    }

    /**
     * Helper method to test file constraints uploaded file.
     *
     * @param uploadFileItem
     *            the uploaded file
     * @return errorMessage indicating error, if any
     */
    public static synchronized Boolean checkFileIsSupported(org.apache.commons.fileupload.FileItem uploadFileItem) {

        Boolean isValid = true;

        // Todo: In-place dummy method for an anti-virus scan on uploads.
        // Changing it affects the file uploads.

        return isValid;
    }

    /**
     * Extracts a zip entry (file entry)
     *
     * @param zipIn
     * @param filePath
     * @throws IOException
     */
    private static void extractFile(ZipInputStream zipIn, File file) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
        byte[] bytesIn = new byte[ZIP_BUFFER_SIZE];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }

    public static File compressFile(String componentOutputDir, File file) {
        File zipFile = null;
        if (file != null && file.exists() && file.canRead()) {
            List<File> fileList = new ArrayList<File>();
            fileList.add(file);
            zipFile = compressFiles(componentOutputDir, fileList);
        }
        return zipFile;
    }

    public static File compressFiles(String componentOutputDir, List<File> zipEntryList) {

        File newZipFile;
        try {
            newZipFile = File.createTempFile("zip_", ".zip", new File(componentOutputDir));

            staticLogger.debug("Creating zip file: " + newZipFile);
        } catch (IOException e) {
            staticLogger.error(e.toString());
            return null;
        }

        BufferedInputStream origin = null;
        FileOutputStream dest = null;
        ZipOutputStream out = null;
        try {
            dest = new FileOutputStream(newZipFile);
            out = new ZipOutputStream(new BufferedOutputStream(dest));

            // Add all input files to the export zip
            for (File inputFile : zipEntryList) {

                if (inputFile != null) {

                    // out.setMethod(ZipOutputStream.DEFLATED);
                    byte data[] = new byte[ZIP_BUFFER_SIZE];

                    FileInputStream fi = new FileInputStream(inputFile);
                    origin = new BufferedInputStream(fi, ZIP_BUFFER_SIZE);
                    ZipEntry entry = new ZipEntry(inputFile.getName());
                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0, ZIP_BUFFER_SIZE)) != -1) {
                        out.write(data, 0, count);
                    }

                    origin.close();
                }
            }
            out.close();

        } catch (IOException exception) {
            staticLogger.error(exception.toString());

        } catch (Exception exception) {
            staticLogger.error(exception.toString());

        } finally {
            if (origin != null) {
                try {
                    origin.close();
                } catch (IOException e) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }

        }

        return newZipFile;
    }

    /**
     * Unzips the first file entry from the zip into firstFile and returns its
     * simple name.
     *
     * @param zipFile
     *            the zip
     * @param firstFile
     *            the destination of the first file entry
     * @return the simple name
     * @throws IOException
     */
    public static String unzipFile(File zipFile, File firstFile) throws IOException {

        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFile));
        ZipEntry entry = zipIn.getNextEntry();
        // iterates over entries in the zip file
        String simpleName = null;
        while (entry != null) {

            if (!entry.isDirectory()) {
                // if the entry is a file, extracts it
                extractFile(zipIn, firstFile);
                simpleName = new File(entry.getName()).getName();
                break;
            } else {
                // if the entry is a directory, try the next file
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
        return simpleName;
    }

    public static void gzipFile(String filePath, String destPath) {

        byte[] buffer = new byte[1024];

        try {

            FileOutputStream fileOutputStream = new FileOutputStream(destPath);
            GZIPOutputStream gzipOutputStream = new GZIPOutputStream(fileOutputStream);
            FileInputStream fileInput = new FileInputStream(filePath);
            Integer bytes_read = null;
            while ((bytes_read = fileInput.read(buffer)) > 0) {
                gzipOutputStream.write(buffer, 0, bytes_read);
            }

            fileInput.close();
            gzipOutputStream.finish();
            gzipOutputStream.close();
        } catch (IOException ex) {
            staticLogger.error("Could not gzip file " + filePath + " to destination " + destPath);
        }
    }

    public static void unGunzipFile(String filePath, String destPath) {

        byte[] buffer = new byte[1024];

        try {
            FileInputStream fileIn = new FileInputStream(filePath);
            GZIPInputStream gZIPInputStream = new GZIPInputStream(fileIn);
            FileOutputStream fileOutputStream = new FileOutputStream(destPath);
            Integer bytes_read = null;
            while ((bytes_read = gZIPInputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, bytes_read);
            }

            gZIPInputStream.close();
            fileOutputStream.close();
            System.out.println("The file was decompressed successfully!");

        } catch (IOException ex) {
            staticLogger.error("Could not gunzip file " + filePath + " to destination " + destPath);
        }
    }

    /**
     * Unzips the contents of a file into unzipFolder and returns the directory as a File object.
     * @param zippedFile the zipped file
     * @param unzipFolder files are extracted to this folder
     * @return the folder as a File object
     * @throws IOException IO exception
     */
    public static File unzipFileToDirectory(File zippedFile, String unzipFolder) throws IOException {
        File newFolder = new File(unzipFolder);
        if (newFolder.exists() && newFolder.isDirectory()
                && zippedFile.exists() && zippedFile.isFile()) {
            byte[] buffer = new byte[1024];
            ZipInputStream zis = new ZipInputStream(new FileInputStream(zippedFile));
            ZipEntry zipEntry = zis.getNextEntry();
            while(zipEntry != null){
                String fileName = zipEntry.getName();
                if (fileName.matches("__.*")) {
                    // Skill annoying __MACOSX crap.
                    zipEntry = zis.getNextEntry();                    
                    continue;
                }
                File newFile = new File(unzipFolder + "/" + fileName);
                File parentDir = (new File(unzipFolder + "/" + fileName)).getParentFile();
                if (!parentDir.exists()) {
                    if (!parentDir.mkdirs()) {
                        System.err.println("Cannot create directory: " + parentDir.toString());
                    }
                }
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
        }

        if (!newFolder.exists() || !newFolder.canRead() || !newFolder.isDirectory()) {
            newFolder = null;
        }
        return newFolder;
    }
    public static void uncompressTar(File tarFile, File dest) throws IOException {
        dest.mkdir();
        TarArchiveInputStream tarIn = null;

        tarIn = new TarArchiveInputStream(
                    new GzipCompressorInputStream(
                        new BufferedInputStream(
                            new FileInputStream(
                                tarFile
                            )
                        )
                    )
                );

        TarArchiveEntry tarEntry = tarIn.getNextTarEntry();
        // tarIn is a TarArchiveInputStream
        while (tarEntry != null) {// create a file with the same name as the tarEntry
            File destPath = new File(dest, tarEntry.getName());

            if (tarEntry.isDirectory()) {
                destPath.mkdirs();
            } else {

                if (!destPath.getParentFile().exists()) {
                    destPath.getParentFile().mkdirs();
                }

                destPath.createNewFile();
                //byte [] btoRead = new byte[(int)tarEntry.getSize()];
                byte [] btoRead = new byte[1024];
                //FileInputStream fin
                //  = new FileInputStream(destPath.getCanonicalPath());
                BufferedOutputStream bout =
                    new BufferedOutputStream(new FileOutputStream(destPath));
                int len = 0;

                while((len = tarIn.read(btoRead)) != -1)
                {
                    bout.write(btoRead,0,len);
                }

                bout.close();
                btoRead = null;

            }
            tarEntry = tarIn.getNextTarEntry();
        }
        tarIn.close();
    }

    public static void bunzip2(String zipFilePath, String outputTarPath) throws IOException {
        FileInputStream in = new FileInputStream(zipFilePath);
        FileOutputStream out = new FileOutputStream(outputTarPath);
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(in);
        final byte[] buffer = new byte[1024];
        int n = 0;
        while (-1 != (n = bzIn.read(buffer))) {
          out.write(buffer, 0, n);
        }
        out.close();
        bzIn.close();
    }


/**
 * Get this workflow's relative base directory, e.g. workflows/ID/
 * @return the workflow's base directory or null if worklowId is null
 */
public static String getWorkflowsDir(Long workflowId) {
    String path = null;
    if (workflowId != null) {
        path = "workflows/" + workflowId.toString() + "/";
    }
    return path;
}

/**
 * Delete a workflow directory recursively.
 * @param workflowItem
 * @throws IOException
 */
public static void deleteWorkflowDir(WorkflowItem workflowItem, String dataFilesDirectory) throws IOException {
    String baseDir = getStrictDirFormat(dataFilesDirectory);
    File workflowDir = new File(baseDir + "/" + getWorkflowsDir((Long) workflowItem.getId()));
    if (workflowDir != null && workflowDir.exists() && workflowDir.isDirectory()) {
        String directoryPath = workflowDir.getAbsolutePath();
        FileUtils.deleteDirectoryRecursively(directoryPath);
    }
}
    /**
     * Creates a new directory with DataShop permissions applied.
     * Parent directories are created too if necessary.
     * On non-POSIX systems, the new directories are created but no permissions are applied.
     * @param path the path of the new directory.
     * @throws IOException
     */
    public static void createDirectoriesWithPermissions(Path path) throws IOException {
        Path absolutePath = path.toAbsolutePath();
        Path rootPath = absolutePath.getRoot();

        // Ignore POSIX permissions on non-UNIX systems
        if (!SystemUtils.IS_OS_UNIX) {
            Files.createDirectories(absolutePath);
            return;
        }

        // Create any new directories in the path and apply DataShop permissions to each
        for (int i = 0; i < path.getNameCount(); i++) {
            Path partialPath = rootPath.resolve(path.subpath(0, i + 1));
            if (!Files.exists(partialPath)) {
                Files.createDirectories(partialPath);
                applyDataShopPermissions(partialPath);
            }
        }
    }


    /**
     * Looks up the group that should own any files created by DataShop.
     * @return the group principal
     * @throws IOException
     */
    public static GroupPrincipal getDataShopFileGroup() throws IOException {
        return FileSystems.getDefault().getUserPrincipalLookupService()
            .lookupPrincipalByGroupName(DATASHOP_GROUP);
    }


    /**
     * Gets the permissions that should be applied to directories created by DataShop.
     * @return the permission set
     */
    public static Set<PosixFilePermission> getDataShopDirectoryPermissions() {
        return DIRECTORY_PERMISSIONS == null ? null
                : PosixFilePermissions.fromString(DIRECTORY_PERMISSIONS);
    }


    /**
     * Gets the permissions that should be applied to files created by DataShop.
     * @return the permission set
     */
    public static Set<PosixFilePermission> getDataShopFilePermissions() {
        return FILE_PERMISSIONS == null ? null : PosixFilePermissions.fromString(FILE_PERMISSIONS);
    }


    /**
     * Applies the appropriate DataShop group and permissions to the specified path.
     * @param path a path that should be owned by DataShop
     * @throws IOException
     */
    public static void applyDataShopPermissions(Path path) throws IOException {
        // Retrieve a POSIX attribute view for the provided path (null on non-POSIX systems)
        PosixFileAttributeView attributeView =
                Files.getFileAttributeView(path, PosixFileAttributeView.class);
        if (attributeView == null) {
            return;
        }

        // Update the file or directory's group
        GroupPrincipal group = getDataShopFileGroup();
        if (group != null) {
            attributeView.setGroup(group);
        }

        // Update the file or directory's permissions
        Set<PosixFilePermission> permissions = Files.isDirectory(path) ?
                getDataShopDirectoryPermissions() : getDataShopFilePermissions();
        if (permissions != null) {
            attributeView.setPermissions(permissions);
        }
    }


    /**
     * Creates a new file with write permissions to be used by the workflow component.
     * The name contains the prefix and the suffix (extension).
     *
     * Note: this method "cleans" the prefix and suffix, substituting underscore for
     * any non-alphanumeric characters... except underscore, backslash, dash and period.
     *
     * @param prefix the prefix
     * @param suffix the suffix (extension)
     * @return the new file with write permissions
     */
    public static File createFile(String componentOutputDir, String prefix, String suffix) {

        prefix = prefix.replaceAll("[^a-zA-Z0-9_\\-.]+", "_");
        suffix = suffix.replaceAll("[^a-zA-Z0-9_\\-.]+", "_");
        File newFile = new File(componentOutputDir + prefix + suffix);

            try {
                newFile.createNewFile();
            } catch (IOException e) {
                staticLogger.error(e.toString());
            }

        if (!newFile.getParentFile().canWrite()) {
            staticLogger.error("Cannot write to file " + newFile.getAbsolutePath());
            newFile = null;
        }



        return newFile;
    }

    /**
     * Creates a new file with write permissions to be used by the workflow component.
     *
     * Note: this method does not do any cleaning of the file name and will cause an
     * error if used with a filename containing a forward slash (/).
     * DO NOT CHANGE THIS BEHAVIOR AS SOME COMPONENTS RELY ON THE LACK OF CLEANING!
     *
     * @param fileName the name of the file to create
     * @return the new file with write permissions
     */
    public static File createFile(String componentOutputDir, String fileName) {

        File newFile = new File(componentOutputDir + fileName);

            try {
                newFile.createNewFile();
            } catch (IOException e) {
                staticLogger.error(e.toString());
            }

        if (!newFile.getParentFile().canWrite()) {
            staticLogger.error("Cannot write to file " + newFile.getAbsolutePath());
            newFile = null;
        }

        return newFile;
    }

}
