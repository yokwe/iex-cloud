package yokwe.iex.cloud;

import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import yokwe.iex.UnexpectedException;
import yokwe.iex.util.CSVUtil;
import yokwe.iex.util.GenericInfo;
import yokwe.iex.util.HttpUtil;
import yokwe.iex.util.Util;

public class Base {
	static final Logger logger = LoggerFactory.getLogger(Base.class);
	
	public static final LocalDateTime NULL_LOCAL_DATE_TIME = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);

	public static final String PATH_DIR = "tmp/iex";

	@Override
	public String toString() {
		try {
			ClassInfo classInfo = ClassInfo.get(this);

			List<String>  result = new ArrayList<>();
			StringBuilder line   = new StringBuilder();
			
			Object o = this;
			for(ClassInfo.FieldInfo fieldInfo: classInfo.fieldInfos) {
				line.setLength(0);
				line.append(fieldInfo.name).append(": ");
				
				switch(fieldInfo.type) {
				case "double":
					line.append(Double.toString(fieldInfo.field.getDouble(o)));
					break;
				case "float":
					line.append(fieldInfo.field.getFloat(o));
					break;
				case "long":
					line.append(fieldInfo.field.getLong(o));
					break;
				case "int":
					line.append(fieldInfo.field.getInt(o));
					break;
				case "short":
					line.append(fieldInfo.field.getShort(o));
					break;
				case "byte":
					line.append(fieldInfo.field.getByte(o));
					break;
				case "char":
					line.append(String.format("'%c'", fieldInfo.field.getChar(o)));
					break;
				default:
				{
					Object value = fieldInfo.field.get(o);
					if (value == null) {
						line.append("null");
					} else if (value instanceof String) {
						// Quote special character in string \ => \\  " => \"
						String stringValue = value.toString().replace("\\", "\\\\").replace("\"", "\\\"");
						line.append("\"").append(stringValue).append("\"");
					} else if (value instanceof LocalDateTime) {
						LocalDateTime  localDateTime  = (LocalDateTime)value;
						OffsetDateTime offsetDateTime = localDateTime.atOffset(ZoneOffset.UTC);
						
						String stringValue;
						if (fieldInfo.useTimeZone != null) {
							switch(fieldInfo.useTimeZone) {
							case UTC:
								stringValue = offsetDateTime.atZoneSameInstant(IEXCloud.UTC).toLocalDateTime().toString();
								break;
							case LOCAL:
								stringValue = offsetDateTime.atZoneSameInstant(IEXCloud.LOCAL).toLocalDateTime().toString();
								break;
							case NEW_YORK:
								stringValue = offsetDateTime.atZoneSameInstant(IEXCloud.NEW_YORK).toLocalDateTime().toString();
								break;
							default:
								logger.error("Unexptected useTimeZone value {}", fieldInfo.useTimeZone);
								throw new UnexpectedException("Unexptected useTimeZone value");
							}
						} else {
							logger.error("No useTimeZone annotation  {}.{}", classInfo.clazzName, fieldInfo.name);
							throw new UnexpectedException("No useTimeZone annotation");
						}
						line.append(stringValue);
					} else if (fieldInfo.isArray) {
						List<String> arrayElement = new ArrayList<>();
						int length = Array.getLength(value);
						for(int i = 0; i < length; i++) {
							Object element = Array.get(value, i);
							if (element instanceof String) {
								// Quote special character in string \ => \\  " => \"
								String stringValue = element.toString().replace("\\", "\\\\").replace("\"", "\\\"");
								arrayElement.add(String.format("\"%s\"", stringValue));
							} else {
								arrayElement.add(String.format("%s", element.toString()));
							}
						}						
						line.append("[").append(String.join(", ", arrayElement)).append("]");
					} else {
						line.append(value.toString());
					}
				}
					break;
				}
				result.add(line.toString());
			}
			
			return String.format("{%s}", String.join(", ", result));
		} catch (IllegalAccessException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}
	}

	protected Base() {
		//
	}
	
	protected Base(JsonObject jsonObject) {
		try {
			ClassInfo iexInfo = ClassInfo.get(this);
			for(ClassInfo.FieldInfo fieldInfo: iexInfo.fieldInfos) {
				// Skip field if name is not exist in jsonObject
				if (!jsonObject.containsKey(fieldInfo.jsonName))continue;
				
				ValueType valueType = jsonObject.get(fieldInfo.jsonName).getValueType();
				
//				logger.debug("parse {} {} {}", fieldInfo.name, valueType.toString(), fieldInfo.type);
				
				switch(valueType) {
				case NUMBER:
					setValue(fieldInfo, jsonObject.getJsonNumber(fieldInfo.jsonName));
					break;
				case STRING:
					setValue(fieldInfo, jsonObject.getJsonString(fieldInfo.jsonName));
					break;
				case TRUE:
					setValue(fieldInfo, true);
					break;
				case FALSE:
					setValue(fieldInfo, false);
					break;
				case NULL:
					setValue(fieldInfo);
					break;
				case OBJECT:
					setValue(fieldInfo, jsonObject.getJsonObject(fieldInfo.jsonName));
					break;
				case ARRAY:
					setValue(fieldInfo, jsonObject.getJsonArray(fieldInfo.jsonName));
					break;
				default:
					logger.error("Unknown valueType {} {}", valueType.toString(), fieldInfo.toString());
					throw new UnexpectedException("Unknown valueType");
				}
			}
			
			// Assign default value, if field value is null)
			for(ClassInfo.FieldInfo fieldInfo: iexInfo.fieldInfos) {
				Object o = fieldInfo.field.get(this);
				// If field is null, assign default value
				if (o == null) {
					if (!fieldInfo.ignoreField) {
						logger.warn("Assign defautl value  {} {} {}", iexInfo.clazzName, fieldInfo.name, fieldInfo.type);
					}
					setValue(fieldInfo);
				}
			}
		} catch (IllegalAccessException | InstantiationException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}
	}
	
	private void setValue(ClassInfo.FieldInfo fieldInfo, JsonNumber jsonNumber) throws IllegalArgumentException, IllegalAccessException {
		switch(fieldInfo.type) {
		case "double":
			fieldInfo.field.set(this, jsonNumber.doubleValue());
			break;
		case "long":
			fieldInfo.field.set(this, jsonNumber.longValue());
			break;
		case "int":
			fieldInfo.field.set(this, jsonNumber.intValue());
			break;
		case "java.math.BigDecimal":
			fieldInfo.field.set(this, jsonNumber.bigDecimalValue());
			break;
		case "java.lang.String":
			// To handle irregular case in Symbols, add this code. Value of iexId in Symbols can be number or String.
			fieldInfo.field.set(this, jsonNumber.toString());
			break;
		case "java.time.LocalDateTime":
			fieldInfo.field.set(this, Util.getLocalDateTimeFromMilli(jsonNumber.longValue()));
			break;
		default:
			logger.error("Unexptected type {}", toString());
			throw new UnexpectedException("Unexptected type");
		}
	}
	private void setValue(ClassInfo.FieldInfo fieldInfo, JsonString jsonString) throws IllegalArgumentException, IllegalAccessException {
		switch(fieldInfo.type) {
		case "java.lang.String":
			fieldInfo.field.set(this, jsonString.getString());
			break;
		case "double":
			fieldInfo.field.set(this, Double.valueOf((jsonString.getString().length() == 0) ? "0" : jsonString.getString()));
			break;
		case "long":
			fieldInfo.field.set(this, Long.valueOf((jsonString.getString().length() == 0) ? "0" : jsonString.getString()));
			break;
		case "int":
			fieldInfo.field.set(this, Integer.valueOf((jsonString.getString().length() == 0) ? "0" : jsonString.getString()));
			break;
		default:
			logger.error("Unexptected type {}", toString());
			throw new UnexpectedException("Unexptected type");
		}
	}
	private void setValue(ClassInfo.FieldInfo fieldInfo, boolean value) throws IllegalArgumentException, IllegalAccessException {
		switch(fieldInfo.type) {
		case "boolean":
			fieldInfo.field.set(this, value);
			break;
		default:
			logger.error("Unexptected type {}", toString());
			throw new UnexpectedException("Unexptected type");
		}
	}
	private void setValue(ClassInfo.FieldInfo fieldInfo) throws IllegalArgumentException, IllegalAccessException {
		switch(fieldInfo.type) {
		case "double":
			fieldInfo.field.set(this, 0);
			break;
		case "long":
			fieldInfo.field.set(this, 0);
			break;
		case "int":
			fieldInfo.field.set(this, 0);
			break;
		case "java.time.LocalDateTime":
			fieldInfo.field.set(this, NULL_LOCAL_DATE_TIME);
			break;
		case "java.lang.String":
			fieldInfo.field.set(this, "");
			break;
		default:
			logger.error("Unexptected type {}", toString());
			throw new UnexpectedException("Unexptected type");
		}
	}
	private void setValue(ClassInfo.FieldInfo fieldInfo, JsonArray jsonArray) throws IllegalArgumentException, IllegalAccessException {
		if (!fieldInfo.isArray) {
			logger.error("Field is not array  {}", toString());
			throw new UnexpectedException("Field is not array");
		}
		
		Class<?> componentType = fieldInfo.field.getType().getComponentType();
		String componentTypeName = componentType.getName();
		switch(componentTypeName) {
		case "java.lang.String":
		{
			int jsonArraySize = jsonArray.size();
			String[] value = new String[jsonArray.size()];
			
			for(int i = 0; i < jsonArraySize; i++) {
				JsonValue jsonValue = jsonArray.get(i);
				switch(jsonValue.getValueType()) {
				case STRING:
					value[i] = jsonArray.getString(i);
					break;
				default:
					logger.error("Unexpected json array element type {} {}", jsonValue.getValueType().toString(), toString());
					throw new UnexpectedException("Unexpected json array element type");
				}
			}
			fieldInfo.field.set(this, value);
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
		ClassInfo classInfo = ClassInfo.get(mapValueClass);
		
		Map<String, Base> ret = new TreeMap<>();
		
		for(String childKey: jsonObject.keySet()) {
			JsonValue childValue = jsonObject.get(childKey);
			ValueType childValueType = childValue.getValueType();
			
			switch(childValueType) {
			case OBJECT:
			{
				JsonObject jsonObjectValue = jsonObject.getJsonObject(childKey);
				Base value = classInfo.construcor.newInstance(jsonObjectValue);

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

	private void setValue(ClassInfo.FieldInfo fieldInfo, JsonObject jsonObject) throws IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException, SecurityException {
		Class<?> fieldType = fieldInfo.field.getType();
		
		if (Base.class.isAssignableFrom(fieldType)) {
			ClassInfo classInfo = ClassInfo.get(fieldType);
			Base child = classInfo.construcor.newInstance(jsonObject);
//			logger.info("child {}", child.toString());
			
			fieldInfo.field.set(this, child);
		} else {
			String fieldTypeName = fieldType.getName();
			switch(fieldTypeName) {
			case "java.util.Map":
			{
				GenericInfo genericInfo = new GenericInfo(fieldInfo.field);
				if (genericInfo.classArguments.length != 2) {
					logger.error("Unexptected genericInfo.classArguments.length {}", genericInfo.classArguments.length);
					throw new UnexpectedException("Unexptected genericInfo.classArguments.length");
				}
				Class<?> mapKeyClass   = genericInfo.classArguments[0];
				Class<?> mapValueClass = genericInfo.classArguments[1];
											
				String mapKeyClassName   = mapKeyClass.getTypeName();
				String mapValueClassName = mapValueClass.getTypeName();
				
//				logger.info("mapKeyClassName   {}", mapKeyClassName);
//				logger.info("mapValueClassName {}", mapValueClassName);
				
				if (!mapKeyClassName.equals("java.lang.String")) {
					logger.error("Unexptected keyTypeName {}", mapKeyClassName);
					throw new UnexpectedException("Unexptected keyTypeName");
				}
				
				switch(mapValueClassName) {
				case "java.lang.Long":
					fieldInfo.field.set(this, buildLongMap(jsonObject));
					break;
				case "java.lang.Integer":
					fieldInfo.field.set(this, buildIntegerMap(jsonObject));
					break;
				default:
					// If value extends from Base
					if (Base.class.isAssignableFrom(mapValueClass)) {
						fieldInfo.field.set(this, buildBaseMap(jsonObject, mapValueClass));
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

	// Date range used in Dividends
	public enum Range {
		Y5  ("5y"),   // Five years
		Y2  ("2y"),   // Two years
		Y1  ("1y"),   // One year
		YTD ("ytd"),  // Year to date
		M6  ("6m"),   // Six months
		M3  ("3m"),   // Three months
		M1  ("1m"),   // One month
		D5  ("5d"),   // Five days
		NEXT("next"); // The next upcoming dividend
		
		public final String value;
		Range(String value) {
			this.value = value;
		}
	}

	// Format
	public enum Format {
		JSON ("json"),  // Five years
		CSV  ("csv");   // Two years
		
		public final String value;
		Format(String value) {
			this.value = value;
		}
	}
	
	public static String encodeString(String symbol) {
		try {
			String ret = URLEncoder.encode(symbol, "UTF-8");
			return ret;
		} catch (UnsupportedEncodingException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}
	}

	public static String[] encodeSymbol(String[] symbols) {
		String[] ret = new String[symbols.length];
		for(int i = 0; i < symbols.length; i++) {
			ret[i] = encodeString(symbols[i]);
		}
		return ret;
	}



	public static <E extends Base> E getObject(String url, Class<E> clazz) {
		ClassInfo classInfo = ClassInfo.get(clazz);

		HttpUtil.Result result = HttpUtil.download(url);
		logger.info("tokenUsed {}", result.tokenUsed);
		String jsonString = result.result;
		if (jsonString == null) {
			logger.error("jsonString == null");
			throw new UnexpectedException("jsonString == null");
		}
//		logger.info("jsonString = {}", jsonString);
		
		try (JsonReader reader = Json.createReader(new StringReader(jsonString))) {
			// Assume result is only one object
			JsonObject arg = reader.readObject();
			@SuppressWarnings("unchecked")
			E ret = (E)classInfo.construcor.newInstance(arg);
			return ret;
		} catch (IllegalAccessException | InstantiationException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}
	}

	public static <E extends Base> List<E> getArray(String url, Class<E> clazz) {
		ClassInfo classInfo = ClassInfo.get(clazz);

		HttpUtil.Result result = HttpUtil.download(url);
		logger.info("tokenUsed {}", result.tokenUsed);
		String jsonString = result.result;
		if (jsonString == null) {
			logger.error("jsonString == null");
			throw new UnexpectedException("jsonString == null");
		}
//		logger.info("jsonString = {}", jsonString);
		
		try (JsonReader reader = Json.createReader(new StringReader(jsonString))) {
			// Assume result is array
			JsonArray jsonArray = reader.readArray();
			
			int jsonArraySize = jsonArray.size();
			@SuppressWarnings("unchecked")
			E[] ret = (E[])Array.newInstance(clazz, jsonArraySize);
			
			for(int i = 0; i < jsonArraySize; i++) {
				JsonObject arg = jsonArray.getJsonObject(i);
				@SuppressWarnings("unchecked")
				E e = (E)classInfo.construcor.newInstance(arg);
				ret[i] = e;
			}
			
			// Sort array
			Arrays.sort(ret);
			
			// Return as list
			return Arrays.asList(ret);
		} catch (IllegalAccessException | InstantiationException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}
	}

	// for symbols
	public static <E extends Base> List<E> getCSV(String url, Class<E> clazz) {
		HttpUtil.Result result = HttpUtil.download(url);
		logger.info("tokenUsed {}", result.tokenUsed);
		String csvString = result.result;
		if (csvString == null) {
			logger.error("csvString == null");
			throw new UnexpectedException("csvString == null");
		}
		Reader reader = new StringReader(csvString);

		List<E> list = CSVUtil.loadWithHeader(reader, clazz);
		
		@SuppressWarnings("unchecked")
		E[] ret = (E[])Array.newInstance(clazz, list.size());
		for(int i = 0; i < ret.length; i++) {
			ret[i] = list.get(i);
		}
		
		// Sort array
		Arrays.sort(ret);
		
		// Return as list
		return Arrays.asList(ret);
	}

	
	
	// CSV Save and Load
	public static void saveCSV(List<Base> dataList) {
		Base o = dataList.get(0);
		ClassInfo classInfo = ClassInfo.get(o);
		
		if (classInfo.path == null) {
			logger.error("classInfo.path == null  {}", classInfo);
			throw new UnexpectedException("classInfo.path == null");
		}
		
		String path = String.format("%s%s", PATH_DIR, classInfo.path);
//		logger.info("path = {}", path);

		CSVUtil.saveWithHeader(dataList, path);
	}
	public static List<Base> loadCSV(Class<Base> clazz) {
		ClassInfo classInfo = ClassInfo.get(clazz);
		
		if (classInfo.path == null) {
			logger.error("classInfo.path == null  {}", classInfo);
			throw new UnexpectedException("classInfo.path == null");
		}
		
		String path = String.format("%s%s", PATH_DIR, classInfo.path);
//		logger.info("path = {}", path);
		
		return CSVUtil.loadWithHeader(path, clazz);
	}
}
