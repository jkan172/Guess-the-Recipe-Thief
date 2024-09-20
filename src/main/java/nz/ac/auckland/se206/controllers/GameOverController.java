package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.Utils;

public class GameOverController {

  private static String explanation;
  private static boolean isTextAlreadyDisplayed = false;
  private static boolean isBannerAlreadyDisplayed = false;
  private static String spare = "";

  /** This method sets the output text to the explanation of the guess. */
  public static void setOutputText(String text) {
    explanation = text;
  }

  @FXML private Pane statsPane;
  @FXML private Label lblStats;
  @FXML private TextArea textChat;
  @FXML private Label lblExplanation;
  @FXML private TextArea oldTextArea;

  /**
   * This method is called when the game over scene is loaded. It will set the text of the text area
   */
  @FXML
  public void initialize() {
    System.out.println(oldTextArea);
    System.out.println(isTextAlreadyDisplayed);

    // Set the text of the text area to the explanation of the game
    if ((oldTextArea != null) && (!isTextAlreadyDisplayed)) {
      spare = explanation;
      System.out.println(spare);

      oldTextArea.setWrapText(true);
      oldTextArea.setText(spare);
      oldTextArea.setDisable(true);
      isTextAlreadyDisplayed = true;
    }

    // Set the text of the label to the result of the game

    if (!isBannerAlreadyDisplayed) { // Prevents bug from changing gamestate to loss after timers
      // run out
      if (GuessController.getIsGameWon()) {
        lblStats.setText("Correct! You win!!");
        lblStats.setDisable(true);
        isBannerAlreadyDisplayed = true;
        Utils.updateScoreBoard(Utils.getTimeUsed(), Utils.getPlayerName());
      } else {
        lblStats.setText("Oh no! You Lose!");
        isBannerAlreadyDisplayed = true;
      }
    }
  }

  /**
   * This method is called when the restart button is clicked. It will take the user back to the
   * start scene.
   *
   * @param event
   * @throws ApiProxyException
   * @throws IOException
   */
  @FXML
  private void onHandleRestartClick(ActionEvent event) throws ApiProxyException, IOException {
    App.setRoot("start");
  }

  /**
   * This method is a placeholder for the key pressed event.
   *
   * @param event
   */
  @FXML
  public void onKeyPressed(ActionEvent event) {}

  /**
   * This method is a placeholder for the key released event.
   *
   * @param event
   */
  @FXML
  public void onKeyReleased(ActionEvent event) {}
}
