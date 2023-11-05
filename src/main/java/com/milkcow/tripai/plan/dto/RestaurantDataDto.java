package com.milkcow.tripai.plan.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RestaurantDataDto {
    private int restaurantCount;
    private List<RestaurantData> restaurantDataList;
}
