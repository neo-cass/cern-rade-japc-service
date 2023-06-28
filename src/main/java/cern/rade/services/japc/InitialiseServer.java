package cern.rade.services.japc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@SpringBootApplication
@ServletComponentScan
public class InitialiseServer {
    private static Logger logger = LoggerFactory.getLogger(InitialiseServer.class);
    public static void main(String[] args) {
        SpringApplication.run(InitialiseServer.class, args);
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> logger.error("Uncaught exception in thread {}.", t, e));
    }

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

}

   // public static void main(String[] args) throws Exception {
//            Tomcat tomcat = new Tomcat();
//            tomcat.setPort(8080);
//
//            Context context =  tomcat.addWebapp("/japc", "webapp-build/japc-1.0.0.war");
//            tomcat.start();
//            tomcat.getServer().await();
//        }


//            StandardContext ctx = (StandardContext) tomcat.addWebapp("/", new File(webappDirLocation).getAbsolutePath());
//            System.out.println("configuring app with basedir: " + new File("./" + webappDirLocation).getAbsolutePath());
// Declare an alternative location for your "WEB-INF/classes" dir
//            // Servlet 3.0 annotation will work
//            File additionWebInfClasses = new File("target/classes");
//            WebResourceRoot resources = new StandardRoot(ctx);
//            resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes",
//                    additionWebInfClasses.getAbsolutePath(), "/"));
//            ctx.setResources(resources);
