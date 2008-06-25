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

import org.ppwcode.bean_VI.RousseauBean;
import org.ppwcode.metainfo_I.Copyright;
import org.ppwcode.metainfo_I.License;
import org.ppwcode.metainfo_I.vcs.SvnInfo;
import static org.ppwcode.metainfo_I.License.Type.APACHE_V2;

import be.peopleware.persistence_II.AbstractPersistentBean;


/**
 * <p>Persistent classes need a primary key. Persistent objects
 *   always represent real-world objects, and therefore should be
 *   implemented as {@link RousseauBean}s. This interface
 *   enforces the correct behavior. Supporting code is offered by
 *   {@link AbstractPersistentBean}.</p>
 * <p>Users should be aware that this means that there can be more
 *   than 1 Javav object that represents the same instance in the persistent storage.
 *   To check whether 2 persistent objects represent the same persistent
 *   instance, use {@link #hasSameId(PersistentBean)}.</p>
 * <p>Persistent beans are not {@link Cloneable} however. Implementing
 *   clone for a semantic inheritance tree is a large investment, and
 *   should not be enforced. Furthermore, it still is a bad idea to make
 *   any semantic object {@link Cloneable}. From experience we know that
 *   it is very difficult to decide in general how deep a clone should go.
 *   Persistent beans are {@link java.io.Serializable} though, because
 *   they are often used also as Data Transfer Objects in multi-tier
 *   applications.</p>
 *
 * @author    Jan Dockx
 * @author    PeopleWare n.v.
 */
@Copyright("2004 - $Date$, PeopleWare n.v.")
@License(APACHE_V2)
@SvnInfo(revision = "$Revision$",
         date     = "$Date$")
public interface PersistentBean extends RousseauBean, Serializable {

  /*<property name="id">*/
  //------------------------------------------------------------------

  /**
   * @param     id
   *            The new value
   * @post      (id == null) ? new.getId() == null : new.getId().equals(id);
   *
   * @idea (jand) This should only be changed by Hibernate. So public is too broad.
   *              Can something be done about this?
   * @note (dvankeer): Make hibernate set this field directly instead of using
   *                   the get- & setter. <id .... access="field" ... />
   * @note (dvankeer): If we remove this method we break a lot in the
   *                   application. Especially tests
   */
  void setId(final Long id);

  /**
   * @basic
   */
  Long getId();

  /*</property>*/


  // IDEA (jand) move the String stuff to a ppw-util, or at least ppw-bean
  // IDEA (jand) automatic implementation of hasSameValues there too

  /**
   * Short representation of the bean.
   *
   * @return getClass().getName() + "@" + hashCode()
   *           + "[id: " + $id + "]";
   */
  String toString();

  /**
   * Long representation of this bean.
   */
  String toStringLong();

  /**
   * Append a long representation of this to <code>acc</code>.
   */
  void appendLongRepresentation(StringBuffer acc);

  /**
   * This instance has the same id as the instance <code>other</code>.
   *
   * @param     other
   *            The persisten object to compare to.
   * @return    (other != null)
   *                && ((getId() == null)
   *                    ? other.getId() == null
   *                    : getId().equals(other.getId())));
   */
  boolean hasSameId(final PersistentBean other);

}
