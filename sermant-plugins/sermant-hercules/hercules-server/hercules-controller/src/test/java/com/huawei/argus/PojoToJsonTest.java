package com.huawei.argus;

import com.google.gson.Gson;
import org.junit.Test;
import org.ngrinder.model.PerfTest;
import org.ngrinder.model.Role;
import org.ngrinder.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PojoToJsonTest {

	Gson gson = new Gson();
	@Test
	public void tt() {
		User user = new User("admin", "admin", "qqq", "qqq@qq.com", Role.ADMIN);
		user.setId(1L);
		PerfTest test = new PerfTest(user);
		test.init();
		test.setTestName("testName-0001");
		test.setTargetHosts("127.0.0.1");
		String json = gson.toJson(test);
		System.out.println("json:" + json);
		PerfTest perfTest = gson.fromJson(json, PerfTest.class);
		System.out.println("test:" + test);
		String parseStr = TT.modelStrToJsonStr(test.toString());
		System.out.println("parseStr:" + parseStr);
	}

	@Test
	public void perfTestToJson() {
		StringBuilder sb = new StringBuilder();
		sb.append("{").append("}");
		System.out.println(sb.toString());
		PerfTest perfTest1 = gson.fromJson(sb.toString(), PerfTest.class);
		System.out.println(perfTest1);
	}

	@Test
	public void modelToJson() {
		String content = "org.ngrinder.model.PerfTest@2286778[testName=testName-0001,tagString=,description=,status=SAVED,ignoreSampleCount=0,scheduledTime=<null>,startTime=<null>,finishTime=<null>,targetHosts=127.0.0.1,sendMail=<null>,useRampUp=false,rampUpType=PROCESS,threshold=D,scriptName=,duration=60000,runCount=0,agentCount=0,vuserPerAgent=1,processes=1,rampUpInitCount=0,rampUpInitSleepTime=0,rampUpStep=1,rampUpIncrementInterval=1000,threads=1,tests=<null>,errors=<null>,meanTestTime=<null>,testTimeStandardDeviation=<null>,tps=<null>,peakTps=<null>,port=0,testErrorCause=<null>,distributionPath=<null>,progressMessage=,lastProgressMessage=,testComment=,scriptRevision=-1,stopRequest=<null>,region=NONE,safeDistribution=false,dateString=<null>,grinderProperties=<null>,runningSample=<null>,agentState=<null>,monitorState=<null>,samplingInterval=2,param=,monitoringConfig=<null>,perfScene=<null>,agentIds=<null>,perfTestReportId=<null>,userId=<null>,createdDate=<null>,createdUser=User[ID=1,name=admin,Role=ADMIN],lastModifiedDate=<null>,lastModifiedUser=<null>,id=<null>]" +
			"Process finished with exit code 0";
		String[] split = content.split(",");
		StringBuilder json = new StringBuilder();
		for (int i = 0, len = split.length; i < len; i++) {

		}
	}

	@Test
	public void listToJSON() {
		List<PerfTest> perfTestList = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			User user = new User("admin" + i, "admin" + i, "qqq", "qqq@qq.com", Role.ADMIN);
			user.setId((long)i);
			PerfTest test = new PerfTest(user);
			test.init();
			test.setTestName("testName-000" + i);
			test.setTargetHosts("127.0.0.1");
			perfTestList.add(test);
		}
		List<Object> objectList = Arrays.asList(perfTestList.toArray());
		System.out.println("perfTestList: " + TT.listToJsonArray(objectList));
		Page<PerfTest> page = new PageImpl<PerfTest>(perfTestList);
		System.out.println("page:" + TT.pageToJson(page));
	}

	@Test
	public void  testFile() {

	}

}
