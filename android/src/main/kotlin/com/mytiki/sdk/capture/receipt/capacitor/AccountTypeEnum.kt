package com.mytiki.sdk.capture.receipt.capacitor

enum class AccountTypeEnum(val type: TypeEnum,  val source: String) {
    ACME_MARKETS(TypeEnum.RETAILER, RetailerEnum.ACME_MARKETS.toString()),
    ALBERTSONS(TypeEnum.RETAILER, RetailerEnum.ALBERTSONS.toString()),
    AMAZON(TypeEnum.RETAILER, RetailerEnum.AMAZON.toString()),
    AMAZON_CA(TypeEnum.RETAILER, RetailerEnum.AMAZON_CA.toString()),
    AMAZON_UK(TypeEnum.RETAILER, RetailerEnum.AMAZON_UK.toString()),
    BED_BATH_AND_BEYOND(TypeEnum.RETAILER, RetailerEnum.BED_BATH_AND_BEYOND.toString()),
    BESTBUY(TypeEnum.RETAILER, RetailerEnum.BESTBUY.toString()),
    BJS_WHOLESALE(TypeEnum.RETAILER, RetailerEnum.BJS_WHOLESALE.toString()),
    CHEWY(TypeEnum.RETAILER, RetailerEnum.CHEWY.toString()),
    COSTCO(TypeEnum.RETAILER, RetailerEnum.COSTCO.toString()),
    CVS(TypeEnum.RETAILER, RetailerEnum.CVS.toString()),
    DICKS_SPORTING_GOODS(TypeEnum.RETAILER, RetailerEnum.DICKS_SPORTING_GOODS.toString()),
    DOLLAR_GENERAL(TypeEnum.RETAILER, RetailerEnum.DOLLAR_GENERAL.toString()),
    DOLLAR_TREE(TypeEnum.RETAILER, RetailerEnum.DOLLAR_TREE.toString()),
    DOMINOS_PIZZA(TypeEnum.RETAILER, RetailerEnum.DOMINOS_PIZZA.toString()),
    DOOR_DASH(TypeEnum.RETAILER, RetailerEnum.DOOR_DASH.toString()),
    DRIZLY(TypeEnum.RETAILER, RetailerEnum.DRIZLY.toString()),
    FAMILY_DOLLAR(TypeEnum.RETAILER, RetailerEnum.FAMILY_DOLLAR.toString()),
    FOOD_4_LESS(TypeEnum.RETAILER, RetailerEnum.FOOD_4_LESS.toString()),
    FOOD_LION(TypeEnum.RETAILER, RetailerEnum.FOOD_LION.toString()),
    FRED_MEYER(TypeEnum.RETAILER, RetailerEnum.FRED_MEYER.toString()),
    GAP(TypeEnum.RETAILER, RetailerEnum.GAP.toString()),
    GIANT_EAGLE(TypeEnum.RETAILER, RetailerEnum.GIANT_EAGLE.toString()),
    GRUBHUB(TypeEnum.RETAILER, RetailerEnum.GRUBHUB.toString()),
    HEB(TypeEnum.RETAILER, RetailerEnum.HEB.toString()),
    HOME_DEPOT(TypeEnum.RETAILER, RetailerEnum.HOME_DEPOT.toString()),
    HYVEE(TypeEnum.RETAILER, RetailerEnum.HYVEE.toString()),
    INSTACART(TypeEnum.RETAILER, RetailerEnum.INSTACART.toString()),
    JEWEL_OSCO(TypeEnum.RETAILER, RetailerEnum.JEWEL_OSCO.toString()),
    KOHLS(TypeEnum.RETAILER, RetailerEnum.KOHLS.toString()),
    KROGER(TypeEnum.RETAILER, RetailerEnum.KROGER.toString()),
    LOWES(TypeEnum.RETAILER, RetailerEnum.LOWES.toString()),
    MACYS(TypeEnum.RETAILER, RetailerEnum.MACYS.toString()),
    MARSHALLS(TypeEnum.RETAILER, RetailerEnum.MARSHALLS.toString()),
    MEIJER(TypeEnum.RETAILER, RetailerEnum.MEIJER.toString()),
    NIKE(TypeEnum.RETAILER, RetailerEnum.NIKE.toString()),
    PUBLIX(TypeEnum.RETAILER, RetailerEnum.PUBLIX.toString()),
    RALPHS(TypeEnum.RETAILER, RetailerEnum.RALPHS.toString()),
    RITE_AID(TypeEnum.RETAILER, RetailerEnum.RITE_AID.toString()),
    SAFEWAY(TypeEnum.RETAILER, RetailerEnum.SAFEWAY.toString()),
    SAMS_CLUB(TypeEnum.RETAILER, RetailerEnum.SAMS_CLUB.toString()),
    SEAMLESS(TypeEnum.RETAILER, RetailerEnum.SEAMLESS.toString()),
    SEPHORA(TypeEnum.RETAILER, RetailerEnum.SEPHORA.toString()),
    SHIPT(TypeEnum.RETAILER, RetailerEnum.SHIPT.toString()),
    SHOPRITE(TypeEnum.RETAILER, RetailerEnum.SHOPRITE.toString()),
    SPROUTS(TypeEnum.RETAILER, RetailerEnum.SPROUTS.toString()),
    STAPLES(TypeEnum.RETAILER, RetailerEnum.STAPLES.toString()),
    STAPLES_CA(TypeEnum.RETAILER, RetailerEnum.STAPLES_CA.toString()),
    STARBUCKS(TypeEnum.RETAILER, RetailerEnum.STARBUCKS.toString()),
    TACO_BELL(TypeEnum.RETAILER, RetailerEnum.TACO_BELL.toString()),
    TARGET(TypeEnum.RETAILER, RetailerEnum.TARGET.toString()),
    TJ_MAXX(TypeEnum.RETAILER, RetailerEnum.TJ_MAXX.toString()),
    UBER_EATS(TypeEnum.RETAILER, RetailerEnum.UBER_EATS.toString()),
    ULTA(TypeEnum.RETAILER, RetailerEnum.ULTA.toString()),
    VONS(TypeEnum.RETAILER, RetailerEnum.VONS.toString()),
    WALGREENS(TypeEnum.RETAILER, RetailerEnum.WALGREENS.toString()),
    WALMART(TypeEnum.RETAILER, RetailerEnum.WALMART.toString()),
    WALMART_CA(TypeEnum.RETAILER, RetailerEnum.WALMART_CA.toString()),
    WEGMANS(TypeEnum.RETAILER, RetailerEnum.WEGMANS.toString()),
//    YAHOO(TypeEnum.EMAIL, "YAHOO",
//    OUTLOOK(TypeEnum.EMAIL, "OUTLOOK",
//    AOL(TypeEnum.EMAIL, "AOL",
    GMAIL(TypeEnum.EMAIL, "GMAIL");



    override fun toString() = this.name.lowercase()

    companion object {
        fun fromString(stringValue: String) = AccountTypeEnum.values().first{ it.source == stringValue}

    }

}