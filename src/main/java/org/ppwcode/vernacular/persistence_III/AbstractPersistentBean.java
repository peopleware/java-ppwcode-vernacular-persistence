/*<license>
  Copyright 2004, PeopleWare n.v.
  NO RIGHTS ARE GRANTED FOR THE USE OF THIS SOFTWARE, EXCEPT, IN WRITING,
  TO SELECTED PARTIES.
</license>*/
package org.ppwcode.vernacular.persistence_III;


import java.io.Serializable;

import org.ppwcode.bean_VI.AbstractRousseauBean;


/**
 * A partial implementation of the interface {@link PersistentBean}.
 *
 *
 * @author    nsmeets
 * @author    PeopleWare n.v.
 */
public abstract class AbstractPersistentBean<_IdType_>
    extends AbstractRousseauBean
    implements PersistentBean<_IdType_>, Serializable {

  /*<section name="Meta Information">*/
  //------------------------------------------------------------------

  /** {@value} */
  public static final String CVS_REVISION = "$Revision$";
  /** {@value} */
  public static final String CVS_DATE = "$Date$";
  /** {@value} */
  public static final String CVS_STATE = "$State$";
  /** {@value} */
  public static final String CVS_TAG = "$Name$";

  /*</section>*/



  /*<property name="id">*/
  //------------------------------------------------------------------

  /**
   * @see   PersistentBean
   */
  public final void setId(final _IdType_ id) {
    $id = id;
  }

  /**
   * @basic
   */
  public final _IdType_ getId() {
    return $id;
  }

  private _IdType_ $id;

  /*</property>*/

  /**
   * This instance has the same id as the instance <code>other</code>.
   *
   * @see   PersistentBean
   */
  public final boolean hasSameId(final PersistentBean<_IdType_> other) {
    return (other != null)
             && ((getId() == null)
                   ? other.getId() == null
                   : getId().equals(other.getId()));
  }

}
