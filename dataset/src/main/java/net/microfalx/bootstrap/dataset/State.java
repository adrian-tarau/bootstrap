package net.microfalx.bootstrap.dataset;

/**
 * An enum for a data set state.
 */
public enum State {

    /**
     * Data set is in view mode (current record is displayed in all details)
     */
    VIEW,

    /**
     * Data set is in edit mode (current record is edited)
     */
    EDIT,

    /**
     * Data set is in add mode (current record is a newly created record)
     */
    ADD,

    /**
     * Data set is in browse mode (navigating the result set)
     */
    BROWSE
}
