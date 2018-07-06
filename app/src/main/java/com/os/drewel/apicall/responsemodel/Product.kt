package com.os.drewel.apicall.responsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Product : Serializable {

    @SerializedName("product_id")
    @Expose
    var productId: String? = null
    @SerializedName("product_name")
    @Expose
    var productName: String? = null
    @SerializedName("price")
    @Expose
    var price: String? = null
    @SerializedName("avg_price")
    @Expose
    var avgPrice: String? = null
    @SerializedName("offer_price")
    @Expose
    var offerPrice: String? = null
    @SerializedName("offer_expires_on")
    @Expose
    var offerExpiresOn: String? = null
    @SerializedName("weight")
    @Expose
    var weight: String? = null
    @SerializedName("weight_in")
    @Expose
    var weightIn: String? = null
    @SerializedName("quantity")
    @Expose
    var quantity: String? = null
    @SerializedName("product_description")
    @Expose
    var productDescription: String? = null
    @SerializedName("avg_rating")
    @Expose
    var avgRating: String? = null
    @SerializedName("min_quantity")
    @Expose
    var minQuantity: String? = null
    @SerializedName("out_of_stock")
    @Expose
    var outOfStock: Int? = null
    @SerializedName("is_offer")
    @Expose
    var isOffer: Int? = null
    @SerializedName("is_wishlist")
    @Expose
    var isWishlist: Int? = null
    @SerializedName("product_image")
    @Expose
    var productImage: String? = null

    @SerializedName("wishlist_id")
    @Expose
    var wishlistId: String? = null

    @SerializedName("review_submited")
    @Expose
    var review_submited: Int? = null

    @SerializedName("cart_qyantity")
    @Expose
    var cart_qyantity: String? = null

    @SerializedName("is_already_added_to_cart")
    @Expose
    var is_already_added_to_cart: Int? = null

    override fun toString(): String {
        return "Product(productId=$productId, productName=$productName, price=$price, avgPrice=$avgPrice, offerPrice=$offerPrice, offerExpiresOn=$offerExpiresOn, weight=$weight, weightIn=$weightIn, quantity=$quantity, productDescription=$productDescription, avgRating=$avgRating, minQuantity=$minQuantity, outOfStock=$outOfStock, isOffer=$isOffer, isWishlist=$isWishlist, productImage=$productImage, wishlistId=$wishlistId, review_submited=$review_submited, cart_qyantity=$cart_qyantity, is_already_added_to_cart=$is_already_added_to_cart)"
    }

}
