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

package org.ppwcode.vernacular.persistence_III.junit.jpa;


import static org.junit.Assert.fail;
import static org.ppwcode.metainfo_I.License.Type.APACHE_V2;

import java.io.Serializable;
import java.util.List;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.RollbackException;
import javax.persistence.TransactionRequiredException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.ppwcode.metainfo_I.Copyright;
import org.ppwcode.metainfo_I.License;
import org.ppwcode.metainfo_I.vcs.SvnInfo;
import org.ppwcode.vernacular.exception_III.ExternalError;
import org.ppwcode.vernacular.persistence_III.PersistentBean;
import org.ppwcode.vernacular.persistence_III.dao.jpa.JpaPagingList;


/**
 * A simple helper class for hibernate actions within jUnit tests.
 *
 * @author  Jan Dockx
 * @author  Tom Mahieu
 * @author  Peopleware n.v.
 */
@Copyright("2004 - $Date$, PeopleWare n.v.")
@License(APACHE_V2)
@SvnInfo(revision = "$Revision$",
    date     = "$Date$")
    public abstract class AbstractJpaTest {

  private static final Log _LOG = LogFactory.getLog(AbstractJpaTest.class);

  private static EntityManagerFactory $entityManagerFactory;

  private static final String JPA_PERSISTENCE_UNIT_NAME = AbstractJpaTest.class.getName() + "_persistenceunit";

  @BeforeClass
  public void initEntityManagerFactory() {
    try {
      _LOG.debug("Creating EntityManagerFactory; Using persistence unit " + JPA_PERSISTENCE_UNIT_NAME);
      $entityManagerFactory = Persistence.createEntityManagerFactory(JPA_PERSISTENCE_UNIT_NAME);
      _LOG.debug("EntityManagerFactory created.");
    }
    catch (PersistenceException all) {
      all.printStackTrace();
      fail("Failed to create EntityManagerFactory.");
    }
  }

  @AfterClass
  public void deinitEntityManagerFactory() {
    _LOG.debug("discarding EntityManagerFactory");
    $entityManagerFactory = null;
    _LOG.debug("EntityManagerFactory discarded");
  }

  public void createEntityManager() {
    try {
      $entityManager = $entityManagerFactory.createEntityManager();
    }
    catch (PersistenceException all) {
      all.printStackTrace();
      fail("Failed to create EntityManager.");
    }
  }

  public void discardEntityManager() {
    try {
      $entityManager.close();
      $entityManager = null;
    }
    catch (IllegalStateException ise) {
      ise.printStackTrace();
      fail("Couldn't discard EntityManager.");
    }
    catch (PersistenceException all) {
      all.printStackTrace();
      fail("Couldn't discard EntityManager.");
    }
  }

  public void beginTransaction() {
    try {
      $tx = $entityManager.getTransaction();
      $tx.begin();
    }
    catch (IllegalStateException ise) {
      ise.printStackTrace();
      fail("Couldn't start Transaction.");
    }
    catch (PersistenceException all) {
      all.printStackTrace();
      fail("Couldn't start Transaction.");
    }
  }

  public void commitTransaction() {
    try {
      $tx.commit();
      $tx = null;
    }
    catch (IllegalStateException ise) {
      ise.printStackTrace();
      fail("Couldn't commit Transaction");
    }
    catch (RollbackException rbe) {
      rbe.printStackTrace();
      fail("Couldn't commit Transaction");
    }
    catch (PersistenceException all) {
      all.printStackTrace();
      fail("Couldn't commit Transaction");
    }
  }

  public void rollbackTransaction() {
    try {
      $tx.rollback();
      $tx = null;
    }
    catch (IllegalStateException ise) {
      ise.printStackTrace();
      fail("Couldn't roll back Transaction.");
    }
    catch (PersistenceException all) {
      all.printStackTrace();
      fail("Couldn't roll back Transaction.");
    }
  }

  public Object create(final Object object) {
    try {
      $entityManager.persist(object);
      if (object instanceof PersistentBean) {
        return ((PersistentBean<?>)object).getPersistenceId();
      }
      else {
        return null;
      }
    }
    catch (EntityExistsException eee) {
      eee.printStackTrace();
      fail("Failed to create the object in the database.");
    }
    catch (IllegalStateException ise) {
      ise.printStackTrace();
      fail("Failed to create the object in the database.");
    }
    catch (IllegalArgumentException iae) {
      iae.printStackTrace();
      fail("Failed to create the object in the database.");
    }
    catch (TransactionRequiredException tre) {
      tre.printStackTrace();
      fail("Failed to create the object in the database.");
    }
    return null;
  }

  public void delete(final Object object) {
    $entityManager.remove(object);
  }

  public Object retrieve(final Class<?> clazz, final Serializable id) {
    Object result = null;
    try {
      result = $entityManager.find(clazz, id);
    }
    catch (IllegalStateException ise) {
      ise.printStackTrace();
      fail("Failed to retrieve the object from the database.");
    }
    catch (IllegalArgumentException iae) {
      iae.printStackTrace();
      fail("Failed to retrieve the object from the database.");
    }
    return result;
  }

  public <_PersistentObject_> List<_PersistentObject_> retrieve(final Class<_PersistentObject_> persistentObjectType) {
    String qlString = "SELECT c FROM " + persistentObjectType.getName() + " c ORDER BY c.id";

    @SuppressWarnings("unchecked")
    List<_PersistentObject_> result = (List<_PersistentObject_>) retrieve(qlString);
    return result;
  }

  public final static int DEFAULT_PAGE_SIZE = 100;

  public int getPageSize() {
    return DEFAULT_PAGE_SIZE;
  }

  public <_Id_ extends Serializable, _PersistentBean_ extends PersistentBean<_Id_>> JpaPagingList<_Id_, _PersistentBean_>
  retrievePages(final Class<_PersistentBean_> persistentObjectType) {
    try {
      Query countq = $entityManager.createQuery("SELECT COUNT(*) FROM " + persistentObjectType.getName());
      Query listq = $entityManager.createQuery("SELECT c FROM " + persistentObjectType.getName() + " c ORDER BY c.id");
      return new JpaPagingList<_Id_, _PersistentBean_>(listq, countq, getPageSize());
    }
    catch (IllegalStateException ise) {
      ise.printStackTrace();
      fail("Failed to retrieve objects from database");
    }
    catch (IllegalArgumentException iae) {
      iae.printStackTrace();
      fail("Failed to retrieve objects from database");
    }
    catch (ExternalError peErr) {
      peErr.printStackTrace();
      fail("Failed to retrieve objects from database");
    }
    return null;
  }

  public List<?> retrieve(String qlQueryString) {
    List<?> result = null;
    try {
      Query q = $entityManager.createQuery(qlQueryString);
      result = q.getResultList();
    }
    catch (IllegalStateException e) {
      assert false : "HibernateExceptionshould not happen: " + e;
    }
    return result;
  }

  public EntityManager getEntityManager() {
    return $entityManager;
  }

  private EntityManager $entityManager;

  public EntityTransaction getTransaction() {
    return $tx;
  }

  private EntityTransaction $tx;

}
