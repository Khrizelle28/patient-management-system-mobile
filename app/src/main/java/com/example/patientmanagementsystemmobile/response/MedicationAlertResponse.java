package com.example.patientmanagementsystemmobile.response;

import com.example.patientmanagementsystemmobile.models.MedicationAlert;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class MedicationAlertResponse {
    private boolean success;
    private String message;
    private List<MedicationAlertData> data;
    private MedicationAlertData alert; // For single alert operations (create/update)

    public MedicationAlertResponse(boolean success, String message, List<MedicationAlertData> data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public List<MedicationAlertData> getData() {
        return data;
    }

    public MedicationAlertData getAlert() {
        return alert;
    }

    // Setters
    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(List<MedicationAlertData> data) {
        this.data = data;
    }

    public void setAlert(MedicationAlertData alert) {
        this.alert = alert;
    }

    // Inner class for medication alert data from API
    public static class MedicationAlertData {
        private int id;
        private String patient_id;
        private String time;
        private String period;
        private String medication_name;
        private String remarks;
        private boolean is_enabled;
        private String selected_days;
        private int duration_days;
        private String start_date;
        private String created_at;
        private String updated_at;

        // Prescribed pieces feature fields
        private int prescribed_pieces;
        private int times_per_day;
        private String start_day;
        private String first_dose_time;
        private String first_dose_period;

        // Getters
        public int getId() {
            return id;
        }

        public String getPatient_id() {
            return patient_id;
        }

        public String getTime() {
            return time;
        }

        public String getPeriod() {
            return period;
        }

        public String getMedication_name() {
            return medication_name;
        }

        public String getRemarks() {
            return remarks;
        }

        public boolean isIs_enabled() {
            return is_enabled;
        }

        public String getSelected_days() {
            return selected_days;
        }

        public int getDuration_days() {
            return duration_days;
        }

        public String getStart_date() {
            return start_date;
        }

        public String getCreated_at() {
            return created_at;
        }

        public String getUpdated_at() {
            return updated_at;
        }

        // Setters
        public void setId(int id) {
            this.id = id;
        }

        public void setPatient_id(String patient_id) {
            this.patient_id = patient_id;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public void setPeriod(String period) {
            this.period = period;
        }

        public void setMedication_name(String medication_name) {
            this.medication_name = medication_name;
        }

        public void setRemarks(String remarks) {
            this.remarks = remarks;
        }

        public void setIs_enabled(boolean is_enabled) {
            this.is_enabled = is_enabled;
        }

        public void setSelected_days(String selected_days) {
            this.selected_days = selected_days;
        }

        public void setDuration_days(int duration_days) {
            this.duration_days = duration_days;
        }

        public void setStart_date(String start_date) {
            this.start_date = start_date;
        }

        public void setCreated_at(String created_at) {
            this.created_at = created_at;
        }

        public void setUpdated_at(String updated_at) {
            this.updated_at = updated_at;
        }

        // Getters and setters for prescribed pieces feature
        public int getPrescribed_pieces() {
            return prescribed_pieces;
        }

        public void setPrescribed_pieces(int prescribed_pieces) {
            this.prescribed_pieces = prescribed_pieces;
        }

        public int getTimes_per_day() {
            return times_per_day;
        }

        public void setTimes_per_day(int times_per_day) {
            this.times_per_day = times_per_day;
        }

        public String getStart_day() {
            return start_day;
        }

        public void setStart_day(String start_day) {
            this.start_day = start_day;
        }

        public String getFirst_dose_time() {
            return first_dose_time;
        }

        public void setFirst_dose_time(String first_dose_time) {
            this.first_dose_time = first_dose_time;
        }

        public String getFirst_dose_period() {
            return first_dose_period;
        }

        public void setFirst_dose_period(String first_dose_period) {
            this.first_dose_period = first_dose_period;
        }

        // Parse alarm times from JSON or legacy format
        private List<MedicationAlert.AlarmTime> parseAlarmTimes() {
            List<MedicationAlert.AlarmTime> alarmTimes = new ArrayList<>();

            try {
                if (time != null && time.trim().startsWith("[")) {
                    // JSON array format - multiple alarm times
                    JSONArray jsonArray = new JSONArray(time);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        alarmTimes.add(new MedicationAlert.AlarmTime(
                            obj.getString("time"),
                            obj.getString("period")
                        ));
                    }
                } else {
                    // Legacy single time format
                    alarmTimes.add(new MedicationAlert.AlarmTime(
                        time != null ? time : "",
                        period != null ? period : "AM"
                    ));
                }
            } catch (JSONException e) {
                // Fallback to legacy format on parse error
                alarmTimes.add(new MedicationAlert.AlarmTime(
                    time != null ? time : "",
                    period != null ? period : "AM"
                ));
            }

            return alarmTimes;
        }

        // Convert to MedicationAlert model
        public MedicationAlert toMedicationAlert() {
            MedicationAlert alert = new MedicationAlert(
                    id,
                    time != null ? time : "",
                    period != null ? period : "AM",
                    medication_name,
                    remarks != null ? remarks : "",
                    is_enabled,
                    selected_days != null ? selected_days : "",
                    duration_days,
                    start_date != null ? start_date : ""
            );

            // Set the parsed alarm times
            alert.setAlarmTimes(parseAlarmTimes());

            // Set prescribed pieces feature fields
            alert.setPrescribedPieces(prescribed_pieces);
            alert.setTimesPerDay(times_per_day);
            alert.setStartDay(start_day);
            alert.setFirstDoseTime(first_dose_time);
            alert.setFirstDosePeriod(first_dose_period);

            return alert;
        }
    }
}
