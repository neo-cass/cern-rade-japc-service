package cern.rade.services.japc;

import cern.japc.core.AcquiredParameterValue;
import cern.japc.core.Parameter;
import cern.japc.core.ParameterException;
import cern.japc.core.Selector;
import cern.japc.core.factory.ParameterFactory;
import cern.japc.core.factory.SelectorFactory;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;

@WebServlet(value = "/init",loadOnStartup = 0)
public class Initialise extends HttpServlet {

//    @Override
//    public void init() throws ServletException {
//       // super.init();
//        System.out.println("Init test");
//        try {
//            System.setProperty("japc.ccs.descriptors.enabled", "true");
//            Parameter p1 = ParameterFactory.newInstance().newParameter("LNE.VVGBH.0434/STAQ");
//            Parameter p = ParameterFactory.newInstance().newParameter("LNE.VVGBH.0434/State");
//            Selector sl = SelectorFactory.newSelector("");
//            AcquiredParameterValue apv = p.getValue(sl);
//            AcquiredParameterValue apv1 = p1.getValue(sl);
//            System.out.println(apv);
//            System.out.println(apv1);
//        } catch (ParameterException e) {
//            throw new RuntimeException(e);
//        }
//    }
}
