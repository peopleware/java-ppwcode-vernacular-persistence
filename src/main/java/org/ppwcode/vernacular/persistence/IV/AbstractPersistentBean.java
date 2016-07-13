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

import java.io.Serializable;

/**
 * <p>A partial implementation of the interface {@link PersistentBean}.</p>
 * <p>Sadly, we cannot implement {@link #getPersistenceId()}, because we cannot completely annotate
 *   the property for JPA use here. The mapping must be done on the private instance field, which
 *   is not available in subclasses, so it should be done here. Here, however, we do not have all information
 *   in the most general case. Notably, we probably want to use a generator specific for the concrete class by
 *   name. The alternative is to do mapping with XML files, without annotations.
 *   See {@link AbstractIdPersistentBean}.</p>
 *
 * <p>Implementation of {@link #getPersistenceId()} should be done in subclasses, with the following idiom:</p>
 * <pre>
 * /**
 *  * Basic inspector. Initially {@code null}.
 *  *-
 * public final _Id_ getPersistenceId(){
 *   return $persistenceId;
 * }
 *
 * ATId
 * ATGeneratedValue(strategy = GenerationType.TABLE, generator = "<SPECIFIC GENERATOR NAME>")
 * ATColumn(name = "id")
 * private _Id_ $persistenceId;
 * </pre>
 *
 * <p>Note: this version does not use the ppwcode util serialization alternative for serialization, as did earlier
 *   versions. The main reason, remote communication of serialized objects, has disappeared. It might be necessary
 *   to reinstate this at some time fo other reasons.</p>
 *
 * @author    Nele Smeets
 * @author    Ruben Vandeginste
 * @author    Jan Dockx
 * @author    PeopleWare n.v.
 */
@SuppressWarnings("WeakerAccess")
public abstract class AbstractPersistentBean<_Id_ extends Serializable> extends AbstractRousseauBean
    implements PersistentBean<_Id_> {

  /*<property name="id">*/
  //------------------------------------------------------------------

  public final boolean hasSamePersistenceId(final PersistentBean<_Id_> other) {
    return (other != null)
            && ((getPersistenceId() == null)
              ? other.getPersistenceId() == null
              : getPersistenceId().equals(other.getPersistenceId()));
  }

  /*</property>*/

}
