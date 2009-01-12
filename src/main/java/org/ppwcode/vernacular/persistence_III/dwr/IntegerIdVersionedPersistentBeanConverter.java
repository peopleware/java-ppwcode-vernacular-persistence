package org.ppwcode.vernacular.persistence_III.dwr;

import static org.ppwcode.metainfo_I.License.Type.APACHE_V2;

import org.directwebremoting.extend.Property;
import org.directwebremoting.util.Logger;
import org.ppwcode.metainfo_I.Copyright;
import org.ppwcode.metainfo_I.License;
import org.ppwcode.metainfo_I.vcs.SvnInfo;

@Copyright("2004 - $Date$, PeopleWare n.v.")
@License(APACHE_V2)
@SvnInfo(revision = "$Revision$",
		date     = "$Date$")
/**
 * @author tmahieu
 * 
 * @mudo documentation etc.
 */
public class IntegerIdVersionedPersistentBeanConverter extends AbstractPersistentBeanConverter {

	@Override
	public Class<?> getPropertyType(String key, Property property) {
		Class<?> propType = null;
		if (key.equals("persistenceId") || key.equals("persistenceVersion")) {
			propType = java.lang.Integer.class;
		} else {
			propType = property.getPropertyType();
		}
		return propType;
	}

	/**
	 * The log stream
	 */
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(IntegerIdVersionedPersistentBeanConverter.class);

}
