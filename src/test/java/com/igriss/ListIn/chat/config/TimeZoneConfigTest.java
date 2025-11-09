package com.igriss.ListIn.chat.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

class TimeZoneConfigTest {

    private final TimeZone originalTimeZone = TimeZone.getDefault();

    @AfterEach
    void restoreOriginalTimeZone() {
        TimeZone.setDefault(originalTimeZone);
    }

    @Test
    void init_shouldSetDefaultTimeZoneToParis() {
        TimeZoneConfig config = new TimeZoneConfig();

        config.init();

        assertThat(TimeZone.getDefault().getID()).isEqualTo("Europe/Paris");
    }
}
