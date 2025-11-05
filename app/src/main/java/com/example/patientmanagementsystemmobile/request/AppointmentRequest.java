package com.example.patientmanagementsystemmobile.request;

// AppointmentRequest.java
public class AppointmentRequest {
    private String patient_id;
    private String doctor_id;
    private String appointment_date;
    private String appointment_time;
    private String notes;
    private String service_type;
    private Double service_price;
    private Boolean has_pap_smear;
    private Double pap_smear_price;
    private Boolean needs_medical_certificate;
    private Double medical_certificate_price;
    private Double total_amount;

    public AppointmentRequest(String patientId, String doctorId, String date, String time, String notes) {
        this.patient_id = patientId;
        this.doctor_id = doctorId;
        this.appointment_date = date;
        this.appointment_time = time;
        this.notes = notes;
    }

    public AppointmentRequest(String patientId, String doctorId, String date, String time, String notes,
                             String serviceType, Double servicePrice, Boolean hasPapSmear, Double papSmearPrice,
                             Boolean needsMedicalCertificate, Double medicalCertificatePrice, Double totalAmount) {
        this.patient_id = patientId;
        this.doctor_id = doctorId;
        this.appointment_date = date;
        this.appointment_time = time;
        this.notes = notes;
        this.service_type = serviceType;
        this.service_price = servicePrice;
        this.has_pap_smear = hasPapSmear;
        this.pap_smear_price = papSmearPrice;
        this.needs_medical_certificate = needsMedicalCertificate;
        this.medical_certificate_price = medicalCertificatePrice;
        this.total_amount = totalAmount;
    }

    // Getters and setters
    public String getPatient_id() { return patient_id; }
    public void setPatient_id(String patient_id) { this.patient_id = patient_id; }

    public String getDoctor_id() { return doctor_id; }
    public void setDoctor_id(String doctor_id) { this.doctor_id = doctor_id; }

    public String getAppointment_date() { return appointment_date; }
    public void setAppointment_date(String appointment_date) { this.appointment_date = appointment_date; }

    public String getAppointment_time() { return appointment_time; }
    public void setAppointment_time(String appointment_time) { this.appointment_time = appointment_time; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getService_type() { return service_type; }
    public void setService_type(String service_type) { this.service_type = service_type; }

    public Double getService_price() { return service_price; }
    public void setService_price(Double service_price) { this.service_price = service_price; }

    public Boolean getHas_pap_smear() { return has_pap_smear; }
    public void setHas_pap_smear(Boolean has_pap_smear) { this.has_pap_smear = has_pap_smear; }

    public Double getPap_smear_price() { return pap_smear_price; }
    public void setPap_smear_price(Double pap_smear_price) { this.pap_smear_price = pap_smear_price; }

    public Boolean getNeeds_medical_certificate() { return needs_medical_certificate; }
    public void setNeeds_medical_certificate(Boolean needs_medical_certificate) { this.needs_medical_certificate = needs_medical_certificate; }

    public Double getMedical_certificate_price() { return medical_certificate_price; }
    public void setMedical_certificate_price(Double medical_certificate_price) { this.medical_certificate_price = medical_certificate_price; }

    public Double getTotal_amount() { return total_amount; }
    public void setTotal_amount(Double total_amount) { this.total_amount = total_amount; }
}
