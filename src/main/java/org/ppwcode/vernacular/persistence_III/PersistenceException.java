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

package org.ppwcode.vernacular.persistence_III;


import static org.ppwcode.metainfo_I.License.Type.APACHE_V2;

import org.ppwcode.metainfo_I.Copyright;
import org.ppwcode.metainfo_I.License;
import org.ppwcode.metainfo_I.vcs.SvnInfo;
import org.ppwcode.vernacular.exception_II.InternalException;
import org.toryt.annotations_I.Expression;
import org.toryt.annotations_I.MethodContract;


/**
 * Superclass that gathers internal exceptions that are specific for
 * persistence operations.
 *
 * @mudo need to work on i18n messages
 *
 * @author    Jan Dockx
 * @author    David Van Keer
 * @author    PeopleWare n.v.
 */
@Copyright("2004 - $Date$, PeopleWare n.v.")
@License(APACHE_V2)
@SvnInfo(revision = "$Revision$",
         date     = "$Date$")
public class PersistenceException extends InternalException {

  /**
   * @param     messageKey
   *            The string that identifies a localized end user feedback message about the
   *            non-nominal behavior.
   * @param     cause
   *            The exception that occured, causing this exception to be thrown, if that is
   *            the case.
   */
  @MethodContract(
    pre  = @Expression("_messageKey == null || _messageKey == EMPTY || validmessageKey(_messageKey)"),
    post = {
      @Expression("message == (_messageKey == null || _messageIdentfier == EMPTY) ? DEFAULT_MESSAGE_KEY : _messageKey"),
      @Expression("cause == _cause")
    }
  )
  public PersistenceException(final String messageKey, final Throwable cause) {
    super(messageKey, cause);
  }

  /**
   * @param     messageKey
   *            The string that identifies a localized end user feedback message about the
   *            non-nominal behavior.
   * @param     cause
   *            The exception that occured, causing this exception to be thrown, if that is
   *            the case.
   */
  @MethodContract(
    pre  = @Expression("_messageKey == null || _messageKey == EMPTY || validmessageKey(_messageKey)"),
    post = {
      @Expression("message == (_messageKey == null || _messageIdentfier == EMPTY) ? DEFAULT_MESSAGE_KEY : _messageKey"),
      @Expression("cause == _cause")
    }
  )
  public PersistenceException(final String messageKey) {
    this(messageKey, null);
  }

}

