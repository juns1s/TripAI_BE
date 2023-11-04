package com.milkcow.tripai.plan.service.restaurant;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.milkcow.tripai.global.exception.GeneralException;
import com.milkcow.tripai.global.result.ApiResult;
import com.milkcow.tripai.plan.dto.RestaurantData;
import com.milkcow.tripai.plan.dto.RestaurantDataDto;
import com.milkcow.tripai.plan.embedded.PriceRange;
import com.milkcow.tripai.plan.exception.PlanException;
import com.milkcow.tripai.plan.result.PlanResult;
import com.milkcow.tripai.plan.util.DateUtil;
import java.util.ArrayList;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class RestaurantServiceImpl implements RestaurantService {
    @Value("${API-keys.Restaurants.Typeahead-URL}")
    private String SEARCH_DESTINATION_URL;
    @Value("${API-keys.Restaurants.Search-URL}")
    private String SEARCH_RESTAURANT_URL;
    @Value("${API-keys.Restaurants.API-Key}")
    private String APIKEY;
    @Value("${API-keys.Restaurants.API-Host}")
    private String APIHOST;

    @Override
    public RestaurantDataDto getRestaurantData(String destination, String startDate, String endDate, int maxPrice) {
        try {
            long duration = DateUtil.calculateDuration(startDate, endDate);
            PriceRange priceRange = PriceRange.of((int) (maxPrice / duration));

            ArrayList<RestaurantData> restaurantDataList = new ArrayList<>();
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = setHeaders();

            String typeaheadBody = setTypeaheadBody(destination);

            HttpEntity<String> typeaheadRequestEntity = new HttpEntity<>(typeaheadBody, headers);

            ResponseEntity<String> typeaheadResponse = restTemplate.exchange(SEARCH_DESTINATION_URL, HttpMethod.POST,
                    typeaheadRequestEntity,
                    String.class);
            if (typeaheadResponse.getStatusCode() != HttpStatus.OK) {
                throw new PlanException(PlanResult.RESTAURANT_DESTINATION_API_REQUEST_FAILED);
            }

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode typeaheadJson = objectMapper.readTree(typeaheadResponse.getBody());
            String locationId = typeaheadJson.path("results").path("data").path(0).path("result_object")
                    .get("location_id").asText();

            String restaurantBody = setRestaurantBody(locationId);

            HttpEntity<String> restaurantRequestEntity = new HttpEntity<>(restaurantBody, headers);

            ResponseEntity<String> restaurantResponse = restTemplate.exchange(SEARCH_RESTAURANT_URL, HttpMethod.POST,
                    restaurantRequestEntity,
                    String.class);
            if (restaurantResponse.getStatusCode() != HttpStatus.OK) {
                throw new PlanException(PlanResult.RESTAURANT_SEARCH_API_REQUEST_FAILED);
            }
            JsonNode restaurantJson = objectMapper.readTree(restaurantResponse.getBody());
            JsonNode restaurantList = restaurantJson.path("results").path("data");

            for (JsonNode r : restaurantList) {
                Optional<RestaurantData> restaurantData = parseRestaurantData(r, priceRange);
                restaurantData.ifPresent(restaurantDataList::add);
            }

            return RestaurantDataDto.builder()
                    .restaurantCount(restaurantDataList.size())
                    .restaurantDataList(restaurantDataList)
                    .build();

        } catch (JsonProcessingException e) {
            throw new GeneralException(ApiResult.INTERNAL_SERVER_ERROR);
        } catch (HttpClientErrorException e) {
            System.out.println("e = " + e);
            throw new PlanException(PlanResult.RESTAURANT_API_KEY_LIMIT_EXCESS);
        }
    }

    private HttpHeaders setHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("content-type", "application/x-www-form-urlencoded");
        headers.add("X-RapidAPI-Key", this.APIKEY);
        headers.add("X-RapidAPI-Host", this.APIHOST);

        return headers;
    }

    private String setTypeaheadBody(String destination) {
        return "q=" + destination +
                "&language=ko_KR";
    }

    private String setRestaurantBody(String locationId) {
        return "location_id=" + locationId +
                "&language=ko_KR" +
                "&currency=KRW";
    }

    private Optional<RestaurantData> parseRestaurantData(JsonNode restaurant, PriceRange range) {
        String price = restaurant.get("price_level").asText();
        int dollarCount = price.split(" - ")[0].length();

        System.out.println("price = " + price);
        System.out.println("dollarCount = " + dollarCount);
        System.out.println();

        if (!range.isInRange(dollarCount)) {
            return Optional.empty();
        }

        String name = restaurant.get("name").asText();
        double lat = Double.parseDouble(restaurant.get("latitude").asText());
        double lng = Double.parseDouble(restaurant.get("longitude").asText());
        //TODO 영업시간 전달방식 결정
        restaurant.path("hours").get("week_ranges");
        String image = restaurant.path("photo").path("images").path("small").get("url").asText();

        RestaurantData restaurantData = RestaurantData.builder()
                .name(name)
                .lat(lat)
                .lng(lng)
                .price(price)
                .image(image)
                .build();
        return Optional.of(restaurantData);
    }
}
