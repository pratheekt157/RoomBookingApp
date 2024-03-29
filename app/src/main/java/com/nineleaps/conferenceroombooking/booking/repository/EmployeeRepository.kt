package com.nineleaps.conferenceroombooking.booking.repository

import com.nineleaps.conferenceroombooking.model.EmployeeList
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

class EmployeeRepository @Inject constructor(){

    /**
     * function will call the api which will return some data
     */
    fun getEmployeeList(email: String, listener: ResponseListener) {
        val requestCall: Call<List<EmployeeList>> = RestClient.getWebServiceData()?.getEmployees(email)!!
        requestCall.enqueue(object : Callback<List<EmployeeList>> {
            override fun onFailure(call: Call<List<EmployeeList>>, t: Throwable) {
                listener.onFailure(ErrorException.error(t))
            }

            override fun onResponse(call: Call<List<EmployeeList>>, response: Response<List<EmployeeList>>) {
                if ((response.code() == Constants.OK_RESPONSE) or (response.code() == Constants.SUCCESSFULLY_CREATED)) {
                    listener.onSuccess(response.body()!!)
                } else {
                    listener.onFailure(response.code())
                }
            }

        })
    }
}

