/*<license>
Copyright 2004 - $Date: 2008-10-06 16:44:16 +0200 (Mon, 06 Oct 2008) $ by PeopleWare n.v..

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

import java.sql.SQLException;

import javax.ejb.EJBAccessException;
import javax.ejb.EJBContext;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.persistence.OptimisticLockException;
import javax.persistence.TransactionRequiredException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ppwcode.vernacular.exception_II.ExceptionHelpers;
import org.ppwcode.vernacular.exception_II.ExternalError;
import org.ppwcode.vernacular.exception_II.InternalException;
import org.ppwcode.vernacular.persistence_III.dao.AbstractDao;
import org.ppwcode.vernacular.persistence_III.sql.SqlExceptionHandler;


/**

 */
public class Ejb3ExceptionHandler {

  private final static Log _LOG = LogFactory.getLog(Ejb3ExceptionHandler.class);

  @javax.annotation.Resource
  EJBContext ejbContext;

  IN THE DAO??? NO, THEN IT BECOMES JPA / EJB3 dependent

  @AroundInvoke
  public Object handleExceptions(InvocationContext invCtx) throws Throwable { // <<-EXception
    try {
      return invCtx.proceed();
      /* MUDO I think this only works with indirect RequiresNew or BMT; because interceptor methods are supposed to share the transaction scope of the invoked method.
       * With a traditional Requires transaction attribute, the commit wil not yet have happened, and thus there will (not often) be an exception yet.
       */
    }
    catch (Throwable t) {
      ExternalError extErr = huntFor(t, ExternalError.class);
      if (extErr != null) {
        // log and warn the administrator
        _LOG.error("A deployment issue occured. This requires attention of the administrator", extErr);
        // MUDO send a mail
        // rethrow
        throw extErr; // since this is an unchecked exception, this will abort any transaction
      }
      InternalException intExc = huntFor(t, InternalException.class);
      if (intExc != null) {
        // this is an application exception; abort the transaction and rethrow; there is no need to warn anybody
        _LOG.debug("internal exception); this is non-nominal, normal and expected behavior", intExc);
        ejbContext.setRollbackOnly(); // we force a roll-back here
        throw t;
      }
      SQLException sqlExc = huntFor(t, SQLException.class);
      if (sqlExc != null) {
        // TODO would be better if we could inject the SqlExceptionHandler here
        Object target = invCtx.getTarget();
        try {
          AbstractDao adTarget = (AbstractDao)adTarget;
          SqlExceptionHandler seh = adTarget.getSqlExceptionHandler();
          if (seg != null) {
            try {
              InternalException iExc = seh.handle((SQLException)t);
              throw iExc; -- internal
            }
            catch (Exception -- external or programming)
          }
        }
        catch (ClassCastException ccExc) {
          external or programming is default
        }
      }
      OptimisticLockException olExc = huntFor(t, OptimisticLockException.class);
      if (olExc != null) {

      }
      TransactionRequiredException trExc = huntFor(t, TransactionRequiredException.class); also local
      if (trExc != null) {

      }
      EJBAccessException eaExc = huntFor(t, EJBAccessException.class); --> SecurityException
      ObjectNotFoundException--> IdNotFoundException cehck with enitty manager
      RemoveException
      //javax.ejb.EJBTransactionRolledbackException, + Local
      // more ?!?!?!

      /*
       * persistence
EntityExistsException
EntityNotFoundException
NonUniqueResultException
NoResultException
OptimisticLockException
PersistenceException
RollbackException
TransactionRequiredException
       *
       * transaction
HeuristicCommitException
HeuristicMixedException
HeuristicRollbackException
InvalidTransactionException
NotSupportedException
RollbackException
SystemException
TransactionRequiredException
TransactionRolledbackException
       */
      else {
        // all other exceptions are considered programming errors
      }
    }
  }

}

