package com.ovi.skyblockconnect.utilities;

import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HypixelTimers {
    private static final long dayDuration = 20 * 60;
    static long birthday = 1559790900;
    private final String eventName;
    private final long eventTimer;

    public HypixelTimers(String eventName, long eventTimer) {
        this.eventName = eventName;
        this.eventTimer = eventTimer;
    }

    public static HypixelTimers[] getEventsList() {
        return new HypixelTimers[]{
                new HypixelTimers("<font color='#19bf8d'>Jacob Contest:</font>", getJacobStartTimer()),
                new HypixelTimers("<font color='#db6b6f'>Dark Auction:</font>", getDarkAuctionStartTimer()),
                new HypixelTimers("<font color='#FFD966'>Bank Interest:</font>", getIntEventTimer(0, 1, 1, 0, 0)),
                new HypixelTimers("<font color='#FFD966'>Bank Interest:</font>", getIntEventTimer(0, 4, 1, 0, 0)),
                new HypixelTimers("<font color='#FFD966'>Bank Interest:</font>", getIntEventTimer(0, 7, 1, 0, 0)),
                new HypixelTimers("<font color='#FFD966'>Bank Interest:</font>", getIntEventTimer(0, 10, 1, 0, 0)),
                new HypixelTimers("<font color='#F4B183'>Traveling Zoo:</font>", getIntEventTimer(0, 4, 1, 0, 3)),
                new HypixelTimers("<font color='#F4B183'>Traveling Zoo:</font>", getIntEventTimer(0, 10, 1, 0, 3)),
                new HypixelTimers("<font color='#FFACAC'>Next Mayor:</font>", getIntEventTimer(0, 3, 27, 0, 0)),
                new HypixelTimers("<font color='#95BDFF'>Election Starts:</font>", getIntEventTimer(0, 6, 27, 0, 0)),
                new HypixelTimers("<font color='#db9044'>Spooky Fishing:</font>", getIntEventTimer(0, 8, 26, 0, 9)),
                new HypixelTimers("<font color='#E5BA73'>Spooky Festival:</font>", getIntEventTimer(0, 8, 29, 0, 3)),
                new HypixelTimers("<font color='#7FE9DE'>Jerry Island:</font>", getIntEventTimer(0, 12, 1, 0, 31)),
                new HypixelTimers("<font color='#B6E2A1'>Season of Jerry:</font>", getIntEventTimer(0, 12, 24, 0, 3)),
                new HypixelTimers("<font color='#7e5aad'>New Year Celebration:</font>", getIntEventTimer(0, 12, 29, 0, 3))

        };
    }

    public static HypixelTimers[] EventsList() {
        HypixelTimers[] eventsList = getEventsList();
        Arrays.sort(eventsList, Comparator.comparingLong(HypixelTimers::getEventTimer));
        return eventsList;
    }

    public static long getIntEventTimer(long year, long month, long day, long hour, long durationInDays) {
        long eventTimeInSeconds = birthday + hour * 50 + day * dayDuration + month * dayDuration * 31 + year * dayDuration * 31 * 12
                + (year <= 0 ? (((System.currentTimeMillis() / 1000 - birthday) / (dayDuration * 31 * 12) - 1) * 12 * 31 * 60 * 20) : 0);
        while (eventTimeInSeconds + (durationInDays * 20 * 60) <= (System.currentTimeMillis() / 1000))
            eventTimeInSeconds += 12 * 31 * 60 * 20;
        return (eventTimeInSeconds - System.currentTimeMillis() / 1000);
    }

    public static long getJacobStartTimer() {
        long DATime = 15; // alarm always in the 15th minute
        long secondsSinceLastFullHour = ((System.currentTimeMillis() / 1000) % 3600);
        long tmp_res = (DATime * 60 - secondsSinceLastFullHour);
        return (tmp_res <= 0) ? tmp_res + 3600 : tmp_res;
    }

    public static long getDarkAuctionStartTimer() {
        long DATime = 55; // alarm always in the 55th minute
        long secondsSinceLastFullHour = ((System.currentTimeMillis() / 1000) % 3600);
        long tmp_res = (DATime * 60 - secondsSinceLastFullHour);
        return (tmp_res <= 0) ? tmp_res + 3600 : tmp_res;
    }

    public static void main(String[] args) {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
        }, 0, 1, TimeUnit.HOURS);
    }

    public static String getGameTime() {
        long SkyblockAge = (System.currentTimeMillis() / 1000) - birthday;
        long years = SkyblockAge / (dayDuration * 31 * 12);
        long months = (SkyblockAge / (dayDuration * 31)) - years * 12;
        long days = SkyblockAge / dayDuration - (months * 31 + years * 12 * 31);
        long hours = SkyblockAge / 50 - (days * 24 + months * 31 * 24 + years * 12 * 31 * 24);
        return years + "-" + months + "-" + days + " " + (hours > 12 ? hours - 12 : hours) + ":00" + (hours >= 12 ? "pm" : "am");
    }

    public String getEventName() {
        return eventName;
    }

    public long getEventTimer() {
        return eventTimer;
    }

}