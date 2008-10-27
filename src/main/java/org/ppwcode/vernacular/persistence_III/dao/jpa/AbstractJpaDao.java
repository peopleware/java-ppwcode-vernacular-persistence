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
 * <p>An AbstractJpaDao provides an entity manager.</p>
 * <p>Since some subclasses do not commit themselves, we cannot in general say that we handle SQL
 *   exceptions in these types. So, in general, we do not need a Sql Exception Handler. So,
 *   we do not inherit from AbstractDao.</p>
 * <p>When used in an EJB container, add dependency injection for the entity manager
 * (e.g.,</p>
 * <pre>
 * &#64;PersistenceContext(unitName=&quot;<var>persistence_unit_name</var>&quot;)
 * public final EntityManager getEntityManager() {
 *   return super.getEntityManager();
 * }
 * </pre>,
 * <p>or add the following lines to <kbd>META-INF/ejb-jar.xml</kbd>:</p>
 * <pre>
 * <ejb-jar>
 *   <enterprise-beans>
 *     <session>
 *       <ejb-name>
 *       <ejb-class>
 *       <persistence-context-ref><var>persistence_unit_name</var></persistence-context-ref>
 *     </sesso
 *
 * MUDO unfinished doc
 * ...
 * ...
 * </pre>
 * .
 *
 * MUDO doc unfinished
 *   /*<property name="entity manager">
  -------------------------------------------------------------------------ASTERIX/

  @Basic(init = @Expression("null"))
  public EntityManager getEntityManager() {
    return $entityManager;
  }

  @MethodContract(
    post = @Expression("entityManager == _manager")
  )
  public final void setEntityManager(EntityManager manager) {
    $entityManager = manager;
  }

  private EntityManager $entityManager;

  /*</property>ASTERIX/

 *
 * @node We tried to generalize injection of the entity manager via a constructor with the
 *       persistence unit name as parameter, and then using
 *       <code>$entityManager = Persistence.createEntityManagerFactory(persistenceUnitName)</code>
 *       to get the entity manager ourselfs. <strong>This works outside the container, but
 *       not in the container.</strong> When we tried to use this inside WebSphere, the result
 *       was a locked database. We presume the reason for that was that 2 entity managers,
 *       one from the container, and the one we got ourselfs, are fighting for the resource.
 */
public abstract class AbstractJpaDao implements Dao {

  /*
   * This does NOT work in a JEE container. See the note in the class documentation.
   * Keep this code here to avoid making the same mistake in the future.
   */
  //  /**
  //   * With this constructor, the entity manager is set to an entity manager of
  //   * the persistence unit with name {@code persistenceUnitName}.
  //   */
  //  @MethodContract(pre  = {
  //                    @Expression("_persistenceUnitName != null"),
  //                    @Expression(value = "! Persistence.createEntityManagerFactory(persistenceUnitName) throws",
  //                                description = "_persistenceUnitName must be an existing persistence unit name")
  //                  },
  //                  post = @Expression("entityManager == Persistence.createEntityManagerFactory(_persistenceUnitName).createEntityManager()"))
  //  protected AbstractJpaDao(String persistenceUnitName) {
  //    preArgumentNotNull(persistenceUnitName, "persistenceUnitName");
  //    EntityManagerFactory emf = null;
  //    try {
  //      emf = Persistence.createEntityManagerFactory(persistenceUnitName);
  //    }
  //    catch (PersistenceException pExc) {
  //      unexpectedException(pExc);
  //    }
  //    $entityManager = emf.createEntityManager();
  //  }



  @Basic
  public abstract EntityManager getEntityManager();


  @MethodContract(post = @Expression("result ? entityManager != null"))
  public boolean isOperational() {
    return getEntityManager() != null;
  }

}
