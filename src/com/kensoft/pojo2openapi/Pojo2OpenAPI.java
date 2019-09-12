/**
 * 
 */
package com.kensoft.pojo2openapi;


/**
 * This program converts a set of POJO classes into YAML format ready for use with Swagger WADL definition.
 * 
 * @author ken_kum
 *
 */
public class Pojo2OpenAPI {

	/**
	 * @param args0 - root directory of target class path
	 * @param args1 - package path from the root directory
	 * @param args2 - (optional, default "definitions") definition root of the tree structure, will be used as part of $ref object references
	 * @param args3 - (optional, default "swagger-definitions.yaml") output filename
	 */
	public static void main(String[] args) {
		ConvertUtil cu = new ConvertUtil();
		String definitionRoot = "definitions";
		String outFile = "swagger-definitions.yaml";
		String path = args[0];
		String pathPackageStr = args[1];
		if(args.length > 2 && args[2] != null) {
			definitionRoot = args[2];
		}
		if(args.length > 3 && args[3] != null) {
			outFile = args[3];
		}
		
		cu.run(path, pathPackageStr, definitionRoot, outFile);
	}
}
