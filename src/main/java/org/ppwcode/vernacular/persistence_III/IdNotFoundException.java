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


import static org.ppwcode.metainfo_I.License.Type.APACHE_V2;

import java.io.Serializable;

import org.ppwcode.metainfo_I.Copyright;
import org.ppwcode.metainfo_I.License;
import org.ppwcode.metainfo_I.vcs.SvnInfo;
import org.toryt.annotations_I.Basic;
import org.toryt.annotations_I.Expression;
import org.toryt.annotations_I.Invars;
import org.toryt.annotations_I.MethodContract;


/**
 * <p>This exception signals failure to locate a object with the given
 *   {@link #getId() id}. Since we cannot use
 *   generics in exceptions, the type of the id is just {@link Object}.</p>
 *
 * @author    Jan Dockx
 * @author    David Van Keer
 * @author    PeopleWare n.v.
 */
@Copyright("2004 - $Date$, PeopleWare n.v.")
@License(APACHE_V2)
@SvnInfo(revision = "$Revision$",
         date     = "$Date$")
public class IdNotFoundException extends PersistenceException {

  /*<construction>*/
  //------------------------------------------------------------------

  @MethodContract(
    pre = {
      @Expression("persistentBeanType != null"),
      @Expression("_id != null")
    },
    post = {
      @Expression("persistentBeanType == _persistentBeanType"),
      @Expression("id == _id"),
      @Expression("message == (_messageKey == null || _messageKey == EMPTY) ? DEFAULT_MESSAGE_KEY : _messageKey"),
      @Expression("cause == _cause")
    }
  )
  public <_Id_ extends Serializable>
  IdNotFoundException(Class<? extends PersistentBean<_Id_>> persistentBeanType,
                      _Id_ id, String messageKey, Throwable cause) {
    super(messageKey, cause);
    assert persistentBeanType != null;
    assert id != null;
    $persistentBeanType = persistentBeanType;
    $id = id;
  }

  @MethodContract(
    pre = {
      @Expression("persistentBeanType != null"),
      @Expression("_id != null")
    },
    post = {
      @Expression("persistentBeanType == _persistentBeanType"),
      @Expression("id == _id"),
      @Expression("message == DEFAULT_MESSAGE_KEY"),
      @Expression("cause == null")
    }
  )
  public <_Id_ extends Serializable>
  IdNotFoundException(Class<? extends PersistentBean<_Id_>> persistentBeanType, _Id_ id) {
    this(persistentBeanType, id, null, null);
  }

  @MethodContract(
    pre = {
      @Expression("persistentBeanType != null"),
      @Expression("_id != null")
    },
    post = {
      @Expression("persistentBeanType == _persistentBeanType"),
      @Expression("id == _id"),
      @Expression("message == DEFAULT_MESSAGE_KEY"),
      @Expression("cause == _cause")
    }
  )
  public <_Id_ extends Serializable>
  IdNotFoundException(Class<? extends PersistentBean<_Id_>> persistentBeanType, _Id_ id, Throwable cause) {
    this(persistentBeanType, id, null, cause);
  }

  /*</construction;>*/


  /*<property name="persistentBeanType">*/
  //------------------------------------------------------------------

  @Basic(invars = @Expression("persistentBeanType != null"))
  public final Class<? extends PersistentBean<?>> getPersistentBeanType() {
    return $persistentBeanType;
  }

  @Invars(@Expression("$persistentBeanType != null"))
  private final Class<? extends PersistentBean<?>> $persistentBeanType;

  /*</property>*/



  /*<property name="id">*/
  //------------------------------------------------------------------

  @Basic(invars = @Expression("id != null"))
  public final Object getId() {
    return $id;
  }

  @Invars(@Expression("$id != null"))
  private final Object $id;

  /*</property>*/

}
