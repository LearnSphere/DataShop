/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.webservices;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.json.JSONObject;

import edu.cmu.pslc.datashop.discoursedb.dao.ContributionDao;
import edu.cmu.pslc.datashop.discoursedb.dao.DataSourcesDao;
import edu.cmu.pslc.datashop.discoursedb.dao.DiscourseDao;
import edu.cmu.pslc.datashop.discoursedb.dao.DUserDao;
import edu.cmu.pslc.datashop.discoursedb.dao.DiscourseDbDaoFactory;
import edu.cmu.pslc.datashop.discoursedb.dao.DiscoursePartDao;
import edu.cmu.pslc.datashop.discoursedb.dao.DiscourseRelationDao;
import edu.cmu.pslc.datashop.discoursedb.item.DiscourseItem;
import edu.cmu.pslc.datashop.dao.AlphaScoreDao;
import edu.cmu.pslc.datashop.dao.AuthorizationDao;
import edu.cmu.pslc.datashop.dao.CfTxLevelDao;
import edu.cmu.pslc.datashop.dao.CustomFieldDao;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.DomainDao;
import edu.cmu.pslc.datashop.dao.LearnlabDao;
import edu.cmu.pslc.datashop.dao.SampleDao;
import edu.cmu.pslc.datashop.dao.SampleMetricDao;
import edu.cmu.pslc.datashop.dao.TransactionDao;
import edu.cmu.pslc.datashop.dto.AuthorizationDTO;
import edu.cmu.pslc.datashop.dto.CachedFileInfo;
import edu.cmu.pslc.datashop.dto.CustomFieldDTO;
import edu.cmu.pslc.datashop.dto.DatasetDTO;
import edu.cmu.pslc.datashop.dto.DiscourseDTO;
import edu.cmu.pslc.datashop.dto.ExternalAnalysisDTO;
import edu.cmu.pslc.datashop.dto.FilterDTO;
import edu.cmu.pslc.datashop.dto.KcModelDTO;
import edu.cmu.pslc.datashop.dto.LearningCurveOptions;
import edu.cmu.pslc.datashop.dto.LearningCurvePoint;
import edu.cmu.pslc.datashop.dto.LearningCurveSkillDetails;
import edu.cmu.pslc.datashop.dto.SampleDTO;
import edu.cmu.pslc.datashop.dto.ExternalAnalysisFile;
import edu.cmu.pslc.datashop.dto.StepRollupExportOptions;
import edu.cmu.pslc.datashop.extractors.StepRollupExportTask;
import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.AuthorizationId;
import edu.cmu.pslc.datashop.item.AuthorizationItem;
import edu.cmu.pslc.datashop.item.CfTxLevelItem;
import edu.cmu.pslc.datashop.item.CustomFieldItem;
import edu.cmu.pslc.datashop.item.CustomFieldNameValueItem;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.ExternalAnalysisItem;
import edu.cmu.pslc.datashop.item.FileItem;
import edu.cmu.pslc.datashop.item.FilterItem;
import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.item.PaperItem;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.item.SkillItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.item.TransactionItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.servlet.export.CachedExportFileReader;
import edu.cmu.pslc.datashop.servlet.export.TransactionExportHelper;
import edu.cmu.pslc.datashop.servlet.export.TxExportBean;
import edu.cmu.pslc.datashop.servlet.filesinfo.FilesInfoHelper;
import edu.cmu.pslc.datashop.servlet.kcmodel.KCModelHelper;
import edu.cmu.pslc.datashop.servlet.learningcurve.LearningCurveContext;
import edu.cmu.pslc.datashop.servlet.learningcurve.LearningCurveDatasetProducer;
import edu.cmu.pslc.datashop.servlet.learningcurve.LearningCurveImage;
import edu.cmu.pslc.datashop.servlet.webservices.WebService.AccessParam;
import edu.cmu.pslc.datashop.util.DataShopInstance;
import edu.cmu.pslc.datashop.util.FileUtils;
import edu.cmu.pslc.datashop.util.LogException;
import edu.cmu.pslc.datashop.util.LogUtils;
import edu.cmu.pslc.datashop.xml.XMLConstants;
import static
    edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.INACCESSIBLE_DATASET_ERR;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.INVALID_DATASET_ERR;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.UNRELEASED_DATASET_ERR;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.INACCESSIBLE_SAMPLE_ERR;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.INVALID_SAMPLE_ERR;
import static
        edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.INVALID_EXTERNAL_ANALYSIS_ERR;
import static
    edu.cmu.pslc.datashop.servlet
        .webservices.WebServiceException.INACCESSIBLE_EXTERNAL_ANALYSIS_ERR;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.INVALID_PARAM_VAL_ERR;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.NO_CACHED_FILE_ERR;
import static
    edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.INVALID_POST_REQUEST_BODY_ERR;
import static
edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.INVALID_CUSTOM_FIELD_FOR_DATASET_ERR;
import static
edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.paramValueConflictException;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.unknownErrorException;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.invalidDataException;
import static
    edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.invalidParamValueException;
import static edu.cmu.pslc.datashop.servlet.
    webservices.WebServiceException.INACCESSIBLE_CUSTOM_FIELD_ERR;
import static edu.cmu.pslc.datashop.servlet.
    webservices.WebServiceException.INVALID_VALUE_FOR_ELEMENT_ERR;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.INVALID_SKILL;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.INVALID_SKILL_MODEL;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.INVALID_XML_ERR;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.UNAUTHORIZED_USER_ERR;
import static java.util.Arrays.asList;
import static java.lang.String.format;

import edu.cmu.pslc.datashop.util.MailUtils;
import edu.cmu.pslc.datashop.util.ServerNameUtils;

/**
 * Web service methods requiring database access go here.
 *
 * @author Jim Rankin
 * @version $Revision: 15839 $ <BR>
 *          Last modified by: $Author: hcheng $ <BR>
 *          Last modified on: $Date: 2012-09-13 15:10:27 -0400 (Thu, 13 Sep
 *          2012) $ <!-- $KeyWordsOff: $ -->
 */
public class WebServiceHelper {
    /** Max file size is 200MB. */
    private static final int MAX_FILE_SIZE = 200000000;
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());
    /** The session factory. */
    private SessionFactory sessionFactory;
    /** The DiscourseDB session factory. */
    private SessionFactory discourseSessionFactory;
    /** Base directory for the files associated with a dataset. */
    private String baseDir;
    /** the file path of the transaction export stored procedure */
    private String txExportSpFilePath;
    /** the file path of the aggregator stored procedure */
    private String aggregatorSpFilePath;
    /** Flag indicating if emails are being sent. */
    private boolean isSendmailActive;
    /** Email address for datashop-help. */
    private String emailAddressDatashopHelp;
    /** Display when an export needs to be cached. */
    private static final String PLEASE_TRY_AGAIN = "Caching process is starting. Try again later.";
    /** Display when a transaction export has never been cached. */
    private static final String NO_TX_CACHE =
            "Transactions cache file for sample %d does not exist. "
            + PLEASE_TRY_AGAIN;
    /** Display when a transaction export needs to be re-cached. */
    private static final String OUT_OF_DATE_TX_CACHE =
            "Transactions cache file for sample %d is out-of-date. "
            + PLEASE_TRY_AGAIN;
    /** Display when a step export has never been cached. */
    private static final String NO_STEP_CACHE =
            "Student-steps cache file for sample %d does not exist. "
            + PLEASE_TRY_AGAIN;
    /** Display when a step export needs to be re-cached. */
    private static final String OUT_OF_DATE_STEP_CACHE =
            "Student-steps cache file for sample %d is out-of-date. "
            + PLEASE_TRY_AGAIN;
    /**
     * Maximum number of allowed characters for CustomField types
     * number, string and and date.
     */
    private static final Integer MAX_NUM_CHARS_CF = 255;
    /** Maximum number of allowed characters for CustomField big type. */
    private static final Integer MAX_NUM_CHARS_CF_BIG = 65000;
    
    /** Default e-mail address of DataShop Email Bucket. */
    private static final String EMAIL_DATASHOP_BUCKET_DEFAULT =
        "qa-ds-email-bucket@lists.andrew.cmu.edu";
    /** Default e-mail address of DataShop Email Bucket. */
    private static String emailAddressDatashopBucket = EMAIL_DATASHOP_BUCKET_DEFAULT;

    /** Returns sessionFactory. @return Returns the sessionFactory. */
    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /** Set sessionFactory. @param sessionFactory The sessionFactory to set. */
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /** Open a new session. @return the new session */
    protected Session newSession() {
        return sessionFactory.openSession();
    }

    /** Returns discourseSessionFactory. @return Returns the discourseSessionFactory. */
    public SessionFactory getDiscourseSessionFactory() {
        return discourseSessionFactory;
    }

    /** Set discourseSessionFactory.
     *  @param discourseSessionFactory The discourseSessionFactory to set.
     */
    public void setDiscourseSessionFactory(SessionFactory discourseSessionFactory) {
        this.discourseSessionFactory = discourseSessionFactory;
    }

    /** Open a new DiscourseDb session. @return the new session */
    private Session newDiscourseSession() {
        return discourseSessionFactory.openSession();
    }

    /**
     * Initialize the base directory and transaction export file path.
     *
     * @param baseDir
     *            base directory for the files associated with a dataset
     * @param txExportSpFilePath
     *            the file path of the transaction export stored procedure
     * @param isSendmailActive flag indicating if sendmail is enabled
     * @param emailAddressDatashopHelp email address for datashop-help
     */
    public void init(String baseDir, String txExportSpFilePath, String aggregatorSpFilePath,
                     boolean isSendmailActive, String emailAddressDatashopHelp) {
        this.baseDir = baseDir;
        this.txExportSpFilePath = txExportSpFilePath;
        this.aggregatorSpFilePath = aggregatorSpFilePath;
        this.isSendmailActive = isSendmailActive;
        this.emailAddressDatashopHelp = emailAddressDatashopHelp;
    }

    /**
     * Attach the item to the current session. Avoids lazy initialization
     * errors.
     *
     * @param <T>
     *            the kind of item
     * @param session
     *            the current session
     * @param klass
     *            the item class
     * @param item
     *            the item
     * @return the item attached to the current session
     */
    protected <T extends Item> T attach(Session session, final Class<T> klass,
            T item) {
        if (item == null) {
            return null;
        }
        return (T) session.get(klass, (Serializable) item.getId());
    }

    /**
     * Attach all the item to the current session. Avoids lazy initialization
     * errors.
     *
     * @param <T>
     *            the kind of item
     * @param session
     *            the current session
     * @param klass
     *            the item class
     * @param items
     *            the items
     * @return the item attached to the current session
     */
    private <T extends Item> List<T> attach(final Session session,
            final Class<T> klass, final List<T> items) {
        return new ArrayList<T>() {
            {
                for (T item : items) {
                    add(attach(session, klass, item));
                }
            }
        };
    }

    /** the dataset data access object. @return the dataset data access object */
    private DatasetDao datasetDao() {
        return DaoFactory.DEFAULT.getDatasetDao();
    }

    /**
     * Find the dataset with the id.
     *
     * @param user
     *            check accessibility for this user
     * @param id
     *            the dataset id
     * @param session
     *            the current session
     * @param access
     *            editable, viewable or all
     * @return the dataset with the id
     * @throws WebServiceException
     *             thrown for invalid or unauthorized dataset id
     */
    private DatasetItem findDataset(UserItem user, Integer id, Session session,
            AccessParam access) throws WebServiceException {
        DatasetItem dataset = attach(session, DatasetItem.class, datasetDao()
                .find(id));
        if ((dataset == null) || isDatasetDeleted(dataset)) {
            throw new WebServiceException(INVALID_DATASET_ERR, "Dataset " + id
                    + " is not valid.");
        }
        // Check if this user is authorized for this project, or the project has
        // public access.
        if (!isAccessible(user, dataset.getProject(), access)) {
            throw inaccessibleDatasetError(id);
        }
        return dataset;
    }

    /**
     * Find the dataset with the id. (Access defaults to viewable.)
     *
     * @param user
     *            check accessibility for this user
     * @param id
     *            the dataset id
     * @param session
     *            the current session
     * @return the dataset with the id
     * @throws WebServiceException
     *             thrown for invalid or unauthorized dataset id
     */
    private DatasetItem findDataset(UserItem user, Integer id, Session session)
            throws WebServiceException {
        return findDataset(user, id, session, AccessParam.VIEWABLE);
    }

    /**
     * Find the dataset with the id.
     *
     * @param user
     *            check accessibility for this user
     * @param id
     *            the dataset id
     * @param access
     *            editable, viewable or all
     * @return the dataset with the id
     * @throws WebServiceException
     *             thrown for invalid or unauthorized dataset id
     */
    protected DatasetItem findDataset(UserItem user, Integer id,
            AccessParam access) throws WebServiceException {
        Session session = null;
        DatasetItem item = null;
        try {
            session = newSession();
            item = findDataset(user, id, session, access);
            UserItem attachedUser = attach(session, UserItem.class, user);
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return item;
    }

    /**
     * Method to check if dataset has been released.
     * @param datasetItem the dataset
     * @return boolean indicating availability
     */
    private boolean isDatasetReleased(DatasetItem datasetItem) {
        if (datasetItem == null) { return false; }

        if (datasetItem.getReleasedFlag() == null) {
            return false;
        }

        return datasetItem.getReleasedFlag();
    }

    /**
     * Method to check if dataset has been marked for delete.
     * @param datasetItem the dataset
     * @return boolean indicating availability
     */
    private boolean isDatasetDeleted(DatasetItem datasetItem) {
        if (datasetItem == null) { return true; }

        if (datasetItem.getDeletedFlag() == null) {
            return false;
        }

        return datasetItem.getDeletedFlag();
    }

    /**
     * User does not have the proper access rights for the dataset.
     *
     * @param id
     *            the dataset id
     * @return exception encoding attempt at unauthorized dataset access
     */
    private WebServiceException inaccessibleDatasetError(Integer id) {
        return new WebServiceException(INACCESSIBLE_DATASET_ERR, "Dataset "
                + id + " is not accessible.");
    }

    /**
     * Dataset has not been released. It is not available for exports.
     *
     * @param id
     *            the dataset id
     * @return exception encoding attempt at unreleased dataset access
     */
    private WebServiceException unreleasedDatasetError(Integer id) {
        return new WebServiceException(UNRELEASED_DATASET_ERR, "Dataset "
                + id + " is not released.");
    }

    /**
     * Returns true if the project is public.
     *
     * @param project
     *            the project
     * @return true if the project is public, false otherwise
     */
    private boolean isPublic(ProjectItem project) {
        if (project == null) {
            return false;
        }
        return authDao().isPublic((Integer) project.getId());
    }

    /**
     * Returns the authorization level given a user and project, or null if not
     * authorized.
     *
     * @param user
     *            the user
     * @param project
     *            the project
     * @return level if authorized, null otherwise
     */
    public String getAuth(UserItem user, ProjectItem project) {
        if (project == null || user == null) {
            return null;
        }

        String authLevel = authDao().getAuthorization((String) user.getId(),
                                                      (Integer) project.getId());

        // Public project is equivalent to 'edit' if authLevel is null.
        if ((authLevel == null) && isPublic(project)) {
            return "edit";
        }
        return authLevel;
    }

    /** The authorization DAO. @return the authorization DAO */
    private AuthorizationDao authDao() {
        return DaoFactory.DEFAULT.getAuthorizationDao();
    }

    /**
     * Translate the dataset item into a dataset DTO.
     *
     * @param user
     *            check accessibility for this user
     * @param item
     *            the item
     * @param verbose
     *            provide all the output (if true) or only a subset
     * @return the item translated into a DTO
     */
    protected DatasetDTO datasetDTOForItem(UserItem user, DatasetItem item,
            boolean verbose) {
        logDebug("datasetDTOForItem verbose is ", verbose);
        DatasetDTO dto = new DatasetDTO();
        if (item != null
                && (item.getDeletedFlag() == null || !item.getDeletedFlag())) {
            dto.setId((Integer) item.getId());
            ProjectItem project = item.getProject();
            if (project != null) {
                if (project.getPrimaryInvestigator() != null) {
                    dto.setPi((String) project.getPrimaryInvestigator().getId());
                    dto.setPiName((String) project.getPrimaryInvestigator().getName());
                }
                if (project.getDataProvider() != null
                        && !project.getDataProvider().equals(
                                project.getPrimaryInvestigator())) {
                    dto.setDataProvider((String) project.getDataProvider().getId());
                    dto.setDataProviderName((String) project.getDataProvider().getName());
                    logDebug("datasetDTOForItem dp is ", project.getDataProvider());
                } else {
                    logDebug("datasetDTOForItem dp is not set or not different");
                }
            }
            if (item.getDomain() != null) {
                Integer domainId = (Integer) item.getDomain().getId();
                DomainDao domainDao = DaoFactory.DEFAULT.getDomainDao();
                if (domainDao.get(domainId) != null) {
                    dto.setDomain(domainDao.get(domainId).getName());
                }
            }
            if (item.getLearnlab() != null) {
                Integer learnlabId = (Integer) item.getLearnlab().getId();
                LearnlabDao learnlabDao = DaoFactory.DEFAULT.getLearnlabDao();
                if (learnlabDao.get(learnlabId) != null) {
                    dto.setLearnlab(learnlabDao.get(learnlabId).getName());
                }
            }
            dto.setName(item.getDatasetName());
            dto.setStartDate(item.getStartTime());
            dto.setEndDate(item.getEndTime());
            dto.setStatus(item.getStatus());
            dto.setNumberOfSamples(item.getSamplesExternal().size());
            dto.setNumberOfKcModels(item.getSkillModelsExternal().size());

            SampleMetricDao metricDao = DaoFactory.DEFAULT.getSampleMetricDao();
            dto.setNumberOfStudents(metricDao.getTotalStudents(item));
            dto.setNumberOfTransactions(metricDao.getTotalTransactions(item));
            dto.setNumberOfUniqueSteps(metricDao.getTotalUniqueSteps(item));
            dto.setNumberOfSteps(metricDao.getTotalPerformedSteps(item));

            // Sigh. Because of the conversion from milliseconds, the result
            // can be a very small, non-zero number that must be rounded/formatted
            // to zero.
            Double studentHours = metricDao.getTotalStudentHours(item);
            BigDecimal bd = new BigDecimal(studentHours).setScale(2, RoundingMode.HALF_UP);
            dto.setNumberOfStudentHours(bd.doubleValue());
            dto.setNumberOfAccessibleSamples(authDao().findMySamples(user, item));

            boolean isReleased =
                (item.getReleasedFlag() == null) ? false : item.getReleasedFlag();
            dto.setReleased(isReleased);

            project = item.getProject();

            if (project != null) {
                dto.setProject(project.getProjectName());

                String access = authDao().getAuthorization((String) user.getId(),
                                                           (Integer) project.getId());

                // dataset is public if default user has view or edit access
                boolean isPublic = isPublic(project);

                // if no authorizations for this user, check for public access
                if (access == null) {
                    access = isPublic ? "public" : "private";
                }
                dto.setAccess(access);
                dto.setPublic(isPublic);
            }

            if (verbose) {
                AlphaScoreDao alphaScoreDao = DaoFactory.DEFAULT.getAlphaScoreDao();
                dto.setTutor(item.getTutor());
                if (item.getCurriculum() != null) {
                    dto.setCurriculum(item.getCurriculum().getCurriculumName());
                }
                dto.setDescription(item.getDescription());
                dto.setSchool(item.getSchool());
                dto.setAdditionalNotes(item.getNotes());
                dto.setHasStudyData(item.getStudyFlag());
                if (item.getStudyFlag().equals(DatasetItem.STUDY_FLAG_YES)) {
                    dto.setHypothesis(item.getHypothesis());
                }
                dto.setAcknowledgment(item.getAcknowledgment());
                PaperItem citation = item.getPreferredPaper();
                if (citation != null) {
                    dto.setCitation(citation.getCitation());
                }
                for (SkillModelItem skillModelItem : item.getSkillModelsExternal()) {
                    KcModelDTO kcModelDto = new KcModelDTO();
                    kcModelDto.setId(((Long) skillModelItem.getId()).intValue());
                    int totalNumStudentsWithAlphaScore = alphaScoreDao.find(
                            skillModelItem).size();
                    int numSkills = skillModelItem.getSkillsExternal().size();
                    int numParameters = numSkills * 2
                            + totalNumStudentsWithAlphaScore;
                    kcModelDto.setAic(skillModelItem.getAic());
                    kcModelDto.setBic(skillModelItem.getBic());
                    kcModelDto.setName(skillModelItem.getSkillModelName());
                    kcModelDto.setLogisticRegressionModelStatus(skillModelItem
                            .getLfaStatusForDisplay());
                    kcModelDto.setLogisticRegressionModelStatusDescription(skillModelItem
                                    .getLfaStatusDescription());
                    kcModelDto.setNumberOfKcs(numSkills);
                    kcModelDto.setObservationsWithKcs(skillModelItem
                            .getNumObservations());
                    if (SkillModelItem.LFA_STATUS_COMPLETE.equals(skillModelItem
                            .getLfaStatus())) {
                        kcModelDto.setNumberOfParameters(numParameters);
                    }
                    kcModelDto.setLogLikelihood(skillModelItem.getLogLikelihood());
                    kcModelDto.setCrossValidationStatus(skillModelItem
                            .getCvStatusForDisplay());
                    kcModelDto.setCrossValidationStatusDescription(skillModelItem
                                    .getCvStatusDescription());

                    if (SkillModelItem.LFA_STATUS_COMPLETE.equals(skillModelItem
                            .getCvStatus())) {
                        kcModelDto
                                .setStudentStratifiedCrossValidationRmse(skillModelItem
                                        .getCvStudentStratifiedRmse());
                        kcModelDto
                                .setStepStratifiedCrossValidationRmse(skillModelItem
                                        .getCvStepStratifiedRmse());
                        kcModelDto
                                .setUnstratifiedCrossValidationRmse(skillModelItem
                                        .getCvUnstratifiedRmse());
                        kcModelDto
                                .setUnstratifiedNumberOfObservations(skillModelItem
                                        .getUnstratifiedNumObservations());
                        kcModelDto.setUnstratifiedNumberOfParameters(skillModelItem
                                .getUnstratifiedNumParameters());
                    }
                    dto.addKcModel(kcModelDto);
                }
            }
        }
        return dto;
    }

    /**
     * Fetch the dataset item for the id from the database and use it to
     * populate a dataset DTO.
     *
     * @param user
     *            check accessibility for this user
     * @param datasetId
     *            a dataset id
     * @param verbose
     *            provide all the output (if true) or only a subset
     * @param access
     *            editable, viewable or all
     * @return a dataset DTO representing the dataset with the given id
     * @throws WebServiceException
     *             thrown for invalid or unauthorized dataset id
     */
    public DatasetDTO datasetDTOForId(UserItem user, Integer datasetId,
            boolean verbose, AccessParam access) throws WebServiceException {
        DatasetDTO dto = null;
        Session session = null;

        try {
            session = newSession();

            DatasetItem item = findDataset(user, datasetId, session, access);
            UserItem attachedUser = attach(session, UserItem.class, user);
            if (item.getDeletedFlag() == null || !item.getDeletedFlag()) {
                dto = datasetDTOForItem(attachedUser, item, verbose);
            }
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return dto;
    }

    /**
     * Fetch the dataset item for the id from the database and use it to
     * populate a dataset DTO.
     *
     * @param datasetId a dataset id
     * @return a dataset DTO representing the dataset with the given id
     * @throws WebServiceException
     *             thrown for invalid or unauthorized dataset id
     */
    public DatasetDTO datasetDTOForId(Integer datasetId) throws WebServiceException {
        DatasetDTO dto = null;
        Session session = null;

        try {
            session = newSession();

            UserItem user = DataShopInstance.getMasterUser();
            DatasetItem item = findDataset(user, datasetId, session);
            UserItem attachedUser = attach(session, UserItem.class, user);
            if (item.getDeletedFlag() == null || !item.getDeletedFlag()) {
                dto = datasetDTOForItem(attachedUser, item, true);
            }
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return dto;
    }

    /**
     * All of the datasets.
     *
     * @param user
     *            check accessibility for this user
     * @param verbose
     *            provide all the output (if true) or only a subset
     * @return all of the datasets
     */
    public List<DatasetDTO> allDatasetDTOs(UserItem user, boolean verbose) {
        List<DatasetDTO> dtos = new ArrayList<DatasetDTO>();
        Session session = null;

        try {
            session = newSession();

            List<DatasetItem> datasets = attach(session, DatasetItem.class,
                    datasetDao().findUndeletedDatasets());

            addDatasetDTOs(user, verbose, dtos, datasets);
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return dtos;
    }

    /**
     * Translate the dataset items into dataset DTOs.
     *
     * @param user
     *            the user
     * @param verbose
     *            provide all the output (if true) or only a subset
     * @param dtos
     *            the list of DTOs
     * @param datasets
     *            the list of items
     */
    private void addDatasetDTOs(UserItem user, boolean verbose,
            List<DatasetDTO> dtos, List<DatasetItem> datasets) {
        for (DatasetItem dataset : datasets) {
            dtos.add(datasetDTOForItem(user, dataset, verbose));
        }
    }

    /**
     * All of the datasets the user can view (or edit).
     *
     * @param user
     *            the user
     * @param access
     *            editable, viewable or all
     * @param verbose
     *            provide all the output (if true) or only a subset
     * @return all of the datasets the user can view (or edit)
     */
    public List<DatasetDTO> datasetDTOsForUser(UserItem user,
            AccessParam access, boolean verbose) {
        List<DatasetDTO> dtos = new ArrayList<DatasetDTO>();
        Session session = null;

        try {
            session = newSession();

            UserItem attachedUser = attach(session, UserItem.class, user);
            List<DatasetItem> datasets = new ArrayList<DatasetItem>();
            List<ProjectItem> projects = DaoFactory.DEFAULT.getProjectDao()
                    .findAll();

            for (ProjectItem project : projects) {
                project = attach(session, ProjectItem.class, project);
                DatasetDao dsDao = DaoFactory.DEFAULT.getDatasetDao();

                boolean isProjectOrDatashopAdminFlag = user.getAdminFlag();
                if (!isProjectOrDatashopAdminFlag) {
                    AuthorizationDao authDao = DaoFactory.DEFAULT.getAuthorizationDao();
                    isProjectOrDatashopAdminFlag = authDao.isProjectAdmin(user, project);
                }
                if (isAccessible(attachedUser, project, access)) {
                    List<DatasetItem> dsList = new ArrayList<DatasetItem>(
                            dsDao.findByProject(project, isProjectOrDatashopAdminFlag));
                    datasets.addAll(attach(session, DatasetItem.class, dsList));
                }
            }
            addDatasetDTOs(attachedUser, verbose, dtos, datasets);
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return dtos;
    }

    /**
     * Whether datasets in the given project are accessible to the user.
     *
     * @param user
     *            the user
     * @param project
     *            the project for which we want to test access
     * @param access
     *            editable, viewable or all
     * @return whether datasets in the given project are accessible to the user
     */
    private boolean isAccessible(UserItem user, ProjectItem project,
            AccessParam access) {
        String authLevel = getAuth(user, project);
        switch (access) {
        case EDITABLE:
            return (isPublic(project) || "edit".equals(authLevel)
                    || "admin".equals(authLevel) || user.getAdminFlag());
        case VIEWABLE:
            return authLevel != null || isPublic(project) || user.getAdminFlag();
        case ALL:
        default:
            return true;
        }
    }

    /**
     * Translate the sample item into a sample DTO.
     *
     * @param item
     *            the item
     * @param verbose
     *            provide all the output (if true) or only a subset
     * @return the item translated into a DTO
     */
    private SampleDTO sampleDTOForItem(SampleItem item, boolean verbose) {
        SampleDTO dto = new SampleDTO();
        SampleMetricDao metricDao = DaoFactory.DEFAULT.getSampleMetricDao();

        dto.setId((Integer) item.getId());
        dto.setDescription(item.getDescription());
        dto.setName(item.getSampleName());
        if (item.getOwner() != null) {
            dto.setOwner((String) item.getOwner().getId());
        }
        dto.setNumberOfTransactions(metricDao.getTotalTransactions(item));

        if (verbose) {
            for (FilterItem filterItem : item.getFiltersExternal()) {
                FilterDTO filterDto = new FilterDTO();

                filterDto.setColumn(filterItem.getAttribute());
                filterDto.setFilterText(filterItem.getFilterString());
                filterDto.setOperator(filterItem.getOperator());
                dto.addFilter(filterDto);
            }
        }

        return dto;
    }

    /**
     * Find the id of the All Data sample for the dataset.
     *
     * @param user
     *            check accessibility for this user
     * @param datasetId
     *            the dataset id
     * @return the id of the All Data sample for the dataset
     * @throws WebServiceException
     *             for invalid, inaccessible dataset id, or no All Data sample
     *             found.
     */
    public int allDataSampleId(UserItem user, int datasetId)
            throws WebServiceException {
        Session session = null;

        try {
            session = newSession();

            DatasetItem dataset = findDataset(user, datasetId, session);
            SampleItem allData = sampleDao().findOrCreateDefaultSample(dataset);

            if (allData == null) {
                throw new WebServiceException(INVALID_SAMPLE_ERR,
                        "No All Data sample found for dataset.");
            }

            return (Integer) allData.getId();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    /**
     * Get the sample item for the dataset with the given id.
     *
     * @param user
     *            check accessibility for this user
     * @param datasetId
     *            the dataset id
     * @param sampleId
     *            the sample id
     * @param access
     *            the access parameter
     * @param session
     *            the active hibernate session
     * @return the sample item for the dataset with the given id
     * @throws WebServiceException
     *             for invalid, inaccessible sample id
     */
    private SampleItem sampleItemForId(UserItem user, Integer datasetId,
            Integer sampleId, AccessParam access, Session session)
            throws WebServiceException {
        SampleItem item = null;
        DatasetItem datasetItem = findDataset(user, datasetId, session);

        if (datasetItem != null
                && (datasetItem.getDeletedFlag() == null || !datasetItem.getDeletedFlag())) {
            for (SampleItem sample : datasetItem.getSamplesExternal()) {
                if (sample.getId().equals(sampleId)) {
                    item = sample;
                }
            }
        }
        if (item == null) {
            throw new WebServiceException(INVALID_SAMPLE_ERR, "Sample "
                    + sampleId + " is not valid for dataset " + datasetId + ".");
        }
        if (!item.isAccessible(user, access == AccessParam.EDITABLE)) {
            throw new WebServiceException(INACCESSIBLE_SAMPLE_ERR, "Sample "
                    + sampleId + " is not accessible for dataset " + datasetId
                    + ".");
        }

        return item;
    }

    /**
     * Get the sample item for the dataset with the given id.
     *
     * @param user
     *            check accessibility for this user
     * @param datasetId
     *            the dataset id
     * @param sampleId
     *            the sample id
     * @param session
     *            the active hibernate session
     * @return the sample item for the dataset with the given id
     * @throws WebServiceException
     *             for invalid, inaccessible sample id
     */
    private SampleItem sampleItemForId(UserItem user, Integer datasetId,
            Integer sampleId, Session session) throws WebServiceException {
        return sampleItemForId(user, datasetId, sampleId, AccessParam.VIEWABLE,
                session);
    }

    /**
     * Get the sample for the dataset with the given id.
     *
     * @param user
     *            check accessibility for this user
     * @param datasetId
     *            the dataset id
     * @param sampleId
     *            the sample id
     * @param access
     *            the access parameter
     * @param verbose
     *            provide all the output (if true) or only a subset
     * @return the sample as a DTO
     * @throws WebServiceException
     *             for invalid, inaccessible sample id
     */
    public SampleDTO sampleDTOForId(UserItem user, Integer datasetId,
            Integer sampleId, AccessParam access, boolean verbose)
            throws WebServiceException {
        logDebug("sampleId is ", sampleId);
        SampleDTO dto = null;
        SampleItem item = null;
        Session session = null;

        try {
            session = newSession();
            item = sampleItemForId(user, datasetId, sampleId, access, session);
            dto = sampleDTOForItem(item, verbose);
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return dto;
    }

    /**
     * All of the samples the user can view (or edit) for the given dataset.
     *
     * @param user
     *            the user
     * @param datasetId
     *            the dataset id
     * @param access
     *            editable, viewable or all
     * @param verbose
     *            provide all the output (if true) or only a subset
     * @return all of the samples the user can view (or edit) for the given
     *         dataset
     * @throws WebServiceException
     *             thrown for invalid or unauthorized dataset id
     */
    public List<SampleDTO> sampleDTOs(Integer datasetId, UserItem user,
            AccessParam access, boolean verbose) throws WebServiceException {
        List<SampleDTO> dtos = new ArrayList<SampleDTO>();
        Session session = null;

        try {
            session = newSession();

            DatasetItem dataset = findDataset(user, datasetId, session, access);
            if (dataset == null
                || (dataset.getDeletedFlag() != null && dataset.getDeletedFlag())) {
                    return dtos;
            }
            List<SampleItem> samples = attach(session, SampleItem.class,
                    sampleDao().find(dataset, user));

            for (SampleItem sample : samples) {
                if (sample.isAccessible(user, access == AccessParam.EDITABLE)) {
                    dtos.add(sampleDTOForItem(sample, verbose));
                }
            }
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return dtos;
    }

    /**
     * All of the external analyses the user can view for the given dataset.
     * User can do this if he has view access to dataset.
     *
     * @param user the user
     * @param datasetId the dataset id
     * @return all of the external analyses the user can view for the given dataset
     * @throws WebServiceException
     *             thrown for invalid or unauthorized dataset id
     */
    public List<ExternalAnalysisDTO> externalAnalysisDTOs(Integer datasetId,
            UserItem user) throws WebServiceException {
        List<ExternalAnalysisDTO> dtos = new ArrayList<ExternalAnalysisDTO>();
        Session session = null;
        try {
            session = newSession();
            DatasetItem dataset = findDataset(user, datasetId, session,
                    AccessParam.VIEWABLE);
            List<ExternalAnalysisFile> eaList = filesInfoHelper()
                    .getExternalAnalysisList(dataset);
            // attach external file item to session and turn item to DTO
            if (eaList != null && eaList.size() > 0) {
                for (ExternalAnalysisFile eaFile : eaList) {
                    ExternalAnalysisItem eaItem = attach(session,
                            ExternalAnalysisItem.class,
                            eaFile.getExternalAnalysisItem());
                    dtos.add(externalAnalysisDTOForItem(eaItem));
                }
            }
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return dtos;
    }

    /**
     * Get the external analysis for analysis item.
     *
     * @param eaItem
     *            the external analysis item
     * @return the file content as string
     * @throws WebServiceException
     *             for invalid, inaccessible external analysis id
     */
    public String externalAnalysisTextFileContent(ExternalAnalysisItem eaItem)
            throws WebServiceException {
        logDebug("externalAnalysisId is ", eaItem.getId());
        StringBuffer fileContent = new StringBuffer();
        try {
            File theFile = new File(eaItem.getFile().getUrl(baseDir));
            fileContent.append(org.apache.commons.io.FileUtils
                    .readFileToString(theFile, Charset.defaultCharset().toString()));
        } catch (IOException exception) {
            logger.debug("IOException occurred in getting external analysis file content "
                         + eaItem.getFile().getFileName() + "("
                         + eaItem.getFile().getId() + ").",
                         exception);
            throw unknownErrorException();
        }
        return fileContent.toString();
    }

    /**
     * Get the external analysis for analysis item.
     *
     * @param eaItem
     *            the external analysis item
     * @return byte array for binary file
     * @throws WebServiceException
     *             for invalid, inaccessible external analysis id
     */
    public byte[] externalAnalysisBinaryFileContent(ExternalAnalysisItem eaItem)
            throws WebServiceException {
        logDebug("externalAnalysisId is ", eaItem.getId());
        File theFile = new File(eaItem.getFile().getUrl(baseDir));
        byte[] result = new byte[(int) theFile.length()];
        try {
            BufferedInputStream input = null;
            try {
                int totalBytesRead = 0;
                input = new BufferedInputStream(new FileInputStream(theFile));
                while (totalBytesRead < result.length) {
                    int bytesRemaining = result.length - totalBytesRead;
                    int bytesRead = input.read(result, totalBytesRead,
                            bytesRemaining);
                    if (bytesRead > 0) {
                        totalBytesRead = totalBytesRead + bytesRead;
                    }
                }
            } finally {
                input.close();
            }
        } catch (FileNotFoundException ex) {
            logger.debug("FileNotFoundException occurred in getting external analysis file content "
                         + eaItem.getFile().getFileName() + "("
                         + eaItem.getFile().getId() + ").",
                         ex);
            throw unknownErrorException();
        } catch (IOException exception) {
            logger.debug("IOException occurred in getting external analysis file content "
                         + eaItem.getFile().getFileName() + "("
                         + eaItem.getFile().getId() + ").",
                         exception);
            throw unknownErrorException();
        }
        return result;
    }

    /**
     * Delete the external analysis for the dataset with the given analysis id.
     *
     * @param user
     *            check accessibility for this user
     * @param datasetId
     *            the dataset id
     * @param externalAnalysisId
     *            the external analysis id
     * @throws WebServiceException
     *             for invalid, inaccessible external analysis id
     */
    public void externalAnalysisDeleteForId(UserItem user, Integer datasetId,
            Integer externalAnalysisId) throws WebServiceException {
        logDebug("externalAnalysisId is ", externalAnalysisId);
        Session session = null;

        try {
            session = newSession();
            DatasetItem dataset = findDataset(user, datasetId, session,
                    AccessParam.VIEWABLE);
            ExternalAnalysisItem item = externalAnalysisItemForId(user,
                    datasetId, externalAnalysisId, session);
            // check if user is owner
            if (!item.getOwner().equals(user) && !user.getAdminFlag()) {
                throw new WebServiceException(INACCESSIBLE_EXTERNAL_ANALYSIS_ERR,
                                              "Insufficient privileges to delete "
                                              + "external analysis " + item.getId()
                                              + ". You are not the owner.");
            }
            // delete file from file system
            File theFile = new File(item.getFile().getUrl(baseDir));
            if (theFile.exists()) {
                if (theFile.delete()) {
                    // Delete the file from the database
                    filesInfoHelper().deleteExternalAnalysisItem(dataset, item);
                    UserLogger.log(dataset, user,
                            UserLogger.EXTERNAL_ANALYSIS_DELETE,
                            "Deleting externalAnalysis (" + externalAnalysisId
                                    + ") for file "
                                    + item.getFile().getFileName() + " ("
                                    + item.getFile().getId() + ")");
                } else {
                    logger.error("Unable to delete " + theFile.getAbsoluteFile());
                    throw unknownErrorException();
                }
            } else {
                // Delete the file from the database, even though file doesn't
                // exist in the file system
                filesInfoHelper().deleteExternalAnalysisItem(dataset, item);
                logger.warn("Attempting to delete " + theFile.getAbsoluteFile()
                        + " failed as file does not exist");
                throw unknownErrorException();
            }
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    /**
     * Add the external analysis for the dataset.
     * @param dataset DatasetItem for the analysis to be attached to
     * @param dsFileItem FileItem to be created
     * @param eaItem the external analysis item to be created
     * @param content the string content
     * @param createTime the creation time
     * @throws WebServiceException the web service exception
     */
    public void addExternalAnalysis(DatasetItem dataset, FileItem dsFileItem,
            ExternalAnalysisItem eaItem, String content, Date createTime)
            throws WebServiceException {
        String wholePath = baseDir + "/" + dsFileItem.getFilePath();
        // Create the directory
        File newDirectory = new File(wholePath);
        if (newDirectory.isDirectory() || newDirectory.mkdirs()) {
            FileUtils.makeWorldReadable(newDirectory);
            if (logger.isDebugEnabled()) {
                logger.debug("The directory has been created."
                        + newDirectory.getAbsolutePath());
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MMdd_HHmmss");
            String fileName = "ds" + dataset.getId() + "_analysis_"
                    + sdf.format(createTime);
            String fileExt = ".txt";
            // check that file with this name does not already exist.
            // if it does start incrementing adding _1, _2, to the end
            // of the file until a name is found that doesn't exist.
            List<String> fileList = Arrays.asList(newDirectory.list());
            int i = 0;
            String fileNameToSave = fileName + fileExt;
            while (fileList.contains(fileNameToSave)) {
                i++;
                fileNameToSave = fileName + "_" + i + fileExt;
            }
            dsFileItem.setFileName(fileNameToSave);
            // Write the file to the directory
            File newFile = new File(wholePath, fileNameToSave);
            if (logger.isDebugEnabled()) {
                logger.debug("Absolute path is " + newFile.getAbsolutePath());
            }
            try {
                FileWriter fw = new FileWriter(newFile);
                fw.write(content);
                fw.close();
            } catch (IOException iox) {
                logger.debug("IOException occurred in adding an external analysis file "
                             + fileNameToSave + ").",
                             iox);
                throw unknownErrorException();
            }
            // check file size less than 200MB
            if (newFile.length() > MAX_FILE_SIZE) {
                // delete created file
                newFile.delete();
                throw new WebServiceException(INVALID_POST_REQUEST_BODY_ERR,
                        "External analysis file too large.");
            } else {
                eaItem.getFile().setFileSize(newFile.length());
                filesInfoHelper().addExternalAnalysisItem(dataset, eaItem);
            }
        }
    }

    /**
     * Translate the external analysis item into an external analysis DTO.
     *
     * @param item
     *            the analysis item
     * @return the item translated into a DTO
     */
    private ExternalAnalysisDTO externalAnalysisDTOForItem(
            ExternalAnalysisItem item) {
        ExternalAnalysisDTO dto = new ExternalAnalysisDTO();
        dto.setId((Integer) item.getId());
        dto.setKcModelName(item.getSkillModelName());
        dto.setStatisticalModel(item.getStatisticalModel());
        dto.setTitle(item.getFile().getTitle());
        dto.setDescription(item.getFile().getDescription());
        dto.setFileName(item.getFile().getFileName());
        dto.setOwner(item.getOwner().getId().toString());
        dto.setAdded(item.getFile().getAddedTime());
        return dto;
    }

    /**
     * Get the external analysis item for the dataset with the given id.
     *
     * @param user
     *            check accessibility for this user
     * @param datasetId
     *            the dataset id
     * @param externalAnalysisId
     *            the id of the external analysis
     * @param session
     *            the active hibernate session
     * @return the external analysis item for the dataset with the given id
     * @throws WebServiceException
     *             for invalid, inaccessible external analysis id
     */
    private ExternalAnalysisItem externalAnalysisItemForId(UserItem user,
            Integer datasetId, Integer externalAnalysisId, Session session)
            throws WebServiceException {
        ExternalAnalysisItem item = null;
        DatasetItem dataset = findDataset(user, datasetId, session,
                AccessParam.VIEWABLE);

        if (dataset != null) {
            List<ExternalAnalysisFile> eaList = filesInfoHelper()
                    .getExternalAnalysisList(dataset);
            // attach external file item to session and turn item to DTO
            if (eaList != null && eaList.size() > 0) {
                for (ExternalAnalysisFile eaFile : eaList) {
                    ExternalAnalysisItem eaItem = eaFile
                            .getExternalAnalysisItem();
                    if (eaItem.getId().equals(externalAnalysisId)) {
                        item = eaItem;
                    }
                }
            }
        }
        if (item == null) {
            throw new WebServiceException(INVALID_EXTERNAL_ANALYSIS_ERR,
                    "External analysis " + externalAnalysisId
                            + " is not valid for dataset " + datasetId + ".");
        }

        return item;
    }

    /**
     * Get the external analysis item for the dataset with the given id.
     *
     * @param user
     *            check accessibility for this user
     * @param datasetId
     *            the dataset id
     * @param externalAnalysisId
     *            the id of the external analysis
     * @return the external analysis item for the dataset with the given id
     * @throws WebServiceException
     *             for invalid, inaccessible external analysis id
     */
    public ExternalAnalysisItem externalAnalysisItemForId(UserItem user,
            Integer datasetId, Integer externalAnalysisId)
            throws WebServiceException {
        ExternalAnalysisItem item = null;
        Session session = null;

        try {
            session = newSession();
            item = externalAnalysisItemForId(user, datasetId,
                    externalAnalysisId, session);
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return item;
    }

    /**
     * Get the KC Model item for the dataset with the given id.
     * @param user check accessibility for this user
     * @param datasetId the dataset id
     * @param kcModelId the kc model id
     * @throws WebServiceException for invalid, inaccessible external analysis id
     * @return the skill model item
     */
    public SkillModelItem kcModelForId(UserItem user, Integer datasetId,
            Integer kcModelId) throws WebServiceException {
        Session session = null;
        SkillModelItem item = null;
        try {
            session = newSession();
            DatasetItem dataset = findDataset(user, datasetId, session,
                    AccessParam.VIEWABLE);

            List<SkillModelItem> kcItemList = dataset.getSkillModelsExternal();
            if (kcItemList != null && kcItemList.size() > 0) {
                for (SkillModelItem kcModelItem : kcItemList) {
                    if (kcModelItem.getId().toString()
                            .equals(kcModelId.toString())) {
                        item = kcModelItem;
                    }
                }
            }
            if (item == null) {
                throw new WebServiceException(INVALID_PARAM_VAL_ERR,
                        "Invalid value for parameter kc_model: " + kcModelId
                                + ".");
            }
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return item;
    }

    /**
     * Get the KC Model item for the specified dataset and model name.
     * @param user check accessibility for this user
     * @param datasetId the dataset id
     * @param kcModelName the kc model name
     * @throws WebServiceException for invalid, inaccessible external analysis id
     * @return the skill model item
     */
    public SkillModelItem kcModelForName(UserItem user, Integer datasetId,
                                         String kcModelName)
        throws WebServiceException
    {
        Session session = null;
        SkillModelItem item = null;
        try {
            session = newSession();
            DatasetItem dataset = findDataset(user, datasetId, session,
                                              AccessParam.VIEWABLE);

            List<SkillModelItem> kcItemList = dataset.getSkillModelsExternal();
            if (kcItemList != null && kcItemList.size() > 0) {
                for (SkillModelItem kcModelItem : kcItemList) {
                    if (kcModelItem.getSkillModelName().equals(kcModelName)) {
                        item = kcModelItem;
                    }
                }
            }

            if (item == null) {
                throw new WebServiceException(INVALID_PARAM_VAL_ERR,
                                              "Invalid value for parameter kc_model: "
                                              + kcModelName
                                              + ".");
            }
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return item;
    }

    /** Convenience method for getting the sample DAO. @return the sample DAO */
    private SampleDao sampleDao() {
        return DaoFactory.DEFAULT.getSampleDao();
    }

    /**
     * Create a CachedExportFileReader for the sample.
     *
     * @param sample
     *            the sample
     * @return a CachedExportFileReader for the sample
     */
    private CachedExportFileReader cachedFileReader(SampleItem sample) {
        return txExportHelper().cachedFileReaderRaw(sample, baseDir);
    }

    /**
     * Shortcut to the file info helper.
     *
     * @return the transaction export helper
     */
    private FilesInfoHelper filesInfoHelper() {
        return HelperFactory.DEFAULT.getFilesInfoHelper();

    }

    /**
     * Get custom field names for the custom field id given
     *
     * @param datasetId dataset id to get custom fields from
     * @param user UserItem
     * @param customFieldIdList the list of custom field id
     */
    public List<String> cfNames(Integer datasetId, UserItem user, List<String> customFieldIdList)
            throws WebServiceException {
            Session session = null;
            List<String> rows = new ArrayList<String>();
            if (customFieldIdList != null && customFieldIdList.size() > 0) {
                    try {
                        session = newSession();
                        DatasetItem dataset = findDataset(user, datasetId, session,
                                        AccessParam.VIEWABLE);
                        List<CustomFieldItem> cfs = attach(session, CustomFieldItem.class,
                                        DaoFactory.DEFAULT.getCustomFieldDao()
                                        .find(dataset, customFieldIdList));
                        for (CustomFieldItem cfItem : cfs) {
                                rows.add(cfItem.getCustomFieldName());
                        }
                    } finally {
                        if (session != null) {
                            session.close();
                        }
                    }
            }
            return rows;
    }

    /**
     * Get the custom field items for the dataset with the given id.
     *
     * @param user
     *            check accessibility for this user
     * @param datasetId
     *            the dataset id
     * @param customFieldId
     *            the id of the custom field
     * @param session
     *            the active hibernate session
     * @return the custom field item for the dataset with the given id
     * @throws WebServiceException
     *             for invalid, inaccessible custom field id
     */
    private CustomFieldItem customFieldItemForId(UserItem user, Integer datasetId,
                                                 Long customFieldId, Session session)
            throws WebServiceException {
            DatasetItem datasetItem = findDataset(user, datasetId, session);
            return customFieldItemForId(user, datasetItem, customFieldId, session);
    }

    /**
     * Get the custom field items for the dataset given.
     *
     * @param user
     *            check accessibility for this user
     * @param datasetItem
     *            the dataset item
     * @param customFieldId
     *            the id of the custom field
     * @param session
     *            the active hibernate session
     * @return the custom field item for the dataset with the given id
     * @throws WebServiceException for invalid, inaccessible custom field id
     */
    private CustomFieldItem customFieldItemForId(UserItem user,
            DatasetItem datasetItem, Long customFieldId, Session session)
                    throws WebServiceException {
        CustomFieldItem item = null;
        if (datasetItem != null
                && (datasetItem.getDeletedFlag() == null || !datasetItem.getDeletedFlag())) {
            for (CustomFieldItem cfItem : datasetItem.getCustomFieldsExternal()) {
                if (((Long)cfItem.getId()).intValue() == customFieldId.intValue()) {
                    item = cfItem;
                    break;
                }
            }
        }
        if (item == null) {
            throw new WebServiceException(INVALID_CUSTOM_FIELD_FOR_DATASET_ERR,
                    "Custom field " + customFieldId
                    + " is not valid for dataset " + datasetItem.getId() + ".");
        }

        return item;
    }

    /**
     * Translate the custom field item into a custom field DTO.
     *
     * @param item
     *            the item
     * @return the item translated into a DTO
     */
    private CustomFieldDTO customFieldDTOForItem(CustomFieldItem item) {
        CustomFieldDTO dto = new CustomFieldDTO();
        dto.setId((Long)item.getId());
        dto.setDescription(item.getDescription());
        dto.setName(item.getCustomFieldName());
        if (item.getOwner() != null) {
            dto.setOwner((String) item.getOwner().getId());
        }
        dto.setLevel(item.getLevel());

        return dto;
    }

    /**
     * Get the custom field for the dataset with the given id.
     *
     * @param user
     *            check accessibility for this user
     * @param datasetId
     *            the dataset id
     * @param customFieldId
     *            the custom field id
     * @param ownedByUser
     *            get only the ones owned by user
     *
     * @return the custom field as a DTO
     * @throws WebServiceException
     *             for invalid custom field id
     */
    public CustomFieldDTO customFieldDTOForId(UserItem user, Integer datasetId,
            Long customFieldId, boolean ownedByUser)
            throws WebServiceException {
        CustomFieldDTO dto = null;
        CustomFieldItem item = null;
        Session session = null;

        try {
            session = newSession();
            item = customFieldItemForId(user, datasetId, customFieldId, session);
            if (!ownedByUser || item.isOwnedByUser(user)) {
                dto = customFieldDTOForItem(item);
            }
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return dto;
    }

    /**
     * All of the custom field the user can view (or own) for the given dataset.
     *
     * @param user
     *            the user
     * @param datasetId
     *            the dataset id
     * @param ownedByUser
     *            get custom fields owned by user
     *
     * @return all of the custom fields the user can view (or own) for the given
     *         dataset
     * @throws WebServiceException
     *             thrown for invalid or unauthorized dataset id
     */
    public List<CustomFieldDTO> customFieldDTOs(Integer datasetId,
                    UserItem user, boolean ownedByUser)
    throws WebServiceException {
        List<CustomFieldDTO> dtos = new ArrayList<CustomFieldDTO>();
        Session session = null;

        try {
            session = newSession();
            DatasetItem datasetItem = findDataset(user, datasetId, session);
            if (datasetItem != null
                            && (datasetItem.getDeletedFlag() == null
                                            || !datasetItem.getDeletedFlag())) {
                    for (CustomFieldItem cfItem : datasetItem.getCustomFieldsExternal()) {
                            if (!ownedByUser || cfItem.isOwnedByUser(user)) {
                                        dtos.add(customFieldDTOForItem(cfItem));
                            }
                    }
            }
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return dtos;
    }

    /**
     * Add the custom field for the dataset.
     * @param customFieldItem CustomFieldItem to be added
     * @throws WebServiceException the web service exception
     */
    public void addOrModifyCustomField(Integer datasetId, UserItem user,
                    CustomFieldItem customFieldItem) throws WebServiceException {
        Session session = null;
        try {
            session = newSession();
            DatasetItem datasetItem = findDataset(user,
                    datasetId, session, AccessParam.EDITABLE);
            //check if this custom field name is already exists
            boolean exist = false;
            for (CustomFieldItem cfItem : datasetItem.getCustomFieldsExternal()) {
                if (cfItem.getCustomFieldName().trim().equals(
                        customFieldItem.getCustomFieldName().trim())) {
                    if (customFieldItem.getId() == null
                            || !cfItem.getId().equals(customFieldItem.getId())) {
                        exist = true;
                        break;
                    }
                }
            }
            if (!exist) {
                customFieldItem.setDataset(datasetItem);
                boolean newCF = customFieldItem.getId() == null;
                // Create a new file item in the database
                CustomFieldDao cfDao = DaoFactory.DEFAULT.getCustomFieldDao();
                cfDao.saveOrUpdate(customFieldItem);

                // Add the custom field to the given dataset if new else reset
                if (newCF) {
                    datasetItem.addCustomField(customFieldItem);
                } else {
                    for (CustomFieldItem cfItem
                            : datasetItem.getCustomFieldsExternal()) {
                        //update the current custom fields in dataset
                        if (cfItem.getId().equals(customFieldItem.getId())) {
                            cfItem.setCustomFieldName(
                                    customFieldItem.getCustomFieldName());
                            cfItem.setDescription(
                                    customFieldItem.getDescription());
                        }
                    }
                }
                //change dataset system log to schedule export cache
            } else {
                throw paramValueConflictException("Custom field with name "
                        + customFieldItem.getCustomFieldName()
                        + " already exists for this dataset.");
            }
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    /**
     * Parse xml into CustomFieldItem.
     * @param xmlStr xml string to be parsed
     * @return CustomFieldItem
     * @throws WebServiceException the web service exception
     */
    public CustomFieldItem getCustomFieldItemFromXML(String xmlStr) throws WebServiceException {
        CustomFieldItem returnVal = new CustomFieldItem();
        Element messageElement = null;
        try {
            StringReader reader = new StringReader(xmlStr);
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(reader);
            Element root = doc.getRootElement();
            Element cfElement = root.getChild(XMLConstants.CUSTOM_FIELD_ELEMENT);
            if (cfElement == null) {
                logger.error("Invalid XML format: missing root or </custom_field> element.");
                throw new WebServiceException(INVALID_XML_ERR, "Invalid XML format.");
            }
            String idAttr = cfElement.getAttributeValue("id");
            try {
                returnVal.setId(Long.parseLong(idAttr));
            } catch (NumberFormatException nex) {
                //FIXME do something for the NumberFormatException
            }
            //get each element from XML
            Iterator messageIter = cfElement.getChildren().iterator();
            while (messageIter.hasNext()) {
                messageElement = (Element)messageIter.next();
                String elementName = messageElement.getName();
                if (elementName.equals("name")) {
                    returnVal.setCustomFieldName(messageElement.getTextTrim());
                } else if (elementName.equals("description")) {
                    returnVal.setDescription(messageElement.getTextTrim());
                } else if (elementName.equals("level")) {
                    returnVal.setLevel(messageElement.getTextTrim());
                } else if (elementName.equals("owner")) {
                    returnVal.setOwner(new UserItem(messageElement.getTextTrim()));
                }
            }
        } catch (LogException exception) {
            logger.warn("LogException occurred. " + exception.getMessage());
            throw new WebServiceException(INVALID_VALUE_FOR_ELEMENT_ERR,
                                          "Invalid value for element '" + messageElement.getName()
                                          + "': " + messageElement.getTextTrim());
        } catch (JDOMException exception) {
            logger.warn("Invalid XML format: " + exception.getMessage());
            throw new WebServiceException(INVALID_XML_ERR, "Invalid XML format.");
        } catch (IOException exception) {
            logger.warn("IOException occurred. " + exception.getMessage());
            throw unknownErrorException();
        }
        return returnVal;
    }

    /**
     * Delete the custom fields for the dataset with the given custom field id.
     *
     * @param wsUserLog
     *            web service user log
     * @param user
     *            check accessibility for this user
     * @param datasetId
     *            the dataset id
     * @param customFieldId
     *            the custom field id
     * @return the number of transactions associated with the custom field before it was deleted
     * @throws WebServiceException
     *             for invalid, inaccessible custom field id
     */
    public int customFieldDeleteForId(WebServiceUserLog wsUserLog,
            UserItem user, Integer datasetId,
            Long customFieldId) throws WebServiceException {
        logDebug("customFieldId is ", customFieldId);
        Session session = null;
        try {
            session = newSession();
            // If dataset isn't public or user doesn't have at least VIEW access,
            // this operation will fail with INACCESSIBLE_DATASET_ERR.
            DatasetItem dataset = findDataset(user, datasetId, session);
            CustomFieldItem item = customFieldItemForId(user,
                            dataset, customFieldId, session);

            String errorStr = "Insufficient privileges to delete custom field "
                + customFieldId + ". You are not the owner.";

            // Only DS Admin can delete data for system-created CFs.
            if ((item.getOwner().getName().equals(UserItem.SYSTEM_USER))
                && !user.getAdminFlag()) {
                logger.error("Cannot delete system-created CustomFields.");
                throw new WebServiceException(INACCESSIBLE_CUSTOM_FIELD_ERR, errorStr);
            }

            // Now determine if user has permission to set a Custom Field.
            String authLevel = getAuth(user, dataset.getProject());
            if ((authLevel != null) &&
                !(authLevel.equals("edit") || authLevel.equals("admin") || user.getAdminFlag())) {
                throw new WebServiceException(INACCESSIBLE_CUSTOM_FIELD_ERR,
                                              "Insufficient privileges to delete custom field "
                                              + customFieldId + ".");
            }

            // Operation only available to CF owner or DS Admin
            if (!item.isOwnedByUser(user) &&
                !(user.getAdminFlag() || authLevel.equals("admin"))) {
                throw new WebServiceException(INACCESSIBLE_CUSTOM_FIELD_ERR, errorStr);
            }

            //delete custom field
            CustomFieldDao cfDao = DaoFactory.DEFAULT.getCustomFieldDao();
            CfTxLevelDao cfTxLevelDao = DaoFactory.DEFAULT.getCfTxLevelDao();
            int deletedTxnCount = cfTxLevelDao.clear(item);
            cfDao.clear(item);
            dataset.removeCustomField(item);
            UserLogger.logCfDelete(wsUserLog.getDataset(), wsUserLog.getUser(),
                    customFieldId, item.getCustomFieldName(), deletedTxnCount, true);
            return deletedTxnCount;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    /**
     * set custom field values.
     *
     * @param user
     *            check accessibility for this user
     * @param datasetId
     *            the dataset id
     * @param customFieldId
     *            the custom field id
     * @param cfNameValues
     *            the list of name value pairs for transaction GUID and custom field value
     * @return the custom field item
     * @throws WebServiceException
     *             for invalid, inaccessible custom field id, wrong data format
     */
    public CustomFieldItem setCustomFieldValues(UserItem user, Integer datasetId,
            Long customFieldId, List<CustomFieldNameValueItem> cfNameValues)
            throws WebServiceException {
        logDebug("set setCustomFieldValues, customFieldId is ", customFieldId);
        CustomFieldItem cfItem = null;
        Session session = null;
        try {
            session = newSession();
            DatasetItem dataset = findDataset(user, datasetId, session);
            cfItem = customFieldItemForId(user, dataset, customFieldId, session);

            String errorStr = "Insufficient privileges to set custom field "
                + customFieldId + ". You are not the owner.";

            // Cannot set data for system-created CFs.
            if (cfItem.getOwner().getName().equals(UserItem.SYSTEM_USER)) {
                logger.error("Cannot set data for system-created CustomFields.");
                throw new WebServiceException(INACCESSIBLE_CUSTOM_FIELD_ERR, errorStr);
            }

            String authLevel = getAuth(user, dataset.getProject());
            // Operation only available to CF owner or DS or Project Admin
            if (!cfItem.isOwnedByUser(user) &&
                !(user.getAdminFlag() || authLevel.equals("admin"))) {
                logger.error("Only the CF owner or DS or Project Admin may set CF data.");
                throw new WebServiceException(INACCESSIBLE_CUSTOM_FIELD_ERR, errorStr);
            }

            CfTxLevelDao cfTxLevelDao = DaoFactory.DEFAULT.getCfTxLevelDao();

            //process each custom field name value
            for (CustomFieldNameValueItem nameValue : cfNameValues) {
                CfTxLevelItem levelItem = new CfTxLevelItem ();
                //get TransactionItem from dataset item based on GUID
                TransactionDao txDao = DaoFactory.DEFAULT.getTransactionDao();
                TransactionItem txnItem = txDao.findByGUID(nameValue.getName());
                if ((txnItem != null)
                    && (datasetId.equals((Integer)txnItem.getDataset().getId()))) {
                    levelItem.setTransactionExternal(txnItem);
                } else {
                    errorStr = "Unable to find TransactionItem for GUID ["
                        + nameValue.getName() + "] in dataset " + datasetId;
                    logger.error(errorStr);
                    throw invalidDataException(errorStr);
                }

                String cfValue = nameValue.getBigValue();
                if (cfValue == null) {
                    cfValue = nameValue.getValue();
                }

                // Check that the value doesn't exceed size limit.
                if (cfValue.length() > MAX_NUM_CHARS_CF_BIG) {
                    errorStr = "Value for GUID [" + nameValue.getName()
                        + "] exceeds " + MAX_NUM_CHARS_CF_BIG + " characters.";
                    logger.info(errorStr);
                    throw invalidDataException(errorStr);
                }

                levelItem.setValue(cfValue);
                levelItem.setBigValue(nameValue.getBigValue());
                levelItem.setType(nameValue.getType());
                levelItem.setLoggingFlag(false);
                levelItem.setCustomFieldExternal(cfItem);
                //add this new level item to custom field item
                cfTxLevelDao.saveOrUpdate(levelItem);
            }
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return cfItem;
    }

    public List<LearningCurveSkillDetails> getLearningCurveClassification(UserItem user,
                    Integer datasetId, String kcModel, LearningCurveContext lcContext) throws WebServiceException {
            Session session = null;
            List<LearningCurveSkillDetails> lcDetailList = new ArrayList<LearningCurveSkillDetails>();
            try {
                session = newSession();
                DatasetItem dsItem = findDataset(user, datasetId, session, AccessParam.VIEWABLE);
                if (!isDatasetReleased(dsItem) && !user.getAdminFlag()) {
                        throw unreleasedDatasetError(datasetId);
                }
                //get "All Data" sample
                List<SampleItem> sampleList = new ArrayList<SampleItem>();
                for (SampleItem sample : dsItem.getSamplesExternal() ) {
                        if (sample.getSampleName().equals(SampleItem.ALL_TRANSACTIONS_SAMPLE_NAME)) {
                                sampleList.add(sample);
                        }
                }
                List<SkillModelItem> skillModels = new ArrayList<SkillModelItem>();
                if (kcModel != null) {
                        for (SkillModelItem smItem : dsItem.getSkillModelsExternal() ) {
                                if (smItem.getSkillModelName().equals(kcModel)) {
                                        skillModels.add(smItem);
                                }
                        }
                        if (skillModels.size() == 0) {
                                throw new WebServiceException(INVALID_SKILL_MODEL, "Skill model " + kcModel
                                                + " is not valid.");
                        }
                } else {
                        skillModels = dsItem.getSkillModelsExternal();
                }

                //get each skillModel
                for (SkillModelItem skillModelItem : skillModels) {
                      //get all skills for the skill model
                        LearningCurveOptions reportOptions = new LearningCurveOptions();
                        reportOptions.setSelectedMeasure(LearningCurveOptions.ERROR_RATE_TYPE);
                        reportOptions.setPrimaryModel(skillModelItem);
                        reportOptions.setIsViewBySkill(true);
                        List<SkillItem> skills = skillModelItem.getSkillsExternal();
                        LearningCurveDatasetProducer producer = new LearningCurveDatasetProducer(sampleList, reportOptions);
                        for (SkillItem skillItem : skills) {
                                LearningCurveSkillDetails lcDetails = new LearningCurveSkillDetails();
                                lcDetails.setDatasetName(dsItem.getDatasetName());
                                lcDetails.setKcModelName(skillModelItem.getSkillModelName());
                                lcDetails.setKcName(skillItem.getSkillName());
                                if (skillItem.getBeta() != null)
                                        lcDetails.setIntercept(skillItem.getBeta());
                                if (skillItem.getGamma() != null)
                                        lcDetails.setSlope(skillItem.getGamma());
                                List<LearningCurvePoint> graphPoints = producer.getGraphPoints(null, sampleList.get(0), skillItem);
                                if (graphPoints == null || graphPoints.size() == 0) {
                                        lcDetails.setCategory(LearningCurveImage.CLASSIFIED_TOO_LITTLE_DATA);
                                        lcDetails.setNumberOfStepInstances(0);
                                        lcDetails.setNumberOfUniqueStep(0);
                                } else {
                                        //get only valid points
                                        int stepInstCnt = 0;
                                        List<LearningCurvePoint> validPoints = new ArrayList<LearningCurvePoint>(graphPoints);
                                        boolean lowAndFlat = true;
                                        for (Iterator<LearningCurvePoint> pointsIt = graphPoints.iterator(); pointsIt.hasNext();) {
                                                    LearningCurvePoint graphPoint = pointsIt.next();
                                                    if (graphPoint.getStudentsCount() < lcContext.getStudentThreshold()) {
                                                            validPoints.remove(graphPoint);
                                                    } else {
                                                            // Look at errorRate for 'low and flat'
                                                            if (graphPoint.getErrorRates() >= lcContext.getLowErrorThreshold()) {
                                                                lowAndFlat = false;
                                                            }
                                                    }
                                        }
                                        if (validPoints == null || validPoints.size() == 0) {
                                                lcDetails.setCategory(LearningCurveImage.CLASSIFIED_TOO_LITTLE_DATA);
                                                lcDetails.setNumberOfStepInstances(0);
                                                lcDetails.setNumberOfUniqueStep(0);
                                        } else {
                                                long skillId = new Long(skillItem.getId().toString());
                                                lcDetails.setCategory(producer.classifyLearningCurve(lcContext, skillId, validPoints, lowAndFlat));
                                                //go thru each validPoints for unique step
                                                for (LearningCurvePoint thisPoint : validPoints) {
                                                        stepInstCnt += thisPoint.getObservations();
                                                        if (thisPoint.getOpportunityNumber() == 1) {
                                                                lcDetails.setNumberOfUniqueStep(thisPoint.getStepsCount());
                                                                lcDetails.setNumberOfOpportunityOneStepInstances(thisPoint.getObservations());
                                                        }
                                                }
                                                lcDetails.setNumberOfStepInstances(stepInstCnt);
                                        }
                                }
                                lcDetailList.add(lcDetails);
                        }
                }
            } finally {
                if (session != null) {
                    session.close();
                }
            }
            return lcDetailList;
    }

    public List<LearningCurvePoint> getLearningCurvePoints(UserItem user, Integer datasetId,
                                                           String modelName, String skillName)
        throws WebServiceException {

        Session session = null;
        List<LearningCurvePoint> result = new ArrayList<LearningCurvePoint>();
        try {
            session = newSession();
            DatasetItem dsItem = findDataset(user, datasetId, session, AccessParam.VIEWABLE);
            if (!isDatasetReleased(dsItem) && !user.getAdminFlag()) {
                throw unreleasedDatasetError(datasetId);
            }

            //get "All Data" sample
            List<SampleItem> sampleList = new ArrayList<SampleItem>();
            for (SampleItem sample : dsItem.getSamplesExternal() ) {
                if (sample.getSampleName().equals(SampleItem.ALL_TRANSACTIONS_SAMPLE_NAME)) {
                    sampleList.add(sample);
                    break;
                }
            }

            SkillModelItem kcModel = null;
            List<SkillModelItem> skillModels = dsItem.getSkillModelsExternal();
            for (SkillModelItem model : skillModels) {
                if (model.getSkillModelName().equals(modelName)) {
                    kcModel = model;
                    break;
                }
            }

            if (kcModel == null) {
                throw new WebServiceException(INVALID_SKILL_MODEL, "Skill model " + modelName
                                              + " is not valid.");
            }

            SkillItem skill = null;
            List<SkillItem> skills = kcModel.getSkillsExternal();
            for (SkillItem s : skills) {
                if (s.getSkillName().equals(skillName)) {
                    skill = s;
                    break;
                }
            }

            if (skill == null) {
                throw new WebServiceException(INVALID_SKILL, "Skill " + skillName
                                              + " is not valid.");
            }

            LearningCurveOptions reportOptions = new LearningCurveOptions();
            reportOptions.setSelectedMeasure(LearningCurveOptions.ERROR_RATE_TYPE);
            reportOptions.setPrimaryModel(kcModel);
            reportOptions.setIsViewBySkill(true);
        reportOptions.setDisplayLowStakesCurve(getIsHighStakesAvailable(dsItem));
            LearningCurveDatasetProducer producer =
                new LearningCurveDatasetProducer(sampleList, reportOptions);


            result = producer.getGraphPoints(null, sampleList.get(0), skill);

        } finally {
            if (session != null) { session.close(); }
        }

        return result;
    }

    private Boolean getIsHighStakesAvailable(DatasetItem dataset) {
    CustomFieldDao cfDao = DaoFactory.DEFAULT.getCustomFieldDao();
    List<CustomFieldItem> cfList = cfDao.findMatchingByName("highStakes", dataset, true);
    return (cfList.size() > 0);
    }

    public List<SkillModelItem> getSkillModels(UserItem user, Integer datasetId) throws WebServiceException {
            Session session = null;
            List<SkillModelItem> modelList = new ArrayList<SkillModelItem>();
            try {
                session = newSession();
                DatasetItem dsItem = findDataset(user, datasetId, session, AccessParam.VIEWABLE);
                modelList = dsItem.getSkillModelsExternal();
            } finally {
                if (session != null) {
                    session.close();
                }
            }
            return modelList;
    }

    /**
     * Shortcut to KCModel helper.
     *
     * @return the KCModel helper
     */
    private KCModelHelper kcModelHelper() {
        return HelperFactory.DEFAULT.getKCModelHelper();
    }

    public JSONObject verifyAndImportKCMData(Integer datasetId, UserItem user, String importData) throws WebServiceException {
            Session session = null;
            JSONObject verificationResult = null;
            try {
                session = newSession();
                DatasetItem dsItem = findDataset(user, datasetId, session, AccessParam.EDITABLE);
                // Now determine if user has permission to import KCM.
                String authLevel = getAuth(user, dsItem.getProject());
                if (!authLevel.equals("edit") && !authLevel.equals("admin") && !user.getAdminFlag()) {
                    throw new WebServiceException(WebServiceException.INACCESSIBLE_DATASET_ERR,
                                                  "Insufficient privileges to import KCM.");
                }
                if (!isDatasetReleased(dsItem) && !user.getAdminFlag()) {
                        throw unreleasedDatasetError(datasetId);
                }
                verificationResult = kcModelHelper().verifyModelDataForWebService(importData, user, dsItem);
                logger.info("KCM input data verification is successful. " +  verificationResult);
                //create a temp file
                String wholePath = baseDir + "/" + FileUtils.cleanForFileSystem(dsItem.getDatasetName());
                // Create the directory
                File newDirectory = new File(wholePath);
                if (newDirectory.isDirectory() || newDirectory.mkdirs()) {
                    FileUtils.makeWorldReadable(newDirectory);
                    if (logger.isDebugEnabled()) {
                        logger.debug("The directory has been created." + newDirectory.getAbsolutePath());
                    }
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MMdd_HHmmss");
                    String fileName = "ds" + dsItem.getId() + "_KCMImport_" + sdf.format(new Date());
                    String fileExt = ".tmp";
                    // check that file with this name does not already exist.
                    // if it does start incrementing adding _1, _2, to the end
                    // of the file until a name is found that doesn't exist.
                    List<String> fileList = Arrays.asList(newDirectory.list());
                    int i = 0;
                    String fileNameToSave = fileName + fileExt;
                    while (fileList.contains(fileNameToSave)) {
                        i++;
                        fileNameToSave = fileName + "_" + i + fileExt;
                    }
                    // Write the file to the directory
                    File newFile = new File(wholePath, fileNameToSave);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Created tmp file for KCM import, absolute path is " + newFile.getAbsolutePath());
                    }
                    try {
                            FileWriter fw = new FileWriter(newFile);
                            fw.write(importData);
                            fw.close();
                    } catch (IOException iox) {
                        logger.debug("IOException occurred when creating tmp file for KCM import ("
                                     + fileNameToSave + ").",
                                     iox);
                        unknownErrorException();
                    }
                    kcModelHelper().importModelDataForWebService(dsItem, user, newFile, verificationResult, aggregatorSpFilePath, baseDir + "/ssss");

                } else {
                        unknownErrorException();
                }
            } finally {
                    if (session != null) {
                        session.close();
                    }
            }
            return verificationResult;
    }

    /**
     * Shortcut to the transaction export helper.
     *
     * @return the transaction export helper
     */
    private TransactionExportHelper txExportHelper() {
        return HelperFactory.DEFAULT.getTransactionExportHelper();
    }

    /**
     * Iterate over at most limit transactions starting at offset for the
     * sample.
     *
     * @param user
     *            the user
     * @param datasetId
     *            the dataset id
     * @param sampleId
     *            the sample id
     * @param limit
     *            maximum number of rows to read
     * @param offset
     *            index of the first row to read
     * @return a CachedExportFileReader for the sample
     * @throws WebServiceException
     *             for invalid, inaccessible dataset or sample id
     */
    public Iterable<List<String>> transactionRows(UserItem user, int datasetId,
            int sampleId, int limit, int offset) throws WebServiceException {
        Session session = null;
        Iterable<List<String>> rows = new ArrayList<List<String>>();

        try {
            session = newSession();

            SampleItem sample = sampleItemForId(user, datasetId, sampleId,
                    session);
            CachedExportFileReader reader = cachedFileReader(sample);
            DatasetItem dataset = findDataset(user, datasetId, session);
            // DS-Admin can export unreleased datasets.
            if (!isDatasetReleased(dataset) && !user.getAdminFlag()) {
                throw unreleasedDatasetError(datasetId);
            }

            boolean isUpToDate = txExportHelper().isUpToDate(dataset, sample);
            if (isUpToDate && reader != null) {
                rows = reader.rowsIter(limit, offset);
                if (!rows.iterator().hasNext()) {
                    throw invalidParamValueException("offset", offset);
                }
            } else {
                // no cached file, so start up the export
                if (!isUpToDate) {
                    logDebug("Sample is not up to date for sample ", sampleId);
                } else {
                    logDebug("No cached file found for sample ", sampleId);
                }
                TxExportBean txExportBean = HelperFactory.DEFAULT
                        .getTxExportBean();

                txExportBean.setAttributes(asList(sample), dataset, baseDir,
                        (String) user.getId(), txExportSpFilePath);
                txExportBean.setSendEmail(isSendmailActive);
                txExportBean.setEmailAddress(emailAddressDatashopHelp);
                new Thread(txExportBean).start();
                String cacheMsg = reader == null ? NO_TX_CACHE
                        : OUT_OF_DATE_TX_CACHE;
                throw new WebServiceException(NO_CACHED_FILE_ERR, format(
                        cacheMsg, sampleId));
            }

        } catch (IOException ioe) {
            throw invalidParamValueException("offset", offset);
        } catch (IndexOutOfBoundsException iob) {
            throw invalidParamValueException("offset", offset);
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return rows;
    }

    /**
     * Construct a step rollup export task for the sample.
     *
     * @param sample
     *            the sample
     * @return a step rollup export task for the sample
     */
    private StepRollupExportTask stepRollupTask(final SampleItem sample) {
        CachedFileInfo info = new CachedFileInfo(sample.getDataset(), sample,
                baseDir);
        StepRollupExportOptions options =
                new StepRollupExportOptions(sample);
            options.setDisplayAllModels(true);
            options.setExportCachedVersion(true);
        return new StepRollupExportTask(info, options);
    }

    /**
     * Start caching the step rollup if it doesn't exist.
     *
     * @param user
     *            the user
     * @param datasetId
     *            the dataset id
     * @param sampleId
     *            the sample id
     */
    private void cacheStepRollup(final UserItem user, final int datasetId,
            final int sampleId) {
        Session session = null;
        StepRollupExportTask task = null;

        try {
            session = newSession();

            final SampleItem sample = sampleItemForId(user, datasetId,
                    sampleId, session);
            task = stepRollupTask(sample);

            if (!task.isExportStarted()) {
                String existingCachedFileName = task.getCachedFileName();
                StepRollupExportOptions options =
                        new StepRollupExportOptions(sample);
                        options.setDisplayAllModels(true);
                        options.setExportCachedVersion(true);
                task.writeStepRollupExport(null, options,
                        null, null);
                task.createCachedFile();
                task.logExportCompleted(true);
                if (existingCachedFileName != null) {
                    // delete the old cached version
                    new File(existingCachedFileName).delete();
                }
            }
        } catch (Exception e) {
            logger.error("Error creating step rollup export in response to a"
                    + " web services request.", e);
        } finally {
            if (session != null) {
                session.close();
            }
            if (task != null) {
                task.cleanup();
            }
        }
    }

    /**
     * Iterate over at most limit steps starting at offset for the sample.
     *
     * @param user
     *            the user
     * @param datasetId
     *            the dataset id
     * @param sampleId
     *            the sample id
     * @param limit
     *            maximum number of rows to read
     * @param offset
     *            index of the first row to read
     * @return the rows
     * @throws WebServiceException
     *             if something goes wrong
     */
    public Iterable<List<String>> stepRows(final UserItem user,
            final int datasetId, final int sampleId, int limit, int offset)
            throws WebServiceException {
        Session session = null;
        Iterable<List<String>> rows = new ArrayList<List<String>>();

        try {
            session = newSession();

            DatasetItem dataset = findDataset(user, datasetId, session);
            // DS-Admin can export unreleased datasets.
            if (!isDatasetReleased(dataset) && !user.getAdminFlag()) {
                throw unreleasedDatasetError(datasetId);
            }

            final SampleItem sample = sampleItemForId(user, datasetId,
                    sampleId, session);
            final StepRollupExportTask task = stepRollupTask(sample);
            CachedExportFileReader reader = task.cachedFileReader();

            if (task.isUpToDate() && reader != null) {
                try {
                    rows = reader.rowsIter(limit, offset);
                } catch (IndexOutOfBoundsException e) {
                    throw invalidParamValueException("offset", offset);
                }
            } else {
                // no cached file, so start up the export
                logDebug("recaching step file for ", sampleId);
                String cacheMsg = reader == null ? NO_STEP_CACHE
                        : OUT_OF_DATE_STEP_CACHE;
                new Thread(new Runnable() {
                    public void run() {
                        cacheStepRollup(user, datasetId, sampleId);
                    }
                }).start();
                throw new WebServiceException(NO_CACHED_FILE_ERR, format(
                        cacheMsg, sampleId));
            }
        } catch (IOException ioe) {
            throw invalidParamValueException("offset", offset);
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return rows;
    }

    /**
     * Delete the dataset with the given id.
     *
     * @param user check accessibility for this user
     * @param datasetId the dataset id
     * @throws WebServiceException for invalid, inaccessible dataset id
     */
    public void datasetDeleteForId(UserItem user, Integer datasetId)
        throws WebServiceException
    {
        // This should only be called from the DatasetDeleteService, but
        // just in case, confirm that the user is the webservice_request user.
        UserItem remoteRequestsUser = DataShopInstance.getMasterUser();
        if (!user.getId().equals(remoteRequestsUser.getId())) {
            throw new WebServiceException(UNAUTHORIZED_USER_ERR,
                                          "Insufficient privileges to delete "
                                          + "a dataset remotely.");
        }

        DatasetDao dsDao = DaoFactory.DEFAULT.getDatasetDao();
        DatasetItem dataset = dsDao.get(datasetId);
        if ((dataset == null) || isDatasetDeleted(dataset)) {
            throw new WebServiceException(INVALID_DATASET_ERR, "Dataset "
                                          + datasetId
                                          + " is not valid.");
        }

        dsDao.delete(dataset);
    }

    /**
     * Delete the discourse with the given id.
     *
     * @param user check accessibility for this user
     * @param discourseId the discourse id
     * @throws WebServiceException for invalid, inaccessible discourse id
     */
    public void discourseDeleteForId(UserItem user, Long discourseId)
        throws WebServiceException
    {
        // This should only be called from the DiscourseDeleteService, but
        // just in case, confirm that the user is the webservice_request user.
        UserItem remoteRequestsUser = DataShopInstance.getMasterUser();
        if (!user.getId().equals(remoteRequestsUser.getId())) {
            throw new WebServiceException(UNAUTHORIZED_USER_ERR,
                                          "Insufficient privileges to delete "
                                          + "a discourse remotely.");
        }

        DiscourseDao discourseDao = DiscourseDbDaoFactory.DEFAULT.getDiscourseDao();
        DiscourseItem discourse = discourseDao.get(discourseId);
        if ((discourse == null) || isDiscourseDeleted(discourse)) {
            throw new WebServiceException(INVALID_DATASET_ERR,
                                          "Discourse " + discourseId
                                          + " is not valid.");
        }

        discourseDao.delete(discourse);
    }

    /**
     * Find the discourse for the given id.
     * @param user check accessibility for this user
     * @param id the discourse id
     * @param session the current session
     * @param access editable, viewable or all
     * @return the discourse with the id
     * @throws WebServiceException thrown for invalid or unauthorized discourse id
     */
    private DiscourseItem findDiscourse(UserItem user, Long id, Session session,
                                        AccessParam access)
        throws WebServiceException
    {
        DiscourseDao discourseDao = DiscourseDbDaoFactory.DEFAULT.getDiscourseDao();
        DiscourseItem discourse = attach(session, DiscourseItem.class, discourseDao.find(id));
        if ((discourse == null) || isDiscourseDeleted(discourse)) {
            throw new WebServiceException(INVALID_DATASET_ERR, "Discourse " + id
                                          + " is not valid.");
        }

        // Check if this user is authorized for this project, or the project has
        // public access.
        Integer projectId = discourse.getProjectId();
        ProjectItem discourseProject = DaoFactory.DEFAULT.getProjectDao().get(projectId);
        if (!isAccessible(user, discourseProject, access)) {
            throw inaccessibleDiscourseError(id);
        }
        return discourse;
    }

    /**
     * Find the discourse for the given id.
     * @param user check accessibility for this user
     * @param id the discourse id
     * @param session the current session
     * @return the discourse with the id
     * @throws WebServiceException thrown for invalid or unauthorized discourse id
     */
    private DiscourseItem findDiscourse(UserItem user, Long id, Session session)
        throws WebServiceException
    {
        return findDiscourse(user, id, session, AccessParam.VIEWABLE);
    }

    /**
     * Find the discourse for the given id.
     * @param user check accessibility for this user
     * @param id the discourse id
     * @param access editable, viewable or all
     * @return the discourse with the id
     * @throws WebServiceException thrown for invalid or unauthorized discourse id
     */
    protected DiscourseItem findDiscourse(UserItem user, Long id, AccessParam access)
        throws WebServiceException
    {
        Session session = null;
        DiscourseItem item = null;
        try {
            session = newDiscourseSession();
            item = findDiscourse(user, id, session, access);
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return item;
    }

    /**
     * Method to check if discourse has been marked for delete.
     * @param discourseItem the discourse
     * @return boolean indicating availability
     */
    private boolean isDiscourseDeleted(DiscourseItem discourseItem) {
        if (discourseItem == null) { return true; }

        if (discourseItem.getDeletedFlag() == null) {
            return false;
        }

        return discourseItem.getDeletedFlag();
    }

    /**
     * Translate the discourse item into a discourse DTO.
     * @param item the item
     * @param verbose provide all the output (if true) or only a subset
     * @return the item translated into a DTO
     */
    private DiscourseDTO discourseDTOForItem(DiscourseItem item, boolean verbose) {
        logDebug("discourseDTOForItem verbose is ", verbose);
        DiscourseDTO dto = new DiscourseDTO();
        if (item != null && (item.getDeletedFlag() == null || !item.getDeletedFlag())) {
            dto.setId((Long) item.getId());
            dto.setName(item.getName());
            DiscourseDao discourseDao = DiscourseDbDaoFactory.DEFAULT.getDiscourseDao();
            Date startTime = discourseDao.getStartTimeByDiscourse(item);
            Date endTime = discourseDao.getEndTimeByDiscourse(item);
            dto.setDateRange(getDateRangeString(startTime, endTime));
            DUserDao userDao = DiscourseDbDaoFactory.DEFAULT.getDUserDao();
            dto.setNumberOfUsers(userDao.getCountByDiscourse(item));
            DiscoursePartDao discoursePartDao = DiscourseDbDaoFactory.DEFAULT.getDiscoursePartDao();
            dto.setNumberOfDiscourseParts(discoursePartDao.getCountByDiscourse(item));
            ContributionDao contributionDao = DiscourseDbDaoFactory.DEFAULT.getContributionDao();
            dto.setNumberOfContributions(contributionDao.getCountByDiscourse(item));
            DataSourcesDao dsDao = DiscourseDbDaoFactory.DEFAULT.getDataSourcesDao();
            dto.setNumberOfDataSources(dsDao.getCountByDiscourse(item));
            DiscourseRelationDao discourseRelationDao =
                DiscourseDbDaoFactory.DEFAULT.getDiscourseRelationDao();
            dto.setNumberOfRelations(discourseRelationDao.getCountByDiscourse(item));
        }
        return dto;
    }

    /**
     * Fetch the discourse item for the id from the database and use it to
     * populate a discourse DTO.
     *
     * @param discourseId a discourse id
     * @return a discourse DTO representing the discourse with the given id
     * @throws WebServiceException thrown for invalid or unauthorized discourse id
     */
    public DiscourseDTO discourseDTOForId(Long discourseId)
        throws WebServiceException
    {
        DiscourseDTO dto = null;
        Session session = null;

        try {
            session = newDiscourseSession();

            UserItem user = DataShopInstance.getMasterUser();
            DiscourseItem item = findDiscourse(user, discourseId, session);
            if (item.getDeletedFlag() == null || !item.getDeletedFlag()) {
                dto = discourseDTOForItem(item, true);
            }
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return dto;
    }

    /**
     * User does not have the proper access rights for the discourse.
     *
     * @param id the discourse id
     * @return exception encoding attempt at unauthorized discourse access
     */
    private WebServiceException inaccessibleDiscourseError(Long id) {
        return new WebServiceException(INACCESSIBLE_DATASET_ERR, "Discourse "
                                       + id + " is not accessible.");
    }

    /**
     * Convert given list of AuthorizationItems to DTOs and attach to session.
     *
     * @param authItems the list of AuthorizationItems
     * @return list of AuthorizationDTO objects
     */
    public List<AuthorizationDTO> authorizationDTOs(List<AuthorizationItem> authItems) {
        List<AuthorizationDTO> dtos = new ArrayList<AuthorizationDTO>();

        Session session = null;
        try {
            session = newSession();
            // attach the item to the session and convert to DTO
            if ((authItems != null) && (authItems.size() > 0)) {
                for (AuthorizationItem ai : authItems) {
                    AuthorizationItem item = attach(session, AuthorizationItem.class, ai);
                    if (item != null) {
                        dtos.add(authorizationDTOForItem(item));
                    }
                }
            }
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return dtos;
    }

    /**
     * Helper method to create AuthorizationDTO from AuthorizationItem.
     *
     * @param authItem the AuthorizationItem
     * @return the AuthorizationDTO
     */
    private AuthorizationDTO authorizationDTOForItem(AuthorizationItem authItem) {
        if (authItem == null) { return null; }

        AuthorizationDTO result = new AuthorizationDTO();
        result.setUser((String)authItem.getUser().getId());
        result.setUserName(authItem.getUser().getUserName());
        result.setProject(authItem.getProject().getProjectName());
        result.setLevel(authItem.getLevel());
        return result;
    }

    /** Format for the date range method, getDateRangeString. */
    private static FastDateFormat prettyDateFormat = FastDateFormat.getInstance("MMM d, yyyy");

    /**
     * Helper method to generate a string of date ranges given start and end times.
     * @param startTime the starting time
     * @param endTime the ending time
     * @return String the range
     */
    private String getDateRangeString(Date startTime, Date endTime) {
        String dateRangeString = "-";

        if (startTime != null) {
            dateRangeString = prettyDateFormat.format(startTime);
        }
        if ((startTime != null) && (endTime != null)) {
            dateRangeString += " - ";
        }
        if (endTime != null) {
            if (dateRangeString == null) {
                dateRangeString = "";
            }
            dateRangeString += prettyDateFormat.format(endTime);
        }
        return dateRangeString;
    }

    /**
     * Only log if debugging is enabled.
     *
     * @param args
     *            concatenate all arguments into the string to be logged
     */
    public void logDebug(Object... args) {
        LogUtils.logDebug(logger, args);
    }
    
    /**
     * Copied from AbstractServlet
     * Utility to send email with a BCC to address specified by ds-email-bucket.
     * @param fromAddress the sender of the email
     * @param toAddress the recipient of the email
     * @param subject the subject of the email
     * @param message the body of the email
     */
    protected void sendEmail(String fromAddress, String toAddress, String subject, String message) {
            if (DataShopInstance.getIsSendmailActive() != null) {
                    isSendmailActive = DataShopInstance.getIsSendmailActive();
            }
            if (DataShopInstance.getDatashopBucketEmail() != null) {
                    emailAddressDatashopBucket = DataShopInstance.getDatashopBucketEmail();
            }
            if (isSendmailActive) {
                    // Append footer to message if not null.
                    String footer = ServerNameUtils.getFooterForEmail(fromAddress);
                    if (footer != null) {
                        message += footer;
                    }
        
                    // Send email, with BCC to ds-email-bucket.
                    List<String> bccList = new ArrayList<String>();
                    bccList.add(emailAddressDatashopBucket);
        
                    // Use null "Reply To" address.
                    MailUtils.sendEmail(fromAddress, toAddress, null, bccList, subject, message);
        }
    }
}
