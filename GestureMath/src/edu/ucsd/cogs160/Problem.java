package edu.ucsd.cogs160;

/**
 * Problem
 * 
 * Data structure class to hold the specification for a single math problem.
 *  Problems are stored as XML in pretraining.xml and training.xml
 * 
 * @author mlah
 *
 */
public class Problem {
    
    //TODO: public member variables is not best practice, but it is faster just to treat this like a struct or something
    public int problem_id;
    public String type;
    public int left1;
    public int left2;
    public int left3;
    public int left_exclude;
    public int right;
    public String blank_location;
    public int solution;
    
    Problem (int problem_id, String type, int left1, int left2, int left3, int left_exclude, int right, String blank_location, int solution) {
        this.problem_id = problem_id;
        this.type = type;
        this.left1 = left1;
        this.left2 = left2;
        this.left3 = left3;
        this.left_exclude = left_exclude;
        this.right = right;
        this.blank_location = blank_location;
        this.solution = solution;
    }
}
