/*
 * Copyright 2002-2020 the original author or authors.
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

package org.springframework.http.converter;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link HttpMessageConverter} that can read and write strings.
 *
 * <p>By default, this converter supports all media types (<code>&#42;/&#42;</code>),
 * and writes with a {@code Content-Type} of {@code text/plain}. This can be overridden
 * by setting the {@link #setSupportedMediaTypes supportedMediaTypes} property.
 *
 * @author Arjen Poutsma
 * @author Juergen Hoeller
 * @since 3.0
 */

/**
 *   HttpMessageConverter 的实现类：完成请求报文到字符串和字符串到响应报文的转换
 *  * 默认情况下，此转换器支持所有媒体类型(*&#47;*)，并使用 Content-Type 为 text/plain 的内容类型进行写入
 *  * 这可以通过 setSupportedMediaTypes(父类 AbstractHttpMessageConverter 中的方法)
 *  方法设置 supportedMediaTypes 属性来覆盖
 *
 */
public class StringHttpMessageConverter extends AbstractHttpMessageConverter<String> {

	/**
	 * The default charset used by the converter.
	 */
	// 默认字符集(产生乱码的根源)
	public static final Charset DEFAULT_CHARSET = StandardCharsets.ISO_8859_1;


	@Nullable
	// 可使用的字符集
	private volatile List<Charset> availableCharsets;
	//标识是否输出 Response Headers:Accept-Charset(默认输出)
	private boolean writeAcceptCharset = false;


	/**
	 * A default constructor that uses {@code "ISO-8859-1"} as the default charset.
	 * @see #StringHttpMessageConverter(Charset)
	 */
	 // 使用 "ISO-8859-1" 作为默认字符集的默认构造函数
	public StringHttpMessageConverter() {
		this(DEFAULT_CHARSET);
	}

	/**
	 * A constructor accepting a default charset to use if the requested content
	 * type does not specify one.
	 */
	// 如果请求的内容类型 Content-Type 没有指定一个字符集，则使用构造函数提供的默认字符集
	public StringHttpMessageConverter(Charset defaultCharset) {
		super(defaultCharset, MediaType.TEXT_PLAIN, MediaType.ALL);
	}


	/**
	 * Whether the {@code Accept-Charset} header should be written to any outgoing
	 * request sourced from the value of {@link Charset#availableCharsets()}.
	 * The behavior is suppressed if the header has already been set.
	 * <p>As of 5.2, by default is set to {@code false}.
	 */
	// 标识是否输出 Response Headers:Accept-Charset 默认是 true
	public void setWriteAcceptCharset(boolean writeAcceptCharset) {
		this.writeAcceptCharset = writeAcceptCharset;
	}


	@Override
	public boolean supports(Class<?> clazz) {
		return String.class == clazz;
	}

	@Override
	//  将请求报文转换为字符串
	protected String readInternal(Class<? extends String> clazz, HttpInputMessage inputMessage) throws IOException {
		// 通过读取请求报文里的 Content-Type 来获取字符集
		Charset charset = getContentTypeCharset(inputMessage.getHeaders().getContentType());
		// //调用 StreamUtils 工具类的 copyToString 方法来完成转换
		return StreamUtils.copyToString(inputMessage.getBody(), charset);
	}

	@Override
	// 返回字符串的大小(转换为字节数组后的大小)
	// 依赖于 MediaType 提供的字符集
	protected Long getContentLength(String str, @Nullable MediaType contentType) {
		Charset charset = getContentTypeCharset(contentType);
		return (long) str.getBytes(charset).length;
	}


	@Override
	protected void addDefaultHeaders(HttpHeaders headers, String s, @Nullable MediaType type) throws IOException {
		if (headers.getContentType() == null ) {
			if (type != null && type.isConcrete() && type.isCompatibleWith(MediaType.APPLICATION_JSON)) {
				// Prevent charset parameter for JSON..
				headers.setContentType(type);
			}
		}
		super.addDefaultHeaders(headers, s, type);
	}

	@Override
	// 将字符串转换为响应报文
	protected void writeInternal(String str, HttpOutputMessage outputMessage) throws IOException {
		HttpHeaders headers = outputMessage.getHeaders();
		// 输出 Response Headers:Accept-Charset(默认输出)
		if (this.writeAcceptCharset && headers.get(HttpHeaders.ACCEPT_CHARSET) == null) {
			headers.setAcceptCharset(getAcceptedCharsets());
		}
		Charset charset = getContentTypeCharset(headers.getContentType());
		// 调用 StreamUtils 工具类的 copy 方法来完成转换
		StreamUtils.copy(str, charset, outputMessage.getBody());
	}


	/**
	 * Return the list of supported {@link Charset Charsets}.
	 * <p>By default, returns {@link Charset#availableCharsets()}.
	 * Can be overridden in subclasses.
	 * @return the list of accepted charsets
	 */
	// 返回所支持的字符集 默认返回 Charset.availableCharsets() 子类可以覆盖该方法  获得 ContentType 对应的字符集
	protected List<Charset> getAcceptedCharsets() {
		List<Charset> charsets = this.availableCharsets;
		if (charsets == null) {
			charsets = new ArrayList<>(Charset.availableCharsets().values());
			this.availableCharsets = charsets;
		}
		return charsets;
	}

	private Charset getContentTypeCharset(@Nullable MediaType contentType) {
		if (contentType != null && contentType.getCharset() != null) {
			return contentType.getCharset();
		}
		else if (contentType != null && contentType.isCompatibleWith(MediaType.APPLICATION_JSON)) {
			// Matching to AbstractJackson2HttpMessageConverter#DEFAULT_CHARSET
			return StandardCharsets.UTF_8;
		}
		else {
			Charset charset = getDefaultCharset();
			Assert.state(charset != null, "No default charset");
			return charset;
		}
	}

}
