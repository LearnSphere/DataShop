/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2012
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.irb;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.comparators.NullComparator;
import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.AuthorizationDao;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.FileDao;
import edu.cmu.pslc.datashop.dao.IrbDao;
import edu.cmu.pslc.datashop.dao.ProjectDao;
import edu.cmu.pslc.datashop.dao.ProjectIrbMapDao;
import edu.cmu.pslc.datashop.dao.ProjectShareabilityHistoryDao;
import edu.cmu.pslc.datashop.dao.UserDao;
import edu.cmu.pslc.datashop.dao.UserRoleDao;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.FileItem;
import edu.cmu.pslc.datashop.item.IrbItem;
import edu.cmu.pslc.datashop.item.ProjectIrbMapId;
import edu.cmu.pslc.datashop.item.ProjectIrbMapItem;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.ProjectShareabilityHistoryItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;
import edu.cmu.pslc.datashop.servlet.project.ProjectPageServlet;


/**
 * Helper to get data from multiple tables in the database for the Project IRB page.
 *
 * @author Cindy Tipper
 * @version $Revision: 11780 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2014-11-25 14:57:55 -0500 (Tue, 25 Nov 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class IrbHelper extends AbstractServlet {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** Default constructor. */
    public IrbHelper() { }

    /**
     * Get list of all IRBs in the system.
     * @param context the IRB context
     * @return a list of IRB DTOs
     */
    public List<IrbDto> getAllIRBs(IrbContext context) {
        IrbDao irbDao = DaoFactory.DEFAULT.getIrbDao();

        List<IrbItem> irbItems = null;

        String searchByString = context.getAllIRBsSearchBy();
        if (searchByString == null) {
            irbItems = irbDao.findAll();
        } else {
            irbItems = irbDao.findAllMatching(searchByString, false);
        }

        List<IrbDto> result = getIrbList(irbItems);

        String sortByColumn = context.getAllIRBsSortByColumn();
        Boolean isAscending = context.isAllIRBsAscending(sortByColumn);

        Comparator<IrbDto> comparator =
            IrbDto.getComparator(IrbDto.getSortByParameters(sortByColumn, isAscending));

        Comparator<IrbDto> nullComparator = new NullComparator(comparator, false);

        Collections.sort(result, nullComparator);

        return result;
    }

    /**
     * Get all Project Review DTOs.
     * @param context the IRB context
     * @param userItem the current user accessing report
     * @return a list of ProjectReview DTOs
     */
    public List<ProjectReviewDto> getAllProjectReviewDtos(IrbContext context, UserItem userItem) {
        IrbReviewFilter filter = context.getReviewFilter();

        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        List<ProjectItem> projectList = new ArrayList<ProjectItem>();
        if (filter == null) {
            projectList = projectDao.findAll();
        } else {
            projectList = projectDao.findAllFiltered(filter);
         }

        List<ProjectReviewDto> result = getProjectReviewList(projectList, context, userItem);

        if ((filter != null) && (filter.getUnreviewedDatasets() != null)) {
            result = filterByUnreviewedDatasets(result, filter);
        }
        if ((filter != null) && (filter.getPublicStr() != null)) {
            result = filterByPublic(result, filter);
        }
        String searchByPiDp = context.getReviewSearchByPiDp();
        if (searchByPiDp != null && searchByPiDp.length() > 0) {
            result = filterByPiDp(result, context.getReviewSearchByPiDp());
        }
        return result;
    }

    /**
     * Get list of all IRBs in the system.
     * @param user the user that created or administers the IRBs
     * @param searchBy the String by which to narrow the IRB title search
     * @return a list of IRB DTOs
     */
    public List<IrbDto> getAllIRBsByUser(UserItem user, String searchBy) {
        IrbDao irbDao = DaoFactory.DEFAULT.getIrbDao();

        List<IrbItem> irbItems = null;

        // DataShop Administrators get access to everything
        if (user.getAdminFlag()) {
            irbItems = irbDao.findAllMatching(searchBy, true);
        } else {
            irbItems = irbDao.findAllMatchingByUser(user, searchBy, true);

            // Now add IRBs attached to project for which user is admin.
            AuthorizationDao authDao = DaoFactory.DEFAULT.getAuthorizationDao();
            List<ProjectItem> projects = authDao.findProjectsByAdmin(user);
            for (ProjectItem p : projects) {
                List<IrbItem> irbs = p.getIrbsExternal();
                for (IrbItem i : irbs) {
                    if ((!irbItems.contains(i)) && matchesSearch(i, searchBy)) { irbItems.add(i); }
                }
            }
        }

        List<IrbDto> result = getIrbList(irbItems);

        return result;
    }

    /**
     * Helper method to determine if an IRB matches a given search criteria.
     * @param item the IRB item
     * @param searchBy the search criteria on which to filter title
     * @return flag indicating match
     */
    private boolean matchesSearch(IrbItem item, String searchBy) {

        if (searchBy.equals("")) { return true; }

        if (item.getTitle().contains(searchBy)) { return true; }

        return false;
    }

    /**
     * Get list of projects for a given IRB.
     * @param irb the IRB item
     * @return a list of Project items
     */
    public List<ProjectItem> getProjectsByIRB(IrbItem irb) {
        ProjectIrbMapDao pimDao = DaoFactory.DEFAULT.getProjectIrbMapDao();
        List<ProjectIrbMapItem> projectIrbMapList = pimDao.findByIrb(irb);

        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();

        List<ProjectItem> result = new ArrayList<ProjectItem>(projectIrbMapList.size());
        for (ProjectIrbMapItem item : projectIrbMapList) {
            ProjectIrbMapId mapId = (ProjectIrbMapId)item.getId();
            ProjectItem projectItem = projectDao.get(mapId.getProjectId());
            result.add(projectItem);
        }

        return result;
    }

    /**
     * Generate an IrbProject DTO given a project item.
     * @param projectItem the project item
     * @param userItem the current user accessing report
     * @return an IrbProject DTO
     */
    public IrbProjectDto getIrbProjectDto(ProjectItem projectItem, UserItem userItem) {
        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        Integer projectId = (Integer)projectItem.getId();
        projectItem = projectDao.get(projectId);
        ProjectShareabilityHistoryDao pshDao =
                DaoFactory.DEFAULT.getProjectShareabilityHistoryDao();
        List<ProjectShareabilityHistoryItem> pshList = pshDao.findByProjectNewestFirst(projectItem);

        // Sort IRB list by Approval Date.
        List<IrbDto> irbList = getIrbList(projectItem.getIrbsExternal());
        Comparator<IrbDto> comparator =
            IrbDto.getComparator(IrbDto.getSortByParameters(IrbDto.COLUMN_APPROVAL, true));
        Comparator<IrbDto> nullComparator = new NullComparator(comparator, false);
        Collections.sort(irbList, nullComparator);

        IrbProjectDto dto = new IrbProjectDto(getProjectReviewDto(projectItem, userItem), irbList,
                                              getProjectShareabilityList(pshList));
        return dto;
    }

    /**
     * Generate an ProjectReview DTO given a project item.
     * @param projectItem the project item
     * @param userItem the current user accessing report
     * @return an ProjectReview DTO
     */
    public ProjectReviewDto getProjectReviewDto(ProjectItem projectItem, UserItem userItem) {
        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        UserDao userDao = DaoFactory.DEFAULT.getUserDao();
        AuthorizationDao authDao = DaoFactory.DEFAULT.getAuthorizationDao();
        DatasetDao dsDao = DaoFactory.DEFAULT.getDatasetDao();

        Integer projectId = (Integer)projectItem.getId();
        projectItem = projectDao.get(projectId);

        UserItem piItem = projectItem.getPrimaryInvestigator();
        if (piItem != null) {
            piItem = userDao.get((String)projectItem.getPrimaryInvestigator().getId());
        }
        UserItem dpItem = projectItem.getDataProvider();
        if (dpItem != null) {
            dpItem = userDao.get((String)projectItem.getDataProvider().getId());
        }
        List<UserItem> paList = authDao.findProjectAdmins(projectId);

        boolean publicFlag = authDao.isPublic(projectId);

        ProjectReviewDto dto = new ProjectReviewDto(projectItem,
                piItem, dpItem, paList, publicFlag);
        dto.setResearchManagersNotes(projectItem.getResearchManagersNotes());

        boolean isProjectOrDatashopAdminFlag = userItem.getAdminFlag();
        if (!isProjectOrDatashopAdminFlag) {
            isProjectOrDatashopAdminFlag = authDao.isProjectAdmin(userItem, projectItem);
        }

        List<DatasetItem> datasets =
                (List<DatasetItem>) dsDao.findByProject(projectItem, isProjectOrDatashopAdminFlag);
        dto.setNumUnreviewedDatasets(countUnreviewedDatasets(datasets));
        dto.setNumDatasets(datasets.size());
        return dto;
    }

    /**
     * Get IRB DTO given only IRB id.
     * @param irbId the IRB id
     * @return an IRB DTO
     */
    public IrbDto getIrbDto(Integer irbId) {
        IrbDao irbDao = DaoFactory.DEFAULT.getIrbDao();
        IrbItem item = irbDao.get(irbId);
        return getIrbDto(item);
    }

    /**
     * Convert an IRB item to a DTO.
     * @param item the IRB item
     * @return an IRB DTO
     */
    public IrbDto getIrbDto(IrbItem item) {
        IrbDao irbDao = DaoFactory.DEFAULT.getIrbDao();
        Integer irbId = (Integer)item.getId();
        item = irbDao.get(irbId);
        IrbDto dto = new IrbDto(irbId, item.getTitle(), item.getProtocolNumber(),
                                item.getPi(), item.getApprovalDate(),
                                item.getExpirationDate(), item.getExpirationDateNa(),
                                item.getGrantingInstitution());
        dto.setNotes(item.getNotes());
        dto.setAddedTime(item.getAddedTime());
        dto.setUpdatedTime(item.getUpdatedTime());
        dto.setNumProjects(item.getProjectsExternal().size());
        dto.setFileList(getIrbFileList(item.getFilesExternal()));

        // Initialize UserItems
        UserDao userDao = DaoFactory.DEFAULT.getUserDao();
        UserItem addedBy = userDao.get((String)item.getAddedBy().getId());
        dto.setAddedBy(addedBy);
        UserItem updatedBy = userDao.get((String)item.getUpdatedBy().getId());
        dto.setUpdatedBy(updatedBy);
        return dto;
    }

    /**
     * Convert a project shareability history item to a DTO.
     * @param item the ProjectShareabilityHistory item
     * @return a ProjectShareabilityhistory DTO
     */
    public ProjectShareabilityHistoryDto getProjectShareabilityDto(
            ProjectShareabilityHistoryItem item) {
        ProjectShareabilityHistoryDao pshDao =
                DaoFactory.DEFAULT.getProjectShareabilityHistoryDao();
        Integer pshId = (Integer)item.getId();
        item = pshDao.get(pshId);

        // Initialize UserItem
        UserDao userDao = DaoFactory.DEFAULT.getUserDao();
        UserItem updatedBy = userDao.get((String)item.getUpdatedBy().getId());

        ProjectShareabilityHistoryDto dto =
                new ProjectShareabilityHistoryDto(pshId, (Integer)item.getProject().getId(),
                        updatedBy, item.getUpdatedTime(), item.getShareableStatus());
        return dto;
    }

    /**
     * Helper method to update project shareability status.
     * Only make changes if there is a change in the value
     * @param newSRS new shareability review status
     * @param userItem User affecting change
     * @param projectId project id
     * @param baseUrl the base Datashop URL
     * @return IrbProjectDto updated IRB project DTO
     */
    public IrbProjectDto updateShareabilityStatus(String newSRS, UserItem userItem,
                                                  Integer projectId, String baseUrl) {
        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        ProjectItem projectItem = projectDao.get(projectId);

        if (newSRS.equals(ProjectItem.SHAREABLE_STATUS_SHAREABLE)
                || newSRS.equals(ProjectItem.SHAREABLE_STATUS_SHAREABLE_NOT_PUBLIC)
                || newSRS.equals(ProjectItem.SHAREABLE_STATUS_NOT_SHAREABLE)) {
            projectItem.setNeedsAttention(false);

            String logMsg = "User " + userItem.getId() + " changed SRS to " + newSRS;
            logMsg += ". Project ";
            logMsg += projectItem.getProjectName() + " (" + projectItem.getId() + "). ";
            logMsg += "Needs Attention:  No.";
            logger.info(logMsg);
        }
        projectItem.setShareableStatus(newSRS);
        projectItem.setUpdatedBy(userItem);
        projectItem.setUpdatedTime(new Date());
        projectDao.saveOrUpdate(projectItem);

        ProjectShareabilityHistoryDao pshDao =
                DaoFactory.DEFAULT.getProjectShareabilityHistoryDao();

        ProjectShareabilityHistoryItem item = new ProjectShareabilityHistoryItem();
        item.setProject(projectItem);
        item.setUpdatedBy(userItem);
        item.setUpdatedTime(projectItem.getUpdatedTime());
        item.setShareableStatus(newSRS);
        pshDao.saveOrUpdate(item);

        IrbProjectDto irbProjectDto = getIrbProjectDto(projectItem, userItem);
        irbProjectDto.setMessageLevel(IrbProjectDto.STATUS_MESSAGE_LEVEL_SUCCESS);

        // send email unless SRS is set to a determined state (shareable or not shareable)
        if (newSRS.equals(ProjectItem.SHAREABLE_STATUS_SHAREABLE)
         || newSRS.equals(ProjectItem.SHAREABLE_STATUS_SHAREABLE_NOT_PUBLIC)
         || newSRS.equals(ProjectItem.SHAREABLE_STATUS_NOT_SHAREABLE)) {
            sendShareabilityStatusChangeEmail(projectItem, baseUrl);
            irbProjectDto.setMessage("Shareability Review Status updated. Email sent.");
        } else {
            irbProjectDto.setMessage("Shareability Review Status updated. No email sent.");
        }

        return irbProjectDto;
    }

    /**
     * Helper method for generating email following a change to the shareability status.
     * @param project the Project Item
     * @param baseUrl the base Datashop URL
     */
    private void sendShareabilityStatusChangeEmail(ProjectItem project, String baseUrl) {

        // Get a list of Project Administrators for this project.
        AuthorizationDao authDao = DaoFactory.DEFAULT.getAuthorizationDao();
        List<UserItem> projectAdmins = authDao.findProjectAdmins((Integer)project.getId());

        StringBuffer subject = new StringBuffer();
        subject.append("Shareability review status changed for project '");
        subject.append(project.getProjectName());
        subject.append("'");

        String shareabilityStatus = project.getShareableStatus();
        String shareabilityStatusDisplay =
                ProjectItem.getShareabilityStatusString(shareabilityStatus);

        StringBuffer dearBuffer = new StringBuffer("");
        dearBuffer.append("<!DOCTYPE html><html lang=\"en\">");
        dearBuffer.append("<body>");
        dearBuffer.append("<p>Dear ");

        StringBuffer messageBuffer = new StringBuffer("");
        messageBuffer.append(",</p>");
        messageBuffer.append("<p>");
        messageBuffer.append("We are notifying you that the shareability review status ");
        messageBuffer.append("of your project \"");
        messageBuffer.append("<a href=\"");
        messageBuffer.append(baseUrl);
        messageBuffer.append("/" + ProjectPageServlet.SERVLET + "?id=" + project.getId());
        messageBuffer.append("\">");
        messageBuffer.append(project.getProjectName());
        messageBuffer.append("</a>");
        messageBuffer.append("\" has been set to \"");
        messageBuffer.append(shareabilityStatusDisplay);
        messageBuffer.append("\". ");
        if (shareabilityStatus.equals("shareable")) {
            messageBuffer.append("You may now freely share this project with ");
            messageBuffer.append("others outside of your research team.");
        } else {
            messageBuffer.append("You may receive an additional message from the ");
            messageBuffer.append("research manager explaining this change.");
            messageBuffer.append("</p>");
            messageBuffer.append("The shareability review process for DataShop projects ");
            messageBuffer.append("(required of any project whose owner wishes to share ");
            messageBuffer.append("it) is described ");
            messageBuffer.append("<a href=\"");
            messageBuffer.append(baseUrl);
            messageBuffer.append("/help?page=irb");
            messageBuffer.append("\">here</a>");
            messageBuffer.append(". If you do not wish to share this project, you can ");
            messageBuffer.append("ignore this message.");
        }
        messageBuffer.append("</p>");
        messageBuffer.append("If you have any questions or concerns, please don't hesitate ");
        messageBuffer.append("to contact the DataShop research manager at ");
        messageBuffer.append("<a href=\"mailto:");
        messageBuffer.append(getEmailAddressDatashopRM());
        messageBuffer.append("\">");
        messageBuffer.append(getEmailAddressDatashopRM());
        messageBuffer.append("</a>");
        messageBuffer.append("</p>");
        messageBuffer.append("<p>Thanks,<br />");
        messageBuffer.append("The DataShop Team</p>");
        messageBuffer.append("</body></html>");

        UserDao userDao = DaoFactory.DEFAULT.getUserDao();
        for (UserItem pa: projectAdmins) {
            pa = userDao.get((String)pa.getId());

            if ((pa.getEmail() == null) || (pa.getEmail().length() == 0)) { continue; }

            if (isSendmailActive()) {
                StringBuffer sb = new StringBuffer(dearBuffer);
                sb.append(pa.getName());
                sb.append(messageBuffer);

                sendEmail(getEmailAddressDatashopHelp(), pa.getEmail(),
                          subject.toString(), sb.toString());
            }
        }
    }

    /**
     * Get list of the files associated with an IRB.
     * @param item the IRB item
     * @return a list of IRB file DTOs
     */
    public List<IrbFileDto> getIrbFileList(IrbItem item) {
        return getIrbFileList(item.getFilesExternal());
    }

    /**
     * Helper method to establish map between newly-added IRB and project.
     * @param irb the IRB item
     * @param project the Project item
     * @param user the User adding IRB to Project
     */
    public void addIRBToProject(IrbItem irb, ProjectItem project, UserItem user) {

        ProjectIrbMapDao pimDao = DaoFactory.DEFAULT.getProjectIrbMapDao();
        ProjectIrbMapItem pimItem = new ProjectIrbMapItem();
        pimItem.setIrbExternal(irb);
        pimItem.setProjectExternal(project);
        pimItem.setAddedBy(user);
        pimItem.setAddedTime(new Date());
        pimDao.saveOrUpdate(pimItem);

        // Update IRB "updated*" fields
        IrbDao irbDao = DaoFactory.DEFAULT.getIrbDao();
        irb.setUpdatedBy(user);
        irb.setUpdatedTime(pimItem.getAddedTime());
        irbDao.saveOrUpdate(irb);

        // Update Project "updated*" fields
        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        project.setUpdatedBy(user);
        project.setUpdatedTime(pimItem.getAddedTime());
        projectDao.saveOrUpdate(project);
    }

    /**
     * Helper method to remove an IRB from a project.
     * @param irb the IRB item
     * @param project the Project item
     * @param user the User adding IRB to Project
     */
    public void removeIRBFromProject(IrbItem irb, ProjectItem project, UserItem user) {

        ProjectIrbMapDao pimDao = DaoFactory.DEFAULT.getProjectIrbMapDao();
        ProjectIrbMapId pimId = new ProjectIrbMapId(project, irb);
        ProjectIrbMapItem pimItem = pimDao.find(pimId);
        pimDao.delete(pimItem);

        // Update IRB and Project "updated*" fields
        IrbDao irbDao = DaoFactory.DEFAULT.getIrbDao();
        irb.setUpdatedBy(user);
        irb.setUpdatedTime(new Date());
        irbDao.saveOrUpdate(irb);

        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        project.setUpdatedBy(user);
        project.setUpdatedTime(irb.getAddedTime());
        projectDao.saveOrUpdate(project);
    }

    /**
     * Helper method to remove an IRB from a project.
     * @param irb the IRB item
     * @param user the User adding IRB to Project
     * @param baseDir the directory structure base
     */
    public void deleteIRB(IrbItem irb, UserItem user, String baseDir) {

        IrbDao irbDao = DaoFactory.DEFAULT.getIrbDao();
        irb = irbDao.get((Integer)irb.getId());

        // Remove files associated with IRB.
        FileDao fileDao = DaoFactory.DEFAULT.getFileDao();
        Set<FileItem> files = irb.getFiles();
        for (FileItem f : files) {
            String fileName = f.getFileName();
            String wholePath = baseDir + File.separator + f.getFilePath();
            File theFile = new File(wholePath, fileName);
            if (theFile.exists()) {
                if (theFile.delete()) {
                    fileDao.delete(f);
                }
            }
        }

        // DS1549.
        if (user.equals(irb.getAddedBy())) {
            List<ProjectItem> projectList = irb.getProjectsExternal();
            if (projectList.size() == 1) {
                removeIRBFromProject(irb, projectList.get(0), user);
            }
        }

        irbDao.delete(irb);
    }

    /**
     * Helper method to remove a file from an IRB.
     * @param irb the IRB item
     * @param file the File item
     * @param user the User adding IRB to Project
     */
    public void deleteFileFromIRB(IrbItem irb, FileItem file, UserItem user) {

        FileDao fileDao = DaoFactory.DEFAULT.getFileDao();
        Integer fileId = (Integer)file.getId();
        file = fileDao.get(fileId);

        IrbDao irbDao = DaoFactory.DEFAULT.getIrbDao();
        Integer irbId = (Integer)irb.getId();
        irb = irbDao.get(irbId);

        fileDao.delete(file);

        irb.removeFile(file);
        irb.setUpdatedBy(user);
        irb.setUpdatedTime(new Date());
        irbDao.saveOrUpdate(irb);

    }

    /**
     * Helper method to establish map between newly-added file and IRB.
     * @param file the File item
     * @param irb the IRB item
     * @param user the User adding IRB to Project
     */
    public void addFileToIRB(FileItem file, IrbItem irb, UserItem user) {
        FileDao fileDao = DaoFactory.DEFAULT.getFileDao();
        Integer fileId = (Integer)file.getId();
        file = fileDao.get(fileId);

        IrbDao irbDao = DaoFactory.DEFAULT.getIrbDao();
        Integer irbId = (Integer)irb.getId();
        irb = irbDao.get(irbId);

        file.addIrb(irb);
        fileDao.saveOrUpdate(file);

        irb.addFile(file);
        irb.setUpdatedBy(user);
        irb.setUpdatedTime(file.getAddedTime());
        irbDao.saveOrUpdate(irb);
    }

    /**
     * Helper method for generating email following an addition of an IRB to the system.
     * @param userItem the currently logged in user
     * @param projectItem the Project Item
     * @param baseUrl the base Datashop URL
     * @param irbItem the IRB Item
     */
    void sendIrbAddedEmail(UserItem userItem, ProjectItem projectItem,
            String baseUrl, IrbItem irbItem) {

        // Do not send email if the user has the research manager role.
        UserRoleDao userRoleDao = DaoFactory.DEFAULT.getUserRoleDao();
        if (userRoleDao.hasResearchManagerRole(userItem)) {
            logger.info("Research Manager added an IRB. Not sending email.");
            return;
        }

        // Otherwise, send it!
        StringBuffer subject = new StringBuffer();
        subject.append("IRB added for project '");
        subject.append(projectItem.getProjectName());
        subject.append("'");

        StringBuffer messageBuffer = new StringBuffer("");
        messageBuffer.append("<!DOCTYPE html><html lang=\"en\">");
        messageBuffer.append("<body>");
        messageBuffer.append("<p>Dear DataShop Research Manager,</p>");

        messageBuffer.append("<p>");
        messageBuffer.append("We are notifying you that the user ");
        messageBuffer.append(userItem.getName());
        messageBuffer.append(" has added an IRB to the project ");
        messageBuffer.append("<a href=\"");
        messageBuffer.append(baseUrl);
        messageBuffer.append("/" + ProjectPageServlet.SERVLET + "?id=" + projectItem.getId());
        messageBuffer.append("\">");
        messageBuffer.append(projectItem.getProjectName());
        messageBuffer.append("</a>");
        messageBuffer.append(". ");
        messageBuffer.append("</p>");
        messageBuffer.append("<p>");
        messageBuffer.append("You can review the changes on the project's ");
        messageBuffer.append("<a href=\"");
        messageBuffer.append(baseUrl);
        messageBuffer.append("/" + IrbServlet.SERVLET + "?id=" + projectItem.getId());
        messageBuffer.append("\">");
        messageBuffer.append("IRB page");
        messageBuffer.append("</a>");
        messageBuffer.append(".");
        messageBuffer.append("</p>");

        messageBuffer.append("<p>");
        messageBuffer.append("IRB Protocol Number: ");
        messageBuffer.append(irbItem.getProtocolNumber());
        messageBuffer.append("<br>");
        messageBuffer.append("Title: ");
        messageBuffer.append(irbItem.getTitle());
        messageBuffer.append("<br>");
        messageBuffer.append("IRB PI: ");
        messageBuffer.append(irbItem.getPi());
        messageBuffer.append("<br>");
        messageBuffer.append("IRB Approval Date: ");
        messageBuffer.append(irbItem.getApprovalDateStr());
        messageBuffer.append("<br>");
        messageBuffer.append("IRB Expiration Date: ");
        if (irbItem.getExpirationDateNa()) {
            messageBuffer.append(IrbItem.NOT_APPLICABLE);
        } else {
            messageBuffer.append(irbItem.getExpirationDateStr());
        }
        messageBuffer.append("<br>");
        messageBuffer.append("Granting Institution: ");
        messageBuffer.append(irbItem.getGrantingInstitution());
        messageBuffer.append("</p>");

        messageBuffer.append("<p>Thanks,<br />");
        messageBuffer.append("The DataShop Team</p>");
        messageBuffer.append("</body></html>");

        if (isSendmailActive()) {
            sendEmail(getEmailAddressDatashopHelp(), getEmailAddressDatashopRM(),
                    subject.toString(), messageBuffer.toString());
        }
    }

    /**
     * Helper method to submit a project for review.
     * @param project the Project item
     * @param user the User adding IRB to Project
     * @param baseUrl the base Datashop URL
     */
    public void submitProjectForReview(ProjectItem project, UserItem user, String baseUrl) {
        // send email
        sendSubmitForReviewEmail(project, user, baseUrl);

        updateShareabilityStatus(ProjectItem.SHAREABLE_STATUS_SUBMITTED_FOR_REVIEW,
                user, (Integer)project.getId(), baseUrl);
    }

    /**
     * Helper method for generating email following 'submit for review' request.
     * @param project the Project Item
     * @param user the User Item
     * @param baseUrl the base Datashop URL
     */
    private void sendSubmitForReviewEmail(ProjectItem project, UserItem user, String baseUrl) {

        if (!isSendmailActive()) { return; }

        StringBuffer subject = new StringBuffer();
        subject.append("Shareability review submitted for project '");
        subject.append(project.getProjectName());
        subject.append("'");

        StringBuffer messageBuffer = new StringBuffer("");

        messageBuffer.append("<!DOCTYPE html><html lang=\"en\">");
        messageBuffer.append("<body>");

        messageBuffer.append("<p>Dear DataShop Research Manager,</p>");
        messageBuffer.append("<p>");
        messageBuffer.append("We are notifying you that the user ");
        messageBuffer.append(user.getName());
        messageBuffer.append(" has submitted the project \"");
        messageBuffer.append("<a href=\"");
        messageBuffer.append(baseUrl);
        messageBuffer.append("/" + ProjectPageServlet.SERVLET + "?id=" + project.getId());
        messageBuffer.append("\">");
        messageBuffer.append(project.getProjectName());
        messageBuffer.append("</a>");
        messageBuffer.append("\" for shareability review. ");
        messageBuffer.append("You can review this project and any associated documentation ");
        messageBuffer.append("<a href=\"");
        messageBuffer.append(baseUrl);
        messageBuffer.append("/ProjectIRB?id=");
        messageBuffer.append((Integer)project.getId());
        messageBuffer.append("\">here.</a>");
        messageBuffer.append("</p>");
        messageBuffer.append("<p>");
        messageBuffer.append("Feel free to email this user directly with any questions: ");
        messageBuffer.append("<a href=\"mailto:");
        messageBuffer.append(user.getEmail());
        messageBuffer.append("\">");
        messageBuffer.append(user.getEmail());
        messageBuffer.append("</a>");
        messageBuffer.append("</p>");
        messageBuffer.append("<p>Thanks,<br />");
        messageBuffer.append("The DataShop Team</p>");
        messageBuffer.append("</body></html>");

        sendEmail(getEmailAddressDatashopHelp(), getEmailAddressDatashopRM(),
                  subject.toString(), messageBuffer.toString());
    }

    /**
     * Helper method to generate a list of ProjectReview DTOs given a list of Project items.
     * @param projectList list of Project items
     * @param context the IrbContext to determine sorting and searchBy
     * @param userItem the current user accessing report
     * @return a List of ProjectReview DTOs
     */
    private List<ProjectReviewDto> getProjectReviewList(List<ProjectItem> projectList,
                                                        IrbContext context, UserItem userItem) {

        String searchByString = context.getReviewSearchBy().toLowerCase();

        List<ProjectReviewDto> result = new ArrayList<ProjectReviewDto>(projectList.size());
        for (ProjectItem item : projectList) {
            if (searchByString == null) {
                result.add(getProjectReviewDto(item, userItem));
            } else if (item.getProjectName().toLowerCase().contains(searchByString)) {
                result.add(getProjectReviewDto(item, userItem));
            }
        }

        String sortByColumn = context.getReviewSortByColumn();
        Boolean isAscending = context.isReviewAscending(sortByColumn);

        Comparator<ProjectReviewDto> comparator =
            ProjectReviewDto.getComparator(ProjectReviewDto.
                    getSortByParameters(sortByColumn, isAscending));

        Comparator<ProjectReviewDto> nullComparator = new NullComparator(comparator, false);

        Collections.sort(result, nullComparator);

        return result;
    }

    /**
     * Helper method to filter list of ProjectReview DTOs by 'Unreviewed Datasets'.
     * @param projectList list of ProjectReview DTOs
     * @param filter the IrbReviewFilter
     * @return a filtered List of ProjectReview DTOs
     */
    private List<ProjectReviewDto> filterByUnreviewedDatasets(List<ProjectReviewDto> projectList,
                                                              IrbReviewFilter filter) {

        String unreviewedStr = filter.getUnreviewedDatasets();

        List<ProjectReviewDto> result = new ArrayList<ProjectReviewDto>(projectList.size());
        for (ProjectReviewDto dto : projectList) {
            if (dto.getNumDatasets() == 0) { continue; }
            if ((unreviewedStr.equals("none"))
                && (dto.getNumUnreviewedDatasets() == 0)) {
                result.add(dto);
            } else if ((unreviewedStr.equals("all"))
                       && (dto.getNumUnreviewedDatasets().equals(dto.getNumDatasets()))) {
                result.add(dto);
            } else if ((unreviewedStr.equals("some"))
                       && (dto.getNumUnreviewedDatasets() != 0)
                       && (!dto.getNumUnreviewedDatasets().equals(dto.getNumDatasets()))) {
                result.add(dto);
            }
        }

        return result;
    }

    /**
     * Helper method to filter list of ProjectReview DTOs by the public/private field.
     * @param projectList list of ProjectReview DTOs
     * @param filter the IrbReviewFilter
     * @return a filtered List of ProjectReview DTOs
     */
    private List<ProjectReviewDto> filterByPublic(List<ProjectReviewDto> projectList,
                                                  IrbReviewFilter filter) {
        String publicStr = filter.getPublicStr();

        List<ProjectReviewDto> result = new ArrayList<ProjectReviewDto>(projectList.size());

        for (ProjectReviewDto dto : projectList) {
            if ((publicStr.equalsIgnoreCase("public")) && (dto.isPublicFlag())) {
                result.add(dto);
            } else if ((publicStr.equalsIgnoreCase("private")) && (!dto.isPublicFlag())) {
                result.add(dto);
            }
        }

        return result;
    }

    /**
     * Helper method to filter list of ProjectReview DTOs by the PI/DP field, ignoring case.
     * @param projectList list of ProjectReview DTOs
     * @param search the string the user wants to search the PI and DP fields for
     * @return a filtered List of ProjectReview DTOs
     */
    private List<ProjectReviewDto> filterByPiDp(List<ProjectReviewDto> projectList,
                                                String search) {
        List<ProjectReviewDto> result = new ArrayList<ProjectReviewDto>(projectList.size());

        String lowerSearch = search.toLowerCase();

        for (ProjectReviewDto dto : projectList) {
            String piDp = dto.getPiDpSearchString();
            if (piDp != null && piDp.toLowerCase().contains(lowerSearch)) {
                result.add(dto);
            }
        }

        return result;
    }

    /**
     * Helper method to generate a list of IrbFile DTOs given a list File items.
     * @param fileList list of File items
     * @return a List of IrbFile DTOs
     */
    private List<IrbFileDto> getIrbFileList(List<FileItem> fileList) {
        List<IrbFileDto> dtoList = new ArrayList<IrbFileDto>(fileList.size());
        for (FileItem item : fileList) {
            IrbFileDto dto = getIrbFileDto(item);
            dtoList.add(dto);
        }
        return dtoList;
    }

    /**
     * Convert a file item to an IrbFile DTO.
     * @param item the File item
     * @return an IrbFile DTO
     */
    public IrbFileDto getIrbFileDto(FileItem item) {
        FileDao fileDao = DaoFactory.DEFAULT.getFileDao();
        Integer fileId = (Integer)item.getId();
        item = fileDao.get(fileId);
        item.hashCode();
        item.getOwner().hashCode();
        IrbFileDto dto = new IrbFileDto((Integer)item.getId(), item.getFileName(),
                                        item.getFileSize(), item.getDisplayFileSize(),
                                        item.getAddedTime(), item.getOwner());
        return dto;
    }

    /**
     * Helper method to generate a list of IRB DTOs given a list IRB items.
     * @param itemList list of IRB items
     * @return a List of IRB DTOs
     */
    private List<IrbDto> getIrbList(List<IrbItem> itemList) {
        List<IrbDto> dtoList = new ArrayList<IrbDto>(itemList.size());
        for (IrbItem item : itemList) {
            IrbDto dto = getIrbDto(item);
            dtoList.add(dto);
        }
        return dtoList;

    }

    /**
     * Helper method to generate a list of project shareability DTOs
     * given a list project shareability items.
     * @param itemList list of ProjectShareabilityHistory items
     * @return a List of ProjectShareabilityHistory DTOs
     */
    private List<ProjectShareabilityHistoryDto> getProjectShareabilityList(
            List<ProjectShareabilityHistoryItem> itemList) {

        List<ProjectShareabilityHistoryDto> dtoList =
                new ArrayList<ProjectShareabilityHistoryDto>(itemList.size());
        for (ProjectShareabilityHistoryItem psh : itemList) {
            dtoList.add(getProjectShareabilityDto(psh));
        }
        return dtoList;
    }

    /**
     * Constant for AppearsAnonymousFlag for datasets not yet reviewed.
     */
    private static final String NOT_REVIEWED = "not_reviewed";

    /**
     * Helper method to determine how many of the given datasets
     * are in the "Not Reviewed" state.
     * @param datasets list of datasets to check
     * @return Integer the count
     */
    private Integer countUnreviewedDatasets(List<DatasetItem> datasets) {

        int count = 0;
        for (DatasetItem item : datasets) {
            String appearsAnonymous = item.getAppearsAnonymous();
            if (appearsAnonymous == null) { continue; }
            if (appearsAnonymous.equals(NOT_REVIEWED)) { count++; }
        }

        return new Integer(count);
    }
}
