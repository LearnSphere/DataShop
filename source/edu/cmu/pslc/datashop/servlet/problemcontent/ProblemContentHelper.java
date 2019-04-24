/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2014
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.problemcontent;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.comparators.NullComparator;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.FileDao;
import edu.cmu.pslc.datashop.dao.PcConversionDatasetMapDao;
import edu.cmu.pslc.datashop.dao.PcConversionDao;
import edu.cmu.pslc.datashop.dao.PcProblemDao;
import edu.cmu.pslc.datashop.dao.ProblemDao;
import edu.cmu.pslc.datashop.dto.ErrorReportBySkill;
import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.PcConversionDatasetMapId;
import edu.cmu.pslc.datashop.item.PcConversionDatasetMapItem;
import edu.cmu.pslc.datashop.item.PcConversionItem;
import edu.cmu.pslc.datashop.item.PcProblemItem;
import edu.cmu.pslc.datashop.item.ProblemItem;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.FileItem;
import edu.cmu.pslc.datashop.item.UserItem;
import static edu.cmu.pslc.datashop.problemcontent.Constants.RESOURCES_PATH;

/**
 * Helper to get data from multiple tables in the database for the Problem Content page.
 *
 * @author Cindy Tipper
 * @version $Revision: 11467 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2014-08-13 09:57:37 -0400 (Wed, 13 Aug 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ProblemContentHelper {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());
    /** Default constructor. */
    public ProblemContentHelper() { }

    /** Constant for servlet to display resources. */
    private static final String DISPLAY_RESOURCES_STR = "servlet/DisplayResources?fileName=";

    /**
     * Get contents of HTML file for specified PC Problem.
     * @param pcProblemId the PC problem id
     * @param baseDir the base directory for DataShop files
     * @return html
     */
    public String getHtml(Long pcProblemId, String baseDir) {

        try {
            PcProblemDao pcProblemDao = DaoFactory.DEFAULT.getPcProblemDao();
            PcProblemItem pcProblem = pcProblemDao.get(pcProblemId);

            FileItem htmlFile = pcProblem.getHtmlFile();
            if (htmlFile != null) {
                FileDao fileDao = DaoFactory.DEFAULT.getFileDao();
                htmlFile = fileDao.get((Integer)htmlFile.getId());
                String filePath = htmlFile.getFilePath();
                String actualFileName = htmlFile.getUrl(baseDir);
                File theFile = new File(actualFileName);

                String resourcePath = DISPLAY_RESOURCES_STR + filePath;

                String theFileAsString = FileUtils.readFileToString(theFile, "UTF-8");
                theFileAsString = theFileAsString.replaceAll(RESOURCES_PATH, resourcePath);
                return theFileAsString;
            } else {
                logger.error("HTML file not found.");
            }
        } catch (Exception e) {
            logger.error("Failed to read HTML file: ", e);
        }

        return "HTML file not found.";
    }

    /**
     * Helper method to determine if Problem Content is available for
     * the specified dataset. True only if problems have been mapped.
     * @param dataset the DatasetItem
     * @return flag indicating presence of problem content
     */
    public boolean isProblemContentAvailable(DatasetItem dataset) {
        PcConversionDatasetMapDao pcConversionDatasetMapDao =
            DaoFactory.DEFAULT.getPcConversionDatasetMapDao();
        boolean isAvailable = pcConversionDatasetMapDao.isDatasetMapped(dataset);
        return isAvailable;
    }

    /**
     * Helper method to determine if Problem Content is available for the specified problem.
     * @param problem the ProblemItem
     * @return flag indicating presence of problem content
     */
    public boolean isProblemContentAvailable(ProblemItem problem) {
        ProblemDao dao = DaoFactory.DEFAULT.getProblemDao();
        problem = dao.get((Long)problem.getId());
        if (problem.getPcProblem() != null) {
            return true;
        }
        return false;
    }

    /**
     * Helper method to determine if Problem Content is available for the specified problem.
     * @param problemIdStr the problem id
     * @return flag indicating presence of problem content
     */
    public boolean isProblemContentAvailable(String problemIdStr) {
        if (problemIdStr == null) { return false; }

        Long problemId;
        try {
            problemId = Long.parseLong(problemIdStr);
        } catch (Exception e) {
            problemId = null;
        }
        if (problemId == null) { return false; }

        ProblemDao dao = DaoFactory.DEFAULT.getProblemDao();
        ProblemItem problem = dao.get(problemId);
        if (problem.getPcProblem() != null) { return true; }

        return false;
    }

    /**
     * Helper method to parse the ErrorReportBySkill DTO to generate
     * a string of problem names, formatted for viewing problem
     * content, if applicable.
     * @param errRpt error report by skill
     * @return the formatted string of problem names
     */
    public String getProblemNameListForDisplay(ErrorReportBySkill errRpt) {
        // If we don't have problem IDs, no changes to be made.
        if (errRpt.getProblemIdList() == null) { return errRpt.getProblemNameList(); }

        String[] nameIdPairs = errRpt.getProblemIdList().split(ErrorReportBySkill.SEPARATOR);

        StringBuffer sb = new StringBuffer();

        for (String pair : nameIdPairs) {
            if (pair.contains("~~")) {

                String[] pairSplit = pair.split("~~");
                if (pairSplit.length == 2) {
                    String name = pairSplit[0];
                    String id = pairSplit[1];
                    if (name != null && name.trim().length() > 0
                        && id != null && id.trim().length() > 0) {
                        sb.append(name);
                        if (isProblemContentAvailable(id)) {
                            sb.append(" -- ");
                            sb.append("<a href=\"javascript:viewProblem(");
                            sb.append(id);
                            sb.append(")\" class=\"error-report-view-problem-by-kc\">");
                            sb.append("view problem</a>");

                        } else {
                            sb.append("<span class=\"error-report-view-problem-none\" ");
                            sb.append("title=\"Problem Content is not available ");
                            sb.append("for this problem.\">");
                            sb.append(" -- view problem</span>");
                        }
                        sb.append(ErrorReportBySkill.SEPARATOR);
                    }
                }
            }
        }

        return sb.toString();
    }

    /**
     * Helper method to generate DTO necessary to display 'Problem Content'
     * mapping tool for a specific dataset.
     * @param datasetId the dataset id
     * @param context the ProblemContentContext
     * @return ProblemContentDto
     */
    public ProblemContentDto getProblemContentDto(Integer datasetId,
                                                  ProblemContentContext context) {
        DatasetDao dsDao = DaoFactory.DEFAULT.getDatasetDao();
        DatasetItem dataset = dsDao.get(datasetId);
        ProblemDao problemDao = DaoFactory.DEFAULT.getProblemDao();

        String conversionTool = context.getConversionTool();
        String searchBy = context.getSearchBy();

        PcConversionDao dao = DaoFactory.DEFAULT.getPcConversionDao();
        List<PcConversionItem> contentVersions =
            dao.getContentVersionsByTool(conversionTool, dataset, searchBy);
        List<MappedContentDto> mappedContent = dao.getMappedContent(dataset);

        String sortBy = context.getSortBy();
        Boolean isAscending = context.isAscending(sortBy);

        Comparator<MappedContentDto> comparator =
            MappedContentDto.getComparator(MappedContentDto.
                                           getSortByParameters(sortBy, isAscending));
        Comparator<MappedContentDto> nullComparator = new NullComparator(comparator, false);
        Collections.sort(mappedContent, nullComparator);

        ProblemContentDto result = new ProblemContentDto(contentVersions, mappedContent);

        result.setNumProblems(problemDao.getNumProblems(dataset));

        long count = 0;
        for (MappedContentDto mcDto: mappedContent) {
            if (mcDto.getStatus().equalsIgnoreCase(PcConversionDatasetMapItem.STATUS_COMPLETE)) {
                count += mcDto.getNumProblemsMapped();
            }
        }
        result.setNumProblemsMapped(count);

        return result;
    }

    /**
     * Delete a mapping for the specified content version.
     * @param conversionId the PcConverisonItem id
     * @param datasetId the dataset id
     * @param userItem the logged in user
     */
    public void deleteContentVersionMapping(Long conversionId, Integer datasetId,
                                      UserItem userItem) {

        PcConversionDao dao = DaoFactory.DEFAULT.getPcConversionDao();
        PcConversionItem pci = dao.get(conversionId);
        DatasetDao dsDao = DaoFactory.DEFAULT.getDatasetDao();
        DatasetItem dataset = dsDao.get(datasetId);

        // Remove reference to PcProblem in Problem...
        ProblemDao problemDao = DaoFactory.DEFAULT.getProblemDao();
        List<ProblemItem> mappedProblems = problemDao.getMappedProblemsByConversion(pci, dataset);
        for (ProblemItem p : mappedProblems) {
            p.setPcProblem(null);
            problemDao.saveOrUpdate(p);
        }

        PcConversionDatasetMapDao mapDao = DaoFactory.DEFAULT.getPcConversionDatasetMapDao();
        PcConversionDatasetMapId mapId = new PcConversionDatasetMapId(pci, dataset);
        PcConversionDatasetMapItem mapItem = mapDao.get(mapId);
        mapDao.delete(mapItem);

        String logInfoStr = "Removed dataset to problem content map for version: "
            + " (" + conversionId + ")";
        UserLogger.log(dataset, userItem, UserLogger.UNMAP_PROBLEM_CONTENT, logInfoStr, false);
    }

    /**
     * Delete a PC conversion item.
     * @param conversionId the conversion Id
     * @param userItem the logged in user
     */
    public void deletePcConversion(Long conversionId, UserItem userItem) {

        PcConversionDao dao = DaoFactory.DEFAULT.getPcConversionDao();
        PcConversionItem pci = dao.get(conversionId);

        String logInfoStr = "Removed PcConversion: "
            + pci.getContentVersion() + " (" + conversionId + ")";

        dao.delete(pci);

        UserLogger.log(userItem, UserLogger.DELETE_PROBLEM_CONTENT, logInfoStr, false);
    }
}
