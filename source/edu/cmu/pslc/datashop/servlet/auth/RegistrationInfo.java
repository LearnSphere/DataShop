/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.auth;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.item.UserItem;

/**
 * Helps in the creation of a new account.
 *
 * @author Alida Skogsholm
 * @version $Revision: 14925 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2018-03-14 11:46:43 -0400 (Wed, 14 Mar 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public class RegistrationInfo {

    /** Debug logging. */
    private static Logger logger = Logger.getLogger(RegistrationInfo.class);

    /** Password minimum length is 6 characters. */
    private static final int PASSWORD_MIN_LENGTH = 6;
    /** Password maximum length is 12 characters. */
    private static final int PASSWORD_MAX_LENGTH = 12;

    /** Accept terms required message. */
    private static final String ACCEPT_TERMS_MSG =
        "You must agree to the DataShop terms of use to create an account.";
    /** Account Id already taken. */
    private static final String ACCOUNT_ALREADY_TAKEN_MSG =
        "The username you entered is already taken.";
    /** Invalid account id  message. */
    private static final String INVALID_ACCOUNT_ID_MSG = "Please enter a username.";
    /** First name message. */
    private static final String INVALID_FIRST_NAME_MSG = "Please enter your first name.";
    /** Last name message. */
    private static final String INVALID_LAST_NAME_MSG = "Please enter your last name.";
    /** Password mismatch message. */
    static final String PASSWORD_MISMATCH_MSG = "Please retype your password exactly.";
    /** Invalid email message. */
    private static final String INVALID_EMAIL_MSG = "Please enter your email address."
        + " We need this in case you forget your password.";
    /** Invalid password message. */
    static final String INVALID_PASSWORD_MSG = "Please choose a password"
        + " that is 6-12 characters long, contains only letters, numbers,"
        + " underscores, and hyphens (spaces are not allowed), and does not contain"
        + " the username. Note that passwords are case sensitive.";
    /** General Failure message. */
    private static final String GENERAL_FAILURE_MSG = "An error has occurred."
        + " Please make sure to use a unique and valid username"
        + " and that the password does not contain the username.";

    /** Indicates whether user has agreed to the Terms of Use. */
    private boolean touAgreeFlag = false;
    /** Indicates whether this is a WEBISO registration or not. */
    private boolean webIsoFlag = false;
    /** User item. */
    private UserItem userItem;
    /** Password One. */
    private String password1;
    /** Password Two. */
    private String password2;

    /** Indicates whether user has agreed to the terms. */
    private boolean invalidTermsAgreement = false;
    /** Indicates whether the account id is valid. */
    private boolean invalidAccountId = false;
    /** Indicates whether the first name is valid. */
    private boolean invalidFirstName = false;
    /** Indicates whether the last name is valid. */
    private boolean invalidLastName = false;
    /** Indicates whether the email address is valid. */
    private boolean invalidEmail = false;
    /** Indicates whether the password is valid. */
    private boolean invalidPassword = false;
    /** Indicates whether the passwords do not match. */
    private boolean passwordMismatch = false;
    /** Indicates general failure. */
    private boolean generalFailure = false;

    /** Indicates use by LearnSphere registration/login. */
    private boolean isLearnSphere = false;
    /** Accept terms required message. */
    private static final String LS_ACCEPT_TERMS_MSG =
        "You must agree to the LearnSphere terms of use to create an account.";

    /**
     * Default constructor.
     */
    public RegistrationInfo() {
    }

    /**
     * Returns the Terms of Use Agreement flag.
     * @return true if user has agreed to the Terms of Use
     */
    public boolean isTouAgree() {
        return touAgreeFlag;
    }

    /**
     * Sets the Terms of Use Agreement flag.
     * @param flag indicates whether user agreed to the Terms of Use
     */
    public void setTouAgreeFlag(boolean flag) {
        if (!flag) {
            invalidTermsAgreement = true;
        }
        this.touAgreeFlag = flag;
    }

    /**
     * Returns the WEBISO flag.
     * @return true if this is a WEBISO registration
     */
    public boolean isWebIso() {
        return webIsoFlag;
    }

    /**
     * Sets the WEBISO flag.
     * @param flag indicates whether this is a WEBISO registration
     */
    public void setWebIso(boolean flag) {
        this.webIsoFlag = flag;
    }

    /**
     * Returns the user item.
     * @return the user item
     */
    public UserItem getUserItem() {
        return userItem;
    }

    /**
     * Sets the user item.
     * @param userItem the user item
     */
    public void setUserItem(UserItem userItem) {
        this.userItem = userItem;
        // Check the account id
        if (((String)userItem.getId()).length() <= 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("Invalid username, zero length.");
            }
            invalidAccountId = true;
        }
        // Check the first name
        if (userItem.getFirstName() != null
                && ((String)userItem.getFirstName()).length() <= 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("Invalid first name, zero length.");
            }
            invalidFirstName = true;
        }
        // Check the last name
        if (userItem.getLastName() != null
                && ((String)userItem.getLastName()).length() <= 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("Invalid last name, zero length.");
            }
            invalidLastName = true;
        }
        // Check the email address
        if (userItem.getEmail() != null
                && ((String)userItem.getEmail()).length() <= 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("Invalid email address, zero length.");
            }
            invalidEmail = true;
        }
    }

    /**
     * Returns the first password.  This assumes that caller has checked validity.
     * @return the password
     */
    public String getPassword() {
        return this.password1;
    }

    /**
     * Sets the passwords and checks them.
     * @param accountId the account id, needed for validity check
     * @param pass1 password one
     * @param pass2 password two
     */
    public void setPasswords(String accountId, String pass1, String pass2) {
        this.password1 = pass1;
        this.password2 = pass2;

        if (logger.isDebugEnabled()) {
            logger.debug("Checking password validity.");
        }

        invalidPassword = !(isValidPassword(accountId, pass1));

        if (!password1.equals(password2)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Passwords do not match, zero length.");
            }
            passwordMismatch = true;
        }
    }

    /**
     * Check if the password is valid.  Need the account ID as its not
     * allowed to have the account id in the password.
     * @param accountId the account id
     * @param password the password
     * @return true if the password is valid, false otherwise
     */
    static boolean isValidPassword(String accountId, String password) {

        // check for null
        if ((accountId == null) || (password == null)) {
            return false;
        }

        // check that password is not an empty string
        if ((password.length() <= 0) || (password.length() <= 0)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Invalid passwords, zero length.");
            }
            return false;
        }

        // check the length
        if ((password.length() < PASSWORD_MIN_LENGTH)
            || (password.length() > PASSWORD_MAX_LENGTH)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Invalid passwords, invalid length.");
            }
            return false;
        }

        /// check the account id is not in the password
        if (password.toLowerCase().indexOf(accountId.toLowerCase()) >= 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("Password cannot contain username.");
            }
            return false;
        }

        // check for valid characters
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);

            if (Character.isLetterOrDigit(c)) {
                continue;
            } else if (c == '-' || c == '_') {
                continue;
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Password contains an invalid character.");
                }
                return false;
            }
        }

        return true;
    }

    /**
     * Returns true if the account id is invalid, false if it is valid.
     * @return true if the account id is invalid, false otherwise
     */
    public boolean isInvalidAccountId() {
        return invalidAccountId;
    }

    /**
     * Sets the invalid account id flag.
     * @param flag the flag
     */
    public void setInvalidAccountId(boolean flag) {
        this.invalidAccountId = flag;
    }

    /**
     * Sets the general failure flag.
     * @param flag the flag
     */
    public void setGeneralFailure(boolean flag) {
        this.generalFailure = flag;
    }

    public void setIsLearnSphere(boolean flag) { 
        this.isLearnSphere = flag;
    }

    /**
     * Check if any of the flags indicate that this user item is not valid.
     * @return true if user item is a valid new item
     */
    public boolean isValid() {
        if ((invalidTermsAgreement)
            || (invalidAccountId)
            || (invalidFirstName)
            || (invalidLastName)
            || (invalidEmail)
            || (invalidPassword)
            || (passwordMismatch)
            || (generalFailure)) {
            return false;
        }
        return true;
    }

    /**
     * Pass through method to get the account id.
     * @return the account/user id
     */
    public String getAccountId() {
        return (String)(userItem.getId());
    }

    /**
     * Pass through method to get the first name.
     * @return the first name
     */
    public String getFirstName() {
        return userItem.getFirstName();
    }

    /**
     * Pass through method to get the last name.
     * @return the last name
     */
    public String getLastName() {
        return userItem.getLastName();
    }

    /**
     * Pass through method to get the email address.
     * @return the email address
     */
    public String getEmail() {
        return userItem.getEmail();
    }

    /**
     * Pass through method to get the institution.
     * @return the institution
     */
    public String getInstitution() {
        return userItem.getInstitution();
    }

    /**
     * Pass through method to get the user alias.
     * @return the userAlias
     */
    public String getUserAlias() {
        return userItem.getUserAlias();
    }

    /**
     * Returns HTML with an error message if the account id is invalid.
     * @return the HTML with an error message if invalid, but an empty string otherwise
     */
    public String getInvalidTouAgreeMessage() {
        if (invalidTermsAgreement) {
            if (isLearnSphere) {
                return LS_ACCEPT_TERMS_MSG;
            } else {
                return ACCEPT_TERMS_MSG;
            }
        }
        return "";
    }

    /**
     * Returns HTML with an error message if the account id is invalid.
     * @return the HTML with an error message if invalid, but an empty string otherwise
     */
    public String getInvalidAccountIdMessage() {
        if (invalidAccountId) {
            if (((String)userItem.getId()).length() == 0) {
                return getInvalidMessage(INVALID_ACCOUNT_ID_MSG);
            }
            return getInvalidMessage(ACCOUNT_ALREADY_TAKEN_MSG);
        }
        return "";
    }

    /**
     * Returns HTML with an error message if the first name is invalid.
     * @return the HTML with an error message if invalid, but an empty string otherwise
     */
    public String getInvalidFirstNameMessage() {
        if (invalidFirstName) {
            return getInvalidMessage(INVALID_FIRST_NAME_MSG);
        }
        return "";
    }

    /**
     * Returns HTML with an error message if the last name is invalid.
     * @return the HTML with an error message if invalid, but an empty string otherwise
     */
    public String getInvalidLastNameMessage() {
        if (invalidLastName) {
            return getInvalidMessage(INVALID_LAST_NAME_MSG);
        }
        return "";
    }

    /**
     * Returns HTML with an error message if the last name is invalid.
     * @return the HTML with an error message if invalid, but an empty string otherwise
     */
    public String getInvalidEmailMessage() {
        if (invalidEmail) {
            return getInvalidMessage(INVALID_EMAIL_MSG);
        }
        return "";
    }

    /**
     * Returns HTML with an error message if the passwords are invalid.
     * @return the HTML with an error message if invalid, but an empty string otherwise
     */
    public String getInvalidPasswordMessage() {
        if (invalidPassword) {
            return getInvalidMessage(INVALID_PASSWORD_MSG);
        }
        if (passwordMismatch) {
            return getInvalidMessage(PASSWORD_MISMATCH_MSG);
        }
        if (generalFailure) {
            return getInvalidMessage(GENERAL_FAILURE_MSG);
        }
        return "";
    }

    /**
     * Utility method.
     * @param message the error message
     * @return the HTML with the error message filled in
     */
    private String getInvalidMessage(String message) {
        return "<tr><td>&nbsp;</td><td class=\"errorMessage\">" + message + "</td></tr>";
    }
}
