package eng.luan;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;

import eng.luan.model.TransacaoRequest;

public class RestRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        restConfiguration().bindingMode(RestBindingMode.auto);

        rest("/clientes")
            .post("/{id}/transacoes")
                .type(TransacaoRequest.class)
                .to("direct:transacao")
            .get("/{id}/extrato")
                .to("direct:extrato");
  }
    
}
