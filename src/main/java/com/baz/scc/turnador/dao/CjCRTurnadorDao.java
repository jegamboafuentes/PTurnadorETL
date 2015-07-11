package com.baz.scc.turnador.dao;

import com.baz.scc.turnador.model.*;
import com.baz.scc.turnador.support.CjCRPAppConfig;
import com.baz.scc.turnador.support.CjCRPModoFuente;
import com.baz.scc.turnador.support.CjCRPModoProceso;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class CjCRTurnadorDao {
    
    private static final Logger LOG = Logger.getLogger(CjCRTurnadorDao.class);
    public int cantidadRegistros;
    @Autowired
    private CjCRPModoFuente modoFuente ;
    //SQL
    @Autowired
    @Qualifier("sqlJdbcTemplate")
    private JdbcTemplate sqlJdbcTemplate;
    //Oracle
    @Autowired
    @Qualifier("usrcajaJdbcTemplate")
    private JdbcTemplate usrcajaJdbcTemplate;
    //AppConfig
    @Autowired
    private CjCRPModoProceso proceso;
        
    public CjCRControl consultaControl(){
        CjCRControl registroControl =  sqlJdbcTemplate.query("SELECT * FROM CONTROL WITH(NOLOCK)", new ResultSetExtractor<CjCRControl>() {

            @Override
            public CjCRControl extractData(ResultSet rs) throws SQLException, DataAccessException {
                CjCRControl controlO = new CjCRControl();
                if(rs.next()){
                    controlO.setFiNoTienda(rs.getInt(1));
                    controlO.setFcTdaNombre(rs.getString(2));
                    controlO.setFiIdCia(rs.getInt(3));
                    controlO.setFcCiaNombre(rs.getString(4));
                    controlO.setFiIdCanal(rs.getInt(5));
                    controlO.setFiIdPais(rs.getInt(6));
                    controlO.setFiArea(rs.getInt(7));
                            
                }
                return controlO;
            }
        });
        return registroControl;
    }
    
    // ----------------------------------------------------------------------------------------------- Pool Atencion Inicio
    public List<CjCRPoolAtencion> consultaPoolAtencion(){
        LOG.info("Inicia proceso de extraccion de TACJCCTRPOOLATENCION");
        final CjCRControl oControl = consultaControl();
        oControl.setFiNoTienda(1643); //<---------
        
        return sqlJdbcTemplate.query("SELECT * FROM TACJCCTRPOOLATENCION WITH(NOLOCK) ORDER BY FDFECHAINSERTA ASC", new RowMapper<CjCRPoolAtencion>(){
            @Override
            public CjCRPoolAtencion mapRow(ResultSet rs, int rowNum) throws SQLException {
                CjCRPoolAtencion poolAtencion = new CjCRPoolAtencion();
                poolAtencion.setFcEmpNoId(rs.getString(1));
                poolAtencion.setFcRutaImagen(rs.getString(2));
                poolAtencion.setFiStatusPoolId(rs.getInt(3)); 
                poolAtencion.setFcPuntoAtencion(rs.getString(4));
                poolAtencion.setFdFechaInserta(rs.getString(5));
                poolAtencion.setFcUserInserta(rs.getString(6));
                poolAtencion.setFdFechaModif(rs.getString(7));
                poolAtencion.setFcUserModif(rs.getString(8));
                poolAtencion.setFiPaisId(oControl.getFiIdPais());
                poolAtencion.setFiCanalId(oControl.getFiIdCanal());
                poolAtencion.setFiSucursalId(oControl.getFiNoTienda());
                
                return poolAtencion;
            }
             
        });
    } //Consulta el Pool de atencion de SQL Server
    
    public List<CjCRPoolAtencion> consultaPoolAtencionOracle(){
        return usrcajaJdbcTemplate.query("SELECT * FROM TACJTRPOOLATEN WHERE FISUCURSALID = "+modoFuente.getSucursal()+" ORDER BY FDFECHAINSERTA ASC", new RowMapper<CjCRPoolAtencion>() {

            @Override
            public CjCRPoolAtencion mapRow(ResultSet rs, int rowNum) throws SQLException {
                CjCRPoolAtencion poolAtencion = new CjCRPoolAtencion();
                
                poolAtencion.setFcEmpNoId(rs.getString(1));
                poolAtencion.setFcRutaImagen(rs.getString(5));
                poolAtencion.setFiStatusPoolId(rs.getInt(6)); 
                poolAtencion.setFcPuntoAtencion(rs.getString(7));
                poolAtencion.setFdFechaInserta(rs.getString(8));
                poolAtencion.setFcUserInserta(rs.getString(9));
                poolAtencion.setFdFechaModif(rs.getString(10));
                poolAtencion.setFcUserModif(rs.getString(11));
                poolAtencion.setFiPaisId(rs.getInt(2));
                poolAtencion.setFiCanalId(rs.getInt(3));
                poolAtencion.setFiSucursalId(rs.getInt(4));
                
                return poolAtencion;
            }
             
        });
    } //Consulta datos de Oracle
    
    public void insertaPoolEmpleado(final List<CjCRPoolAtencion> pool, final int cantidadDatos){
       // LOG.info("Inicia carga de datos TACJCCTRPOOLATENCION");
        String sql = "INSERT INTO TACJTRPOOLATEN"
                + " (FCEMPNOID, FIPAISID, FICANALID, FISUCURSALID, FCRUTAIMAGEN, FISTATUSPOOLID, FDFECHAINSERTA, FCUSERINSERTA, FDFECHAMODIF, FCUSERMODIF, FCPUNTOATENCION)"
                + "   VALUES (?,?,?,?,?,?,TO_TIMESTAMP(?,'YYYY.MM.DD HH24:MI:SS.FF'),?,TO_TIMESTAMP(?,'YYYY.MM.DD HH24:MI:SS.FF'),?,?)";
        BatchPreparedStatementSetter batch = new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                
                CjCRPoolAtencion oPool = pool.get(i);     
                dedicatedPoolAtencionOperacion(ps, oPool); 
            }
            @Override
            public int getBatchSize() {
                return cantidadDatos;
            }
        };
        
        try{
            usrcajaJdbcTemplate.batchUpdate(sql, batch);
            //LOG.info("Se agregaron con exito: "+cantidadRegistros+" registros");
        }catch(DataAccessException e){
            LOG.error("Error en la insercion masiva TACJTRPOOLATEN", e);
        }
    }
    
    private void dedicatedPoolAtencionOperacion(PreparedStatement ps, CjCRPoolAtencion poolAtencion){
        try{
                    ps.setString(1, poolAtencion.getFcEmpNoId());
                    ps.setInt(2, poolAtencion.getFiPaisId());
                    ps.setInt(3, poolAtencion.getFiCanalId());
                    ps.setInt(4, poolAtencion.getFiSucursalId());
                    ps.setString(5, poolAtencion.getFcRutaImagen());
                    ps.setInt(6, poolAtencion.getFiStatusPoolId());
                    ps.setString(7, poolAtencion.getFdFechaInserta());
                    ps.setString(8, poolAtencion.getFcUserInserta());
                    ps.setString(9, poolAtencion.getFdFechaModif());
                    ps.setString(10, poolAtencion.getFcUserModif());
                    ps.setString(11, poolAtencion.getFcPuntoAtencion());          
                    cantidadRegistros++;
                }catch(SQLException ex){
                    LOG.error("Error al insertar Registros: "+poolAtencion, ex);
                }
    }
    // ----------------------------------------------------------------------------------------------- Pool Atencion Fin
    // ----------------------------------------------------------------------------------------------- Turno Inicio
    public List<CjCRTurno> consultaTurno(){
        LOG.info("Inicia proceso de extraccion de TACJCCTRTURNO");
        final CjCRControl oControl = consultaControl();
        
        oControl.setFiNoTienda(1643); //<---------
        return sqlJdbcTemplate.query("SELECT * FROM TACJCCTRTURNO WITH(NOLOCK) WHERE "+proceso.procesoModo(), new RowMapper<CjCRTurno>(){
            @Override
            public CjCRTurno mapRow(ResultSet rs, int rowNum) throws SQLException {
                CjCRTurno turnos = new CjCRTurno();
       
                turnos.setFiFecha(rs.getInt(1));
                turnos.setFiTurnoId(rs.getInt(2)); 
                turnos.setFiOrigenId(rs.getInt(3));
                turnos.setFcEmpNoId(rs.getString(4));
                turnos.setFiFilaId(rs.getInt(5));        
                turnos.setFiUnidadNegocioId(rs.getInt(6));
                turnos.setFiTurnoSeguimiento(rs.getInt(7));
                turnos.setFiPrioridad(rs.getInt(8));
                turnos.setFiStatusTurno(rs.getInt(9));
                turnos.setFiVirtual(rs.getInt(10));      
                turnos.setFdFechaInserta(rs.getString(11));
                turnos.setFcUserInserta(rs.getString(12));
                turnos.setFdFechaModif(rs.getString(13));
                turnos.setFcUserModif(rs.getString(14));       
                turnos.setFiPaisId(oControl.getFiIdPais());
                turnos.setFiCanalId(oControl.getFiIdCanal());
                turnos.setFiSucursalId(oControl.getFiNoTienda());
                return turnos;
            }
        });
        
    }
    
    public List<CjCRTurno> consultaTurnoOracle(){
        return usrcajaJdbcTemplate.query("SELECT * FROM TACJTRTURNO WHERE "+proceso.procesoModo()+" AND FISUCURSALID = "+modoFuente.getSucursal()+" ORDER BY FDFECHAINSERTA ASC", new RowMapper<CjCRTurno>() {

            @Override
            public CjCRTurno mapRow(ResultSet rs, int rowNum) throws SQLException {
                CjCRTurno turnos = new CjCRTurno();
                
                turnos.setFiFecha(rs.getInt(1));
                turnos.setFiTurnoId(rs.getInt(2)); 
                turnos.setFiOrigenId(rs.getInt(7));
                turnos.setFcEmpNoId(rs.getString(8));
                turnos.setFiFilaId(rs.getInt(9));        
                turnos.setFiUnidadNegocioId(rs.getInt(3));
                turnos.setFiTurnoSeguimiento(rs.getInt(10));
                turnos.setFiPrioridad(rs.getInt(11));
                turnos.setFiStatusTurno(rs.getInt(12));
                turnos.setFiVirtual(rs.getInt(13));      
                turnos.setFdFechaInserta(rs.getString(14));
                turnos.setFcUserInserta(rs.getString(15));
                turnos.setFdFechaModif(rs.getString(16));
                turnos.setFcUserModif(rs.getString(17));       
                turnos.setFiPaisId(rs.getInt(4));
                turnos.setFiCanalId(rs.getInt(5));
                turnos.setFiSucursalId(rs.getInt(6));
                return turnos;
            }
             
        });
    } //Consulta datos de Oracle
    
    public void insertaTurno(final List<CjCRTurno> turnos, final int cantidadDatos){
        //LOG.info("Inicia carga de datos TACJCCTRTURNO");
        String sql = "INSERT INTO TACJTRTURNO"
                + " (FIFECHA, FITURNOID, FIUNIDADNEGOCIOID, FIPAISID, FICANALID, FISUCURSALID, FIORIGENID, FCEMPNOID, FIFILAID, FITURNOSEGUIMIENTO, FIPRIORIDAD, FISTATUSTURNO, FIVIRTUAL, FDFECHAINSERTA, FCUSERINSERTA, FDFECHAMODIF, FCUSERMODIF)"
                + "   VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,TO_TIMESTAMP(?,'YYYY.MM.DD HH24:MI:SS.FF'),?,TO_TIMESTAMP(?,'YYYY.MM.DD HH24:MI:SS.FF'),?)";
        BatchPreparedStatementSetter batch = new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                
                CjCRTurno oTurno = turnos.get(i);     
                dedicatedTurnosOperacion(ps, oTurno);
            }

            @Override
            public int getBatchSize() {
                return cantidadDatos;
            }
        };
        
        try{
            usrcajaJdbcTemplate.batchUpdate(sql, batch);
            //LOG.info("Se agregaron con exito: "+cantidadRegistros+" registros");
        }catch(DataAccessException e){
            LOG.error("Error en la insercion masiva TACJTRTURNO", e);
        }
        
    }
    
    private void dedicatedTurnosOperacion(PreparedStatement ps, CjCRTurno turno){
        try{
                    ps.setInt(1, turno.getFiFecha());
                    ps.setInt(2, turno.getFiTurnoId());
                    ps.setInt(3, turno.getFiUnidadNegocioId());
                    ps.setInt(4, turno.getFiPaisId());
                    ps.setInt(5, turno.getFiCanalId());
                    ps.setInt(6, turno.getFiSucursalId());
                    ps.setInt(7, turno.getFiOrigenId());
                    ps.setString(8, turno.getFcEmpNoId());
                    ps.setInt(9, turno.getFiFilaId());
                    ps.setInt(10, turno.getFiTurnoSeguimiento());
                    ps.setInt(11, turno.getFiPrioridad());
                    ps.setInt(12, turno.getFiStatusTurno());
                    ps.setInt(13, turno.getFiVirtual());
                    ps.setString(14, turno.getFdFechaInserta());
                    ps.setString(15, turno.getFcUserInserta());
                    ps.setString(16, turno.getFdFechaModif());
                    ps.setString(17, turno.getFcUserModif());
                    
                    cantidadRegistros++;
                }catch(SQLException ex){
                    LOG.error("Error al insertar Registros: "+turno, ex);
                }
    }
    // ----------------------------------------------------------------------------------------------- Turno Fin
    // ----------------------------------------------------------------------------------------------- Historico Inicio
    public List<CjCRHistorico> consultaHistorico(){
        LOG.info("Inicia proceso de extraccion de TACJCCTRHISTORICO");
        final CjCRControl oControl = consultaControl();
        oControl.setFiNoTienda(1643); //<---------
        return sqlJdbcTemplate.query("SELECT * FROM TACJCCTRHISTORICO WITH(NOLOCK) WHERE "+proceso.procesoModo(), new RowMapper<CjCRHistorico>(){
            @Override
            public CjCRHistorico mapRow(ResultSet rs, int rowNum) throws SQLException {
                CjCRHistorico historico = new CjCRHistorico();
                historico.setFiFecha(rs.getInt(1));
                historico.setFiTurnoId(rs.getInt(2));
                historico.setFiUnidadNegocioId(rs.getInt(3));
                historico.setFiStatusTurnoId(rs.getInt(4));
                historico.setFdActualizacion(rs.getString(5));
                historico.setFdFechaInserta(rs.getString(6));
                historico.setFcUserInserta(rs.getString(7));
                historico.setFdFechaModif(rs.getString(8));
                historico.setFcUserModif(rs.getString(9));
                historico.setFiPaisId(oControl.getFiIdPais());
                historico.setFiCanalId(oControl.getFiIdCanal());
                historico.setFiSucursalId(oControl.getFiNoTienda());
                return historico;
            }
             
        });
    }
    
    public List<CjCRHistorico> consultaHistoricoOracle(){
        return usrcajaJdbcTemplate.query("SELECT * FROM TACJTRHISTORICO WHERE "+proceso.procesoModo()+" AND FISUCURSALID = "+modoFuente.getSucursal()+" ORDER BY FDFECHAINSERTA ASC", new RowMapper<CjCRHistorico>() {

            @Override
            public CjCRHistorico mapRow(ResultSet rs, int rowNum) throws SQLException {
                CjCRHistorico historico = new CjCRHistorico();
                
                historico.setFiFecha(rs.getInt(1));
                historico.setFiTurnoId(rs.getInt(2));
                historico.setFiUnidadNegocioId(rs.getInt(3));
                historico.setFiStatusTurnoId(rs.getInt(4));
                historico.setFdActualizacion(rs.getString(8));
                historico.setFdFechaInserta(rs.getString(9));
                historico.setFcUserInserta(rs.getString(10));
                historico.setFdFechaModif(rs.getString(11));
                historico.setFcUserModif(rs.getString(12));
                historico.setFiPaisId(rs.getInt(5));
                historico.setFiCanalId(rs.getInt(6));
                historico.setFiSucursalId(rs.getInt(7));
                return historico;
            }
             
        });
    } //Consulta datos de Oracle
    
    public void insertaHistorico(final List<CjCRHistorico> historico, final int cantidadDatos){
        //LOG.info("Inicia carga de datos TACJCCTRHISTORICO");
        String sql = "INSERT INTO TACJTRHISTORICO"
                + " (FIFECHA, FITURNOID, FIUNIDADNEGOCIOID, FISTATUSTURNOID, FIPAISID, FICANALID, FISUCURSALID, FDACTUALIZACION, FDFECHAINSERTA, FCUSERINSERTA, FDFECHAMODIF, FCUSERMODIF)"
                + "   VALUES (?,?,?,?,?,?,?,TO_TIMESTAMP(?,'YYYY.MM.DD HH24:MI:SS.FF'),TO_TIMESTAMP(?,'YYYY.MM.DD HH24:MI:SS.FF'),?,TO_TIMESTAMP(?,'YYYY.MM.DD HH24:MI:SS.FF'),?)";
        BatchPreparedStatementSetter batch = new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                
                CjCRHistorico oHistorico = historico.get(i);     
                dedicatedHistoricoOperacion(ps, oHistorico);
            }

            @Override
            public int getBatchSize() {
                return cantidadDatos;
            }
        };
        
        try{
            usrcajaJdbcTemplate.batchUpdate(sql, batch);
            //LOG.info("Se agregaron con exito: "+cantidadRegistros+" registros");
        }catch(DataAccessException e){
            LOG.error("Error en la insercion masiva TACJTRHISTORICO", e);
        }
        
    }
   
    private void dedicatedHistoricoOperacion(PreparedStatement ps, CjCRHistorico historico){
        try{
                    ps.setInt(1, historico.getFiFecha());
                    ps.setInt(2, historico.getFiTurnoId());
                    ps.setInt(3, historico.getFiUnidadNegocioId());
                    ps.setInt(4, historico.getFiStatusTurnoId());
                    ps.setInt(5, historico.getFiPaisId());
                    ps.setInt(6, historico.getFiCanalId());
                    ps.setInt(7, historico.getFiSucursalId());
                    ps.setString(8, historico.getFdActualizacion());                 
                    ps.setString(9, historico.getFdFechaInserta());
                    ps.setString(10, historico.getFcUserInserta());
                    ps.setString(11, historico.getFdFechaModif());
                    ps.setString(12, historico.getFcUserModif());
                    
                    cantidadRegistros++;
                }catch(SQLException ex){
                    LOG.error("Error al insertar Registros: "+historico, ex);
                }
    }
    // ----------------------------------------------------------------------------------------------- Historico Inicio
    
    
    
}


