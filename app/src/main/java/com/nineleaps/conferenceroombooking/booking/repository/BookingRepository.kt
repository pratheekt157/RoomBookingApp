package com.nineleaps.conferenceroombooking.booking.repository

import com.nineleaps.conferenceroombooking.model.Booking
import com.nineleaps.conferenceroombooking.services.ResponseListener
import com.nineleaps.conferenceroombooking.services.RestClient
import com.nineleaps.conferenceroombooking.utils.Constants
import com.nineleaps.conferenceroombooking.utils.ErrorException
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

class BookingRepository @Inject constructor(){

    /**
     * function will initialize the MutableLivedata Object and than call a function for api call
     * Passing the Context and model and call API, In return sends the status of LiveData
     */
    fun addBookingDetails(mBooking: Booking, listener: ResponseListener) {
        /**
         * api call using retorfit
         */
        val requestCall: Call<ResponseBody> = RestClient.getWebServiceData()?.addBookingDetails(mBooking)!!
        requestCall.enqueue(object : Callback<ResponseBody> {

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                listener.onFailure(ErrorException.error(t))
            }
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if ((response.code() == Constants.OK_RESPONSE) or (response.code() == Constants.SUCCESSFULLY_CREATED)) {
                    listener.onSuccess(response.code())
                } else {
                    listener.onFailure(response.code())
                }
            }
        })
    }

}
