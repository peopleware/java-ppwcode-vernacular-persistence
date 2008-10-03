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

import javax.ejb.Remote;

import org.ppwcode.metainfo_I.Copyright;
import org.ppwcode.metainfo_I.License;
import org.ppwcode.metainfo_I.vcs.SvnInfo;
import org.ppwcode.vernacular.exception_II.InternalException;
import org.ppwcode.vernacular.persistence_III.IdNotFoundException;
import org.ppwcode.vernacular.persistence_III.PersistentBean;
import org.ppwcode.vernacular.semantics_VI.bean.RousseauBean;
import org.toryt.annotations_I.Expression;
import org.toryt.annotations_I.MethodContract;
import org.toryt.annotations_I.Throw;


/**
 * <p>In contrast to the {@link AsyncCrudDao}, this {@link Dao} is stateless. This is done to make it possible to
 *   align transaction borders with method calls, which makes working with a remote implementation that much easier.
 *   Thus, for mutating methods (merge, delete), calling the method will result in the start of a transaction,
 *   the execution of the functionality, and a commit or roll-back. Failure is reported with exceptions according to
 *   the code exception vernacular.</p>
// MUDO
// * <p>Because a transaction that is atomic on the scale of the end user can be quite extensive, and include
// *   updates of existing entities, creation of new entities, and deletion of other entities, the create, update and
// *   delete functionality is also available gathered in 1 method
// *   -@link #writePersistentBeans(Set toBeCreated, Set toBeUpdated, Set toBeDeleted)}.
// *   User code should gather the {@link PersistentBean}s to be created, updated and deleted in a set, and then call
// *   this method to write information to persistent storage (possibly remotely). To support this stateful functionality,
// *   it might be appropriate to wrap an instance of this type in an {@link AsyncCrudDao} in user code.</p>
// END MUDO
 * <p>{@link #retrievePersistentBean(Class, Serializable)} and {@link #retrieveAllPersistentBeans(Class, boolean)}
 *   can be called outside a transaction. Objects that are deleted have their {@link PersistentBean#getPersistenceId()}
 *   set to {@code null}.</p>
 * <p>Before a {@link PersistentBean} is written to the persistent storage (see {@link #mergePersistentBean(PersistentBean)},
 *   it is {@link RousseauBean#normalize() normalized} and checked for {@link RousseauBean#civilized() civility}.</p>
 * <p>In throwing exceptions, we try to make a difference between programming errors, external exceptional conditions, and
 *   internal exceptional conditions. Semantic problems are certainly of the latter nature. But also the database can signal
 *   semantic problems (constraint violations mainly).</p>
 * <p>We understand that the limited functionality of this DAO cannot cope with the complete needs of persistence access.
 *   There is e.g., no notion of locking, e.g.. However, we do know from experience that this functionality covers a very
 *   large part of the needs, and that there are many applications that need no other functionality than this.</p>
 *
 * @mudo contracts
 */
@Copyright("2004 - $Date: 2008-08-29 10:41:30 +0200 (Fri, 29 Aug 2008) $, PeopleWare n.v.")
@License(APACHE_V2)
@SvnInfo(revision = "$Revision: 2342 $",
         date     = "$Date: 2008-08-29 10:41:30 +0200 (Fri, 29 Aug 2008) $")
@Remote
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
              })
    }
  )
  <_Id_ extends Serializable, _PersistentBean_ extends PersistentBean<_Id_>>
  _PersistentBean_ retrievePersistentBean(final Class<_PersistentBean_> persistentBeanType, final _Id_ id) throws IdNotFoundException;

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
    }
  )
  <_PersistentBean_ extends PersistentBean<?>>
  Set<_PersistentBean_> retrieveAllPersistentBeans(final Class<_PersistentBean_> persistentBeanType, final boolean retrieveSubClasses);

  /**
   * Create or update. Create id ID is null, update if not.
   */
  public <_Id_ extends Serializable, _PB_ extends PersistentBean<_Id_>> _PB_ mergePersistentBean(_PB_ pb) throws InternalException;

  /**
   * The entire bean is returned, for reasons of consistency with the other methods.
   */
  public <_Id_ extends Serializable, _PB_ extends PersistentBean<_Id_>> _PB_ deletePersistentBean(_PB_ pb) throws InternalException;

}
