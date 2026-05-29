package master.master.web.rest.dto;

/** DTO carrying the optional manager comment used when approving a leave request. */
public class ApproveLeaveRequestDto {
  private String approvalComment;

  /** Returns the comment attached to the approval decision. */
  public String getApprovalComment() {
    return approvalComment;
  }

  /** Updates the comment attached to the approval decision. */
  public void setApprovalComment(String approvalComment) {
    this.approvalComment = approvalComment;
  }
}
