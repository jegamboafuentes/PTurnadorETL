package com.baz.scc.turnador.logic;

import com.baz.scc.turnador.dao.CjCRTurnadorDao;
import com.baz.scc.turnador.model.*;
import com.baz.scc.turnador.support.*;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.jfree.util.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.*;


@Component
public class CjCRTurnadorLogic {
    
    @Autowired
    private CjCRPAppConfig appConfig;
    @Autowired
    private CjCRPModoProceso modoProceso;
    @Autowired
    private CjCRPModoFuente modoFuente;
    
    private static final Logger LOG = Logger.getLogger(CjCRTurnadorLogic.class);
    
    
    
    @Autowired
    private CjCRTurnadorDao turnadorDao;
    
    
    
    
    public void muestaInfoInicial(){
        LOG.info("-------------------------------------------------------------");
        LOG.info("---------------------Turnador ETL----------------------------");
        LOG.info("Modo: "+ appConfig.getProcesoModo());
        LOG.info("Funte de datos: "+appConfig.getFuenteDatos());
    }
    
    public void cargaTotal(){
        
        cargaPoolAtencion();
        
        
        cargaTurnos();

        
        cargaHistorial();
        
    }
    
    private void cargaTurnos(){
        List<CjCRTurno> descargaTurnos = validaTurnos();
        LOG.info("Inicia carga de datos TACJTRTURNO");
        if(descargaTurnos.isEmpty()){
            LOG.info("No hay registros");
        }else{
            int bloque = appConfig.getCantidadDatosOracle(); // Aqui va la referencia al app config
            int numeroBloques = 0;
            List<CjCRTurno> bloqueRealizado = new ArrayList<CjCRTurno>();

            if(bloque==0){
                numeroBloques = 1;
            }else if(bloque>descargaTurnos.size()){
                numeroBloques = 1;
                bloque = descargaTurnos.size();
            }else{
                numeroBloques = descargaTurnos.size() / bloque;
                if(descargaTurnos.size() % bloque>0){
                    numeroBloques += 1;
                }
            }

            for(int i = 0; i<numeroBloques; i++){
                if(i==numeroBloques -1){
                    bloqueRealizado = descargaTurnos.subList(i*bloque, descargaTurnos.size());
                }else{
                    bloqueRealizado = descargaTurnos.subList(i*bloque, i*bloque +bloque);
                }   
                turnadorDao.insertaTurno(bloqueRealizado, bloqueRealizado.size());
            }
            
        }
        
        LOG.info("Se agregaron con exito: "+turnadorDao.cantidadRegistros+" registros");
        turnadorDao.cantidadRegistros = 0;
        
    }
    
    private List<CjCRTurno> validaTurnos(){ //Compara entre el turnos de oracle y el de SQL
        LOG.info("Inicia validacion de CjCRTurno");
        List<CjCRTurno> nuevoTurno = new ArrayList<CjCRTurno>();
        List<CjCRTurno> turnoSQL = new ArrayList<CjCRTurno>();
        List<CjCRTurno> turnoOracle = new ArrayList<CjCRTurno>();
        turnoSQL = modoFuente.getTurno();
        turnoOracle = turnadorDao.consultaTurnoOracle();
        CjCRTurno turnoNull = new CjCRTurno();
            turnoNull.setFiFecha(30000101);
            turnoNull.setFiTurnoId(-1);
            turnoNull.setFiUnidadNegocioId(-1);
            turnoNull.setFiPaisId(-1);
            turnoNull.setFiCanalId(-1);
            turnoNull.setFiSucursalId(-1);              
        if(turnoOracle.size()==0){
            nuevoTurno = turnoSQL;
        }else{
            if(turnoSQL.size()>turnoOracle.size()){
                int diferencia = turnoSQL.size() - turnoOracle.size();
//                boolean volteo = false;
//                if(turnoSQL.get(1).getFiFecha()<turnoOracle.get(1).getFiFecha()){
//                    volteo = true;
//                    Collections.reverse(turnoOracle);
//                }
                    
                for (int i = 0; i<diferencia; i++){
                    turnoOracle.add(turnoNull);                    
                }
//                if(volteo){
//                    Collections.reverse(turnoOracle);
//                }
            }
            for(int i = 0; i<turnoSQL.size();i++){
                int fiFechaSql = turnoSQL.get(i).getFiFecha();
                int fiFechaOracle = turnoOracle.get(i).getFiFecha();
                int fiTurnoIdSql = turnoSQL.get(i).getFiTurnoId();
                int fiTurnoIdOracle = turnoOracle.get(i).getFiTurnoId();
                int fiUnidadNegocioIdSql = turnoSQL.get(i).getFiUnidadNegocioId();
                int fiUnidadNegocioIdOracle = turnoOracle.get(i).getFiUnidadNegocioId();

                int fiPaisIdSql = turnoSQL.get(i).getFiPaisId();
                int fiPaisIdOracle = turnoOracle.get(i).getFiPaisId();
                int fiCanalIdSql = turnoSQL.get(i).getFiCanalId();
                int fiCanalIdOracle = turnoOracle.get(i).getFiCanalId();
                int fiSucursalIdSql = turnoSQL.get(i).getFiSucursalId();
                int fiSucursalIdOracle = turnoOracle.get(i).getFiSucursalId();

                if((fiFechaSql != fiFechaOracle && fiTurnoIdSql != fiTurnoIdOracle && fiUnidadNegocioIdSql != fiUnidadNegocioIdOracle)||  fiPaisIdSql!=fiPaisIdOracle  ||  fiCanalIdSql!=fiCanalIdOracle   ||    fiSucursalIdSql!=fiSucursalIdOracle){
                       nuevoTurno.add(turnoSQL.get(i));
                       System.out.println(turnoSQL.get(i).getFiFecha()+"//"+turnoSQL.get(i).getFiTurnoId()+"//"+turnoSQL.get(i).getFiSucursalId());
                   }
            }
            
        }
        return nuevoTurno;
    }
    
    private void cargaPoolAtencion(){
        List<CjCRPoolAtencion> descargaPool = validaPoolAtencion();
        LOG.info("Inicia carga de datos TACJTRPOOLATEN");
        if(descargaPool.isEmpty()){
            LOG.info("No hay registros");
        }else{
            int bloque = appConfig.getCantidadDatosOracle(); // Aqui va la referencia al app config
            int numeroBloques = 0;
            List<CjCRPoolAtencion> bloqueRealizado = new ArrayList<CjCRPoolAtencion>();

            if(bloque==0){
                numeroBloques = 1;
            }else if(bloque>descargaPool.size()){
                numeroBloques = 1;
                bloque = descargaPool.size();
            }else{
                numeroBloques = descargaPool.size() / bloque;
                if(descargaPool.size() % bloque>0){
                    numeroBloques += 1;
                }
            }
            
            for(int i = 0; i<numeroBloques; i++){
                if(i==numeroBloques -1){
                    bloqueRealizado = descargaPool.subList(i*bloque, descargaPool.size());
                }else{
                    bloqueRealizado = descargaPool.subList(i*bloque, i*bloque +bloque);
                }   
                turnadorDao.insertaPoolEmpleado(bloqueRealizado, bloqueRealizado.size());
            }
            
        }
        LOG.info("Se agregaron con exito: "+turnadorDao.cantidadRegistros+" registros");
        turnadorDao.cantidadRegistros = 0;
    }
    
    private List<CjCRPoolAtencion> validaPoolAtencion(){ //Compara entre el pool de oracle y el de SQL
        LOG.info("Inicia validacion de CjCRPoolAtencion");
        List<CjCRPoolAtencion> nuevoPool = new ArrayList<CjCRPoolAtencion>();
        List<CjCRPoolAtencion> poolSQL = new ArrayList<CjCRPoolAtencion>();
        List<CjCRPoolAtencion> poolOracle = new ArrayList<CjCRPoolAtencion>();
        poolSQL = modoFuente.getPoolAtencion();
        poolOracle = turnadorDao.consultaPoolAtencionOracle();
        CjCRPoolAtencion poolNull = new CjCRPoolAtencion();
            poolNull.setFcEmpNoId("0");
            poolNull.setFiPaisId(-1);
            poolNull.setFiCanalId(-1);
            poolNull.setFiSucursalId(-1);              
        if(poolOracle.size()==0){
            nuevoPool = poolSQL;
        }else{
            if(poolSQL.size()>poolOracle.size()){
                int diferencia = poolSQL.size() - poolOracle.size();
                for (int i = 0; i<diferencia; i++){
                    poolOracle.add(poolNull);
                }
            }
            for(int i = 0; i<poolSQL.size();i++){
                String fcEmpNoIdSql = poolSQL.get(i).getFcEmpNoId();
                String fcEmpNoIdOracle = poolOracle.get(i).getFcEmpNoId();
                int fiPaisIdSql = poolSQL.get(i).getFiPaisId();
                int fiPaisIdOracle = poolOracle.get(i).getFiPaisId();
                int fiCanalIdSql = poolSQL.get(i).getFiCanalId();
                int fiCanalIdOracle = poolOracle.get(i).getFiCanalId();
                int fiSucursalIdSql = poolSQL.get(i).getFiSucursalId();
                int fiSucursalIdOracle = poolOracle.get(i).getFiSucursalId();

                if(!fcEmpNoIdSql.equals(fcEmpNoIdOracle)||  fiPaisIdSql!=fiPaisIdOracle  ||  fiCanalIdSql!=fiCanalIdOracle   ||    fiSucursalIdSql!=fiSucursalIdOracle){
                       nuevoPool.add(poolSQL.get(i));
                   }
            }
            
        }
        return nuevoPool;
    }
    
    private void cargaHistorial(){
        List<CjCRHistorico> descargaHistorico = validaHistorial();
        LOG.info("Inicia carga de datos TACJTRHISTORICO");
        if(descargaHistorico.isEmpty()){
            LOG.info("No hay registros");
        }else{
            int bloque = appConfig.getCantidadDatosOracle(); // Aqui va la referencia al app config
            int numeroBloques = 0;
            List<CjCRHistorico> bloqueRealizado = new ArrayList<CjCRHistorico>();

            if(bloque==0){
                numeroBloques = 1;
            }else if(bloque>descargaHistorico.size()){
                numeroBloques = 1;
                bloque = descargaHistorico.size();
            }else{
                numeroBloques = descargaHistorico.size() / bloque;
                if(descargaHistorico.size() % bloque>0){
                    numeroBloques += 1;
                }
            }
            
            for(int i = 0; i<numeroBloques; i++){
                if(i==numeroBloques -1){
                    bloqueRealizado = descargaHistorico.subList(i*bloque, descargaHistorico.size());
                }else{
                    bloqueRealizado = descargaHistorico.subList(i*bloque, i* bloque+bloque);
                }   
                turnadorDao.insertaHistorico(bloqueRealizado, bloqueRealizado.size());
            }
            
        }
        
        LOG.info("Se agregaron con exito: "+turnadorDao.cantidadRegistros+" registros");
        turnadorDao.cantidadRegistros = 0;
    }
    
    private List<CjCRHistorico> validaHistorial(){ //Compara entre el turnos de oracle y el de SQL
        LOG.info("Inicia validacion de CjCRHistorico");
        List<CjCRHistorico> nuevoHistorico = new ArrayList<CjCRHistorico>();
        List<CjCRHistorico> historicoSQL = new ArrayList<CjCRHistorico>();
        List<CjCRHistorico> historicoOracle = new ArrayList<CjCRHistorico>();
        historicoSQL = modoFuente.getHistorico();
        historicoOracle = turnadorDao.consultaHistoricoOracle();
        CjCRHistorico historicoNull = new CjCRHistorico();
            historicoNull.setFiFecha(30000101);
            historicoNull.setFiTurnoId(-1);
            historicoNull.setFiUnidadNegocioId(-1);
            historicoNull.setFiPaisId(-1);
            historicoNull.setFiCanalId(-1);
            historicoNull.setFiSucursalId(-1);              
        if(historicoOracle.size()==0){
            nuevoHistorico = historicoSQL;
        }else{
            if(historicoSQL.size()>historicoOracle.size()){
                int diferencia = historicoSQL.size() - historicoOracle.size();
//                boolean volteo = false;
//                if(turnoSQL.get(1).getFiFecha()<turnoOracle.get(1).getFiFecha()){
//                    volteo = true;
//                    Collections.reverse(turnoOracle);
//                }
                    
                for (int i = 0; i<diferencia; i++){
                    historicoOracle.add(historicoNull);                    
                }
//                if(volteo){
//                    Collections.reverse(turnoOracle);
//                }
            }
            for(int i = 0; i<historicoSQL.size();i++){
                int fiFechaSql = historicoSQL.get(i).getFiFecha();
                int fiFechaOracle = historicoOracle.get(i).getFiFecha();
                int fiTurnoIdSql = historicoSQL.get(i).getFiTurnoId();
                int fiTurnoIdOracle = historicoOracle.get(i).getFiTurnoId();
                int fiUnidadNegocioIdSql = historicoSQL.get(i).getFiUnidadNegocioId();
                int fiUnidadNegocioIdOracle = historicoOracle.get(i).getFiUnidadNegocioId();

                int fiPaisIdSql = historicoSQL.get(i).getFiPaisId();
                int fiPaisIdOracle = historicoOracle.get(i).getFiPaisId();
                int fiCanalIdSql = historicoSQL.get(i).getFiCanalId();
                int fiCanalIdOracle = historicoOracle.get(i).getFiCanalId();
                int fiSucursalIdSql = historicoSQL.get(i).getFiSucursalId();
                int fiSucursalIdOracle = historicoOracle.get(i).getFiSucursalId();

                if((fiFechaSql != fiFechaOracle && fiTurnoIdSql != fiTurnoIdOracle && fiUnidadNegocioIdSql != fiUnidadNegocioIdOracle)||  fiPaisIdSql!=fiPaisIdOracle  ||  fiCanalIdSql!=fiCanalIdOracle   ||    fiSucursalIdSql!=fiSucursalIdOracle){
                       nuevoHistorico.add(historicoSQL.get(i));
                   }
            }
            
        }
        return nuevoHistorico;
    }
    
    public void muestraInfoServer(){
        LOG.info("Tienda no: " + turnadorDao.consultaControl().getFiNoTienda());
        LOG.info("Canal: " + turnadorDao.consultaControl().getFiIdCanal());
        LOG.info("Pais: "+turnadorDao.consultaControl().getFiIdPais());
    }
    
    public void imprimeTurnos(){
        List<CjCRTurno> turnos = turnadorDao.consultaTurno();
        for (CjCRTurno i : turnos){
            System.out.print(i.getFiFecha()+" | "+i.getFiTurnoId()+" | "+i.getFiUnidadNegocioId()+" | "+i.getFiOrigenId()+" | "+i.getFcEmpNoId()+" | "+i.getFiFilaId()+" | "+i.getFiTurnoSeguimiento()+" | "+i.getFiPrioridad()+" | "+i.getFiStatusTurno()+" | "+i.getFiVirtual()+" | "+i.getFdFechaInserta()+" | "+i.getFcUserInserta()+" | "+i.getFdFechaModif()+" | "+i.getFcUserModif());
            System.out.println("");
        }
        System.out.println("Total = "+turnos.size());
    }
    
    public void imprimeHistorico(){
        List<CjCRHistorico> historico = turnadorDao.consultaHistorico();
        for (CjCRHistorico i : historico){
            System.out.print(i.getFiFecha()+" | "+i.getFiTurnoId()+" | "+i.getFiUnidadNegocioId()+" | "+i.getFiStatusTurnoId()+" | "+i.getFdActualizacion()+" | "+i.getFdFechaInserta()+" | "+i.getFcUserInserta()+" | "+i.getFdFechaModif()+" | "+i.getFcUserModif());
            System.out.println("");
        }
        System.out.println("Total = "+historico.size());
    }
    
    public void imprimePoolAtencion(){
        for (CjCRPoolAtencion i : turnadorDao.consultaPoolAtencion()){
            System.out.print(i.getFcEmpNoId()+" | "+i.getFiStatusPoolId()+" | "+i.getFcRutaImagen()+" | "+i.getFdFechaInserta()+" | "+i.getFcUserInserta()+" | "+i.getFdFechaModif()+" | "+i.getFcUserModif()+" | "+i.getFcPuntoAtencion());
            System.out.println("");
        }
    }
    
    public void impromePoolAtencionOracle(){
        for (CjCRPoolAtencion i : turnadorDao.consultaPoolAtencionOracle()){
            System.out.print(i.getFcEmpNoId()+" | "+i.getFiStatusPoolId()+" | "+i.getFcRutaImagen()+" | "+i.getFdFechaInserta()+" | "+i.getFcUserInserta()+" | "+i.getFdFechaModif()+" | "+i.getFcUserModif()+" | "+i.getFcPuntoAtencion());
            System.out.println("");
        }
    }
      
    public void pruebaProceso(){
        modoProceso.procesoModo();
    }
    
    public void pruevaValida(){
//        for(CjCRPoolAtencion i : validaPoolAtencion()){
//            System.out.print(i.getFcEmpNoId()+" | "+i.getFiStatusPoolId()+" | "+i.getFcRutaImagen()+" | "+i.getFdFechaInserta()+" | "+i.getFcUserInserta()+" | "+i.getFdFechaModif()+" | "+i.getFcUserModif()+" | "+i.getFcPuntoAtencion());
//            System.out.println("");
//        }
        
        for(CjCRTurno i : validaTurnos()){
            System.out.print(i.getFiFecha()+" | "+i.getFiTurnoId()+" | "+i.getFiUnidadNegocioId()+" | "+i.getFiOrigenId()+" | "+i.getFcEmpNoId()+" | "+i.getFiFilaId()+" | "+i.getFiTurnoSeguimiento()+" | "+i.getFiPrioridad()+" | "+i.getFiStatusTurno()+" | "+i.getFiVirtual()+" | "+i.getFdFechaInserta()+" | "+i.getFcUserInserta()+" | "+i.getFdFechaModif()+" | "+i.getFcUserModif());
            System.out.println("");
        }
    }
}
