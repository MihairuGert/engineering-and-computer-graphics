package paint.ui;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import paint.ui.clickable.*;
import paint.ui.windows.NewWindow;
import paint.ui.windows.SettingsDialog;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.layout.Region;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Menu extends ToolBar {
    private Clickable new_b;
    private Clickable open;
    private Clickable save;
    private Clickable settingsBtn;
    private Clickable aboutBtn;

    private Map<Button, ToolButton> toolMap = new HashMap<>();
    private ToolButton activeTool = null;

    private Stage stage;
    private DrawPanel drawPanel;
    private Settings settings;
    private SettingsDialog settingsDialog;

    private NewWindow newWindow;

    private final ReadOnlyObjectWrapper<ToolButton> activeToolProp = new ReadOnlyObjectWrapper<>();

    private Button redButton;
    private Button greenButton;
    private Button blueButton;
    private Button yellowButton;
    private Region currentColorIndicator;

    public ReadOnlyObjectProperty<ToolButton> activeToolProperty() {
        return activeToolProp.getReadOnlyProperty();
    }

    public void activateTool(ToolMode mode) {
        for (ToolButton tb : toolMap.values()) {
            if (tb.getToolMode() == mode) {
                setActiveTool(tb);
                return;
            }
        }
    }

    private void setActiveTool(ToolButton newTool) {
        if (activeTool == newTool) {
            if (activeTool != null) {
                activeTool.setInactive();
                activeTool.onDeactivate(drawPanel);
            }
            drawPanel.setCurrentTool(ToolMode.NONE);
            activeTool = null;
        } else {
            if (activeTool != null) {
                activeTool.setInactive();
                activeTool.onDeactivate(drawPanel);
            }
            if (newTool != null) {
                newTool.setActive();
                newTool.onActivate(drawPanel);
                drawPanel.setCurrentTool(newTool.getToolMode());
            }
            activeTool = newTool;
        }
        activeToolProp.set(activeTool);
    }

    public Menu(Stage stage, DrawPanel drawPanel) {
        super();
        this.stage = stage;
        this.drawPanel = drawPanel;
        this.settings = new Settings();

        new_b = new Clickable("new.png");
        new_b.button.setOnAction(this::handleNew);
        new_b.setTip("Создать новое изображение");

        open = new Clickable("open.png");
        open.button.setOnAction(this::handleOpen);
        open.setTip("Открыть новое изображение");

        save = new Clickable("save.png");
        save.button.setOnAction(this::handleSave);
        save.setTip("Сохранить текущее изображение");

        ToolButton line = new Line("line_active.png", "line.png");
        line.button.setOnAction(this::handleToolAction);
        line.setTip("Линия");
        toolMap.put(line.button, line);

        ToolButton pencil = new Pencil("pencil_active.png", "pencil.png");
        pencil.button.setOnAction(this::handleToolAction);
        pencil.setTip("Карандаш");
        toolMap.put(pencil.button, pencil);

        ToolButton fill = new Fill("fill_active.png", "fill.png");
        fill.button.setOnAction(this::handleToolAction);
        fill.setTip("Заливка");
        toolMap.put(fill.button, fill);

        ToolButton stamp = new Stamp("stamp_active.png", "stamp.png");
        stamp.button.setOnAction(this::handleToolAction);
        stamp.setTip("Штамп");
        toolMap.put(stamp.button, stamp);

        settingsBtn = new Clickable("settings.png");
        settingsBtn.button.setOnAction(this::handleSettings);
        settingsBtn.setTip("Настройки");

        aboutBtn = new Clickable("about.png");
        aboutBtn.button.setOnAction(this::handleAbout);
        aboutBtn.setTip("О программе");

        redButton = createColorButton(Color.RED);
        greenButton = createColorButton(Color.GREEN);
        blueButton = createColorButton(Color.BLUE);
        yellowButton = createColorButton(Color.YELLOW);

        currentColorIndicator = createColorIndicator();

        getItems().addAll(aboutBtn.button, settingsBtn.button, new Separator(),
                new_b.button, open.button, save.button, new Separator(),
                pencil.button, line.button, fill.button, stamp.button,
                new Separator(),
                redButton, greenButton, blueButton, yellowButton,
                new Separator(),
                currentColorIndicator);
    }

    private Button createColorButton(Color color) {
        Button btn = new Button();
        btn.setPrefSize(20, 20);
        btn.setPadding(Insets.EMPTY);
        btn.setStyle("-fx-background-color: " + toRgbString(color) + "; -fx-border-color: black; -fx-border-width: 1;");
        btn.setOnAction(e -> {
            settings.setCurrentColor(color);
            updateCurrentColorIndicator();
        });
        return btn;
    }

    private Region createColorIndicator() {
        Region indicator = new Region();
        indicator.setPrefSize(20, 20);
        indicator.setStyle("-fx-background-color: " + toRgbString(settings.getCurrentColor()) + "; -fx-border-color: black; -fx-border-width: 1;");
        return indicator;
    }

    private void updateCurrentColorIndicator() {
        currentColorIndicator.setStyle("-fx-background-color: " + toRgbString(settings.getCurrentColor()) + "; -fx-border-color: black; -fx-border-width: 1;");
    }

    private String toRgbString(Color color) {
        return String.format("rgb(%d, %d, %d)",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    public void handleToolAction(ActionEvent event) {
        Button source = (Button) event.getSource();
        ToolButton clickedTool = toolMap.get(source);
        if (clickedTool != null) {
            setActiveTool(clickedTool);
        }
    }

    public static ImageView createIcon(String source) {
        ImageView image = new ImageView(source);
        image.setFitHeight(20);
        image.setFitWidth(20);
        return image;
    }

    public static Button newButtonWithImage(String source) {
        ImageView img = createIcon(source);
        Button button = new Button();
        button.setGraphic(img);
        button.setPadding(Insets.EMPTY);
        button.setStyle("-fx-background-color: transparent; -fx-border-width: 0;");

        return button;
    }

    void handleNew(ActionEvent event) {
        newWindow.showNew();
    }

    void handleOpen(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null && drawPanel != null) {
            try {
                Image image = new Image("file:" + selectedFile.getAbsolutePath());

                drawPanel.loadImage(image);

                System.out.println("Loaded image: " + selectedFile.getAbsolutePath());
            } catch (Exception e) {
                System.err.println("Error loading image: " + e.getMessage());
            }
        }
    }

    void handleSave(ActionEvent event) {
        if (drawPanel == null || drawPanel.getImage() == null) {
            UI.showErr("Ошибка сохранения", "Нет изображения для сохранения.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить изображение");

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PNG Image (*.png)", "*.png"),
                new FileChooser.ExtensionFilter("JPEG Image (*.jpg)", "*.jpg"),
                new FileChooser.ExtensionFilter("BMP Image (*.bmp)", "*.bmp"),
                new FileChooser.ExtensionFilter("GIF Image (*.gif)", "*.gif")
        );

        fileChooser.setInitialFileName("шедеврдостойныйТретьяковскойГалерее.png");

        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                FileChooser.ExtensionFilter selectedFilter = fileChooser.getSelectedExtensionFilter();
                String extension = "";

                String description = selectedFilter.getDescription();
                if (description.contains("PNG")) {
                    extension = "png";
                } else if (description.contains("JPEG") || description.contains("JPG")) {
                    extension = "jpg";
                } else if (description.contains("BMP")) {
                    extension = "bmp";
                } else if (description.contains("GIF")) {
                    extension = "gif";
                }

                WritableImage writableImage = drawPanel.getImg();

                saveImage(writableImage, file, extension);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Сохранение успешно!");
                alert.setHeaderText(null);
                alert.setContentText("Изображение сохранено в файл:\n" + file.getAbsolutePath());
                alert.showAndWait();

            } catch (Exception e) {
                UI.showErr("Ошибка сохранения", "Не удалось сохранить изображение: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void saveImage(WritableImage writableImage, File file, String format) throws IOException {
        BufferedImage bImage = SwingFXUtils.fromFXImage(writableImage, null);

        if (format.equalsIgnoreCase("png")) {
            ImageIO.write(bImage, "png", file);
        } else if (format.equalsIgnoreCase("jpg") || format.equalsIgnoreCase("jpeg")) {
            BufferedImage rgbImage = new BufferedImage(
                    bImage.getWidth(),
                    bImage.getHeight(),
                    BufferedImage.TYPE_INT_RGB
            );
            rgbImage.createGraphics().drawImage(bImage, 0, 0, null);
            ImageIO.write(rgbImage, "jpg", file);
        } else if (format.equalsIgnoreCase("bmp")) {
            ImageIO.write(bImage, "bmp", file);
        } else if (format.equalsIgnoreCase("gif")) {
            ImageIO.write(bImage, "gif", file);
        }
    }

    void handleSettings(ActionEvent event) {
        if (settingsDialog == null) {
            settingsDialog = new SettingsDialog(stage, settings);
        }
        settingsDialog.showAndWait();
        updateCurrentColorIndicator();
    }

    void handleAbout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("О пыинте");
        alert.setHeaderText("Пыинт v0.1");
        alert.setContentText("Автор: Пятанов М.Ю.\n\n" +
                "Свой \"MS Paint\", сделанный в рамках курса по инженерной графике НГУ ФИТ 3 курс.\n\n" +
                "Подсказки по использованию:\n" +
                "Инструмент «линия» характеризуется двумя параметрами: толщиной линии и текущим цветом. \n" +
                "Линии рисуются поверх существующего изображения.\n\n" +
                "Инструмент «штамп» осуществляет рисование контура выбранной фигуры.\n" +
                        " Фигурой является правильный выпуклый многоугольник или правильная звезда.\n" +
                        " Штамп рисуется поверх существующего изображения линиями толщины 1 черным цветом.\n" +
                        " Параметры инструмента «штамп»:\n" +
                        " 1) форма (тип многоугольника);\n" +
                        " 2) размер (радиус многоугольника в пикселях);\n" +
                        " 3) поворот (поворот многоугольника вокруг центра в градусах).\n\n" +
                        "Инструмент «заливка» начиная с указанной кликом точки (точки затравки) выполняет алгоритм Span-заливки.\n" +
                        " То есть все пиксели четырехсвязной пиксельной области цвета затравки заполняются текущим цветом.\n" +
                        " Инструмент «заливка» имеет один параметр – цвет. Цвет для заливки и рисования линии берется из текущего цвета.\n\n"+
                "© 2026 Все права защищены.");

        alert.showAndWait();
    }

    public Settings getSettings() {
        return settings;
    }

    public void setDrawPanel(DrawPanel drawPanel) {
        this.drawPanel = drawPanel;
        if (drawPanel != null && newWindow == null) {
            newWindow = new NewWindow(stage, drawPanel);
        }
    }
}