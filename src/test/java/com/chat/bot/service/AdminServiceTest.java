package com.chat.bot.service;

import com.chat.bot.constants.ChatMessages;
import com.chat.bot.entity.ConversationNode;
import com.chat.bot.entity.ConversationNode.NodeType;
import com.chat.bot.repository.ConversationNodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the AdminService class.
 */
class AdminServiceTest {

    /**
     * Mocked repository for conversation nodes.
     */
    @Mock
    private ConversationNodeRepository repository;

    /**
     * Service instance under test.
     */
    @InjectMocks
    private AdminService adminService;

    /**
     * Sample conversation node representing an invalid node.
     */
    private ConversationNode invalidNode;

    /**
     * Sets up the test environment before each test.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        invalidNode = new ConversationNode();
        invalidNode.setId(999L);
        invalidNode.setMessage(ChatMessages.INVALID_MESSAGE.getMessage());
        invalidNode.setMessageName(ChatMessages.INVALID_MESSAGE_NAME.getMessage());
        invalidNode.setDeletable(false);
        invalidNode.setNodeType(NodeType.INVALID_NODE);

        when(repository.save(any(ConversationNode.class))).thenAnswer(invocation -> {
            ConversationNode node = invocation.getArgument(0);
            if (node.getId() == null) {
                node.setId((long) (Math.random() * 1000));
            }
            return node;
        });
    }

    /**
     * Tests the retrieval of all conversation nodes.
     */
    @Test
    void testGetAllNodes() {
        ConversationNode node = new ConversationNode();
        when(repository.findAll()).thenReturn(Collections.singletonList(node));

        assertEquals(1, adminService.getAllNodes().size());
        verify(repository, times(1)).findAll();
    }

    /**
     * Tests the retrieval of a conversation node by its ID.
     */
    @Test
    void testGetNodeById() {
        ConversationNode node = new ConversationNode();
        when(repository.findById(1L)).thenReturn(Optional.of(node));

        assertEquals(node, adminService.getNodeById(1L));
        verify(repository, times(1)).findById(1L);
    }

    /**
     * Tests that an exception is thrown when a node is not found by its ID.
     */
    @Test
    void testGetNodeByIdThrowsExceptionWhenNodeNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> adminService.getNodeById(1L));
        verify(repository, times(1)).findById(1L);
    }

    /**
     * Tests the saving of a conversation node.
     */
    @Test
    void testSaveNode() {
        ConversationNode node = new ConversationNode();
        node.setNodeType(NodeType.NORMAL_NODE);

        when(repository.count()).thenReturn(1L);
        adminService.saveNode(node);

        verify(repository, times(1)).save(node);
    }

    /**
     * Tests that the first node is set correctly if no nodes exist.
     */
    @Test
    void testSaveNodeSetsFirstNodeIfNoNodesExist() {
        ConversationNode node = new ConversationNode();
        node.setNodeType(NodeType.FIRST_NODE);

        when(repository.count()).thenReturn(0L);
        adminService.saveNode(node);

        assertEquals(NodeType.FIRST_NODE, node.getNodeType());
        verify(repository, times(1)).save(node);
    }

    /**
     * Tests the deletion of all conversation nodes.
     */
    @Test
    void testDeleteAllNodes() {
        adminService.deleteAllNodes();

        verify(repository, times(1)).deleteAll();
    }

    /**
     * Tests the deletion of a conversation node by its ID.
     */
    @Test
    void testDeleteNode() {
        ConversationNode node = new ConversationNode();
        node.setId(1L);
        node.setDeletable(true);
        when(repository.findById(1L)).thenReturn(Optional.of(node));
        when(repository.existsById(1L)).thenReturn(true);

        adminService.deleteNode(1L);

        verify(repository, times(1)).deleteById(1L);
    }

    /**
     * Tests that an exception is thrown when a node is not deletable.
     */
    @Test
    void testDeleteNodeThrowsExceptionWhenNodeNotDeletable() {
        ConversationNode node = new ConversationNode();
        node.setId(1L);
        node.setDeletable(false);
        when(repository.findById(1L)).thenReturn(Optional.of(node));
        when(repository.existsById(1L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> adminService.deleteNode(1L));
        verify(repository, times(0)).deleteById(1L);
    }

    /**
     * Tests that an exception is thrown when a node does not exist.
     */
    @Test
    void testDeleteNodeThrowsExceptionWhenNodeDoesNotExist() {
        when(repository.existsById(1L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> adminService.deleteNode(1L));
        verify(repository, times(0)).deleteById(1L);
    }

    /**
     * Tests that responses are updated correctly when a node is deleted.
     */
    @Test
    void testDeleteNodeUpdatesResponsesForDeletedNode() {
        ConversationNode node1 = new ConversationNode();
        node1.setId(1L);
        node1.setDeletable(true);

        ConversationNode node2 = new ConversationNode();
        node2.setId(2L);
        Map<String, Long> responses = new HashMap<>();
        responses.put("response", 1L);
        node2.setResponses(responses);

        invalidNode.setId(999L);
        when(repository.findByNodeType(NodeType.INVALID_NODE)).thenReturn(Optional.of(invalidNode));

        when(repository.findById(1L)).thenReturn(Optional.of(node1));
        when(repository.existsById(1L)).thenReturn(true);
        when(repository.findAll()).thenReturn(Arrays.asList(node1, node2));

        adminService.deleteNode(1L);

        ArgumentCaptor<ConversationNode> nodeCaptor = ArgumentCaptor.forClass(ConversationNode.class);
        verify(repository, times(2)).save(nodeCaptor.capture());

        ConversationNode updatedNode = nodeCaptor.getAllValues().get(1);
        assertEquals(node2.getId(), updatedNode.getId());
        assertEquals(node2.getResponses().get("response"), updatedNode.getResponses().get("response"));
    }

    /**
     * Tests the saving of responses for a conversation node.
     */
    @Test
    void testSaveResponses() {
        ConversationNode node = new ConversationNode();
        node.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(node));

        adminService.saveResponses(1L, Arrays.asList("yes", "no"), Arrays.asList(2L, 3L));

        ArgumentCaptor<ConversationNode> nodeCaptor = ArgumentCaptor.forClass(ConversationNode.class);
        verify(repository, times(1)).save(nodeCaptor.capture());

        Map<String, Long> responses = nodeCaptor.getValue().getResponses();
        assertEquals(2, responses.size());
        assertEquals(2L, responses.get("yes"));
        assertEquals(3L, responses.get("no"));
    }

    /**
     * Tests the addition of responses to a conversation node.
     */
    @Test
    void testAddResponses() {
        ConversationNode node = new ConversationNode();
        node.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(node));

        adminService.addResponses(1L, Arrays.asList("maybe", "never"), Arrays.asList(4L, 5L));

        ArgumentCaptor<ConversationNode> nodeCaptor = ArgumentCaptor.forClass(ConversationNode.class);
        verify(repository, times(1)).save(nodeCaptor.capture());

        Map<String, Long> responses = nodeCaptor.getValue().getResponses();
        assertEquals(2, responses.size());
        assertEquals(4L, responses.get("maybe"));
        assertEquals(5L, responses.get("never"));
    }

    /**
     * Tests the validation of node type uniqueness.
     */
    @Test
    void testValidateNodeTypeUniqueness() {
        ConversationNode node = new ConversationNode();
        node.setId(1L);
        node.setNodeType(NodeType.FIRST_NODE);

        ConversationNode existingNode = new ConversationNode();
        existingNode.setId(2L);
        when(repository.findByNodeType(NodeType.FIRST_NODE)).thenReturn(Optional.of(existingNode));

        assertThrows(IllegalArgumentException.class, () -> adminService.saveNode(node));
    }
}
