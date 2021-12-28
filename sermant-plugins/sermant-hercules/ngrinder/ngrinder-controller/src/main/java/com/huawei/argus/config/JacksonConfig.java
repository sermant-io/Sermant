/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.argus.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.validation.Validator;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

/**
 * 功能描述：
 *
 *
 * @since 2021-11-18
 */
@Configuration
public class JacksonConfig implements WebMvcConfigurer {
	@Override
	public void addFormatters(FormatterRegistry formatterRegistry) {

	}

	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> list) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
		Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder()
			.indentOutput(true)
			.dateFormat(simpleDateFormat)
			.timeZone(TimeZone.getTimeZone("Asia/Shanghai"));
		list.add(new MappingJackson2HttpMessageConverter(builder.build()));
	}

	@Override
	public void extendMessageConverters(List<HttpMessageConverter<?>> list) {

	}

	@Override
	public Validator getValidator() {
		return null;
	}

	@Override
	public void configureContentNegotiation(ContentNegotiationConfigurer contentNegotiationConfigurer) {

	}

	@Override
	public void configureAsyncSupport(AsyncSupportConfigurer asyncSupportConfigurer) {

	}

	@Override
	public void configurePathMatch(PathMatchConfigurer pathMatchConfigurer) {

	}

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> list) {

	}

	@Override
	public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> list) {

	}

	@Override
	public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> list) {

	}

	@Override
	public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> list) {

	}

	@Override
	public void addInterceptors(InterceptorRegistry interceptorRegistry) {

	}

	@Override
	public MessageCodesResolver getMessageCodesResolver() {
		return null;
	}

	@Override
	public void addViewControllers(ViewControllerRegistry viewControllerRegistry) {

	}

	@Override
	public void configureViewResolvers(ViewResolverRegistry viewResolverRegistry) {

	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry resourceHandlerRegistry) {

	}

	@Override
	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer defaultServletHandlerConfigurer) {

	}

	@Override
	public void addCorsMappings(CorsRegistry corsRegistry) {

	}
}
