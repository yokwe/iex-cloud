package yokwe.iex.cloud;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import yokwe.iex.UnexpectedException;
import yokwe.iex.cloud.IEXCloud.IgnoreField;
import yokwe.iex.cloud.IEXCloud.JSONName;
import yokwe.iex.cloud.IEXCloud.TimeZone;
import yokwe.iex.cloud.IEXCloud.UseTimeZone;
import yokwe.iex.util.GenericInfo;
import yokwe.iex.util.Util;


public class ClassInfo {
	static final Logger logger = LoggerFactory.getLogger(ClassInfo.class);

	public static final LocalDateTime NULL_LOCAL_DATE_TIME = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);

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
		
		public void setValue(Base base, JsonNumber jsonNumber) throws IllegalArgumentException, IllegalAccessException {
			switch(type) {
			case "double":
				field.set(base, jsonNumber.doubleValue());
				break;
			case "long":
				field.set(base, jsonNumber.longValue());
				break;
			case "int":
				field.set(base, jsonNumber.intValue());
				break;
			case "java.math.BigDecimal":
				field.set(base, jsonNumber.bigDecimalValue());
				break;
			case "java.lang.String":
				// To handle irregular case in Symbols, add this code. Value of iexId in Symbols can be number or String.
				field.set(base, jsonNumber.toString());
				break;
			case "java.time.LocalDateTime":
				field.set(base, Util.getLocalDateTimeFromMilli(jsonNumber.longValue()));
				break;
			default:
				logger.error("Unexptected type {}", toString());
				throw new UnexpectedException("Unexptected type");
			}
		}
		public void setValue(Base base, JsonString jsonString) throws IllegalArgumentException, IllegalAccessException {
			switch(type) {
			case "java.lang.String":
				field.set(base, jsonString.getString());
				break;
			case "double":
				field.set(base, Double.valueOf((jsonString.getString().length() == 0) ? "0" : jsonString.getString()));
				break;
			case "long":
				field.set(base, Long.valueOf((jsonString.getString().length() == 0) ? "0" : jsonString.getString()));
				break;
			case "int":
				field.set(base, Integer.valueOf((jsonString.getString().length() == 0) ? "0" : jsonString.getString()));
				break;
			default:
				logger.error("Unexptected type {}", toString());
				throw new UnexpectedException("Unexptected type");
			}
		}
		public void setValue(Base base, boolean value) throws IllegalArgumentException, IllegalAccessException {
			switch(type) {
			case "boolean":
				field.set(base, value);
				break;
			default:
				logger.error("Unexptected type {}", toString());
				throw new UnexpectedException("Unexptected type");
			}
		}
		public void setValue(Base base) throws IllegalArgumentException, IllegalAccessException {
			switch(type) {
			case "double":
				field.set(base, 0);
				break;
			case "long":
				field.set(base, 0);
				break;
			case "int":
				field.set(base, 0);
				break;
			case "java.time.LocalDateTime":
				field.set(base, NULL_LOCAL_DATE_TIME);
				break;
			case "java.lang.String":
				field.set(base, "");
				break;
			default:
				logger.error("Unexptected type {}", toString());
				throw new UnexpectedException("Unexptected type");
			}
		}
		public void setValue(Base base, JsonArray jsonArray) throws IllegalArgumentException, IllegalAccessException {
			if (!isArray) {
				logger.error("Field is not array  {}", toString());
				throw new UnexpectedException("Field is not array");
			}
			
			Class<?> componentType = field.getType().getComponentType();
			String componentTypeName = componentType.getName();
			switch(componentTypeName) {
			case "java.lang.String":
			{
				int jsonArraySize = jsonArray.size();
				String[] value = new String[jsonArray.size()];
				
				for(int j = 0; j < jsonArraySize; j++) {
					JsonValue jsonValue = jsonArray.get(j);
					switch(jsonValue.getValueType()) {
					case STRING:
						value[j] = jsonArray.getString(j);
						break;
					default:
						logger.error("Unexpected json array element type {} {}", jsonValue.getValueType().toString(), toString());
						throw new UnexpectedException("Unexpected json array element type");
					}
				}
				field.set(base, value);
			}
				break;
			default:
				logger.error("Unexpected array component type {} {}", componentTypeName, toString());
				throw new UnexpectedException("Unexpected array component type");
			}
		}
		
		private static Map<String, Long> buildLongMap(JsonObject jsonObject) {
			Map<String, Long> ret = new TreeMap<>();
			
			for(String childKey: jsonObject.keySet()) {
				JsonValue childValue = jsonObject.get(childKey);
				ValueType childValueType = childValue.getValueType();
				
				switch(childValueType) {
				case NUMBER:
				{
					JsonNumber jsonNumber = jsonObject.getJsonNumber(childKey);
					long value = jsonNumber.longValue();
					ret.put(childKey, value);
				}
					break;
				case STRING:
				{
					JsonString jsonString = jsonObject.getJsonString(childKey);
					long value = Long.valueOf((jsonString.getString().length() == 0) ? "0" : jsonString.getString());
					ret.put(childKey, value);
				}
					break;
				default:
					logger.error("Unexptected childValueType {}", childValueType);
					throw new UnexpectedException("Unexptected childValueType");
				}
			}
			return ret;
		}
		private static Map<String, Integer> buildIntegerMap(JsonObject jsonObject) {
			Map<String, Integer> ret = new TreeMap<>();
			
			for(String childKey: jsonObject.keySet()) {
				JsonValue childValue = jsonObject.get(childKey);
				ValueType childValueType = childValue.getValueType();
				
				switch(childValueType) {
				case NUMBER:
				{
					JsonNumber jsonNumber = jsonObject.getJsonNumber(childKey);
					int value = jsonNumber.intValue();
					ret.put(childKey, value);
				}
					break;
				case STRING:
				{
					JsonString jsonString = jsonObject.getJsonString(childKey);
					int value = Integer.valueOf((jsonString.getString().length() == 0) ? "0" : jsonString.getString());
					ret.put(childKey, value);
				}
					break;
				default:
					logger.error("Unexptected childValueType {}", childValueType);
					throw new UnexpectedException("Unexptected childValueType");
				}
			}
			return ret;
		}
		private static Map<String, Base> buildBaseMap(JsonObject jsonObject, Class<?> mapValueClass) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
			Map<String, Base> ret = new TreeMap<>();
			
			for(String childKey: jsonObject.keySet()) {
				JsonValue childValue = jsonObject.get(childKey);
				ValueType childValueType = childValue.getValueType();
				
				switch(childValueType) {
				case OBJECT:
				{
					JsonObject jsonObjectValue = jsonObject.getJsonObject(childKey);
					Base value = (Base)mapValueClass.getDeclaredConstructor(JsonObject.class).newInstance(jsonObjectValue);

					ret.put(childKey, value);
				}
					break;
				default:
					logger.error("Unexptected childValueType {}", childValueType);
					throw new UnexpectedException("Unexptected childValueType");
				}
			}
			return ret;
		}

		public void setValue(Base base, JsonObject jsonObject) throws IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException, SecurityException {
			Class<?> fieldType = field.getType();
			
			if (Base.class.isAssignableFrom(fieldType)) {
				Base child = (Base)fieldType.getDeclaredConstructor(JsonObject.class).newInstance(jsonObject);
//				logger.info("child {}", child.toString());
				
				field.set(base, child);
			} else {
				String fieldTypeName = fieldType.getName();
				switch(fieldTypeName) {
				case "java.util.Map":
				{
					GenericInfo genericInfo = new GenericInfo(field);
					if (genericInfo.classArguments.length != 2) {
						logger.error("Unexptected genericInfo.classArguments.length {}", genericInfo.classArguments.length);
						throw new UnexpectedException("Unexptected genericInfo.classArguments.length");
					}
					Class<?> mapKeyClass   = genericInfo.classArguments[0];
					Class<?> mapValueClass = genericInfo.classArguments[1];
												
					String mapKeyClassName   = mapKeyClass.getTypeName();
					String mapValueClassName = mapValueClass.getTypeName();
					
//					logger.info("mapKeyClassName   {}", mapKeyClassName);
//					logger.info("mapValueClassName {}", mapValueClassName);
					
					if (!mapKeyClassName.equals("java.lang.String")) {
						logger.error("Unexptected keyTypeName {}", mapKeyClassName);
						throw new UnexpectedException("Unexptected keyTypeName");
					}
					
					switch(mapValueClassName) {
					case "java.lang.Long":
						field.set(base, buildLongMap(jsonObject));
						break;
					case "java.lang.Integer":
						field.set(base, buildIntegerMap(jsonObject));
						break;
					default:
						// If value extends from Base
						if (Base.class.isAssignableFrom(mapValueClass)) {
							field.set(base, buildBaseMap(jsonObject, mapValueClass));
						} else {
							logger.error("Unexptected keyTypeName {}", mapKeyClassName);
							throw new UnexpectedException("Unexptected keyTypeName");
						}
					}
				}
					break;
				default:
					logger.error("Unexptected type {}", toString());
					logger.error("fieldTypeName {}", fieldTypeName);
					throw new UnexpectedException("Unexptected type");
				}
			}
		}
	}
	
	public static ClassInfo get(Base o) {
		return get(o.getClass());
	}
	public static ClassInfo get(Class<? extends Base> clazz) {
		String key = clazz.getName();
		
		if (map.containsKey(key)) return map.get(key);
		
		ClassInfo value = new ClassInfo(clazz);
		map.put(key, value);
		return value;
	}

	public final String      clazzName;
	public final String      method;
	public final String      path;
	public final FieldInfo[] fieldInfos;
	public final int         fieldSize;
	public final String      filter;
	
	private static Field getField(Class<?> clazz, String fieldName) {
		try {
			return clazz.getDeclaredField(fieldName);
		} catch (NoSuchFieldException e) {
			return null;
		}
	}
	
	ClassInfo(Class<? extends Base> clazz) {
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
		} catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
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
