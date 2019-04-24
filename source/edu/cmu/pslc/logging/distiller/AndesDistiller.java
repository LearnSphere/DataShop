/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005-2006
 * All Rights Reserved
 */
package edu.cmu.pslc.logging.distiller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.JDOMParseException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import edu.cmu.pslc.datashop.xml.XmlParser;
import edu.cmu.pslc.datashop.xml.XmlParserFactory;
import edu.cmu.pslc.logging.Message;

/**
 * A distiller to transform the Andes data from using interpretations (the new way)
 * to using event descriptors (the old way).
 * It will also apply the location heuristic.
 *
 * @author Alida Skogsholm
 * @version $Revision: 4441 $
 * <BR>Last modified by: $Author: bkb $
 * <BR>Last modified on: $Date: 2008-01-07 10:17:45 -0500 (Mon, 07 Jan 2008) $
 * <!-- $KeyWordsOff: $ -->
 */
public class AndesDistiller {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** Directory for plain XML files. */
    private static String inputXMLDir = null;

    /** Directory for plain XML files. */
    private static String outputXMLDir = null;

    /** The directory name of the input files. */
    private String inputFileDirectory = null;

    /** The SaxBuilder to use. */
    private SAXBuilder builder;

    /** Holds the elements to be modified from the prevXMLDocument. */
    private ArrayList prevToBeModified = new ArrayList();

    /** Holds the elements to be modified from the currXMLDocument. */
    private ArrayList currToBeModified = new ArrayList();

    /** Holds the child elements from the prevXMLDocument. */
    private List prevSetOfChildren = null;

    /** Holds the child elements from the currXMLDocument. */
    private List currSetOfChildren = null;

    /** The ID of the last student processed. */
    private String prevStudentID = null;

    /** The ID of the current student being processed. */
    private String currStudentID = null;

    /** The output file name of the modified prevXMLDocument. */
    private String prevOutputFileName = null;

    /** The string of the directory for the previous file file */
    private String prevOutputDirectoryName = null;

    /** The output file name of the modified currXMLDocument. */
    private String currOutputFileName = null;

    /** The string of the directory for the current file */
    private String currOutputDirectoryName = null;

    /** The last XML document processed. */
    private Document prevXMLDocument = null;

    /** The current XML document being processed. */
    private Document currXMLDocument = null;

    /** The name of the next file to be processed. */
    private String nextFileName = null;

    /** HashMap of the context message ID to the problem name */
    private HashMap <String, String> contextIdProblemNameMap = new HashMap <String, String> ();

    /** Default constructor. */
    public AndesDistiller() {
    }

    /**
     * Do plain XML logging only.
     */
    private void setUpLogging() {
        // Create a directory; all non-existent ancestor directories are
        // automatically created
        boolean success = (new File(outputXMLDir)).mkdirs();
        if (!success) {
            // Directory creation failed
            logger.warn("Failed to create directory: " + outputXMLDir);
        }
    }

    /**
     * A recursive function to return a list of all the files in a top
     * level directory, including all the subdirectories.
     * This method will skip CVS directories.
     * @param theFile a file or directory
     * @param directoryString stored the string of the directory.
     * @return a complete list of the files in this directory
     */
    private List <FileInfo> getFiles(File theFile, String directoryString) {
        List <FileInfo> fileList = new ArrayList <FileInfo>();

        if (theFile.isFile()) {
            logger.debug("Adding file " + theFile.getName());
            fileList.add(new FileInfo(theFile, directoryString));
        } else if (theFile.isDirectory()) {
            if (theFile.getName().equals("CVS")) {
                logger.info("Skipping directory " + theFile.getName());
            } else {

                if (!theFile.getAbsolutePath().equals(
                        new File(inputFileDirectory).getAbsolutePath())) {
                    boolean success = (
                            new File(
                                    outputXMLDir + File.separator
                                  + directoryString + File.separator
                                  + theFile.getName())).mkdirs();
                    if (!success) {
                        // Directory creation failed
                        logger.warn("Failed to create directory: " + outputXMLDir);
                    }
                    directoryString += File.separator + theFile.getName();
                }

                File[] files = theFile.listFiles();
                for (int idx = 0; idx < files.length; idx++) {
                    fileList.addAll(getFiles(files[idx], directoryString));
                }

            }
        } // end else

        return fileList;
    }

    /**
     * Returns an array of files given a directory.
     * @param directoryName the directory path
     * @return an array of files
     */
    private List <FileInfo> getFilenameList(String directoryName) {

        File topLevelDirectory = new File(directoryName);

        if (!topLevelDirectory.isDirectory()) {
            logger.warn("Not a directory: " + directoryName);
        } else if (logger.isDebugEnabled()) {
            logger.debug("Directory found: " + directoryName);
        }

        logger.info("Top level directory is " + topLevelDirectory.getName());
        logger.info("Adding files... ");
        List <FileInfo> fileList = getFiles(topLevelDirectory, "");
        Collections.sort(fileList);
        return fileList;
    }

    /**
     * Returns log data given a filename.
     * @param fileName the name of the file
     * @param theFile as File
     * @param retry Integer of which retry the function is on.
     * @return the XML in the given file
     * @throws IOException if the file is unable to be opened after several attempts.
     */
    private Document getLogData(String fileName, File theFile, Integer retry) throws IOException {
        final int totalRetries = 3;
        Document document = null;
        try {
            document = builder.build(theFile);
        } catch (IOException ex) {
            logger.warn("IOException occurred with " + fileName + ". ", ex);
            if (retry < totalRetries) {
                logger.info("Retrying file. ");
                document = getLogData(fileName, theFile, retry++);
            } else {
                logger.error("Unable to open file.. failed after " + totalRetries + " tries!");
                throw new IOException("Unable to open file " + fileName + " after 3 tries!");
            }
            document = null;
        } catch (JDOMParseException ex) {
            logger.warn("JDOMParseException occurred with " + fileName + ". ", ex);
            document = null;
        } catch (JDOMException ex) {
            logger.warn("JDOMException occurred with " + fileName + ". ", ex);
            document = null;
        }
        return document;
    }

    /**
     * Responsible for parsing the XML files.  Uses throwOutTheInterpretations() to
     * break any <correct_step_sequences> with multiple steps into individual
     * tool/tutor message pairs.  Then uses applyTheLocationHeuristic() to
     * apply the location heuristic.
     * @param directory name of the directory
     */
    private void convert(String directory) {
        builder = new SAXBuilder();
            logger.info("Getting files...");
            List <FileInfo> fileList = getFilenameList(directory);

            logger.info("Reading and converting " + fileList.size() + " files.");

            XmlParser parser = null;
            for (int i = 0, n = fileList.size(); i < n; i++) {
                FileInfo theFileInfo = fileList.get(i);
                File theFile = theFileInfo.getTheFile();
                File nextFile;
                if (i + 1 < n) {
                    FileInfo theNextFileInfo = fileList.get(i + 1);
                    nextFile = theNextFileInfo.getTheFile();
                    nextFileName = nextFile.getName();
                }
                String inFileName = theFile.getName();
                currOutputFileName = theFile.getName() + ".modified.xml";
                currOutputDirectoryName = theFileInfo.getDirectoryString();
                logger.debug("Reading file " + inFileName + " (" + (i + 1) + "/" + n + ")");
                if (!theFile.isFile()) {
                    logger.warn("Invalid file " + inFileName + ". Skipping File.");
                    continue;
                }

                try {
                    currXMLDocument = getLogData(inFileName, theFile, 0);
                } catch (IOException ioException) {
                    logger.error("Error to opening file for parsing.", ioException);
                    break;
                }

                if (currXMLDocument == null) {
                    logger.warn("Invalid XML in file " + inFileName + ". Skipping File.");
                    continue;
                }

                parser = XmlParserFactory.getInstance().get(currXMLDocument);
                if (parser == null) {
                    logger.warn("Invalid XML in file " + inFileName + ". Skipping File.");
                    continue;
                }

                currSetOfChildren = currXMLDocument.getRootElement().getChildren();

                try {
                    throwOutTheInterpretations();
                    applyTheLocationHeuristic();
                } catch (IOException ioException) {
                    logger.error("Caught " + ioException.getMessage()
                            + " while modifying the XML. Ending conversion.", ioException);
                    break;
                }

                if (prevToBeModified != null && prevToBeModified.size() == 0) {
                    if (prevOutputFileName != null) {
                        // this means there are no outstanding steps to assign
                        logger.debug("convert(): No outstanding msg.  Writing "
                                + outputXMLDir + prevOutputDirectoryName
                                + prevOutputFileName);
                        FileLogger newFileLogger = new FileLogger(
                                outputXMLDir + prevOutputDirectoryName,
                                prevOutputFileName);
                        newFileLogger.writeFile(prevXMLDocument);
                    }
                } else if ((i + 1) == n) {
                    // no files left to process
                    // should we write both here regardless?
                    logger.debug("convert(): No files left to process.  Writing "
                            + outputXMLDir + prevOutputDirectoryName
                            + prevOutputFileName);
                    FileLogger newFileLogger = new FileLogger(
                            outputXMLDir + prevOutputDirectoryName,
                            prevOutputFileName);
                    newFileLogger.writeFile(prevXMLDocument);
                }
            } // end for loop
            logger.info("\nDone.");
    }

    /**
     * This method looks for <interpretation> elements and converts them to <semantic_event>
     * Also, <tutor_msg> with <correct_step_seq> with multiple steps are broken into individual
     * tool/tutor pairings, reusing the same <tool_msg> for each new <tutor_msg>
     * @throws IOException input/output exception
     */
    private void throwOutTheInterpretations() throws IOException {
        logger.debug(currSetOfChildren.size() + " messages found.");

        List stepKeys = null;
        HashMap stepSkillMap = null;
        Element toolMessageElement = null;
        Element tutorMessageElement = null;
        Element problemElement = null;
        Element contextMsg = null;
        List messagesMissingLocation = new ArrayList();
        //String tutorProblemName = new String();

        // look for tool/tutor pairings
        for (int i = 0; i < currSetOfChildren.size(); i++) {
            Element messageElement = (Element)currSetOfChildren.get(i);

            if (messageElement.getName().equals(Message.CONTEXT_MSG_ELEMENT)) {
                contextMsg = messageElement;
                if (i == 0) { assignCurrentStudent(messageElement); }
                problemElement = getProblemElement(contextMsg);

                //create a mapping of context message IDs to problem element in order to always
                //have the ability get the problem name.
                String contextMessageId =
                    contextMsg.getAttribute("context_message_id").getValue();
                contextIdProblemNameMap.put(contextMessageId, problemElement.getChildText("name"));
            }

            // once we find a tutor_msg, grab the message before it, that should be
            // it's accompanying tool_msg
            if (messageElement.getName().equals(Message.TUTOR_MSG_ELEMENT)) {
                boolean stepsFound = false;
                tutorMessageElement = messageElement;
                toolMessageElement = (Element)currSetOfChildren.get(i - 1);

                /*
                 * We found a pair, now process them.  We need to convert the
                 * 'interpretation' to an 'event_descriptor' & create new tool/tutor
                 * pairings for interpretations with multiple steps.
                 * Use 'step_info' as the 'selection' value.
                 * Use 'rule' as 'skill' name
                 * Update transaction_id if multiple steps
                 */
                Iterator interpIter = tutorMessageElement.getChildren("interpretation").iterator();
                Element chosenInterp = null;
                while (interpIter.hasNext()) {
                    Element interpElement = (Element)interpIter.next();
                    if ((interpElement.getName().equals("interpretation"))
                            && interpElement.getAttributeValue("chosen").equals("true")) {
                        chosenInterp = interpElement;
                        break; //chosen interpretation found, no need to continue.
                    }
                }

                if (chosenInterp != null) {
                    Element correctStepSequence = chosenInterp.getChild("correct_step_sequence");
                    if (correctStepSequence != null) {
                        stepSkillMap = new HashMap();
                        stepKeys = new ArrayList();
                        for (Iterator stepIter = correctStepSequence.getChildren("step").iterator();
                                stepIter.hasNext();) {
                            Element stepElement = (Element)stepIter.next();
                            Element stepInfoElement = stepElement.getChild("step_info");
                            String stepInfo = stepInfoElement.getText();
                            if (stepInfo != null && stepInfo.length() > 0) {
                                stepsFound = true;
                                Element ruleElement = stepElement.getChild("rule");
                                String rule = null;
                                if (ruleElement != null) {
                                    rule = ruleElement.getText();
                                } else {
                                    rule = "";
                                }
                                stepKeys.add(stepInfo);
                                stepSkillMap.put(stepInfo, rule);
                            }
                        }
                    } else {
                        if (logger.isDebugEnabled()) {
                            Element semanticEvent = tutorMessageElement.getChild("semantic_event");
                            logger.warn("Interpretation has no correct step sequence for txn #"
                                    + semanticEvent.getAttributeValue("transaction_id"));
                        }
                    }
                } else {
                    if (logger.isDebugEnabled()) {
                        Element semanticEvent = tutorMessageElement.getChild("semantic_event");
                        logger.debug("No interpretation for tutor_message "
                                + semanticEvent.getAttributeValue("transaction_id"));
                    }
                }

                //remove the interpretations as we no longer need them
                //and are throwing away all the incorrect sequences and not chosen ones.
                tutorMessageElement.removeChildren("interpretation");

                if (stepsFound) {
                    String contextMessageId =
                        tutorMessageElement.getAttributeValue("context_message_id");
                    String tutorProblemName = contextIdProblemNameMap.get(contextMessageId);
                    logger.debug("tutor problem name is: " + tutorProblemName);

                    if (stepSkillMap.size() == 1) {
                        processSingleStep(tutorMessageElement, stepKeys);
                        applyTemporalHeuristic(messagesMissingLocation, tutorProblemName,
                                stepKeys, stepSkillMap);

                    } else {
                        int toolIndex = currSetOfChildren.indexOf(toolMessageElement);
                        currSetOfChildren.remove(toolMessageElement);
                        currSetOfChildren.remove(tutorMessageElement);
                        Collection <Element> pairs = processMultipleSteps(toolMessageElement,
                                tutorMessageElement, stepKeys, stepSkillMap);

                        for (Iterator pairsIt = pairs.iterator(); pairsIt.hasNext();) {
                            Element elementToAdd = (Element)pairsIt.next();
                            if (toolIndex < currSetOfChildren.size()) {
                                currSetOfChildren.add(toolIndex, elementToAdd);
                            } else {
                                currSetOfChildren.add(elementToAdd);
                            }
                            toolIndex += 1;
                        }

                        applyTemporalHeuristic(messagesMissingLocation, tutorProblemName,
                                stepKeys, stepSkillMap);
                        // The for loop is going to increment i by one, but we need to first
                        // add on the number of insertions we just made,
                        // so i = toolIndex-1, to account for the i++
                        i = toolIndex - 1;
                        logger.debug("\t Updated i value is: " + i);
                    }
                } else {
                    // add message missing steps elements list
                    // because no step information was found.
                    messagesMissingLocation.add(tutorMessageElement);
                }
            } // end if (tool_msg_element)
        } // end while
    } // end throwOutTheInterpretations

    /**
     * Grabs the user_id for the student we are currently processing and assigns
     * it to currStudentID.
     * @param contextMsg the context_message for this file
     */
     private void assignCurrentStudent(Element contextMsg) {
        Element meta = contextMsg.getChild("meta");
        String studentId = meta.getChildText("user_id");
        if (logger.isDebugEnabled() || !studentId.equals(currStudentID)) {
            logger.info("Processing student: " + studentId);
        }
        currStudentID = studentId;
    }

    /**
     * Grabs the problem from the context_message
     * @param contextMsg the context message to use
     * @return the problem element
     */
     private Element getProblemElement(Element contextMsg) {
        Element problem = null;
        Element datasetLevel = contextMsg.getChild("dataset").getChild("level");
        boolean notFound = true;
        try {
            while (notFound) {
                if (datasetLevel.getChild("level") == null) {
                    notFound = false;
                } else {
                    datasetLevel = datasetLevel.getChild("level");
                }
            }
        } catch (NullPointerException npe) {
            logger.debug("Crazy NPE catch. ", npe);
            // not doing anything
        }
        problem = datasetLevel.getChild("problem");
        return problem;
    }

    /**
     * Implementation of the temporal heuristic.  This method creates event
     * descriptors for those elements missing them and then assigns the appropriate
     * step name.
     * @param missingStepElements the elements missing an <event_descriptor>
     * @param tutorMsgProblemName the name of the problem to check against
     * @param stepKeys the list of step names for the decomposed tutor messages
     * @param stepSkillMap a map of steps to skills.
     */
     private void applyTemporalHeuristic(List missingStepElements, String tutorMsgProblemName,
             List stepKeys, HashMap stepSkillMap) {
        String stepNameToAssign = new String();
        String skillName = new String();

        if (missingStepElements.size() > 0) {
            Iterator it = stepKeys.iterator();
            if (it.hasNext()) {
                stepNameToAssign = (String) it.next();
                skillName = (String) stepSkillMap.get(stepNameToAssign);
            }
        }

        for (Iterator mIter = missingStepElements.iterator(); mIter.hasNext();) {
            Element missing = (Element) mIter.next();
            Element semanticEvent = missing.getChild("semantic_event");
            String problemName = contextIdProblemNameMap.get(
                    missing.getAttributeValue("context_message_id"));

            // check to make sure the messages correspond to the same problem,
            // otherwise discard the missing step element
            if (tutorMsgProblemName.equals(problemName)) {

                // now check for result, if correct ignore
                // also if an event descriptor already exists ignore
                String actionEvaluation = missing.getChildText("action_evaluation");
                if (actionEvaluation != "CORRECT") {
                    setLocation(missing, stepNameToAssign);
                    Element skill = missing.getChild("skill");

                    //don't add skills to CALC_REQUESTS or CALC_RESULTS
                    if (skill == null
                            && !semanticEvent.getAttributeValue("name").equals("CALC_REQUEST")
                            && !semanticEvent.getAttributeValue("name").equals("CALC_RESULT")) {

                        if (skillName.length() > 0) {
                            missing.addContent(createSkillElement(skillName));
                        }
                    }

                } // != CORRECT
            } // if problemName
        }
        missingStepElements.clear();
    }

    /**
     * Processes tutor_msg elements with a single step.  Inserts a new <event_descriptor>
     * element into the tutor_msg.
     * @param tutorMessageElement the tutor_msg to modify
     * @param stepKeys the list of step/skill keys
     */
    private void processSingleStep(Element tutorMessageElement, List stepKeys) {
        Element semanticEvent = tutorMessageElement.getChild("semantic_event");
        Attribute transactionID = semanticEvent.getAttribute("transaction_id");
        if (logger.isDebugEnabled()) {
            logger.debug("Single steps for " + tutorMessageElement.getName()
                    + " with transaction_id = "
                    + transactionID.getValue());
        }
        setLocation(tutorMessageElement, (String)stepKeys.get(0));
    }

    /**
     * Processes tutor_msg_elements with multiple steps.  The tutor_msg is
     * decomposed into a new tutor_msg with accompanying tool_msg
     * for each step_info value.  The <rule> element is used to assign the skill.
     * @param toolMessage the tool_msg to use in the pairings
     * @param tutorMessage the tutor_msg to break into steps
     * @param stepKeys the list of step/skill keys
     * @param stepSkillMap the mapping of steps to skills
     * @return newPairs the broken-out tool/tutor pairings
     */
    private ArrayList processMultipleSteps(Element toolMessage,
            Element tutorMessage, List stepKeys, HashMap stepSkillMap) {
        ArrayList newPairs = new ArrayList();
        Element semanticEvent = toolMessage.getChild("semantic_event");
        Attribute transactionID = semanticEvent.getAttribute("transaction_id");
        Attribute contextMsgID = toolMessage.getAttribute("context_message_id");
        logger.debug("Multiple steps for this msg.  Transaction_id is: "
                + transactionID.getValue());

        for (int i = 0, n = stepKeys.size(); i < n; i++) {
            String updatedTxID = transactionID.getValue() + "_" + i;

            // copy the tool_msg
            Element newToolMessage = new Element("tool_message");
            List copiedContent = toolMessage.cloneContent();
            newToolMessage.setAttribute((Attribute) contextMsgID.clone());
            newToolMessage.setContent(copiedContent);
            newToolMessage.getChild("semantic_event").setAttribute("transaction_id",
                    updatedTxID);
            newPairs.add(newToolMessage);

            // create the new tutor_msg
            Element newTutorMessage = new Element("tutor_message");
            copiedContent.clear();
            copiedContent = tutorMessage.cloneContent();
            newTutorMessage.setAttribute((Attribute) contextMsgID.clone());
            newTutorMessage.setContent(copiedContent);
            newTutorMessage.getChild("semantic_event").setAttribute("transaction_id",
                    updatedTxID);

            // assume there are as many <skill> elements as there are steps, so loop through
            // and remove them all (wonder if this could throw an error?)
            for (int j = 0; j < stepKeys.size(); j++) {
                newTutorMessage.removeChild("skill");
            }

            //do not add skill elements to calc requests.
            if (!semanticEvent.getAttributeValue("name").equals("CALC_REQUEST")
                    && !semanticEvent.getAttributeValue("name").equals("CALC_RESULT")) {
                newTutorMessage.addContent(
                        createSkillElement((String) stepSkillMap.get(stepKeys.get(i))));
            }

            //set the location on the tutor message.
            setLocation(newTutorMessage, (String) stepKeys.get(i));
            newPairs.add(newTutorMessage);
        }

        return newPairs;
    }

    /**
     * Applies the Location Heuristic to the currXMLDocument.
     *
     * If a tutor_msg has an INCORRECT or HINT_REQUEST, stick it in the queue to be
     * modified.  When we find the next CORRECT for that student and problem, back-fill
     * the <selection> elements for each msg in the queue with the stepName from the
     * CORRECT msg.
     *
     * Students may have multiple files, and it may be the case that a student file
     * ends with elements in the queue.  In that case, persist the queue to prevToBeModified
     * and look for the next CORRECT in the next file.  Once found, back-fill and write
     * the prevXMLDocument.
     *
     * If there are outstanding items in the queue but the currXMLDocument is not for
     * the same student, then we can't do anything, so just write the prevXMLDocument.
     * @throws IOException input/output exception
     */
    private void applyTheLocationHeuristic() throws IOException {
        Element toolMessageElement = null;
        Element tutorMessageElement = null;

        // look for tool messages with <semantic_event> name == HINT_REQUEST or ATTEMPT
        for (int i = 0; i < currSetOfChildren.size(); i++) {
            Element messageElement = (Element)currSetOfChildren.get(i);
            int tutorMsgIndex = 0;
            if (messageElement.getName().equals(Message.TOOL_MSG_ELEMENT)) {
                toolMessageElement = messageElement;

                Element semanticEvent = toolMessageElement.getChild(("semantic_event"));
                String txID = semanticEvent.getAttributeValue("transaction_id");
                String semanticEventName = semanticEvent.getAttributeValue("name");

                if (semanticEventName.equals("ATTEMPT")) {
                    logger.debug("tx_id " + txID + " is an ATTEMPT");
                    tutorMessageElement = (Element) currSetOfChildren.get(i + 1);
                    String actionEvaluation = tutorMessageElement.getChildText("action_evaluation");
                    if (actionEvaluation.equals("INCORRECT")) {
                        logger.debug("tutor_msg " + txID + " has an INCORRECT");
                        tutorMsgIndex = i + 1;
                        currToBeModified.add(tutorMsgIndex);
                    } else if (actionEvaluation.equals("CORRECT")) {
                        processCorrectAttempt(tutorMessageElement, toolMessageElement);
                    } else if (!actionEvaluation.equals("TRUE_BUT_IRRELEVANT")) {
                        logger.warn("Unknown actionEvaluation for contextID/txnID "
                                + tutorMessageElement.getAttributeValue("context_message_id")
                                + "/" + txID
                                + " :: " + actionEvaluation);
                    }
                } else if (semanticEventName.equals("HINT_REQUEST")) {
                    logger.debug("tx_id " + txID + " is a HINT_REQUEST");
                    tutorMsgIndex = i + 1;
                    currToBeModified.add(tutorMsgIndex);
                }
            } // end if tool_msg
            // if this file only has a context message and nothing else, make sure
            // to write the prevXMLDocument.
            if (messageElement.getName().equals(Message.CONTEXT_MSG_ELEMENT)
                    && currSetOfChildren.size() == 1 && prevOutputDirectoryName != null) {
                FileLogger newFileLogger = new FileLogger(
                            outputXMLDir + prevOutputDirectoryName,
                            prevOutputFileName);
                newFileLogger.writeFile(prevXMLDocument);
                logger.debug("This file only has a context message.  "
                        + "Writing " + prevOutputDirectoryName
                        + File.separator + prevOutputFileName);
            }
        } // end for

        // now that we've seen all of the children, assign those left to process to prevToBeModified
        // Also, set all 'prev' variables with the current values.
        if (currSetOfChildren.size() != 0) {
            prevToBeModified.clear();
            if (prevSetOfChildren != null) { prevSetOfChildren.clear(); }
            prevToBeModified.addAll(currToBeModified);

            if (prevToBeModified.size() > 0) { takeAPeek(); }
            prevSetOfChildren = currSetOfChildren;

            logger.debug("size of currSetOfChildren: " + currSetOfChildren.size());
            logger.debug("size of prevSetOfChildren: " + prevSetOfChildren.size());

            // take a peek at the next file - if it's the same student then write
            // prevOutputFileName, otherwise we'll lose it
            prevStudentID = currStudentID;
            prevOutputFileName = currOutputFileName;
            prevOutputDirectoryName = currOutputDirectoryName;
            prevXMLDocument = currXMLDocument;
            logger.debug("I've stumbled upon the EOF!!!");
            currToBeModified.clear();
        }
    } // end applyTheLocationHeuristic

    /**
     * This function processes the location heuristic for a correct attempt.
     *
     * @param tutorMessageElement the tutorMessage
     * @param toolMessageElement the toolMessage
     */
    private void processCorrectAttempt(Element tutorMessageElement, Element toolMessageElement) {
        Element semanticEvent = toolMessageElement.getChild(("semantic_event"));
        String txID = semanticEvent.getAttributeValue("transaction_id");

        String currToolLocation = null;
        Element toolEventDescriptor = toolMessageElement.getChild("event_descriptor");
        if (toolEventDescriptor != null) {
            Element selection = toolEventDescriptor.getChild("selection");
            if (selection != null) {
                currToolLocation = selection.getText();
            } else {
                logger.debug("no selection for toolMessage with txn id " + txID);
            }
        } else {
            logger.debug("event descriptor null for toolMessage with txn id " + txID);
        }

        String stepName = null;
        Element tutorEventDescriptor =
            tutorMessageElement.getChild("event_descriptor");
        if (tutorEventDescriptor != null) {
            stepName = tutorEventDescriptor.getChildText("selection");
        }
        if (stepName == null && logger.isDebugEnabled()) {
            logger.debug("Could not find step name for tutor msg " + txID);
        }

        if (prevStudentID != null
                && prevStudentID.equals(currStudentID)
                && prevToBeModified.size() > 0) {
            logger.debug("This student matches the last one processed.");

            // this student has more than one log file, so now we can try to
            // fill in the outstanding preToBeModified msg and write the file.
            for (Iterator prevModIt = prevToBeModified.iterator();
                    prevModIt.hasNext();) {

                /* first see if the location matches (get the selection/
                 * action from the tool_message).  We have the index
                 * for the tutor_msg that needs fixed, so subtract
                 * 1 from each stored index to get that tutor_msg's
                 * corresponding tool_msg
                 */
                int modifyMeIndex = (Integer) prevModIt.next();
                int toolMsgIndex = modifyMeIndex - 1;
                Element pairedToolMsg =
                    (Element) prevSetOfChildren.get(toolMsgIndex);
                String prevToolLocation = null;

                //get the location/selection
                Element prevEventDescriptor = pairedToolMsg.getChild("event_descriptor");
                if (prevEventDescriptor != null) {
                    prevToolLocation = prevEventDescriptor.getChildText("selection");
                }

                if (prevToolLocation != null
                        && prevToolLocation.equals(currToolLocation)) {
                    logger.debug("the tool locations match, applying location heuristic");

                    Element modifyMe = (Element) prevSetOfChildren.get(
                            modifyMeIndex);
                    String contextMessageId =
                        modifyMe.getAttributeValue("context_message_id");
                    String prevProblemName =
                        contextIdProblemNameMap.get(contextMessageId);

                    logger.debug("prevProblemName is: " + prevProblemName);
                    setLocation(modifyMe, stepName);
                }
            }

            logger.debug("applyTheLocationHeuristic(): writing "
                    + prevOutputDirectoryName + prevOutputFileName);
            FileLogger newFileLogger = new FileLogger(
                    outputXMLDir + prevOutputDirectoryName,
                    prevOutputFileName);
            newFileLogger.writeFile(prevXMLDocument);
        } else if (prevStudentID != currStudentID && prevToBeModified.size() != 0) {
            // there may be some left to process from the last student file,
            // but we don't have any more files for that student,
            // so write the xml file
            logger.info("applyTheLocationHeuristic(): "
                    + " Have " + prevToBeModified.size()
                    + " orphans but onto a different student."
                    + " Writing " + prevOutputFileName);
            FileLogger newFileLogger = new FileLogger(
                        outputXMLDir + prevOutputDirectoryName,
                        prevOutputFileName);
            newFileLogger.writeFile(prevXMLDocument);
        }

        // if it's not the same student, then the prevToBeModified
        // cannot be processed
        prevToBeModified.clear();
        if (currToBeModified.size() != 0) {
            logger.debug("Attempting to apply step name " + stepName + " to "
                    + currToBeModified.size() + " tool messages");

            for (int j = 0; j < currToBeModified.size(); j++) {
                /**
                 * first see if the location matches (get the selection/
                 * action from the tool_message).  We have the index
                 * for the tutor_msg that needs fixed, so subtract 1
                 * from each stored index to get that tutor_msg's
                 * corresponding tool_msg
                 */
                int modifyMeIndex = (Integer) currToBeModified.get(j);
                int toolMsgIndex = modifyMeIndex - 1;
                Element pairedToolMsg =
                    (Element) currSetOfChildren.get(toolMsgIndex);
                String prevToolLocation = null;
                try {
                    prevToolLocation = pairedToolMsg.getChild(
                    "event_descriptor").getChildText("selection");
                } catch (Exception e) {
                    logger.debug("Could not find the "
                            + " location for the prev tool msg.");
                }
                logger.debug("prevToolLocation is: " + prevToolLocation);
                logger.debug("currToolLocation is: " + currToolLocation);
                if (prevToolLocation != null
                        && prevToolLocation.equals(currToolLocation)) {
                    logger.debug("the tool locations match, "
                            + "applying location heuristic");
                    Element modifyMe = (Element) currSetOfChildren.get(
                            modifyMeIndex);

                    String contextMessageId =
                        modifyMe.getAttributeValue("context_message_id");
                    String currProblemName =
                        contextIdProblemNameMap.get(contextMessageId);

                    logger.debug("currProblemName is: " + currProblemName);

                    setLocation(modifyMe, stepName);
                } else {
                    // remove the element from currToBeModified
                    logger.debug("The tool locations do not match.");
                    //currToBeModified.remove(j);
                }
            } // end for loop of tool messages to modify.
            currToBeModified.clear();
        } else {
            logger.debug("Zero tool messages to apply location " + stepName + " to.");
        }

        //all processed so clear out the list.
        currToBeModified.clear();
    }


    /**
     * Sets the location for a given message.  If the <event_descriptor> element does
     * not exist it is created and added.  If the <selection> element does not exist it
     * is created.  If the <selection> element exists it's contents are replaced with the
     * locationText.
     * @param msgElement the message element to set the location for.
     * Must be a <tool_message> or a <tutor_message>
     * @param locationText String of the location.
     */
    private void setLocation(Element msgElement, String locationText) {

        if (!msgElement.getName().equals(Message.TOOL_MSG_ELEMENT)
                && !msgElement.getName().equals(Message.TUTOR_MSG_ELEMENT)) {
            throw new IllegalArgumentException("Element " + msgElement.getName()
                    + " is not of type 'tutor_message' or 'tool_message'.");
        }

        Element eventDescriptor = msgElement.getChild("event_descriptor");
        if (eventDescriptor == null) {
            logger.debug("No event_descriptor creating new.");
            eventDescriptor = new Element("event_descriptor");
            msgElement.addContent(eventDescriptor);
        }

        Element selectionElement = eventDescriptor.getChild("selection");
        if (selectionElement == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("No selection found, creating a new selection with text "
                        + locationText);
            }
            selectionElement = new Element("selection");
            eventDescriptor.addContent(selectionElement);
        } else if (logger.isDebugEnabled()) {
            logger.debug("Previous  selection found, replacing '"
                    + selectionElement.getText() + "' with '"
                    + locationText + "' ");
        }
        selectionElement.setText(locationText);
    }

    /**
     * Creates a new skill element for a stepName.
     * @param skillName the <rule> value we'll use for the skill name
     * @return a new skill element
     */
    private Element createSkillElement(String skillName) {
        Element skillElement = new Element("skill");
        Element nameElement = new Element("name");
        nameElement.setText(skillName);
        skillElement.addContent(nameElement);
        return skillElement;
    }

    /**
     * Compares the prevStudentID with the nextStudent id to see if they are
     * the same.  If so, write the modified xml file.
     */
    public void takeAPeek() {
        if (nextFileName != null) {
            String[] fileNameSplit = nextFileName.split("-");
            String nextStudent = fileNameSplit[0];
            if (nextStudent != null) {
                if (nextStudent.equals(prevStudentID)) {
                    FileLogger newFileLogger = new FileLogger(
                                outputXMLDir + prevOutputDirectoryName,
                                prevOutputFileName);
                    newFileLogger.writeFile(prevXMLDocument);
                    logger.debug("takeAPeek(): "
                            + "Writing " + prevOutputDirectoryName
                            + File.separator + prevOutputFileName);
                }
            }
        }
    }

    /**
     * Read and parse the files in the input directory and
     * put modified files in the output directory.
     */
    public final void justDoIt() {
        convert(inputFileDirectory);
    }

    /**
     * Handle the command line arguments.
     * @param args - command line arguments passed into main
     */
    protected void handleOptions(String[] args) {
        logger.info("In handle options");
        if (args.length == 0 || args == null) {
            logger.error("No arguments specified.");
            return;
        }

        java.util.ArrayList argsList = new java.util.ArrayList();
        for (int i = 0; i < args.length; i++) {
            argsList.add(args[i]);
        }

        // loop through the arguments
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-inputDir")) {
                if (++i < args.length) {
                    inputXMLDir = args[i];
                    inputFileDirectory = inputXMLDir;
                    logger.debug("found the input directory");
                } else {
                    logger.error("A file name must be specified with this argument");
                    System.exit(1);
                }
            } else if (args[i].equals("-outputDir")) {
                if (++i < args.length) {
                    outputXMLDir = args[i];
                    logger.debug("found the output directory");
                } else {
                    logger.error("A file name must be specified with this argument");
                    System.exit(1);
                }
            }
        } // end for loop
    } // end handleOptions

    /**
     * Main.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        Logger logger = Logger.getLogger("AndesDistiller.main");
        logger.info("AndesDistiller starting...");
        AndesDistiller distiller = null;
        try {
            // create a distiller
            distiller = new AndesDistiller();

            // parse command line options
            distiller.handleOptions(args);

            // Production mode is set on the command line
            distiller.setUpLogging();

            // read file, parse it and do something cool with it
            distiller.justDoIt();

        } catch (Throwable throwable) {
            logger.error("Unknown error occurred:" + throwable.getMessage(), throwable);
        }
        logger.info("AndesDistiller done.");
    } // end main

    /**
     * Inner class to handling writing the file.
     */
    class FileLogger {
        /** File writer. */
        private PrintWriter outputStream;

        /**
         * Constructor which creates the file logger given a file name.
         * @param directory the location for the file
         * @param fileName the file name
         */
        public FileLogger(String directory, String fileName) {
            if (directory == null) {
                throw new IllegalArgumentException("Directory name cannot be null");
            }
            if (fileName == null) {
                throw new IllegalArgumentException("File name cannot be null");
            }
            String pathFile = directory + File.separator + fileName;

            try {
                this.outputStream = new PrintWriter(new FileOutputStream(pathFile));
            } catch (IOException exception) {
                logger.error("Failed to open file for writing " + pathFile, exception);
            }
        }

        /**
         * Closes the output stream; this is required.
         * @param document the JDOM document to write
         */
        public void writeFile(Document document) {
            XMLOutputter xmlOutputter = new XMLOutputter();
            try {
                xmlOutputter.output(document, outputStream);
            } catch (IOException exception) {
                logger.warn("IOException occurred: " + exception.getMessage());
            } finally {
                outputStream.close();
            }
        }
    } // end inner class FileLogger

    /** Inner class to save information about a given file. */
    class FileInfo implements Comparable <FileInfo> {
        /** The file to process */
        private File theFile;
        /** The directory the file lives in (past the given base) */
        private String directoryString;

        /**
         * Constructor.
         * @param theFile the file to process
         * @param directoryString the directory of the file.
         */
        public FileInfo(File theFile, String directoryString) {
            this.theFile = theFile;
            this.directoryString = directoryString;
        }

        /** Returns directoryString. @return Returns the directoryString. */
        public String getDirectoryString() {
            return directoryString;
        }

        /** Set directoryString. @param directoryString The directoryString to set. */
        public void setDirectoryString(String directoryString) {
            this.directoryString = directoryString;
        }

        /** Returns theFile. @return Returns the theFile. */
        public File getTheFile() {
            return theFile;
        }

        /** Set theFile. @param theFile The theFile to set. */
        public void setTheFile(File theFile) {
            this.theFile = theFile;
        }

        /**
         * Compares two objects using each attributes of this class.
         *
         * @param otherInfo the FileInto to compare this to.
         * @return the value 0 if equal; a value less than 0 if it is less than;
         * a value greater than 0 if it is greater than
         */
        public int compareTo(FileInfo otherInfo) {
            int value = 0;
            value = this.getDirectoryString().compareTo(otherInfo.getDirectoryString());
            if (value != 0) { return value; }
            value = this.getTheFile().compareTo(otherInfo.getTheFile());
            if (value != 0) { return value; }
            return 0;
        }
    }
} // end AndesDistiller.java
