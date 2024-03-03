package eng.luan.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class AtualizaSaldoProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        int valor = (int) exchange.getIn().getHeader("valor");
        int saldo = (int) exchange.getIn().getHeader("saldo");
        int limite = (int) exchange.getIn().getHeader("limite");
        String tipo = (String) exchange.getIn().getHeader("tipo");
        if (tipo.equals("d")){
            if (saldo - valor < limite * -1) {
                throw new Exception("Limite Excedido");
            } else {
                saldo = saldo - valor;
            }
        } else {
            saldo = saldo + valor;
        }
        exchange.getIn().setHeader("saldo", saldo);
    }

}
