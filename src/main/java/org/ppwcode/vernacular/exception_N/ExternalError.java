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
 * Superclass that gathers all external errors that can be thrown.
 * External errors carry a message intended for administrators in English,
 * which describes the error consisely. They never carry references to
 * semantic objects, to avoid keeping them alive longer then absolutely
 * required (they are probably in an undefined state). Any extra information
 * that the thrower wishes to signal has to be translated into a String
 * representation and added to the message.
 */
@Copyright("2004 - $Date$, PeopleWare n.v.")
@License(APACHE_V2)
@SvnInfo(revision = "$Revision$",
         date     = "$Date$")
public class ExternalError extends Error {

  @MethodContract(
    post = {
      @Expression("message == _message"),
      @Expression("cause == _cause")
    }
  )
  public ExternalError(String message, Throwable cause) {
    super(message, cause);
  }

  @MethodContract(
    post = {
      @Expression("message == _message"),
      @Expression("cause == null")
    }
  )
  public ExternalError(String message) {
    super(message);
  }

}

