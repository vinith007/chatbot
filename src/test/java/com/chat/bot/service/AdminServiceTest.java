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

class AdminServiceTest {

    @Mock
    private ConversationNodeRepository repository;

    @InjectMocks
    private AdminService adminService;

    private ConversationNode invalidNode;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        invalidNode = new ConversationNode();
        invalidNode.setId(999L);  // Set the invalid node ID explicitly
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

    @Test
    void testGetAllNodes() {
        ConversationNode node = new ConversationNode();
        when(repository.findAll()).thenReturn(Collections.singletonList(node));

        assertEquals(1, adminService.getAllNodes().size());
        verify(repository, times(1)).findAll();
    }

    @Test
    void testGetNodeById() {
        ConversationNode node = new ConversationNode();
        when(repository.findById(1L)).thenReturn(Optional.of(node));

        assertEquals(node, adminService.getNodeById(1L));
        verify(repository, times(1)).findById(1L);
    }

    @Test
    void testGetNodeByIdThrowsExceptionWhenNodeNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> adminService.getNodeById(1L));
        verify(repository, times(1)).findById(1L);
    }

    @Test
    void testSaveNode() {
        ConversationNode node = new ConversationNode();
        node.setNodeType(NodeType.NORMAL_NODE);

        when(repository.count()).thenReturn(1L);
        adminService.saveNode(node);

        verify(repository, times(1)).save(node);
    }

    @Test
    void testSaveNodeSetsFirstNodeIfNoNodesExist() {
        ConversationNode node = new ConversationNode();
        node.setNodeType(NodeType.FIRST_NODE);

        when(repository.count()).thenReturn(0L);
        adminService.saveNode(node);

        assertEquals(NodeType.FIRST_NODE, node.getNodeType());
        verify(repository, times(1)).save(node);
    }

    @Test
    void testDeleteAllNodes() {
        adminService.deleteAllNodes();

        verify(repository, times(1)).deleteAll();
    }

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

    @Test
    void testDeleteNodeThrowsExceptionWhenNodeDoesNotExist() {
        when(repository.existsById(1L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> adminService.deleteNode(1L));
        verify(repository, times(0)).deleteById(1L);
    }

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
        assertEquals(node2.getResponses().get("response"), updatedNode.getResponses().get("response")); // Ensure it matches the invalidNode ID
    }

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
