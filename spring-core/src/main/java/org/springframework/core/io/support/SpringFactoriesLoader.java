/*
 * Copyright 2002-2019 the original author or authors.
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

package org.springframework.core.io.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.io.UrlResource;
import org.springframework.lang.Nullable;
import org.springframework.util.*;

import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * General purpose factory loading mechanism for internal use within the framework.
 *
 * <p>{@code SpringFactoriesLoader} {@linkplain #loadFactories loads} and instantiates
 * factories of a given type from {@value #FACTORIES_RESOURCE_LOCATION} files which
 * may be present in multiple JAR files in the classpath. The {@code spring.factories}
 * file must be in {@link Properties} format, where the key is the fully qualified
 * name of the interface or abstract class, and the value is a comma-separated list of
 * implementation class names. For example:
 *
 * <pre class="code">example.MyService=example.MyServiceImpl1,example.MyServiceImpl2</pre>
 *
 * where {@code example.MyService} is the name of the interface, and {@code MyServiceImpl1}
 * and {@code MyServiceImpl2} are two implementations.
 *
 * @author Arjen Poutsma
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 3.2
 */
public final class SpringFactoriesLoader {

	/**
	 * The location to look for factories.
	 * <p>Can be present in multiple JAR files.
	 */
	public static final String FACTORIES_RESOURCE_LOCATION = "META-INF/spring.factories";


	private static final Log logger = LogFactory.getLog(SpringFactoriesLoader.class);

	private static final Map<ClassLoader, MultiValueMap<String, String>> cache = new ConcurrentReferenceHashMap<>();


	private SpringFactoriesLoader() {
	}


	/**
	 * Load and instantiate the factory implementations of the given type from
	 * {@value #FACTORIES_RESOURCE_LOCATION}, using the given class loader.
	 * <p>The returned factories are sorted through {@link AnnotationAwareOrderComparator}.
	 * <p>If a custom instantiation strategy is required, use {@link #loadFactoryNames}
	 * to obtain all registered factory names.
	 * @param factoryType the interface or abstract class representing the factory
	 * @param classLoader the ClassLoader to use for loading (can be {@code null} to use the default)
	 * @throws IllegalArgumentException if any factory implementation class cannot
	 * be loaded or if an error occurs while instantiating any factory
	 * @see #loadFactoryNames
	 */
	// 通过classLoader从各个jar包的classpath下面的META-INF/spring.factories加载并解析其key-value值，然后创建其给定类型的工厂实现
	// 返回的工厂通过AnnotationAwareOrderComparator进行排序过的。
	// AnnotationAwareOrderComparator就是通过@Order注解上面的值进行排序的，值越高，则排的越靠后
	// 如果需要自定义实例化策略，请使用loadFactoryNames方法获取所有注册工厂名称。
	public static <T> List<T> loadFactories(Class<T> factoryType, @Nullable ClassLoader classLoader) {
		// 首先断言，传入的接口或者抽象类的Class对象不能为null
		Assert.notNull(factoryType, "'factoryType' must not be null");
		ClassLoader classLoaderToUse = classLoader;
		// 将传入的classLoader赋值给classLoaderToUse
		// 判断classLoaderToUse是否为空，如果为空，则使用默认的SpringFactoriesLoader的classLoader
		if (classLoaderToUse == null) {
			classLoaderToUse = SpringFactoriesLoader.class.getClassLoader();
		}
		// 加载所有的META-INF/spring.factories并解析，获取其配置的factoryNames
		//factoryType为EnvironmentPostProcessor.class
		List<String> factoryImplementationNames = loadFactoryNames(factoryType, classLoaderToUse);
		if (logger.isTraceEnabled()) {
			logger.trace("Loaded [" + factoryType.getName() + "] names: " + factoryImplementationNames);
		}
		List<T> result = new ArrayList<>(factoryImplementationNames.size());
		// 通过反射对所有的配置进行实例化
		for (String factoryImplementationName : factoryImplementationNames) {
			result.add(instantiateFactory(factoryImplementationName, factoryType, classLoaderToUse));
		}
		AnnotationAwareOrderComparator.sort(result);
		return result;
	}

	/**
	 * Load the fully qualified class names of factory implementations of the
	 * given type from {@value #FACTORIES_RESOURCE_LOCATION}, using the given
	 * class loader.
	 * @param factoryType the interface or abstract class representing the factory
	 * @param classLoader the ClassLoader to use for loading resources; can be
	 * {@code null} to use the default
	 * @throws IllegalArgumentException if an error occurs while loading factory names
	 * @see #loadFactories
	 */
	// 使用给定的类加载器从META-INF/spring.factories加载给定类型的工厂实现的完全限定类名。
	public static List<String> loadFactoryNames(Class<?> factoryType, @Nullable ClassLoader classLoader) {
		// 通过Class对象获取全限定类名(org.springframework.context.ApplicationListener)
		String factoryTypeName = factoryType.getName();
		// loadSpringFactories方法是获取所有的springFactories
		// getOrDefault是通过key即权限定名称，获取到对应的类的集合。因为value是通过逗号相隔的，可以有多个，所以是list
		// getOrDefault如果存在就返回，如果不存在那么就返回给的默认值
		return loadSpringFactories(classLoader).getOrDefault(factoryTypeName, Collections.emptyList());
	}
	// 加载所有的springFactories
	private static Map<String, List<String>> loadSpringFactories(@Nullable ClassLoader classLoader) {
		// 首先从cache中获取，根据classLoader，
		// cache是以ClassLoader作为key的。是静态的final修饰的。整个应用只有一份
		MultiValueMap<String, String> result = cache.get(classLoader);
		// 如果为null，证明还没有加载过，如果不为空，那么就添加
		if (result != null) {
			return result;
		}

		try {
			// 三目表达式，判断参数classLoader是否为空，如果不为空，那么直接使用传入的classLoader获取META-INF/spring.factories
			// 如果为空，那么就使用系统的classLoader来获取META-INF/spring.factories
			// 总之健壮性比较强
			Enumeration<URL> urls = (classLoader != null ?
					classLoader.getResources(FACTORIES_RESOURCE_LOCATION) :
					ClassLoader.getSystemResources(FACTORIES_RESOURCE_LOCATION));
			result = new LinkedMultiValueMap<>();
			while (urls.hasMoreElements()) {
				URL url = urls.nextElement();
				UrlResource resource = new UrlResource(url);
				Properties properties = PropertiesLoaderUtils.loadProperties(resource);
				// 将所有的key放入result

				for (Map.Entry<?, ?> entry : properties.entrySet()) {
					String factoryTypeName = ((String) entry.getKey()).trim();
					for (String factoryImplementationName : StringUtils.commaDelimitedListToStringArray((String) entry.getValue())) {
						result.add(factoryTypeName, factoryImplementationName.trim());
					}
				}
			}
			// 将加载的放入缓存
			cache.put(classLoader, result);
			return result;
		}
		catch (IOException ex) {
			throw new IllegalArgumentException("Unable to load factories from location [" +
					FACTORIES_RESOURCE_LOCATION + "]", ex);
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> T instantiateFactory(String factoryImplementationName, Class<T> factoryType, ClassLoader classLoader) {
		try {
			// 根据全限定类名通过反射获取Class对象
			Class<?> factoryImplementationClass = ClassUtils.forName(factoryImplementationName, classLoader);
			// 判断获取的Class对象是否从factoryType里面来，
			// 说具体点就是判断我们配置的spring.factories中的权限定类名所对应的类是否是对应的子类或者实现类
			// 比如
			// org.springframework.context.ApplicationContextInitializer=\
			// org.springframework.boot.context.ConfigurationWarningsApplicationContextInitializer,\
			// org.springframework.boot.context.ContextIdApplicationContextInitializer,
			// 那么他就会验证ConfigurationWarningsApplicationContextInitializer和ContextIdApplicationContextInitializer是否是ApplicationContextInitializer的子类
			// isAssignableFrom()方法与instanceof关键字的区别总结为以下两个点：
			// isAssignableFrom()方法是从类继承的角度去判断，instanceof关键字是从实例继承的角度去判断。
			// isAssignableFrom()方法是判断是否为某个类的父类，instanceof关键字是判断是否某个类的子类。
			// 如果不是，则会保存
			// factoryType:interface org.springframework.boot.env.EnvironmentPostProcessor
			// factoryImplementationClass:class com.nieyp.ausware.elasticsearch.core.CustormEnvironmentPostProcessor
			// 需要值得注意的是isAssignableFrom被native修饰,不能直接看实现
			// public native boolean isAssignableFrom(Class<?> cls);
			if (!factoryType.isAssignableFrom(factoryImplementationClass)) {
				throw new IllegalArgumentException(
						"Class [" + factoryImplementationName + "] is not assignable to factory type [" + factoryType.getName() + "]");
			}
			return (T) ReflectionUtils.accessibleConstructor(factoryImplementationClass).newInstance();
		}
		catch (Throwable ex) {
			throw new IllegalArgumentException(
				"Unable to instantiate factory class [" + factoryImplementationName + "] for factory type [" + factoryType.getName() + "]",
				ex);
		}
	}

}
