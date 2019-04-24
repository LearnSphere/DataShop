package edu.cmu.pslc.logging.distiller;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.util.VersionInformation;
import edu.cmu.pslc.importdata.ImportUtilities;

/**
 * Simple Java class to break apart the enormous VLAB logs for loading
 * to DataShop.
 * @author kcunning
 */
public class VLABDistiller {
    /** Tool name. */
    private static final String TOOL_NAME = "VLAB Distiller";
    /** Input file for processing. */
    private static String inputFileName;
    /** Verbose Flag */
    private boolean verbose = false;
    /** Magic 500. */
    private static final Integer FIVE_HUNDRED = new Integer(500);
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Simple constructor.
     */
    public VLABDistiller() { };

    /**
     * Handle the command line arguments.
     * @param args - command line arguments passed into main
     */
    protected void handleOptions(String[] args) {
        if (args.length == 0 || args == null) {
            logger.error("No arguments specified.");
            System.exit(-1);
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
            } else if (args[i].equals("-i") || args[i].equals("-inputFile")) {
                if (++i < args.length) {
                    inputFileName = args[i];
                    logger.debug("found the input file");
                } else {
                    logger.error("A file name must be specified with this argument");
                    System.exit(1);
                }
            } else if (args[i].equals("-v") || args[i].equals("-verbose")) {
                verbose = true;
                logger.info("VLAB Distiller running in verbose mode.");
            } else if (args[i].equals("-version")) {
                logger.info(VersionInformation.getReleaseString());
                System.exit(0);
            }
        } // end for loop
    } // end handleOptions

    /**
     * Display the usage of this utility.
     */
    public void displayUsage() {
        logger.info("USAGE: java -classpath ... "
                     + TOOL_NAME
                     + " [-inputFile input_file_name]");
        logger.info("Options:");
        logger.info("\t-i, -inputFile   \t The name of the input file");
        logger.info("\t-h, -help          \t Display this help and exit");
        logger.info("\t-v, -verbose       \t Run the tool in verbose mode");
        logger.info("\t-version          \t Display the version and exit");
        System.exit(-1);
    }

    /**
     * Take the input file and break into several smaller files.
     * @param inputFileName
     */
    private void process() {
        String fileNamePrefix = "VLAB_unknown_acid_study_";
        Integer fileNameSuffix = new Integer(1);
        BufferedReader reader = getFileToRead();

        // read 499 lines then write them to a file (499 to maintain xml pairings)
        List<String> buffer = new ArrayList<String>();
        Boolean keepReading = true;
        while (keepReading) {
            for (int counter = 0; counter < FIVE_HUNDRED; counter++) {
                try {
                    String line = reader.readLine();
                    if (line != null) {
                        buffer.add(line);
                        buffer.add("\n");
                    } else {
                        keepReading = false;
                    }
                } catch (IOException e) {
                    logger.info("Done reading.");
                }
            }
            writeNewFile(fileNamePrefix, fileNameSuffix, buffer);
            buffer.clear();
            fileNameSuffix++;
        } // end while(keepReading)
        logger.info("Created " + fileNameSuffix + " new files.");
        try {
            reader.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    } // end process()

    /**
     * Gets the file to read.
     * @return the buffered reader object
     */
    private BufferedReader getFileToRead() {
        BufferedReader inputReader = null;
        try {
            inputReader = ImportUtilities.handleInputFile(inputFileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return inputReader;
    }
    /**
     * Create the new file and write it to disk.
     * @param name the name of the file
     * @param suffix the suffix
     * @param buffer the list of strings
     */
    private void writeNewFile(String name, Integer suffix, List<String> buffer) {
        String xmlSuffix = ".log";
        String newFileName = "D:\\vlab_processed\\" + name + suffix + xmlSuffix;
        try {
            Writer outWriter =
                ImportUtilities.setupOutputFile(newFileName, false);
            for (String line : buffer) {
               outWriter.append(line);
            }
            outWriter.flush();
            outWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (verbose) {
            logger.info("Writing " + newFileName);
        }
    }

    /**
     * Main method.
     * @param args the arguments
     */
    public static void main(String[] args) {
        Logger logger = Logger.getLogger("VLABDistiller.main");
        String version = VersionInformation.getReleaseString();
        logger.info("VLABDistiller starting (" + version + ")...");
        VLABDistiller distiller = null;
        try {
            // create the XV, passing true to set metaFlag value to true
            distiller = new VLABDistiller();

            // parse command line options
            distiller.handleOptions(args);

            // perform schema and custom validation
            distiller.process();

        } catch (Throwable throwable) {
            logger.error("Unknown error occurred: " + throwable.getMessage(), throwable);
        }
        logger.info("VLABDistiller finished.");
    }

} // end VLABDistiller.java
