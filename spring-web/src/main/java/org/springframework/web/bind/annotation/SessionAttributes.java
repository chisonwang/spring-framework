/*
 * Copyright 2002-2015 the original author or authors.
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

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * Annotation that indicates the session attributes that a specific handler uses.
 *
 * <p>This will typically list the names of model attributes which should be
 * transparently stored in the session or some conversational storage,
 * serving as form-backing beans. <b>Declared at the type level</b>, applying
 * to the model attributes that the annotated handler class operates on.
 *
 * <p><b>NOTE:</b> Session attributes as indicated using this annotation
 * correspond to a specific handler's model attributes, getting transparently
 * stored in a conversational session. Those attributes will be removed once
 * the handler indicates completion of its conversational session. Therefore,
 * use this facility for such conversational attributes which are supposed
 * to be stored in the session <i>temporarily</i> during the course of a
 * specific handler's conversation.
 *
 * <p>For permanent session attributes, e.g. a user authentication object,
 * use the traditional {@code session.setAttribute} method instead.
 * Alternatively, consider using the attribute management capabilities of the
 * generic {@link org.springframework.web.context.request.WebRequest} interface.
 *
 * <p><b>NOTE:</b> When using controller interfaces (e.g. for AOP proxying),
 * make sure to consistently put <i>all</i> your mapping annotations &mdash;
 * such as {@code @RequestMapping} and {@code @SessionAttributes} &mdash; on
 * the controller <i>interface</i> rather than on the implementation class.
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 2.5
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
//  @SessionAttribute注解是Spring MVC框架中用于将模型数据存储到HttpSession对象中的注解。
//  使用该注解可以方便地在多个请求之间共享模型数据，而无需每次都重新获取或创建数据
// 通常情况下，控制器方法会通过Model对象将模型数据传递给视图进行渲染
// 但是，在某些场景下，可能需要让模型数据在多个请求之间共享。例如，在用户登录成功后，
// 可能需要将用户信息存储到Session中，并在后续的所有请求中都可以使用该用户信息。
// 在这种情况下，可以使用@SessionAttribute注解将模型数据存储到Session中。
// 该注解有两个参数：value和types，分别用于指定Session属性的名称和类型
public @interface SessionAttributes {

	/**
	 * Alias for {@link #names}.
	 */
	@AliasFor("names")
	String[] value() default {};

	/**
	 * The names of session attributes in the model that should be stored in the
	 * session or some conversational storage.
	 * <p><strong>Note</strong>: This indicates the <em>model attribute names</em>.
	 * The <em>session attribute names</em> may or may not match the model attribute
	 * names. Applications should therefore not rely on the session attribute
	 * names but rather operate on the model only.
	 * @since 4.2
	 */
	@AliasFor("value")
	String[] names() default {};

	/**
	 * The types of session attributes in the model that should be stored in the
	 * session or some conversational storage.
	 * <p>All model attributes of these types will be stored in the session,
	 * regardless of attribute name.
	 */
	Class<?>[] types() default {};

}
