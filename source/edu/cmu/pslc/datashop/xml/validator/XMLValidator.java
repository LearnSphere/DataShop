/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2007
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.xml.validator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.contrib.input.LineNumberElement;
import org.jdom.contrib.input.LineNumberSAXBuilder;
import org.jdom.filter.ElementFilter;
import org.jdom.input.SAXBuilder;

import edu.cmu.pslc.datashop.util.VersionInformation;
import edu.cmu.pslc.datashop.xml.XMLConstants;
import edu.cmu.pslc.logging.Message;
import edu.cmu.pslc.logging.util.DateTools;

/**
 * Validates XML files against the DataShop schema.  Also performs custom
 * validation if file passes schema validation.
 * @author kcunning
 * @version $Revision: 7679 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2012-04-25 15:31:44 -0400 (Wed, 25 Apr 2012) $
 * <!-- $KeyWordsOff: $ -->
 */
public class XMLValidator {
    /** Tool name. */
    private static final String TOOL_NAME = "XML Validator";
    /** Constant for Warn. */
    private static final String WARN = "warn";
    /** Constant for Error. */
    private static final String ERROR = "error";
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());
    /** The SaxBuilder to use. */
    private SAXBuilder builder;
    /** The directory holding xml files to validate. */
    private static String inputFileDirectory;
    /** Verbose Flag */
    private boolean verbose = false;
    /** Meta Element Flag*/
    private boolean checkMetaFlag;

    /** Input file being validated */
    private String currentFile = null;
    /** The validator report object */
    private XVReport report = null;

    /**
     * JDOM parsing exception string for misplaced '&'.
     */
    private static final String AMP_PARSE_EXCEPTION = "The entity name must immediately follow "
            + "the '&' in the entity reference.";
    /**
     * JDOM parsing exception string for misplaced '<'.
     */
    private static final String LT_PARSE_EXCEPTION = "The content of elements must consist of "
            + "well-formed character data or markup.";

    /**
     * Constructor.
     * @param checkMetaFlag - flag to tell the XV to check meta elements or ignore them.
     */
    public XMLValidator(boolean checkMetaFlag) {
        builder = new LineNumberSAXBuilder();
        builder.setValidation(true);
        builder.setFeature(XMLConstants.SCHEMA_FEATURE, true);
        builder.setProperty(XMLConstants.SCHEMA_LOCATION_PROPERTY, XMLConstants.SCHEMA_LOCATION);
        this.checkMetaFlag = checkMetaFlag;
        report = new XVReport();
    };

    /**
     * Validates the list of xml files against the DataShop schema.
     * @param directory the name of the directory that holds the files.
     * @return a summary report containing number of files processed, number of files containing
     * errors, number of files containing warnings, and the total number of errors and warnings.
     * @throws IOException throws IOExcpetion if something goes wrong with the files
     */
    public XVReport validateTheFiles(String directory) throws IOException {
        if (verbose) {
            if (checkMetaFlag) {
                logger.info("XV set to check for meta elements.");
            } else {
                logger.info("XV set to not check for meta elements.");
            }
            logger.info("Getting files...");
        }
        List fileList = getFilenameList(directory);
        if (verbose) { logger.info("Reading and Validating ..."); }
        report.setNumFilesProcessed(fileList.size());

        for (int currListPosition = 0, numFilesToProcess = fileList.size();
                currListPosition < numFilesToProcess; currListPosition++) {
            File theFile = (File)fileList.get(currListPosition);
            setCurrentFile(theFile.getName());
            if (verbose) {
                logger.info("Reading file " + getCurrentFile()
                        + " (" + (currListPosition + 1) + "/" + numFilesToProcess + ")");
            }

            if (theFile.isFile()) {
                if (isValidFile(theFile)) {
                    report.increaseNumValidFiles();
                } else {
                    report.increaseNumErrorFiles();
                }
            }
        } // end for loop

        report.printReport();
        return report;
    } // end validateTheFiles()

    /**
     * Build a file into the schema and run the custom validation.
     * @param theFile File to validate.
     * @return true if the file validates, false otherwise.
     */
    public boolean isValidFile(File theFile) {
        if (currentFile == null) { setCurrentFile("XML File"); }
        try {
            Document xmlDoc = builder.build(theFile);
            if (verbose) { logger.info("File conforms to the schema."); }

            if (isDataShopCompliant(xmlDoc)) {
                return true;
            } else {
                logger.error("Invalid file " + getCurrentFile());
                return false;
            }
        } catch (JDOMException ex) {
            if ((ex.toString().indexOf(AMP_PARSE_EXCEPTION) > 0)
                    || (ex.toString().indexOf(LT_PARSE_EXCEPTION) > 0)) {
                report.increaseHTMLWarnings();
            }
            logger.error(ex.toString());
            report.increaseTotalErrors();
            return false;
        } catch (IOException ioException) {
            logger.error("IOException trying to parse build the file.", ioException);
            report.increaseTotalErrors();
            return false;
        }
    }

    /**
     * Performs additional schema validation on top of the general validation
     * provided through the SAXParser.
     * @param xmlDoc - the xml document to examine
     * @return false if any ERRORS are found, true otherwise.
     */
    private boolean isDataShopCompliant(Document xmlDoc) {
        boolean successFlag = true;
        for (Iterator iter = xmlDoc.getDescendants(new ElementFilter()); iter.hasNext();) {
            LineNumberElement element = (LineNumberElement) iter.next();
            checkElement(element);
            String elementName = element.getName();
            if (elementName != null) {
                if (elementName.equals(Message.CONTEXT_MSG_ELEMENT)) {
                    if (!checkContextMessage(element)) {
                        successFlag = false;
                    }
                } else if (elementName.equals(Message.TOOL_MSG_ELEMENT)) {
                    if (!checkToolMessage(element)) {
                        successFlag = false;
                    }
                } else if (elementName.equals(Message.TUTOR_MSG_ELEMENT)) {
                    if (!checkTutorMessage(element)) {
                        successFlag = false;
                    }
                } else if (elementName.equals(Message.MSG_ELEMENT)) {
                    logger.info(createIgnoreMsg(Message.MSG_ELEMENT));
                }
            } else {
                logger.error(createNameErrorMsg(element));
                report.increaseTotalErrors();
            }
        }
        return successFlag;
    }

    /**
     * Checks the content of the 'context_message' element.  Will check the 'meta' element if
     * the checkMetaFlag is set.  If 'class', 'condition', 'custom_field', 'skill' are present
     * check their content.  'dataset' should have a 'name' and 'level'.
     * @param message - the context_message to examine.
     * @return true if everything checks out, false otherwise
     */
    private boolean checkContextMessage(LineNumberElement message) {
        boolean successFlag = true;
        boolean metaFound = false;
        int contextMsgLineNum = message.getStartLine();
        if (!checkContextMsgID(message, Message.CONTEXT_MSG_ELEMENT)) {
            successFlag = false;
        }
        for (Iterator childrenIt = message.getDescendants(new ElementFilter());
                childrenIt.hasNext();) {
            LineNumberElement element = (LineNumberElement) childrenIt.next();
            String elementName = element.getName();
            if (elementName != null) {
                if (elementName.equals(XMLConstants.META_ELEMENT)) {
                    metaFound = true;
                    if (checkMetaFlag) {
                        if (!checkMeta(element, Message.CONTEXT_MSG_ELEMENT)) {
                            successFlag = false;
                        }
                    } else {
                        if (verbose) {
                            logger.info(createIgnoreMsg(XMLConstants.META_ELEMENT));
                        }
                    }
                } else if (elementName.equals(XMLConstants.CLASS_ELEMENT)) {
                    if (!checkClass(element)) {
                        successFlag = false;
                    }
                } else if (elementName.equals(XMLConstants.DATASET_ELEMENT)) {
                    if (!checkDataset(element)) {
                        successFlag = false;
                    }
                } else if (elementName.equals(XMLConstants.CONDITION_ELEMENT)) {
                    if (!checkCondition(element)) {
                        successFlag = false;
                    }
                } else if (elementName.equals(XMLConstants.CUSTOM_FIELD_ELEMENT)) {
                    if (!checkCustomField(element)) {
                        successFlag = false;
                    }
                } else if (elementName.equals(XMLConstants.SKILL_ELEMENT)) {
                    if (!checkSkill(element)) {
                        successFlag = false;
                    }
                }
            } else {
                logger.error(createNameErrorMsg(element));
                report.increaseTotalErrors();
                successFlag = false;
            }
        }
        if (checkMetaFlag) {
            if (!metaFound) {
                logger.warn("<" + XMLConstants.META_ELEMENT + "> element missing from the <"
                        + Message.CONTEXT_MSG_ELEMENT + "> at line " + contextMsgLineNum
                        + " in " + currentFile);
                report.increaseTotalWarnings();
            }
        }
        return successFlag;
    }

    /**
     * Checks on the context_message_id attribute.  If it exists for the provided
     * parent then check its value to make sure it is valid.
     * @param message the context_message, tool or tutor element
     * @param parent the parent of the contextMsgId (context, tool or tutor message)
     * @return true if everything checks out, false otherwise
     */
    private boolean checkContextMsgID(LineNumberElement message, String parent) {
        Attribute contextMsgId = message.getAttribute(XMLConstants.CONTEXT_MESSAGE_ID);
        return (checkAttribute(contextMsgId, XMLConstants.CONTEXT_MESSAGE_ID, message, ERROR));
    }

    /**
     * Checks the content of the 'tool_message' element.  Will check the 'meta' is checkMetaFlag
     * is set to true.  Either the 'semantic_event' or 'ui_event' element should have content.
     * @param message the tool_message to examine
     * @return false if any ERRORS are found, true otherwise.
     */
    private boolean checkToolMessage(LineNumberElement message) {
        boolean successFlag = true;
        boolean metaFound = false;
        if (!checkContextMsgID(message, Message.TOOL_MSG_ELEMENT)) {
            successFlag = false;
        }
        for (Iterator childrenIt = message.getDescendants(new ElementFilter());
                childrenIt.hasNext();) {
            LineNumberElement element = (LineNumberElement) childrenIt.next();
            String elementName = element.getName();
            if (elementName != null) {
                if (elementName.equals(XMLConstants.META_ELEMENT)) {
                    metaFound = true;
                    if (checkMetaFlag) {
                        if (!checkMeta(element, Message.TOOL_MSG_ELEMENT)) {
                            successFlag = false;
                        }
                    } else {
                        if (verbose) {
                            logger.info(createIgnoreMsg(XMLConstants.META_ELEMENT));
                        }
                    }
                } else if (elementName.equals(XMLConstants.PROBLEM_NAME_ELEMENT)) {
                    logger.warn(createProblemWarningMsg(element, Message.TOOL_MSG_ELEMENT));
                    report.increaseTotalWarnings();
                } else if (elementName.equals(XMLConstants.SEMANTIC_EVENT_ELEMENT)) {
                    if (!checkSemanticEvent(element, Message.TOOL_MSG_ELEMENT)) {
                        successFlag = false;
                    }
                } else if (elementName.equals(XMLConstants.EVENT_DESCRIPTOR_ELEMENT)) {
                    if (!checkEventDescriptor(element, Message.TOOL_MSG_ELEMENT)) {
                        successFlag = false;
                    }
                } else if (elementName.equals(XMLConstants.UI_EVENT_ELEMENT)) {
                    logger.info(createIgnoreMsg("UI_EVENT_ELEMENT"));
                } else if (elementName.equals(XMLConstants.CUSTOM_FIELD_ELEMENT)) {
                    if (!checkCustomField(element)) {
                        successFlag = false;
                    }
                } else if (elementName.equals(XMLConstants.REPLAY_ELEMENT)) {
                    if (verbose) {
                        logger.info(createIgnoreMsg(XMLConstants.REPLAY_ELEMENT));
                    }
                }
            } else {
                logger.error(createNameErrorMsg(element));
                report.increaseTotalErrors();
                successFlag = false;
            }
        }

        if (checkMetaFlag) {
            if (!metaFound) {
                logger.warn("<" + XMLConstants.META_ELEMENT + "> element missing from the <"
                        + Message.TOOL_MSG_ELEMENT + ">");
                report.increaseTotalWarnings();
            }
        }
        return successFlag;
    }

    /**
     * Checks the content of the 'tutor_message' element.  Will check the 'meta' element if
     * the checkMetaFlag is set to ture.  Warn if 'problem_name' is present.
     * The 'event_descriptor' should have at least one child element whose content is not empty.
     * If 'skill' is present check for its content - same for 'custom_field'.
     * @param message the tutor_message to examine
     * @return false is any ERRORS are found, true otherwise
     */
    private boolean checkTutorMessage(LineNumberElement message) {
        boolean successFlag = true;
        boolean metaFound = false;
        if (!checkContextMsgID(message, Message.TUTOR_MSG_ELEMENT)) {
            successFlag = false;
        }
        for (Iterator childrenIt = message.getDescendants(new ElementFilter());
                childrenIt.hasNext();) {
            LineNumberElement element = (LineNumberElement) childrenIt.next();
            String elementName = element.getName();
            if (elementName != null) {
                if (elementName.equals(XMLConstants.META_ELEMENT)) {
                    metaFound = true;
                    if (checkMetaFlag) {
                        if (!checkMeta(element, Message.TUTOR_MSG_ELEMENT)) {
                            successFlag = false;
                        }
                    } else {
                        if (verbose) {
                            logger.info(createIgnoreMsg(XMLConstants.META_ELEMENT));
                        }
                    }
                } else if (elementName.equals(XMLConstants.PROBLEM_NAME_ELEMENT)) {
                    logger.warn(createProblemWarningMsg(element, Message.TUTOR_MSG_ELEMENT));
                    report.increaseTotalWarnings();
                } else if (elementName.equals(XMLConstants.SEMANTIC_EVENT_ELEMENT)) {
                    if (!checkSemanticEvent(element, Message.TUTOR_MSG_ELEMENT)) {
                        successFlag = false;
                    }
                } else if (elementName.equals(XMLConstants.EVENT_DESCRIPTOR_ELEMENT)) {
                    if (!checkEventDescriptor(element, Message.TUTOR_MSG_ELEMENT)) {
                        successFlag = false;
                    }
                } else if (elementName.equals(XMLConstants.ACTION_EVALUATION_ELEMENT)) {
                    if (element.getText().equals(XMLConstants.EMPTY_STRING)) {
                        logger.warn(createChildErrorMsg(element,
                                Message.TUTOR_MSG_ELEMENT));
                        report.increaseTotalWarnings();
                    }
                } else if (elementName.equals(XMLConstants.SKILL_ELEMENT)) {
                    if (!checkSkill(element)) {
                        successFlag = false;
                        logger.error(createChildErrorMsg(element, Message.TUTOR_MSG_ELEMENT));
                        report.increaseTotalErrors();
                    }
                } else if (elementName.equals(XMLConstants.CUSTOM_FIELD_ELEMENT)) {
                    if (!checkCustomField(element)) {
                        successFlag = false;
                    }
                } else if (elementName.equals(XMLConstants.UI_EVENT_ELEMENT)) {
                    logger.info(createIgnoreMsg(XMLConstants.UI_EVENT_ELEMENT));
                } else if (elementName.equals(XMLConstants.REPLAY_ELEMENT)) {
                    if (verbose) {
                        logger.info(createIgnoreMsg(XMLConstants.REPLAY_ELEMENT));
                    }
                } else if (elementName.equals(XMLConstants.INTERPRETATION_ELEMENT)) {
                    logger.info(createIgnoreMsg(XMLConstants.INTERPRETATION_ELEMENT));
                }
             } else {
                 logger.error(createNameErrorMsg(element));
                 report.increaseTotalErrors();
                successFlag = false;
            }
        }
        if (checkMetaFlag) {
            if (!metaFound) {
                logger.warn("<" + XMLConstants.META_ELEMENT + "> element missing from the <"
                        + Message.TUTOR_MSG_ELEMENT + ">");
                report.increaseTotalWarnings();
            }
        }
        return successFlag;
    }

    /**
     * Checks the value of the provided attribute.  First check for null.  If null print
     * the appropriate message type (error or warn). If it is equal to the EMPTY_STRING
     * print the appropriate message type (error or warn).
     * @param attribute - the attribute to examine
     * @param attributeName - the name of the attribute (needed if the attribute turns out
     *          to be null)
     * @param parent - the parent for the given attribute
     * @param messageType - type of logger message to be written (error or warn)
     * @return true if value is valid, false otherwise
     */
    private boolean checkAttribute(Attribute attribute, String attributeName,
            LineNumberElement parent, String messageType) {
        boolean successFlag = true;
        if (attribute == null) {
            if (messageType.equals(ERROR)) {
                logger.error(createNullAttributeMsg(attributeName, parent.getName()));
                report.increaseTotalErrors();
                successFlag = false;
            } else { // must be a warn
                logger.warn(createNullAttributeMsg(attributeName, parent.getName()));
                report.increaseTotalWarnings();
            }
        } else {
            if (attribute.getValue() == XMLConstants.EMPTY_STRING) {
                if (messageType.equals(ERROR)) {
                    logger.error(createAttributeErrorMsg(attribute, parent.getStartLine(),
                            parent.getName()));
                    report.increaseTotalErrors();
                    successFlag = false;
                } else { // must be a warn
                    logger.warn(createAttributeErrorMsg(attribute, parent.getStartLine(),
                            parent.getName()));
                    report.increaseTotalWarnings();
                }
            }
        }
        return successFlag;
    }



    /**
     * Checks the content of the <class> element.  It should not be completely empty.
     * Also check the length of the data for any elements that are present.
     * @param classElement the <class> element to examine
     * @return true always, since any problems with <class> are only a warning, not an error
     */
    private boolean checkClass (LineNumberElement classElement) {
        // First check that the class empty is not completely empty.  If so, that's a warning.
        if (classElement.getChildren().size() == 0) {
            logger.warn(createChildErrorMsg(classElement, Message.CONTEXT_MSG_ELEMENT));
            report.increaseTotalWarnings();

        // Then check if the contents of the sub-elements are not too long.
        } else {
            for (Iterator childrenIt = classElement.getDescendants(new ElementFilter());
                    childrenIt.hasNext();) {
                LineNumberElement element = (LineNumberElement) childrenIt.next();
                String elementName = element.getName();
                String elementText = element.getText();

                if (elementName.equals(XMLConstants.NAME_ELEMENT)) {
                    if (elementText.equals(XMLConstants.EMPTY_STRING)) {
                        logger.warn(createChildErrorMsg(element,
                                XMLConstants.CLASS_ELEMENT));
                        report.increaseTotalWarnings();
                    } else if (elementText.length() > XMLConstants.CLASS_NAME_LENGTH) {
                        logger.warn(createLengthErrorMsg(element,
                                XMLConstants.CLASS_ELEMENT, XMLConstants.CLASS_NAME_LENGTH));
                        report.increaseTotalWarnings();
                    }
                } else if (elementName.equals(XMLConstants.SCHOOL_ELEMENT)) {
                    if (elementText.equals(XMLConstants.EMPTY_STRING)) {
                        logger.warn(createChildErrorMsg(element,
                                XMLConstants.CLASS_ELEMENT));
                        report.increaseTotalWarnings();
                    } else if (elementText.length() > XMLConstants.SCHOOL_NAME_LENGTH) {
                        logger.warn(createLengthErrorMsg(element,
                                XMLConstants.CLASS_ELEMENT, XMLConstants.SCHOOL_NAME_LENGTH));
                        report.increaseTotalWarnings();
                    }
                } else if (elementName.equals(XMLConstants.PERIOD_ELEMENT)) {
                    if (elementText.equals(XMLConstants.EMPTY_STRING)) {
                        logger.warn(createChildErrorMsg(element,
                                XMLConstants.CLASS_ELEMENT));
                        report.increaseTotalWarnings();
                    } else if (elementText.length() > XMLConstants.PERIOD_NAME_LENGTH) {
                        logger.warn(createLengthErrorMsg(element,
                                XMLConstants.CLASS_ELEMENT, XMLConstants.PERIOD_NAME_LENGTH));
                        report.increaseTotalWarnings();
                    }
                } else if (elementName.equals(XMLConstants.DESCRIPTION_ELEMENT)) {
                    if (elementText.equals(XMLConstants.EMPTY_STRING)) {
                        logger.warn(createChildErrorMsg(element,
                                XMLConstants.CLASS_ELEMENT));
                        report.increaseTotalWarnings();
                    } else if (elementText.length() > XMLConstants.DESCRIPTION_LENGTH) {
                        logger.warn(createLengthErrorMsg(element,
                                XMLConstants.CLASS_ELEMENT, XMLConstants.DESCRIPTION_LENGTH));
                        report.increaseTotalWarnings();
                    }
                } else if (elementName.equals(XMLConstants.INSTRUCTOR_ELEMENT)) {
                    if (elementText.equals(XMLConstants.EMPTY_STRING)) {
                        logger.warn(createChildErrorMsg(element,
                                XMLConstants.CLASS_ELEMENT));
                        report.increaseTotalWarnings();
                    } else if (elementText.length() > XMLConstants.INSTRUCTOR_NAME_LENGTH) {
                        logger.warn(createLengthErrorMsg(element,
                                XMLConstants.CLASS_ELEMENT, XMLConstants.INSTRUCTOR_NAME_LENGTH));
                        report.increaseTotalWarnings();
                    }
                }
            }
        }
        return true; //return true as only warnings are produced in this method
    }

    /**
     * Checks the content of the <condition> element.  <name> should
     * have valid content.
     * @param conditionElement the condition element to examine
     * @return true if everything checks out, false otherwise
     */
    private boolean checkCondition(Element conditionElement) {
        boolean successFlag = true;
        for (Iterator childrenIt = conditionElement.getDescendants(new ElementFilter());
                childrenIt.hasNext();) {
            LineNumberElement element = (LineNumberElement) childrenIt.next();
            String elementName = element.getName();
            if (elementName.equals(XMLConstants.NAME_ELEMENT)) {
                if (!(checkName(element, XMLConstants.CONDITION_NAME_LENGTH,
                        XMLConstants.CONDITION_ELEMENT))) {
                    successFlag = false;
                }
            }
        }
        return successFlag;
    }

    /**
     * Checks the content of the 'custom_field' element.  Both 'name' & 'value'
     * should have valid content.
     * @param customField the 'custom_field' element to examine
     * @return true if everything checks out, false otherwise
     */
    private boolean checkCustomField (Element customField) {
        boolean successFlag = true;
        for (Iterator childrenIt = customField.getDescendants(new ElementFilter());
                childrenIt.hasNext();) {
            LineNumberElement element = (LineNumberElement) childrenIt.next();
            String elementName = element.getName();
            if (elementName.equals(XMLConstants.NAME_ELEMENT)) {
                if (!checkName(element, XMLConstants.CUSTOM_FIELD_NAME_LENGTH,
                        XMLConstants.CUSTOM_FIELD_ELEMENT)) {
                    successFlag = false;
                }
            } else if (elementName.equals(XMLConstants.VALUE_ELEMENT)) {
                if (element.getText().equals(XMLConstants.EMPTY_STRING)) {
                    successFlag = false;
                    logger.error(createChildErrorMsg(element, XMLConstants.CUSTOM_FIELD_ELEMENT));
                    report.increaseTotalErrors();
                }
            }
        }
        return successFlag;
    }

    /**
     * Checks the content of the <dataset> element.  Both <name> and <level>
     * should have valid content.
     * @param dataset the dataset element to examine
     * @return true if everything checks out, false otherwise
     */
    private boolean checkDataset(LineNumberElement dataset) {
        boolean successFlag = true;
        for (Iterator childrenIt = dataset.getDescendants(new ElementFilter());
                childrenIt.hasNext();) {
            LineNumberElement element = (LineNumberElement) childrenIt.next();
            String elementName = element.getName();
            if (elementName.equals(XMLConstants.NAME_ELEMENT)) {
                if (!checkName(element, XMLConstants.DATASET_NAME_LENGTH,
                        XMLConstants.DATASET_ELEMENT)) {
                    successFlag = false;
                }
            } else if (elementName.equals(XMLConstants.LEVEL_ELEMENT)) {
                if (!checkLevel(element)) {
                    successFlag = false;
                }
            }
        }
        return successFlag;
    }

    /**
     * Checks the content of the 'event_descriptor' element.  It should not be
     * completely empty (should have at least 1 SAI).  If SAI are present, they should
     * have content.
     * @param eventDescriptor the 'event_descriptor' element to examine
     * @param parent who this 'event_descriptor' belongs to (tool or tutor)
     * @return true if everything checks out, false otherwise
     */
    private boolean checkEventDescriptor (LineNumberElement eventDescriptor, String parent) {
        boolean successFlag = true;
        int size = eventDescriptor.getChildren().size();
        if (size == 0) {
            logger.warn(createChildErrorMsg(eventDescriptor, parent));
            report.increaseTotalWarnings();
        }
        for (Iterator childrenIt = eventDescriptor.getDescendants(new ElementFilter());
                childrenIt.hasNext();) {
            LineNumberElement element = (LineNumberElement) childrenIt.next();
            String elementName = element.getName();
            if (elementName.equals(XMLConstants.SELECTION_ELEMENT)) {
                if (element.getText().equals(XMLConstants.EMPTY_STRING)) {
                    logger.warn(createChildErrorMsg(element,
                            XMLConstants.EVENT_DESCRIPTOR_ELEMENT));
                    report.increaseTotalWarnings();
                }
            } else if (elementName.equals(XMLConstants.ACTION_ELEMENT)) {
                if (element.getText().equals(XMLConstants.EMPTY_STRING)) {
                    logger.warn(createChildErrorMsg(element,
                            XMLConstants.EVENT_DESCRIPTOR_ELEMENT));
                    report.increaseTotalWarnings();
                }
            } else if (elementName.equals(XMLConstants.INPUT_ELEMENT)) {
                if (element.getText().equals(XMLConstants.EMPTY_STRING)) {
                    logger.warn(createChildErrorMsg(element,
                            XMLConstants.EVENT_DESCRIPTOR_ELEMENT));
                    report.increaseTotalWarnings();
                }
            }
        }
        return successFlag;
    }

    /**
     * Checks the content of the 'meta' element.  All children should
     * have content ('user_id', 'session_id', 'time' and 'time_zone'.  Also check
     * the format of the time.
     * @param meta the meta element to examine
     * @param parent the parent element of the meta tag.
     * @return true if everything checks out, false otherwise
     */
    private boolean checkMeta(LineNumberElement meta, String parent) {
        boolean successFlag = true;
        boolean timePresent = false;
        LineNumberElement element = (LineNumberElement) meta.getChild(XMLConstants.USER_ID_ELEMENT);
        if (element.getText().equals(XMLConstants.EMPTY_STRING)) {
            successFlag = false;
            logger.error(createChildErrorMsg(element, XMLConstants.META_ELEMENT));
            report.increaseTotalErrors();
        }
        element = (LineNumberElement) meta.getChild(XMLConstants.SESSION_ID_ELEMENT);
        if (element.getText().equals(XMLConstants.EMPTY_STRING)) {
            successFlag = false;
            logger.error(createChildErrorMsg(element, XMLConstants.META_ELEMENT));
            report.increaseTotalErrors();
        }
        element = (LineNumberElement) meta.getChild(XMLConstants.TIME_ELEMENT);
        String time = element.getTextTrim();
        if (time.equals(XMLConstants.EMPTY_STRING)) {
            successFlag = false;
            logger.error(createChildErrorMsg(element, XMLConstants.META_ELEMENT));
            report.increaseTotalErrors();
        } else {
            timePresent = true;
        }
        element = (LineNumberElement) meta.getChild(XMLConstants.TIME_ZONE_ELEMENT);
        String timeZone = element.getTextTrim();
        if (timeZone.equals(XMLConstants.EMPTY_STRING)) {
            successFlag = false;
            timeZone = null;
            logger.error(createChildErrorMsg(element, XMLConstants.META_ELEMENT));
            report.increaseTotalErrors();
        }
        if (timePresent) {
            if (!DateTools.checkDate(time, timeZone)) {
                successFlag = false;
                logger.error("Unable to parse the <" + XMLConstants.TIME_ELEMENT
                        + "> and <" + XMLConstants.TIME_ZONE_ELEMENT + "> in the <"
                        + XMLConstants.META_ELEMENT + "> at line " + meta.getStartLine()
                        + " in '" + getCurrentFile() + "'.");
                report.increaseTotalErrors();
            }
        }
        return successFlag;
    }

    /**
     * Checks the content of the <level> element.  <level> either has another <level> or
     * <problem> child.  <name> should also contain valid content.  This method can
     * operate recursively.
     * @param level - the level element to examine
     * @return true if everything checks out, false otherwise
     */
    private boolean checkLevel (LineNumberElement level) {
        boolean successFlag = true;
        Attribute type = level.getAttribute("type");
        checkAttribute(type, "type", level, WARN);
        LineNumberElement element = (LineNumberElement) level.getChild(XMLConstants.LEVEL_ELEMENT);
        if (element != null) {
            if (!checkLevel(element)) {
                successFlag = false;
            }
        }
        element = (LineNumberElement) level.getChild(XMLConstants.PROBLEM_ELEMENT);
        if (element != null) {
            if (!checkProblem(element)) {
                successFlag = false;
            }
        }
        return successFlag;
    }

    /**
     * Checks the <name> element has content and its length.
     * @param element the name element to examine
     * @param length the maximum length for this name
     * @param parent the parent element of the name.
     * @return true if <name> has content, false otherwise
     */
    private boolean checkName (LineNumberElement element, Integer length, String parent) {
        boolean successFlag = true;
        String textValue = element.getText().trim();
        if (textValue.equals(XMLConstants.EMPTY_STRING)) {
            successFlag = false;
            logger.error(createChildErrorMsg(element, parent));
            report.increaseTotalErrors();
        } else if (length != null && (textValue.length() > length)) {
            successFlag = false;
            logger.error(createLengthErrorMsg(element, parent, length));
            report.increaseTotalErrors();
        }
        return successFlag;
    }

    /**
     * Checks the content of the element.  Issues a warning if the
     * element contains HTML-ish characters.
     * @param ele the element to examine
     */
    private void checkElement(LineNumberElement ele) {
        List<org.jdom.Content> contents = ((Element)ele).getContent();
        if ((contents.size() == 1) && (contents.get(0) instanceof org.jdom.CDATA)) {
            // Contents of 'ele' is well-formed CDATA; we're done.
            return;
        }

        // At this point, if special characters exist, it isn't well-formed CDATA.
        String theText = ele.getTextTrim();
        if (specialCharactersPresent(theText)) {
            if (verbose) {
                logger.warn(createHTMLWarningMsg(ele));
            }
            report.increaseHTMLWarnings();
        }
    }

    /**
     * Checks for the presence of special characters, specifically: <, > and &.
     * Returns true if any special characters are present.
     * @param theText String to check for special characters
     * @return result flag indicating presence of special characters
     */
    private boolean specialCharactersPresent(String theText) {
        boolean result = false;
        if ((theText.indexOf('&') > -1)
                || (theText.indexOf('<') > -1)
                || (theText.indexOf('>') > -1)) {
            result = true;
        }
        return result;
    }

    /**
     * Checks the content of the <problem> element.  The <name> element
     * should have valid content.
     * @param problem the problem element to examine
     * @return true if everything checks out, false otherwise
     */
    private boolean checkProblem (LineNumberElement problem) {
        boolean successFlag = true;
        LineNumberElement element = (LineNumberElement) problem.getChild(XMLConstants.NAME_ELEMENT);
        if (!checkName(element, XMLConstants.PROBLEM_NAME_LENGTH,
                XMLConstants.PROBLEM_ELEMENT)) {
            successFlag = false;
        }
        return successFlag;
    }

    /**
     * Checks the content of the 'semantic_event' element.
     * @param semanticEvent the 'semantic_event' element to examine
     * @param parent who this 'semantic_event' belongs to (tool or tutor)
     * @return true if everything checks out, false otherwise
     */
    private boolean checkSemanticEvent (LineNumberElement semanticEvent, String parent) {
        boolean successFlag = true;
        Attribute name = semanticEvent.getAttribute("name");
        checkAttribute(name, "name", semanticEvent, WARN);
        return successFlag;
    }

    /**
     * Checks the content of the <skill> element.  The <name> element
     * should have valid content.
     * @param skill the skill element to examine
     * @return true if everything checks out, false otherwise
     */
    private boolean checkSkill (LineNumberElement skill) {
        boolean successFlag = true;
        LineNumberElement element = (LineNumberElement) skill.getChild(XMLConstants.NAME_ELEMENT);
        if (!checkName(element, null, XMLConstants.SKILL_ELEMENT)) {
            successFlag = false;
        }
        return successFlag;
    }

    /**
     * Creates an error message indicating the provided attribute does not have
     * a valid value.
     * @param attribute the attribute with the bad value
     * @param lineNumber the line at which the attribute is located
     * @param parent the parent of the attribute
     * @return the custom error message
     */
    private String createAttributeErrorMsg(Attribute attribute, int lineNumber, String parent) {
        String msg = "Attribute '" + attribute.getName() + "' for <" + parent
            + "> at line " + lineNumber + " in '" + getCurrentFile() + "' does not have a value.";
        return msg;
    }

    /**
     * Creates an error message for the provided child/parent pair.
     * @param child the child who is missing some content
     * @param parent the child's parent element
     * @param maxLength the max length of the content of the given element
     * @return the custom error message
     */
    private String createLengthErrorMsg(LineNumberElement child, String parent, int maxLength) {
        String msg = "Content is too long for child <" + child.getName()
            + "> in parent <" + parent + "> at line " + child.getStartLine()
            + " in '" + getCurrentFile() + "'. Max length is " + maxLength + ".";
        return msg;
    }

    /**
     * Creates an error message for the provided child/parent pair.
     * @param child the child who is missing some content
     * @param parent the child's parent element
     * @return the custom error message
     */
    private String createChildErrorMsg(LineNumberElement child, String parent) {
        String msg = "Content is missing for child <" + child.getName()
            + "> in parent <" + parent + "> at line " + child.getStartLine()
            + " in '" + getCurrentFile() + "'.";
        return msg;
    }

    /**
     * Creates an error message stating the given element has an invalid name.
     * @param element the element who has an invalid name
     * @return the custom error message
     */
    private String createNameErrorMsg(LineNumberElement element) {
        String msg = "The element at line " + element.getStartLine()
                + " in " + currentFile + " does not have a valid name";
        return msg;
    }

    /**
     * Creates an "ignore" message string for the provided element.
     * @param element the element that's being snubbed
     * @return the custom ignore message
     */
    private String createIgnoreMsg(String element) {
        String msg = "Ignoring the <" + element + "> element.";
        return msg;
    }

    /**
     * Creates a warning message for a 'problem_name' element found in a tool
     * or tutor_message element.
     * @param element the 'problem_name' element
     * @param parent where the 'problem_name' lives (tool or tutor)
     * @return the custom warning message
     */
    private String createProblemWarningMsg(LineNumberElement element, String parent) {
        String msg = "<" + element.getName() + "> in the <" + parent
                + "> at line " + element.getStartLine() + " in " + currentFile
                + " is redundant.  Make sure <problem> is included in the <"
                + Message.CONTEXT_MSG_ELEMENT + ">";
        return msg;
    }

    /**
     * Creates a "does not exist" message for the provided attribute/parent pair.
     * @param attribute the attribute that does not exist
     * @param parent the lonely parent
     * @return the custom message
     */
    private String createNullAttributeMsg(String attribute, String parent) {
        String msg = "Attribute '" + attribute + "' for <" + parent
            + "> in '" + getCurrentFile() + "' does not exist.";
        return msg;
    }

    /**
     * Creates a warning message stating that the given element has content
     * which might be non-CDATA HTML, which can cause problems for import.
     * @param element the element that has the questionable content
     * @return the custom warning message
     */
    private String createHTMLWarningMsg(LineNumberElement element) {
        String msg = "The element at line " + element.getStartLine()
                + " in " + currentFile + " has HTML content which may cause parsing problems: "
                + "\"" + element.getTextTrim() + "\"";
        return msg;
    }

    /**
     * Sets the name of the file currently being validated.
     * @param fileName the name of the file
     */
    private void setCurrentFile(String fileName) {
        this.currentFile = fileName;
    }

    /**
     * Gets the name of the file currently being validated.
     * @return the name of the file
     */
    private String getCurrentFile() {
        return this.currentFile;
    }

    /**
     * Returns an array of files given a directory.
     * @param directoryName the directory path
     * @return an array of files
     */
    private List getFilenameList(String directoryName) {

        File topLevelDirectory = new File(directoryName);

        if (!topLevelDirectory.isDirectory()) {
            logger.error("Not a directory: " + directoryName);
            report.increaseTotalErrors();
        } else if (logger.isDebugEnabled()) {
            logger.debug("Directory found: " + directoryName);
        }

        logger.info("Top level directory is " + topLevelDirectory.getName());

        return getFiles(topLevelDirectory);
    }

    /**
     * A recursive function to return a list of all the files in a top
     * level directory, including all the subdirectories.
     * This method will skip CVS directories.
     * @param theFile a file or directory
     * @return a complete list of the files in this directory
     */
    private List getFiles(File theFile) {
        List fileList = new ArrayList();

        if (theFile.isFile() && isXMLFile(theFile)) {
            if (verbose) {
                logger.info("Adding file " + theFile.getName());
            }
            fileList.add(theFile);
        } else if (theFile.isDirectory()) {
            if (theFile.getName().equals("CVS")) {
                if (verbose) {
                    logger.info("Skipping directory " + theFile.getName());
                }
            } else {
                if (verbose) {
                    logger.info("Found directory " + theFile.getName());
                }
                File[] files = theFile.listFiles();
                for (int idx = 0; idx < files.length; idx++) {
                    if (files[idx].isFile() && isXMLFile(files[idx])) {
                        if (verbose) {
                            logger.info("Adding file " + files[idx].getName());
                        }
                        fileList.add(files[idx]);
                    } else if (files[idx].isDirectory()) {
                        List moreFiles = getFiles(files[idx]);
                        fileList.addAll(moreFiles);
                    }
                } // end for loop
            }
        } // end else
        return fileList;
    }

    /**
     * Looks at the suffix for the given file.  If it is an XML file return true,
     * else return false.
     * @param theFile the file to check
     * @return true if an xml file, false otherwise
     */
    public boolean isXMLFile(File theFile) {
        String fileName = theFile.getName();
        if (fileName != null && fileName.endsWith(XMLConstants.XML_SUFFIX)) {
            return true;
        }
        return false;
    }

    /**
     * Display the usage of this utility.
     */
    public void displayUsage() {
        logger.info("USAGE: java -classpath ... "
                     + TOOL_NAME
                     + " [-inputDir input_directory_name]");
        logger.info("Options:");
        logger.info("\t-i, -inputDir   \t The name of the input directory");
        logger.info("\t-h, -help          \t Display this help and exit");
        logger.info("\t-v, -verbose       \t Run the tool in verbose mode");
        logger.info("\t-version          \t Display the version and exit");
        System.exit(-1);
    }

    /**
     * Handle the command line arguments.
     * @param args command line arguments passed into main
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
            } else if (args[i].equals("-i") || args[i].equals("-inputDir")) {
                if (++i < args.length) {
                    inputFileDirectory = args[i];
                    logger.debug("found the input directory");
                } else {
                    logger.error("A directory name must be specified with this argument");
                    System.exit(1);
                }
            } else if (args[i].equals("-v") || args[i].equals("-verbose")) {
                verbose = true;
                logger.info("XV running in verbose mode.");
            } else if (args[i].equals("-version")) {
                logger.info(VersionInformation.getReleaseString());
                System.exit(0);
            }
        } // end for loop

        // check for the required arguments
        if (inputFileDirectory == null) {
            logger.error("A directory name is required.");
            displayUsage();
            System.exit(1);
        }
    } // end handleOptions

    /**
     * Main.
     * @param args the list of command line arguments
     */
    public static void main(String[] args) {
        Logger logger = Logger.getLogger("XMLValidator.main");
        String version = VersionInformation.getReleaseString();
        logger.info("XMLValidator starting (" + version + ")...");
        XMLValidator validator = null;
        try {
            // create the XV, passing true to set metaFlag value to true
            validator = new XMLValidator(true);

            // parse command line options
            validator.handleOptions(args);

            // perform schema and custom validation
            validator.validateTheFiles(inputFileDirectory);

        } catch (Throwable throwable) {
            logger.error("Unknown error occurred:" + throwable.getMessage(), throwable);
        }
        logger.info("XMLValidator finished.");
    } // end main()


} // end XMLValidator
