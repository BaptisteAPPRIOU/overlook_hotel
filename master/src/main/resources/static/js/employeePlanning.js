    // Planning data for Alpine.js
    function planningData() {
        return {
            employees: [],
            currentWeek: new Date(),
            selectedEmployees: [],
            shifts: {
                'EMPLOYEE': { color: '#0d6efd', name: 'Employee' },
                'MANAGER': { color: '#198754', name: 'Manager' },
                // Default color for any unrecognized shift type
                'default': { color: '#6c757d', name: 'Employee' } // Default to Employee for any unknown type
            },
            schedule: {},
            
            // Setup shift modal handlers
            setupShiftModalHandlers() {
                // Add event listener for the Add Shift button
                const addShiftBtn = document.getElementById('addShiftBtn');
                const addShiftForm = document.getElementById('addShiftForm');
                
                if (addShiftBtn && addShiftForm) {
                    addShiftBtn.addEventListener('click', async () => {
                        // Get form data
                        const formData = new FormData(addShiftForm);
                        const selectedDate = formData.get('date');
                        const employeeId = formData.get('employeeId');
                        const position = formData.get('position');
                        const startTime = formData.get('startTime');
                        const endTime = formData.get('endTime');
                        
                        // Basic validation
                        if (!employeeId || !selectedDate || !position || !startTime || !endTime) {
                            alert('Please fill in all required fields');
                            return;
                        }
                        
                        if (startTime >= endTime) {
                            alert('Start time must be before end time');
                            return;
                        }
                        
                        // Parse date to get day of week
                        const date = new Date(selectedDate);
                        
                        // Call the Alpine.js addShift method
                        await this.addShift(employeeId, date, position, startTime, endTime);
                        
                        // Close the modal
                        const modal = bootstrap.Modal.getInstance(document.getElementById('addShiftModal'));
                        modal.hide();
                        addShiftForm.reset();
                    });
                    
                    console.log('Shift modal handlers set up');
                }
            },
            
            // Helper methods to handle employee data structure
            getEmployeeName(employee) {
                if (employee.employeeName) {
                    return employee.employeeName;
                } else if (employee.firstName && employee.lastName) {
                    return employee.firstName + ' ' + employee.lastName;
                } else if (employee.user && employee.user.firstName && employee.user.lastName) {
                    return employee.user.firstName + ' ' + employee.user.lastName;
                } else {
                    return 'Employee #' + (employee.employeeId || employee.userId || 'Unknown');
                }
            },
            
            getEmployeeInitials(employee) {
                if (employee.firstName && employee.lastName) {
                    return employee.firstName.charAt(0) + employee.lastName.charAt(0);
                } else if (employee.employeeName) {
                    const nameParts = employee.employeeName.split(' ');
                    if (nameParts.length >= 2) {
                        return nameParts[0].charAt(0) + nameParts[1].charAt(0);
                    } else if (nameParts.length === 1) {
                        return nameParts[0].charAt(0);
                    }
                } else if (employee.user && employee.user.firstName && employee.user.lastName) {
                    return employee.user.firstName.charAt(0) + employee.user.lastName.charAt(0);
                }
                return '??';
            },
            
            getEmployeeRole(employee) {
                // Check if the employee has a user object with a role
                if (employee.user && employee.user.role) {
                    return employee.user.role === 'ADMIN' ? 'MANAGER' : 'EMPLOYEE';
                }
                
                // If there's a role property directly on the employee
                if (employee.role) {
                    return employee.role === 'ADMIN' ? 'MANAGER' : 'EMPLOYEE';
                }
                
                // Default to EMPLOYEE if no role information is available
                return 'EMPLOYEE';
            },
            
            getEmployeeHours(employee) {
                // Calculate actual hours from schedule if available
                let totalHours = 0;
                const employeeId = employee.userId || employee.employeeId;
                
                if (this.schedule[employeeId]) {
                    // Create an array of all shifts to be able to analyze them in sequence
                    const allShifts = [];
                    Object.entries(this.schedule[employeeId]).forEach(([date, shifts]) => {
                        shifts.forEach(shift => {
                            allShifts.push({...shift, date});
                        });
                    });
                    
                    // Sort shifts by date and start time
                    allShifts.sort((a, b) => {
                        if (a.date !== b.date) return a.date.localeCompare(b.date);
                        return a.startTime.localeCompare(b.startTime);
                    });
                    
                    // Process shifts in order
                    for (let i = 0; i < allShifts.length; i++) {
                        const shift = allShifts[i];
                        const nextShift = i < allShifts.length - 1 ? allShifts[i + 1] : null;
                        const nextDay = nextShift && this.isConsecutiveDay(shift.date, nextShift.date);
                        
                        if (shift.startTime && shift.endTime) {
                            try {
                                // Handle special case for 24:00
                                let endHour, endMin;
                                if (shift.endTime === '24:00') {
                                    endHour = 24;
                                    endMin = 0;
                                } else {
                                    const endParts = shift.endTime.split(':');
                                    endHour = parseInt(endParts[0]);
                                    endMin = parseInt(endParts[1]);
                                }
                                
                                const startParts = shift.startTime.split(':');
                                const startHour = parseInt(startParts[0]);
                                const startMin = parseInt(startParts[1]);
                                
                                // Calculate duration in hours
                                let hours = endHour - startHour;
                                let mins = endMin - startMin;
                                
                                if (mins < 0) {
                                    hours--;
                                    mins += 60;
                                }
                                
                                // Check for consecutive overnight shifts across days
                                if (nextShift && nextDay && shift.endTime === '24:00' && nextShift.startTime === '00:00') {
                                    // Don't double count the midnight minute when shifts are consecutive across days
                                    totalHours += hours + (mins / 60) - (1/60);
                                    console.log(`Consecutive overnight shifts detected: ${shift.date} ${shift.startTime}-${shift.endTime} and ${nextShift.date} ${nextShift.startTime}-${nextShift.endTime}`);
                                } else {
                                    // Normal calculation
                                    totalHours += hours + (mins / 60);
                                }
                            } catch (e) {
                                console.error('Error calculating hours for shift:', e, shift);
                            }
                        }
                    }
                }
                
                // If no hours calculated or weeklyHours property exists, use that
                if (totalHours === 0 && employee.weeklyHours) {
                    totalHours = employee.weeklyHours;
                }
                
                // Round to 1 decimal place
                totalHours = Math.round(totalHours * 10) / 10;
                
                return totalHours + ' h/35 h';
            },
            
            async loadEmployees() {
                try {
                    const response = await fetch('/api/planning/employees', {
                        headers: {
                            'Authorization': 'Bearer ' + localStorage.getItem('jwtToken')
                        }
                    });
                    if (response.ok) {
                        const data = await response.json();
                        console.log('Employee data from API:', data);
                        
                        // Process employee data to ensure consistent structure
                        this.employees = data.map(employee => {
                            // Add logic to identify manager roles correctly
                            if (employee.user && employee.user.role) {
                                // Keep the original role but make sure it's properly normalized for display
                                if (employee.user.role === 'ADMIN' || employee.user.role === 'MANAGER') {
                                    employee.displayRole = 'MANAGER';
                                } else {
                                    employee.displayRole = 'EMPLOYEE';
                                }
                            } else {
                                employee.displayRole = 'EMPLOYEE';
                            }
                            return employee;
                        });
                        
                        // Update modal dropdowns after loading employees
                        this.populateShiftModalEmployees();
                    }
                } catch (error) {
                    console.error('Error loading employees:', error);
                }
            },
            populateShiftModalEmployees() {
                const shiftEmployeeSelect = document.getElementById('shiftEmployee');
                if (shiftEmployeeSelect && this.employees) {
                    // Clear existing options except the first "Select Employee"
                    shiftEmployeeSelect.innerHTML = '<option value="">Select Employee</option>';
                    
                    // Add employees to dropdown
                    this.employees.forEach(employee => {
                        const option = document.createElement('option');
                        option.value = employee.employeeId;
                        option.textContent = employee.employeeName;
                        shiftEmployeeSelect.appendChild(option);
                    });
                }
            },
            async loadSchedule() {
                try {
                    const weekStart = this.getWeekStart(this.currentWeek);
                    const startDateStr = weekStart.toISOString().split('T')[0];
                    
                    const response = await fetch('/api/planning/week?start=' + startDateStr, {
                        headers: {
                            'Authorization': 'Bearer ' + localStorage.getItem('jwtToken')
                        }
                    });
                    if (response.ok) {
                        // Clear the schedule before loading new data
                        this.schedule = {};
                        
                        // Get the raw schedule data from the backend
                        const rawSchedule = await response.json();
                        
                        // Process the schedule data for our frontend structure
                        // The backend returns: Map<Long, Map<String, List<Map<String, Object>>>>
                        // Where Long is the employee ID, String is the date, and List contains the shifts
                        Object.entries(rawSchedule).forEach(([employeeId, employeeDates]) => {
                            this.schedule[employeeId] = {};
                            Object.entries(employeeDates).forEach(([dateStr, shifts]) => {
                                // Normalize shift types to only EMPLOYEE or MANAGER
                                const normalizedShifts = shifts.map(shift => {
                                    // Create a copy of the shift
                                    const normalizedShift = {...shift};
                                    
                                    // Normalize the shift type - strictly enforce only EMPLOYEE or MANAGER
                                    normalizedShift.type = normalizedShift.position || normalizedShift.type || 'EMPLOYEE';
                                    normalizedShift.type = normalizedShift.type === 'MANAGER' ? 'MANAGER' : 'EMPLOYEE';
                                    
                                    // Ensure each shift has a unique ID - use the ID from backend if available
                                    if (!normalizedShift.id) {
                                        normalizedShift.id = Date.now() + Math.random().toString(36).substring(2, 9);
                                    }
                                    
                                    // Log each shift's ID for debugging purposes
                                    console.log(`Processing shift for employee ${employeeId} on ${dateStr}: ID=${normalizedShift.id}, Type=${normalizedShift.type}`);
                                    
                                    return normalizedShift;
                                });
                                
                                // Always use an array for shifts to support multiple shifts per day
                                this.schedule[employeeId][dateStr] = normalizedShifts;
                                
                                console.log(`Loaded ${normalizedShifts.length} shifts for employee ${employeeId} on ${dateStr}:`, normalizedShifts);
                                
                                // Log each shift individually
                                normalizedShifts.forEach((shift, index) => {
                                    console.log(`Shift ${index + 1}:`, shift.startTime, '-', shift.endTime, '(', shift.id, ')');
                                });
                            });
                        });
                    } else {
                        console.error('Failed to load schedule:', response.status, response.statusText);
                    }
                } catch (error) {
                    console.error('Error loading schedule:', error);
                }
            },
            getWeekStart(date) {
                const d = new Date(date);
                const day = d.getDay();
                const diff = d.getDate() - day + (day === 0 ? -6 : 1);
                return new Date(d.setDate(diff));
            },
            getWeekDays() {
                const weekStart = this.getWeekStart(this.currentWeek);
                const days = [];
                for (let i = 0; i < 7; i++) {
                    const day = new Date(weekStart);
                    day.setDate(weekStart.getDate() + i);
                    days.push(day);
                }
                return days;
            },
            formatDate(date) {
                return date.toLocaleDateString('en-US', { 
                    weekday: 'short', 
                    month: 'short', 
                    day: 'numeric' 
                });
            },
            formatWeekRange() {
                const days = this.getWeekDays();
                const start = days[0];
                const end = days[6];
                return start.toLocaleDateString('en-US', { month: 'short', day: 'numeric' }) + ' – ' + end.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
            },
            previousWeek() {
                // Create a new Date object to ensure reactivity in Alpine.js
                const newDate = new Date(this.currentWeek);
                newDate.setDate(newDate.getDate() - 7);
                this.currentWeek = newDate;
                this.loadSchedule();
            },
            nextWeek() {
                // Create a new Date object to ensure reactivity in Alpine.js
                const newDate = new Date(this.currentWeek);
                newDate.setDate(newDate.getDate() + 7);
                this.currentWeek = newDate;
                this.loadSchedule();
            },
            goToToday() {
                // Set currentWeek to today to ensure proper week calculation
                this.currentWeek = new Date();
                this.loadSchedule();
            },
            toggleEmployee(employeeId) {
                const index = this.selectedEmployees.indexOf(employeeId);
                if (index > -1) {
                    this.selectedEmployees.splice(index, 1);
                } else {
                    this.selectedEmployees.push(employeeId);
                }
            },
            isEmployeeSelected(employeeId) {
                return this.selectedEmployees.includes(employeeId);
            },
            getEmployeeSchedule(employeeId, date) {
                const dateStr = date.toISOString().split('T')[0];
                if (this.schedule[employeeId] && this.schedule[employeeId][dateStr]) {
                    return this.schedule[employeeId][dateStr];
                }
                return [];
            },
            async addShift(employeeId, date, shiftType, startTime, endTime) {
                // Normalize shift type to ensure we only have EMPLOYEE or MANAGER
                let normalizedShiftType = 'EMPLOYEE'; // Default to EMPLOYEE
                
                if (shiftType === 'MANAGER') {
                    normalizedShiftType = 'MANAGER';
                }
                
                // Check if the employee has a user role and set shift type accordingly
                const employee = this.employees.find(e => (e.userId == employeeId || e.employeeId == employeeId));
                if (employee) {
                    // If the employee has a user role of ADMIN or MANAGER, set them as MANAGER type
                    if (employee.user && (employee.user.role === 'ADMIN' || employee.user.role === 'MANAGER')) {
                        normalizedShiftType = 'MANAGER';
                    } else {
                        normalizedShiftType = 'EMPLOYEE';
                    }
                }
                
                const dateStr = date.toISOString().split('T')[0];
                if (!this.schedule[employeeId]) {
                    this.schedule[employeeId] = {};
                }
                if (!this.schedule[employeeId][dateStr]) {
                    this.schedule[employeeId][dateStr] = [];
                }
                
                // Calculate weekday (1-7, where 1=Monday, 7=Sunday)
                let weekday = date.getDay(); // 0=Sunday, 1=Monday, ..., 6=Saturday
                weekday = weekday === 0 ? 7 : weekday; // Convert to 1-7 format (1=Monday, 7=Sunday)
                
                // Create a new shift directly through the API
                try {
                    const shiftData = {
                        employeeId: parseInt(employeeId),
                        date: dateStr,
                        weekday: weekday,
                        position: normalizedShiftType,
                        startTime: startTime,
                        endTime: endTime
                    };
                    
                    // Use the endpoint for adding a new shift
                    const response = await fetch('/api/planning/shifts', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json',
                            'Authorization': 'Bearer ' + localStorage.getItem('jwtToken')
                        },
                        body: JSON.stringify(shiftData)
                    });
                    
                    if (response.ok) {
                        // Store the returned ID which contains the weekday information
                        if (response.ok) {
                            const result = await response.json();
                            if (result.id) {
                                console.log('Shift created with ID:', result.id);
                            }
                        }
                        
                        // Reload the schedule to get the updated data from the server
                        await this.loadSchedule();
                    } else {
                        console.error('Failed to add shift:', await response.text());
                    }
                } catch (error) {
                    console.error('Error adding shift:', error);
                }
            },
            
            // Method to open the edit shift modal
            openEditShiftModal(shift, employee, date) {
                // Get the modal elements
                const modal = document.getElementById('editShiftModal');
                const editShiftId = document.getElementById('editShiftId');
                const editEmployeeId = document.getElementById('editEmployeeId');
                const editShiftEmployee = document.getElementById('editShiftEmployee');
                const editShiftDate = document.getElementById('editShiftDate');
                const editShiftPosition = document.getElementById('editShiftPosition');
                const editShiftStartTime = document.getElementById('editShiftStartTime');
                const editShiftEndTime = document.getElementById('editShiftEndTime');
                
                console.log('Opening edit modal for shift:', shift);
                
                // Make sure we have the weekday in the shift ID
                let shiftId = shift.id;
                if (!shiftId || typeof shiftId !== 'string' || !shiftId.includes(',')) {
                    // If the ID doesn't contain the weekday, construct a proper ID
                    if (shift.weekday) {
                        shiftId = employee.userId + ',' + shift.weekday;
                        console.log('Constructed shift ID with weekday:', shiftId);
                    }
                }
                
                // Set the values in the form - with additional logging for debugging
                editShiftId.value = shiftId;
                
                // Ensure we have a valid employee ID
                const employeeIdValue = employee.userId || employee.employeeId;
                console.log('Setting employee ID in form:', employeeIdValue, 'from employee:', employee);
                editEmployeeId.value = employeeIdValue;
                
                editShiftEmployee.value = this.getEmployeeName(employee);
                editShiftDate.value = date.toISOString().split('T')[0]; // Format as YYYY-MM-DD
                editShiftPosition.value = shift.type || 'EMPLOYEE';
                
                // Make sure start and end times are set
                if (editShiftStartTime && shift.startTime) {
                    editShiftStartTime.value = shift.startTime;
                }
                
                if (editShiftEndTime && shift.endTime) {
                    editShiftEndTime.value = shift.endTime;
                }
                
                // Double check the form values were set properly
                console.log('Edit form values set:', {
                    id: editShiftId.value,
                    employeeId: editEmployeeId.value,
                    employee: editShiftEmployee.value,
                    date: editShiftDate.value,
                    position: editShiftPosition.value,
                    startTime: editShiftStartTime ? editShiftStartTime.value : 'not set',
                    endTime: editShiftEndTime ? editShiftEndTime.value : 'not set'
                });
                
                // Setup event handlers for the buttons
                const deleteShiftBtn = document.getElementById('deleteShiftBtn');
                const saveShiftChangesBtn = document.getElementById('saveShiftChangesBtn');
                
                // Always use the shiftId we've constructed to ensure it has the weekday
                deleteShiftBtn.onclick = () => this.deleteShift(shiftId, employee.userId || employee.employeeId, date);
                saveShiftChangesBtn.onclick = () => this.updateShift();
                
                // Show the modal
                const bsModal = new bootstrap.Modal(modal);
                bsModal.show();
            },
            
            // Method to delete a shift
            async deleteShift(shiftId, employeeId, date) {
                if (confirm('Are you sure you want to delete this shift?')) {
                    try {
                        const dateStr = date instanceof Date ? date.toISOString().split('T')[0] : date;
                        
                        // Extract the weekday from the shift ID if possible
                        let weekdayValue = null;
                        
                        console.log('Raw shift ID:', shiftId);
                        
                        // Parse the ID to extract the weekday value
                        if (shiftId && typeof shiftId === 'string') {
                            const idParts = shiftId.split(',');
                            if (idParts.length > 1) {
                                weekdayValue = idParts[1].trim();
                                console.log('Found weekday value in ID:', weekdayValue);
                            }
                        } else if (shiftId && typeof shiftId === 'object') {
                            // If the shift object itself is passed
                            if (shiftId.weekday) {
                                weekdayValue = shiftId.weekday;
                                console.log('Found weekday directly in shift object:', weekdayValue);
                            } else if (shiftId.id && typeof shiftId.id === 'string' && shiftId.id.includes(',')) {
                                // Try to extract from the shift.id property
                                const idParts = shiftId.id.split(',');
                                if (idParts.length > 1) {
                                    weekdayValue = idParts[1].trim();
                                    console.log('Found weekday in shift.id property:', weekdayValue);
                                }
                            }
                        }
                        
                        if (!weekdayValue) {
                            console.error('Could not extract weekday from shift ID:', shiftId);
                            alert('Error: Could not identify the specific shift to delete.');
                            return;
                        }
                        
                        // Delete the specific shift using the explicit weekday
                        const deleteUrl = `/api/planning/shifts/${employeeId}/${dateStr}/${weekdayValue}`;
                        
                        console.log('Deleting shift with URL:', deleteUrl);
                        
                        const response = await fetch(deleteUrl, {
                            method: 'DELETE',
                            headers: {
                                'Authorization': 'Bearer ' + localStorage.getItem('jwtToken')
                            }
                        });
                        
                        if (response.ok) {
                            // Close the modal
                            const modalEl = document.getElementById('editShiftModal');
                            if (modalEl) {
                                const modal = bootstrap.Modal.getInstance(modalEl);
                                if (modal) modal.hide();
                            }
                            
                            // Reload the schedule to ensure everything is in sync
                            await this.loadSchedule();
                            
                            alert('Shift deleted successfully!');
                        } else {
                            alert('Error deleting shift. Please try again.');
                        }
                    } catch (error) {
                        console.error('Error deleting shift:', error);
                        alert('Error deleting shift. Please try again.');
                    }
                }
            },
            
            // Method to update a shift
            async updateShift() {
                const form = document.getElementById('editShiftForm');
                const formData = new FormData(form);
                
                // Validate times
                if (!validateEditShiftTimes()) {
                    return; // Stop if validation fails
                }
                
                // Get the form fields
                const shiftId = formData.get('id');
                let employeeId = formData.get('employeeId');
                const date = formData.get('date');
                const position = formData.get('position');
                const startTime = formData.get('startTime');
                const endTime = formData.get('endTime');
                
                console.log('Form data retrieved:', {
                    shiftId,
                    employeeId,
                    date,
                    position,
                    startTime,
                    endTime
                });
                
                // More robust employee ID extraction - try multiple approaches
                if (!employeeId || employeeId === '') {
                    console.log('Employee ID not found in form data, trying alternative sources');
                    
                    // Try to get from hidden field using direct ID
                    const employeeIdElem = document.getElementById('editEmployeeId');
                    if (employeeIdElem && employeeIdElem.value) {
                        employeeId = employeeIdElem.value;
                        console.log('Found employeeId in hidden field by ID:', employeeId);
                    } else {
                        // Try with querySelector as fallback
                        const employeeIdHidden = document.querySelector('#editShiftModal input[name="employeeId"]');
                        if (employeeIdHidden && employeeIdHidden.value) {
                            employeeId = employeeIdHidden.value;
                            console.log('Found employeeId with querySelector:', employeeId);
                        }
                    }
                    
                    // If still not found, try to extract from the shift ID
                    if (!employeeId && shiftId && typeof shiftId === 'string' && shiftId.includes(',')) {
                        const idParts = shiftId.split(',');
                        if (idParts.length > 0 && idParts[0]) {
                            employeeId = idParts[0].trim();
                            console.log('Extracted employeeId from shiftId:', employeeId);
                        }
                    }
                    
                    // If still not found, display error
                    if (!employeeId) {
                        console.error('Could not find employee ID from any source');
                        alert('Error: Employee ID is missing. Cannot update shift.');
                        return;
                    }
                }
                
                // Calculate weekday (1-7, where 1=Monday, 7=Sunday)
                // Parse date as local date to avoid timezone issues
                const dateParts = date.split('-');
                const dateObj = new Date(parseInt(dateParts[0]), parseInt(dateParts[1]) - 1, parseInt(dateParts[2]));
                let weekday = dateObj.getDay(); // 0=Sunday, 1=Monday, ..., 6=Saturday
                weekday = weekday === 0 ? 7 : weekday; // Convert to 1-7 format (1=Monday, 7=Sunday)
                
                const shiftData = {
                    id: shiftId,
                    employeeId: parseInt(employeeId),
                    date: date,
                    weekday: weekday,
                    position: position,
                    startTime: startTime,
                    endTime: endTime
                };
                
                console.log('Prepared shift data:', shiftData);
                
                try {
                    // Final check to ensure we have a valid employeeId and required fields
                    if (!employeeId) {
                        alert('Error: Employee ID is missing. Cannot update shift.');
                        return;
                    }
                    
                    if (!date || !startTime || !endTime || !position) {
                        alert('Error: Missing required shift information');
                        return;
                    }
                    
                    // First, delete the existing shift if it exists
                    if (shiftId) {
                        // Extract weekday from shiftId if it's in format "employeeId,weekday"
                        let weekdayValue = null;
                        
                        if (typeof shiftId === 'string' && shiftId.includes(',')) {
                            const idParts = shiftId.split(',');
                            if (idParts.length > 1) {
                                weekdayValue = idParts[1].trim();
                                console.log('Found weekday value in ID:', weekdayValue);
                            }
                        }
                        
                        // If we couldn't extract a weekday, use the calculated one
                        if (!weekdayValue) {
                            weekdayValue = weekday.toString();
                            console.log('Using calculated weekday:', weekdayValue);
                        }
                        
                        const deleteUrl = `/api/planning/shifts/${employeeId}/${date}/${weekdayValue}`;
                        console.log('Deleting shift before update with URL:', deleteUrl);
                        
                        try {
                            await fetch(deleteUrl, {
                                method: 'DELETE',
                                headers: {
                                    'Authorization': 'Bearer ' + localStorage.getItem('jwtToken')
                                }
                            });
                            console.log('Deleted existing shift before update');
                        } catch (deleteError) {
                            console.warn('Could not delete existing shift, continuing with create:', deleteError);
                        }
                    }
                    
                    // Create a new shift with updated values
                    const apiUrl = `/api/planning/shifts`;
                    console.log('Creating new shift with URL:', apiUrl, 'and data:', shiftData);
                    
                    try {
                        const response = await fetch(apiUrl, {
                            method: 'POST',
                            headers: {
                                'Content-Type': 'application/json',
                                'Authorization': 'Bearer ' + localStorage.getItem('jwtToken')
                            },
                            body: JSON.stringify(shiftData)
                        });
                        
                        if (response.ok) {
                            // Get the response data if available
                            let responseData;
                            try {
                                responseData = await response.json();
                                console.log('Shift update successful, response:', responseData);
                            } catch (e) {
                                console.log('No JSON response available');
                            }
                            
                            // Close the modal
                            const modal = bootstrap.Modal.getInstance(document.getElementById('editShiftModal'));
                            modal.hide();
                            
                            // Reload the schedule to ensure everything is in sync
                            await this.loadSchedule();
                            
                            alert('Shift updated successfully!');
                        } else {
                            const errorText = await response.text();
                            console.error('Error updating shift:', response.status, errorText);
                            alert(`Error updating shift (${response.status}). Please try again.`);
                        }
                    } catch (networkError) {
                        console.error('Network error when updating shift:', networkError);
                        alert('Network error when updating shift. Please check your connection and try again.');
                    }
                } catch (error) {
                    console.error('Error updating shift:', error);
                    alert('Error updating shift. Please try again.');
                }
            },
            isConsecutiveDay(date1, date2) {
                // Function to check if two date strings are consecutive days
                // date1 and date2 are in the format 'YYYY-MM-DD'
                try {
                    const d1 = new Date(date1);
                    const d2 = new Date(date2);
                    
                    // Check if the second date is the day after the first
                    const oneDayInMs = 24 * 60 * 60 * 1000; // One day in milliseconds
                    return (d2.getTime() - d1.getTime()) === oneDayInMs;
                } catch (e) {
                    console.error('Error comparing dates:', e, date1, date2);
                    return false;
                }
            },
        };
    }

    // Navigation functionality
    document.addEventListener('DOMContentLoaded', function() {
        const navButtons = document.querySelectorAll('.nav-btn[data-cat]');
        const sections = document.querySelectorAll('.main-card[data-cat]');

        // Time validation function for shift creation
        window.validateShiftTimes = function() {
            const startTimeInput = document.getElementById('shiftStartTime');
            const endTimeInput = document.getElementById('shiftEndTime');
            const validationMessage = document.getElementById('timeValidationMessage');
            const submitButton = document.querySelector('#addShiftForm + .modal-footer .btn-primary');
            
            // Only validate if both fields have values
            if (startTimeInput.value && endTimeInput.value) {
                if (startTimeInput.value >= endTimeInput.value) {
                    validationMessage.style.display = 'block';
                    startTimeInput.setCustomValidity('Start time must be earlier than end time');
                    endTimeInput.setCustomValidity('End time must be later than start time');
                    if (submitButton) {
                        submitButton.disabled = true;
                    }
                    return false;
                } else {
                    validationMessage.style.display = 'none';
                    startTimeInput.setCustomValidity('');
                    endTimeInput.setCustomValidity('');
                    if (submitButton) {
                        submitButton.disabled = false;
                    }
                    return true;
                }
            }
            return true;
        };
        
        // Time validation function for edit shift modal
        window.validateEditShiftTimes = function() {
            const startTimeInput = document.getElementById('editShiftStartTime');
            const endTimeInput = document.getElementById('editShiftEndTime');
            const validationMessage = document.getElementById('editTimeValidationMessage');
            const saveButton = document.getElementById('saveShiftChangesBtn');
            
            // Only validate if both fields have values
            if (startTimeInput.value && endTimeInput.value) {
                if (startTimeInput.value >= endTimeInput.value) {
                    validationMessage.style.display = 'block';
                    startTimeInput.setCustomValidity('Start time must be earlier than end time');
                    endTimeInput.setCustomValidity('End time must be later than start time');
                    if (saveButton) {
                        saveButton.disabled = true;
                    }
                    return false;
                } else {
                    validationMessage.style.display = 'none';
                    startTimeInput.setCustomValidity('');
                    endTimeInput.setCustomValidity('');
                    if (saveButton) {
                        saveButton.disabled = false;
                    }
                    return true;
                }
            }
            return true;
        };
        
        // Navigation handler
        navButtons.forEach(button => {
            button.addEventListener('click', function() {
                const category = this.getAttribute('data-cat');

                // Update active button
                navButtons.forEach(btn => btn.classList.remove('active'));
                this.classList.add('active');

                // Show/hide sections
                sections.forEach(section => {
                    if (section.getAttribute('data-cat') === category) {
                        section.classList.remove('hidden');
                    } else {
                        section.classList.add('hidden');
                    }
                });
            });
        });

        // Email concatenation for employee registration
        const emailPrefix = document.getElementById('emailPrefix');
        const emailHidden = document.getElementById('email');

        if (emailPrefix && emailHidden) {
            emailPrefix.addEventListener('input', function() {
                emailHidden.value = emailPrefix.value + '@olh.fr';
            });
        }

        // Validate edit shift times when the modal is shown
        const editShiftModal = document.getElementById('editShiftModal');
        if (editShiftModal) {
            editShiftModal.addEventListener('shown.bs.modal', function() {
                validateEditShiftTimes();
            });
        }
        
        // Add event handler for shift form submission
        const addShiftForm = document.getElementById('addShiftForm');
        if (addShiftForm) {
            addShiftForm.addEventListener('submit', async function(e) {
                e.preventDefault();
                
                // Validate times
                if (!validateShiftTimes()) {
                    return; // Stop if validation fails
                }
                
                const formData = new FormData(addShiftForm);
                const employeeId = formData.get('employeeId');
                const dateStr = formData.get('date');
                const position = formData.get('position');
                const startTime = formData.get('startTime');
                const endTime = formData.get('endTime');
                
                if (!employeeId || !dateStr || !position || !startTime || !endTime) {
                    alert('Please fill in all fields');
                    return;
                }
                
                // Calculate weekday
                const dateObj = new Date(dateStr);
                
                // Get the Alpine.js component instance
                const planningInterface = document.getElementById('planningInterface');
                if (planningInterface && planningInterface.__x) {
                    // Call the Alpine.js component's addShift method
                    await planningInterface.__x.data.addShift(employeeId, dateObj, position, startTime, endTime);
                    
                    // Close the modal
                    const modal = bootstrap.Modal.getInstance(document.getElementById('addShiftModal'));
                    modal.hide();
                } else {
                    console.error('Could not access the Alpine.js component');
                }
            });
        }
    });

    function validateShiftTimes() {
        const startTimeInput = document.getElementById('shiftStartTime');
        const endTimeInput = document.getElementById('shiftEndTime');
        const messageDiv = document.getElementById('timeValidationMessage');

        const startTime = startTimeInput.value;
        const endTime = endTimeInput.value;

        if (startTime && endTime) {
            // Special case for 24:00 which is valid as an end time
            if (endTime === '24:00') {
                // Valid time range (24:00 is valid as end time)
                messageDiv.style.display = 'none';
                endTimeInput.classList.remove('is-invalid');
                return;
            }

            const [startHours, startMinutes] = startTime.split(':').map(Number);
            const [endHours, endMinutes] = endTime.split(':').map(Number);

            // Convert times to minutes since start of day
            const startTotalMinutes = startHours * 60 + startMinutes;
            const endTotalMinutes = endHours * 60 + endMinutes;

            if (startTotalMinutes >= endTotalMinutes) {
                // Invalid time range
                messageDiv.style.display = 'block';
                endTimeInput.classList.add('is-invalid');
            } else {
                // Valid time range
                messageDiv.style.display = 'none';
                endTimeInput.classList.remove('is-invalid');
            }
        } else {
            // If either field is empty, hide the message
            messageDiv.style.display = 'none';
        }
    }

    function validateEditShiftTimes() {
        const startTimeInput = document.getElementById('editShiftStartTime');
        const endTimeInput = document.getElementById('editShiftEndTime');
        const messageDiv = document.getElementById('editTimeValidationMessage');

        const startTime = startTimeInput.value;
        const endTime = endTimeInput.value;

        if (startTime && endTime) {
            // Special case for 24:00 which is valid as an end time
            if (endTime === '24:00') {
                // Valid time range (24:00 is valid as end time)
                messageDiv.style.display = 'none';
                endTimeInput.classList.remove('is-invalid');
                return;
            }

            const [startHours, startMinutes] = startTime.split(':').map(Number);
            const [endHours, endMinutes] = endTime.split(':').map(Number);

            // Convert times to minutes since start of day
            const startTotalMinutes = startHours * 60 + startMinutes;
            const endTotalMinutes = endHours * 60 + endMinutes;

            if (startTotalMinutes >= endTotalMinutes) {
                // Invalid time range
                messageDiv.style.display = 'block';
                endTimeInput.classList.add('is-invalid');
            } else {
                // Valid time range
                messageDiv.style.display = 'none';
                endTimeInput.classList.remove('is-invalid');
            }
        } else {
            // If either field is empty, hide the message
            messageDiv.style.display = 'none';
        }
    }