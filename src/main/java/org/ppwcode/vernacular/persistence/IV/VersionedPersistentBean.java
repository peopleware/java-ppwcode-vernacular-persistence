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
 * <p>Persistency should always be implemented with versioning (optimistic locking). For that, the property
 *   {@link #getPersistenceVersion() version} is added to this interface. There are several different possible
 *   types of versioning, using an integer, date, or even a UUID. For that reason, the type of the property
 *   is generic. We advise however to use Integer as version type. The property is kept generic to allow
 *   for legacy systems.</p>
 * <p>_Version_ must be {@link Serializable}, because PersistentBeans are {@link Serializable} and the
 *   {@link #getPersistenceVersion()} is not {@code transient}. Although there is a case to be made
 *   to enforce that {@code _Version_} should be {@link Comparable}, in essence the ability to check for
 *   {@link Object#equals(Object) equality} suffices.</p>
 *
 * @author    Jan Dockx
 * @author    PeopleWare n.v.
 */
public interface VersionedPersistentBean<_Id_ extends Serializable, _Version_ extends Serializable>
    extends PersistentBean<_Id_> {

  /*<property name="persistence version">*/
  //------------------------------------------------------------------

  /*
    @Basic(init = @Expression("null"))
  */
  _Version_ getPersistenceVersion();

  /*</property>*/

}
