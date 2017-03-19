package com.roborebelsscouting;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Main {

    public static String userDir = System.getProperty("user.home");
    public static String dataSheetDir = userDir + File.separator + "Documents" + File.separator + "Datasheets";
    public Writer writer = null;
    public ArrayList<RobotData> robotList = new ArrayList<RobotData>();
    public void createHeader(int robotNumber) {
        String outString = "<html>\n";
        outString += "<title>" + robotNumber + " " + "Robot Stats</title>\n";
        outString += "<h1>" + robotNumber + " " + "Robot Stats</h1>" + "\n";

        try {
            writer.write(outString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void createStyle() {
        String outString = "<style>\n";
        outString += "table, th, td {\n";
        outString += "    border: 1px solid black;\n";
        outString += "    border-collapse: collapse;\n";
        outString += "}\n";
        outString += "</style>" + "\n\n";

        try {
            writer.write(outString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void createTableCategoryRow(String tableText, DataGroup dg) {
        String outString = "<tr>" + "\n";
        outString += "<td>" + tableText + "</td>\n";
        outString += "<td>" + String.format("%.2f", dg.avg) + "</td>\n";
        outString += "<td>" + String.format("%d", dg.rank) + "</td>\n";
        outString += "</tr>\n";

        try {
            writer.write(outString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createTableHeader() {
        String outString = "<tr>" + "\n";
        outString += "<th>" + "Category" + "</th>\n";
        outString += "<th>" + "Average" + "</th>\n";
        outString += "<th>" + "Rank" + "</th>\n";
        outString += "</tr>\n";

        try {
            writer.write(outString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // write your code here
        new Main().getDataFromDB();
    }

    public void getDataFromDB() {

        // make directory if not found
        File dataSheetDirFile = new File(dataSheetDir);
        if (dataSheetDirFile.exists() == false) {
            dataSheetDirFile.mkdir();
        }

        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("My SQL JDBC Driver Not Registered?");
            e.printStackTrace();
            return;
        }
        System.out.println("Getting Data from SQL Database");

        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/roborebels", "root", "roborebels1153");
            Statement stmt;
            ResultSet rs;

            //create hashmap of data
            stmt = conn.createStatement();

            //get match table, then create robots
            rs = stmt.executeQuery("SELECT * from matchdata");

            //process Data
            RobotData rd;
            while (rs.next()) {
                int rn = rs.getInt("RobotNumber");
                String matchName = rs.getString("matchNumber");
                Integer matchNumber = Integer.parseInt(matchName);

                if (haveRobot(rn)) {
                    // check to see if we have this match number already, if not, add it to the list and increase
                    // number of matches
                    if (getRobot(rn).matchList.contains(matchNumber) == false) {
                        getRobot(rn).matches++;
                        getRobot(rn).matchList.add(matchNumber);
                    }
                } else {
                    rd = new RobotData();
                    rd.robotNumber = rn;
                    rd.matches = 1;
                    rd.matchList.add(matchNumber);
                    robotList.add(rd);

                }
            }
            rs.close();
            rs = stmt.executeQuery("SELECT * from matchdata");
            while (rs.next()) {
                int rn = rs.getInt("RobotNumber");
                if (haveRobot(rn)) {
                    String gameEvent = rs.getString("gameEvent");
                    if (gameEvent.equals("crossBaselineAuto")){getRobot(rn).autoCross.total++;}
                    if (gameEvent.equals("climbed")){getRobot(rn).climb.total++;}
                    if (gameEvent.equals("gearPlacedAuto")){getRobot(rn).autoGears.total++;}
                    if (gameEvent.equals("gearPlacedTeleop")){getRobot(rn).teleGears.total++;}
                    if (gameEvent.equals("lowGoal")){getRobot(rn).lowShots.total++;}
                    if (gameEvent.equals("highGoal")){getRobot(rn).highAttempt.total++;}
                    if (gameEvent.equals("lowGoalAuto")){getRobot(rn).autoLowShots.total++;}
                    if (gameEvent.equals("highGoalAuto")){getRobot(rn).autoHighShots.total++;}
                    if (gameEvent.equals("gearPlacedTeleop") || gameEvent.equals("gearPlacedAuto")){getRobot(rn).gears.total++;}

                }
            }
            rs.close();
            stmt.close();

            //averages
            for (RobotData r : robotList) {
                r.autoLowShots.avg = (double) r.autoLowShots.total / r.matches;
                r.autoHighShots.avg = (double) r.autoHighShots.total / r.matches;
                r.autoCross.avg = (double) r.autoCross.total / r.matches;
                r.autoGears.avg = (double) r.autoGears.total / r.matches;
                r.teleGears.avg = (double) r.teleGears.total / r.matches;
                r.climb.avg = (double) r.climb.total / r.matches;
                r.gears.avg = (double) r.gears.total / r.matches;
                r.highAttempt.avg = (double) r.highAttempt.total / r.matches;
                r.lowShots.avg = (double) r.lowShots.total / r.matches;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        getRanks();

        for (RobotData r : robotList) {
            try {
                System.out.println("Robot Number: " + r.robotNumber);
                String fileName = dataSheetDir + File.separator + r.robotNumber + ".html";
                File oldFile = new File(fileName);

                if (oldFile.exists()) {
                    oldFile.delete();
                }
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName),"utf-8"));
                writer.write("<!doctype html>\n");
                createHeader(r.robotNumber);
                createStyle();

                writer.write("<body>\n");

                writer.write("<img src=\"" + r.robotNumber + ".jpg\" alt=\"ROBOT " + r.robotNumber + " is probably broken or invisible.\" style=\"width:304px;height:228px;\">");

                writer.write("<table>\n");
                createTableHeader();
                createTableCategoryRow("Auto Crossing", r.autoCross);
                createTableCategoryRow("Auto Gear Scoring", r.autoGears);
                createTableCategoryRow("Climbing", r.climb);
                createTableCategoryRow("Gear Scoring", r.gears);
                createTableCategoryRow("High Shooting", r.highAttempt);
                createTableCategoryRow("Low Shooting", r.lowShots);
                writer.write("</table>\n");

                writer.write("Matches: " + r.matches);

                writer.write("</body>\n");
                writer.write("</html>\n");

                writer.close();

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        // write a csv of all possible combinations of alliances with us, team 1153
        List<Integer> teamList = new ArrayList<Integer>();
        for (RobotData r : robotList) {
            if (r.robotNumber != 1153) {
                teamList.add(r.robotNumber);
            }
        }

        List<AllianceData> adList = new ArrayList<AllianceData>();

        try {
            String fileName = dataSheetDir + File.separator + "allianceData.csv";
            File oldFile = new File(fileName);

            if (oldFile.exists()) {
                oldFile.delete();
            }
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "utf-8"));

            String outputString;
            writer.write("robot1,robot2,robot3,Average Auto Fuel,Average Tele Fuel, Average Auto Gear, Average Tele Gear, Average Tele Climb, Predicted Score\n");

            for (Integer t2 : teamList) {
                for (Integer t3 : teamList) {
                    if (t3 != t2) {
                        AllianceData ad = new AllianceData();
                        ad.robot1 = 1153;
                        ad.robot2 = t2;
                        ad.robot3 = t3;

                        // create the combined averages
                        // in auto low shots are worth 1/3 point, high shots = 1pt
                        ad.avgAutoFuel = (getRobot(1153).autoLowShots.avg + getRobot(t2).autoLowShots.avg + getRobot(t3).autoLowShots.avg) / 3 +
                                (getRobot(1153).autoHighShots.avg + getRobot(t2).autoHighShots.avg + getRobot(t3).autoHighShots.avg);
                        ad.avgTeleFuel = (getRobot(1153).lowShots.avg + getRobot(t2).lowShots.avg + getRobot(t3).lowShots.avg) / 9 +
                                (getRobot(1153).autoHighShots.avg + getRobot(t2).autoHighShots.avg + getRobot(t3).autoHighShots.avg) / 3;
                        ad.avgAutoGear = getRobot(1153).autoGears.avg + getRobot(t2).autoGears.avg + getRobot(t3).autoGears.avg;
                        ad.avgTeleGear = getRobot(1153).teleGears.avg + getRobot(t2).teleGears.avg + getRobot(t3).teleGears.avg;
                        ad.avgTeleClimb = getRobot(1153).climb.avg + getRobot(t2).climb.avg + getRobot(t3).climb.avg;

                        ad.calcStrength();

                        outputString = ad.robot1 + "," + ad.robot2 + "," + ad.robot3 + "," +
                                String.format("%.2f,%.2f,%.2f,%.2f,%.2f,%.1f,",ad.avgAutoFuel,
                                        ad.avgTeleFuel,
                                        ad.avgAutoGear,
                                        ad.avgTeleGear,
                                        ad.avgTeleClimb,
                                        ad.allianceStrength) + "\n";
                        writer.write(outputString);

                    }
                }
            }

            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // check the robot list to see if we have a robot already with the given number
    public boolean haveRobot(int robotNumber) {
        for (RobotData r : robotList) {
            if (r.robotNumber == robotNumber) {
                return true;
            }
        }
        return false;
    }

    // get the robot with the given robotNumber from the list
    // or return null
    public RobotData getRobot(int robotNumber) {

        for (RobotData r : robotList) {
            if (r.robotNumber == robotNumber) {
                return r;
            }
        }
        return null;
    }

    public void getRanks() {
        // rank the robots based on average alliance score
        ArrayList<RobotData> rankList = new ArrayList<RobotData>();
        for (RobotData r : robotList) {
            rankList.add(r);
        }

        Collections.sort(rankList, new Comparator<RobotData>() {
            public int compare(RobotData o1, RobotData o2) {
                if (o1.lowShots.avg == o2.lowShots.avg)
                    return 0;
                return o1.lowShots.avg > o2.lowShots.avg ? -1 : 1;
            }
        });
        // now loop through the lists and set the rank based on avg score
        for (int c = 0; c < rankList.size(); c++) {
            if (c > 0) {
                int prev_rank = getRobot(rankList.get(c-1).robotNumber).autoLowShots.rank;
                if (getRobot(rankList.get(c).robotNumber).autoLowShots.avg < getRobot(rankList.get(c-1).robotNumber).autoLowShots.avg) {
                    getRobot(rankList.get(c).robotNumber).autoLowShots.rank = prev_rank + 1;
                } else {
                    getRobot(rankList.get(c).robotNumber).autoLowShots.rank = prev_rank;
                }
            } else {
                getRobot(rankList.get(c).robotNumber).autoLowShots.rank = 1;
            }
        }

        for (int c = 0; c < rankList.size(); c++) {
            if (c > 0) {
                int prev_rank = getRobot(rankList.get(c-1).robotNumber).autoHighShots.rank;
                if (getRobot(rankList.get(c).robotNumber).autoHighShots.avg < getRobot(rankList.get(c-1).robotNumber).autoHighShots.avg) {
                    getRobot(rankList.get(c).robotNumber).autoHighShots.rank = prev_rank + 1;
                } else {
                    getRobot(rankList.get(c).robotNumber).autoHighShots.rank = prev_rank;
                }
            } else {
                getRobot(rankList.get(c).robotNumber).autoHighShots.rank = 1;
            }
        }

        for (int c = 0; c < rankList.size(); c++) {
            if (c > 0) {
                int prev_rank = getRobot(rankList.get(c-1).robotNumber).lowShots.rank;
                if (getRobot(rankList.get(c).robotNumber).lowShots.avg < getRobot(rankList.get(c-1).robotNumber).lowShots.avg) {
                    getRobot(rankList.get(c).robotNumber).lowShots.rank = prev_rank + 1;
                } else {
                    getRobot(rankList.get(c).robotNumber).lowShots.rank = prev_rank;
                }
            } else {
                getRobot(rankList.get(c).robotNumber).lowShots.rank = 1;
            }
        }

        Collections.sort(rankList, new Comparator<RobotData>() {
            public int compare(RobotData o1, RobotData o2) {
                if (o1.highAttempt.avg == o2.highAttempt.avg)
                    return 0;
                return o1.highAttempt.avg > o2.highAttempt.avg ? -1 : 1;
            }
        });
        // now loop through the lists and set the rank based on avg score
        for (int c = 0; c < rankList.size(); c++) {
            if (c > 0) {
                int prev_rank = getRobot(rankList.get(c-1).robotNumber).highAttempt.rank;
                if (getRobot(rankList.get(c).robotNumber).highAttempt.avg < getRobot(rankList.get(c-1).robotNumber).highAttempt.avg) {
                    getRobot(rankList.get(c).robotNumber).highAttempt.rank = prev_rank + 1;
                } else {
                    getRobot(rankList.get(c).robotNumber).highAttempt.rank = prev_rank;
                }
            } else {
                getRobot(rankList.get(c).robotNumber).highAttempt.rank = 1;
            }
        }
        Collections.sort(rankList, new Comparator<RobotData>() {
            public int compare(RobotData o1, RobotData o2) {
                if (o1.autoCross.avg == o2.autoCross.avg)
                    return 0;
                return o1.autoCross.avg > o2.autoCross.avg ? -1 : 1;
            }
        });
        // now loop through the lists and set the rank based on avg score
        for (int c = 0; c < rankList.size(); c++) {
            getRobot(rankList.get(c).robotNumber).autoCross.rank = c + 1;
        }
        for (int c = 0; c < rankList.size(); c++) {
            if (c > 0) {
                int prev_rank = getRobot(rankList.get(c-1).robotNumber).autoCross.rank;
                if (getRobot(rankList.get(c).robotNumber).autoCross.avg < getRobot(rankList.get(c-1).robotNumber).autoCross.avg) {
                    getRobot(rankList.get(c).robotNumber).autoCross.rank = prev_rank + 1;
                } else {
                    getRobot(rankList.get(c).robotNumber).autoCross.rank = prev_rank;
                }
            } else {
                getRobot(rankList.get(c).robotNumber).autoCross.rank = 1;
            }
        }

        Collections.sort(rankList, new Comparator<RobotData>() {
            public int compare(RobotData o1, RobotData o2) {
                if (o1.autoGears.avg == o2.autoGears.avg)
                    return 0;
                return o1.autoGears.avg > o2.autoGears.avg ? -1 : 1;
            }
        });
        // now loop through the lists and set the rank based on avg score
        for (int c = 0; c < rankList.size(); c++) {
            if (c > 0) {
                int prev_rank = getRobot(rankList.get(c-1).robotNumber).autoGears.rank;
                if (getRobot(rankList.get(c).robotNumber).autoGears.avg < getRobot(rankList.get(c-1).robotNumber).autoGears.avg) {
                    getRobot(rankList.get(c).robotNumber).autoGears.rank = prev_rank + 1;
                } else {
                    getRobot(rankList.get(c).robotNumber).autoGears.rank = prev_rank;
                }
            } else {
                getRobot(rankList.get(c).robotNumber).autoGears.rank = 1;
            }
        }
        Collections.sort(rankList, new Comparator<RobotData>() {
            public int compare(RobotData o1, RobotData o2) {
                if (o1.gears.avg == o2.gears.avg)
                    return 0;
                return o1.gears.avg > o2.gears.avg ? -1 : 1;
            }
        });
        // now loop through the lists and set the rank based on avg score
        for (int c = 0; c < rankList.size(); c++) {
            if (c > 0) {
                int prev_rank = getRobot(rankList.get(c-1).robotNumber).gears.rank;
                if (getRobot(rankList.get(c).robotNumber).gears.avg < getRobot(rankList.get(c-1).robotNumber).gears.avg) {
                    getRobot(rankList.get(c).robotNumber).gears.rank = prev_rank + 1;
                } else {
                    getRobot(rankList.get(c).robotNumber).gears.rank = prev_rank;
                }
            } else {
                getRobot(rankList.get(c).robotNumber).gears.rank = 1;
            }
        }

        Collections.sort(rankList, new Comparator<RobotData>() {
            public int compare(RobotData o1, RobotData o2) {
                if (o1.climb.avg == o2.climb.avg)
                    return 0;
                return o1.climb.avg > o2.climb.avg ? -1 : 1;
            }
        });
        // now loop through the lists and set the rank based on avg score
        for (int c = 0; c < rankList.size(); c++) {
            if (c > 0) {
                int prev_rank = getRobot(rankList.get(c-1).robotNumber).climb.rank;
                if (getRobot(rankList.get(c).robotNumber).climb.avg < getRobot(rankList.get(c-1).robotNumber).climb.avg) {
                    getRobot(rankList.get(c).robotNumber).climb.rank = prev_rank + 1;
                } else {
                    getRobot(rankList.get(c).robotNumber).climb.rank = prev_rank;
                }
            } else {
                getRobot(rankList.get(c).robotNumber).climb.rank = 1;
            }
        }
        Collections.sort(rankList, new Comparator<RobotData>() {
            public int compare(RobotData o1, RobotData o2) {
                if (o1.accuracy == o2.accuracy)
                    return 0;
                return o1.accuracy > o2.accuracy ? -1 : 1;
            }
        });
        // now loop through the lists and set the rank based on avg score
        for (int c = 0; c < rankList.size(); c++) {
            getRobot(rankList.get(c).robotNumber).accuracyRank = c + 1;
        }
    }
}