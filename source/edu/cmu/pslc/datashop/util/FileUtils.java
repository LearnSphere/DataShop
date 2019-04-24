/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2008
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;

/**
 * This class provides utility methods useful when creating directories
 * on the file system as well as storing files.
 *
 * @author Kyle A Cunningham
 * @version $Revision: 15837 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2019-02-01 09:41:47 -0500 (Fri, 01 Feb 2019) $
 * <!-- $KeyWordsOff: $ -->
 *
 */
public final class FileUtils {
    /** Logger for this class. */
    private static Logger logger = Logger.getLogger(FileUtils.class);
    /** Lookup service for users and groups. */
    private static UserPrincipalLookupService userPrincipalLookupService =
            FileSystems.getDefault().getUserPrincipalLookupService();

    /** Delimiter used in DataShop MySQL stored procedures. */
    private static final String MYSQL_SP_DELIMITER = "$$";
    /** Delimiter string constant. */
    private static final String DELIMITER_STRING = "DELIMITER";
    /** Semicolon constant. */
    private static final String SEMI_COLON = ":";
    /** End of sql file delimiter constant. */
    private static final String END_OF_FILE_DELIMITER = DELIMITER_STRING + " ;";
    /** Deterministic string constant. */
    private static final String DETERMINISTIC_STRING = "DETERMINISTIC";
    /** Invoker string constant. */
    private static final String INVOKER_STRING = "INVOKER";
    /** Permissions required for DataShop-related files in POSIX symbolic notation. */
    public static final String FILE_PERMISSIONS = "rw-rw-r--";
    /** Permissions required for DataShop-related directories in POSIX symbolic notation. */
    public static final String DIRECTORY_PERMISSIONS = "rwxrwxr-x";
    /** Group required for DataShop-related files or directories. */
    public static final String DATASHOP_GROUP = "datashop";


    /**
     * Private constructor as this is a utility class.
     */
    private FileUtils() { }

    /** Regular expression to find KC Model headers. */
    private static final String UNACCEPTABLE_CHARS_REGEX =
            Pattern.compile("[^a-z^A-Z^_^0-9]",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE).toString();

    /**
     * Cleans the string to be used in a file name or directory name.
     * @param toBeCleaned the string to clean.
     * @return the cleaned string.
     */
    public static String cleanForFileSystem(String toBeCleaned) {
        StringBuffer cleanedStringBuffer = new StringBuffer();
        String[] cleanedStringSplit = toBeCleaned.split(UNACCEPTABLE_CHARS_REGEX);

        for (int position = 0; position < cleanedStringSplit.length; position++) {
            if (!cleanedStringSplit[position].equals("")) {
                cleanedStringBuffer.append(cleanedStringSplit[position]);
                if ((position + 1) < cleanedStringSplit.length) {
                    cleanedStringBuffer.append("_");
                }
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Original string :: " + toBeCleaned);
            logger.debug("Cleaned string :: " + cleanedStringBuffer.toString());
        }
        return cleanedStringBuffer.toString();
    }

    /**
     * Looks up the group that should own any files created by DataShop.
     * @return the group principal
     * @throws IOException
     */
    public static GroupPrincipal getDataShopFileGroup() throws IOException {
        return DATASHOP_GROUP == null ? null
                : userPrincipalLookupService.lookupPrincipalByGroupName(DATASHOP_GROUP);
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
     * Create a temporary file with a randomly generated name.
     * @return a temporary file.
     * @throws IOException IOException.
     */
    public static File createTemporaryFile() throws IOException {
        File tempFile = null;
        Random rand = new Random();
        String temporaryFileName = new Integer(rand.nextInt()).toString();
        tempFile = File.createTempFile(temporaryFileName, null);
        tempFile.deleteOnExit();
        return tempFile;
    }

    /**
     * Dumps the current textBuffer to a temporary file to save on space.
     * @param textBuffer the text to write to the file.
     * @param tempFile the temporary file to write to.
     * @param overwriteFlag flag indicating if the file should be overwritten or
     *         appended to.
     * @throws IOException IOException.
     */
    public static void dumpToFile(CharSequence textBuffer, File tempFile, boolean overwriteFlag)
            throws IOException {
        BufferedWriter out = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(tempFile, overwriteFlag), "UTF8"));
        out.append(textBuffer);
        out.close();
    }

    /**
     * This was useful for me in debugging, and might be useful again,
     * so leaving in for now.
     * @param logger a logger
     * @param f a file we want to dump
     * @throws IOException better indicator of success
     */
    public static void dumpFile(Logger logger, File f) throws IOException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(f));
            String line;

            logger.debug("dumping file: " + f);
            while ((line = reader.readLine()) != null) {
                logger.debug(line);
            }
        } catch (IOException io) {
            logger.debug("couldn't find " + f);
        } finally {
            if (reader != null) { reader.close(); }
        }
    }

    /**
     * Dumps the current textBuffer to a temporary file to save on space.
     * @param textBuffer the text to write to the file.
     * @param tempFile the temporary file to write to.
     * @param length the number of bytes to write.
     * @param overwriteFlag flag indicating if the file should be overwritten or
     *         appended to.
     * @throws IOException IOException.
     */
    public static void dumpToFile(byte[] textBuffer, int length, File tempFile,
            boolean overwriteFlag) throws IOException {
        BufferedOutputStream out = new BufferedOutputStream(
                new FileOutputStream(tempFile, overwriteFlag));
        out.write(textBuffer, 0, length);
        out.close();
    }

    /**
     * Copies one file to the other.
     * @param in the file to copy
     * @param out the new file
     * @return true if successful, false otherwise
     * @throws IOException better indicator of success
     */
    public static boolean copyFile(File in, File out)  throws IOException  {
        boolean successFlag = false;
        FileInputStream inputStream = new FileInputStream(in);
        FileOutputStream outputStream = new FileOutputStream(out);
        FileChannel inChannel = inputStream.getChannel();
        FileChannel outChannel = outputStream.getChannel();
        try {
            //inChannel.transferTo(0, inChannel.size(), outChannel);
            final long size = inChannel.size();
            long position = 0;
            long numBytes = 1000000;
            while (position < size) {
                position += outChannel.transferFrom(inChannel, position, numBytes);
            }
            successFlag = true;
        }  catch (IOException exception) {
            throw exception;
        } finally {
            if (inChannel != null) { inChannel.close(); }
            if (outChannel != null) { outChannel.close(); }
            if (outputStream != null) { outputStream.close(); }
            if (inputStream != null) { inputStream.close(); }
            System.gc();
        }
        return successFlag;
    }

    /** buffer size used by copyStream. */
    private static final int BUFFER_SIZE = 18024;

    /**
     * Write the contents of in into out.
     * @param in input
     * @param out output
     * @throws IOException if something goes wrong
     */
    public static void copyStream(InputStream in, OutputStream out) throws IOException {
        try {
            byte[] buf = new byte[BUFFER_SIZE];
            int len;

            while ((len = in.read(buf)) > 0) { out.write(buf, 0, len); }
        } finally {
            in.close();
            out.close();
        }
    }

    /**
     * Takes the given file path, opens the file, and replaces all occurrences of the
     * provided sequence with the replacement string.
     * @param filePath the path to the file to open and process.
     * @param toReplace the character sequence to replace.
     * @param toInsert the character sequence to insert.
     * @return a List containing a series of SQL statements to be executed in order, or, if trouble
     *      reading the file, null.
     * @throws IOException if file at filePath does not exist
     */
    public static synchronized List<String> openAndReplaceSequence(String filePath,
            String toReplace, String toInsert) throws IOException {
        return openAndReplaceSequence(filePath, toReplace, toInsert, null);
    }

    /**
     * Takes the given file path, opens the file, and replaces all occurrences of the
     * provided sequence with the replacement string.
     * NOTE:  This method depends on the DELIMITER used in the MySQL stored procedure files.
     * @param filePath the path to the file to open and process.
     * @param toReplace the character sequence to replace.
     * @param toInsert the character sequence to insert.
     * @param delimiter the delimiter string used throughout the .sql file.  Will default to "$$"
     *          if none is supplied.
     * @return a List containing a series of SQL statements to be executed in order, or, if trouble
     *      reading the file, null.
     * @throws IOException if file at filePath does not exist
     */
    public static synchronized List<String> openAndReplaceSequence(String filePath,
            String toReplace, String toInsert, String delimiter) throws IOException {
        if (delimiter == null) {
            delimiter = MYSQL_SP_DELIMITER;
        }
        if (filePath == null) {
            throw new FileNotFoundException("filePath is null.");
        }
        List<String> sqlBuffer = new ArrayList<String>();
        BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath));
        String line;
        boolean endOfFile = false;

        while ((line = bufferedReader.readLine()) != null) {
            // continue to read until we reach a delimiter.  Then we know we've
            // found a chunk of text to be executed.
            while (!line.endsWith(delimiter)) {
                String tempLine = bufferedReader.readLine();
                if (tempLine == null) {
                    endOfFile = true;
                    break;
                } else {
                    line += tempLine;
                }
            }
            // this indicates the end of the file.
            if (!line.equals(END_OF_FILE_DELIMITER)) {
                sqlBuffer.add(line.replaceAll(toReplace, toInsert)
                        .replaceAll(DETERMINISTIC_STRING, DETERMINISTIC_STRING + " ")
                        .replaceAll(INVOKER_STRING, INVOKER_STRING + " ")
                        .replace(SEMI_COLON, "")
                        .replace(DELIMITER_STRING, "")
                        .replace(delimiter, ""));
            }
            if (endOfFile) {
                break;
            }
        }
        bufferedReader.close();

        return sqlBuffer;
    }

    /**
     * Count the number of lines in file.
     * @param file the file
     * @return the number of lines in file
     * @throws IOException better indicator of success
     */
    public static int countLines(File file) throws IOException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            int i = 0;

            while (reader.readLine() != null) { i++; }

            return i;
        } catch (IOException e) {
            logger.error("error opening file: " + file);
            return -1;
        } finally {
            if (reader != null) { reader.close(); }
        }
    }

    /**
     * Count the number of lines in file.
     * @param filePath the name of the file
     * @return the number of lines in file
     * @throws IOException better indicator of success
     */
    public static int countLines(String filePath) throws IOException {
        return countLines(new File(filePath));
    }

    /**
     * Transform an input stream into an iterator over the text lines in the stream.
     * Memory consumption is limited to just the current line being read.
     * IMPORTANT USAGE NOTE: Must iterate over all lines from beginning to end.  Otherwise,
     * the stream will not be properly closed.
     * @param is the stream to iterate over
     * @return an iterator over the text lines in the stream
     */
    public static Iterable<String> lineIter(final InputStream is) {
        final Iterator<String> iter = new Iterator<String>() {
            private BufferedReader rdr = null;
            private String line = null;
            private boolean readNext = true;

            /**
             * Get the next line if available, close the reader and input stream if we have
             * reached the end.
             */
            private void nextLine() {
                try {
                    if (rdr == null) {
                        rdr = new BufferedReader(new InputStreamReader(is));
                    }
                    if (readNext) { line = rdr.readLine(); }
                } catch (Exception e) {
                    line = null;
                }
            }

            public boolean hasNext() { nextLine(); readNext = false; return line != null; }

            public String next() { nextLine(); readNext = true; return line; }

            public void remove() { throw new UnsupportedOperationException(); }
        };

        return new Iterable<String>() { public Iterator<String> iterator() { return iter; } };
    }

    /**
     * Renames one file to the other.
     * If the new file is used by an existing file, then replace the file
     * and delete the file to be renamed.
     * @param fileToBeRenamed the file to be renamed
     * @param file the new file
     * @return true if successful, false otherwise
     * @throws IOException better indicator of success
     */
    public static boolean renameFile(File fileToBeRenamed, File file)  throws IOException  {
        boolean successFlag = false;
        if (fileToBeRenamed.exists()) {
            if (file.exists() && file.delete()) {
                if (fileToBeRenamed.renameTo(file)) {
                  successFlag = true;
                  fileToBeRenamed.delete();
                }
            }
        }
        return successFlag;
    }

    /**
     * Delete the specified File from the file system given the file name only.
     * @param fileName the name of the file to be deleted
     * @return true if successful, false otherwise
     */
    public static boolean deleteFile(String fileName) {
        File file = new File(fileName);
        return deleteFile(file);
    }

    /**
     * Delete the specified File from the file system. This will
     * perform a recursive delete if specified File is a directory.
     * @param fileToBeDeleted the file to be deleted
     * @return true if successful, false otherwise
     */
    public static boolean deleteFile(File fileToBeDeleted) {

        if (!fileToBeDeleted.exists()) { return false; }

        boolean successFlag = true;
        if (fileToBeDeleted.isDirectory()) {
            for (File f : fileToBeDeleted.listFiles()) {
                successFlag = successFlag && deleteFile(f);
            }
        }

        return successFlag && fileToBeDeleted.delete();
    }

    /**
     * Based solely on the file contentType, determine if this is a zip file.
     * @param contentType the MIME content-type
     * @return flag indicating if File is a zip file
     */
    public static boolean isZipFile(String contentType) {
        // .zip, .bz2
        if (contentType.indexOf("zip") > 0) {
            return true;
        }
        // .gz
        if (contentType.indexOf("octet-stream") > 0) {
            return true;
        }
        return false;
    }

    /** Constant. */
    private static final int BUFFER = 2048;
    /** Constant. */
    private static final int EXT_LEN = 4;

    /**
     * Extracts the contents of a zip file into a new folder.
     * @param zipFileName the name of the zip file
     * @throws IOException an IOException
     * @return returns the name of the folder this method created
     */
    public static String extractFolder(String zipFileName) throws IOException {
        File file = new File(zipFileName);

        // Get the name of a the new directory from the zip file name
        ZipFile zip = new ZipFile(file);
        String folder = zipFileName.substring(0, zipFileName.length() - EXT_LEN);
        // Create the new directory
        new File(folder).mkdir();

        // Loop through the zip entries
        Enumeration zipFileEntries = zip.entries();
        while (zipFileEntries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();

            String currentEntry = entry.getName();
            File destFile = new File(folder, currentEntry);
            File destinationParent = destFile.getParentFile();

            // create the parent directory structure if needed
            if (destinationParent.mkdirs()) {
                FileUtils.makeWorldReadable(destinationParent);
            }

            if (!entry.isDirectory()) {
                BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry));
                int currentByte;
                // establish buffer for writing file
                byte[] data = new byte[BUFFER];

                // write the current file to disk
                FileOutputStream fos = new FileOutputStream(destFile);
                BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);

                // read and write until last byte is encountered
                while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
                    dest.write(data, 0, currentByte);
                }
                dest.flush();
                dest.close();
                is.close();
            }
        }
        return folder;
    }

    /**
     * Utility to strip path from file name.
     * @param fileName the full file name
     * @return the stripped-down version of the file name
     */
    public static String removePathFromFileName(String fileName) {
        String fileNameNoPath = fileName;

        if (fileName != null) {
            // Because of file-system goofiness, we can have both
            // types of slashes in the path. Sigh.
            int lastIndex = 0;
            int lastIndexForward = fileName.lastIndexOf("/");
            int lastIndexBackward = fileName.lastIndexOf("\\");
            lastIndex =
                (lastIndexForward > lastIndexBackward) ? lastIndexForward : lastIndexBackward;
            if (lastIndex > 0) {
                fileNameNoPath = fileName.substring(lastIndex + 1);
            }
        }

        return fileNameNoPath;
    }

    public static void deleteDirectoryRecursively(String directoryPath) throws IOException {
        Path directory = Paths.get(directoryPath);
        File testFile = new File(directoryPath);
        if (testFile != null && testFile.exists() && testFile.isDirectory()) {
            Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
               @Override
               public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                   Files.delete(file);
                   return FileVisitResult.CONTINUE;
               }

               @Override
               public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                   Files.delete(dir);
                   return FileVisitResult.CONTINUE;
               }
            });
        }

    }

    /**
     * Update file permissions using the specified chmod command.
     * @param theFile the File to be modified
     * @param chmodCommand a String indicating how to run chmod
     * @return flag indicating if command was successful
     */
    public static boolean updateFilePermissions(File theFile, String chmodCommand) {
        boolean permissionsSet = true;
        try {
            String osName = System.getProperty("os.name").toLowerCase();
            if (osName.indexOf("win") >= 0) {
                logger.trace("Windows detected. No attempt to change dir permissions.");
            } else {
                Process p = null;
                try {
                    p = Runtime.getRuntime().exec(chmodCommand + " " + theFile.getCanonicalPath());
                    p.waitFor();
                } catch (InterruptedException ie) {
                    logger.error("Process failed to execute while assigning directory permissions.");
                    permissionsSet = false;
                }
                if (p != null && p.exitValue() == 0) {
                    logger.trace("Successfully changed the newly uploaded"
                                 + " created dir's permissions for MySQL.");
                }
            }
        } catch (IOException e) {
            logger.error("Exception caused by assigning directory permissions.");
            permissionsSet = false;
        }

        return permissionsSet;
    }

    /**
     * Update file permissions using the specified chmod command.
     * @param theFile the File to be modified
     * @param chmodCommand a String indicating how to run chmod
     * @return flag indicating if command was successful
     */
    public static Boolean makeWorldReadable(File theFile) {
        Boolean permissionsSet = false;
        String chmodFile = "chmod 664";
        String chmodDirectory = "chmod 775";

        if (theFile != null && theFile.exists()) {

            String chmodCommand = null;
            if (theFile.isFile()) {
                chmodCommand = chmodFile;
            } else if (theFile.isDirectory()) {
                chmodCommand = chmodDirectory;
            }

            permissionsSet = updateFilePermissions(theFile, chmodCommand);
        }

        return permissionsSet;
    }

    /**
     * Generate a truncated version of the file, returning a file with only
     * the first N specified lines.
     * @param inputFile the File
     * @param numLines the number of lines in truncated file
     */
    public static File truncateFile(File inputFile, int numLines) {
        File shortFile = null;
        try {
            LineIterator it = org.apache.commons.io.FileUtils.lineIterator(inputFile, null);

            String shortFileName = "head_" + inputFile.getName();
            shortFile = new File(inputFile.getParent(), shortFileName);

            if (it != null) {
                List<String> lines = new ArrayList<String>();
                try {
                    int count = 0;
                    while (it.hasNext() && (count++ <= numLines)) {
                        lines.add(it.nextLine());
                    }
                } finally {
                    it.close();
                }

                org.apache.commons.io.FileUtils.writeLines(shortFile, null, lines);
                updateFilePermissions(shortFile, "chmod 664");
            }
        } catch (Exception e) {
            logger.error("Failed to create truncated file." + e);
            shortFile = null;
        }

        return shortFile;
    }

} // end FileUtils.java
