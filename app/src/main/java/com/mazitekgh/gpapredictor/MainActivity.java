package com.mazitekgh.gpapredictor;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
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

    private static final double STEP = 0.05;
    //colors to be used for different semester plot
    private final int[] colors = {Color.BLACK, Color.YELLOW, Color.GREEN, Color.BLUE,
            Color.CYAN, Color.MAGENTA, Color.DKGRAY, Color.LTGRAY, Color.BLUE, Color.YELLOW};
    private EditText etGpa;
    private GraphView graph;
    private Button predictButton;
    private TextView tvShowOutput;
    private int pos = 2;
    private double userGpa = 0.0;
    @NonNull
    //Stores user input
    private PointsGraphSeries<DataPoint> pointClick = new PointsGraphSeries<>();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        graph = (GraphView) findViewById(R.id.graph);
        graph.getGridLabelRenderer().setVerticalAxisTitle(getString(R.string.your_gpa));
        graph.getGridLabelRenderer().setHorizontalAxisTitle(getString(R.string.new_gpa));
        etGpa = (EditText) findViewById(R.id.etGPA_id);
        Spinner spSemester = (Spinner) findViewById(R.id.spSemester_id);
        predictButton = (Button) findViewById(R.id.PredictButton_id);
        tvShowOutput = (TextView) findViewById(R.id.tvToast);
        spSemester.setOnItemSelectedListener(this);

        ArrayAdapter<CharSequence> semesters =
                ArrayAdapter.createFromResource(this, R.array.select_semester,
                        R.layout.support_simple_spinner_dropdown_item);


        spSemester.setAdapter(semesters);



        predictButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etGpa.getText().toString().isEmpty()) {
                    etGpa.setError(getString(R.string.Enter_Previous_gpa));
                } else {
                    userGpa = Float.parseFloat(etGpa.getText().toString());
                    if (pos == 0) {

                        Toast.makeText(getApplicationContext(), R.string.please_select_semester,
                                Toast.LENGTH_SHORT).show();

                    } else {
                        initGraph(graph, userGpa, pos);
                    }
                }
            }
        });

        // initGraph(graph);
    }

    /**
     * used to plot the graph
     *
     * @param graph    the GraphView to plot the graph on
     * @param myOldGpa the previous GPA.
     * @param mySem    the new semester to compute its GPA
     */
    private void initGraph(@NonNull final GraphView graph, Double myOldGpa, int mySem) {
        double[] oldGpa = getGpaValues(STEP);
        double[] newGpa = getCgpaValues(myOldGpa, mySem);
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
                tvShowOutput.setText("CGPA = " + String.format("%.2f", dataPoint.getY()) + " if you get "
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

    /**
     * Generate gpa values from 0 to 4 a step of {@Link step}
     *
     * @param step double specify increment
     * @return the computed GPA in an array of type double
     */
    @NonNull
    private double[] getGpaValues(double step) {
        int N = (int) (4 / step) + 1;
        double[] result = new double[N];
        result[0] = 0;
        for (int i = 1; i < N; i++) {
            result[i] = result[i - 1] + step;
        }
        return result;
    }

    /**
     * calculate the cumulative GPA using the current using the previous CGPA{@Link oldGPA}
     * and the current semester {@Link sem}
     * @param oldGPA the previous gpa
     * @param sem the semester you want to compute it gpa
     * @return array of CGPA values of type double
     */
    @NonNull
    private double[] getCgpaValues(double oldGPA, int sem) {

        double[] x = getGpaValues(STEP);
        int N = x.length;
        double[] y = new double[N];

        for (int i = 0; i < N; i++) {
            y[i] = (1 / (double) sem) * (x[i] + ((double) sem - 1) * (oldGPA));
        }
        return y;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        /*
         Save values on configuration change
          */
        outState.putDouble(GPA, userGpa);
        outState.putInt(SEM, pos);


    }

    @Override
    protected void onRestoreInstanceState(@Nullable Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        /*
        Restore Values after Configuration change
         */
        if (savedInstanceState != null) {
            userGpa = savedInstanceState.getDouble(GPA);
            pos = savedInstanceState.getInt(SEM);
            predictButton.performClick();
            //initGraph(graph, userGpa, pos);
        }

    }

    /**
     * Indicate the point on the graph the user selected
     * @param x x coordinate of the user selected point
     * @param y y coordinate  of the user selected point
     */
    private void getUserSelectedPoint(double x,double y) {

        DataPoint[] pnt = {new DataPoint(x, y)};
        if (pointClick.isEmpty()) {
            pointClick = new PointsGraphSeries<>(pnt);
            graph.addSeries(pointClick);
            pointClick.setSize(14);
            pointClick.setColor(Color.RED);

        } else {
            pointClick.resetData(pnt);
        }


    }

    /**
     * method to hide the keyboard
     * @param view the current view
     */
    private void hideKeyboard(@Nullable View view) {
        if (view != null) {
            view.clearFocus();
            InputMethodManager imm =
                    (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
