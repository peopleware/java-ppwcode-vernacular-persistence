/*<license>
Copyright 2008 - $Date$ by PeopleWare n.v..

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

package org.ppwcode.vernacular.persistence_III.jpa.test.semantics;


import org.apache.openjpa.persistence.jdbc.ElementJoinColumn;
import org.ppwcode.metainfo_I.Copyright;
import org.ppwcode.metainfo_I.License;

import static org.ppwcode.metainfo_I.License.Type.APACHE_V2;
import org.ppwcode.metainfo_I.vcs.SvnInfo;
import org.ppwcode.vernacular.semantics_VI.exception.CompoundPropertyException;
import org.ppwcode.vernacular.semantics_VI.exception.PropertyException;
import org.toryt.annotations_I.Basic;
import org.toryt.annotations_I.Expression;
import org.toryt.annotations_I.Invars;
import org.toryt.annotations_I.MethodContract;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;


//MUDO update intervention step/demand with the link
/**
 * The {@link SubY} describes all actions for which time can be registered, that
 * are general in nature, and do not refer to other entities (in this system or other systems).
 */
@Entity
@Table(name="suby")
@Copyright("2008 - $Date$, PeopleWare n.v.")
@License(APACHE_V2)
@SvnInfo(revision = "$Revision$",
         date     = "$Date$")
public class SubY extends Y {

  public static final String EMPTY = "";


  /* <property name="q">
     -------------------------------------------------------------------------*/
  /**
   * Can only be {@code null} on initialization.
   *
   * @return LocalizedNameDescription the description of the task
   */
  @Basic(
  init   = @Expression("$nameDescription.isEmpty()")
  )
  public final Set<Q> getQs() {
    return new HashSet<Q>($qs);
  }

  @MethodContract(
    pre = @Expression("q != null"),
    post = @Expression("$qs.contains(q)")
  )
  public final void addQ(Q q) {
    if (q != null) {
      $qs.add(q);
    }
  }

  @MethodContract(post = @Expression("!$qs.contains(q)"))
  public final void removeQ(Q q) {
    $qs.remove(q);
  }

  @OneToMany(targetEntity = Q.class, cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, fetch= FetchType.EAGER
      )
  @ElementJoinColumn(name = "suby_fk")
  @Invars({
      @Expression("$qs != null"),
      @Expression("!$qs.contains(null)")
    }
  )
  // hibernate and JPA do not allow final for the set.
  private Set<Q> $qs = new HashSet<Q>();
  //set is null after deserialization if not initialized before serialization

  /* </property> */



  /* <property name="active">
     -------------------------------------------------------------------------*/
  /**
   * Can only be {@code null} on initialization.
   *
   * @return boolean true if the task is still active
   */
  @Basic(
    init = @Expression("active == false")
  )
  public final boolean isActive() {
    return $active;
  }

  @MethodContract(
    post = @Expression("active == _active")
  )
  public final void setActive(boolean active) {
    $active = active;
  }

  @Column(name = "active")
  private boolean $active;
  /* </property> */


  @Override
  @MethodContract(
    post = {
        @Expression("name == null ? result.contains(this, 'name', 'NULL_NOT_ALLOWED', null)")
      }
  )
  public CompoundPropertyException wildExceptions() {
    CompoundPropertyException cpe = super.wildExceptions();

    for (Q q : getQs()) {
      if (q.getLocale() == null) {
        cpe.addElementException((new PropertyException(this, "nameDescription", "NULL_NOT_ALLOWED", null)));
      }
      if (q.getName() == null) {
        cpe.addElementException((new PropertyException(this, "nameDescription", "NULL_NOT_ALLOWED", null)));
      }
    }
    return cpe;
  }

}
