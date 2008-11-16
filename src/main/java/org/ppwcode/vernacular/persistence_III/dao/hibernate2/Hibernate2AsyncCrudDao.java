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


package org.ppwcode.vernacular.persistence_III.dao.hibernate2;


import static org.ppwcode.metainfo_I.License.Type.APACHE_V2;
import static org.ppwcode.vernacular.exception_III.ProgrammingErrorHelpers.dependency;
import static org.ppwcode.vernacular.exception_III.ProgrammingErrorHelpers.pre;
import static org.ppwcode.vernacular.exception_III.ProgrammingErrorHelpers.preArgumentNotNull;
import static org.ppwcode.vernacular.exception_III.ProgrammingErrorHelpers.unexpectedException;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.QueryException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ppwcode.metainfo_I.Copyright;
import org.ppwcode.metainfo_I.License;
import org.ppwcode.metainfo_I.vcs.SvnInfo;
import org.ppwcode.vernacular.exception_III.ExternalError;
import org.ppwcode.vernacular.exception_III.ApplicationException;
import org.ppwcode.vernacular.persistence_III.IdNotFoundException;
import org.ppwcode.vernacular.persistence_III.PersistentBean;
import org.ppwcode.vernacular.persistence_III.dao.AsyncCrudDao;
import org.ppwcode.vernacular.semantics_VI.exception.PropertyException;
import org.toryt.annotations_I.Expression;
import org.toryt.annotations_I.Invars;
import org.toryt.annotations_I.MethodContract;


/**
 * <p>Asynchronous CRUD functionality with Hibernate. There are no extra
 *   requirements for {@link PersistentBean}s to be used with Hibernate,
 *   apart from the definition of <kbd>hbm</kbd> files.</p>
 *
 * @note Hibernate 2 version was used very much in the past, so can be considered stable.
 *       But: code has changed since ppwcode-vernacular-persistence II, so we are not sure.
 *       Since we are not currently working with Hibernate, we will not invest more in this
 *       at the moment (so no unit tests, etc.).
 *
 * @author    Jan Dockx
 * @author    PeopleWare n.v.
 *
 * @mudo strengthening preconditions is a nono!!
 */
@Copyright("2004 - $Date$, PeopleWare n.v.")
@License(APACHE_V2)
@SvnInfo(revision = "$Revision$",
         date     = "$Date$")
public class Hibernate2AsyncCrudDao extends AbstractHibernate2Dao implements AsyncCrudDao {

  private static final Log LOG = LogFactory.getLog(Hibernate2AsyncCrudDao.class);



  private static final String NO_PENDING_TRANSACTION = "No transaction pending";
  private static final String PENDING_TRANSACTION = "There is a transaction still pending";
  private static final String NO_ID_IN_PERSISTENT_OBJECT = "No id in persistent object";
  private static final String SHOULD_HAVE_NO_ID_IN_PERSISTENT_OBJECT = "There should be no id in persistent object";



  /*<property name="session">*/
  //------------------------------------------------------------------

  @Override
  @MethodContract(
    pre  = @Expression("! isInTransaction"),
    post = {}
  )
  public final void setSession(final Session session) {
    pre(! isInTransaction(), "Cannot set session now, transaction still in use");
    super.setSession(session);
  }

  /*</property>*/



  @Invars(@Expression("inTransaction == ($tx != null"))
  private Transaction $tx;

  @MethodContract(
    pre  = @Expression("session != null"),
    post = {}
  )
  public final void startTransaction() {
    LOG.debug("Starting hibernate transaction ...");
    pre(! isInTransaction(), PENDING_TRANSACTION);
    dependency(getSession(), "session");
    assert $tx == null;
    try {
      $tx = getSession().beginTransaction();
      setInTransaction(true);
    }
    catch (HibernateException hExc) {
      throw new ExternalError("Could not create Hibernate transaction", hExc);
    }
    LOG.debug("Hibernate transaction started.");
  }

  public final void commitTransaction() throws ApplicationException {
    LOG.debug("Starting commit ...");
    pre(! isInTransaction(), PENDING_TRANSACTION);
    assert $tx != null;
    try {
      $tx.commit();
      $tx = null;
      resetId($deleted);
      $deleted = new HashSet<PersistentBean<?>>();
      $created = new HashSet<PersistentBean<?>>();
      setInTransaction(false);
      LOG.debug("Commit completed.");
    }
    catch (HibernateException hExc) {
      LOG.debug("Commit failed.", hExc);
      handleHibernateException(hExc, "Committing");
      // throws ApplicationException, PersistenceExternalError
    }
  }

  /**
   * Reset the id of the {@link PersistentBean PersistentBeans} in
   * <code>persistentBeans</code> to <code>null</code>.
   */
  @MethodContract(
    pre = @Expression("persistentBeans != null"),
    post = @Expression("for (PersistentBean<?> persistentBean : persistentBeans) {persistentBean.id == null}")
  )
  private void resetId(Set<PersistentBean<?>> persistentBeans) {
    assert persistentBeans != null;
    for (PersistentBean<?> persistentBean : persistentBeans) {
      persistentBean.setPersistenceId(null);
    }
  }

  /**
   * For {@link #isCreated(PersistentBean) created} persistent beans, the
   * {@link PersistentBean#getPersistenceId()} is reset to <code>null</code> (part of rollback).
   */
  public final void cancelTransaction() {
    LOG.debug("Cancelling transaction.");
    pre(! isInTransaction(), PENDING_TRANSACTION);
    assert $tx != null;
    try {
      $tx.rollback();
      resetId($created);
      // $deleted objects get to keep there original id, as they are not really deleted
    }
    catch (HibernateException hExc) {
      throw new ExternalError("could not rollback Hibernate transaction. This is serious.", hExc);
    }
    finally {
      $tx = null;
      setInTransaction(false);
      $deleted = new HashSet<PersistentBean<?>>();
      $created = new HashSet<PersistentBean<?>>();
    }
  }

  @MethodContract(
    pre  = @Expression("session != null"),
    post = {}
  )
  public final void createPersistentBean(final PersistentBean<?> pb) throws PropertyException, ApplicationException {
    LOG.debug("Creating new record for bean \"" + pb + "\" ...");
    dependency(getSession(), "session");
    preArgumentNotNull(pb, "pb");
    pre(pb.getPersistenceId() == null, SHOULD_HAVE_NO_ID_IN_PERSISTENT_OBJECT);
    pre(isInTransaction(), NO_PENDING_TRANSACTION);
    try {
      LOG.trace("Gather all beans to be created, taking into account cascade");
      List<PersistentBean<?>> allToBeCreated = relatedFreshPersistentBeans(pb);
      // we need to normalize and check all these beans
      Iterator<PersistentBean<?>> iter = allToBeCreated.iterator();
      while (iter.hasNext()) {
        PersistentBean<?> current = iter.next();
        LOG.trace("Normalizing  \"" + current + "\" and checking civility ...");
        current.normalize();
        current.checkCivility(); // PropertyException
// MUDO (jand) package all PropertyExceptions for all beans together; don't stop after one!!!
        LOG.trace("\"" + current + "\" checks out ok");
      }
      getSession().save(pb);
        // cascade done by Hibernate; all elements of allToBeCreated are created
// IDEA (jand) by doing the cascade ourselfs, we might be able to get better exceptions
      $created.addAll(allToBeCreated);
      if (LOG.isDebugEnabled()) {
        LOG.debug("Creating succesfull.");
        iter = allToBeCreated.iterator();
        while (iter.hasNext()) {
          PersistentBean<?> current = iter.next();
          LOG.debug("    generated " + current.getPersistenceId() + " as id for " + current);
        }
      }
    }
    catch (HibernateException hExc) {
      LOG.debug("Creation of new record failed.");
      handleHibernateException(hExc, "Creating");
      // throws ApplicationException, ExternalError
    }
    assert pb.getPersistenceId() != null;
  }

  /**
   * <code>pb</code> is part of the result
   *
   * @todo move method static as utility method
   */
  @MethodContract(
    pre = {
      @Expression("pb != null"),
      @Expression("pd.id == null")
    },
    post = {}
  )
  private List<PersistentBean<?>> relatedFreshPersistentBeans(PersistentBean<?> pb) {
    assert pb != null;
    assert pb.getPersistenceId() == null;
    List<PersistentBean<?>> result = new LinkedList<PersistentBean<?>>();
    result.add(pb);
    int current = 0;
    while (current < result.size()) {
      PersistentBean<?> currentPb = result.get(current);
      current++;
      PropertyDescriptor[] pds = PropertyUtils.getPropertyDescriptors(currentPb);
      for (int i = 0; i < pds.length; i++) {
        PersistentBean<?> related = relatedPeristentBean(currentPb, pds[i]);
        if ((related != null) && (related.getPersistenceId() == null) &&  (! result.contains(related))) {
            /* if it is a fresh bean and it is the first time that we encounter it,
             * it is to be part of the result;
             * we also need to process it further: remember it on the agenda */
          result.add(related); // adds at the end of the list; size++
        }
      }
    }
    return Collections.unmodifiableList(result);
  }

  /**
   * The value of the property <code>pd</code> of <code>pb</code>, if
   * <ul>
   *    <li>it is readable</li>
   *    <li>it is a {@link PersistentBean}
   * </ul>
   * <code>null</code> otherwise (also if there is an exception reading).
   */
  @MethodContract(
    pre = {
      @Expression("pb != null"),
      @Expression("pd != null")
    },
    post = {}
  )
  private PersistentBean<?> relatedPeristentBean(PersistentBean<?> pb, PropertyDescriptor pd) {
    assert pb != null;
    assert pd != null;
    PersistentBean<?> result = null;
    if (PersistentBean.class.isAssignableFrom(pd.getPropertyType())) {
      Method rm = pd.getReadMethod();
      if (rm != null) {
        // found a property that returns a related bean; get it
        try {
          result = (PersistentBean<?>)rm.invoke(pb);
        }
        catch (IllegalArgumentException iaExc) {
          assert false : "Should not happen, since there are no arguments, and the implicit argument is "
                         + "not null and of the correct type";
        }
        catch (IllegalAccessException e) {
          assert false : "IllegalAccessException should not happen: " + e;
        }
        catch (InvocationTargetException e) {
          assert false : "InvocationTargetException should not happen: " + e;
        }
        catch (NullPointerException e) {
          assert false : "NullPointerException should not happen: " + e;
        }
        /* ExceptionInInitializerError can occur with invoke, but we do not
           take into account errors */
      }
    }
    return result;
  }

  @MethodContract(
    pre  = @Expression("session != null"),
    post = {}
  )
  public <_Id_ extends Serializable, _PersistentBean_ extends PersistentBean<_Id_>>
  _PersistentBean_ retrievePersistentBean(final Class<_PersistentBean_> persistentBeanType, final _Id_ id)
      throws IdNotFoundException {
    LOG.debug("Retrieving record with id = " + id + " ...");
    dependency(getSession(), "session");
    preArgumentNotNull(persistentBeanType, "persistentBeanType");
    preArgumentNotNull(id, "id");
    _PersistentBean_ result = null;
    try {
      @SuppressWarnings("unchecked")
      PersistentBean<?> candidate = (PersistentBean)getSession().get(persistentBeanType, id);
      if (candidate == null) {
        LOG.debug("Record not found");
        throw new IdNotFoundException(persistentBeanType, id, null, null);
      }
      // When hibernate caching is active they can give back a object with
      // the correct ID but of the wrong type, so this extra check is
      // introduced as a workaround for it. A posting was done to the hibernate
      // forum to ask if it is a bug or if we are missing something.
      //
      // URL: http://forum.hibernate.org/viewtopic.php?t=938177
      if (! persistentBeanType.isInstance(result)) {
        LOG.debug("Incorrect record found (Wrong type");
        throw new IdNotFoundException(persistentBeanType, id, null, null);
      }
      @SuppressWarnings("unchecked")
      _PersistentBean_ persistentBean = (_PersistentBean_)candidate;
      result = persistentBean;
    }
    catch (ClassCastException ccExc) {
      unexpectedException(ccExc, "retrieved object was not a PersistentBean");
    }
    catch (HibernateException hExc) {
      // this cannot be that we did not find an object with that id, since we use get
      throw new ExternalError("problem getting record from DB", hExc);
    }
    assert result != null;
    assert result.getPersistenceId().equals(id);
    assert persistentBeanType.isInstance(result);
    if (LOG.isDebugEnabled()) {
      LOG.debug("Retrieval succeeded (" + result + ")");
    }
    return result;
  }

  @MethodContract(
    pre  = @Expression("session != null"),
    post = {}
  )
  public <_PersistentBean_ extends PersistentBean<?>>
  Set<_PersistentBean_> retrieveAllPersistentBeans(final Class<_PersistentBean_> persistentBeanType, final boolean retrieveSubClasses) {
    LOG.debug("Retrieving all records of type \"" + persistentBeanType + "\" ...");
    dependency(getSession(), "session");
    preArgumentNotNull(persistentBeanType, "persistentBeanType");
    Set<_PersistentBean_> results = new HashSet<_PersistentBean_>();
    try {
      if (retrieveSubClasses) {
        @SuppressWarnings("unchecked")
        List<_PersistentBean_> list = getSession().createCriteria(persistentBeanType).list();
        results.addAll(list);
      }
      else {
        try {
          @SuppressWarnings("unchecked")
          List<_PersistentBean_> list = getSession().createQuery("FROM " + persistentBeanType.getName() +
                                                                 " as persistentObject WHERE persistentObject.class = " +
                                                                 persistentBeanType.getName()).list();
          results.addAll(list);
        }
        catch (QueryException qExc) {
          if (qExc.getMessage().matches("could not resolve property: class of: .*")) {
            @SuppressWarnings("unchecked")
            List<_PersistentBean_> list = getSession().createCriteria(persistentBeanType).list();
            results.addAll(list);
          }
        }
      }
    }
    catch (HibernateException hExc) {
      throw new ExternalError("problem getting all instances of " + persistentBeanType.getName(), hExc);
    }
    assert results != null;
    LOG.debug("Retrieval succeeded (" + results.size() + " objects retrieved)"); //$NON-NLS-2$
    return results;
  }

  @MethodContract(
    pre  = @Expression("session != null"),
    post = {}
  )
  public final void updatePersistentBean(final PersistentBean<?> pb) throws PropertyException, ApplicationException {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Updating bean \"" + pb + "\" ...");
    }
    dependency(getSession(), "session");
    preArgumentNotNull(pb, "pb");
    pre(pb.getPersistenceId() != null, NO_ID_IN_PERSISTENT_OBJECT);
    pre(isInTransaction(), NO_PENDING_TRANSACTION);
    try {
      if (LOG.isTraceEnabled()) {
        LOG.trace("Normalizing  \"" + pb + "\" ...");
      }
      pb.normalize();
      pb.checkCivility(); // PropertyException
// MUDO (jand) normalize and checkCivility off all reachable PB's (cascade)
      if (LOG.isTraceEnabled()) {
        LOG.trace("Normalization of \"" + pb + "\" done.");
      }
      getSession().update(pb);
      /*
       * If there is a persistent instance with the same identifier, different
       * from this pb, an exception is thrown. This cannot happen since pb is
       * fresh from the DB: we got it with retrieve or created it ourself.
       */
      LOG.debug("Update succeeded.");
    }
    catch (HibernateException hExc) {
      LOG.debug("Update failed.");
      handleHibernateException(hExc, "updating");
      // throws ApplicationException, PersistenceExternalError
      // MUDO need code to throw IdNotFoundException
    }
  }

  @MethodContract(
    pre  = @Expression("session != null"),
    post = {}
  )
  public void deletePersistentBean(final PersistentBean<?> pb) throws ApplicationException {
    LOG.debug("Deleting persistent bean \"" + pb + "\" ...");
    dependency(getSession(), "session");
    preArgumentNotNull(pb, "pb");
    pre(pb.getPersistenceId() != null, NO_ID_IN_PERSISTENT_OBJECT);
    pre(isInTransaction(), NO_PENDING_TRANSACTION);
    try {
      getSession().delete(pb);
      $deleted.add(pb);
// MUDO (jand) take into account cascade delete
    }
    catch (HibernateException hExc) {
      LOG.debug("Deletion failed.");
      handleHibernateException(hExc, "Deleting");
      // throws ApplicationException, PersistenceExternalError
      // MUDO need code to throw IdNotFoundException
    }
    LOG.debug("Deletion succeeded.");
  }



  /*<property name="created">*/
  //------------------------------------------------------------------

  /**
   * Returns true when the given persistent bean has been created (i.e.,
   * has been used as a parameter in {@link #createPersistentBean(PersistentBean)});
   * returns false otherwise.
   *
   * @param  pb
   * @basic
   */
  public boolean isCreated(final PersistentBean<?> pb) {
    return $created.contains(pb);
  }

  @Invars({
    @Expression("$created != null"),
    @Expression("! $created.contains(null)")
  })
  private Set<PersistentBean<?>> $created = new HashSet<PersistentBean<?>>();

  /*</property>*/



  /*<property name="deleted">*/
  //------------------------------------------------------------------

  public boolean isDeleted(final PersistentBean<?> pb) {
    return $deleted.contains(pb);
  }

  @Invars({
    @Expression("$deleted != null"),
    @Expression("! $deleted.contains(null)")
  })
  private Set<PersistentBean<?>> $deleted = new HashSet<PersistentBean<?>>();

  /*</property>*/



 /*<property name="inTransaction">*/
 //------------------------------------------------------------------

  public final boolean isInTransaction() {
    return $isInTransaction;
  }

  /**
   * Set the given boolean value, reflecting whether a transaction is open
   * or not.
   */
  @MethodContract(
    post = @Expression("inTransaction == _inTransaction")
  )
  protected final void setInTransaction(final boolean inTransaction) {
    $isInTransaction = inTransaction;
  }

  private boolean $isInTransaction;

  /*</property>*/

}
