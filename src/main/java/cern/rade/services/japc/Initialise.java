package cern.rade.services.japc;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;

@WebServlet(value = "/init",loadOnStartup = 0)
public class Initialise extends HttpServlet {
    Logger logger = LoggerFactory.getLogger(Initialise.class);

    @Override
    public void init() throws ServletException {
        // super.init();

        logger.debug("init");
        logger.info("test");
        logger.error("test");
    }
}
