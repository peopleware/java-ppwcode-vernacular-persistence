/*<license>
Copyright 2004 - $Date$ by PeopleWare n.v..

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

package org.ppwcode.vernacular.persistence_III.jpa;


import static org.ppwcode.vernacular.exception_II.ProgrammingErrorHelpers.preArgumentNotNull;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ppwcode.vernacular.exception_II.InternalTransportException;
import org.ppwcode.vernacular.semantics_VI.bean.RousseauBean;
import org.ppwcode.vernacular.semantics_VI.exception.CompoundPropertyException;
import org.toryt.annotations_I.Expression;
import org.toryt.annotations_I.MethodContract;
import org.toryt.annotations_I.Throw;


/**
 * <p>This is a JPA entity listener that normalizes and validates {@link RousseauBean} instances immediately
 *   prior to database insertion and update, using {@link RousseauBean#normalize()} and
 *   {@link RousseauBean#wildExceptions()}.</p>
 * <p>Although this a persistence-related issue, this class only depends of the entities implementing
 *   {@link RousseauBean}, and thus can be used in circumstances where you do not wish to apply the
 *   {@code PersistentBean} interface. The listener methods do nothing for entities that not have type
 *   {@link RousseauBean}, so it is no problem to define the listener as the default entity listener for all inserts
 *   and updates. This can be done in the persistency unit definition as follows:</p>
 * <pre>
 *   &lt;entity-mappings&gt;
 *     &lt;entity-listeners&gt;
 *       &lt; entity-listeners class=&quot;org.ppwcode.vernacular.persistence_III.jpa.JpaRousseauBeanValidator&quot;&gt;
 *     &lt;/entity-listeners&gt;
 *   &lt;/entity-mappings&gt;
 * </pre>
 * <p>If you use entities that extend {@code AbstractPersistentBean}, this is not necessary, since that class defines
 *   the listener for itself and all subtypes.</p>
 * <p>In case validation fails ({@code ! }{@link CompoundPropertyException#isEmpty()}), the exception that expresses
 *   the validation problem is packaged into a {@link InternalTransportException}, because JPA entity listener methods
 *   are only allowed to throw {@link RuntimeException RuntimeExceptions}. When a listener does throw an exception,
 *   the current transaction is rolled-back, and all instances of session beans implicated in the current
 *   thread are discarded.</p>
 *
 * @note The problem with this approach is that, with multiple updates and inserts in 1 transaction, only the
 *       first validation that fails has the opportunity to express its woes.
 */
public class JpaRousseauBeanValidator {

  private final static Log _LOG = LogFactory.getLog(JpaRousseauBeanValidator.class);

  @PrePersist
  @PreUpdate
  @MethodContract(
    pre  = @Expression("entity != null"),
    post = {
      @Expression("entity.normalize()"),
      @Expression(value = "entity'civilized",
                  description = "if this bean is not civilized before the call, " +
                      "nothing can make this postcondition true, and thus an " +
                      "exception must be thrown")
    },
    exc = @Throw(type = InternalTransportException.class,
                 cond = {
                   @Expression("! entity'civilized"),
                   @Expression("thrown.cause != null"),
                   @Expression("thrown.cause instanceof CompoundPropertyException"),
                   @Expression("thrown.cause.like(wildExceptions)"),
                   @Expression("thrown.cause.closed")
                 })
  )
  public void validate(Object entity) throws InternalTransportException {
    _LOG.trace("pre-insert or -update called for " + entity);
    assert preArgumentNotNull(entity, "entity");
    try {
      RousseauBean rb = (RousseauBean)entity; // ClassCastException
      _LOG.trace("entity is a RousseauBean; we will normalize and then check civility");
      rb.normalize();
      rb.checkCivility(); // CompoundPropertyException
      _LOG.trace("entity is normalized and is civil; database change may proceed");
    }
    catch (ClassCastException ccExc) {
      // NOP don't touch non-rousseau bean entities
      _LOG.trace("entity is not a RousseauBean; no normalization or validation done");
    }
    catch (CompoundPropertyException wildExc) {
      _LOG.debug("entity is normalized, and found wild; the exception will be thrown, " +
                 "packaged in a InternalTransportException", wildExc);
      throw new InternalTransportException(wildExc);
    }
    finally {
      _LOG.trace("pre-insert or -update done for " + entity);
    }
  }

}

