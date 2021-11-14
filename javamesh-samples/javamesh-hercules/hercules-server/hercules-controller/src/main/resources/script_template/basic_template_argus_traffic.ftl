import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSession
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import java.security.cert.CertificateException
import java.security.cert.X509Certificate

import static net.grinder.script.Grinder.grinder
import static org.junit.Assert.*
import static org.hamcrest.Matchers.*
import net.grinder.plugin.http.HTTPRequest
import net.grinder.plugin.http.HTTPPluginControl
import net.grinder.script.GTest
import net.grinder.script.Grinder
import net.grinder.scriptengine.groovy.junit.GrinderRunner
import net.grinder.scriptengine.groovy.junit.annotation.BeforeProcess
import net.grinder.scriptengine.groovy.junit.annotation.BeforeThread
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith

import java.util.Date
import java.util.List
import java.util.ArrayList

import HTTPClient.Cookie
import HTTPClient.CookieModule
import HTTPClient.HTTPResponse
import HTTPClient.NVPair
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import java.util.Random

@RunWith(GrinderRunner)
class TestRunner {
    public static GTest test
    public static HTTPRequest request
    public static NVPair[] headers = []
    public static NVPair[] params = []
    public static Cookie[] cookies = []
    public static File file = new File("sceneJsonData.json")
    public static traffic_config = file.getText()
    public static traffic_proportion
    public static traffic_host
    public static count_sum
    public static count_num
    public static random = new Random()
    public static jsonSlurper = new JsonSlurper()
    public static jsonOutput = new JsonOutput()
    public static header
    public static traffics
    public static api
    public static body
    public static method
    public static random_num
    public static HTTPResponse result
    public static body_length = 0
    public static HttpURLConnection conn

    @BeforeProcess
    public static void beforeProcess() {
        File directory = new File("")
        println directory.getCanonicalPath()
        println directory.getAbsolutePath()

        HTTPPluginControl.getConnectionDefaults().timeout = 6000
        test = new GTest(1, "script_generate")
        request = new HTTPRequest()

        SSLContext sslcontext = SSLContext.getInstance("SSL","SunJSSE");
        MyX509TrustManager myX509 = new MyX509TrustManager()
        sslcontext.init(null, [myX509] as TrustManager[], new java.security.SecureRandom());

        HostnameVerifier ignoreHostnameVerifier = new HostnameVerifier() {
            public boolean verify(String s, SSLSession sslsession) {
                System.out.println("WARNING: Hostname is not matched for cert.");
                return true;
            }
        };
        HttpsURLConnection.setDefaultHostnameVerifier(ignoreHostnameVerifier);
        HttpsURLConnection.setDefaultSSLSocketFactory(sslcontext.getSocketFactory());

        grinder.logger.info("before process.")
    }

    @BeforeThread
    public void beforeThread() {
        test.record(this, "test")
        grinder.statistics.delayReports=true;
        grinder.logger.info("before thread.");
    }

    @Before
    public void before() {
        traffics = jsonSlurper.parseText(traffic_config)
        count_sum = traffics.count_sum
        traffic_host = traffics.traffic_host
        traffic_proportion = traffics.traffic_proportion
        header = traffics.header
        random_num = random.nextInt(count_sum) + 1
        count_num = 0
        for (traffic in traffic_proportion){
            if ((random_num > count_num) & (random_num <= (count_num + traffic.count))){
                api = traffic_host + traffic.traffic_url
                method = traffic.http_method
                if (method == 'POST' || method == 'PUT'){
                    body_length = 0
                    for (i in traffic.request_body){
                        body_length = body_length + 1
                    }
                    body = traffic.request_body[random.nextInt(body_length)]
                    body = jsonOutput.toJson(body)
                }
                break
            }
            count_num = count_num + traffic.count
        }

        URL url = new URL(api)

        conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod(method);
		conn.setConnectTimeout(5000)
		conn.setReadTimeout(20000)
        conn.setInstanceFollowRedirects(false);
        for (i in header){
            conn.setRequestProperty(i.getKey(),header.get(i.getKey()))
        }
        if (method == "POST" || method == "PUT"){
            conn.setDoOutput(true)
            conn.setDoInput(true)
            OutputStream os = conn.getOutputStream()
            os.write(body.getBytes())
            os.flush()
            os.close()
        }


        cookies.each { CookieModule.addCookie(it, HTTPPluginControl.getThreadHTTPClientContext()) }
        grinder.logger.info("before thread. init headers and cookies");
    }

    @Test
    public void test(){
        grinder.logger.info(method + " " + api)
        conn.connect()
        grinder.logger.info(conn.getResponseCode().toString())
        if (conn.getResponseCode() == 301 || conn.getResponseCode() == 302) {
            grinder.logger.warn("Warning. The response may not be correct. The response code was {}.", conn.getResponseCode());
        } else {
            assertThat(conn.getResponseCode(), greaterThan(199));
        }
    }

    static class MyX509TrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] certificates, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] ax509certificate, String s) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            // TODO Auto-generated method stub
            return null;
        }


    }

    public static String getReturn(HttpURLConnection connection) throws IOException {
        StringBuffer buffer = new StringBuffer();
        InputStream inputStream = connection.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String str = null;
        while ((str = bufferedReader.readLine()) != null) {
            buffer.append(str);
        }
        String result = buffer.toString();
        return result;

    }
}
