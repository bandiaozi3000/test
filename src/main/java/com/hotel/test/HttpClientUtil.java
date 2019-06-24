package com.hotel.test;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Map.Entry;


/**
 * httpclient 工具类
 *
 * @author sk.zhang
 * @date 2018/10/10
 */
@Component
public class HttpClientUtil {

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);

    /**
     * 设置超时毫秒数
     */
    private static final int CONNECT_TIMEOUT = 15000;

    /**
     * 设置传输毫秒数
     */
    private static final int SOCKET_TIMEOUT = 15000;

    /**
     * 获取请求超时毫秒数
     */
    private static final int REQUESTCONNECT_TIMEOUT = 15000;

    /**
     * 最大连接数
     */
    private static final int CONNECT_TOTAL = 100;

    /**
     * 设置每个路由的基础连接数
     */
    private static final int CONNECT_ROUTE = 20;

    /**
     * 设置重用连接时间
     */
    private static final int VALIDATE_TIME = 30000;

    private static final String RESPONSE_CONTENT = "通信失败";

    private static PoolingHttpClientConnectionManager manager = null;

    private static CloseableHttpClient client = null;

    static {
        ConnectionSocketFactory csf = PlainConnectionSocketFactory.getSocketFactory();
        LayeredConnectionSocketFactory lsf = createSSLConnSocketFactory();
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", csf).register("https", lsf).build();
        manager = new PoolingHttpClientConnectionManager(registry);
        manager.setMaxTotal(CONNECT_TOTAL);
        manager.setDefaultMaxPerRoute(CONNECT_ROUTE);
        manager.setValidateAfterInactivity(VALIDATE_TIME);
        SocketConfig config = SocketConfig.custom().setSoTimeout(SOCKET_TIMEOUT).build();
        manager.setDefaultSocketConfig(config);
        RequestConfig requestConf = RequestConfig.custom().setConnectTimeout(CONNECT_TIMEOUT)
                .setConnectionRequestTimeout(REQUESTCONNECT_TIMEOUT).setSocketTimeout(SOCKET_TIMEOUT).build();
        client = HttpClients.custom().setConnectionManager(manager).setDefaultRequestConfig(requestConf).setRetryHandler(
                //实现了HttpRequestRetryHandler接口的
                //public boolean retryRequest(IOException exception, int executionCount, HttpContext context)方法
                (exception, executionCount, context) -> {
                    if (executionCount >= 3) {
                        return false;
                    }
                    //如果服务器断掉了连接那么重试
                    if (exception instanceof NoHttpResponseException) {
                        return true;
                    }
                    //不重试握手异常
                    if (exception instanceof SSLHandshakeException) {
                        return false;
                    }
                    //IO传输中断重试
                    if (exception instanceof InterruptedIOException) {
                        return true;
                    }
                    //未知服务器
                    if (exception instanceof UnknownHostException) {
                        return false;
                    }
                    //超时就重试
                    if (exception instanceof ConnectTimeoutException) {
                        return true;
                    }
                    if (exception instanceof SSLException) {
                        return false;
                    }
                    HttpClientContext cliContext = HttpClientContext.adapt(context);
                    HttpRequest request = cliContext.getRequest();
                    if (!(request instanceof HttpEntityEnclosingRequest)) {
                        return true;
                    }
                    return false;
                }).build();
        if (manager != null && manager.getTotalStats() != null) {
            logger.info("客户池状态：{}", manager.getTotalStats().toString());
        }
    }

    private static SSLConnectionSocketFactory createSSLConnSocketFactory() {
        SSLContext context;
        context = SSLContexts.createSystemDefault();
        return new SSLConnectionSocketFactory(context, NoopHostnameVerifier.INSTANCE);
    }

    /**
     * 执行请求
     *
     * @param method
     * @return
     */
    private String res(HttpRequestBase method) {
        HttpClientContext context = HttpClientContext.create();
        CloseableHttpResponse response = null;
        String content = RESPONSE_CONTENT;
        try {
            //执行GET/POST请求
            response = client.execute(method, context);
            //获取响应实体
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                //Charset charset = ContentType.getOrDefault(entity).getCharset();
                content = EntityUtils.toString(entity, "utf-8");
                EntityUtils.consume(entity);
            }
        } catch (ConnectTimeoutException cte) {
            logger.error("请求连接超时，由于:{} ", cte.getLocalizedMessage());
            cte.printStackTrace();
        } catch (SocketTimeoutException ste) {
            logger.error("请求通信超时，由于:{}", ste.getLocalizedMessage());
            ste.printStackTrace();
        } catch (ClientProtocolException cpe) {
            logger.error("协议错误（比如构造HttpGet对象时传入协议不对(将'http'写成'htp')or响应内容不符合），由于{} ", cpe.getLocalizedMessage());
            cpe.printStackTrace();
        } catch (IOException ie) {
            logger.error("实体转换异常或者网络异常 由于:{}", ie.getLocalizedMessage());
            ie.printStackTrace();
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                logger.error("响应关闭异常,由于:{}", e.getLocalizedMessage());
            }
            if (method != null) {
                method.releaseConnection();
            }
        }
        return content;
    }

    /**
     * get请求
     *
     * @param url
     * @return
     */
    public JSONObject get(String url) {
        HttpGet get = new HttpGet(url);
        return JSONObject.parseObject(res(get));
    }

    public String get(String url, String cookie) {
        HttpGet get = new HttpGet(url);
        if (StringUtils.isNotBlank(cookie)) {
            get.addHeader("cookie", cookie);
        }
        return res(get);
    }

    public byte[] getAsByte(String url) {
        return get(url).toString().getBytes();
    }

    public String getHeaders(String url, String cookie, String headerName) {
        HttpGet get = new HttpGet(url);
        if (StringUtils.isNotBlank(cookie)) {
            get.addHeader("cookie", cookie);
        }
        res(get);
        Header[] headers = get.getHeaders(headerName);
        return headers == null ? null : headers.toString();
    }

    public String getWithRealHeader(String url) {
        HttpGet get = new HttpGet(url);
        get.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;");
        get.addHeader("Accept-Language", "zh-cn");
        get.addHeader("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.0.3) Gecko/2008092417 Firefox/3.0.3");
        get.addHeader("Keep-Alive", "300");
        get.addHeader("Connection", "Keep-Alive");
        get.addHeader("Cache-Control", "no-cache");
        return res(get);
    }

    public String post(String url, Map<String, String> param, String cookie) {
        HttpPost post = new HttpPost(url);
        param.keySet().forEach(key -> {
            String value = param.get(key);
            if (value != null) {
                post.addHeader(key, value);
            }
        });
        if (StringUtils.isNotBlank(cookie)) {
            post.addHeader("cookie", cookie);
        }
        return res(post);
    }

    public String post(String url, String data) {
        HttpPost post = new HttpPost(url);
        if (StringUtils.isNotBlank(data)) {
            post.addHeader("Content-Type", "application/json");
        }
        post.setEntity(new StringEntity(data, ContentType.APPLICATION_JSON));
        return res(post);
    }

//    public String post(String url, String httpEntity, ContentType contentType, String header) {
//        HttpPost post = new HttpPost(url);
//        post.addHeader("Content-Type", header);
//        post.setEntity(httpEntity);
//        return res(post);
//    }

    public String post(String url, Map<String, String> param, String cookie, Map<String, String> headers) {
        HttpPost post = new HttpPost(url);
        StringBuilder reqEntity = new StringBuilder();
        for (Entry<String, String> entry : param.entrySet()) {
            post.addHeader(entry.getKey(), entry.getValue());
            try {
                reqEntity.append(entry.getKey()).append("=")
                        .append(URLEncoder.encode(entry.getValue(), "utf-8")).append("&");
            } catch (UnsupportedEncodingException e) {
                logger.error("请求实体转换异常，不支持的字符集，由于:{}", e.getLocalizedMessage());
                e.printStackTrace();
            }
        }
        try {
            post.setEntity(new StringEntity(reqEntity.toString()));
        } catch (UnsupportedEncodingException e) {
            logger.error("请求设置实体异常，不支持的字符集，由于:{}", e.getLocalizedMessage());
            e.printStackTrace();
        }
        if (StringUtils.isNotEmpty(cookie)) {
            post.addHeader("cookie", cookie);
        }
        return res(post);
    }

    /*public static void main(String[] args) {
        //String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=wx946c5e4b2d1d1d5a&secret=e9462d1450c32c3266666e9206fa5a2f&code=9999999&grant_type=authorization_code";
        String url = "http://api.map.baidu.com/place/v2/suggestion?query=美恒大厦&region=上海市&city_limit=true&output=json&ak=ukfGWsZpd6x6KpXv4m6A5sBnwAVn3okK";
        HttpClientUtil httpClientUtil = new HttpClientUtil();
        JSONObject result = httpClientUtil.get(url);
        System.err.println(result);
    }*/

}
