// Create this file: adapter/PeopleAdapter.java
package com.example.patientmanagementsystemmobile.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.patientmanagementsystemmobile.R;
import com.example.patientmanagementsystemmobile.models.Person;
import java.util.List;

public class PeopleAdapter extends RecyclerView.Adapter<PeopleAdapter.PersonViewHolder> {

    private List<Person> people;
    private OnPersonClickListener onPersonClickListener;

    // Interface for click events
    public interface OnPersonClickListener {
        void onPersonClick(Person person);
    }

    // Constructor
    public PeopleAdapter(List<Person> people) {
        this.people = people;
    }

    // Set click listener
    public void setOnPersonClickListener(OnPersonClickListener listener) {
        this.onPersonClickListener = listener;
    }

    @NonNull
    @Override
    public PersonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_person, parent, false);
        return new PersonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PersonViewHolder holder, int position) {
        Person person = people.get(position);
        holder.bind(person);

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (onPersonClickListener != null) {
                onPersonClickListener.onPersonClick(person);
            }
        });
    }

    @Override
    public int getItemCount() {
        return people != null ? people.size() : 0;
    }

    // Method to update the list
    public void updatePeople(List<Person> newPeople) {
        this.people = newPeople;
        notifyDataSetChanged();
    }

    // ViewHolder class
    public static class PersonViewHolder extends RecyclerView.ViewHolder {
        private TextView nameTextView;
        private TextView specialtyTextView;
        private TextView scheduleTextView;
        private TextView availabilityTextView;

        public PersonViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameText);
            specialtyTextView = itemView.findViewById(R.id.specialtyText);
            scheduleTextView = itemView.findViewById(R.id.timeText);
            availabilityTextView = itemView.findViewById(R.id.statusText);
        }

        public void bind(Person person) {
            nameTextView.setText(person.getName());
            specialtyTextView.setText(person.getSpecialty());
            scheduleTextView.setText(person.getSchedule());

            if (person.isAvailable()) {
                availabilityTextView.setText("Available");
                availabilityTextView.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
                itemView.setAlpha(1.0f);
            } else {
                availabilityTextView.setText("Not Available");
                availabilityTextView.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
                itemView.setAlpha(0.6f);
            }
        }
    }
}