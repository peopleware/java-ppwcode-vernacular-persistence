/*<license>
Copyright 2005 - $Date$ by PeopleWare n.v..

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

package org.ppwcode.vernacular.persistence_III.dao.jpa;


import javax.persistence.EntityManager;

import org.ppwcode.vernacular.persistence_III.dao.Dao;
import org.toryt.annotations_I.Basic;
import org.toryt.annotations_I.Expression;
import org.toryt.annotations_I.MethodContract;


/**
 * An AbstractJpaDao provides an entity manager.
 * Since some subclasses do not commit themselves, we cannot in general say that we handle SQL
 * exceptions in these types. So, in general, we do not need a Sql Exception Handler. So,
 * we do not inherit from AbstractDao.
 *
 * When used in an EJB container, add dependency injection for the entity manager
 * (e.g., <code>@#64;PersistenceContext(unitName="<var>persistence_unit_name</var>)</code>.
 *
 * @mudo generalize persistence context injection and explain
 */
public abstract class AbstractJpaDao implements Dao {

  /*<property name="entity manager">
  -------------------------------------------------------------------------*/

  @Basic
  public final EntityManager getEntityManager() {
    return $entityManager;
  }

  @MethodContract(
    post = @Expression("entityManager == _manager")
  )
  public final void setEntityManager(EntityManager manager) {
    $entityManager = manager;
  }

  private EntityManager $entityManager;

  /*</property>*/


  @MethodContract(post = @Expression("result ? entityManager != null"))
  public boolean isOperational() {
    return $entityManager != null;
  }

}
