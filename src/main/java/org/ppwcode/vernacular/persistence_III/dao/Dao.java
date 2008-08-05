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

package org.ppwcode.vernacular.persistence_III.dao;

import static org.ppwcode.metainfo_I.License.Type.APACHE_V2;

import org.ppwcode.metainfo_I.Copyright;
import org.ppwcode.metainfo_I.License;
import org.ppwcode.metainfo_I.vcs.SvnInfo;
import org.ppwcode.vernacular.persistence_III.PersistentBean;

/**
 * Data Access Object. This interface is mainly used for documentation
 * purposes, to flag a type as a <acronym title="Data Access Object">DAO</acronym>.
 * In projects, interfaces should be defined that extend this interface,
 * with methods that have a technology independent contract that describes
 * interaction with persistent storage. The actual implementation of these
 * DAO methods will depend on the persistence technology used: different
 * classes that implement the DAO interface will produce the desired result
 * in different technologies. Those classes can extend a technology
 * specific superclass that offers support for that technology (e.g.,
 * JDBC, Hibernate, JDO, EJB, RMI, JPA, &hellip;).
 *
 * DAO instances are almost always stateful, because of the underlying
 * persistence technology.
 *
 * Implementations should be JavaBeans, with a default constructor. Further dependencies should be filled
 * out using setters, and DAO methods should be allowed to consider it a programming error if the dependencies
 * are not fulfilled when the DAO method is called.
 *
 * Subtypes may depend on the fact that the objects in persistent storage are
 * {@link PersistentBean PersistentBeans}, although this will not always be necessary.
 *
 * A Dao cannot be made {@link java.io.Serializable} (we tried). Hibernate dao's
 * probably keep a reference to a Hibernate Session, and, although Hibernate
 * Sessions are {@link java.io.Serializable}, they cannot be serialized while they are connected.
 * So, we state as part of the contract that Dao's are <strong>not
 * {@link java.io.Serializable}</strong>. Note that this poses a particular problem when
 * Dao's are used in web applications, where all objects in sessions scope must
 * be {@link java.io.Serializable}.
 *
 * @author Jan Dockx
 * @author PeopleWare n.v.
 */
@Copyright("2004 - $Date$, PeopleWare n.v.")
@License(APACHE_V2)
@SvnInfo(revision = "$Revision$",
         date     = "$Date$")
public interface Dao {

  // NOP

}
