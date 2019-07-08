package yokwe.iex.cloud;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import yokwe.iex.UnexpectedException;
import yokwe.iex.cloud.IEXCloud.IgnoreField;
import yokwe.iex.cloud.IEXCloud.JSONName;
import yokwe.iex.cloud.IEXCloud.TimeZone;
import yokwe.iex.cloud.IEXCloud.UseTimeZone;


public class ClassInfo {
	static final Logger logger = LoggerFactory.getLogger(ClassInfo.class);

	private static Map<String, ClassInfo> map = new TreeMap<>();
	
	public static class FieldInfo {
		public final Field    field;
		public final String   name;
		public final String   jsonName;
		public final String   type;
		public final boolean  isArray;
		public final boolean  ignoreField;
		public final TimeZone useTimeZone;
		
		FieldInfo(Field field) {
			this.field = field;
			
			this.name  = field.getName();

			// Use JSONName if exists.
			JSONName jsonName = field.getDeclaredAnnotation(JSONName.class);
			this.jsonName = (jsonName == null) ? field.getName() : jsonName.value();
			
			Class<?> type = field.getType();
			this.type     = type.getName();
			this.isArray  = type.isArray();
			
			this.ignoreField = field.getDeclaredAnnotation(IgnoreField.class) != null;
			
			UseTimeZone useTimeZone = field.getDeclaredAnnotation(UseTimeZone.class);
			if (useTimeZone != null) {
				this.useTimeZone = useTimeZone.value();
			} else {
				this.useTimeZone = null;
			}
		}
		
		@Override
		public String toString() {
			return String.format("{%s %s %s %s}", name, type, isArray, ignoreField);
		}
	}
	
	public static ClassInfo get(Base o) {
		return get(o.getClass());
	}
	public static ClassInfo get(Class<?> clazz) {
		if (Base.class.isAssignableFrom(clazz)) {
			@SuppressWarnings("unchecked")
			Class<Base> clazzBase = (Class<Base>)clazz;

			String key = clazzBase.getName();
			
			if (map.containsKey(key)) return map.get(key);
			
			ClassInfo value = new ClassInfo(clazzBase);
			map.put(key, value);
			return value;
		} else {
			logger.error("Unexpected clazz {}", clazz.getClass().getName());
			throw new UnexpectedException("Unexpected clazz");
		}
	}

	public final String            clazzName;
	public final String            method;
	public final String            path;
	public final FieldInfo[]       fieldInfos;
	public final int               fieldSize;
	public final String            filter;
	public final Constructor<Base> construcor;
	
	private static Field getField(Class<?> clazz, String fieldName) {
		try {
			return clazz.getDeclaredField(fieldName);
		} catch (NoSuchFieldException e) {
			return null;
		}
	}
	
	ClassInfo(Class<Base> clazz) {
		try {
			List<Field> fieldList = new ArrayList<>();
			for(Field field: clazz.getDeclaredFields()) {
				// Skip static field
				if (Modifier.isStatic(field.getModifiers())) continue;
				fieldList.add(field);
			}
			FieldInfo[] fieldInfos = new FieldInfo[fieldList.size()];
			for(int i = 0; i < fieldInfos.length; i++) {
				fieldInfos[i] = new FieldInfo(fieldList.get(i));
			}
			
			int fieldSize = 0;
			{
				for(int i = 0; i < fieldInfos.length; i++) {
					if (fieldInfos[i].ignoreField) continue;
					fieldSize++;
				}
			}
			
			String method;
			{
				Field field = getField(clazz, "METHOD");
				if (field != null) {
					if (!Modifier.isStatic(field.getModifiers())) {
						logger.error("METHOD field is not static {}", clazz.getName());
						throw new UnexpectedException("METHOD field is not static");
					}
					String fieldTypeName = field.getType().getName();
					if (!fieldTypeName.equals("java.lang.String")) {
						logger.error("Unexpected fieldTypeName {}", fieldTypeName);
						throw new UnexpectedException("Unexpected fieldTypeName");
					}
					Object value = field.get(null);
					if (value instanceof String) {
						method = (String)value;
					} else {
						logger.error("Unexpected value {}", value.getClass().getName());
						throw new UnexpectedException("Unexpected value");
					}
				} else {
					method = null;
				}
			}

			String path;
			{
				Field field = getField(clazz, "PATH");
				if (field != null) {
					if (!Modifier.isStatic(field.getModifiers())) {
						logger.error("PATH field is not static {}", clazz.getName());
						throw new UnexpectedException("PATH field is not static");
					}
					String fieldTypeName = field.getType().getName();
					if (!fieldTypeName.equals("java.lang.String")) {
						logger.error("Unexpected fieldTypeName {}", fieldTypeName);
						throw new UnexpectedException("Unexpected fieldTypeName");
					}
					Object value = field.get(null);
					if (value instanceof String) {
						path = (String)value;
					} else {
						logger.error("Unexpected value {}", value.getClass().getName());
						throw new UnexpectedException("Unexpected value");
					}
				} else {
					path = null;
				}
			}
			
			String filter;
			{
				List<String>filterList = new ArrayList<>();
				for(ClassInfo.FieldInfo fiedlInfo: fieldInfos) {
					if (fiedlInfo.ignoreField) continue;
					filterList.add(fiedlInfo.jsonName);
				}
				filter = String.join(",", filterList.toArray(new String[0]));
			}

			this.clazzName  = clazz.getName();
			this.method     = method;
			this.path       = path;
			this.fieldInfos = fieldInfos;
			this.fieldSize  = fieldSize;
			this.filter     = filter;
			this.construcor = clazz.getDeclaredConstructor(JsonObject.class);
		} catch (SecurityException | IllegalArgumentException | IllegalAccessException | NoSuchMethodException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}
	}
	
	@Override
	public String toString() {
		return String.format("%s  %s  %s  %s", clazzName, method, path, Arrays.asList(this.fieldInfos));
	}
}
