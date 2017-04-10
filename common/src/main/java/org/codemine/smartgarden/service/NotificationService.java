/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.codemine.smartgarden.service;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author benchan
 */
public class NotificationService {
    
    private String email;
    private String password;
    
    private static Logger logger = Logger.getLogger(NotificationService.class);
    
    public NotificationService(String email,String password){
        this.email=email;
        this.password=password;
    }
    
    public void sendAsyncEmail(final String subject,final String body){
          
        Thread sendMailThread = new Thread() {
            @Override
            public void run() {
                try {
                    Properties props = new Properties();
                    props.put("mail.smtp.host", "smtp.gmail.com");
                    props.put("mail.smtp.socketFactory.port", "465");
                    props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                    props.put("mail.smtp.auth", "true");
                    props.put("mail.smtp.port", "465");

                    Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(email, password);
                        }
                    });

                    Message message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(email));
                    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
                    message.setSubject(subject);
                    message.setText(body);
                    Transport.send(message);
                } catch (MessagingException e) {
                    logger.log(Level.ERROR, "sendEmail", e);
                }
            }

        };
        sendMailThread.start();
    }
}
