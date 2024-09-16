package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionRequest;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionResult;
import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;
import nz.ac.auckland.apiproxy.chat.openai.Choice;
import nz.ac.auckland.apiproxy.config.ApiProxyConfig;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameStateContext;
import nz.ac.auckland.se206.ImageManager;
import nz.ac.auckland.se206.Person;
import nz.ac.auckland.se206.SceneManager;
import nz.ac.auckland.se206.Utils;
import nz.ac.auckland.se206.prompts.PromptEngineering;
import nz.ac.auckland.se206.ringIndicator.RingProgressIndicator;
import nz.ac.auckland.se206.speech.TextToSpeech;

/**
 * Controller class for the room view. Handles user interactions within the room where the user can
 * chat with customers and guess their profession.
 */
public class RoomController {
  private static boolean isFirstTimeInit = true;
  private static boolean hasTalked = false;
  private static boolean walletFound = false;
  private static boolean cameraFound = false;
  private static boolean dashcamFound = false;
  private static boolean isCarFound = false;
  private static GameStateContext context = new GameStateContext();
  private static double timeToCount = 80000;
  private static double timeToCountTo = 80000;
  private static double timeForGuessing = 60000;
  private static int progress = 0;
  private static RingProgressIndicator ringProgressIndicator = new RingProgressIndicator();

  @FXML private Rectangle rectPerson1;
  @FXML private Rectangle rectPerson2;
  @FXML private Rectangle rectPerson3;
  @FXML private Rectangle officer;
  @FXML private Rectangle officer2;
  @FXML private Rectangle camera;
  @FXML private Rectangle dashcam;
  @FXML private Rectangle car;

  @FXML private Ellipse trashBin;

  @FXML private Label lblProfession;
  @FXML private Label timerLabel;
  @FXML private Label chatStats;

  @FXML private Button btnGuess;
  @FXML private Button btnSend;
  @FXML private Button btnBack;
  @FXML private Button btnSlide;

  @FXML private TextArea txtaChat;

  @FXML private TextField txtInput;

  @FXML private ImageView carImage;
  @FXML private ImageView ownerImage;
  @FXML private ImageView workerImage;
  @FXML private ImageView brotherImage;
  @FXML private ImageView crimeImage;
  @FXML private ImageView displayImage;

  @FXML private StackPane indicatorPane;
  @FXML private Pane statsPane;

  @FXML public ComboBox<HBox> imagesComboBox;

  @FXML private HBox imagesHBox;

  private ChatCompletionRequest chatCompletionRequest;
  private Person person;
  private ImageView currentImage = null;

  public ImageManager currentImageManager;
  public ImageManager ownerImageManager;
  public ImageManager workerImageManager;
  public ImageManager brotherImageManager;
  public ImageManager crimeImageManager;
  public Scene suspectScene;

  private Timeline timeline = new Timeline();

  /**
   * Initializes the room view. If it's the first time initialization, it will provide instructions
   * via text-to-speech.
   */
  @FXML
  public void initialize() {
    // Probably delete this since we will only load this scene once
    if (isFirstTimeInit) {
      isFirstTimeInit = false;
    }

    btnSend
        .sceneProperty()
        .addListener(
            (observable, oldScene, newScene) -> {
              Stage stage = (Stage) newScene.getWindow();
              stage.sizeToScene();
              if (newScene != null) {
                newScene.addEventHandler(
                    KeyEvent.KEY_PRESSED,
                    event -> {
                      if (event.getCode() == KeyCode.ENTER) {
                        try {
                          onSendMessage(new ActionEvent());
                        } catch (ApiProxyException | IOException e) {
                          e.printStackTrace();
                        }
                      }
                    });
              }
            });

    currentImageManager = new ImageManager(currentImage);
    ownerImageManager = new ImageManager(ownerImage);
    workerImageManager = new ImageManager(workerImage);
    brotherImageManager = new ImageManager(brotherImage);
    crimeImageManager = new ImageManager(crimeImage);

    ColorAdjust colorAdjust = new ColorAdjust();
    colorAdjust.setBrightness(-0.45);
    ownerImage.setEffect(colorAdjust);
    workerImage.setEffect(colorAdjust);
    brotherImage.setEffect(colorAdjust);
    crimeImage.setEffect(colorAdjust);
    styleScene();

    context.setRoomController(this);
    indicatorPane.getChildren().add(ringProgressIndicator);
    ringProgressIndicator.setRingWidth(60);
    // Timer label is updated here
    if (timeToCount % 1000 == 0) {
      timerLabel.setText(Utils.formatTime(timeToCount - timeForGuessing));
    }

    timeline
        .getKeyFrames()
        .add(
            new KeyFrame(
                Duration.millis(1),
                event -> {
                  if (timeToCount > timeForGuessing) {
                    timeToCount--;
                    progress =
                        (int)
                            (100
                                - (((timeToCountTo - timeForGuessing)
                                        - (timeToCount - timeForGuessing))
                                    * 100
                                    / (timeToCountTo - timeForGuessing)));
                  } else if ((timeToCount > 0)) {
                    // Here the timer has exceeded the time for investigation and the game must
                    // switch to the guess scene.
                    // Program switch to guess scene here.
                    System.out.println("Switching to guessing state");
                    context.setState(context.getGuessingState());
                    try {
                      App.setRoot("guess");
                    } catch (IOException e) {
                      e.printStackTrace();
                    }
                    // Stop the timer here, as once the suer switch to guessing state, they aren't
                    // coming back
                    timeline.stop();
                  }

                  ringProgressIndicator.setProgress(progress);
                  timerLabel.setText(Utils.formatTime(timeToCount - timeForGuessing));
                }));
    timeline.setCycleCount(Timeline.INDEFINITE);
    timeline.play();
    // play an instruction sound when entering the room for the first time
    // Media media = new Media(getClass().getResource("/sounds/enter_room.mp3").toExternalForm());
    // MediaPlayer mediaPlayer = new MediaPlayer(media);
    // mediaPlayer.play();
    // isFirstTimeInit = false;
    // }
  }

  public static void setTimeToCount(double timeFromPreviousScene) {
    timeToCount = timeFromPreviousScene;
  }

  public static void setProgress(int progressFromPreviousScene) {
    progress = progressFromPreviousScene;
  }

  public Pane getStatsPane() {
    return statsPane;
  }

  // Don't think this is needed anymore
  // public Boolean getTimeOver() {
  //   return isTimeOver;
  // }

  public void disableAll() {
    officer.setDisable(true);
    officer2.setDisable(true);
    trashBin.setDisable(true);
    camera.setDisable(true);
    dashcam.setDisable(true);
    car.setDisable(true);
  }

  public void talked() {
    hasTalked = true;
  }

  public void noTalking() {
    rectPerson1.setDisable(true);
    rectPerson2.setDisable(true);
    rectPerson3.setDisable(true);
    officer.setDisable(true);
    officer2.setDisable(true);
    btnSend.setDisable(true);
    workerImage.setDisable(true);
    ownerImage.setDisable(true);
    brotherImage.setDisable(true);
  }

  public void enableTalking() {
    rectPerson1.setDisable(false);
    rectPerson2.setDisable(false);
    rectPerson3.setDisable(false);
    officer.setDisable(false);
    officer2.setDisable(false);
    btnSend.setDisable(false);
    workerImage.setDisable(false);
    ownerImage.setDisable(false);
    brotherImage.setDisable(false);
  }

  public Rectangle getDashcam() {
    return dashcam;
  }

  public Button getBtnBack() {
    return btnBack;
  }

  public void foundWallet() {
    walletFound = true;
  }

  public Boolean isWalletFound() {
    return walletFound;
  }

  public void foundCamera() {
    cameraFound = true;
  }

  public void foundCar() {
    isCarFound = true;
  }

  public Boolean isCameraFound() {
    return cameraFound;
  }

  public Boolean getHasTalked() {
    return hasTalked;
  }

  public Boolean isDashcamFound() {
    return dashcamFound;
  }

  public Boolean isCarFound() {
    return isCarFound;
  }

  public Button getBtnGuess() {
    return btnGuess;
  }

  public void stopTimeLine() {
    timeline.stop();
  }

  public ImageView getCarImage() {
    return carImage;
  }

  public GameStateContext getContext() {
    return context;
  }

  public void setChatStats(String stats) {
    chatStats.setText(stats);
  }

  public void diableRectangles() {
    rectPerson1.setDisable(true);
    rectPerson2.setDisable(true);
    rectPerson3.setDisable(true);
    officer.setDisable(true);
    officer2.setDisable(true);
    trashBin.setDisable(true);
    dashcam.setDisable(false);
    car.setDisable(true);
  }

  public void enableRectangles() {
    rectPerson1.setDisable(false);
    rectPerson2.setDisable(false);
    rectPerson3.setDisable(false);
    officer.setDisable(false);
    officer2.setDisable(false);
    trashBin.setDisable(false);
    car.setDisable(false);
    dashcam.setDisable(true);
  }

  public Person getPerson() {
    return person;
  }

  /**
   * Handles the key pressed event.
   *
   * @param event the key event
   */
  @FXML
  public void onKeyPressed(KeyEvent event) {
    System.out.println("Key " + event.getCode() + " pressed");
  }

  /**
   * Handles the key released event.
   *
   * @param event the key event
   */
  @FXML
  public void onKeyReleased(KeyEvent event) {
    System.out.println("Key " + event.getCode() + " released");
  }

  /**
   * Handles the event when the crime scene icon is clicked. This method is triggered by a mouse
   * click event on the crime scene element. It performs the necessary actions to transition to the
   * crime scene view.
   *
   * @param event the MouseEvent that triggered this handler
   * @throws IOException if an input or output exception occurs
   * @throws ApiProxyException if there is an issue with the API proxy
   */
  @FXML
  void onCrimeSceneClicked(MouseEvent event) throws ApiProxyException, IOException {
    Scene sceneOfButton = btnGuess.getScene();
    sceneOfButton.setRoot(SceneManager.getRoot(SceneManager.Scene.CRIME));
  }

  /**
   * Handles mouse clicks on rectangles representing people in the room.
   *
   * @param event the mouse event triggered by clicking a rectangle
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void handleRectangleClick(MouseEvent event) throws IOException {
    Rectangle clickedRectangle = (Rectangle) event.getSource();
    context.handleRectangleClick(event, clickedRectangle.getId());
  }

  /**
   * Handles the guess button click event.
   *
   * @param event the action event triggered by clicking the guess button
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void handleGuessClick(ActionEvent event) throws IOException {
    // Need to pass current timer values into Guess timer
    setGuessTime((timeToCount - timeForGuessing));
    // context.handleGuessClick();
    App.setRoot("guess");
  }

  /**
   * Generates the system prompt based on the profession.
   *
   * @return the system prompt string
   */
  private String getSystemPrompt() {
    Map<String, String> map = new HashMap<>();
    map.put("profession", person.getProfession());
    map.put("name", person.getName());
    map.put("role", person.getRole());
    if (person.hasTalked()) {
      return PromptEngineering.getPrompt("chat3.txt", map, person);
    }
    return PromptEngineering.getPrompt("chat2.txt", map, person);
  }

  /**
   * Sets the profession for the chat context and initializes the ChatCompletionRequest.
   *
   * @param profession the profession to set
   */
  public void setPerson(Person person) {
    if (this.person == person) {
      return;
    }

    txtaChat.clear();
    this.person = person;

    Platform.runLater(
        () -> {
          ProgressIndicator statsIndicator = new ProgressIndicator();
          statsIndicator.setMinSize(1, 1);
          statsPane.getChildren().add(statsIndicator);

          context
              .getRoomController()
              .setChatStats("Talking to " + context.getRoomController().getPerson().getName());
        });
    try {
      ApiProxyConfig config = ApiProxyConfig.readConfig();
      chatCompletionRequest =
          new ChatCompletionRequest(config)
              .setN(1)
              .setTemperature(0.2)
              .setTopP(0.5)
              .setMaxTokens(100);
      runGpt(new ChatMessage("system", getSystemPrompt()));
      person.talked();
    } catch (ApiProxyException e) {
      e.printStackTrace();
    }
  }

  /**
   * Appends a chat message to the chat text area.
   *
   * @param msg the chat message to append
   */
  private void appendChatMessage(ChatMessage msg) {
    txtaChat.appendText(msg.getRole() + ": " + msg.getContent() + "\n\n");
  }

  /**
   * Runs the GPT model with a given chat message.
   *
   * @param msg the chat message to process
   * @return the response chat message
   * @throws ApiProxyException if there is an error communicating with the API proxy
   */
  private ChatMessage runGpt(ChatMessage msg) throws ApiProxyException {
    chatCompletionRequest.addMessage(msg);
    try {
      ChatCompletionResult chatCompletionResult = chatCompletionRequest.execute();
      Choice result = chatCompletionResult.getChoices().iterator().next();
      chatCompletionRequest.addMessage(result.getChatMessage());
      appendChatMessage(result.getChatMessage());
      Platform.runLater(
          () -> {
            context.getRoomController().enableTalking();
            context.getRoomController().getStatsPane().getChildren().clear();
          });
      TextToSpeech.speak(result.getChatMessage().getContent(), context);
      return result.getChatMessage();
    } catch (ApiProxyException e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Sends a message to the GPT model.
   *
   * @param event the action event triggered by the send button
   * @throws ApiProxyException if there is an error communicating with the API proxy
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void onSendMessage(ActionEvent event) throws ApiProxyException, IOException {
    String message = txtInput.getText().trim();
    if (message.isEmpty()) {
      return;
    }

    if (context.getGameState() == context.getGameOverState()) {
      Alert alert = new Alert(AlertType.INFORMATION);
      alert.setTitle("Game Over");
      alert.setHeaderText("Game Over");
      alert.setContentText("You can not talk to the suspects anymore.");
      alert.showAndWait();
      txtInput.clear();
      return;
    }

    txtInput.clear();
    ChatMessage msg = new ChatMessage("user", message);
    appendChatMessage(msg);

    ProgressIndicator statsIndicator = new ProgressIndicator();
    statsIndicator.setMinSize(1, 1);
    statsPane.getChildren().add(statsIndicator);

    noTalking();
    Task<Void> task =
        new Task<Void>() {

          @Override
          protected Void call() throws Exception {
            runGpt(msg);
            return null;
          }
        };

    // task.setOnSucceeded(
    //     event1 -> {
    //       statsPane.getChildren().remove(statsIndicator);
    //       // setChatStats("Talking to " + person.getName() + " who is in " + person.getColor());
    //       enableTalking();
    //     });
    Thread backgroundThread = new Thread(task);
    backgroundThread.start();
  }

  @FXML
  public void onBackPressed() {
    enableRectangles();
    carImage.setVisible(false);
    btnBack.setVisible(false);
    btnBack.setDisable(true);
  }

  @FXML
  public void styleScene() {

    ownerImage.setOnMouseEntered(
        e -> {
          ownerImageManager.hoverIn();
        });
    ownerImage.setOnMouseExited(
        e -> {
          ownerImageManager.hoverOut();
        });

    workerImage.setOnMouseEntered(
        e -> {
          workerImageManager.hoverIn();
        });
    workerImage.setOnMouseExited(
        e -> {
          workerImageManager.hoverOut();
        });

    brotherImage.setOnMouseEntered(
        e -> {
          brotherImageManager.hoverIn();
        });
    brotherImage.setOnMouseExited(
        e -> {
          brotherImageManager.hoverOut();
        });

    crimeImage.setOnMouseEntered(
        e -> {
          crimeImageManager.hoverIn();
        });
    crimeImage.setOnMouseExited(
        e -> {
          crimeImageManager.hoverOut();
        });

    btnSlide.setOnAction(event -> toggleHBox());
  }

  @FXML
  public void enableImages() {
    ownerImage.setDisable(false);
    workerImage.setDisable(false);
    brotherImage.setDisable(false);
    crimeImage.setDisable(false);
  }

  @FXML
  public void handleImageClick(MouseEvent event) throws IOException, InterruptedException {
    ImageView clickedImage = (ImageView) event.getSource();
    String id = clickedImage.getId();

    ColorAdjust colorAdjustOut = new ColorAdjust();
    colorAdjustOut.setBrightness(0);
    DropShadow dropShadowOut = new DropShadow();
    dropShadowOut.setRadius(10);
    dropShadowOut.setOffsetX(0);
    dropShadowOut.setOffsetY(0);
    dropShadowOut.setColor(javafx.scene.paint.Color.GRAY);
    dropShadowOut.setInput(colorAdjustOut);
    TranslateTransition transition = new TranslateTransition(Duration.seconds(0.5), imagesHBox);
    switch (id) {
      case "ownerImage":
        if (currentImage != null && currentImage.getId().equals("ownerImage")) {
          return;
        }
        displayImage.setImage(new Image(ownerImage.getImage().getUrl()));
        currentImage = ownerImage;
        currentImageManager.setImageView(currentImage);
        transition.setToY(-imagesHBox.getHeight()); // Move off-screen
        transition.setOnFinished(e -> imagesHBox.setVisible(false)); // Hide after animation
        transition.play();
        context.handleRectangleClick(event, "rectPerson2");
        break;
      case "workerImage":
        if (currentImage != null && currentImage.getId().equals("workerImage")) {
          return;
        }
        displayImage.setImage(new Image(workerImage.getImage().getUrl()));
        currentImage = workerImage;
        currentImageManager.setImageView(currentImage);
        transition.setToY(-imagesHBox.getHeight()); // Move off-screen
        transition.setOnFinished(e -> imagesHBox.setVisible(false)); // Hide after animation
        transition.play();
        context.handleRectangleClick(event, "rectPerson1");
        break;
      case "crimeImage":
        carImage.setVisible(true);
        btnBack.setVisible(true);
        break;
      case "brotherImage":
        if (currentImage != null && currentImage.getId().equals("brotherImage")) {
          return;
        }
        displayImage.setImage(new Image(brotherImage.getImage().getUrl()));
        currentImage = brotherImage;
        currentImageManager.setImageView(currentImage);
        transition.setToY(-imagesHBox.getHeight()); // Move off-screen
        transition.setOnFinished(e -> imagesHBox.setVisible(false)); // Hide after animation
        transition.play();
        context.handleRectangleClick(event, "rectPerson3");
    }
  }

  private void toggleHBox() {
    // Create the transition
    TranslateTransition transition = new TranslateTransition(Duration.seconds(0.5), imagesHBox);

    if (imagesHBox.isVisible()) {
      // Slide out
      transition.setToY(-imagesHBox.getHeight()); // Move off-screen
      transition.setOnFinished(event -> imagesHBox.setVisible(false)); // Hide after animation
    } else {
      // Slide in
      imagesHBox.setVisible(true); // Show before animation
      transition.setFromY(-imagesHBox.getHeight()); // Start off-screen
      transition.setToY(0); // Move to visible position
    }

    // Play the transition
    transition.play();
  }

  public static void setGuessTime(double time) {
    GuessController.setTimeToGuess(time);
  }
}
