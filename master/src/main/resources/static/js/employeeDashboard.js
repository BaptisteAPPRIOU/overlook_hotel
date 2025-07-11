const buttons  = document.querySelectorAll('.nav-btn');
const sections = document.querySelectorAll('.main-card');

buttons.forEach(btn => {
  btn.addEventListener('click', () => {
    // active button
    buttons.forEach(b => b.classList.remove('active'));
    btn.classList.add('active');

    // show only the matching section
    const cat = btn.dataset.cat;
    sections.forEach(sec => {
      sec.classList.toggle('hidden', sec.dataset.cat !== cat);
    });
  });
});

// Handle Add Shift form submission
document.addEventListener('DOMContentLoaded', function() {
    const addShiftForm = document.getElementById('addShiftForm');
    
    if (addShiftForm) {
        addShiftForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            
            const formData = new FormData(addShiftForm);
            const selectedDate = formData.get('date');
            const employeeId = formData.get('employeeId');
            const position = formData.get('position');
            const startTime = formData.get('startTime');
            const endTime = formData.get('endTime');
            
            // Validate required fields
            if (!employeeId) {
                alert('Please select an employee');
                return;
            }
            if (!selectedDate) {
                alert('Please select a date');
                return;
            }
            
            // Validate time comparison
            if (startTime && endTime && startTime >= endTime) {
                alert('Start time cannot be later than or equal to end time');
                return;
            }
            if (!position) {
                alert('Please select a position');
                return;
            }
            if (!startTime) {
                alert('Please enter a start time');
                return;
            }
            if (!endTime) {
                alert('Please enter an end time');
                return;
            }
            
            // Calculate weekday (1-7, where 1=Monday, 7=Sunday)
            // Parse date as local date to avoid timezone issues
            const dateParts = selectedDate.split('-');
            const date = new Date(parseInt(dateParts[0]), parseInt(dateParts[1]) - 1, parseInt(dateParts[2]));
            let weekday = date.getDay(); // 0=Sunday, 1=Monday, ..., 6=Saturday
            weekday = weekday === 0 ? 7 : weekday; // Convert to 1-7 format (1=Monday, 7=Sunday)
            
            const shiftData = {
                employeeId: parseInt(employeeId),
                date: selectedDate,
                weekday: weekday,
                position: position,
                startTime: startTime,
                endTime: endTime
            };
            
            console.log('Adding new shift:', shiftData);
            
            try {
                const response = await fetch('/api/planning/shifts', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': 'Bearer ' + localStorage.getItem('jwtToken')
                    },
                    body: JSON.stringify(shiftData)
                });
                
                const result = await response.json();
                
                if (result.success) {
                    console.log('Shift added successfully with ID:', result.id);
                    
                    // Close modal and refresh the planning view
                    const modal = bootstrap.Modal.getInstance(document.getElementById('addShiftModal'));
                    modal.hide();
                    addShiftForm.reset();
                    
                    // Refresh the planning data if Alpine.js is available
                    if (window.Alpine) {
                        const planningComponent = document.getElementById('planningInterface');
                        if (planningComponent && planningComponent._x_dataStack) {
                            planningComponent._x_dataStack[0].loadSchedule();
                        }
                    }
                    
                    alert('Shift added successfully!');
                } else {
                    alert('Error adding shift: ' + result.message);
                }
            } catch (error) {
                console.error('Error adding shift:', error);
                alert('Error adding shift. Please try again.');
            }
        });
    }
});