package eng.luan;

import org.apache.camel.builder.RouteBuilder;

import eng.luan.processor.ExtratoProcessor;

public class ExtratoRoute extends RouteBuilder {

    ExtratoProcessor extratoProcessor = new ExtratoProcessor();

    @Override
    public void configure() throws Exception {
        
        from("direct:extrato")
            .setHeader("id").method(Integer.class, "parseInt(${header.id})")

            .setBody().simple("SELECT * FROM clientes WHERE cliente_id = :?id;")
            .to("jdbc:datasource?useHeadersAsParameters=true")

            .setHeader("saldo").simple("${body[0].get(saldo)}")
            .setHeader("limite").simple("${body[0].get(limite)}")
            .log("Saldo: ${header.saldo} Limite: ${header.limite}")

            .setBody().simple("SELECT * FROM transacoes WHERE cliente_id = :?id ORDER BY realizada_em DESC LIMIT 10;")
            .to("jdbc:datasource?useHeadersAsParameters=true")

            .process(extratoProcessor);
    }

    
}
