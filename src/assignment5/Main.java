package assignment5;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Main extends Application {

	private final int LEFT_COLUMN_FIELDS_WIDTH = 150;
	private final int LEFT_COLUMN_FIELDS_HEIGHT= 20;
	private GridPane gridPane;
	private HBox errorBox;
	private List<String> fileNames;
	private List<String> allCritterNames;
	private AnimationTimer animationTimer;
	private int animationSpeed = 1;
	private Text statsText;
	private String critterStat;
	private String myPackage;

	public static void main(String[] args) {
		launch(args);
	}

    /**
     * Overridden method for JavaFX. This contains all of the GUI handling.
     * @param primaryStage is the outermost container
     * @throws Exception exception
     */
	@Override
	public void start(Stage primaryStage) throws Exception{
	    //Get the names of the package and all assignable critters
        getAllAssignable();

        //Set Title and create layout container
		primaryStage.setTitle("CritterGUI");
		BorderPane borderPane = new BorderPane();

        //Create vertical box and add it to borderpane
        VBox vBox = addVBox();
		fillTopBox((VBox)vBox.getChildren().get(0));
		fillBottomBox((VBox) vBox.getChildren().get(1));
		borderPane.setLeft(vBox);

		//Create gridpane and display the world.
		gridPane = new GridPane();
		Critter.displayWorld(gridPane);
		gridPane.setStyle("-fx-background-color: rgba(120, 137, 154, 1)");
		borderPane.setCenter(gridPane);

        //Create horizontal box and add it to borderpane
        HBox hBox = addHBox();
        fillLeftBox((HBox) hBox.getChildren().get(0));
        fillRightBox((HBox) hBox.getChildren().get(1));
        borderPane.setTop(hBox);

		//Create lower horizontal box for error messages
		errorBox = new HBox();
		fillErrorBox();
		borderPane.setBottom(errorBox);

		//Create scene, add borderpane manager and add scene to stage.
		Scene scene = new Scene(borderPane);
		primaryStage.setScene(scene);
		animationTimer = new AnimationTimer() {
			private long lastUpdate = 0;
			@Override
			public void handle(long now) {
				if (now - lastUpdate >=500_000_000){
					for (int i = 0; i < animationSpeed; i++)
						Critter.worldTimeStep();
					updateWorld();
					updateStats();
					lastUpdate = now;
				}

			}
		};

		gridPane.setMinHeight(480);
        primaryStage.show();
		hideErrorText();
	}

    /**
     * Recursively search through all filies starting from directory supplied and append them to {@link #fileNames}
     * @param curDir is the directing to start traversing downwards
     * @param fileNames is the variable to append all file names to
     */
    private static void getAllFiles(File curDir, List<String> fileNames) {

        File[] filesList = curDir.listFiles();
        if (filesList != null) {
            for (File f : filesList) {
                if (f.isDirectory()) {
                    getAllFiles(f, fileNames);
                }
                if (f.isFile()) {
                    fileNames.add(f.getName());
                }
            }
        }
    }

    /**
     * Method searches through {@link #fileNames} to find java classes that are can be instantiated as a subclass of
     * Critter. Stores the simple names in {@link #allCritterNames}
     */
    private void getAllAssignable(){
        fileNames = new ArrayList<>();
        allCritterNames = new ArrayList<>();
        File curDir = new File("src");
        getAllFiles(curDir, fileNames);

        myPackage = Critter.class.getPackage().toString().split(" ")[1];
        for (String name: fileNames){
            String simpleName = name.substring(0, name.lastIndexOf('.'));
            try {
                Class critterClass = Class.forName(myPackage + '.' + simpleName);
                boolean critterBool = Critter.class.isAssignableFrom(critterClass) && critterClass != Critter.class;
                if (critterBool){
                    allCritterNames.add(simpleName);
                }
            } catch (ClassNotFoundException  | NoClassDefFoundError | ClassCastException e) {}
        }
    }

    /**
     * Creates a horizontal box composed of two horizontal boxes. Used for th Titlebar
     * @return a {@link HBox} with two children
     */
	private HBox addHBox() {
		HBox hbox = new HBox();

		HBox hboxL = new HBox();
        hboxL.setSpacing(10);
        hboxL.setPadding(new Insets(15, 49, 15, 50));
		hboxL.setStyle("-fx-background-color: rgba(53, 117, 180, 1)");

		HBox hboxR = new HBox();
		hboxR.setAlignment(Pos.CENTER);
		hboxR.setPadding(new Insets(0,0,0,10));
		hboxR.setSpacing(10);

		hbox.getChildren().addAll(hboxL, hboxR);

		return hbox;
	}

    /**
     * Creates a {@link VBox} with two Children. Used for the command toolbar
     * @return a Vbox with two children
     */
	private VBox addVBox() {
		VBox vbox = new VBox();

		VBox vboxT = new VBox();
		vboxT.setPadding(new Insets(10));
		vboxT.setSpacing(8);

		VBox vboxB = new VBox();
		vboxB.setPadding(new Insets(10));
		vboxB.setSpacing(8);

		vbox.getChildren().addAll(vboxT, vboxB);
		return vbox;
	}

    /**
     * Clear's the gridPane and displays a fresh world.
     */
	private void updateWorld(){
		gridPane.getChildren().clear();
		Critter.displayWorld(gridPane);
	}

    /**
     * Fills the command toolbar with widgets. This is specifically used for creating the command buttons and options
     * @param vbox is the {@link VBox} to fill.
     */
	private void fillTopBox(VBox vbox){
		Text firstTitle = new Text("Commands");
		firstTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
		vbox.getChildren().add(firstTitle);

		Button makeButton = new Button("Make");
		makeButton.setMinSize(LEFT_COLUMN_FIELDS_WIDTH,LEFT_COLUMN_FIELDS_HEIGHT);
		VBox.setMargin(makeButton, new Insets(0, 0, 0, 8));
		vbox.getChildren().add(makeButton);
		makeButton.setOnAction(e -> {
			vbox.getChildren().remove(6,vbox.getChildren().size());

			Text secondTitle = new Text("Make");
			secondTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
			vbox.getChildren().add(secondTitle);
			ComboBox<String> comboBox = new ComboBox<>();
			comboBox.setMaxSize(LEFT_COLUMN_FIELDS_WIDTH,LEFT_COLUMN_FIELDS_HEIGHT);
			comboBox.getItems().addAll(allCritterNames);
			comboBox.getSelectionModel().selectFirst();
			VBox.setMargin(comboBox, new Insets(0, 0, 0, 8));
			vbox.getChildren().add(comboBox);

			TextField textField = new TextField();
			VBox.setMargin(textField, new Insets(0, 0, 0, 8));
			textField.setMaxSize(LEFT_COLUMN_FIELDS_WIDTH, LEFT_COLUMN_FIELDS_HEIGHT);
			textField.setPromptText("How many (#)");
			vbox.getChildren().add(textField);

			Button okButton = new Button("OK");
			VBox.setMargin(okButton, new Insets(0, 0, 0, LEFT_COLUMN_FIELDS_WIDTH-26));
			vbox.getChildren().add(okButton);
			okButton.setOnAction(g -> {
				String critterString = comboBox.getValue();
				String sNumber = textField.getText();
				int iNumber = 0;
				try{
					iNumber = Integer.parseInt(sNumber);
					for (int i = 0; i < iNumber; i++)
						Critter.makeCritter(critterString);
					updateWorld();
					hideErrorText();
				} catch (Exception f){
					setErrorText("'" + sNumber + "' is not a viable input. Try entering a positive integer.");
				}
			});

			Separator separator = new Separator();
			vbox.getChildren().add(separator);
		});

		Button seedButton = new Button("Seed");
		seedButton.setMinSize(LEFT_COLUMN_FIELDS_WIDTH,LEFT_COLUMN_FIELDS_HEIGHT);
		VBox.setMargin(seedButton, new Insets(0, 0, 0, 8));
		vbox.getChildren().add(seedButton);
		seedButton.setOnAction(event -> {
			vbox.getChildren().remove(6,vbox.getChildren().size());

			Text secondTitle = new Text("Seed");
			secondTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
			vbox.getChildren().add(secondTitle);

			TextField textField = new TextField();
			VBox.setMargin(textField, new Insets(0, 0, 33, 8));
			textField.setMaxSize(LEFT_COLUMN_FIELDS_WIDTH, LEFT_COLUMN_FIELDS_HEIGHT);
			textField.setPromptText("New Seed (#)");
			vbox.getChildren().add(textField);

			Button okButton = new Button("OK");
			VBox.setMargin(okButton, new Insets(0, 0, 0, LEFT_COLUMN_FIELDS_WIDTH-26));
			vbox.getChildren().add(okButton);
			okButton.setOnAction(g -> {
				String sNumber = textField.getText();
				long iNumber = 0;
				try{
					iNumber = Integer.parseInt(sNumber);
					Critter.setSeed(iNumber);
					hideErrorText();
				} catch (Exception e){
					setErrorText("'" + sNumber + "' is not a viable input. Try entering a positive integer");
				}
			});
			Separator separator = new Separator();
			vbox.getChildren().add(separator);
		});

		Button animateButton = new Button("Animate");
		animateButton.setMinSize(LEFT_COLUMN_FIELDS_WIDTH,LEFT_COLUMN_FIELDS_HEIGHT);
		VBox.setMargin(animateButton, new Insets(0, 0, 0, 8));
		vbox.getChildren().add(animateButton);
		animateButton.setOnAction(event -> {
			vbox.getChildren().remove(6,vbox.getChildren().size());

			Text secondTitle = new Text("Animate");
			secondTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
			vbox.getChildren().add(secondTitle);

			TextField textField = new TextField();
			VBox.setMargin(textField, new Insets(0, 0, 33, 8));
			textField.setMaxSize(LEFT_COLUMN_FIELDS_WIDTH, LEFT_COLUMN_FIELDS_HEIGHT);
			textField.setPromptText("Animation Speed (#)");
			vbox.getChildren().add(textField);

			Button okButton = new Button("OK");
			VBox.setMargin(okButton, new Insets(0, 0, 0, LEFT_COLUMN_FIELDS_WIDTH-26));
			vbox.getChildren().add(okButton);
			okButton.setOnAction(g -> {
				String sNumber = textField.getText();
				int iNumber = 0;
				try{
					iNumber = Integer.parseInt(sNumber);
					animationSpeed = iNumber;
					hideErrorText();
				} catch (Exception e){
					setErrorText("'" + sNumber + "' is not a viable input. Try entering a positive integer");
				}
			});
			Separator separator = new Separator();
			vbox.getChildren().add(separator);
		});

		Button clearButton = new Button("Clear World");
		clearButton.setMinSize(LEFT_COLUMN_FIELDS_WIDTH,LEFT_COLUMN_FIELDS_HEIGHT);
		VBox.setMargin(clearButton, new Insets(0, 0, 0, 8));
		clearButton.setOnAction(e -> {
			Critter.clearWorld();
			updateWorld();
			statsText.setText("");
		});
		vbox.getChildren().add(clearButton);

		Separator separator = new Separator();
		vbox.getChildren().add(separator);

		makeButton.fire();
	}

    /**
     * Fills the command toolbar with widgets. This is specifically used for creating the stats section
     * @param vbox is the {@link VBox} to fill.
     */
	private void fillBottomBox(VBox vbox){
		Text firstTitle = new Text("Stats");
		firstTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
		vbox.getChildren().add(firstTitle);

		ComboBox<String> comboBox = new ComboBox<>();
		comboBox.getItems().addAll(allCritterNames);
		comboBox.getSelectionModel().selectFirst();
		comboBox.setOnAction(e ->{
			critterStat = comboBox.getValue();
			statsText.setText("");
		});
		VBox.setMargin(comboBox, new Insets(0, 0, 0, 8));
		vbox.getChildren().add(comboBox);
		critterStat = comboBox.getValue();

		statsText = new Text();
		VBox.setMargin(statsText, new Insets(0,0,0,8));
		statsText.setWrappingWidth(LEFT_COLUMN_FIELDS_WIDTH);
		vbox.getChildren().add(statsText);
	}

    /**
     * Method uses reflection to select which Critter class should have its stats updated.
     */
	private void updateStats(){
		try{
			Class critterClass = Class.forName(myPackage + "." + critterStat);
			List<Critter> critterList = Critter.getInstances(critterClass.getSimpleName());
			Method method = critterClass.getMethod("runStats", List.class);
			String stats = (String) method.invoke(null, critterList);
			statsText.setText(stats);
		} catch (Exception e){
			e.printStackTrace();
		}
	}

    /**
     * Creation of title for project
     * @param hbox is the {@link HBox} to fill
     */
	private void fillLeftBox(HBox hbox){
		Text title = new Text("Critters");
		title.setFont(Font.font("Arial", FontWeight.BOLD, 22));
		hbox.getChildren().add(title);
	}

    /**
     * Creation of widgets for animation options.
     * @param hbox is the {@link HBox} to fill
     */
	private void fillRightBox(HBox hbox){
		Button buttonCurrent = new Button("Stop Animation");
		buttonCurrent.setOnAction(e ->{
			animationTimer.stop();
		});
		buttonCurrent.setPrefSize(100, 20);

		Button buttonProjected = new Button("Start Animation");
		buttonProjected.setOnAction(e ->{
			animationTimer.start();
		});
		buttonProjected.setPrefSize(100, 20);
		hbox.getChildren().addAll(buttonProjected, buttonCurrent);
	}

    /**
     * Fills the error box. No paramater as it needs to be accessed from a higher scope
     */
	private void fillErrorBox(){
		Text title = new Text("Error message: ");
		title.setFont(Font.font("Arial", FontWeight.BOLD, 14));
		HBox.setMargin(title, new Insets(10));
		errorBox.getChildren().add(title);
	}

    /**
     * Set the error mssage to the desired input.
     * @param error is the error message to display
     */
	private void setErrorText(String error){
		errorBox.getChildren().get(0).setVisible(true);
		errorBox.getChildren().get(0).managedProperty().bind(errorBox.visibleProperty());
		System.out.println("WTF");
		((Text) errorBox.getChildren().get(0)).setText("Error message: " + error);
	}

    /**
     * Hide error message rom view
     */
	private void hideErrorText(){
		errorBox.getChildren().get(0).setVisible(false);
	}

}
