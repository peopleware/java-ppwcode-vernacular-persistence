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

package org.ppwcode.vernacular.persistence_III.dao.hibernate2;


import static org.ppwcode.metainfo_I.License.Type.APACHE_V2;

import java.sql.SQLException;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;

import org.ppwcode.exception_N.SemanticException;
import org.ppwcode.metainfo_I.Copyright;
import org.ppwcode.metainfo_I.License;
import org.ppwcode.metainfo_I.vcs.SvnInfo;
import org.ppwcode.util.exception.Exceptions;
import org.ppwcode.vernacular.exception_N.InternalException;
import org.ppwcode.vernacular.persistence_III.PersistenceExternalError;
import org.ppwcode.vernacular.persistence_III.dao.AbstractDao;


/**
 * Support methods for Hibernate 2 <acronym title="Data Access Object">DAO</acronym>'s.
 * These implementations need a Hibernate 2 {@link Session}.
 *
 * @author Jan Dockx
 * @author Peopleware n.v.
 */
@Copyright("2004 - $Date: 2008-06-26 12:34:30 +0200 (Thu, 26 Jun 2008) $, PeopleWare n.v.")
@License(APACHE_V2)
@SvnInfo(revision = "$Revision: 1374 $",
         date     = "$Date: 2008-06-26 12:34:30 +0200 (Thu, 26 Jun 2008) $")
public abstract class AbstractHibernate2Dao extends AbstractDao {


  /*<property name="session">*/
  //------------------------------------------------------------------

  /**
   * Return the Hibernate session for this Dao.
   */
  public final Session getSession() {
      return $session;
  }


  /**
   * @param     session
   *            The hibernate session to use for database manipulations.
   * @post      new.getSession() == session;
   */
  public void setSession(final Session session) {
      $session = session;
  }

  private Session $session;

  /*</property>*/


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
      throws InternalException, PersistenceExternalError {
    SQLException sqlExc = (SQLException)Exceptions.huntFor(hExc, SQLException.class);
    if ((sqlExc != null) && (getSqlExceptionHandler() != null)) {
      InternalException iExc = getSqlExceptionHandler().handle(sqlExc);
      if (iExc != null) {
        throw iExc;
      }
      // if we are here, the above handler did not translate into an InternalException
      // cannot be that the record is not found
      // the sql exception is thus considered an external problem
      throw new PersistenceExternalError("problem " + operationName + " record", hExc);
    }
    // Now, if it is not a sql exception we are dealing with, maybe there is an internal
    // exception transported by the hibernate exception
    InternalException iExc = (InternalException)Exceptions.huntFor(hExc, InternalException.class);
    if (iExc != null) {
      throw iExc;
    }
    else {
      // cannot be that the record is not found
      // the Hibernate exception is thus considered an external problem
      throw new PersistenceExternalError("problem " + operationName + " record", hExc);
    }
  }

}
