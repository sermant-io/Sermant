package com.huawei.argus.monitor.common;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.ngrinder.model.MonitoringConfig;
import org.ngrinder.model.PerfTest;
import org.ngrinder.perftest.repository.PerfTestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * @Author: j00466872
 * @Date: 2019/5/7 17:13
 */
@Component
public class MonitorSwitch {

	private static final Logger LOG = LoggerFactory.getLogger(MonitorSwitch.class);

	@Autowired
	private PerfTestRepository perfTestRepository;

	private boolean requestInHostIps(List<String> targetHostIps, String command, MonitoringConfig monitoringConfig) throws IOException {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		for (String targetHostIp : targetHostIps) {
			try {
				StringBuilder stringBuilder = new StringBuilder();
				stringBuilder.append(String.format("http://%s:8844/%s?host=%s", targetHostIp, command, targetHostIp));
				if (!monitoringConfig.isNmonAll())
					stringBuilder.append("&nmon=false");
				if (!monitoringConfig.isJvmThr())
					stringBuilder.append("&jvm_thread=false");
				if (!monitoringConfig.isJvmCpu())
					stringBuilder.append("&jvm_os=false");
				if (!monitoringConfig.isJvmMem())
					stringBuilder.append("&jvm_mem=false");
				if (!monitoringConfig.isJvmGc())
					stringBuilder.append("&jvm_gc=false");
				if (!monitoringConfig.isJvmMp())
					stringBuilder.append("&jvm_mp=false");
				if (!monitoringConfig.isJvmCl())
					stringBuilder.append("&jvm_cl=false");
				HttpGet httpGet = new HttpGet(stringBuilder.toString());
				LOG.info("Executing request " + httpGet.getRequestLine());

				ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
					@Override
					public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
						int status = response.getStatusLine().getStatusCode();
						if (status >= 200 && status < 300) {
							HttpEntity entity = response.getEntity();
							return entity != null ? EntityUtils.toString(entity) : null;
						} else {
							throw new ClientProtocolException("Unexpected response status: " + status);
						}
					}
				};
				String responseBody = httpClient.execute(httpGet, responseHandler);
				LOG.info("----------------------------------------");
				LOG.info(responseBody);
			} catch (Exception e) {
//				e.printStackTrace();
				LOG.info("Request to " + targetHostIp + " failed.");
			}
			finally {
				httpClient.close();
			}
		}
		return true;
	}

	public boolean startMonitorByPerfTest(PerfTest perfTest) throws IOException {
		List<String> targetHostIps = perfTest.getTargetHostIP();

		return requestInHostIps(targetHostIps, "start", perfTest.getMonitoringConfig());
	}

	public boolean stopMonitorByPerfTest(PerfTest perfTest) throws IOException {
		List<String> targetHostsIps = perfTest.getTargetHostIP();

		return requestInHostIps(targetHostsIps, "stop", perfTest.getMonitoringConfig());
	}

}
