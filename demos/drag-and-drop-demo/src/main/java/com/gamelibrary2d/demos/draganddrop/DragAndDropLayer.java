package com.gamelibrary2d.demos.draganddrop;

import com.gamelibrary2d.CoordinateSpace;
import com.gamelibrary2d.Point;
import com.gamelibrary2d.components.GameObject;
import com.gamelibrary2d.components.containers.AbstractLayer;
import com.gamelibrary2d.components.denotations.PixelAware;
import com.gamelibrary2d.denotations.Bounded;
import com.gamelibrary2d.denotations.Renderable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DragAndDropLayer<T extends GameObject> extends AbstractLayer<T> {
    private final HitDetection hitDetection = new HitDetection();
    private final List<DraggedObject<T>> draggedObjects = new ArrayList<>();
    private final List<DragStartedListener<T>> dragStartedListeners = new CopyOnWriteArrayList<>();
    private final List<DragFinishedListener<T>> dragFinishedListeners = new CopyOnWriteArrayList<>();

    public void addDragStartedPublisher(DragStartedListener<T> listener) {
        dragStartedListeners.add(listener);
    }

    public void addDragFinishedPublisher(DragFinishedListener<T> listener) {
        dragFinishedListeners.add(listener);
    }

    @Override
    protected boolean onPointerDown(int id, int button, float x, float y, float transformedX, float transformedY) {
        return startDrag(id, button, transformedX, transformedY)
                || super.onPointerDown(id, button, x, y, transformedX, transformedY);
    }

    @Override
    public boolean onPointerMove(int id, float x, float y, float transformedX, float transformedY) {
        if (drag(id, transformedX, transformedY)) {
            super.onSwallowedPointerMove(id);
            return true;
        } else {
            return super.onPointerMove(id, x, y, transformedX, transformedY);
        }
    }

    @Override
    public void onPointerUp(int id, int button, float x, float y, float transformedX, float transformedY) {
        if (finishDrag(id, button)) {
            super.onPointerMove(id, x, y, transformedX, transformedY);
        } else {
            super.onPointerUp(id, button, x, y, transformedX, transformedY);
        }
    }

    private boolean startDrag(int id, int button, float transformedX, float transformedY) {
        List<T> objects = getItems();
        for (int i = objects.size() - 1; i >= 0; --i) {
            T obj = objects.get(i);
            if (hitDetection.isPixelVisible(obj, transformedX, transformedY)) {
                draggedObjects.add(new DraggedObject<>(obj, id, button, transformedX, transformedY));
                publishDragStarted(id, obj);
                return true;
            }
        }

        return false;
    }

    private boolean drag(int pointerId, float transformedX, float transformedY) {
        for (DraggedObject<T> obj : draggedObjects) {
            if (obj.getPointerId() == pointerId) {
                obj.drag(transformedX, transformedY);
                return true;
            }
        }

        return false;
    }

    private boolean finishDrag(int pointerId, int button) {
        for(int i = 0; i < draggedObjects.size(); ++i) {
            DraggedObject<T> obj = draggedObjects.get(i);
            if (obj.getPointerId() == pointerId && obj.getPointerButton() == button) {
                draggedObjects.remove(i);
                publishDragFinished(pointerId, obj.obj);
                return true;
            }
        }

        return false;
    }

    private void publishDragStarted(int pointerId, T obj) {
        for (DragStartedListener<T> listener : dragStartedListeners) {
            listener.onDragStarted(pointerId, obj);
        }
    }

    private void publishDragFinished(int pointerId, T obj) {
        for (DragFinishedListener<T> listener : dragFinishedListeners) {
            listener.onDragFinished(pointerId, obj);
        }
    }

    private static class HitDetection {
        private final Transformer transformer = new Transformer();

        public boolean isPixelVisible(Renderable obj, float x, float y) {
            if (obj instanceof PixelAware) {
                Point p = transformer.transform(obj, x, y);
                return ((PixelAware) obj).isPixelVisible(p.getX(), p.getY());
            } else if (obj instanceof Bounded) {
                Point p = transformer.transform(obj, x, y);
                return ((Bounded) obj).getBounds().contains(p.getX(), p.getY());
            } else {
                return false;
            }
        }

        private static class Transformer {
            private final Point point = new Point();

            private Point transform(Renderable obj, float x, float y) {
                point.set(x, y);
                if (obj instanceof CoordinateSpace) {
                    point.transformTo((CoordinateSpace) obj);
                }

                return point;
            }
        }
    }

    private static class DraggedObject<T extends GameObject> {
        private final T obj;
        private final int pointerId;
        private final int pointerButton;
        private float prevPointerPosX;
        private float prevPointerPosY;

        public DraggedObject(T obj, int pointerId, int pointerButton, float pointerPosX, float pointerPosY) {
            this.obj = obj;
            this.pointerId = pointerId;
            this.pointerButton = pointerButton;
            this.prevPointerPosX = pointerPosX;
            this.prevPointerPosY = pointerPosY;
        }

        public int getPointerId() {
            return pointerId;
        }

        public int getPointerButton() {
            return pointerButton;
        }

        public void drag(float pointerPosX, float pointerPosY) {
            obj.getPosition().add(
                    pointerPosX - prevPointerPosX,
                    pointerPosY - prevPointerPosY
            );

            prevPointerPosX = pointerPosX;
            prevPointerPosY = pointerPosY;
        }
    }

    public interface DragStartedListener<T> {
        void onDragStarted(int pointerId, T obj);
    }

    public interface DragFinishedListener<T> {
        void onDragFinished(int pointerId, T obj);
    }
}
