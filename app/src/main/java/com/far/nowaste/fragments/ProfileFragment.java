package com.far.nowaste.fragments;

import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.far.nowaste.MainActivity;
import com.far.nowaste.objects.Saving;
import com.far.nowaste.objects.Utente;
import com.far.nowaste.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.common.primitives.Ints;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.eazegraph.lib.charts.BarChart;
import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.BarModel;
import org.eazegraph.lib.models.PieModel;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    // definizione variabili
    ImageView mImage;
    TextView mFullName, mEmail;

    // grafici
    TextView tvSaving, tvPlastica, tvOrganico, tvSecco, tvCarta, tvVetro, tvMetalli, tvElettrici, tvSpeciali;
    PieChart pieChart;
    BarChart barChart;
    MaterialButton mSavingBtn;

    List<Integer> colors;

    String tipo;

    // firebase
    FirebaseAuth fAuth;
    FirebaseUser fUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // collegamento view
        mImage = view.findViewById(R.id.userImageView);
        mFullName = view.findViewById(R.id.nameTextView);
        mEmail = view.findViewById(R.id.emailTextView);

        // dassegnazione views
        pieChart = view.findViewById(R.id.piechart);
        barChart = view.findViewById(R.id.istograph);
        tvPlastica = view.findViewById(R.id.tvPlastica);
        tvOrganico = view.findViewById(R.id.tvOrganico);
        tvSecco = view.findViewById(R.id.tvSecco);
        tvCarta = view.findViewById(R.id.tvCarta);
        tvVetro = view.findViewById(R.id.tvVetro);
        tvMetalli = view.findViewById(R.id.tvMetalli);
        tvElettrici = view.findViewById(R.id.tvElettrici);
        tvSpeciali = view.findViewById(R.id.tvSpeciali);
        tvSaving = view.findViewById(R.id.tvSaving);
        mSavingBtn = view.findViewById(R.id.savingButton);

        colors = new ArrayList<>();
        colors.add(ContextCompat.getColor(getContext(), R.color.plastica));
        colors.add(ContextCompat.getColor(getContext(), R.color.organico));
        colors.add(ContextCompat.getColor(getContext(), R.color.secco));
        colors.add(ContextCompat.getColor(getContext(), R.color.carta));
        colors.add(ContextCompat.getColor(getContext(), R.color.vetro));
        colors.add(ContextCompat.getColor(getContext(), R.color.metalli));
        colors.add(ContextCompat.getColor(getContext(), R.color.elettrici));
        colors.add(ContextCompat.getColor(getContext(), R.color.speciali));

        fAuth = FirebaseAuth.getInstance();

        retrieveCurrentUser();
        retrieveUserData();

        tipo = "co2";
        mSavingBtn.setText(Html.fromHtml("CO<sub><small><small>2</small></small></sub>"));
        mSavingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (tipo) {
                    case "co2":
                        tipo = "petrolio";
                        setPieChartData(tipo, MainActivity.OIL_ARRAY_LIST);
                        mSavingBtn.setText("Petrolio");
                        break;
                    case "petrolio":
                        tipo = "energia";
                        setPieChartData(tipo, MainActivity.ENERGY_ARRAY_LIST);
                        mSavingBtn.setText("Energia");
                        break;
                    case "energia":
                        tipo = "co2";
                        setPieChartData(tipo, MainActivity.CARBON_DIOXIDE_ARRAY_LIST);
                        mSavingBtn.setText(Html.fromHtml("CO<sub><small><small>2</small></small></sub>"));
                        break;
                }
            }
        });

        return view;
    }

    private void retrieveCurrentUser() {
        if (MainActivity.CURRENT_USER != null) {
            // imposta nome, cognome e immagine
            mFullName.setText(MainActivity.CURRENT_USER.getFullName());
            mEmail.setText(MainActivity.CURRENT_USER.getEmail());
            if (!MainActivity.CURRENT_USER.getImage().equals("")) {
                Glide.with(getContext()).load(MainActivity.CURRENT_USER.getImage()).apply(RequestOptions.circleCropTransform()).into(mImage);
            }
        } else {
            FirebaseFirestore fStore = FirebaseFirestore.getInstance();
            fUser = fAuth.getCurrentUser();

            // imposta dati personali
            fStore.collection("users").document(fUser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    Utente utente = documentSnapshot.toObject(Utente.class);
                    // imposta nome, cognome e immagine
                    mFullName.setText(utente.getFullName());
                    mEmail.setText(utente.getEmail());
                    if (!utente.getImage().equals("")) {
                        Glide.with(getContext()).load(utente.getImage()).apply(RequestOptions.circleCropTransform()).into(mImage);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("LOG", "Error! " + e.getLocalizedMessage());
                }
            });
        }
    }

    private void retrieveUserData() {
        if (MainActivity.CARBON_DIOXIDE_ARRAY_LIST == null || MainActivity.QUANTITA == null) {
            FirebaseFirestore fStore = FirebaseFirestore.getInstance();
            fStore.collection("users").document(fAuth.getCurrentUser().getUid())
                    .collection("carbon_dioxide").orderBy("year", Query.Direction.ASCENDING)
                    .orderBy("month", Query.Direction.ASCENDING).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    // dichiarazione array
                    ArrayList<ArrayList<Saving>> carbonDioxideArrayList;

                    // inizializza la lista
                    carbonDioxideArrayList = new ArrayList<>();
                    for (int i = 0; i < 8; i++) {
                        carbonDioxideArrayList.add(i, new ArrayList<Saving>());
                    }

                    // carica la lista
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Saving item = document.toObject(Saving.class);
                        carbonDioxideArrayList.get(item.getNtipo()).add(item);
                    }
                    setPieChartData("co2", carbonDioxideArrayList);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("LOG", "Error! " + e.getLocalizedMessage());
                }
            });

            // retrieve QUANTITA
            fStore = FirebaseFirestore.getInstance();
            fStore.collection("users").document(fAuth.getCurrentUser().getUid()).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            List<Integer> quantitaList = (List<Integer>) documentSnapshot.get("quantita");
                            int [] quantita = new int[7];
                            quantita = Ints.toArray(quantitaList);
                            setBarChartData(quantita);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("LOG", "Error! " + e.getLocalizedMessage());
                }
            });
        } else {
            setPieChartData("co2", MainActivity.CARBON_DIOXIDE_ARRAY_LIST);
            setBarChartData(MainActivity.QUANTITA);
        }
    }

    private void setPieChartData(String type, ArrayList<ArrayList<Saving>> arrayOfArray){
        pieChart.clearChart();
        String unit;
        // aggiorna la descrizione
        if (type.equals("co2")) {
            tvSaving.setText(Html.fromHtml("Hai risparmiato (in CO<sub><small><small>2</small></small></sub>):"));
            unit = " g";
        } else {
            if (type.equals("petrolio")) {
                unit = " g";
            } else {
                unit = " Wh";
            }
            tvSaving.setText("Hai risparmiato (in " + type +"):");
        }

        for (int i = 0; i < 8; i++) {
            ArrayList<Saving> arrayList = arrayOfArray.get(i);
            int punteggio = 0;

            if (!arrayList.isEmpty()) {
                for (Saving item : arrayList) {
                    punteggio += item.getPunteggio();
                }
            }

            // aggiungi una slice alla PieChart
            pieChart.addPieSlice(new PieModel(Integer.parseInt((punteggio) + ""), colors.get(i)));

            // aggiorna le TextView
            switch (i) {
                case 0:
                    tvPlastica.setText(punteggio + unit);
                    break;
                case 1:
                    tvOrganico.setText(punteggio + unit);
                    break;
                case 2:
                    tvSecco.setText(punteggio + unit);
                    break;
                case 3:
                    tvCarta.setText(punteggio + unit);
                    break;
                case 4:
                    tvVetro.setText(punteggio + unit);
                    break;
                case 5:
                    tvMetalli.setText(punteggio + unit);
                    break;
                case 6:
                    tvElettrici.setText(punteggio + unit);
                    break;
                case 7:
                    tvSpeciali.setText(punteggio + unit);
                    break;
            }
        }

        pieChart.setUsePieRotation(false);
        // animate the PieChart
        pieChart.startAnimation();
    }

    private void setBarChartData(int[] quantita) {
        for (int i = 0; i < 8; i++) {
            barChart.addBar(new BarModel(quantita[i] + "", (float) quantita[i], colors.get(i)));
        }
        barChart.startAnimation();
    }
}
