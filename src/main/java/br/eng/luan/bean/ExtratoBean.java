package br.eng.luan.bean;

import java.time.Instant;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import br.eng.luan.model.Extrato;
import br.eng.luan.model.Saldo;
import br.eng.luan.model.Transacao;

public class ExtratoBean {

    public Extrato montaExtrato( ArrayList<LinkedHashMap<String, Object>> itens ) {

        Saldo saldoExtrato = new Saldo(
            (int) itens.get(0).get("saldo"), 
            Instant.now().toString(), 
            (int) itens.get(0).get("limite") );
            
        Extrato extrato = new Extrato(saldoExtrato, new ArrayList<>());

        if (itens.get(0).get("valor") != null) {
            for (LinkedHashMap<String, Object> item : itens) {
                extrato.inserirTransacao(new Transacao(
                    (int) item.get("valor"), 
                    (String) item.get("tipo"), 
                    (String) item.get("descricao"), 
                    ((Timestamp) item.get("realizada_em")).toInstant().toString()
                ));
            }
        }
        return extrato;
    }
    
}
