package manon.user.api;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import manon.user.UserNotFoundException;
import manon.user.document.User;
import manon.util.basetest.InitBeforeClass;
import manon.util.web.UserPage;
import org.testng.annotations.Test;

import java.util.List;

import static manon.app.config.ControllerAdviceBase.FIELD_ERRORS;
import static manon.app.config.ControllerAdviceBase.FIELD_MESSAGE;
import static manon.user.registration.RegistrationStateEnum.ACTIVE;
import static manon.user.registration.RegistrationStateEnum.BANNED;
import static manon.user.registration.RegistrationStateEnum.SUSPENDED;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class UserAdminWSTest extends InitBeforeClass {
    
    @Test
    public void shouldFindAllDesc() {
        Response res = whenAdmin().getRequestSpecification()
                .get(API_USER_ADMIN + "/all?offset=0&size=100&sort=creationDate,DESC");
        res.then()
                .contentType(ContentType.JSON)
                .statusCode(SC_OK);
        UserPage result = readValue(res, UserPage.class);
        List<User> users = result.getContent();
        assertEquals(users.size(), userCount);
        assertEquals(result.getTotalElements(), userCount);
        for (int i = 1; i < users.size(); i++) {
            long top = users.get(i - 1).getCreationDate().getTime();
            long bottom = users.get(i).getCreationDate().getTime();
            assertTrue(top >= bottom, "order");
        }
    }
    
    @Test
    public void shouldFindAllAsc() {
        Response res = whenAdmin().getRequestSpecification()
                .get(API_USER_ADMIN + "/all?offset=0&size=100&sort=creationDate,ASC");
        res.then()
                .contentType(ContentType.JSON)
                .statusCode(SC_OK);
        UserPage result = readValue(res, UserPage.class);
        List<User> users = result.getContent();
        assertEquals(users.size(), userCount);
        assertEquals(result.getTotalElements(), userCount);
        for (int i = 1; i < users.size(); i++) {
            long top = users.get(i - 1).getCreationDate().getTime();
            long bottom = users.get(i).getCreationDate().getTime();
            assertTrue(top <= bottom, "order");
        }
    }
    
    @Test
    public void shouldFindAllSmallPageStartPart() {
        Response res = whenAdmin().getRequestSpecification()
                .get(API_USER_ADMIN + "/all?size=3");
        res.then()
                .contentType(ContentType.JSON)
                .statusCode(SC_OK);
        UserPage result = readValue(res, UserPage.class);
        List<User> users = result.getContent();
        assertEquals(users.size(), 3);
        assertEquals(result.getTotalElements(), userCount);
    }
    
    @Test
    public void shouldFindAllSmallPageEndPart() {
        Response res = whenAdmin().getRequestSpecification()
                .get(API_USER_ADMIN + "/all?page=1&size=3");
        res.then()
                .contentType(ContentType.JSON)
                .statusCode(SC_OK);
        UserPage result = readValue(res, UserPage.class);
        List<User> users = result.getContent();
        assertEquals(users.size(), userCount - 3);
        assertEquals(result.getTotalElements(), userCount);
    }
    
    @Test
    public void shouldFindAllSmallPageMiddlePart() {
        Response res = whenAdmin().getRequestSpecification()
                .get(API_USER_ADMIN + "/all?size=1");
        res.then()
                .contentType(ContentType.JSON)
                .statusCode(SC_OK);
        UserPage result = readValue(res, UserPage.class);
        List<User> users = result.getContent();
        assertEquals(users.size(), 1);
        assertEquals(result.getTotalElements(), userCount);
    }
    
    @Test
    public void shouldCycleRegistrationState() {
        List<String> uids = List.of(userId(1), userId(2));
        for (String uid : uids) {
            whenAdmin().getRequestSpecification()
                    .post(API_USER_ADMIN + "/" + uid + "/suspend")
                    .then()
                    .statusCode(SC_OK)
                    .contentType(ContentType.TEXT)
                    .body(equalTo(SUSPENDED.name()));
            whenAdmin().getRequestSpecification()
                    .post(API_USER_ADMIN + "/" + uid + "/ban")
                    .then()
                    .statusCode(SC_OK)
                    .contentType(ContentType.TEXT)
                    .body(equalTo(BANNED.name()));
            whenAdmin().getRequestSpecification()
                    .post(API_USER_ADMIN + "/" + uid + "/activate")
                    .then()
                    .statusCode(SC_OK)
                    .contentType(ContentType.TEXT)
                    .body(equalTo(ACTIVE.name()));
        }
    }
    
    @Test
    public void shouldNotActivateUnknown() {
        whenAdmin().getRequestSpecification()
                .post(API_USER_ADMIN + "/" + UNKNOWN_USER_ID + "/activate")
                .then()
                .statusCode(SC_NOT_FOUND)
                .contentType(ContentType.JSON)
                .body(FIELD_ERRORS, equalTo(UserNotFoundException.class.getSimpleName()))
                .body(FIELD_MESSAGE, equalTo(UNKNOWN_USER_ID));
    }
    
    @Test
    public void shouldNotBanUnknown() {
        whenAdmin().getRequestSpecification()
                .post(API_USER_ADMIN + "/" + UNKNOWN_USER_ID + "/ban")
                .then()
                .statusCode(SC_NOT_FOUND)
                .contentType(ContentType.JSON)
                .body(FIELD_ERRORS, equalTo(UserNotFoundException.class.getSimpleName()))
                .body(FIELD_MESSAGE, equalTo(UNKNOWN_USER_ID));
    }
    
    @Test
    public void shouldNotSuspendUnknown() {
        whenAdmin().getRequestSpecification()
                .post(API_USER_ADMIN + "/" + UNKNOWN_USER_ID + "/suspend")
                .then()
                .statusCode(SC_NOT_FOUND)
                .contentType(ContentType.JSON)
                .body(FIELD_ERRORS, equalTo(UserNotFoundException.class.getSimpleName()))
                .body(FIELD_MESSAGE, equalTo(UNKNOWN_USER_ID));
    }
}