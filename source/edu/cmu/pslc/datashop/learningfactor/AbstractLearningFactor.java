/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2008
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.learningfactor;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import edu.cmu.pslc.afm.dataObject.AFMDataObject;
import edu.cmu.pslc.afm.datashop.AbstractCalculator;
import edu.cmu.pslc.afm.datashop.AbstractCrossValidationCalculator;
import edu.cmu.pslc.afm.datashop.Calculator;
import edu.cmu.pslc.afm.datashop.CrossValidationCalculator;
import edu.cmu.pslc.afm.datashop.SkillParameters;
import edu.cmu.pslc.afm.parameterFitters.AFMParameterFitter;
import edu.cmu.pslc.afm.tools.AFMArrayUtils;
import edu.cmu.pslc.afm.transferModel.AFMTransferModel;
import edu.cmu.pslc.afm.transferModel.PenalizedAFMTransferModel;
import edu.cmu.pslc.datashop.dao.AlphaScoreDao;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.SkillDao;
import edu.cmu.pslc.datashop.dao.SkillModelDao;
import edu.cmu.pslc.datashop.dao.StepRollupDao;
import edu.cmu.pslc.datashop.dao.StudentDao;
import edu.cmu.pslc.datashop.extractors.AbstractExtractor;
import edu.cmu.pslc.datashop.helper.DatasetState;
import edu.cmu.pslc.datashop.helper.SystemLogger;
import edu.cmu.pslc.datashop.item.AlphaScoreId;
import edu.cmu.pslc.datashop.item.AlphaScoreItem;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.SkillItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.item.StudentItem;
import edu.cmu.pslc.datashop.util.LogUtils;

/**
 * Abstract implementation of isValidSSS and runCalculator for initiating the
 * Learning Factors Algorithms.
 *
 * @author Benjamin Billings
 * @version $Revision: 13763 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2017-01-20 15:42:44 -0500 (Fri, 20 Jan 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public abstract class AbstractLearningFactor extends AbstractExtractor {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** Flag indicating whether to use CFM or not. See DS817. */
    private static final boolean USE_CFM = false;

    /** How many fold of division of cross validation.*/
    private static final int CV_FOLD_DEFAULT = 3;
    private int numCVFold = CV_FOLD_DEFAULT;

    /** How many runs of cross validation.*/
    private static final int CV_NUM_RUNS_DEFAULT = 20;
    private int numCVRun = CV_NUM_RUNS_DEFAULT;

    /** Constant. */
    private static final int SSSS_ARRAY_LENGTH = 4;
    /** Constant. */
    private static final int SSSVS_ARRAY_LENGTH = 5;

    /** The default maximum number of skills in a skill model  for LFA to be run. */
    public static final int NUM_SKILLS_LIMIT = 300;

    /** The externally set maximum number of skills. */
    private int numSkillsLimit = 0;
    
    /** The default upper limit for the memory size (determined by the size of AFM transferModel X array) for LFA to be run. */
    public static final double MEMORY_SIZE_LIMIT = 2500;
    
    /** The externally set maximum memory size. */
    private double memoryLimit = MEMORY_SIZE_LIMIT;

    /** Returns the skill limit. @return the numSkillsLimit */
    public int getNumSkillsLimit() { return numSkillsLimit; }

    /** Sets the skills limit. @param numSkillsLimit the numSkillsLimit to set */
    public void setNumSkillsLimit(int numSkillsLimit) {
        this.numSkillsLimit = numSkillsLimit; }
    
    /** Returns the memory limit. @return the memoryLimit */
    public double getMemoryLimit() { return memoryLimit; }

    /** Sets the memory limit. @param memoryLimit the memoryLimit to set */
    public void setMemoryLimit(double memoryLimit) {
        this.memoryLimit = memoryLimit; }
    
    public void setNumCVFold(int numCVFold) {
            this.numCVFold = numCVFold;
    }
    public int getNumCVFold() {
            return numCVFold;
    }
    public void setNumCVRun(int numCVRun) {
            this.numCVRun = numCVRun;
    }
    public int getNumCVRun() {
            return numCVFold;
    }
    /**
     * Called by LearningFactorMain and KCModelAggregatorBean. Put the common
     * elements of LFA and CV in here to save resources and speed up if possible.
     * @param datasetItem the dataset item
     * @param skillModelItem the skill model item
     * @param runLFA run LFA algorithm
     * @param forceRunLFA ignore dataset last modified time, force to run LFA
     * @param runCV boolean run CV algorithm
     * @param forceRunCV ignore dataset last modified time, force to run CV
     * @param outputSSSS the output SSSS
     */
    public void runCalculators(DatasetItem datasetItem,
            SkillModelItem skillModelItem,
            boolean runLFA, boolean forceRunLFA,
            boolean runCV, boolean forceRunCV, boolean outputSSSS) {
        runCalculators(datasetItem, skillModelItem,
                runLFA, forceRunLFA,
                runCV, forceRunCV,
                outputSSSS, null);
    }

    /**
     * Called by LearningFactorMain and KCModelAggregatorBean. Put the common
     * elements of LFA and CV in here to save resources and speed up if possible.
     * @param datasetItem the dataset item
     * @param skillModelItem the skill model item
     * @param runLFA run LFA algorithm
     * @param forceRunLFA ignore dataset last modified time, force to run LFA iff runLFA
     * @param runCV boolean run CV algorithm
     * @param forceRunCV ignore dataset last modified time, force to run CV iff runLFA
     * @param outputSSSS the output SSSS
     * @param ssssDir the directory to put the SSSS output file
     */
    public void runCalculators(DatasetItem datasetItem,
            SkillModelItem skillModelItem,
            boolean runLFA, boolean forceRunLFA,
            boolean runCV, boolean forceRunCV, boolean outputSSSS, String ssssDir) {
        String logPrefix = getLogPrefix(datasetItem, skillModelItem, "LFA/CV");
        String logPrefixLFA = getLogPrefix(datasetItem, skillModelItem, "LFA");
        String logPrefixCV = getLogPrefix(datasetItem, skillModelItem, "CV");

        String oldLFAStatus = skillModelItem.getLfaStatus();
        String oldLFAStatusDescription = skillModelItem.getLfaStatusDescription();
        String oldCVStatus  = skillModelItem.getCvStatus();
        String oldCVStatusDescription = skillModelItem.getCvStatusDescription();

        boolean isLFAQueued = (oldLFAStatus == null) ? false :
            oldLFAStatus.equals(SkillModelItem.LFA_STATUS_QUEUED);
        boolean isCVQueued = (oldCVStatus == null) ? false :
            oldCVStatus.equals(SkillModelItem.CV_STATUS_QUEUED);

        boolean requiresLFA = DatasetState.requiresLFA(datasetItem, skillModelItem);
        boolean requiresCV = DatasetState.requiresCV(datasetItem, skillModelItem);

        // Any reason we can't leave right now if !requires && !force && !queued? Nope.
        if ((!requiresLFA && !forceRunLFA && !isLFAQueued)
            && (!requiresCV && !forceRunCV && !isCVQueued)) {
            logger.info(logPrefixLFA + "Nothing to do: not required, not forced, not queued.");
            logger.info(logPrefixCV + "Nothing to do: not required, not forced, not queued.");
            return;
        }
        
        long startTime = System.currentTimeMillis();

        // Check if LFA is allowed to run on this skill model.
        if (runLFA) {
            if (!forceRunLFA && !skillModelItem.getAllowLFAFlag()) {
                logger.info(logPrefixLFA + "AllowLFAFlag is false.");

                //Change the status to 'DO NOT RUN' if queued but not allowed,
                //but do not log to the dataset system log table as there are too many
                //logs then. DS1232
                // Trac #632. IFF current status is 'queued'.
                if (isLFAQueued) {
                    skillModelItem.setLfaStatus(SkillModelItem.LFA_STATUS_DO_NOT_RUN);
                    skillModelItem.setLfaStatusDescription(
                                      SkillModelItem.LFA_CV_STATUS_DESCRIPTION_NOT_ALLOWED_TO_RUN);
                    markLFAStatus(skillModelItem, datasetItem, oldLFAStatus,
                                  oldLFAStatusDescription, false);
                }
                runLFA = false;
            } else if (forceRunLFA && !skillModelItem.getAllowLFAFlag()) {
                logger.info(logPrefixLFA
                        + "AllowLFAFlag is false, but LFA is forced to run");
            } else {
                logger.debug(logPrefixLFA + "AllowLFAFlag is true.");
            }
        }

        // Check if CV is allowed to run on this skill model.
        if (runCV) {
            if (!forceRunCV && !skillModelItem.getAllowLFAFlag()) {
                logger.info(logPrefixCV + "AllowLFAFlag is false.");

                //Change the status to 'DO NOT RUN' if queued but not allowed,
                //but do not log to the dataset system log table as there are too many
                //logs then. DS1232
                // Trac #632. IFF current status is 'queued'.
                if (isCVQueued) {
                    skillModelItem.setCvStatus(SkillModelItem.CV_STATUS_DO_NOT_RUN);
                    skillModelItem.setCvStatusDescription(
                                      SkillModelItem.LFA_CV_STATUS_DESCRIPTION_NOT_ALLOWED_TO_RUN);
                    markCVStatus(skillModelItem, datasetItem, oldCVStatus,
                                 oldCVStatusDescription, false);
                }
                runCV = false;
            } else if (forceRunCV && !skillModelItem.getAllowLFAFlag()) {
                logger.info(logPrefixCV
                        + "AllowLFAFlag is false, but CV is forced to run");
            } else {
                logger.debug(logPrefixCV + "AllowLFAFlag is true.");
            }
        }

        // Check the number of skills, the skill model has to be sure
        // it is not over the limit.
        SkillDao skillDao = DaoFactory.DEFAULT.getSkillDao();
        int numSkills =  skillDao.getNumSkills(skillModelItem);
        if (numSkillsLimit != 0 && numSkills > numSkillsLimit) {
            String logMsg = "Over skill limit"
                    + " has " + numSkills + " skills,"
                    + " and the limit is " + numSkillsLimit + ".";
            if (runLFA) {
                if (requiresLFA || forceRunLFA) {
                    handleOverSkillLimit(datasetItem, skillModelItem,
                                         logPrefixLFA + logMsg, startTime, SystemLogger.ACTION_LFA,
                                         false, SkillModelItem.LFA_STATUS_DO_NOT_RUN,
                                         SkillModelItem.LFA_CV_STATUS_DESCRIPTION_OVER_SKILL_LIMIT);
                }
                runLFA = false;
            }
            if (runCV) {
                if (requiresCV || forceRunCV) {
                    handleOverSkillLimit(datasetItem, skillModelItem,
                                         logPrefixCV + logMsg, startTime, SystemLogger.ACTION_CV,
                                         false, SkillModelItem.CV_STATUS_DO_NOT_RUN,
                                         SkillModelItem.LFA_CV_STATUS_DESCRIPTION_OVER_SKILL_LIMIT);
                }
                runCV = false;
            }
        } else {
            logDebug(logPrefix, "Not over skill limit ",
                    "as it has ", numSkills, " skills, ",
                    "and the limit is ", numSkillsLimit);
        }

        // Check if the dataset has been modified before running this algorithm again.
        if (runLFA) {
            if (forceRunLFA && !requiresLFA) {
                logger.info(logPrefixLFA
                            + "LFA not required, but LFA is forced to run.");
            }
            if (requiresLFA) {
                logger.info(logPrefixLFA
                            + "LFA required as data has been modified recently.");
            }

            skillModelItem.setLfaStatus(SkillModelItem.LFA_STATUS_QUEUED);
            skillModelItem.setLfaStatusDescription(null);
            markLFAStatus(skillModelItem, datasetItem,
                          oldLFAStatus, oldLFAStatusDescription, false);
        }

        // Check if the dataset has been modified before running this algorithm again.
        if (runCV) {
            if (forceRunCV && !requiresCV) {
                logger.info(logPrefixCV
                            + "CV not required, but CV is forced to run");
            }
            if (requiresCV) {
                logger.info(logPrefixLFA
                            + "CV required as data has been modified recently.");
            }
            skillModelItem.setCvStatus(SkillModelItem.CV_STATUS_QUEUED);
            skillModelItem.setCvStatusDescription(null);
            markCVStatus(skillModelItem, datasetItem,
                         oldCVStatus, oldCVStatusDescription, false);
        }

        if (!runLFA && !runCV && !outputSSSS) {
            return;
        }

        String[][] sssvs = null;
        StepRollupDao stepRollupDao = DaoFactory.DEFAULT.getStepRollupDao();
        SkillModelDao skillModelDao = DaoFactory.DEFAULT.getSkillModelDao();
        int numStudent = 0;
        Long numObs = null;
        try {
            sssvs = stepRollupDao.getSSSVS(datasetItem, skillModelItem);
            List<String[]> tempSSSVS = new ArrayList<String[]>();
            final int STUDENT_COLUMN = 0;
            final int SUCCESS_COLUMN = 1;
            final int STEP_COLUMN = 2;
            final int PROBLEM_COLUMN = 3;
            final String CORRECT = "1";
            final String INCORRECT = "0";
            for (String[] thisRow : sssvs) {
                    if (thisRow[SUCCESS_COLUMN].equals(CORRECT) || thisRow[SUCCESS_COLUMN].equals(INCORRECT)) {
                            tempSSSVS.add(thisRow);
                    }
            }
            sssvs = new String[tempSSSVS.size()][];
            String lastStudent = "";
            String lastSuccess = "";
            String lastStep = "";
            String lastProblemView = "";
            for (int i = 0; i < tempSSSVS.size(); i++) {
                    sssvs[i] = tempSSSVS.get(i);
                    // the number of rows in the SSSVS is not the same as the number of observations
                    // if even one step has more than one skill associated with it, do this instead
                    if (!lastStudent.equals(sssvs[i][STUDENT_COLUMN]) ||
                                    !lastSuccess.equals(sssvs[i][SUCCESS_COLUMN]) ||
                                    !lastStep.equals(sssvs[i][STEP_COLUMN]) ||
                                    !lastProblemView.equals(sssvs[i][PROBLEM_COLUMN])) {
                            if (numObs == null)
                                    numObs = 0l;
                            numObs++;
                            
                            if (!lastStudent.equals(sssvs[i][STUDENT_COLUMN]))
                                    numStudent++;
                    }
                    lastStudent = sssvs[i][STUDENT_COLUMN];
                    lastSuccess = sssvs[i][SUCCESS_COLUMN];
                    lastStep = sssvs[i][STEP_COLUMN];
                    lastProblemView = sssvs[i][PROBLEM_COLUMN];
            }
            if (numObs == null) { numObs = 0l; }
            skillModelItem.setNumObservations(numObs.intValue());
            skillModelDao.saveOrUpdate(skillModelItem);
        } catch (OutOfMemoryError error) {
            error.printStackTrace();

            if (runLFA) {
                handleRuntimeErrorException(datasetItem, skillModelItem,
                        logPrefixLFA + "OutOfMemoryError getting SSSVS.", startTime,
                        SystemLogger.ACTION_LFA,
                        false, SkillModelItem.LFA_STATUS_UNABLE_TO_RUN, SkillModelItem.LFA_CV_STATUS_DESCRIPTION_OUT_MEMORY_GET_SSSVS);
            }
            if (runCV) {
                handleRuntimeErrorException(datasetItem, skillModelItem,
                        logPrefixCV + "OutOfMemoryError getting SSSVS.", startTime,
                        SystemLogger.ACTION_CV,
                        false, SkillModelItem.CV_STATUS_UNABLE_TO_RUN, SkillModelItem.LFA_CV_STATUS_DESCRIPTION_OUT_MEMORY_GET_SSSVS);
            }
            return;
        }
        
        //check if transferModel X array size is bigger than 2.5 gb
        long xArraySize = numObs*(numStudent + 2*numSkills)*8/1000000;
        if (xArraySize > memoryLimit) {
                String logMsg = "Over memory limit"
                                + ", it needs " + xArraySize + " MB,"
                                + " and the limit is " + memoryLimit + ".";
                if (runLFA) {
                        if (requiresLFA || forceRunLFA) {
                                handleOverSkillLimit(datasetItem, skillModelItem,
                                                logPrefixLFA + logMsg, startTime, SystemLogger.ACTION_LFA,
                                                false, SkillModelItem.LFA_STATUS_DO_NOT_RUN,
                                                SkillModelItem.LFA_CV_STATUS_DESCRIPTION_OVER_MEMORY_LIMIT);
                        }
                        runLFA = false;
                }
                if (runCV) {
                        if (requiresCV || forceRunCV) {
                                handleOverSkillLimit(datasetItem, skillModelItem,
                                                logPrefixCV + logMsg, startTime, SystemLogger.ACTION_CV,
                                                false, SkillModelItem.CV_STATUS_DO_NOT_RUN,
                                                SkillModelItem.LFA_CV_STATUS_DESCRIPTION_OVER_MEMORY_LIMIT);
                        }
                        runCV = false;
                 }
        } else {
                logDebug(logPrefix, "Not over memory limit ",
                                "as it needs ", xArraySize + " MB, ",
                                "and the limit is ", memoryLimit);
        }
      
        if (isValidSSSVS(sssvs, logPrefix)) {
            logDebug(logPrefix, "Valid SSSVS.");
            if (outputSSSS) {
                if (ssssDir == null) {
                    ssssDir = "ssss";
                }
                File ssssDirFileObj = new File(ssssDir);
                if ((ssssDirFileObj.exists())
                        || (!ssssDirFileObj.exists() && ssssDirFileObj.mkdirs())) {
                    String fileName = ssssDir + File.separator
                            + "sssvs_ds_" + datasetItem.getId()
                            + "_kcm_" + skillModelItem.getId() + ".txt";
                    logger.info(logPrefix + "Creating SSSVS file " + fileName);
                    AFMArrayUtils.writeString2DArray(sssvs, fileName);
                } else {
                    logger.error("The SSSS directory could not be created: " + ssssDir);
                }
            }
        } else {
            if (runLFA) {
                handleRuntimeErrorException(datasetItem, skillModelItem,
                        logPrefixLFA + "Invalid SSSVS.", startTime, SystemLogger.ACTION_LFA,
                        true, SkillModelItem.LFA_STATUS_UNABLE_TO_RUN, SkillModelItem.LFA_CV_STATUS_DESCRIPTION_INVALID_SSSVS);
            }
            if (runCV) {
                handleRuntimeErrorException(datasetItem, skillModelItem,
                        logPrefixCV + "Invalid SSSVS.", startTime, SystemLogger.ACTION_CV,
                        true, SkillModelItem.CV_STATUS_UNABLE_TO_RUN, SkillModelItem.LFA_CV_STATUS_DESCRIPTION_INVALID_SSSVS);
            }
            return;
        }

        boolean willLfaRunFlag = AbstractCalculator.willLFARun(sssvs);
        logDebug("runCalculator willLfaRunFlag = ", willLfaRunFlag);
        if (!willLfaRunFlag) {
            if (runLFA) {
                handleRuntimeErrorException(datasetItem, skillModelItem,
                        logPrefixLFA + "Not running, nonsense results expected.",
                        startTime, SystemLogger.ACTION_LFA,
                        true, SkillModelItem.LFA_STATUS_UNABLE_TO_RUN, SkillModelItem.LFA_CV_STATUS_DESCRIPTION_ALL_0_OR_1);
            }
            if (runCV) {
                handleRuntimeErrorException(datasetItem, skillModelItem,
                        logPrefixCV + "Not running, nonsense results expected.",
                        startTime, SystemLogger.ACTION_CV,
                        true, SkillModelItem.CV_STATUS_UNABLE_TO_RUN, SkillModelItem.LFA_CV_STATUS_DESCRIPTION_ALL_0_OR_1);
            }
            return;
        }

        boolean outOfMemory = false;
        AFMTransferModel afmTransferModel = null;
        if (runLFA) {
            logger.info(logPrefixLFA + "Running LFA calculator.");
            try {
                afmTransferModel = runLFACalculator(datasetItem, skillModelItem, sssvs,
                        startTime, logPrefixLFA);
            } catch (OutOfMemoryError error) {
                error.printStackTrace();
                handleRuntimeErrorException(datasetItem, skillModelItem,
                        logPrefixLFA + "Error: OutOfMemoryError.", startTime,
                        SystemLogger.ACTION_LFA,
                        false, SkillModelItem.LFA_STATUS_UNABLE_TO_RUN, SkillModelItem.LFA_STATUS_DESCRIPTION_OUT_MEMORY_RUNNING);
                outOfMemory = true;
            } catch (Exception e) {
                // Catch failures here so that if called from within a loop,
                // AFM will be run on the next sample/dataset.
                e.printStackTrace();
                handleRuntimeErrorException(datasetItem, skillModelItem,
                        logPrefixLFA + "Error: Invalid data.", startTime,
                        SystemLogger.ACTION_LFA,
                        false, SkillModelItem.LFA_STATUS_UNABLE_TO_RUN, SkillModelItem.LFA_CV_STATUS_DESCRIPTION_INVALID_SSSVS);
            }
        }
        if (runCV && !outOfMemory) {
            // Check if mapping type is allowed to have CV
            String mapType = skillModelItem.getMappingType();
            if (mapType.equals(SkillModelItem.MAPPING_PROBLEM_TO_KC)
                    || mapType.equals(SkillModelItem.MAPPING_KC)) {
                logger.info(logPrefixCV + "Wrong model type: " + mapType);
                String oldStatus = skillModelItem.getCvStatus();
                String oldStatusDescription = skillModelItem.getCvStatusDescription();
                skillModelItem.setCvStatus(SkillModelItem.CV_STATUS_DO_NOT_RUN);
                skillModelItem.setCvStatusDescription(SkillModelItem.CV_STATUS_DESCRIPTION_INVALID_MODEL_TYPE);
                markCVStatus(skillModelItem, datasetItem, oldStatus, oldStatusDescription, false);
                return;
            }
            logger.info(logPrefixCV + "Running CV calculator.");
            try {
                runCvCalculator(datasetItem, skillModelItem, sssvs,
                                afmTransferModel, 
                                startTime, logPrefixCV);
            } catch (OutOfMemoryError error) {
                error.printStackTrace();
                handleRuntimeErrorException(datasetItem, skillModelItem,
                        logPrefixCV + "Error: OutOfMemoryError.", startTime,
                        SystemLogger.ACTION_CV,
                        false, SkillModelItem.CV_STATUS_UNABLE_TO_RUN, SkillModelItem.CV_STATUS_DESCRIPTION_OUT_MEMORY_RUNNING);
            }
        } else if (runCV && outOfMemory) {
            //no need to run CV
            handleRuntimeErrorException(datasetItem, skillModelItem,
                    logPrefixCV + "Error: Not running due to previous OutOfMemoryError.", startTime,
                    SystemLogger.ACTION_CV,
                    false, SkillModelItem.CV_STATUS_UNABLE_TO_RUN, SkillModelItem.CV_STATUS_DESCRIPTION_OUT_MEMORY_RUNNING);
        }
    }

    /**
     * Run the LFA calculator.
     * @param datasetItem the dataset item
     * @param skillModelItem the skill model item
     * @param sssvs the SSSVS v is for view
     * @param lfaStartTime timestamp of start needed for logging
     * @param logPrefix the dataset name and id,
     * and skill model name and id in a string - logging
     * @return the AFM Transfer Model
     */
    public AFMTransferModel runLFACalculator(DatasetItem datasetItem,
            SkillModelItem skillModelItem,
            String[][] sssvs, long lfaStartTime, String logPrefix) {
        logDebug("runLFACalculator starting, num rows in ssss is ", sssvs.length);

        AlphaScoreDao alphaDao = DaoFactory.DEFAULT.getAlphaScoreDao();
        SkillDao skillDao = DaoFactory.DEFAULT.getSkillDao();
        SkillModelDao skillModelDao = DaoFactory.DEFAULT.getSkillModelDao();
        StudentDao studentDao = DaoFactory.DEFAULT.getStudentDao();
        StepRollupDao stepRollupDao = DaoFactory.DEFAULT.getStepRollupDao();

        String modelType = "";
        boolean cfmFlag = USE_CFM;
        if (cfmFlag) {
            Integer maxSkills = skillDao.getMaxSkillCount(skillModelItem);
            cfmFlag = (maxSkills != 1);
        }

        Calculator calculator = null;
        if (cfmFlag) {
            //calculator = AbstractCalculator.getCFMInstance("CFM model", ssss);
            //modelType = "CFM";
        } else {
            if (skillModelItem.getMappingType().equals(SkillModelItem.MAPPING_CORRECT_TRANS)) {
                calculator = AbstractCalculator.getAFMInstance("AFM model", sssvs, true);
            } else {
                calculator = AbstractCalculator.getAFMInstance("AFM model", sssvs, false);
            }
            modelType = "AFM";
        }

        logDebug("runLFACalculator: modelType is ", modelType);
        double aic = calculator.getAIC();
        double bic = calculator.getBIC();
        double logLikelihood = calculator.getLogLikelihood();

        Double aicD = new Double(aic);
        Double bicD = new Double(bic);
        Double logLikelihoodD = new Double(logLikelihood);

        boolean validValues = true;

        if (aicD.isInfinite()) {
            logger.warn(logPrefix + "AIC is Infinite. ");
            validValues = false;
        }
        if (bicD.isInfinite()) {
            logger.warn(logPrefix + "BIC is Infinite. ");
            validValues = false;
        }
        if (logLikelihoodD.isInfinite()) {
            logger.warn(logPrefix + "Log Likelihood is Infinite. ");
            validValues = false;
        }
        if (aicD.isNaN()) {
            logger.warn(logPrefix + "AIC is NaN. ");
            validValues = false;
        }
        if (bicD.isNaN()) {
            logger.warn(logPrefix + "BIC is NaN. ");
            validValues = false;
        }
        if (logLikelihoodD.isNaN()) {
            logger.warn(logPrefix + "Log Likelihood is NaN. ");
            validValues = false;
        }

        if (validValues) {
            skillModelItem.setAic(aicD);
            skillModelItem.setBic(bicD);
            skillModelItem.setLogLikelihood(logLikelihoodD);
            skillModelDao.saveOrUpdate(skillModelItem);

            int numSkills = calculator.getNumSkills();
            int numStudents = calculator.getNumStudents();

            for (int idx = 0; idx < numSkills; idx++) {
                Long skillId = calculator.getSkillId(idx);
                SkillItem skillItem = skillDao.get(skillId);
                SkillParameters skillParams =
                        calculator.getSkillParameters(idx);
                skillItem.setBeta(skillParams.getSkillIntercept());
                skillItem.setGamma(skillParams.getSkillSlope());
                skillDao.saveOrUpdate(skillItem);
            }

            for (int idx = 0; idx < numStudents; idx++) {
                Long studentId = calculator.getStudentId(idx);
                StudentItem studentItem = studentDao.get(studentId);
                AlphaScoreItem alphaItem =
                        new AlphaScoreItem(new AlphaScoreId(studentItem,
                                skillModelItem));
                double alpha = calculator.getStudentIntercept(idx);
                alphaItem.setAlpha(new Double(alpha));
                alphaDao.saveOrUpdate(alphaItem);
            }

            long elapsedTime = System.currentTimeMillis() - lfaStartTime;
            String logMsg = logPrefix + modelType + ".";
            logger.info(logMsg);
            Integer numObs = skillModelItem.getNumObservations().intValue();
            SystemLogger.log(datasetItem, skillModelItem, null,
                    SystemLogger.ACTION_LFA, logMsg,
                    Boolean.TRUE, numObs, elapsedTime);
            String oldLFAStatus = skillModelItem.getLfaStatus();
            String oldLFAStatusDescription = skillModelItem.getLfaStatusDescription();
            skillModelItem.setLfaStatus(SkillModelItem.LFA_STATUS_COMPLETE);
            skillModelItem.setLfaStatusDescription(SkillModelItem.LFA_CV_STATUS_DESCRIPTION_COMPLETED);
            markLFAStatus(skillModelItem, datasetItem, oldLFAStatus, oldLFAStatusDescription, false);
            stepRollupDao.callLfaBackfillSP(skillModelItem);

        } else {
            handleRuntimeErrorException(datasetItem, skillModelItem,
                    logPrefix + "Error Occurred: Invalid value(s) found.",
                    lfaStartTime, SystemLogger.ACTION_LFA,
                    true, SkillModelItem.LFA_STATUS_UNABLE_TO_RUN, SkillModelItem.LFA_STATUS_DESCRIPTION_INVALID_RESULT_VALUES);
        }
        logDebug("runLFACalculator finished");
        if (calculator != null) {
            return calculator.getTransferModel();
        } else {
            return null;
        }
    }

    /**
     * Run the cross validation calculator.
     * @param datasetItem the dataset item
     * @param skillModelItem the skill model item
     * @param sssvs the SSSVS
     * @param cvStartTime timestamp of start needed for logging
     * @param logPrefix the dataset name and id,
     * and skill model name and id in a string - logging
     * @throws TrainingResultDataErrorException
     */
    public void runCvCalculator(DatasetItem datasetItem,
            SkillModelItem skillModelItem,
            String[][] sssvs, AFMTransferModel afmTransferModel,
            long cvStartTime, String logPrefix) {
        if (afmTransferModel == null) {
            logDebug("runCvCalculator starting, num rows in sssvs is ", sssvs.length);
            logDebug("CV fold is ", numCVFold);
            logDebug("CV Run is ", numCVRun);
        } else {
            logDebug("runCvCalculator starting, existing AFMTransferModel, SSS row count "
                    + afmTransferModel.getAFMDataObject().getMinSSSLength());
            logDebug("CV fold is ", numCVFold);
            logDebug("CV Run is ", numCVRun);
        }
        SkillModelDao skillModelDao = DaoFactory.DEFAULT.getSkillModelDao();
        String logMsg = logPrefix;
        //student stratified cross validation
        AFMTransferModel afmTransferModelValueHolder = null;
        if (afmTransferModel != null) {
                if (afmTransferModel.getClass().getName().equals((new PenalizedAFMTransferModel()).getClass().getName())) {
                        afmTransferModelValueHolder = new PenalizedAFMTransferModel();
                } else {
                        afmTransferModelValueHolder = new AFMTransferModel();
                }
                afmTransferModelValueHolder.init(afmTransferModel.getAFMDataObject());
                afmTransferModelValueHolder.setSlopeSpec(afmTransferModel.getSlopeSpec());
        }        
        CrossValidationCalculator cvStudentStratifiedCalculator = null;
        boolean studentCVRunSuccess = false;
        try {
            logger.info(logPrefix + "Running student stratified CV calculator.");
            if (afmTransferModel != null) {
                cvStudentStratifiedCalculator = AbstractCrossValidationCalculator.
                        getStudentStratificationCrossValidationInstance(afmTransferModel, numCVFold, numCVRun);
                logMsg += "Student stratified CV successful, "
                        + "with pre-calculated AFM transfer model.";
                studentCVRunSuccess = true;
            } else if (skillModelItem.getMappingType().equals(
                    SkillModelItem.MAPPING_CORRECT_TRANS)) {
                cvStudentStratifiedCalculator = AbstractCrossValidationCalculator.
                        getStudentStratificationCrossValidationInstance(sssvs, true, numCVFold, numCVRun);
                logMsg += "Student stratified CV successful, "
                        + "with SSSVS data and student-step concatenation.";
                studentCVRunSuccess = true;
            } else {
                cvStudentStratifiedCalculator = AbstractCrossValidationCalculator.
                        getStudentStratificationCrossValidationInstance(sssvs, false, numCVFold, numCVRun);
                logMsg += "Student stratified CV successful, "
                        + "with SSSVS data and no student-step concatenation.";
                studentCVRunSuccess = true;
            }
        } catch (Exception cvException) {
            logMsg += cvException.getMessage();
            logger.info(logMsg);
        }

        //step stratified cross validation
        CrossValidationCalculator cvStepStratifiedCalculator = null;
        boolean stepCVRunSuccess = false;
        try {
            logger.info(logPrefix + "Running step stratified CV calculator.");
            if (afmTransferModel != null) {
                    
                    //reset afmTransferModel
                    if (afmTransferModelValueHolder.getClass().getName().equals((new PenalizedAFMTransferModel()).getClass().getName())) {
                            afmTransferModel = new PenalizedAFMTransferModel();
                    } else {
                            afmTransferModel = new AFMTransferModel();
                    }
                    
                afmTransferModel.init(afmTransferModelValueHolder.getAFMDataObject());
                afmTransferModel.setSlopeSpec(afmTransferModelValueHolder.getSlopeSpec());
                
                cvStepStratifiedCalculator = AbstractCrossValidationCalculator.
                        getStepStratificationCrossValidationInstance(afmTransferModel, numCVFold, numCVRun);
                logMsg += "Step stratified CV successful, " + "with pre-calculated AFM transfer model.";
                stepCVRunSuccess = true;
            } else if (skillModelItem.getMappingType().equals(
                    SkillModelItem.MAPPING_CORRECT_TRANS)) {
                cvStepStratifiedCalculator = AbstractCrossValidationCalculator.
                        getStepStratificationCrossValidationInstance(sssvs, true, numCVFold, numCVRun);
                logMsg += "Step stratified CV successful, "
                        + "with SSSVS data and student-step concatenation.";
                stepCVRunSuccess = true;
            } else {
                cvStepStratifiedCalculator = AbstractCrossValidationCalculator.
                        getStepStratificationCrossValidationInstance(sssvs, false, numCVFold, numCVRun);
                logMsg += "Step stratified CV successful, "
                        + "with SSSVS data and no student-step concatenation.";
                stepCVRunSuccess = true;
            }
        } catch (Exception cvException) {
            logMsg += cvException.getMessage();
            logger.info(logMsg);
        }
        
        //unstratified cross validation
        CrossValidationCalculator cvUnstratifiedCalculator = null;
        boolean unstratifiedCVRunSuccess = false;
        try {
            logger.info(logPrefix + "Running unstratified CV calculator.");
            if (afmTransferModel != null) {
                  //reset afmTransferModel
                    if (afmTransferModelValueHolder.getClass().getName().equals((new PenalizedAFMTransferModel()).getClass().getName())) {
                            afmTransferModel = new PenalizedAFMTransferModel();
                    } else {
                            afmTransferModel = new AFMTransferModel();
                    }
                    
                afmTransferModel.init(afmTransferModelValueHolder.getAFMDataObject());
                afmTransferModel.setSlopeSpec(afmTransferModelValueHolder.getSlopeSpec());
                
                cvUnstratifiedCalculator =
                        AbstractCrossValidationCalculator.
                        getNonstratificationCrossValidationInstance(afmTransferModel, numCVFold, numCVRun);
                logMsg += "Unstratified CV successful, "
                        + "with pre-calculated AFM transfer model.";
                unstratifiedCVRunSuccess = true;
            } else if (skillModelItem.getMappingType().equals(
                    SkillModelItem.MAPPING_CORRECT_TRANS)) {
                cvUnstratifiedCalculator =
                        AbstractCrossValidationCalculator.
                        getNonstratificationCrossValidationInstance(sssvs, true, numCVFold, numCVRun);
                logMsg += "Unstratified CV successful, "
                        + "with SSSVS data and student-step concatenation.";
                unstratifiedCVRunSuccess = true;
            } else {
                cvUnstratifiedCalculator =
                        AbstractCrossValidationCalculator.
                        getNonstratificationCrossValidationInstance(sssvs, false, numCVFold, numCVRun);
                logMsg += "Unstratified CV successful, "
                        + "with SSSVS data and no student-step concatenation.";
                unstratifiedCVRunSuccess = true;
            }
        } catch (Exception cvException) {
            logMsg += cvException.getMessage();
            logger.info(logMsg);
        }
        
        if (!studentCVRunSuccess && !stepCVRunSuccess && !unstratifiedCVRunSuccess) {
            handleRuntimeErrorException(datasetItem, skillModelItem,
                    logMsg, cvStartTime, SystemLogger.ACTION_CV,
                    true, SkillModelItem.CV_STATUS_UNABLE_TO_RUN, SkillModelItem.CV_STATUS_DESCRIPTION_FAILED_RUN);
            return;
        }

        //save results
        boolean valuesUpdated = false;
        if (cvStudentStratifiedCalculator != null) {
            Double studentRmseD = new Double(cvStudentStratifiedCalculator.getRMSE());
            if (studentRmseD.isInfinite()) {
                logger.warn(logPrefix + "Student stratified cross validation RMSE is Infinite. ");
            } else if (studentRmseD.isNaN()) {
                logger.warn(logPrefix + "Student stratified cross validation RMSE is NaN. ");
            } else {
                skillModelItem.setCvStudentStratifiedRmse(studentRmseD);
                valuesUpdated = true;
            }
        }
        if (cvStepStratifiedCalculator != null) {
            Double stepRmseD = new Double(cvStepStratifiedCalculator.getRMSE());
            if (stepRmseD.isInfinite()) {
                logger.warn(logPrefix + "Step stratified cross validation RMSE is Infinite. ");
            } else if (stepRmseD.isNaN()) {
                logger.warn(logPrefix + "Step stratified cross validation RMSE is NaN. ");
            } else {
                skillModelItem.setCvStepStratifiedRmse(stepRmseD);
                valuesUpdated = true;
            }
        }
        if (cvUnstratifiedCalculator != null) {
            Double unstratifiedRmseD = new Double(cvUnstratifiedCalculator.getRMSE());
            int unstratifiedNumOfObservations = cvUnstratifiedCalculator.getNumberOfObservations();
            Integer unstratifiedNumOfObservationsI = new Integer(unstratifiedNumOfObservations);
            int unstratifiedNumOfParameters = cvUnstratifiedCalculator.getNumberOfParameters();
            Integer unstratifiedNumOfParametersI = new Integer(unstratifiedNumOfParameters);
            if (unstratifiedRmseD.isInfinite()) {
                logger.warn(logPrefix + "Unstratified cross validation RMSE is Infinite. ");
            } else if (unstratifiedRmseD.isNaN()) {
                logger.warn(logPrefix + "Unstratified cross validation RMSE is NaN. ");
            } else {
                skillModelItem.setCvUnstratifiedRmse(unstratifiedRmseD);
                valuesUpdated = true;
            }
            if (unstratifiedNumOfObservations == 0) {
                logger.warn(logPrefix + "Unstratified number of observations is 0. ");
            } else {
                skillModelItem.setUnstratifiedNumObservations(unstratifiedNumOfObservationsI);
                valuesUpdated = true;
            }
            if (unstratifiedNumOfParameters == 0) {
                logger.warn(logPrefix + "Unstratified number of parameter is 0. ");
            } else {
                skillModelItem.setUnstratifiedNumParameters(unstratifiedNumOfParametersI);
                valuesUpdated = true;
            }
        }

        if (valuesUpdated) {
            skillModelDao.saveOrUpdate(skillModelItem);
            //log message
            long elapsedTime = System.currentTimeMillis() - cvStartTime;
            if (studentCVRunSuccess && stepCVRunSuccess && unstratifiedCVRunSuccess) {
                logMsg = logPrefix + "Success.";
                logger.info(logMsg);
                SystemLogger.log(datasetItem, skillModelItem, null,
                        SystemLogger.ACTION_CV, logMsg,
                        Boolean.TRUE, skillModelItem.getNumObservations(), elapsedTime);
                String oldCvStatus = skillModelItem.getCvStatus();
                String oldCvStatusDescription = skillModelItem.getCvStatusDescription();
                skillModelItem.setCvStatus(SkillModelItem.LFA_STATUS_COMPLETE);
                skillModelItem.setCvStatusDescription(SkillModelItem.LFA_CV_STATUS_DESCRIPTION_COMPLETED);
                markCVStatus(skillModelItem, datasetItem, oldCvStatus, oldCvStatusDescription, false);
            } else {
                logMsg = logPrefix + "Incomplete Success. ";
                String status = "";
                if (!studentCVRunSuccess) {
                    logMsg += "Student stratified CV didn't run. ";
                    if (status.equals(""))
                            status = SkillModelItem.CV_STATUS_DESCRIPTION_INCOMPLETE + ".";
                    status += " " + SkillModelItem.CV_STATUS_DESCRIPTION_INCOMPLETE_STUDENT + ".";
                    skillModelItem.setCvStudentStratifiedRmse(null);
                }
                if (!stepCVRunSuccess) {
                    logMsg += "Step stratified CV didn't run. ";
                    if (status.equals(""))
                            status = SkillModelItem.CV_STATUS_DESCRIPTION_INCOMPLETE + ".";
                    status += " " + SkillModelItem.CV_STATUS_DESCRIPTION_INCOMPLETE_ITEM + ".";
                    skillModelItem.setCvStepStratifiedRmse(null);
                }
                if (!unstratifiedCVRunSuccess) {
                    logMsg += "Unstratified CV didn't run. ";
                    if (status.equals(""))
                            status = SkillModelItem.CV_STATUS_DESCRIPTION_INCOMPLETE + ".";
                    status += " " + SkillModelItem.CV_STATUS_DESCRIPTION_INCOMPLETE_UNSTRATIFIED + ".";
                    skillModelItem.setCvUnstratifiedRmse(null);
                    skillModelItem.setUnstratifiedNumObservations(null);
                    skillModelItem.setUnstratifiedNumParameters(null);
                }
                logger.info(logMsg);
                SystemLogger.log(datasetItem, skillModelItem, null,
                        SystemLogger.ACTION_CV, logMsg,
                        Boolean.TRUE, skillModelItem.getNumObservations(), elapsedTime);
                String oldCvStatus = skillModelItem.getCvStatus();
                String oldCvStatusDescription = skillModelItem.getCvStatusDescription();
                skillModelItem.setCvStatus(SkillModelItem.CV_STATUS_IMCOMPLETE);
                skillModelItem.setCvStatusDescription(status);
                markCVStatus(skillModelItem, datasetItem, oldCvStatus, oldCvStatusDescription, false);
            }
        } else {
            handleRuntimeErrorException(datasetItem, skillModelItem,
                    logPrefix + "Error Occurred: No valid values produced for model cv",
                    cvStartTime, SystemLogger.ACTION_CV,
                    true, SkillModelItem.CV_STATUS_UNABLE_TO_RUN, SkillModelItem.CV_STATUS_DESCRIPTION_INVALID_RESULT_VALUES);
        }
        logDebug("runCvCalculator finished");

    }

    /**
     * Determines if the SSSS is a valid one.
     * @param ssss the SSSS
     * @param logPrefix the dataset name and id,
     * and skill model name and id in a string - logging
     * @return Boolean of true if it's valid, false otherwise.
     */
    public Boolean isValidSSSS(String[][] ssss, String logPrefix) {
        boolean validSSSS = false;
        if ((ssss != null) && (ssss.length > 0)) {
            if ((ssss[0] != null) && (ssss[0].length == SSSS_ARRAY_LENGTH)) {
                validSSSS = true;
            } else {
                if (ssss[0] == null)  {
                    logInfo(logPrefix, "SSSS[0] is null.");
                } else {
                    logInfo(logPrefix,
                            "SSSS[0].length != SSSS_ARRAY_LENGTH, length is ",
                            ssss[0].length);
                }
            }
        } else {
            if (ssss == null) {
                logInfo(logPrefix, "SSSS is null.");
            } else {
                logInfo(logPrefix, "SSSS length is 0.");
            }
        }
        return validSSSS;
    }

    /**
     * Determines if the SSSVS is a valid one.
     * @param sssvs the SSSVS
     * @param logPrefix the dataset name and id,
     * and skill model name and id in a string - logging
     * @return Boolean of true if it's valid, false otherwise.
     */
    public Boolean isValidSSSVS(String[][] sssvs, String logPrefix) {
        boolean validSSSVS = false;
        if ((sssvs != null) && (sssvs.length > 0)) {
            if ((sssvs[0] != null) && (sssvs[0].length == SSSVS_ARRAY_LENGTH)) {
                validSSSVS = true;
            } else {
                if (sssvs[0] == null)  {
                    logInfo(logPrefix, "SSSVS[0] is null.");
                } else {
                    logInfo(logPrefix,
                            "SSSVS[0].length != SSSVS_ARRAY_LENGTH, length is ",
                            sssvs[0].length);
                }
            }
        } else {
            if (sssvs == null) {
                logInfo(logPrefix, "SSSVS is null.");
            } else {
                logInfo(logPrefix, "SSSVS length is 0.");
            }
        }
        return validSSSVS;
    }
    
    public void handleOverSkillLimit(
            DatasetItem datasetItem, SkillModelItem skillModelItem,
            String logMsg, long startTime,
            String systemLoggerAction, boolean clearValues,
            String status, String statusDescription) {
        long elapsedTime = System.currentTimeMillis() - startTime;

        logDebug(logMsg);

        SystemLogger.log(datasetItem, skillModelItem, systemLoggerAction,
                logMsg, Boolean.TRUE, elapsedTime);

        // Append limit info.
        if (statusDescription.equals(SkillModelItem.LFA_CV_STATUS_DESCRIPTION_OVER_SKILL_LIMIT)) {
            statusDescription += " (" + numSkillsLimit + ")";
        } else if (statusDescription.equals(SkillModelItem.
                                            LFA_CV_STATUS_DESCRIPTION_OVER_MEMORY_LIMIT)) {
            statusDescription += " (" + memoryLimit + " MB)";
        }

        String oldStatus = null;
        String oldStatusDescription = null;
        if (systemLoggerAction == SystemLogger.ACTION_LFA) {
            oldStatus = skillModelItem.getLfaStatus();
            oldStatusDescription = skillModelItem.getLfaStatusDescription();
            skillModelItem.setLfaStatus(status);
            skillModelItem.setLfaStatusDescription(statusDescription);
            markLFAStatus(skillModelItem, datasetItem, oldStatus, oldStatusDescription, clearValues);
        } else if (systemLoggerAction == SystemLogger.ACTION_CV) {
            oldStatus = skillModelItem.getCvStatus();
            oldStatusDescription = skillModelItem.getCvStatusDescription();
            skillModelItem.setCvStatus(status);
            skillModelItem.setCvStatusDescription(statusDescription);
            markCVStatus(skillModelItem, datasetItem, oldStatus, oldStatusDescription, clearValues);
        }
    }

    /**
     * Handle runtime errors such as out-of-memory,
     * invalid SSSS and ill-formed SSSS.
     * @param datasetItem the DatasetItem
     * @param skillModelItem the SkillModelItem
     * @param logMsg String specific error message
     * @param startTime long when the operation starts
     * @param systemLoggerAction String acceptable values are
     * SystemLogger.ACTION_LFA and SystemLogger.ACTION_CV
     * @param clearValues boolean if current values in database should be erased
     * @param lfaStatus the new status for the given skill model
     * to false or not
     */
    public void handleRuntimeErrorException(
            DatasetItem datasetItem, SkillModelItem skillModelItem,
            String logMsg, long startTime,
            String systemLoggerAction, boolean clearValues,
            String status, String statusDescription) {
        long elapsedTime = System.currentTimeMillis() - startTime;

        logInfo(logMsg);
        SystemLogger.log(datasetItem, skillModelItem, systemLoggerAction,
                logMsg, Boolean.FALSE, elapsedTime);
        String oldStatus = null;
        String oldStatusDescription = null;
        if (systemLoggerAction == SystemLogger.ACTION_LFA) {
            oldStatus = skillModelItem.getLfaStatus();
            oldStatusDescription = skillModelItem.getLfaStatusDescription();
            skillModelItem.setLfaStatus(status);
            skillModelItem.setLfaStatusDescription(statusDescription);
            markLFAStatus(skillModelItem, datasetItem, oldStatus, oldStatusDescription, clearValues);
        } else if (systemLoggerAction == SystemLogger.ACTION_CV) {
            oldStatus = skillModelItem.getCvStatus();
            oldStatusDescription = skillModelItem.getCvStatusDescription();
            skillModelItem.setCvStatus(status);
            skillModelItem.setCvStatusDescription(statusDescription);
            markCVStatus(skillModelItem, datasetItem, oldStatus, oldStatusDescription, clearValues);
        }
    }

    /**
     * Little helper method to consistently create a prefix of the dataset name
     * and id and the skill model name and id for the log4j and system logging.
     * @param datasetItem the dataset item
     * @param skillModelItem the skill model item
     * @param type the type of action to indicate in the prefix
     * @return a string useful for logging
     */
    public String getLogPrefix(DatasetItem datasetItem,
            SkillModelItem skillModelItem, String type) {
        return type + " [" + datasetItem.getDatasetName()
                + " (" + datasetItem.getId() + ") / "
                + skillModelItem.getSkillModelName()
                + " (" + skillModelItem.getId() + ")] : ";
    }

    /**
     * Mark skill model LFA status as do-not-run, unable-to-run,
     * queued, completed.
     * @param skillModelItem the skill model to mark
     * @param datasetItem the dataset item
     * @param previousLFAStatus the allowed LFA status values
     * @param clearValue clear current LFA values in db
     */
    protected void markLFAStatus(SkillModelItem skillModelItem,
            DatasetItem datasetItem,
            String previousLFAStatus, String previousLFAStatusDescription, boolean clearValue) {
        if (skillModelItem == null || datasetItem == null) { return; }

        String newLFAStatus = skillModelItem.getLfaStatus();
        String newLFAStatusDescription = skillModelItem.getLfaStatusDescription();
        boolean changed = true;
        if (previousLFAStatus != null && newLFAStatus != null && previousLFAStatus.equals(newLFAStatus) &&
                        previousLFAStatusDescription != null && newLFAStatusDescription != null
                        && previousLFAStatusDescription.equals(newLFAStatusDescription)) {
            changed = false;
        }

        String logPrefix = getLogPrefix(datasetItem, skillModelItem, "LFA");
        if (changed) {
            SkillModelDao skillModelDao = DaoFactory.DEFAULT.getSkillModelDao();
            skillModelDao.saveOrUpdate(skillModelItem);
            logger.info(logPrefix + "Skill model change LFA status from '"
                    + previousLFAStatus + "' to '" + skillModelItem.getLfaStatus() + "'");
            if (clearValue) {
                clearLfaValues(skillModelItem);
                logger.info(logPrefix + "Clear LFA values");
            }
        } else {
            logger.debug(logPrefix + "LFA status not changed: " + newLFAStatus);
        }
    }

    /**
     * Mark skill model CV status as do-not-run, unable-to-run,
     * queued, completed.
     * @param skillModelItem the skill model to mark
     * @param datasetItem the dataset item
     * @param previousCVStatus the allowed CV status values (same as LFA values)
     * @param clearValue clear the current CV values in db
     */
    protected void markCVStatus(SkillModelItem skillModelItem,
            DatasetItem datasetItem,
            String previousCVStatus, String previousCVStatusDescription, boolean clearValue) {
        if (skillModelItem == null || datasetItem == null) { return; }

        String newCVStatus = skillModelItem.getCvStatus();
        String newCVStatusDescription = skillModelItem.getCvStatusDescription();
        boolean changed = true;
        if (previousCVStatus != null && newCVStatus != null 
                        && previousCVStatus.equals(newCVStatus) &&
                        previousCVStatusDescription != null && newCVStatusDescription != null 
                        && previousCVStatusDescription.equals(newCVStatusDescription)) {
            changed = false;
        }

        String logPrefix = getLogPrefix(datasetItem, skillModelItem, "CV");
        if (changed) {
            SkillModelDao skillModelDao = DaoFactory.DEFAULT.getSkillModelDao();
            skillModelDao.saveOrUpdate(skillModelItem);
            logger.info(logPrefix + "CV status changed from '" + previousCVStatus
                    + "' to '" + skillModelItem.getCvStatus() + "'");
            if (clearValue) {
                clearCvValues(skillModelItem);
                logger.info(logPrefix + "Clear CV values");
            }
        } else {
            logger.debug(logPrefix + "CV status not changed: " + newCVStatus);
        }
    }

    /**
     * Clear the LFA values for the given skill model from
     * the AlphaScore table (student intercept),
     * the skill table (the skill intercept and slope),
     * and the skill model table (AIC, BIC, log likelihood).
     * @param skillModelItem the given skill model
     */
    protected void clearLfaValues(SkillModelItem skillModelItem) {
        AlphaScoreDao alphaDao = DaoFactory.DEFAULT.getAlphaScoreDao();
        SkillDao skillDao = DaoFactory.DEFAULT.getSkillDao();
        SkillModelDao skillModelDao = DaoFactory.DEFAULT.getSkillModelDao();

        // clear out the alpha score table
        alphaDao.clear(skillModelItem);

        // clear out the skill intercept and slope values from the skill table
        List skillList = skillDao.find(skillModelItem);
        for (Iterator<SkillItem> iter = skillList.iterator(); iter.hasNext();) {
            SkillItem skillItem = iter.next();
            skillItem.setBeta(null);
            skillItem.setGamma(null);
            skillDao.saveOrUpdate(skillItem);
        }

        // clear out the AIC, BIC, and log likelihood values from KCM table
        skillModelItem.setAic(null);
        skillModelItem.setBic(null);
        skillModelItem.setLogLikelihood(null);
        skillModelDao.saveOrUpdate(skillModelItem);
    }

    /**
     * Clear the cross validation values for the given skill model from
     *  the skill model table (cv_rmse, cv_num_observation).
     * @param skillModelItem the given skill model
     */
    protected void clearCvValues(SkillModelItem skillModelItem) {
        SkillModelDao skillModelDao = DaoFactory.DEFAULT.getSkillModelDao();
        skillModelItem.setCvStudentStratifiedRmse(null);
        skillModelItem.setCvStepStratifiedRmse(null);
        skillModelItem.setCvUnstratifiedRmse(null);
        skillModelItem.setUnstratifiedNumObservations(null);
        skillModelItem.setUnstratifiedNumParameters(null);
        skillModelDao.saveOrUpdate(skillModelItem);
    }

    /**
     * Get list of SkillModelItems. Use skillModelId if not null,
     * otherwise use dataset id.
     * @param datasetIdString dataset id
     * @param skillModelIdString skill model id
     * @return a list of skill model items
     */
    protected List<SkillModelItem> getSkillModels(String datasetIdString,
            String skillModelIdString) {
        DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
        SkillModelDao skillModelDao = DaoFactory.DEFAULT.getSkillModelDao();

        List<SkillModelItem> skillModels = new ArrayList<SkillModelItem>();

        if (skillModelIdString != null) {
            SkillModelItem skillModelItem =
                    skillModelDao.get(new Long(skillModelIdString));
            if (skillModelItem != null) {
                skillModels.add(skillModelItem);
            } else {
                logger.info("Invalid skill model id: " + skillModelIdString);
            }

        } else if (datasetIdString != null) {
            DatasetItem datasetItem =
                    datasetDao.get(new Integer(datasetIdString));
            if (datasetItem == null
                    || (datasetItem.getDeletedFlag() != null
                    && datasetItem.getDeletedFlag().equals(true))) {
                logger.info("Invalid dataset id: " + datasetIdString);
            } else {
                logDebug("Valid dataset id: ", datasetItem.getId());
                skillModels = skillModelDao.find(datasetItem);
                if (skillModels == null || skillModels.size() == 0) {
                    logger.info("No skill model found for this dataset id: "
                            + datasetIdString);
                }
            }
        } else {
            skillModels = skillModelDao.findAllUndeleted();
        }

        if (skillModels == null || skillModels.size() == 0) {
            logger.info("Fail to get skill models.");
        }
        return skillModels;
    }

    /**
     * Get dataset item with skill model id.
     * @param skillModelId skill model id
     * @return get the dataset for the given skill model
     */
    protected DatasetItem getDatasetItemBySkillModelId(Long skillModelId) {
        DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
        SkillModelDao skillModelDao = DaoFactory.DEFAULT.getSkillModelDao();
        DatasetItem datasetItem = null;

        if (skillModelId != null) {
            SkillModelItem skillModelItem = skillModelDao.get(skillModelId);
            if (skillModelItem != null) {
                datasetItem = skillModelItem.getDataset();
                if (datasetItem != null) {
                    datasetItem = datasetDao.get((Integer)(datasetItem.getId()));
                } else {
                    logger.warn("getDatasetItemBySkillModelId: "
                            + "datasetItem is null for skill model: " + skillModelId);
                }
            } else {
                logger.warn("getDatasetItemBySkillModelId: "
                        + "skillModelItem is null for skill model: " + skillModelId);
            }
        }
        if (datasetItem == null) {
            logger.info("Can't get dataset item. skill model id: " + skillModelId + ".");
        }
        return datasetItem;
    }

    /**
     * Only log if debugging is enabled. Log debug message for this class
     * @param args concatenate all arguments into the string to be logged
     */
    private void logDebug(Object... args) {
        LogUtils.logDebug(logger, args);
    }

    /**
     * Just like logger.info.
     * @param args concatenate all arguments into the string to be logged
     */
    private void logInfo(Object... args) {
        LogUtils.logInfo(logger, args);
    }
}
