/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.sourcedb;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * Takes care of startup and makes Session handling convenient.
 * The so called ThreadLocal Session pattern is useful here, we keep the current
 * unit of work associated with the current thread.
 * Note: Copied from Chapter 2 (2.2.5) of the Hibernate Manual.
 *
 * More notes from the manual:
 * This class does not only produce the global SessionFactory in its static initializer
 * (called once by the JVM when the class is loaded), but also has a ThreadLocal
 * variable to hold the Session for the current thread. No matter when you call
 * HibernateUtil.currentSession(), it will always return the same Hibernate unit
 * of work in the same thread. A call to HibernateUtil.closeSession()  ends the unit
 * of work currently associated with the thread.
 *
 * Make sure you understand the Java concept of a thread-local variables before you
 * use this helper. A more powerful HibernateUtil helper can be found in CaveatEmptor
 * on http://caveatemptor.hibernate.org/ - as well as in the book "Hibernate in Action".
 * Note that this class is not necessary if you deploy Hibernate in a J2EE application
 * server: a Session will be automatically bound to the current JTA transaction and
 * you can look up the SessionFactory through JNDI. If you use JBoss AS, Hibernate
 * can be deployed as a managed system service and will automatically bind the SessionFactory
 * to a JNDI name.
 *
 * @author Shanwen Yu
 * @version $Revision: 6706 $
 * <BR>Last modified by: $Author: shanwen $
 * <BR>Last modified on: $Date: 2011-03-11 16:01:00 -0500 (Fri, 11 Mar 2011) $
 * <!-- $KeyWordsOff: $ -->
 */
public final class SourceDbHibernateUtil {

    /** The configuration file name for this utility. */
    private static final String CONFIG_FILE = "hibSource.cfg.xml";

    /** The session factory for the default configuration file. */
    public static final SessionFactory SOURCE_SESSION_FACTORY;

    static {
        try {
            // Create the SessionFactory from hibSource.cfg.xml
            SOURCE_SESSION_FACTORY = new Configuration().configure(CONFIG_FILE)
                                        .buildSessionFactory();

        } catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    /** Private constructor. */
    private SourceDbHibernateUtil() { }

    /** Session. */
    public static final ThreadLocal SOURCE_SESSION = new ThreadLocal();

    /**
     * Get the current session.
     * @return the current session
     */
    public static Session currentSession() {
        Session s = (Session) SOURCE_SESSION.get();
        // Open a new Session, if this thread has none yet
        if (s == null) {
            s = SOURCE_SESSION_FACTORY.openSession();
            // Store it in the ThreadLocal variable
            SOURCE_SESSION.set(s);
        }
        return s;
    }

    /**
     * Close the session.
     */
    public static void closeSession() {
        Session s = (Session) SOURCE_SESSION.get();
        if (s != null) {
            s.close();
        }
        SOURCE_SESSION.set(null);
    }
}
