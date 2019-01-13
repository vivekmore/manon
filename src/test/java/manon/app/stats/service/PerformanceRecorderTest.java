package manon.app.stats.service;

import org.junit.jupiter.api.Test;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;

public class PerformanceRecorderTest {
    
    private static final String STATS_TITLE = "Services performance (ms):";
    private static final String STATS_HEADER = " calls     min     max     total     avg  median  name";
    
    @Test
    public void shouldEmptyShowStats() {
        PerformanceRecorderImpl performanceRecorder = new PerformanceRecorderImpl(Clock.systemDefaultZone());
        assertThat(performanceRecorder.isEmpty()).isTrue();
        assert (performanceRecorder.showStats()).isEmpty();
    }
    
    @Test
    public void shouldShowStats() {
        PerformanceRecorderImpl performanceRecorder = new PerformanceRecorderImpl(Clock.systemDefaultZone());
        
        performanceRecorder.saveTime("manon.user.UserService:do1()", 1);
        performanceRecorder.saveTime("manon.user.UserService:do1()", 2);
        performanceRecorder.saveTime("manon.user.UserService:do1()", 2);
        performanceRecorder.saveTime("manon.user.UserService:do2()", 100);
        performanceRecorder.saveTime("manon.user.UserService:do2()", 200);
        performanceRecorder.saveTime("manon.user.UserService:do2()", 300);
        performanceRecorder.saveTime("manon.user.UserService:do2()", 200);
        
        assertThat(performanceRecorder.isEmpty()).isFalse();
        assertThat(performanceRecorder.showStats()).isEqualTo(STATS_TITLE + "\n" + STATS_HEADER + "\n" +
            "     3       1       2         5       1       2  m.user.UserService:do1()\n" +
            "     4     100     300       800     200     200  m.user.UserService:do2()\n" +
            STATS_HEADER);
    }
    
    @Test
    public void shouldShowStatsManyTimesWithDifferentOrders() {
        PerformanceRecorderImpl performanceRecorder = new PerformanceRecorderImpl(Clock.systemDefaultZone());
        
        performanceRecorder.saveTime("manon.user.UserService:do1()", 1);
        performanceRecorder.saveTime("manon.user.UserService:do2()", 100);
        
        assertThat(performanceRecorder.isEmpty()).isFalse();
        assertThat(performanceRecorder.showStats()).isEqualTo(STATS_TITLE + "\n" + STATS_HEADER + "\n" +
            "     1       1       1         1       1       1  m.user.UserService:do1()\n" +
            "     1     100     100       100     100     100  m.user.UserService:do2()\n" +
            STATS_HEADER);
        
        performanceRecorder.saveTime("manon.user.UserService:do1()", 2);
        performanceRecorder.saveTime("manon.user.UserService:do1()", 2000);
        performanceRecorder.saveTime("manon.user.UserService:do2()", 200);
        performanceRecorder.saveTime("manon.user.UserService:do2()", 300);
        performanceRecorder.saveTime("manon.user.UserService:do2()", 200);
        
        assertThat(performanceRecorder.isEmpty()).isFalse();
        assertThat(performanceRecorder.showStats()).isEqualTo(STATS_TITLE + "\n" + STATS_HEADER + "\n" +
            "     4     100     300       800     200     200  m.user.UserService:do2()\n" +
            "     3       1    2000      2003     667       2  m.user.UserService:do1()\n" +
            STATS_HEADER);
    }
}
