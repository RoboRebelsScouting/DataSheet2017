package com.roborebelsscouting;

/**
 * Created by elmhursts20 on 2/19/2017
 */
public class RobotData {
    public int robotNumber;

    public int matches;

    public DataGroup gears;

    public DataGroup autoGears;

    public DataGroup autoCross;

    public DataGroup lowShots;

    public DataGroup highAttempt;

    public DataGroup climb;

    public double accuracy;
    public int accuracyRank;

    public RobotData() {
        robotNumber = 0;
        matches = 0;
        gears = new DataGroup();
        autoGears = new DataGroup();
        autoCross = new DataGroup();
        lowShots = new DataGroup();
        highAttempt = new DataGroup();
        climb = new DataGroup();
        accuracy = 0;
        accuracyRank = 0;
    }
}
