/*
* Carnegie Mellon University, Human Computer Interaction Institute
* Copyright 2005
* All Rights Reserved
*/
package edu.cmu.pslc.datashop.servlet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.commons.lang.time.FastDateFormat;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.CustomFieldDao;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.DatasetInstanceMapDao;
import edu.cmu.pslc.datashop.dao.DatasetSystemLogDao;
import edu.cmu.pslc.datashop.dao.PcConversionDatasetMapDao;
import edu.cmu.pslc.datashop.dao.ProblemDao;
import edu.cmu.pslc.datashop.dao.SampleDao;
import edu.cmu.pslc.datashop.dao.SetDao;
import edu.cmu.pslc.datashop.dao.SkillDao;
import edu.cmu.pslc.datashop.dao.SkillModelDao;
import edu.cmu.pslc.datashop.dao.StudentDao;
import edu.cmu.pslc.datashop.item.CustomFieldItem;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.DatasetLevelItem;
import edu.cmu.pslc.datashop.item.ProblemItem;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.item.SetItem;
import edu.cmu.pslc.datashop.item.SkillItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.item.StudentItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.errorreport.ErrorReportServlet;
import edu.cmu.pslc.datashop.servlet.export.ExportContext;
import edu.cmu.pslc.datashop.servlet.kcmodel.KCModelContext;
import edu.cmu.pslc.datashop.servlet.kcmodel.KCModelHelper;
import edu.cmu.pslc.datashop.servlet.problemcontent.ProblemContentHelper;
import edu.cmu.pslc.datashop.servlet.sampletodataset.SamplesHelper;
import edu.cmu.pslc.datashop.util.LogUtils;
import static edu.cmu.pslc.datashop.util.StringUtils.join;
import static edu.cmu.pslc.datashop.servlet.AbstractServlet.json;

 /**
 * This class is for controlling the navigation.
 *
 * @author Benjamin Billings
 * @version $Revision: 14234 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2017-08-03 12:09:39 -0400 (Thu, 03 Aug 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public class NavigationHelper implements java.io.Serializable {

    /** Debug logging. */
    private static Logger logger = Logger.getLogger(NavigationHelper.class.getName());

    /** LCPID list of skill or student IDs Parameter. */
    private static final String LIST_OF_IDS_PARAM = "ids";

    /** Deselect this set action. */
    private static final String DESELECT_SET = "deselectSet";
    /** Student constant 'students'. */
    private static final String STUDENT = "students";

    /** Maximum number of chars for a line '15'. */
    public static final int LINE_LENGTH = 12;

    /** String to add to indicate extra text '...'. */
    public static final String EXTRA_CHAR_STR = "...";

    /** String spacing frequency '10'. */
    public static final int STR_SPACING_FREQ = 10;

    /** Default constructor. */
    public NavigationHelper() {
        logDebug("DataShop NavigationHelper.constructor");
    }

    /**
     * Returns the currently selected dataset.
     * @param info information stored in the HTTP session via DatasetContext class.
     * @return DatasetItem - the current dataset as a dataset item.
     */
    public DatasetItem getDataset(DatasetContext info) {
        return info.getDataset();
    }

    /**
     * Returns the DatasetItem for a given dataset id.
     * @param datasetId the Id to get a dataset for.
     * @return the DatasetItem.     */
    public DatasetItem getDataset(Integer datasetId) {
        return DaoFactory.DEFAULT.getDatasetDao().get(datasetId);
    }

    /**
     * Returns the currently selected user.
     * @param info information stored in the HTTP session via DatasetContext class.
     * @return UserItem - the current user as a UserItem.
     */
    public UserItem getUser(DatasetContext info) {
        return info.getUser();
    }

    /**
     * Returns the list of current skills as skill items.
     * @param info information stored in the HTTP session via DatasetContext class.
     * @return List - a list of all current skill items as SkillItems.
     */
    public List getSkills(DatasetContext info) {
        List skillList = new ArrayList();
        List infoSkillList = info.getNavContext().getSkillList();
        synchronized (infoSkillList) {
            for (Iterator sIter = infoSkillList.iterator(); sIter.hasNext();) {
                SelectableItem selectableItem = (SelectableItem)sIter.next();
                skillList.add(selectableItem.getItem());
            }
        }
        Collections.sort(skillList);
        return skillList;
    }

    /**
     * Returns the list of skills that are selected.
     * @param info information stored in the HTTP session via DatasetContext class.
     * @return List - a list of all skill items as SkillItem.
     */
    public List getSelectedSkills(DatasetContext info) {
        List activeSkillsList = new ArrayList();
        List skillList = info.getNavContext().getSkillList();
        synchronized (skillList) {
            for (Iterator sIter = skillList.iterator(); sIter.hasNext();) {
                SelectableItem selectableItem = (SelectableItem)sIter.next();
                if (selectableItem.isSelected()) {
                    activeSkillsList.add(selectableItem.getItem());
                }
            }
        }

        Collections.sort(activeSkillsList);
        return activeSkillsList;
    }

    /**
     * Returns the list of students that are selected.
     * @param info information stored in the HTTP session via DatasetContext class.
     * @return List - a list of all student items as StudentItem.
     */
    public List getSelectedStudents(DatasetContext info) {
        List activeStudentsList = new ArrayList();
        List studentList = info.getNavContext().getStudentList();
        synchronized (studentList) {
            for (Iterator sIter = studentList.iterator(); sIter.hasNext();) {
                SelectableItem selectableItem = (SelectableItem)sIter.next();
                if (selectableItem.isSelected()) {
                    activeStudentsList.add(selectableItem.getItem());
                }
            }
        }
        Collections.sort(activeStudentsList);
        return activeStudentsList;
    }

    /**
     * Returns the list of samples that are selected.
     * @param info information stored in the HTTP session via DatasetContext class.
     * @return List - a the list of selected samples as SampleItems.
     */
    public List getSelectedSamples(DatasetContext info) {
        List<Comparable> selectedSamplesList = new ArrayList<Comparable>();
        List<SelectableItem> sampleList = info.getNavContext().getSampleList();
        synchronized (sampleList) {
            for (SelectableItem selectableItem : sampleList) {
                if (selectableItem.isSelected()) {
                    selectedSamplesList.add(selectableItem.getItem());
                }
            }
        }
        Collections.sort(selectedSamplesList);
        return selectedSamplesList;
    }

    /**
     * Comma delimited list of the selected samples.
     * @param info information stored in the HTTP session via DatasetContext class.
     * @return a comma delimited list of the selected samples
     */
    public String getSelectedSamplesString(DatasetContext info) {
        final List<SampleItem> selectedSamples = getSelectedSamples(info);
        final SampleDao sampleDao = DaoFactory.DEFAULT.getSampleDao();

        return join(", ", new ArrayList<String>() { {
            for (SampleItem sample : selectedSamples) {
                // make sure the sample is in sync with the db
                sample = sampleDao.get((Integer)sample.getId());
                if (sample != null) {
                    String sampleName = sample.getSampleName();

                    // don't include the same sample name twice
                    if (!contains(sampleName)) {
                        add(sampleName);
                    }
                }
            }
        } });
    }

    /**
     * Given the DatasetContext, get the list of selected samples and determine
     * the status of each cached file.
     * @param context the DatasetContext.
     * @param selection the export type string (byStudentStep, byTransaction, or byProblem)
     * @return a map of samples and their corresponding cached file status.
     */
    public Map<SampleItem, String> getCachedFileStatus(DatasetContext context, String selection) {
        HashMap<SampleItem, String> sampleFileStatus = new HashMap<SampleItem, String>();
        ExportContext exportContext = context.getExportContext();
        DatasetSystemLogDao dao = DaoFactory.DEFAULT.getDatasetSystemLogDao();
        final List<SampleItem> selectedSamples = getSelectedSamples(context);
        FastDateFormat dateFormat = FastDateFormat.getInstance("MMM dd, yyyy h:mm a z");

        for (SampleItem sample : selectedSamples) {
            // get the last cached date
            Date lastCached = dao.getLastCachedTime(sample, selection);
            if (lastCached == null) {
                sampleFileStatus.put(sample, null);
            } else {
                sampleFileStatus.put(sample, dateFormat.format(lastCached));
            }
        }
        return sampleFileStatus;
    }

    /**
     * Returns the top skill.
     * @param info information stored in the HTTP session via DatasetContext class.
     * @return the top skill as a skillItem.
     */
    public SkillItem getTopSkill(DatasetContext info) {
        return info.getNavContext().getTopSkill();
    }

    /**
     * Returns the top student.
     * @param info information stored in the HTTP session via DatasetContext class.
     * @return the top student as a studentItem.
     */
    public StudentItem getTopStudent(DatasetContext info) {
        return info.getNavContext().getTopStudent();
    }

    /**
     * Returns the single selected ProblemItem.
     * @param info information stored in the HTTP session via DatasetContext class.
     * @return ProblemItem - the selected problemItem or null if none selected.
     */
    public ProblemItem getSelectedProblem(DatasetContext info) {
        return info.getNavContext().getSelectedProblem();
    }

    /**
     * Returns the list of problems that are selected.
     * @param info information stored in the HTTP session via DatasetContext class.
     * @return List - a list of all skill items as SkillItem.
     */
    public List getSelectedProblems(DatasetContext info) {
        List activeProblemList = new ArrayList();
        List problemList = info.getNavContext().getProblemList();
        synchronized (problemList) {
            for (Iterator pIter = problemList.iterator(); pIter.hasNext();) {
                SelectableItem selectableItem = (SelectableItem)pIter.next();
                if (selectableItem.isSelected()) {
                    activeProblemList.add(selectableItem.getItem());
                }
            }
        }
        Collections.sort(activeProblemList);
        return activeProblemList;
    }

    /**
     * Retrieves information about a problem in a HTML list format.
     * @param problemId the Long Id of the problem.
     * @param req the servlet request object.
     * @return a string of HTML.
     */
    public String getProblemInfo(Long problemId, HttpServletRequest req) {

        ProblemDao dao = DaoFactory.DEFAULT.getProblemDao();
        ProblemItem theProblem = dao.get(problemId);
        if (theProblem == null) {
            return "<p>No info found</p>";
        } else {
            StringBuffer returnString = new StringBuffer();
            returnString.append("<p><strong>Name :</strong> "
                    + theProblem.getProblemName() + "</p>");
            returnString.append("<p><strong>Description :</strong> ");

            if (theProblem.getProblemDescription() == null) {
                returnString.append("none");
            } else {
                returnString.append(theProblem.getProblemDescription());
            }
            returnString.append("</p>");

            DatasetLevelItem level = theProblem.getDatasetLevel();
            DatasetItem dataset = getDataset((Integer)level.getDataset().getId());

            // 'View Problem' button
            if (isProblemContentAvailable(dataset)) {
                if (isProblemContentAvailable(problemId)) {
                    returnString.append("<div id=\"tooltip-view-problem-button\">");
                    returnString.append("<a id=\"tooltipViewProblemLink\" ");
                    returnString.append("href=\"javascript:viewProblem(");
                    returnString.append(problemId);
                    returnString.append(")\"");
                    returnString.append("class=\"ui-state-default ui-corner-all\" ");
                    returnString.append(">");
                    returnString.append("View Problem");
                    returnString.append("</a></div>");
                } else {
                    returnString.append("<div id=\"tooltip-view-problem-button\">");
                    returnString.append("<span id=\"tooltipViewProblemLink\" ");
                    returnString.append("class=\"ui-state-default ui-corner-all ");
                    returnString.append("dead_link ui-state-disabled\" ");
                    returnString.
                        append("title=\"Problem content is not available for this problem.\"");
                    returnString.append(">");
                    returnString.append("View Problem");
                    returnString.append("</span></div>");
                }
            }

            // 'Error Report' button, if not on ER page.
            String currentPage = (String)req.getSession(true).getAttribute("recent_report");
            if (!currentPage.equals(ErrorReportServlet.SERVLET_NAME)) {
                returnString.append(addErrorReportButton(problemId));
            }

            returnString.append("<p><strong>Problem Hierarchy :</strong></p>");

            StringBuffer levelBuffer = new StringBuffer();
            do {

                if (level.getLevelTitle() == null) {
                    levelBuffer.insert(0, "<div>" + level.getLevelName());
                } else {
                    levelBuffer.insert(0, "<div>" + level.getLevelTitle() + ": "
                        + level.getLevelName());
                }
                levelBuffer.append("</div>");
                level = level.getParent();
            } while (level != null);

            returnString.append(levelBuffer + "</ul>");

            if (logger.isDebugEnabled()) {
                logger.debug("Returning Get Problem String :: " + returnString.toString());
            }

            return returnString.toString();
        }
    }

    private String addErrorReportButton(Long problemId) {

        StringBuffer sb = new StringBuffer();

        sb.append("<div id='tooltip-error-report-button'>");
        sb.append("<a id='tooltipErrorReportLink' ");
        sb.append("href='javascript:errorReport(");
        sb.append(problemId);
        sb.append(")'");
        sb.append("class='ui-state-default ui-corner-all' ");
        sb.append(">");
        sb.append("Error Report");
        sb.append("</a></div>");

        return sb.toString();
    }

    /**
     * Method to determine if Problem Content is available for
     * the specified dataset.
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
     * @param problemId the problem id
     * @return flag indicating presence of problem content
     */
    public boolean isProblemContentAvailable(Long problemId) {
        ProblemContentHelper pcHelper = HelperFactory.DEFAULT.getProblemContentHelper();
        ProblemItem problem = new ProblemItem(problemId);
        return pcHelper.isProblemContentAvailable(problem);
    }

    /**
     * Returns the selected primary skill model id.
     * @param info information stored in the HTTP session via DatasetContext class.
     * @return SkillModelItem - the selected skillModel id or null if none selected.
     */
    public Long getSelectedSkillModel(DatasetContext info) {
        return getSelectedSkillModelGeneric(info, true);
    }

    /**
     * Returns the selected primary skill model item.
     * @param info information stored in the HTTP session via DatasetContext class.
     * @return SkillModelItem - the selected skillModelItem or null if none selected.
     */
    public SkillModelItem getSelectedSkillModelItem(DatasetContext info) {
        return DaoFactory.DEFAULT.getSkillModelDao().get(
                getSelectedSkillModelGeneric(info, true)
        );
    }

    /**
     * Returns the selected secondary skill model id.
     * @param info information stored in the HTTP session via DatasetContext class.
     * @return SkillModelItem - the selected skillModel id or null if none selected.
     */
    public Long getSecondarySelectedSkillModel(DatasetContext info) {
        return getSelectedSkillModelGeneric(info, false);
    }

    /**
     * Returns the selected secondary skill model item.
     * @param info information stored in the HTTP session via DatasetContext class.
     * @return SkillModelItem - the selected skillModelItem or null if none selected.
     */
    public SkillModelItem getSecondarySelectedSkillModelItem(DatasetContext info) {
        return DaoFactory.DEFAULT.getSkillModelDao().get(
                getSelectedSkillModelGeneric(info, false)
        );
    }

    /**
     * Returns the selected skill model.
     * @param info information stored in the HTTP session via DatasetContext class.
     * @param isPrimary boolean of whether to get the primary or 2ndary model.
     * @return SkillModelItem - the selected skillModel id or null if none selected.
     */
    private Long getSelectedSkillModelGeneric(DatasetContext info, boolean isPrimary) {
        SkillModelItem selectedItem = null;

        List <SelectableItem> skillModelList;
        if (isPrimary) {
            skillModelList = info.getNavContext().getSkillModelList();
        } else {
            skillModelList = info.getNavContext().getSecondarySkillModelList();
        }
        synchronized (skillModelList) {
            for (SelectableItem item : skillModelList) {
                if (item.isSelected()) { selectedItem = (SkillModelItem)item.getItem(); }
            }

            if (selectedItem == null && isPrimary) {
                //return the first one and set it active.
                if (!skillModelList.isEmpty()) {
                    SelectableItem selectableItem =
                        (SelectableItem)skillModelList.get(0);
                    selectableItem.isSelected();
                    return (Long)((SkillModelItem)selectableItem.getItem()).getId();
                } else {
                    return null;
                }
            } else if (selectedItem == null && !isPrimary) {
                logger.debug("No secondary model selected, returning null");
                return null;
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Returning selected skill model: " + selectedItem.getId());
                }
                return (Long)selectedItem.getId();
            }
        }
    }

    /**
     * Displays the list of all samples for the current dataset/user.
     * @param displayDiv boolean indicating whether to display the surrounding div or not.
     * @param info information stored in the HTTP session via DatasetContext class.
     * @return String - a string containing the skill navigation HTML code.
     */
    public String displaySampleNav(boolean displayDiv, DatasetContext info) {
        StringBuffer navString = new StringBuffer();
        if (displayDiv) {
            navString.append("<div id=\"samples\">\n");
        }
        navString.append("<div class=\"navigationBoxHeader\"><h2>Samples ");
        Boolean editFlag = info.getEditFlag() || info.isDataShopAdmin();
        Boolean projectAdminFlag = info.getAdminFlag() || info.isDataShopAdmin();
        if (editFlag) {
            navString.append("<span><a href=\"javascript:editSample()\">"
                + "<img src=\"images/add_file.gif\" "
                + "alt=\"New Sample\" title=\"Define a New Sample\" /></a></span>");
        }
        navString.append("</h2></div>");
        navString.append("<p><a id=\"sample_deselect\" class=\"deselectLink\" "
                         + "href=\"javascript:selectSample"
                         + "('unselect_all');\">deselect all</a></p>\n<ul>\n");
        //check for new samples in the DB.
        SampleDao sampleDao = DaoFactory.DEFAULT.getSampleDao();
        List<SampleItem> databaseSamples = sampleDao.find(info.getDataset(), info.getUser());
        List toRemove = new ArrayList();
        //walk through the list of samples in the session.
        List<SelectableItem> sampleList = info.getNavContext().getSampleList();
        synchronized (sampleList) {
            for (SelectableItem listItem : sampleList) {
                SampleItem sampleListItem = (SampleItem)listItem.getItem();
                //check that the sample item from the current list is still in the database.
                if (databaseSamples.contains(sampleListItem)) {
                    databaseSamples.remove(sampleListItem);
                } else {
                    toRemove.add(listItem);
                }
            }
        }
        //see if there are any to remove
        if (toRemove.size() > 0) { info.getNavContext().getSampleList().removeAll(toRemove); }
        //if any items are left in the databaseSampleList they are new additions
        //and should be added to the currentSamples list.
        for (SampleItem sample : databaseSamples) {
            info.getNavContext().addSample(new SelectableItem(sample));
        }
        logDebug("New samples in database. Adding ", databaseSamples.size(),
                " items to currentSamplesList");
        StringBuffer myListHtml = new StringBuffer();
        myListHtml.append("<li id=\"mySamples\"><span>My Samples</span><ul>\n");
        StringBuffer sharedListHtml = new StringBuffer();
        sharedListHtml.append("<li id=\"sharedSamples\"><span>Shared Samples</span><ul>\n");
        sampleList = info.getNavContext().getSampleList();
        synchronized (sampleList) {
            Collections.sort(sampleList);
            for (SelectableItem selectableItem : sampleList) {
                SampleItem selectedSample = (SampleItem)selectableItem.getItem();
                //re-attach the sample to the hibernate session.
                SampleItem sampleItem = null;
                if (selectedSample != null) {
                    sampleItem = sampleDao.get((Integer)selectedSample.getId());
                    if (sampleItem == null) {
                        logger.warn("Unable to re-attach sample with id " + selectedSample.getId());
                        //DS1216 - removing from list causes a ConcurrentModificationException
                        //sampleList.remove(selectableItem);
                        //TODO do we need to remove from list after loop?
                        continue;
                    }
                } else {
                    logger.warn("selectedSample was null");
                    //DS1216 - removing from list causes a ConcurrentModificationException
                    //sampleList.remove(selectableItem);
                    //TODO do we need to remove from list after loop?
                    continue;
                }
                String croppedName;
                if (sampleItem.getSampleName().length() > LINE_LENGTH) {
                    croppedName = sampleItem.getSampleName().substring
                        (0, LINE_LENGTH - EXTRA_CHAR_STR.length()) + EXTRA_CHAR_STR;
                } else {
                    croppedName = sampleItem.getSampleName();
                }
                if (sampleItem.getOwner().equals(info.getUser())
                        && !sampleItem.getSampleName().equals(
                                SampleItem.ALL_TRANSACTIONS_SAMPLE_NAME)) {

                    myListHtml.append("\t\t<li id=\"li_");
                    myListHtml.append(sampleItem.getId());
                    myListHtml.append("\"");
                    if (selectableItem.isSelected()) {
                        myListHtml.append(" class=\"selectedSkill\"");
                    }
                    myListHtml.append("\">");
                    myListHtml.append("<a class=\"sampleName\" title=\"");
                    myListHtml.append(sampleItem.getSampleName());
                    if (sampleItem.getDescription() != null) {
                        myListHtml.append(" : " + sampleItem.getDescription());
                    }
                    //NAME IS FOR TESTING! DO NOT CHANGE OR REMOVE.
                    myListHtml.append("\" name=\"" + sampleItem.getSampleName());
                    myListHtml.append("\" href=\"javascript:selectSample('"
                            + sampleItem.getId() + "');\">");
                    myListHtml.append(croppedName + "</a>");
                    if (editFlag) {
                        myListHtml.append("<span class=\"icons\"><a href=\"javascript:editSample('"
                            + sampleItem.getId() + "');\">");
                        myListHtml.append("<img src=\"images/edit.gif\" alt=\"Edit\" ");
                        myListHtml.append("title=\"Edit Sample\" /></a>");
                        myListHtml.append("<a href=\"javascript:deleteSample('"
                            + sampleItem.getId()
                            + "');\"><img src=\"images/delete.gif\" ");
                        myListHtml.append("alt=\"Delete\" title=\"Delete Sample\" /></a>");
                        // Save to Dataset
                        myListHtml.append("<input type=\"hidden\" id=\"sampleName_"
                            + sampleItem.getId() + "\" value=\"" + sampleItem.getSampleName()
                            + "\"  />");
                        if (projectAdminFlag) {
                            myListHtml.append("<a href=\"javascript:s2dSaveSample('"
                                + sampleItem.getId() + "', true);\">"
                                    + SamplesHelper.SAVE_ICON + "</a>");
                        }
                    }
                    if (sampleItem.getGlobalFlag().booleanValue()) {
                        myListHtml.append("<img src=\"images/users.gif\" alt=\"(shared)\" "
                                + "title=\"You have shared this sample\" />");
                    }
                    myListHtml.append("</span>");
                    myListHtml.append("</li>\n");
                } else {
                    sharedListHtml.append("\t\t<li id=\"li_");
                    sharedListHtml.append(sampleItem.getId());
                    sharedListHtml.append("\"");
                    if (selectableItem.isSelected()) {
                        sharedListHtml.append(" class=\"selectedSkill\"");
                    }
                    sharedListHtml.append("\">");
                    sharedListHtml.append("<a class=\"sampleName\" title=\"");
                    sharedListHtml.append(sampleItem.getSampleName());
                    if (sampleItem.getDescription() != null) {
                        sharedListHtml.append(" : " + sampleItem.getDescription());
                    }
                    //NAME IS FOR TESTING! DO NOT CHANGE OR REMOVE.
                    sharedListHtml.append("\" name=\"" + sampleItem.getSampleName());
                    sharedListHtml.append("\" href=\"javascript:selectSample('"
                            + sampleItem.getId() + "');\">");
                    sharedListHtml.append(croppedName + "</a>");
                    if (editFlag) {
                        sharedListHtml.append("<span class=\"icons\">");
                        sharedListHtml.append("<a href=\"javascript:editSample('"
                            + sampleItem.getId() + "');\"><img src=\"images/edit.gif\" ");
                        sharedListHtml.append("alt=\"Edit\" title=\"Edit Sample\" /></a>");
                        sharedListHtml.append("<input type=\"hidden\" id=\"sampleName_"
                                + sampleItem.getId() + "\" value=\"" + sampleItem.getSampleName()
                                + "\"  />");
                        if (projectAdminFlag) {
                            sharedListHtml.append("<a href=\"javascript:s2dSaveSample('"
                                + sampleItem.getId() + "', true);\">"
                                    + SamplesHelper.SAVE_ICON + "</a>");
                        }
                        sharedListHtml.append("</span>");
                    }
                    sharedListHtml.append("</li>\n");
                }
            } // end for loop
        }

        myListHtml.append("</ul></li>");
        sharedListHtml.append("</ul></li>");
        navString.append(myListHtml);
        navString.append(sharedListHtml);
        navString.append("</ul>\n");
        if (displayDiv) {
            navString.append("</div>\n");
        }
        return navString.toString();
    }

    /**
     * Display Skill NavBox.
     * @param info session info
     * @return JSON Object with the data
     * @throws JSONException JSON Exception
     */
    public JSONObject displaySkillNav(DatasetContext info) throws JSONException {
        logger.debug("displaySkillNav");
        List skillList = info.getNavContext().getSkillList();

        JSONObject itemListJSON = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        synchronized (skillList) {
            Collections.sort(skillList);

            for (Iterator sIter = skillList.iterator(); sIter.hasNext();) {
                SelectableItem selectableItem = (SelectableItem)sIter.next();
                SkillItem item = (SkillItem)selectableItem.getItem();

                JSONObject itemJSON = new JSONObject();
                itemJSON.put("id", item.getId());
                itemJSON.put("name", item.getSkillName());
                itemJSON.put("isSelected", selectableItem.isSelected());
                jsonArray.put(itemJSON);
            } //end for loop
        }

        if (jsonArray.length() > 0) {
            itemListJSON.put("itemList", jsonArray);
        } else {
            itemListJSON.put("itemList", "");
        }

        if (logger.isTraceEnabled()) {
            logger.trace("Sending skill list as JSONObject: " + itemListJSON);
        }

        return itemListJSON;
    }

    /**
     * Builds a javascript string containing all information necessary for building
     * a navigation item for the problems.
     * @param info information stored in the HTTP session via DatasetContext class.
     * @param multiSelect will this list be multiple or single select.
     * @return String - a string containing a javascript array.  The string is of the
     * format [id(integer)][isSelected(T/F)][name].
     * @throws JSONException JSON exception
     */
    public JSONObject displayProblemNav(DatasetContext info, boolean multiSelect)
        throws JSONException {
        logger.debug("Displaying ProblemNav");
        List<SelectableItem> problemList = info.getNavContext().getProblemList();
        ProblemItem selectedProblem = info.getNavContext().getSelectedProblem();

        JSONObject itemListJSON = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        synchronized (problemList) {
            Collections.sort(problemList);

            for (SelectableItem selectableItem : problemList) {
                ProblemItem item = (ProblemItem)selectableItem.getItem();
                boolean isSelected = multiSelect ? selectableItem.isSelected()
                        : item.equals(selectedProblem);

                jsonArray.put(json("id", item.getId(), "name", item.getProblemName(),
                        "isSelected", isSelected));
            } //end for loop
        }

        if (jsonArray.length() > 0) {
            itemListJSON.put("itemList", jsonArray);
        } else {
            itemListJSON.put("itemList", "");
        }

        if (logger.isTraceEnabled()) {
            logger.trace("Sending problem list as JSONObject: " + itemListJSON);
        }

        return itemListJSON;
    }

    /**
     * Display Student NavBox. (Right?)
     * @param info the session info
     * @return a JSON Object with student data
     * @throws JSONException JSON exception
     */
    public JSONObject displayStudentNav(DatasetContext info) throws JSONException {
        logger.debug("Displaying StudentNav");

        List studentList = info.getNavContext().getStudentList();

        JSONObject itemListJSON = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        synchronized (studentList) {
            Collections.sort(studentList);

            for (Iterator sIter = studentList.iterator(); sIter.hasNext();) {
                SelectableItem selectableItem = (SelectableItem)sIter.next();
                StudentItem item = (StudentItem)selectableItem.getItem();

                JSONObject itemJSON = new JSONObject();
                itemJSON.put("id", item.getId());
                itemJSON.put("name", item.getAnonymousUserId());
                itemJSON.put("isSelected", selectableItem.isSelected());
                jsonArray.put(itemJSON);
            } //end for loop
        }

        if (jsonArray.length() > 0) {
            itemListJSON.put("itemList", jsonArray);
        } else {
            itemListJSON.put("itemList", "");
        }

        if (logger.isTraceEnabled()) {
            logger.trace("Sending student list as JSONObject: " + itemListJSON);
        }

        return itemListJSON;
    }

    /** Constant used for disabled tab tooltip. */
    private static final String TOOLTIP_NOT_LOGGED_IN =
        "You must be logged in to use this feature.";

    /** Constant used for disabled tab tooltip. */
    private static final String TOOLTIP_NO_PERMISSION =
        "You do not have permission to access this dataset.";

    /** Constant used for disabled tab tooltip. */
    private static final String TOOLTIP_NO_TRANSACTION_DATA =
        "There is no transaction data for this dataset.";

    /** Constant used for disabled tab tooltip. */
    private static final String TOOLTIP_REMOTE_DATASET =
        "This dataset can be found on a remote DataShop instance.";

    /**
     * Creates and returns the HTML for the list of tabs across the screen.
     * @param activeTab the Tab currently selected.
     * @param datasetContext the DatasetContext
     * @param isAuthorized flag that tells if current user is authorized to access the dataset
     * @param user logged in user, null if not logged in
     * @return String of HTML for the tabs.
     */
    public String displayTabs(String activeTab,
                              DatasetContext datasetContext,
                              boolean isAuthorized, UserItem user) {

        Integer datasetId = (Integer)datasetContext.getDataset().getId();
        Boolean hasData = true;
        if (datasetContext.getNumTransactions() == 0) { hasData = false; }
        Boolean isRemote = isDatasetRemote(datasetContext.getDataset());
        Boolean userLoggedIn = (user == null) ? false : true;
        Boolean isAdmin = false;
        if (user != null && user.getAdminFlag()) {
            isAdmin = true;
        }
        String tooltipStr = null;
        if (!userLoggedIn) {
            tooltipStr = TOOLTIP_NOT_LOGGED_IN;
        } else if (!isAuthorized) {
            tooltipStr = TOOLTIP_NO_PERMISSION;
        } else if (isRemote) {
            tooltipStr = TOOLTIP_REMOTE_DATASET;
        } else if (!hasData) {
            tooltipStr = TOOLTIP_NO_TRANSACTION_DATA;
        }

        return displayTabs(activeTab, datasetId,
                           isAuthorized, isRemote, userLoggedIn, hasData, isAdmin,
                           tooltipStr, user);
    }

    /**
     * Creates and returns the HTML for the list of tabs across the screen.
     * @param activeTab the Tab currently selected.
     * @param datasetId The current dataset.
     * @param isAuthorized flag that tells if current user is authorized to access the dataset
     * @param user logged in user, null if not logged in
     * @return String of HTML for the tabs.
     */
    public String displayTabs(String activeTab, Integer datasetId,
                              boolean isAuthorized, UserItem user) {

        Boolean isRemote = isDatasetRemote(datasetId);
        Boolean userLoggedIn = (user == null) ? false : true;
        Boolean isAdmin = false;
        if (user != null && user.getAdminFlag()) {
            isAdmin = true;
        }
        String tooltipStr = null;
        if (!userLoggedIn) {
            tooltipStr = TOOLTIP_NOT_LOGGED_IN;
        } else if (!isAuthorized) {
            tooltipStr = TOOLTIP_NO_PERMISSION;
        } else if (isRemote) {
            tooltipStr = TOOLTIP_REMOTE_DATASET;
        }
        return displayTabs(activeTab, datasetId,
                           isAuthorized, isRemote, userLoggedIn, true, isAdmin, tooltipStr, user);
    }

    /**
     * Creates and returns the HTML for the list of tabs across the screen.
     * @param activeTab the Tab currently selected.
     * @param datasetId The current dataset.
     * @param isAuthorized if the user is authorized to view the dataset
     * @param isRemote if the dataset is remote
     * @param userLoggedIn if the user is logged in
     * @param  hasData if the dataset has transactions
     * @param isAdmin if the user admin flag is true
     * @param tooltip the tooltip to use for disabled items
     * @param user logged in user, null if not logged in
     * @return String of HTML for the tabs.
     */
    private String displayTabs(String activeTab, Integer datasetId,
           Boolean isAuthorized, Boolean isRemote, Boolean userLoggedIn,
               Boolean hasData, Boolean isAdmin, String tooltip, UserItem user) {

        DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
        DatasetItem datasetItem = datasetDao.get(datasetId);
        Long numAttachments = datasetDao.countPapers(datasetItem)
                + datasetDao.countExternalAnalyses(datasetItem)
                + datasetDao.countFiles(datasetItem);
        String attachmentsStr = "";
        if (numAttachments > 0) {
            attachmentsStr = " <span class=\"fileCount\">(" + numAttachments + ")</span>";
        }

        String htmlStr = "<ul>\n";
        Boolean tabEnabled = isAuthorized && userLoggedIn && !isRemote && hasData;

        // (DS1427) To differentiate the color of the letters when disabled
        String tagClass = "";
        if (!tabEnabled || isRemote) {
            tagClass = " class=\"disabledItem\"";
        }
        String disabledItemTitle = "";
        if (tooltip != null) {
            disabledItemTitle = " title=\"" + tooltip + "\"";
        }

        if (activeTab.compareTo("DatasetInfoReport") == 0) {
            htmlStr += "\t<li id=\"dsinfoTab\"><span "
                    + ">Dataset Info</span></li>\n";
        } else {
            htmlStr += "\t<li id=\"dsinfoTab\"><a href=\"DatasetInfo?datasetId="
                    + datasetId + "\">Dataset Info</a></li>\n";
        }

        if (activeTab.compareTo("FilesInfo") == 0 || isRemote) {
            htmlStr += "\t<li id=\"fiTab\"><span" + tagClass + disabledItemTitle
                    + ">Files" + attachmentsStr + "</span></li>\n";
        } else {
            htmlStr += "\t<li id=\"fiTab\">" + "<a href=\"Files?datasetId="
                    + datasetId + "\"><span class=\"filesText\">Files</span>"
                    + attachmentsStr + "</a></li>\n";
        }

        if (activeTab.compareTo("Performance_Profiler") == 0 || !tabEnabled) {
            htmlStr += "\t<li id=\"ppTab\"><span" + tagClass + disabledItemTitle
                    + ">Performance Profiler</span></li>\n";
        } else {
            htmlStr += "\t<li id=\"ppTab\">"
                    + "<a href=\"PerformanceProfiler?datasetId=" + datasetId
                    + "\">Performance Profiler</a></li>\n";
        }

        if (activeTab.compareTo("Error_Report") == 0 || !tabEnabled) {
            htmlStr += "\t<li id=\"erTab\"><span" + tagClass + disabledItemTitle
                    + ">Error Report</span></li>\n";
        } else {
            htmlStr += "\t<li id=\"erTab\"><a href=\"ErrorReport?datasetId="
                    + datasetId + "\">Error Report</a></li>\n";
        }

        if (activeTab.compareTo("Learning_Curve") == 0 || !tabEnabled) {
            htmlStr += "\t<li id=\"lcTab\"><span" + tagClass + disabledItemTitle
                    + ">Learning Curve</span></li>\n";
        } else {
            htmlStr += "\t<li id=\"lcTab\"><a href=\"LearningCurve?datasetId="
                    + datasetId + "\">Learning Curve</a></li>\n";
        }

        if (activeTab.compareTo("Exporter") == 0 || !tabEnabled) {
            htmlStr += "\t<li id=\"exportTab\"><span" + tagClass + disabledItemTitle
                    + ">Export</span></li>\n";
        } else {
            htmlStr += "\t<li id=\"exportTab\"><a href=\"Export?datasetId="
                    + datasetId + "\">Export</a></li>\n";
        }

        htmlStr += "\t<li id=\"wfTab\">"
                + "<a href=\"LearnSphere?datasetId=" + datasetId
                + "\" target=\"blank\">Workflows</a></li>\n";

        htmlStr += "</ul>\n";

        return htmlStr;
    }

    /**
     * Creates and returns the HTML for displaying the curriculum bread
     * crumb navigation.
     * @param info information stored in the HTTP session via DatasetContext class.
     * @return String of HTML for the bread crumb navigation.
     */
    public String displayBreadCrumb(DatasetContext info) {
        String htmlStr = "";
        DatasetItem currentDataset = info.getDataset();

        if (currentDataset != null && currentDataset.getDatasetName() != null) {
            htmlStr = "<ul>\n"
                + "\t<li><a href=\"index.jsp\">Home</a> ></li>\n"
                + "\t<li>"
                + currentDataset.getDatasetName()
                + "</li>\n"
                + "</ul>\n";
        }
        return htmlStr;
    }

    /**
     * Creates and returns the name of the dataset for displaying the header navigation.
     * @param info information stored in the HTTP session via DatasetContext class.
     * @return String dataset name with no HTML
     */
    public String displayDatasetName(DatasetContext info) {
        String str = "";
        DatasetItem currentDataset = info.getDataset();
        if (currentDataset != null && currentDataset.getDatasetName() != null) {
            str = currentDataset.getDatasetName();
        }
        return str;
    }

    /**
     * Changes a sample from selected to not selected.
     * Also refreshes the sample list against the DB and refreshes dependant lists.
     * @param info information stored in the HTTP session via DatasetContext class.
     * @param sampleId Integer of the sampleId to select/deselect.
     */
    public void selectSample(Integer sampleId, DatasetContext info) {
        selectSample(sampleId, info, true);
    }

    /**
     * Makes a sample selected.
     * Also refreshes the sample list against the DB and refreshes dependant lists.
     * @param info information stored in the HTTP session via DatasetContext class.
     * @param sampleId Integer of the sampleId to select.
     * @param allowDeselect Boolean of whether or not to deselect the sample if selected
     * deselect the sample if it is current selected.
     */
    private void selectSample(Integer sampleId, DatasetContext info,
            boolean allowDeselect) {
        if (logger.isDebugEnabled()) {
            logger.debug("selectSample: begin select sample with id: " + sampleId);
        }
        SampleDao sampleDao = DaoFactory.DEFAULT.getSampleDao();
        SampleItem selectedSample = sampleDao.get(sampleId);

        //get the current list of items from the database.
        List databaseSampleList = sampleDao.find(info.getDataset(), info.getUser());
        //if there are no samples in the DB initialize the sample list so that
        //the default gets created.
        if (databaseSampleList.size() == 0) {
            initializeSampleList(info);
        }

        List sampleList = info.getNavContext().getSampleList();
        synchronized (sampleList) {
            for (Iterator iter = sampleList.iterator(); iter.hasNext();) {
                SelectableItem listItem = (SelectableItem)iter.next();
                SampleItem sampleListItem = (SampleItem)listItem.getItem();

                //check that the sample item from the current list is still in the database.
                if (databaseSampleList.contains(sampleListItem)) {
                    //remove it from the database sample if it exists.
                    databaseSampleList.remove(sampleListItem);
                }
            }
        }

        //if any items are left in the databaseSampleList they are new additions
        //and should be added to the currentSamples list.
        for (Iterator it = databaseSampleList.iterator(); it.hasNext();) {
            info.getNavContext().addSample(new SelectableItem((Comparable)it.next()));
        }
        if (logger.isDebugEnabled() && databaseSampleList.size() > 0) {
            logger.debug("selectSample: New samples in database. Adding "
                    + databaseSampleList.size() + " items to currentSamplesList");
        }

        sampleList = info.getNavContext().getSampleList();
        synchronized (sampleList) {
        for (Iterator iter = sampleList.iterator(); iter.hasNext();) {
                SelectableItem listItem = (SelectableItem)iter.next();
                SampleItem sampleListItem = (SampleItem)listItem.getItem();

                //see if the sample is the one we want to select.
                if (sampleListItem.equals(selectedSample)) {
                    if (listItem.isSelected() && allowDeselect) {
                        listItem.setIsSelected(false);
                    } else {
                        listItem.setIsSelected(true);
                    }
                }
            }
        }

        //now update the dependant lists.
        initializeStudentList(info);
        initializeProblemList(info);
        initializeSkillModelList(info);
        logger.debug("selectSample: end");
    }

    /**
     * Makes a sample selected.
     * Also refreshes the sample list against the DB and refreshes dependant lists.
     * @param info information stored in the HTTP session via DatasetContext class.
     * @param sampleIdListString a comma separated list of sample IDs
     * @param allowDeselect Boolean of whether or not to deselect the sample if selected
     * deselect the sample if it is current selected.
     */
    private void selectSamples(String sampleIdListString, DatasetContext info,
            boolean allowDeselect) {
        if (logger.isDebugEnabled()) {
            logger.debug("selectSample: begin select sample with ids: " + sampleIdListString);
        }
        SampleDao sampleDao = DaoFactory.DEFAULT.getSampleDao();
        String[] ids = sampleIdListString.split(",");
        List<SampleItem> selectedSampleList = new ArrayList();
        for (int i = 0; i < ids.length; i++) {
            // if id isn't an integer, make no changes to the list
            if (ids[i].matches("\\d+")) {
                Integer id = new Integer(ids[i]);
                SampleItem selectedSample = sampleDao.get(id);
                selectedSampleList.add(selectedSample);
            }
        }

        //
        //get the current list of items from the database.
        //
        List databaseSampleList = sampleDao.find(info.getDataset(), info.getUser());
        //if there are no samples in the DB initialize the sample list so that
        //the default gets created.
        if (databaseSampleList.size() == 0) {
            initializeSampleList(info);
        }

        List sampleList = info.getNavContext().getSampleList();
        synchronized (sampleList) {
            for (Iterator iter = sampleList.iterator(); iter.hasNext();) {
                SelectableItem listItem = (SelectableItem)iter.next();
                SampleItem sampleListItem = (SampleItem)listItem.getItem();

                //check that the sample item from the current list is still in the database.
                if (databaseSampleList.contains(sampleListItem)) {
                    //remove it from the database sample if it exists.
                    databaseSampleList.remove(sampleListItem);
                }
            }
        }

        //
        //if any items are left in the databaseSampleList they are new additions
        //and should be added to the currentSamples list.
        //
        for (Iterator it = databaseSampleList.iterator(); it.hasNext();) {
            info.getNavContext().addSample(new SelectableItem((Comparable)it.next()));
        }
        if (logger.isDebugEnabled() && databaseSampleList.size() > 0) {
            logger.debug("selectSample: New samples in database. Adding "
                    + databaseSampleList.size() + " items to currentSamplesList");
        }

        sampleList = info.getNavContext().getSampleList();
        synchronized (sampleList) {
            for (SampleItem selectedSample : selectedSampleList) {
                for (Iterator iter = sampleList.iterator(); iter.hasNext();) {
                    SelectableItem listItem = (SelectableItem)iter.next();
                    SampleItem sampleListItem = (SampleItem)listItem.getItem();

                    //see if the sample is the one we want to select.
                    if (sampleListItem.equals(selectedSample)) {
                        if (listItem.isSelected() && allowDeselect) {
                            listItem.setIsSelected(false);
                        } else {
                            listItem.setIsSelected(true);
                        }
                    }
                } // end for loop on the list of selected items
            }  // end for loop on the list of selected samples
        } // end synchronized block

        // [ysahn] the three following operations is causing major slowdown
        //now update the dependant lists.
        initializeStudentList(info);
        initializeProblemList(info);
        initializeSkillModelList(info);
        logger.debug("selectSample: end");
    }

    /** Marks all skills as un-selected.
     * @param info information stored in the HTTP session via DatasetContext class.
     */
    public void unselectAllSamples(DatasetContext info) {
        logger.debug("unselectAllSamples: begin");
        List sampleList = info.getNavContext().getSampleList();
        synchronized (sampleList) {
            for (Iterator iter = sampleList.iterator(); iter.hasNext();) {
                SelectableItem listItem = (SelectableItem)iter.next();
                listItem.setIsSelected(false);
            }
        }

        initializeStudentList(info);
        initializeProblemList(info);
        initializeSkillModelList(info);
        logger.debug("unselectAllSamples: end");
    }

    /**
     * Changes a skill from selected to not selected.
     * @param info information stored in the HTTP session via DatasetContext class.
     * @param selSkillId a Long of the skill id to switch selection status on.
     */
    public void selectSkill(Long selSkillId, DatasetContext info) {
        List skillList = info.getNavContext().getSkillList();
        synchronized (skillList) {
            for (Iterator iter = skillList.iterator(); iter.hasNext();) {
                SelectableItem listItem = (SelectableItem)iter.next();
                SkillItem skillItem = (SkillItem)listItem.getItem();

                if (skillItem.getId().equals(selSkillId)) {
                    //checks if the item we are unselected was previously the
                    //top skill.  If so then un-select it as the top skill.
                    if (listItem.isSelected()) {
                        listItem.setIsSelected(false);
                        if (skillItem.equals(info.getNavContext().getTopSkill())) {
                            info.getNavContext().setTopSkill(null);
                        }
                    } else {
                        listItem.setIsSelected(true);
                    }
                }
            }
        }
    }

    /**
     * Changes a student from selected to not selected or vis versa.
     * @param info information stored in the HTTP session via DatasetContext class.
     * @param selStudentId a Long of the student id to switch selection status on/off.
     */
    public void selectStudent(Long selStudentId, DatasetContext info) {
        List studentList = info.getNavContext().getStudentList();
        synchronized (studentList) {
            for (Iterator iter = studentList.iterator(); iter.hasNext();) {
                SelectableItem listItem = (SelectableItem)iter.next();
                StudentItem studentItem = (StudentItem)listItem.getItem();

                if (studentItem.getId().equals(selStudentId)) {
                    if (listItem.isSelected()) {
                        listItem.setIsSelected(false);
                    } else {
                        listItem.setIsSelected(true);
                    }
                }
            }
        }
    }

    /** Marks all students as selected.
     * @param info information stored in the HTTP session via DatasetContext class.
     */
    public void selectAllStudents(DatasetContext info) {
        List studentList = info.getNavContext().getStudentList();
        synchronized (studentList) {
            for (Iterator iter = studentList.iterator(); iter.hasNext();) {
                SelectableItem listItem = (SelectableItem)iter.next();
                listItem.setIsSelected(true);
            }
        }
    }

    /** Marks all students as un-selected.
     * @param info information stored in the HTTP session via DatasetContext class.
     */
    public void unselectAllStudents(DatasetContext info) {
        List studentList = info.getNavContext().getStudentList();
        synchronized (studentList) {
            for (Iterator iter = studentList.iterator(); iter.hasNext();) {
                SelectableItem listItem = (SelectableItem)iter.next();
                listItem.setIsSelected(false);
            }
        }
    }


    /** Marks all skills as selected.
     * @param info information stored in the HTTP session via DatasetContext class.
     */
    public void selectAllSkills(DatasetContext info) {
        List<SelectableItem> skillList = info.getNavContext().getSkillList();
        synchronized (skillList) {
            for (SelectableItem listItem : skillList) {
                listItem.setIsSelected(true);
            }
        }
    }

    /** Marks all skills as un-selected.
     * @param info information stored in the HTTP session via DatasetContext class.
     */
    public void unselectAllSkills(DatasetContext info) {
        List<SelectableItem> skillList = info.getNavContext().getSkillList();
        synchronized (skillList) {
            for (SelectableItem listItem : skillList) {
                listItem.setIsSelected(false);
            }
        }
        info.getNavContext().setTopSkill(null);
    }


    /** Marks all skills as selected.
     * @param info information stored in the HTTP session via DatasetContext class.
     */
    public void selectAllProblems(DatasetContext info) {
        List itemList = info.getNavContext().getProblemList();
        synchronized (itemList) {
            for (Iterator iter = itemList.iterator(); iter.hasNext();) {
                SelectableItem listItem = (SelectableItem)iter.next();
                listItem.setIsSelected(true);
            }
        }
    }

    /** Marks all skills as un-selected.
     * @param info information stored in the HTTP session via DatasetContext class.
     */
    public void unselectAllProblems(DatasetContext info) {
        List itemList = info.getNavContext().getProblemList();
        synchronized (itemList) {
            for (Iterator iter = itemList.iterator(); iter.hasNext();) {
                SelectableItem listItem = (SelectableItem)iter.next();
                listItem.setIsSelected(false);
            }
        }
        info.getNavContext().setTopSkill(null);
    }

    /**
     * Sets the top skill id, that is the skill id of the skill
     * shown in the big graph at the top.
     * @param id skill id
     * @param info information stored in the HTTP session via DatasetContext class.
     */
    public void setTopSkill(Long id, DatasetContext info) {
        if (id == null) {
            info.getNavContext().setTopSkill(null);
        } else {
            SkillDao skillDao = DaoFactory.DEFAULT.getSkillDao();
            info.getNavContext().setTopSkill(skillDao.get(id));
        }
    }

    /**
     * Sets the top student id, that is the student id of the student
     * shown in the big graph at the top.
     * @param id student id
     * @param info information stored in the HTTP session via DatasetContext class.
     */
    public void setTopStudent(Long id, DatasetContext info) {
        if (id == null) {
            info.getNavContext().setTopStudent(null);
        } else {
            StudentDao studentDao = DaoFactory.DEFAULT.getStudentDao();
            info.getNavContext().setTopStudent(studentDao.get(id));
        }
    }

    /**
     * Selects a particular problem, deselects all others.
     * @param selProblem the problem to switch selection to.
     * @param info information stored in the HTTP session via DatasetContext class.
     * @param multiSelect flag indicating whether to allow multiple selections or single only.
     */
    public void selectProblem(Integer selProblem, DatasetContext info, boolean multiSelect) {
        List problemList = info.getNavContext().getProblemList();
        synchronized (problemList) {
            for (Iterator iter = problemList.iterator(); iter.hasNext();) {
                SelectableItem selectableItem = (SelectableItem)iter.next();
                ProblemItem problemItem = (ProblemItem)selectableItem.getItem();
                if (((Long)problemItem.getId()).intValue() == selProblem.intValue()) {
                    if (multiSelect) {
                        if (selectableItem.isSelected()) {
                            selectableItem.setIsSelected(false);
                        } else {
                            selectableItem.setIsSelected(true);
                        }
                    } else {
                        info.getNavContext().setSelectedProblem(problemItem);
                    }
                }
            }
        }
    }

    /**
     * Selects a particular skillModel, deselects all others.
     * @param selModel the id of the skill model to switch selection to.
     * @param info information stored in the HTTP session via DatasetContext class.
     */
    public void selectSkillModel(int selModel, DatasetContext info) {
        logger.debug("Select Skill Model: " + selModel);
        List<SelectableItem> skillModelList = info.getNavContext().getSkillModelList();
        synchronized (skillModelList) {
            for (SelectableItem selectableItem : skillModelList) {
                SkillModelItem modelItem = (SkillModelItem)selectableItem.getItem();
                boolean selected = ((Long)modelItem.getId()).intValue() == selModel;
                selectableItem.setIsSelected(selected);
                if (selected) {
                    logger.info("Select Primary KCM (" + selModel + ") for user: "
                        + info.getUser().getId());
                }
            }
        }

        if (getSecondarySelectedSkillModel(info) != null
                && getSecondarySelectedSkillModel(info) == getSelectedSkillModel(info)) {
            selectSecondarySkillModel(null, info);
            logger.debug("Primary and Secondary Models the same, setting secondary to NULL");
        }
        updateSkillSetList(info); // change the list of sets in the list
        initializeSkillList(info, false);  //changing skill models changes the skills displayed
    }

    /**
     * Selects a particular skillModel, deselects all others.
     * @param selModel the id of the skill model to switch selection to.
     * @param info information stored in the HTTP session via DatasetContext class.
     */
    public void selectSecondarySkillModel(Long selModel, DatasetContext info) {
        logger.debug("Select Skill Model: " + selModel);
        List skillModelList = info.getNavContext().getSecondarySkillModelList();
        synchronized (skillModelList) {
            for (Iterator iter = skillModelList.iterator(); iter.hasNext();) {
                SelectableItem selectableItem = (SelectableItem)iter.next();
                SkillModelItem modelItem = (SkillModelItem)selectableItem.getItem();
                if (((Long)modelItem.getId()).equals(selModel)) {
                    logger.info("Select Secondary KCM (" + selModel + ") for user: "
                        + info.getUser().getId());
                    selectableItem.setIsSelected(true);
                } else {
                    selectableItem.setIsSelected(false);
                }
            }
        }
    }

    /**
     * Changes a skill set from selected to not selected.
     * @param info information stored in the HTTP session via DatasetContext class.
     * @param setId an integer of the set id to switch selection status on.
     * @param skillModelItem the selected primary skill model
     * @param newSkillList the set of skills which are part of the given set
     */
    public void selectSkillSet(DatasetContext info, Integer setId,
            SkillModelItem skillModelItem, List newSkillList) {

        if (logger.isDebugEnabled()) {
            logger.debug("selectSkillSet: " + setId + " with " + newSkillList.size() + " skills.");
        }

        // First set the set item as selected
        selectSkillSet(info, setId);

        // Then updated the selected skills by going through all the skills in the
        // current skill list, then selecting it, if it is in the set, or
        // deselecting it if it is not.
        List currentSkillList = info.getNavContext().getSkillList();
        synchronized (currentSkillList) {
            for (Iterator it = currentSkillList.iterator(); it.hasNext();) {
                SelectableItem selectable = (SelectableItem)it.next();
                SkillItem skillItem = (SkillItem)selectable.getItem();

                if (newSkillList.contains(skillItem)) {
                    selectable.setIsSelected(true);
                    if (logger.isDebugEnabled()) {
                        logger.debug("selecting: " + skillItem.getSkillName());
                    }
                } else {
                    selectable.setIsSelected(false);
                    if (logger.isDebugEnabled()) {
                        logger.debug("deselecting: " + skillItem.getSkillName());
                    }
                }
            }
        }
    }

    /**
     * Select a skill set.
     * @param info get the list of sets from the navigation context
     * @param newSetId the set id to select
     */
    public void selectSkillSet(DatasetContext info, Integer newSetId) {
        if (logger.isDebugEnabled()) {
            logger.debug("selectSkillSet: " + newSetId);
        }

        // First set the set item as selected
        List setList = info.getNavContext().getSkillSetList();
        synchronized (setList) {
            for (Iterator iter = setList.iterator(); iter.hasNext();) {
                SelectableItem listItem = (SelectableItem)iter.next();
                SetItem setItem = (SetItem)listItem.getItem();

                if (setItem.getId().equals(newSetId)) {
                    listItem.setIsSelected(true);
                    logger.info("Selecting set: " + setItem.getName()
                            + " for user: " + info.getUser().getId());
                    if (logger.isDebugEnabled()) {
                        logger.debug("selectSkillSet: calling setIsSelected(true)" + listItem);
                    }
                } else {
                    logger.info("Deselecting set: " + setItem.getName()
                            + " for user: " + info.getUser().getId());
                    listItem.setIsSelected(false);
                    if (logger.isDebugEnabled()) {
                        logger.debug("selectSkillSet: calling setIsSelected(false)" + listItem);
                    }
                }
            }
        }
        // Right after skill set loaded, it is not modified
        info.getNavContext().setSkillSetModified(false);
    }

    /**
     * Returns the selected skill set.
     * @param info information stored in the HTTP session via DatasetContext class.
     * @return id of the selected skill set, null otherwise
     */
    public Integer getSelectedSkillSetId(DatasetContext info) {
        SetItem selectedSetItem = getSelectedSkillSetItem(info);
        if (selectedSetItem != null) {
            return selectedSetItem.getSetId();
        }
        return null;
    }

    /**
     * Get the selected skill set item.
     * @param info the dataset context.
     * @return a set item.
     */
    public SetItem getSelectedSkillSetItem(DatasetContext info) {
        SetItem selectedItem = null;
        logger.debug("getSelectedSkillSetItem: start");

        List setList = info.getNavContext().getSkillSetList();
        synchronized (setList) {
            for (Iterator iter = setList.iterator(); iter.hasNext();) {
                SelectableItem selectableItem = (SelectableItem)iter.next();
                if (selectableItem.isSelected()) {
                    selectedItem = (SetItem)selectableItem.getItem();
                }
            }

            if (selectedItem == null) {
                logger.debug("getSelectedSkillSetItem: done : returning null");
                return null;
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("getSelectedSkillSetItem: done : returning set: "
                            + selectedItem.getName() + " (" + selectedItem.getId() + ")");
                }
                return selectedItem;
            }
        }
    }
    /**
     * Rename a skill set.
     * @param info the dataset context.
     * @param oldName the current name of the skill set.
     * @param newName the new name of the skill set.
     */
    public void renameSkillSetItem(DatasetContext info, String oldName, String newName) {
        List setList = info.getNavContext().getSkillSetList();
        synchronized (setList) {
            for (Iterator iter = setList.iterator(); iter.hasNext();) {
                SelectableItem listItem = (SelectableItem)iter.next();
                SetItem setItem = (SetItem)listItem.getItem();
                if (setItem.getName().equals(oldName)) {
                    setItem.setName(newName);
                    break;
                }
            }
        }
    }

    /**
     * Marks all skill sets as un-selected.
     * @param info information stored in the HTTP session via DatasetContext class.
     */
    public void unselectAllSkillSets(DatasetContext info) {
        List setList = info.getNavContext().getSkillSetList();
        synchronized (setList) {
            for (Iterator iter = setList.iterator(); iter.hasNext();) {
                SelectableItem listItem = (SelectableItem)iter.next();
                listItem.setIsSelected(false);
            }
        }
    }

    /**
     * Clears the set list and adds just the sets in the selected skill model.
     * @param info information stored in the HTTP session via DatasetContext class
     */
    public void updateSkillSetList(DatasetContext info) {
        SetDao setDao = DaoFactory.DEFAULT.getSetDao();
        SkillModelItem skillModelItem = getSelectedSkillModelItem(info);
        // If skillModelItem not found, our work here is done.
        if (skillModelItem == null) { return; }
        List setItemList = setDao.findSkillSets(skillModelItem);
        List selectableItemlist = new ArrayList <SelectableItem> ();
        for (Iterator iter = setItemList.iterator(); iter.hasNext();) {
            SetItem setItem = (SetItem)iter.next();
            SelectableItem selectableItem = new SelectableItem(setItem);
            selectableItemlist.add(selectableItem);
        }
        info.getNavContext().setSkillSetList(selectableItemlist);
    }

    /**
     * Handle the post and get flags for all navigation helper items.
     * @param req HttpServletRequest from a servlet.
     * @param info information stored in the HTTP session via DatasetContext class.
     * @return a string of the update item if an AJAX update is required,
     *  or null if no item was updated or is a full page refresh update.
     */
    public String updateNav(HttpServletRequest req, final DatasetContext info) {

        String sampleIdParam = req.getParameter("sampleId"); //param
        if (sampleIdParam != null && sampleIdParam.compareTo("") != 0) {
            if (sampleIdParam.startsWith("unselect_all")) {
                logger.info("Unselect all samples for user: " + info.getUser().getId());
                unselectAllSamples(info);
            } else {
                if (sampleIdParam.indexOf(',') < 0) {
                    Integer id = new Integer(sampleIdParam);
                    logger.info("Select sample (" + id + ") for user: " + info.getUser().getId());
                    selectSample(id, info);
                } else {
                    logger.info("Select samples (" + sampleIdParam
                            + ") for user: " + info.getUser().getId());
                    selectSamples(sampleIdParam, info, true);
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("NavigationHelper.updateNav sampleId: " + sampleIdParam);
            }
            return null; //sample is a large enough change to warrant a full refresh.
        }

        String sampleListRefreshString = req.getParameter("sampleListRefresh"); // param
        if (sampleListRefreshString != null && sampleListRefreshString.compareTo("") != 0) {
            logger.debug("NavigationHelper.updateNav refresh sample list");

            String updatedSample = req.getParameter("updatedSample"); //param
            if (updatedSample != null && updatedSample.compareTo("") != 0) {
                logger.debug("NavigationHelper.updateNav updatedSample = " + updatedSample);
                selectSample(new Integer(updatedSample), info, false);
            }
            return null; // do a full page refresh, please
        }

        String listParam = req.getParameter("list");
        if (listParam != null && listParam.compareTo("") != 0) {
            return updateNavLists(req, info);
        }

        String topSkillIdParam = req.getParameter("topSkillId"); // param
        if (topSkillIdParam != null && topSkillIdParam.compareTo("") != 0) {
            setTopSkill(topSkillIdParam.equals("null") ? null : new Long(topSkillIdParam), info);
            logger.debug("NavigationHelper.updateNav topSkill: " + topSkillIdParam);
        }

        String skillModelIdParam = req.getParameter("skillModelId"); // param
        if (skillModelIdParam != null && skillModelIdParam.compareTo("") != 0) {
            int id = new Integer(skillModelIdParam).intValue();
            selectSkillModel(id, info);
            info.getNavContext().setUserSelectedKCM(true);
            logger.info("Select KCM (" + id + ") for user: " + info.getUser().getId());
            logger.debug("NavigationHelper.updateNav selSkillModelId: " + skillModelIdParam);
            logger.debug("NavigationHelper.updateNav top skill is : " + getTopSkill(info));
        }

        String secondarySkillModelIdParam = req.getParameter("secondarySkillModelId"); // param
        if (secondarySkillModelIdParam != null && secondarySkillModelIdParam.compareTo("") != 0) {
            if (secondarySkillModelIdParam.equals("none_selected")) {
                logger.info("Deselect Secondary KCM for user: " + info.getUser().getId());
                selectSecondarySkillModel(null, info);
            } else {
                Long id = new Long(secondarySkillModelIdParam);
                selectSecondarySkillModel(id, info);
                logger.info("Select Secondary KCM (" + id + ") for user: "
                        + info.getUser().getId());
            }
            logger.debug("NavigationHelper.updateNav selSecondarySkillModelId: "
                    + secondarySkillModelIdParam);
        }

        String skillSetIdParam = req.getParameter("skillSetId"); // param
        if (skillSetIdParam != null && skillSetIdParam.compareTo("") != 0) {
            logger.debug("NavigationHelper.updateNav refresh skill set to " + skillSetIdParam);
            if (skillSetIdParam.equals("null")) {
                logger.info("Deselect all skill sets for user: " + info.getUser().getId());
                unselectAllSkillSets(info);
            } else {
                Integer setId = new Integer(skillSetIdParam);
                SetDao setDao = DaoFactory.DEFAULT.getSetDao();
                SetItem setItem = setDao.get(setId);
                UserItem userItem = info.getUser();
                DatasetItem datasetItem = info.getDataset();
                SkillModelItem skillModelItem = getSelectedSkillModelItem(info);
                SetHelper setHelper = HelperFactory.DEFAULT.getSetHelper();
                List skillList = setHelper.loadSkillSet(datasetItem, skillModelItem,
                        userItem, setItem);
                selectSkillSet(info, setId, skillModelItem, skillList);
                logger.info("Select skill set (" + setId + ") for user: " + info.getUser().getId());
            }
            logger.debug("NavigationHelper.updateNav skillSetId: " + skillSetIdParam);
        }

        String topStudentIdParam = req.getParameter("topStudentId"); // param
        if (topStudentIdParam != null && topStudentIdParam.compareTo("") != 0) {
            if (topStudentIdParam.equals("null")) {
                setTopStudent(null, info);
            } else {
                Long topStudentId = new Long(topStudentIdParam);
                setTopStudent(topStudentId, info);
            }
            logger.debug("NavigationHelper.updateNav topStudent: " + topStudentIdParam);
        }
        return null;
    }

    /**
     * Handles the updates form the javascript navigation lists.
     * @param req the servlet request object.
     * @param info the saved session info.
     * @return String of navigation updates.
     */
    private String updateNavLists(HttpServletRequest req, DatasetContext info) {
        String listParam = req.getParameter("list");
        logger.debug("List Param : " + listParam);
        String actionParam = req.getParameter("action");
        logger.debug("Action Param : " + actionParam);
        String multiSelectParam = req.getParameter("multiSelect");
        logger.debug("MultiSelect Param : " + multiSelectParam);

        boolean multiSelect;
        if (multiSelectParam != null && multiSelectParam.compareTo("") != 0) {
            multiSelect = new Boolean(multiSelectParam).booleanValue();
        } else {
            multiSelect = true;
        }

        //handle problem actions.
        if (listParam.compareTo("problems") == 0) {
            if (actionParam.compareTo("refresh") == 0) {
                try {
                    return displayProblemNav(info, multiSelect).toString();
                } catch (JSONException exception) {
                    logger.error("DOH!", exception);
                }
            } else if (actionParam.compareTo("select") == 0)  {
                String selectIdParam = req.getParameter("itemId");
                if (selectIdParam != null && selectIdParam.compareTo("") != 0) {
                    try {
                        Integer selectId = new Integer(selectIdParam);
                        selectProblem(selectId, info, multiSelect);
                        return "problem selected AOK!";
                    } catch (NumberFormatException formatException) {
                        logger.warn("NumberFormatException :: problem list item "
                                + "param was not a number ");
                        //TODO what to return?
                    }
                }
            }  else if (actionParam.compareTo("selectAll") == 0) {
                selectAllProblems(info);
            } else if (actionParam.compareTo("deselectAll") == 0) {
                unselectAllProblems(info);
            } else if (actionParam.compareTo("getInfo") == 0) {
                String selectIdParam = req.getParameter("itemId");
                if (selectIdParam != null && selectIdParam.compareTo("") != 0) {
                    try {
                        Long selectId = new Long(selectIdParam);
                        return getProblemInfo(selectId, req);
                    } catch (NumberFormatException formatException) {
                        logger.warn("NumberFormatException :: problem list item "
                                + "param was not a number ");
                        //TODO what to return?
                    }
                }
            } else {
                logger.warn("Unknown action for skill nav :: " + actionParam);
            }
        }

        //handle skill actions.
        if (listParam.compareTo("skills") == 0) {
            try {
                if (actionParam.compareTo("refresh") == 0) {
                    return displaySkillNav(info).toString();
                } else if (actionParam.compareTo("select") == 0)  {
                    String selectIdParam = req.getParameter("itemId");
                    if (selectIdParam != null && selectIdParam.compareTo("") != 0) {
                        try {
                            Long selectId = new Long(selectIdParam);
                            selectSkill(selectId, info);
                            info.getNavContext().setSkillSetModified(true);
                            return "skill selected AOK!";
                        } catch (NumberFormatException formatException) {
                            logger.warn("NumberFormatException :: skills list item "
                                    + "param was not a number ");
                            //TODO what to return?
                        }
                    }
                } else if (actionParam.compareTo("selectAll") == 0) {
                    selectAllSkills(info);
                    info.getNavContext().setSkillSetModified(true);
                } else if (actionParam.compareTo("deselectAll") == 0) {
                    unselectAllSkills(info);
                    info.getNavContext().setSkillSetModified(true);
                } else if (actionParam.compareTo("selectSet") == 0
                        || actionParam.compareTo("deselectSet") == 0) {
                    String result = processLCPIDSelection(req, info, actionParam);
                    info.getNavContext().setSkillSetModified(true);
                    return result;
                } else {
                    logger.warn("Unknown action for skill nav :: " + actionParam);
                }

            } catch (JSONException e) {
                logger.error("updateNavLists: JSONException: ", e);
            }
        }

        //handle student actions.
        if (listParam.compareTo("students") == 0) {
            if (actionParam.compareTo("refresh") == 0) {
                try {
                    return displayStudentNav(info).toString();
                } catch (JSONException exception) {
                    logger.error("DOH!", exception);
                }
            } else if (actionParam.compareTo("select") == 0)  {
                String selectIdParam = req.getParameter("itemId");
                if (selectIdParam != null && selectIdParam.compareTo("") != 0) {
                    try {
                        Long selectId = new Long(selectIdParam);
                        selectStudent(selectId, info);
                        return "student selected AOK!";
                    } catch (NumberFormatException formatException) {
                        logger.warn("NumberFormatException :: student list item "
                                + "param was not a number ");
                        //TODO what to return?
                    }
                }
            } else if (actionParam.compareTo("selectAll") == 0) {
                selectAllStudents(info);
            } else if (actionParam.compareTo("deselectAll") == 0) {
                unselectAllStudents(info);
            } else if (actionParam.compareTo("selectSet") == 0
                    || actionParam.compareTo("deselectSet") == 0) {
                String result = processLCPIDSelection(req, info, actionParam);
                return result;
            } else {
                logger.warn("Unknown action for skill nav :: " + actionParam);
            }
        }
        return null;
    }

    /**
     * Process the LCPID select only/deselect only request.  This method handles both student
     * and skill requests.
     * @param request the http servlet request.
     * @param info the dataset context.
     * @param action whether to deselect a set of IDs or select only the set of IDs.
     * @return a success or error message.
     */
    private String processLCPIDSelection(HttpServletRequest request, DatasetContext info,
            String action) {
        String measure;
        String[] listOfIds;
        if (action != null && action != "") {
            measure = request.getParameter("list");
            if (measure == null ||  measure == "") { measure = STUDENT; }

            listOfIds = request.getParameterValues(LIST_OF_IDS_PARAM);
            /**
             * We don't really expect this could happen, because if no skills or students
             * are selected, then a LC graph cannot be generated, so the user would never
             * get to this point.  But, it's better to be defensive about things.
             */
            if (listOfIds == null || listOfIds.length == 0) {
                String msg = "List of " + measure + " ids was null or empty.";
                logger.error(msg);
                return "ERROR";
            }

            if (action.equals(DESELECT_SET)) {
                /**
                 * deselect all of these skills or students. Only skills that were
                 * currently selected can be in this list, so we don't have to worry
                 * about checking for unselected skills.
                 */
                for (String id : listOfIds) {
                    Long longId = new Long(id);
                    if (measure.equals(STUDENT)) {
                        selectStudent(longId, info);
                    } else {
                        selectSkill(longId, info);
                    }
               }
           } else {
                // select only these skills or students.
                if (measure.equals(STUDENT)) {
                    unselectAllStudents(info);
                    for (String id : listOfIds) {
                        Long studentId = new Long(id);
                        selectStudent(studentId, info);
                    }
                } else {
                    unselectAllSkills(info);
                    for (String id : listOfIds) {
                        Long skillId = new Long(id);
                        selectSkill(skillId, info);
                    }
                }
            }
        } else {
            logger.warn("A null action was passed into processLCPIDSelection.  Odd.");
        }
        return "SUCCESS";
    } // end processLCPIDSelection()

    /** this function inserts spaces in a string.  The spaces have the effect of breaking
     * up very long strings so that they will wrap nicely when displayed on a web-page.
     * The breaks will occur only after non-alpha or number chars and only at the frequency
     * of STR_SPACING_FREQ.
     * @param str - The string you wish to break up.
     * @return String - the String with spaces inserted.
     */
    public String displayString(String str) {
        String newStr = "";
        int counter = 0;
        if (str.length() > STR_SPACING_FREQ) {
            for (int i = 0; i < str.length();) {
                if (!(str.substring(i, i + 1)).matches("^[a-zA-Z0-9]$")
                        && counter > STR_SPACING_FREQ) {
                    newStr += str.charAt(i) + " ";
                    counter = 0;
                } else {
                    counter++;
                    newStr += str.charAt(i);
                }
                i++;
            }
        } else {
            newStr = str;
        }
        return newStr;
    }

    /**
     * Calls the initialize functions in the correct order.
     * <br><strong>NOTE:</strong> Assumes that Dataset and User
     * have already been set in the DatasetContext.
     * @param info information stored in the HTTP session via DatasetContext class.
     */
    public void initializeAll(DatasetContext info) {
        logger.debug("initializeAll: begin");

        initializeSampleList(info);
        // keep this before the initializeSkillList because you can't
        // get the skill list until you have the skill models
        initializeSkillModelList(info);
        //lastly initialize the list of problems and students
        initializeProblemList(info);
        initializeStudentList(info);

        logger.debug("initializeAll: end");
    }

    /**
     * Populates the currentSkills list. This is called whenever a change in curriculum occurs.
     * It gets a listing of all skill items in the curriculum, places each of those into
     * a SelectableItem and then populates the currentSkills list.
     * @param info information stored in the HTTP session via DatasetContext class.
     * @param setSelected indicates whether a skill set is selected or not
     */
    private void initializeSkillList(DatasetContext info, boolean setSelected) {
        logDebug("initializeSkillList: start");
        boolean defaultSelected = !setSelected;
        //first find the selected skill model item.
        SkillModelItem skillModel = null;
        NavigationContext navContext = info.getNavContext();
        List<SelectableItem> skillModels = navContext.getSkillModelList();

        synchronized (skillModels) {
            for (SelectableItem selectable : skillModels) {
                if (selectable.isSelected()) {
                    skillModel = (SkillModelItem)selectable.getItem();
                }
            }
        }

        List<SelectableItem> skills = navContext.getSkillList();

        synchronized (skills) {
            //get the old list so that we can attempt to retain selected items.
            List<SelectableItem> oldList = new ArrayList<SelectableItem>(skills);
            SampleDao sampleDao = DaoFactory.DEFAULT.getSampleDao();

            if (skills != null) {
                skills.clear();
            } else {
                skills = new ArrayList<SelectableItem>();
            }
            if (skillModel != null) {
                //make sure the skillModelItem is attached to the session.
                List<SelectableItem> samples = navContext.getSampleList();

                synchronized (samples) {
                    for (SelectableItem selectable : samples) {
                        if (selectable.isSelected()) {
                            SampleItem sample = (SampleItem)selectable.getItem();
                            List<SkillItem> skillList = sampleDao.getSkillList(sample, skillModel);
                            for (SkillItem skill : skillList) {
                                navContext.addSkill(new SelectableItem(skill, defaultSelected,
                                        false));
                            }
                        }
                    }
                }
                logDebug("Skill list has ", navContext.getSkillList().size(), " skill items");
            } else {
                logDebug("No skill model was selected so no skills available.");
            }

            // If the user has modifications to what skills are selected, then try to preserve them,
            // Otherwise, select the skills in the selected set if there is one.
            if (oldList.size() > 0) {
                //walk through the old list and see if any of the skills in the new list match,
                //and then set or unset them according to their isSelected status in the old list.
                for (SelectableItem selectable : oldList) {
                    logDebug("OLD LIST: ", selectable);
                    if (skills.contains(selectable)) {
                        skills.get(skills.indexOf(selectable)).
                                setIsSelected(selectable.isSelected());
                    }
                }
            } else {
                SetItem selectedSkillSetItem = getSelectedSkillSetItem(info);
                if (selectedSkillSetItem != null) {
                    // this bit is duplicated from selectSkillSet method line 1236
                    // Then updated the selected skills by going through all the skills in the
                    // current skill list, then selecting it, if it is in the set, or
                    // deselecting it if it is not.
                    SetHelper setHelper = HelperFactory.DEFAULT.getSetHelper();
                    UserItem userItem = info.getUser();
                    DatasetItem datasetItem = info.getDataset();
                    List setSkillList = setHelper.loadSkillSet(datasetItem, skillModel,
                            userItem, selectedSkillSetItem);
                    for (SelectableItem selectable : skills) {
                        SkillItem skill = (SkillItem)selectable.getItem();
                        boolean isSelected = setSkillList.contains(skill);

                        selectable.setIsSelected(isSelected);
                        logDebug((isSelected ? "de" : ""), "selecting: ", skill.getSkillName());
                    }
                }
            } // end if oldList size greater than zero
        }
        //finally unset the top skill.
        setTopSkill(null, info);
        logDebug("initializeSkillList: done");
    }

    /**
     * Populates the currentStudents list. This is called whenever a change in curriculum occurs.
     * It gets a listing off all student items in the curriculum, places each of those into
     * a SelectableItem and populates a the currentSkills list.
     * @param info information stored in the HTTP session via DatasetContext class.
     */
    private void initializeStudentList(DatasetContext info) {

        List studentList = info.getNavContext().getStudentList();
        synchronized (studentList) {
            //get the old list so that we can attempt to retain selected items.
            List oldList = new ArrayList();
            oldList.addAll(studentList);

            if (studentList != null) {
                studentList.clear();
            } else {
                studentList = new ArrayList();
            }

            SampleDao sampleDao = DaoFactory.DEFAULT.getSampleDao();

            Set newStudentSet = new HashSet();
            List sampleList = info.getNavContext().getSampleList();
            synchronized (sampleList) {
                for (Iterator it = sampleList.iterator(); it.hasNext();) {
                    SelectableItem selectable = (SelectableItem)it.next();
                    if (selectable.isSelected()) {
                        SampleItem sampleItem = (SampleItem)selectable.getItem();
                        List sampleStudentList =
                            sampleDao.getStudentListFromAggregate(sampleItem);
                        newStudentSet.addAll(sampleStudentList);
                    }
                }
            }

            for (Iterator it = newStudentSet.iterator(); it.hasNext();) {
                SelectableItem selectable =
                    new SelectableItem((Comparable)it.next(), true, false);
                info.getNavContext().addStudent(selectable);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Student list set with " + studentList.size() + " student items");
            }

            //walk through the old list and see if any of the students in the new list match,
            //and then set or unset them according to their isSelected status in the old list.
            for (Iterator it = oldList.iterator(); it.hasNext();) {
                SelectableItem selectable = (SelectableItem)it.next();
                if (studentList.contains(selectable)) {
                    ((SelectableItem)studentList.get(studentList.indexOf(selectable))).
                            setIsSelected(selectable.isSelected());
                }
            }
        }
    }

    /**
     * Populates the sampleList with a collection of selectable items.  These
     * samples are for a single dataset, and either globally visible or owned
     * by the current user.
     * @param info information stored in the HTTP session via DatasetContext class.
     */
    private void initializeSampleList(DatasetContext info) {
        SampleDao sampleDao = DaoFactory.DEFAULT.getSampleDao();
        List samples = new ArrayList();
        List sampleList = info.getNavContext().getSampleList();
        synchronized (sampleList) {
            samples = sampleDao.find(info.getDataset(), info.getUser());
            Collections.sort(samples);
            sampleList.clear();
            for (Iterator skIter = samples.iterator(); skIter.hasNext();) {
                sampleList.add(new SelectableItem((Comparable)skIter.next()));
            }

            if ((sampleList).size() < 1
                    && DaoFactory.DEFAULT.getTransactionDao().count(info.getDataset()) > 0) {
                getDefaultSample(info);
            }
        }
    }

    /**
     * Gets the default sample if none was found.
     * @param info the HttpSession info.
     */
    private void getDefaultSample(DatasetContext info) {
        SampleDao sampleDao = DaoFactory.DEFAULT.getSampleDao();
        SampleItem newSample = sampleDao.findOrCreateDefaultSample(info.getDataset());
        info.getNavContext().addSample(new SelectableItem(newSample, false, false));
    }

    /**
     * Populates the allProblem list. This is called whenever a change in datasets occurs.
     * It gets a listing of all problem items in the dataset, places each of those into
     * a SelectableItem and populates the allProblems list.
     * @param info information stored in the HTTP session via DatasetContext class.
     */
    private void initializeProblemList(DatasetContext info) {
        List<SelectableItem> problemList = info.getNavContext().getProblemList();
        synchronized (problemList) {
            //get the old list so that we can attempt to retain selected items.
            List<SelectableItem> oldList = new ArrayList<SelectableItem>();

            oldList.addAll(problemList);
            problemList.clear();

            SampleDao sampleDao = DaoFactory.DEFAULT.getSampleDao();
            Set<Comparable> newProblemSet = new HashSet<Comparable>();
            List<SelectableItem> sampleList = info.getNavContext().getSampleList();

            synchronized (sampleList) {
                for (SelectableItem selectable : sampleList) {
                    if (selectable.isSelected()) {
                        SampleItem sampleItem = (SampleItem)selectable.getItem();
                        newProblemSet.addAll(sampleDao.getProblemList(sampleItem));
                    }
                }
            }

            for (Comparable itemId : newProblemSet) {
                info.getNavContext().addProblem(new SelectableItem(itemId, true, false));
            }
            logDebug("Problem list set with ", problemList.size(), " problem items");

            //walk through the old list and see if any of the problems in the new list match,
            //and then set or unset them according to their isSelected status in the old list.
            for (SelectableItem problem : oldList) {
                int problemIndex = problemList.indexOf(problem);
                if (problemIndex != -1) {
                    problemList.get(problemIndex).setIsSelected(problem.isSelected());
                }
            }
            Collections.sort(problemList);

            //check that the selected single problem is still a valid selection.
            //if it's not set to the first item in the list.
            ProblemItem selectedSingleProblem = info.getNavContext().getSelectedProblem();
            SelectableItem selectableSingle = new SelectableItem(selectedSingleProblem);
            if (selectedSingleProblem == null || !problemList.contains(selectableSingle)) {
                ProblemItem singleSelected = problemList.size() > 0
                    ? (ProblemItem)problemList.get(0).getItem() : null;
                info.getNavContext().setSelectedProblem(singleSelected);
            }
        }
    }

    /**
     * Populates the currentSkillModels list. This is called whenever a change in datasets occurs.
     * It gets a listing off all skill models items in the curriculum, places each of those into
     * a SelectableItem and populates the currentSkillModels list.
     * @param info information stored in the HTTP session via DatasetContext class.
     */
    private void initializeSkillModelList(DatasetContext info) {
        logger.debug("initializeSkillModelList: start");

        //DS1295: KC Model Sort
        //Sort the KC models by the user settings on the KC Models page
        KCModelContext kcModelContext = info.getKCModelContext();
        String kcmSortBy = kcModelContext.getSortBy();
        Boolean kcmSortByAscendingFlag = kcModelContext.getSortByAscendingFlag();
        Boolean kcmGroupByNumObservations = false;
        KCModelHelper modelHelper = HelperFactory.DEFAULT.getKCModelHelper();
        List <SkillModelItem> skillModelList =
            modelHelper.getModelListSorted(info.getDataset(),
                                           kcmSortBy,
                                           kcmSortByAscendingFlag,
                                           kcmGroupByNumObservations);

        List <SelectableItem> infoSkillModelList = info.getNavContext().getSkillModelList();

        //get the currently selected skill model id.
        Long selectedId = getSelectedSkillModel(info);
        SkillModelItem selectedKCM = null;

        synchronized (infoSkillModelList) {
            infoSkillModelList.clear();
            int numSelected = 0;
            for (Iterator it = skillModelList.iterator(); it.hasNext();) {
                SkillModelItem item = (SkillModelItem)it.next();
                logTrace("Adding Model " + item);
                SelectableItem selectable = new SelectableItem(item);
                if (((Long)item.getId()).equals(selectedId)) {
                    selectable.setIsSelected(true);
                    numSelected++;
                    selectedKCM = item;
                }
                infoSkillModelList.add(selectable);
            }
            logTrace("Added ", infoSkillModelList.size(), " of ", skillModelList.size(), " found",
                    " skill models in dataset ", info.getDataset().getId());
            if (numSelected == 0 && infoSkillModelList.size() > 0) {
                SelectableItem selectable = (SelectableItem)infoSkillModelList.get(0);
                selectable.setIsSelected(true);
                selectedKCM = (SkillModelItem)selectable.getItem();
                logDebug("Selecting first model : ", selectedKCM.getSkillModelName());
            } else if (numSelected > 1) {
                logger.warn("More than one skill model selected");
            }
        }

        if (infoSkillModelList.size() > 1) {
            logger.debug("Initializing Secondary Model");
            //set the list of secondary skill models.
            List infoSkillModelList2 = info.getNavContext().getSecondarySkillModelList();
            selectedId = getSecondarySelectedSkillModel(info);
            logDebug("Secondary Model Selected has Id :: ", selectedId);
            synchronized (infoSkillModelList2) {
                infoSkillModelList2.clear();
                for (Iterator it = skillModelList.iterator(); it.hasNext();) {
                    SkillModelItem item = (SkillModelItem)it.next();

                    if (item.getGlobalFlag().booleanValue()
                        || info.getUser().equals(item.getOwner())) {
                        if (logger.isTraceEnabled()) {
                            logger.trace("Adding 2ndary Model " + item);
                        }
                        SelectableItem selectable = new SelectableItem(item);
                        if (((Long)item.getId()).equals(selectedId)) {
                            selectable.setIsSelected(true);
                        }
                        infoSkillModelList2.add(selectable);
                    }
                }
                if (logger.isTraceEnabled()) {
                    logger.trace("2ndary model list :: Added " + infoSkillModelList2.size()
                            + " of " + skillModelList.size() + " found"
                            + " skill models in dataset " + info.getDataset().getId());
                }

                //TODO should check if the selected skill model was/is still in the list.
                //to hold the same selection whenever possible.
                //if (infoSkillModelList.size() > 0) {
                //    ((SelectableItem)infoSkillModelList.get(0)).setIsSelected(true);
                //}
                info.getNavContext().setSecondarySkillModelList(infoSkillModelList2);
            }
        } else {
            List infoSkillModelList2 = info.getNavContext().getSecondarySkillModelList();
            selectSecondarySkillModel(null, info);
            synchronized (infoSkillModelList2) {
                infoSkillModelList2.clear();
                info.getNavContext().setSecondarySkillModelList(infoSkillModelList2);
            }
            logDebug("Clearing secondary skill model list");
        }

        // initialize the skill list in case the skill model changed.
        boolean setSelectedFlag = false;
        if (selectedKCM != null) {
            setSelectedFlag = initializeSkillSetList(info, selectedKCM);
        }

        initializeSkillList(info, setSelectedFlag);
        logger.debug("initializeSkillModelList: done");
    }

     /**
     * Only log if debugging is enabled.
     * @param args concatenate all arguments into the string to be logged
     */
    private void logDebug(Object... args) {
        LogUtils.logDebug(logger, args);
    }

    /**
     * Only log if trace is enabled.
     * @param args concatenate all arguments into the string to be logged
     */
    private void logTrace(Object... args) {
        LogUtils.logTrace(logger, args);
    }

    /**
     * Populates the skill set list. This is called whenever a change in the skill model occurs.
     * @param info information stored in the HTTP session via DatasetContext class.
     * @param selectedKCM the currently selected primary skill model
     * @return true if a set is selected, false otherwise
     */
    private boolean initializeSkillSetList(DatasetContext info, SkillModelItem selectedKCM) {
        logger.trace("INIT SET: initializeSkillSetList: start");
        boolean setSelectedFlag = false;
        //re-attach the selectedKCM to the session via a get.
        SkillModelDao skillModelDao = DaoFactory.DEFAULT.getSkillModelDao();
        selectedKCM = skillModelDao.get((Long)selectedKCM.getId());

        SetDao setDao = DaoFactory.DEFAULT.getSetDao();
        List setList = setDao.findSkillSets(selectedKCM);

        List infoSkillSetList = info.getNavContext().getSkillSetList();

        //get the currently selected skill set id
        Integer selectedId = getSelectedSkillSetId(info);

        synchronized (infoSkillSetList) {
            infoSkillSetList.clear();
            int numSelected = 0;
            for (Iterator it = setList.iterator(); it.hasNext();) {
                SetItem item = (SetItem)it.next();

                SelectableItem selectable = new SelectableItem(item);
                if (((Integer)item.getId()).equals(selectedId)) {
                    selectable.setIsSelected(true);
                    numSelected++;
                    setSelectedFlag = true;
                }
                infoSkillSetList.add(selectable);
            }
            if (logger.isTraceEnabled()) {
                logger.trace("INIT SET: Added " + infoSkillSetList.size()
                        + " of " + setList.size() + " found"
                        + " skill set in skill model " + selectedKCM.getId()
                        + " and selected set is " + selectedId);
            }

            if (numSelected == 0 && infoSkillSetList.size() > 0) {
                if (logger.isTraceEnabled()) {
                    logger.trace("INIT SET: :No skill sets are selected.");
                }
            } else if (numSelected > 1) {
                logger.warn("INIT SET: More than one skill set is selected");
            }
        }
        if (logger.isTraceEnabled()) {
            logger.trace("INIT SET: initializeSkillSetList: done : returning " + setSelectedFlag);
        }
        return setSelectedFlag;
    }

    /**
     * Sorts the primary and secondary KCM lists
     * AND change the selected KCM if user has not made a selection yet.
     * @param info the dataset context
     * @param kcmSortBy the property of the KC Model to sort by
     * @param kcmSortAscendingFlag indicates whether to sort ascending or not
     */
    public void sortKcModels(DatasetContext info,
                             String kcmSortBy,
                             Boolean kcmSortAscendingFlag) {
        info.getNavContext().sortKcModels(kcmSortBy, kcmSortAscendingFlag);

        Boolean userSelected = info.getNavContext().getUserSelectedKCM();
        if (!userSelected) {
            List<SelectableItem> skillModelList = info.getNavContext().getSkillModelList();
            if (skillModelList.size() > 0) {
                SkillModelItem modelItem = (SkillModelItem)(skillModelList.get(0).getItem());
                int selModel = ((Long)modelItem.getId()).intValue();
                selectSkillModel(selModel, info);
            }
        }
    }

    /**
     * Converts this item to a String. This is used for creating unique IDs for learning curves.
     * @return String - the String representation of this item.
     * @param info information stored in the HTTP session via DatasetContext class.
     */
    public String toString(DatasetContext info) {
        StringBuffer value = new StringBuffer();
        value.append(info.getDataset().hashCode());
        value.append(info.getLearningCurveContext().getMaxOpportunityNumber());
        value.append(getSelectedSamples(info));
        value.append(getSelectedSkills(info));
        value.append(getSelectedStudents(info));
        value.append(getSelectedProblem(info));
        value.append(getSelectedSkillModel(info));
        value.append(getSelectedSkillSetItem(info));
        return value.toString();
    }

    /**
     * Method to determine if the specified dataset is remote.
     * @param dataset the DatasetItem
     * @return flag indicating if remote
     */
    public boolean isDatasetRemote(DatasetItem dataset) {
        DatasetInstanceMapDao dao = DaoFactory.DEFAULT.getDatasetInstanceMapDao();
        return dao.isDatasetRemote(dataset);
    }

    /**
     * Method to determine if highStakes data is present in the dataset.
     * Typically, only true for OLI datasets.
     * @return Boolean indicating if high-stakes error rate is present
     */
    public Boolean getHighStakesDataPresent(DatasetItem dataset) {
	CustomFieldDao cfDao = DaoFactory.DEFAULT.getCustomFieldDao();
	List<CustomFieldItem> cfList =
            cfDao.findMatchingByName("highStakes", dataset, true);
        return (cfList.size() > 0);
    }

    /**
     * Method to determine if the specified dataset is remote.
     * @param dataset the DatasetItem
     * @return flag indicating if remote
     */
    private boolean isDatasetRemote(Integer datasetId) {
        DatasetItem dataset = DaoFactory.DEFAULT.getDatasetDao().find(datasetId);
        if (dataset == null) { return false; }

        return isDatasetRemote(dataset);
    }

} // end class NavigationHelper
