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

package org.ppwcode.vernacular.persistence_III.dao;

import static org.ppwcode.metainfo_I.License.Type.APACHE_V2;

import java.util.Set;

import org.ppwcode.bean_VI.CompoundPropertyException;
import org.ppwcode.bean_VI.PropertyException;
import org.ppwcode.bean_VI.RousseauBean;
import org.ppwcode.metainfo_I.Copyright;
import org.ppwcode.metainfo_I.License;
import org.ppwcode.metainfo_I.vcs.SvnInfo;
import org.ppwcode.vernacular.exception_N.TechnicalException;
import org.ppwcode.vernacular.persistence_III.IllegalPersistenceStateException;
import org.ppwcode.vernacular.persistence_III.PersistenceConfigurationError;
import org.ppwcode.vernacular.persistence_III.PersistenceExternalError;
import org.ppwcode.vernacular.persistence_III.PersistentBean;
import org.toryt.annotations_I.Basic;
import org.toryt.annotations_I.Expression;
import org.toryt.annotations_I.MethodContract;
import org.toryt.annotations_I.Throw;

import be.peopleware.persistence_II.IdNotFoundException;


/**
 * <p>This interface gathers the methods needed for CRUD functionality in an asynchronous context, e.g.,
 *   a web application. Clients will call {@link #createPersistentBean(PersistentBean)},
 *   {@link #updatePersistentBean(PersistentBean)} and
 *   {@link #deletePersistentBean(PersistentBean)} always in the context of a transaction. A transaction
 *   is started by {@link #startTransaction()}, and completes with, either a call to
 *   {@link #commitTransaction(PersistentBean)} or {@link #cancelTransaction()}. With this, we
 *   completely dismiss the notion of <dfn>auto-commit</dfn>. In practice, we experience that
 *   an auto-commit feature often leads to confusion for developers, as it is unclear whether
 *   some code is executing in auto-commit mode or not.</p>
 * <p>{@link #retrievePersistentBean(Class, Object)} can be called outside a transaction. Objects that
 *   are deleted have their {@link PersistentBean#getId()} set to null on
 *   {@link #commitTransaction(PersistentBean)}.</p>
 * <p>Before a {@link PersistentBean} is written to the persistent storage (see
 *   {@link #createPersistentBean(PersistentBean)} and {@link #updatePersistentBean(PersistentBean)}, it is
 *   {@link RousseauBean#normalize() normalized} and checked for {@link RousseauBean#isCivilized() civility}.</p>
 * <p>We understand that the limited functionality of this DAO cannot cope with the complete needs of
 *   persistence access. There is e.g., no notion of locking, versioning, etcetera. However, we do know
 *   from experience that this functionality covers a very large part of the needs, and that there are
 *   many applications that need no other functionality than this.</p>
 * <p>In throwing exceptions, we try to make a difference between programming errors, external exceptional
 *   conditions, and internal exceptional conditions. Semantic problems are certainly of the latter nature.
 *   But also the database can signal semantic problems (constraint violations mainly).</p>
 *
 * @author    Jan Dockx
 * @author    PeopleWare n.v.
 */
@Copyright("2004 - $Date$, PeopleWare n.v.")
@License(APACHE_V2)
@SvnInfo(revision = "$Revision$",
         date     = "$Date$")
public interface AsyncCrudDao extends Dao {

  /**
   * <p>Start a transaction. The transaction will be closed in
   *   {@link #commitTransaction(PersistentBean)}
   *   or {@link #cancelTransaction()}.</p>
   * <p>This instance should keep track of the transaction state
   *   until it is requested to close the transaction.</p>
   */
  @MethodContract(
    post = {
      @Expression("inTransaction"),
      @Expression(value = "! 'inTransaction",
                  description = "Cannot be made true by this method when it is false in the old state. " +
                              "So the only option for the implementer is to throw an exception when this occurs.")
    },
    exc = {
      @Throw(type = IllegalPersistenceStateException.class,
             cond = @Expression(value = "inTransaction")),
      @Throw(type = PersistenceConfigurationError.class,
             cond = @Expression(value = "true",
                                description = "could perform the operation because of a bad configuration of " +
                                              "this object, which is considered a programming error or external condition")),
      @Throw(type = PersistenceExternalError.class,
             cond = @Expression(value = "true",
                                description = "could perform the operation because of some problem with persistency " +
                                              "which we consider external"))
    }
  )
  void startTransaction() throws IllegalPersistenceStateException, PersistenceExternalError, PersistenceConfigurationError;

  /**
   * <p>Commit a transaction. The transaction was started by
   *  {@link #startTransaction()}.</p>
   * <p>This instance should keep track of the transaction state
   *   until it is requested to close the transaction.</p>
   *
   * @param  pb
   *         The {@link PersistentBean} instance this transaction was mainly
   *         concerned with.
   *         This is used as {@link CompoundPropertyException#getOrigin()}
   *         in potential {@link CompoundPropertyException}s that are only
   *         discovered on commit.
   * @mudo   The above posed problems in the past. Can we solve this now?
   */
  @MethodContract(
    post = {
      @Expression("! inTransaction"),
      @Expression("for (PersistentBean pb2) {'isDeleted(pb2) ? pb2.id == null}"),
      @Expression("for (PersistentBean pb2) {! isDeleted(pb2)}"),
      @Expression(value = "'inTransaction",
                  description = "Cannot be made true by this method when it is false in the old state. " +
                              "So the only option for the implementer is to throw an exception when this occurs."),
      @Expression(value = "_pb != null",
                  description = "Cannot be made true it is false in the old state. So the only option for the " +
                                "implementer is to throw an exception when this occurs.")
    },
    exc = {
      @Throw(type = PropertyException.class,
             cond = @Expression(value = "true",
                                description = "the commit was stopped for semantic reasons")), // is this only pb.getWildExceptions()?
//      @Throw(type = IllegalPersistenceArgumentException.class,
//             cond = @Expression(value = "_pb == null")),
      @Throw(type = IllegalPersistenceStateException.class,
             cond = @Expression(value = "! inTransaction")),
      @Throw(type = PersistenceConfigurationError.class,
             cond = @Expression(value = "true",
                                description = "could perform the operation because of a bad configuration of " +
                                              "this object, which is considered a programming error or external condition")),
      @Throw(type = PersistenceExternalError.class,
             cond = @Expression(value = "true",
                                description = "could perform the operation because of some problem with persistency " +
                                              "which we consider external"))
    }
  )
  void commitTransaction(PersistentBean<?> pb) throws PropertyException, // IllegalPersistenceArgumentException,
      IllegalPersistenceStateException, PersistenceExternalError, PersistenceConfigurationError;

  /**
   * <p>Cancel a transaction. The transaction was started by
   *   {@link #startTransaction()}.</p>
   * <p>This instance should keep track of the transaction state
   *   until it is requested to close the transaction.</p>
   */
  @MethodContract(
    post = {
      @Expression("! inTransaction"),
      @Expression("for (PersistentBean pb) {! isDeleted(pb)}"),
      @Expression(value = "'inTransaction",
                  description = "Cannot be made true by this method when it is false in the old state. " +
                              "So the only option for the implementer is to throw an exception when this occurs."),
    },
    exc = {
      @Throw(type = IllegalPersistenceStateException.class,
             cond = @Expression("! inTransaction")),
      @Throw(type = PersistenceConfigurationError.class,
             cond = @Expression(value = "true",
                                description = "could perform the operation because of a bad configuration of " +
                                              "this object, which is considered a programming error or external condition")),
      @Throw(type = PersistenceExternalError.class,
             cond = @Expression(value = "true",
                                description = "could perform the operation because of some problem with persistency " +
                                              "which we consider external"))
    }
  )
  void cancelTransaction() throws  PropertyException, IllegalPersistenceStateException, PersistenceExternalError,
      PersistenceConfigurationError;

  @Basic(init = @Expression("false"))
  boolean isInTransaction();

  /**
   * <p>Take a persistent bean instance <code>pb</code> that exists in memory, but not yet in the persistent
   *   storage, and create it in the persistent storage.</p>
   * <p>Before a record for <code>pb</code> is created in the persistent storage, first <code>pb.normalize()</code>
   *   is called, and we check whether <code>pb.isCivilized()</code>.
   * <p>This method cascades creation of necessary related objects: all {@link PersistentBean PersistentBeans}
   *   that are reachable via public properties from <code>pb</code>, whose {@link PersistentBean#getId() id}
   *   is <code>null</code>, recursively, are also created.</p>
   *
   * @idea (jand) security exceptions
   */
  @MethodContract(
    post = {
      @Expression("isCreated(_pb)"),
      @Expression(value = "'inTransaction",
                  description = "Cannot be made true by this method when it is false in the old state. " +
                                "So the only option for the implementer is to throw an exception when this occurs."),
      @Expression(value = "_pb != null",
                  description = "Cannot be made true it is false in the old state. So the only option for the " +
                                "implementer is to throw an exception when this occurs."),
      @Expression(value = "_pb'id != null",
                  description = "Cannot be made true it is false in the old state. So the only option for the " +
                                "implementer is to throw an exception when this occurs."),
      @Expression(value = "_pb'civilized",
                  description = "Cannot be made true it is false in the old state. So the only option for the " +
                                "implementer is to throw an exception when this occurs.")
  },
    exc = {
      @Throw(type = PropertyException.class,
             cond = @Expression("_pb'civilized && " +
                                "(_pb.wildExceptions.size > 1 ? " +
                                   "(thrown.like(_pb.wildExceptions) && thrown.closed) : " +
                                   "thrown.like(_pb.wildExceptions.anElement)")),
//      @Throw(type = IllegalPersistenceArgumentException.class,
//             cond = @Expression(value = "_pb == null")),
//      @Throw(type = IllegalPersistenceArgumentException.class,
//             cond = @Expression(value = "_pb'id == null")),
      @Throw(type = IllegalPersistenceStateException.class,
             cond = @Expression("! inTransaction")),
      @Throw(type = PersistenceConfigurationError.class,
             cond = @Expression(value = "true",
                                description = "could perform the operation because of a bad configuration of " +
                                              "this object, which is considered a programming error or external condition")),
      @Throw(type = PersistenceExternalError.class,
             cond = @Expression(value = "true",
                                description = "could perform the operation because of some problem with persistency " +
                                              "which we consider external"))
    }
  )
  void createPersistentBean(final PersistentBean<?> pb) throws PropertyException, // IllegalPersistenceArgumentException,
      IllegalPersistenceStateException, PersistenceExternalError, PersistenceConfigurationError;

  /**
   * <p>Return a persistent bean instance that represents the data of the record with key <code>id</code> of type
   *   <code>persistentBeanType</code> in the persistent storage.</p>
   *
   * @idea (jand) security exceptions
   */
  @MethodContract(
    post = {
      @Expression("result != null"),
      @Expression("result.id == _id"),
      @Expression("result.civilized"),
      @Expression(value = "_id != null",
                  description = "Cannot be made true it is false in the old state. So the only option for the " +
                                "implementer is to throw an exception when this occurs."),
      @Expression(value = "_persistentBeanType != null",
                  description = "Cannot be made true it is false in the old state. So the only option for the " +
                                "implementer is to throw an exception when this occurs."),
  },
    exc = {
       @Throw(type = IdNotFoundException.class,
              cond = @Expression(value = "true",
                                 description = "no instance found in persistent storage with primary key 'id' " +
                                                "of type persistentBeanType")),
//      @Throw(type = IllegalPersistenceArgumentException.class, // or IdNotFoundException?
//             cond = @Expression(value = "_id == null")),
//      @Throw(type = IllegalPersistenceArgumentException.class,
//             cond = @Expression("_persistentBeanType == null")),
      @Throw(type = PersistenceConfigurationError.class,
             cond = @Expression(value = "true",
                                description = "could perform the operation because of a bad configuration of " +
                                              "this object, which is considered a programming error or external condition")),
      @Throw(type = PersistenceExternalError.class,
             cond = @Expression(value = "true",
                                description = "could perform the operation because of some problem with persistency " +
                                              "which we consider external"))
    }
  )
  <_IdType_, _PersistentBean_ extends PersistentBean<_IdType_>>
  _PersistentBean_ retrievePersistentBean(final Class<_PersistentBean_> persistentBeanType, final _IdType_ id)
      throws IdNotFoundException, //IllegalPersistenceArgumentException,
             IllegalPersistenceStateException, PersistenceExternalError, PersistenceConfigurationError;

  /**
   * <p>Return the set of all persistent bean instances that represent the
   *   data of the records of type
   *   <code>persistentBeanType</code> in the
   *   persistent storage.</p>
   *
   * @param  retrieveSubClasses
   *         whether or not to also retrieve subclasses of persistentBeanType
   * @post   persistentBeanType != null;
   *         This cannot be made true by this method when it is
   *         false in the old state. So the only option for the
   *         implementer is to throw an exception when this occurs.
   * @post   PersistentBean.class.isAssignableFrom(persistentBeanType);
   *         This cannot be made true by this method when it is
   *         false in the old state. So the only option for the
   *         implementer is to throw an exception when this occurs.
   * @result result != null;
   * @result ! result.contains(null);
   * @result (forall Object o; result.contains(o);
   *            persistentBeanType.isInstance(o));
   * @result (forall PersistentBean pb; result.contains(pb);
   *            pb.getId() != null);
   * @result (forall PersistentBean pb; result.contains(pb); pb.isCivilized());
   * @throws TechnicalException
   *         ; could not perform the operation
   * @throws TechnicalException
   *         persistentBeanType == null
   *            || PersistentBean.class.isAssignableFrom(persistentBeanType);
   *
   * @idea (jand) security exceptions
   */
  <_PersistentBean_ extends PersistentBean<?>>
  Set<_PersistentBean_> retrieveAllPersistentBeans(final Class<_PersistentBean_> persistentBeanType, final boolean retrieveSubClasses)
      throws TechnicalException;

  /**
   * <p>Take a persistent bean instance <code>pb</code> that exists
   *   in memory and represents an existing record in the persistent
   *   storage, and change the data in the persistent storage to
   *   reflect the current state of <code>pb</code>.</p>
   * <p>Before the state of <code>pb</code> is written to the persistent
   *   storage, first <code>pb.normalize()</code> is called,
   *   and we check whether <code>pb.isCivilized()</code>.
   * <p>The state of <code>pb</code> remains completely unchanged,
   *   apart from normalization
   *   (<code>pb.hasSameValues(new pb)</code>).</p>
   *
   * @post   isInTransaction();
   *         This cannot be made true by this method when it is
   *         false in the old state. So the only option for the
   *         implementer is to throw an exception when this occurs.
   * @post   pb != null;
   *         This cannot be made true by this method when it is
   *         false in the old state. So the only option for the
   *         implementer is to throw an exception when this occurs.
   * @post   pb.getId() != null;
   *         This cannot be made true by this method when it is
   *         false in the old state. So the only option for the
   *         implementer is to throw an exception when this occurs.
   * @post   pb.isCivilized();
   *         This cannot be made true by this method when it is
   *         false in the old state. So the only option for the
   *         implementer is to throw an exception when this occurs.
   * @post   new pb.hasSameValues(pb);
   * @post   new pb.hasSameId(pb);
   * @throws CompoundPropertyException
   *         pb.getWildExceptions();
   *         The operation was stopped for a semantic reason (! pb.isCivilized()).
   *         The CompoundPropertyException thrown will be closed.
   * @throws TechnicalException
   *         ; could not perform the operation
   * @throws TechnicalException
   *         ! isInTransaction()
   *            || pb == null
   *            || pb.getId() == null;
   *
   * @idea (jand) security, unmodifiable exceptions
   */
  void updatePersistentBean(final PersistentBean<?> pb)
      throws PropertyException, IdNotFoundException, //IllegalPersistenceArgumentException,
      IllegalPersistenceStateException, PersistenceExternalError, PersistenceConfigurationError;

  /**
   * <p>Take a persistent bean instance <code>pb</code> that exists
   *   in memory and represents an existing record in the persistent
   *   storage, and remove that record from persistent storage.</p>
   * <p>The state of <code>pb</code> remains completely unchanged,
   *   apart from normalization
   *   (<code>pb.hasSameValues(new pb)</code>).</p>
   * <p>The state of <code>pb</code> remains
   *   unchanged, including the <code>id</code> (the
   *   <code>id</code> cannot change during a transaction).
   *   The <code>id</code> will be set to <code>null</code>
   *   on {@link #commitTransaction(PersistentBean)}. Also, there
   *   is no normalization.</p>
   *
   * @post   isInTransaction();
   *         This cannot be made true by this method when it is
   *         false in the old state. So the only option for the
   *         implementer is to throw an exception when this occurs.
   * @post   pb != null;
   *         This cannot be made true by this method when it is
   *         false in the old state. So the only option for the
   *         implementer is to throw an exception when this occurs.
   * @post   pb.getId() != null;
   *         This cannot be made true by this method when it is
   *         false in the old state. So the only option for the
   *         implementer is to throw an exception when this occurs.
   * @post   new.isDeleted(pb);
   * @post   new pb.hasSameValues(pb);
   * @post   new pb.hasSameId(pb);
   * @throws TechnicalException
   *         ; could not perform the operation
   * @throws TechnicalException
   *         ! isInTransaction()
   *            || pb == null
   *            || pb.getId() == null;
   *
   * @idea (jand) security, unmodifiable exceptions
   */
  void deletePersistentBean(final PersistentBean<?> pb) throws //IllegalPersistenceArgumentException,
  IllegalPersistenceStateException, PersistenceExternalError, PersistenceConfigurationError; // error on foreign key? cascade delete?

  @Basic(init = @Expression("false"))
  boolean isCreated(final PersistentBean<?> pb);

  @Basic(init = @Expression("false"))
  boolean isDeleted(final PersistentBean<?> pb);

}

