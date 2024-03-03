package br.eng.luan.model;

import java.util.ArrayList;

public class Extrato {
    
    private Saldo saldo;

    private ArrayList<Transacao> ultimas_transacoes;

    public void inserirTransacao(Transacao transacao) {
        this.ultimas_transacoes.add(transacao);
    }

    public Saldo getSaldo() {
        return saldo;
    }

    public void setSaldo(Saldo saldo) {
        this.saldo = saldo;
    }

    public ArrayList<Transacao> getUltimas_transacoes() {
        return ultimas_transacoes;
    }

    public void setUltimas_transacoes(ArrayList<Transacao> ultimas_transacoes) {
        this.ultimas_transacoes = ultimas_transacoes;
    }

    
}
