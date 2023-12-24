package com.vocabularyflashcardapp;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class VocabularyFlashcardApp extends Application {

    private List<String> words = new ArrayList<>();
    private List<String> translations = new ArrayList<>();
    private Label flashcardLabel;
    private TextArea flashcardsTextArea;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Vocabulary Flashcard App");

        flashcardLabel = new Label();
        flashcardLabel.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");

        flashcardsTextArea = new TextArea();
        flashcardsTextArea.setEditable(false);
        flashcardsTextArea.setStyle("-fx-font-size: 24;");
        flashcardsTextArea.setWrapText(true);

        Button addCardButton = new Button("Add Flashcard");
        addCardButton.setStyle("-fx-font-size: 18;");
        addCardButton.setOnAction(e -> addFlashcard());

        Button viewAllButton = new Button("View All Flashcards");
        viewAllButton.setOnAction(e -> viewAllFlashcards());
        viewAllButton.setStyle("-fx-font-size: 18;");

        Button searchButton = new Button("Search Word");
        searchButton.setOnAction(e -> searchWord());
        searchButton.setStyle("-fx-font-size: 18;");

        Button quizButton = new Button("Take Quiz");
        quizButton.setOnAction(e -> takeQuiz());
        quizButton.setStyle("-fx-font-size: 18;");

        BorderPane layout = new BorderPane();
        layout.setTop(flashcardLabel);
        layout.setCenter(flashcardsTextArea);

        BorderPane buttonPanel = new BorderPane();
        buttonPanel.setLeft(addCardButton);
        buttonPanel.setCenter(viewAllButton);
        buttonPanel.setRight(searchButton);
        buttonPanel.setBottom(quizButton);
        BorderPane.setMargin(addCardButton, new Insets(10));
        BorderPane.setMargin(viewAllButton, new Insets(10));
        BorderPane.setMargin(searchButton, new Insets(10));
        BorderPane.setMargin(quizButton, new Insets(10));

        layout.setBottom(buttonPanel);

        loadFlashcards(); // Load flashcards from JSON file on program start

        Scene scene = new Scene(layout, 600, 400);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void addFlashcard() {
        String newWord = TextInputDialog("Enter the new word:");
        String newTranslation = TextInputDialog("Enter the translation:");

        if (newWord != null && newTranslation != null && !newWord.trim().isEmpty() && !newTranslation.trim().isEmpty()) {
            // Check if the word already exists
            if (!words.contains(newWord.trim())) {
                words.add(newWord.trim());
                translations.add(newTranslation.trim());
                flashcardLabel.setText(String.format("\t\t\t\t\t\t%s\t%s", newWord.trim(), newTranslation.trim()));
                saveFlashcards(); // Save flashcards to JSON file after adding
                showMessage("Flashcard added successfully!", "Add");
            } else {
                showError("The word already exists. Please enter a new word.", "Duplicate Word");
            }
        } else {
            showError("Invalid input. Please enter both word and translation.", "Invalid Input");
        }
    }

    private void viewAllFlashcards() {
        flashcardsTextArea.setEditable(false);
        String flashcardsText = "";

        flashcardsTextArea.setStyle("-fx-font-size: 16;");
        if (!words.isEmpty()) {
            for (int i = 0; i < words.size(); i++) {
                flashcardsText += "Word: " + words.get(i) + "\n";
                flashcardsText += "Translation: " + translations.get(i) + "\n\n";
            }
        } else {
            flashcardsText += "No flashcards available. Add flashcards first.";
        }
        flashcardsTextArea.setText(flashcardsText);
    }

    private void searchWord() {
        String searchInput = TextInputDialog("Enter the word to search:");

        if (searchInput != null && !searchInput.trim().isEmpty()) {
            searchInput = searchInput.trim().toLowerCase();

            int index = -1;
            for (int i = 0; i < words.size(); i++) {
                if (words.get(i).toLowerCase().equals(searchInput)) {
                    index = i;
                    break;
                }
            }

            if (index != -1) {
                showMessage("Translation: " + translations.get(index), "Word Found");
            } else {
                showError("Word not found.", "Word Not Found");
            }
        } else {
            showError("Invalid input. Please enter a word to search.", "Invalid Input");
        }
    }

    private void takeQuiz() {
        String input = TextInputDialog("Enter the number of words for the quiz:");

        if (input != null && !input.trim().isEmpty()) {
            try {
                int numberOfWords = Integer.parseInt(input.trim());

                if (numberOfWords > 0 && numberOfWords <= words.size()) {
                    // Shuffle the words list
                    List<String> shuffledWords = getShuffledWords();

                    // Select the first 'numberOfWords' words for the quiz
                    List<String> quizWords = shuffledWords.subList(0, numberOfWords);

                    // Display each word and ask for the translation
                    int correctAnswers = 0;

                    for (String quizWord : quizWords) {
                        String userAnswer = TextInputDialog("Translate the word: " + quizWord);
                        int index = words.indexOf(quizWord);

                        if (index != -1 && userAnswer != null) {
                            String correctTranslation = translations.get(index);

                            if (userAnswer.equalsIgnoreCase(correctTranslation)) {
                                correctAnswers++;
                            } else {
                                // Display correct translation immediately when the user answers incorrectly
                                showError("Incorrect!\nCorrect Translation: " + correctTranslation, "Incorrect Answer");
                            }
                        }
                    }

                    // Display quiz result
                    showMessage("Quiz completed!\nCorrect Answers: " + correctAnswers + "/" + numberOfWords, "Quiz Result");
                } else {
                    showError("Invalid number of words. Please enter a number between 1 and " + words.size(), "Invalid Input");
                }
            } catch (NumberFormatException e) {
                showError("Invalid input. Please enter a valid number.", "Invalid Input");
            }
        }
    }

    private List<String> getShuffledWords() {
        List<String> shuffledWords = new ArrayList<>(words);
        Collections.shuffle(shuffledWords);
        return shuffledWords;
    }

    private String TextInputDialog(String prompt) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText(null);
        dialog.setContentText(prompt);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("my-dialog");

        return dialog.showAndWait().orElse(null);
    }

    private void showMessage(String message, String title) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("my-dialog");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String errorMessage, String title) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("my-dialog");
        alert.setHeaderText(null);
        alert.setContentText(errorMessage);
        alert.showAndWait();
    }

    private void saveFlashcards() {
        JSONObject jsonFlashcards = new JSONObject();
        JSONArray jsonWords = new JSONArray(words);
        JSONArray jsonTranslations = new JSONArray(translations);

        jsonFlashcards.put("words", jsonWords);
        jsonFlashcards.put("translations", jsonTranslations);

        try {
            Files.write(Paths.get("flashcards.json"), jsonFlashcards.toString(2).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadFlashcards() {
        try {
            String jsonString = new String(Files.readAllBytes(Paths.get("flashcards.json")), StandardCharsets.UTF_8);
            JSONObject jsonFlashcards = new JSONObject(jsonString);
    
            JSONArray jsonWords = jsonFlashcards.getJSONArray("words");
            JSONArray jsonTranslations = jsonFlashcards.getJSONArray("translations");
    
            words.clear();
            translations.clear();
    
            for (int i = 0; i < jsonWords.length(); i++) {
                words.add(jsonWords.getString(i));
            }
    
            for (int i = 0; i < jsonTranslations.length(); i++) {
                translations.add(jsonTranslations.getString(i));
            }
    
        } catch (IOException e) {
            // File not found or other IO error. Ignore if it's the first run.
        }
    }
    public static void main(String[] args) {
        launch(args);
    }
}

