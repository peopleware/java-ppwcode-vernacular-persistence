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

import org.ppwcode.vernacular.semantics_VI.exception.CompoundPropertyException;


public class _Contract_VersionedPersistentBean {


  // interface, not actual tests, only contract

  public static void assertInvariants(VersionedPersistentBean<?, ?> subject) {
    _Contract_PersistentBean.assertInvariants(subject);
    // no local invariants
  }

  public static void contractEquals(VersionedPersistentBean<?, ?> subject, Object other, boolean result) {
    // validate
    _Contract_PersistentBean.contractEquals(subject, other, result);
  }

  public static void contractHashCode(VersionedPersistentBean<?, ?> subject, int result) {
    _Contract_PersistentBean.contractHashCode(subject, result);
  }

  public static void contractToString(VersionedPersistentBean<?, ?> subject, String result) {
    _Contract_PersistentBean.contractToString(subject, result);
  }

  public static void contractWildExceptions(VersionedPersistentBean<?, ?> subject, CompoundPropertyException result) {
    _Contract_PersistentBean.contractWildExceptions(subject, result);
  }

  public static void contractCivilized(VersionedPersistentBean<?, ?> subject, boolean result) {
    _Contract_PersistentBean.contractCivilized(subject, result);
  }

  public static void contractPostCheckCivility(boolean OLDCivilized, VersionedPersistentBean<?, ?> subject) {
    _Contract_PersistentBean.contractPostCheckCivility(OLDCivilized, subject);
  }

  public static void contractExcCheckCivility(boolean OLDCivilized, VersionedPersistentBean<?, ?> subject, CompoundPropertyException thrown) {
    _Contract_PersistentBean.contractExcCheckCivility(OLDCivilized, subject, thrown);
  }

  public static void contractNormalize(VersionedPersistentBean<?, ?> subject, boolean result) {
    _Contract_PersistentBean.contractNormalize(subject, result);
  }

  public static <_Id_ extends Serializable> void contractSetId(VersionedPersistentBean<_Id_, ?> subject, _Id_ newId) {
    _Contract_PersistentBean.contractSetId(subject, newId);
  }

  public static <_Id_ extends Serializable> void contractHasSameId(VersionedPersistentBean<_Id_, ?> subject, VersionedPersistentBean<_Id_, ?> other, boolean result) {
    _Contract_PersistentBean.contractHasSameId(subject, other, result);
  }

  public static <_Version_ extends Serializable> void contractSetPersistenceVersion(VersionedPersistentBean<?, _Version_> subject, _Version_ newVersion) {
    assertEquals(newVersion, subject.getPersistenceVersion());
  }

}


