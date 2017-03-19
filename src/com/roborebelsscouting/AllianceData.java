package com.roborebelsscouting;

/**
 * Created by jelmhurst on 3/19/2017.
 */
public class AllianceData {
    int robot1;
    int robot2;
    int robot3;

    // these numbers are combined averages for the alliance
    double avgAutoFuel;
    double avgAutoGear;
    double avgTeleFuel;
    double avgTeleGear;
    double avgTeleClimb;

    // strength of alliance
    double allianceStrength;

    // given the 5 factors, calculate a strength number
    public void calcStrength() {
        double strength = 0.0;

        double totalGears = avgAutoGear + avgTeleGear;
        int rotors = 0;
        int autoRotors = 0;

        if (avgAutoGear >= 1.0 && avgAutoGear < 3.0) {
            strength += 60;
            autoRotors = 1;
        }
        if (avgAutoGear == 3.0) {
            strength += 60;
            autoRotors = 2;
        }

        // gears needed for rotors:
        // 1 gear = 1 rotor
        // 3 gears = 2 rotors
        // 7 gears = 3 rotors
        // 13 gears = 4 rotors

        if (totalGears >= 13) {
            rotors = 4;
        } else if (totalGears >= 7) {
            rotors = 3;
        } else if (totalGears >= 3) {
            rotors = 2;
        } else if (totalGears >= 1) {
            rotors = 1;
        } else {
            rotors = 0;
        }

        int teleRotors = rotors - autoRotors;
        // 40 points for each rotor in tele
        strength += (teleRotors * 40);

        strength += avgAutoFuel;
        strength += avgTeleFuel;
        int climbPoints = (int)(avgTeleClimb) * 50;
        strength += climbPoints;

        allianceStrength = strength;
    }

}

