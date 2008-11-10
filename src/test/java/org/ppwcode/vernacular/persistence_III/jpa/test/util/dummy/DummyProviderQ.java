/*<license>
Copyright 2008 - $Date: $ by PeopleWare n.v..

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

import java.util.Locale;

import org.ppwcode.metainfo_I.Copyright;
import org.ppwcode.metainfo_I.License;
import org.ppwcode.metainfo_I.vcs.SvnInfo;
import org.ppwcode.vernacular.persistence_III.jpa.test.semantics.Q;
import org.ppwcode.vernacular.semantics_VI.exception.PropertyException;
import static org.junit.Assert.fail;
import static org.ppwcode.metainfo_I.License.Type.APACHE_V2;


@Copyright("2008 - $Date: $, PeopleWare n.v.")
@License(APACHE_V2)
@SvnInfo(revision = "$Revision: $",
         date     = "$Date: $")
public class DummyProviderQ {

  /**
   * Creates a LocalizedNameDescription on bases of the parameters
   *
   * @param loc the locale
   * @param name the name of the description
   * @param desc the description itself
   * @return LocalizedNameDescription the name/description pair created
   */
  public static Q getQ(Locale loc, String name, String desc) {
    Q q = null;
    try {
      q = new Q();
      q.setLocale(loc);
      q.setName(name);
      q.setDescription(desc);
    } catch (PropertyException e) {
      fail("Should not come here");
    }
    return q;
  }

}
