package com.example.poketouch

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView

class ControllerButtonsAdapter(
    private val options: MutableList<String>
    ) : RecyclerView.Adapter<ControllerButtonsAdapter.ButtonViewHolder>() {

    class ButtonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        public val button: Button

        init {
            button = view.findViewById(R.id.obButton)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ButtonViewHolder {
        return ButtonViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.options_button,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ButtonViewHolder, position: Int) {
        val curOption = options[position]
        holder.button.text = curOption
    }

    override fun getItemCount(): Int {
        return options.size
    }

    public fun clearOptions() {
        options.clear()
        notifyDataSetChanged()
    }

    public fun addOption(option: String) {
        options.add(option)
        notifyItemChanged(options.size - 1)
    }
}