package manon.service.user;

import manon.document.user.FriendshipEvent;
import manon.err.user.UserNotFoundException;
import manon.model.user.FriendshipEventCode;
import manon.util.ExistForTesting;

import java.util.List;

public interface FriendshipEventService {
    
    List<FriendshipEvent> findAllFriendshipEventsByUserOrderByCreationDateDesc(long userId);
    
    void registerEvents(long userIdFrom, long userIdTo, FriendshipEventCode eventCodeFrom, FriendshipEventCode eventCodeTo)
        throws UserNotFoundException;
    
    @ExistForTesting(why = "FriendshipWSIntegrationTest")
    long countAllFriendshipEventsByUser(long userId);
    
    @ExistForTesting(why = "AbstractIntegrationTest")
    void deleteAll();
}