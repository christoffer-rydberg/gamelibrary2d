package com.gamelibrary2d.splitscreen;

import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.denotations.Renderable;
import com.gamelibrary2d.components.GameObject;
import com.gamelibrary2d.disposal.DefaultDisposer;
import com.gamelibrary2d.disposal.Disposer;
import com.gamelibrary2d.opengl.ModelMatrix;
import com.gamelibrary2d.opengl.renderers.Canvas;

public class SplitLayoutLeaf<T> implements SplitLayout {
    private final InternalTargetSettings internalTargetSettings;
    private final T param;
    private final PrepareUpdateAction<T> prepareUpdate;
    private final PrepareRenderAction<T> prepareRender;

    private Disposer disposer;
    private Canvas<Renderable> canvas;
    private GameObject target;

    /**
     * Creates a new instance of {@link SplitLayoutLeaf}.
     *
     * @param target        The target view of the {@link SplitLayer}.
     * @param prepareRender Preparation action performed before rendering.
     * @param param         Parameter used in preparation actions.
     * @param disposer      Disposer used for internal resources.
     */
    public SplitLayoutLeaf(GameObject target,
                           PrepareRenderAction<T> prepareRender,
                           T param,
                           Disposer disposer) {
        this(target, null, prepareRender, param, disposer);
    }

    /**
     * Creates a new instance of {@link SplitLayoutLeaf}.
     *
     * @param target        The target view of the {@link SplitLayer}.
     * @param prepareUpdate Preparation action performed before updating.
     * @param param         Parameter used in preparation actions.
     * @param disposer      Disposer used for internal resources.
     */
    public SplitLayoutLeaf(GameObject target,
                           PrepareUpdateAction<T> prepareUpdate,
                           T param,
                           Disposer disposer) {
        this(target, prepareUpdate, null, param, disposer);
    }

    /**
     * Creates a new instance of {@link SplitLayoutLeaf}.
     *
     * @param target        The target view of the {@link SplitLayer}.
     * @param prepareUpdate Preparation action performed before updating.
     * @param prepareRender Preparation action performed before rendering.
     * @param param         Parameter used in preparation actions.
     * @param disposer      Disposer used for internal resources.
     */
    public SplitLayoutLeaf(GameObject target,
                           PrepareUpdateAction<T> prepareUpdate,
                           PrepareRenderAction<T> prepareRender,
                           T param,
                           Disposer disposer) {
        this.disposer = new DefaultDisposer();
        this.target = target;
        this.param = param;
        this.prepareUpdate = prepareUpdate;
        this.prepareRender = prepareRender;
        this.internalTargetSettings = new InternalTargetSettings();
        this.disposer = new DefaultDisposer(disposer);
        updateSettingsFromTarget();
    }

    @Override
    public void update(GameObject target, Rectangle viewArea, float deltaTime) {
        this.target = target;
        if (canvas == null || !canvas.getBounds().equals(viewArea)) {
            disposer.dispose();
            canvas = Canvas.create(this::renderAction, viewArea, true, disposer);
        }

        updateTargetFromSettings();
        if (prepareUpdate != null)
            prepareUpdate.invoke(param, viewArea, deltaTime);
        updateSettingsFromTarget();
    }

    @Override
    public void render(float alpha) {
        updateTargetFromSettings();
        if (prepareRender != null) {
            prepareRender.invoke(param, canvas.getBounds());
        }
        canvas.render(alpha);
        updateSettingsFromTarget();
    }

    private void renderAction(float alpha) {
        ModelMatrix.instance().pushMatrix();
        Rectangle renderArea = canvas.getBounds();
        ModelMatrix.instance().translatef(renderArea.getLowerX(), renderArea.getLowerY(), 0);
        target.render(alpha);
        ModelMatrix.instance().popMatrix();
    }

    private void updateTargetFromSettings() {
        target.setCoordinateSpace(internalTargetSettings);
    }

    private void updateSettingsFromTarget() {
        internalTargetSettings.setCoordinateSpace(target);
    }
}
