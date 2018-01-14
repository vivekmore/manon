package manon.user.repository;

import manon.user.document.User;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String>, UserRepositoryCustom {
    
    @NotNull Optional<User> findById(@NotNull String id);
    
    Optional<User> findByUsername(String username);
    
    @Query(value = "{'username':?0 }", exists = true)
    boolean usernameExists(String username);
}
