package com.os.drewel.apicall.responsemodel.productdetailresponsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.os.drewel.apicall.responsemodel.Product

class Data {

    @SerializedName("Product")
    @Expose
    var product: ProductDetail? = null
    @SerializedName("RelatedProducts")
    @Expose
    var relatedProducts: List<Product>? = null

}
