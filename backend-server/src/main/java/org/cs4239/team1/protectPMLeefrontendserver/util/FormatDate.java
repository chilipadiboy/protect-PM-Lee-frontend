package org.cs4239.team1.protectPMLeefrontendserver.util;

import org.cs4239.team1.protectPMLeefrontendserver.exception.BadRequestException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

import static java.time.ZoneOffset.UTC;

public class FormatDate {

    public static Instant formatDate(String date){

        String dateTime = date + " 23:59:59";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        TemporalAccessor temporalAccessor = formatter.parse(dateTime);
        LocalDateTime localDateTime = LocalDateTime.from(temporalAccessor);
        ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, UTC);
        Instant expirationDateTime = Instant.from(zonedDateTime);
        if (expirationDateTime.isBefore(Instant.now())){
            throw new BadRequestException("Invalid Date");
        }

        return expirationDateTime;
    }
}
