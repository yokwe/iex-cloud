package yokwe.iex.cloud;

import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
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
				
//				logger.debug("parse {} {} {}", name, valueType.toString(), type);
				
				switch(valueType) {
				case NUMBER:
				{
					JsonNumber jsonNumber = jsonObject.getJsonNumber(fieldInfo.jsonName);
					
					switch(fieldInfo.type) {
					case "double":
						fieldInfo.field.set(this, jsonNumber.doubleValue());
						break;
					case "long":
						fieldInfo.field.set(this, jsonNumber.longValue());
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
						logger.error("Unexptected type {} {}", valueType.toString(), fieldInfo.toString());
						throw new UnexpectedException("Unexptected type");
					}
				}
					break;
				case STRING:
				{
					JsonString jsonString = jsonObject.getJsonString(fieldInfo.jsonName);
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
						logger.error("Unexptected type {} {}", valueType.toString(), fieldInfo.toString());
						throw new UnexpectedException("Unexptected type");
					}
				}
					break;
				case TRUE:
				{
					switch(fieldInfo.type) {
					case "boolean":
						fieldInfo.field.set(this, true);
						break;
					default:
						logger.error("Unexptected type {} {}", valueType.toString(), fieldInfo.toString());
						throw new UnexpectedException("Unexptected type");
					}
				}
					break;
				case FALSE:
				{
					switch(fieldInfo.type) {
					case "boolean":
						fieldInfo.field.set(this, false);
						break;
					default:
						logger.error("Unexptected type {} {}", valueType.toString(), fieldInfo.toString());
						throw new UnexpectedException("Unexptected type");
					}
				}
					break;
				case NULL:
				{
					switch(fieldInfo.type) {
					case "double":
						fieldInfo.field.set(this, 0);
						break;
					case "long":
						fieldInfo.field.set(this, 0);
						break;
					case "java.time.LocalDateTime":
						fieldInfo.field.set(this, NULL_LOCAL_DATE_TIME);
						break;
					case "java.lang.String":
						fieldInfo.field.set(this, "");
						break;
					default:
						logger.error("Unexptected type {} {}", valueType.toString(), fieldInfo.toString());
						throw new UnexpectedException("Unexptected type");
					}
				}
					break;
				case OBJECT:
				{
					Class<?> fieldType = fieldInfo.field.getType();
					
					if (Base.class.isAssignableFrom(fieldType)) {
						JsonObject childJson = jsonObject.get(fieldInfo.jsonName).asJsonObject();
//						logger.info("childJson {}", childJson.toString());
						
						Base child = (Base)fieldType.getDeclaredConstructor(JsonObject.class).newInstance(childJson);
//						logger.info("child {}", child.toString());
						
						fieldInfo.field.set(this, child);
					} else {
						String fieldTypeName = fieldType.getName();
						switch(fieldTypeName) {
						case "java.util.Map":
						{
							java.lang.reflect.Type type = fieldInfo.field.getGenericType();
							if (type instanceof ParameterizedType) {
								ParameterizedType parameterizedType = (ParameterizedType)type;
								
								java.lang.reflect.Type[] types = parameterizedType.getActualTypeArguments();
								if (types.length != 2) {
									logger.error("Unexptected types.length {}", types.length);
									throw new UnexpectedException("Unexpected types.length");
								}

								String keyTypeName   = types[0].getTypeName();
								String valueTypeName = types[1].getTypeName();
								
//								logger.info("keyTypeName   {}", keyTypeName);
//								logger.info("valueTypeName {}", valueTypeName);
								
								if (!keyTypeName.equals("java.lang.String")) {
									logger.error("Unexptected keyTypeName {}", keyTypeName);
									throw new UnexpectedException("Unexptected keyTypeName");
								}
								
								switch(valueTypeName) {
								case "java.lang.Long":
								{
									Map<String, Long> child = new TreeMap<>();
									JsonObject childJson = jsonObject.get(fieldInfo.jsonName).asJsonObject();
									for(String childKey: childJson.keySet()) {
										JsonValue childValue = childJson.get(childKey);
										ValueType childValueType = childValue.getValueType();
										
										switch(childValueType) {
										case NUMBER:
										{
											JsonNumber jsonNumber = childJson.getJsonNumber(childKey);
											long value = jsonNumber.longValue();
											child.put(childKey, value);
										}
											break;
										case STRING:
										{
											JsonString jsonString = childJson.getJsonString(childKey);
											long value = Long.valueOf((jsonString.getString().length() == 0) ? "0" : jsonString.getString());
											child.put(childKey, value);
										}
											break;
										default:
											logger.error("Unexptected childValueType {}", childValueType);
											throw new UnexpectedException("Unexptected childValueType");
										}
									}
									fieldInfo.field.set(this, child);
								}
									break;
								case "java.lang.Integer":
								{
									Map<String, Integer> child = new TreeMap<>();
									JsonObject childJson = jsonObject.get(fieldInfo.jsonName).asJsonObject();
									for(String childKey: childJson.keySet()) {
										JsonValue childValue = childJson.get(childKey);
										ValueType childValueType = childValue.getValueType();
										
										switch(childValueType) {
										case NUMBER:
										{
											JsonNumber jsonNumber = childJson.getJsonNumber(childKey);
											int value = jsonNumber.intValue();
											child.put(childKey, value);
										}
											break;
										case STRING:
										{
											JsonString jsonString = childJson.getJsonString(childKey);
											int value = Integer.valueOf((jsonString.getString().length() == 0) ? "0" : jsonString.getString());
											child.put(childKey, value);
										}
											break;
										default:
											logger.error("Unexptected childValueType {}", childValueType);
											throw new UnexpectedException("Unexptected childValueType");
										}
									}
									fieldInfo.field.set(this, child);
								}
									break;
								default:
									logger.error("Unexptected keyTypeName {}", keyTypeName);
									throw new UnexpectedException("Unexptected keyTypeName");
								}
							} else {
								throw new UnexpectedException("Unexptected");
							}
							
						}
							break;
						default:
							logger.error("Unexptected type {} {}", valueType.toString(), fieldInfo.toString());
							logger.error("fieldTypeName {}", fieldTypeName);
							throw new UnexpectedException("Unexptected type");
						}
					}
				}
					break;
				case ARRAY:
				{					
					if (fieldInfo.isArray) {
						JsonArray childJson = jsonObject.get(fieldInfo.jsonName).asJsonArray();

						Class<?> componentType = fieldInfo.field.getType().getComponentType();
						String componentTypeName = componentType.getName();
						switch(componentTypeName) {
						case "java.lang.String":
						{
							int childJsonArraySize = childJson.size();
							String[] value = new String[childJson.size()];
							
							for(int j = 0; j < childJsonArraySize; j++) {
								JsonValue childJsonValue = childJson.get(j);
								switch(childJsonValue.getValueType()) {
								case STRING:
									value[j] = childJson.getString(j);
									break;
								default:
									logger.error("Unexpected json array element type {} {}", childJsonValue.getValueType().toString(), fieldInfo.toString());
									throw new UnexpectedException("Unexpected json array element type");
								}
							}
							fieldInfo.field.set(this, value);
						}
							break;
						default:
							logger.error("Unexpected array component type {} {}", componentTypeName, fieldInfo.toString());
							throw new UnexpectedException("Unexpected array component type");
						}
					} else {
						logger.error("Unexptected field is not Array {}", fieldInfo.toString());
						throw new UnexpectedException("Unexptected field is not Array");
					}
				}
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
					switch(fieldInfo.type) {
					case "double":
						if (!fieldInfo.ignoreField) {
							logger.warn("Assign defautl value  {} {} {}", iexInfo.clazzName, fieldInfo.name, fieldInfo.type);
						}
						fieldInfo.field.setDouble(this, 0);
						break;
					case "java.lang.String":
						if (!fieldInfo.ignoreField) {
							logger.warn("Assign defautl value  {} {} {}", iexInfo.clazzName, fieldInfo.name, fieldInfo.type);
						}
						fieldInfo.field.set(this, "");
						break;
					case "java.time.LocalDateTime":
						if (!fieldInfo.ignoreField) {
							logger.warn("Assign defautl value  {} {} {}", iexInfo.clazzName, fieldInfo.name, fieldInfo.type);
						}
						fieldInfo.field.set(this, NULL_LOCAL_DATE_TIME);
						break;
					default:
						logger.error("Unexpected field type {} {}", iexInfo.clazzName, fieldInfo.toString());
						throw new UnexpectedException("Unexpected field type");
					}
				}
			}
		} catch (IllegalAccessException | InstantiationException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}
	}

	protected static String[] encodeSymbol(String[] symbols) {
		try {
			String[] ret = new String[symbols.length];
			for(int i = 0; i < symbols.length; i++) {
				ret[i] = URLEncoder.encode(symbols[i], "UTF-8");
			}
			return ret;
		} catch (UnsupportedEncodingException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}
	}

	// for Status
	public static <E extends Base> E getObject(Context context, Class<E> clazz) {
		ClassInfo classInfo = ClassInfo.get(clazz);
		
		// 'https://cloud.iexapis.com/v1/status?token=sk_bb977734bffe47ef8dca20cd4cfad878'
		if (classInfo.method == null) {
			logger.error("method == null {}", classInfo);
			throw new UnexpectedException("method == null");
		}
		String url = context.getURL(classInfo.method);
//		logger.info("url = {}", url);
		
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
			E ret = clazz.getDeclaredConstructor(JsonObject.class).newInstance(arg);
			return ret;
		} catch (IllegalAccessException | InstantiationException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}
	}

	// for symbols
	public static <E extends Base> List<E> getArray(Context context, Class<E> clazz) {
		ClassInfo classInfo = ClassInfo.get(clazz);
		
		// 'https://cloud.iexapis.com/v1/status?token=sk_bb977734bffe47ef8dca20cd4cfad878'
		if (classInfo.method == null) {
			logger.error("method == null {}", classInfo);
			throw new UnexpectedException("method == null");
		}
		String url = context.getURL(classInfo.method);
//		logger.info("url = {}", url);
		
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
				ret[i]  = clazz.getDeclaredConstructor(JsonObject.class).newInstance(arg);
			}
			
			// Sort array
			Arrays.sort(ret);
			
			// Return as list
			return Arrays.asList(ret);
		} catch (IllegalAccessException | InstantiationException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}
	}

	// for symbols
	public static <E extends Base> List<E> getCSV(Context context, Class<E> clazz) {
		ClassInfo classInfo = ClassInfo.get(clazz);
		
		// 'https://cloud.iexapis.com/v1/status?token=sk_bb977734bffe47ef8dca20cd4cfad878'
		if (classInfo.method == null) {
			logger.error("method == null {}", classInfo);
			throw new UnexpectedException("method == null");
		}
		String url = context.getURLAsCSV(classInfo.method);
//		logger.info("url = {}", url);
		
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

	public static <E extends Base> void saveCSV(List<E> dataList) {
		E o = dataList.get(0);
		ClassInfo classInfo = ClassInfo.get(o);
		
		if (classInfo.path == null) {
			logger.error("classInfo.path == null  {}", classInfo);
			throw new UnexpectedException("classInfo.path == null");
		}
		
		String path = String.format("%s%s", PATH_DIR, classInfo.path);
//		logger.info("path = {}", path);

		CSVUtil.saveWithHeader(dataList, path);
	}
	protected static <E extends Base> List<E> loadCSV(Class<E> clazz) {
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
