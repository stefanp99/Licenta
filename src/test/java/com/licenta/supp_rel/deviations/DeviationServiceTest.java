package com.licenta.supp_rel.deviations;

import com.licenta.supp_rel.contracts.Contract;
import com.licenta.supp_rel.deliveries.Delivery;
import com.licenta.supp_rel.tolerances.ToleranceService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class DeviationServiceTest {

    @Test
    public void isQtyDeviation(){
        Delivery delivery = new Delivery();
        delivery.setExpectedQuantity(100L);
        Contract contract = new Contract();
        contract.setPlantId("1");
        contract.setSupplierId("1");
        contract.setMaterialCode("MATERIAL_1");
        delivery.setContract(contract);

        ToleranceService toleranceService = mock(ToleranceService.class);
        DeviationService deviationService = mock(DeviationService.class);
        when(toleranceService.getUpperQtyToleranceByPlantIdSupplierIdMaterialCode("1", "1", "MATERIAL_1")).thenReturn(100f);
        when(toleranceService.getLowerQtyToleranceByPlantIdSupplierIdMaterialCode("1", "1", "MATERIAL_1")).thenReturn(100f);

        assertFalse(deviationService.isQtyDeviation(delivery, 100L));
        assertTrue(deviationService.isQtyDeviation(delivery, 121L));
        assertTrue(deviationService.isQtyDeviation(delivery, 79L));

        when(toleranceService.getUpperQtyToleranceByPlantIdSupplierIdMaterialCode("1", "1", "MATERIAL_1")).thenReturn(0f);
        when(toleranceService.getLowerQtyToleranceByPlantIdSupplierIdMaterialCode("1", "1", "MATERIAL_1")).thenReturn(0f);

        assertFalse(deviationService.isQtyDeviation(delivery, 100L));

    }
}
