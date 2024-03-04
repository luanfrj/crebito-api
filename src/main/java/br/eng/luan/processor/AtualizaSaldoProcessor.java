package br.eng.luan.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import br.eng.luan.exception.ValidacaoException;

public class AtualizaSaldoProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        int valor = (int) exchange.getIn().getHeader("valor");
        int saldo = (int) exchange.getIn().getHeader("saldo");
        int limite = (int) exchange.getIn().getHeader("limite");
        String tipo = (String) exchange.getIn().getHeader("tipo");
        String descricao = (String) exchange.getIn().getHeader("descricao");

        if (!(exchange.getIn().getHeader("valor") instanceof Integer) || valor < 0) {
            throw new ValidacaoException("Valor deve ser inteiro e positivo");
        }

        if (descricao == null || descricao.length() < 1 || descricao.length() > 10) {
            throw new ValidacaoException("A descrição não deve ter entre 1 e 10 caracteres");
        }

        switch (tipo) {
            case ("d"):
                if (saldo - valor < -limite) {
                    throw new ValidacaoException("Limite Excedido");
                } else {
                    saldo = saldo - valor;
                }
                break;
            case ("c"):
                saldo = saldo + valor;
                break;
            default:
                throw new ValidacaoException("Tipo inválido");
        }
        exchange.getIn().setHeader("saldo", saldo);
    }

}
