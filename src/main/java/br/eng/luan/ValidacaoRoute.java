package br.eng.luan;

import org.apache.camel.builder.RouteBuilder;

import jakarta.enterprise.context.ApplicationScoped;

import org.jboss.logging.Logger;

import br.eng.luan.processor.UnLockProcessor;

@ApplicationScoped
public class ValidacaoRoute extends RouteBuilder {

    static final Logger logger = Logger.getLogger(ValidacaoRoute.class);

    private UnLockProcessor unLockProcessor = new UnLockProcessor();

    @Override
    public void configure() throws Exception {

        from("direct:unlockStop")
            .process(unLockProcessor)
            .stop();
    }
    
}
