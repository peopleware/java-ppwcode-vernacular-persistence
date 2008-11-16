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

import java.io.Serializable;
import java.util.Set;

import org.ppwcode.metainfo_I.Copyright;
import org.ppwcode.metainfo_I.License;
import org.ppwcode.metainfo_I.vcs.SvnInfo;
import org.ppwcode.vernacular.exception_III.ApplicationException;
import org.ppwcode.vernacular.exception_III.NoLongerSupportedError;
import org.ppwcode.vernacular.persistence_III.AlreadyChangedException;
import org.ppwcode.vernacular.persistence_III.IdNotFoundException;
import org.ppwcode.vernacular.persistence_III.PersistentBean;
import org.ppwcode.vernacular.persistence_III.VersionedPersistentBean;
import org.ppwcode.vernacular.semantics_VI.bean.RousseauBean;
import org.ppwcode.vernacular.semantics_VI.exception.CompoundPropertyException;
import org.toryt.annotations_I.Expression;
import org.toryt.annotations_I.MethodContract;
import org.toryt.annotations_I.Throw;


/**
 * <p>A stateless {@link Dao DAO} that offers generalized CRUD methods. . This interface expresses commonality
 *   between {@link RequiredTransactionStatelessCrudDao} and {@link AtomicStatelessCrudDao}.</p>
 * <p>{@link #retrievePersistentBean(Class, Serializable)} and {@link #retrieveAllPersistentBeans(Class, boolean)}
 *   can be called outside a transaction. This interface does not define how the other methods interact with
 *   transactions.</p>
 * <p>Before a {@link PersistentBean} is written to the persistent storage (see {@link #createPersistentBean(VersionedPersistentBean)}
 *   and {@link #updatePersistentBean(VersionedPersistentBean)}, it is {@link RousseauBean#normalize() normalized} and checked for
 *   {@link RousseauBean#civilized() civility}. This entails also checking for civility of all upstream beans, either part
 *   of the submitted object graph, or already in the database. That way, wild conditions concerning collections of children
 *   (e.g., no period overlap for children in the collection) are enforced.</p>
 * <p>In throwing exceptions, we try to make a difference between programming errors, external exceptional conditions, and
 *   internal exceptional conditions (ppwcode exception vernacular). How we handle anything that happens at commit or
 *   roll-back time, is not expressed in this interface, but differentiated in subtypes.</p>
 * <p>We understand that the limited functionality of this DAO cannot cope with the complete needs of persistence access.
 *   There is e.g., no notion of locking, e.g.. However, we do know from experience that this functionality covers a very
 *   large part of the needs, and that there are many applications that need no other functionality than this.</p>
 * <p>Do not use this interface directly, since the transaction attributes of the methods are undefined. Use
 *   one of the subtypes {@link RequiredTransactionStatelessCrudDao} or {@link AtomicStatelessCrudDao} instead.</p>
 * <p>Do not expose this interface or its subtypes as part of the API in your business application directly. A better approach
 *   is to extend the interface in your version of the business logic:</p>
 * <pre>
 *   package my.business.application_IV.businesslogic;
 *
 *   ...
 *
 *   &#64;<var>(Remote|Local)</var>
 *   public interface <var>Something</var>StatelessCrudDao extends org.ppwcode.vernacular.persistence_III.dao.<var>Something</var>StatelessCrudDao {
 *
 *     // NOP
 *
 *   }
 * </pre>
 * <p>That is why this interface or its subtypes do not have the {@code &#64;Remote} or {@code &#64;Local} annotation (apart
 *   from infecting this library package with a dependency on EJB3 annotations). In this way you have the possibility to keep
 *   backward compatibility when your business application's semantics change, and the class / object model and data model change.
 *   In that case, you develop a new version in package {@code my.business.application_V}, introducing
 *   {@code my.business.application_V.businesslogic.<var>Something</var>StatelessCrudDao}. With that, your clients can now choose which
 *   version they want to use. From the old version, you keep the necessary classes, but since the database structure probably has
 *   changed, retrieving and updating data cannot easily happen the same way. In particular, your semantics (persistent bean
 *   subtypes) will probably no longer map to the database. This means that your original implementation of
 *   {@code my.business.application_IV.businesslogic.<var>Something</var>StatelessCrudDao} with the old semantics (entities) will no
 *   longer work. By changing the implementation of {@code my.business.application_IV.businesslogic.<var>Something</var>StatelessCrudDao}
 *   to map old semantic POJO's (now no longer entities) to new entities (if at all possible), you make the old API forward compatible
 *   with the new semantics. Because this is not always possible with all methods of this interface in all circumstances, all methods can
 *   throw a {@link NoLongerSupportedError}.</p>
 */
@Copyright("2004 - $Date$, PeopleWare n.v.")
@License(APACHE_V2)
@SvnInfo(revision = "$Revision$",
         date     = "$Date$")
public interface StatelessCrudDao extends Dao {

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
    pre  = {
      @Expression("_persistentBeanType != null"),
      @Expression("_id != null")
    },
    post = {
      @Expression("result != null"),
      @Expression("result.id == _id")
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
       @Throw(type = NoLongerSupportedError.class,
              cond = {@Expression("true")})
    }
  )
  <_Id_ extends Serializable, _PersistentBean_ extends PersistentBean<_Id_>>
  _PersistentBean_ retrievePersistentBean(final Class<_PersistentBean_> persistentBeanType, final _Id_ id) throws IdNotFoundException, NoLongerSupportedError;

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
    pre  = @Expression("_persistentBeanType != null"),
    post = {
      @Expression("result != null"),
      @Expression("! result.contains(null)"),
      @Expression("for (PersistentBean pb : result) {pb.id != null}")
    },
    exc  =  @Throw(type = NoLongerSupportedError.class,
                   cond = {@Expression("true")})
  )
  <_PersistentBean_ extends PersistentBean<?>>
  Set<_PersistentBean_> retrieveAllPersistentBeans(final Class<_PersistentBean_> persistentBeanType, final boolean retrieveSubClasses) throws NoLongerSupportedError;

  /**
   * Create the object {@code pb} in persistent storage. Return that object with filled-out {@link PersistentBean#getPersistenceId()}.
   * Before commit, the {@link RousseauBean#civilized() civility} is verified on {@code pb} and all of its upstream beans
   * (to-one relationships), in their state such as they exist in the database. All upstream beans should exist in the database, and
   * be unchanged. Otherwise, an {@link AlreadyChangedException} is thrown. No validation is done on downstream beans: there should
   * be no downstream beans in {@code pb}. It is a programming error to submit a bean with downstream associated beans.
   *
   * @mudo contract
   * @idea (jand) security exceptions
   * @mudo describe effect of cascade settings
   */
  @MethodContract(
    pre  = {
      @Expression("pb != null"),
      @Expression("pb.persistenceId == null"),
      @Expression("pb.persistenceVersion == null")
    },
    post = {
      @Expression("pb'normalize()"),
      @Expression("pb'civilized()"),
      @Expression("hasSameValues(pb, result)"),
      @Expression("result.persistenceId != null"),
      @Expression("result.persistenceVersion == 1")
    },
    exc  =  {
      @Throw(type = NoLongerSupportedError.class, cond = {@Expression("true")}),
      @Throw(type = CompoundPropertyException.class, cond = @Expression("! 'pb.civilized()")),
      @Throw(type = ApplicationException.class, cond = {@Expression("true")})
    }
  )
  public <_Id_ extends Serializable, _Version_ extends Serializable, _PB_ extends VersionedPersistentBean<_Id_, _Version_>>
  _PB_ createPersistentBean(_PB_ pb) throws ApplicationException, NoLongerSupportedError;

  /**
   * Update the object {@code pb} in persistent storage. Return that object. Before commit, the
   * {@link RousseauBean#civilized() civility} is verified on {@code pb} and all of its upstream beans
   * (to-one relationships), in their state such as they exist in the database. All upstream beans
   * should exist in the database, and be unchanged. Otherwise, an {@link AlreadyChangedException}
   * is thrown. No validation is done on downstream beans: there should be no downstream beans in
   * {@code pb}.
   *
   * @mudo contract
   * @idea (jand) security exceptions
   * @mudo describe effect of cascade settings
   */
  @MethodContract(
    pre  = {
      @Expression("pb != null"),
      @Expression("pb.persistenceId != null"),
      @Expression("pb.persistenceVersion != null")
    },
    post = {
      @Expression("pb'normalize()"),
      @Expression("pb'civilized()"),
      @Expression("hasSameValues(pb, result)"),
      @Expression("result.persistenceId == pb'persistenceId"),
      @Expression("result.persistenceVersion == pb'persistenceVersion + 1")
    },
    exc  =  {
      @Throw(type = NoLongerSupportedError.class, cond = {@Expression("true")}),
      @Throw(type = CompoundPropertyException.class, cond = @Expression("! 'pb.civilized()")),
      @Throw(type = ApplicationException.class, cond = {@Expression("true")})
    }
  )
  public <_Id_ extends Serializable, _Version_ extends Serializable, _PB_ extends VersionedPersistentBean<_Id_, _Version_>>
  _PB_ updatePersistentBean(_PB_ pb) throws ApplicationException, NoLongerSupportedError;

  /**
   * Delete the bean {@code pb}, and associated beans, depending on cascade DELETE settings, from persistent storage.
   * The entire bean is returned, for reasons of consistency with the other methods.
   *
   * @mudo contract
   * @idea (jand) security exceptions
   */
  @MethodContract(
    pre  = {
      @Expression("pb != null"),
      @Expression("pb.persistenceId != null"),
      @Expression("pb.persistenceVersion != null")
    },
    post = {
      @Expression("pb'normalize()"),
      @Expression("pb'civilized()"),
      @Expression("hasSameValues(pb, result)"),
      @Expression("result.persistenceId == null"),
      @Expression("result.persistenceVersion == null")
    },
    exc  =  {
      @Throw(type = NoLongerSupportedError.class, cond = {@Expression("true")}),
      @Throw(type = ApplicationException.class, cond = {@Expression("true")})
    }
  )
  public <_Id_ extends Serializable, _Version_ extends Serializable, _PB_ extends VersionedPersistentBean<_Id_, _Version_>>
  _PB_ deletePersistentBean(_PB_ pb) throws ApplicationException, NoLongerSupportedError;

}
