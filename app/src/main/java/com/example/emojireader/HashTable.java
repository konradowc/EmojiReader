package com.example.emojireader;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;

/*
Does not have full functionality of a hash table,
since we will not need to remove values
 */
public class HashTable
{
    private Record[] table; // our hash table
    private int M; // size
    private int getRecSeqIndex; // getRecordSequential() helper variable



    public HashTable(int M, Context c) throws IOException
    {
        table = new Record[M];
        this.M = M;
        getRecSeqIndex = 0;

        /*
        Initialize values from text file
         */

        // fill up with empty records
        for(int i = 0; i < M; i++)
        {
            // empty records
            table[i] = new Record();
        }

        // fill up with emojis in the file
        InputStream is = c.getAssets().open("EmojiList.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        while((line = reader.readLine()) != null)
        {
            try {
                insert(line);
            } catch (Exception e) {
                System.out.println("!!! Unable to initialize hash table");
            }
        }

        //insert("😔"); // pensive
        return;
    }

    /*
    insert():
     */

    public void insert(String emoji) throws Exception
    {
        int unicode = emoji.codePointAt(0);
        int hashVal = hash(unicode);
        /*
        keep trying to insert
         */
        for(int i = 0; i < M; i++)
        {
            Record currRecord = table[(hashVal + probe(unicode, i)) % M];
            if(currRecord.key == -1) // empty
            {
                // records are already created, just need to set them
                table[(hashVal + probe(unicode, i)) % M].key = unicode;
                table[(hashVal + probe(unicode, i)) % M].rawEmoji = emoji;
                // do not need to set timeList, because these are automatically generated to be empty lists,
                // which can then be added to using addEmojiInstance()
                return;
            }
        }

        // cannot insert
        throw new Exception();
    }

    public ArrayList<TimeInstance> getList(int target) throws Exception
    {
        int hashVal = hash(target);
        /*
        keep trying to find
         */
        for(int i = 0; i < M; i++)
        {
            Record currRecord = table[(hashVal + probe(target, i)) % M];
            if(currRecord.key == target) // our key
            {
                return currRecord.timeList;
            }
        }

        throw new Exception(); // unable to find
    }

    /*
    getRecordSequential(): find sequential logical records
    NOTE: need to use in a for loop from 0-end, since it relies on optimization
    that allows it to skip some checking. To reset it, pass in an index of 0
     */

    public Record getRecordSequential(int index) // logical target index
    {
        if(index == 0)
            getRecSeqIndex = 0;

        int count = -1; // logical current index
        int i = getRecSeqIndex-1; // hash table index
        while(index != count)
        {
            i++; // at beginning, this sets it to 0

            if(i == M) // we've gone too far!
            {
                return new Record();
            }
            else if(table[i].key != -1) // we have a valid record
            {
                count++;
            }
        }

        return table[i]; // we have essentially found table[index], except we skipped all the empty records
    }

    public void addEmojiInstance(int target, Long time) throws Exception
    {
        int hashVal = hash(target);
        /*
        keep trying to find
         */
        for(int i = 0; i < M; i++)
        {
            Record currRecord = table[(hashVal + probe(target, i)) % M];
            if(currRecord.key == target) // our key
            {
                currRecord.timeList.add(new TimeInstance(time));
                return;
            }
        }

        // unable to find the target emoji in hash table; potentially not a problem
        throw new Exception();
    }

    // mid-square method
    private int hash(int key)
    {
        String square = String.valueOf((long) key * (long) key);
        String midsquare = square;
        if(square.length() > 5) // we can actually make it a midsquare
        {
            int mid = square.length() / 2;
            midsquare = square.substring(mid-2, mid+1);
        }

        return Integer.parseInt(midsquare) % M;
    }

    private int probe(int key, int i)
    {
        return i;
    }
}
