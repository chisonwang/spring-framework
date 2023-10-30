/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.web.bind.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that identifies methods which initialize the
 * {@link org.springframework.web.bind.WebDataBinder} which
 * will be used for populating command and form object arguments
 * of annotated handler methods.
 *
 * <p>Such init-binder methods support all arguments that {@link RequestMapping}
 * supports, except for command/form objects and corresponding validation result
 * objects. Init-binder methods must not have a return value; they are usually
 * declared as {@code void}.
 *
 * <p>Typical arguments are {@link org.springframework.web.bind.WebDataBinder}
 * in combination with {@link org.springframework.web.context.request.WebRequest}
 * or {@link java.util.Locale}, allowing to register context-specific editors.
 *
 * @author Juergen Hoeller
 * @since 2.5
 * @see org.springframework.web.bind.WebDataBinder
 * @see org.springframework.web.context.request.WebRequest
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
// @InitBinder注解可以作用在被@Controller注解的类的方法上，
// 表示为当前控制器注册一个属性编辑器，用于对WebDataBinder进行初始化，
// 且只对当前的Controller有效。
// @InitBinder 标注的方法会被多次执行的，也就是说来一次请求就会执行一次@InitBinder注解方法的内容。
// @InitBinder对@RequestBody的请求参数是失效的.
// 因为@InitBinder用于初始化DataBinder的数据绑定、类型转换等场景；
// @RequestBody的数据解析、格式转换是通过消息转换器来完成的，所以即使自定义了属性编辑器，对@RequestBody的参数是不生效的，@RequestBody的数据绑定转换，如果是JSON格式的数据一般都是交给了JACKSON来完成的

public @interface InitBinder {

	/**
	 * The names of command/form attributes and/or request parameters
	 * that this init-binder method is supposed to apply to.
	 * <p>Default is to apply to all command/form attributes and all request parameters
	 * processed by the annotated handler class. Specifying model attribute names or
	 * request parameter names here restricts the init-binder method to those specific
	 * attributes/parameters, with different init-binder methods typically applying to
	 * different groups of attributes or parameters.
	 */
	String[] value() default {};

}
