// Add availability slot
function addSlot() {
    const container = document.getElementById('availabilitySlots');
    const slotHtml = `
        <div class="availability-slot mb-2">
            <div class="row">
                <div class="col-4">
                    <select class="form-select form-select-sm" name="dayOfWeek" required>
                        <option value="">Day</option>
                        <option value="MONDAY">Monday</option>
                        <option value="TUESDAY">Tuesday</option>
                        <option value="WEDNESDAY">Wednesday</option>
                        <option value="THURSDAY">Thursday</option>
                        <option value="FRIDAY">Friday</option>
                        <option value="SATURDAY">Saturday</option>
                        <option value="SUNDAY">Sunday</option>
                    </select>
                </div>
                <div class="col-3">
                    <input type="time" class="form-control form-control-sm" name="startTime" required>
                </div>
                <div class="col-3">
                    <input type="time" class="form-control form-control-sm" name="endTime" required>
                </div>
                <div class="col-2">
                    <button type="button" class="btn btn-sm btn-danger" onclick="removeSlot(this)">×</button>
                </div>
            </div>
        </div>
    `;
    container.insertAdjacentHTML('beforeend', slotHtml);
}

// Remove availability slot
function removeSlot(button) {
    button.closest('.availability-slot').remove();
}

// Generate slots for interviewer
async function generateSlots(interviewerId) {
    try {
        const response = await fetch(`/api/v1/interviewers/${interviewerId}/generate-slots`, {
            method: 'POST'
        });

        if (response.ok) {
            alert('Slots generated successfully!');
        } else {
            alert('Error generating slots');
        }
    } catch (error) {
        alert('Network error: ' + error.message);
    }
}

// Edit and Delete interviewer functions
async function editInterviewer(interviewerId) {
    try {
        const response = await fetch(`/api/v1/interviewers/${interviewerId}`);
        const interviewer = await response.json();
        
        // Pre-fill the form with existing data
        document.getElementById('name').value = interviewer.name;
        document.getElementById('email').value = interviewer.email;
        document.getElementById('maxInterviewsPerWeek').value = interviewer.maxInterviewsPerWeek;
        
        // Change form to update mode
        const form = document.getElementById('interviewerForm');
        form.setAttribute('data-interviewer-id', interviewerId);
        form.querySelector('button[type="submit"]').textContent = 'Update Interviewer';
        
        alert('Form loaded with interviewer data. Modify and submit to update.');
    } catch (error) {
        alert('Error loading interviewer data: ' + error.message);
    }
}

async function deleteInterviewer(interviewerId) {
    if (!confirm('Are you sure you want to delete this interviewer? This will also delete all their slots.')) {
        return;
    }
    
    try {
        const response = await fetch(`/api/v1/interviewers/${interviewerId}`, {
            method: 'DELETE'
        });
        
        if (response.ok) {
            alert('Interviewer deleted successfully!');
            location.reload();
        } else {
            alert('Error deleting interviewer');
        }
    } catch (error) {
        alert('Network error: ' + error.message);
    }
}

// Form submission handler
document.getElementById('interviewerForm').addEventListener('submit', async function(e) {
    e.preventDefault();
    
    const formData = new FormData(this);
    const availabilitySlots = [];
    
    // Collect availability slots
    const slots = document.querySelectorAll('.availability-slot');
    slots.forEach(slot => {
        const dayOfWeek = slot.querySelector('[name="dayOfWeek"]').value;
        const startTime = slot.querySelector('[name="startTime"]').value;
        const endTime = slot.querySelector('[name="endTime"]').value;
        
        if (dayOfWeek && startTime && endTime) {
            availabilitySlots.push({
                dayOfWeek: dayOfWeek,
                startTime: startTime,
                endTime: endTime
            });
        }
    });
    
    const requestData = {
        name: formData.get('name'),
        email: formData.get('email'),
        maxInterviewsPerWeek: parseInt(formData.get('maxInterviewsPerWeek')),
        availabilitySlots: availabilitySlots
    };

    const interviewerId = this.getAttribute('data-interviewer-id');
    const isUpdate = !!interviewerId;
    
    try {
        const response = await fetch(isUpdate ? `/api/v1/interviewers/${interviewerId}` : '/api/v1/interviewers', {
            method: isUpdate ? 'PUT' : 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(requestData)
        });

        if (response.ok) {
            alert(`Interviewer ${isUpdate ? 'updated' : 'created'} successfully!`);
            location.reload();
        } else {
            const error = await response.json();
            alert('Error: ' + error.message);
        }
    } catch (error) {
        alert('Network error: ' + error.message);
    }
});

// View slots for interviewer
function viewSlots(interviewerId) {
    window.open(`/api/v1/interview-slots/interviewer/${interviewerId}`, '_blank');
}