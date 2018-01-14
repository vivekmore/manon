package manon.util.basetest;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import manon.Application;
import manon.user.UserExistsException;
import manon.user.UserNotFoundException;
import manon.user.document.User;
import manon.user.registration.service.RegistrationService;
import manon.user.service.UserAdminService;
import manon.user.service.UserService;
import manon.util.Tools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.config.EncoderConfig.encoderConfig;
import static java.lang.System.currentTimeMillis;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * Recreate data before test class.
 * To recreate data before every test method, see {@link InitBeforeTest}.
 */
@Slf4j
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@TestExecutionListeners(listeners = DependencyInjectionTestExecutionListener.class)
public abstract class InitBeforeClass extends BaseTests {
    
    @Setter
    private boolean initialized = false;
    
    @LocalServerPort
    private int port;
    
    @Autowired
    protected UserService userService;
    @Autowired
    protected UserAdminService userAdminService;
    @Autowired
    protected RegistrationService registrationService;
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    private final Map<Integer, User> userCache = new HashMap<>();
    
    public long userCount;
    
    public int getNumberOfUsers() {
        return 2;
    }
    
    @BeforeClass
    public void beforeClass() {
        initialized = false;
        RestAssured.config.encoderConfig(encoderConfig().defaultContentCharset("UTF-8"));
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }
    
    @BeforeMethod
    public void beforeMethod() throws Exception {
        if (!initialized) {
            initDb();
            initialized = true;
        }
    }
    
    public void initDb() throws UserExistsException, UserNotFoundException {
        long t1 = currentTimeMillis();
        for (String cn : mongoTemplate.getDb().listCollectionNames()) {
            mongoTemplate.dropCollection(cn);
        }
        userCache.clear();
        userAdminService.ensureAdmin();
        for (int idx = 0; idx < getNumberOfUsers(); idx++) {
            registrationService.activate(registrationService.registerPlayer(makeName(idx), makePwd(idx)).getId());
        }
        userCount = userService.count();
        log.debug("(Unit Test) called initDb from test class {}, took {} ms", this.getClass().getSimpleName(), currentTimeMillis() - t1);
    }
    
    public String makeName(int idx) {
        return "USERNAME" + idx;
    }
    
    public String makePwd(int idx) {
        return "p4ssw0rd" + idx;
    }
    
    @AfterClass
    public void afterClass() {
        for (String cn : mongoTemplate.getDb().listCollectionNames()) {
            mongoTemplate.dropCollection(cn);
        }
        setInitialized(false);
    }
    
    //
    // Helpers: get generated test users and authenticate with their credentials
    //
    
    public Rs whenAnonymous() {
        return new Rs(RestAssured.given().auth().none(), "", "");
    }
    
    public Rs whenAdmin() {
        return new Rs(RestAssured.given().auth().basic(ADMIN_NAME, ADMIN_PWD), ADMIN_NAME, ADMIN_PWD);
    }
    
    /** When player n°humanId, where humanId is an index starting at 1. */
    public Rs whenPX(int humanId) {
        int idx = humanId - 1;
        return new Rs(RestAssured.given().auth().basic(
                makeName(idx), makePwd(idx)),
                makeName(idx), makePwd(idx));
    }
    
    /** When player 1. */
    public Rs whenP1() {
        return whenPX(1);
    }
    
    /** When player 2. */
    public Rs whenP2() {
        return whenPX(2);
    }
    
    /** When player 3. */
    public Rs whenP3() {
        return whenPX(3);
    }
    
    @SuppressWarnings("SameParameterValue")
    public String pwd(int humanId) {
        return makePwd(humanId - 1);
    }
    
    @SuppressWarnings("SameParameterValue")
    public String name(int humanId) {
        return makeName(humanId - 1);
    }
    
    /** Get user id of player n°humanId, where humanId is an index starting at 1. */
    @SuppressWarnings("SameParameterValue")
    public String userId(int humanId) {
        return findAndCacheUserByhumanId(humanId).getId();
    }
    
    private User findAndCacheUserByhumanId(int humanId) {
        int idx = humanId - 1;
        if (!userCache.containsKey(idx)) {
            userCache.put(idx, userService.readByUsername(makeName(idx)));
        }
        return userCache.get(idx);
    }
    
    //
    // DataProviders
    //
    
    public final String DP_TRUEFALSE = "dataProviderTrueFalse";
    
    @DataProvider
    public Object[][] dataProviderTrueFalse() {
        return new Object[][]{
                {true},
                {false}
        };
    }
    
    //
    // Utils
    //
    
    /** Convert object to JSON. */
    @SneakyThrows(IOException.class)
    public <T> T readValue(Response content, Class<T> valueType) {
        return Tools.JSON.readValue(content.asString(), valueType);
    }
    
    /** Compute a long string. */
    public String verylongString(String base) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 500; i++) {
            sb.append(base);
        }
        return sb.toString();
    }
}
