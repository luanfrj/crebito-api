package br.eng.luan;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

import br.eng.luan.bean.AtualizaSaldoBean;
import br.eng.luan.bean.HazelcastBean;
import br.eng.luan.model.TransacaoRequest;
import br.eng.luan.model.TransacaoResponse;
import org.jboss.logging.Logger;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TransacaoRoute extends RouteBuilder {

    static final Logger logger = Logger.getLogger(TransacaoRoute.class);

    @Override
    public void configure() throws Exception {

        from("direct:transacao")
            .log(LoggingLevel.DEBUG, logger.getName(), "INICIO TRANSACAO")
            .filter().simple("${header.id} not regex '^-?\\d+?$'")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("400"))
                .setBody(constant("Url invalida"))
                .stop()
            .end()
            .log(LoggingLevel.TRACE, logger.getName(), "INICIO unmarshal")
            .unmarshal().json(TransacaoRequest.class)
            .log(LoggingLevel.TRACE, logger.getName(), "FIM unmarshal")
            .filter().simple("${body.isNotValid()}")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("422"))
                .setBody(constant("Requisição inválida"))
                .stop()
            .end()
            .log(LoggingLevel.TRACE, logger.getName(), "FIM validação")
            .setHeader("id").method(Integer.class, "parseInt(${header.id})")
            .setHeader("valor").simple("${body.valor.intValue()}")
            .setHeader("tipo").simple("${body.tipo}")
            .setHeader("descricao").simple("${body.descricao}")
            .log(LoggingLevel.TRACE, logger.getName(), "FIM Set HEADERS")
            .bean(HazelcastBean.class, "lock(${header.id})")
            .log(LoggingLevel.TRACE, logger.getName(), "FIM Lock")
            .setBody().constant("SELECT * FROM clientes WHERE cliente_id = :?id;")
            .to("jdbc:datasource?useHeadersAsParameters=true")
            .log(LoggingLevel.TRACE, logger.getName(), "FIM Query 1")
            .filter().simple("${body.isEmpty()}")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("404"))
                .setBody(constant("Cliente inexistente"))
                .to("direct:unlockStop")
            .end()
            .log(LoggingLevel.TRACE, logger.getName(), "FIM Valida Cliente")
            .setHeader("limite").simple("${body[0].get(limite)}")
            .setHeader("saldo").method(AtualizaSaldoBean.class, "atualizaSaldo(${header.tipo}, ${body[0].get(saldo)}, ${header.valor})")
            .log(LoggingLevel.TRACE, logger.getName(), "FIM Seta Saldo")
            .filter().method(AtualizaSaldoBean.class, "isSaldoInValid(${header.saldo}, ${header.limite})")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("422"))
                .setBody(constant("Limite excedido"))
                .to("direct:unlockStop")
            .end()
            .log(LoggingLevel.TRACE, logger.getName(), "Escrita no BD Iniciada")
            .wireTap("direct:insereTransacao")
            .to("direct:updateSaldo")           
            .log(LoggingLevel.TRACE, logger.getName(), "Escrita no BD concluída")
            .bean(HazelcastBean.class, "unLock(${header.id})")
            .log(LoggingLevel.TRACE, logger.getName(), "Fim UNLOCK")
            .setBody().constant(new TransacaoResponse())
            .script().simple("${body.setLimite(${header.limite})}")
            .script().simple("${body.setSaldo(${header.saldo})}")
            .marshal().json(JsonLibrary.Jackson)
            .log(LoggingLevel.DEBUG, logger.getName(), "FIM TRANSACAO");

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
