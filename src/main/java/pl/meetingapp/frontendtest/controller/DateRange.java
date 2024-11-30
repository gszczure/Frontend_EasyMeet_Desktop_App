package pl.meetingapp.frontendtest.controller;

import lombok.Getter;
import lombok.Setter;

// Klasa DataRange jest używana do reprezentowania przedzialu daty (Data range) wybranej przez uzytkownika. Zrobiona jest po to by Id kazdego z przedzialu nie bylo wyswietlane w ListView, a dalej bylo uzywane podczas usuwania wybranego przedzialu
@Getter
@Setter
public class DateRange {
    private String startDate;
    private String endDate;
    private Long dateRangeId;
    private String userFullName;

    public DateRange(String startDate, String endDate, Long dateRangeId, String userFullName) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.dateRangeId = dateRangeId;
        this.userFullName = userFullName;
    }

    @Override
    public String toString() {
        return startDate + " to " + endDate + " ( Added by: " + userFullName + ")";
    }
}
