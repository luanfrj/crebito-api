package br.eng.luan.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import com.hazelcast.cp.lock.FencedLock;

import br.eng.luan.HazelcastService;

public class LockProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        int id = (Integer) exchange.getIn().getHeader("id");
        HazelcastService hazelcastService = HazelcastService.getServiceInstance();
        FencedLock lock = hazelcastService.getLock(Integer.toString(id));
        lock.lock();
    }
}
