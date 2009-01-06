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

package org.ppwcode.vernacular.transaction_I.jta;


import static javax.transaction.Status.STATUS_ACTIVE;
import static javax.transaction.Status.STATUS_COMMITTING;
import static javax.transaction.Status.STATUS_MARKED_ROLLBACK;
import static javax.transaction.Status.STATUS_PREPARED;
import static javax.transaction.Status.STATUS_PREPARING;
import static org.ppwcode.metainfo_I.License.Type.APACHE_V2;
import static org.ppwcode.util.exception_III.ProgrammingErrorHelpers.preArgumentNotNull;
import static org.ppwcode.util.exception_III.ProgrammingErrorHelpers.unexpectedException;

import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;
import javax.persistence.RollbackException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.ppwcode.metainfo_I.Copyright;
import org.ppwcode.metainfo_I.License;
import org.ppwcode.metainfo_I.vcs.SvnInfo;
import org.toryt.annotations_I.Basic;
import org.toryt.annotations_I.Expression;
import org.toryt.annotations_I.Invars;
import org.toryt.annotations_I.MethodContract;


/**
 * <p>A bridge to use a {@link UserTransaction} as an {@link EntityTransaction}.</p>
 * <p>For some dumbfounded reason I cannot find any explanation for on the 'net,
 *   JPA developers found it necessary to create a new transaction interface
 *   (see {@link EntityTransaction}), slightly different from the interface
 *   provided by JTA (see {@link UserTransaction}. This is a bridge to
 *   use a {@link UserTransaction} as an {@link EntityTransaction}.</p>
 */
@Copyright("2004 - $Date$, PeopleWare n.v.")
@License(APACHE_V2)
@SvnInfo(revision = "$Revision$",
         date     = "$Date$")
public class UserEntityTransactionBridge implements EntityTransaction {


  private static final String EXCEPTION_MESSAGE = "exception from underlying UserTransaction";



  @MethodContract(pre  = @Expression("_userTransaction != null"),
                  post = @Expression("userTransaction == _userTransaction"))
  public UserEntityTransactionBridge(UserTransaction userTransaction) {
    assert preArgumentNotNull(userTransaction, "userTransaction");
    $userTransaction = userTransaction;
  }



  @Basic(invars = @Expression("userTransaction != null"))
  public final UserTransaction getUserTransaction() {
    return $userTransaction;
  }

  @Invars(@Expression("$userTransaction != null"))
  private final UserTransaction $userTransaction;



  public void begin() throws IllegalStateException {
    if (isActive()) {
      throw new IllegalStateException("transaction is already active");
    }
    try {
      getUserTransaction().begin();
    }
    catch (NotSupportedException nsExc) {
      unexpectedException(nsExc);
    }
    catch (SystemException sExc) {
      unexpectedException(sExc);
    }
  }

  public void commit() throws IllegalStateException, RollbackException {
    try {
      getUserTransaction().commit();
    }
    // IllegalStateException passes through
    catch (HeuristicRollbackException hrbExc) {
      throw new RollbackException(EXCEPTION_MESSAGE, hrbExc);
    }
    catch (javax.transaction.RollbackException rbExc) {
      throw new RollbackException(EXCEPTION_MESSAGE, rbExc);
    }
    catch (SecurityException sExc) {
      unexpectedException(sExc);
    }
    catch (HeuristicMixedException hmExc) {
      unexpectedException(hmExc);
    }
    catch (SystemException sExc) {
      unexpectedException(sExc);
    }
  }

  public void rollback() throws IllegalStateException, PersistenceException {
    try {
      getUserTransaction().rollback();
    }
    // IllegalStateException passes through
    catch (SecurityException sExc) {
      throw new PersistenceException(EXCEPTION_MESSAGE, sExc);
    }
    catch (SystemException sExc) {
      throw new PersistenceException(EXCEPTION_MESSAGE, sExc);
    }
  }

  public void setRollbackOnly() throws IllegalStateException {
    try {
      getUserTransaction().setRollbackOnly();
    }
    // IllegalStateException passes through
    catch (SystemException sExc) {
      unexpectedException(sExc);
    }
  }

  public boolean getRollbackOnly() {
    try {
      return getUserTransaction().getStatus() == STATUS_MARKED_ROLLBACK;
    }
    catch (SystemException sExc) {
      unexpectedException(sExc);
      return false; // keep compiler happy
    }
  }

  public boolean isActive() {
    try {
      int status = getUserTransaction().getStatus();
      return status == STATUS_ACTIVE || status == STATUS_COMMITTING || status == STATUS_MARKED_ROLLBACK ||
             status == STATUS_PREPARING || status == STATUS_PREPARING  || status == STATUS_PREPARED;
    }
    catch (SystemException sExc) {
      unexpectedException(sExc);
      return false; // keep compiler happy
    }
  }

}
