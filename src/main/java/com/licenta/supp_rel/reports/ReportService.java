package com.licenta.supp_rel.reports;

import com.licenta.supp_rel.deviations.Deviation;
import com.licenta.supp_rel.deviations.DeviationService;
import com.licenta.supp_rel.deviations.DeviationTypes;
import com.licenta.supp_rel.email.EmailService;
import com.licenta.supp_rel.plants.Plant;
import com.licenta.supp_rel.plants.PlantRepository;
import com.licenta.supp_rel.reportChoices.ReportChoice;
import com.licenta.supp_rel.reportChoices.ReportChoiceRepository;
import com.licenta.supp_rel.suppliers.Supplier;
import com.licenta.supp_rel.suppliers.SupplierRepository;
import com.licenta.supp_rel.user.User;
import com.licenta.supp_rel.user.UserRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ReportService {
    @Autowired
    ReportChoiceRepository reportChoiceRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PlantRepository plantRepository;
    @Autowired
    SupplierRepository supplierRepository;

    @Autowired
    EmailService emailService;

    @Autowired
    DeviationService deviationService;

    public List<DailyReportDTO> createDailyReport(Integer userId, Date date) {
        if (date == null)
            date = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);//yesterday

        User user = userRepository.findById(userId).orElseThrow();
        ReportChoice reportChoice = reportChoiceRepository.findByUser(user).orElseThrow();

        List<Deviation> deviations = deviationService.findDeviationsByTypeSupplierMaterialPlantCreationDate(
                "*", reportChoice.getPlantId(), reportChoice.getSupplierId(), reportChoice.getMaterialCode(), date);

        List<DailyReportDTO> dailyReportDTOs = new ArrayList<>();
        for (Deviation deviation : deviations) {
            String plantId = deviation.getDelivery().getContract().getPlant().getId();
            String supplierId = deviation.getDelivery().getContract().getSupplier().getId();
            String materialCode = deviation.getDelivery().getContract().getMaterialCode();
            String deviationType = deviation.getType().toString();

            Plant plant = plantRepository.findById(plantId).orElseThrow();
            Supplier supplier = supplierRepository.findById(supplierId).orElseThrow();

            DailyReportDTO foundReport = findReport(dailyReportDTOs, plantId, supplierId, materialCode, deviationType);

            boolean isDayDevi = deviationType.equals(DeviationTypes.dayPlus.toString()) || deviationType.equals(DeviationTypes.dayMinus.toString());
            if (foundReport != null) {
                dailyReportDTOs.remove(foundReport);
                DailyReportDTO dailyReportDTO = new DailyReportDTO();
                dailyReportDTO.setPlant(plant);
                dailyReportDTO.setSupplier(supplier);
                dailyReportDTO.setMaterialCode(materialCode);
                dailyReportDTO.setDeviationType(deviationType);
                dailyReportDTO.setDeviationsNr(foundReport.getDeviationsNr() + 1);
                dailyReportDTO.setDate(deviation.getCreationDate());

                Float sum = foundReport.getAverageDeviation() * foundReport.getDeviationsNr();
                if (isDayDevi) {
                    sum += deviation.getTimeDiff();
                    dailyReportDTO.setAverageDeviation(sum / dailyReportDTO.getDeviationsNr());
                } else {
                    sum += deviation.getQuantityDiff();
                    dailyReportDTO.setAverageDeviation(sum / dailyReportDTO.getDeviationsNr());
                }

                dailyReportDTOs.add(dailyReportDTO);
            } else {
                DailyReportDTO dailyReportDTO = new DailyReportDTO();
                dailyReportDTO.setPlant(plant);
                dailyReportDTO.setSupplier(supplier);
                dailyReportDTO.setMaterialCode(materialCode);
                dailyReportDTO.setDeviationType(deviationType);
                dailyReportDTO.setDeviationsNr(1);
                dailyReportDTO.setDate(deviation.getCreationDate());

                if (isDayDevi)
                    dailyReportDTO.setAverageDeviation(Float.valueOf(deviation.getTimeDiff()));
                else
                    dailyReportDTO.setAverageDeviation(deviation.getQuantityDiff());
                dailyReportDTOs.add(dailyReportDTO);
            }
        }

        if (dailyReportDTOs.size() > 0) {
            StringBuilder html = new StringBuilder("""
                    <!DOCTYPE html>
                    <html>
                      <head>
                        <title>Supplier Deviations</title>
                        <style>
                          table {
                            border-collapse: collapse;
                            width: 100%;
                          }

                          th,
                          td {
                            padding: 8px;
                            text-align: left;
                            border-bottom: 1px solid #ddd;
                          }

                          th {
                            background-color: #21216c;
                            color: #fff;
                          }

                          tr:nth-child(even) {
                            background-color: #f2f2f2;
                          }

                          tr:hover {
                            background-color: #526f78;
                            color: #fff;
                          }
                        </style>
                      </head>
                      <body>
                        <table>
                          <thead>
                            <tr>
                              <th>Supplier ID</th>
                              <th>Supplier Name</th>
                              <th>Supplier Country</th>
                              <th>Supplier City</th>
                              <th>Material Code</th>
                              <th>Plant ID</th>
                              <th>Plant Segment</th>
                              <th>Plant Country</th>
                              <th>Plant City</th>
                              <th>Deviation Type</th>
                              <th>Number of Deviations</th>
                              <th>Average Deviation</th>
                              <th>Delivery Date</th>
                            </tr>
                          </thead>
                          <tbody>""");

            for (DailyReportDTO dailyReportDTO : dailyReportDTOs) {
                html.append("<tr>");
                html.append("<td>").append(dailyReportDTO.getSupplier().getId()).append("</td>");
                html.append("<td>").append(dailyReportDTO.getSupplier().getName()).append("</td>");
                html.append("<td>").append(dailyReportDTO.getSupplier().getCountry()).append("</td>");
                html.append("<td>").append(dailyReportDTO.getSupplier().getCity()).append("</td>");
                html.append("<td>").append(dailyReportDTO.getMaterialCode()).append("</td>");
                html.append("<td>").append(dailyReportDTO.getPlant().getId()).append("</td>");
                html.append("<td>").append(dailyReportDTO.getPlant().getSegment()).append("</td>");
                html.append("<td>").append(dailyReportDTO.getPlant().getCountry()).append("</td>");
                html.append("<td>").append(dailyReportDTO.getPlant().getCity()).append("</td>");
                html.append("<td>").append(dailyReportDTO.getDeviationType()).append("</td>");
                html.append("<td>").append(dailyReportDTO.getDeviationsNr()).append("</td>");
                html.append("<td>").append(dailyReportDTO.getAverageDeviation()).append("</td>");
                html.append("<td>").append(dailyReportDTO.getDate()).append("</td>");
                html.append("</tr>");
            }

            html.append("""
                          </tbody>
                        </table>
                      </body>
                    </html>""");

            try {
                emailService.sendMail(emailService.constructEmail("Daily Notifications","",
                        html.toString(), user));
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
        }
        return dailyReportDTOs;
    }

    private DailyReportDTO findReport(List<DailyReportDTO> dailyReportDTOs, String plantId, String supplierId,
                                      String materialCode, String deviationType) {
        Plant plant = plantRepository.findById(plantId).orElseThrow();
        Supplier supplier = supplierRepository.findById(supplierId).orElseThrow();
        for (DailyReportDTO dailyReportDTO : dailyReportDTOs)
            if (dailyReportDTO.getPlant().equals(plant) && dailyReportDTO.getSupplier().equals(supplier) &&
                    dailyReportDTO.getMaterialCode().equals(materialCode) && dailyReportDTO.getDeviationType().equals(deviationType))
                return dailyReportDTO;
        return null;
    }
}
