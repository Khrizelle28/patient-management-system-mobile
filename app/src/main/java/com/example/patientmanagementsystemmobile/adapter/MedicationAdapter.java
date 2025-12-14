package com.example.patientmanagementsystemmobile.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.patientmanagementsystemmobile.R;
import com.example.patientmanagementsystemmobile.models.MedicationAlert;
import com.example.patientmanagementsystemmobile.models.MedicationAlarmDisplayItem;
import com.example.patientmanagementsystemmobile.utils.MedicationIntakeTracker;
import java.util.List;

public class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.MedicationViewHolder> {
    private List<MedicationAlarmDisplayItem> displayItemList;
    private OnMedicationActionListener listener;
    private Context context;
    private MedicationIntakeTracker intakeTracker;

    public interface OnMedicationActionListener {
        void onEditClick(MedicationAlert medication, int position);
        void onDeleteClick(MedicationAlert medication, int position);
        void onToggleSwitch(MedicationAlert medication, boolean isEnabled);
        void onMarkAsTaken(MedicationAlert medication, int position);
    }

    public MedicationAdapter(List<MedicationAlarmDisplayItem> displayItemList, Context context) {
        this.displayItemList = displayItemList;
        this.context = context;
        this.intakeTracker = new MedicationIntakeTracker(context);
    }

    public void setOnMedicationActionListener(OnMedicationActionListener listener) {
        this.listener = listener;
    }

    public void updateList(List<MedicationAlarmDisplayItem> newList) {
        this.displayItemList = newList;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < displayItemList.size()) {
            displayItemList.remove(position);
            notifyItemRemoved(position);
            if (displayItemList.isEmpty()) {
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
        MedicationAlarmDisplayItem displayItem = displayItemList.get(position);
        holder.bind(displayItem, position);
    }

    @Override
    public int getItemCount() {
        return displayItemList.size();
    }

    public class MedicationViewHolder extends RecyclerView.ViewHolder {
        private TextView timeTextView, periodTextView, medicationNameTextView, remarksTextView, durationTextView, progressTextView;
        private TextView dayMonday, dayTuesday, dayWednesday, dayThursday, dayFriday, daySaturday, daySunday;
        private TextView textViewDay;
        private Switch medicationSwitch;
        private Button editButton, deleteButton, markTakenButton;
        private LinearLayout segmentedProgressContainer;

        public MedicationViewHolder(@NonNull View itemView) {
            super(itemView);
            timeTextView = itemView.findViewById(R.id.textViewTime);
            textViewDay = itemView.findViewById(R.id.textViewDay);
            periodTextView = itemView.findViewById(R.id.textViewPeriod);
            medicationNameTextView = itemView.findViewById(R.id.textViewMedicationName);
            remarksTextView = itemView.findViewById(R.id.textViewRemarks);
            durationTextView = itemView.findViewById(R.id.textViewDuration);
            medicationSwitch = itemView.findViewById(R.id.switchMedication);
            editButton = itemView.findViewById(R.id.buttonEdit);
            deleteButton = itemView.findViewById(R.id.buttonDelete);
            markTakenButton = itemView.findViewById(R.id.buttonMarkTaken);
            segmentedProgressContainer = itemView.findViewById(R.id.segmentedProgressContainer);
            progressTextView = itemView.findViewById(R.id.textViewProgress);
            dayMonday = itemView.findViewById(R.id.dayMonday);
            dayTuesday = itemView.findViewById(R.id.dayTuesday);
            dayWednesday = itemView.findViewById(R.id.dayWednesday);
            dayThursday = itemView.findViewById(R.id.dayThursday);
            dayFriday = itemView.findViewById(R.id.dayFriday);
            daySaturday = itemView.findViewById(R.id.daySaturday);
            daySunday = itemView.findViewById(R.id.daySunday);
        }

        public void bind(MedicationAlarmDisplayItem displayItem, int position) {
            MedicationAlert medication = displayItem.getMedication();
            int alarmIndex = displayItem.getAlarmIndex();
            MedicationAlert.AlarmTime alarmTime = displayItem.getAlarmTime();

            // Display time
            timeTextView.setText(alarmTime.getTime());
            periodTextView.setText(alarmTime.getPeriod());

            // Display day name for prescribed pieces medications
            if (displayItem.isPrescribedPieces() && displayItem.getCalculatedDayName() != null && !displayItem.getCalculatedDayName().isEmpty()) {
                textViewDay.setText(displayItem.getCalculatedDayName());
                textViewDay.setVisibility(View.VISIBLE);
            } else {
                textViewDay.setVisibility(View.GONE);
            }

            medicationNameTextView.setText(medication.getMedicationName());
            medicationSwitch.setChecked(medication.isEnabled());
            if (medication.getRemarks() != null && !medication.getRemarks().isEmpty()) {
                remarksTextView.setText(medication.getRemarks());
                remarksTextView.setVisibility(View.VISIBLE);
            } else {
                remarksTextView.setVisibility(View.GONE);
            }
            updateProgress(medication, alarmIndex);
            displaySelectedDays(medication);
            displayDurationInfo(medication);
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
                    // For prescribed pieces, check if this specific alarm has been taken
                    boolean alreadyTaken = displayItem.isPrescribedPieces()
                        ? intakeTracker.wasTakenToday(medication.getId(), alarmIndex)
                        : intakeTracker.wasTakenToday(medication.getId());

                    if (!alreadyTaken) {
                        if (displayItem.isPrescribedPieces()) {
                            MedicationIntakeTracker.markAsTaken(context, medication.getId(), alarmIndex);
                        } else {
                            intakeTracker.markAsTaken(medication.getId());
                        }
                        updateProgress(medication, alarmIndex);
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

        private void updateProgress(MedicationAlert medication, int alarmIndex) {
            boolean hasExpired = intakeTracker.hasExpired(medication.getStartDate(), medication.getDurationDays());

            // Check if this is a prescribed pieces medication
            boolean isPrescribedPieces = medication.getPrescribedPieces() > 0;

            int totalDoses;
            int completedDoses;
            boolean isTodaySelected;
            boolean thisAlarmTaken;

            if (isPrescribedPieces) {
                // Prescribed pieces: total = prescribed pieces, count all taken doses
                totalDoses = medication.getPrescribedPieces();
                completedDoses = countTotalIntake(medication.getId(), totalDoses);
                // For prescribed pieces, medication is always "available" (not day-specific)
                isTodaySelected = true;
                // Check if THIS specific alarm has been taken
                thisAlarmTaken = intakeTracker.wasTakenToday(medication.getId(), alarmIndex);
            } else {
                // Legacy weekly recurring: use old logic
                isTodaySelected = isTodaySelectedDay(medication.getSelectedDays());
                totalDoses = calculateTotalDoses(medication.getSelectedDays(), medication.getAlarmTimes().size());
                completedDoses = intakeTracker.getWeeklyIntakeCount(medication.getId(), medication.getSelectedDays());
                thisAlarmTaken = intakeTracker.wasTakenToday(medication.getId());
            }

            progressTextView.setText(completedDoses + "/" + totalDoses + " doses");

            // Get dose statuses (0=not taken, 1=green/on time, 2=red/late)
            int[] doseStatuses = intakeTracker.getWeeklyDoseStatuses(
                medication.getId(),
                medication.getSelectedDays(),
                medication.getAlarmTimes()
            );

            // Create segmented progress bar
            createSegmentedProgressBar(doseStatuses, totalDoses);

            // Determine button state based on THIS specific alarm
            if (hasExpired || (isPrescribedPieces && completedDoses >= totalDoses)) {
                markTakenButton.setText("Completed");
                markTakenButton.setEnabled(false);
                markTakenButton.setAlpha(0.5f);
            } else if (!isPrescribedPieces && !isTodaySelected) {
                markTakenButton.setText("Not Today");
                markTakenButton.setEnabled(false);
                markTakenButton.setAlpha(0.5f);
            } else if (thisAlarmTaken) {
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

        private int calculateTotalDoses(String selectedDays, int timesPerDay) {
            int numDays = countSelectedDays(selectedDays);
            int doses = timesPerDay > 0 ? timesPerDay : 1;
            return numDays * doses;
        }

        /**
         * Count total intake for prescribed pieces medications
         */
        private int countTotalIntake(int medicationId, int totalDoses) {
            int count = 0;
            String today = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                    .format(new java.util.Date());

            // Count how many doses have been taken across all alarm indices
            for (int alarmIdx = 0; alarmIdx < totalDoses; alarmIdx++) {
                // Check if this alarm index was taken on any day
                // For now, just check today (can be expanded to check all days since start)
                String key = "taken_" + today + "_" + medicationId + "_" + alarmIdx;
                android.content.SharedPreferences prefs = context.getSharedPreferences(
                        "medication_intake", android.content.Context.MODE_PRIVATE);
                if (prefs.getBoolean(key, false)) {
                    count++;
                }
            }
            return count;
        }

        private void displaySelectedDays(MedicationAlert medication) {
            TextView[] dayViews = {dayMonday, dayTuesday, dayWednesday, dayThursday, dayFriday, daySaturday, daySunday};

            // Check if this is a prescribed pieces medication
            if (medication.getPrescribedPieces() > 0 && medication.getStartDay() != null) {
                // Hide all day buttons for prescribed pieces (or show only start day)
                for (TextView dayView : dayViews) dayView.setVisibility(View.GONE);
                return;
            }

            // Legacy: show selected days
            for (TextView dayView : dayViews) dayView.setVisibility(View.GONE);
            String selectedDays = medication.getSelectedDays();
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

        private void displayDurationInfo(MedicationAlert medication) {
            StringBuilder durationText = new StringBuilder();

            // Check if this is a prescribed pieces medication
            if (medication.getPrescribedPieces() > 0 && medication.getStartDay() != null) {
                // Display prescribed pieces format
                durationText.append("Starting ").append(medication.getStartDay());
                durationText.append(" - ").append(medication.getPrescribedPieces()).append(" doses");
                if (medication.getTimesPerDay() > 0) {
                    durationText.append(" (").append(medication.getTimesPerDay()).append("x/day)");
                }
            } else {
                // Legacy format
                String[] dayNames = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
                String selectedDays = medication.getSelectedDays();
                int durationDays = medication.getDurationDays();

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

        /**
         * Create segmented progress bar with color-coded doses
         * @param doseStatuses Array of dose statuses (0=not taken, 1=green/on time, 2=red/late)
         * @param totalDoses Total number of doses
         */
        private void createSegmentedProgressBar(int[] doseStatuses, int totalDoses) {
            segmentedProgressContainer.removeAllViews();

            if (totalDoses == 0) return;

            for (int i = 0; i < totalDoses; i++) {
                View segment = new View(context);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1.0f / totalDoses
                );

                // Add small margin between segments
                if (i > 0) {
                    params.leftMargin = 2;
                }

                segment.setLayoutParams(params);

                // Set color based on status
                int color;
                if (i < doseStatuses.length) {
                    switch (doseStatuses[i]) {
                        case 1: // Taken on time - Green
                            color = 0xFF27AE60;
                            break;
                        case 2: // Taken late - Red
                            color = 0xFFE74C3C;
                            break;
                        default: // Not taken - Light gray
                            color = 0xFFE0E0E0;
                            break;
                    }
                } else {
                    color = 0xFFE0E0E0; // Default gray for future doses
                }

                segment.setBackgroundColor(color);
                segmentedProgressContainer.addView(segment);
            }
        }
    }
}
