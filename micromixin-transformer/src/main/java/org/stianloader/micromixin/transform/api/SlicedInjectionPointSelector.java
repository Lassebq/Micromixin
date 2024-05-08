package org.stianloader.micromixin.transform.api;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.stianloader.micromixin.transform.internal.SimpleRemapper;
import org.stianloader.micromixin.transform.internal.selectors.inject.TailInjectionPointSelector;
import org.stianloader.micromixin.transform.internal.util.ASMUtil;

public class SlicedInjectionPointSelector {

    @NotNull
    private final InjectionPointSelector selector;
    @Nullable
    private final SlicedInjectionPointSelector from;
    @Nullable
    private final SlicedInjectionPointSelector to;

    public SlicedInjectionPointSelector(@NotNull InjectionPointSelector selector, @Nullable SlicedInjectionPointSelector from, @Nullable SlicedInjectionPointSelector to) {
        this.selector = selector;
        this.from = from;
        this.to = to;
    }

    /**
     * Obtains the instruction immediately after the next instruction.
     *
     * <p>Handle with care, improper use may lead to unexpected behaviour with frames or jumps!
     * The method is mainly intended to be used in order to easily check whether an instruction lies between
     * two other instructions.
     *
     * @param method The method to find the entrypoints in.
     * @param remapper The remapper instance to make use of. This is used to remap any references of the mixin class to the target class when applying injection point constraints.
     * @param sharedBuilder Shared {@link StringBuilder} instance to reduce {@link StringBuilder} allocations.
     * @return Instruction immediately after the next instruction.
     */
    @Nullable
    public AbstractInsnNode getAfterSelected(@NotNull MethodNode method, @NotNull SimpleRemapper remapper, @NotNull StringBuilder sharedBuilder) {
        AbstractInsnNode first = this.getFirstInsn(method, remapper, sharedBuilder);
        if (first == null) {
            return null;
        }
        AbstractInsnNode selected = ASMUtil.afterInstruction(first);
        AbstractInsnNode afterSelected = selected.getNext();
        if (afterSelected == null) {
            return selected;
        } else {
            return afterSelected;
        }
    }

    /**
     * Obtains the first {@link AbstractInsnNode} that corresponds to the first applicable entrypoint within
     * the provided method as defined by this {@link InjectionPointSelector}. The {@link AbstractInsnNode} may
     * not be virtual, that is it may not have an {@link AbstractInsnNode#getOpcode() opcode} value of -1.
     *
     * <p>Implementations of this method are trusted to not go out of bounds when it comes to the slices.
     * Failure to do so could have nasty consequences for user behaviour as micromixin-transformer or any
     * other caller is not guaranteed to verify the location of the matched instruction (but inversely it is not
     * guaranteed that such as check won't be introduced in the future).
     *
     * @param method The method to find the entrypoints in.
     * @param remapper The remapper instance to make use of. This is used to remap any references of the mixin class to the target class when applying injection point constraints.
     * @param sharedBuilder Shared {@link StringBuilder} instance to reduce {@link StringBuilder} allocations.
     * @return The first matched instruction, or null if no instructions match.
     */
    @Nullable
    public AbstractInsnNode getFirstInsn(@NotNull MethodNode method, @NotNull SimpleRemapper remapper, @NotNull StringBuilder sharedBuilder) {
        return this.selector.getFirstInsn(method, this.from, this.to, remapper, sharedBuilder);
    }

    @Nullable
    public SlicedInjectionPointSelector getFrom() {
        return this.from;
    }

    /**
     * Obtains the {@link AbstractInsnNode AbstractInsnNodes} that correspond to every applicable entrypoint within
     * the provided method as defined by this {@link SlicedInjectionPointSelector}.
     *
     * <p>Implementations of this method are trusted to not go out of bounds when it comes to the slices.
     * Failure to do so could have nasty consequences for user behaviour as micromixin-transformer or any
     * other caller is not guaranteed to verify the location of the matched instructions (but inversely it is not
     * guaranteed that such as check won't be introduced in the future).
     *
     * @param method The method to find the entrypoints in.
     * @param remapper The remapper instance to make use of. This is used to remap any references of the mixin class to the target class when applying injection point constraints.
     * @param sharedBuilder Shared {@link StringBuilder} instance to reduce {@link StringBuilder} allocations.
     * @return The selected instruction nodes that correspond to this entry point.
     */
    @NotNull
    public Collection<? extends AbstractInsnNode> getMatchedInstructions(@NotNull MethodNode method, @NotNull SimpleRemapper remapper, @NotNull StringBuilder sharedBuilder) {
        return this.selector.getMatchedInstructions(method, this.from, this.to, remapper, sharedBuilder);
    }

    @NotNull
    public InjectionPointSelector getSelector() {
        return this.selector;
    }

    @Nullable
    public SlicedInjectionPointSelector getTo() {
        return this.to;
    }

    @Deprecated
    public boolean supportsConstructors() {
        // FIXME This is completely bogus behaviour!
        return this.selector == TailInjectionPointSelector.INSTANCE;
    }

    /**
     * Checks whether the injection point can be used in conjunction with the Redirect-annotation.
     * Generally should only be true if this injection point selector can select method instruction nodes.
     *
     * @return True if usable in redirects, false otherwise.
     * @deprecated Redirect in theory (not implemented in micromixin-transformer as of now) also supports redirecting
     * PUTFIELD/GETFIELD, PUTSTATIC/GETStATIC, xALOAD and the ARRAYLENGTH instructions. Added with the introduction of
     * slices which make the semantics of the matched instruction type less predictable at first glance, this method
     * has little sense going forward.
     */
    @Deprecated
    public boolean supportsRedirect() {
        return this.selector.supportsRedirect();
    }

    @Override
    @NotNull
    public String toString() {
        return "SlicedInjectionPointSelector[" + this.selector.fullyQualifiedName + ", from = " + this.from + ", to = " + this.to + "]";
    }
}
