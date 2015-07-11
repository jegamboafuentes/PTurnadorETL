package com.baz.scc.turnador.support;

public class CjCRPAppConfig {
    private String procesoModo;
    private String procesoFechaInicio;
    private String procesoFechaFin;
    private Integer cantidadDatosOracle;
    private String fuenteDatos;
    private String servidor;
    private String servicio;
    private Integer sucursal;

    
    public String getServidor() {
        return servidor;
    }

    public void setServidor(String servidor) {
        this.servidor = servidor;
    }

    public String getServicio() {
        return servicio;
    }

    public void setServicio(String servicio) {
        this.servicio = servicio;
    }
    
    public String getProcesoModo() {
        return procesoModo;
    }

    public void setProcesoModo(String procesoModo) {
        this.procesoModo = procesoModo;
    }

    public String getProcesoFechaInicio() {
        return procesoFechaInicio;
    }

    public void setProcesoFechaInicio(String procesoFechaInicio) {
        this.procesoFechaInicio = procesoFechaInicio;
    }

    public String getProcesoFechaFin() {
        return procesoFechaFin;
    }

    public void setProcesoFechaFin(String procesoFechaFin) {
        this.procesoFechaFin = procesoFechaFin;
    }

    public Integer getCantidadDatosOracle() {
        return cantidadDatosOracle;
    }

    public void setCantidadDatosOracle(Integer cantidadDatosOracle) {
        this.cantidadDatosOracle = cantidadDatosOracle;
    }

    public String getFuenteDatos() {
        return fuenteDatos;
    }
    
    public void setFuenteDatos(String fuenteDatos) {
        this.fuenteDatos = fuenteDatos;
    }
    
    public Integer getSucursal() {
        return sucursal;
    }

    public void setSucursal(Integer sucursal) {
        this.sucursal = sucursal;
    }
    
    
    
}
