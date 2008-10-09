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
import org.ppwcode.vernacular.exception_II.InternalException;
import org.ppwcode.vernacular.exception_II.NoLongerSupportedError;
import org.ppwcode.vernacular.persistence_III.PersistentBean;


/**
 * <p>In contrast to the {@link AsyncCrudDao}, this {@link Dao} is stateless. This interface exists mainly in support
 *   if EJB3 implementations of {@link StatelessCrudTransactionDao}, but other uses are likely. The difference with
 *   that interface is that in this interface, we do not create transactions. The methods in this interface need to be
 *   called in the context of an existing transaction (either managed by the developer, or a container) (~ mandatory).
 *   The reason for the separation is exception handling: most relevant exceptions will only occur when committing the
 *   transaction, and in this library we cannot generally decide whether we you should call the CRUD operations these 2
 *   interfaces offer within an existing transaction or not. Therefore, if you need to extend the transaction broader than
 *   the CRUD operations, you should use this interface, and deal with exception handling on commit yourself. If not,
 *   you should use {@link StatelessCrudTransactionDao}, which deals with exceptions for you.</p>
 * <p>We assume most uses of this interface will happen locally, called from another session bean, or in a Data Access
 *   Layer in a 2-tier setup.</p>
 * <p>In throwing exceptions, we try to make a difference between programming errors, external exceptional conditions, and
 *   internal exceptional conditions, but anything that happens at commit time is to be handled by the caller.</p>
 */
@Copyright("2004 - $Date$, PeopleWare n.v.")
@License(APACHE_V2)
@SvnInfo(revision = "$Revision$",
         date     = "$Date$")
public interface StatelessCrudJoinTransactionDao extends StatelessCrudDao {

  /**
   * {@inheritDoc}
   * This method requires an existing transaction, and doesn't commit itself. In case of semantic problems (wildness), the transaction
   * is set to roll-back only.
   *
   * @mudo specific exception for rollback, or InternalException
   * @mudo contract
   */
  public <_Id_ extends Serializable, _PB_ extends PersistentBean<_Id_>> _PB_ mergePersistentBean(_PB_ pb) throws InternalException, NoLongerSupportedError;

  /**
   * {@inheritDoc}
   * This method requires an existing transaction, and doesn't commit or roll-back itself.
   *
   * @mudo specific exception for rollback, or InternalException
   * @mudo contract
   */
  public <_Id_ extends Serializable, _PB_ extends PersistentBean<_Id_>> _PB_ deletePersistentBean(_PB_ pb) throws InternalException, NoLongerSupportedError;

}
