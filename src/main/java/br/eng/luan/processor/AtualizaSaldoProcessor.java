package br.eng.luan.processor;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

import br.eng.luan.exception.ValidacaoException;

public class AtualizaSaldoProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {

        Message messageIn = exchange.getIn();
        int saldoValue = messageIn.getHeader("saldo", int.class);
        int limite = messageIn.getHeader("limite", int.class);
        double valor = messageIn.getHeader("valor", double.class);
        String tipo = messageIn.getHeader("tipo", String.class);
        String descricao = messageIn.getHeader("descricao", String.class);

        if (valor % 1 != 0 || valor < 1) {
            throw new ValidacaoException("Valor deve ser inteiro e positivo");
        }

        if (descricao == null || descricao.length() < 1 || descricao.length() > 10) {
            throw new ValidacaoException("A descrição não deve ter entre 1 e 10 caracteres");
        }

        int valor_int = (int) valor;
        AtomicInteger saldo = new AtomicInteger(saldoValue);
        
        switch (tipo) {
            case ("d"):
                saldo.addAndGet(-valor_int);
                break;
            case ("c"):
                saldo.addAndGet(valor_int);
                break;
            default:
                throw new ValidacaoException("Tipo inválido");
        }
        if (saldo.get() < -limite) {
            throw new ValidacaoException("Limite Excedido");
        }

        messageIn.setHeader("valor", valor_int);
        messageIn.setHeader("tipo", tipo);
        messageIn.setHeader("descricao", descricao);
        messageIn.setHeader("saldo", saldo.get());

        exchange.setIn(messageIn);
    }

}
