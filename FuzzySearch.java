package DSproject;

import java.util.ArrayList;

public class FuzzySearch {
    
    private double SimScore;
    private int IssueIndex;
    
    public FuzzySearch() {
    }
    
    public FuzzySearch(double SimScore, int IssueIndex) {
        this.SimScore = SimScore;
        this.IssueIndex = IssueIndex;
    }
    
    //Levenshtein Distance (searching algo)
    public static int editDistance(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    costs[j] = j;
                } else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1)) {
                            newValue = Math.min(Math.min(newValue, lastValue),
                                    costs[j]) + 1;
                        }
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0) {
                costs[s2.length()] = lastValue;
            }
        }
        return costs[s2.length()];
    }

    //method to calc similarity score
    public static double matchScore(String s1, String s2) {
        String longer = s1, shorter = s2;
        if (s1.length() < s2.length()) { // longer should always have greater length
            longer = s2;
            shorter = s1;
        }
        int longerLength = longer.length();
        if (longerLength == 0) {
            return 1.0;
            /* both strings are zero length */ }

        return (longerLength - editDistance(longer, shorter)) / (double) longerLength;
    }

    public double getSimScore() {
        return SimScore;
    }

    public void setSimScore(double SimScore) {
        this.SimScore = SimScore;
    }

    public int getIssueIndex() {
        return IssueIndex;
    }

    public void setIssueIndex(int IssueIndex) {
        this.IssueIndex = IssueIndex;
    }
}
