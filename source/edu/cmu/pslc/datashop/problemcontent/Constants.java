/*
* Carnegie Mellon University, Human Computer Interaction Institute
* Copyright 2014
* All Rights Reserved
*/
package edu.cmu.pslc.datashop.problemcontent;


 /**
 * This class is a collection of constants use by Problem Content-related
 * tools and servlets.
 * This is a utility class with a private constructor.
 *
 * @author Cindy Tipper
 * @version $Revision: 11225 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2014-06-20 08:17:05 -0400 (Fri, 20 Jun 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public final class Constants {

    /** Private constructor - utility class. */
    private Constants() { }

    /** Tag to find and replace for path to resources in generated HTML. */
    public static final String RESOURCES_PATH = "RESOURCES_PATH";
    /** Character encoding for input stream readers. */
    public static final String UTF8 = "UTF8";
    /** The buffered reader buffer size. */
    public static final int IS_READER_BUFFER = 8192;
    /** OLI directory designation. */
    public static final String OLI_DIR_PART = "/oli/";

}
