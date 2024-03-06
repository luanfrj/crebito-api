package br.eng.luan;

import org.apache.camel.Exchange;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import br.eng.luan.processor.ExtratoProcessor;
import br.eng.luan.processor.LockProcessor;
import br.eng.luan.processor.UnLockProcessor;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped 
public class ExtratoRoute extends RouteBuilder {

    static final Logger logger = LoggerFactory.getLogger(ExtratoRoute.class);

    @ConfigProperty(name = "hazelcast.host")
    String hazelcastHost;

    private ExtratoProcessor extratoProcessor = new ExtratoProcessor();

    private LockProcessor lockProcessor = new LockProcessor();

    private UnLockProcessor unLockProcessor = new UnLockProcessor();

    @Override
    public void configure() throws Exception {
        
        from("direct:extrato")
            .setProperty("hazelcastHost", constant(hazelcastHost))
            .doTry()
                .setHeader("id").method(Integer.class, "parseInt(${header.id})")
                .endDoTry()
            .doCatch(Exception.class)
                .setHeader("id").constant(6)
            .end()
            .process(lockProcessor)
            .setBody().constant("SELECT * FROM clientes AS c " +
                "LEFT JOIN transacoes AS t ON c.cliente_id = t.cliente_id " +
                "WHERE c.cliente_id = :?id ORDER BY realizada_em DESC LIMIT 10;")
            .to("jdbc:datasource?useHeadersAsParameters=true")
            .process(unLockProcessor)
            .choice()
                .when().simple("${body.isEmpty()}")
                    .setBody(constant("Cliente inexistente"))
                    .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("404"))
                .otherwise()
                    .process(extratoProcessor)
                    .marshal().json(JsonLibrary.Jackson)
            .end();
    }

    
}
