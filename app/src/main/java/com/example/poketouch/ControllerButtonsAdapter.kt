package com.example.poketouch

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView

class ControllerButtonsAdapter(
    private val options: MutableList<ControllerButtonOption>
    ) : RecyclerView.Adapter<ControllerButtonsAdapter.ButtonViewHolder>() {

    class ButtonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        public val button: Button

        init {
            button = view.findViewById(R.id.obButton)
        }
    }

    public data class ControllerButtonOption(
        public val text: String,
        public val callback: (Int) -> Unit
    )

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
        holder.button.text = curOption.text
        holder.button.setOnClickListener {
            curOption.callback(position)
        }
    }

    override fun getItemCount(): Int {
        return options.size
    }

    public fun clearOptions() {
        options.clear()
        notifyDataSetChanged()
    }

    public fun addOption(text: String, cb: (Int) -> Unit) {
        options.add(ControllerButtonOption(text, cb))
        notifyItemChanged(options.size - 1)
    }
}