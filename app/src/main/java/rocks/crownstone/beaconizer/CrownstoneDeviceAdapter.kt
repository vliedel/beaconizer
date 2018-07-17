package rocks.crownstone.beaconizer

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.device_item.view.*


class CrownstoneDeviceAdapter(val deviceList: ArrayList<CrownstoneDevice>): RecyclerView.Adapter<CrownstoneDeviceAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        //Log.d("CrownstoneDeviceAdapter", "onCreateViewHolder(): Create view holder")
        val v = LayoutInflater.from(parent?.context).inflate(R.layout.device_item, parent, false)
        return ViewHolder(v);
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        //Log.d("CrownstoneDeviceAdapter", "onBindViewHolder(): Add item at position: ${position}")
        holder?.bindItems(deviceList[position])
    }

    override fun getItemCount(): Int {
        return deviceList.size
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        fun bindItems(device: CrownstoneDevice) {
            itemView.deviceName.text = device.name
            itemView.deviceAddress.text = device.address
        }
    }

}