package br.eng.luan.bean;

public class AtualizaSaldoBean {
    
    public int atualizaSaldo(String tipo, int saldoAtual, int valor) {
        return tipo.equals("c") ? saldoAtual + valor : saldoAtual - valor;
    }

    public boolean isSaldoInValid(int saldoAtual, int limite) {
        return ((limite + saldoAtual) < 0);
    }
}
