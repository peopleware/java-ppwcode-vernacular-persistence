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

package org.ppwcode.vernacular.persistence_III;


import static org.ppwcode.metainfo_I.License.Type.APACHE_V2;

import java.io.Serializable;

import org.ppwcode.metainfo_I.Copyright;
import org.ppwcode.metainfo_I.License;
import org.ppwcode.metainfo_I.vcs.SvnInfo;
import org.ppwcode.vernacular.semantics_VI.bean.RousseauBean;
import org.toryt.annotations_I.Basic;
import org.toryt.annotations_I.Expression;
import org.toryt.annotations_I.MethodContract;



/**
 * <p>Persistent classes need a primary key. Persistent objects always represent real-world objects,
 *   and therefore should be implemented as {@link RousseauBean}s. This interface enforces the correct
 *   behavior. Supporting code is offered by {@link AbstractPersistentBean}.</p>
 * <p>Users should be aware that this means that there can be more than 1 Java object that represents
 *   the same instance in the persistent storage. To check whether 2 persistent objects represent the
 *   same persistent instance, use {@link #hasSamePersistenceId(PersistentBean)}.</p>
 * <p>Persistent beans are not {@link Cloneable} however. Implementing clone for a semantic inheritance
 *   tree is a large investment, and should not be enforced. Furthermore, it still is a bad idea to make
 *   any semantic object {@link Cloneable}. From experience we know that it is very difficult to decide
 *   in general how deep a clone should go. Persistent beans are {@link Serializable} though, because
 *   they are often used also as Data Transfer Objects in multi-tier applications.</p>
 *<p>{@code _Id_} must be {@link Serializable}, because PersistentBeans are {@link Serializable}
 *   and the {@link #getPersistenceId()} is not {@code transient}. (And BTW, id's must be
 *   {@link Serializable} for Hibernate too ... :-) ).</p>
 *
 * @author    Jan Dockx
 * @author    PeopleWare n.v.
 */
@Copyright("2004 - $Date$, PeopleWare n.v.")
@License(APACHE_V2)
@SvnInfo(revision = "$Revision$",
         date     = "$Date$")
public interface PersistentBean<_Id_ extends Serializable> extends RousseauBean, Serializable {

  /*<property name="id">*/
  //------------------------------------------------------------------

  @Basic(init = @Expression("null"))
  _Id_ getPersistenceId();

  /**
   * This instance has the same id as the instance <code>other</code>.
   *
   * @param     other
   *            The persistent object to compare to.
   */
  @MethodContract(
    post = @Expression("_other != null && persistenceId == _other.persistenceId")
  )
  boolean hasSamePersistenceId(final PersistentBean<_Id_> other);

  /**
   * @param     persistenceId
   *            The new value
   *
   * @idea This method should not appear in this interface. Once an id is set,
   *       it should always remain the same (final, immutable property).
   *       Persistence engines need a way to set the property, but that is it.
   *       The question is whether it is possible to do testing than?
   */
  @MethodContract(
    post = @Expression("persistenceId == _persistenceId")
  )
  void setPersistenceId(final _Id_ persistenceId);

  /*</property>*/

}
