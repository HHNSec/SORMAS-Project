package de.symeda.sormas.app.event.eventparticipant.read;

import java.util.Date;
import java.util.List;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import de.symeda.sormas.app.BaseReadFragment;
import de.symeda.sormas.app.R;
import de.symeda.sormas.app.backend.event.EventParticipant;
import de.symeda.sormas.app.backend.vaccination.Vaccination;
import de.symeda.sormas.app.core.adapter.databinding.OnListItemClickListener;
import de.symeda.sormas.app.databinding.FragmentFormListLayoutBinding;
import de.symeda.sormas.app.immunization.reducedVaccination.VaccinationReducedListAdapter;
import de.symeda.sormas.app.immunization.vaccination.VaccinationListViewModel;
import de.symeda.sormas.app.immunization.vaccination.VaccinationReadActivity;

public class EventParticipantReadVaccinationListFragment extends BaseReadFragment<FragmentFormListLayoutBinding, List<Vaccination>, EventParticipant>
	implements OnListItemClickListener {

	private VaccinationReducedListAdapter adapter;

	public static EventParticipantReadVaccinationListFragment newInstance(EventParticipant activityRootData) {
		return newInstance(EventParticipantReadVaccinationListFragment.class, null, activityRootData);
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		((EventParticipantReadActivity) getActivity()).showPreloader();

		Date eventStartDate = getActivityRootData().getEvent().getStartDate();
		Date eventEndDate = getActivityRootData().getEvent().getEndDate();
		Date eventReportDate = getActivityRootData().getEvent().getReportDateTime();
		Date vaccinationListGrayoutDate = eventStartDate != null ? eventStartDate : eventEndDate != null ? eventEndDate : eventReportDate;
		adapter = new VaccinationReducedListAdapter(vaccinationListGrayoutDate);

		VaccinationListViewModel model = ViewModelProviders.of(this).get(VaccinationListViewModel.class);
		model.initializeViewModel(getActivityRootData());
		model.getVaccinations().observe(this, vaccinations -> {
			adapter.submitList(vaccinations);
			((EventParticipantReadActivity) getActivity()).hidePreloader();
			updateEmptyListHint(vaccinations);
		});
	}

	@Override
	public final View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
		adapter.setOnListItemClickListener(this);
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	protected void prepareFragmentData(Bundle savedInstanceState) {

	}

	@Override
	protected void onLayoutBinding(FragmentFormListLayoutBinding contentBinding) {
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
		contentBinding.recyclerViewForList.setLayoutManager(linearLayoutManager);
		contentBinding.recyclerViewForList.setAdapter(adapter);
	}

	@Override
	protected String getSubHeadingTitle() {
		Resources r = getResources();
		return r.getString(R.string.caption_event_participant_vaccinations);
	}

	@Override
	public int getReadLayout() {
		return R.layout.fragment_form_list_layout;
	}

	@Override
	public int getRootReadLayout() {
		return R.layout.fragment_root_list_form_layout;
	}

	@Override
	public List<Vaccination> getPrimaryData() {
		throw new UnsupportedOperationException("Sub list fragments don't hold their data");
	}

	@Override
	public void onListItemClick(View view, int position, Object item) {
		Vaccination vaccination = (Vaccination) item;
		VaccinationReadActivity.startActivity(getActivity(), vaccination.getUuid());
	}
}
