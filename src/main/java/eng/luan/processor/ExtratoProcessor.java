package eng.luan.processor;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import eng.luan.model.Extrato;
import eng.luan.model.Saldo;
import eng.luan.model.Transacao;

public class ExtratoProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        int saldo = (int) exchange.getIn().getHeader("saldo");
        int limite = (int) exchange.getIn().getHeader("limite");

        ArrayList<HashMap<String, Object>> resultList = (ArrayList) exchange.getIn().getBody();

        Saldo saldoExtrato = new Saldo(saldo, Instant.now().toString(), limite);
        Extrato extrato = new Extrato();

        extrato.setSaldo(saldoExtrato);

        extrato.setUltimas_transacoes(new ArrayList<>());

        for (Map result : resultList) {
            Transacao transacao = new Transacao(
                (int) result.get("valor"), 
                (String) result.get("tipo"), 
                (String) result.get("descricao"),
                ((Timestamp) result.get("realizada_em")).toInstant().toString());
            extrato.inserirTransacao(transacao);
        }

        exchange.getIn().setBody(extrato);
    }
    
}
