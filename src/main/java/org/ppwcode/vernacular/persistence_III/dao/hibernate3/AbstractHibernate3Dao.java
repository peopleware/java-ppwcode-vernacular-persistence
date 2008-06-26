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

package org.ppwcode.vernacular.persistence_III.dao.hibernate3;


import static org.ppwcode.metainfo_I.License.Type.APACHE_V2;

import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.ppwcode.bean_VI.CompoundPropertyException;
import org.ppwcode.bean_VI.PropertyException;
import org.ppwcode.exception_N.SemanticException;
import org.ppwcode.metainfo_I.Copyright;
import org.ppwcode.metainfo_I.License;
import org.ppwcode.metainfo_I.vcs.SvnInfo;
import org.ppwcode.util.exception.Exceptions;
import org.ppwcode.vernacular.exception_N.TechnicalException;
import org.ppwcode.vernacular.persistence_III.PersistentBean;
import org.ppwcode.vernacular.persistence_III.dao.AbstractDao;
import org.ppwcode.vernacular.persistence_III.dao.Dao;
import org.ppwcode.vernacular.persistence_III.sql.SqlExceptionHandler;
import org.ppwcode.vernacular.persistence_III.sql.mysql.MySqlSqlExceptionHandler;
import org.toryt.annotations_I.Basic;
import org.toryt.annotations_I.Expression;
import org.toryt.annotations_I.MethodContract;



/**
 * Support methods for Hibernate <acronym title="Data Access Object">DAO</acronym>'s.
 * These implementations need a Hibernate {@link Session}.
 *
 * @author Jan Dockx
 * @author Peopleware n.v.
 *
 * @mudo not done
 */
@Copyright("2004 - $Date$, PeopleWare n.v.")
@License(APACHE_V2)
@SvnInfo(revision = "$Revision$",
         date     = "$Date$")
public abstract class AbstractHibernate3Dao extends AbstractDao {

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
    post = @Expression("session == _session")
  )
  public void setSession(final Session session) throws TechnicalException {
    $session = session;
  }

  private Session $session;

  /*</property>*/



  /**
   * Apart from SQLExceptions thrown by the driver, the server, stored procedures or
   * constraint violations, when using middleware, that can throw exceptions too.
   * In the Hibernate case, we never get naked SQLExceptions, but we only get
   * Hibernate exceptions from the middleware. These are either exceptions of the
   * middleware, or wrappers around SQLExceptions.
   * This method tries to decide whether the {@link HibernateException} we get is
   * from the middleware or from SQL. It it is from the middleware, it might be an
   * encapsulated {@link SemanticException} (e.g., raised by interceptors), or a programming
   * or configuration error. When it is a {@link SQLException} we let the {@link #getSqlExceptionHandler()}
   * handle it.
   */
  protected final void handleHibernateException(final HibernateException hExc, final String operationName, final PersistentBean<?> pb)
      throws TechnicalException, CompoundPropertyException {
    SQLException sqlExc = (SQLException)Exceptions.huntFor(hExc, SQLException.class);
    if ((sqlExc != null) && (getSqlExceptionHandler() != null)) {
      PropertyException pExc = getSqlExceptionHandler().handle(sqlExc, pb);
      if (pExc != null) {
        wrapInCompoundAndThrow(pExc);
      }
      // if we are here, the above handler did not translate into a PropertyException
      // cannot be that the record is not found
      throw new TechnicalException("problem " + operationName + " record", hExc);
    }
    CompoundPropertyException cp = (CompoundPropertyException)Exceptions
         .huntFor(hExc, CompoundPropertyException.class);
    if (cp != null) {
      throw cp;
    }
    else {
      // cannot be that the record is not found
      throw new TechnicalException("problem "
                                      + operationName
                                      + " record",
                                  hExc);
    }
  }

  private void wrapInCompoundAndThrow(final PropertyException pExc) throws CompoundPropertyException {
    assert pExc != null;
    CompoundPropertyException cpExc = new CompoundPropertyException(pExc.getOrigin(), null, null, null);
    cpExc.addElementException(pExc);
    cpExc.close();
    throw cpExc;
  }

}
