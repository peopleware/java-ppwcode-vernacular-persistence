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

package org.ppwcode.vernacular.persistence_III.dao.jpa;


import static org.apache.commons.beanutils.PropertyUtils.getPropertyDescriptors;
import static org.ppwcode.util.reflect_I.PropertyHelpers.propertyValue;
import static org.ppwcode.util.reflect_I.PropertyHelpers.setPropertyValue;
import static org.ppwcode.vernacular.exception_II.ProgrammingErrorHelpers.dependency;
import static org.ppwcode.vernacular.exception_II.ProgrammingErrorHelpers.newAssertionError;
import static org.ppwcode.vernacular.exception_II.ProgrammingErrorHelpers.pre;
import static org.ppwcode.vernacular.exception_II.ProgrammingErrorHelpers.preArgumentNotNull;
import static org.ppwcode.vernacular.exception_II.ProgrammingErrorHelpers.unexpectedException;
import static org.ppwcode.vernacular.persistence_III.PersistentBeanHelpers.upstreamPersistentBeans;
import static org.ppwcode.vernacular.semantics_VI.bean.RousseauBeanHelpers.wildExceptions;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.TransactionRequiredException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ppwcode.vernacular.exception_II.InternalException;
import org.ppwcode.vernacular.exception_II.SemanticException;
import org.ppwcode.vernacular.persistence_III.AlreadyChangedException;
import org.ppwcode.vernacular.persistence_III.IdNotFoundException;
import org.ppwcode.vernacular.persistence_III.PersistentBean;
import org.ppwcode.vernacular.persistence_III.VersionedPersistentBean;
import org.ppwcode.vernacular.persistence_III.dao.Dao;
import org.ppwcode.vernacular.persistence_III.dao.RequiredTransactionStatelessCrudDao;
import org.ppwcode.vernacular.semantics_VI.exception.CompoundPropertyException;
import org.ppwcode.vernacular.semantics_VI.exception.PropertyException;

/**
 * <p>A stateless {@link Dao DAO} that offers generalized CRUD methods. Methods here are executed either in an existing
 *   transaction or, if no transaction exists, in a new transaction (~ required transaction). Methods follow the
 *   ppwcode exception vernacular as much as possible. Exceptions thrown during commit or roll-back are not handled
 *   according to the vernacular.</p>
 * <p>When used as a session bean, add</p>
 * <pre>
 * &#64;TransactionManagement(TransactionManagementType.BEAN)
 * &#64;TransactionAttribute(TransactionAttributeType.REQUIRED)
 * ...
 * </pre>
 * <p>to the type.</p>
 *
 * @mudo unit tests
 */
public class JpaStatelessCrudDao extends AbstractJpaDao implements RequiredTransactionStatelessCrudDao {

  private final static Log _LOG = LogFactory.getLog(JpaStatelessCrudDao.class);


  /* only 1 database access, thus SUPPORTS would suffice; yet, to avoid dirty reads, as per JPA recomendation: Required */
  public <_PersistentBean_ extends PersistentBean<?>> Set<_PersistentBean_>
  retrieveAllPersistentBeans(Class<_PersistentBean_> persistentBeanType, boolean retrieveSubClasses) {
    _LOG.debug("Retrieving all records of type \"" + persistentBeanType + "\" ...");
    assert preArgumentNotNull(persistentBeanType, "persistentBeanType");
    assert dependency(getEntityManager(), "entityManager");
    try {
      Query query = null;
      if (retrieveSubClasses) {
        query = getEntityManager().createQuery("FROM " + persistentBeanType.getName());
      }
      else {
        query = getEntityManager().createQuery("FROM " + persistentBeanType.getName() +
                                           " as persistentObject WHERE persistentObject.class = " +
                                           persistentBeanType.getName());
        // MUDO this approach is untested with JPA
      }
      @SuppressWarnings("unchecked")
      List<_PersistentBean_> result = query.getResultList();
      assert result != null;
      Set<_PersistentBean_> setResult = new HashSet<_PersistentBean_>(result);
      _LOG.debug("Retrieval succeeded (" + setResult.size() + " objects retrieved)");
      return setResult;
    }
    catch (IllegalArgumentException iaExc) {
      unexpectedException(iaExc, "query string problem");
    }
    catch (IllegalStateException isExc) {
      unexpectedException(isExc);
    }
    return null; // keep compiler happy
  }

  /* only 1 database access, thus SUPPORTS would suffice; yet, to avoid dirty reads, as per JPA recomendation: Required */
  public <_Id_ extends Serializable, _PersistentBean_ extends PersistentBean<_Id_>>
  _PersistentBean_ retrievePersistentBean(Class<_PersistentBean_> persistentBeanType, _Id_ id)
      throws IdNotFoundException {
    _LOG.debug("Retrieving record with id = " + id + " of type " + persistentBeanType + " ...");
    assert preArgumentNotNull(persistentBeanType, "persistentBeanType");
    assert preArgumentNotNull(id, "id");
    assert dependency(getEntityManager(), "entityManager");
    _PersistentBean_ result = null;
    try {
      result = getEntityManager().find(persistentBeanType, id);
    }
    catch (IllegalArgumentException exc) {
      unexpectedException(exc, "" + persistentBeanType + "is not a type the JPA entity manager recognizes");
    }
    if (result == null) {
      _LOG.debug("Record not found");
      throw new IdNotFoundException(persistentBeanType, id);
    }
    else {
      assert id.equals(result.getPersistenceId());
      assert result.getClass() == persistentBeanType;
      if (_LOG.isDebugEnabled()) {
        _LOG.debug("Retrieval succeeded (" + result + ")");
      }
      return result;
    }
  }

  public <_Id_ extends Serializable, _Version_ extends Serializable, _PB_ extends VersionedPersistentBean<_Id_, _Version_>>
  _PB_ createPersistentBean(_PB_ pb) throws InternalException {
    _LOG.debug("Creating persistent bean: " + pb);
    assert preArgumentNotNull(pb, "pb");
    assert pre(pb.getPersistenceId() == null);
    assert pre(pb.getPersistenceVersion() == null);
    assert dependency(getEntityManager(), "enitytManager");
    // Since we are only persisting pb, we only need to normalize pb
    pb.normalize();
    /* for all first level upstream associations (to-one): replace the referenced object with a fresh copy
     * with that id from the database.
     */
    replaceUpstreamBeansWithManagedEntity(pb);
    /* now we persist; this isn't committed yet, but we want access to lazy loaded sets when we calculate
     * wild exceptions
     */
    try {
      getEntityManager().persist(pb); // not committed yet, throws load of exceptions
       /* we only validate upstream bean; find them
        * If you submit a pb with associated beans over a to-many relationship, in which there is a new bean or a changed
        * bean, and Cascade is on for persist and / or merge, that data would reach the database unchecked;
        * See lower;
        */
      validate(pb);
      /* Downstream beans might be here; maybe we have a cascade persist on downstream beans, or they exist already.
       * In the future, it might be possible to deal with that case (validation is not trivial. For now, we do the
       * effort to throw an exception: downstream beans must be empty
       */
      noDownstreamBeans(pb);
    }
    catch (IllegalStateException exc) {
      unexpectedException(exc, "entity manager in illegal state for merge");
    }
    catch (IllegalArgumentException exc) {
      unexpectedException(exc, "persistent bean not accepted as merge argument by entity manager");
    }
    catch (TransactionRequiredException exc) {
      unexpectedException(exc, "transaction is required!");
    }
    return pb;
    // now we will get a commit; the exceptions raised here need to be handled still
  }

  private <_PB_> void noDownstreamBeans(_PB_ pb) {
    PropertyDescriptor[] pds = getPropertyDescriptors(pb);
    for (PropertyDescriptor pd : pds) {
      if (Collection.class.isAssignableFrom(pd.getPropertyType())) {
        /* Check that this is an association; this we would be able to see in the generic paramater of the
           collection (PersistentBean), but that is erased. So, to see the difference with a to-many attribute,
           we need a special note, an annotation.
           TODO for now, we don't do this, and consider all collections as to-many associations. We check civility and
                persist for all PersistentBeans in the collection */
        Collection<?> downstreamObjects = propertyValue(pb, pd.getName(), Collection.class);
        // we consider this a true association if there is at least one persistent bean in the collection
        if (containsPersistentBean(downstreamObjects)) {
          newAssertionError("to-many associations (downstream) of beans to persist must be empty", null);
        }
      }
    }
  }

  /**
   * {@inheritDoc}
   *
   * @todo on change of an upstream association, the civility of the old parent is not checked in the current implementation
   */
  public <_Id_ extends Serializable, _Version_ extends Serializable, _PB_ extends VersionedPersistentBean<_Id_, _Version_>>
  _PB_ updatePersistentBean(_PB_ pb) throws InternalException {
    _LOG.debug("Updating persistent bean: " + pb);
    assert preArgumentNotNull(pb, "pb");
    assert pre(pb.getPersistenceId() != null);
    assert pre(pb.getPersistenceVersion() != null);
    assert dependency(getEntityManager(), "enitytManager");
    // Since we are only persisting pb, we only need to normalize pb
    pb.normalize();
//    /* MUDO not done for merg now (it is for persist); we hypothesize that merge does this for us;
//     *      code in comment until we know for sure
//     */
//    /* for all first level upstream associations (to-one): replace the referenced object with a fresh copy
//     * with that id from the database.
//     */
//    replaceUpstreamBeansWithManagedEntity(pb);
    /* now we merge; this isn't committed yet, but we want access to lazy loaded sets when we calculate
     * wild exceptions
     */
    try {
      _PB_ newPb = getEntityManager().merge(pb); // not committed yet, throws load of exceptions
       validate(newPb);
    }
    catch (IllegalStateException exc) {
      unexpectedException(exc, "entity manager in illegal state for merge");
    }
    catch (IllegalArgumentException exc) {
      unexpectedException(exc, "persistent bean not accepted as merge argument by entity manager");
    }
    catch (TransactionRequiredException exc) {
      unexpectedException(exc, "transaction is required!");
    }
    return pb;
    // now we will get a commit; the exceptions raised here need to be handled still
  }

  private void replaceUpstreamBeansWithManagedEntity(VersionedPersistentBean<?, ?> pb) throws AlreadyChangedException, InternalException {
    PropertyDescriptor[] pds = getPropertyDescriptors(pb);
    for (PropertyDescriptor pd : pds) {
      if (VersionedPersistentBean.class.isAssignableFrom(pd.getPropertyType())) {
        // it's a to-one association
        String propertyName = pd.getName();
        PersistentBean<?> upstreamDetachedBean = propertyValue(pb, propertyName, PersistentBean.class);
        if (upstreamDetachedBean != null) {
          if (upstreamDetachedBean.getPersistenceId() == null) {
            newAssertionError("this method cannot persist or merge other beans than pb, and the upstream bean " +
                              upstreamDetachedBean + " does not exist yet in the persistent storage " +
                              "(id == null)", null);
          }
          if ((upstreamDetachedBean instanceof VersionedPersistentBean<?, ?>) &&
              ((VersionedPersistentBean<?, ?>)upstreamDetachedBean).getPersistenceVersion() == null) {
            newAssertionError("this method cannot persist or merge other beans than pb, and the upstream bean " +
                              upstreamDetachedBean + " does not exist yet in the persistent storage " +
                              "(version == null)", null);
          }
          PersistentBean<?> upstreamManagedBean =
            getEntityManager().find(upstreamDetachedBean.getClass(), upstreamDetachedBean.getPersistenceId());
          if (upstreamManagedBean == null) {
            throw new AlreadyChangedException(upstreamDetachedBean, null);
          }
          if ((upstreamDetachedBean instanceof VersionedPersistentBean<?, ?>) &&
              (! ((VersionedPersistentBean<?, ?>)upstreamDetachedBean).getPersistenceVersion().equals(
                                                                                                      ((VersionedPersistentBean<?, ?>)upstreamManagedBean).getPersistenceVersion()))) {
            throw new AlreadyChangedException(upstreamDetachedBean, null);
          }
          setPropertyValue(pb, propertyName, upstreamManagedBean);
        }
      }
    }
  }

  private boolean containsPersistentBean(Collection<?> downstreamObjects) {
    for (Object o : downstreamObjects) {
      if (o instanceof PersistentBean<?>) {
        return true;
      }
    }
    return false;
  }

  private void validate(PersistentBean<?> newPb) throws PropertyException {
     /* we only validate upstream bean; find them
      * If you submit a pb with associated beans over a to-many relationship, in which there is a new bean or a changed
      * bean, and Cascade is on for persist and / or merge, that data would reach the database unchecked;
      * See lower;
      */
    Set<PersistentBean<?>> managedUpstreamBeans = upstreamPersistentBeans(newPb);
    /* we lock all the upstream beans before we do wild exception checks; otherwise, it is possible that somebody else
     * is in the mean time adding or changing another pb to our upstream bean; since we depend on optimistic locking,
     * we need to force that our commit invalidates the other commit; because it happens at the same time, the other
     * change is concurrently doing civilized checks on the parent, based on the old collection of children (without
     * our new child or with the old data of the child we are updating). Some civilized checks might will validate
     * conditions of the child selection as a whole, e.g., that a period in children does not overlap. If we check
     * with the old collection and our new data, and the other transaction also checks with the old collection and
     * its new data, the combination of our new data and the other transactions new data is not validated.
     * We tackle this by inforcing that our commit also updates the version number of the parent, and the other
     * transaction has to apply versioning on our parent to. If we are first, the other transaction is then rolled-back,
     * because we are first. Thus, there is no possibility to add wild data to the database for this case.
     * Getting a write lock is the easiest way to increment the version number for a potentially unchanged object.
     * There is no need to lock pb itself.
     */
    for (PersistentBean<?> upstreamBean : managedUpstreamBeans) {
      if (upstreamBean.getPersistenceId() != null) { // new record; assuming id is set on commit, and not sooner
        getEntityManager().lock(upstreamBean, LockModeType.WRITE); // throws load of exceptions
      }
    }
    /* now, check civility on all upstream beans */
    CompoundPropertyException cpe = wildExceptions(managedUpstreamBeans);
    /* Downstream beans might be here. However, we are not adding changing that set, because the association
     * is owned by the many-side (for which we are a to one). If our detached version had downstream beans,
     * those references are discarded by the merge already.
     */
    handleWildExceptions(newPb, cpe);
  }

  /**
   * If there are exceptions, stop and throw them (but log this first).
   */
  private <_PB_> void handleWildExceptions(_PB_ pb, CompoundPropertyException cpe) throws PropertyException {
    if (! cpe.isEmpty()) {
      if (_LOG.isDebugEnabled()) {
        _LOG.debug("persistent bean offered for persist os not civilized; rollback", cpe);
      }
      getEntityManager().getTransaction().setRollbackOnly();
      cpe.throwIfNotEmpty();
    }
    else {
      _LOG.debug("persist succeeded; attempting commit and returning new persistent bean: " + pb);
    }
  }

  public <_Id_ extends Serializable, _Version_ extends Serializable, _PB_ extends VersionedPersistentBean<_Id_, _Version_>>
  _PB_ deletePersistentBean(_PB_ pb) throws SemanticException {
    _LOG.debug("Deleting persistent bean: " + pb);
    assert preArgumentNotNull(pb, "pb");
    assert dependency(getEntityManager(), "enitytManager");
    try {
      getEntityManager().remove(pb); // database will hurl when not possible, depending on cascade settings
      // checking civility here makes no sense
    }
    catch (IllegalStateException exc) {
      unexpectedException(exc, "entity manager in illegal state for merge");
    }
    catch (IllegalArgumentException exc) {
      unexpectedException(exc, "persistent bean not accepted as merge argument by entity manager");
    }
    catch (TransactionRequiredException exc) {
      _LOG.error("transaction is required!");
      unexpectedException(exc);
    }
    pb.setPersistenceId(null);
    _LOG.debug("delete succeeded; attempting commit and returning new persistent bean: " + pb);
    return pb;
  }

}
