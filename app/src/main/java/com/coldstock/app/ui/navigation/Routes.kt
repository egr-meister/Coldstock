package com.coldstock.app.ui.navigation

/** Central route definitions for Navigation Compose. */
object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"                       // Freezer Drawer Map
    const val FREEZER_SETUP = "freezer_setup"     // add
    const val FREEZER_EDIT = "freezer_edit"       // freezer_edit/{freezerId}
    const val DRAWER_DETAIL = "drawer_detail"     // drawer_detail/{drawerId}
    const val ADD_PRODUCT = "add_product"         // add_product?drawerId={drawerId}
    const val EDIT_PRODUCT = "edit_product"       // edit_product/{productId}
    const val PRODUCT_DETAIL = "product_detail"   // product_detail/{productId}
    const val ALL_PRODUCTS = "all_products"
    const val SEARCH = "search"
    const val USE_FIRST = "use_first"
    const val HISTORY = "history"
    const val HISTORY_DETAIL = "history_detail"   // history_detail/{eventId}
    const val FREEZER_MANAGEMENT = "freezer_management"
    const val DRAWER_MANAGEMENT = "drawer_management"
    const val STATISTICS = "statistics"
    const val SETTINGS = "settings"

    const val ARG_FREEZER_ID = "freezerId"
    const val ARG_DRAWER_ID = "drawerId"
    const val ARG_PRODUCT_ID = "productId"
    const val ARG_EVENT_ID = "eventId"

    fun freezerEdit(id: String) = "$FREEZER_EDIT/$id"
    fun drawerDetail(id: String) = "$DRAWER_DETAIL/$id"
    fun addProduct(drawerId: String? = null) =
        if (drawerId.isNullOrBlank()) ADD_PRODUCT else "$ADD_PRODUCT?$ARG_DRAWER_ID=$drawerId"
    fun editProduct(id: String) = "$EDIT_PRODUCT/$id"
    fun productDetail(id: String) = "$PRODUCT_DETAIL/$id"
    fun historyDetail(id: String) = "$HISTORY_DETAIL/$id"
}
