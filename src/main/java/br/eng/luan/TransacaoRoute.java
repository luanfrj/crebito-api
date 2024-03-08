package br.eng.luan;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import br.eng.luan.bean.AtualizaSaldoBean;
import br.eng.luan.model.TransacaoRequest;
import br.eng.luan.model.TransacaoResponse;
import br.eng.luan.processor.LockProcessor;
import br.eng.luan.processor.UnLockProcessor;
import org.jboss.logging.Logger;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TransacaoRoute extends RouteBuilder {

    static final Logger logger = Logger.getLogger(TransacaoRoute.class);

    @ConfigProperty(name = "hazelcast.host")
    String hazelcastHost;

    private LockProcessor lockProcessor = new LockProcessor();

    private UnLockProcessor unLockProcessor = new UnLockProcessor();

    @Override
    public void configure() throws Exception {

        from("direct:transacao")

            .setProperty("hazelcastHost", constant(hazelcastHost))
            .log(LoggingLevel.DEBUG, logger.getName(), "INICIO Valida ID")
            .filter().simple("${header.id} not regex '^-?\\d+?$'")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("400"))
                .setBody(constant("Url invalida"))
                .stop()
            .end()
            .log(LoggingLevel.DEBUG, logger.getName(), "INICIO unmarshal")
            .unmarshal().json(TransacaoRequest.class)
            .log(LoggingLevel.DEBUG, logger.getName(), "FIM unmarshal")
            .filter().simple("${body.isNotValid()}")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("422"))
                .setBody(constant("Requisição inválida"))
                .stop()
            .end()
            .log(LoggingLevel.DEBUG, logger.getName(), "FIM validação")
            .setHeader("id").method(Integer.class, "parseInt(${header.id})")
            .setHeader("valor").simple("${body.valor.intValue()}")
            .setHeader("tipo").simple("${body.tipo}")
            .setHeader("descricao").simple("${body.descricao}")
            .log(LoggingLevel.DEBUG, logger.getName(), "FIM Set HEADERS")
            // .filter().simple("${header.tipo} == 'c'")
            //     .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("400"))
            //     .setBody(constant("Teste"))
            //     .stop()
            // .end()
            .process(lockProcessor)
            .log(LoggingLevel.DEBUG, logger.getName(), "FIM Lock")
            .setBody().constant("SELECT * FROM clientes WHERE cliente_id = :?id;")
            .to("jdbc:datasource?useHeadersAsParameters=true")
            .log(LoggingLevel.DEBUG, logger.getName(), "FIM Query 1")
            .filter().simple("${body.isEmpty()}")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("404"))
                .setBody(constant("Cliente inexistente"))
                .to("direct:unlockStop")
            .end()
            .log(LoggingLevel.DEBUG, logger.getName(), "FIM Valida Cliente")
            .setHeader("limite").simple("${body[0].get(limite)}")
            .setHeader("saldo").method(AtualizaSaldoBean.class, "atualizaSaldo(${header.tipo}, ${body[0].get(saldo)}, ${header.valor})")
            .log(LoggingLevel.DEBUG, logger.getName(), "FIM Seta Saldo")
            .filter().method(AtualizaSaldoBean.class, "isSaldoInValid(${header.saldo}, ${header.limite})")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("422"))
                .setBody(constant("Limite excedido"))
                .to("direct:unlockStop")
            .end()
            .log(LoggingLevel.DEBUG, logger.getName(), "Escrita no BD Iniciada")
            .wireTap("direct:insereTransacao")
            .to("direct:updateSaldo")
            .log(LoggingLevel.DEBUG, logger.getName(), "Escrita no BD concluída")
            /*
            .setBody().constant("UPDATE clientes SET saldo = :?saldo " +
                "WHERE cliente_id = :?id; " +
                "INSERT INTO transacoes (cliente_id, valor, tipo, descricao) " +
                "VALUES (:?id, :?valor, :?tipo, :?descricao);")
            .to("jdbc:datasource?useHeadersAsParameters=true")
            */
            .process(unLockProcessor)
            .log(LoggingLevel.DEBUG, logger.getName(), "Fim UNLOCK")
            .setBody().constant(new TransacaoResponse())
            .script().simple("${body.setLimite(${header.limite})}")
            .script().simple("${body.setSaldo(${header.saldo})}")
            .marshal().json(JsonLibrary.Jackson)
            .log(LoggingLevel.DEBUG, logger.getName(), "Fim PROCESSAMENTO");

        from("direct:updateSaldo")
            .setBody().constant("UPDATE clientes SET saldo = :?saldo " +
                "WHERE cliente_id = :?id;")
            .to("jdbc:datasource?useHeadersAsParameters=true");

        from("direct:insereTransacao")
            .setBody().constant("INSERT INTO transacoes (cliente_id, valor, tipo, descricao) " +
                "VALUES (:?id, :?valor, :?tipo, :?descricao);")
            .to("jdbc:datasource?useHeadersAsParameters=true");

    }
    
}
