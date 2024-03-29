package com.gamelibrary2d.demos.collisiondetection;

import com.gamelibrary2d.Game;
import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.collision.CollisionDetection;
import com.gamelibrary2d.collision.handlers.BounceHandler;
import com.gamelibrary2d.collision.handlers.CollisionHandler;
import com.gamelibrary2d.collision.handlers.RestrictedAreaHandler;
import com.gamelibrary2d.collision.handlers.UpdatedHandler;
import com.gamelibrary2d.components.containers.DefaultLayer;
import com.gamelibrary2d.components.containers.Layer;
import com.gamelibrary2d.components.frames.AbstractFrame;
import com.gamelibrary2d.opengl.renderers.ContentRenderer;
import com.gamelibrary2d.opengl.renderers.SurfaceRenderer;
import com.gamelibrary2d.opengl.resources.DefaultTexture;
import com.gamelibrary2d.opengl.resources.Quad;
import com.gamelibrary2d.opengl.resources.Surface;
import com.gamelibrary2d.opengl.resources.Texture;

import java.io.IOException;

public class DemoFrame extends AbstractFrame {
    private final CollisionDetection collisionDetection;
    private final Rectangle gameArea;
    private final UpdatedHandler<Ball> restrictedAreaHandler;
    private final CollisionHandler<Ball, Ball> bounceHandler = new BounceHandler<>(Ball.class);
    private final Layer<Ball> ballLayer = new DefaultLayer<>();
    private ContentRenderer ballRenderer;

    DemoFrame(Game game) {
        super(game);
        gameArea = new Rectangle(0, 0, game.getWindow().getWidth(), game.getWindow().getHeight());
        restrictedAreaHandler = new RestrictedAreaHandler<>(gameArea, Ball::accelerate);
        collisionDetection = new CollisionDetection(gameArea, 128, 10);
    }

    @Override
    protected void onBegin() {
        try {
            Texture ballTexture = DefaultTexture.create(Ball.class.getResource("/ball.png"), this);
            Surface ballSurface = Quad.create(Rectangle.create(32, 32), this);
            ballRenderer = new SurfaceRenderer<>(ballSurface, ballTexture);
            ballRenderer.setColor(152f / 255f, 251f / 255f, 152f / 255f);
            BallTool tool = BallTool.create(this, ballRenderer, this::addBall);

            final float ballWidth = ballSurface.getBounds().getWidth();
            final float ballHeight = ballSurface.getBounds().getHeight();
            final float pyramidCenterX = gameArea.getWidth() / 2f;
            final float pyramidBaseY = 2f * gameArea.getHeight() / 3f;

            // 1st row
            addBall(pyramidCenterX - 2 * ballWidth, pyramidBaseY);
            addBall(pyramidCenterX - ballWidth, pyramidBaseY);
            addBall(pyramidCenterX, pyramidBaseY);
            addBall(pyramidCenterX + ballWidth, pyramidBaseY);
            addBall(pyramidCenterX + 2 * ballWidth, pyramidBaseY);

            // 2nd row
            addBall(pyramidCenterX - ballWidth * 1.5f, pyramidBaseY - ballHeight * 0.9f);
            addBall(pyramidCenterX - ballWidth * 0.5f, pyramidBaseY - ballHeight * 0.9f);
            addBall(pyramidCenterX + ballWidth * 0.5f, pyramidBaseY - ballHeight * 0.9f);
            addBall(pyramidCenterX + ballWidth * 1.5f, pyramidBaseY - ballHeight * 0.9f);

            // 3nd row
            addBall(pyramidCenterX - ballWidth, pyramidBaseY - ballHeight * 0.9f * 2);
            addBall(pyramidCenterX, pyramidBaseY - ballHeight * 0.9f * 2);
            addBall(pyramidCenterX + ballWidth, pyramidBaseY - ballHeight * 0.9f * 2);

            // 4th row
            addBall(pyramidCenterX - ballWidth * 0.5f, pyramidBaseY - ballHeight * 0.9f * 3);
            addBall(pyramidCenterX + ballWidth * 0.5f, pyramidBaseY - ballHeight * 0.9f * 3);

            // 5th row
            addBall(pyramidCenterX, pyramidBaseY - ballHeight * 0.9f * 4);

            add(ballLayer);
            add(tool);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void onUpdate(float deltaTime) {
        collisionDetection.update(deltaTime);
        super.onUpdate(deltaTime);
    }

    private void addBall(Ball ball) {
        ballLayer.add(ball);
        collisionDetection.add(ball, restrictedAreaHandler, bounceHandler);
    }

    private void addBall(float x, float y) {
        addBall(new Ball(ballRenderer, x, y));
    }

    @Override
    protected void onEnd() {

    }

    @Override
    protected void onDispose() {

    }
}
