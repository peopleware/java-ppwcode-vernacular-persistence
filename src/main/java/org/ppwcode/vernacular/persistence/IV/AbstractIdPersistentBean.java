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

import java.io.Serializable;

/**
 * <p>Extension of {@link AbstractPersistentBean}, that adds an implementation of
 *   {@link #getPersistenceId()}.</p>
 * <p>Sadly, we cannot completely annotate the property for JPA use here. The mapping must be done on the private
 *   instance field, which is not available in subclasses, so it should be done here. Here, however, we do not have
 *   all information in the most general case. Notably, we probably want to use a generator specific for the concrete
 *   class by name. When you use this class, you should use XML mapping.</p>
 *
 * @author    Nele Smeets
 * @author    Ruben Vandeginste
 * @author    Jan Dockx
 * @author    PeopleWare n.v.
 */
@SuppressWarnings("WeakerAccess")
public abstract class AbstractIdPersistentBean<_Id_ extends Serializable> extends AbstractPersistentBean<_Id_> {

  /*<property name="id">*/
  //------------------------------------------------------------------

  /*
    @Basic(init = @Expression("null"))
  */
  public final _Id_ getPersistenceId() {
    return $persistenceId;
  }

  /**
   * Provided to make testing possible.
   */
  protected void setPersistenceId(_Id_ persistenceId) {
    $persistenceId = persistenceId;
  }

  private _Id_ $persistenceId;

  /*</property>*/

}
