package br.eng.luan;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.lock.FencedLock;


public class HazelcastService {

    private HazelcastInstance hazelcastInstance;

    private static HazelcastService hazelcastService;

    public HazelcastService(String hazelcastHost) {
        ClientConfig clientConfig = new ClientConfig();
        String[] members = {hazelcastHost};

        clientConfig.getNetworkConfig().addAddress(members);
        this.hazelcastInstance = HazelcastClient.newHazelcastClient(clientConfig);
    }

    public static HazelcastService getServiceInstance(String hazelcastHost) {
        if (hazelcastService == null) {
            hazelcastService = new HazelcastService(hazelcastHost);
        }
        return hazelcastService;
    }
    
    public FencedLock getLock(String key) {
        FencedLock lock = hazelcastInstance.getCPSubsystem().getLock(key);
        return lock;
    } 

}
