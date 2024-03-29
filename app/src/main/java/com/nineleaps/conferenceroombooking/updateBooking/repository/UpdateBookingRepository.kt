package com.nineleaps.conferenceroombooking.updateBooking.repository

import com.nineleaps.conferenceroombooking.model.UpdateBooking
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

class UpdateBookingRepository @Inject constructor() {
    /**
     * function will make an API call to make request for the updation of booking
     * and call the interface method with data from server
     */
    fun updateBookingDetails(mUpdateBooking: UpdateBooking, listener: ResponseListener) {
        val requestCall: Call<ResponseBody> = RestClient.getWebServiceData()?.update(mUpdateBooking)!!
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