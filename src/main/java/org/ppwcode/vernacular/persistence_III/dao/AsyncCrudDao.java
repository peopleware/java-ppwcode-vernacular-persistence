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
import org.ppwcode.exception_N.SemanticException;
import org.ppwcode.metainfo_I.Copyright;
import org.ppwcode.metainfo_I.License;
import org.ppwcode.metainfo_I.vcs.SvnInfo;
import org.ppwcode.vernacular.persistence_III.AlreadyChangedException;
import org.ppwcode.vernacular.persistence_III.IdNotFoundException;
import org.ppwcode.vernacular.persistence_III.PeristenceIllegalArgumentError;
import org.ppwcode.vernacular.persistence_III.PeristenceIllegalStateError;
import org.ppwcode.vernacular.persistence_III.PersistenceConfigurationError;
import org.ppwcode.vernacular.persistence_III.PersistenceExternalError;
import org.ppwcode.vernacular.persistence_III.PersistentBean;
import org.toryt.annotations_I.Basic;
import org.toryt.annotations_I.Expression;
import org.toryt.annotations_I.MethodContract;
import org.toryt.annotations_I.Throw;

import be.peopleware.persistence_II.dao.PagingList;



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
      @Throw(type = PeristenceIllegalStateError.class,
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
  void startTransaction() throws PeristenceIllegalStateError, PersistenceExternalError, PersistenceConfigurationError;

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
      @Expression("for (PersistentBean pbDel) {'isDeleted(pbDel) ? pbDel.id == null}"),
      @Expression("for (PersistentBean pbDel) {! isDeleted(pbDel)}"),
      @Expression("for (PersistentBean pbCreated) {'isCreated(pbCreated) ? pbCreated.id != null}"),
      @Expression("for (PersistentBean pbCreated) {! isCreated(pbCreated)}"),
      @Expression(value = "'inTransaction",
                  description = "Cannot be made true by this method when it is false in the old state. " +
                              "So the only option for the implementer is to throw an exception when this occurs."),
    },
    exc = {
      @Throw(type = SemanticException.class,
             cond = @Expression(value = "true",
                                description = "the commit was stopped for semantic reasons, either wild exceptions, " +
                                          "or exceptions from the persistent storage (which probably cannot be " +
                                          "translated into property exceptions)")),
      @Throw(type = PeristenceIllegalStateError.class,
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
  void commitTransaction() throws SemanticException, PeristenceIllegalStateError, PersistenceExternalError,
      PersistenceConfigurationError;

  /**
   * <p>Cancel a transaction. The transaction was started by
   *   {@link #startTransaction()}.</p>
   * <p>This instance should keep track of the transaction state
   *   until it is requested to close the transaction.</p>
   */
  @MethodContract(
    post = {
      @Expression("! inTransaction"),
      @Expression("for (PersistentBean pbDel) {! isDeleted(pbDel)}"),
      @Expression("for (PersistentBean pbCreated) {! isCreated(pbCreated)}"),
      @Expression(value = "'inTransaction",
                  description = "Cannot be made true by this method when it is false in the old state. " +
                              "So the only option for the implementer is to throw an exception when this occurs."),
    },
    exc = {
      @Throw(type = PeristenceIllegalStateError.class,
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
  void cancelTransaction() throws PeristenceIllegalStateError, PersistenceExternalError, PersistenceConfigurationError;

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
             cond = @Expression("! _pb'civilized && " +
                                "(_pb.wildExceptions.size > 1 ? " +
                                   "(thrown.like(_pb.wildExceptions) && thrown.closed) : " +
                                   "thrown.like(_pb.wildExceptions.anElement)")),
      @Throw(type = SemanticException.class,
             cond = @Expression(value = "true", description = "another mechanism then our RousseauBean mechanism " +
                                                              "signals a semantic problem")),
      @Throw(type = PeristenceIllegalArgumentError.class,
             cond = @Expression(value = "_pb == null")),
      @Throw(type = PeristenceIllegalArgumentError.class,
             cond = @Expression(value = "_pb'id != null")),
      @Throw(type = PeristenceIllegalStateError.class,
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
  void createPersistentBean(final PersistentBean<?> pb) throws PropertyException, SemanticException,
      PeristenceIllegalArgumentError, PeristenceIllegalStateError, PersistenceExternalError, PersistenceConfigurationError;

  /**
   * <p>Return a persistent bean instance that represents the data of the record with key <code>id</code> of type
   *   <code>persistentBeanType</code> in the persistent storage.</p>
   * <p>Of particular note is the fact that returned beans <em>need not necessarily need to be civilized</em>.
   *   This is strange, and probably a bad practice, but we have encountered situations where our code
   *   needs to be more stringent (in creates and updates) than legacy data existing already in the database.</p>
   *
   * @idea (jand) security exceptions
   */
  @MethodContract(
    post = {
      @Expression("result != null"),
      @Expression("result.id == _id"),
      @Expression(value = "_id != null",
                  description = "Cannot be made true it is false in the old state. So the only option for the " +
                                "implementer is to throw an exception when this occurs."),
      @Expression(value = "_persistentBeanType != null",
                  description = "Cannot be made true it is false in the old state. So the only option for the " +
                                "implementer is to throw an exception when this occurs."),
    },
    exc = {
       @Throw(type = IdNotFoundException.class,
              cond = {
                @Expression(value = "true",
                            description = "no instance found in persistent storage with primary key 'id' " +
                                          "of type persistentBeanType"),
                @Expression("thrown.persistentBeanType == _persistentBeanType"),
                @Expression("thrown.id == _id")
              }),
        @Throw(type = PeristenceIllegalArgumentError.class,
               cond = @Expression(value = "_persistentBeanType == null")),
        @Throw(type = PeristenceIllegalArgumentError.class,
               cond = @Expression(value = "_id == null")),
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
      throws IdNotFoundException, PeristenceIllegalArgumentError, PersistenceExternalError, PersistenceConfigurationError;

  /**
   * <p>Return the set of all persistent bean instances that represent the data of the records of type
   *   <code>persistentBeanType</code> in the persistent storage.</p>
   * <p>If this is too much data, consider using the {@link PagingList}.</p>
   * <p>Of particular note is the fact that returned beans <em>need not necessarily need to be civilized</em>.
   *   This is strange, and probably a bad practice, but we have encountered situations where our code
   *   needs to be more stringent (in creates and updates) than legacy data existing already in the database.</p>
   *
   * @param  retrieveSubClasses
   *         whether or not to also retrieve instances of subtypes of {@code persistentBeanType}; if
   *         {@code persistentBeanType} is  abstract, an empty set will be returned if {@code retrieveSubclasses}
   *         is false
   *
   * @idea (jand) security exceptions
   */
  @MethodContract(
    post = {
      @Expression("result != null"),
      @Expression("! result.contains(null)"),
      @Expression("for (PersistentBean pb : result) {pb.id != null}"),
      @Expression(value = "_persistentBeanType != null",
                  description = "Cannot be made true it is false in the old state. So the only option for the " +
                                "implementer is to throw an exception when this occurs."),
    },
    exc = {
        @Throw(type = PeristenceIllegalArgumentError.class,
               cond = @Expression(value = "_persistentBeanType == null")),
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
  <_PersistentBean_ extends PersistentBean<?>>
  Set<_PersistentBean_> retrieveAllPersistentBeans(final Class<_PersistentBean_> persistentBeanType, final boolean retrieveSubClasses)
      throws PeristenceIllegalArgumentError, PersistenceExternalError, PersistenceConfigurationError;

  /**
   * <p>Take a persistent bean instance <code>pb</code> that exists in memory and represents an existing record in the persistent
   *   storage, and change the data in the persistent storage to reflect the current state of <code>pb</code>.</p>
   * <p>Before the state of <code>pb</code> is written to the persistent storage, first <code>pb.normalize()</code> is called,
   *   and we check whether <code>pb.isCivilized()</code>.</p>
   * <p>The state of <code>pb</code> remains completely unchanged, apart from normalization.</p>
   *
   * @idea (jand) security exceptions, unmodifiable error
   */
  @MethodContract(
    post = {
      @Expression(value = "true",
                  description = "The object is updated in persistence storage"),
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
             cond = @Expression("! _pb'civilized && " +
                                "(_pb.wildExceptions.size > 1 ? " +
                                   "(thrown.like(_pb.wildExceptions) && thrown.closed) : " +
                                   "thrown.like(_pb.wildExceptions.anElement)")),
      @Throw(type = SemanticException.class,
             cond = @Expression(value = "true", description = "another mechanism then our RousseauBean mechanism " +
                                                              "signals a semantic problem")),
      @Throw(type = IdNotFoundException.class,
             cond = {
               @Expression(value = "true",
                           description = "no instance found in persistent storage with primary key 'id' " +
                                         "of type persistentBeanType"),
               @Expression("thrown.persistentBeanType == _persistentBeanType"),
               @Expression("thrown.id == _id")
             }),
      @Throw(type = AlreadyChangedException.class,
             cond = {
               @Expression(value = "true",
                           description = "the instance in persistent storage of which pb is a representation " +
                                         "has changed in persistent storage since the last time we looked; " +
                                         "we will not override the latest data (optimistic locking, versioning)"),
               @Expression("thrown.persistentBean == _persistentBean")
             }),
      @Throw(type = PeristenceIllegalArgumentError.class,
             cond = @Expression(value = "_pb == null")),
      @Throw(type = PeristenceIllegalArgumentError.class,
             cond = @Expression(value = "_pb'id == null")),
      @Throw(type = PeristenceIllegalStateError.class,
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
  void updatePersistentBean(final PersistentBean<?> pb) throws PropertyException, SemanticException, IdNotFoundException, AlreadyChangedException,
      PeristenceIllegalArgumentError, PeristenceIllegalStateError, PersistenceConfigurationError, PersistenceExternalError;

  /**
   * <p>Take a persistent bean instance <code>pb</code> that exists in memory and represents an existing record in the persistent
   *   storage, and remove that record from persistent storage.</p>
   * <p>The state of <code>pb</code> remains unchanged, including the <code>id</code> (the <code>id</code> cannot change during
   *   a transaction). The <code>id</code> will be set to <code>null</code> on {@link #commitTransaction(PersistentBean)}. Also, there
   *   is no normalization.</p>
   *
   * @idea (jand) security exceptions, unmodifiable error
   * @todo error on foreign key? cascade delete?
   */
  @MethodContract(
    post = {
      @Expression(value = "true",
                  description = "The object is deleted from persistence storage"),
      @Expression("isDeleted(_pb)"),
      @Expression(value = "'inTransaction",
                  description = "Cannot be made true by this method when it is false in the old state. " +
                                "So the only option for the implementer is to throw an exception when this occurs."),
      @Expression(value = "_pb != null",
                  description = "Cannot be made true it is false in the old state. So the only option for the " +
                                "implementer is to throw an exception when this occurs."),
      @Expression(value = "_pb'id != null",
                  description = "Cannot be made true it is false in the old state. So the only option for the " +
                                "implementer is to throw an exception when this occurs.")
    },
    exc = {
      @Throw(type = SemanticException.class,
             cond = @Expression(value = "true", description = "another mechanism then our RousseauBean mechanism " +
                                                              "signals a semantic problem")),
      @Throw(type = IdNotFoundException.class,
             cond = {
               @Expression(value = "true",
                           description = "no instance found in persistent storage with primary key 'id' " +
                                         "of type persistentBeanType"),
               @Expression("thrown.persistentBeanType == _persistentBeanType"),
               @Expression("thrown.id == _id")
             }),
      @Throw(type = PeristenceIllegalArgumentError.class,
             cond = @Expression(value = "_pb == null")),
      @Throw(type = PeristenceIllegalArgumentError.class,
             cond = @Expression(value = "_pb'id == null")),
      @Throw(type = PeristenceIllegalStateError.class,
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
  void deletePersistentBean(final PersistentBean<?> pb) throws SemanticException, IdNotFoundException, PeristenceIllegalArgumentError,
      PeristenceIllegalStateError, PersistenceConfigurationError, PersistenceExternalError;

  @Basic(init = @Expression("false"))
  boolean isCreated(final PersistentBean<?> pb);

  @Basic(init = @Expression("false"))
  boolean isDeleted(final PersistentBean<?> pb);

}

