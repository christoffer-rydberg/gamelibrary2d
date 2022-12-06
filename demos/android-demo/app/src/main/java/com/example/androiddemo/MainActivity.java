package com.example.androiddemo;

import android.app.Activity;
import com.example.framework.AbstractGameActivity;
import com.example.framework.Android_Framework;
import com.example.framework.DeviceUtil;
import com.example.sound.android.DefaultSoundManager;
import com.gamelibrary2d.demos.networkgame.client.DemoGame;
import com.gamelibrary2d.demos.networkgame.client.ServerManager;
import com.gamelibrary2d.demos.networkgame.client.input.VirtualController;
import com.gamelibrary2d.disposal.Disposer;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AbstractGameActivity {

    public MainActivity() {
        super(MainActivity::createGame);
    }

    private static KeyPair createKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        return keyGen.generateKeyPair();
    }

    private static DemoGame createGame(Activity activity, Disposer disposer) {
        try {
            return new DemoGame(
                    new Android_Framework(),
                    VirtualController::new,
                    new AndroidResourceManager(activity.getAssets()),
                    new ServerManager(createKeyPair()),
                    DefaultSoundManager.create(disposer)
            );
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void initialize() {
        DeviceUtil.hideSystemUI(this);
        DeviceUtil.lockOrientation(this, DeviceUtil.DeviceOrientation.LANDSCAPE);
        super.initialize();
    }
}
