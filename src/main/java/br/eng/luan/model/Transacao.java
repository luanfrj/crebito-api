package br.eng.luan.model;

public class Transacao {
    
    private int valor;

    private String tipo;

    private String descricao;

    private String realizada_em;

    public Transacao(int valor, String tipo, String descricao, String realizada_em) {
        this.valor = valor;
        this.tipo = tipo;
        this.descricao = descricao;
        this.realizada_em = realizada_em;
    }

    public int getValor() {
        return valor;
    }

    public void setValor(int valor) {
        this.valor = valor;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getRealizada_em() {
        return realizada_em;
    }

    public void setRealizada_em(String realizada_em) {
        this.realizada_em = realizada_em;
    }

}
