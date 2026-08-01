package ma.lias.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
  private final JavaMailSender mailSender;
  @Value("${app.mail-enabled:false}") private boolean enabled;
  public EmailService(JavaMailSender mailSender){this.mailSender=mailSender;}
  public void send(String to, String subject, String body){
    if(!enabled){ System.out.println("MAIL DISABLED -> "+to+" | "+subject+" | "+body); return; }
    SimpleMailMessage msg = new SimpleMailMessage(); msg.setTo(to); msg.setSubject(subject); msg.setText(body); mailSender.send(msg);
  }
}
