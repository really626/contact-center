package org.zhongweixian.ivr.tcp;

import com.alibaba.fastjson.JSONObject;
import org.cti.cc.constant.Constant;
import org.cti.cc.entity.Station;
import org.cti.cc.mapper.StationMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import org.zhongweixian.client.AuthorizationToken;
import org.zhongweixian.client.tcp.NettyClient;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by caoliang on 2021/8/7
 */
//@Component
public class TcpClientManager {
    private Logger logger = LoggerFactory.getLogger(TcpClientManager.class);

    @Value("${spring.cloud.nacos.server-addr}")
    private String serverAddr;

    @Autowired
    private StationMapper stationMapper;

    private Map<String, NettyClient> nettyClientMap = new HashMap<>();

    private Map<Integer, ThreadPoolExecutor> executorMap = new ConcurrentHashMap<>();


    @Autowired
    private TcpClientHandler tcpClientHandler;

    /**
     * 建立socket连接
     *
     * @param station
     */
    private void connect(Station station) {
        AuthorizationToken authorizationToken = new AuthorizationToken();
        authorizationToken.setPongTimeout(0);
        JSONObject payload = new JSONObject();
        payload.put("cmd", "login");
        payload.put("stationType", 3);
        payload.put("domain", Constant.HTTP + station.getHost());
        authorizationToken.setPayload(payload.toJSONString());
        NettyClient nettyClient = new NettyClient(station.getApplicationHost(), 7250, authorizationToken, tcpClientHandler);
        nettyClient.setMaxReConnect(2);
        nettyClientMap.put(station.getHost(), nettyClient);
    }

    public void start() {

    }

    /**
     * 停止线程
     */
    public void stop() {
        if (CollectionUtils.isEmpty(nettyClientMap)) {
            return;
        }
        nettyClientMap.forEach((k, v) -> {
            v.close();
        });
    }
}
