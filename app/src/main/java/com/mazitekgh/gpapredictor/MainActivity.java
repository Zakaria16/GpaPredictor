package com.mazitekgh.gpapredictor;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.PointsGraphSeries;
import com.jjoe64.graphview.series.Series;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String SEM = "SEM_KEY";
    private static final String GPA = "GPA_KEY";
    private EditText etGpa;

    private GraphView graph;
    private Button predictButton;
    private TextView tvToast;
    private int pos = 2;
    private double userGpa = 0.0;
    @NonNull
    private PointsGraphSeries<DataPoint> pg=new PointsGraphSeries<>();
    private final int[] colors = {Color.BLACK,Color.YELLOW,Color.GREEN,Color.BLUE,
    Color.CYAN,Color.MAGENTA,Color.DKGRAY,Color.LTGRAY,Color.BLUE,Color.YELLOW};



    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        graph = (GraphView) findViewById(R.id.graph);
        etGpa = (EditText) findViewById(R.id.etGPA_id);
        Spinner spSemester = (Spinner) findViewById(R.id.spSemester_id);
        predictButton = (Button) findViewById(R.id.PredictButton_id);
        tvToast = (TextView) findViewById(R.id.tvToast);
        spSemester.setOnItemSelectedListener(this);

        ArrayAdapter<String> semesters = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item);
        semesters.addAll("Select a Semester", "Level 100 Sem 1", "Level 100 Sem 2",
                "Level 200 Sem 1", "Level 200 Sem 2",
                "Level 300 Sem 1", "Level 300 Sem 2",
                "Level 400 Sem 1", "Level 400 Sem 2");

        spSemester.setAdapter(semesters);
        etGpa.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    float x = Float.parseFloat(etGpa.getText().toString());
                    if (x > 4 || x < 0) {
                        Toast.makeText(getApplicationContext(), "GPA must be \n between 0 and 4",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


        predictButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etGpa.getText().toString().isEmpty()) {
                    etGpa.setError(getString(R.string.Enter_Previous_gpa));
                } else {
                    userGpa = Float.parseFloat(etGpa.getText().toString());
                    if (userGpa == 0 && pos == 0) {

                        Toast.makeText(getApplicationContext(), R.string.Enter_values,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    initGraph(graph, userGpa, pos);

                }
            }
        });

        // initGraph(graph);
    }


    private void initGraph(@NonNull final GraphView graph, Double myOldGpa, int mySem) {
        double[] oldGpa = getGpaValues();
        double[] newGpa = getNewGpaValues(myOldGpa, mySem);
        DataPoint[] gpa = new DataPoint[oldGpa.length];
        for (int i = 0; i < oldGpa.length; i++) {
            gpa[i] = new DataPoint(oldGpa[i], newGpa[i]);
        }
        // first series is a line

        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(gpa);
        series.setDrawBackground(true);
        series.setAnimated(true);
        series.setDrawDataPoints(true);
        series.setColor(colors[pos]);
        series.setTitle("Sem "+pos +" Prediction");

        // enable scaling


        // set manual X bounds
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(4);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(4);
        graph.getViewport().setScalable(true);
        graph.getViewport().setScalableY(true);
        // enable scrolling
        graph.getViewport().setScrollable(true);


        graph.addSeries(series);
        series.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, @NonNull DataPointInterface dataPoint) {
                /*
                Toast.makeText(graph.getContext(), "if you get " + (float) dataPoint.getX()
                                + " this " + pos + " Semester\n"
                                + "Your new GPA wil be "
                                + (float) dataPoint.getY()
                        , Toast.LENGTH_LONG).show();
                        */
                tvToast.setText("CGPA = "+String.format("%.2f",dataPoint.getY()) +" if you get "
                        +String.format("%.2f",dataPoint.getX()) +" this "+pos +" Semester");


        getUserSelectedPoint(dataPoint.getX(),dataPoint.getY());

            }
        });


        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        pos = position;
        hideKeyboard(view);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @NonNull
    private double[] getGpaValues() {
        double[] result = new double[81];
        result[0] = 0;
        for (int i = 1; i < 81; i++) {
            result[i] = result[i - 1] + 0.05;
        }
        return result;
    }

    @NonNull
    private double[] getNewGpaValues(double oldGPA, int sem) {
        double[] y = new double[81];
        double[] x = getGpaValues();
        for (int i = 0; i < 81; i++) {
            y[i] = (1 / (double) sem) * (x[i] + ((double) sem - 1) * (oldGPA));
        }
        return y;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putDouble(GPA, userGpa);
        outState.putInt(SEM, pos);


    }

    @Override
    protected void onRestoreInstanceState(@Nullable Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            userGpa = savedInstanceState.getDouble(GPA);
            pos = savedInstanceState.getInt(SEM);
            predictButton.performClick();
            //initGraph(graph, userGpa, pos);
        }

    }

    private void getUserSelectedPoint(double x,double y)
    {

            DataPoint[] pnt = {new DataPoint(x,y)};
        if(pg.isEmpty()) {
            pg = new PointsGraphSeries<>(pnt);
            graph.addSeries(pg);
            pg.setSize(14);
            pg.setColor(Color.RED);

        }
        else{
            pg.resetData(pnt);
        }




    }

    private void hideKeyboard(@Nullable View view) {
        if (view != null) {
            view.clearFocus();
            InputMethodManager imm =
                    (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
