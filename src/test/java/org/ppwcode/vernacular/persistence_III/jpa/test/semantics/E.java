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


import static org.ppwcode.metainfo_I.License.Type.APACHE_V2;
import static org.ppwcode.util.exception_III.ProgrammingErrorHelpers.pre;
import static org.ppwcode.util.exception_III.ProgrammingErrorHelpers.preArgumentNotNull;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.ppwcode.metainfo_I.Copyright;
import org.ppwcode.metainfo_I.License;
import org.ppwcode.metainfo_I.vcs.SvnInfo;
import org.ppwcode.util.serialization_I.DoNotSerialize;
import org.ppwcode.vernacular.persistence_III.jpa.AbstractIntegerIdVersionedPersistentBean;
import org.ppwcode.vernacular.semantics_VI.exception.CompoundPropertyException;
import org.ppwcode.vernacular.semantics_VI.exception.PropertyException;
import org.toryt.annotations_I.Basic;
import org.toryt.annotations_I.Expression;
import org.toryt.annotations_I.Invars;
import org.toryt.annotations_I.MethodContract;
import org.toryt.annotations_I.Throw;


/**
 *
 */
@Entity
@Table(name="e")
@Copyright("2008 - $Date$, PeopleWare n.v.")
@License(APACHE_V2)
@SvnInfo(revision = "$Revision$",
         date     = "$Date$")
public class E extends AbstractIntegerIdVersionedPersistentBean {

  public static final String EMPTY = "";


  /*<property name="name">
  -------------------------------------------------------------------------*/
  /**
   * Can be {@code null}.
   */
  @Basic(invars = @Expression("name != EMPTY"),
         init   = @Expression("name == null"))
  public final String getName() {
    return $name;
  }

  @MethodContract(
    post = @Expression("name == _name"),
    exc  = @Throw(type = PropertyException.class, cond = @Expression("_name == EMPTY"))
  )
  public final void setName(String name) throws PropertyException {
    if (EMPTY.equals(name)) {
      throw new PropertyException(this, "name", "EMPTY_NOT_ALLOWED", null);
    }
    $name = name;
  }

  @Column(name="name")
  private String $name;

  /*</property>*/



  /*<property name="xs">
  -------------------------------------------------------------------------*/

  @MethodContract(pre  = {@Expression("_x != null"),
                          @Expression("_x.e == this")},
                  post = @Expression("xs.contains(_t)"))
  final void addX(X x) throws PropertyException {
    assert preArgumentNotNull(x, "x");
    assert pre(x.getE() == this);
    $xs.add(x);
  }

  @MethodContract(post = @Expression("!xs.contains(_x)"))
  final void removeX(X x) {
    $xs.remove(x);
  }

  @Basic(init   = @Expression("xs.isEmpty()"),
         invars = {@Expression("xs != null"),
                   @Expression("! xs.contains(null)"),
                   @Expression("for (X x : xs) {x.e == this}")})
  final public Set<X> getXs() {
    return new HashSet<X>($xs);
  }

  @OneToMany(mappedBy = "$e", cascade = {}, fetch= FetchType.LAZY)
  @Invars({@Expression("$xs != null"),
           @Expression("!$xs.contains(null)"),
           @Expression("for (X x : $xs) { x.e == this }")})
  @DoNotSerialize
  // hibernate and JPA do not allow final for the set.
  private Set<X> $xs = new HashSet<X>();
  // set is null after deserialization if not initialized before serialization

  /*</property>*/



  @Override
  @MethodContract(
    post = @Expression("name == null ? result.contains(this, 'name', 'NULL_NOT_ALLOWED', null)")
  )
  public CompoundPropertyException wildExceptions() {
    CompoundPropertyException cpe = super.wildExceptions();
    if (getName() == null) {
      cpe.addElementException((new PropertyException(this, "name", "NULL_NOT_ALLOWED", null)));
    }
    return cpe;
  }

}

