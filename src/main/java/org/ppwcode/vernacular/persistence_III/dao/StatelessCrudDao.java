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
import org.ppwcode.vernacular.exception_II.InternalException;
import org.ppwcode.vernacular.exception_II.NoLongerSupportedError;
import org.ppwcode.vernacular.persistence_III.IdNotFoundException;
import org.ppwcode.vernacular.persistence_III.PersistentBean;
import org.ppwcode.vernacular.semantics_VI.bean.RousseauBean;
import org.toryt.annotations_I.Expression;
import org.toryt.annotations_I.MethodContract;
import org.toryt.annotations_I.Throw;


/**
 * <p>In contrast to the {@link AsyncCrudDao}, this {@link Dao} is stateless. This interface expresses commonality
 *   between {@link StatelessCrudTransactionDao} and {@link StatelessCrudJoinTransactionDao}.</p>
 * <p>{@link #retrievePersistentBean(Class, Serializable)} and {@link #retrieveAllPersistentBeans(Class, boolean)}
 *   can be called outside a transaction. This interface does not define how the other methods interact with
 *   transactions. Objects that are deleted have their {@link PersistentBean#getPersistenceId()}
 *   set to {@code null}.</p>
 * <p>Before a {@link PersistentBean} is written to the persistent storage (see {@link #mergePersistentBean(PersistentBean)},
 *   it is {@link RousseauBean#normalize() normalized} and checked for {@link RousseauBean#civilized() civility}. This entails
 *   also checking for civility of all upstream beans, either part of the submitted object graph, or already in the database.
 *   That way, wild conditions concerning collections of children (e.g., no period overlap for children in the collection)
 *   are enforced.</p>
 * <p>In throwing exceptions, we try to make a difference between programming errors, external exceptional conditions, and
 *   internal exceptional conditions. How we handle anything that happens at commit time, is not expressed in this interface,
 *   but differentiated in subtypes.</p>
 * <p>We understand that the limited functionality of this DAO cannot cope with the complete needs of persistence access.
 *   There is e.g., no notion of locking, e.g.. However, we do know from experience that this functionality covers a very
 *   large part of the needs, and that there are many applications that need no other functionality than this.</p>
 * <p>Do not expose this interface or its subtypes as part of the API in your business application directly. A better approach
 *   is to extend the interface in your version of the business logic:</p>
 * <pre>
 *   package my.business.application_IV.businesslogic;
 *
 *   ...
 *
 *   &#64;<var>(Remote|Local)</var>
 *   public Stateless<var>XXX</var>CrudDao extends org.ppwcode.vernacular.persistence_III.dao.Stateless<var>XXX</var>CrudDao {
 *
 *     // NOP
 *
 *   }
 * </pre>
 * <p>That is why this interface or its subtypes do not have the {@code &#64;Remote} or {@code &#64;Local} annotation (apart
 *   from infecting this library package with a dependency on EJB3 annotations). In this way you have the possibility to keep
 *   backward compatibility when your business application's semantics change, and the class / object model and data model change.
 *   In that case, you develop a new version in package {@code my.business.application_V}, introducing
 *   {@code my.business.application_V.businesslogic.Stateless<var>XXX</var>CrudDao}. With that, your clients can now choose which
 *   version they want to use. From the old version, you keep the necessary classes, but since the database structure probably has
 *   changed, retrieving and updating data cannot easily happen the same way. In particular, your semantics (persistent bean
 *   subtypes) will probably no longer map to the database. This means that your original implementation of
 *   {@code my.business.application_IV.businesslogic.Stateless<var>XXX</var>CrudDao} with the old semantics (entities) will no
 *   longer work. By changing the implementation of {@code my.business.application_IV.businesslogic.Stateless<var>XXX</var>CrudDao}
 *   to map old semantic POJO's (now no longer entities) to new entities (if at all possible), you make the new semantics backward
 *   compatible with the old interface. Because this is not always possible with all methods of this interface in all
 *   circumstances, all methods can throw a {@link NoLongerSupportedError}.</p>
 *
 *
// MUDO
// * <p>Because a transaction that is atomic on the scale of the end user can be quite extensive, and include
// *   updates of existing entities, creation of new entities, and deletion of other entities, the create, update and
// *   delete functionality is also available gathered in 1 method
// *   -@link #writePersistentBeans(Set toBeCreated, Set toBeUpdated, Set toBeDeleted)}.
// *   User code should gather the {@link PersistentBean}s to be created, updated and deleted in a set, and then call
// *   this method to write information to persistent storage (possibly remotely). To support this stateful functionality,
// *   it might be appropriate to wrap an instance of this type in an {@link AsyncCrudDao} in user code.</p>
// END MUDO
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
   * Create or update the object graph reachable from {@code pb}. Object with a {@link PersistentBean#getPersistenceId()} {@code null}
   * will be created, objects with an effective {@link PersistentBean#getPersistenceId()} will be updated. Note that not only {@code pb}
   * is created or updated, but all beans in the object graph that is reachable from {@code pb}, depending on the cascade settings.
   *
   * @mudo specific exception for rollback, or InternalException
   * @mudo contract
   * @idea (jand) security exceptions
   */
  @MethodContract(
    post = {},
    exc  =  @Throw(type = NoLongerSupportedError.class,
                   cond = {@Expression("true")})
  )
  public <_Id_ extends Serializable, _PB_ extends PersistentBean<_Id_>> _PB_ mergePersistentBean(_PB_ pb) throws InternalException, NoLongerSupportedError;

  /**
   * Delete the bean {@code pb}, and associated beans, depending on cascade settings.
   * The entire bean is returned, for reasons of consistency with the other methods.
   *
   * @mudo specific exception for rollback, or InternalException
   * @mudo contract
   * @idea (jand) security exceptions
   */
  @MethodContract(
    post = {},
    exc  =  @Throw(type = NoLongerSupportedError.class,
                   cond = {@Expression("true")})
  )
  public <_Id_ extends Serializable, _PB_ extends PersistentBean<_Id_>> _PB_ deletePersistentBean(_PB_ pb) throws InternalException, NoLongerSupportedError;

}
