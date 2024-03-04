package br.eng.luan;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;

public class RestRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        restConfiguration()
            .bindingMode(RestBindingMode.json);

        rest("/clientes")
            .bindingMode(RestBindingMode.off)
            .post("/{id}/transacoes")
                .to("direct:transacao")
            .get("/{id}/extrato")
                .to("direct:extrato");
  }
    
}
