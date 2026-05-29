package master.master.web.rest.dto;

import lombok.Builder;
import lombok.Data;

/** DTO containing the main aggregated counters displayed on the employee dashboard. */
@Data
@Builder
public class DashboardStatsDto {
  private Long totalEmployees;
  private Long pendingLeaveRequests;
  private Long totalRooms;
  private Double averageRating;
  private Long activeBookings;
  private Long completedTasks;
}
