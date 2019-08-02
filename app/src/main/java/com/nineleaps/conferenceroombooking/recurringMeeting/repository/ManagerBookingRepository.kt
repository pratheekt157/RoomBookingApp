package com.nineleaps.conferenceroombooking.recurringMeeting.repository

import com.nineleaps.conferenceroombooking.model.ManagerBooking
import com.nineleaps.conferenceroombooking.services.ResponseListener
import com.nineleaps.conferenceroombooking.services.RestClient
import com.nineleaps.conferenceroombooking.services.RestClient1
import com.nineleaps.conferenceroombooking.utils.Constants
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

class ManagerBookingRepository @Inject constructor() {
    /**
     * function will initialize the MutableLivedata Object and than call a function for api call
     * Passing the Context and model and call API, In return sends the status of LiveData
     */
    fun addBookingDetails(mBooking: ManagerBooking, token: String, listener: ResponseListener) {
        /**
         * api call using retrofit
         */
        val requestCall: Call<ResponseBody> =
            RestClient1.getWebServiceData()?.addManagerBookingDetails(mBooking)!!
        requestCall.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                when (t) {
                    is SocketTimeoutException -> {
                        listener.onFailure(Constants.POOR_INTERNET_CONNECTION)
                    }
                    is UnknownHostException -> {
                        listener.onFailure(Constants.POOR_INTERNET_CONNECTION)
                    }
                    else -> {
                        listener.onFailure(Constants.INTERNAL_SERVER_ERROR)
                    }
                }
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