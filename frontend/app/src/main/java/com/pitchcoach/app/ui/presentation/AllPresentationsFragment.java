package com.pitchcoach.app.ui.presentation;

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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.pitchcoach.app.R;
import com.pitchcoach.app.data.AllPresentationsViewModel;
import com.pitchcoach.app.databinding.FragmentAllPresentationsBinding;
import com.pitchcoach.app.model.PresentationItem;
import com.pitchcoach.app.ui.adapter.PresentationAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AllPresentationsFragment extends Fragment {

    public static final String MODE_KEY = "mode";
    private static final String MODE_PRACTICE = "practice";

    private FragmentAllPresentationsBinding binding;
    private AllPresentationsViewModel viewModel;
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

        practiceMode = MODE_PRACTICE.equals(requireArguments().getString(MODE_KEY));

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
                com.pitchcoach.app.ui.util.UiUtils.showSnackbar(
                        binding.getRoot(), getString(R.string.snackbar_placeholder));
            }

            @Override
            public void onDelete(PresentationItem item) {
                confirmDelete(item);
            }
        });
        binding.presentationList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.presentationList.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(AllPresentationsViewModel.class);
        viewModel.init(requireContext().getApplicationContext());
        viewModel.getItems().observe(getViewLifecycleOwner(), items -> {
            updateFilterLabels(items);
            applyFilters(items);
        });

        binding.searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                query = s == null ? "" : s.toString();
                applyFilters(viewModel.getItems().getValue());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.filterAll.setOnCheckedChangeListener((button, checked) -> {
            if (checked) {
                filter = "all";
                applyFilters(viewModel.getItems().getValue());
            }
        });
        binding.filterUpcoming.setOnCheckedChangeListener((button, checked) -> {
            if (checked) {
                filter = "upcoming";
                applyFilters(viewModel.getItems().getValue());
            }
        });
        binding.filterPast.setOnCheckedChangeListener((button, checked) -> {
            if (checked) {
                filter = "past";
                applyFilters(viewModel.getItems().getValue());
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
                applyFilters(viewModel.getItems().getValue());
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
        int upcoming = 0;
        int past = 0;
        for (PresentationItem item : items) {
            if (item.past) {
                past++;
            } else {
                upcoming++;
            }
        }
        binding.filterAll.setText(getString(R.string.filter_all, items.size()));
        binding.filterUpcoming.setText(getString(R.string.filter_upcoming, upcoming));
        binding.filterPast.setText(getString(R.string.filter_past, past));
    }

    private void applyFilters(List<PresentationItem> items) {
        if (items == null || binding == null) {
            return;
        }
        String normalized = query.trim().toLowerCase();
        List<PresentationItem> visible = new ArrayList<>();
        for (PresentationItem item : items) {
            boolean matchesQuery = item.title.toLowerCase().contains(normalized);
            boolean matchesFilter = "all".equals(filter)
                    || ("upcoming".equals(filter) && !item.past)
                    || ("past".equals(filter) && item.past);
            if (matchesQuery && matchesFilter) {
                visible.add(item);
            }
        }
        if (sortIndex == 2) {
            Collections.sort(visible, Comparator.comparingDouble(
                    item -> -parseScore(item.score)));
        } else if (sortIndex == 1) {
            Collections.sort(visible, Comparator.comparingInt(item -> item.createdOrder));
        }
        adapter.submitList(visible);
        boolean empty = visible.isEmpty();
        binding.emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.presentationList.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private double parseScore(String score) {
        try {
            return Double.parseDouble(score);
        } catch (NumberFormatException exception) {
            return -1;
        }
    }

    private void confirmDelete(PresentationItem item) {
        new MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_PitchCoach_Dialog)
                .setTitle(R.string.delete_dialog_title)
                .setMessage(R.string.delete_dialog_message)
                .setPositiveButton(R.string.delete, (dialog, which) -> viewModel.delete(item.title))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
