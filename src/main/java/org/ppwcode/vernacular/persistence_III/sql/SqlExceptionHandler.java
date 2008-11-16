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

package org.ppwcode.vernacular.persistence_III.sql;


import static org.ppwcode.metainfo_I.License.Type.APACHE_V2;

import java.sql.SQLException;

import org.ppwcode.metainfo_I.Copyright;
import org.ppwcode.metainfo_I.License;
import org.ppwcode.metainfo_I.vcs.SvnInfo;
import org.ppwcode.vernacular.exception_III.ExternalError;
import org.ppwcode.vernacular.exception_III.ApplicationException;
import org.ppwcode.vernacular.persistence_III.dao.Dao;
import org.toryt.annotations_I.Expression;
import org.toryt.annotations_I.MethodContract;
import org.toryt.annotations_I.Throw;


/**
 * <p>Abstraction of how to handle {@link SQLException SQLExceptions}.</p>
 * <p>{@link Dao Dao's} often must deal with potential exceptions from a JDBC driver. These either come from
 *   the driver, the database server, or from exceptions that are raised by triggers or stored procedures,
 *   or by constraint violations. They all turn up as {@link SQLException SQLExceptions}.</p>
 * <p>The first kinds are of an external nature. Normally, this is fatal for an application. According to
 *   ppwcode exception vernacular, they should be encapsulated in an {@link ExternalError}. The latter kinds
 *   are semantic exceptions. Normally, they would result in a roll-back, user feedback, and continuation
 *   of the normal operation of the application. According to ppwcode exception vernacular, they should be
 *   encapsulated in an {@link ApplicationException}</p>
 * <p>It is impossible for {@link Dao Dao's} to decide of which kind the {@link SQLException} is in general.
 *   Database exceptions are not much more than a string, and there is no standardization over different
 *   database engines. Furthermore, exceptions raised by triggers and stored procedures, and constraints,
 *   are application specific.</p>
 * <p>Implementations of {@link #handle(SQLException)} should return an {@link ApplicationException}
 *   that wraps the given {@link SQLException} if they find it of an internal (semantic, persistent) nature. If not,
 *   they should not end nominally, but throw an {@link ExternalError} or {@link AssertionError} instead.
 *   Implementation methods should have no side effects. During this process, it is possible that access of the
 *   persistent storage is needed (e.g., a case encounter regularly is where i18n messages for semantic exceptions
 *   are defined in tables in the database). If such an access fails, the method should throw an {@link ExternalError}
 *   or {@link AssertionError} without further ado.</p>
 *
 * @author    Jan Dockx
 * @author    PeopleWare n.v.
 */
@Copyright("2004 - $Date$, PeopleWare n.v.")
@License(APACHE_V2)
@SvnInfo(revision = "$Revision$",
         date     = "$Date$")
public interface SqlExceptionHandler {

  /**
   * Return an {@link ApplicationException} wrapping <code>sqlException</code>
   * if you find the latter of a semantic nature. Otherwise, return null.
   *
   * @param sqlException
   *        The exception to handle.
   */
  @MethodContract(
    pre  = @Expression("_sqlException != null"),
    post = @Expression("true"),
    exc  = {
      @Throw(type = AssertionError.class,
             cond = @Expression(value = "true",
                                description = "could perform the operation because of a bad configuration of " +
                                              "this object, which is considered a programming error or external condition")),
      @Throw(type = ExternalError.class,
             cond = @Expression(value = "true",
                                description = "could perform the operation because of some problem with persistency " +
                                              "which we consider external"))
    }
  )
  ApplicationException handle(SQLException sqlException);

}
