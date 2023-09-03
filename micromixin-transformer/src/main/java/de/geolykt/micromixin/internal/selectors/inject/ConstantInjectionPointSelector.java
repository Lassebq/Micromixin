package de.geolykt.micromixin.internal.selectors.inject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
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
import de.geolykt.micromixin.internal.annotation.ConstantSelector;
import de.geolykt.micromixin.internal.util.ASMUtil;

public class ConstantInjectionPointSelector extends InjectionPointSelector {
    @NotNull
    private static final Set<String> ALL_NAMES = new HashSet<String>(Arrays.asList("org.spongepowered.asm.mixin.injection.points.BeforeConstant", "CONSTANT"));

    @NotNull
    public static final InjectionPointSelectorProvider PROVIDER = new InjectionPointSelectorProvider() {
        @NotNull
        public String getFullyQualifiedName() {
            return "org.spongepowered.asm.mixin.injection.points.BeforeConstant";
        }

        @NotNull
        public Set<String> getAllNames() {
            return ConstantInjectionPointSelector.ALL_NAMES;
        }

        @NotNull
        public InjectionPointSelector create(@Nullable List<String> args, @Nullable InjectionPointTargetConstraint constraint) {
            if (constraint != null) {
                throw new MixinParseException("Broken mixin: Superfluous discriminator found in @At(\"" + this.getFullyQualifiedName() + "\"). Usage of the 'target' or 'desc' constraints is not applicable to the CONSTANT injection point.");
            }
            if (args == null) {
                throw new MixinParseException("Broken mixin: No constant discriminator could be found in @At(\"" + this.getFullyQualifiedName() + "\") args");
            }
            return new ConstantInjectionPointSelector(ConstantSelector.parse(args));
        }
    };

    @NotNull
    private final ConstantSelector constSelector;

    private ConstantInjectionPointSelector(@NotNull ConstantSelector constSelector) {
        super("org.spongepowered.asm.mixin.injection.points.BeforeConstant", "CONSTANT");
        this.constSelector = constSelector;
    }

    @Override
    @NotNull
    public Collection<LabelNode> getLabels(@NotNull MethodNode method, @NotNull SimpleRemapper remapper, @NotNull StringBuilder sharedBuilder) {
        // IMPLEMENT fetch ordinal
        List<LabelNode> labels = new ArrayList<LabelNode>();
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (this.constSelector.matchesConstant(insn)) {
                labels.add(ASMUtil.getLabelNodeBefore(insn, method.instructions));
            }
        }
        return labels;
    }

    @Override
    public boolean supportsRedirect() {
        return false;
    }
}
