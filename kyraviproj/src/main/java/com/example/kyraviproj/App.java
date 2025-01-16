package com.example.kyraviproj;

/**
 * Hello world!
 *
 */
import java.util.*;


public class App 
{
    public static void main( String[] args )
    {
        Scanner input = new Scanner(System.in);
        Auth.authorizationCodeUri_Sync(); //All authentication related things that absolutely need to be called from the main method

        String username = Auth.getUser();
        System.out.println("\nWelcome to the Spotify Recap Application " + username + "!");

        char hi = 'y';
        int choice = 0;
        char choice2 = ' ';
        int choice3 = 0;
        // System.out.println("This is the menu, please type in an option.");

        while (hi == 'y') {
            System.out.println("1 - Top Artists, 2 - Top Tracks, 3 - Top Albums, 4 - Playlists");
            choice = input.nextInt();
            input.nextLine();
            if (choice == 1) {
                System.out.println("What time period? a - 4 weeks/1 month, b - 6 months/ 1/2 year, c - 1 year");
                choice2 = input.nextLine().charAt(0);
                System.out.println("How many artists to display?");
                choice3 = input.nextInt();
                input.nextLine();
                if (choice2 == 'a') {
                    Auth.getUsersTopArtists_Sync("short_term", choice3);
                }
                if (choice2 == 'b') {
                    Auth.getUsersTopArtists_Sync("medium_term" , choice3);
                }
                if (choice2 == 'c') {
                    Auth.getUsersTopArtists_Sync("long_term", choice3);
                }
            }
            else if (choice == 2) {
                System.out.println("What time period? a - 4 weeks/1 month, b - 6 months/ 1/2 year, c - 1 year");
                choice2 = input.nextLine().charAt(0);
                System.out.println("How many tracks to display?");
                choice3 = input.nextInt();
                input.nextLine();
                if (choice2 == 'a') {
                    Auth.getUsersTopTracks_Sync("short_term", choice3);
                }
                if (choice2 == 'b') {
                    Auth.getUsersTopTracks_Sync("medium_term", choice3);
                }
                if (choice2 == 'c') {
                    Auth.getUsersTopTracks_Sync("long_term", choice3);
                }
            }
            else if (choice == 3) {
                System.out.println("What time period? a - 4 weeks/1 month, b - 6 months/ 1/2 year, c - 1 year");
                choice2 = input.nextLine().charAt(0);
                System.out.println("How many albums to display?");
                choice3 = input.nextInt();
                input.nextLine();
                if (choice2 == 'a') {
                    Auth.getUsersTopAlbums_Sync("short_term", choice3);
                }
                if (choice2 == 'b') {
                    Auth.getUsersTopAlbums_Sync("medium_term", choice3);
                }
                if (choice2 == 'c') {
                    Auth.getUsersTopAlbums_Sync("long_term", choice3);
                }
            }
            else if (choice == 4) {
                System.out.println("How many recent playlists to display?");
                choice3 = input.nextInt();
                input.nextLine();
                Auth.getListofCurrentUsersPlaylists_Sync(3);
            }
            System.out.println("Enter y if you want to continue, any other character to end.");
            hi = input.nextLine().charAt(0);
        }
        input.close();
        System.out.println("Thank you for using this program! by Kireeti Ravi.");
    }
}
