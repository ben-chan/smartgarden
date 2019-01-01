package org.codemine.smartgarden;

import org.codemine.smartgarden.controller.SmartGardenServlet;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import org.apache.catalina.Context;

import org.apache.catalina.startup.Tomcat;
import org.codemine.smartgarden.service.SmartGardenService;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.support.SpringBootServletInitializer;

@SpringBootApplication()
public class WebApplication extends SpringBootServletInitializer {

    public static void main(final String[] args) {
        SpringApplication application = new SpringApplication(WebApplication.class);
        boolean hasProfile = false;
        for (String arg : args) {
            if (arg.contains("--spring.profiles.active=dev")) {
                application.setAdditionalProfiles("dev");
                hasProfile = true;
            }
        }
        if (!hasProfile) {
            application.setAdditionalProfiles("dev");
        }
        application.run(args);
    }

    @Override
    protected final SpringApplicationBuilder configure(final SpringApplicationBuilder application) {
        return application.sources(WebApplication.class);
    }

    @Bean
    public ServletRegistrationBean servletRegistrationBean(SmartGardenService smartGardenService) {
        return new ServletRegistrationBean(new SmartGardenServlet(), "/smartgarden/*");
    }

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        super.onStartup(servletContext); //To change body of generated methods, choose Tools | Templates.
    }

    @Bean
    public TomcatEmbeddedServletContainerFactory tomcatFactory() {
        return new TomcatEmbeddedServletContainerFactory() {

            @Override
            protected TomcatEmbeddedServletContainer getTomcatEmbeddedServletContainer(
                    Tomcat tomcat) {
                tomcat.enableNaming();
                return super.getTomcatEmbeddedServletContainer(tomcat);
            }

            @Override
            protected void postProcessContext(Context context) {
//                ContextResource resource = new ContextResource();
//                resource.setName("jdbc/smartgarden-dataSource");
//                resource.setType(DataSource.class.getName());
//                resource.setProperty("dataSourceClassName", jdbcDataSourceClassName);
//                resource.setProperty("url", jdbcUrl);
//                resource.setProperty("username", jdbcUsername);
//                resource.setProperty("password", jdbcPassword);
//                context.getNamingResources().addResource(resource);
            }
        };
    }

//    @Bean(destroyMethod = "close")
//    @Scope("prototype")
//    public DataSource dataSource() throws IllegalArgumentException, NamingException {
//        JndiObjectFactoryBean bean = new JndiObjectFactoryBean();
//        bean.setJndiName("java:comp/env/jdbc/smartgarden-dataSource");
//        bean.setProxyInterface(DataSource.class);
//        bean.setLookupOnStartup(false);
//        bean.afterPropertiesSet();
//        DataSource dataSource = (DataSource) bean.getObject();
//
//        return dataSource;
//    }
}
