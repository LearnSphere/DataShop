/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.example;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import edu.cmu.pslc.datashop.dao.CurriculumDao;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.DatasetLevelDao;
import edu.cmu.pslc.datashop.dao.InstructorDao;
import edu.cmu.pslc.datashop.dao.SchoolDao;
import edu.cmu.pslc.datashop.dao.StudentDao;
import edu.cmu.pslc.datashop.item.CurriculumItem;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.DatasetLevelItem;
import edu.cmu.pslc.datashop.item.InstructorItem;
import edu.cmu.pslc.datashop.item.SchoolItem;
import edu.cmu.pslc.datashop.item.StudentItem;

/**
 * Handle the transaction for context message.
 *
 * @author Hui Cheng
 * @version $Revision: 10513 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2014-02-03 12:56:21 -0500 (Mon, 03 Feb 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ExampleTransactionCallback implements TransactionCallback {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** Blah. */
    private String schoolName;
    /** Blah. */
    private String instName;
    /** Blah. */
    private String studentName;

    /**
     * Constructor.
     * @param schoolName the school name
     * @param instName the instructor name
     * @param studentName the student name
     */
    public ExampleTransactionCallback(String schoolName, String instName, String studentName) {
        this.schoolName = schoolName;
        this.instName = instName;
        this.studentName = studentName;
    }

    /**
     * Implement the interface.
     * All transaction actions are in one method for rollback.
     * The steps are:
     * <UL>
     * <LI>save school</LI>
     * <LI>save student</LI>
     * <LI>save instructor after setting schoolId</LI>
     * <LI>save class after setting instructorId and schoolId</LI>
     * <LI>save dataset and all related</LI>
     * </UL>
     * @param ts TransactionStatus.
     * @return Object.
     * */
    public Object doInTransaction(TransactionStatus ts) {

        StudentItem studentItem = null;
        try {

            // first, get a School DAO from the DAO factory.
            SchoolDao schoolDao = DaoFactory.DEFAULT.getSchoolDao();

            // create a school item.
            SchoolItem schoolItem = new SchoolItem();
            schoolItem.setSchoolName(schoolName);

            // check if there is a school with this name already
            // find the item by name
            Collection schoolList = schoolDao.find(schoolName);

            // if the school does not exist, then save it to the database
            schoolItem = (SchoolItem)schoolDao.findOrCreate(schoolList, schoolItem);

            // now, get an instructor DAO, create an instructor item and save it.
            InstructorDao instDao = DaoFactory.DEFAULT.getInstructorDao();
            InstructorItem instItem = new InstructorItem();
            instItem.setInstructorName(instName);
            instItem.setSchool(schoolItem);

            // check if there is a instructor with this name already
            // find the item by name
            Collection instList = instDao.find(instName);

            // check if this instructor exists, and if it does get that one
            instItem = (InstructorItem)instDao.findOrCreate(instList, instItem);

            // get a student DAO, create a student item, etc.
            StudentDao studentDao = DaoFactory.DEFAULT.getStudentDao();
            Collection studentList = studentDao.find(studentName);

            studentItem = new StudentItem();
            studentItem.setActualUserId(studentName);

            // check if this student exists, and if it does get that one
            studentItem = (StudentItem)studentDao.findOrCreate(studentList, studentItem);

            // create a curriculum
            CurriculumDao curricDao = DaoFactory.DEFAULT.getCurriculumDao();

            CurriculumItem curricItem = new CurriculumItem();
            curricItem.setCurriculumName("Algebra for Dummies");
            curricItem = (CurriculumItem)curricDao.findOrCreate(curricDao.findAll(), curricItem);

            DatasetItem datasetItem = new DatasetItem();
            datasetItem.setDatasetName("DataShop 101");
            datasetItem.setStudyFlag(DatasetItem.STUDY_FLAG_NOT_SPEC);
            datasetItem.setCurriculum(curricItem);

            DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
            datasetItem = (DatasetItem)datasetDao.findOrCreate(
                    curricItem.getDatasetsExternal(), datasetItem);

            DatasetLevelItem unitLevel = new DatasetLevelItem();
            unitLevel.setLevelTitle("Unit");
            unitLevel.setLevelName("Data Layer");
            unitLevel.setDataset(datasetItem);

            DatasetLevelDao datasetLevelDao = DaoFactory.DEFAULT.getDatasetLevelDao();
            unitLevel = (DatasetLevelItem)datasetLevelDao.
                    findOrCreate(datasetItem.getDatasetLevelsExternal(), unitLevel);

        } catch (Exception exception) {
            ts.setRollbackOnly();

            logger.warn("Saving of example failed. " + exception, exception);

            return null;
        }
        return studentItem;
    }
}