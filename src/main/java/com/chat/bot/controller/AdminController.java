package com.chat.bot.controller;

import com.chat.bot.entity.ConversationNode;
import com.chat.bot.service.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller class for managing the admin operations related to conversation nodes.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    /**
     * Admin Service
     */
    @Autowired
    private AdminService adminService;

    /**
     * Lists all conversation nodes.
     *
     * @param model the model to hold the list of nodes
     * @return the name of the admin view
     */
    @GetMapping
    public String listNodes(Model model) {
        logger.info("Listing all conversation nodes");
        model.addAttribute("nodes", adminService.getAllNodes());
        return "admin";
    }

    /**
     * Displays the form to edit a conversation node.
     *
     * @param id the ID of the node to edit
     * @param model the model to hold the node details
     * @return the name of the edit node view
     */
    @GetMapping("/edit/{id}")
    public String editNode(@PathVariable("id") Long id, Model model) {
        logger.info("Editing node with ID: {}", id);
        model.addAttribute("node", adminService.getNodeById(id));
        return "editNode";
    }

    /**
     * Updates a conversation node.
     *
     * @param id the ID of the node to update
     * @param node the updated node details
     * @param model the model to hold error messages if any
     * @return the redirect URL to the admin view
     */
    @PostMapping("/update/{id}")
    public String updateNode(@PathVariable("id") Long id, @ModelAttribute ConversationNode node, Model model) {
        try {
            node.setId(id);
            adminService.saveNode(node);
            logger.info("Updated node with ID: {}", id);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("node", node);
            return "editNode";
        } catch (Exception e) {
            logger.error("Error updating node with ID: {}", id, e);
            return "redirect:/error";
        }
        return "redirect:/admin";
    }

    /**
     * Deletes a conversation node.
     *
     * @param id the ID of the node to delete
     * @param model the model to hold error messages if any
     * @return the redirect URL to the admin view
     */
    @GetMapping("/delete/{id}")
    public String deleteNode(@PathVariable("id") Long id, Model model) {
        try {
            adminService.deleteNode(id);
        } catch (IllegalArgumentException e) {
            logger.error("Error deleting node with ID: {}: {}", id, e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "redirect:/error";
        } catch (Exception e) {
            logger.error("Error deleting node with ID: {}", id, e);
            return "redirect:/error";
        }
        return "redirect:/admin";
    }

    /**
     * Displays the form to add a new conversation node.
     *
     * @param model the model to hold the new node details
     * @return the name of the add node view
     */
    @GetMapping("/add")
    public String addNode(Model model) {
        logger.info("Adding a new node");
        model.addAttribute("node", new ConversationNode());
        return "addNode";
    }


    /**
     * Saves a new conversation node.
     *
     * @param node the new node details
     * @param model the model to hold error messages if any
     * @return the redirect URL to the admin view
     */
    @PostMapping("/add")
    public String saveNode(@ModelAttribute ConversationNode node, Model model) {
        try {
            adminService.saveNode(node);
            logger.info("Saved new node with ID: {}", node.getId());
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("node", node);  // Make sure to add the node back to the model in case of an error
            return "addNode";
        }
        return "redirect:/admin";
    }

    /**
     * Displays the form to edit responses for a conversation node.
     *
     * @param id the ID of the node to edit responses for
     * @param model the model to hold the node and all nodes details
     * @return the name of the edit responses view
     */
    @GetMapping("/responses/{id}")
    public String editResponses(@PathVariable("id") Long id, Model model) {
        logger.info("Editing responses for node with ID: {}", id);
        ConversationNode node = adminService.getNodeById(id);
        model.addAttribute("node", node);
        model.addAttribute("allNodes", adminService.getAllNodes());
        return "editResponses";
    }

    /**
     * Saves responses for a conversation node.
     *
     * @param id the ID of the node to save responses for
     * @param responseKeys the list of response keys
     * @param nextNodeIds the list of next node IDs
     * @param model the model to hold error messages if any
     * @return the redirect URL to the admin view
     */
    @PostMapping("/responses/{id}")
    public String saveResponses(@PathVariable("id") Long id,
                                @RequestParam(value = "responseKeys", required = false) List<String> responseKeys,
                                @RequestParam(value = "nextNodeIds", required = false) List<Long> nextNodeIds,
                                Model model) {
        try {
            adminService.saveResponses(id, responseKeys, nextNodeIds);
            logger.info("Saved responses for node with ID: {}", id);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "editResponses";
        } catch (Exception e) {
            logger.error("Error saving responses for node with ID: {}", id, e);
            return "redirect:/error";
        }
        return "redirect:/admin";
    }

    /**
     * Displays the form to add responses for a conversation node.
     *
     * @param id the ID of the node to add responses for
     * @param model the model to hold the node and all nodes details
     * @return the name of the add responses view
     */
    @GetMapping("/responses/add/{id}")
    public String addResponses(@PathVariable("id") Long id, Model model) {
        logger.info("Adding responses for node with ID: {}", id);
        ConversationNode node = adminService.getNodeById(id);
        model.addAttribute("node", node);
        model.addAttribute("allNodes", adminService.getAllNodes());
        return "addResponses";
    }

    /**
     * Saves new responses for a conversation node.
     *
     * @param id the ID of the node to add responses for
     * @param responseKeys the list of response keys
     * @param nextNodeIds the list of next node IDs
     * @param model the model to hold error messages if any
     * @return the redirect URL to the admin view
     */
    @PostMapping("/responses/add/{id}")
    public String saveNewResponses(@PathVariable("id") Long id,
                                   @RequestParam(value = "responseKeys", required = false) List<String> responseKeys,
                                   @RequestParam(value = "nextNodeIds", required = false) List<Long> nextNodeIds,
                                   Model model) {
        try {
            adminService.addResponses(id, responseKeys, nextNodeIds);
            logger.info("Added new responses for node with ID: {}", id);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "addResponses";
        } catch (Exception e) {
            logger.error("Error adding new responses for node with ID: {}", id, e);
            return "redirect:/error";
        }
        return "redirect:/admin";
    }

    /**
     * Deletes all conversation nodes.
     *
     * @param model the model to hold error messages if any
     * @return the redirect URL to the admin view
     */
    @GetMapping("/deleteAll")
    public String deleteAllNodes(Model model) {
        try {
            adminService.deleteAllNodes();
        } catch (IllegalArgumentException e) {
            logger.error("Error deleting all nodes: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "redirect:/error";
        } catch (Exception e) {
            logger.error("Error deleting all nodes", e);
            return "redirect:/error";
        }
        return "redirect:/admin";
    }
}
