package manon.service.user.impl;

import lombok.SneakyThrows;
import manon.document.user.User;
import manon.document.user.UserSnapshot;
import manon.util.Tools;
import manon.util.basetest.AbstractIT;
import org.assertj.core.api.Assertions;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;

public class UserSnapshotServiceIT extends AbstractIT {

    @Test
    public void shouldFindOne() throws Exception {
        saveUserSnapshot();
        UserSnapshot us = saveUserSnapshot();
        saveUserSnapshot();

        Assertions.assertThat(userSnapshotService.findOne(us.getId()).orElseThrow(Exception::new)).isEqualTo(us);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void shouldFindOneFailReadLazyDataOutsideASession() {
        UserSnapshot us = saveUserSnapshot();

        Assertions.assertThatThrownBy(() -> userSnapshotService.findOne(us.getId()).orElseThrow(Exception::new).getUser().hashCode())
            .isInstanceOf(LazyInitializationException.class);
    }

    @Test
    public void shouldFailFindOneUnknown() {
        Assertions.assertThat(userSnapshotService.findOne(UNKNOWN_ID)).isEmpty();
    }

    public Object[] dataProviderCount() {
        return new Object[][]{
            {0, 0},
            {1, 1},
            {2, 2}
        };
    }

    @ParameterizedTest
    @MethodSource("dataProviderCount")
    public void shouldCount(int created, int expected) {
        for (int i = 0; i < created; i++) {
            saveUserSnapshot();
        }

        Assertions.assertThat(userSnapshotService.count()).isEqualTo(expected);
    }

    @Test
    public void shouldCountToday() {
        for (int i = 0; i < 3; i++) {
            userSnapshotService.saveAll(Arrays.asList(
                makeUserSnapshot(),
                saveUserSnapshot().toBuilder().creationDate(Tools.yesterday()).build(),
                saveUserSnapshot().toBuilder().creationDate(Tools.tomorrow()).build()
            ));
        }

        Assertions.assertThat(userSnapshotService.countToday()).isEqualTo(3);
    }

    @Test
    public void shouldDeleteToday() {
        LocalDateTime yesterday = Tools.yesterday();
        LocalDateTime tomorrow = Tools.tomorrow();
        for (int i = 0; i < 3; i++) {
            userSnapshotService.saveAll(Arrays.asList(
                makeUserSnapshot(),
                saveUserSnapshot().toBuilder().creationDate(yesterday).build(),
                saveUserSnapshot().toBuilder().creationDate(tomorrow).build()
            ));
        }

        userSnapshotService.deleteToday();

        Assertions.assertThat(userSnapshotService.countToday()).isEqualTo(0);
        Assertions.assertThat(userSnapshotService.count()).isEqualTo(6);
        userSnapshotService.findAll().forEach(userSnapshot ->
            Assertions.assertThat(userSnapshot.getCreationDate())
                .isBetween(yesterday, tomorrow));
    }

    @Test
    public void shouldKeepRecent() {
        LocalDateTime before2Days = Tools.nowMinusDays(2);
        for (int i = 0; i < 3; i++) {
            userSnapshotService.saveAll(Arrays.asList(
                makeUserSnapshot(),
                saveUserSnapshot().toBuilder().creationDate(Tools.nowMinusDays(3)).build(),
                saveUserSnapshot().toBuilder().creationDate(before2Days).build(),
                saveUserSnapshot().toBuilder().creationDate(Tools.yesterday()).build(),
                saveUserSnapshot().toBuilder().creationDate(Tools.tomorrow()).build()
            ));
        }

        userSnapshotService.keepRecent(Duration.ofDays(2));

        Assertions.assertThat(userSnapshotService.count()).isEqualTo(9);
        userSnapshotService.findAll().forEach(userSnapshot ->
            Assertions.assertThat(userSnapshot.getCreationDate()).isAfterOrEqualTo(before2Days));
    }

    @Test
    public void shouldSaveAll() {
        LocalDateTime before = Tools.now();
        userSnapshotService.saveAll(Arrays.asList(
            makeUserSnapshot(),
            makeUserSnapshot()
        ));
        LocalDateTime after = Tools.now();

        userSnapshotService.findAll().forEach(userSnapshot -> {
            Assertions.assertThat(userSnapshot.getUser().getId()).isEqualTo(user(1).getId());
            Assertions.assertThat(userSnapshot.getCreationDate()).isBetween(before, after);
        });
    }

    @SneakyThrows
    private UserSnapshot makeUserSnapshot() {
        User user = userService.findByUsername(name(1)).orElseThrow(Exception::new);
        return UserSnapshot.from(user);
    }

    private UserSnapshot saveUserSnapshot() {
        return userSnapshotService.save(makeUserSnapshot());
    }
}
