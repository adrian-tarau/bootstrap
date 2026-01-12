package net.microfalx.bootstrap.support.report.fragment;

import net.microfalx.bootstrap.support.report.AbstractFragmentProvider;
import net.microfalx.bootstrap.support.report.Fragment;

@net.microfalx.lang.annotation.Provider
public class SummaryProvider extends AbstractFragmentProvider {

    @Override
    public Fragment create() {
        return Fragment.builder("Summary").template("summary")
                .icon("fa-solid fa-list-check")
                .order(10)
                .build();
    }


}
