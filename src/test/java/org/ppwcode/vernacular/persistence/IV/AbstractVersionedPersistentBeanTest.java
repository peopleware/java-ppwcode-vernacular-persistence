/*<license>
Copyright 2005 - 2016 by PeopleWare n.v..

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

package org.ppwcode.vernacular.persistence.IV;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ppwcode.vernacular.semantics.VII.bean.stubs.NumberOfProperties;
import org.ppwcode.vernacular.semantics.VII.exception.CompoundPropertyException;
import org.ppwcode.vernacular.semantics.VII.exception.PropertyException;

import java.io.Serializable;
import java.util.*;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


@SuppressWarnings({"WeakerAccess", "UnusedParameters"})
public class AbstractVersionedPersistentBeanTest {

  @SuppressWarnings({"WeakerAccess", "unused"})
  public static class AbstractVersionedPersistentBeanSTUB<_Id_ extends Serializable>
    extends AbstractVersionedPersistentBean<_Id_, Integer> implements NumberOfProperties {

    private _Id_ $persistenceId;

    public AbstractVersionedPersistentBeanSTUB(_Id_ id, String property1,
                                               Date property2, Set<String> property3, int[] property4) {
      super();
      $persistenceId = id;
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

    public int nrOfProperties() {
      return 6; // 4 + persistenceId + persistenceVersion
    }

    public int nrOfSimpleProperties() {
      return nrOfProperties() - 2;
    }

    @Override
    public _Id_ getPersistenceId() {
      return $persistenceId;
    }
  }

  @SuppressWarnings("Duplicates")
  public static class AbstractVersionedPersistentBeanWILD<_Id_ extends Serializable>
    extends AbstractVersionedPersistentBeanSTUB<_Id_> {

    public AbstractVersionedPersistentBeanWILD(_Id_ id, String property1,
        Date property2, Set<String> property3, int[] property4) {
      super(id, property1, property2, property3, property4);
    }

    @Override
    public CompoundPropertyException wildExceptions() {
      CompoundPropertyException cpe = super.wildExceptions();
      cpe.addElementException(new PropertyException(this, "property1", null, null));
      cpe.addElementException(new PropertyException(this, "property1", null, null));
      cpe.addElementException(new PropertyException(this, "property2", null, null));
      cpe.addElementException(new PropertyException(this, "property2", null, null));
      cpe.addElementException(new PropertyException(this, "property3", null, null));
      cpe.addElementException(new PropertyException(this, "property3", null, null));
      return cpe;
    }

  }

  @SuppressWarnings("unused")
  public static class AbstractVersionedPersistentBeanNOPROPERTIES<_Id_ extends Serializable>
    extends AbstractVersionedPersistentBean<_Id_, Integer> implements NumberOfProperties {

    private _Id_ $persistenceId;

    public int nrOfProperties() {
        return 2; // 0 + persistenceId + persistenceVersion
      }

      public int nrOfSimpleProperties() {
        return nrOfProperties();
      }

    @Override
    public _Id_ getPersistenceId() {
      return $persistenceId;
    }
  }


  private List<AbstractPersistentBean<?>> subjects;

  @Before
  public void setUp() throws Exception {
    subjects = new ArrayList<>();
    AbstractVersionedPersistentBeanSTUB<?> subject =
      new AbstractVersionedPersistentBeanSTUB<Integer>(null, null, null, null, null);
    subjects.add(subject);
    Integer id = 9;
    Set<String> stringSet = new HashSet<>();
    stringSet.add("string 1");
    stringSet.add("string 2");
    stringSet.add(null);
    int[] intArray = {5, 6, 4, 8};
    subject = new AbstractVersionedPersistentBeanSTUB<>(id, null, null, null, null);
    subjects.add(subject);
    subject = new AbstractVersionedPersistentBeanSTUB<Integer>(null,"PROPERTY 1", null, null, null);
    subjects.add(subject);
    subject = new AbstractVersionedPersistentBeanSTUB<Integer>(null, null, new Date(), null, null);
    subjects.add(subject);
    subject = new AbstractVersionedPersistentBeanSTUB<Integer>(null, null, null, stringSet, null);
    subjects.add(subject);
    subject = new AbstractVersionedPersistentBeanSTUB<Integer>(null, null, null, null, intArray);
    subjects.add(subject);
    subject = new AbstractVersionedPersistentBeanSTUB<>(id, "PROPERTY 1", new Date(), stringSet, intArray);
    subjects.add(subject);
    subject = new AbstractVersionedPersistentBeanWILD<>(id, "PROPERTY 1", new Date(), stringSet, intArray);
    subjects.add(subject);
  }

  @After
  public void tearDown() throws Exception {
    subjects = null;
  }

  public static void assertInvariants(PersistentBean<?> subject) {
    // no own invariants
    _Contract_PersistentBean.assertInvariants(subject);
  }

  public static void testEquals(AbstractPersistentBean<?> subject, Object other) {
    // execute
    boolean result = subject.equals(other);
    // validate
    _Contract_PersistentBean.contractEquals(subject, other, result);
    assertInvariants(subject);
  }

  @Test
  public void testEqualsObject() {
    for (AbstractPersistentBean<?> subject : subjects) {
      testEquals(subject, null);
      testEquals(subject, subject);
      testEquals(subject, new Object());
      testEquals(subject, new AbstractVersionedPersistentBeanSTUB<Integer>(null, "a dummy string", null, null, null));
    }
  }

  public static void testHashCode(AbstractPersistentBean<?> subject) {
    // execute
    int result = subject.hashCode();
    // validate
    _Contract_PersistentBean.contractHashCode(subject, result);
    assertInvariants(subject);
  }

  @Test
  public void testHashCode() {
    subjects.forEach(AbstractVersionedPersistentBeanTest::testHashCode);
  }

  public static void testToString(AbstractPersistentBean<?> subject) {
    // execute
    String result = subject.toString();
    // validate
    _Contract_PersistentBean.contractToString(subject, result);
    assertInvariants(subject);
  }

  @Test
  public void testToString() {
    subjects.forEach(AbstractVersionedPersistentBeanTest::testToString);
  }

  @Test
  public void testClone() {
    // The method clone cannot be tested, because the method is declared
    // protected in AbstractSemanticBean. This is exactly the intended behaviour.
  }

  public static CompoundPropertyException testGetWildExceptions(AbstractPersistentBean<?> subject) {
    // execute
    CompoundPropertyException result = subject.wildExceptions();
    // validate
    _Contract_PersistentBean.contractWildExceptions(subject, result);
    assertInvariants(subject);
    return result;
  }

  private static void contractPROTECTEDGetWildExceptions(AbstractPersistentBean<?> subject, CompoundPropertyException result) {
    assertNull(result.getPropertyName());
    assertTrue(result.isEmpty());
  }

  @Test
  public void testGetWildExceptions() {
    for (AbstractPersistentBean<?> subject : subjects) {
      CompoundPropertyException result = testGetWildExceptions(subject);
      if (subject.getClass() == AbstractVersionedPersistentBeanSTUB.class) {
        contractPROTECTEDGetWildExceptions(subject, result);
      }
    }
  }

  public static void testIsCivilized(AbstractPersistentBean<?> subject) {
    // execute
    boolean result = subject.civilized();
    // validate
    _Contract_PersistentBean.contractCivilized(subject, result);
    assertInvariants(subject);
  }

  @Test
  public void testIsCivilized() {
    subjects.forEach(AbstractVersionedPersistentBeanTest::testIsCivilized);
  }

  public static void testCheckCivility(AbstractPersistentBean<?> subject) {
    boolean OLDCivilized = subject.civilized();
    try {
      subject.checkCivility();
      _Contract_PersistentBean.contractPostCheckCivility(OLDCivilized, subject);
    }
    catch (CompoundPropertyException thrown) {
      _Contract_PersistentBean.contractExcCheckCivility(OLDCivilized, subject, thrown);
    }
    assertInvariants(subject);
  }

  @Test
  public void testCheckCivility() {
    subjects.forEach(AbstractVersionedPersistentBeanTest::testCheckCivility);
  }

  public static void testNormalize(AbstractPersistentBean<?> subject) {
    // execute
    subject.normalize();
    // validate
    assertInvariants(subject);
  }

  @Test
  public void testNormalize() {
    subjects.forEach(AbstractVersionedPersistentBeanTest::testNormalize);
  }

}

