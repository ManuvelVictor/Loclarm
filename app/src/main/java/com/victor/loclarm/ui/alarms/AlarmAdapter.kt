package com.victor.loclarm.ui.alarms

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.victor.loclarm.databinding.AlarmItemBinding
import com.victor.loclarm.db.Alarm

class AlarmAdapter : RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {

    private var alarms = emptyList<Alarm>()

    inner class AlarmViewHolder(private val binding: AlarmItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(alarm: Alarm) {
            binding.alarmTime.text = alarm.id.toString()
            binding.alarmStatus.isChecked = alarm.isActive
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val binding = AlarmItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlarmViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val currentItem = alarms[position]
        holder.bind(currentItem)
    }

    override fun getItemCount() = alarms.size

    fun setData(alarms: List<Alarm>) {
        this.alarms = alarms
        notifyDataSetChanged()
    }
}
