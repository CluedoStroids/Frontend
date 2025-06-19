package at.aau.se2.cluedo

import android.app.Application
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import at.aau.se2.cluedo.viewmodels.CardAdapter
import com.example.myapplication.R
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class CardAdapterTests {

    private lateinit var adapter: CardAdapter
    private lateinit var parent: ViewGroup
    private lateinit var inflater: LayoutInflater
    private val testCards = listOf(R.drawable.card_wrench, R.drawable.hall)

    @Before
    fun setup() {
        adapter = CardAdapter(testCards)
        inflater = LayoutInflater.from(ApplicationProvider.getApplicationContext())
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        parent = FrameLayout(context)
    }

    @Test
    fun testEqualItemSize() {
        assertEquals(testCards.size, adapter.itemCount)
    }

    /*
    @Test
    fun testOnCreateViewHolder() {
        val viewHolder = adapter.onCreateViewHolder(parent, 0)
        assertNotNull(viewHolder)
        assertNotNull(viewHolder.imageView)
    }

    @Test
    fun testOnBindViewHolder() {
        val viewHolder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(viewHolder, 0)
        val imageView = viewHolder.imageView
        val tag = imageView.drawable.constantState
        assertNotNull("Image drawable should not be null", tag)
    }
     */

}