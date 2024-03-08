package br.eng.luan.model;

public class TransacaoRequest {

    private double valor;

    private String tipo;

    private String descricao;

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
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

    public boolean isNotValid() {
        if (
            ( valor % 1 == 0 && valor > 0 ) && 
            ( tipo.equals("c") || tipo.equals("d") ) && 
            ( descricao != null && descricao.length() > 0 && descricao.length() < 11) ){
            return false;
        }
        return true;
    }
    
}
