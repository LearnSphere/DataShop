/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.oli;

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
 * @author Alida Skogsholm
 * @version $Revision: 2050 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2005-12-23 12:03:11 -0500 (Fri, 23 Dec 2005) $
 * <!-- $KeyWordsOff: $ -->
 */
public final class OliHibernateUtil {

    /** The configuration file name for this utility. */
    private static final String CONFIG_FILE = "hibLog.cfg.xml";

    /** The session factory for the default configuration file. */
    public static final SessionFactory LOG_SESSION_FACTORY;

    static {
        try {
            // Create the SessionFactory from hibernate.cfg.xml
            LOG_SESSION_FACTORY = new Configuration().configure(CONFIG_FILE).buildSessionFactory();

        } catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    /** Private constructor. */
    private OliHibernateUtil() { }

    /** Session. */
    public static final ThreadLocal LOG_SESSION = new ThreadLocal();

    /**
     * Get the current session.
     * @return the current session
     */
    public static Session currentSession() {
        Session s = (Session) LOG_SESSION.get();
        // Open a new Session, if this thread has none yet
        if (s == null) {
            s = LOG_SESSION_FACTORY.openSession();
            // Store it in the ThreadLocal variable
            LOG_SESSION.set(s);
        }
        return s;
    }

    /**
     * Close the session.
     */
    public static void closeSession() {
        Session s = (Session) LOG_SESSION.get();
        if (s != null) {
            s.close();
        }
        LOG_SESSION.set(null);
    }
}
