package org.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.*;

public class PERG extends Application {

    private final TextField patternField = new TextField();
    private final TextArea resultArea = new TextArea();
    private final List<File> selectedFiles = new ArrayList<>();
    private File selectedDirectory = null;

    private final VBox patternPane = new VBox(15);
    private final VBox filePane = new VBox(15);
    private final VBox optionPane = new VBox(15);
    private final VBox resultPane = new VBox(15);
    private final StackPane mainPane = new StackPane();

    // Options checkboxes
    private final CheckBox recursiveBox = new CheckBox("Recursive (-r)");
    private final CheckBox verboseBox = new CheckBox("Verbose (-V)");
    private final CheckBox invertBox = new CheckBox("Invert Match (-v)");
    private final CheckBox parallelBox = new CheckBox("File Parallelism (-w)");
    private final CheckBox hiddenBox = new CheckBox("Include Hidden Files (-i)");
    private final CheckBox afterContextBox = new CheckBox("After Context (-A)");
    private final CheckBox caseSensitiveBox = new CheckBox("Case Sensitive (-c)");
    private final TextField afterContextField = new TextField();
    
    // Radio buttons for search mode
    private final ToggleGroup searchModeGroup = new ToggleGroup();
    private final RadioButton noneRadio = new RadioButton("Search All Files in Folder");
    private final RadioButton fileSearchRadio = new RadioButton("Multiple File Search (-f)");
    
    private final Button helpButton = new Button("Help (-h)");
    
    // UI elements
    private Label selectedFolderLabel;
    private VBox selectedFilesDisplay;
    private VBox fileUploadSection;

    @Override
    public void start(Stage stage) {
        stage.setTitle("PERG - Parallel File Search 🔍");
        stage.setWidth(700);
        stage.setHeight(800);

        setupPatternPane();
        setupFilePane();
        setupOptionPane();
        setupResultPane();

        mainPane.getChildren().addAll(patternPane, filePane, optionPane, resultPane);
        patternPane.setVisible(true);
        filePane.setVisible(false);
        optionPane.setVisible(false);
        resultPane.setVisible(false);

        stage.setScene(new Scene(mainPane));
        stage.show();
    }

    private void setupPatternPane() {
        patternPane.setAlignment(Pos.CENTER);
        patternPane.setPadding(new Insets(30));
        patternPane.setBackground(new Background(new BackgroundFill(Color.web("#7d3c98"), CornerRadii.EMPTY, Insets.EMPTY)));

        // Main heading - PERG
        Label mainHeading = new Label("PERG");
        mainHeading.setStyle("-fx-font-size: 48px; -fx-text-fill: #FDFEFE; -fx-font-weight: bold; -fx-font-family: 'Arial Black', Arial, sans-serif;");

        // Subtitle - Full description
        Label subtitle = new Label("\"A parallelized version of the grep tool that searches for a string pattern across multiple files or within a large file\"");
        subtitle.setStyle("-fx-font-size: 16px; -fx-text-fill: #F8C471; -fx-font-weight: bold; -fx-font-style: italic; -fx-text-alignment: center; -fx-wrap-text: true;");
        subtitle.setMaxWidth(550);
        subtitle.setWrapText(true);

        // Search pattern input
        Label patternLabel = new Label("Enter Search Pattern:");
        patternLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #F5CBA7; -fx-font-weight: bold;");

        patternField.setPromptText("e.g. error, TODO, function, etc.");
        patternField.setMaxWidth(350);
        patternField.setStyle("-fx-font-size: 14px; -fx-padding: 8px;");

        Button nextBtn = new Button("Next ➡ Select Folder");
        nextBtn.setStyle("-fx-background-color: #E67E22; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10px 20px;");
        nextBtn.setOnAction(e -> {
            if (patternField.getText().isEmpty()) {
                showAlert("Please enter a search pattern.");
                return;
            }
            patternPane.setVisible(false);
            filePane.setVisible(true);
        });

        // Add some spacing between elements
        VBox contentBox = new VBox(20);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.getChildren().addAll(
            mainHeading,
            new Region() {{ setPrefHeight(10); }}, // Spacer
            subtitle,
            new Region() {{ setPrefHeight(30); }}, // Spacer
            patternLabel,
            patternField,
            new Region() {{ setPrefHeight(10); }}, // Spacer
            nextBtn
        );

        patternPane.getChildren().add(contentBox);
    }

    private void setupFilePane() {
        filePane.setAlignment(Pos.CENTER);
        filePane.setPadding(new Insets(30));
        filePane.setBackground(new Background(new BackgroundFill(Color.web("#7d3c98"), CornerRadii.EMPTY, Insets.EMPTY)));

        Label label = new Label("Select Folder to Search:");
        label.setStyle("-fx-font-size: 20px; -fx-text-fill: #F5CBA7; -fx-font-weight: bold;");

        Button uploadBtn = new Button("📁 Select Folder");
        uploadBtn.setStyle("-fx-background-color: #E67E22; -fx-font-weight: bold;");

        selectedFolderLabel = new Label("No folder selected");
        selectedFolderLabel.setStyle("-fx-text-fill: #F5CBA7;");

        uploadBtn.setOnAction(e -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Select Folder to Search");
            File directory = directoryChooser.showDialog(null);
            if (directory != null) {
                selectedDirectory = directory;
                selectedFolderLabel.setText("Selected: " + directory.getAbsolutePath());
                showAlert("Folder selected: " + directory.getName());
            }
        });

        Button backBtn = new Button("🔙 Back");
        backBtn.setStyle("-fx-background-color: #E67E22; -fx-font-weight: bold;");
        backBtn.setOnAction(e -> {
            filePane.setVisible(false);
            patternPane.setVisible(true);
        });

        Button nextBtn = new Button("➡ Options");
        nextBtn.setStyle("-fx-background-color: #E67E22; -fx-font-weight: bold;");
        nextBtn.setOnAction(e -> {
            if (selectedDirectory == null) {
                showAlert("Please select a folder.");
                return;
            }
            filePane.setVisible(false);
            optionPane.setVisible(true);
        });

        filePane.getChildren().addAll(label, uploadBtn, selectedFolderLabel, new HBox(20, backBtn, nextBtn));
    }

    private void setupOptionPane() {
        optionPane.setAlignment(Pos.CENTER);
        optionPane.setPadding(new Insets(30));
        optionPane.setBackground(new Background(new BackgroundFill(Color.web("#7d3c98"), CornerRadii.EMPTY, Insets.EMPTY)));

        afterContextField.setPromptText("Lines for -A");
        afterContextField.setMaxWidth(50);

        // Setup radio buttons for search mode
        noneRadio.setToggleGroup(searchModeGroup);
        fileSearchRadio.setToggleGroup(searchModeGroup);
        noneRadio.setSelected(true);

        // File upload section for -f option
        fileUploadSection = new VBox(10);
        fileUploadSection.setVisible(false);
        
        Button selectFilesBtn = new Button("Select File(s)");
        selectFilesBtn.setStyle("-fx-background-color: #D35400; -fx-text-fill: white;");
        
        selectedFilesDisplay = new VBox(5);
        selectedFilesDisplay.setStyle("-fx-background-color: #F8F9FA; -fx-padding: 10; -fx-border-color: #DEE2E6; -fx-border-radius: 5;");
        
        ScrollPane filesScrollPane = new ScrollPane(selectedFilesDisplay);
        filesScrollPane.setMaxHeight(150);
        filesScrollPane.setFitToWidth(true);
        filesScrollPane.setStyle("-fx-background-color: transparent;");
        
        Label filesCountLabel = new Label("No files selected");
        filesCountLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #495057;");
        
        selectFilesBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select File(s) for Search");
            List<File> files = fileChooser.showOpenMultipleDialog(null);
            if (files != null && !files.isEmpty()) {
                selectedFiles.clear();
                selectedFiles.addAll(files);
                updateSelectedFilesDisplay();
                filesCountLabel.setText(files.size() + " file(s) selected");
                showAlert(files.size() + " files selected for search.");
            }
        });
        
        Button clearFilesBtn = new Button("Clear Selection");
        clearFilesBtn.setStyle("-fx-background-color: #6C757D; -fx-text-fill: white;");
        clearFilesBtn.setOnAction(e -> {
            selectedFiles.clear();
            updateSelectedFilesDisplay();
            filesCountLabel.setText("No files selected");
        });
        
        HBox fileButtonsBox = new HBox(10, selectFilesBtn, clearFilesBtn);
        fileUploadSection.getChildren().addAll(fileButtonsBox, filesCountLabel, filesScrollPane);

        // Toggle file upload section based on radio button selection
        searchModeGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == fileSearchRadio) {
                fileUploadSection.setVisible(true);
            } else {
                fileUploadSection.setVisible(false);
                selectedFiles.clear();
                updateSelectedFilesDisplay();
                filesCountLabel.setText("No files selected");
            }
        });

        // Help button
        helpButton.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-weight: bold;");
        helpButton.setOnAction(e -> showHelpFromExecutable());

        // Search mode section
        VBox searchModeBox = new VBox(5);
        searchModeBox.getChildren().addAll(
            new Label("Search Mode:"),
            noneRadio,
            fileSearchRadio,
            fileUploadSection
        );
        searchModeBox.setStyle("-fx-background-color: #ECF0F1; -fx-padding: 10; -fx-border-color: #BDC3C7; -fx-border-radius: 5;");

        VBox optionsBox = new VBox(10, 
            searchModeBox,
            new Separator(),
            recursiveBox, 
            verboseBox, 
            invertBox, 
            parallelBox, 
            hiddenBox,
            caseSensitiveBox,  // Added case sensitive option
            new HBox(10, afterContextBox, afterContextField),
            new Separator(),
            helpButton
        );
        optionsBox.setPadding(new Insets(10));
        optionsBox.setStyle("-fx-background-color: #F5CBA7; -fx-padding: 10; -fx-border-color: #D35400;");

        Button backBtn = new Button("🔙 Back");
        backBtn.setStyle("-fx-background-color: #E67E22; -fx-font-weight: bold;");
        backBtn.setOnAction(e -> {
            optionPane.setVisible(false);
            filePane.setVisible(true);
        });

        Button searchBtn = new Button("Start Search 🔍");
        searchBtn.setStyle("-fx-background-color: #E67E22; -fx-font-weight: bold;");
        searchBtn.setOnAction(e -> {
            if (fileSearchRadio.isSelected() && selectedFiles.isEmpty()) {
                showAlert("Please select at least one file for multiple file search.");
                return;
            }
            optionPane.setVisible(false);
            resultPane.setVisible(true);
            resultArea.clear();
            
            String searchMode = noneRadio.isSelected() ? "All files in folder" : "Multiple files (" + selectedFiles.size() + " files)";
            resultArea.appendText("Search Mode: " + searchMode + "\n");
            resultArea.appendText("Searching for pattern: " + patternField.getText() + "\n");
            resultArea.appendText("Case Sensitive: " + (caseSensitiveBox.isSelected() ? "Yes" : "No") + "\n\n");
            runPergSearch(patternField.getText());
        });

        optionPane.getChildren().addAll(optionsBox, new HBox(20, backBtn, searchBtn));
    }

    private void updateSelectedFilesDisplay() {
        selectedFilesDisplay.getChildren().clear();
        
        if (selectedFiles.isEmpty()) {
            Label noFilesLabel = new Label("No files selected");
            noFilesLabel.setStyle("-fx-text-fill: #6C757D; -fx-font-style: italic;");
            selectedFilesDisplay.getChildren().add(noFilesLabel);
        } else {
            for (int i = 0; i < selectedFiles.size(); i++) {
                File file = selectedFiles.get(i);
                HBox fileRow = new HBox(10);
                fileRow.setAlignment(Pos.CENTER_LEFT);
                
                Label fileLabel = new Label((i + 1) + ". " + file.getName());
                fileLabel.setStyle("-fx-text-fill: #495057;");
                
                Label pathLabel = new Label("(" + file.getParent() + ")");
                pathLabel.setStyle("-fx-text-fill: #6C757D; -fx-font-size: 10px;");
                
                VBox fileInfo = new VBox(2, fileLabel, pathLabel);
                fileRow.getChildren().add(fileInfo);
                
                selectedFilesDisplay.getChildren().add(fileRow);
            }
        }
    }

    private void setupResultPane() {
        resultPane.setAlignment(Pos.TOP_CENTER);
        resultPane.setPadding(new Insets(20));
        resultPane.setBackground(new Background(new BackgroundFill(Color.web("#7d3c98"), CornerRadii.EMPTY, Insets.EMPTY)));

        Label label = new Label("Search Results:");
        label.setStyle("-fx-font-size: 18px; -fx-text-fill: #F5CBA7; -fx-font-weight: bold;");

        resultArea.setEditable(false);
        resultArea.setPrefHeight(500);
        resultArea.setWrapText(true);

        Button backBtn = new Button("🔙 Back");
        backBtn.setStyle("-fx-background-color: #E67E22; -fx-font-weight: bold;");
        backBtn.setOnAction(e -> {
            resultPane.setVisible(false);
            optionPane.setVisible(true);
        });

        Button restartBtn = new Button("🔁 Back to Start");
        restartBtn.setStyle("-fx-background-color: #E67E22; -fx-font-weight: bold;");
        restartBtn.setOnAction(e -> {
            resultPane.setVisible(false);
            patternPane.setVisible(true);
        });

        resultPane.getChildren().addAll(label, resultArea, new HBox(20, backBtn, restartBtn));
    }

    private void runPergSearch(String pattern) {
        new Thread(() -> {
            try {
                if (fileSearchRadio.isSelected() && !selectedFiles.isEmpty()) {
                    // Search multiple files individually
                    for (int i = 0; i < selectedFiles.size(); i++) {
                        File file = selectedFiles.get(i);
                        Platform.runLater(() -> {
                            resultArea.appendText("=== Searching File " + (selectedFiles.indexOf(file) + 1) + "/" + selectedFiles.size() + ": " + file.getName() + " ===\n");
                        });
                        
                        searchSingleFile(file, pattern, i + 1);
                        
                        if (i < selectedFiles.size() - 1) {
                            Platform.runLater(() -> {
                                resultArea.appendText("\n" + "=".repeat(50) + "\n\n");
                            });
                        }
                    }
                } else {
                    // Search directory
                    searchDirectory(pattern);
                }
            } catch (Exception e) {
                Platform.runLater(() -> resultArea.appendText("\nError: " + e.getMessage() + "\n"));
            }
        }).start();
    }

    private void searchSingleFile(File file, String pattern, int fileNumber) {
        try {
            long startTime = System.nanoTime();

            List<String> command = new ArrayList<>();
            command.add("C:\\Users\\dell\\Downloads\\JAVA PDC\\app\\src\\main\\java\\org\\example\\perg.exe");
            command.add("-f");
            command.add(file.getAbsolutePath());

            if (recursiveBox.isSelected()) command.add("-r");
            if (verboseBox.isSelected()) command.add("-V");
            if (invertBox.isSelected()) command.add("-v");
            if (parallelBox.isSelected()) command.add("-w");
            if (hiddenBox.isSelected()) command.add("-i");
            if (caseSensitiveBox.isSelected()) command.add("-c");
            if (afterContextBox.isSelected()) {
                command.add("-A");
                command.add(afterContextField.getText().trim().isEmpty() ? "1" : afterContextField.getText().trim());
            }

            command.add(pattern);

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);

            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            long endTime = System.nanoTime();
            long durationMillis = (endTime - startTime) / 1_000_000;

            String resultText = output.toString().trim();
            Platform.runLater(() -> {
                if (resultText.isEmpty()) {
                    resultArea.appendText("NO MATCHES FOUND\n");
                } else if (resultText.contains("not found")) {
                    resultArea.appendText(resultText + "\n");
                } else {
                    resultArea.appendText("Results:\n");
                    resultArea.appendText(resultText + "\n");
                }
                resultArea.appendText("Execution Time: " + durationMillis + " ms\n");
            });

        } catch (Exception e) {
            Platform.runLater(() -> resultArea.appendText("Error searching file: " + e.getMessage() + "\n"));
        }
    }

    private void searchDirectory(String pattern) {
        try {
            long startTime = System.nanoTime();

            List<String> command = new ArrayList<>();
            command.add("C:\\Users\\dell\\Downloads\\JAVA PDC\\app\\src\\main\\java\\org\\example\\perg.exe");

            if (recursiveBox.isSelected()) command.add("-r");
            if (verboseBox.isSelected()) command.add("-V");
            if (invertBox.isSelected()) command.add("-v");
            if (parallelBox.isSelected()) command.add("-w");
            if (hiddenBox.isSelected()) command.add("-i");
            if (caseSensitiveBox.isSelected()) command.add("-c");
            if (afterContextBox.isSelected()) {
                command.add("-A");
                command.add(afterContextField.getText().trim().isEmpty() ? "1" : afterContextField.getText().trim());
            }

            command.add(pattern);

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            pb.directory(selectedDirectory);

            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            long endTime = System.nanoTime();
            long durationMillis = (endTime - startTime) / 1_000_000;

            String resultText = output.toString().trim();
            Platform.runLater(() -> {
                resultArea.appendText("Directory: " + selectedDirectory.getName() + "\n");
                
                if (resultText.isEmpty()) {
                    resultArea.appendText("NO MATCHES FOUND\n");
                } else if (resultText.contains("not found")) {
                    resultArea.appendText(resultText + "\n");
                } else {
                    resultArea.appendText("Results:\n");
                    resultArea.appendText(resultText + "\n");
                }
                resultArea.appendText("Execution Time: " + durationMillis + " ms\n\n");
            });

        } catch (Exception e) {
            Platform.runLater(() -> resultArea.appendText("Error searching directory: " + e.getMessage() + "\n"));
        }
    }

    private void showHelpFromExecutable() {
        new Thread(() -> {
            try {
                List<String> command = new ArrayList<>();
                command.add("C:\\Users\\dell\\Downloads\\JAVA PDC\\app\\src\\main\\java\\org\\example\\perg.exe");
                command.add("-h");

                ProcessBuilder pb = new ProcessBuilder(command);
                pb.redirectErrorStream(true);
                pb.directory(new File("C:\\Users\\dell\\Downloads\\JAVA PDC\\app\\src\\main\\java\\org\\example"));

                Process process = pb.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder helpOutput = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    helpOutput.append(line).append("\n");
                }

                int exitCode = process.waitFor();
                String helpText = helpOutput.toString();

                Platform.runLater(() -> {
                    Alert helpAlert = new Alert(Alert.AlertType.INFORMATION);
                    helpAlert.setTitle("Perg Help");
                    helpAlert.setHeaderText("Parallel File Search Help");
                    
                    if (helpText.trim().isEmpty()) {
                        helpAlert.setContentText("Help information not available. Please check if perg.exe is working correctly.");
                    } else {
                        helpAlert.setContentText(helpText);
                    }
                    
                    helpAlert.getDialogPane().setPrefWidth(800);
                    helpAlert.getDialogPane().setPrefHeight(600);
                    
                    TextArea textArea = new TextArea(helpText);
                    textArea.setEditable(false);
                    textArea.setWrapText(true);
                    textArea.setPrefRowCount(25);
                    textArea.setPrefColumnCount(100);
                    
                    helpAlert.getDialogPane().setContent(textArea);
                    helpAlert.showAndWait();
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Help Error");
                    errorAlert.setHeaderText("Could not retrieve help information");
                    errorAlert.setContentText("Error executing perg.exe -h: " + e.getMessage());
                    errorAlert.showAndWait();
                });
            }
        }).start();
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch();
    }
}