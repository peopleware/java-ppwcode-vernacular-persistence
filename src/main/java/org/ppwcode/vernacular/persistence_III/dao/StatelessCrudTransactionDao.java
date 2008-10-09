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
 * <p>In contrast to the {@link AsyncCrudDao}, this {@link Dao} is stateless. This is done to make it possible to
 *   align transaction borders with method calls, which makes working with a remote implementation that much easier.
 *   Thus, for mutating methods (merge, delete), calling the method will result in the start of a transaction,
 *   the execution of the functionality, and a commit or roll-back. Failure is reported with exceptions according to
 *   the ppwcode exception vernacular.</p>
 * <p>Methods from this interface that change the persisted state work in their own separated transaction. When called
 *   within a transaction, the current transaction is suspended, and the method executes inside a new transaction that
 *   can commit successfully before returning on its own (~ requires new). When you need more synchronization with the
 *   rest of your algorithm, use another means of getting the same result ({@link StatelessCrudDao} comes to mind :-) ).</p>
 * <p>We assume most uses of this interface will happen remotely, called from a remote client.</p>
 * <p>In throwing exceptions, we try to make a difference between programming errors, external exceptional conditions, and
 *   internal exceptional conditions. Semantic problems are certainly of the latter nature. But also the database can signal
 *   semantic problems (constraint violations mainly). This type tries to handle also anything that happens at commit.</p>
 */
@Copyright("2004 - $Date$, PeopleWare n.v.")
@License(APACHE_V2)
@SvnInfo(revision = "$Revision$",
         date     = "$Date$")
public interface StatelessCrudTransactionDao extends StatelessCrudDao {

  /**
   * {@inheritDoc}
   * This method is an atomic transaction. Any exception that is thrown signals a roll-back of the transaction. If the methods ends nominally,
   * the transaction is successfully committed.
   *
   * @mudo specific exception for rollback, or InternalException
   * @mudo contract
   */
  public <_Id_ extends Serializable, _PB_ extends PersistentBean<_Id_>> _PB_ mergePersistentBean(_PB_ pb) throws InternalException, NoLongerSupportedError;

  /**
   * {@inheritDoc}
   * This method is an atomic transaction. Any exception that is thrown signals a roll-back of the transaction. If the methods ends nominally,
   * the transaction is successfully committed.
   *
   * @mudo specific exception for rollback, or InternalException
   * @mudo contract
   */
  public <_Id_ extends Serializable, _PB_ extends PersistentBean<_Id_>> _PB_ deletePersistentBean(_PB_ pb) throws InternalException, NoLongerSupportedError;

}
