package controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import service.MachineService;

import java.sql.SQLException;

@Controller
public class AdminMachineWebController {
    private final MachineService machines = new MachineService();

    @GetMapping("/admin/machines")
    public String list(HttpSession session, Model model) {
        if (session.getAttribute("adminId") == null) return "redirect:/admin-login";

        try {
            var equipment = machines.getAllMachineStatuses();
            model.addAttribute("washers", equipment.stream()
                    .filter(item -> "WASHER".equals(item.getMachineType())).toList());
            model.addAttribute("dryers", equipment.stream()
                    .filter(item -> "DRYER".equals(item.getMachineType())).toList());
            model.addAttribute("formToken", PaymentWebController.formToken(session));
        } catch (SQLException e) {
            model.addAttribute("error", "Unable to load washers and dryers.");
        }
        return "admin-machines";
    }

    @PostMapping("/admin/machines")
    public String update(HttpSession session, RedirectAttributes flash,
                         @RequestParam("formToken") String token,
                         @RequestParam("type") String type, @RequestParam("id") int id,
                         @RequestParam("status") String status,
                         @RequestParam(name = "reason", defaultValue = "") String reason) {
        String adminId = (String) session.getAttribute("adminId");
        if (adminId == null) return "redirect:/admin-login";
        if (!PaymentWebController.validToken(session, token)) {
            flash.addFlashAttribute("error", "Reload the machine list and try again.");
            return "redirect:/admin/machines";
        }

        if (id <= 0 || !("WASHER".equals(type) || "DRYER".equals(type))) {
            flash.addFlashAttribute("error", "Select a valid washer or dryer.");
            return "redirect:/admin/machines";
        }

        String card = type + "-" + id;
        String errorField = "reason";
        try {
            if (!("AVAILABLE".equals(status) || "OUT_OF_SERVICE".equals(status))) {
                errorField = "status";
                throw new IllegalArgumentException("Select Available or Out of Service.");
            }
            if ("OUT_OF_SERVICE".equals(status) && (reason.isBlank() || reason.trim().length() > 255)) {
                throw new IllegalArgumentException("Enter an out-of-service reason of 1 to 255 characters.");
            }

            errorField = "form";
            machines.updateMachineStatus(type, id, status, reason, adminId);
            flash.addFlashAttribute("machineSuccess", "Equipment status updated.");
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("machineError", e.getMessage());
        } catch (SQLException e) {
            flash.addFlashAttribute("machineError", "Unable to update equipment status. Try again.");
        }

        flash.addFlashAttribute("machineCard", card);
        flash.addFlashAttribute("machineErrorField", errorField);
        flash.addFlashAttribute("submittedStatus", status);
        flash.addFlashAttribute("submittedReason", reason);
        return "redirect:/admin/machines#" + card;
    }
}
