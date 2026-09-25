import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.Random;
import java.util.ArrayList;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

/*
 * input: command line arguments
 * output: starts the deck program
 * creates a deck id and asks the user for a card file!
 */

public class Main {

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        Random random = new Random();
        int deckID = 100000000 + random.nextInt(900000000);

        System.out.println("deck id: " + deckID);

        System.out.print("enter the card file name: ");
        String fileName = input.nextLine();

        readCards(fileName, deckID);

        input.close();
    }

    /*
     * input: file name and deck id
     * output: creates the deck report
     * reads each card and checks if it is validd
     */
    public static void readCards(String fileName, int deckID) {
        double totalCost = 0;
        int cardCount = 0;

        ArrayList<Double> costs = new ArrayList<>();
        ArrayList<String> invalidCards = new ArrayList<>();

        try {
            File cardFile = new File(fileName);
            Scanner fileReader = new Scanner(cardFile);

            while (fileReader.hasNextLine()) {
                String card = fileReader.nextLine();
                cardCount++;

                try {
                    String[] parts = card.split(":", -1);

                    if (parts.length != 2 || parts[0].trim().isEmpty()) {
                        invalidCards.add(card);
                        continue;
                    }

                    double cost = Double.parseDouble(parts[1].trim());

                    totalCost = totalCost + cost;
                    costs.add(cost);

                } catch (NumberFormatException e) {
                    invalidCards.add(card);
                }
            }

            fileReader.close();

            boolean isVoid = invalidCards.size() > 10 || cardCount > 1000;

            if (isVoid) {
                System.out.println("VOID");
            } else {
                System.out.println("total energy: " + totalCost);
                printHistogram(costs);

                System.out.println("invalid cards:");

                for (String card : invalidCards) {
                    System.out.println(card);
                }
            }

            createPDF(deckID, totalCost, costs, invalidCards, isVoid);

        } catch (FileNotFoundException e) {
            System.out.println("file could not be found :(((((");
        }
    }

    /*
     * input: list of card costs
     * output: prints a histogram of the costs
     * counts how many cards have each cost
     */
    public static void printHistogram(ArrayList<Double> costs) {
        System.out.println("cost histogram:");

        for (double cost = 0; cost <= 6; cost += 0.5) {
            int count = 0;

            for (double cardCost : costs) {
                if (cardCost == cost) {
                    count++;
                }
            }

            System.out.print(cost + " energy: ");

            for (int i = 0; i < count; i++) {
                System.out.print("*");
            }

            System.out.println();
        }
    }

    /*
     * input: deck information
     * output: makes a pdf report
     * writes the deck information to the pdf
     */
    public static void createPDF(int deckID, double totalCost,
                                 ArrayList<Double> costs,
                                 ArrayList<String> invalidCards,
                                 boolean isVoid) {

        String fileName = "SpireDeck " + deckID + ".pdf";

        if (isVoid) {
            fileName = "SpireDeck " + deckID + "(VOID).pdf";
        }

        try {
            PDDocument document = new PDDocument();
            PDPage page = new PDPage();
            document.addPage(page);

            PDPageContentStream content = new PDPageContentStream(document, page);

            content.beginText();
            content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
            content.setLeading(18);
            content.newLineAtOffset(50, 750);

            if (isVoid) {
                content.showText("VOID");
            } else {
                content.showText("Deck ID: " + deckID);
                content.newLine();

                content.showText("Total Cost: " + totalCost + " energy");
                content.newLine();

                content.showText("Cost Histogram:");
                content.newLine();

                for (double cost = 0; cost <= 6; cost += 0.5) {
                    int count = 0;

                    for (double cardCost : costs) {
                        if (cardCost == cost) {
                            count++;
                        }
                    }

                    String stars = "";

                    for (int i = 0; i < count; i++) {
                        stars = stars + "*";
                    }

                    content.showText(cost + " energy: " + stars);
                    content.newLine();
                }

                content.showText("Invalid Cards:");
                content.newLine();

                for (String card : invalidCards) {
                    content.showText(card);
                    content.newLine();
                }
            }

            content.endText();
            content.close();

            document.save(fileName);
            document.close();

            System.out.println("report created: " + fileName);

        } catch (IOException e) {
            System.out.println("could not create report");
        }
    }
}