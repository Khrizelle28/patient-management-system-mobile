package com.example.patientmanagementsystemmobile.adapter;

// AppointmentAdapter.java
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.patientmanagementsystemmobile.R;
import com.example.patientmanagementsystemmobile.enums.AppointmentStatus;
import com.example.patientmanagementsystemmobile.models.Appointment;
import com.example.patientmanagementsystemmobile.models.Person;

import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder> {

    private List<Appointment> appointments;
    private Context context;

    public AppointmentAdapter(List<Appointment> appointments, Context context) {
        this.appointments = appointments;
        this.context = context;
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_appointment, parent, false);
        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);
        Log.d("appointment", "appointment list for: " + appointment);
        holder.textDoctorName.setText(appointment.getDoctorName());
        holder.textSpecialty.setText(appointment.getSpecialty());
        holder.textDate.setText(appointment.getDate());
        holder.textTime.setText(appointment.getTime());
        holder.textPurpose.setText(appointment.getPurpose());

        // Set status with color
//        String statusText = getStatusText();
        holder.textStatus.setText(appointment.getStatus());

        int statusColor = getStatusColor(appointment.getStatus());
        holder.textStatus.setTextColor(ContextCompat.getColor(context, statusColor));

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            // Show appointment details dialog
            showAppointmentDetailsDialog(appointment);
        });
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    public void updateAppointments(List<Appointment> newAppointments) {
        appointments.clear();
        appointments.addAll(newAppointments);
        notifyDataSetChanged();
    }

    private String getStatusText(AppointmentStatus status) {
        switch (status) {
            case UPCOMING:
                return "Upcoming";
            case SCHEDULED:
                return "Scheduled";
            case COMPLETED:
                return "Completed";
            case CANCELLED:
                return "Cancelled";
            default:
                return "Unknown";
        }
    }

    private int getStatusColor(String status) {
        switch (status) {
            case "upcoming":
                return android.R.color.holo_orange_dark;
            case "scheduled":
                return android.R.color.holo_blue_dark;
            case "completed":
                return android.R.color.holo_green_dark;
            case "cancelled":
                return android.R.color.holo_red_dark;
            default:
                return android.R.color.black;
        }
    }

    private void showAppointmentDetailsDialog(Appointment appointment) {
        // Create dialog
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_appointment_details);
        dialog.setCancelable(true);

        // Get dialog views
        TextView textDialogAppointmentId = dialog.findViewById(R.id.textDialogAppointmentId);
        TextView textDialogPatientName = dialog.findViewById(R.id.textDialogPatientName);
        TextView textDialogDoctorName = dialog.findViewById(R.id.textDialogDoctorName);
        TextView textDialogSpecialty = dialog.findViewById(R.id.textDialogSpecialty);
        TextView textDialogDate = dialog.findViewById(R.id.textDialogDate);
        TextView textDialogTime = dialog.findViewById(R.id.textDialogTime);
        TextView textDialogStatus = dialog.findViewById(R.id.textDialogStatus);
        TextView textDialogPurpose = dialog.findViewById(R.id.textDialogPurpose);
        Button buttonClose = dialog.findViewById(R.id.buttonClose);

        // Set appointment details
        textDialogAppointmentId.setText("#" + appointment.getId());
        textDialogPatientName.setText(appointment.getPatientName() != null ?
                appointment.getPatientName() : "N/A");
        textDialogDoctorName.setText(appointment.getDoctorName());
        textDialogSpecialty.setText(appointment.getSpecialty());
        textDialogDate.setText(appointment.getDate());
        textDialogTime.setText(appointment.getTime());
        textDialogStatus.setText(appointment.getStatus());
        textDialogPurpose.setText(appointment.getPurpose());

        // Set status color
        int statusColor = getStatusColor(appointment.getStatus());
        textDialogStatus.setTextColor(ContextCompat.getColor(context, statusColor));

        // Close button listener
        buttonClose.setOnClickListener(v -> dialog.dismiss());

        // Make dialog width match parent with some padding
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialog.show();
    }

    static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        TextView textDoctorName;
        TextView textSpecialty;
        TextView textDate;
        TextView textTime;
        TextView textPurpose;
        TextView textStatus;

        public AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            textDoctorName = itemView.findViewById(R.id.textDoctorName);
            textSpecialty = itemView.findViewById(R.id.textSpecialty);
            textDate = itemView.findViewById(R.id.textDate);
            textTime = itemView.findViewById(R.id.textTime);
            textPurpose = itemView.findViewById(R.id.textPurpose);
            textStatus = itemView.findViewById(R.id.textStatus);
        }
    }
}