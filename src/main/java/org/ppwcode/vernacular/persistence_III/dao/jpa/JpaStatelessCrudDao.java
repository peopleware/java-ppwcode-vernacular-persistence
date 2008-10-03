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

package org.ppwcode.vernacular.persistence_III.dao.jpa;


import static org.ppwcode.vernacular.exception_II.ProgrammingErrorHelpers.dependency;
import static org.ppwcode.vernacular.exception_II.ProgrammingErrorHelpers.preArgumentNotNull;
import static org.ppwcode.vernacular.exception_II.ProgrammingErrorHelpers.unexpectedException;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.Query;
import javax.persistence.TransactionRequiredException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ppwcode.vernacular.exception_II.SemanticException;
import org.ppwcode.vernacular.persistence_III.IdNotFoundException;
import org.ppwcode.vernacular.persistence_III.PersistentBean;
import org.ppwcode.vernacular.persistence_III.dao.StatelessCrudDao;
import org.ppwcode.vernacular.semantics_VI.exception.CompoundPropertyException;
import org.ppwcode.vernacular.semantics_VI.exception.PropertyException;

/**
 * @mudo work on transaction annotations
 * @mudo unit tests
 * @mudo catch exceptions and log only external exceptions and programming errors
 */
@Stateless
@TransactionManagement(TransactionManagementType.CONTAINER)
public class JpaStatelessCrudDao extends AbstractJpaDao implements StatelessCrudDao {

  private final static Log _LOG = LogFactory.getLog(JpaStatelessCrudDao.class);

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

  private <_Id_ extends Serializable> void normalizeAndValidateGraph(PersistentBean<_Id_> pb) throws CompoundPropertyException {
    pb.normalize();
    try {

      CREATE PB HELPERS, DO IT THERE IN PUBLIC: normalize and validate all graph

      pb.checkCivility();
    }
  }

  /**
   * Create or update. Create if ID is null, update if not.
   */
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public <_Id_ extends Serializable, _PB_ extends PersistentBean<_Id_>> _PB_ mergePersistentBean(_PB_ pb) throws SemanticException {
    _LOG.debug("Merging persistent bean: " + pb);
    assert preArgumentNotNull(pb, "pb");
    if (_LOG.isDebugEnabled()) {
      _LOG.debug("pb.id = " + pb.getPersistenceId() +
                 ((pb.getPersistenceId() == null) ? " == null: persistent bean will be created" :
                                         " != null: persistent bean will be update"));
    }
    assert dependency(getEntityManager(), "enitytManager");
    _PB_ newPb = null;
    try {
      newPb = getEntityManager().merge(pb); // not committed yet
      /* we merge first, so that we do not need to worry about uninitialized lazy relationships while validating */
      normalizeAndValidateGraph(pb); // throws PropertyException; if, this transaction will be rolled back
    }
    catch (PropertyException exc) {
      if (_LOG.isDebugEnabled()) {
        _LOG.debug("persistent bean offered for merge is not civilized", exc);
      }
      throw exc;
    }
    // MUDO versioning problem??
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
    assert newPb != null;
    assert newPb.getPersistenceId() != null;
    _LOG.debug("merge succeeded; returning new persistent bean: " + newPb);
    return newPb;
  }

  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public <_Id_ extends Serializable, _PB_ extends PersistentBean<_Id_>> _PB_ deletePersistentBean(_PB_ pb) throws SemanticException {
    _LOG.debug("Deleting persistent bean: " + pb);
    assert preArgumentNotNull(pb, "pb");
    assert dependency(getEntityManager(), "enitytManager");
    try {
      getEntityManager().remove(pb);
    }
    // MUDO versioning problem??
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
    _LOG.debug("delete succeeded; returning new persistent bean: " + pb);
    return pb;
  }

}
