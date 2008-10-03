/*<license>
Copyright 2005 - $Date$ by PeopleWare n.v..

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
</license>*/

package org.ppwcode.vernacular.persistence_III.junit.hibernate2;


import static org.junit.Assert.fail;
import static org.ppwcode.metainfo_I.License.Type.APACHE_V2;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.hibernate.Criteria;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.ObjectNotFoundException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.Transaction;
import net.sf.hibernate.cfg.Configuration;
import net.sf.hibernate.expression.Order;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.ppwcode.metainfo_I.Copyright;
import org.ppwcode.metainfo_I.License;
import org.ppwcode.metainfo_I.vcs.SvnInfo;
import org.ppwcode.vernacular.exception_II.ExternalError;
import org.ppwcode.vernacular.persistence_III.PersistentBean;
import org.ppwcode.vernacular.persistence_III.dao.hibernate2.Hibernate2PagingList;


/**
 * A simple helper class for hibernate actions within jUnit tests.
 *
 * @author  David Van Keer
 * @author  Jan Dockx
 * @author  Tom Mahieu
 * @author  Peopleware n.v.
 */
@Copyright("2004 - $Date$, PeopleWare n.v.")
@License(APACHE_V2)
@SvnInfo(revision = "$Revision$",
         date     = "$Date$")
public abstract class AbstractHibernate2Test {

  private static final Log _LOG = LogFactory.getLog(AbstractHibernate2Test.class);

  private static SessionFactory $sessionFactory;

  private static final String JUNIT_CONFIG_FILE_LOCATION = "/hibernate2_junit.cfg.xml";

  @BeforeClass
  public void initSessionFactory() throws HibernateException {
    _LOG.debug("reading Hibernate config from " + JUNIT_CONFIG_FILE_LOCATION);
    Configuration configuration = new Configuration();
    configuration.configure(JUNIT_CONFIG_FILE_LOCATION);
    $sessionFactory = configuration.buildSessionFactory();
    _LOG.debug("Hibernate config read ok.");
  }

  @AfterClass
  public void deinitSessionFactory() {
    _LOG.debug("discarding hibernate session factory");
    $sessionFactory = null;
    _LOG.debug("hibernate session factory discarded");
  }

  public void openSession() {
    try {
      $session = $sessionFactory.openSession();
    }
    catch (HibernateException hExc) {
      hExc.printStackTrace();
      fail("Couldn't open a new hibernate session.");
    }
  }

  public void closeSession() {
    try {
      $session.close();
      $session = null;
    }
    catch (HibernateException hExc) {
      hExc.printStackTrace();
      fail("Failed to close the hibernate session.");
    }
  }

  public void beginTransaction() {
    try {
      $tx = $session.beginTransaction();
    }
    catch (HibernateException hExc) {
      hExc.printStackTrace();
      fail("Couldn't start a hibernate transaction.");
    }
  }

  public void commitTransaction() {
    try {
      $tx.commit();
      $tx = null;
    }
    catch (HibernateException hExc) {
      hExc.printStackTrace();
      fail("Failed to commit the hibernate transaction.");
    }
  }

  public void rollbackTransaction() {
    try {
      $tx.rollback();
      $tx = null;
    }
    catch (HibernateException hExc) {
      hExc.printStackTrace();
      fail("Failed to cancel the hibernate transaction.");
    }
  }

  public Object create(final Object object) {
    try {
      $session.save(object);
      if (object instanceof PersistentBean) {
        return ((PersistentBean<?>)object).getPersistenceId();
      }
      else {
        return null;
      }
    }
    catch (HibernateException hExc) {
      hExc.printStackTrace();
      fail("Failed to create the object in the database.");
    }
    return null;
  }

  public void update(final Object object) {
    try {
      $session.update(object);
    }
    catch (HibernateException hExc) {
      hExc.printStackTrace();
      fail("Failed to update the object in the database.");
    }
  }

  public void delete(final Object object) {
    try {
      $session.delete(object);
    }
    catch (HibernateException hExc) {
      hExc.printStackTrace();
      fail("Failed to delete the object to the database.");
    }
  }

  public Object retrieve(final Class<?> clazz, final Serializable id) {
    Object result = null;
    try {
      result = $session.load(clazz, id);
    }
    catch (ObjectNotFoundException onfExc) {
      return null;
    }
    catch (HibernateException hExc) {
      hExc.printStackTrace();
      fail("Failed to retrieve the object from the database.");
    }
    return result;
  }

  public <_PersistentObject_> Set<_PersistentObject_> retrieve(final Class<_PersistentObject_> persistentObjectType) {
    Criteria crit = $session.createCriteria(persistentObjectType);
    @SuppressWarnings("unchecked")
    Set<_PersistentObject_> retrieve = (Set<_PersistentObject_>)retrieve(crit);
    return retrieve;
  }

  public final static int DEFAULT_PAGE_SIZE = 100;

  public int getPageSize() {
    return DEFAULT_PAGE_SIZE;
  }

  public <_Id_ extends Serializable, _PersistentBean_ extends PersistentBean<_Id_>> Hibernate2PagingList<_Id_, _PersistentBean_>
  retrievePages(final Class<_PersistentBean_> persistentObjectType) {
    try {
      Query cq = $session.createQuery("select count(*) from " + persistentObjectType.getName());
      Criteria crit = $session.createCriteria(persistentObjectType);
      crit.addOrder(Order.asc("id"));
      return new Hibernate2PagingList<_Id_, _PersistentBean_>(crit, cq, getPageSize());
    }
    catch (HibernateException hExc) {
      hExc.printStackTrace();
      fail("Failed to retrieve objects from database");
      return null;
    }
    catch (ExternalError peErr) {
      peErr.printStackTrace();
      fail("Failed to retrieve objects from database");
      return null;
    }
  }

  public Set<?> retrieve(final Criteria criteria) {
    Set<Object> results = new HashSet<Object>();
    try {
      List<?> list = criteria.list();
      results.addAll(list);
    }
    catch (HibernateException hExc) {
      hExc.printStackTrace();
      fail("Failed to retrieve objects from database");
    }
    return results;
  }

  public List<?> retrieve(String HqlQueryString) {
    List<?> roles = null;
    try {
      Query q = getSession().createQuery(HqlQueryString);
      roles = q.list();
    }
    catch (HibernateException e) {
      assert false : "HibernateExceptionshould not happen: " + e;
    }
    return roles;
  }

  public Session getSession() {
    return $session;
  }

  private Session $session;

  public Transaction getTransaction() {
    return $tx;
  }

  private Transaction $tx;

}
