package org.ppwcode.vernacular.persistence_III.dwr;

import static org.ppwcode.util.exception_III.ExceptionHelpers.huntFor;

import java.util.Iterator;
import java.util.Map;

import org.directwebremoting.convert.BeanConverter;
import org.directwebremoting.dwrp.ParseUtil;
import org.directwebremoting.dwrp.ProtocolConstants;
import org.directwebremoting.extend.InboundContext;
import org.directwebremoting.extend.InboundVariable;
import org.directwebremoting.extend.MarshallException;
import org.directwebremoting.extend.Property;
import org.directwebremoting.extend.TypeHintContext;
import org.directwebremoting.util.LocalUtil;
import org.directwebremoting.util.Logger;
import org.directwebremoting.util.Messages;
import org.ppwcode.vernacular.semantics_VI.exception.CompoundPropertyException;
import org.ppwcode.vernacular.semantics_VI.exception.PropertyException;
import org.ppwcode.vernacular.value_III.ValueException;

/**
 * @author tmahieu
 *
 * @mudo documentation, etc.
 */
public abstract class AbstractPersistentBeanConverter extends BeanConverter {

	public abstract Class<?> getPropertyType(String key, Property property);

	/* (non-Javadoc)
	 * @see org.directwebremoting.Converter#convertInbound(java.lang.Class, org.directwebremoting.InboundVariable, org.directwebremoting.InboundContext)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Object convertInbound(Class paramType, InboundVariable iv, InboundContext inctx) throws MarshallException
	{
		String value = iv.getValue();

		// If the text is null then the whole bean is null
		if (value.trim().equals(ProtocolConstants.INBOUND_NULL)) {
			return null;
		}

		if (!value.startsWith(ProtocolConstants.INBOUND_MAP_START)) {
			throw new MarshallException(paramType, Messages.getString("BeanConverter.FormatError", ProtocolConstants.INBOUND_MAP_START));
		}

		if (!value.endsWith(ProtocolConstants.INBOUND_MAP_END)) {
			throw new MarshallException(paramType, Messages.getString("BeanConverter.FormatError", ProtocolConstants.INBOUND_MAP_START));
		}

		value = value.substring(1, value.length() - 1);

		try {
			Object bean;
			if (instanceType != null) {
				bean = instanceType.newInstance();
			} else {
				bean = paramType.newInstance();
			}

			// We should put the new object into the working map in case it
			// is referenced later nested down in the conversion process.
			if (instanceType != null) {
				inctx.addConverted(iv, instanceType, bean);
			}	else {
				inctx.addConverted(iv, paramType, bean);
			}

			Map properties = getPropertyMapFromObject(bean, false, true);

			// Loop through the properties passed in
			Map tokens = extractInboundTokens(paramType, value);
			CompoundPropertyException cpe = new CompoundPropertyException(bean, null, null, null);
			for (Iterator it = tokens.entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				String key = (String) entry.getKey();
				String val = (String) entry.getValue();

				Property property = (Property) properties.get(key);
				if (property == null) {
					log.warn("Missing java bean property to match javascript property: " + key + ". For causes see debug level logs:");

					log.debug("- The javascript may be refer to a property that does not exist");
					log.debug("- You may be missing the correct setter: set" + Character.toTitleCase(key.charAt(0)) + key.substring(1) + "()");
					log.debug("- The property may be excluded using include or exclude rules.");

					StringBuffer all = new StringBuffer();
					for (Iterator pit = properties.keySet().iterator(); pit.hasNext();) {
						all.append(pit.next());
						if (pit.hasNext()) {
							all.append(',');
						}
					}
					log.debug("Fields exist for (" + all + ").");
					continue;
				}

				Class propType = null;
				//Filthy hack to get the type right... The introspection mechanism
				//of DWR seems to have trouble with type erasure.  PPW Persistent Beans
				//have the type of the persistenceId and persistenceVersion templated
				propType = getPropertyType(key, property);

				String[] split = ParseUtil.splitInbound(val);
				String splitValue = split[LocalUtil.INBOUND_INDEX_VALUE];
				String splitType = split[LocalUtil.INBOUND_INDEX_TYPE];

				InboundVariable nested = new InboundVariable(iv.getLookup(), null, splitType, splitValue);
				TypeHintContext incc = createTypeHintContext(inctx, property);

				try {
					Object output = converterManager.convertInbound(propType, nested, inctx, incc);
					property.setValue(bean, output);
				} catch (MarshallException mex) {
					//collect all exceptions
					triageExceptions(bean, property, mex, cpe);
				} 
			}
			//throw the compound exception if it's not empty
			if (cpe.getSize() > 0) {
				cpe.close();
				throw cpe;
			}
			return bean;
		} catch (MarshallException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new MarshallException(paramType, ex);
		}
	}

	private void triageExceptions(Object bean,
			                          Property property,
			                          MarshallException subject,
			                          CompoundPropertyException compound) throws MarshallException {
		Exception e = null;
		// First check for ValueExceptions
		e = huntFor(subject, ValueException.class);
		if (e != null) {
			compound.addElementException(
					new PropertySerializationException(
							bean,
							property.getName(),
							e.getLocalizedMessage(), 
							e)); 
		} else {
			// No Value Exceptions found, check for CompoundPropertyExceptions
			e = huntFor(subject, CompoundPropertyException.class);
			if (e != null) {
				for ( Iterator<PropertyException> it = ((CompoundPropertyException)e).getElementExceptions().iterator();
				      it.hasNext();) {
					compound.addElementException(it.next());
				}
			} else { 
				// No CompoundPropertyExceptions, what about a PropertyException?
				e = huntFor(subject, PropertyException.class);
				if (e != null) {
					compound.addElementException((PropertyException)e);
				} else {
					// Hmm what to do what to do... throw a programming error?
					throw subject;
				}
      }
		}
      // TODO and else?
	}
	
	/**
	 * The log stream
	 */
	private static final Logger log = Logger.getLogger(AbstractPersistentBeanConverter.class);
}
