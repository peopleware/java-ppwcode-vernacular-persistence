/*<license>
Copyright 2004 - $Date$ by PeopleWare n.v..

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


import static org.ppwcode.vernacular.exception_II.ExceptionHelpers.huntFor;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ppwcode.vernacular.exception_II.ExternalError;
import org.ppwcode.vernacular.exception_II.InternalException;
import org.ppwcode.vernacular.exception_II.handle.ExceptionTriager;
import org.toryt.annotations_I.Basic;
import org.toryt.annotations_I.Expression;
import org.toryt.annotations_I.MethodContract;


/**
 * Triage {@link SQLException SQLExceptions}. A {@link SqlExceptionHandler} is used to
 * try to discern {@link InternalException InternalExceptions} from database exceptions.
 * Since other {@link SQLException SQLExceptions} in production (past testing) are probably
 * deployment issues, they are translated into {@link ExternalError ExternalErrors}.
 * The {@link SqlExceptionHandler} should be set to a handler specific for the type of
 * database used.
 */
public class SqlExceptionTriager implements ExceptionTriager {


  private static final Log _LOG = LogFactory.getLog(SqlExceptionTriager.class);


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

  @MethodContract(
    post = {
      @Expression("huntFor(_t, SQLException.class) != null && sqlExceptionHandler != null && " +
                  "sqlExceptionHandler.handle(huntFor(_t, SQLException.class)) != null ? " +
                  "result == sqlExceptionHandler.handle(huntFor(_t, SQLException.class))"),
      @Expression("huntFor(_t, SQLException.class) != null && sqlExceptionHandler != null && " +
                  "sqlExceptionHandler.handle(huntFor(_t, SQLException.class)) == null ? " +
                  "result instanceof ExternalError && result.cause == huntFor(_t, SQLException.class)"),
      @Expression("huntFor(_t, SQLException.class) != null && sqlExceptionHandler == null ? result == _t")
    }
  )
  public Throwable triage(Throwable t) {
    SQLException sqlExc = huntFor(t, SQLException.class);
    if (sqlExc != null) {
      if (getSqlExceptionHandler() != null) {
        InternalException iExc = getSqlExceptionHandler().handle(sqlExc); // errors are errors in the handler, let it pass
        if (iExc != null) {
          return iExc;
        }
        else {
          // any other SQL exception is probably a deployment problem
          return new ExternalError("A SQL error occured that was not recognized as an internal exception", sqlExc);
        }
      }
      else {
        _LOG.warn("SqlExceptionTriager has no SqlExceptionHandler");
      }
    }
    // not a SQLException in sight
    return t;
  }

}
