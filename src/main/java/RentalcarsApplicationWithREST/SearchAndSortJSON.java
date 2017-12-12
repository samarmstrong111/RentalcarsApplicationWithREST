package RentalcarsApplicationWithREST;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import javax.json.*;

public class SearchAndSortJSON {
    /*Before implementing the REST api, this was used to test that the correct output came out of each function.
      Spaces between each output for readability.
     */
    public static void printOutputs(String[] args) throws Exception {
        printNameAndPrice();
        System.out.println("");
        System.out.println("");
        printSIPPEvaluation();
        System.out.println("");
        System.out.println("");
        printCarTypeBySupplier();
        System.out.println("");
        System.out.println("");
        printCarScore();
    }

    /*
    Sums up a score for the car based on the supplier rating and the sipp evaluation for the given criteria, then sorts
    based on the highest scoring car before returning them all as a List<String> object.
     */
    public static List<String> printCarScore() throws Exception
    {
        JsonObject data = grab().getJsonObject("Search");
        JsonArray dataArray = data.getJsonArray("VehicleList");
        List<String[]> output = new ArrayList<>();
        List<String> formattedOutput = new ArrayList<>();

        for(int i = 0; i<dataArray.size();i++)
        {
            double score = getScore(dataArray.getJsonObject(i));
            double supplierScore = dataArray.getJsonObject(i).getJsonNumber("rating").doubleValue();
            String sumOfScores = Double.toString(score + supplierScore);

            String[] evaluation = new String[]{dataArray.getJsonObject(i).getString("name"),Double.toString(score),
                    Double.toString(supplierScore),sumOfScores};

            //System.out.println(i+1+".  " + format(evaluation));
            output.add(evaluation);
            //output.add(format(evaluation));
        }
        output = sortTotalScore(output);
        for(int i = 0; i<output.size();i++)
        {
            formattedOutput.add(format(output.get(i)));
        }
        return formattedOutput;
    }

    /*
    Probably the most awkward one just because it has to sort into subsections and then sort each of those.
    First sorting by alphabetical order in the sipp to get the car types grouped.
    numberOfCarTypes was used for the output earlier but now it's going through rest so the index isn't required as
    Json adds that in automatically when viewed.
    The carType is required here because of the discrepency with the "special" type. It's here so that the special ones
    can be considered separately, and needed as the JsonObject is immutable, so I can't just go in and edit it.
    The sorting is fine as CX will all be grouped when it's sorted by sipp.
    Also it checks to make sure it only gets one of each car type by checking the current car isn't the same as any of
    the previous ones. A bit overkill checking all, but oh well.
     */
    public static List<String> printCarTypeBySupplier() throws Exception
    {
        JsonObject data = grab().getJsonObject("Search");
        JsonArray dataArray = data.getJsonArray("VehicleList");
        List<String> output = new ArrayList<>();

        List<String> previousCarTypes = new ArrayList<String>();
        //int numberOfCarTypes = 0;
        String carType;

        dataArray = sortAlphabetical(dataArray, "sipp");
        dataArray = sortNumberPerCarType(dataArray, "rating");

        for (int i = 0; i < dataArray.size(); i++) {
            if(doorType(dataArray.getJsonObject(i).getString("sipp")).equals("Special"))
            {
                carType = carType(dataArray.getJsonObject(i).getString("sipp")) + " Special";
            }
            else
            {
                carType = carType(dataArray.getJsonObject(i).getString("sipp"));
            }
            if(i==0 || !previousCarTypes.contains(carType))
            {
                String[] evaluation = new String[]{dataArray.getJsonObject(i).getString("name"), carType, dataArray
                        .getJsonObject(i).getString("supplier"),dataArray.getJsonObject(i).get("rating").toString()};

                //System.out.println(numberOfCarTypes+1+ ".  " + format(evaluation));
                output.add(format(evaluation));
                //numberOfCarTypes++;
                previousCarTypes.add(carType);
            }
        }
        return output;
    }

    /*
    Returns the meaning of the sipp with the formatting specified in the test document as a List<String> so that I can
    push it through the REST api easily later.
     */
    public static List<String> printSIPPEvaluation() throws Exception
    {
        JsonObject data = grab().getJsonObject("Search");
        JsonArray dataArray = data.getJsonArray("VehicleList");
        List<String> output = new ArrayList<>();
        String[] evaluation;
        for (int i = 0; i < dataArray.size(); i++) {
            evaluation = SIPPEvaluation(dataArray.getJsonObject(i));
            //System.out.println(i + 1 + ".  " + format(evaluation));
            output.add(format(evaluation));
        }
        return output;
    }

    /*
    Calls the methods below and returns the meaning of the sipp for a given car as a String[] array. Great for easy
    formatting.
     */
    public static String[] SIPPEvaluation(JsonObject vehicle) {
        String vehicleName = vehicle.getString("name");
        String SIPP = vehicle.getString("sipp");
        String[] fuelAC = splitFuelAC(fuelAirCon(SIPP));
        String[] evaluation = new String[]{vehicleName, carType(SIPP), doorType(SIPP), transmissionType(SIPP),
                fuelAC[0], fuelAC[1]};
        return evaluation;
    }

    /*
    Checks the car type for the output. Special doesn't seem to show up here despite the fact it should. It shows up in
    the door type part of sipp instead. I'll leave X in though because it may be tested with other sets of data.
     */
    public static String carType(String SIPP)
    {
        switch (SIPP.charAt(0))
        {
            case 'M':
                return "Mini";
            case 'E':
                return "Economy";
            case 'C':
                return "Compact";
            case 'I':
                return "Intermediate";
            case 'S':
                return "Standard";
            case 'F':
                return "Full size";
            case 'P':
                return "Premium";
            case 'L':
                return "Luxury";
            case 'X':
                return "Special";
            default:
                return "Invalid tag";
        }
    }

    /*
    Checks the door type for the output. Special probably shouldn't be in here, I feel it should be under car type, as
    is given in the table, but it appears in the second slot instead. I'll deal with that in another method to just
    combine the two.
     */
    public static String doorType(String SIPP) {
        switch (SIPP.charAt(1)) {
            case 'B':
                return "2 doors";
            case 'C':
                return "4 doors";
            case 'D':
                return "5 doors";
            case 'W':
                return "Estate";
            case 'T':
                return "Convertable";
            case 'F':
                return "SUV";
            case 'P':
                return "Pickup";
            case 'V':
                return "Passenger Van";
            case 'X':
                return "Special";
            default:
                return "Invalid tag";
        }
    }

    /*
    Checks the transmission type for the output.
     */
    public static String transmissionType(String SIPP) {
        switch (SIPP.charAt(2)) {
            case 'M':
                return "Manual";
            case 'A':
                return "Automatic";
            default:
                return "Invalid tag";
        }
    }

    /*
    Checks the fuel type and if it has AC for the output.
     */
    public static String fuelAirCon(String SIPP) {
        switch (SIPP.charAt(3)) {
            case 'N':
                return "Petrol/no AC";
            case 'R':
                return "Petrol/AC";
            default:
                return "Invalid tag";
        }
    }

    /*
    The fuel type and existence of AC are combined into one letter of the sipp. This short method splits that up so it
    can be easily formatted later.
     */
    public static String[] splitFuelAC(String fuelAC)
    {
        String[] fuelACSplit = new String[]{fuelAC.substring(0, fuelAC.indexOf("/")), fuelAC.substring(fuelAC.indexOf
                ("/") + 1, fuelAC.length())};
        return fuelACSplit;
    }

    /*
    Gets the test data and converts it to a usable JsonArray.
    Sorts the array in terms of price
    Formats the newly sorted output before returning it as a List<String> for outputting through REST.
     */
    public static List<String> printNameAndPrice() throws Exception {
        JsonObject data = grab().getJsonObject("Search");
        JsonArray dataArray = data.getJsonArray("VehicleList");
        dataArray = sortNumber(dataArray, "price");
        List<String> output = new ArrayList<>();

        for (int i = 0; i < dataArray.size(); i++) {
            String[] values = new String[]{dataArray.getJsonObject(i).getString("name"), dataArray.getJsonObject(i)
                    .get("price").toString()};
            String formattedData = i + 1 + ".  " + format(values);
            output.add(format(values));
        }
        return output;
    }

    /*
    Creates a list of the JsonObjects, compares the given value, which is "price" at the moment further up in the
    program, and sorts in ascending numerical order.
    Could be generalised to specify ascending or descending.
     */
    public static JsonArray sortNumber(JsonArray data, String comparisonVariable) {
        List<JsonObject> toBeSorted = new ArrayList<JsonObject>();
        for (int i = 0; i < data.size(); i++) {
            toBeSorted.add(data.getJsonObject(i));
        }

        Collections.sort(toBeSorted, new Comparator<JsonObject>() {

            public int compare(JsonObject a, JsonObject b) {
                double valueA = 0;
                double valueB = 0;

                try {
                    valueA = a.getJsonNumber(comparisonVariable).doubleValue();
                    valueB = b.getJsonNumber(comparisonVariable).doubleValue();
                } catch (Exception e) {

                }

                return Double.compare(valueA, valueB);
            }
        });
        return buildJsonArray(toBeSorted);
    }

    /*
    Once the total score has been calculated and compiled into a String[] array this can go through and sort the new
    list of cars in order of their total score. This needed to be a separate method from just sortNumber as the input is
    of a different type and Java has strict typing.
     */
    public static List<String[]> sortTotalScore(List<String[]> data) {

        Collections.sort(data, new Comparator<String[]>() {

            public int compare(String[] a, String[] b) {
                double valueA = 0;
                double valueB = 0;

                try {
                    valueA = Double.parseDouble(a[3]);
                    valueB = Double.parseDouble(b[3]);
                } catch (Exception e) {

                }
                return Double.compare(valueB, valueA);
            }
        });
        return data;
    }

    /*
    Converts the JsonArray into a sortable list, then sorts everything in alphabetical order in terms of the comparison
    variable given. Further up in the program this has been called using "sipp".
     */
    public static JsonArray sortAlphabetical(JsonArray data, String comparisonVariable) {
        List<JsonObject> toBeSorted = new ArrayList<JsonObject>();
        for (int i = 0; i < data.size(); i++) {
            toBeSorted.add(data.getJsonObject(i));
        }

        Collections.sort(toBeSorted, new Comparator<JsonObject>() {

            public int compare(JsonObject a, JsonObject b) {
                String valueA = "";
                String valueB = "";

                try {
                    valueA = a.getString(comparisonVariable);
                    valueB = b.getString(comparisonVariable);
                } catch (Exception e) {

                }

                return valueA.compareTo(valueB);
            }
        });
        return buildJsonArray(toBeSorted);
    }

    /*
    A method for sorting the entries in terms of car type.
    Works the same as sortNumber but it also checks to see if the car types are identical or not first before sorting
    them if they are.
    I need to add in Compact Special detection.
     */
    public static JsonArray sortNumberPerCarType(JsonArray data, String comparisonVariable) {
        List<JsonObject> toBeSorted = new ArrayList<JsonObject>();
        for (int i = 0; i < data.size(); i++) {
            toBeSorted.add(data.getJsonObject(i));
        }

        Collections.sort(toBeSorted, new Comparator<JsonObject>() {

            public int compare(JsonObject a, JsonObject b) {
                double valueA = 0;
                double valueB = 0;

                if (a.getString("sipp").charAt(0) == b.getString("sipp").charAt(0)) {
                    try {
                        valueA = a.getJsonNumber(comparisonVariable).doubleValue();
                        valueB = b.getJsonNumber(comparisonVariable).doubleValue();
                    } catch (Exception e) {

                    }
                }
                return Double.compare(valueB, valueA);
            }
        });
        return buildJsonArray(toBeSorted);
    }

    /*
    Builds a JsonArray type object from a list of JsonObjects as JsonArrays are abstract and can't be just declared and
    added to.
     */
    public static JsonArray buildJsonArray(List<JsonObject> toBeSorted) {
        JsonArrayBuilder builder = Json.createArrayBuilder();
        for (int i = 0; i < toBeSorted.size(); i++) {
            builder.add(toBeSorted.get(i));
        }
        JsonArray sortedData = builder.build();
        return sortedData;
    }

    /*
    Formats a string based on what I was given in the test examples. I might have misinterpreted this horribly and
    was not supposed to take it as literal, but I don't think the hyphons are part of JSON notation
     */
    public static String format(String[] array) throws Exception {
        String formatedOutput = "";
        for (int i = 0; i < array.length; i++) {
            formatedOutput += "{" + array[i] + "}-";
        }
        return formatedOutput.substring(0, formatedOutput.length() - 1);
    }

    /*
    Creates a sipp score for the car based on the table provided in the test document.
     */
    public static double getScore(JsonObject vehicle) throws Exception
    {
        double score = 0;
        if (vehicle.getString("sipp").charAt(2) == 'M')
        {
            score+=1;
        }
        else if (vehicle.getString("sipp").charAt(2) == 'A')
        {
            score+=5;
        }
        if (vehicle.getString("sipp").charAt(3)=='R')
        {
            score+=2;
        }
        return score;
    }

    /*
    Pulls the JSON object linked from the email I got from Clayton and returns it as a usable JsonObject.
     */
    public static JsonObject grab() throws Exception {
        URL url = new URL("http://www.rentalcars.com/js/vehicles.json");
        try (InputStream is = url.openStream(); JsonReader rdr = Json.createReader(is))
        {
            JsonObject obj = rdr.readObject();
            return obj;
        }
    }
}
