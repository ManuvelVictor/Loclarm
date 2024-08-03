package com.victor.loclarm.ui.alarms

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.victor.loclarm.databinding.AlarmItemBinding
import com.victor.loclarm.db.model.Alarm

class AlarmAdapter(private val onAlarmUpdate: (Alarm) -> Unit, private val onAlarmDelete: (Alarm) -> Unit) : RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {

    private var alarms = emptyList<Alarm>()

    inner class AlarmViewHolder(private val binding: AlarmItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(alarm: Alarm) {
            binding.alarmName.text = alarm.alarmName
            binding.alarmTime.text = alarm.id.toString()
            binding.alarmStatus.isChecked = alarm.isActive
            "${alarm.radius}m".also { binding.alarmDistance.text = it }

            binding.alarmStatus.setOnClickListener {
                alarm.isActive = !alarm.isActive
                onAlarmUpdate(alarm)
            }

            binding.deleteButton.setOnClickListener {
                onAlarmDelete(alarm)
            }

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
