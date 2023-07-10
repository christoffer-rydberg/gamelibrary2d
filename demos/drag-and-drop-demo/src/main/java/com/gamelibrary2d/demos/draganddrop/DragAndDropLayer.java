package com.gamelibrary2d.demos.draganddrop;

import com.gamelibrary2d.CoordinateSpace;
import com.gamelibrary2d.Point;
import com.gamelibrary2d.components.GameObject;
import com.gamelibrary2d.components.containers.AbstractLayer;
import com.gamelibrary2d.components.denotations.PixelAware;
import com.gamelibrary2d.denotations.Bounded;
import com.gamelibrary2d.denotations.Renderable;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class DragAndDropLayer<T extends GameObject> extends AbstractLayer<T> {
    private final HitDetection hitDetection = new HitDetection();
    private final Set<Integer> downPointers = new HashSet<>();
    private final Map<Integer, T> hoveredObjects = new HashMap<>();
    private final List<DraggedObject> draggedObjects = new ArrayList<>();
    private final List<HoverStartedListener<T>> hoverStartedListeners = new CopyOnWriteArrayList<>();
    private final List<HoverFinishedListener<T>> hoverFinishedListeners = new CopyOnWriteArrayList<>();
    private final List<DragStartedListener<T>> dragStartedListeners = new CopyOnWriteArrayList<>();
    private final List<DragFinishedListener<T>> dragFinishedListeners = new CopyOnWriteArrayList<>();

    @Override
    protected boolean onPointerDown(int id, int button, float x, float y, float transformedX, float transformedY) {
        downPointers.add(id);
        T obj = hoveredObjects.remove(id);
        if (obj != null) {
            hoverFinished(obj, id);
        }

        return startDrag(id, button, transformedX, transformedY)
                || super.onPointerDown(id, button, x, y, transformedX, transformedY);
    }

    @Override
    public boolean onPointerMove(int id, float x, float y, float transformedX, float transformedY) {
        if (drag(id, transformedX, transformedY) || hover(id, transformedX, transformedY)) {
            super.onSwallowedPointerMove(id);
            return true;
        } else {
            return super.onPointerMove(id, x, y, transformedX, transformedY);
        }
    }

    @Override
    public void onPointerUp(int id, int button, float x, float y, float transformedX, float transformedY) {
        downPointers.remove(id);
        boolean dragFinished = finishDrag(id, button);
        hover(id, transformedX, transformedY);
        if (!dragFinished) {
            super.onPointerUp(id, button, x, y, transformedX, transformedY);
        }
    }

    private boolean startDrag(int id, int button, float transformedX, float transformedY) {
        List<T> objects = getItems();
        for (int i = objects.size() - 1; i >= 0; --i) {
            T obj = objects.get(i);
            if (startDrag(obj, id, button, transformedX, transformedY)) {
                Draggable draggable = (Draggable) obj;
                draggedObjects.add(new DraggedObject(draggable, id, button, transformedX, transformedY));
                publishDragStarted(id, obj);
                return true;
            }
        }

        return false;
    }

    private boolean startDrag(T obj, int pointerId, int button, float transformedX, float transformedY) {
        return obj.isEnabled()
                && obj instanceof Draggable
                && hitDetection.isPixelVisible(obj, transformedX, transformedY)
                && ((Draggable) obj).onDragStarted(pointerId, button);
    }

    private boolean hover(int pointerId, float transformedX, float transformedY) {
        if (downPointers.contains(pointerId)) {
            return false;
        }

        T prev = hoveredObjects.get(pointerId);
        if (prev != null && !prev.isEnabled()) {
            hoverFinished(prev, pointerId);
            prev = null;
        }

        List<T> objects = getItems();
        for (int i = objects.size() - 1; i >= 0; --i) {
            T current = objects.get(i);
            if (current.isEnabled() && hitDetection.isPixelVisible(current, transformedX, transformedY)) {
                hover(prev, current, pointerId);
                return true;
            }
        }

        if (prev != null) {
            hoverFinished(prev, pointerId);
        }

        return false;
    }

    private void hover(T prev, T current, int pointerId) {
        if (current == prev) {
            hoverPreviousTarget(current, pointerId);
        } else if (prev != null) {
            hoverUpdatedTarget(prev, current, pointerId);
        } else {
            hoverNewTarget(current, pointerId);
        }
    }

    private void hoverPreviousTarget(T current, int pointerId) {
        if (!((Hoverable) current).onHover()) {
            hoverFinished(current, pointerId);
        }
    }

    private void hoverUpdatedTarget(T prev, T current, int pointerId) {
        hoverFinished(prev, pointerId);
        hoverStarted(current, pointerId);
    }

    private void hoverNewTarget(T current, int pointerId) {
        hoverStarted(current, pointerId);
    }

    private void hoverStarted(T obj, int pointerId) {
        if (((Hoverable) obj).onHoverStarted(pointerId)) {
            publishHoverStarted(pointerId, obj);
            hoveredObjects.put(pointerId, obj);
        }
    }

    private void hoverFinished(T obj, int pointerId) {
        ((Hoverable) obj).onHoverFinished(pointerId);
        publishHoverFinished(pointerId, obj);
        hoveredObjects.remove(pointerId);
    }

    private boolean drag(int pointerId, float transformedX, float transformedY) {
        for (int i = 0; i < draggedObjects.size(); ++i) {
            DraggedObject obj = draggedObjects.get(i);
            if (obj.getPointerId() == pointerId) {
                if (!obj.drag(transformedX, transformedY)) {
                    draggedObjects.remove(i);
                    //noinspection unchecked
                    publishDragFinished(pointerId, (T) obj.obj);
                }

                return true;
            }
        }

        return false;
    }

    private boolean finishDrag(int pointerId, int button) {
        for (int i = 0; i < draggedObjects.size(); ++i) {
            DraggedObject obj = draggedObjects.get(i);
            if (obj.getPointerId() == pointerId && obj.getPointerButton() == button) {
                draggedObjects.remove(i);
                obj.obj.onDragFinished(pointerId, button);
                //noinspection unchecked
                publishDragFinished(pointerId, (T) obj.obj);
                return true;
            }
        }

        return false;
    }

    public void addHoverStartedPublisher(HoverStartedListener<T> listener) {
        hoverStartedListeners.add(listener);
    }

    public void addHoverFinishedPublisher(HoverFinishedListener<T> listener) {
        hoverFinishedListeners.add(listener);
    }

    public void addDragStartedPublisher(DragStartedListener<T> listener) {
        dragStartedListeners.add(listener);
    }

    public void addDragFinishedPublisher(DragFinishedListener<T> listener) {
        dragFinishedListeners.add(listener);
    }

    private void publishHoverStarted(int pointerId, T obj) {
        for (HoverStartedListener<T> listener : hoverStartedListeners) {
            listener.onHoverStarted(pointerId, obj);
        }
    }

    private void publishHoverFinished(int pointerId, T obj) {
        for (HoverFinishedListener<T> listener : hoverFinishedListeners) {
            listener.onHoverFinished(pointerId, obj);
        }
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

    private static class DraggedObject {
        private final Draggable obj;
        private final int pointerId;
        private final int pointerButton;
        private float prevPointerPosX;
        private float prevPointerPosY;

        public DraggedObject(Draggable obj, int pointerId, int pointerButton, float pointerPosX, float pointerPosY) {
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

        public boolean drag(float pointerPosX, float pointerPosY) {
            boolean res = obj.onDrag(pointerPosX - prevPointerPosX, pointerPosY - prevPointerPosY);
            prevPointerPosX = pointerPosX;
            prevPointerPosY = pointerPosY;
            return res;
        }
    }

    public interface HoverStartedListener<T> {
        void onHoverStarted(int pointerId, T obj);
    }

    public interface HoverFinishedListener<T> {
        void onHoverFinished(int pointerId, T obj);
    }

    public interface DragStartedListener<T> {
        void onDragStarted(int pointerId, T obj);
    }

    public interface DragFinishedListener<T> {
        void onDragFinished(int pointerId, T obj);
    }
}
