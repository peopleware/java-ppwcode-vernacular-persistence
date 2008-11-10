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

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.ppwcode.metainfo_I.Copyright;
import org.ppwcode.metainfo_I.License;
import org.ppwcode.metainfo_I.vcs.SvnInfo;
import org.ppwcode.vernacular.persistence_III.jpa.AbstractIntegerIdVersionedPersistentBean;

/**
 * generalized class with one subclass SubY
 */
@Entity
@Table (name = "y")
@Inheritance(strategy= InheritanceType.JOINED)
@Copyright("2008 - $Date$, PeopleWare n.v.")
@License(APACHE_V2)
@SvnInfo(revision = "$Revision$",
         date     = "$Date$")
public abstract class Y extends AbstractIntegerIdVersionedPersistentBean {

  /**
  * {@link Y} has a unidirectional relationship with {@link A} but
  * JPA needs to know the relationship between the two to be able to remove
  * {@link Y}.
  */
  @SuppressWarnings("unused")
  @OneToMany(mappedBy = "$y", cascade = {}, fetch= FetchType.LAZY)
  private Set<X> $xs;
}
