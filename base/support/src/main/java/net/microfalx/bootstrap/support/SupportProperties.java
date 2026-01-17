package net.microfalx.bootstrap.support;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("bootstrap.support")
@Setter
@Getter
@ToString
public class SupportProperties {

    /**
     * Indicates whether the system report is enabled.
     */
    private boolean reportEnabled = true;

    /**
     * Indicates whether the system report should be generated on boot.
     */
    private boolean reportOnBoot = true;

    /**
     * The report schedule in CRON format.
     */
    private String reportSchedule = "0 0 0 * * ?";

    /**
     * A comma-separated list of report recipients (email addresses).
     * <p>
     * If the list is empty, the report will be sent to all admins.
     */
    private String reportRecipients = "";

    /**
     * The password used to encrypt the report attachment.
     * <p>
     * If empty, no encryption will be applied. While a default password is provided, it is strongly recommended to
     * change it to a secure value to ensure the confidentiality of the report data (the report might contain sensitive
     * information).
     */
    private String reportPassword = "WzeXYN1j4S675lLsL4Vy";

    /**
     * A name of the reporting system.
     * <p>
     * If the list is empty, the hostname will be used.
     */
    private String reportSystemName = "";
}
