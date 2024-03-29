package com.nineleaps.conferenceroombooking.booking.repository

import android.util.Log
import com.example.conferenceroomapp.model.InputDetailsForRoom
import com.nineleaps.conferenceroombooking.model.RoomDetails
import com.nineleaps.conferenceroombooking.services.ResponseListener
import com.nineleaps.conferenceroombooking.services.RestClient
import com.nineleaps.conferenceroombooking.utils.Constants
import com.nineleaps.conferenceroombooking.utils.ErrorException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject


class ConferenceRoomRepository @Inject constructor() {
    /**
     * function will initialize the MutableLivedata Object and than make API Call
     * Passing the Context and model and call API, In return sends the status of LiveData
     */
    fun getConferenceRoomList(mInputDetailsForRoom: InputDetailsForRoom, listener: ResponseListener) {
        /**
         * api call using Retrofit
         */
        val requestCall: Call<List<RoomDetails>> = RestClient.getWebServiceData()?.getConferenceRoomList(mInputDetailsForRoom)!!
        requestCall.enqueue(object : Callback<List<RoomDetails>> {
            override fun onResponse(call: Call<List<RoomDetails>>, response: Response<List<RoomDetails>>) {
                if ((response.code() == Constants.OK_RESPONSE) or (response.code() == Constants.SUCCESSFULLY_CREATED)) {
                    listener.onSuccess(response.body()!!)
                } else {
                    listener.onFailure(response.code())
                }
            }
            override fun onFailure(call: Call<List<RoomDetails>>, t: Throwable) {
                listener.onFailure(ErrorException.error(t))
            }

        })

    }
}
