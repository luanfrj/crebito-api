package br.eng.luan;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.lock.FencedLock;

import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@Startup
@ApplicationScoped 
public class HazelcastService {

    @Inject
    HazelcastInstance hazelcastInstance;

    private static HazelcastService hazelcastService;

    public HazelcastService(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    public static HazelcastService getServiceInstance() {
        if (hazelcastService == null) {
            System.out.println("--------- Inicializando");
            hazelcastService = new HazelcastService(hazelcastInstance);
        }
        return hazelcastService;
    }
    
    public FencedLock getLock(String key) {
        FencedLock lock = hazelcastInstance.getCPSubsystem().getLock(key);
        return lock;
    } 

}
