package utilities;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;

public class EmailReportSharer {

    private final EmailConfigManager configManager;
    private final CustomLogger logger = new CustomLogger();

    public EmailReportSharer(EmailConfigManager configManager) {
        this.configManager = configManager;
    }

    /**
     * Sends the Extent HTML report as an attachment with an HTML summary body.
     *
     * @param reportPath absolute/relative path to the generated HTML report
     * @param toList     TO recipients
     * @param ccList     CC recipients (may be empty)
     * @param status     "PASSED" or "FAILED"
     * @param passCount  number of passed tests
     * @param failCount  number of failed tests
     * @param skipCount  number of skipped tests
     * @return true if email was sent successfully
     */
    public boolean sendReport(String reportPath,
                              List<String> toList,
                              List<String> ccList,
                              String status,
                              int passCount,
                              int failCount,
                              int skipCount) {
        logger.logInfo("📧 ── sendReport() called ──────────────────────────────────────");
        logger.logInfo("📧 Status     : " + status + "  (pass=" + passCount + " fail=" + failCount + " skip=" + skipCount + ")");
        logger.logInfo("📧 Report path: " + reportPath);

        if (!configManager.isEmailEnabled()) {
            logger.logWarning("📧 Email sending is DISABLED in email_config.properties – skipping.");
            return false;
        }
        if (toList == null || toList.isEmpty()) {
            logger.logWarning("📧 No TO recipients configured – skipping email.");
            return false;
        }

        String sender = configManager.getSenderEmail();
        String password = configManager.getSenderPassword();
        String provider = configManager.getProvider();

        logger.logInfo("📧 Provider   : " + provider);
        logger.logInfo("📧 Sender     : " + sender);

        if (sender.isEmpty() || password.isEmpty()) {
            logger.logError("❌ Email credentials not configured in email_config.properties.");
            return false;
        }

        // ── SMTP settings ──────────────────────────────────────────────────────
        Properties props = new Properties();
        if ("gmail".equalsIgnoreCase(provider)) {
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
        } else {
            logger.logError("Unsupported email provider: " + provider);
            return false;
        }

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(sender, password);
            }
        });

        try {
            logger.logInfo("📧 Building email message...");
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            logger.logInfo("📧 From       : " + sender);

            // TO
            for (String to : toList) {
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(to.trim()));
            }
            logger.logInfo("📧 To         : " + toList);

            // CC
            if (ccList != null) {
                for (String cc : ccList) {
                    if (!cc.trim().isEmpty()) {
                        message.addRecipient(Message.RecipientType.CC, new InternetAddress(cc.trim()));
                    }
                }
                if (!ccList.isEmpty()) logger.logInfo("📧 Cc         : " + ccList);
            }

            // Subject
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String subject = String.format("[%s] %s – %s",
                    status.toUpperCase(),
                    configManager.getSubjectPrefix(),
                    timestamp);
            message.setSubject(subject);
            logger.logInfo("📧 Subject    : " + subject);

            // ── Multipart: HTML body + HTML attachment ─────────────────────────
            Multipart multipart = new MimeMultipart();

            // Part 1 – HTML body with summary table
            MimeBodyPart htmlBody = new MimeBodyPart();
            htmlBody.setContent(buildHtmlBody(status, passCount, failCount, skipCount, timestamp), "text/html; charset=utf-8");
            multipart.addBodyPart(htmlBody);
            logger.logInfo("📧 HTML body added (pass=" + passCount + ", fail=" + failCount + ", skip=" + skipCount + ")");

            // Part 2 – Extent HTML report attachment
            File reportFile = new File(reportPath);
            if (reportFile.exists()) {
                MimeBodyPart attachPart = new MimeBodyPart();
                DataSource source = new FileDataSource(reportFile);
                attachPart.setDataHandler(new DataHandler(source));
                attachPart.setFileName(reportFile.getName());
                multipart.addBodyPart(attachPart);
                logger.logInfo("📧 Attachment : " + reportFile.getName()
                        + "  (" + (reportFile.length() / 1024) + " KB)");
            } else {
                logger.logWarning("📧 Report file not found – sending without attachment: " + reportPath);
            }


            message.setContent(multipart);

            logger.logInfo("📧 Connecting to SMTP " + props.getProperty("mail.smtp.host")
                    + ":" + props.getProperty("mail.smtp.port") + " ...");
            Transport.send(message);

            logger.logInfo("✅ Email sent successfully to: " + toList);
            return true;

        } catch (MessagingException e) {
            logger.logError("❌ Failed to send email: " + e.getMessage());
            return false;
        }
    }

    // ── Legacy overload (kept for backward compatibility) ─────────────────────
    public boolean sendReportAsAttachment(String reportPath, List<String> recipients) {
        return sendReport(reportPath, recipients, null, "COMPLETED", 0, 0, 0);
    }

    // ── HTML email body ───────────────────────────────────────────────────────

    private String buildHtmlBody(String status, int pass, int fail, int skip, String timestamp) {
        String statusColor = "PASSED".equalsIgnoreCase(status) ? "#28a745" : "#dc3545";
        String statusIcon = "PASSED".equalsIgnoreCase(status) ? "✅" : "❌";
        int total = pass + fail + skip;

        return "<!DOCTYPE html><html><body style='font-family:Arial,sans-serif;background:#f4f4f4;padding:20px'>" +
                "<div style='max-width:600px;margin:auto;background:#fff;border-radius:8px;padding:30px;box-shadow:0 2px 8px rgba(0,0,0,.1)'>" +

                // Header
                "<h2 style='margin:0 0 20px;color:#333'>🧪 T3 Widget Smoke Test Report</h2>" +

                // Status badge
                "<div style='display:inline-block;padding:8px 20px;border-radius:20px;color:#fff;font-weight:bold;font-size:18px;background:" + statusColor + "'>" +
                statusIcon + " " + status + "</div>" +
                "<p style='color:#666;margin-top:10px'>Run completed at: <strong>" + timestamp + "</strong></p>" +

                // Summary table
                "<table style='width:100%;border-collapse:collapse;margin:20px 0'>" +
                "<tr style='background:#f8f8f8'>" +
                "  <th style='padding:10px;border:1px solid #ddd;text-align:left'>Metric</th>" +
                "  <th style='padding:10px;border:1px solid #ddd;text-align:center'>Count</th>" +
                "</tr>" +
                "<tr><td style='padding:10px;border:1px solid #ddd'>Total Tests</td>" +
                "    <td style='padding:10px;border:1px solid #ddd;text-align:center'><strong>" + total + "</strong></td></tr>" +
                "<tr style='background:#d4edda'><td style='padding:10px;border:1px solid #ddd'>✅ Passed</td>" +
                "    <td style='padding:10px;border:1px solid #ddd;text-align:center;color:#28a745;font-weight:bold'>" + pass + "</td></tr>" +
                "<tr style='background:#f8d7da'><td style='padding:10px;border:1px solid #ddd'>❌ Failed</td>" +
                "    <td style='padding:10px;border:1px solid #ddd;text-align:center;color:#dc3545;font-weight:bold'>" + fail + "</td></tr>" +
                "<tr style='background:#fff3cd'><td style='padding:10px;border:1px solid #ddd'>⏭️ Skipped</td>" +
                "    <td style='padding:10px;border:1px solid #ddd;text-align:center;color:#856404;font-weight:bold'>" + skip + "</td></tr>" +
                "</table>" +

                // Footer
                "<p style='color:#999;font-size:12px;border-top:1px solid #eee;padding-top:10px'>Full report attached as HTML file. Open in browser to view charts and step details.</p>" +
                "</div></body></html>";
    }
}
