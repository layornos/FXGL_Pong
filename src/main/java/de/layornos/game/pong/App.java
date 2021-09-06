/* (C) Sandro Koch 2021 */
package de.layornos.game.pong;

import static com.almasb.fxgl.dsl.FXGLForKtKt.getAppHeight;
import static com.almasb.fxgl.dsl.FXGLForKtKt.getAppWidth;
import static com.almasb.fxgl.dsl.FXGLForKtKt.getGameScene;
import static com.almasb.fxgl.dsl.FXGLForKtKt.getUIFactoryService;
import static com.almasb.fxgl.dsl.FXGLForKtKt.getWorldProperties;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.physics.CollisionHandler;
import java.util.Map;
import java.util.Random;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class App extends GameApplication {

  private Entity player;
  private Entity opponent;
  private Entity ball;

  private final int GAME_WIDTH = 1280;
  private final int GAME_HEIGHT = 720;
  private final int BALL_SIZE = GAME_HEIGHT/25;
  private final int BALL_SPEED = 5;
  private final int PLAYER_SPEED = 10;
  private final int PADDLE_WIDTH = GAME_WIDTH / 40;
  private final int PADDLE_HEIGHT = GAME_HEIGHT / 5;

  @Override
  protected void initSettings(GameSettings settings) {
    settings.setWidth(GAME_WIDTH);
    settings.setHeight(GAME_HEIGHT);
    settings.setTitle("Pong");
    settings.setVersion("0.0.1");
  }

  @Override
  protected void initGameVars(Map<String, Object> vars) {
    vars.put("score1", 0);
    vars.put("score2", 0);
  }

  @Override
  protected void initGame() {
    player = FXGL.entityBuilder()
                 .type(EntityType.PLAYER)
                 .at(PADDLE_WIDTH, GAME_HEIGHT/2)
                 .viewWithBBox(new Rectangle(PADDLE_WIDTH, PADDLE_HEIGHT))
                 .with(new CollidableComponent(true))
                 .buildAndAttach();
    opponent = FXGL.entityBuilder()
                   .type(EntityType.PLAYER)
                   .at(GAME_WIDTH - PADDLE_WIDTH*2, GAME_HEIGHT/2)
                   .viewWithBBox(new Rectangle(PADDLE_WIDTH, PADDLE_HEIGHT))
                   .with(new CollidableComponent(true))
                   .buildAndAttach();
    ball = FXGL.entityBuilder()
               .type(EntityType.BALL)
               .at(GAME_WIDTH/2 - BALL_SIZE/2, GAME_HEIGHT/2 - BALL_SIZE/2)
               .viewWithBBox(new Rectangle(BALL_SIZE, BALL_SIZE))
               .with(new CollidableComponent(true))
               .with("velocity", new Point2D(BALL_SPEED, BALL_SPEED))
               .buildAndAttach();
  }

  @Override
  protected void initInput() {
    FXGL.onKey(KeyCode.UP, () -> {
      if (player.getBottomY() - player.getHeight() >= 0)
        player.translateY(-PLAYER_SPEED);
    });

    FXGL.onKey(KeyCode.DOWN, () -> {
      if (player.getBottomY() <= GAME_HEIGHT)
        player.translateY(PLAYER_SPEED);
    });
  }

  @Override
  protected void initPhysics() {
    FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(
        EntityType.PLAYER, EntityType.BALL) {
      @Override
      protected void onCollisionBegin(Entity player, Entity ball) {
        Point2D velocity = ball.getObject("velocity");
        ball.setProperty("velocity",
                         new Point2D(-velocity.getX(),
                                     getRandomDirection() * velocity.getY()));
      }
    });
  }

  @Override
  protected void onUpdate(double tpf) {
    Point2D velocity = ball.getObject("velocity");
    ball.translate(velocity);

    if (ball.getBottomY() <= 0) {
      ball.setY(-BALL_SIZE);
      ball.setProperty("velocity",
                       new Point2D(velocity.getX(), -velocity.getY()));
    }

    if (ball.getBottomY() >= getAppHeight()) {
      ball.setY(getAppHeight() - BALL_SIZE);
      ball.setProperty("velocity",
                       new Point2D(velocity.getX(), -velocity.getY()));
    }

    if (ball.getRightX() <= 0) {
      resetBall(velocity);
      getWorldProperties().increment("score2", +1);
    }

    if (ball.getRightX() >= getAppWidth()) {
      resetBall(velocity);
      getWorldProperties().increment("score1", +1);
    }

    if (ball.getY() + BALL_SIZE/2 > (opponent.getY() + PADDLE_HEIGHT/2)) {
      opponent.translateY(PLAYER_SPEED);
    }

    if (ball.getY() + BALL_SIZE/2 < (opponent.getY() + PADDLE_HEIGHT/2)) {
      opponent.translateY(-PLAYER_SPEED);
    }
  }

  private void resetBall(Point2D velocity) {
    ball.setY(getAppHeight() / 2 - BALL_SIZE / 2);
    ball.setX(getAppWidth() / 2 - BALL_SIZE / 2);
    setRandomBallDirection();
    ball.setProperty("velocity", new Point2D(velocity.getX(), velocity.getY()));
  }

  private void setRandomBallDirection() {
    Point2D velocity = ball.getObject("velocity");
    ball.setProperty("velocity",
                     new Point2D(getRandomDirection() * velocity.getX(),
                                 getRandomDirection() * velocity.getY()));
  }

  private int getRandomDirection() {
    Random random = new Random();
    int rand = random.nextInt(1024);
    if (rand % 2 == 0) {
      return 1;
    } else {
      return -1;
    }
  }

  @Override
  protected void initUI() {
    Text textScore1 = getUIFactoryService().newText("", Color.BLACK, 22);
    Text textScore2 = getUIFactoryService().newText("", Color.BLACK, 22);

    textScore1.setTranslateX(10);
    textScore1.setTranslateY(50);

    textScore2.setTranslateX(getAppWidth() - 30);
    textScore2.setTranslateY(50);

    textScore1.textProperty().bind(
        getWorldProperties().intProperty("score1").asString());
    textScore2.textProperty().bind(
        getWorldProperties().intProperty("score2").asString());

    getGameScene().addUINodes(textScore1, textScore2);
  }

  public static void main(String[] args) { launch(args); }
}