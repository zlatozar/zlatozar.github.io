package io.github.zlatozar.repair.department.internal.domain;

/**
 * The life cycle of an instrument in its department:
 *
 * <pre>
 * USABLE --sent for repair--> WAITING_FOR_REPAIR --box approved--> AT_WORKSHOP --returned--> USABLE
 *    \----sent for repair, third-party----> WAITING_FOR_REPLACEMENT (end state)
 * </pre>
 */
public enum InstrumentState {
  USABLE,
  WAITING_FOR_REPAIR,
  AT_WORKSHOP,
  WAITING_FOR_REPLACEMENT
}
