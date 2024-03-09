package br.eng.luan;

import org.apache.camel.builder.RouteBuilder;

import jakarta.enterprise.context.ApplicationScoped;

import org.jboss.logging.Logger;

import br.eng.luan.bean.HazelcastBean;

@ApplicationScoped
public class ValidacaoRoute extends RouteBuilder {

    static final Logger logger = Logger.getLogger(ValidacaoRoute.class);

    @Override
    public void configure() throws Exception {

        from("direct:unlockStop")
            .bean(HazelcastBean.class, "unLock(${header.id})")
            .stop();
    }
    
}
