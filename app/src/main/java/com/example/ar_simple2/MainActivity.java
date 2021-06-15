package com.example.ar_simple2;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.Pose;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements Scene.OnUpdateListener {
    private ArFragment arFragment;
    private float[][] position = new float[2][3];
    int touchth = 0;
    private TextView textView;
    private List<AnchorNode> anchors = new ArrayList<AnchorNode>();
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.arFragment);
        textView = (TextView) findViewById(R.id.textView);
        button = (Button) findViewById(R.id.removebutton);

        arFragment.setOnTapArPlaneListener(((hitResult, plane, motionEvent) -> {
            if(touchth == 2){
                return;
            }

            Anchor anchor = hitResult.createAnchor();
            ModelRenderable.builder().setSource(this, Uri.parse("ArcticFox_Posed.sfb")).build()
                    .thenAccept(modelRenderable -> {addModelToScene(anchor,modelRenderable);
                        if(touchth == 2) {
                            float distance = (float) Math.sqrt((position[0][0] - position[1][0])
                                    * (position[0][0] - position[1][0]) + (position[0][1] - position[1][1]) *
                                    (position[0][1] - position[1][1]) + (position[0][2] - position[1][2]) *
                                    (position[0][2] - position[1][2]));
                            textView.setText("distance is " + Float.toString(distance));
                        }
                    })
                    .exceptionally(throwable -> {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setMessage(throwable.getMessage()).show();
                        return null;
                    });
            //anchors.add(anchor);
            anchor.getPose().getTranslation(position[touchth],0);
            arFragment.getArSceneView().getScene().addOnUpdateListener(this);
        }));


    }

    @Override
    public void onUpdate(FrameTime frameTime){
        Frame frame = arFragment.getArSceneView().getArFrame();
        if(touchth == 2){
            anchors.get(0).getAnchor().getPose().getTranslation(position[0],0);
            anchors.get(1).getAnchor().getPose().getTranslation(position[1],0);

            float distance = (float)Math.sqrt( (position[0][0]- position[1][0])
                    * (position[0][0]- position[1][0]) + (position[0][1]- position[1][1])*
                    (position[0][1]- position[1][1])+(position[0][2]- position[1][2])*
                    (position[0][2]- position[1][2]));
            textView.setText("Distance is "+Float.toString(distance));
        }
        if(touchth == 1){
            anchors.get(0).getAnchor().getPose().getTranslation(position[0],0);
            //anchors.get(0).getPose().getTranslation(position[0],0);
            Pose cameraPose = frame.getCamera().getPose();
            float dx = position[0][0] - cameraPose.tx();
            float dy = position[0][1] - cameraPose.ty();
            float dz = position[0][2] - cameraPose.tz();
            float distance = (float)Math.sqrt(dx*dx+dy*dy+dz*dz);

            textView.setText("One obejct is chousen. The distance of Camera is " + Float.toString(distance));
        }
        if(touchth == 0){
            textView.setText("Obejct is not choosen");
        }
    }

    private void addModelToScene(Anchor anchor, ModelRenderable modelRenderable) {
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchors.add(anchorNode);
        touchth++;
        TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem());
        transformableNode.setParent(anchorNode);
        transformableNode.setRenderable(modelRenderable);
        arFragment.getArSceneView().getScene().addChild(anchorNode);

        transformableNode.setOnTapListener(((hitTestResult, motionEvent) -> {
            Node node = hitTestResult.getNode();
            button.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                          public void onClick(View v) {
                                              anchorNode.removeChild(transformableNode);
                                              arFragment.getArSceneView().getScene().onRemoveChild(anchorNode);
                                              anchors.remove(anchor);
                                              touchth--;
                                          }
                                      }

            );}));
        transformableNode.select();

    }
}
