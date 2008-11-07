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

package org.ppwcode.vernacular.persistence_III.junit;

import static org.ppwcode.metainfo_I.License.Type.APACHE_V2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ppwcode.metainfo_I.Copyright;
import org.ppwcode.metainfo_I.License;
import org.ppwcode.metainfo_I.vcs.SvnInfo;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;


/**
 * <pre>
 * This class executes query scripts configured in the spring context file.
 * <p/>
 * - Statements can be spread over several lines, but each statement has to
 *   be closed with a ';'
 * - Lines starting with '--' are considered comment lines
 * </pre>
 * <p/>
 *
 * @author Olivier Sinnaeve
 * @author Ruben Vandeginste
 */
@Copyright("2008 - $Date: $, PeopleWare n.v.")
@License(APACHE_V2)
@SvnInfo(revision = "$Revision: $",
         date     = "$Date: $")
public class DatabaseHelper extends JdbcDaoSupport {

  private final static Log logger = LogFactory.getLog(DatabaseHelper.class);

  private String createScript;
  private String populateScript;
  private String dropScript;
  private boolean executeDatabaseMethods;


  public void createTables() throws Exception {
    int statements = executeStatementsFromFile(createScript);
    logger.info("Number of statements executed : " + statements);
  }

  public void populateTables() throws Exception {
    logger.info("script: "+populateScript);
    int statements = executeStatementsFromFile(populateScript);
    logger.info("Number of statements executed : " + statements);
  }

  public void dropTables() throws Exception {
    int statements = executeStatementsFromFile(dropScript);
    logger.info("Number of statements executed : " + statements);
  }

  private int executeStatementsFromFile(String file) throws Exception {
    InputStream script = getClass().getClassLoader().getResourceAsStream(file);
    LineNumberReader reader = new LineNumberReader(new InputStreamReader(script));

    // current statement
    int stmts = 0;
    // for the full statement
    String line = "";
    // line just read
    String next = reader.readLine();

    try {
      while (next != null) {
        next = next.trim();

        // discard empty lines and comment lines
        if (!(next.equals("") || next.startsWith("--"))) {
          line += " " + next;
        }

        // bingo if we find a ';'
        if (line.contains(";")) {
          getJdbcTemplate().execute(line);
          line = "";
          stmts++;
        }

        // read next line
        next = reader.readLine();
      }

      // if the current accumulator is not empty, then we have a problem
      if (!line.equals("")) {
        logger.error("Error while processing statement "+ stmts + " (missing ';'?) [failed on line :" + reader.getLineNumber() + "]");
        // MUDO make this a DataAccessException too
        throw new Exception("Error while processing SQL file: " + file);
      }

    } catch (DataAccessException exc) {
      logger.error("Error while processing statement " + stmts + "[failed on line :" + reader.getLineNumber() + "]");
      throw exc;
    }

    return stmts;
  }


  @Required
  public void setCreateScript(String createScript) {
    this.createScript = createScript;
  }

  @Required
  public void setPopulateScript(String populateScript) {
    this.populateScript = populateScript;
  }

  @Required
  public void setDropScript(String dropScript) {
    this.dropScript = dropScript;
  }

  public boolean isExecuteDatabaseMethods() {
    return executeDatabaseMethods;
  }

  @Required
  public void setExecuteDatabaseMethods(boolean executeDatabaseMethods) {
    this.executeDatabaseMethods = executeDatabaseMethods;
  }

}
