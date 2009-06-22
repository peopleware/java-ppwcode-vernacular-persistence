package org.ppwcode.vernacular.persistence_III.dao.jpa;

import javax.persistence.EntityManager;

import org.ppwcode.util.exception_III.ProgrammingErrorHelpers;
import org.toryt.annotations_I.Basic;
import org.toryt.annotations_I.Expression;
import org.toryt.annotations_I.MethodContract;

/**
 * This Dao is meant to be used out of container. i.e. the entitymanager
 * will be programmatically created, instead of being injected by a container.
 * The JpaStatelessCrudDao must be configured with an entity manager using
 * a setter.
 *
 * Note that transaction control is still left out.  It is at this
 * moment not decided whether the CRUD operation should run in its own transaction
 * or will be part of another transaction.
 *
 * Note that the rollbackOnly implementation can be provided since we will
 * use EntityTransactions in this application
 *
 * @author tmahieu
 *
 */
public class JpaOutOfContainerStatelessCrudDao extends JpaStatelessCrudDao {


  /*<property name="entity manager
    --------------------------------------------------------------------- */

  @Override
  @Basic
  public EntityManager getEntityManager() {
    return $entityManager;
  }

  @MethodContract(
      post=@Expression("entityManager == _entitymanager")
  )
  public void setEntityManager(EntityManager entitymanager) {
    $entityManager = entitymanager;
  }

  private EntityManager $entityManager = null;
  /*</property>*/

  @Override
  protected boolean getRollbackOnlyImpl() {
    // TODO Auto-generated method stub
    return getEntityManager().getTransaction().getRollbackOnly();
  }

  @Override
  protected void rollbackOnlyPrecondition() throws AssertionError {
    ProgrammingErrorHelpers.dependency(getEntityManager(), "entityManager");
  }

  @Override
  protected void setRollbackOnlyImpl() throws IllegalStateException {
    getEntityManager().getTransaction().setRollbackOnly();
  }
}
