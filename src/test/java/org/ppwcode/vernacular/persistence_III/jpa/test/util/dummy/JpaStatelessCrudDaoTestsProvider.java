/*<license>
Copyright 2008 - $Date$ by PeopleWare n.v..

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

package org.ppwcode.vernacular.persistence_III.jpa.test.util.dummy;

import static org.ppwcode.metainfo_I.License.Type.APACHE_V2;
import static org.ppwcode.vernacular.persistence_III.jpa.test.util.dummy.DummyProviderE.getE;
import static org.ppwcode.vernacular.persistence_III.jpa.test.util.dummy.DummyProviderSubY.getSubY;
import static org.ppwcode.vernacular.persistence_III.jpa.test.util.dummy.DummyProviderQ.getQ;
import static org.ppwcode.vernacular.persistence_III.jpa.test.util.dummy.DummyProviderX.getX;
import static org.junit.Assert.*;

import org.ppwcode.metainfo_I.Copyright;
import org.ppwcode.metainfo_I.License;
import org.ppwcode.metainfo_I.vcs.SvnInfo;
import org.ppwcode.vernacular.exception_III.ApplicationException;
import org.ppwcode.vernacular.persistence_III.IdNotFoundException;
import org.ppwcode.vernacular.persistence_III.jpa.test.businesslogic.RequiredTransactionStatelessCrudDao;
import org.ppwcode.vernacular.persistence_III.jpa.test.semantics.E;
import org.ppwcode.vernacular.persistence_III.jpa.test.semantics.SubY;
import org.ppwcode.vernacular.persistence_III.jpa.test.semantics.Q;
import org.ppwcode.vernacular.persistence_III.jpa.test.semantics.Y;
import org.ppwcode.vernacular.persistence_III.jpa.test.semantics.X;
import org.ppwcode.vernacular.semantics_VI.exception.CompoundPropertyException;

import javax.persistence.EntityTransaction;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * For methods that require a transaction
 *
 * - If the transaction is not null, local tests it must be provided by the client-test
 * - if the transaction is null, remote test, no transaction needs to be provided
 *
 * This so the the test code could be shared !!
 *
 */
@Copyright("2008 - $Date$, PeopleWare n.v.")
@License(APACHE_V2)
@SvnInfo(revision = "$Revision$",
         date     = "$Date$")
public class JpaStatelessCrudDaoTestsProvider {

  public static void testRetrievePersistentBean(RequiredTransactionStatelessCrudDao jscd)
      throws IdNotFoundException, ParseException {

    try {
      jscd.retrievePersistentBean(X.class, 0);
    } catch (Exception exc) {
      assertTrue(exc instanceof IdNotFoundException);
    }

    X x = jscd.retrievePersistentBean(X.class, 1);
    validateX(x);

    E e = jscd.retrievePersistentBean(E.class, 1);
    validateE(e);

    SubY sy = jscd.retrievePersistentBean(SubY.class, 1);
    validateY(sy);
  }

  public static void testRetrieveAllPersistentBeans(RequiredTransactionStatelessCrudDao jscd)
      throws IdNotFoundException {
    SubY y = jscd.retrievePersistentBean(SubY.class, 1);
    // boolean indicates wheter the subclasses must be retrieved or not
    List<Y> ys = new ArrayList<Y>(jscd.retrieveAllPersistentBeans(Y.class, true));
    assertTrue(ys.size() > 0);
    assertTrue(ys.get(0) instanceof SubY);
    assertTrue(ys.contains(y));

    List<E> es = new ArrayList<E>(jscd.retrieveAllPersistentBeans(E.class, true));
    assertTrue(es.size() > 0);
    assertEquals("test", es.get(0).getName());
    List<X> retrievedXs = new ArrayList<X>(es.get(0).getXs());
    assertEquals(3, retrievedXs.size());
    X x = jscd.retrievePersistentBean(X.class, 1);
    assertTrue(retrievedXs.contains(x));

    List<X> xs = new ArrayList<X>(jscd.retrieveAllPersistentBeans(X.class, true));
    assertTrue(xs.size() > 0);
    E emp = jscd.retrievePersistentBean(E.class, 1);
    assertEquals(emp, xs.get(0).getE());
    assertNotNull(xs.get(0).getY());
    assertTrue(xs.get(0).getY() instanceof SubY);
  }

  public static void testCreatePersistentBean(RequiredTransactionStatelessCrudDao jscd, EntityTransaction tx)
      throws ApplicationException {

    Q q1 = getQ(Locale.ENGLISH, "name 1", "description 1");
    Q q2 = getQ(Locale.FRENCH, "name 2", "description 2");
    SubY suby = getSubY(true, q1, q2);
    startTransaction(tx);
    SubY generalTaskInDb = jscd.createPersistentBean(suby);
    stopTransaction(tx);

    E e = getE("newE");
    startTransaction(tx);
    jscd.createPersistentBean(e);
    stopTransaction(tx);

    X x = getX("a description of the x", e,
        false, new Date(), generalTaskInDb);
    startTransaction(tx);
    jscd.createPersistentBean(x);
    stopTransaction(tx);

    suby = jscd.retrievePersistentBean(SubY.class, suby
        .getPersistenceId());
    assertNotNull(suby);
    assertNotNull(suby.getPersistenceId());
    assertNotNull(suby.getPersistenceVersion());
    assertTrue(suby.getQs().contains(q1));
    assertTrue(suby.getQs().contains(q2));

    e = jscd.retrievePersistentBean(E.class, e
        .getPersistenceId());
    assertNotNull(e);
    assertNotNull(e.getPersistenceId());
    assertNotNull(e.getPersistenceVersion());
    assertTrue(e.getXs().contains(x));

    x = jscd.retrievePersistentBean(X.class, x.getPersistenceId());
    assertNotNull(x);
    assertNotNull(x.getPersistenceId());
    assertNotNull(x.getPersistenceVersion());
    assertTrue(x.getE().equals(e));
    assertTrue(x.getY().equals(suby));
  }

  public static void testUpdateTimeCardPersistentBean(RequiredTransactionStatelessCrudDao jscd, EntityTransaction tx)
      throws ApplicationException {
    String NEW_DESCRIPTION = "new description for test";

    E e = getE("new E");
    startTransaction(tx);
    jscd.createPersistentBean(e);
    stopTransaction(tx);

    Q q1 = getQ(Locale.ENGLISH, "name 1", "description 1");
    Q q2 = getQ(Locale.FRENCH, "name 2", "description 2");
    startTransaction(tx);
    SubY suby = getSubY(false, q1, q2);
    jscd.createPersistentBean(suby);

    X x = jscd.retrievePersistentBean(X.class, 1);
    x.setLocked(true);
    x.setDescription(NEW_DESCRIPTION);
    x.setE(e);
    x.setY(suby);
    jscd.updatePersistentBean(x);

    stopTransaction(tx);

    x = jscd.retrievePersistentBean(X.class, 1);
    assertNotNull(x);
    assertTrue(x.getLocked());
    assertEquals(NEW_DESCRIPTION, x.getDescription());
    assertEquals(e, x.getE());
    assertEquals(suby, x.getY());
    assertTrue(x.getY() instanceof SubY);
    List<Q> testLndList = new ArrayList<Q>(
        ((SubY) x.getY()).getQs());
    assertTrue(testLndList.contains(q1));
    assertTrue(testLndList.contains(q2));

  }

  public static void testUpdateEmployeePersistentBean(RequiredTransactionStatelessCrudDao jscd, EntityTransaction tx)
      throws ApplicationException {
    String NAME = "newName";

    startTransaction(tx);
    E e = jscd.retrievePersistentBean(E.class, 1);
    e.setName(NAME);
    jscd.updatePersistentBean(e);

    stopTransaction(tx);

    e = jscd.retrievePersistentBean(E.class, 1);
    assertNotNull(e);
    assertEquals(NAME, e.getName());
  }

  public static void testUpdateLocalizedNameDescriptionPersistentBean(
      RequiredTransactionStatelessCrudDao jscd, EntityTransaction tx) throws ApplicationException {
    String NEW_NAME = "new name for test";
    String NEW_DESCRIPTION = "new description for test";

    startTransaction(tx);
    SubY suby = jscd.retrievePersistentBean(SubY.class, 3);
    Q newQ = new ArrayList<Q>(suby.getQs()).get(0);
    newQ.setName(NEW_NAME);
    newQ.setDescription(NEW_DESCRIPTION);
    newQ.setLocale(Locale.ENGLISH);
    jscd.updatePersistentBean(suby);

    stopTransaction(tx);

    suby = jscd.retrievePersistentBean(SubY.class, 3);
    newQ = new ArrayList<Q>(suby.getQs()).get(0);
    assertNotNull(suby.getQs());
    assertEquals(NEW_NAME, newQ.getName());
    assertEquals(NEW_DESCRIPTION, newQ.getDescription());
    assertEquals(Locale.ENGLISH, newQ.getLocale());
  }

  public static void testUpdateGeneralTaskPersistentBean(RequiredTransactionStatelessCrudDao jscd, EntityTransaction tx)
      throws ApplicationException {
    startTransaction(tx);
    Q newQ = getQ(Locale.FRENCH, "new Name", "new description");
    SubY suby = jscd.retrievePersistentBean(SubY.class, 1);
    suby.setActive(true);
    suby.addQ(newQ);
    try {
      jscd.updatePersistentBean(suby);
    } catch (CompoundPropertyException e) {
      e.printStackTrace();
    }
    stopTransaction(tx);

    suby = jscd.retrievePersistentBean(SubY.class, 1);
    assertNotNull(suby);
    List<Q> qs = new ArrayList<Q>(suby.getQs());
    assertTrue(qs.contains(newQ));
    assertTrue(suby.isActive());
  }

  public static void testDeleteEmployeePersistentBean(RequiredTransactionStatelessCrudDao jscd, EntityTransaction tx) {

    E emp;
    try {
      startTransaction(tx);

      emp = jscd.retrievePersistentBean(E.class, 1);
      jscd.deletePersistentBean(emp);

      stopTransaction(tx);
    } catch (Exception e1) {
      // an employee cann't be deleted
      assertTrue(true);
    }
  }

  public static void testDeleteTimeCardPersistentBean(RequiredTransactionStatelessCrudDao jscd, EntityTransaction tx) {
    X x;
    try {
      startTransaction(tx);

      x = jscd.retrievePersistentBean(X.class, 1);
      jscd.deletePersistentBean(x);

      stopTransaction(tx);
    } catch (Exception e1) {
      fail("Should not come here!!");
    }

    try {
      jscd.retrievePersistentBean(X.class, 1);
      fail("The object wasn't deleted");
    } catch (Exception ex) {
      assertTrue(ex instanceof IdNotFoundException);
    }
  }

  public static void testDeleteTaskPersistentBean(RequiredTransactionStatelessCrudDao jscd, EntityTransaction tx) {
    SubY t;
    try {
      startTransaction(tx);

      t = jscd.retrievePersistentBean(SubY.class, 1);
      jscd.deletePersistentBean(t);

      stopTransaction(tx);
    } catch (Exception e1) {
      // a general task cann't be deleted
      assertTrue(true);
    }
  }

  /*
   * ----------- convenience methods --------------
   */
  private static void startTransaction(EntityTransaction tx) {
    if (tx != null) {
      tx.begin();
    }
  }

  private static void stopTransaction(EntityTransaction tx) {
    if (tx != null) {
      tx.commit();
    }
  }


  /*
   * ----------- validation methods --------------
   */
  private static void validateE(E e) throws ParseException {
    assertNotNull(e);
    assertEquals("test", e.getName());
    List<X> xs = new ArrayList<X>(e.getXs());
    assertEquals(xs.size(), 3);

    for (X x : xs) {
      if (x.getPersistenceId() == 1) {
        validateX(x);
      }
    }
  }

  private static void validateX(X x) throws ParseException {
    SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd");
    assertNotNull(x);
    assertEquals("test x", x.getDescription());
    assertEquals("test", x.getE().getName());
    assertEquals(sd.parse("2008-10-16"), x.getPeriod());
    assertFalse(x.getLocked());
    validateY(x.getY());
  }

  private static void validateY(Y y) {
    assertTrue(y instanceof SubY);
    assertFalse(((SubY) y).isActive());

    List<Q> qs = new ArrayList<Q>(((SubY) y).getQs());
    assertEquals(qs.size(), 3);

    assertTrue(qs.get(0).getDescription().equals("description 1")
        || qs.get(0).getDescription().equals("description 2")
        || qs.get(0).getDescription().equals("description 3"));
    assertFalse(qs.get(0).getDescription().equals("description 4")
        || qs.get(0).getDescription().equals("description 5"));
  }

}
