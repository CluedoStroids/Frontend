package at.aau.se2.cluedo.viewmodels

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import at.aau.se2.cluedo.data.models.BasicCard
import com.example.myapplication.R


class CardAdapter(private val cards: List<BasicCard>?,
                  private val onSelectionChanged: ((String?) -> Unit)? = null ) :
    RecyclerView.Adapter<CardAdapter.CardViewHolder>() {

    private var selectedCard: String? = null

    inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.card_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card_horizontal, parent, false)
        return CardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val cardIds = BasicCard.getCardIDs(cards)
        val resId = cardIds[position]
        holder.imageView.setImageResource(resId)

        // Highlight selected card (e.g., faded effect)
        holder.imageView.alpha = if (cards?.get(position)?.cardName == selectedCard) 0.5f else 1.0f

        holder.itemView.setOnClickListener {
            if (selectedCard == cards?.get(position)?.cardName) {
                selectedCard = null // Deselect
            } else {
                selectedCard = cards?.get(position)?.cardName
            }
            onSelectionChanged?.invoke(selectedCard)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int = cards?.size ?: 0
}