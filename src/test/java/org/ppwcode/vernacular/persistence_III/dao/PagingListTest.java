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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ppwcode.vernacular.exception_III.ExternalError;
import org.ppwcode.vernacular.persistence_III.AbstractPersistentBean;
import org.ppwcode.vernacular.persistence_III.PersistentBean;


public class PagingListTest {

  // stub for PersistentBean
  public static class AbstractPersistentBeanSTUB<_Id_ extends Serializable>
  extends AbstractPersistentBean<_Id_> {

    public AbstractPersistentBeanSTUB(_Id_ id, String str, int i) {
      super();
      setPersistenceId(id);
      $str = str;
      $i = i;
    }

    public final String getString() {
      return $str;
    }

    public final void setString(String str) {
      $str = str;
    }

    private String $str;


    public final int getInt() {
      return $i;
    }

    public final void setInt(int i) {
      $i = i;
    }

    private int $i;
  }

  // stub for PagingList
  public static class PagingListSTUB<_Id_ extends Serializable, _PersistentBean_ extends PersistentBean<_Id_>>
    extends PagingList<_Id_,_PersistentBean_>{

    public PagingListSTUB(int pageSize, int recordCount, List<_PersistentBean_> pbs) {
      super(pageSize, recordCount);
      $pbs = pbs;
    }

    public List<_PersistentBean_> getPersistentBeanList() {
      return $pbs;
    }

    public void setPersistentBeanList(ArrayList<_PersistentBean_> pbs) {
      $pbs = pbs;
    }

    private List<_PersistentBean_> $pbs;

    @Override
    protected int retrieveRecordCount() {
      return getRecordCount();
    }

    @Override
    protected List<_PersistentBean_> retrievePage(int retrieveSize, int startOfPage) {
      try {
        ArrayList<_PersistentBean_> result = new ArrayList<_PersistentBean_>();
        int i = retrieveSize;
        int j = startOfPage;
        while (i > 0) {
          if (j < $pbs.size()) {
            result.add($pbs.get(j));
          }
          i--;
          j++;
        }
        return result;
      }
      catch (IndexOutOfBoundsException e) {
        throw new ExternalError("cannot retrieve page", e);
      }
    }
  }



  // paginglist
  private List<PagingList<?,?>> subjects;
  private List<Integer> pagesizes;
  private List<List<?>> beanlists;
  private List<Integer> numberpages;

  @Before
  public void setUp() throws Exception {
    // beans
    int beanId;
    String beanString;
    int beanInt;
    //bean1
    List<AbstractPersistentBeanSTUB<Integer>> beans1 = new ArrayList<AbstractPersistentBeanSTUB<Integer>>();
    beanId = 1000;
    beanString = "bean1 ";
    beanInt = 15000;
    for (int i = 0; i < 34; i++) {
      beans1.add(new AbstractPersistentBeanSTUB<Integer>(
          beanId, beanString+Integer.toHexString(beanInt), beanInt));
      beanId++;
      beanInt++;
    }
    // beans2
    List<AbstractPersistentBeanSTUB<Integer>> beans2 = new ArrayList<AbstractPersistentBeanSTUB<Integer>>();
    beanId = 9000;
    beanString = "bean2 ";
    beanInt = 100;
    for (int i = 0; i < 30; i++) {
      beans2.add(new AbstractPersistentBeanSTUB<Integer>(
          beanId, beanString+Integer.toHexString(beanInt), beanInt));
      beanId++;
      beanInt++;
    }
    // beans3
    List<AbstractPersistentBeanSTUB<String>> beans3 = new ArrayList<AbstractPersistentBeanSTUB<String>>();
    beanId = 9000;
    beanString = "bean3 ";
    beanInt = 100;
    for (int i = 0; i < 11; i++) {
      beans3.add(new AbstractPersistentBeanSTUB<String>(
          Integer.toHexString(beanId), beanString+Integer.toHexString(beanInt), beanInt));
      beanId++;
      beanInt++;
    }
    // beans4
    List<AbstractPersistentBeanSTUB<String>>  beans4 = new ArrayList<AbstractPersistentBeanSTUB<String>>();
    // paginglists
    subjects = new ArrayList<PagingList<?,?>>();
    beanlists = new ArrayList<List<?>>();
    pagesizes = new ArrayList<Integer>();
    numberpages = new ArrayList<Integer>();

    // (nr pages, pagesize, record count, list of beans)
    createTestIntegerPagingList(4, 10, beans1.size(), beans1);
    createTestIntegerPagingList(2, 20, beans1.size(), beans1);
    createTestIntegerPagingList(3, 10, beans2.size(), beans2);
    createTestStringPagingList(11, 1, beans3.size(), beans3);
    createTestStringPagingList(1, 11, beans3.size(), beans3);
    createTestStringPagingList(1, 12, beans3.size(), beans3);
    createTestStringPagingList(0, 10, 0, beans4);
  }

  private void
      createTestIntegerPagingList(int nbpages, int pagesize, int recordcount, List<AbstractPersistentBeanSTUB<Integer>> beans) {
    PagingList<?, ?> result = new PagingListSTUB<Integer , AbstractPersistentBeanSTUB<Integer>>(pagesize, beans.size(), beans);
    pagesizes.add(pagesize);
    numberpages.add(nbpages);
    beanlists.add(beans);
    subjects.add(result);
  }

  private void
      createTestStringPagingList(int nbpages, int pagesize, int recordcount, List<AbstractPersistentBeanSTUB<String>> beans) {
    PagingList<?, ?> result = new PagingListSTUB<String , AbstractPersistentBeanSTUB<String>>(pagesize, beans.size(), beans);
    pagesizes.add(pagesize);
    numberpages.add(nbpages);
    beanlists.add(beans);
    subjects.add(result);
  }


  @After
  public void tearDown() throws Exception {
    subjects = null;
  }


  private void assertInvariants(PagingList<?,?> subject){
    assertTrue(subject.getPageSize() > 0);
    assertTrue(subject.getRecordCount() >= 0);
    assertTrue(subject.size() >= 0);
  }

  @Test
  public void testPageSize() {
    int i = 0;
    for (PagingList<?,?> subject : subjects) {
      assertEquals(subject.getPageSize(), pagesizes.get(i++).intValue());
      assertInvariants(subject);
    }
  }

  @Test
  public void testRecordCount() {
    int i = 0;
    for (PagingList<?,?> subject : subjects) {
      assertEquals(subject.getRecordCount(), beanlists.get(i++).size());
      assertInvariants(subject);
    }
  }

  @Test
  public void testSize() {
    int i = 0;
    for (PagingList<?,?> subject : subjects) {
      assertEquals(subject.size(), numberpages.get(i++).intValue());
      assertInvariants(subject);
    }
  }

  @Test
  public void testContents() {
    int subjectIndex = 0;
    for (PagingList<?,?> subject : subjects) {
      if (subject.size() > 0) {
        int el = 0;
        @SuppressWarnings("unchecked")
        PagingList.PagesIterator pi = subject.listIterator();
        /* IMPORTANT - WORKAROUND
         * The code above obviously should be PagingList<?, ?>.PagesIterator
         * to avoid the warning we are suppressing.
         * But, it turns out that compiles fine with eclipse, and on debian via Maven,
         * with javac v 1.5.0_14. On Mac OS X however via Maven, with javac v 1.5.0_13,
         * compilation fails.
         * With this workaround, it compiles also in the latter case. It is unclear
         * what the reason is (maybe our code _is_ bad). See Issue96 on
         * http://code.google.com/p/ppwcode/issues/detail?id=96.
         * IMPORTANT - WORKAROUND */
        while (pi.hasNext()) {
          List<?> beanlist = pi.next();
          for (int j = 0; j < beanlist.size(); j++) {
            assertEquals(beanlist.get(j), beanlists.get(subjectIndex).get(el++));
          }
        }
        assertTrue(el == beanlists.get(subjectIndex).size());
        assertInvariants(subject);
        subjectIndex++;
      } else {
        boolean failed = false;
        try {
          subject.listIterator(); // we are not interested in the result
          failed = true; // we cannot call fail() here, because that merely throws an AssertionError too
        }
        catch (AssertionError ppe) {
          assertInvariants(subject);
        }
        if (failed) {
          fail();
        }
      }
    }
  }
}
