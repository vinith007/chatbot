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

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private AdminService adminService;

    @GetMapping
    public String listNodes(Model model) {
        logger.info("Listing all conversation nodes");
        model.addAttribute("nodes", adminService.getAllNodes());
        return "admin";
    }

    @GetMapping("/edit/{id}")
    public String editNode(@PathVariable("id") Long id, Model model) {
        logger.info("Editing node with ID: {}", id);
        model.addAttribute("node", adminService.getNodeById(id));
        return "editNode";
    }

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
        }
        return "redirect:/admin";
    }

    @GetMapping("/delete/{id}")
    public String deleteNode(@PathVariable("id") Long id, Model model) {
        try {
            adminService.deleteNode(id);
        } catch (IllegalArgumentException e) {
            logger.error("Error deleting node with ID: {}: {}", id, e.getMessage());
            model.addAttribute("error", e.getMessage());
        }
        return "redirect:/admin";
    }

    @GetMapping("/add")
    public String addNode(Model model) {
        logger.info("Adding a new node");
        model.addAttribute("node", new ConversationNode());
        return "addNode";
    }

    @PostMapping("/add")
    public String saveNode(@ModelAttribute ConversationNode node, Model model) {
        try {
            adminService.saveNode(node);
            logger.info("Saved new node with ID: {}", node.getId());
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "addNode";
        }
        return "redirect:/admin";
    }

    @GetMapping("/responses/{id}")
    public String editResponses(@PathVariable("id") Long id, Model model) {
        logger.info("Editing responses for node with ID: {}", id);
        ConversationNode node = adminService.getNodeById(id);
        model.addAttribute("node", node);
        model.addAttribute("allNodes", adminService.getAllNodes());
        return "editResponses";
    }

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
        }
        return "redirect:/admin";
    }

    @GetMapping("/responses/add/{id}")
    public String addResponses(@PathVariable("id") Long id, Model model) {
        logger.info("Adding responses for node with ID: {}", id);
        ConversationNode node = adminService.getNodeById(id);
        model.addAttribute("node", node);
        model.addAttribute("allNodes", adminService.getAllNodes());
        return "addResponses";
    }

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
        }
        return "redirect:/admin";
    }

    @GetMapping("/deleteAll")
    public String deleteAllNodes(Model model) {
        try {
            adminService.deleteAllNodes();
        } catch (IllegalArgumentException e) {
            logger.error("Error deleting all nodes: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
        }
        return "redirect:/admin";
    }
}
