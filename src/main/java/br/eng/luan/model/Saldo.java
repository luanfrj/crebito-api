package br.eng.luan.model;

public class Saldo {

    private int total;

    private String data_extrato;

    private int limite;

    public Saldo(int total, String data_extrato, int limite) {
        this.total = total;
        this.data_extrato = data_extrato;
        this.limite = limite;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public String getData_extrato() {
        return data_extrato;
    }

    public void setData_extrato(String data_extrato) {
        this.data_extrato = data_extrato;
    }

    public int getLimite() {
        return limite;
    }

    public void setLimite(int limite) {
        this.limite = limite;
    }
    
}
