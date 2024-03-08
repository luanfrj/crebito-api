package br.eng.luan;

import org.apache.camel.Exchange;
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

        from("direct:validaCliente")
            .choice()
                .when().simple("${body.isEmpty()}")
                    .setBody(constant("Cliente inexistente"))
                    .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("404"))
                    .to("direct:unlockStop")
                .otherwise()
                    .setHeader("saldo").simple("${body[0].get(saldo)}")
                    .setHeader("limite").simple("${body[0].get(limite)}")
                    .endChoice()
            .end();

        from("direct:unlockStop")
            .process(unLockProcessor)
            .stop();
    }
    
}
