package org.ppwcode.vernacular.persistence_III.dwr;

import java.util.Iterator;
import java.util.Map;
import org.directwebremoting.util.Logger;

import org.directwebremoting.convert.BeanConverter;
import org.directwebremoting.dwrp.ParseUtil;
import org.directwebremoting.dwrp.ProtocolConstants;
import org.directwebremoting.extend.InboundContext;
import org.directwebremoting.extend.InboundVariable;
import org.directwebremoting.extend.MarshallException;
import org.directwebremoting.extend.Property;
import org.directwebremoting.extend.TypeHintContext;
import org.directwebremoting.util.LocalUtil;
import org.directwebremoting.util.Messages;

/**
 *
 * @author rvdginste
 *
 * @mudo documentation etc.
 */
public class IntegerIdTimestampVersionedPersistentBeanConverter extends BeanConverter {

  /* (non-Javadoc)
   * @see org.directwebremoting.Converter#convertInbound(java.lang.Class, org.directwebremoting.InboundVariable, org.directwebremoting.InboundContext)
   */
  @Override
  @SuppressWarnings("unchecked")
  public Object convertInbound(Class paramType, InboundVariable iv, InboundContext inctx) throws MarshallException
  {
    String value = iv.getValue();

    // If the text is null then the whole bean is null
    if (value.trim().equals(ProtocolConstants.INBOUND_NULL))
    {
      return null;
    }

    if (!value.startsWith(ProtocolConstants.INBOUND_MAP_START))
    {
      throw new MarshallException(paramType, Messages.getString("BeanConverter.FormatError", ProtocolConstants.INBOUND_MAP_START));
    }

    if (!value.endsWith(ProtocolConstants.INBOUND_MAP_END))
    {
      throw new MarshallException(paramType, Messages.getString("BeanConverter.FormatError", ProtocolConstants.INBOUND_MAP_START));
    }

    value = value.substring(1, value.length() - 1);

    try
    {
      Object bean;
      if (instanceType != null)
      {
        bean = instanceType.newInstance();
      }
      else
      {
        bean = paramType.newInstance();
      }

      // We should put the new object into the working map in case it
      // is referenced later nested down in the conversion process.
      if (instanceType != null)
      {
        inctx.addConverted(iv, instanceType, bean);
      }
      else
      {
        inctx.addConverted(iv, paramType, bean);
      }

      Map properties = getPropertyMapFromObject(bean, false, true);

      // Loop through the properties passed in
      Map tokens = extractInboundTokens(paramType, value);
      for (Iterator it = tokens.entrySet().iterator(); it.hasNext();)
      {
        Map.Entry entry = (Map.Entry) it.next();
        String key = (String) entry.getKey();
        String val = (String) entry.getValue();

        Property property = (Property) properties.get(key);
        if (property == null)
        {
          log.warn("Missing java bean property to match javascript property: " + key + ". For causes see debug level logs:");

          log.debug("- The javascript may be refer to a property that does not exist");
          log.debug("- You may be missing the correct setter: set" + Character.toTitleCase(key.charAt(0)) + key.substring(1) + "()");
          log.debug("- The property may be excluded using include or exclude rules.");

          StringBuffer all = new StringBuffer();
          for (Iterator pit = properties.keySet().iterator(); pit.hasNext();)
          {
            all.append(pit.next());
            if (pit.hasNext())
            {
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
        if (key.equals("persistenceId")) {
          propType = java.lang.Integer.class;
        } else if (key.equals("persistenceVersion")) {
          propType = java.sql.Timestamp.class;
        } else {
          propType = property.getPropertyType();
        }

        String[] split = ParseUtil.splitInbound(val);
        String splitValue = split[LocalUtil.INBOUND_INDEX_VALUE];
        String splitType = split[LocalUtil.INBOUND_INDEX_TYPE];

        InboundVariable nested = new InboundVariable(iv.getLookup(), null, splitType, splitValue);
        TypeHintContext incc = createTypeHintContext(inctx, property);

        Object output = converterManager.convertInbound(propType, nested, inctx, incc);
        property.setValue(bean, output);
      }

      return bean;
    }
    catch (MarshallException ex)
    {
      throw ex;
    }
    catch (Exception ex)
    {
      throw new MarshallException(paramType, ex);
    }
  }

  /**
   * The log stream
   */
  private static final Logger log = Logger.getLogger(IntegerIdTimestampVersionedPersistentBeanConverter.class);

}
