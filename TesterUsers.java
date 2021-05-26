package AssignDS;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class TesterUsers {
    public static void main(String[] args) throws IOException, ParseException, FileNotFoundException {
        boolean login = false;
        do {
            Scanner sc = new Scanner(System.in);
            System.out.print("Username: ");
            String userInput = sc.nextLine();
            System.out.print("Password: ");
            String passInput = sc.nextLine();

            stop:
            {
                JSONParser jp = new JSONParser();
                JSONObject jo = (JSONObject) jp.parse(new FileReader("D:\\IdeaProjects\\Assignment\\src\\AssignDS\\data.json"));
                JSONArray ja = (JSONArray) jo.get("users");
                for (int i = 0; i < ja.size(); i++) {
                    JSONObject jo_in = (JSONObject) ja.get(i);
                    long id = (long) jo_in.get("userid");
                    String username = (String) jo_in.get("username");
                    String password = (String) jo_in.get("password");

                    if (username.equals(userInput)) {
                        if (password.equals(passInput)) {
                            System.out.println("Login successful !");
                            login = true;
                            break stop;
                        }
                    } else {
                        continue;
                    }
                }
                System.out.println("Failed to login ! Please try again.");
                System.out.println();
            }
        } while (login != true);

        /*JSONParser jp = new JSONParser();
        JSONObject jo = (JSONObject) jp.parse(new FileReader("D:\\IdeaProjects\\Assignment\\src\\AssignDS\\data.json"));
        JSONArray ja = (JSONArray) jo.get("users");
        ArrayList<Users> user = new ArrayList<>();
        for (int i = 0; i < ja.size(); i++) {
            JSONObject jo_in = (JSONObject) ja.get(i);
            long id = (long) jo_in.get("userid");
            String username = (String) jo_in.get("username");
            String password = (String) jo_in.get("password");
            user.add(new Users((int) id, username, password));
        }
        System.out.println(user.toString());
        System.out.println(user.size());*/
    }
}





