package br.eng.luan.bean;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.lock.FencedLock;

import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;


@ApplicationScoped
public class HazelcastBean {

    public static HazelcastInstance hazelcastInstance;

    @ConfigProperty(name = "hazelcast.host") 
    String hosts;
    
    @Startup
    public void startInstance() {
        Config clusterConfig = new Config();
        System.out.println("INCIANDO APLICAÇÃO: " + hosts);
        clusterConfig.setClusterName("rinha-cluster");
        NetworkConfig networkcConfig = clusterConfig.getNetworkConfig();
        JoinConfig joinConfig = networkcConfig.getJoin();
        for (String member : hosts.split(",")) {
            joinConfig.getTcpIpConfig().addMember(member);
        }
        hazelcastInstance = Hazelcast.newHazelcastInstance(clusterConfig);
    }

    public void lock(int key) {
        FencedLock lock = hazelcastInstance.getCPSubsystem().getLock(Integer.toString(key));
        lock.lock();
    }

    public void unLock(int key) {
        FencedLock lock = hazelcastInstance.getCPSubsystem().getLock(Integer.toString(key));
        lock.unlock();
    } 
    
}
