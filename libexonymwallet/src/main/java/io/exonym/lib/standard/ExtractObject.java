package io.exonym.lib.standard;

import javax.xml.bind.JAXBElement;
import java.util.List;

public class ExtractObject {
	
	@SuppressWarnings({ "rawtypes", "unchecked"}) 
	public static <T> T extract(List<Object> list, Class clazz){
		for (Object o : list){
			if (o instanceof JAXBElement<?>){
				JAXBElement<?> element = (JAXBElement<?>)o;
				
				if (clazz.isInstance(element.getValue())){
					return (T)element.getValue();
					
				}
			} else if (clazz.isInstance(o)) {
				return (T)o;
				
			}
		}
		return null;
	}
}
