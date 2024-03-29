package com.nineleaps.conferenceroombooking.Helper

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.nineleaps.conferenceroombooking.R
import com.nineleaps.conferenceroombooking.model.Dashboard
import com.nineleaps.conferenceroombooking.model.GetIntentDataFromActvity
import com.nineleaps.conferenceroombooking.utils.FormatDate
import com.nineleaps.conferenceroombooking.utils.FormatTimeAccordingToZone
import java.text.SimpleDateFormat

@Suppress("NAME_SHADOWING")
class UpcomingBookingAdapter(
    private val dashboardItemList: ArrayList<Dashboard>,
    val mContext: Context,
    private val btnListener: CancelBtnClickListener,
    private val mShowMembers: ShowMembersListener,
    private val mEditBooking: EditBookingListener,
    private val mMoreListener: MoreAminitiesListner
) : androidx.recyclerview.widget.RecyclerView.Adapter<UpcomingBookingAdapter.ViewHolder>() {

    private var mIntentData = GetIntentDataFromActvity()

    /**
     * a variable which will hold the 'Instance' of interface
     */
    companion object {
        var mCancelBookingClickListener: CancelBtnClickListener? = null
        var mShowMembersListener: ShowMembersListener? = null
        var mEditBookingListener: EditBookingListener? = null
        var mMoreAminitiesListener: MoreAminitiesListner? = null
        var count = 0
    }

    /**
     * this override function will set a view for the recyclerview items
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.fragment_upcoming_booking_list_items, parent, false)
        return ViewHolder(view)
    }

    /**
     * bind data with the view
     */
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        mCancelBookingClickListener = btnListener
        mShowMembersListener = mShowMembers
        mEditBookingListener = mEditBooking
        mMoreAminitiesListener = mMoreListener
        val fromTime = dashboardItemList[position].fromTime
        val toTime = dashboardItemList[position].toTime
        val fromDate = fromTime!!.split("T")
        val toDate = toTime!!.split("T")

        if (dashboardItemList[position].isTagged == true) {
            holder.attendeeTextView.visibility = View.VISIBLE
            holder.actionLayout.visibility = View.GONE
            holder.landingView.visibility = View.GONE
        } else {
            holder.attendeeTextView.visibility = View.GONE
            holder.actionLayout.visibility = View.VISIBLE
            holder.landingView.visibility = View.VISIBLE
            holder.editTextView.setOnClickListener {
                editActivity(position)
            }
        }

        if (dashboardItemList[position].amenities!!.isEmpty()) {
            holder.aminityTitle.text = mContext.getString(R.string.no_aminities)
            disableAmenitiesHolder(holder)
        }
        else
            for (i in dashboardItemList[position].amenities!!.indices) {
                getAmenitiesHolder(holder,i,position)
            }

        holder.amenity3.setOnClickListener {
            if (holder.amenity3.text == "More") {
                mMoreListener.moreAmenities(position)
            }
        }
        setDataToFields(holder, position)

        val localStartTime = FormatTimeAccordingToZone.formatDateAsIndianStandardTime("${fromDate[0]} ${fromDate[1]}")
        val localEndTime = FormatTimeAccordingToZone.formatDateAsIndianStandardTime("${fromDate[0]} ${toDate[1]}")

        holder.dateTextView.text = formatDate(localStartTime.split(" ")[0])
        holder.fromTimeTextView.text =
            FormatDate.changeFormateFromDateTimeToTime(localStartTime) + " - " + FormatDate.changeFormateFromDateTimeToTime(
                localEndTime
            )
        setFunctionOnButton(holder, position)
    }

    private fun getAmenitiesHolder(holder:ViewHolder,i:Int,position: Int){
        when {
            i > 3 -> {
                SetDrawable.setDrawable(mContext.getString(R.string.more_amenity_value), holder.amenity3)
                holder.amenity3.text = mContext.getString(R.string.more_amenity_value)
                holder.amenity3.setTextColor(Color.parseColor("#0072BC"))
                holder.amenity3.visibility = View.VISIBLE
            }
            i == 3 -> {
                SetDrawable.setDrawable(dashboardItemList[position].amenities!![i], holder.amenity3)
                holder.amenity3.text = dashboardItemList[position].amenities!![3]
                holder.amenity3.setTextColor(Color.parseColor("#4F4F4F"))
                holder.amenity3.visibility = View.VISIBLE
            }
        }
        when (i) {
            0 -> {
                SetDrawable.setDrawable(dashboardItemList[position].amenities!![i], holder.amenity0)
                holder.amenity0.visibility = View.VISIBLE
            }
            1 -> {
                SetDrawable.setDrawable(dashboardItemList[position].amenities!![i], holder.amenity1)
                holder.amenity1.visibility = View.VISIBLE
            }
            2 -> {
                SetDrawable.setDrawable(dashboardItemList[position].amenities!![i], holder.amenity2)
                holder.amenity2.visibility = View.VISIBLE
            }
        }

    }

    /**
     * Disable the View of the amenities
     */
    private fun disableAmenitiesHolder(holder:ViewHolder){
        holder.amenity3.visibility = View.GONE
        holder.amenity0.visibility = View.GONE
        holder.amenity1.visibility = View.GONE
        holder.amenity2.visibility = View.GONE
    }

    /**
     * formate date
     */
    @SuppressLint("SimpleDateFormat")
    private fun formatDate(date: String): String {
        val simpleDateFormatInput = SimpleDateFormat("yyyy-MM-dd")
        val simpleDateFormatOutput = SimpleDateFormat("dd MMMM yyyy")
        return simpleDateFormatOutput.format(simpleDateFormatInput.parse(date.split("T")[0]))
    }


    /**
     * it will return number of items contains in recyclerview view
     */
    override fun getItemCount(): Int {
        return dashboardItemList.size
    }

    class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        var roomNameTextView: TextView = itemView.findViewById(R.id.conferenceRoomName)
        var fromTimeTextView: TextView = itemView.findViewById(R.id.from_time)
        var dateTextView: TextView = itemView.findViewById(R.id.date)
        var purposeTextView: TextView = itemView.findViewById(R.id.purpose)
        var cancelButton: TextView = itemView.findViewById(R.id.btnCancel)
        var showButton: ImageView = itemView.findViewById(R.id.btnshow)
        var editTextView: TextView = itemView.findViewById(R.id.editBtn)
        var attendeeTextView: TextView = itemView.findViewById(R.id.attendee_text_view)
        var actionLayout: LinearLayout = itemView.findViewById(R.id.action_button_linear_layout)
        var landingView: View = itemView.findViewById(R.id.view_landing)
        var aminityTitle: TextView = itemView.findViewById(R.id.aminities_text_view)
        var amenity0: TextView = itemView.findViewById(R.id.ani_1)
        var amenity1: TextView = itemView.findViewById(R.id.ani_2)
        var amenity2: TextView = itemView.findViewById(R.id.ani_3)
        var amenity3: TextView = itemView.findViewById(R.id.ani_4)
        var dashboard: Dashboard? = null
    }

    /**
     * set data to the fields of view
     */
    @SuppressLint("SetTextI18n")
    private fun setDataToFields(holder: ViewHolder, position: Int) {
        holder.dashboard = dashboardItemList[position]
        holder.roomNameTextView.text =
            "${dashboardItemList[position].roomName}, ${dashboardItemList[position].buildingName}"
        holder.purposeTextView.text = dashboardItemList[position].purpose
        holder.showButton.setOnClickListener {
            setMeetingMembers(position)
        }
    }

    /**
     * send employee List to the activity using interface which will display the list of employee names in the alert dialog
     */
    private fun setMeetingMembers(position: Int) {
        if (dashboardItemList[position].name != null)
            mShowMembers.showMembers(dashboardItemList[position].name!!, position)
        else
            mShowMembers.showMembers(emptyList(), position)

    }

    /**
     * if the booking is cancelled by HR than do nothing and set clickable property to false
     * if the booking is not cancelled and user wants to cancel it than allow user to cancel the booking
     */
    private fun setFunctionOnButton(
        holder: ViewHolder,
        position: Int
    ) {
        holder.cancelButton.setOnClickListener {
            if (holder.cancelButton.text != "Attendee") {
                mCancelBookingClickListener!!.onCLick(position)
            }
        }
    }

    private fun editActivity(position: Int) {
        val fromDate = dashboardItemList[position].fromTime!!.split("T")
        val toDate = dashboardItemList[position].toTime!!.split("T")
        val localStartTime = FormatTimeAccordingToZone.formatDateAsIndianStandardTime("${fromDate[0]} ${fromDate[1]}")
        val localEndTime = FormatTimeAccordingToZone.formatDateAsIndianStandardTime("${fromDate[0]} ${toDate[1]}")


        mIntentData.date = localStartTime.split(" ")[0]
        mIntentData.fromTime = localStartTime.split(" ")[1]
        mIntentData.toTime = localEndTime.split(" ")[1]
        mIntentData.purpose = dashboardItemList[position].purpose
        mIntentData.buildingName = dashboardItemList[position].buildingName
        mIntentData.roomName = dashboardItemList[position].roomName
        mIntentData.roomId = dashboardItemList[position].roomId
        mIntentData.bookingId = dashboardItemList[position].bookingId
        mIntentData.cCMail = dashboardItemList[position].cCMail
        mIntentData.name = dashboardItemList[position].name
        mEditBookingListener!!.editBooking(mIntentData)

    }


    /**
     * An interface which will be implemented by UserDashboardBookingActivity activity
     */
    interface CancelBtnClickListener {
        fun onCLick(position: Int)
    }

    /**
     * An interface which will be implemented by UserDashboardBookingActivity activity to pass employeeList to the activity
     */
    interface ShowMembersListener {
        fun showMembers(mEmployeeList: List<String>, position: Int)
    }

    interface EditBookingListener {
        fun editBooking(mGetIntentDataFromActvity: GetIntentDataFromActvity)
    }

    interface MoreAminitiesListner {
        fun moreAmenities(position: Int)
    }


}
