package com.os.drewel.apicall.responsemodel.productdetailresponsemodel

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class ProductDetail() : Parcelable {

    @SerializedName("brand_name")
    @Expose
    var brandName: String? = null
    @SerializedName("ar_brand_name")
    @Expose
    var ar_brand_name: String? = null

    @SerializedName("brand_id")
    @Expose
    var brandId: String? = null
    @SerializedName("brand_logo")
    @Expose
    var brandLogo: String? = null
    @SerializedName("product_id")
    @Expose
    var productId: String? = null
    @SerializedName("product_name")
    @Expose
    var productName: String? = null
    @SerializedName("ar_product_name")
    @Expose
    var ar_product_name: String? = null
    @SerializedName("ar_product_description")
    @Expose
    var ar_product_description: String? = null

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
    @SerializedName("ProductImage")
    @Expose
    var productImage: List<String>? = null
    @SerializedName("Category")
    @Expose
    var category: List<Category>? = null

    @SerializedName("SubCategory")
    @Expose
    var subCategory: List<Category>? = null

    @SerializedName("avg_rating")
    @Expose
    var avgRating: String? = null


    @SerializedName("out_of_stock")
    @Expose
    var outOfStock: Int? = null
    @SerializedName("is_offer")
    @Expose
    var isOffer: Int? = null
    @SerializedName("is_wishlist")
    @Expose
    var isWishlist: Int? = null
    @SerializedName("review_submited")
    @Expose
    var review_submited: Int? = null

    @SerializedName("is_already_added_to_cart")
    @Expose
    var isAddedToCart: Int? = null
    @SerializedName("cart_qyantity")
    @Expose
    var cartQuantity: Int? = null

    constructor(parcel: Parcel) : this() {
        brandName = parcel.readString()
        brandId = parcel.readString()
        brandLogo = parcel.readString()
        productId = parcel.readString()
        productName = parcel.readString()
        price = parcel.readString()
        avgPrice = parcel.readString()
        offerPrice = parcel.readString()
        offerExpiresOn = parcel.readString()
        weight = parcel.readString()
        weightIn = parcel.readString()
        quantity = parcel.readString()
        productDescription = parcel.readString()
        productImage = parcel.createStringArrayList()
        category = parcel.createTypedArrayList(Category)
        subCategory = parcel.createTypedArrayList(Category)
        avgRating = parcel.readString()
        outOfStock = parcel.readValue(Int::class.java.classLoader) as? Int
        isOffer = parcel.readValue(Int::class.java.classLoader) as? Int
        isWishlist = parcel.readValue(Int::class.java.classLoader) as? Int
        review_submited = parcel.readValue(Int::class.java.classLoader) as? Int
        isAddedToCart = parcel.readValue(Int::class.java.classLoader) as? Int
        cartQuantity = parcel.readValue(Int::class.java.classLoader) as? Int
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(brandName)
        parcel.writeString(brandId)
        parcel.writeString(brandLogo)
        parcel.writeString(productId)
        parcel.writeString(productName)
        parcel.writeString(price)
        parcel.writeString(avgPrice)
        parcel.writeString(offerPrice)
        parcel.writeString(offerExpiresOn)
        parcel.writeString(weight)
        parcel.writeString(weightIn)
        parcel.writeString(quantity)
        parcel.writeString(productDescription)
        parcel.writeStringList(productImage)
        parcel.writeTypedList(category)
        parcel.writeTypedList(subCategory)
        parcel.writeString(avgRating)
        parcel.writeValue(outOfStock)
        parcel.writeValue(isOffer)
        parcel.writeValue(isWishlist)
        parcel.writeValue(review_submited)
        parcel.writeValue(isAddedToCart)
        parcel.writeValue(cartQuantity)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ProductDetail> {
        override fun createFromParcel(parcel: Parcel): ProductDetail {
            return ProductDetail(parcel)
        }

        override fun newArray(size: Int): Array<ProductDetail?> {
            return arrayOfNulls(size)
        }
    }


}
