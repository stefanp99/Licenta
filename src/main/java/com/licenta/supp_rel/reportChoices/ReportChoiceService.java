package com.licenta.supp_rel.reportChoices;

import com.licenta.supp_rel.user.User;
import com.licenta.supp_rel.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReportChoiceService {
    @Autowired
    ReportChoiceRepository reportChoiceRepository;

    @Autowired
    UserRepository userRepository;

    public List<ReportChoiceWithoutUserDTO> findReportChoiceByUser(String userId) {
        List<User> users = new ArrayList<>();
        if (userId == null || userId.equals("")) {
            users = userRepository.findAll();
        } else {
            List<String> userIdsString = List.of(userId.split(","));
            for (String userIdString : userIdsString)
                users.add(userRepository.findById(Integer.valueOf(userIdString)).orElse(null));
        }
        List<ReportChoiceWithoutUserDTO> reportChoices = new ArrayList<>();
        for (User user : users)
            if (user != null) {
                reportChoiceRepository.findByUser(user).ifPresent(reportChoice -> reportChoices.add(new ReportChoiceWithoutUserDTO(reportChoice.getId(), reportChoice.getPlantId(),
                        reportChoice.getSupplierId(), reportChoice.getMaterialCode())));
            }
        return reportChoices;
    }

    public ReportChoiceWithoutUserDTO addReportChoice(Integer userId, String plantId, String supplierId, String materialCode) {
        ReportChoice reportChoice = new ReportChoice();
        User user = userRepository.findById(userId).orElseThrow();
        if(reportChoiceRepository.findByUser(user).orElse(null) != null)
            return updateReportChoice(userId, plantId, supplierId, materialCode);
        if (plantId == null || plantId.equals(""))
            plantId = "*";
        if (supplierId == null || supplierId.equals(""))
            supplierId = "*";
        if (materialCode == null || materialCode.equals(""))
            materialCode = "*";
        reportChoice.setUser(user);
        reportChoice.setPlantId(plantId);
        reportChoice.setSupplierId(supplierId);
        reportChoice.setMaterialCode(materialCode);
        reportChoiceRepository.save(reportChoice);
        return new ReportChoiceWithoutUserDTO(reportChoice.getId(),
                reportChoice.getPlantId(), reportChoice.getSupplierId(), reportChoice.getMaterialCode());
    }

    public ReportChoiceWithoutUserDTO updateReportChoice(Integer userId, String plantId, String supplierId, String materialCode) {
        User user = userRepository.findById(userId).orElseThrow();
        ReportChoice reportChoice = reportChoiceRepository.findByUser(user).orElse(null);
        if(reportChoice == null)
            return addReportChoice(userId, plantId, supplierId, materialCode);
        if (plantId == null || plantId.equals(""))
            plantId = "*";
        if (supplierId == null || supplierId.equals(""))
            supplierId = "*";
        if (materialCode == null || materialCode.equals(""))
            materialCode = "*";

        reportChoice.setPlantId(plantId);
        reportChoice.setSupplierId(supplierId);
        reportChoice.setMaterialCode(materialCode);

        reportChoiceRepository.save(reportChoice);
        return new ReportChoiceWithoutUserDTO(reportChoice.getId(),
                reportChoice.getPlantId(), reportChoice.getSupplierId(), reportChoice.getMaterialCode());
    }

    public ReportChoiceWithoutUserDTO deleteReportChoice(Integer userId) {
        User user = userRepository.findById(userId).orElseThrow();
        ReportChoice reportChoice = reportChoiceRepository.findByUser(user).orElseThrow();
        reportChoiceRepository.delete(reportChoice);
        return new ReportChoiceWithoutUserDTO(reportChoice.getId(),
                reportChoice.getPlantId(), reportChoice.getSupplierId(), reportChoice.getMaterialCode());
    }
}
