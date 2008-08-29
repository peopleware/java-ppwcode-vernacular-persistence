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

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

import org.ppwcode.metainfo_I.Copyright;
import org.ppwcode.metainfo_I.License;
import org.ppwcode.metainfo_I.vcs.SvnInfo;
import org.ppwcode.vernacular.semantics_VI.bean.AbstractRousseauBean;
import org.toryt.annotations_I.Basic;
import org.toryt.annotations_I.Expression;
import org.toryt.annotations_I.MethodContract;


/**
 * A partial implementation of the interface {@link PersistentBean}.
 *
 * @author    Nele Smeets
 * @author    Ruben Vandeginste
 * @author    Jan Dockx
 * @author    PeopleWare n.v.
 */
@Copyright("2004 - $Date$, PeopleWare n.v.")
@License(APACHE_V2)
@SvnInfo(revision = "$Revision$",
         date     = "$Date$")
public abstract class AbstractPersistentBean<_Id_ extends Serializable> extends AbstractRousseauBean
    implements PersistentBean<_Id_> {

  /*<property name="id">*/
  //------------------------------------------------------------------

  public final _Id_ getId() {
    return $id;
  }

  public final boolean hasSameId(final PersistentBean<_Id_> other) {
    return (other != null)  && ((getId() == null) ? other.getId() == null : getId().equals(other.getId()));
  }

  public final void setId(final _Id_ id) {
    $id = id;
  }

  private _Id_ $id;

  /*</property>*/



  /*<property name="version">*/
  //------------------------------------------------------------------

  @Basic(init = @Expression("Long.MIN_VALUE"))
  public final Long getPersistenceVersion() {
    return $version;
  }

  /**
   * @param     version
   *            The new value
   *
   * @note      This method is only available for testing purposes, and therefor is
   *            package accessible.
   */
  @MethodContract(
    post = @Expression("persistenceVersion == _version")
  )
  final void setPersistenceVersion(final Long version) {
    $version = version;
  }

//  @Version
//  @Column(name="version")
  private Long $version;
  //private long $version = Long.MIN_VALUE;

  /*</property>*/

}
