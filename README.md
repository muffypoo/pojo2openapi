## pojo2openapi

Converts a set of POJO classes into YAML format ready for use with Swagger WADL definition.

## Overview

Pojo2OpenAPI takes in a folder of compiled Java classes, and generates a definition in YAML which is suitable for use with the [Swagger](https://swagger.io/) [OpenAPI Specification](https://swagger.io/specification/). The emitter produces the reference objects that can be used in [Paths](https://swagger.io/specification/#pathsObject) objects. The Swagger Online Editor is [here](https://editor.swagger.io).

## Usage

The jar is runnable from the command-line (CLI) with the following arguments provided:

1. root directory of target class path (using forward slashes)
2. package path from the root directory (using forward slashes)
3. (optional, default "definitions") definition root of the tree structure, will be used as part of $ref object references
4. (optional, default "swagger-definitions.yaml") output filename

**Example 1:**
```bat
java -jar pojo2openapi.jar "/users/project-parent/project-application/target/classes/" "com/kensoft/pojo2openapi/beans"
```
**Example 2:**
```bat
java -jar pojo2openapi.jar "/users/project-parent/project-application/target/classes/" "com/kensoft/pojo2openapi/beans" "components" "swagger-comps.yaml"
```

## Annotations

There are optional additions which can be taken into consideration while generating the definitions:

1. @YamlRequired
2. @YamlDescription(value="This is the field description")

Add Pojo2OpenAPI to your project path to make use of the annotations.

## Sample Class

```java
public class HelloClass {
	@YamlDescription(value="Hello description.")
	private String helloString;
	@YamlRequired
	private int helloInt;
	private byte[] helloBase64;
	private HelloClass helloClassClass;
}
```
## Sample Output

```yaml
components:
  HelloClass:
    type: "object"
    properties:
      helloString:
        type: "string"
        description: "Hello description."
      helloInt:
        type: "integer"
        format: "int32"
      helloBase64:
        type: "string"
        format: "byte"
      helloClassClass:
        $ref: "#/components/HelloClass"
    required:
    - "helloInt"
```
