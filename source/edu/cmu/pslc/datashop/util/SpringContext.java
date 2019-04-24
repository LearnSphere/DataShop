/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.util;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * This is a single class holding the Application Context information
 * for all spring beans.
 * Workflow : Workflow components require access the component-designated application context file
 * which exists outside of each components' working directory.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 13851 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2017-02-10 09:44:42 -0500 (Fri, 10 Feb 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public final class SpringContext {

    /** Spring Framework application context. */
    private static ApplicationContext ctx;

    /** Public instance of the spring context. */
    public static final SpringContext INSTANCE = new SpringContext();

    /** Private constructor to make this a singleton. */
    private SpringContext() {

    }

    /**
     * Public access method for application context changed for workflow components.
     * Maintains singleton (and constant/final) nature-- once created, it cannot be reassigned.
     * This makes it slightly safer, but read on.
     *
     * Since workflow components have a working directory of <datashop files dir>/workflows/<id>/<componentId>,
     * then DAO-enabled components must load a shared applicationContext.xml which is both shared and
     * restrictive.
     *
     * Note: Allowing a component to set the applicationContext filepath be dangerous.
     * It effectively allows any component to load any applicationContext.xml's passwords and server addresses.
     * To ensure security, 1) we must not allow user-submitted components to use DAO without review.
     * 2) The path must only be set inside AbstractComponents.java
     * 3) Only 1 of 2 context files may be loaded.. the datashop version or the shared workflow component one
     * 4) We must also never allow anyone outside of the team to modify datashop.jar in CommonLibraries.
     *
     * @return ApplicationContext the application context for Spring in workflow components
     */
    public static ApplicationContext getApplicationContext(String filePath) {
        if (ctx == null) {
            if (filePath == null) {
                // If the file path is not given, use the default directory for this application.
                filePath = "applicationContext.xml";
                ctx = new ClassPathXmlApplicationContext(filePath);
            } else {
                if (filePath.matches("/.*") || filePath.matches("[a-zA-Z]:.*")) {
                    // Use an explicit "file:" prefix to enforce an absolute file path.
                    ctx = new FileSystemXmlApplicationContext("file:" + filePath);
                } else {
                    // Use a relative path.
                    ctx = new FileSystemXmlApplicationContext(filePath);
                }

            }
        }
        return ctx;
    }



}
