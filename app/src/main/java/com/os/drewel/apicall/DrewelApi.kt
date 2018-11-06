package com.os.drewel.apicall

import com.os.drewel.apicall.requestmodel.CheckOutRequest
import com.os.drewel.apicall.requestmodel.SignUpRequest
import com.os.drewel.apicall.responsemodel.addaddressresponsemodel.AddAddressResponse
import com.os.drewel.apicall.responsemodel.addtocartresponsemodel.AddToCartResponse
import com.os.drewel.apicall.responsemodel.applycouponresponsemodel.ApplyCouponResponse
import com.os.drewel.apicall.responsemodel.applyloyaltypointresponsemodel.ApplyLoyaltyPointResponse
import com.os.drewel.apicall.responsemodel.baseresponsemodel.BaseResponse
import com.os.drewel.apicall.responsemodel.cartdetailresponsemodel.CartDetailResponse
import com.os.drewel.apicall.responsemodel.categoryresponsemodel.CategoryListResponse
import com.os.drewel.apicall.responsemodel.checkoutresponsemodel.CheckOutResponse
import com.os.drewel.apicall.responsemodel.couponresponsemodel.CouponListResponse
import com.os.drewel.apicall.responsemodel.deliveryaddressresponsemodel.AddressListResponse
import com.os.drewel.apicall.responsemodel.deliverychargesresponsemodel.DeliveryChangesResponse
import com.os.drewel.apicall.responsemodel.driverlocationresponsemodel.DriverLocationResponse
import com.os.drewel.apicall.responsemodel.googledirectionresultmodel.DirectionResults
import com.os.drewel.apicall.responsemodel.loginresponsemodel.LoginResponse
import com.os.drewel.apicall.responsemodel.loyaltypointresponsemodel.LoyaltyPointResponse
import com.os.drewel.apicall.responsemodel.myorderdetailresponsemodel.MyOrderDetailResponse
import com.os.drewel.apicall.responsemodel.myorderresponsemodel.MyOrderResponse
import com.os.drewel.apicall.responsemodel.notificationresponsemodel.NotificationResponse
import com.os.drewel.apicall.responsemodel.productRequestresponsemodel.ProductRequestResponse
import com.os.drewel.apicall.responsemodel.productdetailresponsemodel.ProductDetailResponse
import com.os.drewel.apicall.responsemodel.productlistresponsemodel.ProductListResponse
import com.os.drewel.apicall.responsemodel.profileresponsemodel.ProfileResponse
import com.os.drewel.apicall.responsemodel.resendOtpresponsemodel.ResendOTPResponse
import com.os.drewel.apicall.responsemodel.reviewResponseModel.ReviewListResponse
import com.os.drewel.apicall.responsemodel.saveaddressresponsemodel.Response
import com.os.drewel.apicall.responsemodel.searchproductresponsemodel.SearchProductResponse
import com.os.drewel.apicall.responsemodel.searchsuggestionresponsemodel.SearchSuggestionResponse
import com.os.drewel.apicall.responsemodel.unreadnotificationresponsemodel.UnreadNotificationResponse
import com.os.drewel.apicall.responsemodel.walletTransactionResponseModel.TransactionResponse
import com.os.drewel.apicall.responsemodel.wishlistresponsemodel.WishlistResponse
import io.reactivex.Observable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

/**
 * Created by monikab on 3/13/2018.
 */
interface DrewelApi {
    companion object {
//        const val BASE_URL = "http://192.168.1.92/drewel/web_services/";
        const val BASE_URL = "https://56.octallabs.com/drewel/web_services/"
        const val HOW_ITS_WORK_URL = "https://56.octallabs.com/drewel/how-it-works"
        const val TERMS_OF_USE_URL = "https://56.octallabs.com/drewel/terms-of-use"
        const val PRIVACY_POLICY_URL = "https://56.octallabs.com/drewel/privacy-policy"
        const val FAQ_URL = "https://56.octallabs.com/drewel/faq"
        const val ABOUT_US = "https://56.octallabs.com/drewel/about-us"
    }

    @Headers("Content-Type: application/json")
    @POST("signup")
    fun signup(@Body signupRequest: SignUpRequest): Observable<LoginResponse>

    @Headers("Content-Type: application/json")
    @POST("login")
    fun login(@Body loginRequest: MutableMap<String, String>): Observable<LoginResponse>

    @Headers("Content-Type: application/json")
    @POST("social_login")
    fun socialLogin(@Body socialLoginRequest: Map<String, String>): Observable<LoginResponse>

    @Headers("Content-Type: application/json")
    @POST("otp_verification")
    fun otpVerification(@Body otpVerificationRequest: Map<String, String>): Observable<LoginResponse>

    @Headers("Content-Type: application/json")
    @POST("resend_otp")
    fun resendOtpVerification(@Body otpVerificationRequest: Map<String, String>): Observable<ResendOTPResponse>

    @Headers("Content-Type: application/json")
    @POST("send_otp_profile")
    fun resendOtpVerificationProfile(@Body otpVerificationRequest: Map<String, String>): Observable<ResendOTPResponse>

    @Headers("Content-Type: application/json")
    @POST("forgot_password")
    fun forgotPassword(@Body forgotPasswordRequest: Map<String, String>): Observable<BaseResponse>

    @Headers("Content-Type: application/json")
    @POST("change_password")
    fun changePassword(@Body changePasswordRequest: Map<String, String>): Observable<BaseResponse>

    @Headers("Content-Type: application/json")
    @POST("profile")
    fun profile(@Body profileRequest: Map<String, String>): Observable<ProfileResponse>

    @Headers("Content-Type: application/json")
    @POST("get_notifications")
    fun getNotifications(@Body getNotificationRequest: Map<String, String>): Observable<NotificationResponse>

    @Headers("Content-Type: application/json")
    @POST("category_list")
    fun getCategory(@Body categoryRequest: Map<String, String>): Observable<CategoryListResponse>

    @Headers("Content-Type: application/json")
    @POST("product_list_by_category")
    fun getProduct(@Body productRequest: Map<String, @JvmSuppressWildcards Any>/*, @Query("brands_id[]") brandAry:JSONArray*/): Observable<ProductListResponse>

    @Headers("Content-Type: application/json")
    @POST("product_detail")
    fun getProductDetail(@Body productDetailRequest: Map<String, String>): Observable<ProductDetailResponse>

    @Headers("Content-Type: application/json")
    @POST("add_address")
    fun addAddress(@Body addAddressRequest: Map<String, String>): Observable<AddAddressResponse>

    @Headers("Content-Type: application/json")
    @POST("availability_check")
    fun checkAddress(@Body addAddressRequest: Map<String, String>): Observable<BaseResponse>

    @Headers("Content-Type: application/json")
    @POST("is_delivery_place")
    fun is_delivery_place(@Body addAddressRequest: Map<String, String>): Observable<AddAddressResponse>

    @Headers("Content-Type: application/json")
    @POST("address_list")
    fun getAddressList(@Body addressListRequest: Map<String, String>): Observable<AddressListResponse>

    @Headers("Content-Type: application/json")
    @POST("set_default_address")
    fun setDefaultAddress(@Body setDefaultAddress: Map<String, String>): Observable<BaseResponse>

    @Headers("Content-Type: application/json")
    @POST("delete_address")
    fun deleteAddress(@Body setDefaultAddress: Map<String, String>): Observable<BaseResponse>

    @Headers("Content-Type: application/json")
    @POST("add_wishlist")
    fun addWishlist(@Body addToWishList: Map<String, String>): Observable<BaseResponse>

    @Headers("Content-Type: application/json")
    @POST("get_wishlist")
    fun getWishlist(@Body wishListRequest: Map<String, String>): Observable<WishlistResponse>


    @Headers("Content-Type: application/json")
    @POST("product_list_offer")
    fun getDiscount(@Body wishListRequest: Map<String, String>): Observable<WishlistResponse>

    /*  @Headers("Content-Type: application/json")
      @POST("brands_list")
      fun getBrandsList(@Body brandListRequest: Map<String, String>): Observable<BrandNameListResponse>*/

    @Headers("Content-Type: application/json")
    @POST("auto_suggestions")
    fun getSearchSuggestion(@Body brandListRequest: Map<String, String>): Observable<SearchSuggestionResponse>

    @Headers("Content-Type: application/json")
    @POST("product_list_by_search")
    fun getSearchProduct(@Body productRequest: Map<String, String>): Observable<SearchProductResponse>

    @Headers("Content-Type: application/json")
    @POST("add_to_cart")
    fun addToCart(@Body addToCartRequest: Map<String, String>): Observable<AddToCartResponse>

    @Headers("Content-Type: application/json")
    @POST("product_notify")
    fun productNotify(@Body notifyMeRequest: Map<String, String>): Observable<BaseResponse>

    @Headers("Content-Type: application/json")
    @POST("get_cart")
    fun getCartDetail(@Body cartRequest: Map<String, String>): Observable<CartDetailResponse>

    @Headers("Content-Type: application/json")
    @POST("logout")
    fun logout(@Body logoutRequest: Map<String, String>): Observable<BaseResponse>

    @Headers("Content-Type: application/json")
    @POST("add_chat")
    fun add_chat(@Body logoutRequest: Map<String, String>): Observable<com.os.drewel.apicall.responsemodel.addchatresponsemodel.BaseResponse>


    @Headers("Content-Type: application/json")
    @POST("notification_status")
    fun notificationOnOff(@Body notificationOnOffRequest: Map<String, String>): Observable<BaseResponse>

    @Multipart
    @POST("edit_profile")
    fun editProfile(@Part("data") requestBody: RequestBody, @Part file: MultipartBody.Part?): Observable<ProfileResponse>

    @Headers("Content-Type: application/json")
    @POST("coupons_list")
    fun getDiscountList(@Body discountListRequest: Map<String, String>): Observable<CouponListResponse>

    @Headers("Content-Type: application/json")
    @POST("product_request_list")
    fun getProductRequestList(@Body discountListRequest: Map<String, String>): Observable<ProductRequestResponse>

    @Headers("Content-Type: application/json")
    @POST("update_cart")
    fun updateCart(@Body cartRequest: Map<String, String>): Observable<AddToCartResponse>

    @Headers("Content-Type: application/json")
    @POST("edit_order")
    fun editCart(@Body cartRequest: Map<String, String>): Observable<AddToCartResponse>

    @Headers("Content-Type: application/json")
    @POST("delete_order")
    fun delete_order(@Body cartRequest: Map<String, String>): Observable<AddToCartResponse>

    @Headers("Content-Type: application/json")
    @POST("delete_cart_product")
    fun deleteCartProduct(@Body cartRequest: Map<String, String>): Observable<AddToCartResponse>

    @Headers("Content-Type: application/json")
    @POST("apply_coupon")
    fun applyCoupon(@Body applyCouponRequest: Map<String, String>): Observable<ApplyCouponResponse>

    @Headers("Content-Type: application/json")
    @POST("apply_loyalty_point")
    fun applyLoyaltyPoint(@Body applyCouponRequest: Map<String, String>): Observable<ApplyLoyaltyPointResponse>

    @Headers("Content-Type: application/json")
    @POST("get_delivery_charges")
    fun getDeliveryCharges(@Body getDeliveryChargesRequest: Map<String, String>): Observable<DeliveryChangesResponse>

    @Headers("Content-Type: application/json")
    @POST("cart_checkout")
    fun checkout(@Body checkOutRequest: CheckOutRequest): Observable<CheckOutResponse>

    @Headers("Content-Type: application/json")
    @POST("order_list")
    fun getMyOrders(@Body myOrderRequest: Map<String, String>): Observable<MyOrderResponse>

    @Headers("Content-Type: application/json")
    @POST("get_order_detail")
    fun getMyOrderDetail(@Body myOrderDetailRequest: Map<String, String>): Observable<MyOrderDetailResponse>

    @Headers("Content-Type: application/json")
    @POST("order_cancel")
    fun cancelOrder(@Body cancelOrderRequest: Map<String, String>): Observable<BaseResponse>

    @Headers("Content-Type: application/json")
    @POST("reorder")
    fun reorder(@Body reorderRequest: Map<String, String>): Observable<AddToCartResponse>

    @Headers("Content-Type: application/json")
    @POST("loyalty_points_list")
    fun getLoyaltyPointsTransaction(@Body loyaltyPointsTransactionRequest: Map<String, String>): Observable<LoyaltyPointResponse>


    @Headers("Content-Type: application/json")
    @POST("transfer_loyalty_points")
    fun transferLoyaltyPoints(@Body loyaltyPointsTransactionRequest: Map<String, String>): Observable<BaseResponse>

    @GET
    fun getGoogleDirection(@Url url: String): Observable<DirectionResults>

    @Headers("Content-Type: application/json")
    @POST("get_delivery_boy_location")
    fun getDriverLocation(@Body driverLocationRequest: Map<String, String>): Observable<DriverLocationResponse>

    @Headers("Content-Type: application/json")
    @POST("read_notification")
    fun readNotification(@Body myOrderDetailRequest: Map<String, String>): Observable<BaseResponse>

    @Headers("Content-Type: application/json")
    @POST("delete_notification")
    fun deleteNotification(@Body myOrderDetailRequest: Map<String, String>): Observable<BaseResponse>


    @Headers("Content-Type: application/json")
    @POST("clear_wallet")
    fun clear_wallet(@Body myOrderDetailRequest: Map<String, String>): Observable<BaseResponse>

    @Headers("Content-Type: application/json")
    @POST("clear_loyalty")
    fun clear_loyalty(@Body myOrderDetailRequest: Map<String, String>): Observable<BaseResponse>

    @Headers("Content-Type: application/json")
    @POST("clear_order")
    fun clear_order(@Body myOrderDetailRequest: Map<String, String>): Observable<BaseResponse>


    @Headers("Content-Type: application/json")
    @POST("product_request")
    fun requestProduct(@Body myOrderDetailRequest: Map<String, String>): Observable<BaseResponse>

    @Headers("Content-Type: application/json")
    @POST("delete_product_request")
    fun delete_product_request(@Body myOrderDetailRequest: Map<String, String>): Observable<BaseResponse>

    @Headers("Content-Type: application/json")
    @POST("edit_product_request")
    fun edit_product_request(@Body myOrderDetailRequest: Map<String, String>): Observable<BaseResponse>

    @Headers("Content-Type: application/json")
    @POST("add_ratings")
    fun addRating(@Body myOrderDetailRequest: Map<String, String>): Observable<BaseResponse>

    @Headers("Content-Type: application/json")
    @POST("product_review_list")
    fun productReviewList(@Body loyaltyPointsTransactionRequest: Map<String, String>): Observable<ReviewListResponse>

    @Headers("Content-Type: application/json")
    @POST("unread_notification")
    fun unread_notification(@Body loyaltyPointsTransactionRequest: Map<String, String>): Observable<UnreadNotificationResponse>

    @Headers("Content-Type: application/json")
    @POST("order_address_save")
    fun order_address_save(@Body addressListRequest: Map<String, String>): Observable<com.os.drewel.apicall.responsemodel.saveaddressresponsemodel.AddressListResponse>

    @Headers("Content-Type: application/json")
    @POST("wallet_list")
    fun getTransactions(@Body getNotificationRequest: Map<String, String>): Observable<TransactionResponse>

}