package com.example.emojireader;

import java.util.ArrayList;

public class Record
{
    /*
    key:
        Emoji in Character.getName() format (int)
    */
    public int key;

    /*
    vals:
        ArrayList of time sent (ArrayList<Long>)
        Actual string of emoji for printing (String)
     */
    public ArrayList<TimeInstance> timeList;
    public String rawEmoji;

        /*
        constructor Record(): will take in a string containing the raw emoji and create a corresponding ArrayList that will store
        individual uses of that emoji (the time it is used)

        -String (the raw emoji, which will be stored, and also converted to a parsable string and stored as the key)
        -no return
         */

    public Record(String e)
    {
        key = e.codePointAt(0);
        timeList = new ArrayList<TimeInstance>();
        rawEmoji = e;
    }

    public Record()
    {
        key = -1;
        timeList = new ArrayList<TimeInstance>();
        rawEmoji = "";
    }
}
