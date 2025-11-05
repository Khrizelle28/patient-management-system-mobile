package com.example.patientmanagementsystemmobile.response;

import com.example.patientmanagementsystemmobile.models.MedicationAlert;
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
        private String created_at;
        private String updated_at;

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

        public void setCreated_at(String created_at) {
            this.created_at = created_at;
        }

        public void setUpdated_at(String updated_at) {
            this.updated_at = updated_at;
        }

        // Convert to MedicationAlert model
        public MedicationAlert toMedicationAlert() {
            return new MedicationAlert(
                    id,
                    time,
                    period,
                    medication_name,
                    remarks != null ? remarks : "",
                    is_enabled
            );
        }
    }
}
