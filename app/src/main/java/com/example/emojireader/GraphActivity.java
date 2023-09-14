package com.example.emojireader;

import static java.time.LocalDateTime.ofEpochSecond;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class GraphActivity extends AppCompatActivity
{
    /*
    Hash table of all emojis

    key:
        <key> emoji in Unicode format (int)
    vals:
        <timeList> ArrayList of time sent (Long)
        <rawEmoji> Actual string of emoji for printing (String)
     */
    private HashTable log;

    /*
    Array of top 10 most-used emojis, and their usage stats
     */
    private Record[] topTen;

    /*
    Graphical elements
     */
    private TextView infoText;
    private GraphView graph;

    /*
    onCreate():
     */

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        File path = getApplicationContext().getFilesDir();
        try {
            log = new HashTable(5000, this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        topTen = new Record[10];

        infoText = findViewById(R.id.infoText);
        graph = findViewById(R.id.graphView);

        try {
            readSMS();
        } catch (Exception e) {
            // if this is being thrown, there is a problem with the HashTable accessing
            e.printStackTrace();
        }

        findTopTen();

        try {
            graphData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    readSMS(): Will read through all SMS messages present on the phone, and parse through them. If emojis are found, they are stored in the
    emojiLog (this is assumed to be initialized).

    -no parameters
    -no return
     */

    private void readSMS() throws Exception
    {
        Cursor cursor = getContentResolver().query(Uri.parse("content://sms"),
                null, null, null, null);

        long currTime = System.currentTimeMillis();
        if(cursor.moveToFirst()) // if we have a message
        {
            do {
                if(cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.TYPE)) // if the message is sent by the user
                        .equals(String.valueOf(Telephony.Sms.MESSAGE_TYPE_SENT)))
                {
                    String text = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)); // content of message
                    Long time = Long.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.DATE))); // time of message
                    parseText(text, time); // parses & adds to hash table accordingly
                }

            } while (cursor.moveToNext()); // keep going until out of messages
        }
    }

    /*
    parseText(): Helper method for readSMS(). Takes in an SMS message (its contents and its time) and parses through
    it. It stores any found emojis in the hash table.
     */

    private void parseText(String text, Long time)
    {
        for(int i = 0; i < text.length(); i++)
        {
            if(text.codePointAt(i) >= 169) // lowest 'emoji'; potentially have an emoji!
            {
                /*
                Try adding the emoji. If it doesn't exist in the hash table, print out the error message
                and continue normally. If it does exist, we need to advance an additional character forward
                because emojis take up two character slots
                 */

                try {
                    log.addEmojiInstance(text.codePointAt(i), time);
                } catch (Exception e) {
                    //System.out.println("!!!: " + text.codePointAt(i) + " text: " + text);
                    i--;
                } finally {
                    i++;
                }
            }
        }
    }

    /*
    graphData()
     */

    private void graphData() throws Exception
    {
        /*
        Setup graph
         */

        // x axis bounds
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(TimeInstance.minTime);
        graph.getViewport().setMaxX(TimeInstance.maxTime);

        // x axis date labels
        graph.getGridLabelRenderer().setHumanRounding(false);
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this));
        graph.getGridLabelRenderer().setNumHorizontalLabels(3); // only 3 because of the space

        // legend
        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setFixedPosition(0,0);
        graph.getLegendRenderer().setTextSize(60);

        // 10 colors
        int[] colors = {
                Color.rgb(212, 175, 55), // gold
                Color.rgb(192, 192, 192), //silver
                Color.rgb(176,141,87), //bronze
                Color.rgb(234,153,153), // light red
                Color.rgb(227,222,219), // chrome
                Color.rgb(182,215,168), // light green
                Color.rgb(162,196,201), // light blue
                Color.rgb(180,167,214), // light purple
                Color.rgb(238,215,161), // cream
                //Color.rgb(205,139,98), // orange
                Color.rgb(236,177,169), // salmon
        };

        /*
        Graph them!
         */
        for(int i = 0; i < 10; i++)
        {
            Record currRec = topTen[i];
            int currColor = colors[i];

            String emoji = currRec.rawEmoji; // our current emoji
            ArrayList<TimeInstance> list = currRec.timeList; // our ArrayList related to that emoji (contains time of sending)
            if(list.size() > 0)
            {
                /*
                Create graph
                */
                LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
                //series.setDrawDataPoints(true);
                series.setTitle(emoji);
                series.setThickness(12-i);
                series.setColor(currColor);
                int height = 0;
                for(int instance = currRec.timeList.size()-1; instance >= 0; instance--)
                {
                    Date d = new Date(list.get(instance).getRawTime());
                    series.appendData(new DataPoint(list.get(instance).getRawTime(), height), true, currRec.timeList.size());
                    height++;
                }

                graph.addSeries(series);
            }
        }
    }

    private void findTopTen()
    {
        Record currRec = log.getRecordSequential(0);
        int i = 0;
        while(currRec.key != -1) {
            for (int j = 0; j < 10; j++)
            {
                if (topTen[j] == null) // empty available spot
                {
                    topTen[j] = currRec;
                    break;
                } else if (currRec.timeList.size() > topTen[j].timeList.size()) // replacing & shifting
                {
                    for (int k = 8; k >= j; k--) // shift everything down by one
                    {
                        topTen[k + 1] = topTen[k];
                    }
                    topTen[j] = currRec;
                    break;
                }
            }
            currRec = log.getRecordSequential(++i);
        }
    }
}