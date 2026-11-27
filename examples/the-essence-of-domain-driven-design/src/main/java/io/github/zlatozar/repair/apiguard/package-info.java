/**
 * HTTP rules that every context shares. A request passes five checks, cheapest first. {@link
 * ProblemHandler} writes each refusal as an RFC 9457 problem, so all contexts answer in one format:
 *
 * <ol>
 *   <li>Origin: out of scope, the example has no authentication.
 *   <li>Size: {@link RequestSizeLimit}, then the Jackson document limit. 413 or 400.
 *   <li>Lexical: {@code pattern}, {@code maxLength} and {@code enum} in the OpenAPI contract. 400.
 *   <li>Syntax: required fields and types in the DTOs generated from the contract. 400.
 *   <li>Semantics: value objects and aggregates. 422 for a refused value. The controllers answer
 *       404 and 409 themselves.
 * </ol>
 */
@ApplicationModule(displayName = "API Guard", allowedDependencies = "sharedkernel")
@NullMarked
package io.github.zlatozar.repair.apiguard;

import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.ApplicationModule;
