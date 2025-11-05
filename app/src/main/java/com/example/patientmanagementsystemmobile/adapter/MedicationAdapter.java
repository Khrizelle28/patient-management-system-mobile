package com.example.patientmanagementsystemmobile.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.patientmanagementsystemmobile.R;

import com.example.patientmanagementsystemmobile.models.MedicationAlert;

import java.util.List;

public class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.MedicationViewHolder> {

    private List<MedicationAlert> medicationList;
    private OnMedicationActionListener listener;

    public interface OnMedicationActionListener {
        void onEditClick(MedicationAlert medication, int position);
        void onDeleteClick(MedicationAlert medication, int position);
        void onToggleSwitch(MedicationAlert medication, boolean isEnabled);
    }

    public MedicationAdapter(List<MedicationAlert> medicationList) {
        this.medicationList = medicationList;
    }

    public void setOnMedicationActionListener(OnMedicationActionListener listener) {
        this.listener = listener;
    }

    public void updateList(List<MedicationAlert> newList) {
        this.medicationList = newList;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        medicationList.remove(position);
        notifyItemRemoved(position);
    }

    @NonNull
    @Override
    public MedicationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medication_alert, parent, false);
        return new MedicationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicationViewHolder holder, int position) {
        MedicationAlert medication = medicationList.get(position);
        holder.bind(medication, position);
    }

    @Override
    public int getItemCount() {
        return medicationList.size();
    }

    public class MedicationViewHolder extends RecyclerView.ViewHolder {

        private TextView timeTextView;
        private TextView periodTextView;
        private TextView medicationNameTextView;
        private TextView remarksTextView;
        private Switch medicationSwitch;
        private Button editButton;
        private Button deleteButton;

        public MedicationViewHolder(@NonNull View itemView) {
            super(itemView);
            timeTextView = itemView.findViewById(R.id.textViewTime);
            periodTextView = itemView.findViewById(R.id.textViewPeriod);
            medicationNameTextView = itemView.findViewById(R.id.textViewMedicationName);
            remarksTextView = itemView.findViewById(R.id.textViewRemarks);
            medicationSwitch = itemView.findViewById(R.id.switchMedication);
            editButton = itemView.findViewById(R.id.buttonEdit);
            deleteButton = itemView.findViewById(R.id.buttonDelete);
        }

        public void bind(MedicationAlert medication, int position) {
            timeTextView.setText(medication.getTime());
            periodTextView.setText(medication.getPeriod());
            medicationNameTextView.setText(medication.getMedicationName());
            medicationSwitch.setChecked(medication.isEnabled());

            // Handle remarks visibility
            if (medication.getRemarks() != null && !medication.getRemarks().isEmpty()) {
                remarksTextView.setText(medication.getRemarks());
                remarksTextView.setVisibility(View.VISIBLE);
            } else {
                remarksTextView.setVisibility(View.GONE);
            }

            // Handle switch toggle
            medicationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    medication.setEnabled(isChecked);
                    if (listener != null) {
                        listener.onToggleSwitch(medication, isChecked);
                    }
                }
            });

            // Handle edit button click
            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onEditClick(medication, position);
                    }
                }
            });

            // Handle delete button click
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onDeleteClick(medication, position);
                    }
                }
            });
        }
    }
}