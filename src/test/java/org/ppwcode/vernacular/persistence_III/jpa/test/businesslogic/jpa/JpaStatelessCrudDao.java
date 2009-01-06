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

package org.ppwcode.vernacular.persistence_III.jpa.test.businesslogic.jpa;



import static org.ppwcode.metainfo_I.License.Type.APACHE_V2;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.ppwcode.metainfo_I.Copyright;
import org.ppwcode.metainfo_I.License;
import org.ppwcode.metainfo_I.vcs.SvnInfo;
import org.ppwcode.vernacular.persistence_III.jpa.test.businesslogic.RequiredTransactionStatelessCrudDao;
import org.toryt.annotations_I.Basic;


@Copyright("2008 - $Date$, PeopleWare n.v.")
@License(APACHE_V2)
@SvnInfo(revision = "$Revision$",
         date     = "$Date$")
//@Stateless
//@TransactionManagement(TransactionManagementType.CONTAINER)
//@TransactionAttribute(value = TransactionAttributeType.REQUIRED)
public class JpaStatelessCrudDao
    extends org.ppwcode.vernacular.persistence_III.dao.jpa.JpaStatelessCrudDao
    implements RequiredTransactionStatelessCrudDao {

//  @PersistenceContext
  private EntityManager $entityManager;

  @Override
  @Basic
  public EntityManager getEntityManager() {
    return $entityManager;
  }

  public void setEntityManager(EntityManager entityManager) {
    this.$entityManager = entityManager;
  }

  @Override
  public EntityTransaction getEntityTransaction() {
    return entityTransactionFromEntityManager();
  }

}
