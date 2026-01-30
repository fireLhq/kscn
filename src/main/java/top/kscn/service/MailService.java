package top.kscn.service;

public interface MailService {
    void sendMessage(String toEmail, String subject, String content);
}