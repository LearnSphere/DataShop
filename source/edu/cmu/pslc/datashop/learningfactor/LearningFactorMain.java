/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.learningfactor;

import java.util.List;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.extractors.ffi.ImportQueue;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.util.VersionInformation;

/**
 * This program initiates the learning factors algorithm (LfA)
 * during log conversion.
 *
 * @author Alida Skogsholm
 * @version $Revision: 13139 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2016-04-20 10:54:15 -0400 (Wed, 20 Apr 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public final class LearningFactorMain extends AbstractLearningFactor {

  /** Debug logging. */
  private Logger logger = Logger.getLogger(getClass().getName());

  /**
   * The dataset id of the dataset to run this algorithm on.
   * Set in the handleOptions method.
   * Optional parameter.
   */
  private String datasetIdString = null;

  /**
   * The skill model id of the dataset to run this algorithm on.
   * If not included but dataset is, then algorithm will run on
   * all skill models.
   * Set in the handleOptions method.
   * Optional parameter.
   */
  private String skillModelIdString = null;

  /**
   * If set, forces the algorithm to run, ignoring the dataset's
   * last modified time.
   * Optional parameter, which is set in the handleOptions method.
   */
  private boolean forceLFAFlag = false;

  /**
   * Optional parameter set in the handleOptions method which indicates
   * whether to force a Cross Validation run or not.
   */
  private boolean forceCVFlag = false;

  /**
   * Indicates whether not to run the LFA calculator.
   * Set in the handleOptions method.
   * Optional parameter.
   */
  private boolean noRunLFAFlag = false;

  /**
   * Optional parameter set in the handleOptions method which indicates
   * whether to skip the Cross Validation run or not.
   */
  private boolean noRunCVFlag = false;

  /**
   * Indicates whether the SSSS text file should be written to a file.
   */
  private boolean outputSSSSFlag = false;

    /** Flag indicating whether to skip datasets marked as junk.  Default is false.*/
    private boolean skipJunkFlag = false;

  /**
   * This is a private constructor and intentionally left blank.
   */
  protected LearningFactorMain() {
  }

  /**
   * Display the usage of this utility.
   */
  protected void displayUsage() {
      System.err.println("\nUSAGE: java -classpath ..."
          + " LearningFactorMain [-dataset dataset_id] [-model skill_model_id]"
          + " [-force] [-output] [-norun]");
      System.err.println("Option descriptions:");
      System.err.println("\t-dataset id \t\t\t dataset id");
      System.err.println("\t-model id   \t\t\t skill model id");
      System.err.println("\t-limit #    \t\t\t number of skills limit");
      System.err.println("\t-force both/LFA/CV     \t\t\t ignore dataset modified time");
      System.err.println("\t-CVFold     \t\t\t how many of fold for cross validation");
      System.err.println("\t-CVRun     \t\t\t how many of times to run cross validation");
      System.err.println("\t-output     \t\t\t output the SSSS text file");
      System.err.println("\t-norun both/LFA/CV     \t\t\t do not run the LFA/CV calculator");
      System.err.println("\t-e, email\t send email if major failure");
      System.err.println("\t-h, -help   \t\t\t print this help message");
  }

  /**
   * Handle the command line arguments.
   * @param args - command line arguments passed into main
   * @return returns null if no exit is required,
     * 0 if exiting successfully (as in case of -help),
     * or any other number to exit with an error status
   */
  protected Integer handleOptions(String[] args) {
    // The value is null if no exit is required,
      // 0 if exiting successfully (as in case of -help),
      // or any other number to exit with an error status
      Integer exitLevel = null;
      if (args != null && args.length != 0) {

          java.util.ArrayList argsList = new java.util.ArrayList();
          for (int i = 0; i < args.length; i++) {
              argsList.add(args[i]);
          }

          // loop through the arguments
          for (int i = 0; i < args.length; i++) {
              if (args[i].equals("-h")) {
                  displayUsage();
                  exitLevel = 0;
              } else if (args[i].equals("-help")) {
                  displayUsage();
                  exitLevel = 0;
              } else if (args[i].equals("-e") || args[i].equals("-email")) {
                  setSendEmailFlag(true);
                  if (++i < args.length) {
                      setEmailAddress(args[i]);
                  } else {
                      System.err.println(
                          "Error: a email address must be specified with this argument");
                      displayUsage();
                      exitLevel = 1;
                  }
              } else if (args[i].equals("-dataset")) {
                  if (++i < args.length) {
                      datasetIdString = args[i];
                      logger.debug("Dataset: " + datasetIdString);
                  } else {
                      System.err.println(
                          "Error: a database id must be specified with this argument");
                      displayUsage();
                      exitLevel = 1;
                  }
              } else if (args[i].equals("-model")) {
                  if (++i < args.length) {
                      skillModelIdString = args[i];
                  } else {
                      System.err.println(
                          "Error: a skill model id must be specified with this argument");
                      displayUsage();
                      exitLevel = 1;
                  }
              } else if (args[i].equals("-limit")) {
                  if (++i < args.length) {
                      try {
                          Integer limit = new Integer(args[i]);
                          setNumSkillsLimit(limit.intValue());
                          logger.info("Limit: " + limit);
                      } catch (NumberFormatException exception) {
                          logger.warn("Invalid limit: " + args[i]);
                      }
                  } else {
                      System.err.println(
                          "Error: a number must be specified with this argument");
                      displayUsage();
                      exitLevel = 1;
                  }
              } else if (args[i].equals("-memorylimit")) {
                      if (++i < args.length) {
                              try {
                                  Integer limit = new Integer(args[i]);
                                  this.setMemoryLimit(limit.intValue());
                                  logger.info("Limit for memory: " + limit);
                              } catch (NumberFormatException exception) {
                                  logger.warn("Invalid memory limit: " + args[i]);
                              }
                          } else {
                              System.err.println(
                                  "Error: a number must be specified with this argument");
                              displayUsage();
                              exitLevel = 1;
                          }
              } else if (args[i].equals("-force")) {
                  if (++i < args.length) {
                      String forceOption = args[i];
                      if (forceOption.equalsIgnoreCase("both")) {
                          forceLFAFlag = true;
                          forceCVFlag = true;
                      } else if (forceOption.equalsIgnoreCase("lfa")) {
                          forceLFAFlag = true;
                          forceCVFlag = false;
                      } else if (forceOption.equalsIgnoreCase("cv")) {
                          forceLFAFlag = false;
                          forceCVFlag = true;
                      }
                  } else {
                      System.err.println(
                          "Error: an option must be specified with this argument");
                      displayUsage();
                      exitLevel = 1;
                  }
              } else if (args[i].equals("-norun")) {
                  if (++i < args.length) {
                      String noRunOption = args[i];
                      if (noRunOption.equalsIgnoreCase("both")) {
                          noRunLFAFlag = true;
                          noRunCVFlag = true;
                      } else if (noRunOption.equalsIgnoreCase("lfa")) {
                          noRunLFAFlag = true;
                          noRunCVFlag = false;
                      } else if (noRunOption.equalsIgnoreCase("cv")) {
                          noRunLFAFlag = false;
                          noRunCVFlag = true;
                      }
                  } else {
                      System.err.println(
                          "Error: an option must be specified with this argument");
                      displayUsage();
                      exitLevel = 1;
                  }
              } else if (args[i].equals("-output")) {
                  outputSSSSFlag = true;
              } else if (args[i].equals("-CVFold")) {
                      if (++i < args.length) {
                              try {
                                      int cvFold = Integer.parseInt(args[i]);
                                      setNumCVFold(cvFold);
                                      logger.debug("cvFold: " + cvFold);
                              } catch (NumberFormatException ex) {
                                      System.err.println(
                                                      "Error: an integer must be specified with this argument");
                                      displayUsage();
                                      exitLevel = 1;
                              }
                      } else {
                              System.err.println(
                                  "Error: number of cross validation fold must be specified with this argument");
                              displayUsage();
                              exitLevel = 1;
                      }
              } else if (args[i].equals("-CVRun")) {
                      if (++i < args.length) {
                              try {
                                      int cvRun = Integer.parseInt(args[i]);
                                      setNumCVRun(cvRun);
                                      logger.debug("cvRun: " + cvRun);
                              } catch (NumberFormatException ex) {
                                      System.err.println(
                                                      "Error: an integer must be specified with this argument");
                                      displayUsage();
                                      exitLevel = 1;
                              }
                      } else {
                              System.err.println(
                                  "Error: a number of cross validation runs must be specified with this argument");
                              displayUsage();
                              exitLevel = 1;
                      }
              } else if (args[i].equals("-skipJunk")) {
                  this.skipJunkFlag = true;
              } else {
                  System.err.println("Error: improper command line arguments: "
                          + argsList);
                  displayUsage();
                  exitLevel = 1;
              } // end if then else

              // If the exitLevel was set, then break out of the loop
              if (exitLevel != null) {
                  break;
              }
          } // end for loop

          // If either -force arg is specified, ignore -skipJunk arg.
          if (forceLFAFlag || forceCVFlag) { skipJunkFlag = false; }
      }

      return exitLevel;
  } // end handleOptions

  /**
   * This is the main method for running the learning factors algorithm
   * on skill models.
   * @param args command line arguments
   */
  public static void main(String[] args) {
    Logger logger = Logger.getLogger("LearningFactorMain.main");
    String version = VersionInformation.getReleaseString();
    logger.info("Learning Factors Analysis starting (" + version + ")...");
    // Call constructor.
    LearningFactorMain theMain = new LearningFactorMain();

    boolean playMode = ImportQueue.isInPlayMode();
    // If an exitLevel exists, it will be used to exit
    // before the LFA is run; otherwise exitLevel is null so continue.
    Integer exitLevel = null;
    // Handle the command line options:
    // The handleOptions method is called before entering the try-catch block
    // because it isn't affected by the ImportQueue.
    exitLevel = theMain.handleOptions(args);
    try {

        // If exitLevel is null, then proceed with analysis
        if (exitLevel == null) {
            // Pause the IQ
            if (playMode) {
                logger.info("main:: Pausing the ImportQueue.");
                ImportQueue.pause();
            }
            // Run the LFA algorithm.
            theMain.run();
        }
    } catch (Throwable throwable) {
        // Log error and send email if this happens!
        theMain.sendErrorEmail(logger, "Unknown error in main method.",
                throwable);
        exitLevel = 1;
    } finally {
        if (playMode) {
            logger.info("main:: Unpausing the ImportQueue.");
            ImportQueue.play();
        }
        exitOnStatus(exitLevel);
        logger.info("Learning Factors Analysis done.");
    }
  }

  /**
   * Run the LFA algorithm on:
   * - all the datasets and skill models
   * - all the skill models for the given dataset
   * - only the given skill model for the given dataset.
   * - cross validation runs with LFA except when "run CV only" flag is set to true
   */
  public void run() {
      // This will only return skill models for undeleted datasets...
      List<SkillModelItem> skillModels =
          getSkillModels(datasetIdString, skillModelIdString);
      if (skillModels == null || skillModels.size() == 0) {
          logger.info("No skill models found: " + skillModelIdString
                      + " for dataset id " + datasetIdString);
          return;
      }
      for (SkillModelItem skillModelItem : skillModels) {
          DatasetItem datasetItem = getDatasetItemBySkillModelId((Long)skillModelItem.getId());
          // At this point, we have only valid datasets, i.e., not deleted.

          // Unless either forceFlag is specified, skip junk datasets.
          if (skipJunkFlag && ((datasetItem.getJunkFlag() != null) && datasetItem.getJunkFlag())) {
              logger.debug("Skipping LFA/CV for "
                           + getLogPrefix(datasetItem, skillModelItem, "LFA/CV")
                           + " as it has a junkFlag of TRUE.");
              continue;
          }

          boolean nullFlag = false;
          boolean runLFA = true;
          boolean runCV = true;
          if (noRunLFAFlag) {
              runLFA = false;
          }
          if (noRunCVFlag) {
              runCV = false;
          }
          // Check that datasetItem and skillModelItem are not null to resolve #379
          if (datasetItem == null) {
              logger.info("Dataset item " + datasetIdString
                  + " is null. Skipping LearningFactorMain.runCalculataors.");
              nullFlag = true;
          } else if (skillModelItem == null) {
              logger.info("SkillModel item is null. Skipping LearningFactorMain.runCalculataors.");
              nullFlag = true;
          }

          if (!nullFlag) {
              runCalculators(datasetItem, skillModelItem,
                         runLFA, forceLFAFlag, runCV, forceCVFlag, outputSSSSFlag);
          }
      }
  }

    /**
     * Sets the datasetIdString.
     * @param datasetIdString The datasetIdString to set.
     */
    protected void setDatasetIdString(String datasetIdString) {
        this.datasetIdString = datasetIdString;
    }

    /**
     * Sets the skillModelIdString.
     * @param skillModelIdString The skillModelIdString to set.
     */
    protected void setSkillModelIdString(String skillModelIdString) {
        this.skillModelIdString = skillModelIdString;
    }

    /**
     * Sets the forceCVFlag.
     * @param forceFlag The forceFlag to set.
     */
    protected void setForceCVFlag(boolean forceFlag) {
        this.forceCVFlag = forceFlag;
    }

    /**
     * Sets the forceLFAFlag.
     * @param forceFlag The forceFlag to set.
     */
    protected void setForceLFAFlag(boolean forceFlag) {
        this.forceLFAFlag = forceFlag;
    }

    /**
     * Sets the outputSSSSFlag.
     * @param outputSSSSFlag The outputSSSSFlag to set.
     */
    protected void setOutputSSSSFlag(boolean outputSSSSFlag) {
        this.outputSSSSFlag = outputSSSSFlag;
    }

    /**
     * Sets the noRunLFAFlag.
     * @param noRunFlag The noRunLFAFlag to set.
     */
    protected void setNoRunLFAFlag(boolean noRunFlag) {
        this.noRunLFAFlag = noRunFlag;
    }

    /**
     * Sets the noRunCVFlag.
     * @param noRunFlag The noRunCVFlag to set.
     */
    protected void setNoRunCVFlag(boolean noRunFlag) {
        this.noRunCVFlag = noRunFlag;
    }

    /**
     * Returns the skillModelIdString.
     * @return the skillModelIdString
     */
    protected String getSkillModelIdString() {
        return skillModelIdString;
    }

} // end class
