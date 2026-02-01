package net.microfalx.bootstrap.dos.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.jdbc.entity.natural.NamedAndTimestampedIdentityAware;

@Entity(name = "DosRuleStats")
@Table(name = "dos_rule_stats")
@Getter
@Setter
@ToString
public class RuleStats extends NamedAndTimestampedIdentityAware<Integer> {

    @Column(name = "country")
    private String country;

    @Column(name = "country_code")
    private String countryCode;

    @Column(name = "region")
    private String region;

    @Column(name = "region_code")
    private String regionCode;

    @Column(name = "city")
    private String city;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "request_count")
    private long requestCount;

    @Column(name = "deny_count")
    private long denyCount;

    @Column(name = "throttle_count")
    private long throttleCount;
}
