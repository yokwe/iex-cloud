package yokwe.iex.cloud;

import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue.ValueType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import yokwe.iex.UnexpectedException;
import yokwe.iex.util.CSVUtil;
import yokwe.iex.util.HttpUtil;

public class Base {
	static final Logger logger = LoggerFactory.getLogger(Base.class);
	
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
					fieldInfo.setValue(this, jsonObject.getJsonNumber(fieldInfo.jsonName));
					break;
				case STRING:
					fieldInfo.setValue(this, jsonObject.getJsonString(fieldInfo.jsonName));
					break;
				case TRUE:
					fieldInfo.setValue(this, true);
					break;
				case FALSE:
					fieldInfo.setValue(this, false);
					break;
				case NULL:
					fieldInfo.setValue(this);
					break;
				case OBJECT:
					fieldInfo.setValue(this, jsonObject.getJsonObject(fieldInfo.jsonName));
					break;
				case ARRAY:
					fieldInfo.setValue(this, jsonObject.getJsonArray(fieldInfo.jsonName));
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
					fieldInfo.setValue(this);
				}
			}
		} catch (IllegalAccessException | InstantiationException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}
	}

	// for Status
	public static <E extends Base> E getObject(Context context, Class<E> clazz) {
		ClassInfo classInfo = ClassInfo.get(clazz);
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

	// for data-points/SYMBOL
	public static <E extends Base> List<E> getCSV(Context context, Class<E> clazz, String sybmol) {
		ClassInfo classInfo = ClassInfo.get(clazz);
		
		// 'https://cloud.iexapis.com/v1/status?token=sk_bb977734bffe47ef8dca20cd4cfad878'
		if (classInfo.method == null) {
			logger.error("method == null {}", classInfo);
			throw new UnexpectedException("method == null");
		}
		String url = context.getURLAsCSV(classInfo.method, sybmol);
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
	public static <E extends Base> List<E> loadCSV(Class<E> clazz) {
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
