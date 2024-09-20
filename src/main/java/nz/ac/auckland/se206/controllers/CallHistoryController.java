package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.net.URISyntaxException;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameStateContext;
import nz.ac.auckland.se206.SceneManager;
import nz.ac.auckland.se206.Utils;
import nz.ac.auckland.se206.ringIndicator.RingProgressIndicator;

public class CallHistoryController {
  private static boolean isFirstTimeInit = true;
  private static double timeToCount = 300000;
  private static double timeToCountTo = 300000;
  private static int progress = 0;
  private static RingProgressIndicator ringProgressIndicator = new RingProgressIndicator();
  private static Timeline timeline = new Timeline();
  private static GameStateContext context = new GameStateContext();

  /**
   * This method sets the time to count
   *
   * @param timeFromPreviousScene
   */
  public static void setTimeToCount(double timeFromPreviousScene) {
    timeToCount = timeFromPreviousScene;
  }

  /**
   * This method passes the time to the phone scene
   *
   * @param timeToCount
   */
  public static void passTimeToPhoneScene(double timeToCount) {
    PhoneController.setTimeToCount(timeToCount);
  }

  private MediaPlayer player;

  @FXML private Button homeButton;
  @FXML private StackPane indicatorPane;
  @FXML private Label timerLabel;
  @FXML private Rectangle voiceMailRectangle;

  /**
   * This method is called when the home button is clicked. It will take the user back to the phone
   *
   * @param event
   */
  @FXML
  private void onHomeButtonClicked(ActionEvent event) {
    Button button = (Button) event.getSource();
    Scene sceneOfButton = button.getScene();
    sceneOfButton.setRoot(SceneManager.getRoot(SceneManager.Scene.PHONE));
    passTimeToPhoneScene(timeToCount);
  }

  /**
   * This method is called when the phone app is clicked. It will take the user to the call history
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

    timeline
        .getKeyFrames()
        .add(
            new KeyFrame(
                Duration.millis(1),
                event -> {
                  if (timeToCount > 0) {
                    timeToCount--;
                    progress = (int) (100 - ((timeToCountTo - timeToCount) * 100 / timeToCountTo));
                  } else if ((timeToCount > 0)) {
                    // Program switch to guess scene here ONLY if clues and suspects have been
                    // correctly interacted with
                    // Before switching state, make sure the game is still in the game started state
                    // and that we havent already switched state. Otherwise it will cause a bug
                    if (!(context.getGameState().equals(context.getGameStartedState()))) {
                      System.out.println("hello b " + context.getGameState());
                      timeline.stop();
                      return;
                    }
                    if (context.isAllSuspectsSpokenTo()
                        && CrimeSceneController.isAnyClueFound()
                        && context.getGameState().equals(context.getGameStartedState())) {
                      context.setState(context.getGuessingState());
                      try {
                        timeline.stop();
                        App.setRoot("guess");
                        return;
                      } catch (IOException e) {
                        e.printStackTrace();
                      }
                      // Stop the timer here, as once the suer switch to guessing state, they aren't
                      // coming back
                      timeline.stop();
                    } else if (!context.isAllSuspectsSpokenTo()
                        && CrimeSceneController.isAnyClueFound()
                        && context.getGameState().equals(context.getGameStartedState())) {
                      context.setState(context.getGameOverState());
                      GameOverController.setOutputText(
                          "You did not speak to every suspect during your investigation!\nWithout"
                              + " doing this, the investigation is incomplete!\n"
                              + "Click play again to replay.");
                      try {
                        timeline.stop();
                        App.setRoot("gamelost");
                        return;
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
                        return;
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
                        return;
                      } catch (IOException e) {
                        e.printStackTrace();
                      }
                    }
                    timeline.stop();
                  }

                  ringProgressIndicator.setProgress(progress);
                  timerLabel.setText(Utils.formatTime(timeToCount));
                }));
    timeline.setCycleCount(Timeline.INDEFINITE);
    timeline.play();
  }

  /**
   * This method is called when the voicemail is clicked. It will play the voicemail sound
   *
   * @param event
   * @throws URISyntaxException
   */
  @FXML
  void onVoicemailClicked(MouseEvent event) throws URISyntaxException {
    Media sound = new Media(App.class.getResource("/sounds/voicemail.mp3").toURI().toString());
    player = new MediaPlayer(sound);
    player.play();
  }

  public void setContext(GameStateContext context) {
    this.context = context;
  }
}
