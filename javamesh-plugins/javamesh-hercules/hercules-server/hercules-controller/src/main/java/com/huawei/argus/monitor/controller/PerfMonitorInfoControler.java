package com.huawei.argus.monitor.controller;

import com.alibaba.fastjson.JSONObject;
import com.huawei.argus.common.InfluxDBUtil;
import com.huawei.argus.monitor.service.impl.PerfMonitorConfigService;
import com.huawei.argus.report.service.PerfTestReportService;
import org.influxdb.dto.QueryResult;
import org.ngrinder.common.controller.BaseController;
import org.ngrinder.model.*;
import org.ngrinder.perftest.service.PerfTestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by hwx683090 on 2019/4/23.
 */
@Controller
@RequestMapping("/perftest/perfMonitorInfo")
public class PerfMonitorInfoControler extends BaseController {

	private static final Logger LOGGER = LoggerFactory.getLogger(PerfMonitorInfoControler.class);

	//聚合查询
	private static final String INFLUXDB_NMON_CPU = "SELECT MEAN(DATETIME) AS DATETIME, FIRST(MSGTYPE) AS MSGTYPE, MEAN(CPUs) AS CPUs, MEAN(Sys) AS Sys, MEAN(UserPercent) AS UserPercent FROM NMON_CPU ";
	private static final String INFLUXDB_NMON_MEM = "SELECT MEAN(DATETIME) AS DATETIME ,FIRST(MSGTYPE) AS MSGTYPE, MEAN(used_percent) AS used_percent FROM NMON_MEM ";
	private static final String INFLUXDB_NMON_DISKREAD = "SELECT MEAN(DATETIME) AS DATETIME, FIRST(MSGTYPE) AS MSGTYPE, MEAN(vda) AS vda FROM NMON_DISKREAD ";
	private static final String INFLUXDB_NMON_DISKWRITE = "SELECT MEAN(DATETIME) AS DATETIME, FIRST(MSGTYPE) AS MSGTYPE, MEAN(vda) AS vda FROM NMON_DISKWRITE ";
	private static final String INFLUXDB_NMON_NET = "SELECT MEAN(DATETIME) AS DATETIME, FIRST(MSGTYPE) AS MSGTYPE, MEAN(eth0read) AS eth0read, MEAN(eth0write) AS eth0write FROM NMON_NET ";

	private static final String INFLUXDB_JVM_GC = "SELECT MEAN(DATETIME) AS DATETIME, FIRST(MSGTYPE) AS MSGTYPE, MEAN(collection_count) AS collection_count, MEAN(collection_time) AS collection_time FROM JVM_GC ";
	private static final String INFLUXDB_JVM_THR = "SELECT MEAN(DATETIME) AS DATETIME, FIRST(MSGTYPE) AS MSGTYPE, MEAN(thread_count) AS thread_count, MEAN(total_started_thread_count) AS total_started_thread_count, " +
		"MEAN(peak_thread_count) AS peak_thread_count, MEAN(dead_lock_thread_count) AS dead_lock_thread_count, MEAN(daemon_thread_count) AS daemon_thread_count FROM JVM_THR ";
	private static final String INFLUXDB_JVM_MEM = "SELECT MEAN(DATETIME) AS DATETIME, FIRST(MSGTYPE) AS MSGTYPE, MEAN(heap_memory_usage) AS heap_memory_usage, MEAN(non_heap_memory_usage) AS non_heap_memory_usage FROM JVM_MEM ";
	private static final String INFLUXDB_JVM_CL = "SELECT MEAN(DATETIME) AS DATETIME, FIRST(MSGTYPE) AS MSGTYPE, MEAN(loaded_class_count) AS loaded_class_count, MEAN(total_loaded_class_count) AS total_loaded_class_count,MEAN(un_loaded_class_count) AS un_loaded_class_count FROM JVM_CL ";
	private static final String INFLUXDB_JVM_CPU = "SELECT MEAN(DATETIME) AS DATETIME, FIRST(MSGTYPE) AS MSGTYPE, MEAN(cpu_used_rate) AS cpu_used_rate FROM JVM_CPU ";
	private static final String INFLUXDB_JVM_MP = "SELECT DATETIME, MSGTYPE, MAX(DATETIME) AS last_time, mp_name, usage FROM JVM_MP ";

	private static final String INFLUXDB_JVM_MP_GROUP_BY = " GROUP BY HOSTNAME, mp_name ";
	private static final String INFLUXDB_WHERE_HOSTNAME_DATETIME = " WHERE HOSTNAME = '?' AND DATETIME >= ? AND DATETIME <= ? ";

	private static final String INFLUXDB_GROUP_BY_TIME_PREFIX = " GROUP BY time(";
	private static final String INFLUXDB_GROUP_BY_TIME_SUFFIX = "ms) LIMIT 100 ";

	//实时查询

	private static final String INFLUXDB_NMON_CPU_TESTING = "SELECT DATETIME, MSGTYPE, CPUs, Sys, UserPercent FROM NMON_CPU ";
	private static final String INFLUXDB_NMON_MEM_TESTING = "SELECT DATETIME ,MSGTYPE, used_percent FROM NMON_MEM ";
	private static final String INFLUXDB_NMON_DISKREAD_TESTING = "SELECT DATETIME, MSGTYPE, vda FROM NMON_DISKREAD ";
	private static final String INFLUXDB_NMON_DISKWRITE_TESTING = "SELECT DATETIME, MSGTYPE, vda FROM NMON_DISKWRITE ";
	private static final String INFLUXDB_NMON_NET_TESTING = "SELECT DATETIME, MSGTYPE, eth0read, eth0write FROM NMON_NET ";

	private static final String INFLUXDB_JVM_GC_TESTING = "SELECT DATETIME, MSGTYPE, collection_count, collection_time FROM JVM_GC ";
	private static final String INFLUXDB_JVM_THR_TESTING = "SELECT DATETIME, MSGTYPE, thread_count, total_started_thread_count, peak_thread_count, dead_lock_thread_count, daemon_thread_count FROM JVM_THR ";
	private static final String INFLUXDB_JVM_MEM_TESTING = "SELECT DATETIME, MSGTYPE, heap_memory_usage, non_heap_memory_usage FROM JVM_MEM ";
	private static final String INFLUXDB_JVM_CL_TESTING = "SELECT DATETIME, MSGTYPE, loaded_class_count, total_loaded_class_count,un_loaded_class_count FROM JVM_CL ";
	private static final String INFLUXDB_JVM_CPU_TESTING = "SELECT DATETIME, MSGTYPE, cpu_used_rate FROM JVM_CPU ";
	private static final String INFLUXDB_JVM_MP_TESTING = "SELECT DATETIME, MSGTYPE, MAX(DATETIME) AS last_time, mp_name, usage FROM JVM_MP ";

	private static final String INFLUXDB_WHERE_HOSTNAME_DATETIME_TESTING = " WHERE HOSTNAME = '?' AND DATETIME >= ? AND time > now() - 1m ";

	@Autowired
	public PerfMonitorConfigService perfMonitorConfigService;

	@Autowired
	private PerfTestService perfTestService;

	@Autowired
	private PerfTestReportService perfTestReportService;

	/**
	 * @param monitor 监控配置实例
	 * @return
	 */
	@RequestMapping(value = {"", "/"}, method = RequestMethod.POST)
	@ResponseBody
	public MonitoringConfig createMonitorConfig(User user, @RequestBody MonitoringConfig monitor) {
		return perfMonitorConfigService.createMonitorConfig(monitor);
	}

	/**
	 * 更新监控配置
	 *
	 * @param monitor 监控配置实例
	 * @return
	 */
	@RequestMapping(value = {"", "/"}, method = RequestMethod.PUT)
	@ResponseBody
	public MonitoringConfig updateMonitorConfig(@RequestBody MonitoringConfig monitor) {
		return perfMonitorConfigService.updateMonitorConfig(monitor);
	}


	/**
	 * 查询监控信息
	 *
	 * @param id 压测任务ID
	 * @return 目标主机列表
	 */
	@RequestMapping(value = "/hosts/{id}", method = RequestMethod.GET)
	@ResponseBody
	public String[] getTargetHosts(User user, @PathVariable(value = "id") long id) {
		PerfTest perfTest = perfTestService.getOne(user, id);
		List<String> lstHosts = perfTest.getTargetHostIP();
		return lstHosts.toArray(new String[lstHosts.size()]);
	}

	/**
	 * 查询目标主机监控信息
	 *
	 * @param msgType 消息类型，1表示压测任务机器信息，2表示JVM信息
	 * @param idType  id类型，1表示压测任务id，2表示压测报告id
	 * @param id      压测任务/压测报告ID
	 * @param host    目标主机
	 * @return 目标主机监控信息
	 */
	@RequestMapping(value = "/{msgType}/{idType}/{id}/{host}/", method = RequestMethod.GET)
	@ResponseBody
	public JSONObject getMonitorInfo(User user,
									 @PathVariable(value = "msgType") long msgType,
									 @PathVariable(value = "idType") long idType,
									 @PathVariable(value = "id") long id,
									 @PathVariable(value = "host") String host) {
		long longStartTime;
		String startTime;
		long longFinishTime;
		String finishTime;
		boolean isTesting = false;
		if (idType == 1) {
			PerfTest perfTest = perfTestService.getOne(user, id);
			if (perfTest == null) return null;

			longStartTime = perfTest.getStartTime().getTime();
			startTime = String.valueOf(longStartTime);
			if (perfTest.getStatus().getCategory() == StatusCategory.TESTING || perfTest.getStatus().getCategory() == StatusCategory.PROGRESSING) {
				longFinishTime = new Date().getTime();
				isTesting = true;
			} else {
				longFinishTime = perfTest.getFinishTime().getTime();
			}
			finishTime = String.valueOf(longFinishTime);
		} else if (idType == 2) {
			PerfTestReport perfTestReport = perfTestReportService.getBasicReportByReportId(id);
			if (perfTestReport == null) return null;

			longStartTime = perfTestReport.getStartTime().getTime();
			startTime = String.valueOf(longStartTime);

			longFinishTime = perfTestReport.getFinishTime().getTime();
			finishTime = String.valueOf(longFinishTime);
		} else {
			LOGGER.error("getMonitorInfo Error : 无效的ID类型--" + idType);
			return null;
		}

		String selectSql;
		String whereSql;
		String groupBySql;

		if (isTesting) {
			//压测任务进行过程，非聚合查询
			whereSql = INFLUXDB_WHERE_HOSTNAME_DATETIME_TESTING.replaceFirst("\\?", host).replaceFirst("\\?", startTime);

			if (msgType == 1) {
				selectSql = INFLUXDB_NMON_CPU_TESTING + whereSql + ";"
					+ INFLUXDB_NMON_MEM_TESTING + whereSql + ";"
					+ INFLUXDB_NMON_DISKREAD_TESTING + whereSql + ";"
					+ INFLUXDB_NMON_DISKWRITE_TESTING + whereSql + ";"
					+ INFLUXDB_NMON_NET_TESTING + whereSql;
			} else if (msgType == 2) {
				selectSql = INFLUXDB_JVM_GC_TESTING + whereSql + ";"
					+ INFLUXDB_JVM_THR_TESTING + whereSql + ";"
					+ INFLUXDB_JVM_MEM_TESTING + whereSql + ";"
					+ INFLUXDB_JVM_CL_TESTING + whereSql + ";"
					+ INFLUXDB_JVM_CPU_TESTING + whereSql + ";"
					+ INFLUXDB_JVM_MP_TESTING + whereSql + INFLUXDB_JVM_MP_GROUP_BY;
			} else {
				LOGGER.error("getMonitorInfo Error : 无效的消息类型--" + msgType);
				return null;
			}

			return queryMonitorData(selectSql, longStartTime);
		} else {
			//聚合查询保持数据聚合结果不超过100条
			if ((longFinishTime - longStartTime) > (100 * 1000)) {
				groupBySql = INFLUXDB_GROUP_BY_TIME_PREFIX + (longFinishTime - longStartTime) / 100 + INFLUXDB_GROUP_BY_TIME_SUFFIX;
			} else {
				groupBySql = INFLUXDB_GROUP_BY_TIME_PREFIX + 1000 + INFLUXDB_GROUP_BY_TIME_SUFFIX;
			}

			whereSql = INFLUXDB_WHERE_HOSTNAME_DATETIME.replaceFirst("\\?", host).replaceFirst("\\?", startTime).replaceFirst("\\?", finishTime);

			if (msgType == 1) {
				selectSql = INFLUXDB_NMON_CPU + whereSql + groupBySql + ";"
					+ INFLUXDB_NMON_MEM + whereSql + groupBySql + ";"
					+ INFLUXDB_NMON_DISKREAD + whereSql + groupBySql + ";"
					+ INFLUXDB_NMON_DISKWRITE + whereSql + groupBySql + ";"
					+ INFLUXDB_NMON_NET + whereSql + groupBySql;
			} else if (msgType == 2) {
				selectSql = INFLUXDB_JVM_GC + whereSql + groupBySql + ";"
					+ INFLUXDB_JVM_THR + whereSql + groupBySql + ";"
					+ INFLUXDB_JVM_MEM + whereSql + groupBySql + ";"
					+ INFLUXDB_JVM_CL + whereSql + groupBySql + ";"
					+ INFLUXDB_JVM_CPU + whereSql + groupBySql + ";"
					+ INFLUXDB_JVM_MP + whereSql + INFLUXDB_JVM_MP_GROUP_BY;
			} else {
				LOGGER.error("getMonitorInfo Error : 无效的消息类型--" + msgType);
				return null;
			}

			return queryMonitorData(selectSql, longStartTime);
		}
	}

	public JSONObject queryMonitorData(String selectSql, long longStartTime) {
		QueryResult queryResult = InfluxDBUtil.query(selectSql);

		JSONObject totalJson = new JSONObject();//返回给前端的json对象
		JSONObject detailJson = new JSONObject();//表里的一条记录
		List<String> columns;//每张表的搜索列名
		List<List<Object>> values;//每张表里的所有记录
		List<Object> value;//表里的一条记录
		List detailList;//每一表里的数据存为一个list
		String seriesName = "";//表名
		int columnsSize;
		int valuesSize;
		for (QueryResult.Result result : queryResult.getResults()) {
			if (result.getSeries() == null) continue;
			detailList = new ArrayList<>();
			for (QueryResult.Series series : result.getSeries()) {
				columns = series.getColumns();
				values = series.getValues();
				seriesName = series.getName();
				columnsSize = columns.size();
				valuesSize = values.size();

				for (int i = 0; i < valuesSize; i++) {
					value = values.get(i);
					if (value.get(1) == null) continue;
					detailJson.clear();
					detailJson.put(columns.get(1), Math.round((Double) value.get(1)) - longStartTime);
					for (int j = 3; j < columnsSize; j++) {
						detailJson.put(columns.get(j), formatValue(value.get(j)));
					}

					//JVM_MP的时间和数据特殊处理
					if (("JVM_MP").equals(seriesName)) {
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						java.util.Date lastTime = new Date(detailJson.getDouble("last_time").longValue());
						detailJson.put("last_time", sdf.format(lastTime));

						String[] usage = detailJson.getString("usage").split(",");
						detailJson.put("max", usage[0]);
						detailJson.put("used", usage[1]);
						detailJson.put("init", usage[2]);
						detailJson.put("committed", usage[3]);
						detailJson.remove("usage");
					}
					detailList.add(detailJson.clone());
				}
			}
			totalJson.put(seriesName, ((ArrayList) detailList).clone());
		}

		return totalJson;
	}

	public static String formatValue(Object value) {
		if (value instanceof String) {
			return value.toString();
		} else if (value instanceof Double) {
			BigDecimal bdValue = new BigDecimal((Double) value);
			return bdValue.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
		} else {
			return value.toString();
		}
	}
}
