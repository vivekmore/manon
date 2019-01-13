package manon.app.trace.service;

import manon.app.trace.model.AppTraceLevel;
import manon.util.basetest.AbstractInitBeforeClass;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

import static manon.app.trace.model.AppTraceEvent.APP_START;
import static manon.app.trace.model.AppTraceEvent.UPTIME;
import static org.assertj.core.api.Assertions.assertThat;

public class AppTraceServiceIntegrationTest extends AbstractInitBeforeClass {
    
    @Autowired
    private AppTraceService appTraceService;
    
    @Override
    public int getNumberOfUsers() {
        return 0;
    }
    
    @Test
    public void shouldLogUptime() {
        appTraceService.deleteByCurrentAppIdAndEvent(UPTIME);
        long nbTraces = appTraceService.count();
        for (int i = 0; i < 3; i++) {
            appTraceService.logUptime();
        }
        assertThat(appTraceService.count()).isEqualTo(nbTraces + 1);
        assertThat(appTraceService.countByCurrentAppId()).isEqualTo(appTraceService.count());
        assertThat(appTraceService.countByCurrentAppIdAndEvent(UPTIME)).isEqualTo(1);
        
        String appId = appTraceService.getAppId();
        appTraceService.findAll().forEach(appTrace -> assertThat(appTrace.getAppId()).isEqualTo(appId));
    }
    
    public Object[] dataProviderShouldLog() {
        return AppTraceLevel.values();
    }
    
    @ParameterizedTest
    @MethodSource("dataProviderShouldLog")
    public void shouldLog(AppTraceLevel level) {
        appTraceService.deleteByCurrentAppIdAndEvent(APP_START);
        long nbTraces = appTraceService.count();
        for (int i = 0; i < 3; i++) {
            appTraceService.log(level, APP_START, "foo");
            appTraceService.log(level, APP_START);
        }
        assertThat(appTraceService.count()).isEqualTo(nbTraces + 6);
        assertThat(appTraceService.countByCurrentAppId()).isEqualTo(appTraceService.count());
        assertThat(appTraceService.countByCurrentAppIdAndEvent(APP_START)).isEqualTo(6);
        
        String appId = appTraceService.getAppId();
        appTraceService.findAll().forEach(appTrace -> assertThat(appTrace.getAppId()).isEqualTo(appId));
    }
}
