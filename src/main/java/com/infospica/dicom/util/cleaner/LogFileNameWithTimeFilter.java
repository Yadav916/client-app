package com.infospica.dicom.util.cleaner;

import com.infospica.dicom.Constants;
import com.infospica.dicom.util.LoggerUtility;

import java.io.File;
import java.io.FilenameFilter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogFileNameWithTimeFilter implements FilenameFilter {
    private final LocalDate consideredDate;
    private final Pattern pattern;
    private final Matcher matcher;
    private final Integer backupTimeUpto;
    private final Integer backupMillisUpto;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public LogFileNameWithTimeFilter(String start, int keepDays) {
        String regPattern = "^" + start + "(\\d+-\\d+-\\d+)(_)([0-9]+)_([0-9]{3}+).*$";
        LoggerUtility.log(LogFileNameWithTimeFilter.class, LoggerUtility.LogLevel.DEBUG, "Considering the pattern: " + regPattern);
        pattern = Pattern.compile(regPattern);
        matcher = pattern.matcher("");

        LocalDate date = LocalDate.now();
        this.consideredDate = date.minusDays(keepDays);
        LocalDateTime consideredDateTimeUpto = this.consideredDate.atTime(LocalTime.now());
        Date ldate = Date.from(consideredDateTimeUpto.atZone(ZoneId.systemDefault()).toInstant());
        this.backupTimeUpto = Integer.valueOf(Constants._TIME_ONLY.format(ldate));
        this.backupMillisUpto = 999;
    }

    @Override
    public boolean accept(File file, String name) {
        if(new File(file, name).isFile()) {
            matcher.reset(name);
            if(!matcher.find()) return false;
            return isDateMatches(matcher.group(1), matcher.group(3), matcher.group(4));
        }
        return false;
    }

    private boolean isDateMatches(String fNameDate, String fNameTime, String fNameMillis) {
        LocalDate localFDate = LocalDate.parse(fNameDate, formatter);
        if(localFDate.isBefore(this.consideredDate)) {
            return true;
        } else if(localFDate.isEqual(this.consideredDate)) {
            return (Integer.valueOf(fNameTime) < this.backupTimeUpto && Integer.valueOf(fNameMillis) < this.backupMillisUpto);
        }
        return false;
    }
}
