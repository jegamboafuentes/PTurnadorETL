package com.baz.scc.turnador.dao;

import com.baz.scc.commons.model.CjCRSucursal;
import com.baz.scc.turnador.model.*;
import com.baz.scc.turnador.support.CjCRPAppConfig;
import com.baz.scc.turnador.support.CjCRPModoFuente;
import com.baz.scc.turnador.support.CjCRPModoProceso;
import com.sun.jersey.api.client.*;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Response;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.stereotype.Repository;


@Repository
public class CjCRTurnadorServiceDao {
    
    private static final Logger LOG = Logger.getLogger(CjCRTurnadorDao.class);
    
    @Autowired
    private CjCRPModoProceso proceso;
    @Autowired
    private CjCRPModoFuente modoFuente;
    @Autowired
    private CjCRPAppConfig appConfig;
    
    
    private String servidor;    //Variables deben de ser parametrizables desde config
    private String servicio;
    private String sucursalLabel = "\"idSucursal\":";
    private Integer idSucursal;
    private final String consultasLabel = ",\"consultas\":";
    private String consulta;
    private final String limitRowLabel = ",\"limitRow\":";
    private String limitRow = "0";
    private int paisId;
    private int canalId;
        
    private void consultaControl(){
        try{
           idSucursal = modoFuente.getSucursal();
           servidor = appConfig.getServidor();
           servicio = appConfig.getServicio();
           Client client = Client.create();
           consulta = "\"SELECT * FROM CONTROL WITH(NOLOCK)\"";
           String url = servidor+servicio;
           WebResource webResource = client.resource(url);
           String input = "{"+sucursalLabel+idSucursal+consultasLabel+consulta+limitRowLabel+limitRow+"}";
           ClientResponse response = webResource.type("application/json").post(ClientResponse.class,input);
           
           if(response.getStatus() != 200){
               throw new RuntimeException("Error: Error en el codigo HTTP "+response.getStatus());
           }
           
            String output = response.getEntity(String.class); 
            ObjectMapper mapper = new ObjectMapper();
            List<CjCRSucursalWs> resultSucursalWS = mapper.readValue(output, new TypeReference<List <CjCRSucursalWs>>(){});
            
            for(CjCRSucursalWs po : resultSucursalWS){
                for(String [] filas : po.getResultados()){
                    paisId = Integer.parseInt(filas[5]);
                    canalId = Integer.parseInt(filas[4]);
                }               
            }
            
           
        }catch(Exception e){
            LOG.error("Error en la extraccion de datos del servidor", e);
        }
        
    }
    
    public List<CjCRPoolAtencion> consultaPoolAtencion(){
        LOG.info("Inicia proceso de extraccion de TACJCCTRPOOLATENCION Service");
        consultaControl();
        List<CjCRPoolAtencion> listPoolAtencion = new ArrayList<CjCRPoolAtencion>();
        
        try{
           Client client = Client.create();
           consulta = "\"SELECT * FROM TACJCCTRPOOLATENCION WITH(NOLOCK) ORDER BY FDFECHAINSERTA ASC\"";
           String url = servidor+servicio;
           WebResource webResource = client.resource(url);
           String input = "{"+sucursalLabel+idSucursal+consultasLabel+consulta+limitRowLabel+limitRow+"}";
           ClientResponse response = webResource.type("application/json").post(ClientResponse.class,input);
           
           if(response.getStatus() != 200){
               throw new RuntimeException("Error: Error en el codigo HTTP "+response.getStatus());
           }
           
            String output = response.getEntity(String.class); 
            ObjectMapper mapper = new ObjectMapper();
            List<CjCRSucursalWs> resultSucursalWS = mapper.readValue(output, new TypeReference<List <CjCRSucursalWs>>(){});
            for(CjCRSucursalWs po : resultSucursalWS){
                for(String [] filas : po.getResultados()){
                    CjCRPoolAtencion oPoolAtencion = new CjCRPoolAtencion();
                    oPoolAtencion.setFcEmpNoId(filas[0]);
                    oPoolAtencion.setFiPaisId(paisId); // ------ Whatch this
                    oPoolAtencion.setFiCanalId(canalId); // ------ Whatch this
                    oPoolAtencion.setFiSucursalId(idSucursal); // ------ Whatch this
                    oPoolAtencion.setFiStatusPoolId(Integer.parseInt(filas[2]));
                    oPoolAtencion.setFcRutaImagen(filas[1]);
                    oPoolAtencion.setFdFechaInserta(filas[4]);
                    oPoolAtencion.setFcUserInserta(filas[5]);
                    oPoolAtencion.setFdFechaModif(filas[6]);
                    oPoolAtencion.setFcUserModif(filas[7]);
                    oPoolAtencion.setFcPuntoAtencion(filas[3]);
                    listPoolAtencion.add(oPoolAtencion);
                }               
            }
            
           
        }catch(Exception e){
            LOG.error("Error en la extraccion de TACJCCTRPOOLATENCION", e);
        }
        
        return listPoolAtencion;
    }
    
    public List<CjCRTurno> consultaTurno(){
        LOG.info("Inicia proceso de extraccion de TACJCCTRTURNO Service");
        consultaControl();
        List<CjCRTurno> listTurno = new ArrayList<CjCRTurno>();
        
        try{
           Client client = Client.create();
           consulta = "\"SELECT * FROM TACJCCTRTURNO WITH(NOLOCK) WHERE "+proceso.procesoModo()+" ORDER BY FDFECHAINSERTA ASC\"";
           String url = servidor+servicio;
           WebResource webResource = client.resource(url);
           String input = "{"+sucursalLabel+idSucursal+consultasLabel+consulta+limitRowLabel+limitRow+"}";
           ClientResponse response = webResource.type("application/json").post(ClientResponse.class,input);
           
           if(response.getStatus() != 200){
               throw new RuntimeException("Error: Error en el codigo HTTP "+response.getStatus());
           }
           
            String output = response.getEntity(String.class); 
            ObjectMapper mapper = new ObjectMapper();
            List<CjCRSucursalWs> resultSucursalWS = mapper.readValue(output, new TypeReference<List <CjCRSucursalWs>>(){});
            for(CjCRSucursalWs po : resultSucursalWS){
                for(String [] filas : po.getResultados()){
                    CjCRTurno oTurno = new CjCRTurno();
                    oTurno.setFiFecha(Integer.parseInt(filas[0]));
                    oTurno.setFiTurnoId(Integer.parseInt(filas[1]));
                    oTurno.setFiUnidadNegocioId(Integer.parseInt(filas[5]));
                    oTurno.setFiPaisId(paisId);
                    oTurno.setFiCanalId(canalId);
                    oTurno.setFiSucursalId(idSucursal);                
                    oTurno.setFiOrigenId(Integer.parseInt(filas[2]));
                    oTurno.setFcEmpNoId(filas[3]);
                    oTurno.setFiFilaId(Integer.parseInt(filas[4]));
                    oTurno.setFiTurnoSeguimiento(Integer.parseInt(filas[6]));
                    oTurno.setFiPrioridad(Integer.parseInt(filas[7]));
                    oTurno.setFiStatusTurno(Integer.parseInt(filas[8]));
                    oTurno.setFiVirtual(Integer.parseInt(filas[9]));
                    oTurno.setFdFechaInserta(filas[10]);
                    oTurno.setFcUserInserta(filas[11]);
                    oTurno.setFdFechaModif(filas[12]);
                    oTurno.setFcUserModif(filas[13]);
                    listTurno.add(oTurno);
                }               
            }
                  
        }catch(Exception e){
            LOG.error("Error en la extraccion de TACJCCTRTURNO", e);
        }
        
        return listTurno;
    }
    
    public List<CjCRHistorico> consultaHistorico(){
        LOG.info("Inicia proceso de extraccion de TACJCCTRHISTORICO Service");
        consultaControl();
        List<CjCRHistorico> listHistorico = new ArrayList<CjCRHistorico>();
        
        try{
           Client client = Client.create();
           consulta = "\"SELECT * FROM TACJCCTRHISTORICO WITH(NOLOCK) WHERE "+proceso.procesoModo()+" ORDER BY FDFECHAINSERTA ASC\"";
           String url = servidor+servicio;
           WebResource webResource = client.resource(url);
           String input = "{"+sucursalLabel+idSucursal+consultasLabel+consulta+limitRowLabel+limitRow+"}";
           ClientResponse response = webResource.type("application/json").post(ClientResponse.class,input);
           
           if(response.getStatus() != 200){
               throw new RuntimeException("Error: Error en el codigo HTTP "+response.getStatus());
           }
           
            String output = response.getEntity(String.class); 
            ObjectMapper mapper = new ObjectMapper();
            List<CjCRSucursalWs> resultSucursalWS = mapper.readValue(output, new TypeReference<List <CjCRSucursalWs>>(){});
            for(CjCRSucursalWs po : resultSucursalWS){
                for(String [] filas : po.getResultados()){
                    CjCRHistorico oHistorico = new CjCRHistorico();
                    oHistorico.setFiFecha(Integer.parseInt(filas[0]));
                    oHistorico.setFiTurnoId(Integer.parseInt(filas[1]));
                    oHistorico.setFiUnidadNegocioId(Integer.parseInt(filas[2]));
                    oHistorico.setFiStatusTurnoId(Integer.parseInt(filas[3]));
                    oHistorico.setFiPaisId(paisId);
                    oHistorico.setFiCanalId(canalId);
                    oHistorico.setFiSucursalId(idSucursal);
                    oHistorico.setFdActualizacion(filas[4]);
                    oHistorico.setFdFechaInserta(filas[5]);
                    oHistorico.setFcUserInserta(filas[6]);
                    oHistorico.setFdFechaModif(filas[7]);
                    oHistorico.setFcUserModif(filas[8]);
                    listHistorico.add(oHistorico);
                }               
            }
                  
        }catch(Exception e){
            LOG.error("Error en la extraccion de TACJCCTRHISTORICO", e);
        }
        
        return listHistorico;
    }
    
}
