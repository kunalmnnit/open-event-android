package org.fossasia.openevent.general.order

import android.content.Intent
import android.os.Bundle
import android.provider.CalendarContract
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.navigation.Navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_order.view.orderCoordinatorLayout
import kotlinx.android.synthetic.main.fragment_order.view.add
import kotlinx.android.synthetic.main.fragment_order.view.name
import kotlinx.android.synthetic.main.fragment_order.view.share
import kotlinx.android.synthetic.main.fragment_order.view.time
import kotlinx.android.synthetic.main.fragment_order.view.view
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventUtils
import org.fossasia.openevent.general.ticket.EVENT_ID
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.fossasia.openevent.general.utils.stripHtml
import org.koin.androidx.viewmodel.ext.android.viewModel

class OrderCompletedFragment : Fragment() {

    private lateinit var rootView: View
    private lateinit var eventShare: Event
    private var id: Long = -1
    private val orderCompletedViewModel by viewModel<OrderCompletedViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = this.arguments
        if (bundle != null) {
            id = bundle.getLong(EVENT_ID, -1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_order, container, false)
        val activity = activity as? AppCompatActivity
        activity?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity?.supportActionBar?.title = ""
        setHasOptionsMenu(true)

        orderCompletedViewModel.loadEvent(id)
        orderCompletedViewModel.event
            .nonNull()
            .observe(this, Observer {
                loadEventDetails(it)
                eventShare = it
            })

        orderCompletedViewModel.message
            .nonNull()
            .observe(this, Observer {
                Snackbar.make(rootView.orderCoordinatorLayout, it, Snackbar.LENGTH_LONG).show()
            })

        rootView.add.setOnClickListener {
            startCalendar(eventShare)
        }

        rootView.view.setOnClickListener {
            openTicketDetails()
        }

        rootView.share.setOnClickListener {
            shareEvent(eventShare)
        }

        return rootView
    }

    private fun loadEventDetails(event: Event) {
        val dateString = StringBuilder()
        val startsAt = EventUtils.getLocalizedDateTime(event.startsAt)

        rootView.name.text = event.name
        rootView.time.text = dateString.append(EventUtils.getFormattedDateShort(startsAt))
                .append(" • ")
                .append(EventUtils.getFormattedTime(startsAt))
                .append(" ")
                .append(EventUtils.getFormattedTimeZone(startsAt))
    }

    private fun startCalendar(event: Event) {
        val intent = Intent(Intent.ACTION_INSERT)
        intent.type = "vnd.android.cursor.item/event"
        intent.putExtra(CalendarContract.Events.TITLE, event.name)
        intent.putExtra(CalendarContract.Events.DESCRIPTION, event.description?.stripHtml())
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, EventUtils.getTimeInMilliSeconds(event.startsAt))
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, EventUtils.getTimeInMilliSeconds(event.endsAt))
        startActivity(intent)
    }

    private fun shareEvent(event: Event) {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, EventUtils.getSharableInfo(event))
        sendIntent.type = "text/plain"
        startActivity(Intent.createChooser(sendIntent, "Share Event Details"))
    }

    private fun redirectToEventsFragment() {
        findNavController(rootView).popBackStack(R.id.eventsFragment, false)
    }

    private fun openEventDetails() {
        findNavController(rootView).popBackStack(R.id.eventDetailsFragment, false)
    }

    private fun openTicketDetails() {
        val navOptions = NavOptions.Builder().setPopUpTo(R.id.eventsFragment, false).build()
        Navigation.findNavController(rootView).navigate(R.id.orderUnderUserFragment, null, navOptions)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.order_completed, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                openEventDetails()
                true
            }
            R.id.tick -> {
                redirectToEventsFragment()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
