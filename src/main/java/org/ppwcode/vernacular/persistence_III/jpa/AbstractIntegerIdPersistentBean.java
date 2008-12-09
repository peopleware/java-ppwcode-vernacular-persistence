/*<license>
Copyright 2005 - $Date: 2008-10-17 11:22:05 +0200 (Fri, 17 Oct 2008) $ by PeopleWare n.v..

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

package org.ppwcode.vernacular.persistence_III.jpa;


import static org.ppwcode.metainfo_I.License.Type.APACHE_V2;
import static org.ppwcode.util.serialization_I.SerializationHelpers.replace;

import java.io.NotSerializableException;
import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.ppwcode.metainfo_I.Copyright;
import org.ppwcode.metainfo_I.License;
import org.ppwcode.metainfo_I.vcs.SvnInfo;
import org.ppwcode.vernacular.persistence_III.AbstractPersistentBean;
import org.ppwcode.vernacular.persistence_III.PersistentBean;
import org.ppwcode.vernacular.persistence_III.VersionedPersistentBean;
import org.ppwcode.vernacular.semantics_VI.bean.AbstractRousseauBean;


/**
 * <p>It turns out that, at least with OpenJPA, we cannot use a {@Link MappedSuperclass} annotation on classes
 *   that have a generic type parameter. For that reason, this class is introduced as a full code copy of
 *   {@link AbstractPersistentBean}, with the {@code _Id_} fixed to {@link Integer}.</p>
 * <p>We tried to use an intermediate class where we resolve the generic parameter, but it turns out that the
 *   problem occurs whenever there is a generic parameter in the class hierarchy. Generic parameters in the
 *   interface hierarchy are not a problem.</p>
 * <p>This is very sad, an we hope this will be resolved in the future. Classes that now
 *   {@code ... extends AbstractIntegerIdPersistentBean} can be changed later, when the workaround is no
 *   longer needed, to {@code ... extends AbstractVersionedPersistentBean<Integer>}.</p>
 * <p>This adds the use of the ppwcode util serialization alternative for serialization.
 *   This means that <code>&#64;DoNotSerialize</code can be used where you want transient serialization, but you
 *   cannot use that keyword because of its effect on JPA and possibly other persistence solutions.</p>
 *
 */
@Copyright("2004 - $Date: 2008-10-17 11:22:05 +0200 (Fri, 17 Oct 2008) $, PeopleWare n.v.")
@License(APACHE_V2)
@SvnInfo(revision = "$Revision: 3134 $",
         date     = "$Date: 2008-10-17 11:22:05 +0200 (Fri, 17 Oct 2008) $")
@MappedSuperclass
@EntityListeners({JpaRousseauBeanValidator.class})
public abstract class AbstractIntegerIdPersistentBean<_Version_ extends Serializable> extends AbstractRousseauBean
    implements VersionedPersistentBean<Integer, _Version_> {

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



  /**
   * Use the ppwcode serialization util alternative for serialization.
   * This means that <code>&#64;DoNotSerialize</code can be used where
   * you want transient serialization, but you cannot use that keyword because
   * of its effect on JPA and possibly other persistence solutions.
   */
  protected final Object writeReplace() throws NotSerializableException {
    return replace(this);
  }

}
