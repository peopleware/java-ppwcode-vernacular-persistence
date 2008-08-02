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

package org.ppwcode.vernacular.persistence_III.dao;


import static org.ppwcode.metainfo_I.License.Type.APACHE_V2;

import org.ppwcode.metainfo_I.Copyright;
import org.ppwcode.metainfo_I.License;
import org.ppwcode.metainfo_I.vcs.SvnInfo;
import org.ppwcode.vernacular.persistence_III.sql.SqlExceptionHandler;
import org.toryt.annotations_I.Basic;
import org.toryt.annotations_I.Expression;
import org.toryt.annotations_I.MethodContract;



/**
 * Support methods for <acronym title="Data Access Object">DAO</acronym>'s.
 * These implementations need a {@link SqlExceptionHandler}.
 *
 * @author Jan Dockx
 * @author Peopleware n.v.
 */
@Copyright("2004 - $Date$, PeopleWare n.v.")
@License(APACHE_V2)
@SvnInfo(revision = "$Revision$",
         date     = "$Date$")
public abstract class AbstractDao implements Dao {

  /*<property name="sqlExceptionHandler">*/
  //------------------------------------------------------------------

  @Basic(init = @Expression("null"))
  public final SqlExceptionHandler getSqlExceptionHandler() {
    return $sqlExceptionHandler;
  }

  @MethodContract(post = @Expression("sqlExceptionHandler == _sqlExceptionHandler"))
  public final void setSqlExceptionHandler(final SqlExceptionHandler sqlExceptionHandler) {
    $sqlExceptionHandler = sqlExceptionHandler;
  }

  private SqlExceptionHandler $sqlExceptionHandler;

  /*</property>*/

}
