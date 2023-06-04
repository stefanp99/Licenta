package com.licenta.supp_rel.statistics;

import com.licenta.supp_rel.charts.GroupedVerticalBarChartDTO;
import com.licenta.supp_rel.charts.VerticalBarChartDTO;
import com.licenta.supp_rel.deliveries.DeliveryService;
import com.licenta.supp_rel.deliveries.DeliverySummaryDTO;
import com.licenta.supp_rel.plants.PlantService;
import com.licenta.supp_rel.suppliers.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.time.temporal.ChronoUnit.DAYS;

@Service
public class StatisticsService {
    @Autowired
    SupplierService supplierService;
    @Autowired
    DeliveryService deliveryService;
    @Autowired
    PlantService plantService;

    @SuppressWarnings("unchecked")
    public <T> T findStatisticsBySupplierMaterialPlant(String supplierId, String materialCode, String plantId,
                                                       String ratingType, String chart, Date startDate, Date endDate) {
        List<DeliverySummaryDTO> deliverySummaryDTOs;
        List<String> supplierIds;
        List<String> materialCodes;
        List<String> plantIds;
        if (supplierId.equals("*"))
            supplierIds = supplierService.findAllSupplierIds();
        else
            supplierIds = Arrays.asList(supplierId.split(","));
        materialCodes = Arrays.asList(materialCode.split(","));
        plantIds = Arrays.asList(plantId.split(","));
        switch (ratingType) {
            case "global" -> deliverySummaryDTOs = findGlobalDeliverySummaries(supplierIds);
            case "material" -> deliverySummaryDTOs = findMaterialDeliverySummaries(supplierIds, materialCodes);
            case "plant" -> deliverySummaryDTOs = findPlantDeliverySummaries(supplierIds, plantIds);
            case "specific" ->
                    deliverySummaryDTOs = findSpecificDeliverySummaries(supplierIds, materialCodes, plantIds);
            default -> deliverySummaryDTOs = null;
        }
        if (deliverySummaryDTOs == null)
            return null;

        List<String> allDeliveryDays = new ArrayList<>();

        Map<String, GroupedVerticalBarChartDTO> groupedVerticalBarChartDTOMap = new HashMap<>();

        switch (chart) {
            case "quantityBySupplierMaterialPlant" -> {
                try {
                    deliverySummaryDTOs = checkDeliverySummaryDTOsUsingRange(deliverySummaryDTOs, startDate, endDate);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                for (DeliverySummaryDTO deliverySummaryDTO : deliverySummaryDTOs) {
                    String suppId = deliverySummaryDTO.getSupplierId();
                    String deliveryDay = deliverySummaryDTO.getDeliveryDay();
                    Long totalQuantity = deliverySummaryDTO.getTotalQuantity();

                    VerticalBarChartDTO verticalBarChartDTO = new VerticalBarChartDTO();
                    verticalBarChartDTO.setName(deliveryDay);
                    verticalBarChartDTO.setValue(Float.valueOf(totalQuantity));

                    if (!allDeliveryDays.contains(deliveryDay))
                        allDeliveryDays.add(deliveryDay);

                    if (groupedVerticalBarChartDTOMap.containsKey(suppId)) {
                        GroupedVerticalBarChartDTO groupedVerticalBarChartDTO = groupedVerticalBarChartDTOMap.get(suppId);
                        groupedVerticalBarChartDTO.getSeries().add(verticalBarChartDTO);
                    } else {
                        GroupedVerticalBarChartDTO groupedVerticalBarChartDTO = new GroupedVerticalBarChartDTO();
                        groupedVerticalBarChartDTO.setName(suppId);
                        List<VerticalBarChartDTO> series = new ArrayList<>();
                        series.add(verticalBarChartDTO);
                        groupedVerticalBarChartDTO.setSeries(series);
                        groupedVerticalBarChartDTOMap.put(suppId, groupedVerticalBarChartDTO);
                    }
                }
                List<GroupedVerticalBarChartDTO> lineCharts = new ArrayList<>(groupedVerticalBarChartDTOMap.values());

                allDeliveryDays = completeListOfDates(allDeliveryDays);
                for (GroupedVerticalBarChartDTO groupedVerticalBarChartDTO : lineCharts) {
                    for (String deliveryDay : allDeliveryDays)
                        if (groupedVerticalBarChartDTO.getSeries().stream().map(VerticalBarChartDTO::getName).noneMatch(deliveryDay::equals))
                            groupedVerticalBarChartDTO.getSeries().add(new VerticalBarChartDTO(deliveryDay, 0F));
                    List<VerticalBarChartDTO> series = groupedVerticalBarChartDTO.getSeries();
                    series.sort(Comparator.comparing(VerticalBarChartDTO::getName));
                    groupedVerticalBarChartDTO.setSeries(series);
                }
                lineCharts.sort(Comparator.comparing(GroupedVerticalBarChartDTO::getName));
                return (T) lineCharts;
            }
            case "totalQuantityBySupplierMaterialPlant" -> {
                try {
                    deliverySummaryDTOs = checkDeliverySummaryDTOsUsingRange(deliverySummaryDTOs, startDate, endDate);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                deliverySummaryDTOs.sort(Comparator.comparing(DeliverySummaryDTO::getSupplierId).thenComparing(DeliverySummaryDTO::getDeliveryDay));
                for (DeliverySummaryDTO deliverySummaryDTO : deliverySummaryDTOs) {
                    String suppId = deliverySummaryDTO.getSupplierId();
                    String deliveryDay = deliverySummaryDTO.getDeliveryDay();
                    Long totalQuantity = deliverySummaryDTO.getTotalQuantity();

                    VerticalBarChartDTO verticalBarChartDTO = new VerticalBarChartDTO();
                    verticalBarChartDTO.setName(deliveryDay);

                    if (!allDeliveryDays.contains(deliveryDay))
                        allDeliveryDays.add(deliveryDay);

                    if (groupedVerticalBarChartDTOMap.containsKey(suppId)) {
                        GroupedVerticalBarChartDTO groupedVerticalBarChartDTO = groupedVerticalBarChartDTOMap.get(suppId);
                        List<VerticalBarChartDTO> series = groupedVerticalBarChartDTO.getSeries();
                        Float cumulativeTotalQuantity = series.get(series.size() - 1).getValue();

                        cumulativeTotalQuantity += totalQuantity;

                        verticalBarChartDTO.setValue(cumulativeTotalQuantity);
                        series.add(verticalBarChartDTO);
                    } else {
                        GroupedVerticalBarChartDTO groupedVerticalBarChartDTO = new GroupedVerticalBarChartDTO();
                        groupedVerticalBarChartDTO.setName(suppId);
                        List<VerticalBarChartDTO> series = new ArrayList<>();
                        verticalBarChartDTO.setValue(Float.valueOf(totalQuantity));
                        series.add(verticalBarChartDTO);
                        groupedVerticalBarChartDTO.setSeries(series);
                        groupedVerticalBarChartDTOMap.put(suppId, groupedVerticalBarChartDTO);
                    }
                }

                List<GroupedVerticalBarChartDTO> lineCharts = new ArrayList<>(groupedVerticalBarChartDTOMap.values());
                allDeliveryDays = completeListOfDates(allDeliveryDays);
                for (GroupedVerticalBarChartDTO groupedVerticalBarChartDTO : lineCharts) {
                    for (String deliveryDay : allDeliveryDays)
                        if (groupedVerticalBarChartDTO.getSeries().stream().map(VerticalBarChartDTO::getName).noneMatch(deliveryDay::equals))
                            groupedVerticalBarChartDTO.getSeries().add(new VerticalBarChartDTO(deliveryDay, 0F));
                    List<VerticalBarChartDTO> series = groupedVerticalBarChartDTO.getSeries();
                    series.sort(Comparator.comparing(VerticalBarChartDTO::getName));
                    for (int i = 0; i < series.size(); i++)
                        if (series.get(i).getValue().equals(0F) && i > 0)
                            series.get(i).setValue(series.get(i - 1).getValue());
                    groupedVerticalBarChartDTO.setSeries(series);
                }
                lineCharts.sort(Comparator.comparing(GroupedVerticalBarChartDTO::getName));
                return (T) lineCharts;

            }
            case "totalQuantityNoDays" -> {
                deliverySummaryDTOs.sort(Comparator.comparing(DeliverySummaryDTO::getSupplierId));
                List<VerticalBarChartDTO> verticalBarChartDTOs = new ArrayList<>();
                Map<String, Long> pieChartMap = new HashMap<>();
                for (DeliverySummaryDTO deliverySummaryDTO : deliverySummaryDTOs) {
                    if (pieChartMap.containsKey(deliverySummaryDTO.getSupplierId()))
                        pieChartMap.put(deliverySummaryDTO.getSupplierId(),
                                pieChartMap.get(deliverySummaryDTO.getSupplierId()) + deliverySummaryDTO.getTotalQuantity());
                    else
                        pieChartMap.put(deliverySummaryDTO.getSupplierId(), deliverySummaryDTO.getTotalQuantity());
                }
                for (Map.Entry<String, Long> pie : pieChartMap.entrySet())
                    verticalBarChartDTOs.add(new VerticalBarChartDTO(pie.getKey(), Float.valueOf(pie.getValue())));
                verticalBarChartDTOs.sort(Comparator.comparing(VerticalBarChartDTO::getValue).reversed());
                return (T) verticalBarChartDTOs;
            }
        }
        return null;
    }

    private List<DeliverySummaryDTO> checkDeliverySummaryDTOsUsingRange(List<DeliverySummaryDTO> deliverySummaryDTOs, Date startDate, Date endDate) throws ParseException {
        List<DeliverySummaryDTO> returnedList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (DeliverySummaryDTO deliverySummaryDTO : deliverySummaryDTOs) {
            Date deliveryDate = sdf.parse(deliverySummaryDTO.getDeliveryDay());
            if (startDate != null) {
                if (endDate != null) {
                    if (!startDate.after(deliveryDate) && !endDate.before(deliveryDate))
                        returnedList.add(deliverySummaryDTO);
                } else {
                    if (!startDate.after(deliveryDate))
                        returnedList.add(deliverySummaryDTO);
                }
            } else {
                if (endDate != null) {
                    if (!endDate.before(deliveryDate))
                        returnedList.add(deliverySummaryDTO);
                } else
                    returnedList.add(deliverySummaryDTO);
            }
        }
        return returnedList;
    }

    private List<DeliverySummaryDTO> findSpecificDeliverySummaries(List<String> supplierIds, List<String> materialCodes, List<String> plantIds) {
        return deliveryService.findDeliveriesBySupplierMaterialPlant(supplierIds, materialCodes, plantIds);
    }

    private List<DeliverySummaryDTO> findPlantDeliverySummaries(List<String> supplierIds, List<String> plantIds) {
        List<String> materialCodes = deliveryService.findAllMaterials();
        List<DeliverySummaryDTO> deliverySummaryDTOs = deliveryService.findDeliveriesBySupplierMaterialPlant(supplierIds, materialCodes, plantIds);
        for (int i = 0; i < deliverySummaryDTOs.size(); i++)
            deliverySummaryDTOs.get(i).setMaterialCode("all");
        for (int i = 0; i < deliverySummaryDTOs.size() - 1; i++)
            for (int j = i + 1; j < deliverySummaryDTOs.size(); j++)
                if (deliverySummaryDTOs.get(i).getSupplierId().equals(deliverySummaryDTOs.get(j).getSupplierId()) &&
                        deliverySummaryDTOs.get(i).getMaterialCode().equals(deliverySummaryDTOs.get(j).getMaterialCode()) &&
                        deliverySummaryDTOs.get(i).getPlantId().equals(deliverySummaryDTOs.get(j).getPlantId()) &&
                        deliverySummaryDTOs.get(i).getDeliveryDay().equals(deliverySummaryDTOs.get(j).getDeliveryDay())) {
                    deliverySummaryDTOs.get(i).setNrDeliveries(deliverySummaryDTOs.get(i).getNrDeliveries() +
                            deliverySummaryDTOs.get(j).getNrDeliveries());
                    deliverySummaryDTOs.get(i).setTotalQuantity(deliverySummaryDTOs.get(i).getTotalQuantity() +
                            deliverySummaryDTOs.get(j).getTotalQuantity());
                    deliverySummaryDTOs.remove(j);
                    j--;
                }
        return deliverySummaryDTOs;
    }

    private List<DeliverySummaryDTO> findMaterialDeliverySummaries(List<String> supplierIds, List<String> materialCodes) {
        List<String> plantIds = plantService.findAllPlantIds();
        List<DeliverySummaryDTO> deliverySummaryDTOs = deliveryService.findDeliveriesBySupplierMaterialPlant(supplierIds, materialCodes, plantIds);
        for (int i = 0; i < deliverySummaryDTOs.size(); i++)
            deliverySummaryDTOs.get(i).setPlantId("all");
        for (int i = 0; i < deliverySummaryDTOs.size() - 1; i++)
            for (int j = i + 1; j < deliverySummaryDTOs.size(); j++)
                if (deliverySummaryDTOs.get(i).getSupplierId().equals(deliverySummaryDTOs.get(j).getSupplierId()) &&
                        deliverySummaryDTOs.get(i).getMaterialCode().equals(deliverySummaryDTOs.get(j).getMaterialCode()) &&
                        deliverySummaryDTOs.get(i).getPlantId().equals(deliverySummaryDTOs.get(j).getPlantId()) &&
                        deliverySummaryDTOs.get(i).getDeliveryDay().equals(deliverySummaryDTOs.get(j).getDeliveryDay())) {
                    deliverySummaryDTOs.get(i).setNrDeliveries(deliverySummaryDTOs.get(i).getNrDeliveries() +
                            deliverySummaryDTOs.get(j).getNrDeliveries());
                    deliverySummaryDTOs.get(i).setTotalQuantity(deliverySummaryDTOs.get(i).getTotalQuantity() +
                            deliverySummaryDTOs.get(j).getTotalQuantity());
                    deliverySummaryDTOs.remove(j);
                    j--;
                }
        return deliverySummaryDTOs;
    }

    private List<DeliverySummaryDTO> findGlobalDeliverySummaries(List<String> supplierIds) {
        List<String> materialCodes = deliveryService.findAllMaterials();
        List<String> plantIds = plantService.findAllPlantIds();
        List<DeliverySummaryDTO> deliverySummaryDTOs = deliveryService.findDeliveriesBySupplierMaterialPlant(supplierIds, materialCodes, plantIds);
        for (int i = 0; i < deliverySummaryDTOs.size(); i++) {
            deliverySummaryDTOs.get(i).setMaterialCode("all");
            deliverySummaryDTOs.get(i).setPlantId("all");
        }
        for (int i = 0; i < deliverySummaryDTOs.size() - 1; i++)
            for (int j = i + 1; j < deliverySummaryDTOs.size(); j++)
                if (deliverySummaryDTOs.get(i).getSupplierId().equals(deliverySummaryDTOs.get(j).getSupplierId()) &&
                        deliverySummaryDTOs.get(i).getMaterialCode().equals(deliverySummaryDTOs.get(j).getMaterialCode()) &&
                        deliverySummaryDTOs.get(i).getPlantId().equals(deliverySummaryDTOs.get(j).getPlantId()) &&
                        deliverySummaryDTOs.get(i).getDeliveryDay().equals(deliverySummaryDTOs.get(j).getDeliveryDay())) {
                    deliverySummaryDTOs.get(i).setNrDeliveries(deliverySummaryDTOs.get(i).getNrDeliveries() +
                            deliverySummaryDTOs.get(j).getNrDeliveries());
                    deliverySummaryDTOs.get(i).setTotalQuantity(deliverySummaryDTOs.get(i).getTotalQuantity() +
                            deliverySummaryDTOs.get(j).getTotalQuantity());
                    deliverySummaryDTOs.remove(j);
                    j--;
                }
        return deliverySummaryDTOs;
    }

    private List<String> completeListOfDates(List<String> allDeliveryDays) {
        Collections.sort(allDeliveryDays);
        LocalDate startDate = LocalDate.parse(allDeliveryDays.get(0));
        LocalDate endDate = LocalDate.parse(allDeliveryDays.get(allDeliveryDays.size() - 1));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        long length = DAYS.between(startDate, endDate);
        String[] allDates = new String[(int) length];
        for (int i = 0; i < length; i++) {
            allDates[i] = startDate.plusDays(i).format(formatter);
        }
        return List.of(allDates);
    }
}
