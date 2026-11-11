package com.pitchcoach.app.view.presentation;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.pitchcoach.app.R;
import com.pitchcoach.app.controller.PresentationController;
import com.pitchcoach.app.databinding.FragmentAllPresentationsBinding;
import com.pitchcoach.app.model.PresentationItem;
import com.pitchcoach.app.view.adapter.PresentationAdapter;
import com.pitchcoach.app.view.util.UiUtils;

import java.util.List;

public class AllPresentationsFragment extends Fragment {

    public static final String MODE_KEY = "mode";
    private static final String MODE_PRACTICE = "practice";

    private FragmentAllPresentationsBinding binding;
    private final PresentationController controller = new PresentationController();
    private List<PresentationItem> currentItems;
    private PresentationAdapter adapter;
    private String query = "";
    private String filter = "all";
    private int sortIndex = 0;
    private boolean practiceMode = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAllPresentationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        practiceMode = getArguments() != null
                && MODE_PRACTICE.equals(getArguments().getString(MODE_KEY));

        binding.appBar.appBarTitle.setText(R.string.all_presentations_title);
        binding.appBar.backButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack());

        adapter = new PresentationAdapter(new PresentationAdapter.Listener() {
            @Override
            public void onOpen(PresentationItem item) {
                if (practiceMode) {
                    NavHostFragment.findNavController(AllPresentationsFragment.this)
                            .navigate(R.id.action_all_to_record);
                } else {
                    NavHostFragment.findNavController(AllPresentationsFragment.this)
                            .navigate(R.id.action_all_to_detail);
                }
            }

            @Override
            public void onEdit(PresentationItem item) {
                UiUtils.showSnackbar(
                        binding.getRoot(), getString(R.string.snackbar_placeholder));
            }

            @Override
            public void onDelete(PresentationItem item) {
                confirmDelete(item);
            }
        });
        binding.presentationList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.presentationList.setAdapter(adapter);

        currentItems = controller.getPresentations(requireContext());
        updateFilterLabels(currentItems);
        applyFilters(currentItems);

        binding.searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                query = s == null ? "" : s.toString();
                applyFilters(currentItems);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.filterAll.setOnCheckedChangeListener((button, checked) -> {
            if (checked) {
                filter = "all";
                applyFilters(currentItems);
            }
        });
        binding.filterUpcoming.setOnCheckedChangeListener((button, checked) -> {
            if (checked) {
                filter = "upcoming";
                applyFilters(currentItems);
            }
        });
        binding.filterPast.setOnCheckedChangeListener((button, checked) -> {
            if (checked) {
                filter = "past";
                applyFilters(currentItems);
            }
        });

        ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.filter_sort_options, android.R.layout.simple_spinner_item);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.sortSpinner.setAdapter(sortAdapter);
        binding.sortSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view,
                                       int position, long id) {
                sortIndex = position;
                applyFilters(currentItems);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        binding.clearFiltersButton.setOnClickListener(v -> {
            binding.searchInput.setText("");
            binding.filterAll.setChecked(true);
            query = "";
            filter = "all";
        });

        binding.newSessionButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_all_to_create));
    }

    private void updateFilterLabels(List<PresentationItem> items) {
        if (items == null) {
            return;
        }
        int upcoming = controller.countUpcoming(items);
        int past = controller.countPast(items);
        binding.filterAll.setText(getString(R.string.filter_all, items.size()));
        binding.filterUpcoming.setText(getString(R.string.filter_upcoming, upcoming));
        binding.filterPast.setText(getString(R.string.filter_past, past));
    }

    private void applyFilters(List<PresentationItem> items) {
        if (items == null || binding == null) {
            return;
        }
        List<PresentationItem> visible = controller.filterAndSort(items, query, filter, sortIndex);
        adapter.submitList(visible);
        boolean empty = visible.isEmpty();
        binding.emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.presentationList.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void confirmDelete(PresentationItem item) {
        new MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_PitchCoach_Dialog)
                .setTitle(R.string.delete_dialog_title)
                .setMessage(R.string.delete_dialog_message)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    currentItems = controller.deletePresentation(requireContext(), item.title);
                    updateFilterLabels(currentItems);
                    applyFilters(currentItems);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
