package org.fossasia.openevent.general.social

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_social_links.eventHostDetails
import kotlinx.android.synthetic.main.fragment_social_links.socialLinksRecycler
import kotlinx.android.synthetic.main.fragment_social_links.socialLinksCoordinatorLayout
import kotlinx.android.synthetic.main.fragment_social_links.view.progressBarSocial
import kotlinx.android.synthetic.main.fragment_social_links.view.socialLinksRecycler
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class SocialLinksFragment : Fragment() {
    private val socialLinksRecyclerAdapter: SocialLinksRecyclerAdapter = SocialLinksRecyclerAdapter()
    private val socialLinksViewModel by viewModel<SocialLinksViewModel>()
    private lateinit var rootView: View
    private var id: Long = -1
    private val EVENT_ID: String = "EVENT_ID"
    private lateinit var linearLayoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = this.arguments
        if (bundle != null) {
            id = bundle.getLong(EVENT_ID, -1)
        }

        socialLinksViewModel.socialLinks
            .nonNull()
            .observe(this, Observer {
                socialLinksRecyclerAdapter.addAll(it)
                handleVisibility(it)
                socialLinksRecyclerAdapter.notifyDataSetChanged()
                Timber.d("Fetched social-links of size %s", socialLinksRecyclerAdapter.itemCount)
            })

        socialLinksViewModel.error
            .nonNull()
            .observe(this, Observer {
                Snackbar.make(socialLinksCoordinatorLayout, it, Snackbar.LENGTH_LONG).show()
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_social_links, container, false)

        rootView.progressBarSocial.isIndeterminate = true

        linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        rootView.socialLinksRecycler.layoutManager = linearLayoutManager

        rootView.socialLinksRecycler.adapter = socialLinksRecyclerAdapter
        rootView.socialLinksRecycler.isNestedScrollingEnabled = false

        socialLinksViewModel.progress
            .nonNull()
            .observe(this, Observer {
                rootView.progressBarSocial.isVisible = it
            })

        socialLinksViewModel.loadSocialLinks(id)

        return rootView
    }

    private fun handleVisibility(socialLinks: List<SocialLink>) {
        eventHostDetails.isGone = socialLinks.isEmpty()
        socialLinksRecycler.isGone = socialLinks.isEmpty()
    }
}
