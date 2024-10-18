/*********************************************************************
 *
 *  This file is part of the [OPEN_MICRO_MOWER_ANDROID] project.
 *  Licensed under the MIT License for non-commercial purposes.
 *  Author: Brook Li
 *  Email: lguitech@126.com
 *
 *  For more details, refer to the LICENSE file or contact [lguitech@126.com].
 *
 *  Commercial use requires a separate license.
 *
 *  This software is provided "as is", without warranty of any kind.
 *
 *********************************************************************/

package com.micronavi.mower.component;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.micronavi.mower.R;
import com.micronavi.mower.bean.BoundingBox;
import com.micronavi.mower.bean.GeoPoint;
import com.micronavi.mower.bean.ScrObject;
import com.micronavi.mower.bean.ScrPoint;
import java.util.ArrayList;

public class DrawSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = DrawSurfaceView.class.getName();

    private final SurfaceHolder surfaceHolder;

    private int view_width = 0;
    private int view_height = 0;

    private final Paint paintBoundaryBorder;
    private final Paint paintBoundaryFill;
    private final Paint paintObstacleBorder;
    private final Paint paintObstacleFill;
    private final Paint paintChannel;
    private final Paint paintWork;
    private final Paint paintHome;
    private final Paint paintGnss;

    private final MapDrawMng mapDrawMng = new MapDrawMng();
    private boolean isFirst = true;

    private final Bitmap bmpHome;
    private final Rect srcRectHome;

    private static final int HOME_RADIUS = 40;
    private static final int GNSS_RADIUS1 = 10;
    private static final int GNSS_RADIUS2 = 15;
    private boolean flag = true;
    private long lastTimeStamp = 0;

    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;

    private final PointF startPoint = new PointF();
    private float oriDis = 1f;
    private PointF midPoint = new PointF();

    public DrawSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);


        paintBoundaryBorder = new Paint();
        paintBoundaryBorder.setStyle(Paint.Style.STROKE);
        paintBoundaryBorder.setStrokeJoin(Paint.Join.ROUND);
        paintBoundaryBorder.setStrokeCap(Paint.Cap.ROUND);
        paintBoundaryBorder.setAntiAlias(true);
        paintBoundaryBorder.setDither(true);
        paintObstacleBorder = new Paint(paintBoundaryBorder);
        paintGnss = new Paint(paintBoundaryBorder);
        paintHome = new Paint(paintBoundaryBorder);
        paintChannel = new Paint(paintBoundaryBorder);
        paintWork = new Paint(paintBoundaryBorder);

        paintBoundaryBorder.setColor(Color.LTGRAY);
        paintBoundaryBorder.setStrokeWidth(8);

        paintObstacleBorder.setColor(Color.LTGRAY);
        paintObstacleBorder.setStrokeWidth(2);

        paintGnss.setColor(Color.rgb(0, 200, 100));
        paintGnss.setStrokeWidth(10);

        paintChannel.setColor(Color.rgb(0, 200, 180));
        paintChannel.setStrokeWidth(15);

        paintWork.setColor(Color.rgb(150, 150, 200));
        paintWork.setStrokeWidth(3);


        paintBoundaryFill = new Paint();
        paintBoundaryFill.setStyle(Paint.Style.FILL);
        paintBoundaryFill.setColor(Color.rgb(29,148,65));

        paintObstacleFill = new Paint();
        paintObstacleFill.setStyle(Paint.Style.FILL);
        paintObstacleFill.setColor(Color.GRAY);

        bmpHome = BitmapFactory.decodeResource(
                getContext().getResources(), R.drawable.home);

        srcRectHome = new Rect(0,0,bmpHome.getWidth(), bmpHome.getHeight());


        setFocusable(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // Initialize drawing if needed
        Canvas canvas = holder.lockCanvas();
        if (canvas != null) {
            canvas.drawColor(Color.GRAY);
            holder.unlockCanvasAndPost(canvas);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        view_width = width;
        view_height = height;
        mapDrawMng.initParam(view_width, view_height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    private void draw() {
        Canvas canvas = null;
        try {
            canvas = surfaceHolder.lockCanvas();
            if (canvas != null) {
                canvas.drawColor(Color.GRAY);
                drawMap(canvas);
            }
        } finally {
            if (canvas != null) {
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    private void drawBoundary(Canvas canvas, ScrObject object)
    {
        ArrayList<ScrPoint> listPoint = object.listPoint;
        if (listPoint == null) {
            return;
        }
        if (listPoint.size() < 3) {
            return;
        }

        Path path = new Path();
        ScrPoint ptCurr = listPoint.get(0);
        path.moveTo(ptCurr.x, ptCurr.y); // 移动到第一个顶点
        for (int j=1; j<listPoint.size(); j++) {
            ptCurr = listPoint.get(j);
            path.lineTo(ptCurr.x, ptCurr.y);
        }
        path.close();

        canvas.drawPath(path, paintBoundaryFill);
        canvas.drawPath(path, paintBoundaryBorder);
    }

    private void drawObstacle(Canvas canvas, ScrObject object)
    {
        ArrayList<ScrPoint> listPoint = object.listPoint;
        if (listPoint == null) {
            return;
        }
        if (listPoint.size() < 3) {
            return;
        }

        Path path = new Path();
        ScrPoint ptCurr = listPoint.get(0);
        path.moveTo(ptCurr.x, ptCurr.y);
        for (int j=1; j<listPoint.size(); j++) {
            ptCurr = listPoint.get(j);
            path.lineTo(ptCurr.x, ptCurr.y);
        }
        path.close();

        canvas.drawPath(path, paintObstacleFill);
        canvas.drawPath(path, paintObstacleBorder);
    }
    private void drawChannel(Canvas canvas, ScrObject object)
    {
        ArrayList<ScrPoint> listPoint = object.listPoint;
        if (listPoint == null) {
            return;
        }
        if (listPoint.size() < 2) {
            return;
        }
        Path path = new Path();
        ScrPoint ptCurr = listPoint.get(0);
        path.moveTo(ptCurr.x, ptCurr.y);
        for (int j=1; j<listPoint.size(); j++) {
            ptCurr = listPoint.get(j);
            path.lineTo(ptCurr.x, ptCurr.y);
        }

        canvas.drawPath(path, paintChannel);
    }
    private void drawWork(Canvas canvas, ScrObject object)
    {
        ArrayList<ScrPoint> listPoint = object.listPoint;
        if (listPoint == null) {
            return;
        }
        if (listPoint.size() < 2) {
            return;
        }
        Path path = new Path();
        ScrPoint ptCurr = listPoint.get(0);
        path.moveTo(ptCurr.x, ptCurr.y);
        for (int j=1; j<listPoint.size(); j++) {
            ptCurr = listPoint.get(j);
            path.lineTo(ptCurr.x, ptCurr.y);
        }

        canvas.drawPath(path, paintWork);
    }
    private void drawHome(Canvas canvas, ScrPoint scrPointHome)
    {
        Rect dstRect = new Rect(scrPointHome.x - HOME_RADIUS,scrPointHome.y - HOME_RADIUS,
                scrPointHome.x + HOME_RADIUS, scrPointHome.y + HOME_RADIUS);
        canvas.drawBitmap(bmpHome, srcRectHome, dstRect, paintHome);
    }
    private void drawGnss(Canvas canvas, ScrPoint scrPointGnss)
    {
        long currentTimeStamp = System.currentTimeMillis();
        if (currentTimeStamp - lastTimeStamp > 500) {
            flag = !flag;
            lastTimeStamp = currentTimeStamp;
        }
        int radius;

        if (flag) {
            radius = GNSS_RADIUS1;
        }
        else {
            radius = GNSS_RADIUS2;
        }
        if (scrPointGnss != null) {
            RectF rect = new RectF();
            rect.left = scrPointGnss.x - radius;
            rect.right = scrPointGnss.x + radius;
            rect.bottom = scrPointGnss.x - radius;
            rect.top = scrPointGnss.x - radius;
            canvas.drawCircle(scrPointGnss.x, scrPointGnss.y, radius, paintGnss);
        }
    }
    private void drawMap(Canvas canvas)
    {
        ScrPoint scrPointGnss = null;
        ScrPoint scrPointHome = null;
        ArrayList<ScrObject> listScrObject = mapDrawMng.getScrObjList();
        ArrayList<ScrObject> listBoundary= new ArrayList<>();
        ArrayList<ScrObject> listObstacle = new ArrayList<>();
        ArrayList<ScrObject> listChannel = new ArrayList<>();
        ArrayList<ScrObject> listWork = new ArrayList<>();
        for (int i=0; i<listScrObject.size(); i++) {
            ScrObject obj = listScrObject.get(i);
            if (obj.objType == MapObject.OBJ_TYPE_ID_HOME) {
                ScrPoint scrPoint = obj.listPoint.get(0);
                scrPointHome = new ScrPoint(scrPoint);
            }
            else if (obj.objType == MapObject.OBJ_TYPE_GNSS) {
                ScrPoint scrPoint = obj.listPoint.get(0);
                scrPointGnss = new ScrPoint((scrPoint));
            }
            else {
                switch (obj.objType) {
                    case MapObject.OBJ_TYPE_ID_BOUNDARY:
                        listBoundary.add(obj);
                        break;
                    case MapObject.OBJ_TYPE_ID_OBSTACLE:
                        listObstacle.add(obj);
                        break;
                    case MapObject.OBJ_TYPE_ID_CHANNEL:
                        listChannel.add(obj);
                        break;
                    case MapObject.OBJ_TYPE_ID_WORK:
                        listWork.add(obj);
                        break;
                }
            }
        }

        for (int i=0; i<listBoundary.size(); i++) {
            drawBoundary(canvas, listBoundary.get(i));
        }

        for (int i=0; i<listObstacle.size(); i++) {
            drawObstacle(canvas, listObstacle.get(i));
        }
        for (int i=0; i<listChannel.size(); i++) {
            drawChannel(canvas, listChannel.get(i));
        }
        for (int i=0; i<listWork.size(); i++) {
            drawWork(canvas, listWork.get(i));
        }


        if (scrPointHome != null) {
            drawHome(canvas, scrPointHome);
        }
        if (scrPointGnss != null) {
            drawGnss(canvas, scrPointGnss);
        };
    }

    public void zoomIn()
    {
        mapDrawMng.zoomIn();
        draw();
    }

    public void zoomValue(float value)
    {
        mapDrawMng.zoomValue(value);
        draw();
    }

    public void zoomOut()
    {
        mapDrawMng.zoomOut();
        draw();
    }

    public void fit_rect(BoundingBox boundingBox)
    {
        mapDrawMng.fit_rect(boundingBox);
        draw();
    }

    public void setCenter_geo(GeoPoint point)
    {
        mapDrawMng.setCenter_geo(point);
        draw();
    }

    private void move(int dx, int dy)
    {
        mapDrawMng.move(dx, dy);
        draw();
    }
    public void calc_and_redraw()
    {
        mapDrawMng.move(0, 0);
        draw();
    }


    public void setDrawMapType( int type)
    {
        mapDrawMng.setDrawMapType(type);
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                handleActionDown(event);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                handlePointerDown(event);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                handleActionUp();
                break;
            case MotionEvent.ACTION_MOVE:
                handleActionMove(event);
                break;
        }
        return true;
    }

    private void handleActionDown(MotionEvent event) {
        startPoint.set(event.getX(), event.getY());
        mode = DRAG;
    }

    private void handlePointerDown(MotionEvent event) {
        oriDis = distance(event);
        if (oriDis > 10f) {
            midPoint = middle(event);
            mode = ZOOM;
        }
    }

    private void handleActionUp() {
        mode = NONE;
    }

    private void handleActionMove(MotionEvent event) {
        if (mode == DRAG) {
            performDrag(event);
        } else if (mode == ZOOM) {
            performZoom(event);
        }
    }

    private void performDrag(MotionEvent event) {
        int dx = (int) (event.getX() - startPoint.x);
        int dy = (int) (event.getY() - startPoint.y);
        startPoint.set(event.getX(), event.getY());
        move(dx, dy);
        draw();
    }

    private void performZoom(MotionEvent event) {
        float newDist = distance(event);
        if (newDist > 10f) {
            float scale = newDist / oriDis;
            oriDis = newDist;
            zoomValue(scale);
            draw();
        }
    }

    private float distance(MotionEvent event) {
        float dx = event.getX(1) - event.getX(0);
        float dy = event.getY(1) - event.getY(0);
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private PointF middle(MotionEvent event) {
        float x = (event.getX(0) + event.getX(1)) / 2;
        float y = (event.getY(0) + event.getY(1)) / 2;
        return new PointF(x, y);
    }


}