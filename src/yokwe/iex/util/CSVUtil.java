package yokwe.iex.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import yokwe.iex.UnexpectedException;

public class CSVUtil {
	private static final Logger logger = LoggerFactory.getLogger(CSVUtil.class);
	
	// CSV file should not have header, because sqlite .import read header as data
	
	// Save List<E> data as CSV file.
	// Load CSV file as List<E> data.
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public static @interface ColumnName {
		String value();
	}
	
	
	private static class ClassInfo {
		private static Map<String, ClassInfo> map = new TreeMap<>();
		
		static ClassInfo get(Class<?> clazz) {
			String key = clazz.getName();
			if (map.containsKey(key)) {
				return map.get(key);
			} else {
				ClassInfo value = new ClassInfo(clazz);
				map.put(key, value);
				return value;
			}
		}

		final Class<?>    clazz;
		final String      name;
		final FieldInfo[] fieldInfos;
		final String[]    names;
		
		ClassInfo(Class<?> value) {
			clazz = value;
			name  = clazz.getName();
			
			List<FieldInfo> list = new ArrayList<>();
			for(Field field: clazz.getDeclaredFields()) {
				// Skip static field
				if (Modifier.isStatic(field.getModifiers())) continue;

				list.add(new FieldInfo(field));
			}
			fieldInfos = list.toArray(new FieldInfo[0]);
			
			names = new String[fieldInfos.length];
			for(int i = 0; i < names.length; i++) {
				names[i] = fieldInfos[i].name;
			}
		}
	}
	private static class FieldInfo {
		final Field    field;
		final String   name;
		final Class<?> clazz;
		final String   clazzName;
		final Map<String, Enum<?>> enumMap;
		
		FieldInfo(Field value) {
			field = value;
			
			ColumnName columnName = field.getDeclaredAnnotation(ColumnName.class);
			name = (columnName == null) ? field.getName() : columnName.value();
			
			clazz = field.getType();
			clazzName  = clazz.getName();
				if (clazz.isEnum()) {
				enumMap = new TreeMap<>();
				
				@SuppressWarnings("unchecked")
				Class<Enum<?>> enumClazz = (Class<Enum<?>>)clazz;
				for(Enum<?> e: enumClazz.getEnumConstants()) {
					enumMap.put(e.toString(), e);
				}
			} else {
				enumMap = null;
			}
		}
	}

	private static int BUFFER_SIZE = 64 * 1024;

	//
	// load Path
	//
	public static <E> List<E> loadWithHeader(String path, Class<E> clazz, String header) {
		String[] names = header.split(",");
		
		CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader(names).withRecordSeparator("\n");
		return load(path, clazz, csvFormat);
	}

	public static <E> List<E> loadWithHeader(String path, Class<E> clazz) {
		String[] names = ClassInfo.get(clazz).names;
		
		CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader(names).withRecordSeparator("\n");
		return load(path, clazz, csvFormat);
	}

	public static <E> List<E> load(String path, Class<E> clazz) {
		CSVFormat csvFormat = CSVFormat.DEFAULT.withRecordSeparator("\n");
		return load(path, clazz, csvFormat);
	}
	
	public static <E> List<E> load(String path, Class<E> clazz, CSVFormat csvFormat) {
		Reader reader;
		try {
			reader = new BufferedReader(new FileReader(path), BUFFER_SIZE);
			return load(reader, clazz, csvFormat);
		} catch (FileNotFoundException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}
	}
	
	//
	// load Reader
	//
	
	public static <E> List<E> loadWithHeader(Reader reader, Class<E> clazz) {
		String[] names = ClassInfo.get(clazz).names;
		
		CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader(names).withRecordSeparator("\n");
		return load(reader, clazz, csvFormat);
	}

	public static <E> List<E> load(Reader reader, Class<E> clazz) {
		CSVFormat csvFormat = CSVFormat.DEFAULT.withRecordSeparator("\n");
		return load(reader, clazz, csvFormat);
	}
	
	private static final String UTF8_BOM = "\uFEFF";
	public static <E> List<E> load(Reader reader, Class<E> clazz, CSVFormat csvFormat) {
		ClassInfo classInfo = ClassInfo.get(clazz);
		
		String[] names = csvFormat.getHeader();
		// Sanity check
		if (names.length != classInfo.names.length) {
			logger.error("names.length != classInfo.names.length  {}  {}  {}", classInfo.name, names.length, classInfo.names.length);
			throw new UnexpectedException("names.length != classInfo.names.length");
		}
		for(int i = 0; i < names.length; i++) {
			String headerName = names[i];
			String fieldName  = classInfo.names[i];
			if (!headerName.equals(fieldName)) {
				logger.error("headerName not equls fieldName  {}  {}  {}", classInfo.name, headerName, fieldName);
				throw new UnexpectedException("headerName not equls fieldName");
			}
		}
		
		try (CSVParser csvParser = csvFormat.parse(reader)) {
			List<E> dataList = new ArrayList<>();
			for(CSVRecord record: csvParser) {
				// Sanity check
				final int size = classInfo.fieldInfos.length;
				if (record.size() != size) {
					logger.error("record.size != size  {} != {}", record.size(), size);
					logger.error("record = {}", record);
					for(int i = 0; i < record.size(); i++) {
						logger.error("record {} = {}", i, record.get(i));
					}
					for(int i = 0; i < size; i++) {
						logger.error("field {} = {}  {}", i, classInfo.fieldInfos[i].name, classInfo.fieldInfos[i].clazzName);
					}
					throw new UnexpectedException("record.size != size");
				}
				
				if (names != null && record.getRecordNumber() == 1) {
					// Sanity check
					int headerSize = record.size();
					if (headerSize != size) {
						logger.error("headerSize != size  {} != {}", headerSize, size);
						throw new UnexpectedException("headerSize != size");
					}
					for(int i = 0; i < size; i++) {
						String headerName = record.get(i);
						
						if (headerName.contains(UTF8_BOM)) headerName = headerName.substring(1);
						
						if (!headerName.equals(names[i])) {
							logger.error("headerName != name  {}  {} != {}", i, headerName, names[i]);
							throw new UnexpectedException("headerName != name");
						}
					}
					continue;
				}
				
				E data = clazz.newInstance();
				for(int i = 0; i < size; i++) {
					String value = record.get(i);
					
					FieldInfo fieldInfo = classInfo.fieldInfos[i];
					
					if (fieldInfo.enumMap != null) {
						if (fieldInfo.enumMap.containsKey(value)) {
							fieldInfo.field.set(data, fieldInfo.enumMap.get(value));
						} else {
							logger.error("Unknow enum value  {}  {}", fieldInfo.clazzName, value);
							throw new UnexpectedException("Unknow enum value");
						}
					} else {
						switch(fieldInfo.clazzName) {
						case "int":
						{
							int intValue = value.isEmpty() ? 0 : Integer.parseInt(value);
							fieldInfo.field.setInt(data, intValue);
						}
							break;
						case "long":
						{
							long longValue = value.isEmpty() ? 0 : Long.parseLong(value);
							fieldInfo.field.setLong(data, longValue);
						}
							break;
						case "double":
						{
							double doubleValue = value.isEmpty() ? 0 : Double.parseDouble(value);
							fieldInfo.field.setDouble(data, doubleValue);
						}
							break;
						case "boolean":
						{
							boolean booleanValue = value.isEmpty() ? false : Boolean.parseBoolean(value);
							fieldInfo.field.setBoolean(data, booleanValue);
						}
							break;
						case "java.lang.String":
							fieldInfo.field.set(data, value);
							break;
						case "java.time.LocalDateTime":
						{
							final LocalDateTime localDateTime;
							
							if (value.isEmpty() || value.equals("0")) {
								localDateTime = LocalDateTime.of(1900, 1, 1, 0, 0);
							} else if (value.matches("^[0-9]+$")) {
								long longValue = Long.parseLong(value);
								// Need time zone to calculate correct date time
								localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(longValue), ZoneOffset.UTC);
							} else {
								localDateTime = LocalDateTime.parse(value);
							}
							
							fieldInfo.field.set(data, localDateTime);
						}
							break;
						default:
							logger.error("Unexptected fieldInfo.clazzName {}", fieldInfo.clazzName);
							logger.error("  enumMap {}", fieldInfo.enumMap);
							throw new UnexpectedException("Unexptected fieldInfo.clazzName");
						}
					}
				}
				dataList.add(data);
			}
			return dataList;
		} catch (IOException | InstantiationException | IllegalAccessException | NumberFormatException | DateTimeParseException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}
	}
	
	
	//
	// save
	//
	public static <E> void saveWithHeader(List<E> dataList, String path) {
		Object o = dataList.get(0);
		ClassInfo classInfo = ClassInfo.get(o.getClass());
		
		CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader(classInfo.names).withRecordSeparator("\n");
		save(dataList, path, csvFormat);
	}

	public static <E> void save(List<E> dataList, String path) {
		CSVFormat csvFormat = CSVFormat.DEFAULT.withRecordSeparator("\n");
		save(dataList, path, csvFormat);
	}
	public static <E> void save(List<E> dataList, String path, CSVFormat csvFormat) {
		ClassInfo classInfo = ClassInfo.get(dataList.get(0).getClass());
		
		Object[] values = new Object[classInfo.fieldInfos.length];

		// Create parent dirs and file if not exists.
		{
			File file = new File(path);
			
			File fileParent = file.getParentFile();
			if (!fileParent.exists()) {
				fileParent.mkdirs();
			}
			if (!file.exists()) {
				try {
					file.createNewFile();
				} catch (IOException e) {
					logger.error("IOException {}", e.toString());
					throw new UnexpectedException("IOException");
				}
			}
		}

		try (CSVPrinter csvPrint = new CSVPrinter(new BufferedWriter(new FileWriter(path), BUFFER_SIZE), csvFormat)) {
			for(E entry: dataList) {
				for(int i = 0; i < values.length; i++) {
					Field field = classInfo.fieldInfos[i].field;
					values[i] = field.get(entry).toString();
				}
				csvPrint.printRecord(values);
			}
		} catch (IOException | IllegalArgumentException | IllegalAccessException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}
	}

}
