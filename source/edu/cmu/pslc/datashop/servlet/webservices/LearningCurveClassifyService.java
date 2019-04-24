/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.webservices;

import static edu.cmu.pslc.datashop.util.CollectionUtils.set;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dto.LearningCurveSkillDetails;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.servlet.learningcurve.LearningCurveContext;
import edu.cmu.pslc.datashop.servlet.learningcurve.LearningCurveImage;
import edu.cmu.pslc.datashop.util.LogUtils;

/**
 * Web service for output learning curve classifications for a given model.
 *
 * @author Hui Cheng
 * <!-- $KeyWordsOff: $ -->
 */
public class LearningCurveClassifyService extends WebService {
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());
    /** student threshold parameter for learning curve*/
    private static final String STUDENT_THRESHOLD = "student_threshold";
    private Integer studentThreshold = null;
    /** opportunity threshold parameter for learning curve*/
    private static final String OPPORTUNITY_THRESHOLD = "opportunity_threshold";
    private Integer opportunityThreshold = null;
    /** low error threshold parameter for learning curve*/
    private static final String LOW_ERROR_THRESHOLD = "low_error_threshold";
    private Double lowErrorThreshold = null;
    /** high error threshold parameter for learning curve*/
    private static final String HIGH_ERROR_THRESHOLD = "high_error_threshold";
    private Double highErrorThreshold = null;
    /** AFM slope threshold parameter for learning curve*/
    private static final String AFM_SLOPE_THRESHOLD = "afm_slope_threshold";
    private Double AFMSlopeThreshold = null;

    /** Constant for the array position of the 'Other' or 'good' classification. */
    private static final int CLASSIFIED_OTHER = 0;
    /** Constant for the array position of the 'No Learning' classification. */
    private static final int CLASSIFIED_NO_LEARNING = 1;
    /** Constant for the array position of the 'Low and flat' classification. */
    private static final int CLASSIFIED_LOW_AND_FLAT = 2;
    /** Constant for the array position of the 'Still high' classification. */
    private static final int CLASSIFIED_STILL_HIGH = 3;
    /** Constant for the array position of the 'Too little data' classification. */
    private static final int CLASSIFIED_TOO_LITTLE_DATA = 4;
    /** Constant for the array position of the # observation. */
    private static final int NUMBER_OF_OBSERVATION = 0;
    /** Constant for the array position of the AIC. */
    private static final int AIC = 1;
    /** Constant for the array position of the item CV. */
    private static final int ITEM_CV = 2;

    /** the parameters that are valid for the get method of this service */
    private static final Set<String> VALID_GET_PARAMS =
        set(DATASET_ID, KC_MODEL, STUDENT_THRESHOLD, OPPORTUNITY_THRESHOLD, 
                        LOW_ERROR_THRESHOLD, HIGH_ERROR_THRESHOLD, AFM_SLOPE_THRESHOLD);

    /**
     * Constructor.
     * @param req the web service request
     * @param resp the web service response
     * @param params all parameters for this request, including path parameters
     */
    public LearningCurveClassifyService(HttpServletRequest req, HttpServletResponse resp,
            Map<String, Object> params) {
        super(req, resp, params);
    }

    /**
     * validate parameters: STUDENT_THRESHOLD, OPPORTUNITY_THRESHOLD,
     * LOW_ERROR_THRESHOLD, HIGH_ERROR_THRESHOLD, AFM_SLOPE_THRESHOLD
     *
     * @return validated parameter title
     * @throws WebServiceException
     *             when title is empty or size is bigger than 255
     */
    private void validateOptionalParams() throws WebServiceException {
            if (stringParam(STUDENT_THRESHOLD) != null)
                    studentThreshold = intParam(STUDENT_THRESHOLD);
            if (stringParam(OPPORTUNITY_THRESHOLD) != null)
                    opportunityThreshold = intParam(OPPORTUNITY_THRESHOLD);
            if (stringParam(LOW_ERROR_THRESHOLD) != null)
                    lowErrorThreshold = doubleParam(LOW_ERROR_THRESHOLD);
            if (stringParam(HIGH_ERROR_THRESHOLD) != null)
                    highErrorThreshold = doubleParam(HIGH_ERROR_THRESHOLD);
            if (stringParam(AFM_SLOPE_THRESHOLD) != null)
                    AFMSlopeThreshold = doubleParam(AFM_SLOPE_THRESHOLD);
    }

    /**
     * Return the learning curve classification for the specified dataset as pipe delimited.
     * sample format: KC Model | KC Name | Category | KC Intercept | KC Slope | # unique steps | # step instances
     * @param wsUserLog web service user log
     */
    public void get(WebServiceUserLog wsUserLog) {
        try {
            validateParameters(wsUserLog, VALID_GET_PARAMS);
            validateOptionalParams();
            logDebug("datasetId: ", datasetParam());
            LearningCurveContext lcContext = new LearningCurveContext();
            if (studentThreshold != null)
                    lcContext.setStudentThreshold(studentThreshold);
            if (opportunityThreshold != null)
                    lcContext.setOpportunityThreshold(opportunityThreshold);
            if (lowErrorThreshold != null)
                    lcContext.setLowErrorThreshold(lowErrorThreshold);
            if (highErrorThreshold != null)
                    lcContext.setHighErrorThreshold(highErrorThreshold);
            if (AFMSlopeThreshold != null)
                    lcContext.setAfmSlopeThreshold(AFMSlopeThreshold);
            //for dataset get LearningCurveClassifyDTO
            StringBuffer outputStr = new StringBuffer();
            DatasetItem dsItem = helper().findDataset(getAuthenticatedUser(), datasetParam(), AccessParam.VIEWABLE);
            List<SkillModelItem> skillModels = helper().getSkillModels(getAuthenticatedUser(), datasetParam());
            List<LearningCurveSkillDetails> learningCurveSkillDetails =
                            helper().getLearningCurveClassification(getAuthenticatedUser(), datasetParam(), stringParam(KC_MODEL), lcContext);
            outputStr.append(writeDatasetResultToString(dsItem.getDatasetName(), learningCurveSkillDetails, skillModels, lcContext));
            outputStr.append("\r\n");
            writeToOutput(outputStr.toString());
        } catch (WebServiceException wse) {
            writeError(wse);
        } catch (Exception e) {
            logger.error("Something went wrong with the XML message.", e);
            writeInternalError();
        }
    }

    /**
     * Write one or more DTOs as string
     * @param dtos the list of LearningCurveSkillDetails specific to a dataset
     */
    private String writeDatasetResultToString(String datasetName, List<LearningCurveSkillDetails> learningCurveSkillDetails, List<SkillModelItem> skillModels, LearningCurveContext lcContext){
            StringBuffer outputStr = new StringBuffer();
            outputStr.append("Dataset: " + datasetName + "\r\n");
            outputStr.append("AFM slope threshold: " + lcContext.getAfmSlopeThreshold() + "\r\n");
            outputStr.append("Student threshold: " + lcContext.getStudentThreshold() + "\r\n");
            outputStr.append("Opportunity threshold: " + lcContext.getOpportunityThreshold() + "\r\n");
            outputStr.append("Low error threshold: " + lcContext.getLowErrorThreshold() + "\r\n");
            outputStr.append("High error threshold: " + lcContext.getHighErrorThreshold() + "\r\n");
            outputStr.append("\r\n");
            outputStr.append(LearningCurveSkillDetails.writeHeader("\t") + "\r\n");
            if (learningCurveSkillDetails == null || learningCurveSkillDetails.size() == 0) {
                    outputStr.append("Empty result\r\n");
            } else {
                    Map<String, int[]> summaries = new TreeMap<String, int[]>();
                    for (LearningCurveSkillDetails dto : learningCurveSkillDetails) { 
                            outputStr.append(dto.writeToString("\t") + "\r\n");
                            String model = dto.getKcModelName();
                            String category = dto.getCategory();
                            if (!summaries.containsKey(model)) {
                                    summaries.put(model, new int[5]);
                            }
                            int[] summary = summaries.get(model);
                            if (category.equals(LearningCurveImage.CLASSIFIED_OTHER))
                                    summary[CLASSIFIED_OTHER]++;
                            else if (category.equals(LearningCurveImage.CLASSIFIED_NO_LEARNING))
                                    summary[CLASSIFIED_NO_LEARNING]++;
                            else if (category.equals(LearningCurveImage.CLASSIFIED_LOW_AND_FLAT))
                                    summary[CLASSIFIED_LOW_AND_FLAT]++;
                            else if (category.equals(LearningCurveImage.CLASSIFIED_STILL_HIGH))
                                    summary[CLASSIFIED_STILL_HIGH]++;
                            else if (category.equals(LearningCurveImage.CLASSIFIED_TOO_LITTLE_DATA))
                                    summary[CLASSIFIED_TOO_LITTLE_DATA]++;
                    }
                    Map<String, String[]> summaries_2 = new TreeMap<String, String[]>();
                    for (SkillModelItem kcModel : skillModels) {
                            String[] numbers = new String[3];
                            numbers[AIC] = kcModel.getEmptyStringOrAICValueForDisplay();
                            numbers[ITEM_CV] = kcModel.getEmptyStringOrCvStepStratifiedRmseForDisplay();
                            if (kcModel.getNumObservations() != null)
                                    numbers[NUMBER_OF_OBSERVATION] = "" + kcModel.getNumObservations();
                            else
                                    numbers[NUMBER_OF_OBSERVATION] = "";
                            summaries_2.put(kcModel.getSkillModelName(), numbers);
                    }
                    //write the summary
                    outputStr.append("\r\n");
                    outputStr.append("Summary\r\n");
                    outputStr.append("KC Model\t" + "%" + LearningCurveImage.CLASSIFIED_OTHER_LABEL + "\t" +
                                                    "%" + LearningCurveImage.CLASSIFIED_NO_LEARNING + "\t" +
                                                    "%" + LearningCurveImage.CLASSIFIED_LOW_AND_FLAT + "\t" +
                                                    "%" + LearningCurveImage.CLASSIFIED_STILL_HIGH + "\t" +
                                                    "%" + LearningCurveImage.CLASSIFIED_TOO_LITTLE_DATA + "\t" +
                                                    "# observation" + "\t" +
                                                    "AIC" + "\t" +
                                                    "Item CV" + "\r\n");
                    for (Map.Entry<String, int[]> entry : summaries.entrySet()) {
                            String kcModel = entry.getKey();
                            outputStr.append(kcModel + "\t");
                            int[] values = entry.getValue();
                            String[] otherValues = summaries_2.get(kcModel);
                            //get the total
                            int total = 0;
                            for (int value : values) {total += value;}
                            for (int value : values) {
                                    int percent = 0;
                                    if (total != 0)
                                            percent = (int)Math.round(((value * 100.0f) / total));
                                    outputStr.append(percent + "\t");
                            }
                            //ouput other values
                            outputStr.append(otherValues[NUMBER_OF_OBSERVATION] + "\t");
                            outputStr.append(otherValues[AIC] + "\t");
                            outputStr.append(otherValues[ITEM_CV] + "\r\n");
                    }

            }
            return outputStr.toString();
    }


    /**
     * Write one or more DTOs as delimited in the response.
     * @param dtos the list of LearningCurveSkillDetails
     * @param delimiter
     * @throws Exception for non IO problems (IO exception is handled, other exceptions
     * probably XML related)
     */
    private void writeToOutput(String outputStr) throws Exception {
        PrintWriter writer = null;
        try {
            if (acceptable("text/plain")) {
                    writer = getResp().getWriter();
                    writer.println(outputStr);
            }
        } catch (IOException ioe) {
            logger.error("Unable to write to output.", ioe);
            writeInternalError();
        } finally {
            if (writer != null) { writer.close(); }
        }
    }

    /**
     * Only log if debugging is enabled.
     * @param args concatenate all arguments into the string to be logged
     */
    public void logDebug(Object... args) { LogUtils.logDebug(logger, args); }
}
