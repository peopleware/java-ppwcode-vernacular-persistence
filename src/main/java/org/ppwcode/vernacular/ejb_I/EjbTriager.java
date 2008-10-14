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

package org.ppwcode.vernacular.ejb_I;


import static org.ppwcode.vernacular.exception_II.ExceptionHelpers.huntFor;
import static org.ppwcode.vernacular.exception_II.ProgrammingErrorHelpers.newAssertionError;

import javax.ejb.AccessLocalException;
import javax.ejb.ConcurrentAccessException;
import javax.ejb.DuplicateKeyException;
import javax.ejb.EJBAccessException;
import javax.ejb.EJBException;
import javax.ejb.EJBTransactionRequiredException;
import javax.ejb.EJBTransactionRolledbackException;
import javax.ejb.NoSuchEJBException;
import javax.ejb.NoSuchEntityException;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.TransactionRequiredLocalException;
import javax.ejb.TransactionRolledbackLocalException;

import org.ppwcode.vernacular.exception_II.ExternalError;
import org.ppwcode.vernacular.exception_II.handle.ExceptionTriager;
import org.ppwcode.vernacular.persistence_III.AlreadyChangedException;
import org.toryt.annotations_I.Expression;
import org.toryt.annotations_I.MethodContract;


/**
 * <p>Triage EJB (javax.ejb) exceptions into the ppwcode exception vernacular.</p>
 * <p>{@link NoSuchEntityException} is converted into an {@link AlreadyChangedException}.
 *   {@link AccessLocalException} and {@link EJBAccessException} are converted into
 *   a {@link SecurityException}. {@link ConcurrentAccessException}, {@link DuplicateKeyException},
 *   {@link EJBTransactionRequiredException}, {@link TransactionRequiredLocalException}, {@link NoSuchEJBException}
 *   and {@link NoSuchObjectLocalException} instances are converted into {@link AssertionError programming errors}.
 *   A general {@link EJBException}, which is not covered by another case, is converted into an
 *   {@link ExternalError}.</p>
 * <p>There is no handling for {@link EJBTransactionRolledbackException} or
 * {@link TransactionRolledbackLocalException}. Roll-back is obviously non-nominal, normal behavior.
 *  We look however for a deeper reason. If we did not find it, we let other triagers try. If nobody recognizes
 *  an internal reason for the roll-back, this is a programming error, and will finally be handled like that.
 *  For us, this is non-triaged.</p>
 * <p>Other exceptions are considered programming errors. They should not occur in a correct program, or
 *  should have been dealt with, or already converted, closer to where they occur, higher in the stack.</p>
 */
public class EjbTriager implements ExceptionTriager {

  @MethodContract(
    post = {
      @Expression("huntFor(t, NoSuchEntityException.class) != null ? result instanceof AlreadyChangedException && result.cause == huntFor(t, NoSuchEntityException.class)"),
      @Expression("huntFor(t, AccessLocalException.class) != null ? result instanceof SecurityException && result.cause == huntFor(t, AccessLocalException.class)"),
      @Expression("huntFor(t, EJBAccessException.class) != null ? result instanceof SecurityException && result.cause == huntFor(t, EJBAccessException.class)"),
      @Expression("huntFor(t, ConcurrentAccessException.class) != null ? result instanceof AssertionError && result.cause == huntFor(t, ConcurrentAccessException.class)"),
      @Expression("huntFor(t, DuplicateKeyException.class) != null ? result instanceof AssertionError && result.cause == huntFor(t, DuplicateKeyException.class)"),
      @Expression("huntFor(t, EJBTransactionRequiredException.class) != null ? result instanceof AssertionError && result.cause == huntFor(t, EJBTransactionRequiredException.class)"),
      @Expression("huntFor(t, TransactionRequiredLocalException.class) != null ? result instanceof AssertionError && result.cause == huntFor(t, TransactionRequiredLocalException.class)"),
      @Expression("huntFor(t, NoSuchEJBException.class) != null ? result instanceof AssertionError && result.cause == huntFor(t, NoSuchEJBException.class)"),
      @Expression("huntFor(t, NoSuchObjectLocalException.class) != null ? result instanceof AssertionError && result.cause == huntFor(t, NoSuchObjectLocalException.class)"),
      @Expression("huntFor(t, EJBException.class) != null ? result instanceof ExternalError && result.cause == huntFor(t, EJBException.class)"),
      @Expression("huntFor(t, NoSuchEntityException.class) == null &&  huntFor(t, AccessLocalException.class) == null && " +
                  "huntFor(t, EJBAccessException.class) == null && huntFor(t, ConcurrentAccessException.class) == null && " +
                  "huntFor(t, DuplicateKeyException.class) == null && huntFor(t, EJBTransactionRequiredException.class) == null && " +
                  "huntFor(t, TransactionRequiredLocalException.class) == null && huntFor(t, NoSuchEJBException.class) == null && " +
                  "huntFor(t, NoSuchObjectLocalException.class) == null && huntFor(t, EJBException.class) == null ? " +
                    "result == t")
    }
  )
  public Throwable triage(Throwable t) {
    /* We do not handle CreateException, FinderException, ObjectNotFoundException, RemoveException
     * These exceptions are intended as application exceptions, which is similar to InternalExceptions in
     * the ppwcode vernacular. If anything, they should be transformed into some form of InternalException
     * before they reach us.
     */
    NoSuchEntityException nseExc = huntFor(t, NoSuchEntityException.class);
    if (nseExc != null) {
      /* this means a bean (we don't know which) we are working with was already removed from the database;
       * that is a version of optimistic locking
       */
      return new AlreadyChangedException(null, nseExc);
    }
    AccessLocalException alExc = huntFor(t, AccessLocalException.class);
    if (alExc !=  null) {
      return new SecurityException(null, alExc);
    }
    EJBAccessException eaExc = huntFor(t, EJBAccessException.class);
    if (eaExc !=  null) {
      return new SecurityException(null, eaExc);
    }

    // it is not necessary to triage programming errors, but hey, now we can add some interesting messages
    ConcurrentAccessException caExc = huntFor(t, ConcurrentAccessException.class);
    if (caExc != null) {
      return newAssertionError("stateful session beans can only be used by one user / client at a " +
                               "time; this should have been dealt with when it occured", caExc);
    }
    DuplicateKeyException dkExc = huntFor(t, DuplicateKeyException.class);
    if (dkExc != null) {
      return newAssertionError("a correct program should never try to generate a duplicate key", dkExc);
    }
    EJBTransactionRequiredException etrExc = huntFor(t, EJBTransactionRequiredException.class);
    if (etrExc != null) {
      return newAssertionError("an existing transaction is required", etrExc);
    }
    TransactionRequiredLocalException trExc = huntFor(t, TransactionRequiredLocalException.class);
    if (trExc != null) {
      return newAssertionError("an existing transaction is required", trExc);
    }
    NoSuchEJBException nsEjbExc = huntFor(t, NoSuchEJBException.class);
    if (nsEjbExc != null) {
      return newAssertionError("an enterprise bean could not be found", nsEjbExc);
    }
    NoSuchObjectLocalException nsoExc = huntFor(t, NoSuchObjectLocalException.class);
    if (nsoExc != null) {
      return newAssertionError("an enterprise bean could not be found", nsoExc);
    }

    EJBException ejbExc = huntFor(t, EJBException.class);
    if (ejbExc != null) {
      return new ExternalError("the system could not execute a request", ejbExc);
    }
    /* There is no handling for EJBTransactionRolledbackException or TransactionRolledbackLocalException.
     * Rollback is obviously non-nominal, normal behavior; We look however for a deeper reason. If we did
     * not find it, we let other triagers try. If nobody recognizes an internal reason for the roll-back,
     * this is a programming error, and will finally be handled like that. For us, this is non-triaged. */
    return null;
  }

}
