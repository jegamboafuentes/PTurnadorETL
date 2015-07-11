package com.baz.scc.turnador.model;

public class CjCRSucursalWs {
    private int tipoQuery;
    private int registrosEncontrados;
    private int registrosActualizados;
    private String [] cabeceros;
    private String [][] resultados;
    private CjCRPerformance performance;
    private int error;

    public int getTipoQuery() {
        return tipoQuery;
    }

    public void setTipoQuery(int tipoQuery) {
        this.tipoQuery = tipoQuery;
    }

    public int getRegistrosEncontrados() {
        return registrosEncontrados;
    }

    public void setRegistrosEncontrados(int registrosEncontrados) {
        this.registrosEncontrados = registrosEncontrados;
    }

    public int getRegistrosActualizados() {
        return registrosActualizados;
    }

    public void setRegistrosActualizados(int registrosActualizados) {
        this.registrosActualizados = registrosActualizados;
    }

    public CjCRPerformance getPerformance() {
        return performance;
    }

    public void setPerformance(CjCRPerformance performance) {
        this.performance = performance;
    }

    public int getError() {
        return error;
    }

    public void setError(int error) {
        this.error = error;
    }

    public String[] getCabeceros() {
        return cabeceros;
    }

    public void setCabeceros(String[] cabeceros) {
        this.cabeceros = cabeceros;
    }

    public String[][] getResultados() {
        return resultados;
    }

    public void setResultados(String[][] resultados) {
        this.resultados = resultados;
    }
    
}
