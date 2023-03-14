package com.licenta.supp_rel.deviations;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
@RequestMapping(path = "deviations")
@RequiredArgsConstructor
public class DeviationController {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @PostMapping("calculate-deviations")
    public ResponseEntity<DeviationResponse> calculateDeviations(@DefaultValue("yyyy-mm-dd") @RequestParam("day") String day){
        if(day.equals("yyyy-mm-dd")) {
            day = dateFormat.format(new Date());
        }
        return null;
    }

}
