/*<license>
Copyright 2004 - $Date$ by PeopleWare n.v..

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

import java.beans.PropertyDescriptor;
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
import org.ppwcode.bean_VI.PropertyException;
import org.ppwcode.exception_N.SemanticException;
import org.ppwcode.metainfo_I.Copyright;
import org.ppwcode.metainfo_I.License;
import org.ppwcode.metainfo_I.vcs.SvnInfo;
import org.ppwcode.vernacular.exception_N.InternalException;
import org.ppwcode.vernacular.persistence_III.PersistenceConfigurationError;
import org.ppwcode.vernacular.persistence_III.PersistenceExternalError;
import org.ppwcode.vernacular.persistence_III.PersistenceIllegalArgumentError;
import org.ppwcode.vernacular.persistence_III.PersistenceIllegalStateError;
import org.ppwcode.vernacular.persistence_III.PersistentBean;
import org.ppwcode.vernacular.persistence_III.dao.AsyncCrudDao;
import org.toryt.annotations_I.Expression;
import org.toryt.annotations_I.Invars;
import org.toryt.annotations_I.MethodContract;
import org.toryt.annotations_I.Throw;


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
 */
@Copyright("2004 - $Date: 2008-06-26 21:17:17 +0200 (Thu, 26 Jun 2008) $, PeopleWare n.v.")
@License(APACHE_V2)
@SvnInfo(revision = "$Revision: 1398 $",
         date     = "$Date: 2008-06-26 21:17:17 +0200 (Thu, 26 Jun 2008) $")
public class Hibernate2AsyncCrudDao extends AbstractHibernate2Dao implements AsyncCrudDao {

  private static final Log LOG = LogFactory.getLog(Hibernate2AsyncCrudDao.class);



  private static final String NULL_SESSION = "Session is null";
  private static final String NO_PENDING_TRANSACTION = "No transaction pending";
  private static final String PENDING_TRANSACTION = "There is a transaction still pending";
  private static final String NO_PERSISTENT_OBJECT = "No persistent object";
  private static final String NO_ID_IN_PERSISTENT_OBJECT = "No id in persistent object";
  private static final String WRONG_SUBTYPE = " not a subtype of PersistentBean";



  /*<property name="session">*/
  //------------------------------------------------------------------

  @Override
  @MethodContract(
    post = @Expression(value = "! 'inTransaction",
                       description = "Cannot be made true by this method when it is false in the old state. " +
                                     "So the only option for the implementer is to throw an exception when this occurs."),
    exc = @Throw(type = PersistenceIllegalStateError.class,
                 cond = @Expression(value = "true"))
  )
  public final void setSession(final Session session) throws PersistenceIllegalStateError {
    if (isInTransaction()) {
      throw new PersistenceIllegalStateError("Cannot set session now, transaction still in use", null);
    }
    super.setSession(session);
  }

  /*</property>*/



  @Invars(@Expression("inTransaction == ($tx != null"))
  private Transaction $tx;

  @MethodContract(
    post = {},
    exc = @Throw(type = PersistenceConfigurationError.class,
                 cond = @Expression("session == null"))
  )
  public final void startTransaction() throws PersistenceIllegalStateError, PersistenceConfigurationError, PersistenceExternalError {
    LOG.debug("Starting hibernate transaction ...");
    if (getSession() == null) {
      throw new PersistenceConfigurationError(NULL_SESSION, null);
    }
    if (isInTransaction()) {
      throw new PersistenceIllegalStateError(PENDING_TRANSACTION, null);
    }
    assert $tx == null;
    try {
      $tx = getSession().beginTransaction();
      setInTransaction(true);
    }
    catch (HibernateException hExc) {
      throw new PersistenceExternalError("Could not create Hibernate transaction", hExc);
    }
    LOG.debug("Hibernate transaction started.");
  }

  public final void commitTransaction() throws InternalException, PersistenceIllegalStateError, PersistenceExternalError {
    LOG.debug("Starting commit ...");
    if (!isInTransaction()) {
      throw new PersistenceIllegalStateError(NO_PENDING_TRANSACTION, null);
    }
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
      // throws InternalException, PersistenceExternalError
    }
  }

  /**
   * Reset the id of the {@link PersistentBean PersistentBeans} in
   * <code>persistentBeans</code> to <code>null</code>.
   */
  @MethodContract(
    pre = @Expression("persistentBeans != null")
    post = @Expression("for (PersistentBean<?> persistentBean : persistentBeans) {persistentBean.id == null}")
  )
  private void resetId(Set<PersistentBean<?>> persistentBeans) {
    assert persistentBeans != null;
    for (PersistentBean<?> persistentBean : persistentBeans) {
      persistentBean.setId(null);
    }
  }

  /**
   * For {@link #isCreated(PersistentBean) created} persistent beans, the
   * {@link PersistentBean#getId()} is reset to <code>null</code> (part of rollback).
   *
   * @throws    TechnicalException
   *            isInTransaction();
   */
  public final void cancelTransaction() throws TechnicalException {
    LOG.debug("Cancelling transaction.");
    if (!isInTransaction()) {
      throw new TechnicalException(NO_PENDING_TRANSACTION, null);
    }
    assert $tx != null;
    try {
      $tx.rollback();
      resetId($created);
      // $deleted objects get to keep there original id, as they are not really deleted
    }
    catch (HibernateException hExc) {
      throw new TechnicalException("could not rollback "
                                       + "Hibernate transaction. "
                                       + "this is serious.",
                                   hExc);
    }
    finally {
      $tx = null;
      setInTransaction(false);
      $deleted = new HashSet();
      $created = new HashSet();
    }
  }

  /**
   * After this method, <code>pb</code> will have an fresh id. Only during commit will
   * this <code>pb</code> actually be created in the DB, so if that fails, we need
   * to call {@link #cancelTransaction()}. This will reset the id to <code>null</code>.
   *
   * @post isCreated(pb);
   * @throws    TechnicalException
   *            !isInTransaction()
   *            || getSession() == null
   *            || pb == null
   *            || pb.getId() != null;
   */
  public final void createPersistentBean(final PersistentBean pb)
      throws CompoundPropertyException, TechnicalException {
    LOG.debug("Creating new record for bean \"" + pb + "\" ..."); //$NON-NLS-2$
    if (getSession() == null) {
      throw new TechnicalException(NULL_SESSION, null);
    }
    if (!isInTransaction()) {
      throw new TechnicalException(NO_PENDING_TRANSACTION, null);
    }
    if (pb == null) {
      throw new TechnicalException(NO_PERSISTENT_OBJECT, null);
    }
    if (pb.getId() != null) {
      throw new TechnicalException("pb cannot have an id",
                                   null);
    }
    try {
      LOG.trace("Gather all beans to be created, taking into account cascade");
      List allToBeCreated = relatedFreshPersistentBeans(pb);
      // we need to normalize and check all these beans
      Iterator iter = allToBeCreated.iterator();
      while (iter.hasNext()) {
        PersistentBean current = (PersistentBean)iter.next();
        LOG.trace("Normalizing  \"" + current + "\" and checking civility ...");
        current.normalize();
        current.checkCivility(); // CompoundPropertyException
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
          PersistentBean current = (PersistentBean)iter.next();
          LOG.debug("    generated " + current.getId() + " as id for " + current);
        }
      }
    }
    catch (HibernateException hExc) {
      LOG.debug("Creation of new record failed.");
      handleHibernateException(hExc, "Creating", pb);
    }
    assert pb.getId() != null;
  }

  /**
   * <code>pb</code> is part of the result
   *
   * @todo move method static as utility method
   * @pre pb != null;
   * @pre pb.getId() == null;
   */
  private List relatedFreshPersistentBeans(PersistentBean pb) {
    assert pb != null;
    assert pb.getId() == null;
    List result = new LinkedList();
    result.add(pb);
    int current = 0;
    while (current < result.size()) {
      PersistentBean currentPb = (PersistentBean)result.get(current);
      current++;
      PropertyDescriptor[] pds = PropertyUtils.getPropertyDescriptors(currentPb);
      for (int i = 0; i < pds.length; i++) {
        PersistentBean related = relatedPeristentBean(currentPb, pds[i]);
        if ((related != null) && (related.getId() == null) &&  (! result.contains(related))) {
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
   * The value if the property <code>pd</code> of <code>pb</code>, if
   * <ul>
   *    <li>it is readable</li>
   *    <li>it is a {@link PersistentBean}
   * </ul>
   * <code>null</code> otherwise 9also if there is an exception reading).
   *
   * @pre pb != null;
   * @pre pd != null;
   */
  private PersistentBean relatedPeristentBean(PersistentBean pb, PropertyDescriptor pd) {
    assert pb != null;
    assert pd != null;
    PersistentBean result = null;
    if (PersistentBean.class.isAssignableFrom(pd.getPropertyType())) {
      Method rm = pd.getReadMethod();
      if (rm != null) {
        // found a property that returns a related bean; get it
        try {
          result = (PersistentBean)rm.invoke(pb, null);
        }
        catch (IllegalArgumentException iaExc) {
          assert false : "Should not happen, since there are no " //$NON-NLS-1$
                         + "arguments, and the implicit argument is " //$NON-NLS-1$
                         + "not null and of the correct type"; //$NON-NLS-1$
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

  /**
   * @param     id
   *            The ID of the PersistentBean to retrieve
   * @param     persistentObjectType
   *            The type of PersistentBean (subclass) to retrieve.
   * @throws    IdNotFoundException
   *            No PersistentBean with <param>id</param> of type
   *            <param>persistentObjectType</param>was found.
   * @throws    TechnicalException
   *            getSession() == null
   *            || id == null
   *            || persistentObjectType == null
   *            || !PersistentBean.class
   *                    .isAssignableFrom(persistentObjectType);
   */
  public PersistentBean retrievePersistentBean(
      final Long id,
      final Class persistentObjectType)
          throws IdNotFoundException, TechnicalException {
    LOG.debug("Retrieving record with id = " + id + " ..."); //$NON-NLS-2$
    if (getSession() == null) {
      throw new TechnicalException(NULL_SESSION, null);
    }
    if (id == null) {
      throw new IdNotFoundException(id, "ID_IS_NULL",
                                    null, persistentObjectType);
    }
    if (persistentObjectType == null) {
      throw new TechnicalException(NO_PERSISTENT_OBJECT, null);
    }
    if (!PersistentBean.class.isAssignableFrom(persistentObjectType)) {
      throw new TechnicalException(persistentObjectType.toString()
                                       + WRONG_SUBTYPE,
                                   null);
    }
    PersistentBean result = null;
    try {
      result = (PersistentBean)getSession().get(persistentObjectType, id);
      if (result == null) {
        LOG.debug("Record not found");
        throw new IdNotFoundException(id, null, null, persistentObjectType);
      }
      // When hibernate caching is active they can give back a object with
      // the correct ID but of the wrong type, so this extra check is
      // introduced as a workaround for it. A posting was done to the hibernate
      // forum to ask if it is a bug or if we are missing something.
      //
      // URL: http://forum.hibernate.org/viewtopic.php?t=938177
      if (!persistentObjectType.isInstance(result)) {
        LOG.debug("Incorrect record found (Wrong type");
        throw new IdNotFoundException(id, null, null, persistentObjectType);
      }

    }
    catch (ClassCastException ccExc) {
      throw new TechnicalException("retrieved object was not a PersistentBean",
                                   ccExc);
    }
    catch (HibernateException hExc) {
      // this cannot be that we did not find an object with that id, since we
      // use get
      throw new TechnicalException("problem getting record from DB", hExc);
    }
    assert result != null;
    assert result.getId().equals(id);
    assert persistentObjectType.isInstance(result);
    if (LOG.isDebugEnabled()) {
      LOG.debug("Retrieval succeeded (" + result + ")"); //$NON-NLS-2$
    }
    return result;
  }

  @MethodContract(
    post = {},
    exc = @Throw(type = PersistenceConfigurationError.class,
                 cond = @Expression("session == null"))
  )
  public <_PersistentBean_ extends PersistentBean<?>>
  Set<_PersistentBean_> retrieveAllPersistentBeans(final Class<_PersistentBean_> persistentBeanType, final boolean retrieveSubClasses)
      throws PersistenceIllegalArgumentError, PersistenceConfigurationError, PersistenceExternalError {
    LOG.debug("Retrieving all records of type \"" + persistentBeanType + "\" ...");
    if (getSession() == null) {
      throw new PersistenceConfigurationError(NULL_SESSION, null);
    }
    if (persistentBeanType == null) {
      throw new PersistenceIllegalArgumentError("persistentObjectType cannot be null", null);
    }
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
            results.addAll(list);
          }
        }
      }
    }
    catch (HibernateException hExc) {
      throw new PersistenceExternalError("problem getting all instances of " + persistentBeanType.getName(), hExc);
    }
    assert results != null;
    LOG.debug("Retrieval succeeded (" + results.size() + " objects retrieved)"); //$NON-NLS-2$
    return results;
  }

  @MethodContract(
    post = {},
    exc = @Throw(type = PersistenceConfigurationError.class,
                 cond = @Expression("session == null"))
  )
  public final void updatePersistentBean(final PersistentBean<?> pb) throws PropertyException, InternalException,
      PersistenceIllegalArgumentError, PersistenceIllegalStateError, PersistenceConfigurationError, PersistenceExternalError {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Updating bean \"" + pb + "\" ...");
    }
    if (getSession() == null) {
      throw new PersistenceConfigurationError(NULL_SESSION, null);
    }
    if (!isInTransaction()) {
      throw new PersistenceIllegalStateError(NO_PENDING_TRANSACTION, null);
    }
    if (pb == null) {
      throw new PersistenceIllegalArgumentError(NO_PERSISTENT_OBJECT, null);
    }
    if (pb.getId() == null) {
      throw new PersistenceIllegalArgumentError(NO_ID_IN_PERSISTENT_OBJECT, null);
    }
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
      // throws InternalException, PersistenceExternalError
      // MUDO need code to throw IdNotFoundException
    }
  }

  @MethodContract(
    post = {},
    exc = @Throw(type = PersistenceConfigurationError.class,
                 cond = @Expression("session == null"))
  )
  public void deletePersistentBean(final PersistentBean<?> pb) throws InternalException, PersistenceIllegalArgumentError,
      PersistenceIllegalStateError, PersistenceConfigurationError, PersistenceExternalError {
    LOG.debug("Deleting persistent bean \"" + pb + "\" ...");
    if (getSession() == null) {
      throw new PersistenceConfigurationError(NULL_SESSION, null);
    }
    if (!isInTransaction()) {
      throw new PersistenceIllegalStateError(NO_PENDING_TRANSACTION, null);
    }
    if (pb == null) {
      throw new PersistenceIllegalArgumentError(NO_PERSISTENT_OBJECT, null);
    }
    if (pb.getId() == null) {
      throw new PersistenceIllegalArgumentError(NO_ID_IN_PERSISTENT_OBJECT, null);
    }
    try {
      getSession().delete(pb);
      $deleted.add(pb);
// MUDO (jand) take into account cascade delete
    }
    catch (HibernateException hExc) {
      LOG.debug("Deletion failed.");
      handleHibernateException(hExc, "Deleting");
      // throws InternalException, PersistenceExternalError
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
