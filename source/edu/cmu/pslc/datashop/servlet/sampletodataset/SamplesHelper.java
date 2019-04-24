/**
 *
 */
package edu.cmu.pslc.datashop.servlet.sampletodataset;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.AuthorizationDao;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.FilterDao;
import edu.cmu.pslc.datashop.dao.ImportQueueDao;
import edu.cmu.pslc.datashop.dao.SampleDao;
import edu.cmu.pslc.datashop.dao.SampleHistoryDao;
import edu.cmu.pslc.datashop.dao.SampleMetricDao;
import edu.cmu.pslc.datashop.dao.TransactionDao;
import edu.cmu.pslc.datashop.dao.UserDao;
import edu.cmu.pslc.datashop.helper.DatasetState;
import edu.cmu.pslc.datashop.item.AuthorizationItem;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.FilterItem;
import edu.cmu.pslc.datashop.item.ImportQueueItem;
import edu.cmu.pslc.datashop.item.SampleHistoryItem;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.item.UserItem;

/**
 * Facilitate the upload of datasets from Upload to Dataset.
 *
 * @author
 * @version $Revision: 11453 $
 * <BR>Last modified by: $Author:  $
 * <BR>Last modified on: $Date:  $
 * <!-- $KeyWordsOff: $ -->
 */
public class SamplesHelper {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());
    /** Pencil icon. */
    private String PENCIL_ICON =
        "<img src=\"images/pencil.png\" alt=\"Rename\" title=\"Rename Sample\">";
    /** Edit icon. */
    private String EDIT_ICON =
        "<img src=\"images/edit.gif\" alt=\"Edit\" title=\"Edit Sample\">";
    /** Delete icon. */
    private String DELETE_ICON =
        "<img src=\"images/delete.gif\" alt=\"Delete\" title=\"Delete Sample\">";
    /** Save as Dataset icon. */
    public static String SAVE_ICON =
        "<img src=\"images/table_add.png\" alt=\"Save\" title=\"Save Sample as Dataset\">";
    /** Regular expression used to check email addresses. */
    public static final String EMAIL_PATTERN =
        "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
        + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    /**
     * Find the sample row info for global samples and those owned by the user. Access to the
     * dataset is checked every time this method is called.
     * @param userItem the user item
     * @param datasetid the dataset id
     * @return a List of objects
     */
    public List<SampleRowDto> getSampleRowInfo(UserItem userItem, Long datasetId) {
        List<SampleRowDto> sampleRows = null;
        DatasetItem datasetItem = null;
        String authLevel = null;

        DatasetDao dsDao = DaoFactory.DEFAULT.getDatasetDao();
        SampleDao sampleDao = DaoFactory.DEFAULT.getSampleDao();
        SampleMetricDao smDao = DaoFactory.DEFAULT.getSampleMetricDao();
        TransactionDao txDao = DaoFactory.DEFAULT.getTransactionDao();
        AuthorizationDao authDao = DaoFactory.DEFAULT.getAuthorizationDao();
        SampleHistoryDao sampleHistoryDao = DaoFactory.DEFAULT.getSampleHistoryDao();

        if (datasetId != null) {
            datasetItem = dsDao.get(datasetId.intValue());
            authLevel = authDao.getAuthLevel(userItem, datasetItem);
        }

        // Get out of here if any input params are bad


        // Get sample rows if ds admin or logged in user.
        if (userItem.getAdminFlag()) {
            sampleRows = sampleDao.getDsAdminSampleRowInfo(datasetId);
        } else {
            sampleRows = sampleDao.getSampleRowInfo(
                (String) userItem.getId(), datasetId);
        }

        for (SampleRowDto row : sampleRows) {

            // Add sample rows only for logged in users
            if (!userItem.getId().equals(UserItem.DEFAULT_USER)) {

                // Add actionable icon field
                StringBuffer icons = new StringBuffer("<span class=\"icons\">");

                icons.append("<input type=\"hidden\" id=\"sampleName_" + row.getSampleId());
                icons.append("\" value=\"" + row.getSampleName() + "\" />");

                if (userItem.getAdminFlag()) {
                    // DS Admin
                    icons.append("<a href=\"javascript:s2dEditSample('" +
                    + row.getSampleId() + "');\">" + EDIT_ICON + "</a>");
                    if (!row.getSampleName().equals("All Data")) {
                        icons.append("<a href=\"javascript:s2dDeleteSample('" +
                            + row.getSampleId() + "');\">" + DELETE_ICON + "</a>");
                        row.setPencilIcon("<a href=\"javascript:s2dEditSample('" +
                                + row.getSampleId() + "');\">" + PENCIL_ICON + "</a>");
                    }
                    icons.append("<a href=\"javascript:s2dSaveSample('" +
                            + row.getSampleId() + "', true);\">" + SAVE_ICON + "</a>");




                } else if (userItem.getId().equals(row.getOwnerId())) {
                    // User is sample owner
                    if (authLevel != null
                            && (authLevel.equals(AuthorizationItem.LEVEL_ADMIN)
                            || authLevel.equals(AuthorizationItem.LEVEL_EDIT))) {
                        // User has Project Admin, Edit, or DS Admin
                        icons.append("<a href=\"javascript:s2dEditSample('" +
                                + row.getSampleId() + "');\">" + EDIT_ICON + "</a>");
                        if (!row.getSampleName().equals("All Data")) {
                            icons.append("<a href=\"javascript:s2dDeleteSample('" +
                                        + row.getSampleId() + "');\">" + DELETE_ICON + "</a>");
                            row.setPencilIcon("<a href=\"javascript:s2dEditSample('" +
                                    + row.getSampleId() + "');\">" + PENCIL_ICON + "</a>");
                        }

                        if (authLevel.equals(AuthorizationItem.LEVEL_ADMIN)) {
                            icons.append("<a href=\"javascript:s2dSaveSample('" +
                                + row.getSampleId() + "', true);\">" + SAVE_ICON + "</a>");
                        }



                    }
                } else if (!userItem.getId().equals(row.getOwnerId())) {

                    // User is not sample owner
                    if ((authLevel != null
                            && (authLevel.equals(AuthorizationItem.LEVEL_ADMIN)
                            || authLevel.equals(AuthorizationItem.LEVEL_EDIT)))) {

                        icons.append("<a href=\"javascript:s2dEditSample('" +
                        + row.getSampleId() + "');\">" + EDIT_ICON + "</a>");
                        // User has Project Admin or Edit

                        if (authLevel.equals(AuthorizationItem.LEVEL_ADMIN)) {
                            icons.append("<a href=\"javascript:s2dSaveSample('" +
                                + row.getSampleId() + "', true);\">" + SAVE_ICON + "</a>");
                        }
                    }
                }

                icons.append("</span>");
                row.setActionableIcons(icons.toString());

                SampleItem sampleItem = sampleDao.get(row.getSampleId());
                boolean requiresAgg = DatasetState.requiresAggregation(datasetItem, sampleItem);
                row.setRequiresAgg(requiresAgg);
                // Has sample history?
                List hasSampleHistory = sampleHistoryDao.find(sampleItem);
                if (hasSampleHistory != null && !hasSampleHistory.isEmpty()) {
                    row.hasSampleHistory(true);
                } else {
                    row.hasSampleHistory(false);
                }
                // Add sample metric info to the item.
                addMetricInfo(sampleItem, row, smDao, txDao);
            }
        }
        return sampleRows;
    }

    /**
     * Returns the sampleHistory for a sample.
     * @param sampleItem the sample item
     * @return a list of sample history items
     */
    public List<SampleHistoryDto> getSampleHistoryRows(SampleItem sampleItem) {
        SampleHistoryDao sampleHistoryDao = DaoFactory.DEFAULT.getSampleHistoryDao();
        List<SampleHistoryItem> sampleHistoryItems = sampleHistoryDao.find(sampleItem);
        List<SampleHistoryDto> sampleHistoryDtos = new ArrayList<SampleHistoryDto>();
        DatasetDao dsDao = DaoFactory.DEFAULT.getDatasetDao();
        ImportQueueDao iqDao = DaoFactory.DEFAULT.getImportQueueDao();
        UserDao userDao = DaoFactory.DEFAULT.getUserDao();

        for (SampleHistoryItem historyItem : sampleHistoryItems) {
            DatasetItem datasetItem = dsDao.get((Integer) historyItem.getDataset().getId());
            if (datasetItem != null) {

                SampleHistoryDto sampleHistoryDto = new SampleHistoryDto();
                sampleHistoryDto.setUserId(historyItem.getUserId());
                if (historyItem.getUserId() != null && userDao.find(historyItem.getUserId()) != null) {
                    Pattern pattern = Pattern.compile(EMAIL_PATTERN);

                    UserItem userItem = userDao.get(historyItem.getUserId());
                    // Set the userLink to display the username, for starters.
                    String nameAndId = getFormattedUserName(userItem, true);
                    if (nameAndId == null) {
                        nameAndId = new String();
                    }

                    // Because getEmail could return a non-email string, we use drop-through logic
                    // in the next two conditional (if) blocks, Block 1 and Block 2.
                    if (userItem != null) {
                        // Block 1
                        sampleHistoryDto.setUserLink(nameAndId);
                    }
                    // If email matches pattern, then display the username with mailto link, instead.
                    if (userItem != null && userItem.getEmail() != null) {
                        // Block 2
                        Matcher matcher = pattern.matcher(userItem.getEmail());
                        if (matcher.matches()) {
                            String userLink = "<a href=\"mailto:" + userItem.getEmail() + "\" >"
                                + getFormattedUserName(userItem, true) + "</a>";
                            sampleHistoryDto.setUserLink(userLink);
                        }
                    }
                }

                sampleHistoryDto.setDatasetId((Integer) datasetItem.getId());
                sampleHistoryDto.setDatasetName(datasetItem.getDatasetName());
                sampleHistoryDto.setSampleId((Integer) sampleItem.getId());
                sampleHistoryDto.setSampleName(sampleItem.getSampleName());
                sampleHistoryDto.setTime(historyItem.getTime());
                sampleHistoryDto.setAction(historyItem.getAction());
                String info = historyItem.getInfo();
                if (historyItem.getAction().equals(SampleHistoryItem.ACTION_CREATE_DATASET_FROM_SAMPLE)) {
                    ImportQueueItem iqItem = iqDao.get(historyItem.getImportQueueId());
                    DatasetItem createdDataset = dsDao.get((Integer) iqItem.getDataset().getId());
                    if (iqItem != null) {
                        String withKCMs = iqItem.getIncludeUserKCMs() ? "with" : "without";
                        String beginLink = "<a href=\"DatasetInfo?datasetId=" + createdDataset.getId() + "\">";
                        String endLink = "</a>";
                        info = "Created dataset '" + beginLink + createdDataset.getDatasetName() + endLink
                            + "' [" + createdDataset.getId() + "] " + "from dataset '"
                            + datasetItem.getDatasetName() + "' [" + datasetItem.getId()
                            + "] and sample '"
                            + sampleItem.getSampleName() + "' "
                            + withKCMs + " user created KC Models";
                    }
                }
                sampleHistoryDto.setInfo(info);


                sampleHistoryDtos.add(sampleHistoryDto);
            }
        }

        return sampleHistoryDtos;
    }

    /**
     * Adds metric info to the given SampleRowDto.
     * @param sampleItem the sample item
     * @param sampleRowDto the sample row DTO
     * @param smDao the sample metric dao
     * @param txDao the transaction dao
     */
    public void addMetricInfo(SampleItem sampleItem, SampleRowDto sampleRowDto,
            SampleMetricDao smDao, TransactionDao txDao) {
        Integer sampleId = (Integer) sampleItem.getId();

        sampleRowDto.setNumTransactions(smDao.getTotalTransactions(sampleItem));
        sampleRowDto.setNumStudents(smDao.getTotalStudents(sampleItem));
        Long numProblems = txDao.getNumProblems(sampleId);
        sampleRowDto.setNumProblems(numProblems);
        sampleRowDto.setNumSteps(smDao.getTotalPerformedSteps(sampleItem));
        sampleRowDto.setNumUniqueSteps(smDao.getTotalUniqueSteps(sampleItem));
        sampleRowDto.setTotalStudentHours(smDao.getTotalStudentHours(sampleItem));
    }

    /**
     * Saves the sample history item.
     * @param userItem the user
     * @param sampleItem the sample
     * @param action the sample history action
     * @param info the info field
     * @param importQueueId the optional importQueueId
     * @return
     */
    public SampleHistoryItem saveSampleHistory(UserItem userItem, SampleItem sampleItem,
        String action, String info,
            Integer importQueueId) {
        SampleDao sampleDao = DaoFactory.DEFAULT.getSampleDao();
        SampleHistoryDao sampleHistoryDao = DaoFactory.DEFAULT.getSampleHistoryDao();
        FilterDao filterDao = DaoFactory.DEFAULT.getFilterDao();
        SampleHistoryItem sampleHistoryItem = new SampleHistoryItem();
        sampleHistoryItem.setAction(action);
        sampleHistoryItem.setDataset(sampleItem.getDataset());
        sampleItem = sampleDao.get((Integer) sampleItem.getId());
        //walk through list adding each filter to the response string.

        if (action.equals(SampleHistoryItem.ACTION_CREATE_DATASET_FROM_SAMPLE)
            || action.equals(SampleHistoryItem.ACTION_CREATE_SAMPLE)
            || action.equals(SampleHistoryItem.ACTION_MODIFY_FILTERS)) {
            StringBuffer filtersText = new StringBuffer();
            List<FilterItem> filterItems = filterDao.find(sampleItem);
            for (FilterItem filterItem : filterItems) {
                filtersText.append("\n" + filterItem.getClazz() + "\t");
                filtersText.append(filterItem.getAttribute() + "\t");
                filtersText.append(filterItem.getFilterString() + "\t");
                filtersText.append(filterItem.getOperator());
            }
            sampleHistoryItem.setFiltersText(filtersText.toString());
        }

        sampleHistoryItem.setImportQueueId(importQueueId);
        sampleHistoryItem.setInfo(info);
        sampleHistoryItem.setSample(sampleItem);
        sampleHistoryItem.setTime(new Date());
        // Set the owner id
        UserDao userDao = DaoFactory.DEFAULT.getUserDao();
        UserItem owner = userDao.get((String) userItem.getId());
        if (owner != null) {
            sampleHistoryItem.setUserId((String) owner.getId());
        }
        sampleHistoryDao.saveOrUpdate(sampleHistoryItem);
        return sampleHistoryItem;

    }

    /**
     * Gets the formatted user name and, optionally, the id.
     * @param user the user
     * @param showUserId whether or not to show the user id
     * @return the formatted user name and, optionally, the id; this function
     * can return null for null user items or when not first/last name are null
     * or empty and the user sets showUserId = false.
     */
    public static String getFormattedUserName(UserItem user, Boolean showUserId) {
        if (user == null) {
            return null;
        }
        String formattedUserName = null;
        if (user.getFirstName() != null && !user.getFirstName().isEmpty()) {
            formattedUserName = user.getFirstName();
        }
        if (user.getLastName() != null && !user.getLastName().isEmpty()) {
            formattedUserName += " " + user.getLastName();
        }
        if (showUserId) {
            String beginParen = "";
            String endParen = "";
            if (formattedUserName != null) {
                beginParen = " (";
                endParen = ")";
            } else {
                formattedUserName = new String();
            }
            formattedUserName += beginParen + user.getId() + endParen;
        }
        return formattedUserName;
    }



}
