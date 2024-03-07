package br.eng.luan;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
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

        from("direct:validaId")
            .choice()
            .when().simple("${header.id} regex '^-?\\d+?$'")
                .log(LoggingLevel.DEBUG, logger.getName(), "ID Correto")
                .setHeader("id").method(Integer.class, "parseInt(${header.id})")
                .endChoice()
            .otherwise()
                .setBody(constant("Url invalida"))
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("400"))
                .stop()
            .end();

        from("direct:validaValor")
            .choice()
                .when().groovy("body.valor % 1 == 0 && body.valor > 0")
                    .log(LoggingLevel.DEBUG, logger.getName(), "Valor Correto")
                    .setHeader("valor").simple("${body.valor.intValue()}")
                    .endChoice()
                .otherwise()
                    .log(LoggingLevel.DEBUG, logger.getName(), "Valor: ${body.valor}")
                    .setBody(constant("Falha de validação"))
                    .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("422"))
                    .stop()
            .end();

        from("direct:validaTipo")
            .choice()
                .when().simple("${body.tipo} regex '^[cd]$'")
                    .log(LoggingLevel.DEBUG, logger.getName(),"Tipo Correto")
                    .setHeader("tipo").simple("${body.tipo}")
                    .endChoice()
                .otherwise()
                    .log(LoggingLevel.DEBUG, logger.getName(), "Tipo: ${body.tipo}")
                    .setBody(constant("Falha de validação"))
                    .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("422"))
                    .stop()
            .end();

        from("direct:validaDescricao")
            .choice()
                .when().groovy("body.descricao != null && body.descricao.length() > 0 && body.descricao.length() < 11")
                    .log(LoggingLevel.DEBUG, logger.getName(), "Descricao correta")
                    .setHeader("descricao").simple("${body.descricao}")
                    .endChoice()
                .otherwise()
                    .log(LoggingLevel.DEBUG, logger.getName(), "Descricao: ${body.descricao}")
                    .setBody(constant("Falha de validação"))
                    .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("422"))
                    .stop()
            .end();

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
