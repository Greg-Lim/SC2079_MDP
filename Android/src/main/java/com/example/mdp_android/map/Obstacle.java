package com.example.mdp_android.map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import android.os.Parcel;
import android.os.Parcelable;
public class Obstacle {
    final int id;
    float startX, startY, endX, endY;
    String text;

    Obstacle(int id, float startX, float startY, float endX, float endY, String text) {
        this.id = id;
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.text = text;
    }

    // Parcelable implementation
    protected Obstacle(Parcel in) {
        id = in.readInt();
        startX = in.readFloat();
        startY = in.readFloat();
        endX = in.readFloat();
        endY = in.readFloat();
        text = in.readString();
    }
    /*
    public static final Creator<Obstacle> CREATOR = new Creator<Obstacle>() {
        @Override
        public Obstacle createFromParcel(Parcel in) {
            return new Obstacle(in);
        }

        @Override
        public Obstacle[] newArray(int size) {
            return new Obstacle[size];
        }
    };*/
}