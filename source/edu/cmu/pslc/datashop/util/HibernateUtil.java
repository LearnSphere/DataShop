/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.util;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * NOTE: This class can only be used by unit tests, as it by-passes Spring.  It
 * cannot be used outside of this scope, as it will fail on QA and Production machines.
 *
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
 * @version $Revision: 13475 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2016-09-08 12:20:04 -0400 (Thu, 08 Sep 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public final class HibernateUtil {

    /** The session factory for the default configuration file. */
    public static final SessionFactory SESSION_FACTORY;

    static {
        try {
            // Create the SessionFactory from hibernate.cfg.xml
            final Configuration config = new Configuration();
            config.setProperty( "hibernate.jdbc.factory_class", "org.hibernate.jdbc.NonBatchingBatcherFactory" );

            SESSION_FACTORY = config.configure().buildSessionFactory();

        } catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    /** Private constructor. */
    private HibernateUtil() { }

    /** Session. */
    public static final ThreadLocal THE_SESSION = new ThreadLocal();

    /**
     * Get the current session.
     * @return the current session
     */
    public static Session currentSession() {
        Session s = (Session) THE_SESSION.get();
        // Open a new Session, if this thread has none yet
        if (s == null) {
            s = SESSION_FACTORY.openSession();
            // Store it in the ThreadLocal variable
            THE_SESSION.set(s);
        }
        return s;
    }

    /**
     * Close the session.
     */
    public static void closeSession() {
        Session s = (Session) THE_SESSION.get();
        if (s != null) {
            s.close();
        }
        THE_SESSION.set(null);
    }
}
