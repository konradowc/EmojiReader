package com.example.emojireader;

public class TimeInstance
{
    static Long minTime = 100000000000000L; // well above any reasonable time
    static Long maxTime = 0L;

    private Long time;

    public TimeInstance(Long t)
    {
        /*
        TimeInstance() is only called when a new element is being added, so we can check min & max
        at this time
         */
        if(t < minTime)
            minTime = t;
        if(t > maxTime)
            maxTime = t;

        time = t;
    }

    public Long getRawTime()
    {
        return time;
    }
}
