package edu.cmu.pslc.datashop.dao.hibernate;

import java.io.Serializable;
import java.util.HashMap;

import org.hibernate.HibernateException;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.id.IdentityGenerator;

import edu.cmu.pslc.datashop.item.Item;

/**
 * An identity generator that permits the next id to be manually overridden.
 * @author epennin1
 *
 */
public class UpdatableIdentityGenerator extends IdentityGenerator {
    /** Manually overridden ids stored per class. */
    protected static HashMap<Class<? extends Item>, Number> nextIds =
            new HashMap<Class<? extends Item>, Number>();
    
    /**
     * Manually overrides the next id for the specified class.
     * @param clazz the class
     * @param nextId the next id
     */
    public static void setNextId(Class<? extends Item> clazz, Number nextId) {
        nextIds.put(clazz, nextId);
    }

    /**
     * Generates the next id for the specified object.
     * @param session the hibernate session
     * @param obj the object
     * @return the next id
     */
    @Override
    public Serializable generate(SessionImplementor session, Object obj) throws HibernateException {
        Number nextId = nextIds.remove(obj.getClass());
        if (nextId != null) {
            return nextId;
        }
        return super.generate(session, obj);
    }
}