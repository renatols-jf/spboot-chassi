package io.github.renatolsjf.chassis.rendering;

import io.github.renatolsjf.chassis.rendering.transforming.TransformingPath;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

//TODO: o printNull não pega, de forma óbvia, valores dentro de um mapa quando este mapa não é criado pela Media e, no lugar,
// é inserido diretamente dentro da Midia. Provavelmente criar um método do tipo printAll ou algo do tipo
// para validar campo a campo e inclusive mapas dentro de mapas.
public abstract class Media {

    private static final boolean PRINT_NULL_DEFAULT = false;

    protected boolean printNull;
    protected Map<String, String> attributes = new HashMap<>();

    protected Media(boolean printNull) {
        this.printNull = printNull;
    }

    public final Media print(String name, Object content) {
        if (this.printNull || content != null) {
            if (content instanceof Renderable) {
                return this.forkRenderable(name, (Renderable) content);
            } else if (content instanceof Collection && ((Collection) content).stream().allMatch(o -> o instanceof Renderable)) {
                return this.forkCollection(name, (Collection<? extends Renderable>) content);
            } else {
                doPrint(name, content);
            }
        }
        return this;
    }

    public final Media addAttribute(String name, String value) {
        this.attributes.put(name, value);
        return this;
    }

    public Map<String, String> getAttributes() {
        return this.attributes;
    }

    protected abstract void doPrint(String name, Object content);
    protected abstract MediaContent getContent(TransformingPath transformingPath);
    public abstract Object render();

    public final Media transform(MediaTransformer trasnformer) {
        return trasnformer.transform(this);
    }

    public final Media forkCollection(String name, Renderable... renderables){
        return this.print(name, ofCollection(this.printNull, renderables).render());
    }

    public final Media forkCollection(String name, Collection<? extends Renderable> renderables) {
        return this.print(name, ofCollection(this.printNull, renderables).render());
    }

    public final Media forkRenderable(String name, Renderable renderable) {
        return this.print(name, ofRenderable(this.printNull, renderable).render());
    }

    public Media shrinkTo(TransformingPath transformingPath, String... attributes) {
        MediaContent c = this.getContent(transformingPath);
        c.keepOnly(attributes);
        return this;
    }

    public static Media ofCollection(Renderable... renderables) {
        return ofCollection(PRINT_NULL_DEFAULT, renderables);
    }

    public static Media ofCollection(boolean printNull, Renderable... renderables) {
        CollectionMedia cm = new CollectionMedia(printNull);
        for(Renderable r: renderables) {
            r.render(cm);
            cm.next();
        }
        return cm;
    }

    public static Media ofCollection(Collection<? extends Renderable> renderables) {
        return ofCollection(PRINT_NULL_DEFAULT, renderables);
    }

    public static Media ofCollection(boolean printNull, Collection<? extends Renderable> renderables) {
        CollectionMedia cm = new CollectionMedia(printNull);
        if(renderables == null) {
            return cm;
        }
        for(Renderable r: renderables) {
            r.render(cm);
            cm.next();
        }
        return cm;
    }

    public static Media ofRenderable(Renderable renderable) {
        return ofRenderable(PRINT_NULL_DEFAULT, renderable);
    }

    public static Media ofRenderable(boolean printNull, Renderable renderable) {
        Media m = new SingleMedia(printNull);
        return renderable != null ? renderable.render(m) : empty();
    }

    public static Media empty() {
        return new VoidMedia(false);
    }

}
