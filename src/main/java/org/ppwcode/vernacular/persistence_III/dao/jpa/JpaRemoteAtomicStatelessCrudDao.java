package org.ppwcode.vernacular.persistence_III.dao.jpa;

import org.ppwcode.vernacular.persistence_III.dao.RemoteAtomicStatelessCrudDao;
import org.toryt.annotations_I.Basic;
import org.toryt.annotations_I.Expression;
import org.toryt.annotations_I.MethodContract;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

public abstract class JpaRemoteAtomicStatelessCrudDao extends
		RemoteAtomicStatelessCrudDao {

	public abstract EntityManagerFactory getEntityManagerFactory();

  /*<property name="entity manager">
  -------------------------------------------------------------------------*/

  @Basic(init = @Expression("null"))
  private EntityManager getEntityManager() {
    return $entityManager;
  }

  @MethodContract(
    post = @Expression("entityManager == _manager")
  )
  private final void setEntityManager(EntityManager manager) {
    $entityManager = manager;
  }

  private EntityManager $entityManager = null;

  /*</property>*/

  /*<property name="transaction">
  -------------------------------------------------------------------------*/
  @Basic(init = @Expression("false"))
  private EntityTransaction getTransaction() {
  	return $transaction;
  }
  
  @MethodContract(
      post = @Expression("transaction == _tx")
    )
  private void setTransaction(EntityTransaction tx) {
  	$transaction = tx;
  }
  
  private EntityTransaction $transaction = null;
  /*</property>*/
  
  @Override
  public boolean isOperational() {
  	return getEntityManager() != null
  		&& getTransaction() != null
  		&& getTransaction().isActive();
  }

  @Override
	protected void beginTransaction() {
			setEntityManager(getEntityManagerFactory().createEntityManager());
			setTransaction(getEntityManager().getTransaction());
			getTransaction().begin();
	}

	@Override
	protected void commitTransaction() {
		getTransaction().commit();
	}

	@Override
	protected void rollbackTransaction() {
		getTransaction().rollback();
	}

}
