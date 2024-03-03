package br.eng.luan;

import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

import br.eng.luan.model.TransacaoRequest;
import br.eng.luan.model.TransacaoResponse;
import br.eng.luan.processor.AtualizaSaldoProcessor;

public class TransacaoRoute extends RouteBuilder {

    private Processor atualizaSaldoProcessor = new AtualizaSaldoProcessor();

    @Override
    public void configure() throws Exception {

        from("direct:transacao")
            .unmarshal().json(TransacaoRequest.class)
            .setHeader("id").method(Integer.class, "parseInt(${header.id})")
            .setHeader("valor").simple("${body.valor}")
            .setHeader("tipo").simple("${body.tipo}")
            .setHeader("descricao").simple("${body.descricao}")
            .log("Valor: ${header.valor} tipo: ${header.tipo} descricao: ${header.descricao}")

            .setBody().simple("SELECT * FROM clientes WHERE cliente_id = :?id;")
            .to("jdbc:datasource?useHeadersAsParameters=true")

            .setHeader("saldo").simple("${body[0].get(saldo)}")
            .setHeader("limite").simple("${body[0].get(limite)}")
            .log("Saldo: ${header.saldo} Limite: ${header.limite}")

            .process(atualizaSaldoProcessor)

            .setBody().simple("UPDATE clientes SET saldo = :?saldo WHERE cliente_id = :?id;")
            .to("jdbc:datasource?useHeadersAsParameters=true")

            .setBody().simple("INSERT INTO transacoes (cliente_id, valor, tipo, descricao) VALUES (:?id, :?valor, :?tipo, :?descricao);")
            .to("jdbc:datasource?useHeadersAsParameters=true")
            
            .setBody().constant(new TransacaoResponse())
            .script().simple("${body.setLimite(${header.limite})}")
            .script().simple("${body.setSaldo(${header.saldo})}")
            .marshal().json(JsonLibrary.Jackson);
    }
    
}
