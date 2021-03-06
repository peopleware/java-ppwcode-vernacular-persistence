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


/**
 * <p>This exception signals a versioning conflict: we tried to change data in persistent
 *   storage for {@link #getObject()}, but the data in persistent storage has
 *   changed already, since the last time we looked. We will not overwrite this more
 *   recent data without user interaction.</p>
 *
 * @author    Jan Dockx
 * @author    PeopleWare n.v.
 */
public class AlreadyChangedException extends PersistenceException {

  /*<construction>*/
  //------------------------------------------------------------------

  /*
    @MethodContract(
      pre = {
        @Expression("object != null")
      },
      post = {
        @Expression("object == _object"),
        @Expression("message == DEFAULT_MESSAGE_KEY"),
        @Expression("cause == _cause")
      }
    )
  */
  public AlreadyChangedException(Object object, Throwable cause) {
    super(null, cause);
    assert object != null;
    $object = object;
  }

  /*</construction;>*/



  /*<property name="persistentBean">*/
  //------------------------------------------------------------------

  /*
    @Basic(invars = @Expression("object != null"))
  */
  public final Object getObject() {
    return $object;
  }

  /*
    @Invars(@Expression("$object != null"))
  */
  private final Object $object;

  /*</property>*/

}
