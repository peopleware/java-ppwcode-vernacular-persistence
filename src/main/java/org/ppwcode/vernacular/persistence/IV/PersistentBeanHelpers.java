/*<license>
Copyright 2004 - 2016 by PeopleWare n.v.

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


import java.beans.PropertyDescriptor;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import static org.apache.commons.beanutils.PropertyUtils.getPropertyDescriptors;
import static org.ppwcode.vernacular.exception.IV.util.ProgrammingErrorHelpers.preArgumentNotNull;
import static org.ppwcode.vernacular.semantics.VII.util.PropertyHelpers.propertyValue;


/**
 * <p>Supporting methods for working with {@link PersistentBean PersistentBeans}.</p>
 *
 * @author    Jan Dockx
 * @author    PeopleWare n.v.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public final class PersistentBeanHelpers {

  private PersistentBeanHelpers() {
    // NOP
  }

  /**
   * The upstream {@link PersistentBean PersistentBeans} starting from {@code pb}.
   * These are the beans that are simple properties of {@code pb}. Upstream means
   * in most cases (this is all that is implemented at this time) the beans
   * reachable via a to-one association.
   */
  /*
    @MethodContract(
      pre  = @Expression("_pb != null"),
      post = {
        @Expression("result != null"),
        @Expression("for (PropertyDescriptor pd : getPropertyDescriptors(_pb)) {" +
                      "PersistentBean.class.isAssignableFrom(pd.propertyType) && propertyValue(_pb, pd.name) != null ? " +
                        "result.contains(propertyValue(_pb, pd.name))" +
                    "}"),
        @Expression("for (PersistentBean pbr : result) {" +
                      "exists (PropertyDescriptor pd : getPropertyDescriptors(_pb)) {" +
                        "pbr == propertyValue(_pb, pd.name)" +
                      "}" +
                    "}")
      }
    )
  */
  public static Set<PersistentBean<?>> directUpstreamPersistentBeans(PersistentBean<?> pb) {
    assert preArgumentNotNull(pb, "pb");
    Set<PersistentBean<?>> result = new HashSet<>();
    PropertyDescriptor[] pds = getPropertyDescriptors(pb);
    for (PropertyDescriptor pd : pds) {
      if (PersistentBean.class.isAssignableFrom(pd.getPropertyType())) {
        PersistentBean<?> upstreamCandidate = propertyValue(pb, pd.getName());
        if (upstreamCandidate != null) {
          result.add(upstreamCandidate);
        }
      }
    }
    return result;
  }

  /**
   * All upstream {@link PersistentBean PersistentBeans} starting from {@code pb}.
   * These are the beans that are simple properties of {@code pb}. Upstream means
   * in most cases (this is all that is implemented at this time) the beans
   * reachable via a to-one association. This is applied recursively.
   * {@code pb} itself is also part of the set.
   */
  /*
    @MethodContract(
      pre  = @Expression("_pb != null"),
      post = {
        @Expression("result != null"),
        @Expression("{_pb} U directUpstreamPersistentBeans(_pb) U " +
                     "union (PersistentBean pbr : directUpstreamPersistentBeans(_pb)) {upstreamPersistentBeans(pbr)}")
      }
    )
  */
  public static Set<PersistentBean<?>> upstreamPersistentBeans(PersistentBean<?> pb) {
    assert preArgumentNotNull(pb, "pb");
    LinkedList<PersistentBean<?>> agenda = new LinkedList<>();
    agenda.add(pb);
    int i = 0;
    while (i < agenda.size()) {
      PersistentBean<?> current = agenda.get(i);
      directUpstreamPersistentBeans(current).stream().filter(pbr -> !agenda.contains(pbr)).forEach(agenda::add);
      i++;
    }
    return new HashSet<>(agenda);
  }

}
