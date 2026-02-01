package net.microfalx.bootstrap.dos.web;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.lang.annotation.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class RequestCounts {

    @Id
    @Visible(false)
    private String id;

    @Position(1)
    @Description("The hostname (as provided in the request) or IP if it cannot be resolved")
    @Width("200px")
    private String name;

    @Position(2)
    @Description("The IP or hostname (as provided in the request)")
    @Width("150px")
    private String ip;

    @Position(3)
    @Description("The canonical hostname (extracted using reverse DNS lookup)")
    @Width("15%")
    private String canonicalHostName;

    @Position(10)
    @Description("The average throughput (requests per second)")
    @Width("90px")
    @Formattable(unit = Formattable.Unit.THROUGHPUT_REQUESTS)
    private float throughput;

    @Position(20)
    @Label(value = "Access", group = "Counts")
    @Description("The number of accesses recorded")
    @Width("90px")
    private int accessCount;

    @Position(20)
    @Label(value = "Not Found", group = "Counts")
    @Description("The number of requests which ended in 'not found'")
    @Width("90px")
    private int notFoundCount;

    @Position(21)
    @Label(value = "Failure", group = "Counts")
    @Description("The number of requests which ended in failure")
    @Width("90px")
    private int failureCount;

    @Position(22)
    @Label(value = "Security", group = "Counts")
    @Description("The number of requests which ended for security reasons")
    @Width("90px")
    private int securityCount;

    @Position(23)
    @Label(value = "Validation", group = "Counts")
    @Description("The number of requests which were invalid due to business rule validation (field validations)")
    @Width("90px")
    private int validationCount;

    @Position(23)
    @Label(value = "Invalid", group = "Counts")
    @Description("The number of requests which were invalid and could not be processed")
    @Width("90px")
    private int invalidCount;

    @Position(30)
    @Description("The location associated with the IP address (if available)")
    @Width("10%")
    private String location;

    @Position(100)
    @Description("The timestamp of the first request")
    @Width("180px")
    private LocalDateTime createdAt;

    @Position(101)
    @Description("The timestamp of the last request")
    @Width("180px")
    private LocalDateTime modifiedAt;

}
