package com.far.nowaste.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.far.nowaste.Other.EventDecorator;
import com.far.nowaste.Objects.Evento;
import com.far.nowaste.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.util.LinkedList;
import java.util.List;

public class CalendarioFragment extends Fragment {

    // definizione view
    MaterialCalendarView mCalendarView;
    TextView mDateTextView, mEventTextView;

    // definizione variabili
    List<Evento> eventi;
    List<CalendarDay> dates;

    // firebase
    FirebaseFirestore fStore;
    FirebaseAuth fAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // associazione view
        View view = inflater.inflate(R.layout.fragment_calendario, container, false);
        mCalendarView = view.findViewById(R.id.calendarView);
        mDateTextView = view.findViewById(R.id.dateTextView);
        mEventTextView = view.findViewById(R.id.eventTextView);

        // inizializzazione liste
        eventi = new LinkedList<>();
        dates = new LinkedList<>();

        // inizializzazione firestore
        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();

        // imposta la data odierna
        CalendarDay currentDay = CalendarDay.today();
        mCalendarView.setDateSelected(CalendarDay.today(), true);
        mDateTextView.setText(currentDay.getDay() + "/" + currentDay.getMonth() + "/" + currentDay.getYear());
        if (fAuth.getCurrentUser() == null) {
            mEventTextView.setText("Accedi per visualizzare i tuoi eventi");
            mEventTextView.setVisibility(View.VISIBLE);
        }

        // onClick day
        mCalendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                mDateTextView.setText(date.getDay() + "/" + date.getMonth() + "/" + date.getYear());
                if (fAuth.getCurrentUser() != null) {
                    mEventTextView.setVisibility(View.INVISIBLE);
                    for (Evento evento : eventi) {
                        if (date.getYear() == evento.getYear() && date.getMonth() == evento.getMonth() && date.getDay() == evento.getDay()) {
                            mEventTextView.setText(evento.getTitle());
                            mEventTextView.setVisibility(View.VISIBLE);
                        }
                    }
                }

            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // prendo gli eventi dal database
        if (fAuth.getCurrentUser() != null) {
            fStore.collection("events").whereEqualTo("email", fAuth.getCurrentUser().getEmail()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()){
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Evento evento = document.toObject(Evento.class);
                            eventi.add(evento);
                            // prelevo le date dai documenti e le metto in dates
                            CalendarDay calendarDay = CalendarDay.from(evento.getYear(), evento.getMonth(), evento.getDay());
                            dates.add(calendarDay);
                        }
                        // aggiungi i dot agli eventi
                        mCalendarView.addDecorator(new EventDecorator(ContextCompat.getColor(getContext(), R.color.cinnamon_satin), dates));
                    }
                }
            });
        }
    }
}