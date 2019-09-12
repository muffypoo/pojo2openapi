package com.kensoft.pojo2openapi.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Description of this property.
 * 
 * @author ken_kum
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface YamlDescription {
	String value() default "";
}
