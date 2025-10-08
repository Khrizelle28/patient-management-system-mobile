package com.example.patientmanagementsystemmobile.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    public MedicationAdapter(List<MedicationAlert> medicationList) {
        this.medicationList = medicationList;
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
        holder.bind(medication);
    }

    @Override
    public int getItemCount() {
        return medicationList.size();
    }

    public class MedicationViewHolder extends RecyclerView.ViewHolder {

        private TextView timeTextView;
        private TextView periodTextView;
        private TextView medicationNameTextView;
        private Switch medicationSwitch;

        public MedicationViewHolder(@NonNull View itemView) {
            super(itemView);
            timeTextView = itemView.findViewById(R.id.textViewTime);
            periodTextView = itemView.findViewById(R.id.textViewPeriod);
            medicationNameTextView = itemView.findViewById(R.id.textViewMedicationName);
            medicationSwitch = itemView.findViewById(R.id.switchMedication);
        }

        public void bind(MedicationAlert medication) {
            timeTextView.setText(medication.getTime());
            periodTextView.setText(medication.getPeriod());
            medicationNameTextView.setText(medication.getMedicationName());
            medicationSwitch.setChecked(medication.isEnabled());

            // Handle switch toggle
            medicationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    medication.setEnabled(isChecked);
                    // You can add additional logic here like saving to database or preferences
                }
            });
        }
    }
}