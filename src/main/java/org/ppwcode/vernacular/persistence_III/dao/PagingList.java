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

package org.ppwcode.vernacular.persistence_III.dao;


import static org.ppwcode.metainfo_I.License.Type.APACHE_V2;
import static org.ppwcode.vernacular.exception_III.ProgrammingErrorHelpers.pre;

import java.io.Serializable;
import java.util.AbstractSequentialList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ppwcode.metainfo_I.Copyright;
import org.ppwcode.metainfo_I.License;
import org.ppwcode.metainfo_I.vcs.SvnInfo;
import org.ppwcode.vernacular.persistence_III.PersistentBean;
import org.ppwcode.vernacular.persistence_III.sql.SqlExceptionHandler;
import org.toryt.annotations_I.Basic;
import org.toryt.annotations_I.Expression;
import org.toryt.annotations_I.Invars;
import org.toryt.annotations_I.MethodContract;
import org.toryt.annotations_I.Throw;



/**
 * A list of lists, that contains the result of a query using paging. Each page, except for the last,
 * has size {@link #getPageSize()}. When the virtual resultset on the DB changes during iteration,
 * we throw a {@link ConcurrentModificationException} when the next page is requested.
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
public abstract class PagingList<_Id_ extends Serializable, _PersistentBean_ extends PersistentBean<_Id_>>
  extends AbstractSequentialList<List<_PersistentBean_>>{

  private static final Log LOG = LogFactory.getLog(PagingList.class);


  /*<construction>*/
  //------------------------------------------------------------------

  @MethodContract(
      pre = {
          @Expression("_pageSize > 0"),
          @Expression("_recordCount >= 0")
      },
      post = {
          @Expression("^pageSize == _pageSize")
      }
  )
  protected PagingList(int pageSize, int recordCount) {
    assert pageSize > 0;
    assert recordCount >= 0;
    $pageSize = pageSize;
    $recordCount = recordCount;
    $size = ($recordCount == 0) ? 0 : (($recordCount - 1) / getPageSize()) + 1;
  }

  /*</construction>*/



  protected abstract int retrieveRecordCount();

  protected abstract List<_PersistentBean_> retrievePage(int retrieveSize, int startOfPage);



  /*<property name="sqlExceptionHandler">*/
  //------------------------------------------------------------------

  @Basic(init = @Expression("null"))
  public final SqlExceptionHandler getSqlExceptionHandler() {
    return $sqlExceptionHandler;
  }

  @MethodContract(post = @Expression("sqlExceptionHandler == _sqlExceptionHandler"))
  public final void setSqlExceptionHandler(final SqlExceptionHandler sqlExceptionHandler) {
    $sqlExceptionHandler = sqlExceptionHandler;
  }

  private SqlExceptionHandler $sqlExceptionHandler;

  /*</property>*/



  /*<property name="page size">*/
  //------------------------------------------------------------------

  @Basic
  public final int getPageSize() {
    return $pageSize;
  }

  @Invars({
    @Expression("$pageSize > 0")
  })
  private int $pageSize;

  /*</property>*/



  /*<property name="record count">*/
  //------------------------------------------------------------------

  /**
   * This must be the same on the next DB access,
   * or we give a {@link java.util.ConcurrentModificationException}.
   */
  @Basic
  public final int getRecordCount() {
    return $recordCount;
  }

  @Invars({
    @Expression("$recordCount >= 0")
  })
  private int $recordCount;

  /*</property>*/



  /*<property name="size">*/
  //------------------------------------------------------------------

  /**
   * The number of pages. This might change,
   * when a next page is requested.
   */
  @Basic
  @Override
  public final int size() {
    return $size;
  }

  @Invars({
    @Expression("$size >= 0")
  })
  private int $size;

  /*</property>*/


  @Override
  public final PagesIterator listIterator() {
    return listIterator(0);
  }

  @Override
  public final PagesIterator listIterator(int index) {
    return new PagesIterator(index);
  }

  public final class PagesIterator
    implements ListIterator<List<_PersistentBean_>> {

    /*<construction>*/
    //------------------------------------------------------------------

    @MethodContract(
      pre  = {
        @Expression("_page >= 0"),
        @Expression("_page < size")
      },
      post = {
          @Expression("nextIndex == _page")
      }
    )
    public PagesIterator(int page) {
      pre(page >= 0, "page must be positive");
      pre(page < size(), "page too large");
      $nextPage = page;
    }

    /*</construction>*/



    /*<property name="currentPage">*/
    //------------------------------------------------------------------

    @Basic
    public final int nextIndex() {
      return $nextPage;
    }

    @MethodContract(
        post = {
            @Expression("result == nextIndex - 1")
        }
    )
    public final int previousIndex() {
      return $nextPage - 1;
    }

    @MethodContract(
        post = {
            @Expression("result == (nextIndex < size - 1)")
        }
    )
    public final boolean hasNext() {
      return $nextPage < size();
    }

    @MethodContract(
        post = {
            @Expression("result == (nextIndex > 0)")
        }
    )
    public final boolean hasPrevious() {
      return $nextPage > 0;
    }

    private int $nextPage;

    /*</property>*/



    /*<property name="expected pk of next and previous page">*/
    //------------------------------------------------------------------

    private _Id_ $expectedFirstPkOfNextPage;

    private _Id_ $expectedLastPkOfPreviousPage;

    /*</property>*/



    /**
     * We will retrieve 1 record extra before and after this page,
     * and remember it's PK; for the next or previous page, we can
     * check that it is the expected record; we cannot do this
     * for the first retrieval, or for the first or last page
     */
    public List<_PersistentBean_> next() throws ConcurrentModificationException {
      LOG.debug("retrieving next page (" + $nextPage + ")");
      validateCount();
      boolean isFirstPage = $nextPage <= 0;
      boolean isLastPage = $nextPage >= size() - 1;
      List<_PersistentBean_> page = retrievePage(isFirstPage, isLastPage, $nextPage);
      validateOverlap($expectedFirstPkOfNextPage, page, 1);
      /* real first page record is at 1 (0 is a dummy for
       * the previous after this, which we will remember in a
       * moment; nothing will happen for the first page,
       * where this is not true.
       */
      rememberPks(isFirstPage, isLastPage, page);
      $nextPage++; // update cursor info
      return page;
    }

    /**
     * We will retrieve 1 record extra before and after this page,
     * and remember it's PK; for the next or previous page, we can
     * check that it is the expected record; we cannot do this
     * for the first retrieval, or for the first or last page
     */
    public List<_PersistentBean_> previous() throws ConcurrentModificationException {
      int pageToRetrieve = $nextPage - 1;
      LOG.debug("retrieving previous page (" + pageToRetrieve + ")");
      validateCount();
      boolean isFirstPage = pageToRetrieve <= 0;
      boolean isLastPage = pageToRetrieve >= size() - 1;
      List<_PersistentBean_> page = retrievePage(isFirstPage, isLastPage, pageToRetrieve);
      validateOverlap($expectedLastPkOfPreviousPage, page, size() - 2);
      /* real first page record is at size() - 2 (size() - 1 is a dummy for
       * the next after this, which we will remember in a
       * moment; nothing will happen for the last page,
       * where this is not true.
       */
      rememberPks(isFirstPage, isLastPage, page);
      $nextPage--; // update cursor info
      return page;
    }

    private List<_PersistentBean_> retrievePage(boolean isFirstPage, boolean isLastPage, int pageToRetrieve)
        throws ConcurrentModificationException {
      LOG.debug("retrieving page " + pageToRetrieve);
      int retrieveSize = getPageSize() + (isFirstPage ? 0 : 1) + (isLastPage ? 0 : 1);
      int realStartOfPage = pageToRetrieve * getPageSize();
      int startOfPage = isFirstPage ? realStartOfPage : realStartOfPage - 1;
      List<_PersistentBean_> page = PagingList.this.retrievePage(retrieveSize, startOfPage);
      LOG.debug("page retrieved successfully");
      if (page.isEmpty()) {
        throw new ConcurrentModificationException("page is empty: resultset for this query changed since last DB access");
      }
      return page;
    }

    private void validateCount() throws ConcurrentModificationException {
      LOG.debug("validating that count of total set has not changed");
      int newRecordCount = retrieveRecordCount();
      if (newRecordCount != getRecordCount()) {
        throw new ConcurrentModificationException("total record count for this query changed since last DB access");
      }
    }

    /**
     * With a next page, we want to check the first real record: is it what we expected?
     * With a previous page, we want to check the last real record: is it what we expected?
     * We do not have an expected PK though, when this is the first page to access
     * (which could be in the middle of things).
     *
     * This does nothing if <code>expectedKey</code> is <code>null</code>.
     *
     * For a next, the position to check is 1 (at position 0 there is a dummy
     * to store for the "previous" after this). For a previous, the position to
     * check is size() - 2 (at position size() - 1 there is a dummy to store for
     * the "next" after this). For the first page however, there is no dummy at
     * the start, and for the last there is no dummy at the end, so we would need
     * to differ the position at which to look in the list. However, this method will
     * never be called for those cases: the last page can only be retrieved with
     * next, or with a previous when <code>!hasNext()</code>, that potentially follows a next.
     * For a next, we only check the first record, which follows the rules. The last
     * next overwrites the remembered PK with null (see {@link #rememberPks(boolean, boolean, List)},
     * so no check will be done with the previous that follows. The first page
     * can only be retrieved with a next when <code>!hasPrevious()</code>,
     * that potentially follows a previous, or with a previous. For a previous,
     * we only check the last record, which follows the rules. The last previous
     * overwrites the remembered PK with null (see {@link #rememberPks(boolean, boolean, List)},
     * so no check will be done in the next that follows. Both remembered PK's are null
     * initially.
     * The last issue arises when a page is both the first and the last page. This
     * page can be reached with both a next and a previous, but always with
     * either !hasPrevious or !hasNext(). In this case, nothing is remembered, ever,
     * and there will be no test.
     * In conclusion we can say that using 1 and size() - 2 as the positions to test
     * is ok in all cases, and diversification is not needed.
     */
    private void validateOverlap(Object expectedKey, List<_PersistentBean_> page, int overlapPosition) throws ConcurrentModificationException {
      LOG.debug("validating overlap: expectedKey = " + expectedKey + " for position = " + overlapPosition);
      if ($expectedLastPkOfPreviousPage != null) {
        _PersistentBean_ pb = page.get(overlapPosition);
        LOG.debug("actual id = " + pb.getPersistenceId());
        if (! expectedKey.equals(pb.getPersistenceId())) {
          throw new ConcurrentModificationException("resultset for this query changed since last DB access");
        }
      }
    }

    private void rememberPks(boolean isFirstPage, boolean isLastPage, List<_PersistentBean_> page) {
      LOG.debug("remembering PK of first and last record");
      /*
       * remember the potential extra records at start and end for the
       * next retrieve, and remove them from the result
       */
      if (! isLastPage) {
        int lastIndex = page.size() - 1;
        _PersistentBean_ pb = page.get(lastIndex);
        $expectedFirstPkOfNextPage = pb.getPersistenceId();
        LOG.debug("new $expectedFirstPkOfNextPage: " + $expectedFirstPkOfNextPage);
        page.remove(lastIndex);
      }
      else {
        $expectedFirstPkOfNextPage = null;
      }
      if (! isFirstPage) {
        _PersistentBean_ pb = page.get(0);
        $expectedLastPkOfPreviousPage = pb.getPersistenceId();
        LOG.debug("new $expectedLastPkOfPreviousPage: " + $expectedLastPkOfPreviousPage);
        page.remove(0);
      }
      else {
        $expectedLastPkOfPreviousPage = null;
      }
    }

    @MethodContract(
        post = {
            @Expression("false")
        },
        exc = {
            @Throw(type = UnsupportedOperationException.class,
                    cond = @Expression("true"))
        }
    )
    public void remove() throws UnsupportedOperationException {
      throw new UnsupportedOperationException();
    }

    @MethodContract(
        post = {
            @Expression("false")
        },
        exc = {
            @Throw(type = UnsupportedOperationException.class,
                    cond = @Expression("true"))
        }
    )
    public void set(List<_PersistentBean_> l) throws UnsupportedOperationException  {
      throw new UnsupportedOperationException();
    }

    @MethodContract(
        post = {
            @Expression("false")
        },
        exc = {
            @Throw(type = UnsupportedOperationException.class,
                    cond = @Expression("true"))
        }
    )
    public void add(List<_PersistentBean_> l) throws UnsupportedOperationException {
      throw new UnsupportedOperationException();
    }

  }

}
