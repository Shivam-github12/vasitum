// View slot details
async function viewSlotDetails(slotId) {
    try {
        const response = await fetch(`/api/v1/interview-slots/${slotId}`);
        const slot = await response.json();
        
        alert(`Slot Details:\\n` +
              `Interviewer: ${slot.interviewerName}\\n` +
              `Date: ${new Date(slot.startTime).toLocaleDateString()}\\n` +
              `Time: ${new Date(slot.startTime).toLocaleTimeString()} - ${new Date(slot.endTime).toLocaleTimeString()}\\n` +
              `Status: ${slot.status}`);
    } catch (error) {
        console.error('Error loading slot details:', error);
        alert('Error loading slot details');
    }
}

// Auto-refresh available slots every 30 seconds
setInterval(() => {
    location.reload();
}, 30000);