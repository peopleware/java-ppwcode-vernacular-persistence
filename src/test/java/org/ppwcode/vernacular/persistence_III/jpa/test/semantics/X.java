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

package org.ppwcode.vernacular.persistence_III.jpa.test.semantics;


import static org.ppwcode.metainfo_I.License.Type.APACHE_V2;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.ppwcode.metainfo_I.Copyright;
import org.ppwcode.metainfo_I.License;
import org.ppwcode.metainfo_I.vcs.SvnInfo;
import org.ppwcode.vernacular.persistence_III.jpa.AbstractIntegerIdIntegerVersionedPersistentBean;
import org.ppwcode.vernacular.semantics_VI.exception.CompoundPropertyException;
import org.ppwcode.vernacular.semantics_VI.exception.PropertyException;
import org.toryt.annotations_I.Basic;
import org.toryt.annotations_I.Expression;
import org.toryt.annotations_I.MethodContract;
import org.toryt.annotations_I.Throw;


/**
 * Master has references to DetailA and DetailB.
 * In addition, it contains a number of properties.
 */
@Entity
@Table(name = "x")
@Copyright("2008 - $Date$, PeopleWare n.v.")
@License(APACHE_V2)
@SvnInfo(revision = "$Revision$",
         date     = "$Date$")
public class X extends AbstractIntegerIdIntegerVersionedPersistentBean {

    public static final String EMPTY = "";


    /*<property name="period">
    -------------------------------------------------------------------------*/

    @Basic(
            init = @Expression("period == null")
    )
    public Date getPeriod() {
        return $period;
    }

    @MethodContract(
            post = @Expression("period == _period")
    )
    public void setPeriod(Date _period) throws PropertyException {
        $period = _period;
    }

    @Column(name = "period")
    private Date $period = null;

    /*</property>*/


    /*<property name="description">
    -------------------------------------------------------------------------*/

    @Basic(invars = @Expression("description != EMPTY"),
            init = @Expression("description == null"))
    public String getDescription() {
        return $description;
    }

    @MethodContract(
            post = @Expression("description == _description"),
            exc = @Throw(type = PropertyException.class, cond = @Expression("_description == null || ''.equals(_description)"))
    )
    public void setDescription(String _description) throws PropertyException {
        if (EMPTY.equals(_description)) {
            throw new PropertyException(this, "description", "EMPTY_NOT_ALLOWED", null);
        }
        this.$description = _description;
    }

    @Column(name = "description")
    private String $description;

    /*</property>*/


    /*<property name="locked">
    -------------------------------------------------------------------------*/

    @Basic(init = @Expression("locked == false"))
    public boolean getLocked() {
        return $locked;
    }

    @MethodContract(
            post = @Expression("locked == _locked")
    )
    public void setLocked(boolean _locked) {
        this.$locked = _locked;
    }

    @Column(name = "locked")
    private boolean $locked;

    /*</property>*/


    /*<property name="e">
    -------------------------------------------------------------------------*/

    @Basic(invars = @Expression("e != null ? e.xs.contains(this)"),
        init = @Expression("e == null"))
        public E getE() {
      return $e;
    }

    @MethodContract(
        post = @Expression("e == _e"),
        exc = @Throw(type = PropertyException.class, cond = @Expression("_e == null"))
    )
    public void setE(E e) throws PropertyException {
      if (e == null) {
        throw new PropertyException(this, "e", "NULL_NOT_ALLOWED", null);
      }
      if ($e != null) {
        $e.removeX(this);
      }
      $e = e;
      $e.addX(this);
    }

    @ManyToOne(cascade = {}, fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "e_fk")
    private E $e;

    /*</property>*/


    /*<property name="y">
    -------------------------------------------------------------------------*/

    @Basic(init = @Expression("$y == null"))
    public Y getY() {
        return $y;
    }

    @MethodContract(
            post = @Expression("$y == y")
    )
    public void setY(Y y) {
        $y = y;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "y_fk")
    private Y $y;

    /*</property>*/



    @Override
    @MethodContract(
            post = {
                    @Expression("period == null ? result.contains(this, 'period', 'NULL_NOT_ALLOWED', null)"),
                    @Expression("description == null ? result.contains(this, 'description', 'NULL_NOT_ALLOWED', null)"),
                    @Expression("e == null ? result.contains(this, 'e', 'NULL_NOT_ALLOWED', null)"),
                    @Expression("y == null ? result.contains(this, 'y', 'NULL_NOT_ALLOWED', null)")
            }
    )
    public CompoundPropertyException wildExceptions() {
        CompoundPropertyException cpe = super.wildExceptions();
        if (getPeriod() == null) {
            cpe.addElementException((new PropertyException(this, "period", "NULL_NOT_ALLOWED", null)));
        }
        if (getDescription() == null) {
            cpe.addElementException((new PropertyException(this, "description", "NULL_NOT_ALLOWED", null)));
        }
        if (getY() == null) {
            cpe.addElementException((new PropertyException(this, "y", "NULL_NOT_ALLOWED", null)));
        }
        if (getE() == null) {
            cpe.addElementException((new PropertyException(this, "e", "NULL_NOT_ALLOWED", null)));
        }
        return cpe;
    }
}
