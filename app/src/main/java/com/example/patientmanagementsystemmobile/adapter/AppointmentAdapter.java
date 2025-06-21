package com.example.patientmanagementsystemmobile.adapter;

// AppointmentAdapter.java
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
            // Handle appointment item click
            // You can implement navigation to appointment details here
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