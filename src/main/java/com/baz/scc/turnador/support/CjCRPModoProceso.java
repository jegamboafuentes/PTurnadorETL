package com.baz.scc.turnador.support;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.log4j.Logger;
import org.jfree.util.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.*;


@Component
public class CjCRPModoProceso {
    
    @Autowired
    private CjCRPAppConfig appConfig;
    
    private static final Logger LOG = Logger.getLogger(CjCRPModoProceso.class);
    DateFormat ano = new SimpleDateFormat("YYYY");
    DateFormat mes = new SimpleDateFormat("MM");
    DateFormat dia = new SimpleDateFormat("dd");
    Date hoy = new Date();
    Date ayer = new Date(hoy.getTime()-86400000);
    
    public String procesoModo(){ 
        String procesoModo = appConfig.getProcesoModo();
        String sqlQuery;
        String diaAyer = ano.format(ayer)+mes.format(ayer)+dia.format(ayer) ;

        if(procesoModo.equals("automatico")){
            sqlQuery = "FIFECHA = "+diaAyer;      
        }else if(procesoModo.equals("rango")){
            String fdInicio = appConfig.getProcesoFechaInicio();
            String fdFin = appConfig.getProcesoFechaFin();
            if(fdInicio.length()==8&&fdFin.length()==8){
                try{
                    int inicio = Integer.parseInt(fdInicio);
                    int fin = Integer.parseInt(fdFin);
                }catch(Exception e){
                    LOG.error("Fecha(s) no valida");
                    sqlQuery = "FIFECHA = 0";
                }
                sqlQuery = "FIFECHA BETWEEN "+fdInicio+" AND "+fdFin;
            }else{
                LOG.error("Fecha(s) no valida");
                sqlQuery = "FIFECHA = 0";
            }
            
                     
        }else{
            LOG.error(procesoModo+" no se reconoce como parametro de configuración.");
            LOG.error("Se procesara en modo automatico");
            sqlQuery = "FIFECHA = "+diaAyer;  
        }
        return sqlQuery;    
    }
    
    
}
