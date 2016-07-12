/*<license>
Copyright 2005 - 2016 by PeopleWare n.v..

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

package org.ppwcode.vernacular.persistence.IV;


import org.ppwcode.vernacular.exception.IV.ApplicationException;


/**
 * Superclass that gathers internal exceptions that are specific for
 * persistence operations.
 *
 * @todo need to work on i18n messages
 *
 * @author    Jan Dockx
 * @author    David Van Keer
 * @author    PeopleWare n.v.
 */
@SuppressWarnings("ALL")
public class PersistenceException extends ApplicationException {

  /**
   * @param     messageKey
   *            The string that identifies a localized end user feedback message about the
   *            non-nominal behavior.
   * @param     cause
   *            The exception that occurred, causing this exception to be thrown, if that is
   *            the case.
   */
  /*
    @MethodContract(
      pre  = @Expression("_messageKey == null || _messageKey == EMPTY || validmessageKey(_messageKey)"),
      post = {
        @Expression("message == (_messageKey == null || _messageIdentfier == EMPTY) ? DEFAULT_MESSAGE_KEY : _messageKey"),
        @Expression("cause == _cause")
      }
    )
  */
  public PersistenceException(final String messageKey, final Throwable cause) {
    super(messageKey, cause);
  }

  /**
   * @param     messageKey
   *            The string that identifies a localized end user feedback message about the
   *            non-nominal behavior.
   */
  /*
    @MethodContract(
      pre  = @Expression("_messageKey == null || _messageKey == EMPTY || validmessageKey(_messageKey)"),
      post = {
        @Expression("message == (_messageKey == null || _messageIdentfier == EMPTY) ? DEFAULT_MESSAGE_KEY : _messageKey"),
        @Expression("cause == _cause")
      }
    )
  */
  public PersistenceException(final String messageKey) {
    this(messageKey, null);
  }

}

