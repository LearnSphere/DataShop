/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.logging.distiller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.cmu.pslc.logging.ContextMessage;
import edu.cmu.pslc.logging.FileLogger;
import edu.cmu.pslc.logging.Message;
import edu.cmu.pslc.logging.OliDatabaseLogger;
import edu.cmu.pslc.logging.OliDiskLogger;
import edu.cmu.pslc.logging.ToolMessage;
import edu.cmu.pslc.logging.TutorMessage;
import edu.cmu.pslc.logging.element.CustomFieldElement;
import edu.cmu.pslc.logging.element.DatasetElement;
import edu.cmu.pslc.logging.element.LevelElement;
import edu.cmu.pslc.logging.element.MetaElement;
import edu.cmu.pslc.logging.element.ProblemElement;
import edu.cmu.pslc.logging.element.SkillElement;
import edu.cmu.pslc.logging.util.DateTools;

/**
 * A distiller for what we generally refer to as "Hao's Data".
 * This is Area Unit of the Geometry curriculum from around 1996 or 1997.
 * This program is expecting a tab delimited file with a strict format.
 *
 * @author Alida Skogsholm
 * @version $Revision: 10730 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2014-03-05 16:33:25 -0500 (Wed, 05 Mar 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class GeometryDistiller {
    /** Determines whether to log to a plain XML file. */
    private static final boolean PLAIN_XML_LOGGING = true;
    /** Determines whether to log to OLI log database. */
    private static final boolean OLI_DATABASE_LOGGING = false;
    /** Determines whether to log to an OLI log file. */
    private static final boolean OLI_DISK_LOGGING = false;

    /** Directory for plain XML files. */
    private static final String PLAIN_XML_DIR = "log/plain/";
    /** Directory for OLI XML within XML files. */
    private static final String OLI_XML_DIR = "log/oli/";

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * This is old data so let's log it for around that time.
     */
    private static final int START_YEAR = 1996;

    /** Default file name to read in and distill. */
    private static final String DEFAULT_FILE_NAME = "all_header.txt";
    /** Default curriculum name. */
    private static final String DEFAULT_CURRICULUM = "Geometry-Area-96-97";
    /** Default time zone. */
    private static final String DEFAULT_TIME_ZONE = "US/Eastern";
    /** Default source for the OLI log action source column. */
    private static final String DEFAULT_SOURCE = "GEOMETRY_TUTOR";

    /** Name of the textbook skill model. */
    private static final String TEXTBOOK_SKILL_MODEL = "Textbook";
    /** Name of the original skill model. */
    private static final String ORIGINAL_SKILL_MODEL = "Original";
    /** Name of the original skill model. */
    private static final String AREA_SKILL_MODEL = "Area";
    /** Area Formula skill of Area Skill Model. */
    private static final String AREA_SKILL = "Area formula";
    /** Non-Area Formula skill of Area Skill Model. */
    private static final String NON_AREA_SKILL = "Non-area formula";
    /** Name of the original skill model. */
    private static final String GEOMETRY_SKILL_MODEL = "Geometry";
    /** Name of any unknown factors, probably never used. */
    private static final String UNKNOWN_FACTOR = "Unknown Factor";
    /** Value for the action evaluation string for correct. */
    private static final String CORRECT_EVAL = "CORRECT";
    /** Value for the action evaluation string for incorrect. */
    private static final String INCORRECT_EVAL = "INCORRECT";

    /** No action. */
    private static final String NO_ACTION = "";
    /** No input. */
    private static final String NO_INPUT = "";

    /** The column number in the input file for the success flag. */
    private static final int SUCCESS_FLAG_FIELD = 0;
    /** The column number in the input file for the student name. */
    private static final int STUDENT_FIELD = 1;
    /** The column number in the input file for the problem name. */
    private static final int PROBLEM_FIELD = 2;
    /** The column number in the input file for the subgoal name. */
    private static final int SUBGOAL_FIELD = 3;
    /** The column number in the input file for the duration. */
    private static final int DURATION_FIELD = 4;
    /** The column number in the input file for the skill. */
    private static final int SKILL_TEXT_BOOK_FIELD = 5;
    /** The column number in the input file for the original skill. */
    private static final int SKILL_ALT_FIELD = 6;
    /** The column number in the input file for where the factors start. */
    private static final int FACTORS_START_FIELD = 7;
    /** The minimum number of fields in a row of the raw data. */
    private static final int MIN_NUM_FIELDS = FACTORS_START_FIELD;

    /** Need a default place to start the time. */
    private static final GregorianCalendar GREG_START = new GregorianCalendar(1996, 1, 1, 0, 0);

    /** Indicate whether to log to the production server or not.  Set on command line. */
    private boolean prodFlag = false;

    /** The file name. */
    private String fileName;
    /** The curriculum name. */
    private String curriculumName;
    /** List of factor names from the first row in the input file. */
    private List factorNames = new ArrayList();
    /** Logging utility which logs to a plain XML file. */
    private FileLogger fileLogger = null;
    /** Logging utility which logs directly to an OLI log database. */
    private OliDatabaseLogger oliDatabaseLogger = null;
    /** Logging utility which logs to an OLI XML within XML file. */
    private OliDiskLogger oliDiskLogger = null;

    /** Default constructor. */
    public GeometryDistiller() {
        this(DEFAULT_FILE_NAME,
             DEFAULT_CURRICULUM);

    }

    /**
     * Constructor with needed information given.
     * @param theFileName the file name
     * @param theCurriculumName the curriculum name
     */
    public GeometryDistiller(String theFileName,
                             String theCurriculumName) {
        this.fileName = theFileName;
        this.curriculumName = theCurriculumName;
    }

    /**
     * Set the action logger to do stream logging and not do disk logging
     * and do production mode if specified on command line.
     * Maybe the other flags should be set on the command line as well.
     */
    private void setUpLogging() {
        if (PLAIN_XML_LOGGING) {
            // Create a directory; all non-existent ancestor directories are
            // automatically created
            boolean success = (new File(PLAIN_XML_DIR)).mkdirs();
            if (!success) {
                // Directory creation failed
                logger.warn("Failed to create directory: " + PLAIN_XML_DIR);
            }
        }

        if (OLI_DATABASE_LOGGING) {
            if (prodFlag) {
                logger.info("Logging to production server.");
                oliDatabaseLogger = OliDatabaseLogger.create(true);
            } else {
                logger.info("Logging to QA server.");
                oliDatabaseLogger = OliDatabaseLogger.create(false);
            }
        }

        if (OLI_DISK_LOGGING) {
            oliDiskLogger = OliDiskLogger.create(OLI_XML_DIR + "GeoOliLog.xml");
        }

    }

    /**
     * Set the action logger to do stream logging and not do disk logging
     * and do production mode if specified on command line.
     * Maybe the other flags should be set on the command line as well.
     */
    private void closeLogging() {
        if (fileLogger != null) {
            fileLogger.close();
        }
        if (OLI_DATABASE_LOGGING) {
            oliDatabaseLogger.close();
        }
        if (OLI_DISK_LOGGING) {
            oliDiskLogger.close();
        }

    }

    /**
     * Logs a session for the OLI loggers but starts a new file
     * for the plain file logger.
     * @param userId the user id
     * @param sessionId the session id
     */
    private void logSession(String userId, String sessionId) {
        if (PLAIN_XML_LOGGING) {
            if (fileLogger != null) {
                fileLogger.close();
            }
            fileLogger = FileLogger.create(PLAIN_XML_DIR + "log_" + userId + ".xml");
        }
        if (oliDatabaseLogger != null) {
            oliDatabaseLogger.logSession(userId, sessionId);
        }
        if (oliDiskLogger != null) {
            oliDiskLogger.logSession(userId, sessionId);
        }
    }

    /**
     * Log the given message with the given timestamp to the logger(s)
     * which have already been setup.
     * @param message the message to log
     * @param time the time stamp to use
     */
    private void log(Message message, Date time) {
        if (fileLogger != null) {
            logger.debug("Logging to plain file");
            fileLogger.log(message, time);
        }
        if (oliDatabaseLogger != null) {
            logger.debug("Logging to oli db");
            oliDatabaseLogger.log(message, time);
        }
        if (oliDiskLogger != null) {
            logger.debug("Logging to oli file");
            oliDiskLogger.log(message, time);
        }
    }

    /**
     * Read and parse the input file, then take that data and log actions.
     */
    public final void justDoIt() {
        logger.info("Reading and parsing file");
        List rawDataList = readAndParseFile();

        logger.info("Do something cool");
        doSomethingCool(rawDataList);
    }

    /**
     * Read the data from the file, parse it and return a list
     * of RawData objects.
     * @return list of type RawData (inner class)
     */
    public List readAndParseFile() {
        List rawDataList = new ArrayList();
        boolean firstRow = true;
        try {
            logger.info("Reading File: " + fileName);
            logger.info("Curriculum: " + curriculumName);
            File inputFile = new File(fileName);
            FileReader fileReader = new FileReader(inputFile);
            BufferedReader bufReader = new BufferedReader(fileReader);

            String success;
            String student;
            String problem;
            String subgoal;
            String duration;
            String skillTextBook;
            String skillOriginal;

            String line = null;
            while ((line = bufReader.readLine()) != null) {
                String[] result = line.split("\t");

                if (result.length < MIN_NUM_FIELDS) {
                    continue;
                }

                success = result[SUCCESS_FLAG_FIELD];
                success = success.trim();
                logger.debug("success = " + success);

                student = result[STUDENT_FIELD];
                student = student.trim();
                logger.debug("student = " + student);

                problem = result[PROBLEM_FIELD];
                problem = problem.trim();
                logger.debug("problem = " + problem);

                subgoal = result[SUBGOAL_FIELD];
                subgoal = subgoal.trim();
                logger.debug("subgoal = " + subgoal);

                duration = result[DURATION_FIELD];
                duration = duration.trim();
                logger.debug("duration = " + duration);

                skillTextBook = result[SKILL_TEXT_BOOK_FIELD];
                skillTextBook = skillTextBook.trim();
                logger.debug("skillTextBook = " + skillTextBook);

                skillOriginal = result[SKILL_ALT_FIELD];
                skillOriginal = "ALT:" + skillOriginal.trim();
                logger.debug("skillOriginal = " + skillOriginal);

                if (firstRow) {
                    for (int idx = FACTORS_START_FIELD; idx < result.length; idx++) {
                        String factorName =  new String(result[idx]).trim();
                        factorNames.add(factorName);
                    }
                    firstRow = false;

                } else {

                    List factorList = new ArrayList();
                    for (int idx = FACTORS_START_FIELD; idx < result.length; idx++) {
                        factorList.add((new String(result[idx])).trim());
                    }

                    RawData rawData = new RawData(
                            success, student, problem, subgoal,
                            duration,
                            skillTextBook, skillOriginal,
                            factorList);

                    rawDataList.add(rawData);
                }

            } // end while more lines to read

            bufReader.close();

        } catch (FileNotFoundException exception) {
            exception.printStackTrace();
            logger.warn(exception.getMessage());
        } catch (IOException exception) {
            exception.printStackTrace();
            logger.warn(exception.getMessage());
        }

        return rawDataList;
    } // end readAndParseFile method

    /**
     * Take the list of raw data and log student actions and tutor responses.
     * @param rawDataList the list of raw data items directly from the input file
     */
    private void doSomethingCool(List rawDataList) {
        String currStudent = "";
        String prevStudent = currStudent;

        String currProblem = "";
        String prevProblem = currProblem;

        String sessionId = "";

        String currStep = "";
        Set<String> stepNameSet = new HashSet<String>();

        ContextMessage contextMsg = null;
        ToolMessage toolMsg = null;
        TutorMessage tutorMsg = null;

        GregorianCalendar theGC = GREG_START;
        Date timeStamp = theGC.getTime();

        for (Iterator iter = rawDataList.iterator(); iter.hasNext();) {
            RawData rawData = (RawData)iter.next();
            currStudent = rawData.getStudent();
            currProblem = rawData.getProblem();
            currStep = rawData.getSubgoal();

            if (currStudent.length() <= 0) {
                continue;
            }

            logger.debug(currStudent + " " + currProblem);

            // Check if its a new student
            if (!currStudent.equals(prevStudent)) {
                logger.debug("New Student: " + currStudent);
                // reset start time
                theGC = resetTime(theGC);
                timeStamp = theGC.getTime();
                String timeString = DateTools.getTimeStringWithOutTimeZone(timeStamp);

                // log session
                sessionId = Message.generateGUID("GEO");
                logSession(currStudent, sessionId);

                MetaElement metaElement = new MetaElement(currStudent, sessionId,
                        timeString, DEFAULT_TIME_ZONE);
                contextMsg = ContextMessage.createStartProblem(metaElement);
                contextMsg.setSource(DEFAULT_SOURCE);
                ProblemElement problem = new ProblemElement(currProblem);
                LevelElement unitLevel = new LevelElement("Unit", "Area", problem);
                contextMsg.setDataset(new DatasetElement(curriculumName, unitLevel));

                logger.debug("Logging context message");
                log(contextMsg, timeStamp);

                stepNameSet.clear();

            } else if (!currProblem.equals(prevProblem)) {
                logger.debug("New Problem: " + currProblem);

                String timeString = DateTools.getTimeStringWithOutTimeZone(timeStamp);

                MetaElement metaElement = new MetaElement(currStudent, sessionId,
                        timeString, DEFAULT_TIME_ZONE);
                contextMsg = ContextMessage.createStartProblem(metaElement);
                contextMsg.setSource(DEFAULT_SOURCE);
                ProblemElement problem = new ProblemElement(currProblem);
                LevelElement unitLevel = new LevelElement("Unit", "Area", problem);
                contextMsg.setDataset(new DatasetElement(curriculumName, unitLevel));

                logger.debug("Logging context message");
                log(contextMsg, timeStamp);

                stepNameSet.clear();

            } else if (stepNameSet.contains(currStep)) {
                logger.debug("New Problem VIEW: " + currProblem);

                String timeString = DateTools.getTimeStringWithOutTimeZone(timeStamp);

                MetaElement metaElement = new MetaElement(currStudent, sessionId,
                        timeString, DEFAULT_TIME_ZONE);
                contextMsg = ContextMessage.createStartProblem(metaElement);
                contextMsg.setSource(DEFAULT_SOURCE);
                ProblemElement problem = new ProblemElement(currProblem);
                LevelElement unitLevel = new LevelElement("Unit", "Area", problem);
                contextMsg.setDataset(new DatasetElement(curriculumName, unitLevel));

                logger.debug("Logging context message");
                log(contextMsg, timeStamp);

                stepNameSet.clear();
            }
            stepNameSet.add(currStep);

            //
            // log tool action (student attempt)
            //
            toolMsg = ToolMessage.create(contextMsg);
            toolMsg.setAsAttempt();
            toolMsg.addSai(rawData.getSubgoal(), NO_ACTION, NO_INPUT);

            logger.debug("Logging tool message");
            log(toolMsg, timeStamp);
            logger.debug("Done logging tool message");

            //
            // log tutor action (system response)
            //
            tutorMsg = TutorMessage.create(toolMsg);
            if (rawData.isCorrect()) {
                tutorMsg.setAsCorrectAttemptResponse();
            } else {
                tutorMsg.setAsIncorrectAttemptResponse();
            }
            tutorMsg.addSai(rawData.getSubgoal(), NO_ACTION, NO_INPUT);

            logger.debug("Start adding skills");
            // add textbook skills
            for (Iterator skillIter = rawData.getTextbookSkillList().iterator();
                    skillIter.hasNext();) {
                String skillName = (String)skillIter.next();
                tutorMsg.addSkill(new SkillElement(skillName, "", TEXTBOOK_SKILL_MODEL));

                if (skillName.toLowerCase().indexOf("area") < 0) {
                    tutorMsg.addSkill(new SkillElement(AREA_SKILL, "", AREA_SKILL_MODEL));
                } else {
                    tutorMsg.addSkill(new SkillElement(NON_AREA_SKILL, "", AREA_SKILL_MODEL));
                }
            }

            // add original skills
            for (Iterator skillIter = rawData.getOriginalSkillList().iterator();
                    skillIter.hasNext();) {
                String skillName = (String)skillIter.next();
                tutorMsg.addSkill(new SkillElement(skillName, "", ORIGINAL_SKILL_MODEL));
            }

            tutorMsg.addSkill(new SkillElement(GEOMETRY_SKILL_MODEL, "", GEOMETRY_SKILL_MODEL));

            logger.debug("Done adding skills");

            logger.debug("Start adding factors as custom fields");
            // add factors
            Iterator factorIter = rawData.getFactorList().iterator();
            Iterator namesIter = factorNames.iterator();
            while (factorIter.hasNext()) {
                String factorValue = (String)factorIter.next();
                String factorName = UNKNOWN_FACTOR;
                if (namesIter.hasNext()) {
                    factorName = (String)namesIter.next();
                    tutorMsg.addCustomField(
                            new CustomFieldElement("Factor " + factorName, factorValue));
                }
            }
            logger.debug("Done adding factors as custom fields");

            logger.debug("Logging tutor message");
            log(tutorMsg, timeStamp);

            //
            // If their first attempt was incorrect,
            // then log a correct attempt.
            //
            if (!rawData.isCorrect()) {
                //add one second to the time stamp
                theGC = addDuration(theGC, 1);
                timeStamp = theGC.getTime();

                //log tool message again
                logger.debug("Logging tool message");
                toolMsg.setAsAttempt();
                log(toolMsg, timeStamp);

                //log tutor message again, now as correct
                logger.debug("Logging tutor message");
                tutorMsg.setToolMessage(toolMsg);
                tutorMsg.setAsCorrectAttemptResponse();
                log(tutorMsg, timeStamp);
            }

            // add this duration to the start time
            int duration = rawData.getDuration();
            if (duration <= 0) {
                duration = 1;
            }
            theGC = addDuration(theGC, duration);
            timeStamp = theGC.getTime();

            // keep track of previous student and problem
            prevStudent = currStudent;
            prevProblem = currProblem;

        } // end for loop
    } // end doSomethingCool method

    /**
     * Inner class to hold each row of data in the input file with
     * no processing involved.
     */
    class RawData {
        /** actionEvaluation. */
        private String actionEvaluation;
        /** correct flag. */
        private boolean correct;
        /** student's name. */
        private String student;
        /** problem name. */
        private String problem;
        /** subgoal. */
        private String subgoal;
        /** time in seconds. */
        private String duration;

        /** list of text book skills. */
        private List skillTextBookList;
        /** textbook skill. */
        private String skillTextBook;

        /** list of original skills. */
        private List skillOriginalList;
        /** original skill. */
        private String skillOriginal;

        /** list of learning factors. */
        private List factorList;

        /**
         * Constructor.
         * @param success 1 = success/correct
         * @param student student's user name
         * @param problem problem name
         * @param subgoal subgoal name
         * @param duration time in seconds
         * @param skillTextBook text book skill
         * @param skillOriginal original skill
         * @param factorList list of learning factors
         */
        RawData (String success,
                        String student,
                        String problem,
                        String subgoal,
                        String duration,
                        String skillTextBook,
                        String skillOriginal,
                        List factorList) {
           if (success.equals("0")) {
                this.actionEvaluation = INCORRECT_EVAL;
                this.correct = false;
           } else {
                this.actionEvaluation = CORRECT_EVAL;
                this.correct = true;
           }
           this.student = student;
           this.problem = problem;
           this.subgoal = subgoal;
           this.duration = duration;
           this.skillTextBook = skillTextBook;
           this.skillTextBookList = new ArrayList();
           this.skillTextBookList.add(skillTextBook);

           this.skillOriginal = skillOriginal;
           this.skillOriginalList = new ArrayList();
           this.skillOriginalList.add(skillOriginal);

           this.factorList = factorList;
        }
        /** Get action evaluation. @return the action evaluation string */
        public String getActionEvalution() { return actionEvaluation; }
        /** Is correct. @return whether or not it is correct */
        public boolean isCorrect() { return correct; }
        /** Get student. @return the student string */
        public String getStudent() { return student; }
        /** Get problem. @return the problem string */
        public String getProblem() { return problem; }
        /** Get subgoal. @return the subgoal string */
        public String getSubgoal() { return subgoal; }
        /** Get duration. @return the duration */
        public int getDuration() {
            int value = 1;
            if (duration.length() > 0) {
                try {
                    value = new Double(duration).intValue();
                } catch (NumberFormatException exception) {
                    logger.info("Caught number format exception.");
                }
            }
            return value;
        }
        /** Get TextbookSkillList. @return the TextbookSkillList */
        public List getTextbookSkillList() {
            return Collections.unmodifiableList(skillTextBookList);
        }
        /** Get OriginalSkillList. @return the OriginalSkillList */
        public List getOriginalSkillList() {
            return Collections.unmodifiableList(skillOriginalList);
        }
        /** Get SkillTextBook. @return the SkillTextBook */
        public String getSkillTextBook() { return skillTextBook; }
        /** Get SkillOriginal. @return the SkillOriginal */
        public String getSkillOriginal() { return skillOriginal; }
        /** Get FactorList. @return the FactorList */
        public List getFactorList() { return factorList; }
    } // end inner class RawData

    /**
     * Handle the command line options.
     * @param args arguments on the command line
     * @see #displayUsage()
     */
    protected void handleOptions(String[] args) {
        if (args.length == 0 || args == null) {
            return;
        }

        java.util.ArrayList argsList = new java.util.ArrayList();
        for (int i = 0; i < args.length; i++) {
            argsList.add(args[i]);
        }

        // loop through the arguments
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-h")) {
                displayUsage();
                System.exit(0);
            } else if (args[i].equals("-f")) {
                if (++i < args.length) {
                    fileName = args[i];
                } else {
                    System.err.println(
                        "Error: a file name must be specified with: -f");
                    displayUsage();
                    System.exit(1);
                }
            } else if (args[i].equals("-c")) {
                if (++i < args.length) {
                    curriculumName = args[i];
                } else {
                    System.err.println(
                        "Error: a curriculum name must be specified with: -c");
                    displayUsage();
                    System.exit(1);
                }
            } else if (args[i].equals("-p")) {
                prodFlag = true;
            } else {
                System.err.println("Error: improper command line arguments: " + argsList);
                displayUsage();
                System.exit(1);
            } // end if then else
        } // end for loop
    } // end handleOptions

    /**
     * Display the usage if an error occurs while handling the
     * command line arguments.
     * @see #handleOptions(String[])
     */
    protected void displayUsage() {
        System.err.println(
                "\nUSAGE: java -classpath ... "
                        + "GeometryDistiller [-f file_name]"
                        + " [-c curriculum_name]"
                        + " [-p]");
        System.err.println("Option descriptions:");
        System.err.println("\t-h\tusage info");
        System.err.println("\t-f\tinput file name");
        System.err.println("\t-c\tcurriculum name");
        System.err.println("\t-p\tproduction flag");
    }

    /**
     * Main.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        Logger logger = Logger.getLogger("GeometryDistiller.main");
        logger.info("GeometryDistiller starting...");
        GeometryDistiller mapper = null;
        try {
            // create a distiller
            mapper = new GeometryDistiller();

            // parse arguments to get file name and curriculum name
            mapper.handleOptions(args);

            // Production mode is set on the command line
            mapper.setUpLogging();

            // read file, parse it and do something cool with it
            mapper.justDoIt();

        } catch (Throwable throwable) {
            logger.error("Unknown error occurred:" + throwable.getMessage(), throwable);
        } finally {
            // close the output streams
            mapper.closeLogging();
        }
        logger.info("GeometryDistiller done.");
    } // end main


    /**
     * Reset the time.
     * Note that somehow the original GREG_DATE which I though was final and static
     * actually changes.  Therefore hard-coding the same time starting place here.
     * @param gregDate the date thing to reset
     * @return gregDate the new date thing
     */
    private GregorianCalendar resetTime(GregorianCalendar gregDate) {
        gregDate.set(START_YEAR, 1, 1, 0, 0);
        return gregDate;
    }
    /**
     * Add the given duration to the given date thing.
     * @param gregDate the current date thing
     * @param duration the duration to be added
     * @return the new date thing
     */
    private GregorianCalendar addDuration(GregorianCalendar gregDate, int duration) {
        gregDate.add(Calendar.SECOND, duration);
        return gregDate;
    }

} // end GeometryDistiller class
