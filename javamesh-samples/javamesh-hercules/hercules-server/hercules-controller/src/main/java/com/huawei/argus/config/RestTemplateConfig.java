/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.argus.config;

import org.apache.http.Header;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 功能描述：restTemplate配置
 *
 * @author z30009938
 * @since 2021-11-03
 */
@Configuration
public class RestTemplateConfig {
	/**
	 * 日志
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(RestTemplateConfig.class);

	@Bean
	public RestTemplate restTemplate() {
		// 添加内容转换器,使用默认的内容转换器
		RestTemplate restTemplate = new RestTemplate(httpRequestFactory());
		// 设置编码格式为UTF-8
		List<HttpMessageConverter<?>> converterList = restTemplate.getMessageConverters();
		HttpMessageConverter<?> converterTarget = null;
		for (HttpMessageConverter<?> item : converterList) {
			if (item.getClass() == StringHttpMessageConverter.class) {
				converterTarget = item;
				break;
			}
		}
		if (converterTarget != null) {
			converterList.remove(converterTarget);
		}
		HttpMessageConverter<?> converter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
		converterList.add(1, converter);

		LOGGER.info("-----restTemplate-----初始化完成");
		return restTemplate;
	}

	public ClientHttpRequestFactory httpRequestFactory() {

		return new HttpComponentsClientHttpRequestFactory(httpClient());

	}

	public HttpClient httpClient() {
		// 长连接保持30秒
		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(30, TimeUnit.SECONDS);

		//设置整个连接池最大连接数 根据自己的场景决定
		connectionManager.setMaxTotal(500);

		//同路由的并发数,路由是对maxTotal的细分
		connectionManager.setDefaultMaxPerRoute(500);

		//requestConfig
		RequestConfig requestConfig = RequestConfig.custom()
			//服务器返回数据(response)的时间，超过该时间抛出read timeout
			.setSocketTimeout(10000)
			//连接上服务器(握手成功)的时间，超出该时间抛出connect timeout
			.setConnectTimeout(5000)
			//从连接池中获取连接的超时时间，超过该时间未拿到可用连接，会抛出org.apache.http.conn.ConnectionPoolTimeoutException: Timeout waiting for connection from pool
			.setConnectionRequestTimeout(500)
			.build();
		//headers
		List<Header> headers = new ArrayList<>();
		headers.add(new BasicHeader("Connection", "Keep-Alive"));
		headers.add(new BasicHeader("Content-type", "application/json;charset=UTF-8"));

		return HttpClientBuilder.create()
			.setDefaultRequestConfig(requestConfig)
			.setConnectionManager(connectionManager)
			.setDefaultHeaders(headers)

			// 保持长连接配置，需要在头添加Keep-Alive
			.setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy())

			//重试次数，默认是3次，没有开启
			.setRetryHandler(new DefaultHttpRequestRetryHandler(2, true))
			.build();
	}
}
