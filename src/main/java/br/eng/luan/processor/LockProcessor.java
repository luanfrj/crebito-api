package br.eng.luan.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import com.hazelcast.cp.lock.FencedLock;

import br.eng.luan.HazelcastService;
import org.jboss.logging.Logger;

public class LockProcessor implements Processor {

    static final Logger logger = Logger.getLogger(LockProcessor.class);

    @Override
    public void process(Exchange exchange) throws Exception {
        int id = (Integer) exchange.getIn().getHeader("id");
        String hazelcastHost = (String) exchange.getProperty("hazelcastHost");
        HazelcastService hazelcastService = HazelcastService.getServiceInstance(hazelcastHost);
        FencedLock lock = hazelcastService.getLock(Integer.toString(id));
        lock.lock();
    }
}
