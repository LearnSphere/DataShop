/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.UserDao;
import edu.cmu.pslc.datashop.item.UserItem;

/**
 * Hibernate and Spring implementation of the UserDao.
 *
 * @author Alida Skogsholm
 * @version $Revision: 14972 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2018-03-22 09:43:45 -0400 (Thu, 22 Mar 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public class UserDaoHibernate extends AbstractDaoHibernate<UserItem> implements UserDao {
    /**
     * Standard get for a UserItem by id.
     * @param id The id of the user.
     * @return the matching UserItem or null if none found
     */
    public UserItem get(String id) { return get(UserItem.class, id); }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List<UserItem> findAll() { return findAll(UserItem.class); }

    /**
     * Standard find for an UserItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired UserItem.
     * @return the matching UserItem.
     */
    public UserItem find(String id) { return find(UserItem.class, id); }

    /**
     * Find or Create method for the default user.
     * @return the default user.
     */
    public UserItem findOrCreateDefaultUser() {
        UserDao userDao = DaoFactory.DEFAULT.getUserDao();
        UserItem defaultUser = userDao.get(UserItem.DEFAULT_USER);
        if (defaultUser == null) {
            defaultUser = new UserItem(UserItem.DEFAULT_USER);
            defaultUser.setAdminFlag(Boolean.FALSE);
            defaultUser.setCreationTime(new Date());
            defaultUser.setLoginType("local");
            userDao.saveOrUpdate(defaultUser);
            defaultUser = userDao.get(UserItem.DEFAULT_USER);
        }
        return defaultUser;
    }

    /**
     * Find or Create method for the system user.
     * @return the system user.
     */
    public UserItem findOrCreateSystemUser() {
        UserDao userDao = DaoFactory.DEFAULT.getUserDao();
        UserItem systemUser = userDao.get(UserItem.SYSTEM_USER);
        if (systemUser == null) {
            systemUser = new UserItem(UserItem.SYSTEM_USER);
            systemUser.setCreationTime(new Date());
            systemUser.setLoginType("local");
            userDao.saveOrUpdate(systemUser);
            systemUser = userDao.get(UserItem.SYSTEM_USER);

            // System user responsible for denying 'expired' access
            // requests so must have datashop-admin privileges.
            systemUser.setAdminFlag(Boolean.TRUE);
        }
        return systemUser;
    }

    /**
     * Find the user corresponding to the web services API token.
     * @param apiToken a web services API token
     * @return the user corresponding to the web services API token.
     */
    public UserItem findUserWithApiToken(String apiToken) {
        return findWithQuery("from UserItem u where u.apiToken = ?", apiToken);
    }

    /** Query string for the findByLastNameAndEmail method. */
    private static final String FIND_BY_LAST_NAME_AND_EMAIL_QUERY =
            "from UserItem userItem"
            + " where lastName = ?"
            + " and email = ?";

    /**
     * Find the user with the given last name and email address.
     * @param lastName the user-entered last name
     * @param email the user-entered email address
     * @return the user item if found, null otherwise
     */
    public List<UserItem> findByLastNameAndEmail(String lastName, String email) {
        Object[] params = {lastName, email};
        return getHibernateTemplate().find(FIND_BY_LAST_NAME_AND_EMAIL_QUERY, params);
    }

    /** Query string for the findByIdAndType method. */
    private static final String FIND_BY_ID_AND_TYPE_QUERY =
            "from UserItem userItem where id = ? and loginType = ?";

    /**
     * Find the user with the given id and login type.
     * For backward compatibility, handle case where loginType might be null.
     * @param id the user id
     * @param type the login type
     * @return the user item if found, null otherwise
     */
    public List<UserItem> findByIdAndType(String id, String type) {
        Object[] params = {id, type};
        logger.debug("findByIdAndType: user id = " + id + ", login type = " + type);
        List<UserItem> users = getHibernateTemplate().find(FIND_BY_ID_AND_TYPE_QUERY, params);
        if ((users == null) || (users.size() == 0)) {
            users = new ArrayList<UserItem>();
            UserItem user = get(id);
            // Only return a user with the same id and an unknown type.
            // Don't allow match for same id with different types.
            if ((user != null) && (user.getLoginType() == null)) {
                users.add(user);
                logger.debug("findByIdAndType: added user: " + user);
            }
        }
        return users;
    }

    /** Query string for the findByLoginIdAndType method. */
    private static final String FIND_BY_LOGIN_ID_AND_TYPE_QUERY =
            "from UserItem userItem where loginId = ? and loginType = ?";

    /**
     * Find the user with the given login id and login type.
     * @param id the user login id (Google, GitHub or LinkedIn-assigned)
     * @param type the login type
     * @return the user item if found, null otherwise
     */
    public List<UserItem> findByLoginIdAndType(String id, String type) {
        Object[] params = {id, type};
        logger.debug("findByLoginIdAndType: login id = " + id + ", login type = " + type);
        return getHibernateTemplate().find(FIND_BY_LOGIN_ID_AND_TYPE_QUERY, params);
    }

    /**
     * Retrieves users by taking the non-null fields in the criteria and applying 'LIKE' operation.
     * The compared fields are: id, name (for first, last), email, and institution.
     * (DS1430)
     *
     * @param criteria currently used: id, firstName (that will match for lastName too),
     * email, institution
     * @param offset starting record
     * @param max maximum records to retrieve
     * @return list of user items
     */
    public List<UserItem> findBy(UserItem criteria, int offset, int max) {
        DetachedCriteria hbCriteria = DetachedCriteria.forClass(UserItem.class);

        if (StringUtils.isNotEmpty((String)criteria.getId())) {
            hbCriteria.add(Restrictions.ilike("id", (String)criteria.getId(), MatchMode.ANYWHERE));
        }
        if (StringUtils.isNotEmpty(criteria.getFirstName())) {
            hbCriteria.add(Restrictions.or(Restrictions.ilike("firstName",
                                                              criteria.getFirstName(),
                                                              MatchMode.ANYWHERE),
                                           Restrictions.ilike("lastName",
                                                              criteria.getFirstName(),
                                                              MatchMode.ANYWHERE)
                                           )
            );
        }
        if (StringUtils.isNotEmpty(criteria.getEmail())) {
            hbCriteria.add(Restrictions.ilike("email", criteria.getEmail(), MatchMode.ANYWHERE));
        }
        if (StringUtils.isNotEmpty(criteria.getInstitution())) {
            hbCriteria.add(Restrictions.ilike("institution", criteria.getInstitution(),
                                              MatchMode.ANYWHERE));
        }

        if (criteria.getCreationTime() != null) {
            hbCriteria.add(Restrictions.ge("creationTime", criteria.getCreationTime()));
        }
        if (criteria.getAdminFlag() != null) {
            hbCriteria.add(Restrictions.eq("adminFlag", criteria.getAdminFlag()));
        }

        return getHibernateTemplate().findByCriteria(hbCriteria, offset, max);
    }
}
