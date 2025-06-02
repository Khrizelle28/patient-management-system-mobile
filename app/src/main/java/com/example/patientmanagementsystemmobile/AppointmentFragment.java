    package com.example.patientmanagementsystemmobile; // <-- **MAKE SURE THIS IS YOUR ACTUAL PACKAGE NAME**
    import android.os.Bundle;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.CalendarView;
    import android.widget.TextView;
    import androidx.annotation.NonNull;
    import androidx.annotation.Nullable;
    import androidx.fragment.app.Fragment;
    import androidx.recyclerview.widget.LinearLayoutManager;
    import androidx.recyclerview.widget.RecyclerView;

    import com.example.patientmanagementsystemmobile.adapter.PeopleAdapter;
    import com.example.patientmanagementsystemmobile.models.Person;

    import java.text.SimpleDateFormat;
    import java.util.*;

    public class AppointmentFragment extends Fragment {

        private CalendarView calendarView;
        private TextView selectedDateText;
        private RecyclerView peopleRecyclerView;
        private PeopleAdapter peopleAdapter;
        private Map<String, List<Person>> scheduleData;
        private String selectedDate;

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_appointment, container, false);

            initViews(view);
            setupScheduleData();
            setupRecyclerView(); // Setup RecyclerView BEFORE calendar
            setupCalendar();     // Calendar setup calls updatePeopleList()

            return view;
        }

        private void initViews(View view) {
            calendarView = view.findViewById(R.id.calendarView);
            selectedDateText = view.findViewById(R.id.selectedDateText);
            peopleRecyclerView = view.findViewById(R.id.peopleRecyclerView);
        }

        private void setupScheduleData() {
            scheduleData = new HashMap<>();

            // Sample data - replace with your actual data source
            List<Person> mondayPeople = Arrays.asList(
                    new Person("Dr. Smith", "Cardiologist", "9:00 AM - 5:00 PM", true),
                    new Person("Dr. Johnson", "Neurologist", "10:00 AM - 4:00 PM", true),
                    new Person("Dr. Brown", "Dermatologist", "8:00 AM - 3:00 PM", false)
            );

            List<Person> tuesdayPeople = Arrays.asList(
                    new Person("Dr. Wilson", "Pediatrician", "9:00 AM - 6:00 PM", true),
                    new Person("Dr. Davis", "Orthopedist", "11:00 AM - 5:00 PM", true)
            );

            List<Person> wednesdayPeople = Arrays.asList(
                    new Person("Dr. Miller", "Psychiatrist", "10:00 AM - 4:00 PM", true),
                    new Person("Dr. Garcia", "Ophthalmologist", "9:00 AM - 3:00 PM", false),
                    new Person("Dr. Anderson", "Radiologist", "8:00 AM - 2:00 PM", true)
            );

            // Add more days as needed
            List<Person> thursdayPeople = Arrays.asList(
                    new Person("Dr. Thompson", "Endocrinologist", "9:00 AM - 4:00 PM", true),
                    new Person("Dr. White", "Pulmonologist", "10:00 AM - 5:00 PM", true)
            );

            List<Person> fridayPeople = Arrays.asList(
                    new Person("Dr. Martinez", "Gastroenterologist", "8:00 AM - 4:00 PM", true),
                    new Person("Dr. Lee", "Rheumatologist", "9:00 AM - 3:00 PM", true),
                    new Person("Dr. Taylor", "Urologist", "11:00 AM - 6:00 PM", false)
            );

            List<Person> saturdayPeople = Arrays.asList(
                    new Person("Dr. Clark", "Emergency Medicine", "8:00 AM - 2:00 PM", true),
                    new Person("Dr. Lewis", "Family Medicine", "10:00 AM - 4:00 PM", true)
            );

            // Generate schedule for today + next 7 business days (excluding Sundays)
            Calendar cal = Calendar.getInstance();
//            cal.set(2025, Calendar.JUNE, 13);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            // Generate schedule for business days only (Monday-Saturday)
            int daysAdded = 0;
            int totalDays = 0;
            int todayDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

            while (daysAdded < 7 && totalDays < 14) { // Safety limit to prevent infinite loop
                int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

                // Skip Sundays
                if (dayOfWeek != Calendar.SUNDAY && totalDays != 0) {
                    String dateKey = dateFormat.format(cal.getTime());

                    // Assign people based on actual day of week
                    switch (dayOfWeek) {
                        case Calendar.MONDAY:
                            scheduleData.put(dateKey, new ArrayList<>(mondayPeople));
                            break;
                        case Calendar.TUESDAY:
                            scheduleData.put(dateKey, new ArrayList<>(tuesdayPeople));
                            break;
                        case Calendar.WEDNESDAY:
                            scheduleData.put(dateKey, new ArrayList<>(wednesdayPeople));
                            break;
                        case Calendar.THURSDAY:
                            scheduleData.put(dateKey, new ArrayList<>(thursdayPeople));
                            break;
                        case Calendar.FRIDAY:
                            scheduleData.put(dateKey, new ArrayList<>(fridayPeople));
                            break;
                        case Calendar.SATURDAY:
                            scheduleData.put(dateKey, new ArrayList<>(saturdayPeople));
                            break;
                    }

                    daysAdded++; // Increment the counter for business days added
                }

                cal.add(Calendar.DAY_OF_MONTH, 1); // Always move to next day
                totalDays++; // Increment total days counter
            }
        }


        private void setupCalendar() {
            // Set initial selected date to today
            Calendar today = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            selectedDate = dateFormat.format(today.getTime());
            updateSelectedDateDisplay();
            updatePeopleList();

            calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
                @Override
                public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                    Calendar selectedCal = Calendar.getInstance();
                    selectedCal.set(year, month, dayOfMonth);
                    selectedDate = dateFormat.format(selectedCal.getTime());

                    updateSelectedDateDisplay();
                    updatePeopleList();
                }
            });
        }

        private void setupRecyclerView() {
            if (getContext() != null) {
                peopleAdapter = new PeopleAdapter(new ArrayList<>());
                peopleRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                peopleRecyclerView.setAdapter(peopleAdapter);

                // Optional: Add click listener
                peopleAdapter.setOnPersonClickListener(person -> {
                    if (person.isAvailable()) {
                        // Handle appointment booking
                        // Toast.makeText(getContext(), "Booking appointment with " + person.getName(), Toast.LENGTH_SHORT).show();
                    } else {
                        // Toast.makeText(getContext(), person.getName() + " is not available", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        private void updateSelectedDateDisplay() {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault());
                Date date = inputFormat.parse(selectedDate);
                String formattedDate = outputFormat.format(date);
                selectedDateText.setText("Available on " + formattedDate);
            } catch (Exception e) {
                selectedDateText.setText("Available on " + selectedDate);
            }
        }

        private void updatePeopleList() {
            List<Person> availablePeople = scheduleData.get(selectedDate);
            if (availablePeople == null) {
                availablePeople = new ArrayList<>();
            }

            // Add null check to prevent crash
            if (peopleAdapter != null) {
                peopleAdapter.updatePeople(availablePeople);
            }
        }

        // Method to add schedule data programmatically
        public void addScheduleForDate(String date, List<Person> people) {
            scheduleData.put(date, people);
            if (selectedDate.equals(date)) {
                updatePeopleList();
            }
        }

        // Method to get schedule data for a specific date
        public List<Person> getScheduleForDate(String date) {
            return scheduleData.get(date);
        }
    }