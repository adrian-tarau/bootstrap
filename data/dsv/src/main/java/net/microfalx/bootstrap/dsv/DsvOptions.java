package net.microfalx.bootstrap.dsv;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class DsvOptions {

    /**
     * Whether the first line of the DSV file contains headers.
     */
    @Builder.Default private boolean header = true;

    /**
     * The character used to separate values in the DSV file.
     */
    @Builder.Default private String delimiter = ",";

    /**
     * The character used to quote values in the DSV file.
     */
    @Builder.Default private char quote = '\"';

    /**
     * The DSV header if the file does not contain one.
     */
    private String[] columns;
}
