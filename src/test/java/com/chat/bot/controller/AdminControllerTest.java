package com.chat.bot.controller;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.chat.bot.entity.ConversationNode;
import com.chat.bot.service.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for the AdminController class.
 */
class AdminControllerTest {

    /**
     * Admin service.
     */
    @Mock
    private AdminService adminService;

    /**
     * Model
     */
    @Mock
    private Model model;

    /**
     * Admin controller
     */
    @InjectMocks
    private AdminController adminController;

    /**
     * Setup
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Tests listing all conversation nodes.
     */
    @Test
    void testListNodes() {
        List<ConversationNode> nodes = new ArrayList<>();
        when(adminService.getAllNodes()).thenReturn(nodes);

        String viewName = adminController.listNodes(model);

        assertEquals("admin", viewName);
        verify(adminService).getAllNodes();
        verify(model).addAttribute("nodes", nodes);
        verifyNoMoreInteractions(adminService, model);
    }

    /**
     * Tests editing a conversation node.
     */
    @Test
    void testEditNode() {
        Long nodeId = 1L;
        ConversationNode node = new ConversationNode();
        when(adminService.getNodeById(nodeId)).thenReturn(node);

        String viewName = adminController.editNode(nodeId, model);

        assertEquals("editNode", viewName);
        verify(adminService).getNodeById(nodeId);
        verify(model).addAttribute("node", node);
        verifyNoMoreInteractions(adminService, model);
    }

    /**
     * Tests updating a conversation node successfully.
     */
    @Test
    void testUpdateNode_Success() {
        Long nodeId = 1L;
        ConversationNode node = new ConversationNode();
        node.setId(nodeId);

        String viewName = adminController.updateNode(nodeId, node, model);

        assertEquals("redirect:/admin", viewName);
        verify(adminService).saveNode(node);
        verifyNoMoreInteractions(adminService, model);
    }

    /**
     * Tests updating a conversation node with an IllegalArgumentException.
     */
    @Test
    void testUpdateNode_IllegalArgumentException() {
        Long nodeId = 1L;
        ConversationNode node = new ConversationNode();
        node.setId(nodeId);
        String errorMessage = "Invalid node";

        doThrow(new IllegalArgumentException(errorMessage)).when(adminService).saveNode(node);

        String viewName = adminController.updateNode(nodeId, node, model);

        assertEquals("editNode", viewName);
        verify(adminService).saveNode(node);
        verify(model).addAttribute("error", errorMessage);
        verify(model).addAttribute("node", node);
        verifyNoMoreInteractions(adminService, model);
    }

    /**
     * Tests deleting a conversation node successfully.
     */
    @Test
    void testDeleteNode_Success() {
        Long nodeId = 1L;

        String viewName = adminController.deleteNode(nodeId, model);

        assertEquals("redirect:/admin", viewName);
        verify(adminService).deleteNode(nodeId);
        verifyNoMoreInteractions(adminService, model);
    }

    /**
     * Tests deleting a conversation node with an IllegalArgumentException.
     */
    @Test
    void testDeleteNode_IllegalArgumentException() {
        Long nodeId = 1L;
        String errorMessage = "Invalid node";

        doThrow(new IllegalArgumentException(errorMessage)).when(adminService).deleteNode(nodeId);

        String viewName = adminController.deleteNode(nodeId, model);

        assertEquals("redirect:/error", viewName);
        verify(adminService).deleteNode(nodeId);
        verify(model).addAttribute("error", errorMessage);
        verifyNoMoreInteractions(adminService, model);
    }

    /**
     * Tests displaying the form to add a new conversation node.
     */
    @Test
    void testAddNode() {
        String viewName = adminController.addNode(model);

        assertEquals("addNode", viewName);
        verify(model).addAttribute(eq("node"), any(ConversationNode.class));
        verifyNoMoreInteractions(adminService, model);
    }

    /**
     * Tests saving a new conversation node successfully.
     */
    @Test
    void testSaveNode_Success() {
        ConversationNode node = new ConversationNode();

        String viewName = adminController.saveNode(node, model);

        assertEquals("redirect:/admin", viewName);
        verify(adminService).saveNode(node);
        verifyNoMoreInteractions(adminService, model);
    }

    /**
     * Tests saving a new conversation node with an IllegalArgumentException.
     */
    @Test
    void testSaveNode_IllegalArgumentException() {
        ConversationNode node = new ConversationNode();
        String errorMessage = "Invalid node";

        doThrow(new IllegalArgumentException(errorMessage)).when(adminService).saveNode(node);

        String viewName = adminController.saveNode(node, model);

        assertEquals("addNode", viewName);
        verify(adminService).saveNode(node);
        verify(model).addAttribute("error", errorMessage);
        verify(model).addAttribute("node", node);
        verifyNoMoreInteractions(adminService, model);
    }

    /**
     * Tests editing responses for a conversation node.
     */
    @Test
    void testEditResponses() {
        Long nodeId = 1L;
        ConversationNode node = new ConversationNode();
        List<ConversationNode> allNodes = new ArrayList<>();
        when(adminService.getNodeById(nodeId)).thenReturn(node);
        when(adminService.getAllNodes()).thenReturn(allNodes);

        String viewName = adminController.editResponses(nodeId, model);

        assertEquals("editResponses", viewName);
        verify(adminService).getNodeById(nodeId);
        verify(adminService).getAllNodes();
        verify(model).addAttribute("node", node);
        verify(model).addAttribute("allNodes", allNodes);
        verifyNoMoreInteractions(adminService, model);
    }

    /**
     * Tests saving responses for a conversation node successfully.
     */
    @Test
    void testSaveResponses_Success() {
        Long nodeId = 1L;
        List<String> responseKeys = List.of("key1", "key2");
        List<Long> nextNodeIds = List.of(2L, 3L);

        String viewName = adminController.saveResponses(nodeId, responseKeys, nextNodeIds, model);

        assertEquals("redirect:/admin", viewName);
        verify(adminService).saveResponses(nodeId, responseKeys, nextNodeIds);
        verifyNoMoreInteractions(adminService, model);
    }

    /**
     * Tests saving responses for a conversation node with an IllegalArgumentException.
     */
    @Test
    void testSaveResponses_IllegalArgumentException() {
        Long nodeId = 1L;
        List<String> responseKeys = List.of("key1", "key2");
        List<Long> nextNodeIds = List.of(2L, 3L);
        String errorMessage = "Invalid response";

        doThrow(new IllegalArgumentException(errorMessage)).when(adminService).saveResponses(nodeId, responseKeys, nextNodeIds);

        String viewName = adminController.saveResponses(nodeId, responseKeys, nextNodeIds, model);

        assertEquals("editResponses", viewName);
        verify(adminService).saveResponses(nodeId, responseKeys, nextNodeIds);
        verify(model).addAttribute("error", errorMessage);
        verifyNoMoreInteractions(adminService, model);
    }

    /**
     * Tests displaying the form to add responses for a conversation node.
     */
    @Test
    void testAddResponses() {
        Long nodeId = 1L;
        ConversationNode node = new ConversationNode();
        List<ConversationNode> allNodes = new ArrayList<>();
        when(adminService.getNodeById(nodeId)).thenReturn(node);
        when(adminService.getAllNodes()).thenReturn(allNodes);

        String viewName = adminController.addResponses(nodeId, model);

        assertEquals("addResponses", viewName);
        verify(adminService).getNodeById(nodeId);
        verify(adminService).getAllNodes();
        verify(model).addAttribute("node", node);
        verify(model).addAttribute("allNodes", allNodes);
        verifyNoMoreInteractions(adminService, model);
    }

    /**
     * Tests saving new responses for a conversation node successfully.
     */
    @Test
    void testSaveNewResponses_Success() {
        Long nodeId = 1L;
        List<String> responseKeys = List.of("key1", "key2");
        List<Long> nextNodeIds = List.of(2L, 3L);

        String viewName = adminController.saveNewResponses(nodeId, responseKeys, nextNodeIds, model);

        assertEquals("redirect:/admin", viewName);
        verify(adminService).addResponses(nodeId, responseKeys, nextNodeIds);
        verifyNoMoreInteractions(adminService, model);
    }

    /**
     * Tests saving new responses for a conversation node with an IllegalArgumentException.
     */
    @Test
    void testSaveNewResponses_IllegalArgumentException() {
        Long nodeId = 1L;
        List<String> responseKeys = List.of("key1", "key2");
        List<Long> nextNodeIds = List.of(2L, 3L);
        String errorMessage = "Invalid response";

        doThrow(new IllegalArgumentException(errorMessage)).when(adminService).addResponses(nodeId, responseKeys, nextNodeIds);

        String viewName = adminController.saveNewResponses(nodeId, responseKeys, nextNodeIds, model);

        assertEquals("addResponses", viewName);
        verify(adminService).addResponses(nodeId, responseKeys, nextNodeIds);
        verify(model).addAttribute("error", errorMessage);
        verifyNoMoreInteractions(adminService, model);
    }

    /**
     * Tests deleting all conversation nodes successfully.
     */
    @Test
    void testDeleteAllNodes_Success() {
        String viewName = adminController.deleteAllNodes(model);

        assertEquals("redirect:/admin", viewName);
        verify(adminService).deleteAllNodes();
        verifyNoMoreInteractions(adminService, model);
    }

    /**
     * Tests deleting all conversation nodes with an IllegalArgumentException.
     */
    @Test
    void testDeleteAllNodes_IllegalArgumentException() {
        String errorMessage = "Invalid nodes";

        doThrow(new IllegalArgumentException(errorMessage)).when(adminService).deleteAllNodes();

        String viewName = adminController.deleteAllNodes(model);

        assertEquals("redirect:/error", viewName);
        verify(adminService).deleteAllNodes();
        verify(model).addAttribute("error", errorMessage);
        verifyNoMoreInteractions(adminService, model);
    }
}
