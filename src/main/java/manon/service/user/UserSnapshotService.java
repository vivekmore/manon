package manon.service.user;

import manon.document.user.UserSnapshot;

import java.util.Optional;

public interface UserSnapshotService {
    
    Optional<UserSnapshot> findOne(long id);
    
    long count();
    
    /** Count User snapshots created today. */
    long countToday();
    
    /** Delete User snapshots created today. */
    void deleteToday();
    
    /** Keep recent User snapshots only.
     * Snapshots older than {@code maxAgeInDays} days are deleted, in addition to future ones. */
    void keepRecent(int maxAgeInDays);
    
    void save(Iterable<UserSnapshot> entities);
}