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

import static org.junit.Assert.fail;
import static org.ppwcode.metainfo_I.License.Type.APACHE_V2;

import java.util.Date;

import org.ppwcode.metainfo_I.Copyright;
import org.ppwcode.metainfo_I.License;
import org.ppwcode.metainfo_I.vcs.SvnInfo;
import org.ppwcode.vernacular.persistence_III.jpa.test.semantics.E;
import org.ppwcode.vernacular.persistence_III.jpa.test.semantics.Y;
import org.ppwcode.vernacular.persistence_III.jpa.test.semantics.X;
import org.ppwcode.vernacular.semantics_VI.exception.PropertyException;


@Copyright("2008 - $Date: $, PeopleWare n.v.")
@License(APACHE_V2)
@SvnInfo(revision = "$Revision: $",
         date     = "$Date: $")
public class DummyProviderX {

  public static X getX(String desc, E e, boolean locked, Date period, Y y) {
    try {

      X x = new X();
      x.setDescription(desc);
      x.setE(e);
      x.setLocked(locked);
      x.setPeriod(period);
      x.setY(y);

      return x;
    } catch (PropertyException exc) {
      fail("Could not create X");
    }
    return null;
  }
}
