package com.baz.scc.turnador.main;

import com.baz.scc.commons.util.CjCRSpringContext;
import com.baz.scc.commons.util.CjCRUtils;
import com.baz.scc.turnador.dao.CjCRTurnadorServiceDao;
import com.baz.scc.turnador.logic.CjCRTurnadorLogic;
import java.text.ParseException;
import org.apache.log4j.Logger;

class CjCRBootstrap {

    private static final Logger LOG = Logger.getLogger(CjCRBootstrap.class);

    private CjCRBootstrap() {
    }

    public static void main(String[] args) {
        try {
            CjCRSpringContext.init();
            CjCRTurnadorLogic turnadorLogic = CjCRSpringContext.getBean(CjCRTurnadorLogic.class);
            
            turnadorLogic.muestaInfoInicial();
            turnadorLogic.cargaTotal();
//            turnadorLogic.muestraInfoServer();
//            turnadorLogic.imprimeTurnos();
//            turnadorLogic.imprimeHistorico();
//            turnadorLogic.imprimePoolAtencion();  
//            turnadorLogic.impromePoolAtencionOracle();
              //turnadorLogic.pruevaValida();
        } catch (Exception e) {
            LOG.error("Error",e);
        }
    }
}
