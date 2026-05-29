package master.master.web.rest.dto;

/** DTO carrying the optional reason and comment used when rejecting a leave request. */
public class RejectLeaveRequestDto {
  private String rejectionReason;
  private String rejectionComment;

  /** Returns the short reason selected for the rejection. */
  public String getRejectionReason() {
    return rejectionReason;
  }

  /** Updates the short reason selected for the rejection. */
  public void setRejectionReason(String rejectionReason) {
    this.rejectionReason = rejectionReason;
  }

  /** Returns the free-form rejection comment. */
  public String getRejectionComment() {
    return rejectionComment;
  }

  /** Updates the free-form rejection comment. */
  public void setRejectionComment(String rejectionComment) {
    this.rejectionComment = rejectionComment;
  }
}
