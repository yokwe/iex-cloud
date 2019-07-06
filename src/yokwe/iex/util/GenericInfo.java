package yokwe.iex.util;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import yokwe.iex.UnexpectedException;

//
// Generic
//
public class GenericInfo {
	public final Class<?>   rawClass;
	public final Class<?>[] classArguments;
	
	public GenericInfo(Field field) {
		Type genericType = field.getGenericType();
		if (genericType instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType)genericType;
			java.lang.reflect.Type rawType = parameterizedType.getRawType();
			Type[] typeArguments = parameterizedType.getActualTypeArguments();

			if (rawType instanceof Class) {
				rawClass = (Class<?>)rawType;
			} else {
				Util.logger.error("Unexpected rawType  {}", rawType.getClass().getName());
				throw new UnexpectedException("Unexpected rawType");
			}

			classArguments = new Class<?>[typeArguments.length];
			for(int i = 0; i < typeArguments.length; i++) {
				Type typeArgument = typeArguments[i];
				if (typeArgument instanceof Class) {
					classArguments[i] = (Class<?>)typeArgument;
				} else {
					Util.logger.error("Unexpected typeArgument  {}", typeArgument.getClass().getName());
					throw new UnexpectedException("Unexpected typeArgument");
				}
			}
		} else {
			Util.logger.error("Unexpected fieldGenericType  {}", genericType.getClass().getName());
			throw new UnexpectedException("Unexpected genericType");
		}
	}
}