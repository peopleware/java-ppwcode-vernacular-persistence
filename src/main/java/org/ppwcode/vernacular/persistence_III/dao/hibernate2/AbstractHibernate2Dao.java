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

package org.ppwcode.vernacular.persistence_III.dao.hibernate2;


import static org.ppwcode.metainfo_I.License.Type.APACHE_V2;
import static org.ppwcode.util.exception_III.ExceptionHelpers.huntFor;

import java.sql.SQLException;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;

import org.ppwcode.metainfo_I.Copyright;
import org.ppwcode.metainfo_I.License;
import org.ppwcode.metainfo_I.vcs.SvnInfo;
import org.ppwcode.vernacular.exception_III.ExternalError;
import org.ppwcode.vernacular.exception_III.ApplicationException;
import org.ppwcode.vernacular.exception_III.SemanticException;
import org.ppwcode.vernacular.persistence_III.dao.AbstractDao;
import org.toryt.annotations_I.Basic;
import org.toryt.annotations_I.Expression;
import org.toryt.annotations_I.MethodContract;


/**
 * Support methods for Hibernate 2 <acronym title="Data Access Object">DAO</acronym>'s.
 * These implementations need a Hibernate 2 {@link Session}.
 *
 * @note Hibernate 2 version was used very much in the past, so can be considered stable.
 *       But: code has changed since ppwcode-vernacular-persistence II, so we are not sure.
 *       Since we are not currently working with Hibernate, we will not invest more in this
 *       at the moment (so no unit tests, etc.).
 *
 * @author Jan Dockx
 * @author Peopleware n.v.
 */
@Copyright("2004 - $Date$, PeopleWare n.v.")
@License(APACHE_V2)
@SvnInfo(revision = "$Revision$",
         date     = "$Date$")
public abstract class AbstractHibernate2Dao extends AbstractDao {


  /*<property name="session">*/
  //------------------------------------------------------------------

  /**
   * Return the Hibernate session for this Dao.
   */
  @Basic(init = @Expression("null"))
  public final Session getSession() {
      return $session;
  }


  /**
   * @param     session
   *            The hibernate session to use for database manipulations.
   */
  @MethodContract(
    post = {
      @Expression("session == _session")
    }
  )
  public void setSession(final Session session) {
      $session = session;
  }

  private Session $session;

  /*</property>*/



  @MethodContract(
    post = @Expression("session != null")
  )
  public boolean isOperational() {
    return $session != null;
  }



  /**
   * Apart from SQLExceptions thrown by the driver, the server, stored procedures or
   * constraint violations, when using middle-ware, that can throw exceptions too.
   * In the Hibernate case, we never get naked SQLExceptions, but we only get
   * Hibernate exceptions from the middle-ware. These are either exceptions of the
   * middleware, or wrappers around SQLExceptions.
   * This method tries to decide whether the {@link HibernateException} we get is
   * from the middleware or from SQL. It it is from the middle-ware, it might be an
   * encapsulated {@link SemanticException} (e.g., raised by interceptors), or a programming
   * or configuration error. When it is a {@link SQLException} we let the {@link #getSqlExceptionHandler()}
   * handle it.
   */
  protected final void handleHibernateException(final HibernateException hExc, final String operationName)
      throws ApplicationException {
    SQLException sqlExc = huntFor(hExc, SQLException.class);
    if ((sqlExc != null) && (getSqlExceptionHandler() != null)) {
      ApplicationException iExc = getSqlExceptionHandler().handle(sqlExc);
      if (iExc != null) {
        throw iExc;
      }
      // if we are here, the above handler did not translate into an ApplicationException
      // cannot be that the record is not found
      // the sql exception is thus considered an external problem (thrown by handle)
    }
    // Now, if it is not a sql exception we are dealing with, maybe there is an internal
    // exception transported by the hibernate exception
    ApplicationException iExc = huntFor(hExc, ApplicationException.class);
    if (iExc != null) {
      throw iExc;
    }
    else {
      // cannot be that the record is not found
      // the Hibernate exception is thus considered an external problem
      throw new ExternalError("problem " + operationName + " record", hExc);
    }
  }

}
