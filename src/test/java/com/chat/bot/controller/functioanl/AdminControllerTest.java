package com.chat.bot.controller.functioanl;

import com.chat.bot.entity.ConversationNode;
import com.chat.bot.service.AdminService;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Functional tests for the AdminController class.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AdminControllerTest {

    /**
     * port
     */
    @LocalServerPort
    private int port;

    /**
     * Admin service
     */
    @MockBean
    private AdminService adminService;

    /**
     * Sets up the test environment before each test.
     */
    @BeforeEach
    public void setup() {
        RestAssured.port = port;
    }

    /**
     * Tests the listing of conversation nodes.
     */
    @Test
    public void testListNodes() {
        List<ConversationNode> nodes = Collections.singletonList(new ConversationNode());
        when(adminService.getAllNodes()).thenReturn(nodes);

        given()
                .when()
                .get("/admin")
                .then()
                .statusCode(HttpStatus.OK.value());

        verify(adminService, times(1)).getAllNodes();
    }

    /**
     * Tests the editing of a conversation node.
     */
    @Test
    public void testEditNode() {
        ConversationNode node = new ConversationNode();
        when(adminService.getNodeById(1L)).thenReturn(node);

        given()
                .when()
                .get("/admin/edit/1")
                .then()
                .statusCode(HttpStatus.OK.value());

        verify(adminService, times(1)).getNodeById(1L);
    }

    /**
     * Tests the updating of a conversation node.
     */
    @Test
    public void testUpdateNode() {
        doNothing().when(adminService).saveNode(any(ConversationNode.class));

        given()
                .contentType(ContentType.JSON)
                .formParam("id", "1")
                .formParam("message", "Test Message")
                .formParam("messageName", "Test Name")
                .formParam("nodeType", "NORMAL_NODE")
                .when()
                .post("/admin/update/1")
                .then()
                .statusCode(HttpStatus.FOUND.value());

        verify(adminService, times(1)).saveNode(any(ConversationNode.class));
    }

    /**
     * Tests the deletion of a conversation node.
     */
    @Test
    public void testDeleteNode() {
        doNothing().when(adminService).deleteNode(1L);

        given()
                .when()
                .get("/admin/delete/1")
                .then()
                .statusCode(HttpStatus.OK.value());

        verify(adminService, times(1)).deleteNode(1L);
    }

    /**
     * Tests the adding of a new conversation node.
     */
    @Test
    public void testAddNode() {
        given()
                .when()
                .get("/admin/add")
                .then()
                .statusCode(HttpStatus.OK.value());
    }

    /**
     * Tests the saving of a new conversation node.
     */
    @Test
    public void testSaveNode() {
        doNothing().when(adminService).saveNode(any(ConversationNode.class));

        given()
                .contentType(ContentType.JSON)
                .formParam("message", "Test Message")
                .formParam("messageName", "Test Name")
                .formParam("nodeType", "NORMAL_NODE")
                .when()
                .post("/admin/add")
                .then()
                .statusCode(HttpStatus.FOUND.value());

        verify(adminService, times(1)).saveNode(any(ConversationNode.class));
    }
}
