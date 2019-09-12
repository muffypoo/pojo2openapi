package com.kensoft.pojo2openapi;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import com.kensoft.pojo2openapi.annotations.YamlDescription;
import com.kensoft.pojo2openapi.annotations.YamlRequired;

/**
 * 
 * @author ken_kum
 *
 */
public class ConvertUtil {
	public void run(String path, String pathPackageStr, String definitionRoot, String outFile) {
		System.out.println("run(\"" + path + "\", \"" + pathPackageStr + "\", \"" + definitionRoot + "\"" + outFile + "\")");
		try {
			//retrieving class files
			Class[] classArr = getClasses(path, pathPackageStr);
			StringBuffer sb = new StringBuffer(definitionRoot+":\r\n");
			for(Class c: classArr) {
				System.out.println("class: " + c);
				//generating yaml for each class
				sb.append(getYaml(c, definitionRoot));
			}
			
			//output to file
			File f = new File(outFile);
			FileWriter fw = new FileWriter(f);
			fw.write(sb.toString());
			fw.flush();
			fw.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Done.");
	}
	
	private Class[] getClasses(String path, String pathPackageStr) throws MalformedURLException, ClassNotFoundException {
		System.out.println("getClasses(\"" + path + "\", \"" + pathPackageStr + "\")");
		File file = new File(path + pathPackageStr);
		File fileCL = new File(path);
		
		//retrieving files from path
	    File[] files = file.listFiles(new FilenameFilter() {
	        @Override public boolean accept(File dir, String name) {
	            return name.endsWith(".class");
	        }
	    });
	    System.out.println("files.length: " + files.length);
	    
	    //loading classes into ClassLoader
	    ClassLoader cl = new URLClassLoader(new URL[] { fileCL.toURI().toURL() });
	    ArrayList<Class> classList = new ArrayList<Class>();
	    for (File f: files) {
	        String className = f.getName().substring(0, f.getName().length() - 6);
	        System.out.println("className: " + className);
	        Class<?> clazz = cl.loadClass(pathPackageStr.replace("/", ".")+className);
	        classList.add(clazz);
	    }
	    return classList.toArray(new Class[classList.size()]);
	}

	private String getYaml(Class c, String definitionRoot) {
		if(c == null) return "";
		System.out.println("getYaml(" + c.getSimpleName() + ")");
		//preparing default response containing simpleName of class
		String result = 
			"  " + c.getSimpleName() + ":\r\n" +
			"    type: \"object\"\r\n" +
			"    properties:\r\n";
		String requiredTemplate = "    - \"$1\"\r\n";
		
		List<String> requiredFieldList = new ArrayList<String>();
		Field[] fieldArr = c.getDeclaredFields();
		for(Field f: fieldArr) {
			if(!Modifier.isStatic(f.getModifiers()) && !Modifier.isFinal(f.getModifiers())) {
				//only retrieve properties which are not constants
				result += "      " + f.getName() + ":\r\n" + getYamlType(f, definitionRoot, requiredFieldList);
				
			}
		}
//		System.out.println("requiredFields2: " + requiredFieldArr);
//		if(!"".equals(requiredFields)) {
//			result += "    required:\r\n" + requiredFieldArr;
//		}
		if(!requiredFieldList.isEmpty()) {
			String requiredStr = "    required:\r\n";
			System.out.println(requiredFieldList);
			for(String s: requiredFieldList) {
				requiredStr += requiredTemplate.replace("$1", s);
			}
			result += requiredStr;
		}
		
		return result;
	}
	
	private String getYamlType(Field f, String definitionRoot, List<String> requiredFieldList) {
//		System.out.println("getYamlType(Field)");
		String result = "";
		String typeTemplate = "        type: \"$1\"\r\n";
		String formatTemplate = "        format: \"$1\"\r\n";
		String refTemplate = "        $ref: \"#/$0/$1\"\r\n".replace("$0", definitionRoot);
		String descTemplate = "        description: \"$1\"\r\n";
		String arrayTemplate = 
			"        items:\r\n" +
			"          type: $1\r\n";
		
		if("photoKeys".equals(f.getName())) {
			System.out.println("hi");
		}
		//conditional check for various native data types from Java and their Object counterparts
		Class typeClass = f.getType();
		String typeName = typeClass.getSimpleName();
//		System.out.println("typeName: " + typeName);
		
		if("byte".equalsIgnoreCase(typeName) || "byte[]".equalsIgnoreCase(typeName)) {
			//byte and byte array will be interpreted as Base64 string
			result = typeTemplate.replace("$1", "string");
			result += formatTemplate.replace("$1", "byte");
		} else if("short".equalsIgnoreCase(typeName)) {
			result = typeTemplate.replace("$1", "number");
		} else if("int".equals(typeName) || "Integer".equals(typeName)) {
			//type as "number" works fine but being explicit to indicate "int32" instead
			result = typeTemplate.replace("$1", "integer");
			result += formatTemplate.replace("$1", "int32");
		} else if("long".equalsIgnoreCase(typeName)) {
			//main difference in numbers is for long to use "int64"
			result = typeTemplate.replace("$1", "integer");
			result += formatTemplate.replace("$1", "int64");
		} else if("float".equalsIgnoreCase(typeName)) {
			result = typeTemplate.replace("$1", "number");
		} else if("double".equalsIgnoreCase(typeName)) {
			result = typeTemplate.replace("$1", "number");
		} else if("boolean".equalsIgnoreCase(typeName)) {
			result = typeTemplate.replace("$1", "boolean");
		} else if("char".equals(typeName) || "Character".equals(typeName)) {
			result = typeTemplate.replace("$1", "string");
		} else if("Date".equalsIgnoreCase(typeName)) {
			//default to using "date-time" to be more inclusive
			result = typeTemplate.replace("$1", "string");
			result += formatTemplate.replace("$1", "date-time");
		} else if("String".equals(typeName)) {
			result = typeTemplate.replace("$1", "string");
		} else if("List".equalsIgnoreCase(typeName) || typeName.contains("List")) {
			//more processing for List object types, not checking for Collections currently 
			
//			System.out.println("typeClass: " + typeClass);
			result = typeTemplate.replace("$1", "array");
			
			//secondary processing for parameterised List type to reference another POJO class
			String typeStr = f.getGenericType().toString();
			if(typeStr.contains("<") && typeStr.contains(">")) {
				String paramType = typeStr.substring(typeStr.indexOf("<")+1, typeStr.indexOf(">")-1);
				boolean isNative = paramType.startsWith("java.lang.");
				typeStr = paramType.substring(paramType.lastIndexOf(".")+1, paramType.length());
//				System.out.println("typeStr: " + typeStr);
				if(isNative) {
					result += arrayTemplate.replace("$1", getDataTypeMapping(paramType));
				} else {
					result += refTemplate.replace("$1", typeStr);
				}
			}
		} else {
			result = refTemplate.replace("$1", typeName);
		}
		
		Annotation[] annArr = f.getAnnotations();
//		System.out.println("annArr: " + annArr.length);
		YamlDescription d;
		for(Annotation a: annArr) {
			if(a instanceof YamlDescription) {
				d = (YamlDescription)a;
				result += descTemplate.replace("$1", d.value());
			}
			
			if(a instanceof YamlRequired) {
				requiredFieldList.add(f.getName());
			}
		}
		
		return result;
	}
	
	private String getDataTypeMapping(String typeName) {
		String result = "string";
		if("short".equalsIgnoreCase(typeName)) {
			result = "number";
		} else if("int".equals(typeName) || "Integer".equals(typeName)) {
			result = "integer";
		} else if("long".equalsIgnoreCase(typeName)) {
			result = "integer";
		} else if("float".equalsIgnoreCase(typeName)) {
			result = "number";
		} else if("double".equalsIgnoreCase(typeName)) {
			result = "number";
		} else if("boolean".equalsIgnoreCase(typeName)) {
			result = "boolean";
		}
		
		return result;
	}
}
