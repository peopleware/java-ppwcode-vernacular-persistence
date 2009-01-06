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


import static org.ppwcode.metainfo_I.License.Type.APACHE_V2;
import static org.ppwcode.util.exception_III.ExceptionHelpers.huntFor;
import static org.ppwcode.util.exception_III.ProgrammingErrorHelpers.newAssertionError;

import javax.transaction.HeuristicCommitException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.TransactionRequiredException;

import org.ppwcode.metainfo_I.Copyright;
import org.ppwcode.metainfo_I.License;
import org.ppwcode.metainfo_I.vcs.SvnInfo;
import org.ppwcode.vernacular.exception_III.ExternalError;
import org.ppwcode.vernacular.exception_III.handle.ExceptionTriager;
import org.toryt.annotations_I.Expression;
import org.toryt.annotations_I.MethodContract;


/**
 * <p>Triage JTA exceptions into the ppwcode exception vernacular.</p>
 * <p>A {@link SystemException} is considered an {@link ExternalError}.</p>
 * <p>There is no handling for RollbackException nor TransactionRolledbackException. Rollback is obviously
 *   non-nominal, normal behavior; We look however for a deeper reason. If we did not find it, we let other
 *   triagers try. If nobody recognizes an internal reason for the roll-back, this is a programming error,
 *   and will finally be handled like that. For us, this is non-triaged.</p>
 * <p>All other expcetions are considered programming errors. They should not occur in a correct system,
 *   or be dealt with closer to their occurence, higher in the stack.</p>
 */
@Copyright("2004 - $Date$, PeopleWare n.v.")
@License(APACHE_V2)
@SvnInfo(revision = "$Revision$",
         date     = "$Date$")
public class JtaTriager implements ExceptionTriager {

  @MethodContract(
    post = {
      @Expression("huntFor(t, SystemException.class) != null ? result instanceof ExternalError && result.cause == huntFor(t, SystemException.class)"),
      @Expression("huntFor(t, HeuristicCommitException.class) != null ? result instanceof AssertionError && result.cause == huntFor(t, HeuristicCommitException.class)"),
      @Expression("huntFor(t, HeuristicMixedException.class) != null ? result instanceof AssertionError && result.cause == huntFor(t, HeuristicMixedException.class)"),
      @Expression("huntFor(t, HeuristicRollbackException.class) != null ? result instanceof AssertionError && result.cause == huntFor(t, HeuristicRollbackException.class)"),
      @Expression("huntFor(t, InvalidTransactionException.class) != null ? result instanceof AssertionError && result.cause == huntFor(t, InvalidTransactionException.class)"),
      @Expression("huntFor(t, TransactionRequiredException.class) != null ? result instanceof AssertionError && result.cause == huntFor(t, TransactionRequiredException.class)"),
      @Expression("huntFor(t, SystemException.class) == null &&  huntFor(t, HeuristicCommitException.class) == null && " +
                  "huntFor(t, HeuristicMixedException.class) == null && huntFor(t, HeuristicRollbackException.class) == null && " +
                  "huntFor(t, InvalidTransactionException.class) == null && huntFor(t, TransactionRequiredException.class) == null ? " +
                    "result == t")
    }
  )
  public Throwable triage(Throwable t) {
    SystemException sExc = huntFor(t, SystemException.class);
    if (sExc != null) {
      return new ExternalError("the system could not execute a request", sExc);
    }
    // it is not necessary to triage programming errors, but hey, now we can add some interesting messages
    HeuristicCommitException hcExc = huntFor(t, HeuristicCommitException.class);
    if (hcExc != null) {
      return newAssertionError("heuristic commit is not acceptable", hcExc);
    }
    HeuristicMixedException hmExc = huntFor(t, HeuristicMixedException.class);
    if (hmExc != null) {
      return newAssertionError("mixed commit is not acceptable", hcExc);
    }
    HeuristicRollbackException hrExc = huntFor(t, HeuristicRollbackException.class);
    if (hrExc != null) {
      return newAssertionError("mixed rollback is not acceptable", hrExc);
    }
    InvalidTransactionException itExc = huntFor(t, InvalidTransactionException.class);
    if (itExc != null) {
      return newAssertionError("invalid transaction context", itExc);
    }
    NotSupportedException nsExc = huntFor(t, NotSupportedException.class);
    if (nsExc != null) {
      return newAssertionError("transaction-related operation is not supported", nsExc);
    }
    TransactionRequiredException trExc = huntFor(t, TransactionRequiredException.class);
    if (trExc != null) {
      return newAssertionError("an existing transaction is required", trExc);
    }
    /* There is no handling for RollbackException nor TransactionRolledbackException. Rollback is obviously
     * non-nominal, normal behavior; We look however for a deeper reason. If we did not find it, we let other
     * triagers try. If nobody recognizes an internal reason for the roll-back, this is a programming error,
     * and will finally be handled like that. For us, this is non-triaged. */
    return t;
  }

}
