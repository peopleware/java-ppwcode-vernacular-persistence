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
 * <p>A stateless {@link Dao DAO} that offers generalized CRUD methods. Methods here are executed in their own,
 *   separate transaction. If a transaction already exists in the callers context, it is suspended (~ requires new).
 *   Methods follow the ppwcode exception vernacular completely. Apart from the business logic, also exceptions that
 *   happen at commit time or roll-back time, are handled according to the vernacular. This entails logging and
 *   warning of the interested parties.</p>
 * <p>This interface allows the used of the stateless CRUD dao as a remote session bean, and outside the container as
 *   part of the <acronym title="Data Access Layer">DAL</acronym>. The interface is especially useful for clients for
 *   which a CRUD operation maps directly to a use case (and thus an atomic action). Clients that want to use the CRUD
 *   functionality as part of a broader transaction are better of using {@link RequiredTransactionStatelessCrudDao},
 *   at the cost of incomplete exception handling transaction.</p>
 */
@Copyright("2004 - $Date$, PeopleWare n.v.")
@License(APACHE_V2)
@SvnInfo(revision = "$Revision$",
         date     = "$Date$")
public interface AtomicStatelessCrudDao extends StatelessCrudDao {

  /**
   * {@inheritDoc}
   *
   * This method is an atomic transaction. Any exception that is thrown signals a roll-back of the transaction. If
   * the methods ends nominally, the transaction is successfully committed. In case of semantic problems (wildness),
   * the transaction is rolled-back only and an {@link ApplicationException} is thrown.
   */
  public <_Id_ extends Serializable, _Version_ extends Serializable, _PB_ extends VersionedPersistentBean<_Id_, _Version_>>
  _PB_ createPersistentBean(_PB_ pb) throws ApplicationException, NoLongerSupportedError;

  /**
   * {@inheritDoc}
   *
   * This method is an atomic transaction. Any exception that is thrown signals a roll-back of the transaction. If
   * the methods ends nominally, the transaction is successfully committed. In case of semantic problems (wildness),
   * the transaction is rolled-back only and an {@link ApplicationException} is thrown.
   */
  public <_Id_ extends Serializable, _Version_ extends Serializable, _PB_ extends VersionedPersistentBean<_Id_, _Version_>>
  _PB_ updatePersistentBean(_PB_ pb) throws ApplicationException, NoLongerSupportedError;

  /**
   * {@inheritDoc}
   *
   * This method is an atomic transaction. Any exception that is thrown signals a roll-back of the transaction. If the methods ends nominally,
   * the transaction is successfully committed.
   */
  public <_Id_ extends Serializable, _Version_ extends Serializable, _PB_ extends VersionedPersistentBean<_Id_, _Version_>>
  _PB_ deletePersistentBean(_PB_ pb) throws ApplicationException, NoLongerSupportedError;

}
