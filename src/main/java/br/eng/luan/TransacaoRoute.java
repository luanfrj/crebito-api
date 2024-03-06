package br.eng.luan;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import br.eng.luan.exception.ValidacaoException;
import br.eng.luan.model.TransacaoRequest;
import br.eng.luan.model.TransacaoResponse;
import br.eng.luan.processor.AtualizaSaldoProcessor;
import br.eng.luan.processor.LockProcessor;
import br.eng.luan.processor.UnLockProcessor;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TransacaoRoute extends RouteBuilder {

    static final Logger logger = LoggerFactory.getLogger(TransacaoRoute.class);

    @ConfigProperty(name = "hazelcast.host")
    String hazelcastHost;
    
    private Processor atualizaSaldoProcessor = new AtualizaSaldoProcessor();

    private LockProcessor lockProcessor = new LockProcessor();

    private UnLockProcessor unLockProcessor = new UnLockProcessor();

    @Override
    public void configure() throws Exception {

        from("direct:transacao")
            .log(LoggingLevel.INFO, "INICIO transacao")
            .setProperty("hazelcastHost", constant(hazelcastHost))
            .unmarshal().json(TransacaoRequest.class)
            .doTry()
                .setHeader("id").method(Integer.class, "parseInt(${header.id})")
                .endDoTry()
            .doCatch(Exception.class)
                .setHeader("id").constant(6)
            .end()
            .process(lockProcessor)
            .setProperty("transacaoRequest", simple("${body}"))

            .setBody().constant("SELECT * FROM clientes WHERE cliente_id = :?id;")
            .to("jdbc:datasource?useHeadersAsParameters=true")

            .choice()
                .when().simple("${body.isEmpty()}")
                    .setBody(constant("Cliente inexistente"))
                    .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("404"))
                .otherwise()
                    .setHeader("saldo").simple("${body[0].get(saldo)}")
                    .setHeader("limite").simple("${body[0].get(limite)}")
                    
                    .doTry()
                        .process(atualizaSaldoProcessor)
                        .log(LoggingLevel.INFO, "Iniciando atualização")
                        .setBody().constant("UPDATE clientes SET saldo = :?saldo " +
                            "WHERE cliente_id = :?id; " +
                            "INSERT INTO transacoes (cliente_id, valor, tipo, descricao) " +
                            "VALUES (:?id, :?valor, :?tipo, :?descricao);")
                        .to("jdbc:datasource?useHeadersAsParameters=true")
                        .log(LoggingLevel.INFO, "FIM atualização")
                        .setBody().constant(new TransacaoResponse())
                        .script().simple("${body.setLimite(${header.limite})}")
                        .script().simple("${body.setSaldo(${header.saldo})}")
                        .marshal().json(JsonLibrary.Jackson)
                        .endDoTry()
                    .doCatch(ValidacaoException.class)
                        .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("422"))
                        .setBody().simple("${exchangeProperty."+Exchange.EXCEPTION_CAUGHT+".getMessage()}")
                    .end()
            .end()
            .process(unLockProcessor)
            .log(LoggingLevel.INFO, "FIM transacao");

    }
    
}
