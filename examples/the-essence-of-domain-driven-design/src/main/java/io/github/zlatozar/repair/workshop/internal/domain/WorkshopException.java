package io.github.zlatozar.repair.workshop.internal.domain;

/** A command the workshop refuses. Sealed, so the web adapter must map every case. */
public abstract sealed class WorkshopException extends RuntimeException {

  private WorkshopException(String message) {
    super(message);
  }

  public static final class UnknownRepairJob extends WorkshopException {
    public UnknownRepairJob(RepairJobId job) {
      super("There is no repair job " + job);
    }
  }

  public static final class NotStarted extends WorkshopException {
    public NotStarted(RepairJobId job) {
      super("Repair job " + job + " has not started yet");
    }
  }

  public static final class AlreadyFinished extends WorkshopException {
    public AlreadyFinished(RepairJobId job) {
      super("Repair job " + job + " is already finished");
    }
  }
}
