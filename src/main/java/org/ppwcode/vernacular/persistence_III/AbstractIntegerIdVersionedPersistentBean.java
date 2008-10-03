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


import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

import org.ppwcode.vernacular.semantics_VI.bean.AbstractRousseauBean;
import org.toryt.annotations_I.Basic;
import org.toryt.annotations_I.Expression;
import org.toryt.annotations_I.MethodContract;


/**
 * <p>It turns out that, at least with OpenJPA, we cannot use a {@Link MappedSuperclass} annotation on classes
 *   that have a generic type parameter. For that reason, this class is introduced as a full code copy of
 *   {@link AbstractPersistentBean} and {@link AbstractVersionedPersistentBean}, with the {@code _Id_} fixed to
 *   {@link Integer}.</p>
 * <p>We tried to use an intermediate class where we resolve the generic parameter, but it turns out that the
 *   problem occurs whenever there is a generic parameter in the class hierarchy. Generic parameters in the
 *   interface hierarchy are not a problem.</p>
 * <p>This is very sad, an we hope this will be resolved in the future. Classes that now
 *   {@code ... extends AbstractIntegerIdVersionedPersistentBean} can be changed later, when the workaround is no
 *   longer needed, to {@code ... extends AbstractVersionedPersistentBean<Integer>}.</p>
 *
 */
@MappedSuperclass
public abstract class AbstractIntegerIdVersionedPersistentBean extends AbstractRousseauBean implements PersistentBean<Integer> {

  /*<property name="id">*/
  //------------------------------------------------------------------

  public final Integer getPersistenceId() {
    return $persistenceId;
  }

  public final boolean hasSamePersistenceId(final PersistentBean<Integer> other) {
    return (other != null)  && ((getPersistenceId() == null) ? other.getPersistenceId() == null : getPersistenceId().equals(other.getPersistenceId()));
  }

  public final void setPersistenceId(final Integer persistenceId) {
    $persistenceId = persistenceId;
  }

  @Id
  @GeneratedValue
  @Column(name="persistenceId")
  private Integer $persistenceId;

  /*</property>*/



  /*<property name="version">*/
  //------------------------------------------------------------------

  /**
   * Note that there is no setter for the persistence version. This is controlled by
   * "magic processes" like JPA, that can write into private fields. The developer
   * should never set the persistence version.
   */
  @Basic(init = @Expression("Long.MIN_VALUE"))
  public final Integer getPersistenceVersion() {
    return $persistenceVersion;
  }

  /**
   * @param     persistenceVersion
   *            The new value
   *
   * @note      This method is only available for testing purposes, and therefor is
   *            package accessible.
   */
  @MethodContract(
    post = @Expression("persistenceVersion == _persistenceVersion")
  )
  final void setPersistenceVersion(final Integer persistenceVersion) {
    $persistenceVersion = persistenceVersion;
  }

  @Version
  @Column(name="persistenceVersion")
  private Integer $persistenceVersion;

  /*</property>*/

}
