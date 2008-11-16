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

package org.ppwcode.vernacular.persistence_III.junit.hibernate2;


import static org.junit.Assert.assertNotNull;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ppwcode.vernacular.persistence_III.PersistentBean;
import org.ppwcode.vernacular.persistence_III.dao.PagingList;
import org.ppwcode.vernacular.semantics_VI.exception.CompoundPropertyException;
import org.ppwcode.vernacular.semantics_VI.exception.PropertyException;
import org.toryt.annotations_I.Basic;
import org.toryt.annotations_I.Expression;
import org.toryt.annotations_I.Invars;
import org.toryt.annotations_I.MethodContract;


/**
 * A simple helper class for Hibernate actions within jUnit tests.
 *
 * @invar   getClassUnderTest() != null;
 * @invar   PersistentBean.class.isAssignableFrom(getClassUnderTest());
 *
 * @author  David Van Keer
 * @author  Jan Dockx
 * @author  Tom Mahieu
 * @author  Peopleware n.v.
 */
public abstract class AbstractHibernate2PersistentBeanTest<_Id_ extends Serializable, _PersistentBean_ extends PersistentBean<_Id_>>
    extends AbstractHibernate2Test {

  private static final Log LOG = LogFactory.getLog(AbstractHibernate2PersistentBeanTest.class);

  /**
   * Create a new test for the given class.
   */
  @MethodContract(
    pre  = @Expression("_classUnderTest != null"),
    post = @Expression("classUnderTest == _classUnderTest")
  )
  protected AbstractHibernate2PersistentBeanTest(final Class<_PersistentBean_> classUnderTest) {
    assert classUnderTest != null;
    $classUnderTest = classUnderTest;
  }



 /*<property name="class under test">*/
 //------------------------------------------------------------------

  /**
   * Returns the class that is tested.
   */
  @Basic(invars = @Expression("classUnderTest != null"))
  public final Class<_PersistentBean_> getClassUnderTest() {
    return $classUnderTest;
  }

  @Invars(@Expression("$classUnderTest != null"))
  private Class<_PersistentBean_> $classUnderTest;

  /*</property>*/



  /**
   * Tests all instances of {@link #getClassUnderTest()} in the underlying
   * storage.
   * The method {@link #validatePersistentBean(PersistentBean)} is used to test
   * the persistent beans.
   * When logging is debug enabled, we only retrieve and test 1 page.
   */
  public void testAlInstances() {
    LOG.debug("Opening Hibernate session and starting a new transaction.");
    openSession();
    LOG.info("Creating paging set to retrieve instances of " + getClassUnderTest() + " from database in a new session.");
    PagingList<_Id_, _PersistentBean_>.PagesIterator pages = loadInstancesToTest().listIterator();
    if (pages.hasNext()) {
      LOG.info("Retrieving instances of page " + pages.nextIndex() + " of "+ getClassUnderTest() + " from database.");
      List<_PersistentBean_> pbs = pages.next();
      LOG.info("Retrieved " + pbs.size() + " PersistentBeans.");
      for (_PersistentBean_ pb : pbs) {
        validatePersistentBean(pb);
      }
    }
    LOG.debug("Closing session");
    closeSession();
  }

  /**
   * Overwrite if you do not wish to test all instances.
   * Session is open.
   */
  protected PagingList<_Id_, _PersistentBean_> loadInstancesToTest() {
    return retrievePages(getClassUnderTest());
  }

//  /**
//   * Retrieves the class contract corresponding to the class that is tested.
//   *
//   * @return (ClassContract)Contracts.typeContractInstance(getClassUnderTest())
//   *               does not throw an exception
//   *           ? result == (ClassContract)Contracts.typeContractInstance(getClassUnderTest())
//   *           : result == null;
//   */
//  protected final ClassContract getClassContract() {
//    ClassContract result = null;
//    try {
//      result = (ClassContract)Contracts.typeContractInstance(getClassUnderTest());
//    }
//    catch (IOException e) {
//      assert false : "IOException should not happen: " + e;
//    }
//    catch (ClassNotFoundException e) {
//      assert false : "ClassNotFoundException should not happen: " + e;
//    }
//    return result;
//  }

  /**
   * Validate the given persistent bean.
   * The following validations are executed:
   * - the given persistent bean should be effective
   * - the invariants are checked
   * - some extra validation, using {@link #extraPersistentBeanValidation(PersistentBean)}
   */
  protected void validatePersistentBean(final _PersistentBean_ pb) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("pb: " + ((pb == null) ? "null" : pb.toString()));
    }
    assertNotNull(pb);
//    validateTypeInvariants(pb);
    boolean civilized = pb.civilized();
    /* data in DB must not really be civilized. What we STORE must be,
     * but what we get doesn't have to be (as long as type invariants
     * are ok.
     * But it is something weird: WARN.
     */
    if (LOG.isWarnEnabled() && (!civilized)) {
      CompoundPropertyException cpe = pb.wildExceptions();
      LOG.warn("Not civilized: " + pb);
      for (PropertyException pe : cpe.getElementExceptions()) {
        LOG.warn("    " + pe.getLocalizedMessage());
        LOG.warn("    originType: " + pe.getOriginType());
        LOG.warn("    origin: " + pe.getOrigin());
        LOG.warn("    propertyName: " + pe.getPropertyName());
      }
    }
    extraPersistentBeanValidation(pb);
  }

  /**
   * Some extra validation to be performed on the given persistent bean.
   * Should be overridden by subclasses.
   */
  protected void extraPersistentBeanValidation(final _PersistentBean_ pb) {
    // NOP
  }

//  private void validateTypeInvariants(final Object instance) {
//    assert instance != null;
//    LOG.debug("getClassContract(): " + getClassContract());
//    Set invars = getClassContract().getTypeInvariantConditions();
//    Map context = new HashMap();
//    context.put(Condition.SUBJECT_KEY, instance);
//    Iterator iter = invars.iterator();
//    while (iter.hasNext()) {
//      Condition c = (Condition)iter.next();
//      boolean result = c.validate(context);
//      if (LOG.isErrorEnabled() && (!result)) {
//        LOG.error("type invariant violation: " + c + " for " + instance);
//      }
//      assertTrue(result);
//    }
//  }

}
