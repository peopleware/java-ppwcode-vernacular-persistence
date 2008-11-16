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

package org.ppwcode.vernacular.persistence_III.junit;


import org.junit.After;
import static org.junit.Assert.assertNotNull;
import static org.ppwcode.metainfo_I.License.Type.APACHE_V2;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.ppwcode.metainfo_I.Copyright;
import org.ppwcode.metainfo_I.License;
import org.ppwcode.metainfo_I.vcs.SvnInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * <p>Helps creating tables and populating them before a test is run.
 * Helps dropping tables after every test run.</p>
 * <p/>
 * <p>This implies that this class helps generating and dropping a database structure
 * before every test is run. This ensures that tests are run against consistent data.</p>
 * <p/>
 * <p>Default properties are:</p>
 * <pre>
 *  $databaseHelper.createScript=scripts/create_ddl.sql
 *  $databaseHelper.populateScript=scripts/dml.sql
 *  $databaseHelper.dropScript=scripts/drop_ddl.sql
 *  $databaseHelper.executeDatabaseMethods=true
 * <p/>
 *  datasource.driverClassName=org.hsqldb.jdbcDriver
 *  datasource.url=jdbc:hsqldb:mem:dummy
 *  datasource.username=sa
 *  datasource.password=
 * </pre>
 * <p/>
 * <p>Overriding these properties is done in a file <b>META-INF/override_database.properties</b> that should be found
 * on the classpath.</p>
 *
 * @author Olivier Sinnaeve
 * @author Ruben Vandeginste
 * @version 1.0.0
 * @since 24-jul-2008
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:$databaseHelper.xml")
@Copyright("2008 - $Date$, PeopleWare n.v.")
@License(APACHE_V2)
@SvnInfo(revision = "$Revision$",
         date     = "$Date$")
public abstract class DatabaseTest {

  private DatabaseHelper $databaseHelper;

  /**
   * creates the tables
   * if the executeDatabaseMethods property is set to true
   *
   * @throws Exception when things go wrong
   */
  @Before
  public void createTablesAndPopulate() throws Exception {
    if ($databaseHelper.isExecuteDatabaseMethods()) {
      $databaseHelper.createTables();
      $databaseHelper.populateTables();
    }
  }

  /**
   * Drops the tables
   * if the executeDatabaseMethods property is set to true
   *
   * @throws Exception when things go wrong
   */
  @After
  public void dropTables() throws Exception {
    if ($databaseHelper.isExecuteDatabaseMethods()) {
      $databaseHelper.dropTables();
    }
  }

  /**
   * Spring auto injected $databaseHelper.
   * Contains the locations of the scripts and executes them.
   *
   * @param databaseHelper the helper to create, populate and drop the tables
   */
  @Autowired
  public void setDatabaseHelper(DatabaseHelper databaseHelper) {
    this.$databaseHelper = databaseHelper;
    assertNotNull(this.$databaseHelper);
  }

  public DatabaseHelper getDatabaseHelper() {
    return $databaseHelper;
  }

}
