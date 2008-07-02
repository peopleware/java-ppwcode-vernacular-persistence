/*<license>
Copyright 2004 - $Date: 2008-06-26 21:33:40 +0200 (Thu, 26 Jun 2008) $ by PeopleWare n.v..

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

import static org.ppwcode.metainfo_I.License.Type.APACHE_V2;

import java.io.Serializable;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ppwcode.metainfo_I.Copyright;
import org.ppwcode.metainfo_I.License;
import org.ppwcode.metainfo_I.vcs.SvnInfo;
import org.ppwcode.vernacular.persistence_III.PersistenceExternalError;
import org.ppwcode.vernacular.persistence_III.PersistentBean;
import org.ppwcode.vernacular.persistence_III.dao.PagingList;
import org.toryt.annotations_I.Basic;
import org.toryt.annotations_I.Expression;
import org.toryt.annotations_I.Invars;
import org.toryt.annotations_I.MethodContract;
import org.toryt.annotations_I.Throw;

@Copyright("2004 - $Date: 2008-06-27 16:28:22 +0200 (Fri, 27 Jun 2008) $, PeopleWare n.v.")
@License(APACHE_V2)
@SvnInfo(revision = "$Revision: 1418 $",
         date     = "$Date: 2008-06-27 16:28:22 +0200 (Fri, 27 Jun 2008) $")
public final class JpaPagingList<_Id_ extends Serializable, _PersistentBean_ extends PersistentBean<_Id_>>
		extends PagingList<_Id_, _PersistentBean_> {

  private static final Log LOG = LogFactory.getLog(JpaPagingList.class);

	
  /*<construction>*/
  //------------------------------------------------------------------

  @MethodContract(
      pre = {
          @Expression("_query != null"),
          @Expression("_countQuery != null"),
          @Expression("_pageSize > 0")
      },
      post = {
          @Expression("^query == _query"),
          @Expression("^countQuery == _countQuery"),
          @Expression("^pageSize == _pageSize")
      },
      exc = {
          @Throw(type = PersistenceExternalError.class,
                 cond = @Expression("true"))
      }
  )
  public JpaPagingList(Query query, Query countQuery, int pageSize) throws PersistenceExternalError {
    super(pageSize, retrieveRecordCount(countQuery));
    assert query != null;
    assert countQuery != null;
    $query = query;
    $countQuery = countQuery;
  }

  /*</construction>*/

	
  /*<property name="query">*/
  //------------------------------------------------------------------

  @Basic
  public final Query getQuery() {
    return $query;
  }

  @Invars({
    @Expression("$query != null")
  })  
  private Query $query;

  /*</property>*/

  
  /*<property name="countQuery">*/
  //------------------------------------------------------------------

  @Basic
  public final Query getCountQuery() {
    return $countQuery;
  }

  private static int retrieveRecordCount(Query countQuery) throws PersistenceExternalError {
    try {
      LOG.debug("retrieving record count");
      int result = ((Integer)countQuery.getSingleResult()).intValue();
      LOG.debug("record count is " + result);
      return result;
    }
    catch (NoResultException nre) {
      throw new PersistenceExternalError("cannot retrieve count", nre);
    }
    catch (NonUniqueResultException nure) {
      throw new PersistenceExternalError("cannot retrieve count", nure);
    }
    catch (IllegalStateException ise) {
      throw new PersistenceExternalError("cannot retrieve count", ise);
    }
  }

  protected final int retrieveRecordCount() throws PersistenceExternalError {
      return retrieveRecordCount(getCountQuery());
  }

  @Invars({
    @Expression("$countQuery != null")
  })
  private Query $countQuery;

  /*</property>*/

  
	@Override
	protected List<_PersistentBean_> retrievePage(int retrieveSize, int startOfPage)
			throws PersistenceExternalError {
		// TODO Auto-generated method stub
		return null;
	}

}