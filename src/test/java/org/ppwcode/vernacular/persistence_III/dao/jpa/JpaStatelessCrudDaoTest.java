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

package org.ppwcode.vernacular.persistence_III.dao.jpa;

import static org.junit.Assert.assertNotNull;
import static org.ppwcode.metainfo_I.License.Type.APACHE_V2;

import java.text.ParseException;

import javax.persistence.Persistence;

import org.junit.Before;
import org.junit.Test;
import org.ppwcode.metainfo_I.Copyright;
import org.ppwcode.metainfo_I.License;
import org.ppwcode.metainfo_I.vcs.SvnInfo;
import org.ppwcode.vernacular.exception_III.ApplicationException;
import org.ppwcode.vernacular.persistence_III.IdNotFoundException;
import org.ppwcode.vernacular.persistence_III.jpa.test.businesslogic.jpa.JpaStatelessCrudDao;
import org.ppwcode.vernacular.persistence_III.jpa.test.util.dummy.JpaStatelessCrudDaoTestsProvider;
import org.ppwcode.vernacular.persistence_III.junit.DatabaseTest;


@Copyright("2008 - $Date$, PeopleWare n.v.")
@License(APACHE_V2)
@SvnInfo(revision = "$Revision$",
         date     = "$Date$")
public class JpaStatelessCrudDaoTest extends DatabaseTest {

  private JpaStatelessCrudDao jscd;

  public JpaStatelessCrudDaoTest() {
    System.setProperty("database.properties.per.test",
        "META-INF/perTest/JpaStatelessCrudDaoTest.properties");
  }

  @Override
  @Before
  public void createTablesAndPopulate() throws Exception {
    jscd = new JpaStatelessCrudDao();
    jscd.setEntityManager(Persistence.createEntityManagerFactory(
        JpaTestConstants.PERSISTENCE_UNIT_DAO_JPA_TEST).createEntityManager());
    super.createTablesAndPopulate();
  }

  @Test
  public void testEjb() throws Exception {
    assertNotNull(jscd);
    assertNotNull(jscd.getEntityManager());
  }


  @Test
  public void testRetrievePersistentBean() throws IdNotFoundException,
      ParseException {
    JpaStatelessCrudDaoTestsProvider.testRetrievePersistentBean(jscd);
  }

  @Test
  public void testRetrieveAllPersistentBeans() throws IdNotFoundException {
    JpaStatelessCrudDaoTestsProvider.testRetrieveAllPersistentBeans(jscd);
  }

  @Test
  public void testCreatePersistentBean() throws ApplicationException {
    JpaStatelessCrudDaoTestsProvider.testCreatePersistentBean(jscd, jscd.getEntityManager().getTransaction());
  }

  @Test
  public void testUpdateTimeCardPersistentBean() throws ApplicationException {
    JpaStatelessCrudDaoTestsProvider.testUpdateTimeCardPersistentBean(jscd, jscd.getEntityManager().getTransaction());
  }

  @Test
  public void testUpdateEmployeePersistentBean() throws ApplicationException {
    JpaStatelessCrudDaoTestsProvider.testUpdateEmployeePersistentBean(jscd, jscd.getEntityManager().getTransaction());
  }

  @Test
  public void testUpdateLocalizedNameDescriptionPersistentBean()
      throws ApplicationException {
    JpaStatelessCrudDaoTestsProvider.testUpdateLocalizedNameDescriptionPersistentBean(jscd, jscd.getEntityManager().getTransaction());
  }

  @Test
  public void testUpdateGeneralTaskPersistentBean() throws ApplicationException {
    JpaStatelessCrudDaoTestsProvider.testUpdateGeneralTaskPersistentBean(jscd, jscd.getEntityManager().getTransaction());
  }

  @Test
  public void testDeleteEmployeePersistentBean() {
    JpaStatelessCrudDaoTestsProvider.testDeleteEmployeePersistentBean(jscd, jscd.getEntityManager().getTransaction());
  }

  @Test
  public void testDeleteTimeCardPersistentBean() {
    JpaStatelessCrudDaoTestsProvider.testDeleteTimeCardPersistentBean(jscd, jscd.getEntityManager().getTransaction());
  }

  @Test
  public void testDeleteTaskPersistentBean() {
    JpaStatelessCrudDaoTestsProvider.testDeleteTaskPersistentBean(jscd, jscd.getEntityManager().getTransaction());
  }
}
