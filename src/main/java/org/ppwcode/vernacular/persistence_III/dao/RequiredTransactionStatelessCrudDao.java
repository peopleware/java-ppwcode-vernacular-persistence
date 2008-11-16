/*<license>
Copyright 2004 - $Date$ by PeopleWare n.v..

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

import java.io.Serializable;

import org.ppwcode.metainfo_I.Copyright;
import org.ppwcode.metainfo_I.License;
import org.ppwcode.metainfo_I.vcs.SvnInfo;
import org.ppwcode.vernacular.exception_III.ApplicationException;
import org.ppwcode.vernacular.exception_III.NoLongerSupportedError;
import org.ppwcode.vernacular.persistence_III.VersionedPersistentBean;


/**
 * <p>A stateless {@link Dao DAO} that offers generalized CRUD methods. Methods here require a transaction (~ required).
 *   Methods follow the ppwcode exception vernacular as much as possible. Exceptions thrown during commit or roll-back
 *   are not handled according to the vernacular.</p>
 * <p>This interface allows the use of the stateless CRUD dao as local or remote session beans, and outside the container,
 *   as part of the <acronym title="Data Access Layer">DAL</acronym>. Clients that want to consider a method call
 *   on an interface as an isolated atomic action are better of using {@link AtomicStatelessCrudDao}, which offers
 *   the same methods, but with complete exception handling, at the cost of an isolated transaction.</p>
 */
@Copyright("2004 - $Date$, PeopleWare n.v.")
@License(APACHE_V2)
@SvnInfo(revision = "$Revision$",
         date     = "$Date$")
public interface RequiredTransactionStatelessCrudDao extends StatelessCrudDao {

  /**
   * {@inheritDoc}
   *
   * This method requires a transaction (~ required). In case of semantic problems (wildness), the transaction is set to
   * roll-back only and an {@link ApplicationException} is thrown.
   */
  public <_Id_ extends Serializable, _Version_ extends Serializable, _PB_ extends VersionedPersistentBean<_Id_, _Version_>>
  _PB_ createPersistentBean(_PB_ pb) throws ApplicationException, NoLongerSupportedError;

  /**
   * {@inheritDoc}
   *
   * This method requires a transaction (~ required). In case of semantic problems (wildness), the transaction is set to
   * roll-back only and an {@link ApplicationException} is thrown.
   */
  public <_Id_ extends Serializable, _Version_ extends Serializable, _PB_ extends VersionedPersistentBean<_Id_, _Version_>>
  _PB_ updatePersistentBean(_PB_ pb) throws ApplicationException, NoLongerSupportedError;

  /**
   * {@inheritDoc}
   *
   * This method requires a transaction (~ required). In case of semantic problems (wildness), the transaction is set to
   * roll-back only and an {@link ApplicationException} is thrown.
   */
  public <_Id_ extends Serializable, _Version_ extends Serializable, _PB_ extends VersionedPersistentBean<_Id_, _Version_>>
  _PB_ deletePersistentBean(_PB_ pb) throws ApplicationException, NoLongerSupportedError;

}
