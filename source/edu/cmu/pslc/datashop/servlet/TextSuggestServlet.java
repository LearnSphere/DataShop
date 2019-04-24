/*
* Carnegie Mellon University, Human Computer Interaction Institute
* Copyright 2005
* All Rights Reserved
*/
package edu.cmu.pslc.datashop.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.CfTxLevelDao;
import edu.cmu.pslc.datashop.dao.ConditionDao;
import edu.cmu.pslc.datashop.dao.CustomFieldDao;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetLevelDao;
import edu.cmu.pslc.datashop.dao.ProblemDao;
import edu.cmu.pslc.datashop.dao.SchoolDao;
import edu.cmu.pslc.datashop.dao.StudentDao;
import edu.cmu.pslc.datashop.dao.TransactionDao;
import edu.cmu.pslc.datashop.dao.hibernate.SampleMapper;
import edu.cmu.pslc.datashop.item.CfTxLevelItem;
import edu.cmu.pslc.datashop.item.ConditionItem;
import edu.cmu.pslc.datashop.item.CustomFieldItem;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.DatasetLevelItem;
import edu.cmu.pslc.datashop.item.ProblemItem;
import edu.cmu.pslc.datashop.item.SchoolItem;
import edu.cmu.pslc.datashop.item.StudentItem;


/**
 * This servlet handles all TextSuggest AJAX requests.  It takes a number of parameters
 * and returns a java script array string as plain text.
 *
 * @author Benjamin Billings
 * @version $Revision: 12840 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-12-18 12:12:17 -0500 (Fri, 18 Dec 2015) $
 * <!-- $KeyWordsOff: $ -->
 */

public class TextSuggestServlet extends AbstractServlet {

    /** logger for this class. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Handles the HTTP get.
     * @param req HttpServletRequest.
     * @param resp HttpServletResponse.
     * @see javax.servlet.http.HttpServlet
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        //no difference, so just forward the request and response to the post.
        doPost(req, resp);
    }

    /**
     * Handles the HTTP post.
     * @param req HttpServletRequest.
     * @param resp HttpServletResponse.
     * @see javax.servlet.http.HttpServlet
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        logDebug("doPost begin :: ", getDebugParamsString(req));
        PrintWriter out = null;
        try {
            setEncoding(req, resp);

            DatasetContext datasetContext = getDatasetContext(req);

            String suggestField = req.getParameter("field"); // param
            if (suggestField == null || suggestField.compareTo("") == 0) {
                logger.debug("TextSuggestServlet.doPost no field found");
            }

            String queryString = req.getParameter("query"); // param
            if (queryString == null) {
                logger.debug("TextSuggestServlet.doPost no queryString found getting all.");
                queryString = "";
            }

            String countMaxStr = req.getParameter("count"); // param
            Integer countMax;
            if (countMaxStr == null || countMaxStr.compareTo("") == 0) {
                logger.debug("TextSuggestServlet.doPost no count found");
                countMax = new Integer(-1);
            } else {
                try {
                    countMax = new Integer(countMaxStr);
                } catch (NumberFormatException exception) {
                    logger.error("Error parsing countMaxString, setting to default of -1 :: "
                            + countMaxStr + " :: " + exception.getMessage());
                    countMax = new Integer(-1);
                }
            }

            String matchAnyStr = req.getParameter("match_anywhere"); // param
            Boolean matchAny;
            if (matchAnyStr == null || matchAnyStr.compareTo("") == 0) {
                logger.debug("TextSuggestServlet.doPost no matchAny found");
                matchAny = new Boolean(true);
            } else {
                matchAny = new Boolean(matchAnyStr);
            }

            String ignoreCaseStr = req.getParameter("ignore_case"); // param
            Boolean ignoreCase;
            if (ignoreCaseStr == null || ignoreCaseStr.compareTo("") == 0) {
                logger.debug("TextSuggestServlet.doPost no ignoreCase found");
                ignoreCase = new Boolean(true);
            } else {
                ignoreCase = new Boolean(ignoreCaseStr);
            }

            List results = getSuggestions(suggestField, queryString, countMax,
                    matchAny, ignoreCase, datasetContext.getDataset());

            String ajaxString = getAjaxString(results);


            if (ajaxString == null) {
                logger.debug("Ajax string unexpectedly null, setting to default.");
                ajaxString = "new Array()";
            }

            //an AJAX request was made by the sample selector.
            if (logger.isDebugEnabled()) {
                logger.debug("Ajax Suggestion Update Sending :" + ajaxString);
            }
            resp.setContentType("text/html");
            out = resp.getWriter();
            out.write(ajaxString);
            out.flush();

        } catch (Exception exception) {
            forwardError(req, resp, logger, exception);

        } finally {
            if (out != null) {
                out.close();
            }
            logger.debug("doPost end");
        }
    }

    /**
     * Builds the string expected by the java script listener function.  In this case it expects
     * a java script array of the form "new Array('itemOne', 'itemTwo')".
     * @param results the list of strings resulting form the query.
     * @return String of a java script array for the java script listener.
     */
    private String getAjaxString(List <String> results) {
        StringBuffer buffer = new StringBuffer();

        buffer.append("<ul>");
        for (String result : results) { buffer.append("<li>" + result + "</li>"); }
        buffer.append("</ul>");

        return buffer.toString();
    }


    /**
     * Gets the list of suggestions given the parameters.
     * @param field The field to get suggestions for.
     * @param query The query of the suggestion
     * @param count the max number of results to return (-1 or 0 will return all results)
     * @param matchAny whether to match anywhere in the string or just the start.
     * @param ignoreCase whether to ignore case.
     * @param dataset the dataset we are getting suggestions for.
     * @return  A list of suggestions strings.
     */
    private List <String> getSuggestions(String field, String query,
            Integer count, Boolean matchAny, Boolean ignoreCase, DatasetItem dataset) {

        List <String> stringList = new ArrayList <String> ();

        if (field == null) { return stringList; }

        if (field.equals(SampleMapper.PROBLEM_NAME.get(SampleMapper.DB))) {
            stringList.addAll(getProblemNameList(query, count, matchAny, ignoreCase, dataset));
        } else if (field.equals(SampleMapper.PROBLEM_DESCRIPTION.get(SampleMapper.DB))) {
            stringList.addAll(getProblemDescriptionList(
                    query, count, matchAny, ignoreCase, dataset));
        } else if (field.equals(SampleMapper.DATASET_LEVEL_NAME.get(SampleMapper.DB))) {
            stringList.addAll(getLevelNameList(query, count, matchAny, ignoreCase, dataset));
        } else if (field.equals(SampleMapper.DATASET_LEVEL_TITLE.get(SampleMapper.DB))) {
            stringList.addAll(getLevelTitleList(query, count, matchAny, ignoreCase, dataset));
        } else if (field.equals(SampleMapper.CUSTOM_FIELD_NAME.get(SampleMapper.DB))) {
            stringList.addAll(getCustomFieldNameList(query, count, matchAny, ignoreCase, dataset));
        } else if (field.equals(SampleMapper.CUSTOM_FIELD_VALUE.get(SampleMapper.DB))) {
            stringList.addAll(getCustomFieldValueList(query, count, matchAny, ignoreCase, dataset));
        } else if (field.equals(SampleMapper.CONDITION_NAME.get(SampleMapper.DB))) {
            stringList.addAll(getConditionNameList(query, count, matchAny, ignoreCase, dataset));
        } else if (field.equals(SampleMapper.CONDITION_TYPE.get(SampleMapper.DB))) {
            stringList.addAll(getConditionTypeList(query, count, matchAny, ignoreCase, dataset));
        } else if (field.equals(SampleMapper.STUDENT_NAME.get(SampleMapper.DB))) {
            stringList.addAll(getStudentAnonIdList(query, count, matchAny, ignoreCase, dataset));
        } else if (field.equals(SampleMapper.SCHOOL_NAME.get(SampleMapper.DB))) {
            stringList.addAll(getSchoolNameList(query, count, matchAny, ignoreCase, dataset));
        } else if (field.equals("transactionTypeTutor")
                || field.equals("transactionTypeTool")
                || field.equals("transactionSubtypeTool")
                || field.equals("transactionSubtypeTutor")) {
            stringList.addAll(
                    getTransactionType(field, query, count, matchAny, ignoreCase, dataset));
        }

        Collections.sort(stringList);
        return stringList;
    }

    /**
     * Shortcut method to get the Problem DAO.
     * @return the problem DAO
     */
    private ProblemDao problemDao() {
        return DaoFactory.DEFAULT.getProblemDao();
    }

    /**
     * Gets the list of suggestions for a problem name.
     * @param query The query of the suggestion
     * @param count the max number of results to return (-1 or 0 will return all results)
     * @param matchAny whether to match anywhere in the string or just the start.
     * @param ignoreCase whether to ignore case.
     * @param dataset the Dataset to restrict the search too.
     * @return  A list of suggestions strings.
     */
    private Set<String> getProblemNameList(final String query, Integer count,
            final Boolean matchAny, Boolean ignoreCase, final DatasetItem dataset) {
        return new HashSet<String>() { {
            for (ProblemItem problem : problemDao().
                    findMatchingByName(query, dataset, matchAny)) {
                add(problem.getProblemName());
            }
        } };
    }

    /**
     * Gets the list of suggestions for a problem name.
     * @param query The query of the suggestion
     * @param count the max number of results to return (-1 or 0 will return all results)
     * @param matchAny whether to match anywhere in the string or just the start.
     * @param ignoreCase whether to ignore case.
     * @param dataset the Dataset to restrict the search too.
     * @return  A list of suggestions strings.
     */
    private Set getProblemDescriptionList(final String query, Integer count,
            final Boolean matchAny, Boolean ignoreCase, final DatasetItem dataset) {
        return new HashSet<String>() { {
            for (ProblemItem problem : problemDao().
                    findMatchingByDescription(query, dataset, matchAny)) {
                add(problem.getProblemDescription());
            }
        } };
    }

    /**
     * Gets the list of suggestions for a level name.
     * @param query The query of the suggestion
     * @param count the max number of results to return (-1 or 0 will return all results)
     * @param matchAny whether to match anywhere in the string or just the start.
     * @param ignoreCase whether to ignore case.
     * @param dataset the Dataset to restrict the search too.
     * @return  A list of suggestions strings.
     */
    private Set getLevelNameList(String query, Integer count,
            Boolean matchAny, Boolean ignoreCase, DatasetItem dataset) {
        Set stringSet = new HashSet();

        DatasetLevelDao dao = DaoFactory.DEFAULT.getDatasetLevelDao();
        List itemList = dao.findMatchingByName(query, dataset, matchAny.booleanValue());
        for (Iterator it = itemList.iterator(); it.hasNext();) {
            DatasetLevelItem item = (DatasetLevelItem)it.next();
            stringSet.add(item.getLevelName());
        }

        return stringSet;
    }

    /**
     * Gets the list of suggestions for a level title.
     * @param query The query of the suggestion
     * @param count the max number of results to return (-1 or 0 will return all results)
     * @param matchAny whether to match anywhere in the string or just the start.
     * @param ignoreCase whether to ignore case.
     * @param dataset the Dataset to restrict the search too.
     * @return  A list of suggestions strings.
     */
    private Set getLevelTitleList(String query, Integer count,
            Boolean matchAny, Boolean ignoreCase, DatasetItem dataset) {
        Set stringSet = new HashSet();

        DatasetLevelDao dao = DaoFactory.DEFAULT.getDatasetLevelDao();
        List itemList = dao.findMatchingByTitle(query, dataset, matchAny.booleanValue());
        for (Iterator it = itemList.iterator(); it.hasNext();) {
            DatasetLevelItem item = (DatasetLevelItem)it.next();
            stringSet.add(item.getLevelTitle());
        }

        return stringSet;
    }

    /**
     * Gets the list of suggestions for the anonymous student Id.
     * @param query The query of the suggestion
     * @param count the max number of results to return (-1 or 0 will return all results)
     * @param matchAny whether to match anywhere in the string or just the start.
     * @param ignoreCase whether to ignore case.
     * @param dataset the Dataset to restrict the search too.
     * @return  A list of suggestions strings.
     */
    private Set getStudentAnonIdList(String query, Integer count,
            Boolean matchAny, Boolean ignoreCase, DatasetItem dataset) {
        Set stringSet = new HashSet();

        StudentDao dao = DaoFactory.DEFAULT.getStudentDao();
        List itemList = dao.findMatchingByAnonId(query, dataset, matchAny.booleanValue());
        for (Iterator it = itemList.iterator(); it.hasNext();) {
            StudentItem item = (StudentItem)it.next();
            stringSet.add(item.getAnonymousUserId());
        }

        return stringSet;
    }

    /**
     * Gets the list of suggestions for school name.
     * @param query The query of the suggestion
     * @param count the max number of results to return (-1 or 0 will return all results)
     * @param matchAny whether to match anywhere in the string or just the start.
     * @param ignoreCase whether to ignore case.
     * @param dataset the Dataset to restrict the search too.
     * @return  A list of suggestions strings.
     */
    private Set getSchoolNameList(String query, Integer count,
            Boolean matchAny, Boolean ignoreCase, DatasetItem dataset) {
        Set stringSet = new HashSet();

        SchoolDao dao = DaoFactory.DEFAULT.getSchoolDao();
        List itemList = dao.findMatchingByName(query, dataset, matchAny.booleanValue());
        for (Iterator it = itemList.iterator(); it.hasNext();) {
            SchoolItem item = (SchoolItem)it.next();
            stringSet.add(item.getSchoolName());
        }
        return stringSet;
    }

    /**
     * Gets the list of suggestions for condition type.
     * @param query The query of the suggestion
     * @param count the max number of results to return (-1 or 0 will return all results)
     * @param matchAny whether to match anywhere in the string or just the start.
     * @param ignoreCase whether to ignore case.
     * @param dataset the Dataset to restrict the search too.
     * @return  A list of suggestions strings.
     */
    private Set getConditionTypeList(String query, Integer count,
            Boolean matchAny, Boolean ignoreCase, DatasetItem dataset) {
        Set stringSet = new HashSet();

        ConditionDao dao = DaoFactory.DEFAULT.getConditionDao();
        List itemList = dao.findMatchingByType(query, dataset, matchAny.booleanValue());
        for (Iterator it = itemList.iterator(); it.hasNext();) {
            ConditionItem item = (ConditionItem)it.next();
            stringSet.add(item.getType());
        }
        return stringSet;
    }

    /**
     * Gets the list of suggestions for condition name.
     * @param query The query of the suggestion
     * @param count the max number of results to return (-1 or 0 will return all results)
     * @param matchAny whether to match anywhere in the string or just the start.
     * @param ignoreCase whether to ignore case.
     * @param dataset the Dataset to restrict the search too.
     * @return  A list of suggestions strings.
     */
    private Set getConditionNameList(String query, Integer count,
            Boolean matchAny, Boolean ignoreCase, DatasetItem dataset) {
        Set stringSet = new HashSet();

        ConditionDao dao = DaoFactory.DEFAULT.getConditionDao();
        List itemList = dao.findMatchingByName(query, dataset, matchAny.booleanValue());
        for (Iterator it = itemList.iterator(); it.hasNext();) {
            ConditionItem item = (ConditionItem)it.next();
            stringSet.add(item.getConditionName());
        }
        return stringSet;
    }

    /**
     * Gets the list of suggestions for tool type.
     * @param typeField the specific type field to get a distinct list for.
     * @param query The query of the suggestion
     * @param count the max number of results to return (-1 or 0 will return all results)
     * @param matchAny whether to match anywhere in the string or just the start.
     * @param ignoreCase whether to ignore case.
     * @param dataset the Dataset to restrict the search too.
     * @return  A list of suggestions strings.
     */
    private List getTransactionType(String typeField, String query, Integer count,
            Boolean matchAny, Boolean ignoreCase, DatasetItem dataset) {
        TransactionDao dao = DaoFactory.DEFAULT.getTransactionDao();
        List itemList = dao.findMatchingTypes(typeField, query, dataset, matchAny.booleanValue());
        return itemList;
    }

    /**
     * Gets the list of suggestions for a custom field name.
     * @param query The query of the suggestion
     * @param count the max number of results to return (-1 or 0 will return all results)
     * @param matchAny whether to match anywhere in the string or just the start.
     * @param ignoreCase whether to ignore case.
     * @param dataset the Dataset to restrict the search too.
     * @return  A list of suggestions strings.
     */
    private Set getCustomFieldNameList(String query, Integer count, Boolean matchAny,
                                       Boolean ignoreCase, DatasetItem dataset) {
        Set stringSet = new HashSet();

        CustomFieldDao dao = DaoFactory.DEFAULT.getCustomFieldDao();
        List itemList = dao.findMatchingByName(query, dataset, matchAny.booleanValue());
        for (Iterator it = itemList.iterator(); it.hasNext();) {
            CustomFieldItem item = (CustomFieldItem)it.next();
            stringSet.add(item.getCustomFieldName());
        }

        return stringSet;
    }

    /**
     * Gets the list of suggestions for a custom field value.
     * @param query The query of the suggestion
     * @param count the max number of results to return (-1 or 0 will return all results)
     * @param matchAny whether to match anywhere in the string or just the start.
     * @param ignoreCase whether to ignore case.
     * @param dataset the Dataset to restrict the search too.
     * @return  A list of suggestions strings.
     */
    private Set getCustomFieldValueList(String query, Integer count, Boolean matchAny,
                                        Boolean ignoreCase, DatasetItem dataset) {
        Set stringSet = new HashSet();

        CfTxLevelDao dao = DaoFactory.DEFAULT.getCfTxLevelDao();
        List<String> itemList = dao.findMatchingByValue(query, dataset, matchAny.booleanValue());
        for (Iterator<String> it = itemList.iterator(); it.hasNext();) {
            stringSet.add(it.next());
        }

        return stringSet;
    }
}
