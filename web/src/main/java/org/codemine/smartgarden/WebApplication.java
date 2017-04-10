package org.codemine.smartgarden;

import javax.sql.DataSource;

import org.codemine.smartgarden.controller.SmartGardenServlet;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import org.apache.catalina.Context;

import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.ContextResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.jndi.JndiObjectFactoryBean;

@SpringBootApplication()
public class WebApplication extends SpringBootServletInitializer {

    @Value("${dataSource.jdbcDataSourceClassName}")
    private String jdbcDataSourceClassName;
    @Value("${dataSource.jdbcUrl}")
    private String jdbcUrl;
    @Value("${dataSource.jdbcUsername}")
    private String jdbcUsername;
    @Value("${dataSource.jdbcPassword}")
    private String jdbcPassword;
    
    private static boolean testing=false;

    public static void main(final String[] args) {
        if (args.length > 0){
            testing="testing".equalsIgnoreCase(args[0]);
        }
        new SpringApplication(WebApplication.class).run(args);
    }

    @Override
    protected final SpringApplicationBuilder configure(final SpringApplicationBuilder application) {
        return application.sources(WebApplication.class);
    }

    @Bean
    public ServletRegistrationBean servletRegistrationBean() {
        return new ServletRegistrationBean(new SmartGardenServlet(), "/smartgarden/*");
    }

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        super.onStartup(servletContext); //To change body of generated methods, choose Tools | Templates.
        servletContext.setAttribute("testing", WebApplication.testing);
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
                ContextResource resource = new ContextResource();
                resource.setName("jdbc/smartgarden-dataSource");
                resource.setType(DataSource.class.getName());
                resource.setProperty("dataSourceClassName", jdbcDataSourceClassName);
                resource.setProperty("url", jdbcUrl);
                resource.setProperty("username", jdbcUsername);
                resource.setProperty("password", jdbcPassword);
                context.getNamingResources().addResource(resource);
            }
        };
    }

    @Bean(destroyMethod = "close")
    @Scope("prototype")
    public DataSource dataSource() throws IllegalArgumentException, NamingException {
        JndiObjectFactoryBean bean = new JndiObjectFactoryBean();
        bean.setJndiName("java:comp/env/jdbc/smartgarden-dataSource");
        bean.setProxyInterface(DataSource.class);
        bean.setLookupOnStartup(false);
        bean.afterPropertiesSet();
        DataSource dataSource = (DataSource) bean.getObject();

        return dataSource;
    }

}
