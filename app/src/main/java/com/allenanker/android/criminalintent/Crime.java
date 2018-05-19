package com.allenanker.android.criminalintent;


import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;

import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import static android.icu.text.DateFormat.DEFAULT;
import static android.icu.text.DateFormat.SHORT;

public class Crime {

    private UUID mId;
    private String mTitle;
    private Date mDate;
    private boolean mSolved;
    private boolean mRequiresPolice;
    private String mSuspect;

    public Crime() {
        this(UUID.randomUUID());
    }

    public Crime(UUID id) {
        mId = id;
        mDate = new Date();
    }

    public UUID getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }

    public boolean isSolved() {
        return mSolved;
    }

    public void setSolved(boolean solved) {
        mSolved = solved;
    }

    public boolean isRequiresPolice() {
        return mRequiresPolice;
    }

    public void setRequiresPolice(boolean requiresPolice) {
        mRequiresPolice = requiresPolice;
    }

    public String getSuspect() {
        return mSuspect;
    }

    public void setSuspect(String suspect) {
        mSuspect = suspect;
    }

    public String getDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE", Locale.CHINA);
        Date date = getDate();
        String dayOfWeek = sdf.format(date);
        String dateString = dayOfWeek + ", " + DateFormat.getDateInstance(DEFAULT, Locale.CHINA).format(date);

        return dateString;
    }
}
