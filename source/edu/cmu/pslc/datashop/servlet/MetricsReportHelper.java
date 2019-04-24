/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2010
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


import org.apache.log4j.Logger;
import org.apache.commons.lang.time.FastDateFormat;

import edu.cmu.pslc.datashop.dto.LearnlabDomainMetricsReport;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DomainDao;
import edu.cmu.pslc.datashop.dao.LearnlabDao;
import edu.cmu.pslc.datashop.dao.MetricByDomainDao;
import edu.cmu.pslc.datashop.dao.MetricByDomainReportDao;
import edu.cmu.pslc.datashop.dao.MetricByLearnlabDao;
import edu.cmu.pslc.datashop.dao.MetricByLearnlabReportDao;
import edu.cmu.pslc.datashop.dao.MetricReportDao;
import edu.cmu.pslc.datashop.dao.RemoteInstanceDao;
import edu.cmu.pslc.datashop.item.DomainItem;
import edu.cmu.pslc.datashop.item.LearnlabItem;
import edu.cmu.pslc.datashop.item.MetricByDomainItem;
import edu.cmu.pslc.datashop.item.MetricByDomainReportItem;
import edu.cmu.pslc.datashop.item.MetricByLearnlabItem;
import edu.cmu.pslc.datashop.item.MetricByLearnlabReportItem;
import edu.cmu.pslc.datashop.item.MetricReportItem;
import edu.cmu.pslc.datashop.item.RemoteInstanceItem;

/**
 * Static class that prepares for metrics report.
 * @author Shanwen Yu
 * @version $Revision: 13459 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-08-26 12:05:41 -0400 (Fri, 26 Aug 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class MetricsReportHelper {

    /** Debug logging. */
    private static Logger logger = Logger.getLogger(MetricsReportHelper.class);
    /** Formatter to format time string with second granularity with no time zone. */
    private static FastDateFormat dateFormatter =
        FastDateFormat.getInstance(" HH:mm:ss, EEEE, MMMM dd, yyyy");
    /** Constant for name of master (CMU) instance. */
    private static String CMU_INSTANCE_NAME = "CMU";

    /** MetricByDomain Item dao */
    private MetricByDomainDao metricByDomainDao;
    /** Domain Item DAO */
    private DomainDao domainDao;
    /** MetricByLearnlab Item dao */
    private MetricByLearnlabDao metricByLearnlabDao;
    /** Learnlab Item DAO */
    private LearnlabDao learnlabDao;
    /** Metric Report Item DAO */
    private MetricReportDao metricReportDao;
    /** LearnlabDomainMetricsReport DTO */
    private LearnlabDomainMetricsReport report;
    /** List of metric by domain report values*/
    private List domainReportValues;
    /** List of metric by learnlab report values*/
    private List learnlabReportValues;
    /** Domain Item*/
    private DomainItem domain;
    /** Learnlab Item*/
    private LearnlabItem learnlab;
    /** Total Number of Domain Files*/
    private Integer totalDomainFiles;
    /** Total Number of Domain Papers*/
    private Integer totalDomainPapers;
    /** Total Number of Domain Datasets*/
    private Integer totalDomainDatasets;
    /** Total Number of Domain Students*/
    private Integer totalDomainStudents;
    /** Total Number of Domain Actions*/
    private Integer totalDomainActions;
    /** Total Number of Domain Hours*/
    private Double totalDomainHours;
    /** Total Number of Learnlab Files*/
    private Integer totalLearnlabFiles;
    /** Total Number of Learnlab Papers*/
    private Integer totalLearnlabPapers;
    /** Total Number of Learnlab Datasets*/
    private Integer totalLearnlabDatasets;
    /** Total Number of Learnlab Students*/
    private Integer totalLearnlabStudents;
    /** Total Number of Learnlab Actions*/
    private Integer totalLearnlabActions;
    /** Total Number of Learnlab Hours*/
    private Double totalLearnlabHours;

    /**
     * Returns the debug logger.
     * @return logger - an instance of the logger for this class
     */
    public Logger getLogger() { return logger; }

    /** Default constructor. */
    public MetricsReportHelper() {
        logger.info("MetricsReportHelper.constructor");
    }

    /**
     * This function returns a DTO report to be used in jsp page.
     * @return String containing all HTML.
     */
    public LearnlabDomainMetricsReport getReport() {
        if (logger.isDebugEnabled()) {
            logger.debug("getReport ");
        }
        report = new LearnlabDomainMetricsReport();

        report.setFormattedTime(getFormattedReportTime(getReportTime()));

        metricByLearnlabDao = DaoFactory.DEFAULT.getMetricByLearnlabDao();
        learnlabDao = DaoFactory.DEFAULT.getLearnlabDao();
        List<LearnlabItem> learnlabList = learnlabDao.getAll();

        totalLearnlabFiles = 0;
        totalLearnlabPapers = 0;
        totalLearnlabDatasets = 0;
        totalLearnlabStudents = 0;
        totalLearnlabActions = 0;
        totalLearnlabHours = 0.00;

        learnlabReportValues = new ArrayList();

        Integer otherLearnlabId = null;
        Integer unspecifiedLearnlabId = null;
        for (Iterator it = learnlabList.iterator(); it.hasNext();) {
            learnlab = (LearnlabItem)learnlabDao.get((Integer)((LearnlabItem) it.next()).getId());

            if (learnlab == null) { continue; }
            
            // put 'Other' and 'Unspecified' at the bottom of the list
            if (learnlab.getName().equals("Other")) {
                otherLearnlabId = (Integer)learnlab.getId();
            } else if (learnlab.getName().equals("Unspecified")) {
                unspecifiedLearnlabId = (Integer)learnlab.getId();
            } else {
                setLearnlabReportValues(learnlab);
            }
        }
        if (otherLearnlabId != null) {
            learnlab = learnlabDao.get(otherLearnlabId);
            if (learnlab != null) {
                setLearnlabReportValues(learnlab);
            }
        }

        if (unspecifiedLearnlabId != null) {
            learnlab = learnlabDao.get(unspecifiedLearnlabId);
            if (learnlab != null) {
                setLearnlabReportValues(learnlab);
            }
        }

        report.setTotalLearnlabFiles(formatInteger(totalLearnlabFiles));
        report.setTotalLearnlabPapers(formatInteger(totalLearnlabPapers));
        report.setTotalLearnlabDatasets(formatInteger(totalLearnlabDatasets));
        report.setTotalLearnlabStudents(formatInteger(totalLearnlabStudents));
        report.setTotalLearnlabActions(formatInteger(totalLearnlabActions));
        report.setTotalLearnlabHours(formatDouble(totalLearnlabHours));
        report.setMap("Learnlab", learnlabReportValues);

        totalDomainFiles = 0;
        totalDomainPapers = 0;
        totalDomainDatasets = 0;
        totalDomainStudents = 0;
        totalDomainActions = 0;
        totalDomainHours = 0.00;

        metricByDomainDao = DaoFactory.DEFAULT.getMetricByDomainDao();
        domainDao = DaoFactory.DEFAULT.getDomainDao();
        List<DomainItem> domainList = domainDao.getAll();

        domainReportValues = new ArrayList();

        Integer otherDomainId = null;
        Integer unspecifiedDomainId = null;
        for (Iterator it = domainList.iterator(); it.hasNext();) {
            domain = (DomainItem)domainDao.get((Integer)((DomainItem) it.next()).getId());

            if (domain == null) { continue; }
            
            // put 'Other' and 'Unspecified' at the bottom of the list
            if (domain.getName().equals("Other")) {
                otherDomainId = (Integer)domain.getId();
            } else if (domain.getName().equals("Unspecified")) {
                unspecifiedDomainId = (Integer)domain.getId();
            } else {
                setDomainReportValues(domain);
            }
        }
        if (otherDomainId != null) {
            domain = domainDao.get(otherDomainId);
            if (domain != null) {
                setDomainReportValues(domain);
            }
        }

        if (unspecifiedDomainId != null) {
            domain = domainDao.get(unspecifiedDomainId);
            if (domain != null) {
                setDomainReportValues(domain);
            }
        }

        report.setTotalDomainFiles(formatInteger(totalDomainFiles));
        report.setTotalDomainPapers(formatInteger(totalDomainPapers));
        report.setTotalDomainDatasets(formatInteger(totalDomainDatasets));
        report.setTotalDomainStudents(formatInteger(totalDomainStudents));
        report.setTotalDomainActions(formatInteger(totalDomainActions));
        report.setTotalDomainHours(formatDouble(totalDomainHours));
        report.setMap("Domain", domainReportValues);

        // Generate report for each remote instance.
        Map<String, LearnlabDomainMetricsReport> remoteReportsMap =
            new LinkedHashMap<String, LearnlabDomainMetricsReport>();
        RemoteInstanceDao riDao = DaoFactory.DEFAULT.getRemoteInstanceDao();
        List<RemoteInstanceItem> remoteInstances = riDao.findAll();

        // But first, add an entry for the master (CMU)... iff this is the master.
        if (remoteInstances.size() > 0) {
            LearnlabDomainMetricsReport cmuReport = getRemoteReport(null);
            remoteReportsMap.put(CMU_INSTANCE_NAME, cmuReport);
        }

        // Now the remote instances.
        for (Iterator<RemoteInstanceItem> iter = remoteInstances.iterator(); iter.hasNext(); ) {
            RemoteInstanceItem item = iter.next();
            LearnlabDomainMetricsReport remoteReport = getRemoteReport(item);
            if (remoteReport != null) {
                remoteReportsMap.put(generateItemLink(item), remoteReport);
            }
        }
        report.setRemoteInstanceReports(remoteReportsMap);

        return report;
    } // end method getReport

    /**
     * Set report values given a domain.
     * @param domain DomainItem that the values generated from.
     */
    public void setDomainReportValues(DomainItem domain) {

        Integer numFiles = 0;
        Integer numPapers = 0;
        Integer numDatasets = 0;
        Integer numStudents = 0;
        Integer numActions = 0;
        Double numHours = 0.0;

        // Get list of MetricByDomainReportItems for this domain, across remote
        // instances, and add to summary.
        MetricByDomainReportDao mbdrDao = DaoFactory.DEFAULT.getMetricByDomainReportDao();
        List<MetricByDomainReportItem> itemList = mbdrDao.findLatestRemoteByDomain(domain);
        for (Iterator<MetricByDomainReportItem> iter = itemList.iterator(); iter.hasNext(); ) {
            MetricByDomainReportItem item = iter.next();
            numFiles += item.getFiles();
            numPapers += item.getPapers();
            numDatasets += item.getDatasets();
            numStudents += item.getStudents();
            numActions += item.getActions();
            numHours += item.getHours();
        }

        MetricByDomainItem metricByDomain =
                (MetricByDomainItem)metricByDomainDao.findByDomain(domain);
        List singleDomainReportValues = new ArrayList();
        singleDomainReportValues.add(getNameHtml(domain.getName(), false));
        if (metricByDomain != null) {

            numFiles += metricByDomain.getFiles();
            numPapers += metricByDomain.getPapers();
            numDatasets += metricByDomain.getDatasets();
            numActions += metricByDomain.getActions();
            numStudents += metricByDomain.getStudents();
            numHours += metricByDomain.getHours();
        }

        singleDomainReportValues.add(formatInteger(numFiles));
        singleDomainReportValues.add(formatInteger(numPapers));
        singleDomainReportValues.add(formatInteger(numDatasets));
        singleDomainReportValues.add(formatInteger(numActions));
        singleDomainReportValues.add(formatInteger(numStudents));
        singleDomainReportValues.add(formatDouble(numHours));

        totalDomainFiles += numFiles;
        totalDomainPapers += numPapers;
        totalDomainDatasets += numDatasets;
        totalDomainStudents += numStudents;
        totalDomainActions += numActions;
        totalDomainHours += numHours;

        domainReportValues.add(singleDomainReportValues);
    }

    /**
     * Set report values given a learnlab.
     * @param learnlab LearnlabItem that the values generated from.
     */
    public void setLearnlabReportValues(LearnlabItem learnlab) {

        Integer numFiles = 0;
        Integer numPapers = 0;
        Integer numDatasets = 0;
        Integer numStudents = 0;
        Integer numActions = 0;
        Double numHours = 0.0;

        // Get list of MetricByLearnlabReportItems for this learnlab, across remote
        // instances, and add to summary.
        MetricByLearnlabReportDao mblrDao = DaoFactory.DEFAULT.getMetricByLearnlabReportDao();
        List<MetricByLearnlabReportItem> itemList = mblrDao.findLatestRemoteByLearnlab(learnlab);
        for (Iterator<MetricByLearnlabReportItem> iter = itemList.iterator(); iter.hasNext(); ) {
            MetricByLearnlabReportItem item = iter.next();
            numFiles += item.getFiles();
            numPapers += item.getPapers();
            numDatasets += item.getDatasets();
            numStudents += item.getStudents();
            numActions += item.getActions();
            numHours += item.getHours();
        }

        MetricByLearnlabItem metricByLearnlab =
            (MetricByLearnlabItem)metricByLearnlabDao.findByLearnlab(learnlab);
        List singleLearnlabReportValues = new ArrayList();
        singleLearnlabReportValues.add(getNameHtml(learnlab.getName(), true));
        if (metricByLearnlab != null) {

            numFiles += metricByLearnlab.getFiles();
            numPapers += metricByLearnlab.getPapers();
            numDatasets += metricByLearnlab.getDatasets();
            numActions += metricByLearnlab.getActions();
            numStudents += metricByLearnlab.getStudents();
            numHours += metricByLearnlab.getHours();
        }

        singleLearnlabReportValues.add(formatInteger(numFiles));
        singleLearnlabReportValues.add(formatInteger(numPapers));
        singleLearnlabReportValues.add(formatInteger(numDatasets));
        singleLearnlabReportValues.add(formatInteger(numActions));
        singleLearnlabReportValues.add(formatInteger(numStudents));
        singleLearnlabReportValues.add(formatDouble(numHours));

        totalLearnlabFiles += numFiles;
        totalLearnlabPapers += numPapers;
        totalLearnlabDatasets += numDatasets;
        totalLearnlabStudents += numStudents;
        totalLearnlabActions += numActions;
        totalLearnlabHours += numHours;

        learnlabReportValues.add(singleLearnlabReportValues);
    }

    /**
     * Get most recent report time.
     * @return reportDate most recent report time
     */
    public Date getReportTime() {
        Date reportDate = new Date();
        metricReportDao = DaoFactory.DEFAULT.getMetricReportDao();
        reportDate = metricReportDao.getMostRecentTime();
        return reportDate;
    }

    /**
     * Return the integer number as a String.
     * @param number the integer number to convert to a string
     * @return a number string
     */
    public String formatInteger(Integer number) {
        String returnValue = "";
        NumberFormat nf = NumberFormat.getInstance();
        returnValue = nf.format(number);
        return returnValue;
    }

    /**
     * Return the double number as a String.
     * @param number the double number to convert to a string
     * @return a number string
     */
    public String formatDouble(Double number) {
        String returnValue = "";
        DecimalFormat df = new DecimalFormat("##,###,##0.00");
        returnValue = df.format(number);
        return returnValue;
    }

    /**
     * Return the date as a String without the time zone.
     * @param date the date to convert to a string
     * @return a time string without the time zone
     */
    public static String getFormattedReportTime(Date date) {
        if (date == null) { return null; }
        return dateFormatter.format(date);
    }

    /**
     * Helper method for generating HTML used to display name with additional info.
     * @param name the Domain or LearnLab name
     * @param isLearnlab flag
     * @return String the String to be printed
     */
    private String getNameHtml(String name, boolean isLearnlab) {
        return getNameHtml(name, isLearnlab, null);
    }

    /**
     * Helper method for generating HTML used to display name with additional info.
     * @param name the Domain or LearnLab name
     * @param isLearnlab flag
     * @param remoteName the name of remote instance, null if master
     * @param remoteInstance the RemoteInstanceItem
     * @return String the String to be printed
     */
    private String getNameHtml(String name, boolean isLearnlab, String remoteName) {
        StringBuffer sb = new StringBuffer(name);

        String type = isLearnlab ? "learnlab" : "domain";

        String remoteStr = (remoteName != null) ? remoteName + "_" : "";

        if (name.equals("Unspecified")) {
            sb.append(" <span><img src=\"images/information.png\" alt=\"info\"");
            sb.append(" class=\"").append(type).append("_unspecified_tooltip\"");
            sb.append(" id=\"").append(remoteStr).append(type);
            sb.append("_unspecified_info\"/></span>");
        } else if (name.equals("Other")) {
            sb.append(" <span><img src=\"images/information.png\" alt=\"info\"");
            sb.append(" class=\"").append(type).append("_other_tooltip\"");
            sb.append(" id=\"").append(remoteStr).append(type);
            sb.append("_other_info\"/></span>");
        }

        return sb.toString();
    }

    /**
     * This function returns a DTO report to be used in jsp page.
     * @param remoteInstance the remote instance item
     * @return String containing all HTML.
     */
    private LearnlabDomainMetricsReport getRemoteReport(RemoteInstanceItem remoteInstance) {

        MetricReportItem metricReport = null;
        if (remoteInstance == null) {
            // get the local (CMU) report
            metricReport = metricReportDao.getMostRecentLocalReport();
        } else {
            metricReport = metricReportDao.getMostRecentByRemote(remoteInstance);
        }

        // If the remote instance hasn't sent a MetricsReport, nothing to do.
        if (metricReport == null) { return null; }

        metricReport = metricReportDao.get((Integer)metricReport.getId());

        LearnlabDomainMetricsReport remoteReport = new LearnlabDomainMetricsReport();

        metricByLearnlabDao = DaoFactory.DEFAULT.getMetricByLearnlabDao();
        learnlabDao = DaoFactory.DEFAULT.getLearnlabDao();
        List<LearnlabItem> learnlabList = learnlabDao.getAll();

        // We can reuse these global attrs since the initial/composite report is finished.
        totalLearnlabFiles = 0;
        totalLearnlabPapers = 0;
        totalLearnlabDatasets = 0;
        totalLearnlabStudents = 0;
        totalLearnlabActions = 0;
        totalLearnlabHours = 0.00;

        learnlabReportValues = new ArrayList();

        Integer otherLearnlabId = null;
        Integer unspecifiedLearnlabId = null;
        for (Iterator it = learnlabList.iterator(); it.hasNext();) {
            learnlab = (LearnlabItem)learnlabDao.get((Integer)((LearnlabItem) it.next()).getId());

            if (learnlab == null) { continue; }
            
            // put 'Other' and 'Unspecified' at the bottom of the list
            if (learnlab.getName().equals("Other")) {
                otherLearnlabId = (Integer)learnlab.getId();
            } else if (learnlab.getName().equals("Unspecified")) {
                unspecifiedLearnlabId = (Integer)learnlab.getId();
            } else {
                setLearnlabReportValues(learnlab, metricReport);
            }
        }
        if (otherLearnlabId != null) {
            learnlab = learnlabDao.get(otherLearnlabId);
            if (learnlab != null) {
                setLearnlabReportValues(learnlab, metricReport);
            }
        }

        if (unspecifiedLearnlabId != null) {
            learnlab = learnlabDao.get(unspecifiedLearnlabId);
            if (learnlab != null) {
                setLearnlabReportValues(learnlab, metricReport);
            }
        }

        remoteReport.setTotalLearnlabFiles(formatInteger(totalLearnlabFiles));
        remoteReport.setTotalLearnlabPapers(formatInteger(totalLearnlabPapers));
        remoteReport.setTotalLearnlabDatasets(formatInteger(totalLearnlabDatasets));
        remoteReport.setTotalLearnlabStudents(formatInteger(totalLearnlabStudents));
        remoteReport.setTotalLearnlabActions(formatInteger(totalLearnlabActions));
        remoteReport.setTotalLearnlabHours(formatDouble(totalLearnlabHours));
        remoteReport.setMap("Learnlab", learnlabReportValues);

        totalDomainFiles = 0;
        totalDomainPapers = 0;
        totalDomainDatasets = 0;
        totalDomainStudents = 0;
        totalDomainActions = 0;
        totalDomainHours = 0.00;

        metricByDomainDao = DaoFactory.DEFAULT.getMetricByDomainDao();
        domainDao = DaoFactory.DEFAULT.getDomainDao();
        List<DomainItem> domainList = domainDao.getAll();

        domainReportValues = new ArrayList();

        Integer otherDomainId = null;
        Integer unspecifiedDomainId = null;
        for (Iterator it = domainList.iterator(); it.hasNext();) {
            domain = (DomainItem)domainDao.get((Integer)((DomainItem) it.next()).getId());

            if (domain == null) { continue; }

            // put 'Other' and 'Unspecified' at the bottom of the list
            if (domain.getName().equals("Other")) {
                otherDomainId = (Integer)domain.getId();
            } else if (domain.getName().equals("Unspecified")) {
                unspecifiedDomainId = (Integer)domain.getId();
            } else {
                setDomainReportValues(domain, metricReport);
            }
        }
        if (otherDomainId != null) {
            domain = domainDao.get(otherDomainId);
            if (domain != null) {
                setDomainReportValues(domain, metricReport);
            }
        }

        if (unspecifiedDomainId != null) {
            domain = domainDao.get(unspecifiedDomainId);
            if (domain != null) {
                setDomainReportValues(domain, metricReport);
            }
        }
        
        remoteReport.setTotalDomainFiles(formatInteger(totalDomainFiles));
        remoteReport.setTotalDomainPapers(formatInteger(totalDomainPapers));
        remoteReport.setTotalDomainDatasets(formatInteger(totalDomainDatasets));
        remoteReport.setTotalDomainStudents(formatInteger(totalDomainStudents));
        remoteReport.setTotalDomainActions(formatInteger(totalDomainActions));
        remoteReport.setTotalDomainHours(formatDouble(totalDomainHours));
        remoteReport.setMap("Domain", domainReportValues);

        return remoteReport;
    }

    /**
     * Set report values given a learnlab.
     * @param learnlab LearnlabItem that the values generated from.
     * @param metricReport the most recent MetricReport from specific remote instance
     */
    private void setLearnlabReportValues(LearnlabItem learnlab, MetricReportItem metricReport) {

        String riName = CMU_INSTANCE_NAME;

        if (metricReport.getRemoteInstance() != null) {
            RemoteInstanceDao riDao = DaoFactory.DEFAULT.getRemoteInstanceDao();
            RemoteInstanceItem riItem = riDao.get((Long)metricReport.getRemoteInstance().getId());
            riName = riItem.getName();
        }

        MetricByLearnlabReportDao mblrDao = DaoFactory.DEFAULT.getMetricByLearnlabReportDao();
        MetricByLearnlabReportItem item = mblrDao.findByLearnlabReport(learnlab, metricReport);
        List singleLearnlabReportValues = new ArrayList();
        singleLearnlabReportValues.add(getNameHtml(learnlab.getName(), true, riName));
        if (item != null) {

            Integer files = item.getFiles();
            Integer papers = item.getPapers();
            Integer datasets = item.getDatasets();
            Integer actions = item.getActions();
            Integer students = item.getStudents();
            Double hours = item.getHours();

            singleLearnlabReportValues.add(formatInteger(files));
            singleLearnlabReportValues.add(formatInteger(papers));
            singleLearnlabReportValues.add(formatInteger(datasets));
            singleLearnlabReportValues.add(formatInteger(actions));
            singleLearnlabReportValues.add(formatInteger(students));
            singleLearnlabReportValues.add(formatDouble(hours));

            totalLearnlabFiles += files;
            totalLearnlabPapers += papers;
            totalLearnlabDatasets += datasets;
            totalLearnlabStudents += students;
            totalLearnlabActions += actions;
            totalLearnlabHours += hours;

        } else {
            singleLearnlabReportValues.add(0);
            singleLearnlabReportValues.add(0);
            singleLearnlabReportValues.add(0);
            singleLearnlabReportValues.add(0);
            singleLearnlabReportValues.add(0);
            singleLearnlabReportValues.add(formatDouble(0.0));
        }
        learnlabReportValues.add(singleLearnlabReportValues);
    }

    /**
     * Set report values given a domain.
     * @param domain DomainItem that the values generated from.
     * @param metricReport the most recent MetricReport from specific remote instance
     */
    private void setDomainReportValues(DomainItem domain, MetricReportItem metricReport) {

        String riName = CMU_INSTANCE_NAME;

        if (metricReport.getRemoteInstance() != null) {
            RemoteInstanceDao riDao = DaoFactory.DEFAULT.getRemoteInstanceDao();
            RemoteInstanceItem riItem = riDao.get((Long)metricReport.getRemoteInstance().getId());
            riName = riItem.getName();
        }

        MetricByDomainReportDao mbdrDao = DaoFactory.DEFAULT.getMetricByDomainReportDao();
        MetricByDomainReportItem item = mbdrDao.findByDomainReport(domain, metricReport);
        List singleDomainReportValues = new ArrayList();
        singleDomainReportValues.add(getNameHtml(domain.getName(), false, riName));
        if (item != null) {

            Integer files = item.getFiles();
            Integer papers = item.getPapers();
            Integer datasets = item.getDatasets();
            Integer actions = item.getActions();
            Integer students = item.getStudents();
            Double hours = item.getHours();

            singleDomainReportValues.add(formatInteger(files));
            singleDomainReportValues.add(formatInteger(papers));
            singleDomainReportValues.add(formatInteger(datasets));
            singleDomainReportValues.add(formatInteger(actions));
            singleDomainReportValues.add(formatInteger(students));
            singleDomainReportValues.add(formatDouble(hours));

            totalDomainFiles += files;
            totalDomainPapers += papers;
            totalDomainDatasets += datasets;
            totalDomainStudents += students;
            totalDomainActions += actions;
            totalDomainHours += hours;

        } else {
            singleDomainReportValues.add(0);
            singleDomainReportValues.add(0);
            singleDomainReportValues.add(0);
            singleDomainReportValues.add(0);
            singleDomainReportValues.add(0);
            singleDomainReportValues.add(formatDouble(0.0));
        }

        domainReportValues.add(singleDomainReportValues);
    }

    /**
     * Helper method to generate HTML link for Remote Instance.
     * @param remoteInstance the instance
     * @return String the HTML
     */
    private String generateItemLink(RemoteInstanceItem remoteInstance) {
        
        StringBuffer sb = new StringBuffer();

        String url = remoteInstance.getDatashopUrl();
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }
        sb.append("<a href=\"");
        sb.append(url);
        sb.append("\" target=\"_blank\">");
        sb.append(remoteInstance.getName());
        sb.append("</a>");
        return sb.toString();
    }
}
