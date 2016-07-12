/*<license>
Copyright 2005 - 2016 by PeopleWare n.v..

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

package org.ppwcode.vernacular.persistence.IV;

import org.ppwcode.vernacular.semantics.VII.bean.AbstractRousseauBean;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import java.io.Serializable;

/**
 * <p>A partial implementation of the interface {@link PersistentBean}.</p>
 * <p>The {@link #getPersistenceId() persistenceId} is annotated for JPA use.</p>
 *
 * <p>Note: this version does not use the ppwcode util serialization alternative for serialization, as did earlier
 *   versions. The main reason, remote communication of serialized objects, has disappeared. It might be necessary
 *   to reinstate this at some time fo other reasons.</p>
 *
 * @author    Nele Smeets
 * @author    Ruben Vandeginste
 * @author    Jan Dockx
 * @author    PeopleWare n.v.
 *
 * // TODO We now have a dependency here on JPA via annotations. Also, the listener is defined in a subpackage, which
 *       depends on this package. This introduces a cycle! This is a bad idea. Like this, you always need the JPA
 *       libraries, even if they are annotations, because the annotations are loaded in the import statements too
 *       (at least under 1.5). Thus, the annotations must go, and we need to use the xml files.
 */
public abstract class AbstractPersistentBean<_Id_ extends Serializable> extends AbstractRousseauBean
    implements PersistentBean<_Id_> {

  /*<property name="id">*/
  //------------------------------------------------------------------

  public final _Id_ getPersistenceId() {
    return $persistenceId;
  }

  public final boolean hasSamePersistenceId(final PersistentBean<_Id_> other) {
    return (other != null)
            && ((getPersistenceId() == null)
              ? other.getPersistenceId() == null
              : getPersistenceId().equals(other.getPersistenceId()));
  }

  @Id
  @GeneratedValue(strategy = GenerationType.TABLE)
  @Column(name="id")
  private _Id_ $persistenceId;

  /*</property>*/

}
