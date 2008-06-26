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


package org.ppwcode.vernacular.exception_N;


import static org.ppwcode.metainfo_I.License.Type.APACHE_V2;

import org.ppwcode.metainfo_I.Copyright;
import org.ppwcode.metainfo_I.License;
import org.ppwcode.metainfo_I.vcs.SvnInfo;
import org.toryt.annotations_I.Expression;
import org.toryt.annotations_I.MethodContract;


/**
 * Superclass that gathers all internal exceptions that can be thrown.
 * Internal exceptions carry as much information about the exceptional
 * occurence as possible, which can be used higher up to display a meaningful
 * message to the end user.
 */
@Copyright("2004 - $Date$, PeopleWare n.v.")
@License(APACHE_V2)
@SvnInfo(revision = "$Revision$",
         date     = "$Date$")
public class InternalException extends Error {

  @MethodContract(
    post = {
      @Expression("message == _message"),
      @Expression("cause == _cause")
    }
  )
  public InternalException(String message, Throwable cause) {
    super(message, cause);
  }

  @MethodContract(
    post = {
      @Expression("message == _message"),
      @Expression("cause == null")
    }
  )
  public InternalException(String message) {
    super(message);
  }

}

