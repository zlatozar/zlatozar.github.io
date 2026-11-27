package io.github.zlatozar.repair.department.internal.domain;

import io.github.zlatozar.repair.sharedkernel.InstrumentId;

/** A command the department refuses. Sealed, so the web adapter must map every case. */
public abstract sealed class DepartmentException extends RuntimeException {

  private DepartmentException(String message) {
    super(message);
  }

  public static final class UnknownDepartment extends DepartmentException {
    public UnknownDepartment(DepartmentCode department) {
      super("There is no department " + department);
    }
  }

  public static final class UnknownInstrument extends DepartmentException {
    public UnknownInstrument(DepartmentCode department, InstrumentId instrument) {
      super("Department " + department + " has no instrument " + instrument);
    }
  }

  public static final class UnknownRepairBox extends DepartmentException {
    public UnknownRepairBox(DepartmentCode department, RepairBoxId box) {
      super("Department " + department + " has no repair box " + box);
    }
  }

  public static final class NotAtWorkshop extends DepartmentException {
    public NotAtWorkshop(InstrumentId instrument, InstrumentState state) {
      super("Instrument " + instrument + " cannot return: it is " + state);
    }
  }
}
