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

package org.ppwcode.vernacular.persistence_III;


import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Version;

import org.ppwcode.bean_VI.RousseauBean;
import org.ppwcode.metainfo_I.Copyright;
import org.ppwcode.metainfo_I.License;
import org.ppwcode.metainfo_I.vcs.SvnInfo;
import org.toryt.annotations_I.Basic;
import org.toryt.annotations_I.Expression;
import org.toryt.annotations_I.MethodContract;

import static org.ppwcode.metainfo_I.License.Type.APACHE_V2;



/**
 * <p>Persistent classes need a primary key. Persistent objects
 *   always represent real-world objects, and therefore should be
 *   implemented as {@link RousseauBean}s. This interface
 *   enforces the correct behavior. Supporting code is offered by
 *   {@link AbstractPersistentBean}.</p>
 * <p>Users should be aware that this means that there can be more
 *   than 1 Java object that represents the same instance in the persistent storage.
 *   To check whether 2 persistent objects represent the same persistent
 *   instance, use {@link #hasSameId(PersistentBean)}.</p>
 * <p>Persistent beans are not {@link Cloneable} however. Implementing
 *   clone for a semantic inheritance tree is a large investment, and
 *   should not be enforced. Furthermore, it still is a bad idea to make
 *   any semantic object {@link Cloneable}. From experience we know that
 *   it is very difficult to decide in general how deep a clone should go.
 *   Persistent beans are {@link Serializable} though, because
 *   they are often used also as Data Transfer Objects in multi-tier
 *   applications.</p>
 * <p>Persistency should always be implemented with versioning (optimistic
 *   locking). For that, the property {@link #getVersion() version}
 *   is added to this interface. There are several different possible types of versioning,
 *   using an integer, date, or event a GUID. For that reason, the type of the property
 *   is generic.</p>
 *<p>_Id_ and _Version_ must be {@link Serializable}, because PersistentBeans are {@link Serializable}
 *   and the {@link #getId()} is not {@code transient}. (And BTW, id's must be
 *   {@link Serializable} for Hibernate too ... :-) ).</p>
 *
 * @author    Jan Dockx
 * @author    PeopleWare n.v.
 */
@Copyright("2004 - $Date$, PeopleWare n.v.")
@License(APACHE_V2)
@SvnInfo(revision = "$Revision$",
         date     = "$Date$")
public interface PersistentBean<_Id_ extends Serializable, _Version_ extends Serializable> extends RousseauBean, Serializable {

  /*<property name="id">*/
  //------------------------------------------------------------------

  @Id
  @GeneratedValue
  @Column(name="id")
  @Basic(init = @Expression("null"))
  _Id_ getId();

  /**
   * This instance has the same id as the instance <code>other</code>.
   *
   * @param     other
   *            The persistent object to compare to.
   */
  @MethodContract(
                  post = @Expression("other != null && id == other.id")
  )
  boolean hasSameId(final PersistentBean<_Id_, _Version_> other);

  /**
   * @param     id
   *            The new value
   *
   * @idea This method should not appear in this interface. Once an id is set,
   *       it should always remain the same (final, immutable property).
   *       Persistence engines need a way to set the property, but that is it.
   *       The question is whether it is possible to do testing than?
   */
  @MethodContract(
    post = @Expression("id == _id")
  )
  void setId(final _Id_ id);

  /*</property>*/



  /*<property name="persistence version">*/
  //------------------------------------------------------------------

  @Version
  @Basic(init = @Expression("null"))
  _Version_ getVersion();

  /**
   * @param     version
   *            The new value
   *
   * @idea This method should not appear in this interface. Once a version is set,
   *       it should always remain the same (final, immutable property).
   *       Persistence engines need a way to set the property, but that is it.
   *       The question is whether it is possible to do testing than?
   */
  @MethodContract(
    post = @Expression("version == _version")
  )
  void setVersion(final _Version_ version);

  /*</property>*/



}
