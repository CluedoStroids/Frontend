package at.aau.se2.cluedo.viewmodels

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R


class CardAdapter(private val cards: List<Int>,
                  private val onSelectionChanged: ((Int?) -> Unit)? = null ) :
    RecyclerView.Adapter<CardAdapter.CardViewHolder>() {

    private var selectedPosition: Int? = null

    inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.card_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card_horizontal, parent, false)
        return CardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val resId = cards[position]
        holder.imageView.setImageResource(resId)

        // Highlight selected card (e.g., faded effect)
        holder.imageView.alpha = if (position == selectedPosition) 0.5f else 1.0f

        holder.itemView.setOnClickListener {
            if (selectedPosition == position) {
                selectedPosition = null // Deselect
            } else {
                selectedPosition = position
            }
            onSelectionChanged?.invoke(selectedPosition?.let { cards[it] })
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int = cards.size
}