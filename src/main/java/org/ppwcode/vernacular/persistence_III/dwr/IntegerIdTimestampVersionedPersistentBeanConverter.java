package org.ppwcode.vernacular.persistence_III.dwr;

import static org.ppwcode.metainfo_I.License.Type.APACHE_V2;

import org.directwebremoting.util.Logger;

import org.directwebremoting.extend.Property;
import org.ppwcode.metainfo_I.Copyright;
import org.ppwcode.metainfo_I.License;
import org.ppwcode.metainfo_I.vcs.SvnInfo;

/**
 *
 * @author rvdginste
 *
 * @mudo documentation etc.
 */
@Copyright("2004 - $Date$, PeopleWare n.v.")
@License(APACHE_V2)
@SvnInfo(revision = "$Revision$",
         date     = "$Date$")
public class IntegerIdTimestampVersionedPersistentBeanConverter extends AbstractPersistentBeanConverter {

	@Override
	public Class<?> getPropertyType(String key, Property property) {
		Class<?> propType = null;
		if (key.equals("persistenceId")) {
			propType = java.lang.Integer.class;
		} else if (key.equals("persistenceVersion")) {
			propType = java.sql.Timestamp.class;
		} else {
			propType = property.getPropertyType();
		}
		return propType;
	}

	/**
	 * The log stream
	 */
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(IntegerIdTimestampVersionedPersistentBeanConverter.class);

}
