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

package org.ppwcode.vernacular.persistence_III.jpa.test.semantics;

import org.ppwcode.metainfo_I.Copyright;
import org.ppwcode.metainfo_I.License;

import static org.ppwcode.metainfo_I.License.Type.APACHE_V2;
import org.ppwcode.metainfo_I.vcs.SvnInfo;
import org.ppwcode.vernacular.semantics_VI.exception.PropertyException;
import org.toryt.annotations_I.Basic;
import org.toryt.annotations_I.Expression;
import org.toryt.annotations_I.MethodContract;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Locale;

/**
 * The {@link Q} contains a name and description in a
 * specific language.
 */
@Entity
@Table(name="q")
@Copyright("2008 - $Date$, PeopleWare n.v.")
@License(APACHE_V2)
@SvnInfo(revision = "$Revision$",
         date     = "$Date$")
public class Q {

  public static final String EMPTY = "";

  /*<property name="locale">
  -------------------------------------------------------------------------*/

  /**
   * Can only be {@code null} on initialization.
   *
   * @return Locale the locale (language) in which the description is written
   *
   */
  @Basic(
    init = @Expression("locale == null")
  )
  public final Locale getLocale() {
    return $locale;
  }

  @MethodContract(
    post = @Expression("$locale == locale")
  )
  public final void setLocale(Locale locale) {
    $locale = locale;
  }

  @Column(
    name = "locale"
  )
  private Locale $locale;

  /*</property>*/



  /*<property name="name">
  -------------------------------------------------------------------------*/

  /**
   * Can only be {@code null} on initialization.
   *
   * @return String The name representing the short description
   */
  @Basic(
    invars = @Expression("name != EMPTY"),
    init = @Expression("name == null")
  )
  public final String getName() {
    return $name;
  }

  @MethodContract(
    post = @Expression("$name == name")
  )
  public final void setName(String name) throws PropertyException {
    if (EMPTY.equals(name)) {
      throw new PropertyException(this, "name", "EMPTY_NOT_ALLOWED", null);
    }
    $name = name;
  }

  @Column(
    name = "name"
  )
  private String $name;

  /*</property>*/



  /*<property name="description">
  -------------------------------------------------------------------------*/

  /**
   * Can only be {@code null} on initialization.
   *
   * @return String The description
   */
  @Basic(
    init = @Expression("description == null")
  )
  public final String getDescription() {
    return $description;
  }

  /**
   * A description is not obligatory. An empty {@link String} or a
   * <code>null</code> value will always be converted to null.
   *
   * @param description
   *            the description of the task
   */
  @MethodContract(
    post = {
      @Expression("$description == description"),
      @Expression("description.trim() == '' ? $description = null")
    }
  )
  public final void setDescription(String description) {
    if (description != null && EMPTY.equals(description.trim())) {
      description = null;
    }
    $description = description;
  }

  @Column(
    name = "description"
  )
  private String $description;

  /*</property>*/

}
