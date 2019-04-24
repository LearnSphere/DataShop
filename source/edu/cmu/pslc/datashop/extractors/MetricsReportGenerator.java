/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.extractors;

import java.sql.SQLException;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DomainDao;
import edu.cmu.pslc.datashop.dao.LearnlabDao;
import edu.cmu.pslc.datashop.dao.MetricByDomainDao;
import edu.cmu.pslc.datashop.dao.MetricByLearnlabDao;
import edu.cmu.pslc.datashop.dao.MetricReportDao;

import edu.cmu.pslc.datashop.item.DomainItem;
import edu.cmu.pslc.datashop.item.LearnlabItem;
import edu.cmu.pslc.datashop.item.MetricByDomainItem;
import edu.cmu.pslc.datashop.item.MetricByLearnlabItem;

import edu.cmu.pslc.datashop.util.DataShopInstance;
import edu.cmu.pslc.datashop.webservices.DatashopClient;

import static edu.cmu.pslc.datashop.item.MetricReportItem.HEADERS;
import static edu.cmu.pslc.datashop.item.MetricReportItem.DOMAIN;
import static edu.cmu.pslc.datashop.item.MetricReportItem.LEARNLAB;
import static edu.cmu.pslc.datashop.item.MetricReportItem.SEP;

/**
 * This class tallies up the metrics for the DataShop database. Specifically, it
 * logs the number of files, papers, datasets, student actions, students and
 * student hours.
 *
 * @author Kyle Cunningham
 * @version $Revision: 12671 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-10-08 09:36:42 -0400 (Thu, 08 Oct 2015) $
 * <!-- $KeyWordsOff: $ -->
 */

public final class MetricsReportGenerator extends AbstractExtractor {

    /**
     * Constructor.
     */
    private MetricsReportGenerator() { };

    /**
     * Calls metrics_report_sp stored procedure.
     * @throws SQLException SQLException
     */
    private void generateReport() throws SQLException {
        MetricReportDao dao = DaoFactory.DEFAULT.getMetricReportDao();
        dao.callMetricsReportSP();
    }

    /**
     * Helper method to write the latest MetricsReport to the master
     * DataShop instance.
     */
    private void writeReport(Logger logger) {
        StringBuffer sb = new StringBuffer();

        Long remoteInstanceId = DataShopInstance.getRemoteInstanceId();

        // If we failed to initialize this remote instance,
        // no need to write report out to master DataShop.
        if (remoteInstanceId == null) { return; }

        // write headers...
        for (int i = 0; i < HEADERS.size(); i++) {
            sb.append(HEADERS.get(i));
            if (i < (HEADERS.size() - 1)) {
                sb.append("\t");
            }
        }
        sb.append("\n");

        sb.append(getMetricsByDomain());
        sb.append(getMetricsByLearnlab());

        StringBuffer path = new StringBuffer();
        path.append("/instances/").append(remoteInstanceId).append("/metrics/set");

        try {
            DatashopClient client = DatashopClient.getDatashopClientForRemoteRequests();
            if (client != null) {
                client.getPostService(path.toString(), sb.toString(), "text/xml");
            }
        } catch (Exception e) {
            // Send email if error happens.
            String msg = "Failed to push MetricsReport to master DataShop instance.";
            logger.error(msg, e);
            sendErrorEmail(logger, msg, e);
        }
    }

    /**
     * Helper method for reading MetricByDomain info and writing out as a String
     * @return String the result
     */
    private String getMetricsByDomain() {

        StringBuffer sb = new StringBuffer();

        MetricByDomainDao metricByDomainDao = DaoFactory.DEFAULT.getMetricByDomainDao();

        DomainDao domainDao = DaoFactory.DEFAULT.getDomainDao();
        List<DomainItem> domainList = domainDao.getAll();
        Iterator<DomainItem> iter = domainList.iterator();
        while (iter.hasNext()) {
            DomainItem domain = iter.next();
            MetricByDomainItem metricByDomain = metricByDomainDao.findByDomain(domain);
            if (metricByDomain == null) { continue; }

            sb.append(DOMAIN).append(SEP).append(domain.getName());
            sb.append("\t");
            sb.append(metricByDomain.getFiles());
            sb.append("\t");
            sb.append(metricByDomain.getPapers());
            sb.append("\t");
            sb.append(metricByDomain.getDatasets());
            sb.append("\t");
            sb.append(metricByDomain.getActions());
            sb.append("\t");
            sb.append(metricByDomain.getStudents());
            sb.append("\t");
            sb.append(metricByDomain.getHours());
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * Helper method for reading MetricByLearnlab info and writing out as a String
     * @return String the result
     */
    private String getMetricsByLearnlab() {

        StringBuffer sb = new StringBuffer();

        MetricByLearnlabDao metricByLearnlabDao = DaoFactory.DEFAULT.getMetricByLearnlabDao();

        LearnlabDao learnlabDao = DaoFactory.DEFAULT.getLearnlabDao();
        List<LearnlabItem> learnlabList = learnlabDao.getAll();
        Iterator<LearnlabItem> iter = learnlabList.iterator();
        while (iter.hasNext()) {
            LearnlabItem learnlab = iter.next();
            MetricByLearnlabItem metricByLearnlab = metricByLearnlabDao.findByLearnlab(learnlab);
            if (metricByLearnlab == null) { continue; }

            sb.append(LEARNLAB).append(SEP).append(learnlab.getName());
            sb.append("\t");
            sb.append(metricByLearnlab.getFiles());
            sb.append("\t");
            sb.append(metricByLearnlab.getPapers());
            sb.append("\t");
            sb.append(metricByLearnlab.getDatasets());
            sb.append("\t");
            sb.append(metricByLearnlab.getActions());
            sb.append("\t");
            sb.append(metricByLearnlab.getStudents());
            sb.append("\t");
            sb.append(metricByLearnlab.getHours());
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * Main!
     * @param args command line arguments.
     * @throws SQLException
     */
    public static void main(String ...args) {
        Logger logger = Logger.getLogger("MetricsReportGenerator.main");
        logger.info("MetricsReportGenerator starting...");

        // Initialize DataShop instance from the database
        DataShopInstance.initialize();

        MetricsReportGenerator generator = new MetricsReportGenerator();
        try {
            generator.generateReport();

            // If this instance is a slave, write the MetricsReport to the master.
            if (DataShopInstance.isSlave()) {
                generator.writeReport(logger);
            }

        } catch (Throwable throwable) {
            logger.error("Unknown error in main method.", throwable);
            generator.sendErrorEmail(logger, "error in MetricsReportGenerator", throwable);
        } finally {
            logger.info("MetricsReportGenerator done.");
        }
    }
}
