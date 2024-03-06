package br.eng.luan.processor;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import br.eng.luan.exception.ValidacaoException;
import br.eng.luan.model.TransacaoRequest;

public class AtualizaSaldoProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {

        TransacaoRequest transacaoRequest = exchange.getProperty("transacaoRequest", TransacaoRequest.class);
        double valor = transacaoRequest.getValor();
        String tipo = transacaoRequest.getTipo();
        String descricao = transacaoRequest.getDescricao();
        Map headers = exchange.getIn().getHeaders();
        int saldo = (int) headers.get("saldo");
        int limite = (int) headers.get("limite");

        if (valor % 1 != 0 || valor < 1) {
            throw new ValidacaoException("Valor deve ser inteiro e positivo");
        }

        if (descricao == null || descricao.length() < 1 || descricao.length() > 10) {
            throw new ValidacaoException("A descrição não deve ter entre 1 e 10 caracteres");
        }

        int valor_int = (int) valor;

        switch (tipo) {
            case ("d"):
                saldo = saldo - valor_int;
                break;
            case ("c"):
                saldo = saldo + valor_int;
                break;
            default:
                throw new ValidacaoException("Tipo inválido");
        }
        if (saldo < -limite) {
            throw new ValidacaoException("Limite Excedido");
        }

        headers.put("valor", valor_int);
        headers.put("tipo", tipo);
        headers.put("descricao", descricao);
        headers.put("saldo", saldo);

        exchange.getIn().setHeaders(headers);
    }

}
