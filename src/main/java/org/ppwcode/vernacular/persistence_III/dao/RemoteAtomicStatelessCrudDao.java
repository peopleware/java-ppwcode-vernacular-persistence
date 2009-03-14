/*<license>
Copyright 2005 - $Date: 2008-12-16 11:48:48 +0100 (Tue, 16 Dec 2008) $ by PeopleWare n.v..

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

package org.ppwcode.vernacular.persistence_III.dao;


import static org.ppwcode.util.exception_III.ProgrammingErrorHelpers.dependency;
import static org.ppwcode.util.exception_III.ProgrammingErrorHelpers.unexpectedException;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ppwcode.vernacular.exception_III.ExternalError;
import org.ppwcode.vernacular.exception_III.ApplicationException;
import org.ppwcode.vernacular.exception_III.handle.ExceptionHandler;
import org.ppwcode.vernacular.persistence_III.IdNotFoundException;
import org.ppwcode.vernacular.persistence_III.PersistentBean;
import org.ppwcode.vernacular.persistence_III.VersionedPersistentBean;
import org.ppwcode.vernacular.persistence_III.dao.AtomicStatelessCrudDao;
import org.ppwcode.vernacular.persistence_III.dao.RequiredTransactionStatelessCrudDao;
import org.toryt.annotations_I.Basic;
import org.toryt.annotations_I.Expression;
import org.toryt.annotations_I.MethodContract;

/**
 * <p>JPA implementation of {@link AtomicStatelessCrudDao}. This delegates to an instance of
 *   {@link RequiredTransactionStatelessCrudDao}.</p>
 * <p>This is a stateless session bean, whose first intention is remote use.
 *   Because we advise not to use the interface {@link AtomicStatelessCrudDao} directly in the API of your business
 *   application, you cannot use this class directly either. You should extend this class in your business application as
 *   follows:</p>
 * <pre>
 *   package my.business.application_IV.businesslogic.jpa;
 *
 *   ...
 *
 *   &#64;Stateless
 *   <var>&#64;WebService</var>
 *   &#64;TransactionManagement(TransactionManagementType.BEAN)
 *   public class RemoteStatelessCrudDao extends org.ppwcode.vernacular.persistence_III.dao.ejb3.RemoteStatelessCrudDao {
 *
 *     // NOP
 *
 *   }
 * </pre>
 * <p>Furthermore, you need to inject a {@link #getRequiredTransactionStatelessCrudDao() RequiredTransactionStatelessCrudDao}
 *    and an {@link #getExceptionHandler() ExceptionHandler}. A {@link #getUserTransaction() UserTransaction} is injected
 *    as a &#64;Resource.</p>
 * <p>That is why this class does not have the {@code &#64;Stateless}, {@code &#64;WebService} nor {@code &#64;TransactionManagement}
 *   or {@code &#64;TransactionAttribute} annotation (apart from infecting this library package with a dependency on EJB3 annotations).
 *   In this way you have the possibility to keep backward compatibility when your business application's semantics change, and the class
 *   / object model and data model change. In that case, you develop a new version in package {@code my.business.application_V}, introducing
 *   {@code my.business.application_V.businesslogic.jpa.JpaStatelessTransactionCrudDao} implementing a new remote interface. With that,
 *   your clients can now choose which version they want to use. From the old version, you keep the necessary classes, but since the
 *   database structure probably has changed, retrieving and updating data cannot easily happen the same way. In particular, your
 *   semantics (persistent bean subtypes) will probably no longer map to the database. This means that your original implementation of
 *   {@code my.business.application_IV.businesslogic.jpa.JpaStatelessTransactionCrudDao} with the old semantics (entities) will no
 *   longer work. By changing the implementation of {@code my.business.application_IV.businesslogic.jpa.JpaStatelessTransactionCrudDao}
 *   to map old semantic POJO's (now no longer entities) to new entities (if at all possible), you make the old API forward compatible
 *   with the new semantics. Because this is not always possible with all methods of this interface in all circumstances, all methods
 *   can throw a {@code NoLongerSupportedError}.</p>
 *
 * @mudo unit tests
 */
public abstract class RemoteAtomicStatelessCrudDao implements AtomicStatelessCrudDao {

  private final static Log _LOG = LogFactory.getLog(RemoteAtomicStatelessCrudDao.class);


  /*<property name="statelessCrudJoinTransactionDao">
  -------------------------------------------------------------------------*/

  @Basic
  public final RequiredTransactionStatelessCrudDao getRequiredTransactionStatelessCrudDao() {
    return $requiredTransactionStatelessCrudDao;
  }

  @MethodContract(
    post = @Expression("statelessCrudJoinTransactionDao == _statelessCrudJoinTransactionDao")
  )
  public final void setStatelessCrudJoinTransactionDao(RequiredTransactionStatelessCrudDao requiredTransactionStatelessCrudDao) {
    $requiredTransactionStatelessCrudDao = requiredTransactionStatelessCrudDao;
  }

  private RequiredTransactionStatelessCrudDao $requiredTransactionStatelessCrudDao;

  /*</property>*/



  /*<property name="exception handler">
  -------------------------------------------------------------------------*/

  @Basic
  public final ExceptionHandler getExceptionHandler() {
    return $exceptionHandler;
  }

  @MethodContract(
    post = @Expression("exceptionHandler == _exceptionHandler")
  )
  public final void setExceptionHandler(ExceptionHandler exceptionHandler) {
    $exceptionHandler = exceptionHandler;
  }

  private ExceptionHandler $exceptionHandler;

  /*</property>*/

  @MethodContract(post = @Expression("result ? statelessCrudJoinTransactionDao != null && userTransaction != null && exceptionHandler != null"))
  /**
   * Returns true if this RemoteAtomicStatelessCrudDao is involved in an
   * transaction (beginTransaction was called, but not yet rolled back
   * or committed).
   */
  public abstract boolean isOperational();

  /**
   * Starts a transaction. Must only be called by this class
   * (hence protected)
   */
  protected abstract void beginTransaction();

  /**
   * Commits a transaction. Must only be called by this class
   * (hence protected)
   */
  protected abstract void commitTransaction();
  
  /**
   * Rolls back a transaction. Must only be called by this class
   * (hence protected)
   */
  protected abstract void rollbackTransaction();
  
  public <_PersistentBean_ extends PersistentBean<?>> Set<_PersistentBean_>
  retrieveAllPersistentBeans(Class<_PersistentBean_> persistentBeanType, boolean retrieveSubClasses) {
    assert dependency(getExceptionHandler(), "exceptionHandler");
    try {
      beginTransaction();
      Set<_PersistentBean_> result = getRequiredTransactionStatelessCrudDao().retrieveAllPersistentBeans(persistentBeanType, retrieveSubClasses);
      commitTransaction();
      return result;
    }
    catch (Throwable t) {
      handleForNoException(t);
    }
    return null; // keep compiler happy
  }

  public <_VersionedPersistentBean_ extends VersionedPersistentBean<?, Timestamp>> Set<_VersionedPersistentBean_>
  retrieveAllPersistentBeansChangedSince(Class<_VersionedPersistentBean_> persistentBeanType, boolean retrieveSubClasses, Timestamp since) {
    assert dependency(getExceptionHandler(), "exceptionHandler");
    try {
      beginTransaction();
      Set<_VersionedPersistentBean_> result = getRequiredTransactionStatelessCrudDao().retrieveAllPersistentBeansChangedSince(persistentBeanType, retrieveSubClasses, since);
      commitTransaction();
      return result;
    }
    catch (Throwable t) {
      handleForNoException(t);
    }
    return null; // keep compiler happy
  }

  public <_Id_ extends Serializable, _PersistentBean_ extends PersistentBean<_Id_>>
  _PersistentBean_ retrievePersistentBean(Class<_PersistentBean_> persistentBeanType, _Id_ id) throws IdNotFoundException {
    assert dependency(getExceptionHandler(), "exceptionHandler");
    try {
      beginTransaction();
      _PersistentBean_ result = getRequiredTransactionStatelessCrudDao().retrievePersistentBean(persistentBeanType, id);
      commitTransaction();
      return result;
    }
    catch (Throwable t) {
      handleForIdNotFoudException(t);
    }
    return null; // keep compiler happy
  }

  public <_Id_ extends Serializable, _Version_ extends Serializable, _PB_ extends VersionedPersistentBean<_Id_, _Version_>>
  _PB_ updatePersistentBean(_PB_ pb) throws ApplicationException {
    assert dependency(getExceptionHandler(), "exceptionHandler");
    try {
      beginTransaction();
      _PB_ result = getRequiredTransactionStatelessCrudDao().updatePersistentBean(pb);
      commitTransaction();
      return result;
    }
    catch (Throwable t) {
      handleForApplicationException(t);
    }
    return null; // keep compiler happy
  }

  public <_Id_ extends Serializable, _Version_ extends Serializable, _PB_ extends VersionedPersistentBean<_Id_, _Version_>>
  _PB_ createPersistentBean(_PB_ pb) throws ApplicationException {
    assert dependency(getExceptionHandler(), "exceptionHandler");
    try {
      beginTransaction();
      _PB_ result = getRequiredTransactionStatelessCrudDao().createPersistentBean(pb);
      commitTransaction();
      return result;
    }
    catch (Throwable t) {
      handleForApplicationException(t);
    }
    return null; // keep compiler happy
  }

  public <_Id_ extends Serializable, _Version_ extends Serializable, _PB_ extends VersionedPersistentBean<_Id_, _Version_>>
  _PB_ deletePersistentBean(_PB_ pb) throws ApplicationException {
    try {
      beginTransaction();
      _PB_ result = getRequiredTransactionStatelessCrudDao().deletePersistentBean(pb);
      commitTransaction();
      return result;
    }
    catch (Throwable t) {
      handleForApplicationException(t);
    }
    return null; // keep compiler happy
  }

  private void handleForNoException(Throwable t) throws ExternalError, AssertionError {
    Throwable finalException = robustRollback(t);
    try {
      getExceptionHandler().handleException(finalException, _LOG);
    }
    catch (ApplicationException metaExc) {
      unexpectedException(metaExc, "handleException can throw no ApplicationExceptions");
    }
  }

  @SuppressWarnings("unchecked")
  private void handleForIdNotFoudException(Throwable t) throws ExternalError, AssertionError, IdNotFoundException {
    Throwable finalException = robustRollback(t);
    try {
      getExceptionHandler().handleException(finalException, _LOG, IdNotFoundException.class);
    }
    catch (IdNotFoundException infExc) {
      throw infExc;
    }
    catch (ApplicationException metaExc) {
      unexpectedException(metaExc, "handleException can throw no ApplicationExceptions");
    }
  }

  @SuppressWarnings("unchecked")
  private void handleForApplicationException(Throwable t) throws ExternalError, AssertionError, ApplicationException {
    Throwable finalException = robustRollback(t);
    getExceptionHandler().handleException(finalException, _LOG, ApplicationException.class);
  }

  private Throwable robustRollback(Throwable reasonForRollback) {
    Throwable finalException = reasonForRollback;
    try {
      rollbackTransaction();
    }
    catch (Throwable exc) {
      finalException = exc;
    }
    return finalException;
  }

}
