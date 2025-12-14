package com.example.patientmanagementsystemmobile.models;

/**
 * Wrapper class to display individual alarms as separate cards in the RecyclerView.
 * One MedicationAlert with 4 alarm times becomes 4 MedicationAlarmDisplayItem objects.
 */
public class MedicationAlarmDisplayItem {
    private MedicationAlert medication;
    private int alarmIndex; // -1 for legacy medications, 0-3+ for prescribed pieces
    private String calculatedDayName;

    public MedicationAlarmDisplayItem(MedicationAlert medication, int alarmIndex) {
        this.medication = medication;
        this.alarmIndex = alarmIndex;
        this.calculatedDayName = calculateDayName();
    }

    public MedicationAlert getMedication() {
        return medication;
    }

    public int getAlarmIndex() {
        return alarmIndex;
    }

    public String getCalculatedDayName() {
        return calculatedDayName;
    }

    public boolean isPrescribedPieces() {
        return alarmIndex >= 0 && medication.getPrescribedPieces() > 0;
    }

    /**
     * Calculate which day of the week this alarm falls on based on the start day and alarm index.
     */
    private String calculateDayName() {
        if (!isPrescribedPieces() || medication.getStartDay() == null) {
            return "";
        }

        String[] daysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

        // Find start day index
        int startDayIndex = -1;
        for (int i = 0; i < daysOfWeek.length; i++) {
            if (daysOfWeek[i].equalsIgnoreCase(medication.getStartDay())) {
                startDayIndex = i;
                break;
            }
        }

        if (startDayIndex == -1 || medication.getAlarmTimes() == null || alarmIndex < 0 || alarmIndex >= medication.getAlarmTimes().size()) {
            return "";
        }

        // Get the first dose time
        MedicationAlert.AlarmTime firstDose = medication.getAlarmTimes().get(0);
        String[] firstTimeParts = firstDose.getTime().split(":");
        int firstHour = Integer.parseInt(firstTimeParts[0]);
        int firstMinute = Integer.parseInt(firstTimeParts[1]);

        // Convert first dose to 24-hour format
        if (firstDose.getPeriod().equals("PM") && firstHour != 12) {
            firstHour += 12;
        } else if (firstDose.getPeriod().equals("AM") && firstHour == 12) {
            firstHour = 0;
        }

        // Calculate interval in hours
        int timesPerDay = medication.getTimesPerDay();
        if (timesPerDay <= 0) {
            timesPerDay = 1;
        }
        int intervalHours = 24 / timesPerDay;

        // Calculate the hour for this alarm by adding intervals
        int totalMinutes = (firstHour * 60 + firstMinute) + (alarmIndex * intervalHours * 60);

        // Calculate how many days have passed based on total minutes
        int daysPassed = totalMinutes / (24 * 60);

        // Calculate current day
        int currentDayIndex = (startDayIndex + daysPassed) % 7;

        return daysOfWeek[currentDayIndex];
    }

    /**
     * Get the alarm time for this specific alarm.
     */
    public MedicationAlert.AlarmTime getAlarmTime() {
        if (medication.getAlarmTimes() == null || alarmIndex < 0 || alarmIndex >= medication.getAlarmTimes().size()) {
            // Legacy medication or invalid index
            return new MedicationAlert.AlarmTime(medication.getTime(), medication.getPeriod());
        }
        return medication.getAlarmTimes().get(alarmIndex);
    }
}
