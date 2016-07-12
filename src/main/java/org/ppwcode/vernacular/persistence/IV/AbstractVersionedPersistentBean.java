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


import javax.persistence.Column;
import javax.persistence.Version;
import java.io.Serializable;

import static org.ppwcode.vernacular.semantics.VII.util.CloneHelpers.safeReference;


/**
 * A partial implementation of the interface {@link VersionedPersistentBean}.
 *
 * @author    Ruben Vandeginste
 * @author    Jan Dockx
 * @author    PeopleWare n.v.
 */
@SuppressWarnings("WeakerAccess")
public abstract class AbstractVersionedPersistentBean<_Id_ extends Serializable, _Version_ extends Serializable>
    extends AbstractPersistentBean<_Id_>
    implements VersionedPersistentBean<_Id_, _Version_> {

  /*<property name="version">*/
  //------------------------------------------------------------------

  /*
    @Basic(init = @Expression("null"))
  */
  public final _Version_ getPersistenceVersion() {
    return safeReference($persistenceVersion);
  }

  /**
   * Provided to make testing possible.
   */
  protected void setPersistenceVersion(_Version_ persistenceVersion) {
    $persistenceVersion = persistenceVersion;
  }

  @Version
  @Column(name="persistenceVersion")
  private _Version_ $persistenceVersion;

  /*</property>*/

}
