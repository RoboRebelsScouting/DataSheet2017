package com.roborebelsscouting;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by elmhursts20 on 2/19/2017
 */
public class RobotData {
    public int robotNumber;

    public List<Integer> matchList;

    public int matches;

    public DataGroup gears;

    public DataGroup teleGears;
    public DataGroup autoGears;

    public DataGroup autoCross;

    public DataGroup lowShots;

    public DataGroup highAttempt;

    public DataGroup climb;

    public DataGroup autoLowShots;
    public DataGroup autoHighShots;

    public double accuracy;
    public int accuracyRank;
    public int approaches;

    public RobotData() {
        robotNumber = 0;
        matches = 0;
        gears = new DataGroup();
        teleGears = new DataGroup();
        autoGears = new DataGroup();
        autoCross = new DataGroup();
        lowShots = new DataGroup();
        highAttempt = new DataGroup();
        climb = new DataGroup();
        autoLowShots = new DataGroup();
        autoHighShots = new DataGroup();

        accuracy = 0;
        accuracyRank = 0;

        matchList = new ArrayList<Integer>();
    }
}
