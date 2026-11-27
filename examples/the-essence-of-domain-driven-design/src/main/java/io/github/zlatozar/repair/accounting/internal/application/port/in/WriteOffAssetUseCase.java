package io.github.zlatozar.repair.accounting.internal.application.port.in;

/**
 * Writes off an instrument that cannot be repaired: it is no longer one of the hospital's assets.
 * Idempotent: a second call changes nothing.
 */
public interface WriteOffAssetUseCase {

  void writeOff(WriteOffCommand command);
}
