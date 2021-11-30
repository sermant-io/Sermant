package com.huawei.argus.report.service;


import com.alibaba.fastjson.JSONObject;
import com.huawei.argus.report.repository.PerfTestReportRepository;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import net.grinder.util.Pair;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.ngrinder.agent.repository.AgentManagerRepository;
import org.ngrinder.common.controller.BaseController;
import org.ngrinder.model.*;
import org.ngrinder.perftest.service.PerfTestService;
import org.python.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.IOException;
import java.util.*;

import static org.ngrinder.common.util.Preconditions.checkNotEmpty;

@Service
public class PerfTestReportService extends BaseController {

	public static final String PARAM_TEST_CHART_INTERVAL = "chartInterval";
	public static final String PERF_TEST_REPORT_ID = "report_id";

	@Autowired
	private PerfTestService perfTestService;

	@Autowired
	private PerfTestReportRepository perfTestReportRepository;

	@Autowired
	private AgentManagerRepository agentManagerRepository;


	private final Properties mongodbProps = PropertiesLoaderUtils.loadProperties(new ClassPathResource("/mongoDB.properties"));

	public PerfTestReportService() throws IOException {
	}


	//获取并持久化压测报告的图表数据
	@SuppressWarnings("MVCPathVariableInspection")
	public void savePerfGraph(long id, PerfTestReport perfTestReport, String dataType, boolean onlyTotal, int imgWidth) {
		String[] dataTypes = checkNotEmpty(StringUtils.split(dataType, ","), "dataType argument should be provided");
		Map<String, Object> stringObjectMap = savePerfGraphData(id, perfTestReport, dataTypes, onlyTotal, imgWidth);
		Object toJson = JSONObject.toJSON(stringObjectMap);
		//String toJson = toJson(stringObjectMap);
		MongoClient client = new MongoClient(mongodbProps.getProperty("mongo.host"), Integer.parseInt(mongodbProps.getProperty("mongo.port")));
		MongoDatabase clientDatabase = client.getDatabase(mongodbProps.getProperty("mongo.database"));
		MongoCollection<Document> perftest_report = clientDatabase.getCollection(mongodbProps.getProperty("mongo.perfReportTable"));
		Document document = new Document();
		document.append("graph", toJson);
		document.append(PERF_TEST_REPORT_ID, perfTestReport.getId());
		perftest_report.insertOne(document);
		client.close();
	}

	//获取并持久化压测任务的基本数据
	public PerfTestReport saveApiBasicReport(long id, int imgWidth) {
		//PerfTest perftest = getOneWithPermissionCheck(user, id, false);
		PerfTest perftest = getOneWithPermissionCheck(id, false);
		String runTime = perftest.getRuntimeStr();
		return save(perftest, runTime);
	}

	private Map<String, Object> savePerfGraphData(long id, PerfTestReport perfTestReport, String[] dataTypes, boolean onlyTotal, int imgWidth) {
		final PerfTest test = perfTestService.getOne(id);
		int interval = perfTestService.getReportDataInterval(id, dataTypes[0], imgWidth);
		Map<String, Object> resultMap = Maps.newHashMap();
		for (String each : dataTypes) {
			Pair<ArrayList<String>, ArrayList<String>> everyGraphResult = perfTestService.getReportData(id, each, onlyTotal, interval);
			Map<String, Object> dataMap = Maps.newHashMap();
			dataMap.put("labels", everyGraphResult.getFirst());
			dataMap.put("data", everyGraphResult.getSecond());
			resultMap.put(StringUtils.replaceChars(each, "()", ""), dataMap);
		}
		resultMap.put(PARAM_TEST_CHART_INTERVAL, interval * test.getSamplingInterval());
//		resultMap.put(PERF_TEST_REPORT_ID, perfTestReport.getId());
//		System.out.println("resultMap:"+resultMap);
		return resultMap;
	}


	private PerfTest getOneWithPermissionCheck(Long id, boolean withTag) {
		PerfTest perfTest = withTag ? perfTestService.getOneWithTag(id) : perfTestService.getOne(id);

		return perfTest;
	}

	public PerfTestReport save(PerfTest perfTest, String runTime) {

		PerfTestReport perfTestReport = new PerfTestReport();
		perfTestReport.setTestName(perfTest.getTestName());
		perfTestReport.setTagString(perfTest.getTagString());
		perfTestReport.setDescription(perfTest.getDescription());
		perfTestReport.setStatus(perfTest.getStatus());
		perfTestReport.setIgnoreSampleCount(perfTest.getIgnoreSampleCount());
		perfTestReport.setScheduledTime(perfTest.getScheduledTime());
		perfTestReport.setStartTime(perfTest.getStartTime());
		perfTestReport.setFinishTime(perfTest.getFinishTime());
		perfTestReport.setTargetHosts(perfTest.getTargetHosts());
		perfTestReport.setUseRampUp(perfTest.getUseRampUp());
		perfTestReport.setRampUpType(perfTest.getRampUpType());
		perfTestReport.setThreshold(perfTest.getThreshold());
		perfTestReport.setScriptName(perfTest.getScriptName());
		perfTestReport.setDuration(perfTest.getDuration());
		perfTestReport.setRunCount(perfTest.getRunCount());
		perfTestReport.setAgentCount(perfTest.getAgentCount());
		perfTestReport.setVuserPerAgent(perfTest.getVuserPerAgent());
		perfTestReport.setProcesses(perfTest.getProcesses());
		perfTestReport.setRampUpInitCount(perfTest.getRampUpInitCount());
		perfTestReport.setRampUpInitSleepTime(perfTest.getRampUpInitSleepTime());
		perfTestReport.setRampUpStep(perfTest.getRampUpStep());
		perfTestReport.setRampUpIncrementInterval(perfTest.getRampUpIncrementInterval());
		perfTestReport.setThreads(perfTest.getThreads());
		perfTestReport.setTests(perfTest.getTests());
		perfTestReport.setErrors(perfTest.getErrors());
		perfTestReport.setMeanTestTime(perfTest.getMeanTestTime());
		perfTestReport.setTps(perfTest.getTps());
		perfTestReport.setPeakTps(perfTest.getPeakTps());
		perfTestReport.setProgressMessage(perfTest.getProgressMessage());
		perfTestReport.setTestComment(perfTest.getTestComment());
		perfTestReport.setScriptRevision(perfTest.getScriptRevision());
		perfTestReport.setRegion(perfTest.getRegion());
		perfTestReport.setSamplingInterval(perfTest.getSamplingInterval());
		perfTestReport.setParam(perfTest.getParam());
		perfTestReport.setCreatedDate(perfTest.getCreatedDate());
		perfTestReport.setLastModifiedDate(perfTest.getLastModifiedDate());
		perfTestReport.setPerfTestId(perfTest.getId());
		perfTestReport.setRunTime(runTime);
		perfTestReport.setCreatedUser(perfTest.getCreatedUser());
		perfTestReport.setLastModifiedUser(perfTest.getLastModifiedUser());
		perfTestReport.setType(perfTest.getPerfScene() == null ? null : perfTest.getPerfScene().getType());
		perfTestReport.setAgentIds(perfTest.getAgentIds());

		return perfTestReportRepository.save(perfTestReport);
	}

	public Page<PerfTestReport> getApiBasicReport(Pageable pageable) {

		//Sort sort_id = new Sort(Sort.Direction.DESC, "id");
		//List<PerfTestReport> perfTestReports = perfTestReportRepository.findAll(sort_id);

		//注意！PerfTestReport中user是个对象，id为user的id，userId是名字:admin;
		Page<PerfTestReport> perfTestReports = perfTestReportRepository.findAll(pageable);
		return perfTestReports;
	}


	//根据userid分页查询
	public Page<PerfTestReport> getBasicReportByUserId(Pageable pageable, User user, String query) {

		Specifications<PerfTestReport> spec = Specifications.where(idEmptyPredicate());
		// User can see only his own test
		if (user.getRole().equals(Role.USER)) {
			spec = spec.and(createdBy(user));
		}
		if (StringUtils.isNotBlank(query)) {
			spec = spec.and(likeTestNameOrDescription(query));
		}

		Page<PerfTestReport> PerfTestReports = perfTestReportRepository.findAll(spec, pageable);
		return PerfTestReports;
	}

	private static Specification<PerfTestReport> idEmptyPredicate() {
		return new Specification<PerfTestReport>() {
			@Override
			public Predicate toPredicate(Root<PerfTestReport> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return root.get("id").isNotNull();
			}
		};
	}

	private static Specification<PerfTestReport> createdBy(final User user) {
		return new Specification<PerfTestReport>() {
			@Override
			public Predicate toPredicate(Root<PerfTestReport> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.or(cb.equal(root.get("createdUser"), user));
			}
		};
	}

	public static Specification<PerfTestReport> likeTestNameOrDescription(final String queryString) {
		return new Specification<PerfTestReport>() {
			@Override
			public Predicate toPredicate(Root<PerfTestReport> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				String queryStr = ("%" + queryString + "%").toLowerCase();
				return cb.or(cb.like(cb.lower(root.get("testName").as(String.class)), queryStr),
					cb.like(root.get("description").as(String.class), queryStr));
			}
		};
	}


	public PerfTestReport getBasicReportByReportId(Long id) {
		PerfTestReport thisPerfTestReport = perfTestReportRepository.findOne(id);
		return thisPerfTestReport;
	}

	public JSONObject getAllReportByReportId(Long id) {
		JSONObject jsonObject = new JSONObject();
		PerfTestReport thisPerfTestReport = perfTestReportRepository.findOne(id);
		jsonObject.put("PerfTestReport", thisPerfTestReport);
		String agentIdsStr = thisPerfTestReport.getAgentIds();
		if (StringUtils.isNotBlank(agentIdsStr)) {
			String[] agentIdsArr = agentIdsStr.split(",");
			List<String> agentIdsList = Arrays.asList(agentIdsArr);
			Collection<AgentInfo> agentInfos = agentManagerRepository.getSelectedAgents(agentIdsList);
			jsonObject.put("agentInfo", agentInfos);
		} else {
			jsonObject.put("agentInfo", null);
		}


		return jsonObject;
	}


	public JSONObject getPerfGraphDataByReportId(Long id) {
		//从数据库取出原始的总图表数据json
		MongoClient client = new MongoClient(mongodbProps.getProperty("mongo.host"), Integer.parseInt(mongodbProps.getProperty("mongo.port")));
		MongoDatabase clientDatabase = client.getDatabase(mongodbProps.getProperty("mongo.database"));
		MongoCollection<Document> perftest_report = clientDatabase.getCollection(mongodbProps.getProperty("mongo.perfReportTable"));
		//按ip条件查询
		BasicDBObject b = new BasicDBObject();
		b.append("report_id", id);
		//不要_id这个列
		BasicDBObject e = new BasicDBObject();
		e.append("_id", 0);
		Document d = perftest_report.find(b).projection(e).first();
		JSONObject json = (JSONObject) JSONObject.toJSON(d);
		client.close();

		//新建List存放各个图表纵轴数据
		List<Integer> errorsSplit = new ArrayList<>();

		List<Integer> vuserSplit = new ArrayList<>();

		List<Double> tpsSplit = new ArrayList<>();

		List<Double> meantimeSplit = new ArrayList<>();

		List<Double> firsttimeSplit = new ArrayList<>();


		Map graph = (Map) json.get("graph");

		Map errors = (Map) graph.get("Errors");
		List errorsdata = (List) errors.get("data");
		String s1 = (String) errorsdata.get(0);
		//将字符串数组split转换成int数组
		String[] split1 = s1.substring(1, s1.length() - 1).split(",");
		int[] errorsArray = new int[split1.length];
		for (int i=0;i<split1.length;i++) {
			if (split1[i].equals("null")){
				split1[i] = split1[i].replace("null", "0");
			}
			errorsArray[i] = Integer.parseInt(split1[i]);
		}

		Map vuser = (Map) graph.get("Vuser");
		List vuserdata = (List) vuser.get("data");
		String s2 = (String) vuserdata.get(0);
		String[] split2 = s2.substring(1, s2.length() - 1).split(",");
		int[] vuserArray = new int[split2.length];
		for (int i=0;i<split2.length;i++) {
			if (split2[i].equals("null")){
				split2[i] = split2[i].replace("null", "0");
			}
			vuserArray[i] = Integer.parseInt(split2[i]);
		}

		Map tps = (Map) graph.get("TPS");
		List tpsdata = (List) tps.get("data");
		String s3 = (String) tpsdata.get(0);
		String[] split3 = s3.substring(1, s3.length() - 1).split(",");
		double[] tpsArray = new double[split3.length];
		for (int i=0;i<split3.length;i++) {
			if (split3[i].equals("null")){
				split3[i] = split3[i].replace("null", "0");
			}
			tpsArray[i] =  Double.parseDouble(split3[i]);
		}

		Map meantime = (Map) graph.get("Mean_Test_Time_ms");
		List meantimedata = (List) meantime.get("data");
		String s4 = (String) meantimedata.get(0);
		String[] split4 = s4.substring(1, s4.length() - 1).split(",");
		double[] meantimeArray = new double[split4.length];
		for (int i=0;i<split4.length;i++) {
			if (split4[i].equals("null")){
				split4[i] = split4[i].replace("null", "0");
			}
			meantimeArray[i] = Double.parseDouble(split4[i]);
		}

		Map firsttime = (Map) graph.get("Mean_time_to_first_byte");
		List firsttimedata = (List) firsttime.get("data");
		String s5 = (String) firsttimedata.get(0);
		String[] split5 = s5.substring(1, s5.length() - 1).split(",");
		double[] firsttimeArray = new double[split5.length];
		for (int i=0;i<split5.length;i++) {
			if (split5[i].equals("null")){
				split5[i] = split4[i].replace("null", "0");
			}
			firsttimeArray[i] = Double.parseDouble(split5[i]);
		}

		//当图表数据超过120个值时，图表按规则压缩
		if (errorsArray.length >= 120) {
			int interval = (int) Math.round((double) errorsArray.length / 120);
			int point_size = (int) Math.ceil((double) errorsArray.length / interval);

			for (int i = 0; i < point_size; i++) {
				int j = 0;
				int sum = 0;
				while (j < interval && i * interval + j < split1.length) {
					sum += errorsArray[i * interval + j];
					j++;
				}
				errorsSplit.add(Math.round(sum / j));
			}

			errors.put("data", errorsSplit);
			graph.put("Errors", errors);
			graph.put("coefficient",interval);
			json.put("graph", graph);

			for (int i = 0; i < point_size; i++) {
				int j = 0;
				int sum = 0;
				while (j < interval && i * interval + j < split1.length) {
					sum += vuserArray[i * interval + j];
					j++;
				}
				vuserSplit.add(Math.round(sum / j));
			}

			vuser.put("data", vuserSplit);
			graph.put("Vuser", vuser);
			json.put("graph", graph);

			for (int i = 0; i < point_size; i++) {
				int j = 0;
				double sum = 0;
				while (j < interval && i * interval + j < split1.length) {
					sum += tpsArray[i * interval + j];
					j++;
				}
				tpsSplit.add((double)Math.round((sum / j)*100)/100);
			}
			tps.put("data", tpsSplit);
			graph.put("TPS", tps);
			json.put("graph", graph);

			for (int i = 0; i < point_size; i++) {
				int j = 0;
				double sum = 0;
				while (j < interval && i * interval + j < split1.length) {
					sum += meantimeArray[i * interval + j];
					j++;
				}
				meantimeSplit.add((double)Math.round((sum / j)*100)/100);
			}
			meantime.put("data", meantimeSplit);
			graph.put("Mean_Test_Time_ms", meantime);
			json.put("graph", graph);

			for (int i = 0; i < point_size; i++) {
				int j = 0;
				double sum = 0;
				while (j < interval && i * interval + j < split1.length) {
					sum += firsttimeArray[i * interval + j];
					j++;
				}
				firsttimeSplit.add((double)Math.round((sum / j)*100)/100);
			}
			firsttime.put("data", firsttimeSplit);
			graph.put("Mean_time_to_first_byte", firsttime);
			json.put("graph", graph);

			return json;
		} else {
            //正常情况直接返回原始数据
			errors.put("data", errorsArray);
			graph.put("Errors", errors);
			vuser.put("data", vuserArray);
			graph.put("Vuser", vuser);
			graph.put("coefficient",1);
			tps.put("data", tpsArray);
			graph.put("TPS", tps);
			meantime.put("data", meantimeArray);
			graph.put("Mean_Test_Time_ms", meantime);
			firsttime.put("data", firsttimeArray);
			graph.put("Mean_time_to_first_byte", firsttime);
			json.put("graph", graph);
			return json;
		}
	}

	;

	public void deleteBasicReportByReportId(Long id) {
		//删除报告基本数据
		perfTestReportRepository.delete(id);
		//删除报告详细数据（图表）
		MongoClient client = new MongoClient(mongodbProps.getProperty("mongo.host"), Integer.parseInt(mongodbProps.getProperty("mongo.port")));
		MongoDatabase clientDatabase = client.getDatabase(mongodbProps.getProperty("mongo.database"));
		MongoCollection<Document> perftest_report = clientDatabase.getCollection(mongodbProps.getProperty("mongo.perfReportTable"));
		BasicDBObject b = new BasicDBObject();
		b.append("report_id", id);
		perftest_report.findOneAndDelete(b);
		client.close();
	}


}
