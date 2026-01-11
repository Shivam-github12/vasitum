// Load booked slots on page load
document.addEventListener('DOMContentLoaded', function() {
    loadBookedSlots();
});

// Load all booked slots
async function loadBookedSlots() {
    const container = document.getElementById('bookedSlotsContainer');
    container.innerHTML = '<div class="text-center"><div class="spinner-border" role="status"></div><p>Loading booked slots...</p></div>';
    
    try {
        // Get all interviewers first
        const interviewersResponse = await fetch('/api/v1/interviewers');
        if (!interviewersResponse.ok) {
            throw new Error('Failed to fetch interviewers');
        }
        const interviewers = await interviewersResponse.json();
        
        if (interviewers.length === 0) {
            container.innerHTML = '<div class="alert alert-info"><h4>No Interviewers Found</h4><p>Please add interviewers first in the admin panel.</p></div>';
            return;
        }
        
        let allSlots = [];
        
        // Get slots for each interviewer
        for (const interviewer of interviewers) {
            try {
                const slotsResponse = await fetch(`/api/v1/interview-slots/interviewer/${interviewer.id}`);
                if (slotsResponse.ok) {
                    const slots = await slotsResponse.json();
                    allSlots = allSlots.concat(slots);
                }
            } catch (error) {
                console.error(`Error loading slots for interviewer ${interviewer.id}:`, error);
            }
        }
        
        const bookedSlots = allSlots.filter(slot => slot.status === 'BOOKED');
        displaySlots(bookedSlots, 'Booked Slots');
        
    } catch (error) {
        console.error('Error loading booked slots:', error);
        container.innerHTML = '<div class="alert alert-danger"><h4>Error Loading Slots</h4><p>Please try refreshing the page.</p></div>';
    }
}

// Display slots
function displaySlots(slots, title) {
    const container = document.getElementById('bookedSlotsContainer');
    
    if (slots.length === 0) {
        container.innerHTML = `
            <div class="alert alert-info">
                <h4>${title}</h4>
                <p>No booked slots found. Book some slots first!</p>
                <a href="/" class="btn btn-primary">View Available Slots</a>
            </div>
        `;
        return;
    }
    
    let html = `<h4>${title} (${slots.length})</h4><div class="row">`;
    
    slots.forEach(slot => {
        html += `
            <div class="col-md-6 mb-3">
                <div class="card">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-start">
                            <h6 class="card-title">${slot.interviewerName}</h6>
                            <span class="badge bg-success">Booked</span>
                        </div>
                        <p class="card-text">
                            <strong>Date:</strong> ${new Date(slot.startTime).toLocaleDateString()}<br>
                            <strong>Time:</strong> ${new Date(slot.startTime).toLocaleTimeString()} - ${new Date(slot.endTime).toLocaleTimeString()}
                        </p>
                        <p><strong>Candidate:</strong> ${slot.candidateName}<br>
                        <strong>Email:</strong> ${slot.candidateEmail}</p>
                        <button class="btn btn-sm btn-danger" onclick="cancelBooking(${slot.id})">
                            Cancel Booking
                        </button>
                    </div>
                </div>
            </div>
        `;
    });
    
    html += '</div>';
    container.innerHTML = html;
}

// Cancel booking
async function cancelBooking(slotId) {
    if (!confirm('Are you sure you want to cancel this booking?')) {
        return;
    }
    
    try {
        const response = await fetch(`/api/v1/interview-slots/${slotId}/cancel`, {
            method: 'DELETE'
        });
        
        if (response.ok) {
            alert('Booking cancelled successfully!');
            loadBookedSlots();
        } else {
            alert('Error cancelling booking');
        }
    } catch (error) {
        alert('Network error: ' + error.message);
    }
}

// Load all slots
async function loadAllSlots() {
    const container = document.getElementById('bookedSlotsContainer');
    container.innerHTML = '<div class="text-center"><div class="spinner-border" role="status"></div><p>Loading all slots...</p></div>';
    
    try {
        const interviewersResponse = await fetch('/api/v1/interviewers');
        const interviewers = await interviewersResponse.json();
        
        let allSlots = [];
        for (const interviewer of interviewers) {
            try {
                const slotsResponse = await fetch(`/api/v1/interview-slots/interviewer/${interviewer.id}`);
                if (slotsResponse.ok) {
                    const slots = await slotsResponse.json();
                    allSlots = allSlots.concat(slots);
                }
            } catch (error) {
                console.error(`Error loading slots for interviewer ${interviewer.id}:`, error);
            }
        }
        
        displaySlots(allSlots, 'All Slots');
    } catch (error) {
        console.error('Error loading all slots:', error);
        container.innerHTML = '<div class="alert alert-danger">Error loading slots. Please try again.</div>';
    }
}