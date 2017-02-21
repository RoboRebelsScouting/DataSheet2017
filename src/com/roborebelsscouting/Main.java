package com.roborebelsscouting;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Main {
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
    public void createTableFloatRow(String tableText, float tableData) {
        String outString = "<tr>" + "\n";
        outString += "<td>" + tableText + "</td>\n";
        outString += "<td>" + String.format("%.2f", tableData) + "</td>\n";
        outString += "</tr>\n";

        try {
            writer.write(outString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createTableIntRow(String tableText, int tableData) {
        String outString = "<tr>" + "\n";
        outString += "<td>" + tableText + "</td>\n";
        outString += "<td>" + tableData + "</td>\n";
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
        try {
            Class.forName("com.mysql.jdbc");
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
            rs = stmt.executeQuery("SELECT * from matchtable");

            //process Data
            RobotData rd;
            while (rs.next()) {
                int rn = rs.getInt("RobotNumber");
                String matchName = rs.getString("matchNumber");

                if (haveRobot(rn)) {
                    getRobot(rn).matches++;
                } else {
                    rd = new RobotData();
                    rd.robotNumber = rn;
                    rd.matches = 1;
                }
            }
            rs.close();
            rs = stmt.executeQuery("SELECT * from matchtable");
            while (rs.next()) {
                int rn = rs.getInt("RobotNumber");
                if (haveRobot(rn)) {
                    String gameEvent = rs.getString("gameEvent");
                    String phase = rs.getString("phaseOfMatch");
                    if (phase.equals("crossBaselineAuto")){getRobot(rn).autoCross.total++;}
                    if (gameEvent.equals("climbed")){getRobot(rn).climb.total++;}
                    if (phase.equals("gearPlacedAuto")){getRobot(rn).autoGears.total++;}
                    if (phase.equals("lowGoal")){getRobot(rn).lowShots.total++;}
                    if (phase.equals("highGoal")){getRobot(rn).highAttempt.total++;}
                    if (phase.equals("gearPlacedTeleop") || phase.equals("gearPlacedAuto")){getRobot(rn).gears.total++;}

                }
            }
            rs.close();
            stmt.close();

            //averages
            for (RobotData r : robotList) {
                r.autoCross.avg = (double) r.autoCross.total / r.matches;
                r.autoGears.avg = (double) r.autoGears.total / r.matches;
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
                String fileName = File.separator + r.robotNumber + ".html";
                File oldFile = new File(fileName);

                if (oldFile.exists()) {
                    oldFile.delete();
                }
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName),"utf-8"));
                writer.write("<!doctype html>\n");
                createHeader(r.robotNumber);
                createStyle();

                writer.write("<body>\n");

                writer.write("<table>\n");
                createTableFloatRow("Avg Auto Crossing", (float) r.autoCross.avg);
                createTableFloatRow("Avg Auto Gear Scoring", (float) r.autoGears.avg);
                createTableFloatRow("Avg Climbing", (float) r.climb.avg);
                createTableFloatRow("Avg Gear Scoring", (float) r.gears.avg);
                createTableFloatRow("Avg High Shooting", (float) r.highAttempt.avg);
                createTableFloatRow("Avg Low Shooting", (float) r.lowShots.avg);
                writer.write("</table>\n");

                writer.write("<table>\n");
                createTableIntRow("Auto Crossing Rank", r.autoCross.rank);
                createTableIntRow("Auto Gear Scoring Rank", r.autoGears.rank);
                createTableIntRow("Climbing Rank", r.climb.rank);
                createTableIntRow("Gear Scoring Rank", r.gears.rank);
                createTableIntRow("High Shooting Rank", r.highAttempt.rank);
                createTableIntRow("Low Shooting Rank", r.lowShots.rank);
                writer.write("</table>\n");

                writer.write("</body>\n");
                writer.write("</html>\n");

                writer.close();

            } catch (IOException ex) {
                ex.printStackTrace();
            }
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
            getRobot(rankList.get(c).robotNumber).lowShots.rank = c + 1;
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
            getRobot(rankList.get(c).robotNumber).highAttempt.rank = c + 1;
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

        Collections.sort(rankList, new Comparator<RobotData>() {
            public int compare(RobotData o1, RobotData o2) {
                if (o1.autoGears.avg == o2.autoGears.avg)
                    return 0;
                return o1.autoGears.avg > o2.autoGears.avg ? -1 : 1;
            }
        });
        // now loop through the lists and set the rank based on avg score
        for (int c = 0; c < rankList.size(); c++) {
            getRobot(rankList.get(c).robotNumber).autoGears.rank = c + 1;
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
            getRobot(rankList.get(c).robotNumber).gears.rank = c + 1;
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
            getRobot(rankList.get(c).robotNumber).climb.rank = c + 1;
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