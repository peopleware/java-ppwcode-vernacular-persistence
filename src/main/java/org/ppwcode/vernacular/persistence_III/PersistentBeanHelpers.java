/*<license>
Copyright 2004 - $Date$ by PeopleWare n.v.

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


import static org.apache.commons.beanutils.PropertyUtils.getPropertyDescriptors;
import static org.ppwcode.metainfo_I.License.Type.APACHE_V2;
import static org.ppwcode.util.reflect_I.PropertyHelpers.propertyValue;
import static org.ppwcode.vernacular.exception_III.ProgrammingErrorHelpers.preArgumentNotNull;

import java.beans.PropertyDescriptor;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.ppwcode.metainfo_I.Copyright;
import org.ppwcode.metainfo_I.License;
import org.ppwcode.metainfo_I.vcs.SvnInfo;
import org.toryt.annotations_I.Expression;
import org.toryt.annotations_I.MethodContract;


/**
 * <p>Supporting methods for working with {@link PersistentBean PersistentBeans}.</p>
 *
 * @author    Jan Dockx
 * @author    PeopleWare n.v.
 */
@Copyright("2004 - $Date$, PeopleWare n.v.")
@License(APACHE_V2)
@SvnInfo(revision = "$Revision$",
         date     = "$Date$")
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
  public static Set<PersistentBean<?>> directUpstreamPersistentBeans(PersistentBean<?> pb) {
    assert preArgumentNotNull(pb, "pb");
    Set<PersistentBean<?>> result = new HashSet<PersistentBean<?>>();
    PropertyDescriptor[] pds = getPropertyDescriptors(pb);
    for (int i = 0; i < pds.length; i++) {
      PropertyDescriptor pd = pds[i];
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
  @MethodContract(
    pre  = @Expression("_pb != null"),
    post = {
      @Expression("result != null"),
      @Expression("{_pb} U directUpstreamPersistentBeans(_pb) U " +
                   "union (PersistentBean pbr : directUpstreamPersistentBeans(_pb)) {upstreamPersistentBeans(pbr)}")
    }
  )
  public static Set<PersistentBean<?>> upstreamPersistentBeans(PersistentBean<?> pb) {
    assert preArgumentNotNull(pb, "pb");
    LinkedList<PersistentBean<?>> agenda = new LinkedList<PersistentBean<?>>();
    agenda.add(pb);
    int i = 0;
    while (i < agenda.size()) {
      PersistentBean<?> current = agenda.get(i);
      for (PersistentBean<?> pbr : directUpstreamPersistentBeans(current)) {
        if (! agenda.contains(pbr)) {
          agenda.add(pbr);
        }
      }
      i++;
    }
    return new HashSet<PersistentBean<?>>(agenda);
  }

}
