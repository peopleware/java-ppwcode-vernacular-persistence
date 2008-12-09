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
import static org.ppwcode.util.reflect_I.CloneHelpers.safeReference;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Version;

import org.ppwcode.metainfo_I.Copyright;
import org.ppwcode.metainfo_I.License;
import org.ppwcode.metainfo_I.vcs.SvnInfo;
import org.toryt.annotations_I.Basic;
import org.toryt.annotations_I.Expression;
import org.toryt.annotations_I.MethodContract;


/**
 * A partial implementation of the interface {@link VersionedPersistentBean}.
 *
 * @author    Ruben Vandeginste
 * @author    Jan Dockx
 * @author    PeopleWare n.v.
 */
@Copyright("2004 - $Date$, PeopleWare n.v.")
@License(APACHE_V2)
@SvnInfo(revision = "$Revision$",
         date     = "$Date$")
public abstract class AbstractVersionedPersistentBean<_Id_ extends Serializable, _Version_ extends Serializable>
    extends AbstractPersistentBean<_Id_>
    implements VersionedPersistentBean<_Id_, _Version_> {

  /*<property name="version">*/
  //------------------------------------------------------------------

  @Basic(init = @Expression("null"))
  public final _Version_ getPersistenceVersion() {
    return safeReference($persistenceVersion);
  }

  @MethodContract(
    post = @Expression("persistenceVersion == _persistenceVersion")
  )
  public final void setPersistenceVersion(final _Version_ persistenceVersion) {
    $persistenceVersion = safeReference(persistenceVersion);
  }

  @Version
  @Column(name="persistenceVersion")
  private _Version_ $persistenceVersion;

  /*</property>*/

}
