package com.baz.scc.turnador.support;

import com.baz.scc.turnador.dao.*;
import com.baz.scc.turnador.model.*;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.jfree.util.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.*;

@Component
public class CjCRPModoFuente {
    
    @Autowired
    private CjCRPAppConfig appConfig;
    @Autowired
    private CjCRTurnadorDao turnadorBDDao;
    @Autowired
    private CjCRTurnadorServiceDao turnadorServiceDao;
    
    private static final Logger LOG = Logger.getLogger(CjCRPModoFuente.class);
    
    public String getModo(){
        return appConfig.getFuenteDatos();       
    }
    
    public List<CjCRPoolAtencion> getPoolAtencion(){
        List <CjCRPoolAtencion> poolAtencionBD = new ArrayList<CjCRPoolAtencion>();
        List <CjCRPoolAtencion> poolAtencionService = new ArrayList<CjCRPoolAtencion>();  
        String fuente = getModo();
        if (fuente.equals("cliente")){
            poolAtencionService = turnadorServiceDao.consultaPoolAtencion();
            return poolAtencionService;
        }else if(fuente.equals("bd")){
            poolAtencionBD = turnadorBDDao.consultaPoolAtencion();
            return poolAtencionBD;
        }else{
            LOG.error("Modo fuente no valido");
            poolAtencionService = turnadorServiceDao.consultaPoolAtencion();
            return poolAtencionService;
        }
        
        
    }
    public List<CjCRTurno> getTurno(){
        List <CjCRTurno> turnoBD = new ArrayList<CjCRTurno>();
        List <CjCRTurno> turnoService = new ArrayList<CjCRTurno>();  
        String fuente = getModo();
        if (fuente.equals("cliente")){
            turnoService = turnadorServiceDao.consultaTurno();
            return turnoService;
        }else if(fuente.equals("bd")){
            turnoBD = turnadorBDDao.consultaTurno();
            return turnoBD;
        }else{
            LOG.error("Modo fuente no valido");
            turnoService = turnadorServiceDao.consultaTurno();
            return turnoService;
        }
    }
    public List<CjCRHistorico> getHistorico(){
        List <CjCRHistorico> historicoBD = new ArrayList<CjCRHistorico>();
        List <CjCRHistorico> historicoService = new ArrayList<CjCRHistorico>();  
        String fuente = getModo();
        if (fuente.equals("cliente")){
            historicoService = turnadorServiceDao.consultaHistorico();
            return historicoService;
        }else if(fuente.equals("bd")){
            historicoBD = turnadorBDDao.consultaHistorico();
            return historicoBD;
        }else{
            LOG.error("Modo fuente no valido");
            historicoService = turnadorServiceDao.consultaHistorico();
            return historicoService;
        }
    }
    
    public Integer getSucursal(){
        String fuente = getModo();
        Integer sucursal;
        if (fuente.equals("cliente")){
            sucursal = appConfig.getSucursal();
            return sucursal;
        }else if(fuente.equals("bd")){
            sucursal = turnadorBDDao.consultaControl().getFiNoTienda();
            return sucursal;
        }else{
            LOG.error("Modo fuente no valido");
            sucursal = appConfig.getSucursal();
            return sucursal;
        }
    }
    
}
