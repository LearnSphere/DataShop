/*
* Carnegie Mellon University, Human Computer Interaction Institute
* Copyright 2005
* All Rights Reserved
*/
package edu.cmu.pslc.datashop.servlet.datasetinfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DomainDao;
import edu.cmu.pslc.datashop.dao.LearnlabDao;
import edu.cmu.pslc.datashop.dao.PaperDao;
import edu.cmu.pslc.datashop.item.DomainItem;
import edu.cmu.pslc.datashop.item.LearnlabItem;
import edu.cmu.pslc.datashop.item.PaperItem;

 /**
 * This class assists in the creation of the dataset info edit.
 *
 * @author Shanwen Yu
 * @version $Revision: 7169 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2011-11-01 13:02:06 -0400 (Tue, 01 Nov 2011) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DatasetInfoEditHelper {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** Default constructor. */
    public DatasetInfoEditHelper() {
        logger.debug("constructor");
    }

    /**
     * Get a String list for Domain/Learnlab set.
     * @return String List
     */
    public List<String> getDomainLearnlabList() {
        DomainDao domainDao = DaoFactory.DEFAULT.getDomainDao();
        DomainItem domainItem = new DomainItem();
        List<DomainItem> domainList = domainDao.getAll();
        List<String> domainLearnlabList = new ArrayList<String>();
        Integer tempDomainId = 0;
        for (Iterator it = domainList.iterator(); it.hasNext();) {

            domainItem = (DomainItem)domainDao.get((Integer)((DomainItem) it.next()).getId());

            //if domain name is "Other" and list does not hit the bottom,
            // then skip adding "Other" but record its ID
            if (domainItem.getName().equals("Other") && it.hasNext()) {
                tempDomainId = (Integer)domainItem.getId();
            // if the name is not "Other" and list hits the bottom
            // then add regular item and "Other"
            } else if ((!it.hasNext()) && !(domainItem.getName().equals("Other"))) {
                domainLearnlabList = getLearnlabs(domainItem, domainLearnlabList);
                // set domainItem to "Other"
                domainItem = domainDao.get(tempDomainId);

                domainLearnlabList = getLearnlabs(domainItem, domainLearnlabList);
            } else {
                domainLearnlabList = getLearnlabs(domainItem, domainLearnlabList);
            }
        }
        return domainLearnlabList;
    }

    /**
     * Add learnlab names to a given list given a domain.
     * @param domainItem domain that the learnlabs belong to
     * @param itemList list to be added
     * @return String list
     */
    public List<String> getLearnlabs(DomainItem domainItem, List<String> itemList) {
        LearnlabDao learnlabDao = DaoFactory.DEFAULT.getLearnlabDao();
        List<LearnlabItem> learnlabList = domainItem.getLearnlabsExternal();
        Integer tempLearnlabId = 0;
        for (Iterator it = learnlabList.iterator(); it.hasNext();) {
            LearnlabItem learnlabItem = (LearnlabItem)learnlabDao.get((Integer)(
                                                (LearnlabItem) it.next()).getId());
            if (learnlabItem.getName().equals("Other")
                    && domainItem.getName().equals("Other")) {
                itemList.add("Other");
            } else {
                // if the name is "Other" and list does not hit the bottom,
                // then skip adding "Other" but record its ID
                if (it.hasNext() && learnlabItem.getName().equals("Other")) {
                    tempLearnlabId = (Integer)learnlabItem.getId();
                } else if ((!it.hasNext()) && !(learnlabItem.getName().equals("Other"))) {
                    // if the name is not "Other" and list hits the bottom
                    // then add regular item and "Other"
                    itemList.add(domainItem.getName() + "/" + learnlabItem.getName());
                    itemList.add(domainItem.getName() + "/"
                            + learnlabDao.get(tempLearnlabId).getName());
                } else {
                    itemList.add(domainItem.getName() + "/" + learnlabItem.getName());
                }
            }
        }
        return itemList;
    }

    /**
     * Get a String for citation given a file id.
     * @param fileId the id of the file item
     * @return String value of the citation
     */
    public String getCitation(int fileId) {
        logger.debug("getCitation");
        String citation = "";
        PaperDao paperDao = DaoFactory.DEFAULT.getPaperDao();
        PaperItem paperItem = new PaperItem();

        List paperList = paperDao.findAll();
        for (Iterator it = paperList.iterator(); it.hasNext();) {
            paperItem = (PaperItem)it.next();
            if (Integer.parseInt(paperItem.getFile().getId().toString()) == fileId) {

                citation = paperItem.getCitation();
            }
        }
        return citation;
    }
}
