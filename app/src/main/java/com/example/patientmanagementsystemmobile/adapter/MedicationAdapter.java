package com.example.patientmanagementsystemmobile.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.patientmanagementsystemmobile.R;
import com.example.patientmanagementsystemmobile.models.MedicationAlert;
import com.example.patientmanagementsystemmobile.utils.MedicationIntakeTracker;
import java.util.List;

public class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.MedicationViewHolder> {
    private List<MedicationAlert> medicationList;
    private OnMedicationActionListener listener;
    private Context context;
    private MedicationIntakeTracker intakeTracker;

    public interface OnMedicationActionListener {
        void onEditClick(MedicationAlert medication, int position);
        void onDeleteClick(MedicationAlert medication, int position);
        void onToggleSwitch(MedicationAlert medication, boolean isEnabled);
        void onMarkAsTaken(MedicationAlert medication, int position);
    }

    public MedicationAdapter(List<MedicationAlert> medicationList, Context context) {
        this.medicationList = medicationList;
        this.context = context;
        this.intakeTracker = new MedicationIntakeTracker(context);
    }

    public void setOnMedicationActionListener(OnMedicationActionListener listener) {
        this.listener = listener;
    }

    public void updateList(List<MedicationAlert> newList) {
        this.medicationList = newList;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < medicationList.size()) {
            medicationList.remove(position);
            notifyItemRemoved(position);
            if (medicationList.isEmpty()) {
                notifyDataSetChanged();
            }
        }
    }

    @NonNull
    @Override
    public MedicationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medication_alert, parent, false);
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
        private TextView timeTextView, periodTextView, medicationNameTextView, remarksTextView, durationTextView, progressTextView;
        private TextView dayMonday, dayTuesday, dayWednesday, dayThursday, dayFriday, daySaturday, daySunday;
        private Switch medicationSwitch;
        private Button editButton, deleteButton, markTakenButton;
        private ProgressBar progressBar;

        public MedicationViewHolder(@NonNull View itemView) {
            super(itemView);
            timeTextView = itemView.findViewById(R.id.textViewTime);
            periodTextView = itemView.findViewById(R.id.textViewPeriod);
            medicationNameTextView = itemView.findViewById(R.id.textViewMedicationName);
            remarksTextView = itemView.findViewById(R.id.textViewRemarks);
            durationTextView = itemView.findViewById(R.id.textViewDuration);
            medicationSwitch = itemView.findViewById(R.id.switchMedication);
            editButton = itemView.findViewById(R.id.buttonEdit);
            deleteButton = itemView.findViewById(R.id.buttonDelete);
            markTakenButton = itemView.findViewById(R.id.buttonMarkTaken);
            progressBar = itemView.findViewById(R.id.progressBarIntake);
            progressTextView = itemView.findViewById(R.id.textViewProgress);
            dayMonday = itemView.findViewById(R.id.dayMonday);
            dayTuesday = itemView.findViewById(R.id.dayTuesday);
            dayWednesday = itemView.findViewById(R.id.dayWednesday);
            dayThursday = itemView.findViewById(R.id.dayThursday);
            dayFriday = itemView.findViewById(R.id.dayFriday);
            daySaturday = itemView.findViewById(R.id.daySaturday);
            daySunday = itemView.findViewById(R.id.daySunday);
        }

        public void bind(MedicationAlert medication, int position) {
            timeTextView.setText(medication.getTime());
            periodTextView.setText(medication.getPeriod());
            medicationNameTextView.setText(medication.getMedicationName());
            medicationSwitch.setChecked(medication.isEnabled());
            if (medication.getRemarks() != null && !medication.getRemarks().isEmpty()) {
                remarksTextView.setText(medication.getRemarks());
                remarksTextView.setVisibility(View.VISIBLE);
            } else {
                remarksTextView.setVisibility(View.GONE);
            }
            updateProgress(medication);
            displaySelectedDays(medication.getSelectedDays());
            displayDurationInfo(medication.getSelectedDays(), medication.getDurationDays());
            medicationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    medication.setEnabled(isChecked);
                    if (listener != null) listener.onToggleSwitch(medication, isChecked);
                }
            });
            markTakenButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!intakeTracker.wasTakenToday(medication.getId())) {
                        intakeTracker.markAsTaken(medication.getId());
                        updateProgress(medication);
                        if (listener != null) listener.onMarkAsTaken(medication, position);
                    }
                }
            });
            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) listener.onEditClick(medication, position);
                }
            });
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) listener.onDeleteClick(medication, position);
                }
            });
        }

        private void updateProgress(MedicationAlert medication) {
            boolean takenToday = intakeTracker.wasTakenToday(medication.getId());
            boolean hasExpired = intakeTracker.hasExpired(medication.getStartDate(), medication.getDurationDays());
            boolean isTodaySelected = isTodaySelectedDay(medication.getSelectedDays());
            int totalSelectedDays = countSelectedDays(medication.getSelectedDays());
            int daysCompletedThisWeek = intakeTracker.getWeeklyIntakeCount(medication.getId(), medication.getSelectedDays());
            progressTextView.setText(daysCompletedThisWeek + "/" + totalSelectedDays + " doses");
            int progressPercent = totalSelectedDays > 0 ? (daysCompletedThisWeek * 100) / totalSelectedDays : 0;
            progressBar.setProgress(progressPercent);
            boolean isLate = isMoreThan1MinuteLate(medication.getTime(), medication.getPeriod());
            if (!takenToday && isLate && isTodaySelected && !hasExpired) {
                progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(0xFFE74C3C));
            } else {
                progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(0xFF27AE60));
            }
            if (hasExpired) {
                markTakenButton.setText("Expired");
                markTakenButton.setEnabled(false);
                markTakenButton.setAlpha(0.5f);
            } else if (!isTodaySelected) {
                markTakenButton.setText("Not Today");
                markTakenButton.setEnabled(false);
                markTakenButton.setAlpha(0.5f);
            } else if (takenToday) {
                markTakenButton.setText("Taken");
                markTakenButton.setEnabled(false);
                markTakenButton.setAlpha(0.5f);
            } else {
                markTakenButton.setText("Mark as Taken");
                markTakenButton.setEnabled(true);
                markTakenButton.setAlpha(1.0f);
            }
        }

        private boolean isTodaySelectedDay(String selectedDays) {
            if (selectedDays == null || selectedDays.isEmpty()) return true;
            java.util.Calendar cal = java.util.Calendar.getInstance();
            int dayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK);
            int todayNum = (dayOfWeek == java.util.Calendar.SUNDAY) ? 7 : dayOfWeek - 1;
            String[] days = selectedDays.split(",");
            for (String d : days) {
                try {
                    if (Integer.parseInt(d.trim()) == todayNum) return true;
                } catch (NumberFormatException e) {}
            }
            return false;
        }

        private int countSelectedDays(String selectedDays) {
            if (selectedDays == null || selectedDays.isEmpty()) return 7;
            return selectedDays.split(",").length;
        }

        private void displaySelectedDays(String selectedDays) {
            TextView[] dayViews = {dayMonday, dayTuesday, dayWednesday, dayThursday, dayFriday, daySaturday, daySunday};
            for (TextView dayView : dayViews) dayView.setVisibility(View.GONE);
            if (selectedDays != null && !selectedDays.isEmpty()) {
                String[] days = selectedDays.split(",");
                for (String dayStr : days) {
                    try {
                        int day = Integer.parseInt(dayStr.trim());
                        if (day >= 1 && day <= 7) {
                            TextView dayView = dayViews[day - 1];
                            dayView.setVisibility(View.VISIBLE);
                            dayView.setBackgroundResource(R.drawable.day_button_selected);
                            dayView.setTextColor(0xFFFFFFFF);
                        }
                    } catch (NumberFormatException e) {}
                }
            }
        }

        private void displayDurationInfo(String selectedDays, int durationDays) {
            String[] dayNames = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
            StringBuilder durationText = new StringBuilder();
            if (selectedDays == null || selectedDays.isEmpty()) {
                durationText.append("No days selected");
            } else {
                String[] days = selectedDays.split(",");
                if (days.length == 7) {
                    durationText.append("Everyday");
                } else {
                    for (int i = 0; i < days.length; i++) {
                        try {
                            int day = Integer.parseInt(days[i].trim());
                            if (day >= 1 && day <= 7) {
                                if (i > 0) durationText.append(", ");
                                durationText.append(dayNames[day - 1]);
                            }
                        } catch (NumberFormatException e) {}
                    }
                }
                if (durationDays > 0) durationText.append(" for ").append(durationDays).append(" days");
            }
            durationTextView.setText(durationText.toString());
        }

        private boolean isMoreThan1MinuteLate(String time, String period) {
            try {
                String[] timeParts = time.split(":");
                int schedHour = Integer.parseInt(timeParts[0]);
                int schedMinute = Integer.parseInt(timeParts[1]);
                if (period.equals("PM") && schedHour != 12) schedHour += 12;
                else if (period.equals("AM") && schedHour == 12) schedHour = 0;
                java.util.Calendar now = java.util.Calendar.getInstance();
                int currentHour = now.get(java.util.Calendar.HOUR_OF_DAY);
                int currentMinute = now.get(java.util.Calendar.MINUTE);
                int scheduledTimeInMinutes = schedHour * 60 + schedMinute;
                int currentTimeInMinutes = currentHour * 60 + currentMinute;
                int minutesDifference = currentTimeInMinutes - scheduledTimeInMinutes;
                return minutesDifference > 1;
            } catch (Exception e) {
                return false;
            }
        }
    }
}
