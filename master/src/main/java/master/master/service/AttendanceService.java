//package master.master.service;
//
//
//import master.master.mapper.AttendanceMapper;
//import master.master.mapper.EmployeeMapper;
//import master.master.mapper.EmployeeVacationMapper;
//import master.master.repository.EmployeeRepository;
//import master.master.repository.EmployeeVacationRepository;
//import master.master.web.rest.dto.AttendanceDto;
//import master.master.web.rest.dto.EmployeeWorkTimeDto;
//import master.master.web.rest.dto.LeaveRequestDto;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//public class AttendanceService {
//
//    private final EmployeeRepository employeeRepo;
//    private final EmployeeVacationRepository vacationRepo;
//    private final EmployeeMapper employeeMapper;
//    private final EmployeeVacationMapper vacationMapper;
//    private final AttendanceMapper attendanceMapper;
//
//    public AttendanceService(EmployeeRepository employeeRepo,
//                             EmployeeVacationRepository vacationRepo,
//                             EmployeeMapper employeeMapper,
//                             EmployeeVacationMapper vacationMapper,
//                             AttendanceMapper attendanceMapper) {
//        this.employeeRepo = employeeRepo;
//        this.vacationRepo = vacationRepo;
//        this.employeeMapper = employeeMapper;
//        this.vacationMapper = vacationMapper;
//        this.attendanceMapper = attendanceMapper;
//    }
//
//    public List<EmployeeWorkTimeDto> getWorkTimes() {
//        return employeeRepo.findAll().stream()
//                .map(employeeMapper::toWorkTimeDto)
//                .toList();
//    }
//
//    public List<LeaveRequestDto> getLeaveRequests() {
//        return vacationRepo.findByIsAccepted(null).stream()
//                .map(vacationMapper::toDTO)
//                .toList();
//    }
//
//    public List<AttendanceDto> getAttendance() {
//        return employeeRepo.findAll().stream()
//                .map(attendanceMapper::toDTO)
//                .toList();
//    }
//}