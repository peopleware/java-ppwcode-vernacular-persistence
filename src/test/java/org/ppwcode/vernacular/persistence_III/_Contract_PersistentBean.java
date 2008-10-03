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


import static org.junit.Assert.assertEquals;

import java.io.Serializable;

import org.ppwcode.vernacular.semantics_VI.bean.RousseauBeanContract;
import org.ppwcode.vernacular.semantics_VI.exception.CompoundPropertyException;


public class _Contract_PersistentBean {

  // interface, not actual tests, only contract

  public static void assertInvariants(PersistentBean<?> subject) {
    RousseauBeanContract.assertInvariants(subject);
    // no local invariants
  }

  public static void contractEquals(PersistentBean<?> subject, Object other, boolean result) {
    // validate
    RousseauBeanContract.contractEquals(subject, other, result);
  }

  public static void contractHashCode(PersistentBean<?> subject, int result) {
    RousseauBeanContract.contractHashCode(subject, result);
  }

  public static void contractToString(PersistentBean<?> subject, String result) {
    RousseauBeanContract.contractToString(subject, result);
  }

  public static void contractWildExceptions(PersistentBean<?> subject, CompoundPropertyException result) {
    RousseauBeanContract.contractGetWildExceptions(subject, result);
  }

  public static void contractCivilized(PersistentBean<?> subject, boolean result) {
    RousseauBeanContract.contractIsCivilized(subject, result);
  }

  public static void contractPostCheckCivility(boolean OLDCivilized, PersistentBean<?> subject) {
    RousseauBeanContract.contractPostCheckCivility(OLDCivilized, subject);
  }

  public static void contractExcCheckCivility(boolean OLDCivilized, PersistentBean<?> subject, CompoundPropertyException thrown) {
    RousseauBeanContract.contractExcCheckCivility(OLDCivilized, subject, thrown);
  }

  public static void contractNormalize(PersistentBean<?> subject, boolean result) {
    RousseauBeanContract.contractNormalize(subject, result);
  }

  public static <_Id_ extends Serializable> void contractSetId(PersistentBean<_Id_> subject, _Id_ newId) {
    assertEquals(newId, subject.getPersistenceId());
  }

  public static <_Id_ extends Serializable> void contractHasSameId(PersistentBean<_Id_> subject, PersistentBean<_Id_> other, boolean result) {
    assertEquals((other != null) && eqn(subject.getPersistenceId(), other.getPersistenceId()), result);
  }

  private final static boolean eqn(Object one, Object other) {
    return one == null ? other == null : one.equals(other);
  }

}

