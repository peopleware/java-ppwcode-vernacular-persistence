/*<license>
Copyright 2004 - $Date: 2008-11-16 15:35:15 +0100 (Sun, 16 Nov 2008) $ by PeopleWare n.v..

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
import static javax.transaction.Status.STATUS_COMMITTED;
import static javax.transaction.Status.STATUS_COMMITTING;
import static javax.transaction.Status.STATUS_MARKED_ROLLBACK;
import static javax.transaction.Status.STATUS_NO_TRANSACTION;
import static javax.transaction.Status.STATUS_ROLLEDBACK;
import static javax.transaction.Status.STATUS_ROLLING_BACK;
import static org.ppwcode.util.exception_III.ProgrammingErrorHelpers.preArgumentNotNull;

import javax.persistence.EntityTransaction;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.toryt.annotations_I.Basic;
import org.toryt.annotations_I.Expression;
import org.toryt.annotations_I.Invars;
import org.toryt.annotations_I.MethodContract;


/**
 * <p>a bridge to use an {@link EntityTransaction} as a {@link UserTransaction}.</p>
 * <p>For some dumbfounded reason I cannot find any explanation for on the 'net,
 *   JPA developers found it necessary to create a new transaction interface
 *   (see {@link EntityTransaction}), slightly different from the interface
 *   provided by JTA (see {@link UserTransaction}. This is a bridge to
 *   use an {@link EntityTransaction} as a {@link UserTransaction}.</p>
 * <p>To implement an {@link EntityTransaction} using a {@link UserTransaction}
 *   is relatively straightforward. The other way around however, is slightly
 *   more problematic, since {@link UserTransaction} offers more detailed
 *   status information than {@link EntityTransaction}. This class is a best
 *   effort. {@link Status#STATUS_UNKNOWN} is used as escape hatch. Some state
 *   is stored in the bridge to give better status information. The latter is
 *   only consistent if you use all methods through the bridge, and never
 *   directly on the encapsulated {@link EntityTransaction}. This makes this
 *   class a hack.</p>
 */
public class EntityUserTransactionBridge implements UserTransaction {


  @MethodContract(pre  = @Expression("_entityTransaction != null"),
                  post = @Expression("entityTransaction == _entityTransaction"))
  public EntityUserTransactionBridge(EntityTransaction entityTransaction) {
    assert preArgumentNotNull(entityTransaction, "entityTransaction");
    $entityTransaction = entityTransaction;
  }



  @Basic(invars = @Expression("entityTransaction != null"))
  public final EntityTransaction getEntityTransaction() {
    return $entityTransaction;
  }

  @Invars(@Expression("$entityTransaction != null"))
  private final EntityTransaction $entityTransaction;



  private final static String EXCEPTION_MESSAGE = "Exception from underlying EntityException";

  private static void systemException(Exception exc) throws SystemException {
    SystemException se = new SystemException(EXCEPTION_MESSAGE);
    se.initCause(exc);
    throw se;
  }



  public void begin() throws SystemException {
    try {
      getEntityTransaction().begin();
      $approximateStatus = STATUS_ACTIVE;
    }
    // IllegalStateException is dealt with as any other exception
    /* note: we never throw a NotSupportedException; there seems to be no appropriate match
            for the contract in entity transaction */
    catch (Exception exc) {
      systemException(exc);
    }
  }

  public void commit() throws IllegalStateException, RollbackException, SystemException {
    try {
      $approximateStatus = STATUS_COMMITTING;
      getEntityTransaction().commit();
      $approximateStatus = STATUS_COMMITTED;
    }
    // IllegalStateException passes through
    catch (javax.persistence.RollbackException rbExc) {
      $approximateStatus = STATUS_ROLLEDBACK;
      RollbackException rbe = new  RollbackException(EXCEPTION_MESSAGE);
      rbe.initCause(rbExc);
      throw rbe;
    }
    // note: we never throw HeuristicMixedException, HeuristicRollbackException or SecurityException
    catch (Exception exc) {
      systemException(exc);
    }
  }

  public void rollback() throws IllegalStateException, SystemException {
    try {
      $approximateStatus = STATUS_ROLLING_BACK;
      getEntityTransaction().rollback();
      $approximateStatus = STATUS_ROLLEDBACK;
    }
    // IllegalStateException passes through
    // PersistenceException is dealt with as any other exception
    // note: we never throw SecurityException
    catch (Exception exc) {
      systemException(exc);
    }
  }

  public void setRollbackOnly() throws IllegalStateException, SystemException {
    try {
      getEntityTransaction().setRollbackOnly();
      $approximateStatus = STATUS_MARKED_ROLLBACK;
    }
    // IllegalStateException passes through
    catch (Exception exc) {
      systemException(exc);
    }


  }

  public int getStatus() throws SystemException {
    return $approximateStatus;
  }

  private int $approximateStatus = STATUS_NO_TRANSACTION;



  /**
   * <strong>This method is not supported in this version of the bridge.</strong>
   */
  public void setTransactionTimeout(int seconds) throws SystemException {
    throw new SystemException("not supported in this version of the bridge");
  }

}
