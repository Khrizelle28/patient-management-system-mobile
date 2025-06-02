package com.example.patientmanagementsystemmobile.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.patientmanagementsystemmobile.R;
import com.example.patientmanagementsystemmobile.models.Person;
import java.util.*;

public class PeopleAdapter extends RecyclerView.Adapter<PeopleAdapter.PersonViewHolder> {

    private List<Person> people;
    private OnPersonClickListener listener;

    public interface OnPersonClickListener {
        void onPersonClick(Person person);
    }

    public PeopleAdapter(List<Person> people) {
        this.people = people;
    }

    public void setOnPersonClickListener(OnPersonClickListener listener) {
        this.listener = listener;
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
    }

    @Override
    public int getItemCount() {
        return people.size();
    }

    public void updatePeople(List<Person> newPeople) {
        this.people = newPeople;
        notifyDataSetChanged();
    }

    class PersonViewHolder extends RecyclerView.ViewHolder {
        private TextView nameText;
        private TextView specialtyText;
        private TextView timeText;
        private TextView statusText;
        private View itemView;

        public PersonViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            nameText = itemView.findViewById(R.id.nameText);
            specialtyText = itemView.findViewById(R.id.specialtyText);
            timeText = itemView.findViewById(R.id.timeText);
            statusText = itemView.findViewById(R.id.statusText);

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onPersonClick(people.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Person person) {
            nameText.setText(person.getName());
            specialtyText.setText(person.getSpecialty());
            timeText.setText(person.getAvailableTime());

            if (person.isAvailable()) {
                statusText.setText("Available");
                statusText.setTextColor(Color.GREEN);
                itemView.setAlpha(1.0f);
            } else {
                statusText.setText("Unavailable");
                statusText.setTextColor(Color.RED);
                itemView.setAlpha(0.6f);
            }
        }
    }
}