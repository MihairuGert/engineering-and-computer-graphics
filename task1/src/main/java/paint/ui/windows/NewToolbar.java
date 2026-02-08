package paint.ui.windows;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import paint.ui.DrawPanel;
import paint.ui.UI;

public class NewToolbar extends VBox {
    private TextField widthField;
    private TextField heightField;
    private Button createButton;

    private DrawPanel drawPanel;

    private Stage owner;

    public NewToolbar(Stage owner, DrawPanel drawPanel) {
        super();

        this.drawPanel = drawPanel;
        this.owner = owner;

        setPadding(new Insets(10));
        setSpacing(10);
        setStyle("-fx-background-color: #f4f4f4;");

        initializeComponents();
        setupEventHandlers();
    }

    private void setupEventHandlers() {
        createButton.setOnAction(event -> {
            try {
                drawPanel.newImage(getW(), getH());
            } catch (Exception e) {
                UI.showErr(e.getMessage(), "Попробуйте ввести число в интервале от 100 до 4000");
                return;
            }
            owner.close();
        });
    }

    private void initializeComponents() {
        Label titleLabel = new Label("Создать новый рисунок");
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Label sizeLabel = new Label("Размеры:");
        sizeLabel.setStyle("-fx-font-weight: bold;");

        GridPane sizeGrid = new GridPane();
        sizeGrid.setHgap(10);
        sizeGrid.setVgap(10);
        sizeGrid.setPadding(new Insets(5, 0, 5, 0));

        Label widthLabel = new Label("Ширина:");
        widthField = new TextField();
        widthField.setPromptText("от 100 до 4000");
        widthField.setText("1000");

        Label heightLabel = new Label("Высота:");
        heightField = new TextField();
        heightField.setPromptText("от 100 до 4000");
        heightField.setText("1000");

        sizeGrid.add(widthLabel, 0, 0);
        sizeGrid.add(widthField, 1, 0);
        sizeGrid.add(heightLabel, 0, 1);
        sizeGrid.add(heightField, 1, 1);

        createButton = new Button("Создать");
        createButton.setStyle("-fx-font-weight: bold; -fx-background-color: #4CAF50; -fx-text-fill: white;");

        getChildren().addAll(titleLabel, sizeLabel, sizeGrid, createButton);
    }

    public int getW() throws NumberFormatException{
        try {
            int width = Integer.parseInt(widthField.getText());
            if (width < 100 || width > 4000) {
                throw new NumberFormatException("Неправильный формат: " + widthField.getText());
            }
            return width;
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Неправильный формат: " + widthField.getText());
        }
    }

    public int getH() {
        try {
            int height = Integer.parseInt(heightField.getText());
            if (height < 100 || height > 4000) {
                throw new NumberFormatException("Неправильный формат: " + heightField.getText());
            }
            return height;
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Неправильный формат: " + widthField.getText());
        }
    }
}
