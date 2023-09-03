package de.geolykt.micromixin.internal.selectors.inject;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

import de.geolykt.micromixin.SimpleRemapper;
import de.geolykt.micromixin.api.InjectionPointSelector;
import de.geolykt.micromixin.api.InjectionPointSelectorFactory.InjectionPointSelectorProvider;
import de.geolykt.micromixin.api.InjectionPointTargetConstraint;
import de.geolykt.micromixin.internal.MixinParseException;

public class HeadInjectionPointSelector extends InjectionPointSelector implements InjectionPointSelectorProvider {

    @NotNull
    public static final HeadInjectionPointSelector INSTANCE = new HeadInjectionPointSelector();

    private HeadInjectionPointSelector() {
        super("org.spongepowered.asm.mixin.injection.points.MethodHead", "HEAD");
    }

    @Override
    @NotNull
    public Collection<LabelNode> getLabels(@NotNull MethodNode method, @NotNull SimpleRemapper remapper, @NotNull StringBuilder sharedBuilder) {
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn.getOpcode() != -1) {
                LabelNode temp = new LabelNode();
                method.instructions.insertBefore(temp, insn);
                return Collections.singletonList(temp);
            } else if (insn instanceof LabelNode) {
                @SuppressWarnings("null")
                LabelNode temp = (LabelNode) insn; // I don't quite understand why that hack is necessary, but whatever floats your boat...
                return Collections.singletonList(temp);
            }
        }
        // There are no instructions in the list
        LabelNode temp = new LabelNode();
        method.instructions.insert(temp);
        return Collections.singletonList(temp);
    }

    @Override
    public boolean supportsRedirect() {
        return false;
    }

    @NotNull
    public String getFullyQualifiedName() {
        return this.fullyQualifiedName;
    }

    @NotNull
    public Set<String> getAllNames() {
        return this.allNames;
    }

    @NotNull
    public InjectionPointSelector create(@Nullable List<String> args, @Nullable InjectionPointTargetConstraint constraint) {
        if (constraint != null) {
            throw new MixinParseException("Broken mixin: Superfluous discriminator found in @At(\"" + this.getFullyQualifiedName() + "\"). Usage of the 'target' or 'desc' constraints is not applicable to the " + this.getFullyQualifiedName() + " injection point.");
        }
        if (args != null) {
            throw new MixinParseException("Broken mixin: Superfluous discriminator found in @At(\"" + this.getFullyQualifiedName() + "\"). Usage of the 'args' constraints is not applicable to the " + this.getFullyQualifiedName() + " injection point.");
        }
        return this;
    }
}
