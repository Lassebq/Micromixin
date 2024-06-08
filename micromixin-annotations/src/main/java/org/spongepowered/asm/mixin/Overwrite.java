package org.spongepowered.asm.mixin;

import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@link Overwrite} can be applied on methods that need to be explicitly overwritten.
 * By default an unannotated method will replace the bytecode of a method it collides with,
 * but if the annotated member does not collide with a method in the target, a new method will be created.
 * In contrast, {@link Overwrite} does not create new methods and requires a collision.
 * Should no collision occur, a compile-time error is thrown.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ METHOD })
public @interface Overwrite {

    // TODO Add #prefix() and update the link (see line 33 as of 2024-06-08)

    /**
     * <b>The spongeian implementation (and therefore the standard Micromixin implementation) does not remap the name
     * of aliased methods in INVOKESTATIC calls. That is "working as intended" - but be aware that static methods
     * should not be referenced if they are aliased.</b>
     *
     * <p>The aliases of the method. Only one alias is selected from the given list,
     * if none match an exception is thrown during transformation. The actual name of the method
     * is discarded and will not play a role once aliases are defined.
     *
     * <p>Aliases are not affected by (@link #prefix() the prefix).
     *
     * <p>The spongeian Mixin specification advises against the usage of aliases.
     * According to them aliases should only be used if the name of the target
     * has changed. (The behaviour of aliases does not allow much more usecases
     * anyways)
     *
     * <p>In the spongeian mixin implementation, <b>aliases are not supported on non-private members</b>
     * (for this, the access modifier of the target member is being used, not the one of the member in the mixin
     * class). The cited reason for this is that other mixins could add fields that would match the alias and thus
     * invalidate caches. It is unclear why this is an issue in the spongeian implementation, but for compatibility
     * reasons it is recommended to ensure that shadowed members with aliases are private.
     * While micromixin-transformer  and micromixin-remapper support the usage of aliases on non-private members,
     * it will log a warning at application time when doing so and may in the future refuse to apply such mixins.
     * Proceed with care.
     *
     * @return The aliases to use.
     */
    public String[] aliases() default { }; 
}
