package br.eng.luan;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class HazelcastClientConfig {

    @ConfigProperty(name = "hazelcast.host")
    String hazelcastHost;

    @Produces
    HazelcastInstance createInstance() {
        ClientConfig clientConfig = new ClientConfig();
        String[] members = {hazelcastHost};

        clientConfig.getNetworkConfig().addAddress(members);
        return HazelcastClient.newHazelcastClient(clientConfig);
    }
}
