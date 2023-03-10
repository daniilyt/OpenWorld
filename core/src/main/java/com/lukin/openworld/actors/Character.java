package com.lukin.openworld.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.lukin.openworld.LKGame;
import com.lukin.openworld.utils.EntityLoader;

import java.util.HashMap;
import java.util.Map;

public class Character extends Entity {
    private Touchpad touchpad;
    private OrthographicCamera camera;
    private TiledMapTileLayer layer;
    private HashMap<Rectangle, TiledMapTileLayer.Cell> tilesHitbox;
    private boolean centerCamera;
    private boolean direction;
    private Texture hitboxTexture;
    private Texture hitboxTexture2;


    public Character(Touchpad touchpad, TiledMap map, OrthographicCamera camera, EntityLoader.EntityJson entityJson) {
        Pixmap pixmap = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
        hitbox.set(0, 0, 16, 14);
        if (LKGame.DEBUG) {
            tilesHitbox = new HashMap<>(25);
            pixmap.setColor(Color.RED);
            pixmap.drawRectangle(0, 0, 16, 16);
            hitboxTexture = new Texture(pixmap);
            pixmap.setColor(Color.GREEN);
            pixmap.drawRectangle(0, 0, 16, 16);
            hitboxTexture2 = new Texture(pixmap);
        }
        pixmap.dispose();
        this.animation = loadAnimation(entityJson.animation, map.getTileSets(), 0.25f);
        this.touchpad = touchpad;
        this.camera = camera;
        this.centerCamera = true;
        this.layer = (TiledMapTileLayer) map.getLayers().get("layer1");
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (LKGame.DEBUG) {
            for (Map.Entry<Rectangle, TiledMapTileLayer.Cell> entry : tilesHitbox.entrySet()) {
                if (entry.getValue() == null) {
                    batch.draw(hitboxTexture, entry.getKey().x, entry.getKey().y, entry.getKey().width, entry.getKey().height);
                } else {
                    batch.draw(hitboxTexture2, entry.getKey().x, entry.getKey().y, entry.getKey().width, entry.getKey().height);
                }
            }
        }
        Texture texture = animation.getKeyFrame(animationTime, true);
        batch.draw(texture, getX(), getY(), getOriginX(), getOriginY(), 16, 32, getScaleX(), getScaleY(), getRotation(), 0, 0,
                texture.getWidth(), texture.getHeight(), direction, false);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        float x = SPEED * delta * touchpad.getKnobPercentX();
        float y = SPEED * delta * touchpad.getKnobPercentY();
        hitbox.setPosition(getX() + x, getY() + y);
        boolean skipMove = checkPosition(x, y);
        if (x != 0 || y != 0) {
            if(!skipMove){
                moveBy(x, y);
                touchpad.moveBy(x, y);
                camera.translate(x, y);
                direction = x < 0;
            }
            animationTime += delta;
        }else{
            animationTime = 0f;
        }
    }

    @Override
    protected void positionChanged() {
        if (centerCamera) {
            camera.position.x = getX() + 16;
            camera.position.y = getY() + 16;
            camera.update();
        }
    }

    public boolean checkPosition(float addX, float addY) {
        if (LKGame.DEBUG) {
            tilesHitbox.clear();
        }
        Vector2 pos = localToStageCoordinates(new Vector2(addX, addY));
        pos.set(pos.x / 16, pos.y / 16);
        for (int i = -2; i < 3; i++) {
            for (int j = -2; j < 3; j++) {
                TiledMapTileLayer.Cell cell = layer.getCell((int) pos.x + i, (int) (pos.y + j));
                Rectangle rect = new Rectangle((int) (pos.x + i) * 16, (int) (pos.y + j) * 16, 16, 16);
                if (LKGame.DEBUG) {
                    tilesHitbox.put(rect, cell);
                }
                if (cell == null) {
                    if (hitbox.overlaps(rect)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
