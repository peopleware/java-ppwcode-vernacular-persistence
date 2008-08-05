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

package org.ppwcode.vernacular.persistence_III.dao.hibernate3;


import static org.ppwcode.metainfo_I.License.Type.APACHE_V2;

import java.io.Serializable;
import java.util.ConcurrentModificationException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.ppwcode.metainfo_I.Copyright;
import org.ppwcode.metainfo_I.License;
import org.ppwcode.metainfo_I.vcs.SvnInfo;
import org.ppwcode.vernacular.exception_II.ExternalError;
import org.ppwcode.vernacular.persistence_III.PersistentBean;
import org.ppwcode.vernacular.persistence_III.dao.PagingList;
import org.toryt.annotations_I.Basic;
import org.toryt.annotations_I.Expression;
import org.toryt.annotations_I.Invars;
import org.toryt.annotations_I.MethodContract;


/**
 * A list of lists, that contains the result of a Hibernate query
 * using paging. Each page, except for the last,
 * has size {@link #getPageSize()}.
 * When the virtual resultset on the DB changes during iteration,
 * we throw a {@link ConcurrentModificationException} when the next
 * page is requested.
 *
 * @author Jan Dockx
 * @author Ruben Vandeginste
 * @author Peopleware n.v.
 *
 */
@Copyright("2004 - $Date$, PeopleWare n.v.")
@License(APACHE_V2)
@SvnInfo(revision = "$Revision$",
         date     = "$Date$")

public final class Hibernate3PagingList<_Id_ extends Serializable, _PersistentBean_ extends PersistentBean<_Id_>>
    extends PagingList<_Id_, _PersistentBean_> {

  private static final Log LOG = LogFactory.getLog(Hibernate3PagingList.class);


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
      }
  )
  public Hibernate3PagingList(Query query, Query countQuery, int pageSize) {
    super(pageSize, retrieveRecordCount(countQuery));
    assert query != null;
    assert countQuery != null;
    $query = query;
    $countQuery = countQuery;
  }

  @MethodContract(
      pre = {
          @Expression("_criteria != null"),
          @Expression("_countQuery != null"),
          @Expression("_pageSize > 0")
      },
      post = {
          @Expression("^criteria == _criteria"),
          @Expression("^countQuery == _countQuery"),
          @Expression("^pageSize == _pageSize")
      }
  )
  public Hibernate3PagingList(Criteria criteria, Query countQuery, int pageSize) {
    super(pageSize, retrieveRecordCount(countQuery));
    assert criteria != null;
    assert countQuery != null;
    $criteria = criteria;
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



  /*<property name="criteria">*/
  //------------------------------------------------------------------

  @Basic
  public final Criteria getCriteria() {
    return $criteria;
  }

  @Invars({
    @Expression("$criteria != null")
  })
  private Criteria $criteria;

  /*</property>*/



  /*<property name="countQuery">*/
  //------------------------------------------------------------------

  @Basic
  public final Query getCountQuery() {
    return $countQuery;
  }

  private static int retrieveRecordCount(Query countQuery) {
    try {
      LOG.debug("retrieving record count");
      int result = ((Integer)countQuery.uniqueResult()).intValue();
      LOG.debug("record count is " + result);
      return result;
    }
    catch (HibernateException e) {
      throw new ExternalError("cannot retrieve count", e);
    }
  }

  @Override
  protected final int retrieveRecordCount() {
      return retrieveRecordCount(getCountQuery());
  }

  @Invars({
    @Expression("$countQuery != null")
  })
  private Query $countQuery;

  /*</property>*/



  @Override
  protected final List<_PersistentBean_> retrievePage(int retrieveSize, int startOfPage) {
    try {
      List<_PersistentBean_> page = null;
      if ($criteria != null) {
        page = retrievePageCriteria(retrieveSize, startOfPage);
      }
      else if ($query !=  null) {
        page = retrievePageQuery(retrieveSize, startOfPage);
      }
      else {
        assert false : "Cannot happen";
      }
      LOG.debug("page retrieved successfully");
      return page;
    }
    catch (HibernateException hExc) {
      throw new ExternalError("cannot retrieve page", hExc);
    }
  }

  private List<_PersistentBean_> retrievePageCriteria(int retrieveSize, int startOfPage)
      throws HibernateException {
    try {
      $criteria.setMaxResults(retrieveSize); // first and last record is for check only (depending on booleans)
      $criteria.setFirstResult(startOfPage);
      @SuppressWarnings("unchecked")
      List<_PersistentBean_> page = $criteria.list();
      return page;
    }
    catch (ClassCastException ccExc) {
      throw new ExternalError("retrieved list was not a list of PersistentBean objects", ccExc);
    }
  }

  private List<_PersistentBean_> retrievePageQuery(int retrieveSize, int startOfPage)
      throws HibernateException {
    try {
      $query.setMaxResults(retrieveSize); // first and last record is for check only (depending on booleans)
      $query.setFirstResult(startOfPage);
      @SuppressWarnings("unchecked")
      List<_PersistentBean_> page = $query.list();
      return page;
    }
    catch (ClassCastException ccExc) {
      throw new ExternalError("retrieved list was not a list of PersistentBean objets", ccExc);
    }
  }

}
