/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.servlet.kcmodel.KCModelContext;

/**
 * Servlet to handle generic navigation changes all in one place.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 8426 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2012-12-14 14:26:17 -0500 (Fri, 14 Dec 2012) $
 * <!-- $KeyWordsOff: $ -->
 */
public class NavigationServlet extends AbstractServlet {


    /** logger for this class. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Handles the HTTP get.
     * @param req HttpServletRequest.
     * @param resp HttpServletResponse.
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
            KCModelContext kcModelContext = datasetContext.getKCModelContext();

            String getSkillModelList = req.getParameter("getSkillModelList");
            if (getSkillModelList != null) {

                JSONObject json = new JSONObject();
                json.put("kcmSortBy", kcModelContext.getSortBy());
                if (kcModelContext.getSortByAscendingFlag()) {
                    json.put("kcmSortByAsc", "ascending");
                } else {
                    json.put("kcmSortByAsc", "descending");
                }

                String kcmList;
                if (getSkillModelList.compareTo("primary") == 0) {
                    kcmList = getSkillModelList(datasetContext, false);
                } else if (getSkillModelList.compareTo("secondary") == 0) {
                    kcmList = getSkillModelList(datasetContext, true);
                } else {
                    logger.warn("Unknown skill model list param :: " + getSkillModelList);
                    kcmList = "new Array()";
                }
                json.put("kcmList", kcmList);

                out = writeJson(resp, json);
                return;
            }
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
     * Writes a JSON object back.
     * @param resp the HttpServletResponse
     * @param json the JSONObject
     * @return the PrintWriter so it can be closed properly
     * @throws IOException an IO exception in case something bad happens
     */
    private PrintWriter writeJson(HttpServletResponse resp, JSONObject json)
            throws IOException {
        PrintWriter out;
        resp.setContentType("application/json");
        out = resp.getWriter();
        out.write(json.toString());
        out.flush();
        return out;
    }

    /**
     * Creates a java script array of a skill models which contains the ID, isSelected
     * flag and the skill model name.
     * @param datasetContext the http session info
     * @param getSecondary the getSecondary flag
     * @return a java script string
     */
    private String getSkillModelList(DatasetContext datasetContext, boolean getSecondary) {
        StringBuffer arrayString = new StringBuffer();

        arrayString.append("new Array(");
        List <SelectableItem> skillModelList = datasetContext.getNavContext().getSkillModelList();
        SkillModelItem selectedPrimary = null;

        synchronized (skillModelList) {
            for (Iterator sIter = skillModelList.iterator(); sIter.hasNext();) {
                SelectableItem selectableItem = (SelectableItem)sIter.next();
                SkillModelItem item = (SkillModelItem)selectableItem.getItem();
                StringBuffer singleItem = new StringBuffer();
                if (selectableItem.isSelected()) {
                    selectedPrimary = item;
                }
                singleItem.append(" new Array('" + item.getId()
                        + "', " + selectableItem.isSelected()
                        + ", '" + item.getSkillModelName().replaceAll("\'", "\\\\'")
                        + "')");

                arrayString.append(singleItem);
                if (sIter.hasNext()) {
                    arrayString.append(",");
                }
            } // end for loop
        }
        arrayString.append(" )");

        if (getSecondary) {
            arrayString = new StringBuffer();
            List <SelectableItem> secondarySkillModelList =
                datasetContext.getNavContext().getSecondarySkillModelList();
            synchronized (secondarySkillModelList) {
                for (Iterator sIter = secondarySkillModelList.iterator(); sIter.hasNext();) {
                    SelectableItem selectableItem = (SelectableItem)sIter.next();
                    SkillModelItem item = (SkillModelItem)selectableItem.getItem();
                    StringBuffer singleItem = new StringBuffer();
                    if (item.equals(selectedPrimary)) {
                        continue;
                    }
                    if (arrayString.length() != 0) {
                        arrayString.append(",");
                    }
                    singleItem.append(" new Array('" + item.getId()
                            + "', " + selectableItem.isSelected()
                            + ", '" + item.getSkillModelName().replaceAll("\'", "\\\\'")
                            + "')");

                    arrayString.append(singleItem);
                } // end for loop
            }
            arrayString.insert(0, "new Array(");
            arrayString.append(" )");
        }
        return arrayString.toString();
    }
}
