package com.example.patientmanagementsystemmobile.request;

import com.example.patientmanagementsystemmobile.models.MedicationAlert;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.List;

public class MedicationAlertRequest {
    private String patient_id;
    private String time;
    private String period;
    private String medication_name;
    private String remarks;
    private boolean is_enabled;
    private String selected_days;
    private String start_date;
    private int duration_days;

    // Prescribed pieces feature fields
    private int prescribed_pieces;
    private int times_per_day;
    private String start_day;
    private String first_dose_time;
    private String first_dose_period;

    // Legacy constructor (single time)
    public MedicationAlertRequest(String patient_id, String time, String period, String medication_name, String remarks, boolean is_enabled,
                                  String selected_days, String start_date, int duration_days) {
        this.patient_id = patient_id;
        this.time = time;
        this.period = period;
        this.medication_name = medication_name;
        this.remarks = remarks;
        this.is_enabled = is_enabled;
        this.selected_days = selected_days;
        this.start_date = start_date;
        this.duration_days = duration_days;
    }

    // New constructor (multiple alarm times)
    public MedicationAlertRequest(String patient_id, List<MedicationAlert.AlarmTime> alarmTimes,
                                  String medication_name, String remarks, boolean is_enabled,
                                  String selected_days, String start_date, int duration_days) {
        this.patient_id = patient_id;
        this.medication_name = medication_name;
        this.remarks = remarks;
        this.is_enabled = is_enabled;
        this.selected_days = selected_days;
        this.start_date = start_date;
        this.duration_days = duration_days;

        if (alarmTimes.size() == 1) {
            // Single time - legacy format
            this.time = alarmTimes.get(0).getTime();
            this.period = alarmTimes.get(0).getPeriod();
        } else if (alarmTimes.size() > 1) {
            // Multiple times - JSON format
            try {
                JSONArray jsonArray = new JSONArray();
                for (MedicationAlert.AlarmTime alarmTime : alarmTimes) {
                    JSONObject obj = new JSONObject();
                    obj.put("time", alarmTime.getTime());
                    obj.put("period", alarmTime.getPeriod());
                    jsonArray.put(obj);
                }
                this.time = jsonArray.toString();
                this.period = alarmTimes.get(0).getPeriod();
            } catch (JSONException e) {
                // Fallback to first time if JSON fails
                this.time = alarmTimes.get(0).getTime();
                this.period = alarmTimes.get(0).getPeriod();
            }
        }
    }

    // Getters
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

    public String getStart_date() {
        return start_date;
    }

    public int getDuration_days() {
        return duration_days;
    }

    // Setters
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

    public void setStart_date(String start_date) {
        this.start_date = start_date;
    }

    public void setDuration_days(int duration_days) {
        this.duration_days = duration_days;
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
}
