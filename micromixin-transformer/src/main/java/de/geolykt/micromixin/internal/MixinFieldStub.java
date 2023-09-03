package de.geolykt.micromixin.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import de.geolykt.micromixin.SimpleRemapper;
import de.geolykt.micromixin.internal.annotation.MixinAnnotation;
import de.geolykt.micromixin.internal.annotation.MixinShadowAnnotation;
import de.geolykt.micromixin.internal.annotation.MixinUniqueAnnotation;
import de.geolykt.micromixin.internal.annotation.VirtualFieldOverlayAnnotation;

public class MixinFieldStub implements ClassMemberStub {

    @NotNull
    public final ClassNode owner;
    @NotNull
    public final FieldNode field;
    @NotNull
    public final Collection<MixinAnnotation<MixinFieldStub>> annotations;

    private MixinFieldStub(@NotNull ClassNode owner, @NotNull FieldNode field, @NotNull Collection<MixinAnnotation<MixinFieldStub>> annotations) {
        this.owner = owner;
        this.field = field;
        this.annotations = annotations;
    }

    @NotNull
    public static MixinFieldStub parse(@NotNull ClassNode owner, @NotNull FieldNode field) {
        List<MixinAnnotation<MixinFieldStub>> annotations = new ArrayList<MixinAnnotation<MixinFieldStub>>();
        if (field.visibleAnnotations != null) {
            for (AnnotationNode annot : field.visibleAnnotations) {
                if (annot.desc.startsWith("Lorg/spongepowered/asm/")) {
                    if (annot.desc.equals("Lorg/spongepowered/asm/mixin/Shadow;")) {
                        annotations.add(MixinShadowAnnotation.<MixinFieldStub>parse(annot));
                    } else if (annot.desc.equals("Lorg/spongepowered/asm/mixin/Unique;")) {
                        annotations.add(MixinUniqueAnnotation.<MixinFieldStub>parse(annot));
                    } else {
                        throw new MixinParseException("Unimplemented mixin annotation: " + annot.desc);
                    }
                }
            }
        }
        if (annotations.isEmpty()) {
            annotations.add(new VirtualFieldOverlayAnnotation());
        }
        // TODO Implicit field overwrite/overlay!
        return new MixinFieldStub(owner, field, Collections.unmodifiableCollection(annotations));
    }

    @NotNull
    public ClassNode getOwner() {
        return this.owner;
    }

    @NotNull
    public String getName() {
        return this.field.name;
    }

    @NotNull
    public String getDesc() {
        return this.field.desc;
    }

    public void applyTo(@NotNull ClassNode target, @NotNull HandlerContextHelper hctx, @NotNull MixinStub source,
            @NotNull SimpleRemapper remapper, @NotNull StringBuilder sharedBuilder) {
        for (MixinAnnotation<MixinFieldStub> a : this.annotations) {
            a.apply(target, hctx, source, this, remapper, sharedBuilder);
        }
    }

    public void collectMappings(@NotNull ClassNode target, @NotNull HandlerContextHelper hctx,
            @NotNull MixinStub stub, @NotNull SimpleRemapper out,
            @NotNull StringBuilder sharedBuilder) {
        for (MixinAnnotation<MixinFieldStub> annotation : this.annotations) {
            annotation.collectMappings(this, target, out, sharedBuilder);
        }
    }

    public int getAccess() {
        return this.field.access;
    }
}
