package com.licenta.supp_rel.charts;

import com.licenta.supp_rel.ratings.Rating;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChartsService {
    public List<GroupedVerticalBarChartDTO> getCurveRatingsAndCorrectDeliveriesPercentage(List<Rating> ratings){
        List<GroupedVerticalBarChartDTO> groupedVerticalBarChartDTOs = new ArrayList<>();
        for(Rating rating: ratings) {
            List<VerticalBarChartDTO> verticalBarChartDTOS = new ArrayList<>();
            verticalBarChartDTOS.add(new VerticalBarChartDTO("qtyNrDevisRating", rating.getQtyNrDevisRating()));
            verticalBarChartDTOS.add(new VerticalBarChartDTO("dayNrDevisRating", rating.getDayNrDevisRating()));
            verticalBarChartDTOS.add(new VerticalBarChartDTO("qtyDeviationCurveRating", rating.getQtyDeviationCurveRating()));
            verticalBarChartDTOS.add(new VerticalBarChartDTO("dayDeviationCurveRating", rating.getDayDeviationCurveRating()));
            verticalBarChartDTOS.add(new VerticalBarChartDTO("correctDeliveriesPercentage", rating.getCorrectDeliveriesPercentage()));
            groupedVerticalBarChartDTOs.add(new GroupedVerticalBarChartDTO(rating.getSupplier().getId(), verticalBarChartDTOS));
        }
        return groupedVerticalBarChartDTOs;
    }

    public List<VerticalBarChartDTO> getTotalNumberOfDeliveries(List<Rating> ratings){
        List<VerticalBarChartDTO> verticalBarChartDTOs = new ArrayList<>();
        for(Rating rating: ratings)
            verticalBarChartDTOs.add(new VerticalBarChartDTO(rating.getSupplier().getId(), Float.valueOf(rating.getTotalNumberDeliveries())));
        return verticalBarChartDTOs;
    }

    public List<VerticalBarChartDTO> getPriceDeviations(List<Rating> ratings){
        List<VerticalBarChartDTO> verticalBarChartDTOs = new ArrayList<>();
        for(Rating rating: ratings)
            verticalBarChartDTOs.add(new VerticalBarChartDTO(rating.getSupplier().getId(), rating.getPriceDeviationPercentage()));
        return verticalBarChartDTOs;
    }

    public List<VerticalBarChartDTO> getDistanceToPlant(List<Rating> ratings){
        List<VerticalBarChartDTO> verticalBarChartDTOs = new ArrayList<>();
        for(Rating rating: ratings)
            verticalBarChartDTOs.add(new VerticalBarChartDTO(rating.getSupplier().getId(), rating.getDistanceToPlant()));
        return verticalBarChartDTOs;
    }

    public List<GroupedVerticalBarChartDTO> getAllAverageHours(List<Rating> ratings){
        List<GroupedVerticalBarChartDTO> groupedVerticalBarChartDTOs = new ArrayList<>();
        for(Rating rating: ratings) {
            List<VerticalBarChartDTO> verticalBarChartDTOS = new ArrayList<>();
            verticalBarChartDTOS.add(new VerticalBarChartDTO("averageLeadTimeInHours", rating.getAverageLeadTimeInHours()));
            verticalBarChartDTOS.add(new VerticalBarChartDTO("averageNumberOfHoursToDeliver", rating.getAverageNumberOfHoursToDeliver()));
            groupedVerticalBarChartDTOs.add(new GroupedVerticalBarChartDTO(rating.getSupplier().getId(), verticalBarChartDTOS));
        }
        return groupedVerticalBarChartDTOs;
    }

}
