/*<license>
Copyright 2009 - $Date$ by PeopleWare n.v.

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

package org.ppwcode.vernacular.persistence_III.dwr;

import static org.ppwcode.metainfo_I.License.Type.APACHE_V2;
import static org.ppwcode.util.exception_III.ProgrammingErrorHelpers.preArgumentNotNull;

import org.ppwcode.metainfo_I.Copyright;
import org.ppwcode.metainfo_I.License;
import org.ppwcode.metainfo_I.vcs.SvnInfo;

import java.util.Locale;
import org.ppwcode.vernacular.l10n_III.I18nTemplateException;
import org.ppwcode.vernacular.l10n_III.LocalizedException;
import org.ppwcode.vernacular.semantics_VI.exception.PropertyException;

@Copyright("2009 - $Date$, PeopleWare n.v.")
@License(APACHE_V2)
@SvnInfo(revision = "$Revision$",
         date     = "$Date$")
public class PropertySerializationException extends PropertyException {

  public PropertySerializationException(Object origin, String propertyName, String message, Throwable cause) {
    super(origin, propertyName, message, cause);
  }


  /*<section name="getMessageTemplate">*/
  //------------------------------------------------------------------

  @Override
  public String getMessageTemplate(Locale locale) throws I18nTemplateException {
    assert preArgumentNotNull(locale, "locale");
    String result = null;

    if (getCause() instanceof LocalizedException) {
      // super class -> ApplicationException.getMessageTemplate
      result = ((LocalizedException)getCause()).getMessageTemplate(locale);
    } else {
      result = getCause().getLocalizedMessage();
    }

    return result;
  }

  /*</section>*/

}
