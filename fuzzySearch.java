package DSproject;

import java.util.ArrayList;

public class fuzzySearch {
    //Method to obtain fuzzy search results
    public void results (String search, ArrayList issueID) {
        int space = 0;
        //To calculate how many words
        for (int i = 0; i < search.length(); i++) {
            if (search.charAt(i) == 32) {
                space++;
            }
        }

        int[][] difference = new int[issueID.size()][2];
        for (int j = 0; j < issueID.size(); j++) {
            //Obtain each issue
            String[] data = DBConnect.getComment((Integer) issueID.get(j)).split(" ");
            //Split issue according to the number of words of String search
            String[] group = new String[data.length - space];
            for (int k = 0; k < group.length; k++) {
                for (int l = k; l <= k + space; l++) {
                    group[k] += data[l];
                }
            }

            //Obtain levenshtein distance String search and part of issue text
            int[] compare = new int[group.length];
            compare[0] = distance(search, group[0]);
            int smallest = compare[0];
            for (int k = 1; k < group.length; k++) {
                compare[k] = distance(search, group[k]);
                //Obtain the smallest levenshtein distance in issue
                if (compare[k] < smallest) {
                    smallest = compare[k];
                }
            }

            //Store issueID and smallest levenshtein distance of issue
            difference[j][1] = issueID.indexOf(j);
            difference[j][2] = smallest;
        }

        //Use insert sort to sort levenshtein distance of all issues
        for (int n = 1; n < difference.length; n++) {
            int key = difference[n][2];
            int keyIndex = difference[n][1];
            int m = n-1;
            while ( (m > -1) && ( difference [m][2] > key ) ) {
                difference [m+1][2] = difference [m][2];
                difference [m+1][1] = difference [m][1];
                m--;
            }
            difference[m+1][2] = key;
            difference[m+1][1] = keyIndex;
        }

        //Displaying issue with less than 3*(space + 1) levenshtein distance
        for (int p = 0; p < difference.length; p++) {
            if (difference[p][2] < 3*(space + 1)) {
                DBConnect.getComment((Integer) issueID.get(p));
            }
        }

    }

    //Using levenshtein distance to calculate difference
    public int distance(String search, String database) {
        search = search.toLowerCase();
        database = database.toLowerCase();
        // i == 0
        int [] costs = new int [database.length() + 1];
        for (int j = 0; j < costs.length; j++)
            costs[j] = j;
        for (int i = 1; i <= search.length(); i++) {
            // j == 0; nw = lev(i - 1, j)
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= database.length(); j++) {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]), search.charAt(i - 1) == database.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[database.length()];
    }

}
