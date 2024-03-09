package br.eng.luan;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import br.eng.luan.bean.ExtratoBean;
import br.eng.luan.processor.LockProcessor;
import br.eng.luan.processor.UnLockProcessor;
import org.jboss.logging.Logger;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped 
public class ExtratoRoute extends RouteBuilder {

    static final Logger logger = Logger.getLogger(ExtratoRoute.class);

    @ConfigProperty(name = "hazelcast.host")
    String hazelcastHost;

    private LockProcessor lockProcessor = new LockProcessor();

    private UnLockProcessor unLockProcessor = new UnLockProcessor();

    @Override
    public void configure() throws Exception {
        
        from("direct:extrato")
            .log(LoggingLevel.DEBUG, logger.getName(), "INICIO EXTRATO")
            .setProperty("hazelcastHost", constant(hazelcastHost))
            .filter().simple("${header.id} not regex '^-?\\d+?$'")
                .setBody(constant("Url invalida"))
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("400"))
                .stop()
            .end()
            .setHeader("id").method(Integer.class, "parseInt(${header.id})")
            .log(LoggingLevel.TRACE, logger.getName(), "FIM SET HEADER")
            .process(lockProcessor)
            .log(LoggingLevel.TRACE, logger.getName(), "FIM LOCK")
            .setBody().constant("SELECT * FROM clientes AS c " +
                "LEFT JOIN transacoes AS t ON c.cliente_id = t.cliente_id " +
                "WHERE c.cliente_id = :?id ORDER BY realizada_em DESC LIMIT 10;")
            .to("jdbc:datasource?useHeadersAsParameters=true")
            .log(LoggingLevel.TRACE, logger.getName(), "FIM QUERY")
            .process(unLockProcessor)
            .log(LoggingLevel.TRACE, logger.getName(), "FIM UNLOCK")
            .filter().simple("${body.isEmpty()}")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("404"))
                .setBody(constant("Cliente inexistente"))
                .to("direct:unlockStop")
            .end()
            .setBody().method(ExtratoBean.class, "montaExtrato(${body})")
            //.process(extratoProcessor)
            .marshal().json(JsonLibrary.Jackson)
            .log(LoggingLevel.DEBUG, logger.getName(), "FIM EXTRATO");
    }

    
}
