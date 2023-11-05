package com.milkcow.tripai.plan.embedded;

public enum PriceRange {
    FIVE("$", 1, 1, 0, 5),
    FIVE_TO_TEN("$~$$", 1, 2, 5, 10),
    TEN_TO_TWENTY("$~$$$", 1, 3, 10, 20),
    MORE_THAN_TWENTY("$~$$$$", 1, 4, 20, Integer.MAX_VALUE),
    ;
    private final String priceRange;
    private final int minDollar;
    private final int maxDollar;
    private final int minPrice;
    private final int maxPrice;

    PriceRange(String priceRange, int minDollar, int maxDollar, int minPrice, int maxPrice) {
        this.priceRange = priceRange;
        this.minDollar = minDollar;
        this.maxDollar = maxDollar;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }

    public static PriceRange of(int price) {
        if (price <= 50000) {
            return FIVE;
        } else if (price <= 100000) {
            return FIVE_TO_TEN;
        } else if (price <= 200000) {
            return TEN_TO_TWENTY;
        } else {
            return MORE_THAN_TWENTY;
        }
    }

    public boolean isInRange(int dollarCount) {
        return dollarCount >= this.minDollar && dollarCount <= this.maxDollar;
    }
}
