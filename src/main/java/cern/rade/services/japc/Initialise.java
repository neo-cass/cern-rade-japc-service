package cern.rade.services.japc;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;

@WebServlet(value = "/init",loadOnStartup = 0)
public class Initialise extends HttpServlet {

    @Override
    public void init() throws ServletException {
       // super.init();
        System.out.println("Init test");
    }
}
