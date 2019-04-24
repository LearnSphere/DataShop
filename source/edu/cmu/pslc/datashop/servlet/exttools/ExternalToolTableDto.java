package edu.cmu.pslc.datashop.servlet.exttools;

import java.util.List;

/**
 * Data for the External Tools Table page,
 * which is passed to the JSP from the servlet.
 *
 * @author alida
 * @version $Revision: 7819 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2012-08-09 13:40:05 -0400 (Thu, 09 Aug 2012) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ExternalToolTableDto {

    //----- CONSTANTS -----

    /** Constant for the request attribute. */
    public static final String ATTRIB_NAME = "externalToolTableDto";

    /** Constant for the success message level : SUCCESS. */
    public static final String STATUS_MESSAGE_LEVEL_SUCCESS = "SUCCESS";
    /** Constant for the success message level : WARN. */
    public static final String STATUS_MESSAGE_LEVEL_WARN = "WARN";
    /** Constant for the success message level : ERROR. */
    public static final String STATUS_MESSAGE_LEVEL_ERROR = "ERROR";

    //----- ATTRIBUTES -----

    /** Success/Error message */
    private String message;
    /** Level of message, success, warn error. */
    private String messageLevel;
    /** List of tools to display in the table. */
    private List<ExternalToolDto> toolList;
    /** Requested External Tools Role flag */
    private Boolean requestedExtToolsRoleFlag;
    /** External Tools Role flag */
    private Boolean extToolsRoleFlag;
    /** Admin authorization flag */
    private Boolean adminAuthFlag;

    //----- CONSTRUCTOR -----

    /**
     * Constructor.
     * @param message the message
     * @param messageLevel the messageLevel
     * @param toolList the toolList
     */
    public ExternalToolTableDto(String message, String messageLevel,
            List<ExternalToolDto> toolList) {
        this.message = message;
        this.messageLevel = messageLevel;
        this.toolList = toolList;
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
     * Get the toolList.
     * @return the toolList
     */
    public List<ExternalToolDto> getToolList() {
        return toolList;
    }
    /**
     * Set the toolList.
     * @param toolList the toolList to set
     */
    public void setToolList(List<ExternalToolDto> toolList) {
        this.toolList = toolList;
    }

    /**
     * Get the requestedExtToolsRoleFlag.
     * @return the requestedExtToolsRoleFlag
     */
    public Boolean hasRequestedExtToolsRoleFlag() {
        return requestedExtToolsRoleFlag;
    }

    /**
     * Set the requestedExtToolsRoleFlag.
     * @param requestedExtToolsRoleFlag the requestedExtToolsRoleFlag to set
     */
    public void setRequestedExtToolsRoleFlag(Boolean requestedExtToolsRoleFlag) {
        this.requestedExtToolsRoleFlag = requestedExtToolsRoleFlag;
    }

    /**
     * Get the extToolsRoleFlag.
     * @return the extToolsRoleFlag
     */
    public Boolean hasExtToolsRole() {
        return extToolsRoleFlag;
    }

    /**
     * Set the extToolsRoleFlag.
     * @param extToolsRoleFlag the extToolsRoleFlag to set
     */
    public void setExtToolsRoleFlag(Boolean extToolsRoleFlag) {
        this.extToolsRoleFlag = extToolsRoleFlag;
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
