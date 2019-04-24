/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2014
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.importqueue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.AuthorizationDao;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.DomainDao;
import edu.cmu.pslc.datashop.dao.FileDao;
import edu.cmu.pslc.datashop.dao.ImportQueueDao;
import edu.cmu.pslc.datashop.dao.LearnlabDao;
import edu.cmu.pslc.datashop.dao.ProjectDao;
import edu.cmu.pslc.datashop.discoursedb.dao.DiscourseDbDaoFactory;
import edu.cmu.pslc.datashop.discoursedb.dao.DiscourseDao;
import edu.cmu.pslc.datashop.discoursedb.item.DiscourseItem;
import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.AuthorizationItem;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.DomainItem;
import edu.cmu.pslc.datashop.item.FileItem;
import edu.cmu.pslc.datashop.item.ImportQueueItem;
import edu.cmu.pslc.datashop.item.LearnlabItem;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.item.UserItem;

import static edu.cmu.pslc.datashop.util.FileUtils.updateFilePermissions;

/**
 * Helper class to facilitate dataset uploads, as well as the sample to dataset feature.
 *
 * @author Mike Komisin
 * @version $Revision:  $
 * <BR>Last modified by: $Author:  $
 * <BR>Last modified on: $Date: $
 * <!-- $KeyWordsOff: $ -->
 */
public class UploadDatasetHelper {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** Session attribute. */
    public static final String ATTRIB_IQ_ADDED = "iq_added_item_msg";
    /** Session attribute. */
    public static final String MSG_IQ_ADDED = "Dataset added to import queue.";

    /** Sub-path for the location of the user dataset upload files. */
    public static final String SUB_PATH = "user_uploads";

    /**
     * Check for errors in the user input.
     * @param dto the UploadDatasetDto
     * @return true if any errors occur, false otherwise
     */
    public boolean checkForErrors(UploadDatasetDto dto) {
        boolean errors = false;
        if (dto.getDataFileErrorFlag()
                || dto.getSessionDataErrorFlag()) {
            errors = true;
        }

        Boolean isDiscourse = false;

        //if project group is new, then check for name is set and unique
        if (UploadDatasetDto.PROJ_NEW.equals(dto.getProjectSelection())) {
            dto.setNewProjectNameErrorFlag(false);
            String projectName = dto.getNewProjectName();
            if (projectName == null || projectName.length() == 0) {
                dto.setNewProjectNameErrorFlag(true);
                errors = true;
            } else {
                ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
                Collection<ProjectItem> list = projectDao.find(projectName);
                if (list.size() > 0) {
                    dto.setNewProjectNameErrorFlag(true);
                    errors = true;
                }
            }
            // TBD: Currently don't support Discourse in PROJ_NEW option.
        } else if (UploadDatasetDto.PROJ_EXIST.equals(dto.getProjectSelection())) {
            dto.setExistingProjectIdErrorFlag(false);
            Integer projectId = dto.getExistingProjectId();
            if (projectId == null || projectId == 0) {
                dto.setExistingProjectIdErrorFlag(true);
                errors = true;
            } else {

                ProjectItem projectItem = DaoFactory.DEFAULT.getProjectDao().get(projectId);
                if (projectItem != null) {
                    dto.setNewProjectName(projectItem.getProjectName());
                    isDiscourse = projectItem.getIsDiscourseDataset();
                } else {
                    dto.setExistingProjectIdErrorFlag(true);
                    errors = true;
                }
            }
        }

        //check dataset name for uniqueness
        dto.setDatasetNameErrorFlag(false);
        String datasetName = dto.getDatasetName();
        if (datasetName == null || datasetName.length() == 0
                || datasetName.length() > DatasetItem.DATASET_NAME_MAX_LEN) {
            dto.setDatasetNameErrorFlag(true);
            errors = true;
        } else {
            if ((isDiscourse == null) || !isDiscourse) {
                DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
                Collection datasetList = datasetDao.find(datasetName);
                if (datasetList.size() > 0) {
                    dto.setDatasetNameErrorFlag(true);
                    errors = true;
                }
            } else {
                DiscourseDao discourseDao = DiscourseDbDaoFactory.DEFAULT.getDiscourseDao();
                DiscourseItem found = discourseDao.findByName(datasetName);
                if (found != null) {
                    dto.setDatasetNameErrorFlag(true);
                    errors = true;
                }
            }
        }
        return errors;
    }


    /**
     * Create an Import Queue Item from info in the Upload Dataset DTO.
     * @param dto The Upload Dataset DTO which contains all the info on the upload and the results
     * @param owner the current user
     * @param now now
     * @param status status of the import queue item
     * @param baseDir the path to the DataShop base directory
     * @param numFFIVerifyLines the number of lines to use in FFI verification
     * @param whether or not to process KC Model columns for this import
     * @return the Import Queue Item
     */
    public ImportQueueItem createImportQueueItem(UploadDatasetDto dto,
            UserItem owner, Date now, String status, String baseDir, Integer numFFIVerifyLines,
                Boolean includeKCMs) {

        boolean isFilesOnlyDataset = false;
        if (status.equals(ImportQueueItem.STATUS_NO_DATA)) {
            isFilesOnlyDataset = true;
        }

        // get the existing project or create a new one
        ProjectItem projectItem = getOrCreateProject(dto, owner, now);
        if (dto.getProjectSelection().equals(UploadDatasetDto.PROJ_NEW)) {
            dto.setExistingProjectId((Integer)projectItem.getId());
        }

        ImportQueueDao iqDao = DaoFactory.DEFAULT.getImportQueueDao();

        ImportQueueItem iqItem = new ImportQueueItem();

        // If importQueueItemId already set in DTO, we're reusing it...
        if (dto.getImportQueueItemId() != null) {
            Integer iqId = dto.getImportQueueItemId();
            iqItem = iqDao.get(iqId);
        }

        // create/populate IQ item
        iqItem.setDatasetName(dto.getDatasetName());
        iqItem.setDescription(dto.getDatasetDesccription());
        iqItem.setUploadedBy(owner);
        iqItem.setUploadedTime(now);
        iqItem.setLastUpdatedTime(now);
        iqItem.setFormat(dto.getFormat());
        iqItem.setAnonFlag(dto.getAnonymizedFlag());
        iqItem.setStatus(status);
        if (dto.getProjectSelection().equals(UploadDatasetDto.PROJ_CURRENT)
                || dto.getProjectSelection().equals(UploadDatasetDto.PROJ_NEW)
                || dto.getProjectSelection().equals(UploadDatasetDto.PROJ_EXIST)) {
            iqItem.setProject(projectItem);
        }
        iqItem.setDomainName(dto.getDomainName());
        iqItem.setLearnlabName(dto.getLearnlabName());
        iqItem.setStudyFlag(dto.getHasStudyData());
        iqItem.setFromExistingDatasetFlag(dto.getFromExistingFlag());
        iqItem.setIncludeUserKCMs(includeKCMs);
        // save IQ item
        iqDao.saveOrUpdate(iqItem);

        // get the IQ id and save to DTO
        Integer iqId = (Integer)iqItem.getId();
        dto.setImportQueueItemId(iqId);

        // get file item, which should already be saved to database
        if (!isFilesOnlyDataset) {
            FileDao fileDao = DaoFactory.DEFAULT.getFileDao();
            FileItem fileItem = fileDao.get(dto.getFileItemId());
            boolean result = renameFile(fileItem, iqId, dto.getFormat(), baseDir, numFFIVerifyLines);

            if (!result) {
                dto.setDataFileErrorFlag(true);
            }

            iqItem.setFile(fileItem);
            iqDao.saveOrUpdate(iqItem);
        }

        return iqItem;
    }

    /**
     * Used to create a dataset from an existing sample.
     * @param userItem the user item
     * @param fileItem the file item
     * @param datasetItem the dataset item
     * @param sampleItem the sample item
     * @return
     */
    public UploadDatasetDto getUploadDatasetDto(UserItem userItem, FileItem fileItem,
            DatasetItem datasetItem, SampleItem sampleItem,
            String newDatasetName, String newDatasetDesc) {
        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        DatasetDao dsDao = DaoFactory.DEFAULT.getDatasetDao();
        DomainDao domainDao = DaoFactory.DEFAULT.getDomainDao();
        LearnlabDao learnlabDao = DaoFactory.DEFAULT.getLearnlabDao();
        DomainItem domainItem = null;
        if (datasetItem.getDomain() != null) {
            domainItem = domainDao.get((Integer) datasetItem.getDomain().getId());
        } else {
            domainItem = domainDao.findByName("Other");
        }

        LearnlabItem learnlabItem = null;
        if (datasetItem.getLearnlab() != null) {
            learnlabItem = learnlabDao.get((Integer) datasetItem.getLearnlab().getId());
        } else {
            learnlabItem = learnlabDao.findByName("Other");
        }

        Boolean datasetNameExists = true;
        List<DatasetItem> datasetExists = dsDao.find(newDatasetName);
        if (datasetExists == null || datasetExists.isEmpty()) {
            datasetNameExists = false;
        }

        UploadDatasetDto uploadDatasetDto = null;
        ProjectItem projectItem = projectDao.get((Integer) datasetItem.getProject().getId());
        if (projectItem != null && !datasetNameExists) {
            // Getting properties from the existing dataset and project.
            uploadDatasetDto = new UploadDatasetDto();
            uploadDatasetDto.setProjectSelection(UploadDatasetDto.PROJ_CURRENT);
            uploadDatasetDto.setExistingProjectId((Integer)projectItem.getId());
            uploadDatasetDto.setNewProjectName(projectItem.getProjectName());
            uploadDatasetDto.setProjectSelection(UploadDatasetDto.PROJ_EXIST);
            uploadDatasetDto.setAnonymizedFlag(true);
            uploadDatasetDto.setFormat(ImportQueueItem.FORMAT_TAB);
            uploadDatasetDto.setDatasetName(newDatasetName);
            uploadDatasetDto.setDatasetDesccription(newDatasetDesc);
            uploadDatasetDto.setDomainName(domainItem.getName());
            uploadDatasetDto.setLearnlabName(learnlabItem.getName());
            uploadDatasetDto.setHasStudyData(datasetItem.getStudyFlag());
            uploadDatasetDto.setFileItemId((Integer) fileItem.getId());
        }

        return uploadDatasetDto;
    }

    /**
     * Get or create a project item.
     * @param dto The Upload Dataset DTO which contains all the info on the upload and the results
     * @param owner the current user
     * @param now now
     * @return the project item just created
     */
    private ProjectItem getOrCreateProject(UploadDatasetDto dto, UserItem owner, Date now) {
        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        ProjectItem projectItem = null;
        if (dto.getProjectSelection().equals(UploadDatasetDto.PROJ_NEW)) {
            projectItem = new ProjectItem();
            projectItem.setProjectName(dto.getNewProjectName());
            projectItem.setCreatedBy(owner);
            projectItem.setCreatedTime(now);
            projectItem.setDataCollectionType(dto.getDataCollectionType());

            // Trac 314: make the user the PI.
            projectItem.setPrimaryInvestigator(owner);

            projectDao.saveOrUpdate(projectItem);

            //Make the creator of a project a Project Admin automatically
            AuthorizationDao authDao = DaoFactory.DEFAULT.getAuthorizationDao();
            AuthorizationItem authItem = new AuthorizationItem(owner, projectItem);
            authItem.setLevel(AuthorizationItem.LEVEL_ADMIN);
            authDao.saveOrUpdate(authItem);

            // Get newly-created projectItem
            Collection pList = projectDao.find(dto.getNewProjectName());
            if (pList.size() > 0) {
                projectItem = (ProjectItem)(pList.toArray())[0];

                //Log to dataset user log table
                String info = "Project: '" + projectItem.getProjectName()
                    + "' (" + projectItem.getId() + "): Created successfully.";
                UserLogger.log(null, owner, UserLogger.PROJECT_CREATE, info);
                logger.debug(info);

            } else {
                // Should never happen, but... what else should go here?
                logger.error("Failed to create new project: " + dto.getNewProjectName());
            }

        } else if (dto.getProjectSelection().equals(UploadDatasetDto.PROJ_EXIST)) {
            projectItem = projectDao.get((Integer)dto.getExistingProjectId());
            logger.debug("Existing Project: " + projectItem.getProjectName());
        } else if (dto.getProjectSelection().equals(UploadDatasetDto.PROJ_CURRENT)) {
            projectItem = projectDao.get((Integer)dto.getExistingProjectId());
            logger.debug("Current Project: " + projectItem.getProjectName());
        }
        return projectItem;
    }


    /**
     * Utility method to rename the uploaded file now that we have an
     * id for the ImportQueue item and, if appropriate, create a shortened
     * version of the file for the mini-verify.
     * @param fileItem the FileItem
     * @param iqId the ImportQueueItem id
     * @param fileFormat the format of the uploaded file
     * @param baseDir the path to the DataShop base directory
     * @param numFFIVerifyLines the number of lines to use in FFI verification
     * @return boolean indicating success
     */
    private boolean renameFile(FileItem fileItem, Integer iqId, String fileFormat,
            String baseDir, Integer numFFIVerifyLines) {

        if (fileItem == null) { return true; }

        String existingPath = baseDir + File.separator + SUB_PATH;
        File existingFile = new File(existingPath, fileItem.getFileName());

        String newPath = existingPath + File.separator + iqId;
        File newDirectory = new File(newPath);
     // If the directory doesn't exist, create it.
        if (newDirectory.mkdirs()) {
            edu.cmu.pslc.datashop.util.FileUtils.makeWorldReadable(newDirectory);
        }
        if (newDirectory.exists()) {
            // If the directory exists, delete its contents.
            String filePath = baseDir + File.separator + SUB_PATH + File.separator + iqId;
            File[] files = new File(filePath).listFiles();
            for (File file : files) {
                if (file.delete()) {
                    logger.info("Deleted file " + file.getName());
                } else {
                    logger.info("Failed to delete file " + file.getName());
                }
            }
        }
        // Make new directory world readable and executable
        updateFilePermissions(newDirectory, "chmod 775");

        File newFile = new File(newPath, fileItem.getFileName());
        existingFile.renameTo(newFile);

        // Make new file world +r
        boolean permissionsSet = updateFilePermissions(newFile, "chmod 664");

        // If permissions cannot be set for some reason, return
        if (!permissionsSet) {
            return false;
        }

        // Update 'path' in FileItem
        fileItem.setFilePath(SUB_PATH + File.separator + iqId);
        DaoFactory.DEFAULT.getFileDao().saveOrUpdate(fileItem);

        boolean result = true;
        if (fileFormat.equals(ImportQueueItem.FORMAT_TAB)) {
            result = createVersionForVerify(fileItem, newFile, newPath, numFFIVerifyLines);
        }

        return result;
    }


    /**
     * Utility method to create a short version of the file for verification.
     * @param fileItem the FileItem
     * @param theFile the File to read from
     * @param thePath the path of the directory where new version is created
     * @param numFFIVerifyLines the number of lines to use in FFI verification
     * @return boolean indicating success
     */
    private boolean createVersionForVerify(FileItem fileItem, File theFile,
            String thePath, Integer numFFIVerifyLines) {

        boolean result = true;

        try {
            String origFileName = fileItem.getFileName();

            // Create a 'short' version of the file (for verification) which
            // is the first N lines of the uploaded file.
            LineIterator it = null;
            File tmpFile = null;

            // If given a zip file, extract first file for verification...
            if (edu.cmu.pslc.datashop.util.FileUtils.isZipFile(fileItem.getFileType())) {
                ZipFile zipFile = new ZipFile(theFile);
                ZipInputStream zin =
                    new ZipInputStream(new FileInputStream(theFile.getAbsolutePath()));
                ZipEntry zipEntry = zin.getNextEntry();
                while (zipEntry != null) {

                    if (zipEntry.isDirectory()) {
                        zipEntry = zin.getNextEntry();
                        continue;
                    }

                    String zipEntryName = zipEntry.getName();
                    int lastIndex = zipEntryName.lastIndexOf("/");
                    if (lastIndex == -1) { lastIndex = zipEntryName.lastIndexOf("\\"); }
                    if (lastIndex > 0) {
                        origFileName = zipEntryName.substring(lastIndex + 1);
                    } else {
                        origFileName = zipEntryName;
                    }
                    // Replace one or more undesirable characters with a single underscore
                    origFileName = origFileName.replaceAll(
                        UploadDatasetServlet.BAD_FILEPATH_CHARS, "_");

                    tmpFile = new File(thePath, origFileName);
                    FileOutputStream fos = new FileOutputStream(tmpFile);
                    InputStream is = zipFile.getInputStream(zipEntry);
                    edu.cmu.pslc.datashop.util.FileUtils.copyStream(is, fos);

                    it = FileUtils.lineIterator(tmpFile, null);

                    // break out of loop
                    zipEntry = null;
                }
            } else {
                it = FileUtils.lineIterator(theFile, null);
            }

            if (it != null) {
                List<String> lines = new ArrayList<String>();
                try {
                    int count = 0;
                    while (it.hasNext() && (count++ <= numFFIVerifyLines)) {
                        lines.add(it.nextLine());
                    }
                } finally {
                    it.close();
                }
                String shortFileName = ImportQueueHelper.HEAD_FILE_PREFIX + origFileName;
                File shortFile = new File(thePath, shortFileName);
                FileUtils.writeLines(shortFile, null, lines);
                updateFilePermissions(shortFile, "chmod 664");
            }

            // If tmpFile created (read from zip) remove it.
            if (tmpFile != null) { tmpFile.delete(); }

        } catch (IOException ioe) {
            logger.error("Failed to create shortened version of file to verify: " + ioe);
            result = false;
        }

        return result;
    }
}
