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

package org.ppwcode.vernacular.persistence_III;


import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ppwcode.vernacular.semantics_VI.bean.AbstractRousseauBeanTest;
import org.ppwcode.vernacular.semantics_VI.exception.CompoundPropertyException;
import org.ppwcode.vernacular.semantics_VI.exception.PropertyException;


public class AbstractPersistentBeanTest {

  public static class AbstractPersistentBeanSTUB<_Id_ extends Serializable>
    extends AbstractPersistentBean<_Id_, Long> {

    public AbstractPersistentBeanSTUB(_Id_ id, String property1,
            Date property2, Set<String> property3, int[] property4) {
      super();
      setId(id);
      $property1 = property1;
      $property2 = property2;
      $property3 = property3;
      $property4 = property4;
    }

    public final String getProperty1() {
      return $property1;
    }

    public final void setProperty1(String property1) {
      $property1 = property1;
    }

    private String $property1;


    public final Date getProperty2() {
      return $property2;
    }

    public final void setProperty2(Date property2) {
      $property2 = property2;
    }

    private Date $property2;

    public final Set<String> getProperty3() {
      return $property3;
    }

    public final void setProperty3(Set<String> property3) {
      $property3 = property3;
    }

    private Set<String> $property3;

    public final int[] getProperty4() {
      return $property4;
    }

    public final void setProperty4(int[] property4) {
      $property4 = property4;
    }

    private int[] $property4;

  }

  public static class AbstractPersistentBeanWILD<_Id_ extends Serializable>
    extends AbstractPersistentBeanSTUB<_Id_> {

    public AbstractPersistentBeanWILD( _Id_ id, String property1,
        Date property2, Set<String> property3, int[] property4) {
      super(id, property1, property2, property3, property4);
    }

    @Override
    public CompoundPropertyException getWildExceptions() {
      CompoundPropertyException cpe = super.getWildExceptions();
      cpe.addElementException(new PropertyException(this, "property1", null, null));
      cpe.addElementException(new PropertyException(this, "property1", null, null));
      cpe.addElementException(new PropertyException(this, "property2", null, null));
      cpe.addElementException(new PropertyException(this, "property2", null, null));
      cpe.addElementException(new PropertyException(this, "property3", null, null));
      cpe.addElementException(new PropertyException(this, "property3", null, null));
      return cpe;
    }

  }

  public static class AbstractPersistentBeanNOPROPERTIES<_Id_ extends Serializable>
    extends AbstractPersistentBean<_Id_, Long> {
    // NOP
  }


  private List<AbstractPersistentBean<?, ?>> subjects;

  @Before
  public void setUp() throws Exception {
    subjects = new ArrayList<AbstractPersistentBean<?, ?>>();
    AbstractPersistentBeanSTUB<?> subject =
      new AbstractPersistentBeanSTUB<Integer>(null, null, null, null, null);
    subjects.add(subject);
    Integer id = 9;
    Set<String> stringSet = new HashSet<String>();
    stringSet.add("string 1");
    stringSet.add("string 2");
    stringSet.add(null);
    int[] intArray = {5, 6, 4, 8};
    subject = new AbstractPersistentBeanSTUB<Integer>(id, null, null, null, null);
    subjects.add(subject);
    subject = new AbstractPersistentBeanSTUB<Integer>(null,"PROPERTY 1", null, null, null);
    subjects.add(subject);
    subject = new AbstractPersistentBeanSTUB<Integer>(null, null, new Date(), null, null);
    subjects.add(subject);
    subject = new AbstractPersistentBeanSTUB<Integer>(null, null, null, stringSet, null);
    subjects.add(subject);
    subject = new AbstractPersistentBeanSTUB<Integer>(null, null, null, null, intArray);
    subjects.add(subject);
    subject = new AbstractPersistentBeanSTUB<Integer>(id, "PROPERTY 1", new Date(), stringSet, intArray);
    subjects.add(subject);
    subject = new AbstractPersistentBeanWILD<Integer>(id, "PROPERTY 1", new Date(), stringSet, intArray);
    subjects.add(subject);
  }

  @After
  public void tearDown() throws Exception {
    subjects = null;
  }

  public static void assertInvariants(PersistentBean<?, ?> subject) {
    // no own invariants
    PersistentBeanContract.assertInvariants(subject);
  }

  public static void testEquals(AbstractPersistentBean<?, ?> subject, Object other) {
    // execute
    boolean result = subject.equals(other);
    // validate
    PersistentBeanContract.contractEquals(subject, other, result);
    assertInvariants(subject);
  }

  @Test
  public void testEqualsObject() {
    for (AbstractPersistentBean<?, ?> subject : subjects) {
      testEquals(subject, null);
      testEquals(subject, subject);
      testEquals(subject, new Object());
      testEquals(subject, new AbstractPersistentBeanSTUB<Integer>(null, "hfhfh", null, null, null));
    }
  }

  public static void testHashCode(AbstractPersistentBean<?, ?> subject) {
    // execute
    int result = subject.hashCode();
    // validate
    PersistentBeanContract.contractHashCode(subject, result);
    assertInvariants(subject);
  }

  @Test
  public void testHashCode() {
    for (AbstractPersistentBean<?, ?> subject : subjects) {
      testHashCode(subject);
    }
  }

  public static void testToString(AbstractPersistentBean<?, ?> subject) {
    // execute
    String result = subject.toString();
    // validate
    PersistentBeanContract.contractToString(subject, result);
    assertInvariants(subject);
  }

  @Test
  public void testToString() {
    for (AbstractPersistentBean<?, ?> subject : subjects) {
      testToString(subject);
    }
  }

  @Test
  public void testClone() {
    // The method clone cannot be tested, because the method is declared
    // protected in AbstractSemanticBean. This is exactly the intended behaviour.
  }

  public static Set<String> testPropertyNamesForToStringA(AbstractPersistentBean<?, ?> subject, int nrOfProperties) {
    Set<String> result = AbstractRousseauBeanTest.testPropertyNamesForToStringA(subject, nrOfProperties);
    assertTrue(result.contains("wildExceptions"));
    assertTrue(result.contains("civilized"));
    assertInvariants(subject);
    return result;
  }

  public static Set<String> testPropertyNamesForToStringB(AbstractPersistentBean<?, ?> subject, int nrOfProperties) {
    Set<String> result = AbstractRousseauBeanTest.testPropertyNamesForToStringB(subject, nrOfProperties);
    assertTrue(result.contains("wildExceptions"));
    assertTrue(result.contains("civilized"));
    assertInvariants(subject);
    return result;
  }

  @Test
  public void testPropertyNamesForToString2() {
    AbstractPersistentBean<?, ?> subject = new AbstractPersistentBeanNOPROPERTIES<Integer>();
    testPropertyNamesForToStringA(subject, 4);
  }

  @Test
  public void testPropertyNamesForToString1() {
    for (AbstractPersistentBean<?, ?> subject : subjects) {
      testPropertyNamesForToStringB(subject, 6);
    }
  }

  public static void testCollectionString(AbstractPersistentBean<?, ?> subject) {
    AbstractRousseauBeanTest.testCollectionString(subject);
    assertInvariants(subject);
  }

  @Test
  public void testCollectionString() {
    for (AbstractPersistentBean<?, ?> subject : subjects) {
      testCollectionString(subject);
    }
  }

  public static CompoundPropertyException testGetWildExceptions(AbstractPersistentBean<?, ?> subject) {
    // execute
    CompoundPropertyException result = subject.getWildExceptions();
    // validate
    PersistentBeanContract.contractGetWildExceptions(subject, result);
    assertInvariants(subject);
    return result;
  }

  private static void contractPROTECTEDGetWildExceptions(AbstractPersistentBean<?, ?> subject, CompoundPropertyException result) {
    assertNull(result.getPropertyName());
    assertTrue(result.isEmpty());
  }

  @Test
  public void testGetWildExceptions() {
    for (AbstractPersistentBean<?, ?> subject : subjects) {
      CompoundPropertyException result = testGetWildExceptions(subject);
      if (subject.getClass() == AbstractPersistentBeanSTUB.class) {
        contractPROTECTEDGetWildExceptions(subject, result);
      }
    }
  }

  public static void testIsCivilized(AbstractPersistentBean<?, ?> subject) {
    // execute
    boolean result = subject.isCivilized();
    // validate
    PersistentBeanContract.contractIsCivilized(subject, result);
    assertInvariants(subject);
  }

  @Test
  public void testIsCivilized() {
    for (AbstractPersistentBean<?, ?> subject : subjects) {
      testIsCivilized(subject);
    }
  }

  public static void testCheckCivility(AbstractPersistentBean<?, ?> subject) {
    boolean OLDcivilized = subject.isCivilized();
    try {
      subject.checkCivility();
      PersistentBeanContract.contractPostCheckCivility(OLDcivilized, subject);
    }
    catch (CompoundPropertyException thrown) {
      PersistentBeanContract.contractExcCheckCivility(OLDcivilized, subject, thrown);
    }
    assertInvariants(subject);
  }

  @Test
  public void testCheckCivility() {
    for (AbstractPersistentBean<?, ?> subject : subjects) {
      testCheckCivility(subject);
    }
  }

  public static void testNormalize(AbstractPersistentBean<?, ?> subject) {
    // execute
    subject.normalize();
    // validate
    assertInvariants(subject);
  }

  @Test
  public void testNormalize() {
    for (AbstractPersistentBean<?, ?> subject : subjects) {
      testNormalize(subject);
    }
  }

}

