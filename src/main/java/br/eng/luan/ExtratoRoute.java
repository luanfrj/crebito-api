package br.eng.luan;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

import br.eng.luan.processor.ExtratoProcessor;
import br.eng.luan.processor.LockProcessor;
import br.eng.luan.processor.UnLockProcessor;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped 
public class ExtratoRoute extends RouteBuilder {

    private ExtratoProcessor extratoProcessor = new ExtratoProcessor();

    private LockProcessor lockProcessor = new LockProcessor();

    private UnLockProcessor unLockProcessor = new UnLockProcessor();

    @Override
    public void configure() throws Exception {
        
        from("direct:extrato")
            .setHeader("id").method(Integer.class, "parseInt(${header.id})")
            .process(lockProcessor)
            .setBody().simple("SELECT * FROM clientes WHERE cliente_id = :?id;")
            .to("jdbc:datasource?useHeadersAsParameters=true")

            .choice()
                .when().simple("${body.isEmpty()}")
                    .setBody(constant("Cliente inexistente"))
                    .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("404"))
                .otherwise()
                    .setHeader("saldo").simple("${body[0].get(saldo)}")
                    .setHeader("limite").simple("${body[0].get(limite)}")
        
                    .setBody().simple("SELECT * FROM transacoes WHERE cliente_id = :?id ORDER BY realizada_em DESC LIMIT 10;")
                    .to("jdbc:datasource?useHeadersAsParameters=true")
                    
                    .process(extratoProcessor)
                    .marshal().json(JsonLibrary.Jackson)
            .end()
            .process(unLockProcessor);
    }

    
}
