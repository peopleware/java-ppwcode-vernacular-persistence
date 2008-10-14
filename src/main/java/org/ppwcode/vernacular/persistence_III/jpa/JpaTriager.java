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

package org.ppwcode.vernacular.persistence_III.jpa;


import static org.ppwcode.vernacular.exception_II.ExceptionHelpers.huntFor;
import static org.ppwcode.vernacular.exception_II.ProgrammingErrorHelpers.newAssertionError;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.RollbackException;
import javax.persistence.TransactionRequiredException;

import org.ppwcode.vernacular.exception_II.handle.ExceptionTriager;
import org.ppwcode.vernacular.persistence_III.AlreadyChangedException;
import org.toryt.annotations_I.Expression;
import org.toryt.annotations_I.MethodContract;


/**
 * <p>Triage JPA (javax.persistence) exceptions into the ppwcode exception vernacular.</p>
 * <p>{@link OptimisticLockException} and {@link EntityNotFoundException} are converted
 *   into an {@link AlreadyChangedException}.</p>
 * <p>There is no handling for {@link RollbackException}. Roll-back is obviously non-nominal, normal behavior.
 *  We look however for a deeper reason. If we did not find it, we let other triagers try. If nobody recognizes
 *  an internal reason for the roll-back, this is a programming error, and will finally be handled like that.
 *  For us, this is non-triaged.</p>
 * <p>Other exceptions are considered programming errors. They should not occur in a correct program, or
 *  should have been dealt with, or already converted, closer to where they occur, higher in the stack.</p>
 */
public class JpaTriager  implements ExceptionTriager {

  @MethodContract(
    post = {
      @Expression("huntFor(t, OptimisticLockException.class) != null ? result instanceof AlreadyChangedException && result.cause == huntFor(t, OptimisticLockException.class)"),
      @Expression("huntFor(t, EntityNotFoundException.class) != null ? result instanceof AlreadyChangedException && result.cause == huntFor(t, EntityNotFoundException.class)"),
      @Expression("huntFor(t, NonUniqueResultException.class) != null ? result instanceof AssertionError && result.cause == huntFor(t, NonUniqueResultException.class)"),
      @Expression("huntFor(t, NoResultException.class) != null ? result instanceof AssertionError && result.cause == huntFor(t, NoResultException.class)"),
      @Expression("huntFor(t, EntityExistsException.class) != null ? result instanceof AssertionError && result.cause == huntFor(t, EntityExistsException.class)"),
      @Expression("huntFor(t, TransactionRequiredException.class) != null ? result instanceof AssertionError && result.cause == huntFor(t, TransactionRequiredException.class)"),
      @Expression("huntFor(t, OptimisticLockException.class) == null &&  huntFor(t, EntityNotFoundException.class) == null && " +
                  "huntFor(t, NonUniqueResultException.class) == null && huntFor(t, NoResultException.class) == null && " +
                  "huntFor(t, EntityExistsException.class) == null && huntFor(t, TransactionRequiredException.class) == null ? " +
                    "result == t")
    }
  )
  public Throwable triage(Throwable t) {
    OptimisticLockException olExc = huntFor(t, OptimisticLockException.class);
    if (olExc != null) {
      return new AlreadyChangedException(olExc.getEntity(), olExc);
    }
    EntityNotFoundException enfExc = huntFor(t, EntityNotFoundException.class);
    if (enfExc != null) {
      /* this means a bean (we don't know which) we are working with was already removed from the database;
       * that is a version of optimistic locking
       */
      return new AlreadyChangedException(null, olExc);
    }
    // it is not necessary to triage programming errors, but hey, now we can add some interesting messages
    NonUniqueResultException nurExc = huntFor(t, NonUniqueResultException.class);
    if (nurExc != null) {
      return newAssertionError("we got more than one result when we expected only one", nurExc);
    }
    NoResultException nrExc = huntFor(t, NoResultException.class);
    if (nrExc != null) {
      return newAssertionError("we got no results, but we expected results; this possibility " +
                               "should have been taken into account in the code executing the query", t);
    }
    EntityExistsException eeExc = huntFor(t, EntityExistsException.class);
    if (eeExc != null) {
      return newAssertionError("caught entity exists exception from JPA; this cannot happen in a correct program", eeExc);
    }
    TransactionRequiredException trExc = huntFor(t, TransactionRequiredException.class);
    if (trExc != null) {
      return newAssertionError("an existing transaction is required for this operation, and there was none", trExc);
    }
    /* There is no handling for RollbackException. Roll-back is obviously non-nominal, normal behavior; We look however for a
     * deeper reason. If we did not find it, we let other triagers try. If nobody recognizes an internal reason for the roll-back,
     * this is a programming error, and will finally be handled like that. For us, this is non-triaged. */
    return t;
  }

}
