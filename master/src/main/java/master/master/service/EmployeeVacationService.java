// package master.master.service;

// import master.master.repository.EmployeeVacationRepository;
// import master.master.web.rest.dto.LeaveRequestDto;
// import org.springframework.stereotype.Service;
// import master.master.mapper.EmployeeVacationMapper;

// import java.util.List;
// import java.util.stream.Collectors;

// @Service
// public class EmployeeVacationService {

//     private final EmployeeVacationRepository vacationRepository;
//     private final EmployeeVacationMapper vacationMapper;

//     public EmployeeVacationService(EmployeeVacationRepository vacationRepository,
//                                    EmployeeVacationMapper vacationMapper) {
//         this.vacationRepository = vacationRepository;
//         this.vacationMapper = vacationMapper;
//     }

//     public List<LeaveRequestDto> getPendingRequests() {
//         return vacationRepository.findByIsAccepted(null).stream()
//                 .map(vacationMapper::toDTO)
//                 .collect(Collectors.toList());
//     }

//     public long countByStatus(String status) {
//         return switch (status.toUpperCase()) {
//             case "APPROVED" -> vacationRepository.countByIsAccepted(true);
//             case "DECLINED" -> vacationRepository.countByIsAccepted(false);
//             case "PENDING" -> vacationRepository.countByIsAccepted(null);
//             default -> 0;
//         };
//     }
// }
