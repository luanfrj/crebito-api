package br.eng.luan;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.eclipse.microprofile.config.inject.ConfigProperty;

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
            .to("direct:validaId")
            
            .unmarshal().json(TransacaoRequest.class)

            .to("direct:validaValor")
            .to("direct:validaTipo")
            .to("direct:validaDescricao")

            .process(lockProcessor)
            .setBody().constant("SELECT * FROM clientes WHERE cliente_id = :?id;")
            .to("jdbc:datasource?useHeadersAsParameters=true")

            .to("direct:validaCliente")

            .choice()
                .when().simple("${header.tipo} regex '^[c]$'")
                    .log(LoggingLevel.DEBUG, logger.getName(), "Realizando Crédito")
                    .setHeader("saldo").groovy("headers.saldo + headers.valor")
                    .endChoice()
                .otherwise()
                    .log(LoggingLevel.DEBUG, logger.getName(), "Realizando Débito")
                    .setHeader("saldo").groovy("headers.saldo - headers.valor")
            .end()

            .choice()
                .when().groovy("headers.saldo * -1 > headers.limite")
                    .log(LoggingLevel.DEBUG, logger.getName(), "Saldo inválido")
                    .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("422"))
                    .setBody(constant("Limite excedido"))
                    .to("direct:unlockStop")
                .otherwise()
                    .log(LoggingLevel.DEBUG, logger.getName(), "Validação de saldo concluida")
            .end()
 
            .setBody().constant("UPDATE clientes SET saldo = :?saldo " +
                "WHERE cliente_id = :?id; " +
                "INSERT INTO transacoes (cliente_id, valor, tipo, descricao) " +
                "VALUES (:?id, :?valor, :?tipo, :?descricao);")
            .to("jdbc:datasource?useHeadersAsParameters=true")
            .process(unLockProcessor)
            .log(LoggingLevel.DEBUG, logger.getName(), "Escrita no BD concluída")


            .setBody().constant(new TransacaoResponse())
            .script().simple("${body.setLimite(${header.limite})}")
            .script().simple("${body.setSaldo(${header.saldo})}")
            .marshal().json(JsonLibrary.Jackson);

    }
    
}
