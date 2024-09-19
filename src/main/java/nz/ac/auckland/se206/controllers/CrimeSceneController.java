package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.net.URISyntaxException;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.ClueManager;
import nz.ac.auckland.se206.GameStateContext;
import nz.ac.auckland.se206.ImageManager;
import nz.ac.auckland.se206.SceneManager;
import nz.ac.auckland.se206.Utils;
import nz.ac.auckland.se206.ringIndicator.RingProgressIndicator;

public class CrimeSceneController {
  private static boolean isFirstTimeInit = true;

  private static GameStateContext context = new GameStateContext();
  private static double timeToCount = 300000;
  private static double timeToCountTo = 300000;
  private static int progress = 0;
  private static RingProgressIndicator ringProgressIndicator = new RingProgressIndicator();
  private MediaPlayer player;
  private static Timeline timeline = new Timeline();

  @FXML private Button btnGuess;
  @FXML private Button btnSlide;
  @FXML private StackPane indicatorPane;
  @FXML private Label timerLabel;
  @FXML private Rectangle suspect2Scene;
  @FXML private Rectangle suspect1Scene;
  @FXML private Rectangle suspect3Scene;

  @FXML private VBox imagesVBox;

  @FXML private ImageView ownerImage;
  @FXML private ImageView workerImage;
  @FXML private ImageView brotherImage;
  @FXML private ImageView crimeImage;
  @FXML private ImageView cameraImage;
  @FXML private ImageView phoneImage;
  @FXML private ImageView newspaperImage;

  @FXML private Label crimeLabel;
  @FXML private Label workerLabel;
  @FXML private Label ownerLabel;
  @FXML private Label brotherLabel;

  public ImageManager currentImageManager;
  public ImageManager ownerImageManager;
  public ImageManager workerImageManager;
  public ImageManager brotherImageManager;
  public ImageManager crimeImageManager;
  public ClueManager cameraImageManager;
  public ClueManager phoneImageManager;
  public ClueManager newspaperImageManager;

  public String id;

  /**
   * This method is called when the crime scene is loaded. It will set the timer and the progress
   * bar
   */
  @FXML
  public void initialize() {
    if (isFirstTimeInit) {}
    // context.setCrimeController(this); *******NEED THIS
    indicatorPane.getChildren().add(ringProgressIndicator);
    ringProgressIndicator.setRingWidth(50);
    // Timer label is updated here
    if (timeToCount % 1000 == 0) {
      timerLabel.setText(Utils.formatTime(timeToCount));
    }

    btnGuess
        .sceneProperty()
        .addListener(
            (observable, oldScene, newScene) -> {
              if (newScene != null) {
                Stage stage = (Stage) newScene.getWindow();
                stage.sizeToScene();
              }
            });

    ownerImageManager = new ImageManager(ownerImage);
    workerImageManager = new ImageManager(workerImage);
    brotherImageManager = new ImageManager(brotherImage);
    crimeImageManager = new ImageManager(crimeImage);
    cameraImageManager = new ClueManager(cameraImage);
    phoneImageManager = new ClueManager(phoneImage);
    newspaperImageManager = new ClueManager(newspaperImage);

    ColorAdjust colorAdjust = new ColorAdjust();
    colorAdjust.setBrightness(-0.45);
    ownerImage.setEffect(colorAdjust);
    workerImage.setEffect(colorAdjust);
    brotherImage.setEffect(colorAdjust);
    crimeImage.setEffect(colorAdjust);
    styleScene();

    timeline
        .getKeyFrames()
        .add(
            new KeyFrame(
                Duration.millis(1),
                event -> {
                  if (timeToCount > 0) {
                    timeToCount--;
                    progress = (int) (100 - ((timeToCountTo - timeToCount) * 100 / timeToCountTo));
                  } else {
                    System.out.println(timeToCount);
                    // Here the timer has exceeded the time for investigation and the game must
                    // switch to the guess scene.

                    // Before switching state, make sure the game is still in the game started state
                    // and that we havent already switched state. Otherwise it will cause a bug
                    if (!(context.getGameState().equals(context.getGameStartedState()))) {
                      System.out.println("hello e " + context.getGameState());
                      timeline.stop();
                      System.out.println(timeToCount);
                      return;
                    }
                    if (context.isAllSuspectsSpokenTo()
                        && CrimeSceneController.isAnyClueFound()
                        && context.getGameState().equals(context.getGameStartedState())) {
                      context.setState(context.getGuessingState());
                      try {
                        timeline.stop();
                        App.setRoot("guess");

                      } catch (IOException e) {
                        e.printStackTrace();
                      }
                      // Stop the timer here, as once the suer switch to guessing state, they aren't
                      // coming back
                      timeline.stop();
                    } else if (!context.isAllSuspectsSpokenTo()
                        && CrimeSceneController.isAnyClueFound()
                        && context.getGameState().equals(context.getGameStartedState())) {
                      System.out.println("Should be gameover: " + context.getGameState());
                      context.setState(context.getGameOverState());
                      GameOverController.setOutputText(
                          "You did not speak to every suspect during your"
                              + " investigation!\n"
                              + "Without doing this, the investigation is incomplete!\n"
                              + "Click play again to replay.");
                      try {
                        timeline.stop();
                        App.setRoot("gamelost");
                      } catch (IOException e) {
                        e.printStackTrace();
                      }
                    } else if (context.isAllSuspectsSpokenTo()
                        && !CrimeSceneController.isAnyClueFound()
                        && context.getGameState().equals(context.getGameStartedState())) {
                      context.setState(context.getGameOverState());
                      GameOverController.setOutputText(
                          "You did not find any clues in the crime scene!\n"
                              + "Finding clues is vital to conduting a good investigation!\n"
                              + "Click play again to replay");
                      try {
                        timeline.stop();
                        App.setRoot("gamelost");
                      } catch (IOException e) {
                        e.printStackTrace();
                      }
                    } else if (!context.isAllSuspectsSpokenTo()
                        && !CrimeSceneController.isAnyClueFound()
                        && context.getGameState().equals(context.getGameStartedState())) {
                      context.setState(context.getGameOverState());
                      GameOverController.setOutputText(
                          "You did not inspect the crime scene for clues or speak to every"
                              + " suspect!\n"
                              + "These steps are vital in any investigation.\n"
                              + "Click play again to replay.");
                      try {
                        timeline.stop();
                        App.setRoot("gamelost");
                      } catch (IOException e) {
                        e.printStackTrace();
                      }
                    }
                    // Once in guess state, player will never return to crime scene
                    timeline.stop();
                  }
                  ringProgressIndicator.setProgress(progress);
                  timerLabel.setText(Utils.formatTime(timeToCount));
                }));
    timeline.setCycleCount(Timeline.INDEFINITE);
    timeline.play();
    // play an instruction sound when entering the room for the first time
    // Media media = new Media(getClass().getResource("/sounds/enter_room.mp3").toExternalForm());
    // MediaPlayer mediaPlayer = new MediaPlayer(media);
    // mediaPlayer.play();
    // isFirstTimeInit = false;
    // }}
  }

  /**
   * This method is called when the CCTV clue is clicked. It will take the user to the CCTV scene
   *
   * @param event
   */
  @FXML
  void onCCTVClueClicked(MouseEvent event) {
    context.clue1Found();
    // Satisfies requirement of at least one clue being discovered
    context.clueFound();
    Scene sceneOfButton = phoneImage.getScene();
    CCTVController cctvController = SceneManager.getCCTVLoader().getController();
    cctvController.setContext(context);
    sceneOfButton.setRoot(SceneManager.getRoot(SceneManager.Scene.CCTV));
    CCTVController.setTimeToCount(timeToCount);
  }

  /**
   * This method is called when the phone clue is clicked. It will take the user to the phone scene
   *
   * @param event
   */
  @FXML
  void onPhoneClueClicked(MouseEvent event) {
    context.clue2Found();
    // Satisfies requirement of at least one clue being discovered
    context.clueFound();

    Scene sceneOfButton = phoneImage.getScene();

    PhoneController phoneController = SceneManager.getPhoneLoader().getController();
    phoneController.setContext(context);

    sceneOfButton.setRoot(SceneManager.getRoot(SceneManager.Scene.PHONE));
    PhoneController.setTimeToCount(timeToCount);
  }

  /**
   * This method is called when the newspaper clue is clicked. It will take the user to the
   * newspaper scene
   *
   * @param event
   */
  @FXML
  void onNewspaperClueClicked(MouseEvent event) {
    context.clue3Found();
    // Satisfies requirement of at least one clue being discovered
    context.clueFound();
    Scene sceneOfButton = btnGuess.getScene();
    NewspaperController newspaperController = SceneManager.getNewspaperLoader().getController();
    newspaperController.setContext(context);
    sceneOfButton.setRoot(SceneManager.getRoot(SceneManager.Scene.NEWSPAPER));
    NewspaperController.setTimeToCount(timeToCount);
  }

  /**
   * This method is called when the guess button is clicked. It will take the user to the guess
   * scene
   *
   * @param event
   * @throws IOException
   * @throws URISyntaxException
   */
  @FXML
  void onGuessClick(ActionEvent event) throws IOException, URISyntaxException {
    // Check all 3 suspects have been spoken to and at least 1 clue has been clicked
    if (context.isAllSuspectsSpokenTo() && isAnyClueFound()) {
      // context.handleGuessClick();
      timeline.stop();
      context.setState(context.getGuessingState());
      App.setRoot("guess");
    } else if (!context.isAllSuspectsSpokenTo() && isAnyClueFound()) {
      Media sound =
          new Media(App.class.getResource("/sounds/missing_suspect.mp3").toURI().toString());
      player = new MediaPlayer(sound);
      player.play();
      return;
    } else if (context.isAllSuspectsSpokenTo() && !isAnyClueFound()) {
      Media sound =
          new Media(App.class.getResource("/sounds/clue_reminder_1.mp3").toURI().toString());
      player = new MediaPlayer(sound);
      player.play();
      return;
    } else if (!context.isAllSuspectsSpokenTo() && !isAnyClueFound()) {
      Media sound =
          new Media(App.class.getResource("/sounds/keep_investigating.mp3").toURI().toString());
      player = new MediaPlayer(sound);
      player.play();
      return;
    }
  }

  /**
   * This method is called when the suspect 1 is clicked. It will take the user to the suspect scene
   *
   * @param event
   * @throws IOException
   * @throws ApiProxyException
   */
  @FXML
  void onSuspect1Clicked(MouseEvent event) throws IOException, ApiProxyException {
    Scene sceneOfButton = btnGuess.getScene();
    sceneOfButton.setRoot(SceneManager.getRoot(SceneManager.Scene.ROOM));
    passTimeToSuspectScene(timeToCount);
  }

  /**
   * This method is called when the suspect 2 is clicked. It will take the user to the suspect scene
   *
   * @param event
   * @throws IOException
   * @throws ApiProxyException
   */
  @FXML
  void onSuspect2Clicked(MouseEvent event) throws IOException, ApiProxyException {
    Scene sceneOfButton = btnGuess.getScene();
    sceneOfButton.setRoot(SceneManager.getRoot(SceneManager.Scene.ROOM));
    passTimeToSuspectScene(timeToCount);
  }

  /**
   * This method is called when the suspect 3 is clicked. It will take the user to the suspect scene
   *
   * @param event
   * @throws IOException
   * @throws ApiProxyException
   */
  @FXML
  void onSuspect3Clicked(MouseEvent event) throws IOException, ApiProxyException {
    Scene sceneOfButton = btnGuess.getScene();
    sceneOfButton.setRoot(SceneManager.getRoot(SceneManager.Scene.ROOM));
    passTimeToSuspectScene(timeToCount);
  }

  /** This method styles the scene */
  @FXML
  public void styleScene() {

    ownerImage.setOnMouseEntered(
        e -> {
          ownerImageManager.hoverIn();
          ownerLabel.setVisible(true);
        });
    ownerImage.setOnMouseExited(
        e -> {
          ownerImageManager.hoverOut();
          ownerLabel.setVisible(false);
        });

    workerImage.setOnMouseEntered(
        e -> {
          workerImageManager.hoverIn();
          workerLabel.setVisible(true);
        });
    workerImage.setOnMouseExited(
        e -> {
          workerImageManager.hoverOut();
          workerLabel.setVisible(false);
        });

    brotherImage.setOnMouseEntered(
        e -> {
          brotherImageManager.hoverIn();
          brotherLabel.setVisible(true);
        });
    brotherImage.setOnMouseExited(
        e -> {
          brotherImageManager.hoverOut();
          brotherLabel.setVisible(false);
        });

    crimeImage.setOnMouseEntered(
        e -> {
          crimeImageManager.hoverIn();
          crimeLabel.setVisible(true);
        });
    crimeImage.setOnMouseExited(
        e -> {
          crimeImageManager.hoverOut();
          crimeLabel.setVisible(false);
        });

    cameraImage.setOnMouseEntered(
        e -> {
          cameraImageManager.hoverIn();
        });
    cameraImage.setOnMouseExited(
        e -> {
          cameraImageManager.hoverOut();
        });

    phoneImage.setOnMouseEntered(
        e -> {
          phoneImageManager.hoverIn();
        });
    phoneImage.setOnMouseExited(
        e -> {
          phoneImageManager.hoverOut();
        });

    newspaperImage.setOnMouseEntered(
        e -> {
          newspaperImageManager.hoverIn();
        });
    newspaperImage.setOnMouseExited(
        e -> {
          newspaperImageManager.hoverOut();
        });

    btnSlide.setOnAction(event -> toggleHBox());
  }

  /** This method toggles the HBox */
  private void toggleHBox() {
    // Create the transition
    TranslateTransition transition = new TranslateTransition(Duration.seconds(0.5), imagesVBox);

    if (imagesVBox.isVisible()) {
      // Slide out
      transition.setToX(imagesVBox.getWidth() + 30); // Move off-screen
      transition.setOnFinished(event -> imagesVBox.setVisible(false)); // Hide after animation
    } else {
      // Slide in
      imagesVBox.setVisible(true); // Show before animation
      transition.setFromX(imagesVBox.getWidth() + 30); // Start off-screen
      transition.setToX(0); // Move to visible position
    }

    // Play the transition
    transition.play();
  }

  /**
   * This method is called when an image is clicked. It will set the context and take the user to
   * the image scene
   *
   * @param event
   * @throws IOException
   * @throws InterruptedException
   */
  @FXML
  public void handleImageClick(MouseEvent event) throws IOException, InterruptedException {
    ImageView clickedImage = (ImageView) event.getSource();
    id = clickedImage.getId();

    ImageView imageView = (ImageView) event.getSource();
    Scene sceneOfButton = imageView.getScene();

    RoomController roomController = SceneManager.getRoomLoader().getController();
    roomController.setContext(context);
    context.setRoomController(roomController);

    SceneManager.getRoot(SceneManager.Scene.ROOM)
        .sceneProperty()
        .addListener(
            (obs, oldScene, newScene) -> {
              if (newScene != null) {
                try {
                  roomController.setPersonImage(event, id);
                } catch (IOException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
                } // Call the method only when entering root2
              }
            });

    imagesVBox.setVisible(false);
    sceneOfButton.setRoot(SceneManager.getRoot(SceneManager.Scene.ROOM));
  }

  /**
   * This method is a getter that returns the id
   *
   * @return
   */
  public String getId() {
    return id;
  }

  /**
   * This method is a getter that returns the context
   *
   * @return
   */
  public GameStateContext getContext() {
    return context;
  }

  /**
   * This method is s setter that sets the time to count
   *
   * @param timeFromPreviousScene
   */
  public static void setTimeToCount(double timeFromPreviousScene) {
    timeToCount = timeFromPreviousScene;
  }

  /**
   * This method is a setter that sets the progress
   *
   * @param progressFromPreviousScene
   */
  public static void setProgress(int progressFromPreviousScene) {
    progress = progressFromPreviousScene;
  }

  /**
   * This method is a method hat passes the time to the suspect scene
   *
   * @param timeToCount
   */
  public static void passTimeToSuspectScene(double timeToCount) {
    RoomController.setTimeToCount(timeToCount);
  }

  /**
   * This method returns true if any clue is found
   *
   * @return
   */
  public static boolean isAnyClueFound() {
    return context.isAnyClueFound();
  }
}
