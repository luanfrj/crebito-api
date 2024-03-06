package br.eng.luan.processor;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import br.eng.luan.model.Extrato;
import br.eng.luan.model.Saldo;
import br.eng.luan.model.Transacao;

public class ExtratoProcessor implements Processor {

    @Override
    public synchronized void process(Exchange exchange) throws Exception {
        ArrayList<LinkedHashMap<String, Object>> resultList = exchange.getIn().getBody(ArrayList.class);

        int saldo = (int) resultList.get(0).get("saldo");
        int limite = (int) resultList.get(0).get("limite");

        Saldo saldoExtrato = new Saldo(saldo, Instant.now().toString(), limite);
        Extrato extrato = new Extrato();

        extrato.setSaldo(saldoExtrato);

        extrato.setUltimas_transacoes(new ArrayList<>());

        for (LinkedHashMap<String, Object> result : resultList) {
            if (result.get("valor") != null) {
                Transacao transacao = new Transacao(
                    (int) result.get("valor"), 
                    (String) result.get("tipo"), 
                    (String) result.get("descricao"),
                    ((Timestamp) result.get("realizada_em")).toInstant().toString());
                extrato.inserirTransacao(transacao);
            }
        }

        exchange.getIn().setBody(extrato);
    }
    
}
