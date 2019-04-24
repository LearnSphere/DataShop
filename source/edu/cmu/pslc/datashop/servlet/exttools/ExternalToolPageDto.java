package edu.cmu.pslc.datashop.servlet.exttools;

import java.util.List;

/**
 * Data for the External Tool page for a single tool,
 * which is passed to the JSP from the servlet.
 *
 * @author alida
 * @version $Revision: 7819 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2012-08-09 13:40:05 -0400 (Thu, 09 Aug 2012) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ExternalToolPageDto {

    //----- CONSTANTS -----

    /** Constant for the request attribute. */
    public static final String ATTRIB_NAME = "externalToolPageDto";

    //----- ATTRIBUTES -----

    /** Success/Error message */
    private String message;
    /** Level of message, success, warn error. */
    private String messageLevel;
    /** Tool DTO */
    private ExternalToolDto toolDto;
    /** List of tools to display in the table. */
    private List<ExternalToolFileDto> fileList;
    /** Edit authorization flag */
    private Boolean editAuthFlag;
    /** Admin authorization flag */
    private Boolean adminAuthFlag;

    //----- CONSTRUCTOR -----

    /**
     * Constructor.
     * @param message the message
     * @param messageLevel the messageLevel
     * @param toolDto the toolDto
     * @param fileList the fileList
     */
    public ExternalToolPageDto(String message, String messageLevel,
            ExternalToolDto toolDto,
            List<ExternalToolFileDto> fileList) {
        this.message = message;
        this.messageLevel = messageLevel;
        this.toolDto = toolDto;
        this.fileList = fileList;
    }

    //----- GETTERs and SETTERs -----

    /**
     * Get the message.
     * @return the message
     */
    public String getMessage() {
        return message;
    }
    /**
     * Set the message.
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }
    /**
     * Get the messageLevel.
     * @return the messageLevel
     */
    public String getMessageLevel() {
        return messageLevel;
    }
    /**
     * Set the messageLevel.
     * @param messageLevel the messageLevel to set
     */
    public void setMessageLevel(String messageLevel) {
        this.messageLevel = messageLevel;
    }
    /**
     * Get the toolDto.
     * @return the toolDto
     */
    public ExternalToolDto getToolDto() {
        return toolDto;
    }
    /**
     * Set the toolDto.
     * @param toolDto the toolDto to set
     */
    public void setToolDto(ExternalToolDto toolDto) {
        this.toolDto = toolDto;
    }
    /**
     * Get the fileList.
     * @return the fileList
     */
    public List<ExternalToolFileDto> getFileList() {
        return fileList;
    }
    /**
     * Set the fileList.
     * @param fileList the fileList to set
     */
    public void setFileList(List<ExternalToolFileDto> fileList) {
        this.fileList = fileList;
    }

    /**
     * Get the editAuthFlag.
     * @return the editAuthFlag
     */
    public Boolean hasEditAuth() {
        return editAuthFlag;
    }

    /**
     * Set the editAuthFlag.
     * @param editAuthFlag the editAuthFlag to set
     */
    public void setEditAuthFlag(Boolean editAuthFlag) {
        this.editAuthFlag = editAuthFlag;
    }

    /**
     * Get the adminAuthFlag.
     * @return the adminAuthFlag
     */
    public Boolean hasAdminAuth() {
        return adminAuthFlag;
    }

    /**
     * Set the adminAuthFlag.
     * @param adminAuthFlag the adminAuthFlag to set
     */
    public void setAdminAuthFlag(Boolean adminAuthFlag) {
        this.adminAuthFlag = adminAuthFlag;
    }
}
