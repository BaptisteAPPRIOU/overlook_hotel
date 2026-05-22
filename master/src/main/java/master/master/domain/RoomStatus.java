package master.master.domain;

public enum RoomStatus {
  AVAILABLE,
  OCCUPIED,
  RESERVED,
  MAINTENANCE,
  CLEANING,
  OUT_OF_ORDER,
  INACTIVE;

  public String getDisplayName() {
    return name().replace('_', ' ');
  }
}
